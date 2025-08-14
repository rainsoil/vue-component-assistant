package com.chu7.vuecomponentassistant.remote;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.OfficialLibrary;
import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 官方组件库管理器
 */
public class OfficialLibraryManager {
    private static final Logger LOG = Logger.getInstance(OfficialLibraryManager.class);
    private static final String DEFAULT_OFFICIAL_REGISTRY_URL = "https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json";
    
    private final LocalCacheManager cacheManager;
    private final RemoteLibraryManager remoteManager;
    
    public OfficialLibraryManager() {
        this.cacheManager = new LocalCacheManager();
        this.remoteManager = new RemoteLibraryManager();
    }
    
    /**
     * 异步获取官方组件库列表（实时刷新，不使用缓存）
     */
    public CompletableFuture<List<OfficialLibrary>> fetchOfficialLibraries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始获取官方组件库列表（实时刷新）");
                
                // 直接从远程获取，不使用缓存
                String registryUrl = getOfficialRegistryUrl();
                String json = HttpClient.downloadJson(registryUrl);
                List<OfficialLibrary> libraries = parseOfficialLibrariesJson(json);
                
                LOG.info("官方组件库列表获取完成，数量: " + libraries.size());
                return libraries;
                
            } catch (Exception e) {
                LOG.error("获取官方组件库列表失败", e);
                // 返回空列表，不显示默认组件库
                return new java.util.ArrayList<>();
            }
        });
    }
    
    /**
     * 异步下载官方组件库
     */
    public CompletableFuture<ComponentLibrary> downloadOfficialLibrary(String libraryId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始下载官方组件库: " + libraryId);
                
                // 获取官方组件库信息
                List<OfficialLibrary> officialLibraries = fetchOfficialLibraries().get();
                OfficialLibrary officialLibrary = officialLibraries.stream()
                    .filter(lib -> lib.getId().equals(libraryId))
                    .findFirst()
                    .orElseThrow(() -> new VueKitException("未找到官方组件库: " + libraryId));
                
                // 下载组件库
                ComponentLibrary library = remoteManager.downloadLibrary(officialLibrary.getDownloadUrl()).get();
                
                // 设置官方来源信息
                library.setSource("OFFICIAL");
                library.setId(officialLibrary.getId());
                library.setName(officialLibrary.getName());
                library.setDisplayName(officialLibrary.getDisplayName());
                library.setDescription(officialLibrary.getDescription());
                library.setVersion(officialLibrary.getVersion());
                
                LOG.info("官方组件库下载完成: " + library.getName());
                return library;
                
            } catch (Exception e) {
                LOG.error("下载官方组件库失败: " + libraryId, e);
                throw new RuntimeException("下载官方组件库失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 搜索官方组件库
     */
    public CompletableFuture<List<OfficialLibrary>> searchOfficialLibraries(String keyword) {
        return fetchOfficialLibraries().thenApply(libraries -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return libraries;
            }
            
            String lowerKeyword = keyword.toLowerCase();
            return libraries.stream()
                .filter(library -> 
                    library.getName().toLowerCase().contains(lowerKeyword) ||
                    library.getDisplayName().toLowerCase().contains(lowerKeyword) ||
                    library.getDescription().toLowerCase().contains(lowerKeyword) ||
                    (library.getTags() != null && library.getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword)))
                )
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 按分类筛选官方组件库
     */
    public CompletableFuture<List<OfficialLibrary>> filterOfficialLibrariesByCategory(String category) {
        return fetchOfficialLibraries().thenApply(libraries -> {
            if (category == null || category.trim().isEmpty() || "全部".equals(category)) {
                return libraries;
            }
            
            return libraries.stream()
                .filter(library -> category.equals(library.getCategory()))
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 按框架筛选官方组件库
     */
    public CompletableFuture<List<OfficialLibrary>> filterOfficialLibrariesByFramework(String framework) {
        return fetchOfficialLibraries().thenApply(libraries -> {
            if (framework == null || framework.trim().isEmpty() || "全部".equals(framework)) {
                return libraries;
            }
            
            return libraries.stream()
                .filter(library -> framework.equals(library.getFramework()))
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 获取所有分类
     */
    public CompletableFuture<List<String>> getAllCategories() {
        return fetchOfficialLibraries().thenApply(libraries -> 
            libraries.stream()
                .map(OfficialLibrary::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 获取所有框架
     */
    public CompletableFuture<List<String>> getAllFrameworks() {
        return fetchOfficialLibraries().thenApply(libraries -> 
            libraries.stream()
                .map(OfficialLibrary::getFramework)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 检查官方组件库是否已安装
     */
    public boolean isOfficialLibraryInstalled(String libraryId) {
        try {
            ComponentLibrary library = cacheManager.loadLibrary(libraryId);
            return library != null && "OFFICIAL".equals(library.getSource());
        } catch (VueKitException e) {
            LOG.warn("检查官方组件库安装状态失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 获取已安装的官方组件库
     */
    public List<OfficialLibrary> getInstalledOfficialLibraries() {
        try {
            List<OfficialLibrary> allOfficialLibraries = fetchOfficialLibraries().get();
            return allOfficialLibraries.stream()
                .filter(library -> isOfficialLibraryInstalled(library.getId()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("获取已安装的官方组件库失败", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 刷新官方组件库列表
     */
    public CompletableFuture<List<OfficialLibrary>> refreshOfficialLibraries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始刷新官方组件库列表");
                
                // 清除缓存
                try {
                    cacheManager.clearOfficialLibrariesCache();
                } catch (Exception e) {
                    LOG.warn("清除官方组件库缓存失败", e);
                }
                
                // 强制从远程获取最新数据
                String registryUrl = getOfficialRegistryUrl();
                String json = HttpClient.downloadJson(registryUrl);
                List<OfficialLibrary> libraries = parseOfficialLibrariesJson(json);
                
                // 更新缓存
                if (!libraries.isEmpty()) {
                    cacheManager.saveOfficialLibraries(libraries);
                }
                
                LOG.info("官方组件库列表刷新完成，数量: " + libraries.size());
                return libraries;
                
            } catch (Exception e) {
                LOG.error("刷新官方组件库列表失败", e);
                // 返回空列表，不显示默认组件库
                return new java.util.ArrayList<>();
            }
        });
    }
    
    // 解析官方组件库JSON
    private List<OfficialLibrary> parseOfficialLibrariesJson(String json) throws VueKitException {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType();
            List<OfficialLibrary> libraries = gson.fromJson(json, listType);
            
            if (libraries == null) {
                LOG.warn("解析官方组件库JSON返回null，返回空列表");
                return new java.util.ArrayList<>();
            }
            
            LOG.info("成功解析官方组件库JSON，数量: " + libraries.size());
            return libraries;
            
        } catch (Exception e) {
            LOG.error("解析官方组件库JSON失败，返回空列表", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 获取官方组件库注册表URL
     */
    private String getOfficialRegistryUrl() {
        try {
            PluginSettings settings = PluginSettings.getInstance();
            String url = settings.getOfficialLibraryMarketUrl();
            if (url != null && !url.trim().isEmpty()) {
                return url.trim();
            }
        } catch (Exception e) {
            LOG.warn("获取官方组件库市场URL配置失败，使用默认URL", e);
        }
        return DEFAULT_OFFICIAL_REGISTRY_URL;
    }
    
    // 创建默认官方组件库列表
    private List<OfficialLibrary> createDefaultOfficialLibraries() {
        List<OfficialLibrary> libraries = new java.util.ArrayList<>();
        
        // Element Plus
        OfficialLibrary elementPlusLib = new OfficialLibrary(
            "element-plus",
            "Element Plus",
            "Element Plus",
            "Vue 3 组件库",
            "2.5.0",
            "vue3",
            "UI Framework",
            "Element Plus Team",
            "https://element-plus.org",
            "https://cdn.vuekit.dev/libraries/element-plus.json"
        );
        elementPlusLib.setDownloadCount(50000);
        elementPlusLib.setRating(4.9);
        libraries.add(elementPlusLib);
        
        // Vuetify
        OfficialLibrary vuetify = new OfficialLibrary(
            "vuetify",
            "Vuetify",
            "Vuetify",
            "Material Design component framework",
            "3.4.0",
            "vue3",
            "UI Framework",
            "Vuetify Team",
            "https://vuetifyjs.com",
            "https://cdn.vuekit.dev/libraries/vuetify.json"
        );
        vuetify.setDownloadCount(15000);
        vuetify.setRating(4.8);
        libraries.add(vuetify);
        
        // Naive UI
        OfficialLibrary naiveUI = new OfficialLibrary(
            "naive-ui",
            "Naive UI",
            "Naive UI",
            "A Vue 3 Component Library",
            "2.35.0",
            "vue3",
            "UI Framework",
            "Naive UI Team",
            "https://naiveui.com",
            "https://cdn.vuekit.dev/libraries/naive-ui.json"
        );
        naiveUI.setDownloadCount(8000);
        naiveUI.setRating(4.6);
        libraries.add(naiveUI);
        
        // Ant Design Vue
        OfficialLibrary antDesignVue = new OfficialLibrary(
            "ant-design-vue",
            "Ant Design Vue",
            "Ant Design Vue",
            "Vue 3 企业级UI组件库",
            "4.0.0",
            "vue3",
            "UI Framework",
            "Ant Design Vue Team",
            "https://antdv.com",
            "https://cdn.vuekit.dev/libraries/ant-design-vue.json"
        );
        antDesignVue.setDownloadCount(25000);
        antDesignVue.setRating(4.7);
        libraries.add(antDesignVue);
        
        return libraries;
    }
} 