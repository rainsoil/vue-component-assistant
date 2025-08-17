package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态组件库管理器
 * 
 * 功能说明：
 * - 从远程组件库管理器动态获取组件库信息
 * - 替代写死的组件库枚举
 * - 支持动态组件库类型映射
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class DynamicLibraryManager {
    
    private static final Logger LOG = Logger.getInstance(DynamicLibraryManager.class);
    
    // 缓存已安装的组件库信息
    private static final Map<String, LibraryInfo> libraryCache = new ConcurrentHashMap<>();
    
    // 组件库信息类
    public static class LibraryInfo {
        private final String packageName;
        private final String displayName;
        private final String componentPrefix;
        private final String documentationUrlTemplate;
        private final String description;
        
        public LibraryInfo(String packageName, String displayName, String componentPrefix, 
                          String documentationUrlTemplate, String description) {
            this.packageName = packageName;
            this.displayName = displayName;
            this.componentPrefix = componentPrefix;
            this.documentationUrlTemplate = documentationUrlTemplate;
            this.description = description;
        }
        
        public String getPackageName() { return packageName; }
        public String getDisplayName() { return displayName; }
        public String getComponentPrefix() { return componentPrefix; }
        public String getDocumentationUrlTemplate() { return documentationUrlTemplate; }
        public String getDescription() { return description; }
    }
    
    /**
     * 根据包名获取组件库信息
     */
    public static LibraryInfo getLibraryByPackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return null;
        }
        
        // 检查缓存
        if (libraryCache.containsKey(packageName)) {
            return libraryCache.get(packageName);
        }
        
        try {
            // 从远程组件库管理器获取信息
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (packageName.equals(library.getName())) {
                    LibraryInfo info = createLibraryInfoFromComponentLibrary(library);
                    libraryCache.put(packageName, info);
                    return info;
                }
            }
            
            // 如果没有找到，尝试从已知的组件库配置中获取
            LibraryInfo info = getKnownLibraryInfo(packageName);
            if (info != null) {
                libraryCache.put(packageName, info);
                return info;
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取组件库信息失败: " + packageName, e);
        }
        
        return null;
    }
    
    /**
     * 根据组件库名称获取组件库信息
     */
    public static LibraryInfo getLibraryByName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return null;
        }
        
        // 移除版本号部分
        String cleanName = libraryName.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
        
        // 检查缓存
        if (libraryCache.containsKey(cleanName)) {
            return libraryCache.get(cleanName);
        }
        
        try {
            // 从远程组件库管理器获取信息
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (cleanName.equalsIgnoreCase(library.getName())) {
                    LibraryInfo info = createLibraryInfoFromComponentLibrary(library);
                    libraryCache.put(cleanName, info);
                    return info;
                }
            }
            
            // 如果没有找到，尝试从已知的组件库配置中获取
            LibraryInfo info = getKnownLibraryInfo(cleanName);
            if (info != null) {
                libraryCache.put(cleanName, info);
                return info;
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取组件库信息失败: " + libraryName, e);
        }
        
        return null;
    }
    
    /**
     * 从ComponentLibrary创建LibraryInfo
     */
    private static LibraryInfo createLibraryInfoFromComponentLibrary(ComponentLibrary library) {
        String packageName = library.getName();
        String displayName = library.getDisplayName() != null ? library.getDisplayName() : packageName;
        String description = library.getDescription() != null ? library.getDescription() : "";
        
        // 根据组件库名称推断前缀和文档URL
        String prefix = inferComponentPrefix(packageName);
        String docUrl = inferDocumentationUrl(packageName);
        
        return new LibraryInfo(packageName, displayName, prefix, docUrl, description);
    }
    
    /**
     * 从已知的组件库配置中获取信息（作为后备方案）
     */
    private static LibraryInfo getKnownLibraryInfo(String packageName) {
        // 使用 StringNormalizer 进行标准化匹配
        String normalizedName = StringNormalizer.normalize(packageName);
        
        // 检查是否匹配已知的组件库类型
        for (ComponentLibraryDetector.LibraryType type : ComponentLibraryDetector.LibraryType.values()) {
            if (type == ComponentLibraryDetector.LibraryType.UNKNOWN) continue;
            
            String normalizedPackageName = StringNormalizer.normalize(type.getPackageName());
            if (normalizedName.equals(normalizedPackageName)) {
                return createLibraryInfoFromLibraryType(type);
            }
        }
        
        return null;
    }
    
    /**
     * 从 LibraryType 创建 LibraryInfo
     */
    private static LibraryInfo createLibraryInfoFromLibraryType(ComponentLibraryDetector.LibraryType type) {
        switch (type) {
            case ELEMENT_PLUS:
                return new LibraryInfo("element-plus", "Element Plus", "el-", 
                    "https://element-plus.org/zh-CN/component/%s.html", 
                    "Element Plus - 基于 Vue 3 的组件库");
            case ELEMENT_UI:
                return new LibraryInfo("element-ui", "Element UI", "el-", 
                    "https://element.eleme.cn/#/zh-CN/component/%s", 
                    "Element UI - 基于 Vue 2 的组件库");
            case ANT_DESIGN_VUE:
                return new LibraryInfo("ant-design-vue", "Ant Design Vue", "a-", 
                    "https://antdv.com/components/%s-cn", 
                    "Ant Design Vue - 基于 Ant Design 的 Vue 组件库");
            case VUETIFY:
                return new LibraryInfo("vuetify", "Vuetify", "v-", 
                    "https://vuetifyjs.com/en/components/%s/", 
                    "Vuetify - 基于 Material Design 的 Vue 组件库");
            case QUASAR:
                return new LibraryInfo("quasar", "Quasar", "q-", 
                    "https://quasar.dev/vue-components/%s", 
                    "Quasar - 基于 Vue 的跨平台 UI 框架");
            default:
                return null;
        }
    }
    
    /**
     * 推断组件前缀
     */
    private static String inferComponentPrefix(String packageName) {
        // 使用 StringNormalizer 进行标准化匹配
        String normalizedName = StringNormalizer.normalize(packageName);
        
        if (StringNormalizer.contains(normalizedName, "element")) {
            return "el-";
        } else if (StringNormalizer.contains(normalizedName, "ant")) {
            return "a-";
        } else if (StringNormalizer.contains(normalizedName, "vuetify")) {
            return "v-";
        } else if (StringNormalizer.contains(normalizedName, "quasar")) {
            return "q-";
        } else {
            return ""; // 默认无前缀
        }
    }
    
    /**
     * 推断文档URL
     */
    private static String inferDocumentationUrl(String packageName) {
        // 使用 StringNormalizer 进行标准化匹配
        String normalizedName = StringNormalizer.normalize(packageName);
        
        if (StringNormalizer.contains(normalizedName, "element-plus")) {
            return "https://element-plus.org/zh-CN/component/%s.html";
        } else if (StringNormalizer.contains(normalizedName, "element-ui")) {
            return "https://element.eleme.cn/#/zh-CN/component/%s";
        } else if (StringNormalizer.contains(normalizedName, "ant-design-vue")) {
            return "https://antdv.com/components/%s-cn";
        } else if (StringNormalizer.contains(normalizedName, "vuetify")) {
            return "https://vuetifyjs.com/en/components/%s/";
        } else if (StringNormalizer.contains(normalizedName, "quasar")) {
            return "https://quasar.dev/vue-components/%s";
        } else {
            return ""; // 默认无文档URL
        }
    }
    
    /**
     * 获取所有已安装的组件库信息
     */
    public static List<LibraryInfo> getAllInstalledLibraries() {
        List<LibraryInfo> libraries = new ArrayList<>();
        
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                LibraryInfo info = createLibraryInfoFromComponentLibrary(library);
                libraries.add(info);
                libraryCache.put(library.getName(), info);
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取已安装组件库列表失败", e);
        }
        
        return libraries;
    }
    
    /**
     * 检查组件是否属于指定的组件库
     */
    public static boolean isComponentFromLibrary(String componentName, String libraryName) {
        if (componentName == null || libraryName == null) {
            return false;
        }
        
        LibraryInfo libraryInfo = getLibraryByName(libraryName);
        if (libraryInfo == null) {
            return false;
        }
        
        String prefix = libraryInfo.getComponentPrefix();
        return componentName.startsWith(prefix);
    }
    
    /**
     * 清除缓存
     */
    public static void clearCache() {
        libraryCache.clear();
    }
} 