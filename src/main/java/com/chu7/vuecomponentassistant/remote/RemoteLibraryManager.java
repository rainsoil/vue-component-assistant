package com.chu7.vuecomponentassistant.remote;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 远程组件库管理器
 */
public class RemoteLibraryManager {
    private static final Logger LOG = Logger.getInstance(RemoteLibraryManager.class);
    private static final String CORE_LIBRARIES_URL = "https://registry.vuekit.dev/core-libraries.json";
    
    private final LocalCacheManager cacheManager;
    
    public RemoteLibraryManager() {
        this.cacheManager = new LocalCacheManager();
    }
    
    /**
     * 异步下载核心组件库
     */
    public CompletableFuture<List<ComponentLibrary>> downloadCoreLibraries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始下载核心组件库");
                
                // 下载核心组件库列表
                String json = HttpClient.downloadJson(CORE_LIBRARIES_URL);
                List<ComponentLibrary> coreLibraries = parseCoreLibrariesJson(json);
                
                // 保存到缓存
                for (ComponentLibrary library : coreLibraries) {
                    cacheManager.saveLibrary(library);
                }
                
                LOG.info("核心组件库下载完成，数量: " + coreLibraries.size());
                return coreLibraries;
                
            } catch (Exception e) {
                LOG.error("下载核心组件库失败", e);
                throw new RuntimeException("下载核心组件库失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 异步下载指定组件库
     */
    public CompletableFuture<ComponentLibrary> downloadLibrary(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始下载组件库: " + url);
                
                // 下载组件库JSON
                String json = HttpClient.downloadJsonWithRetry(url, 3).get();
                ComponentLibrary library = parseComponentLibraryJson(json);
                
                // 保存到缓存
                cacheManager.saveLibrary(library);
                
                LOG.info("组件库下载完成: " + library.getName());
                return library;
                
            } catch (Exception e) {
                LOG.error("下载组件库失败: " + url, e);
                throw new RuntimeException("下载组件库失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 重新加载远程组件库
     */
    public CompletableFuture<ComponentLibrary> reloadRemoteLibrary(ComponentLibrary library) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (library.getSource() != ComponentLibrary.LibrarySource.CUSTOM_REMOTE) {
                    throw new VueKitException("只能重新加载远程自定义组件库");
                }
                
                String sourceUrl = library.getSourceUrl();
                if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
                    throw new VueKitException("组件库没有有效的源URL");
                }
                
                LOG.info("重新加载远程组件库: " + library.getName() + " -> " + sourceUrl);
                
                // 重新下载
                String json = HttpClient.downloadJsonWithRetry(sourceUrl, 3).get();
                ComponentLibrary updatedLibrary = parseComponentLibraryJson(json);
                
                // 保持原有的ID和来源信息
                updatedLibrary.setId(library.getId());
                updatedLibrary.setSource(library.getSource());
                updatedLibrary.setSourceUrl(library.getSourceUrl());
                
                // 保存到缓存
                cacheManager.saveLibrary(updatedLibrary);
                
                LOG.info("远程组件库重新加载完成: " + updatedLibrary.getName());
                return updatedLibrary;
                
            } catch (Exception e) {
                LOG.error("重新加载远程组件库失败: " + library.getName(), e);
                throw new RuntimeException("重新加载组件库失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 验证远程URL
     */
    public CompletableFuture<Boolean> validateRemoteUrl(String url) {
        return HttpClient.checkUrlAccessibleAsync(url);
    }
    
    /**
     * 预览远程组件库
     */
    public CompletableFuture<ComponentLibrary> previewRemoteLibrary(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("预览远程组件库: " + url);
                
                // 下载并解析，但不保存到缓存
                String json = HttpClient.downloadJson(url);
                ComponentLibrary library = parseComponentLibraryJson(json);
                
                LOG.info("远程组件库预览完成: " + library.getName());
                return library;
                
            } catch (Exception e) {
                LOG.error("预览远程组件库失败: " + url, e);
                throw new RuntimeException("预览组件库失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 获取本地缓存的所有组件库
     */
    public List<ComponentLibrary> getLocalLibraries() {
        return cacheManager.getAllLibraries();
    }
    
    /**
     * 从本地缓存获取组件库
     */
    public ComponentLibrary getLocalLibrary(String libraryId) {
        try {
            return cacheManager.loadLibrary(libraryId);
        } catch (VueKitException e) {
            LOG.error("获取本地组件库失败: " + libraryId, e);
            return null;
        }
    }
    
    /**
     * 删除本地组件库
     */
    public boolean removeLocalLibrary(String libraryId) {
        return cacheManager.removeLibrary(libraryId);
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
    public java.util.Map<String, Object> getCacheStats() {
        return cacheManager.getCacheStats();
    }
    
    // 解析核心组件库JSON
    private List<ComponentLibrary> parseCoreLibrariesJson(String json) throws VueKitException {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ComponentLibrary>>(){}.getType();
            List<ComponentLibrary> libraries = gson.fromJson(json, listType);
            
            if (libraries == null) {
                LOG.warn("解析核心组件库JSON返回null，使用默认列表");
                return createDefaultCoreLibraries();
            }
            
            LOG.info("成功解析核心组件库JSON，数量: " + libraries.size());
            return libraries;
            
        } catch (Exception e) {
            LOG.error("解析核心组件库JSON失败，使用默认列表", e);
            return createDefaultCoreLibraries();
        }
    }
    
    // 解析组件库JSON
    private ComponentLibrary parseComponentLibraryJson(String json) throws VueKitException {
        try {
            Gson gson = new Gson();
            ComponentLibrary library = gson.fromJson(json, ComponentLibrary.class);
            
            if (library == null) {
                throw new VueKitException("解析组件库JSON返回null");
            }
            
            // 验证必要字段
            if (library.getName() == null || library.getName().trim().isEmpty()) {
                throw new VueKitException("组件库名称不能为空");
            }
            
            if (library.getComponents() == null || library.getComponents().isEmpty()) {
                throw new VueKitException("组件库必须包含至少一个组件");
            }
            
            LOG.info("成功解析组件库JSON: " + library.getName());
            return library;
            
        } catch (Exception e) {
            throw new VueKitException("解析组件库JSON失败: " + e.getMessage());
        }
    }
    
    // 创建默认核心组件库列表
    private List<ComponentLibrary> createDefaultCoreLibraries() {
        List<ComponentLibrary> libraries = new java.util.ArrayList<>();
        
        // Element Plus
        ComponentLibrary elementPlusLib = new ComponentLibrary(
            "element-plus",
            "Element Plus",
            "Element Plus",
            "Vue 3 组件库",
            "2.5.0",
            ComponentLibrary.LibrarySource.OFFICIAL,
            "https://cdn.vuekit.dev/libraries/element-plus.json"
        );
        libraries.add(elementPlusLib);
        
        // Element UI
        ComponentLibrary elementUI = new ComponentLibrary(
            "element-ui",
            "Element UI",
            "Element UI",
            "Vue 2 组件库",
            "2.15.0",
            ComponentLibrary.LibrarySource.OFFICIAL,
            "https://cdn.vuekit.dev/libraries/element-ui.json"
        );
        libraries.add(elementUI);
        
        // Ant Design Vue
        ComponentLibrary antDesignVue = new ComponentLibrary(
            "ant-design-vue",
            "Ant Design Vue",
            "Ant Design Vue",
            "Vue 3 企业级UI组件库",
            "4.0.0",
            ComponentLibrary.LibrarySource.OFFICIAL,
            "https://cdn.vuekit.dev/libraries/ant-design-vue.json"
        );
        libraries.add(antDesignVue);
        
        return libraries;
    }
} 