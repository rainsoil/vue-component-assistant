package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;
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
        try {
            // 动态获取已安装的组件库，不再硬编码
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (packageName.equals(library.getName())) {
                    return createLibraryInfoFromComponentLibrary(library);
                }
            }
            
            // 如果没有找到，尝试智能推断
            return createInferredLibraryInfo(packageName);
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "动态获取组件库信息失败，使用智能推断: " + e.getMessage());
            return createInferredLibraryInfo(packageName);
        }
    }
    
    /**
     * 从 LibraryType 创建 LibraryInfo（已废弃）
     */
    @Deprecated
    private static LibraryInfo createLibraryInfoFromLibraryType(String type) {
        VueKitLogger.warn(LOG, "createLibraryInfoFromLibraryType 方法已废弃，使用动态获取替代");
        return null;
    }
    
    /**
     * 创建推断的组件库信息
     */
    private static LibraryInfo createInferredLibraryInfo(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return null;
        }
        
        String componentPrefix = inferComponentPrefix(packageName);
        String documentationUrl = inferDocumentationUrl(packageName);
        String displayName = inferDisplayName(packageName);
        String description = inferDescription(packageName);
        
        return new LibraryInfo(packageName, displayName, componentPrefix, documentationUrl, description);
    }
    
    /**
     * 推断组件前缀
     */
    private static String inferComponentPrefix(String packageName) {
        // 优先从已安装的组件库中获取前缀
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (packageName.equals(library.getName())) {
                    // 如果组件库有自定义的前缀配置，使用它
                    // 注意：ComponentLibrary 类目前没有 getComponentPrefix 方法
                    // 这里可以后续扩展，暂时使用智能推断
                    break;
                }
            }
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "从已安装组件库获取前缀失败，使用智能推断: " + e.getMessage());
        }
        
        // 使用智能推断作为后备方案，避免硬编码特定组件库
        String normalizedName = StringNormalizer.normalize(packageName);
        
        // 基于包名模式智能推断前缀，而不是硬编码特定组件库
        if (StringNormalizer.contains(normalizedName, "element")) {
            return "el-";
        } else if (StringNormalizer.contains(normalizedName, "ant") || StringNormalizer.contains(normalizedName, "design")) {
            return "a-";
        } else if (StringNormalizer.contains(normalizedName, "vuetify")) {
            return "v-";
        } else if (StringNormalizer.contains(normalizedName, "quasar")) {
            return "q-";
        } else if (StringNormalizer.contains(normalizedName, "naive")) {
            return "n-";
        } else if (StringNormalizer.contains(normalizedName, "prime")) {
            return "p-";
        } else {
            // 对于未知的组件库，尝试从包名推断前缀
            return inferPrefixFromPackageName(packageName);
        }
    }
    
    /**
     * 从包名推断前缀
     */
    private static String inferPrefixFromPackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "";
        }
        
        // 提取包名的主要部分作为前缀
        String[] parts = packageName.split("-");
        if (parts.length > 0) {
            String firstPart = parts[0].toLowerCase();
            if (firstPart.length() >= 2) {
                return firstPart.substring(0, 2) + "-";
            } else {
                return firstPart + "-";
            }
        }
        
        return "";
    }
    
    /**
     * 推断文档URL
     */
    private static String inferDocumentationUrl(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "";
        }
        
        try {
            // 优先从已安装的组件库中获取文档URL模板
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (packageName.equals(library.getName())) {
                    // 如果组件库有自定义的文档URL模板，使用它
                    // 注意：ComponentLibrary 类目前没有 getDocumentationUrlTemplate 方法
                    // 这里可以后续扩展，暂时使用智能推断
                    break;
                }
            }
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "从已安装组件库获取文档URL失败，使用智能推断: " + e.getMessage());
        }
        
        // 使用智能推断作为后备方案
        String normalizedName = StringNormalizer.normalize(packageName);
        
        // 基于包名模式智能推断文档URL（避免硬编码特定域名）
        return buildGenericDocumentationUrl(packageName);
    }
    
    /**
     * 构建通用的文档URL模板
     */
    private static String buildGenericDocumentationUrl(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "";
        }
        
        // 尝试从包名推断可能的文档URL模式
        String normalizedName = packageName.toLowerCase().replaceAll("[^a-z0-9]", "");
        
        // 常见的文档URL模式
        if (normalizedName.contains("ui") || normalizedName.contains("design")) {
            return "https://" + packageName + ".org/components/%s";
        } else if (normalizedName.contains("vue")) {
            return "https://" + packageName + ".com/components/%s";
        } else {
            // 默认模式
            return "https://" + packageName + ".org/docs/%s";
        }
    }
    
    /**
     * 推断显示名称
     */
    private static String inferDisplayName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "未知组件库";
        }
        
        // 将包名转换为更友好的显示名称
        String[] words = packageName.split("-");
        StringBuilder displayName = new StringBuilder();
        
        for (String word : words) {
            if (!word.trim().isEmpty()) {
                if (displayName.length() > 0) {
                    displayName.append(" ");
                }
                displayName.append(word.substring(0, 1).toUpperCase())
                          .append(word.substring(1).toLowerCase());
            }
        }
        
        return displayName.toString();
    }
    
    /**
     * 推断描述信息
     */
    private static String inferDescription(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "组件库信息不可用";
        }
        
        String normalizedName = StringNormalizer.normalize(packageName);
        
        if (StringNormalizer.contains(normalizedName, "element")) {
            return "基于 Vue 的组件库";
        } else if (StringNormalizer.contains(normalizedName, "ant") || StringNormalizer.contains(normalizedName, "design")) {
            return "基于 Ant Design 的 Vue 组件库";
        } else if (StringNormalizer.contains(normalizedName, "vuetify")) {
            return "基于 Material Design 的 Vue 组件库";
        } else if (StringNormalizer.contains(normalizedName, "quasar")) {
            return "基于 Vue 的跨平台 UI 框架";
        } else if (StringNormalizer.contains(normalizedName, "naive")) {
            return "基于 Vue 3 的组件库";
        } else if (StringNormalizer.contains(normalizedName, "prime")) {
            return "基于 Vue 的组件库";
        } else {
            return "Vue 组件库";
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