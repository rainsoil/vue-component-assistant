package com.chu7.vuecomponentassistant.remote.cache;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.OfficialLibrary;
import com.chu7.vuecomponentassistant.utils.ErrorHandler;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存管理器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供组件库和官方组件库的本地缓存功能</li>
 *   <li>支持内存缓存和磁盘缓存的双层存储</li>
 *   <li>管理缓存的过期清理和统计信息</li>
 *   <li>提供线程安全的缓存访问机制</li>
 *   <li>支持JSON序列化和反序列化</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>双层缓存：内存缓存提供快速访问，磁盘缓存确保数据持久化</li>
 *   <li>线程安全：使用ConcurrentHashMap确保多线程环境下的安全性</li>
 *   <li>自动过期：支持配置缓存过期时间，自动清理过期数据</li>
 *   <li>错误容错：完善的异常处理和错误恢复机制</li>
 *   <li>性能优化：优先从内存缓存读取，减少磁盘I/O操作</li>
 * </ul>
 * 
 * <p>缓存策略：</p>
 * <ol>
 *   <li>内存缓存：使用ConcurrentHashMap存储最近访问的组件库</li>
 *   <li>磁盘缓存：持久化存储到用户主目录的.vuekit/cache目录</li>
 *   <li>过期清理：自动清理超过7天的过期缓存文件</li>
 *   <li>统计监控：提供详细的缓存使用情况统计</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库的本地存储和快速访问</li>
 *   <li>官方组件库列表的缓存管理</li>
 *   <li>离线环境下的组件库访问</li>
 *   <li>缓存性能监控和优化</li>
 *   <li>磁盘空间管理和清理</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary
 * @see com.chu7.vuecomponentassistant.remote.model.OfficialLibrary
 * @see com.chu7.vuecomponentassistant.exceptions.VueKitException
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class LocalCacheManager {
    
    /**
     * 日志记录器
     * 用于记录缓存管理过程中的关键信息和错误
     */
    private static final Logger LOG = Logger.getInstance(LocalCacheManager.class);
    
    /**
     * 缓存目录名称
     * 在用户主目录下创建的缓存文件夹名称
     */
    private static final String CACHE_DIR = "vuekit_cache";
    
    /**
     * 组件库列表文件名
     * 存储所有组件库信息的JSON文件名
     */
    private static final String LIBRARIES_FILE = "libraries.json";
    
    /**
     * 官方组件库列表文件名
     * 存储官方组件库信息的JSON文件名
     */
    private static final String OFFICIAL_LIBRARIES_FILE = "official_libraries.json";
    
    /**
     * 组件库文件前缀
     * 单个组件库缓存文件的前缀标识
     */
    private static final String COMPONENT_LIBRARY_PREFIX = "library_";
    
    /**
     * 组件库文件后缀
     * 单个组件库缓存文件的后缀标识
     */
    private static final String COMPONENT_LIBRARY_SUFFIX = ".json";
    
    /**
     * 缓存过期时间
     * 缓存文件的有效期，默认为7天（毫秒）
     */
    private static final long CACHE_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * 缓存目录路径
     * 指向用户主目录下的.vuekit/cache目录
     */
    private final Path cacheDir;
    
    /**
     * 组件库内存缓存
     * 使用ConcurrentHashMap存储组件库ID到ComponentLibrary对象的映射
     */
    private final Map<String, ComponentLibrary> memoryCache;
    
    /**
     * 官方组件库内存缓存
     * 使用ConcurrentHashMap存储官方组件库ID到OfficialLibrary对象的映射
     */
    private final Map<String, OfficialLibrary> officialLibrariesCache;
    
    /**
     * 构造函数
     * 
     * <p>初始化本地缓存管理器，创建必要的缓存目录和内存缓存容器。</p>
     * 
     * <p>初始化流程：</p>
     * <ol>
     *   <li>获取缓存目录路径</li>
     *   <li>创建内存缓存容器</li>
     *   <li>确保缓存目录存在</li>
     * </ol>
     * 
     * <p>缓存容器：</p>
     * <ul>
     *   <li>memoryCache：组件库的内存缓存</li>
     *   <li>officialLibrariesCache：官方组件库的内存缓存</li>
     * </ul>
     * 
     * @see #getCacheDirectory()
     * @see #ensureCacheDirectoryExists()
     */
    public LocalCacheManager() {
        this.cacheDir = getCacheDirectory();
        this.memoryCache = new ConcurrentHashMap<>();
        this.officialLibrariesCache = new ConcurrentHashMap<>();
        ensureCacheDirectoryExists();
    }
    
    /**
     * 获取缓存目录
     * 
     * <p>该方法返回缓存目录的完整路径，默认位置在用户主目录下的.vuekit/cache目录。</p>
     * 
     * <p>目录结构：</p>
     * <ul>
     *   <li>Windows: C:\Users\{username}\.vuekit\vuekit_cache</li>
     *   <li>macOS/Linux: /home/{username}/.vuekit/vuekit_cache</li>
     * </ul>
     * 
     * @return 缓存目录的Path对象
     * 
     * @see java.nio.file.Paths
     * @see java.lang.System#getProperty(String)
     */
    private Path getCacheDirectory() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".vuekit", CACHE_DIR);
    }
    
    /**
     * 确保缓存目录存在
     * 
     * <p>该方法检查并创建缓存目录，如果目录不存在则创建完整的目录结构。
     * 确保后续的缓存操作能够正常进行。</p>
     * 
     * <p>创建流程：</p>
     * <ol>
     *   <li>检查缓存目录是否存在</li>
     *   <li>如果不存在，创建完整的目录结构</li>
     *   <li>记录创建结果到日志</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获IOException并记录错误日志</li>
     *   <li>使用ErrorHandler统一处理异常</li>
     *   <li>不影响管理器的正常初始化</li>
     * </ul>
     * 
     * @see java.nio.file.Files#createDirectories(Path)
     * @see com.chu7.vuecomponentassistant.utils.ErrorHandler#handleException(String, Exception, boolean)
     */
    private void ensureCacheDirectoryExists() {
        try {
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                LOG.info("创建缓存目录: " + cacheDir);
            }
        } catch (IOException e) {
            String errorMsg = "创建缓存目录失败: " + cacheDir;
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
    }
    
    /**
     * 保存组件库到缓存
     * 
     * <p>该方法将组件库同时保存到内存缓存和磁盘缓存，确保数据的持久化和快速访问。
     * 支持完整的错误处理和参数验证。</p>
     * 
     * <p>保存流程：</p>
     * <ol>
     *   <li>验证组件库对象的有效性</li>
     *   <li>生成缓存文件名</li>
     *   <li>序列化为JSON并写入磁盘</li>
     *   <li>更新内存缓存</li>
     *   <li>记录操作日志</li>
     * </ol>
     * 
     * <p>参数验证：</p>
     * <ul>
     *   <li>组件库对象不能为null</li>
     *   <li>组件库ID不能为null</li>
     *   <li>组件库名称不能为null</li>
     * </ul>
     * 
     * @param library 要保存的组件库，不能为null
     * @throws VueKitException 当保存操作失败时抛出，包含详细的错误信息
     * @throws IllegalArgumentException 当library为null时抛出
     * 
     * @see #convertLibraryToJson(ComponentLibrary)
     * @see java.nio.file.Files#write(Path, byte[])
     * @see java.util.Objects#requireNonNull(Object, String)
     */
    public void saveLibrary(ComponentLibrary library) throws VueKitException {
        Objects.requireNonNull(library, "组件库不能为null");
        Objects.requireNonNull(library.getId(), "组件库ID不能为null");
        Objects.requireNonNull(library.getName(), "组件库名称不能为null");
        
        try {
            String fileName = COMPONENT_LIBRARY_PREFIX + library.getId() + COMPONENT_LIBRARY_SUFFIX;
            Path filePath = cacheDir.resolve(fileName);
            
            // 转换为JSON并保存
            String json = convertLibraryToJson(library);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
            
            // 更新内存缓存
            memoryCache.put(library.getId(), library);
            
            LOG.info("保存组件库到缓存: " + library.getName() + " -> " + filePath);
        } catch (IOException e) {
            String errorMsg = "保存组件库到缓存失败: " + library.getName();
            LOG.error(errorMsg, e);
            throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
        } catch (Exception e) {
            String errorMsg = "保存组件库时发生未知错误: " + library.getName();
            LOG.error(errorMsg, e);
            throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 从缓存加载组件库
     * 
     * <p>该方法优先从内存缓存加载组件库，如果内存缓存中没有，则从磁盘缓存加载。
     * 实现了缓存的分层访问策略，提高访问性能。</p>
     * 
     * <p>加载策略：</p>
     * <ol>
     *   <li>首先检查内存缓存</li>
     *   <li>如果内存缓存中没有，从磁盘加载</li>
     *   <li>加载成功后更新内存缓存</li>
     *   <li>返回加载的组件库对象</li>
     * </ol>
     * 
     * <p>性能优化：</p>
     * <ul>
     *   <li>内存缓存优先，减少磁盘I/O</li>
     *   <li>加载后自动更新内存缓存</li>
     *   <li>支持并发访问，线程安全</li>
     * </ul>
     * 
     * @param libraryId 组件库ID，不能为null或空字符串
     * @return 加载的组件库对象，如果不存在则返回null
     * @throws VueKitException 当加载操作失败时抛出，包含详细的错误信息
     * @throws IllegalArgumentException 当libraryId为null或空字符串时抛出
     * 
     * @see #convertJsonToLibrary(String)
     * @see java.nio.file.Files#readAllBytes(Path)
     * @see java.lang.String#trim()
     */
    public ComponentLibrary loadLibrary(String libraryId) throws VueKitException {
        if (libraryId == null || libraryId.trim().isEmpty()) {
            throw new IllegalArgumentException("组件库ID不能为null或空字符串");
        }
        
        // 先检查内存缓存
        if (memoryCache.containsKey(libraryId)) {
            LOG.debug("从内存缓存加载组件库: " + libraryId);
            return memoryCache.get(libraryId);
        }
        
        try {
            String fileName = COMPONENT_LIBRARY_PREFIX + libraryId + COMPONENT_LIBRARY_SUFFIX;
            Path filePath = cacheDir.resolve(fileName);
            
            if (!Files.exists(filePath)) {
                LOG.debug("组件库缓存文件不存在: " + libraryId);
                return null;
            }
            
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            ComponentLibrary library = convertJsonToLibrary(json);
            
            // 更新内存缓存
            memoryCache.put(libraryId, library);
            
            LOG.info("从磁盘缓存加载组件库: " + library.getName());
            return library;
        } catch (IOException e) {
            String errorMsg = "从缓存加载组件库失败: " + libraryId;
            LOG.error(errorMsg, e);
            throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
        } catch (Exception e) {
            String errorMsg = "加载组件库时发生未知错误: " + libraryId;
            LOG.error(errorMsg, e);
            throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有缓存的组件库
     * 
     * <p>该方法扫描磁盘缓存目录，加载所有可用的组件库到内存缓存。
     * 提供详细的日志记录，便于调试和监控。</p>
     * 
     * <p>扫描流程：</p>
     * <ol>
     *   <li>扫描缓存目录下的所有组件库文件</li>
     *   <li>逐个读取和解析JSON文件</li>
     *   <li>转换为ComponentLibrary对象</li>
     *   <li>更新内存缓存</li>
     *   <li>返回完整的组件库列表</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>单个文件失败不影响其他文件</li>
     *   <li>损坏的文件会被跳过并记录警告</li>
     *   <li>使用VueKitLogger记录详细信息</li>
     * </ul>
     * 
     * @return 所有缓存的组件库列表，如果没有则返回空列表
     * 
     * @see #convertJsonToLibrary(String)
     * @see com.chu7.vuecomponentassistant.utils.VueKitLogger#logAndIgnore(Logger, String, Exception)
     * @see java.io.File#listFiles(FileFilter)
     */
    public List<ComponentLibrary> getAllLibraries() {
        LOG.info("=== LocalCacheManager.getAllLibraries() 开始 ===");
        List<ComponentLibrary> libraries = new ArrayList<>();
        
        try {
            LOG.info("缓存目录: " + cacheDir);
            File[] files = cacheDir.toFile().listFiles((dir, name) -> 
                name.startsWith(COMPONENT_LIBRARY_PREFIX) && name.endsWith(COMPONENT_LIBRARY_SUFFIX));
            
            LOG.info("找到缓存文件数量: " + (files != null ? files.length : 0));
            
            if (files != null) {
                for (File file : files) {
                    LOG.info("处理缓存文件: " + file.getName());
                    try {
                        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                        LOG.info("文件内容长度: " + json.length() + " 字符");
                        LOG.info("文件内容前100字符: " + json.substring(0, Math.min(100, json.length())));
                        
                        ComponentLibrary library = convertJsonToLibrary(json);
                        if (library != null) {
                            libraries.add(library);
                            memoryCache.put(library.getId(), library);
                            LOG.info("成功加载组件库: " + library.getName() + " (ID: " + library.getId() + ")");
                        } else {
                            LOG.warn("转换组件库失败: " + file.getName());
                        }
                    } catch (Exception e) {
                        LOG.warn("加载缓存文件失败: " + file.getName(), e);
                        VueKitLogger.logAndIgnore(LOG, "跳过损坏的缓存文件: " + file.getName(), e);
                    }
                }
            }
            
            LOG.info("成功加载 " + libraries.size() + " 个组件库到内存缓存");
            LOG.info("加载的组件库列表:");
            for (ComponentLibrary library : libraries) {
                LOG.info("- " + library.getName() + " (ID: " + library.getId() + ", 描述: " + library.getDescription() + ")");
            }
        } catch (Exception e) {
            String errorMsg = "获取所有组件库失败";
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
        
        LOG.info("=== LocalCacheManager.getAllLibraries() 结束，返回 " + libraries.size() + " 个组件库 ===");
        return libraries;
    }
    
    /**
     * 删除组件库缓存
     * 
     * 同时删除磁盘缓存文件和内存缓存项。
     * 
     * @param libraryId 要删除的组件库ID，不能为null
     * @return 如果删除成功返回true，否则返回false
     * @throws IllegalArgumentException 当libraryId为null时抛出
     */
    public boolean removeLibrary(String libraryId) {
        Objects.requireNonNull(libraryId, "组件库ID不能为null");
        
        try {
            String fileName = COMPONENT_LIBRARY_PREFIX + libraryId + COMPONENT_LIBRARY_SUFFIX;
            Path filePath = cacheDir.resolve(fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                memoryCache.remove(libraryId);
                LOG.info("删除组件库缓存: " + libraryId);
                return true;
            } else {
                LOG.debug("组件库缓存文件不存在: " + libraryId);
                return false;
            }
        } catch (IOException e) {
            String errorMsg = "删除组件库缓存失败: " + libraryId;
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
            return false;
        }
    }
    
    /**
     * 保存官方组件库列表
     * 
     * 将官方组件库列表保存到磁盘缓存，并更新内存缓存。
     * 
     * @param libraries 官方组件库列表，不能为null
     * @throws VueKitException 当保存操作失败时抛出
     * @throws IllegalArgumentException 当libraries为null时抛出
     */
    public void saveOfficialLibraries(List<OfficialLibrary> libraries) throws VueKitException {
        Objects.requireNonNull(libraries, "官方组件库列表不能为null");
        
        try {
            Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
            String json = convertOfficialLibrariesToJson(libraries);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
            
            // 更新内存缓存
            officialLibrariesCache.clear();
            for (OfficialLibrary library : libraries) {
                if (library != null && library.getId() != null) {
                    officialLibrariesCache.put(library.getId(), library);
                }
            }
            
            LOG.info("保存官方组件库列表，数量: " + libraries.size());
        } catch (IOException e) {
            String errorMsg = "保存官方组件库列表失败";
            LOG.error(errorMsg, e);
            throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
        } catch (Exception e) {
            String errorMsg = "保存官方组件库列表时发生未知错误";
            LOG.error(errorMsg, e);
            throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载官方组件库列表
     * 
     * 从磁盘缓存加载官方组件库列表，并更新内存缓存。
     * 
     * @return 官方组件库列表，如果加载失败则返回空列表
     */
    public List<OfficialLibrary> loadOfficialLibraries() {
        try {
            Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
            
            if (!Files.exists(filePath)) {
                LOG.debug("官方组件库缓存文件不存在");
                return new ArrayList<>();
            }
            
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            List<OfficialLibrary> libraries = convertJsonToOfficialLibraries(json);
            
            // 更新内存缓存
            officialLibrariesCache.clear();
            for (OfficialLibrary library : libraries) {
                if (library != null && library.getId() != null) {
                    officialLibrariesCache.put(library.getId(), library);
                }
            }
            
            LOG.info("加载官方组件库列表，数量: " + libraries.size());
            return libraries;
        } catch (Exception e) {
            String errorMsg = "加载官方组件库列表失败";
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
            return new ArrayList<>();
        }
    }
    
    /**
     * 清除官方组件库缓存
     * 
     * 删除磁盘缓存文件并清空内存缓存。
     */
    public void clearOfficialLibrariesCache() {
        try {
            Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                LOG.info("删除官方组件库缓存文件");
            }
            
            // 清除内存缓存
            officialLibrariesCache.clear();
            LOG.info("清除官方组件库内存缓存");
            
        } catch (Exception e) {
            String errorMsg = "清除官方组件库缓存失败";
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
    }
    
    /**
     * 清理过期缓存
     * 
     * 删除超过指定时间的缓存文件，释放磁盘空间。
     * 默认过期时间为7天。
     */
    public void cleanExpiredCache() {
        try {
            long currentTime = System.currentTimeMillis();
            int deletedCount = 0;
            
            File[] files = cacheDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (currentTime - file.lastModified() > CACHE_EXPIRY_TIME) {
                        Files.delete(file.toPath());
                        deletedCount++;
                        LOG.info("删除过期缓存文件: " + file.getName());
                    }
                }
            }
            
            LOG.info("缓存清理完成，删除了 " + deletedCount + " 个过期文件");
        } catch (Exception e) {
            String errorMsg = "清理过期缓存失败";
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * 提供缓存使用情况的详细统计信息，包括文件数量、总大小等。
     * 
     * @return 包含缓存统计信息的Map
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            File[] files = cacheDir.toFile().listFiles();
            int totalFiles = files != null ? files.length : 0;
            long totalSize = 0;
            
            if (files != null) {
                for (File file : files) {
                    totalSize += file.length();
                }
            }
            
            stats.put("cacheDirectory", cacheDir.toString());
            stats.put("totalFiles", totalFiles);
            stats.put("totalSizeBytes", totalSize);
            stats.put("totalSizeMB", String.format("%.2f", totalSize / (1024.0 * 1024.0)));
            stats.put("memoryCacheSize", memoryCache.size());
            stats.put("officialLibrariesCacheSize", officialLibrariesCache.size());
            stats.put("lastCleanupTime", System.currentTimeMillis());
            
            LOG.debug("获取缓存统计信息: " + stats);
        } catch (Exception e) {
            String errorMsg = "获取缓存统计信息失败";
            LOG.error(errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
        
        return stats;
    }
    
    /**
     * 强制刷新内存缓存
     * 
     * 清空内存缓存，强制下次访问时从磁盘重新加载。
     */
    public void refreshMemoryCache() {
        memoryCache.clear();
        officialLibrariesCache.clear();
        LOG.info("内存缓存已刷新");
    }
    
    // JSON序列化/反序列化方法
    /**
     * 将组件库转换为JSON字符串
     * 
     * @param library 要转换的组件库
     * @return JSON字符串
     * @throws RuntimeException 当序列化失败时抛出
     */
    private String convertLibraryToJson(ComponentLibrary library) {
        Objects.requireNonNull(library, "组件库不能为null");
        
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(library);
        } catch (Exception e) {
            String errorMsg = "序列化组件库失败: " + library.getName();
            LOG.error(errorMsg, e);
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JSON字符串转换为组件库对象
     * 
     * @param json JSON字符串
     * @return 组件库对象
     * @throws RuntimeException 当反序列化失败时抛出
     */
    private ComponentLibrary convertJsonToLibrary(String json) {
        Objects.requireNonNull(json, "JSON字符串不能为null");
        
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, ComponentLibrary.class);
        } catch (Exception e) {
            String errorMsg = "反序列化组件库失败";
            LOG.error(errorMsg, e);
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 将官方组件库列表转换为JSON字符串
     * 
     * @param libraries 官方组件库列表
     * @return JSON字符串
     * @throws RuntimeException 当序列化失败时抛出
     */
    private String convertOfficialLibrariesToJson(List<OfficialLibrary> libraries) {
        Objects.requireNonNull(libraries, "官方组件库列表不能为null");
        
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(libraries);
        } catch (Exception e) {
            String errorMsg = "序列化官方组件库列表失败";
            LOG.error(errorMsg, e);
            throw new RuntimeException(errorMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JSON字符串转换为官方组件库列表
     * 
     * @param json JSON字符串
     * @return 官方组件库列表，如果解析失败则返回空列表
     */
    private List<OfficialLibrary> convertJsonToOfficialLibraries(String json) {
        Objects.requireNonNull(json, "JSON字符串不能为null");
        
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            String errorMsg = "反序列化官方组件库列表失败";
            LOG.error(errorMsg, e);
            return new ArrayList<>();
        }
    }
} 