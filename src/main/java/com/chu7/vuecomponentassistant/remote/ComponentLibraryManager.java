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
 */
public class ComponentLibraryManager {
    private static final Logger LOG = Logger.getInstance(ComponentLibraryManager.class);
    
    private final LocalCacheManager cacheManager;
    private final RemoteLibraryManager remoteManager;
    private final OfficialLibraryManager officialManager;
    private final Map<String, ComponentLibrary> existingLibraries;
    
    public ComponentLibraryManager() {
        this.cacheManager = new LocalCacheManager();
        this.remoteManager = new RemoteLibraryManager();
        this.officialManager = new OfficialLibraryManager();
        this.existingLibraries = new ConcurrentHashMap<>();
        
        // 初始化时加载所有已存在的组件库
        loadExistingLibraries();
    }
    
    /**
     * 加载已存在的组件库
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
     * 导入组件库
     */
    public ImportResult importLibrary(ComponentLibrary library) {
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
            
            // 保存组件库
            cacheManager.saveLibrary(library);
            existingLibraries.put(normalizedName, library);
            
            LOG.info("成功导入组件库: " + library.getName());
            return ImportResult.success("组件库导入成功", library);
            
        } catch (Exception e) {
            LOG.error("导入组件库失败: " + library.getName(), e);
            return ImportResult.error("导入组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 替换组件库
     */
    public ImportResult replaceLibrary(ComponentLibrary newLibrary, ComponentLibrary existingLibrary) {
        try {
            String normalizedName = normalizeLibraryName(existingLibrary.getName());
            
            // 删除旧组件库
            cacheManager.removeLibrary(existingLibrary.getId());
            existingLibraries.remove(normalizedName);
            
            // 保存新组件库
            cacheManager.saveLibrary(newLibrary);
            existingLibraries.put(normalizedName, newLibrary);
            
            LOG.info("成功替换组件库: " + existingLibrary.getName() + " -> " + newLibrary.getName());
            return ImportResult.success("组件库替换成功", newLibrary, ImportResult.ImportType.REPLACE);
            
        } catch (Exception e) {
            LOG.error("替换组件库失败: " + existingLibrary.getName(), e);
            return ImportResult.error("替换组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有组件库
     */
    public List<ComponentLibrary> getAllLibraries() {
        return cacheManager.getAllLibraries();
    }
    
    /**
     * 根据名称获取组件库
     */
    public ComponentLibrary getLibrary(String name) {
        String normalizedName = normalizeLibraryName(name);
        return existingLibraries.get(normalizedName);
    }
    
    /**
     * 根据ID获取组件库
     */
    public ComponentLibrary getLibraryById(String libraryId) {
        try {
            return cacheManager.loadLibrary(libraryId);
        } catch (VueKitException e) {
            LOG.error("根据ID获取组件库失败: " + libraryId, e);
            return null;
        }
    }
    
    /**
     * 删除组件库
     */
    public boolean removeLibrary(String name) {
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
                .filter(lib -> lib.getSource() == ComponentLibrary.LibrarySource.OFFICIAL)
                .count();
            
            long customLocalCount = allLibraries.stream()
                .filter(lib -> lib.getSource() == ComponentLibrary.LibrarySource.CUSTOM_LOCAL)
                .count();
            
            long customRemoteCount = allLibraries.stream()
                .filter(lib -> lib.getSource() == ComponentLibrary.LibrarySource.CUSTOM_REMOTE)
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
            
            if (library.getSource() != ComponentLibrary.LibrarySource.CUSTOM_REMOTE) {
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