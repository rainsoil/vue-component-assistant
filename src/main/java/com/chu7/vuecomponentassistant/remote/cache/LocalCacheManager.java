package com.chu7.vuecomponentassistant.remote.cache;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.OfficialLibrary;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存管理器
 */
public class LocalCacheManager {
    private static final Logger LOG = Logger.getInstance(LocalCacheManager.class);
    private static final String CACHE_DIR = "vuekit_cache";
    private static final String LIBRARIES_FILE = "libraries.json";
    private static final String OFFICIAL_LIBRARIES_FILE = "official_libraries.json";
    private static final String COMPONENT_LIBRARY_PREFIX = "library_";
    private static final String COMPONENT_LIBRARY_SUFFIX = ".json";
    
    private final Path cacheDir;
    private final Map<String, ComponentLibrary> memoryCache;
    private final Map<String, OfficialLibrary> officialLibrariesCache;
    
    public LocalCacheManager() {
        this.cacheDir = getCacheDirectory();
        this.memoryCache = new ConcurrentHashMap<>();
        this.officialLibrariesCache = new ConcurrentHashMap<>();
        ensureCacheDirectoryExists();
    }
    
    /**
     * 获取缓存目录
     */
    private Path getCacheDirectory() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".vuekit", CACHE_DIR);
    }
    
    /**
     * 确保缓存目录存在
     */
    private void ensureCacheDirectoryExists() {
        try {
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                LOG.info("创建缓存目录: " + cacheDir);
            }
        } catch (IOException e) {
            LOG.error("创建缓存目录失败", e);
        }
    }
    
    /**
     * 保存组件库到缓存
     */
    public void saveLibrary(ComponentLibrary library) throws VueKitException {
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
            LOG.error("保存组件库到缓存失败: " + library.getName(), e);
            throw new VueKitException("保存组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 从缓存加载组件库
     */
    public ComponentLibrary loadLibrary(String libraryId) throws VueKitException {
        // 先检查内存缓存
        if (memoryCache.containsKey(libraryId)) {
            return memoryCache.get(libraryId);
        }
        
        try {
            String fileName = COMPONENT_LIBRARY_PREFIX + libraryId + COMPONENT_LIBRARY_SUFFIX;
            Path filePath = cacheDir.resolve(fileName);
            
            if (!Files.exists(filePath)) {
                return null;
            }
            
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            ComponentLibrary library = convertJsonToLibrary(json);
            
            // 更新内存缓存
            memoryCache.put(libraryId, library);
            
            LOG.info("从缓存加载组件库: " + library.getName());
            return library;
        } catch (IOException e) {
            LOG.error("从缓存加载组件库失败: " + libraryId, e);
            throw new VueKitException("加载组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有缓存的组件库
     */
    public List<ComponentLibrary> getAllLibraries() {
        List<ComponentLibrary> libraries = new ArrayList<>();
        
        try {
            File[] files = cacheDir.toFile().listFiles((dir, name) -> 
                name.startsWith(COMPONENT_LIBRARY_PREFIX) && name.endsWith(COMPONENT_LIBRARY_SUFFIX));
            
            if (files != null) {
                for (File file : files) {
                    try {
                        String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                        ComponentLibrary library = convertJsonToLibrary(json);
                        libraries.add(library);
                        memoryCache.put(library.getId(), library);
                    } catch (Exception e) {
                        LOG.warn("加载缓存文件失败: " + file.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("获取所有组件库失败", e);
        }
        
        return libraries;
    }
    
    /**
     * 删除组件库缓存
     */
    public boolean removeLibrary(String libraryId) {
        try {
            String fileName = COMPONENT_LIBRARY_PREFIX + libraryId + COMPONENT_LIBRARY_SUFFIX;
            Path filePath = cacheDir.resolve(fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                memoryCache.remove(libraryId);
                LOG.info("删除组件库缓存: " + libraryId);
                return true;
            }
        } catch (IOException e) {
            LOG.error("删除组件库缓存失败: " + libraryId, e);
        }
        
        return false;
    }
    
    /**
     * 保存官方组件库列表
     */
    public void saveOfficialLibraries(List<OfficialLibrary> libraries) throws VueKitException {
        try {
            Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
            String json = convertOfficialLibrariesToJson(libraries);
            Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
            
            // 更新内存缓存
            officialLibrariesCache.clear();
            for (OfficialLibrary library : libraries) {
                officialLibrariesCache.put(library.getId(), library);
            }
            
            LOG.info("保存官方组件库列表，数量: " + libraries.size());
        } catch (IOException e) {
            LOG.error("保存官方组件库列表失败", e);
            throw new VueKitException("保存官方组件库列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载官方组件库列表
     */
    public List<OfficialLibrary> loadOfficialLibraries() {
        try {
            Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
            
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }
            
            String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            List<OfficialLibrary> libraries = convertJsonToOfficialLibraries(json);
            
            // 更新内存缓存
            officialLibrariesCache.clear();
            for (OfficialLibrary library : libraries) {
                officialLibrariesCache.put(library.getId(), library);
            }
            
            LOG.info("加载官方组件库列表，数量: " + libraries.size());
            return libraries;
        } catch (Exception e) {
            LOG.error("加载官方组件库列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 清除官方组件库缓存
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
            LOG.error("清除官方组件库缓存失败", e);
        }
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        try {
            long currentTime = System.currentTimeMillis();
            long maxAge = 7 * 24 * 60 * 60 * 1000L; // 7天
            
            File[] files = cacheDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (currentTime - file.lastModified() > maxAge) {
                        Files.delete(file.toPath());
                        LOG.info("删除过期缓存文件: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("清理过期缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
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
            stats.put("memoryCacheSize", memoryCache.size());
            stats.put("officialLibrariesCacheSize", officialLibrariesCache.size());
            
        } catch (Exception e) {
            LOG.error("获取缓存统计信息失败", e);
        }
        
        return stats;
    }
    
    // JSON序列化/反序列化方法
    private String convertLibraryToJson(ComponentLibrary library) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(library);
        } catch (Exception e) {
            LOG.error("序列化组件库失败: " + library.getName(), e);
            throw new RuntimeException("序列化组件库失败: " + e.getMessage());
        }
    }
    
    private ComponentLibrary convertJsonToLibrary(String json) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, ComponentLibrary.class);
        } catch (Exception e) {
            LOG.error("反序列化组件库失败", e);
            throw new RuntimeException("反序列化组件库失败: " + e.getMessage());
        }
    }
    
    private String convertOfficialLibrariesToJson(List<OfficialLibrary> libraries) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(libraries);
        } catch (Exception e) {
            LOG.error("序列化官方组件库列表失败", e);
            throw new RuntimeException("序列化官方组件库列表失败: " + e.getMessage());
        }
    }
    
    private List<OfficialLibrary> convertJsonToOfficialLibraries(String json) {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (Exception e) {
            LOG.error("反序列化官方组件库列表失败", e);
            return new ArrayList<>();
        }
    }
} 