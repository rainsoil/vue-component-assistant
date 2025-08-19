package com.chu7.vuecomponentassistant.remote;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ImportResult;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一组件库管理器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>统一管理所有类型的组件库（官方、远程、本地）</li>
 *   <li>提供组件库的导入、替换、删除等操作</li>
 *   <li>管理组件库的生命周期和状态</li>
 *   <li>协调缓存、远程和官方组件库管理器</li>
 *   <li>自动处理组件库来源类型的设置</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>统一接口设计，简化组件库操作</li>
 *   <li>支持多种组件库来源类型</li>
 *   <li>自动来源类型推断和设置</li>
 *   <li>线程安全的内存缓存管理</li>
 *   <li>完整的错误处理和日志记录</li>
 * </ul>
 * 
 * <p>管理策略：</p>
 * <ol>
 *   <li>导入时自动检查冲突和重复</li>
 *   <li>智能设置组件库来源类型</li>
 *   <li>自动刷新相关配置和缓存</li>
 *   <li>通知相关组件重新加载数据</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库的导入和管理</li>
 *   <li>组件库的更新和替换</li>
 *   <li>组件库的删除和清理</li>
 *   <li>组件库信息的查询和检索</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager
 * @see com.chu7.vuecomponentassistant.remote.RemoteLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.OfficialLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary
 * @see com.chu7.vuecomponentassistant.remote.model.ImportResult
 */
public class ComponentLibraryManager {
    
    /**
     * 日志记录器
     * 用于记录组件库管理过程中的关键信息和错误
     */
    private static final Logger LOG = Logger.getInstance(ComponentLibraryManager.class);
    
    /**
     * 本地缓存管理器
     * 负责组件库的本地存储和缓存管理
     */
    private final LocalCacheManager cacheManager;
    
    /**
     * 远程组件库管理器
     * 负责远程组件库的下载和管理
     */
    private final RemoteLibraryManager remoteManager;
    
    /**
     * 官方组件库管理器
     * 负责官方组件库的管理和更新
     */
    private final OfficialLibraryManager officialManager;
    
    /**
     * 已存在组件库的内存缓存
     * 使用ConcurrentHashMap确保线程安全
     */
    private final Map<String, ComponentLibrary> existingLibraries;
    
    /**
     * 构造函数
     * 
     * <p>初始化组件库管理器的所有必要组件，包括缓存管理器、
     * 远程管理器、官方管理器和内存缓存。</p>
     * 
     * <p>初始化流程：</p>
     * <ol>
     *   <li>创建本地缓存管理器实例</li>
     *   <li>创建远程组件库管理器实例</li>
     *   <li>创建官方组件库管理器实例</li>
     *   <li>创建线程安全的内存缓存</li>
     *   <li>加载已存在的组件库</li>
     * </ol>
     * 
     * @see #loadExistingLibraries()
     */
    public ComponentLibraryManager() {
        this.cacheManager = new LocalCacheManager();
        this.remoteManager = new RemoteLibraryManager();
        this.officialManager = new OfficialLibraryManager();
        this.existingLibraries = new ConcurrentHashMap<>();
        
        // 初始化时加载所有已存在的组件库
        loadExistingLibraries();
        
        // 注意：不再需要修复已存在组件库的 source 字段，因为现在通过导入上下文判断
    }
    
    /**
     * 加载已存在的组件库
     * 
     * <p>该方法在初始化时从本地缓存中加载所有已存在的组件库，
     * 并将它们添加到内存缓存中，提高后续操作的性能。</p>
     * 
     * <p>加载流程：</p>
     * <ol>
     *   <li>从本地缓存获取所有组件库</li>
     *   <li>对每个组件库名称进行标准化处理</li>
     *   <li>添加到内存缓存映射中</li>
     *   <li>记录加载结果</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>不影响管理器的正常初始化</li>
     *   <li>支持后续的动态加载</li>
     * </ul>
     * 
     * @see #normalizeLibraryName(String)
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#getAllLibraries()
     */
    private void loadExistingLibraries() {
        try {
            List<ComponentLibrary> libraries = cacheManager.getAllLibraries();
            for (ComponentLibrary library : libraries) {
                String normalizedName = normalizeLibraryName(library.getName());
                existingLibraries.put(normalizedName, library);
            }
            LOG.info("加载已存在的组件库，数量: " + libraries.size());
        } catch (Exception e) {
            LOG.error("加载已存在的组件库失败", e);
        }
    }
    
    /**
     * 导入组件库（默认来源类型）
     * 
     * <p>该方法使用默认的来源类型导入组件库。
     * 来源类型会根据组件库的sourceUrl自动推断。</p>
     * 
     * @param library 要导入的组件库，不能为null
     * @return 导入结果，包含成功、冲突或错误信息
     * @throws IllegalArgumentException 如果library为null
     * 
     * @see #importLibrary(ComponentLibrary, String)
     */
    public ImportResult importLibrary(ComponentLibrary library) {
        return importLibrary(library, null);
    }
    
    /**
     * 导入组件库（带来源类型）
     * 
     * <p>该方法提供完整的组件库导入流程，包括冲突检查、来源类型设置、
     * 保存操作和相关系统的通知更新。</p>
     * 
     * <p>导入流程：</p>
     * <ol>
     *   <li>检查是否已存在同名组件库</li>
     *   <li>智能设置组件库来源类型</li>
     *   <li>保存组件库到本地缓存</li>
     *   <li>添加到内存缓存</li>
     *   <li>刷新动态配置管理器</li>
     *   <li>通知相关组件重新加载</li>
     * </ol>
     * 
     * <p>来源类型推断规则：</p>
     * <ul>
     *   <li>如果明确指定了来源类型，直接使用</li>
     *   <li>如果sourceUrl是HTTP/HTTPS链接，设置为CUSTOM_REMOTE</li>
     *   <li>如果sourceUrl是本地路径，设置为CUSTOM_LOCAL</li>
     *   <li>默认为CUSTOM_LOCAL</li>
     * </ul>
     * 
     * @param library 要导入的组件库，不能为null
     * @param sourceType 来源类型，可以为null（将自动推断）
     * @return 导入结果，包含成功、冲突或错误信息
     * @throws IllegalArgumentException 如果library为null
     * 
     * @see #normalizeLibraryName(String)
     * @see com.chu7.vuecomponentassistant.remote.model.ImportResult
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager#refreshConfiguration()
     * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager#notifyAllProvidersReload()
     */
    public ImportResult importLibrary(ComponentLibrary library, String sourceType) {
        if (library == null) {
            throw new IllegalArgumentException("组件库不能为null");
        }
        
        try {
            String normalizedName = normalizeLibraryName(library.getName());
            
            // 检查是否已存在同名组件库
            if (existingLibraries.containsKey(normalizedName)) {
                ComponentLibrary existingLibrary = existingLibraries.get(normalizedName);
                return ImportResult.conflict(
                    "已存在名为 '" + library.getName() + "' 的组件库",
                    library,
                    existingLibrary
                );
            }
            
            // 根据导入来源设置 source 字段
            LOG.info("检查组件库source字段 - 当前值: " + library.getSource() + ", 组件库: " + library.getName() + ", 导入来源: " + sourceType);
            
            if (sourceType != null && !sourceType.trim().isEmpty()) {
                // 如果明确指定了来源类型，直接使用
                library.setSource(sourceType);
                LOG.info("使用指定的来源类型: " + library.getName() + " -> " + sourceType);
            } else if (library.getSource() == null || library.getSource().trim().isEmpty()) {
                // 如果没有指定来源类型且source为空，则根据sourceUrl判断
                String sourceUrl = library.getSourceUrl();
                if (sourceUrl != null && !sourceUrl.trim().isEmpty()) {
                    if (sourceUrl.startsWith("http://") || sourceUrl.startsWith("https://")) {
                        library.setSource("CUSTOM_REMOTE");
                    } else {
                        library.setSource("CUSTOM_LOCAL");
                    }
                } else {
                    // 默认为本地自定义组件库
                    library.setSource("CUSTOM_LOCAL");
                }
                LOG.info("根据sourceUrl自动设置组件库来源: " + library.getName() + " -> " + library.getSource());
            }
            
            // 保存组件库
            cacheManager.saveLibrary(library);
            existingLibraries.put(normalizedName, library);
            
            // 刷新动态配置管理器
            try {
                com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager.getInstance().refreshConfiguration();
                LOG.info("已刷新动态配置管理器");
            } catch (Exception e) {
                LOG.warn("刷新动态配置管理器失败: " + e.getMessage());
            }
            
            // 通知所有 ComponentProvider 重新加载组件数据
            try {
                com.chu7.vuecomponentassistant.completion2.ComponentProviderManager.notifyAllProvidersReload();
                LOG.info("已通知所有 ComponentProvider 重新加载数据");
            } catch (Exception e) {
                LOG.warn("通知 ComponentProvider 重新加载失败: " + e.getMessage());
            }
            
            LOG.info("成功导入组件库: " + library.getName());
            return ImportResult.success("组件库导入成功", library);
            
        } catch (Exception e) {
            LOG.error("导入组件库失败: " + library.getName(), e);
            return ImportResult.error("导入组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 替换组件库
     * 
     * <p>该方法用于替换已存在的组件库，通常用于更新组件库版本或配置。
     * 替换过程包括删除旧组件库和保存新组件库。</p>
     * 
     * <p>替换流程：</p>
     * <ol>
     *   <li>从本地缓存删除旧组件库</li>
     *   <li>从内存缓存移除旧组件库</li>
     *   <li>保存新组件库到本地缓存</li>
     *   <li>添加新组件库到内存缓存</li>
     *   <li>通知相关组件重新加载数据</li>
     * </ol>
     * 
     * @param newLibrary 新的组件库，不能为null
     * @param existingLibrary 要替换的现有组件库，不能为null
     * @return 替换结果，包含成功或错误信息
     * @throws IllegalArgumentException 如果newLibrary或existingLibrary为null
     * 
     * @see #normalizeLibraryName(String)
     * @see com.chu7.vuecomponentassistant.remote.model.ImportResult
     */
    public ImportResult replaceLibrary(ComponentLibrary newLibrary, ComponentLibrary existingLibrary) {
        if (newLibrary == null) {
            throw new IllegalArgumentException("新组件库不能为null");
        }
        if (existingLibrary == null) {
            throw new IllegalArgumentException("现有组件库不能为null");
        }
        
        try {
            String normalizedName = normalizeLibraryName(existingLibrary.getName());
            
            // 删除旧组件库
            cacheManager.removeLibrary(existingLibrary.getId());
            existingLibraries.remove(normalizedName);
            
            // 保存新组件库
            cacheManager.saveLibrary(newLibrary);
            existingLibraries.put(normalizedName, newLibrary);
            
            // 通知所有 ComponentProvider 重新加载组件数据
            try {
                com.chu7.vuecomponentassistant.completion2.ComponentProviderManager.notifyAllProvidersReload();
                LOG.info("已通知所有 ComponentProvider 重新加载数据");
            } catch (Exception e) {
                LOG.warn("通知 ComponentProvider 重新加载失败: " + e.getMessage());
            }
            
            LOG.info("成功替换组件库: " + existingLibrary.getName() + " -> " + newLibrary.getName());
            return ImportResult.success("组件库替换成功", newLibrary, ImportResult.ImportType.REPLACE);
            
        } catch (Exception e) {
            LOG.error("替换组件库失败: " + existingLibrary.getName(), e);
            return ImportResult.error("替换组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有组件库
     * 
     * <p>该方法返回本地缓存中的所有组件库列表。
     * 返回的列表是实时的，反映当前缓存中的最新状态。</p>
     * 
     * @return 所有组件库的列表，如果没有则返回空列表
     * 
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#getAllLibraries()
     */
    public List<ComponentLibrary> getAllLibraries() {
        return cacheManager.getAllLibraries();
    }
    
    /**
     * 根据名称获取组件库
     * 
     * <p>该方法根据组件库名称从内存缓存中快速查找组件库。
     * 名称会进行标准化处理以确保匹配的准确性。</p>
     * 
     * @param name 组件库名称，不能为null
     * @return 找到的组件库，如果未找到则返回null
     * @throws IllegalArgumentException 如果name为null
     * 
     * @see #normalizeLibraryName(String)
     */
    public ComponentLibrary getLibrary(String name) {
        if (name == null) {
            throw new IllegalArgumentException("组件库名称不能为null");
        }
        
        String normalizedName = normalizeLibraryName(name);
        return existingLibraries.get(normalizedName);
    }
    
    /**
     * 根据ID获取组件库
     * 
     * <p>该方法根据组件库的唯一ID从本地缓存中加载组件库。
     * 如果加载失败，会记录错误日志并返回null。</p>
     * 
     * @param libraryId 组件库的唯一ID，不能为null
     * @return 找到的组件库，如果未找到或加载失败则返回null
     * @throws IllegalArgumentException 如果libraryId为null
     * 
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#loadLibrary(String)
     */
    public ComponentLibrary getLibraryById(String libraryId) {
        if (libraryId == null) {
            throw new IllegalArgumentException("组件库ID不能为null");
        }
        
        try {
            return cacheManager.loadLibrary(libraryId);
        } catch (VueKitException e) {
            LOG.error("根据ID获取组件库失败: " + libraryId, e);
            return null;
        }
    }
    
    /**
     * 删除组件库
     * 
     * <p>该方法根据组件库名称删除指定的组件库。
     * 删除过程包括从本地缓存和内存缓存中移除组件库。</p>
     * 
     * <p>删除流程：</p>
     * <ol>
     *   <li>根据名称查找组件库</li>
     *   <li>从本地缓存中删除</li>
     *   <li>从内存缓存中移除</li>
     *   <li>记录删除结果</li>
     * </ol>
     * 
     * @param name 要删除的组件库名称，不能为null
     * @return 如果删除成功则返回true，否则返回false
     * @throws IllegalArgumentException 如果name为null
     * 
     * @see #normalizeLibraryName(String)
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#removeLibrary(String)
     */
    public boolean removeLibrary(String name) {
        if (name == null) {
            throw new IllegalArgumentException("组件库名称不能为null");
        }
        
        try {
            String normalizedName = normalizeLibraryName(name);
            ComponentLibrary library = existingLibraries.get(normalizedName);
            
            if (library != null) {
                boolean removed = cacheManager.removeLibrary(library.getId());
                if (removed) {
                    existingLibraries.remove(normalizedName);
                    LOG.info("成功删除组件库: " + name);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            LOG.error("删除组件库失败: " + name, e);
            return false;
        }
    }
    
    /**
     * 根据ID删除组件库
     */
    public boolean removeLibraryById(String libraryId) {
        try {
            ComponentLibrary library = cacheManager.loadLibrary(libraryId);
            if (library != null) {
                String normalizedName = normalizeLibraryName(library.getName());
                boolean removed = cacheManager.removeLibrary(libraryId);
                if (removed) {
                    existingLibraries.remove(normalizedName);
                    LOG.info("成功删除组件库: " + library.getName());
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            LOG.error("根据ID删除组件库失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 检查组件库是否存在
     */
    public boolean libraryExists(String name) {
        String normalizedName = normalizeLibraryName(name);
        return existingLibraries.containsKey(normalizedName);
    }
    
    /**
     * 获取组件库统计信息
     */
    public Map<String, Object> getLibraryStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            List<ComponentLibrary> allLibraries = getAllLibraries();
            
            long officialCount = allLibraries.stream()
                .filter(lib -> "OFFICIAL".equals(lib.getSource()))
                .count();
            
            long customLocalCount = allLibraries.stream()
                .filter(lib -> "CUSTOM_LOCAL".equals(lib.getSource()))
                .count();
            
            long customRemoteCount = allLibraries.stream()
                .filter(lib -> "CUSTOM_REMOTE".equals(lib.getSource()))
                .count();
            
            int totalComponents = allLibraries.stream()
                .mapToInt(lib -> lib.getComponents() != null ? lib.getComponents().size() : 0)
                .sum();
            
            stats.put("totalLibraries", allLibraries.size());
            stats.put("officialLibraries", officialCount);
            stats.put("customLocalLibraries", customLocalCount);
            stats.put("customRemoteLibraries", customRemoteCount);
            stats.put("totalComponents", totalComponents);
            
        } catch (Exception e) {
            LOG.error("获取组件库统计信息失败", e);
        }
        
        return stats;
    }
    
    /**
     * 重新加载远程组件库
     */
    public ImportResult reloadRemoteLibrary(String libraryId) {
        try {
            ComponentLibrary library = getLibraryById(libraryId);
            if (library == null) {
                return ImportResult.error("未找到组件库: " + libraryId);
            }
            
            if (!"CUSTOM_REMOTE".equals(library.getSource())) {
                return ImportResult.error("只能重新加载远程自定义组件库");
            }
            
            ComponentLibrary updatedLibrary = remoteManager.reloadRemoteLibrary(library).get();
            
            // 更新内存缓存
            String normalizedName = normalizeLibraryName(library.getName());
            existingLibraries.put(normalizedName, updatedLibrary);
            
            LOG.info("成功重新加载远程组件库: " + library.getName());
            return ImportResult.success("远程组件库重新加载成功", updatedLibrary, ImportResult.ImportType.UPDATE);
            
        } catch (Exception e) {
            LOG.error("重新加载远程组件库失败: " + libraryId, e);
            return ImportResult.error("重新加载远程组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化核心组件库
     */
    public void initializeCoreLibraries() {
        try {
            LOG.info("开始初始化核心组件库");
            
            // 检查是否已有组件库
            List<ComponentLibrary> existingLibraries = getAllLibraries();
            if (!existingLibraries.isEmpty()) {
                LOG.info("已存在组件库，跳过初始化");
                return;
            }
            
            // 下载核心组件库
            List<ComponentLibrary> coreLibraries = remoteManager.downloadCoreLibraries().get();
            
            // 导入到管理器
            for (ComponentLibrary library : coreLibraries) {
                importLibrary(library);
            }
            
            LOG.info("核心组件库初始化完成，数量: " + coreLibraries.size());
            
        } catch (Exception e) {
            LOG.error("初始化核心组件库失败", e);
        }
    }
    
    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        cacheManager.cleanExpiredCache();
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        return cacheManager.getCacheStats();
    }
    
    /**
     * 标准化组件库名称（用于去重）
     */
    private String normalizeLibraryName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase().trim();
    }
    

    
    // Getter方法
    public RemoteLibraryManager getRemoteManager() {
        return remoteManager;
    }
    
    public OfficialLibraryManager getOfficialManager() {
        return officialManager;
    }
} 