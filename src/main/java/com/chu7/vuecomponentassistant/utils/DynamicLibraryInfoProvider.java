package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Optional;

/**
 * 动态组件库信息提供者
 * 从下载的组件库中获取动态信息，如文档URL模板、组件数据等
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class DynamicLibraryInfoProvider {
    
    private static final Logger LOG = VueKitLogger.getLogger(DynamicLibraryInfoProvider.class);
    
    /** 组件库管理器 */
    private static final ComponentLibraryManager libraryManager = new ComponentLibraryManager();

    /**
     * 根据组件库名称获取推断的文档基础URL
     * 优先从已安装组件库的 sourceUrl 推断，失败则返回空字符串
     */
    public static String getDocumentationBaseUrlFromName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        try {
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            for (ComponentLibrary lib : libraries) {
                if (libraryName.equals(lib.getName())) {
                    String sourceUrl = lib.getSourceUrl();
                    if (sourceUrl != null && !sourceUrl.trim().isEmpty()) {
                        return inferDocumentationBaseUrl(sourceUrl);
                    }
                }
            }
        } catch (Exception e) {
            LOG.debug("根据名称获取文档基础URL失败: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * 从下载的组件库中获取文档URL模板
     * 
     * @param libraryId 组件库ID
     * @param componentName 组件名称
     * @return 文档URL，如果找不到则返回空字符串
     */
    public static String getDocumentationUrl(String libraryId, String componentName) {
        if (libraryId == null || componentName == null) {
            return "";
        }
        
        try {
            // 获取组件库信息
            ComponentLibrary library = getLibraryById(libraryId);
            if (library == null) {
                LOG.debug("未找到组件库: " + libraryId);
                return "";
            }
            
            // 查找组件信息
            ComponentInfo component = findComponentInLibrary(library, componentName);
            if (component == null) {
                LOG.debug("在组件库 " + libraryId + " 中未找到组件: " + componentName);
                return "";
            }
            
            // 返回组件的文档URL
            String docUrl = component.getDocUrl();
            if (docUrl != null && !docUrl.trim().isEmpty()) {
                LOG.debug("找到组件 " + componentName + " 的文档URL: " + docUrl);
                return docUrl;
            }
            
            // 如果没有直接的文档URL，尝试从组件库的sourceUrl推断
            String sourceUrl = library.getSourceUrl();
            if (sourceUrl != null && !sourceUrl.trim().isEmpty()) {
                // 这里可以根据不同的组件库生成文档URL
                // 例如：从 sourceUrl 推断文档基础URL
                String baseUrl = inferDocumentationBaseUrl(sourceUrl);
                if (!baseUrl.isEmpty()) {
                    String componentKey = extractComponentKey(componentName);
                    String inferredUrl = baseUrl + componentKey;
                    LOG.debug("推断的文档URL: " + inferredUrl);
                    return inferredUrl;
                }
            }
            
        } catch (Exception e) {
            LOG.warn("获取组件 " + componentName + " 的文档URL失败", e);
        }
        
        return "";
    }
    
    /**
     * 从下载的组件库中获取组件信息
     * 
     * @param libraryId 组件库ID
     * @param componentName 组件名称
     * @return 组件信息，如果找不到则返回 null
     */
    public static ComponentInfo getComponentInfo(String libraryId, String componentName) {
        if (libraryId == null || componentName == null) {
            return null;
        }
        
        try {
            ComponentLibrary library = getLibraryById(libraryId);
            if (library == null) {
                return null;
            }
            
            return findComponentInLibrary(library, componentName);
        } catch (Exception e) {
            LOG.warn("获取组件信息失败: " + componentName, e);
            return null;
        }
    }
    
    /**
     * 从下载的组件库中获取组件库信息
     * 
     * @param libraryId 组件库ID
     * @return 组件库信息，如果找不到则返回 null
     */
    public static ComponentLibrary getLibraryById(String libraryId) {
        if (libraryId == null) {
            return null;
        }
        
        try {
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            return libraries.stream()
                    .filter(lib -> libraryId.equals(lib.getId()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            LOG.warn("获取组件库失败: " + libraryId, e);
            return null;
        }
    }
    
    /**
     * 自动推断组件库的配置信息
     * 当组件库不在预定义配置中时，尝试从下载的组件库中推断配置
     * 
     * @param libraryId 组件库ID
     * @return 是否成功推断并注册配置
     */
    public static boolean autoInferLibraryConfig(String libraryId) {
        try {
            ComponentLibrary library = getLibraryById(libraryId);
            if (library == null) {
                return false;
            }
            
            // 尝试从组件库信息中推断配置
            String packageName = library.getName();
            String displayName = library.getDisplayName();
            String componentPrefix = inferComponentPrefix(library);
            
            // 自动注册到配置管理器
            DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
            return configManager.autoRegisterFromDownloadedLibrary(libraryId, packageName, displayName, componentPrefix);
            
        } catch (Exception e) {
            LOG.warn("自动推断组件库配置失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 从组件库信息中推断组件前缀
     * 
     * @param library 组件库信息
     * @return 推断的组件前缀
     */
    private static String inferComponentPrefix(ComponentLibrary library) {
        if (library == null || library.getComponents() == null || library.getComponents().isEmpty()) {
            return "";
        }
        
        try {
            // 分析组件名称，尝试推断前缀
            String firstComponentName = library.getComponents().get(0).getName();
            if (firstComponentName != null && firstComponentName.contains("-")) {
                // 提取前缀部分（如 "el-button" -> "el-"）
                int dashIndex = firstComponentName.indexOf("-");
                if (dashIndex > 0) {
                    String prefix = firstComponentName.substring(0, dashIndex + 1);
                    LOG.debug("推断组件前缀: " + prefix + " (从组件: " + firstComponentName + ")");
                    return prefix;
                }
            }
            
            // 如果无法推断，尝试从组件库名称推断
            String libraryName = library.getName().toLowerCase();
            // 使用智能推断，避免硬编码特定组件库
            if (libraryName.contains("element")) {
                return "el-";
            } else if (libraryName.contains("ant") || libraryName.contains("antd") || libraryName.contains("design")) {
                return "a-";
            } else if (libraryName.contains("vuetify")) {
                return "v-";
            } else if (libraryName.contains("quasar")) {
                return "q-";
            } else if (libraryName.contains("naive")) {
                return "n-";
            } else if (libraryName.contains("prime")) {
                return "p-";
            } else {
                // 对于未知的组件库，尝试从包名推断前缀
                return inferPrefixFromLibraryName(libraryName);
            }
            
        } catch (Exception e) {
            LOG.debug("推断组件前缀失败", e);
        }
        
        return "";
    }
    
    /**
     * 从组件库名称推断前缀
     */
    private static String inferPrefixFromLibraryName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        // 提取库名的主要部分作为前缀
        String[] parts = libraryName.split("-");
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
     * 在组件库中查找指定组件
     * 
     * @param library 组件库
     * @param componentName 组件名称
     * @return 组件信息，如果找不到则返回 null
     */
    private static ComponentInfo findComponentInLibrary(ComponentLibrary library, String componentName) {
        if (library == null || library.getComponents() == null || componentName == null) {
            return null;
        }
        
        return library.getComponents().stream()
                .filter(comp -> componentName.equals(comp.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 从组件库的sourceUrl推断文档基础URL
     * 
     * @param sourceUrl 组件库的源URL
     * @return 文档基础URL
     */
    private static String inferDocumentationBaseUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
            return "";
        }
        
        // 优先从已安装的组件库中获取文档URL模板
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (sourceUrl.contains(library.getName())) {
                    // 如果组件库有自定义的文档URL模板，使用它
                    // 注意：ComponentLibrary 类目前没有 getDocumentationUrlTemplate 方法
                    // 这里可以后续扩展，暂时使用智能推断
                    break;
                }
            }
        } catch (Exception e) {
            LOG.debug("从已安装组件库获取文档URL失败，使用智能推断: " + e.getMessage());
        }
        
        // 使用智能推断作为后备方案，避免硬编码特定组件库
        return buildGenericDocumentationUrl(sourceUrl);
    }
    
    /**
     * 构建通用的文档URL
     */
    private static String buildGenericDocumentationUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
            return "";
        }
        
        // 尝试从URL推断可能的文档URL模式
        try {
            // 提取域名部分
            String domain = extractDomainFromUrl(sourceUrl);
            if (domain != null && !domain.isEmpty()) {
                return "https://" + domain + "/components/";
            }
        } catch (Exception e) {
            LOG.debug("构建通用文档URL失败: " + e.getMessage());
        }
        
        return "";
    }
    
    /**
     * 从URL提取域名
     */
    private static String extractDomainFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        
        try {
            // 简单的域名提取逻辑
            if (url.startsWith("http://") || url.startsWith("https://")) {
                String domain = url.substring(url.indexOf("://") + 3);
                int slashIndex = domain.indexOf("/");
                if (slashIndex > 0) {
                    domain = domain.substring(0, slashIndex);
                }
                return domain;
            }
        } catch (Exception e) {
            LOG.debug("提取域名失败: " + e.getMessage());
        }
        
        return "";
    }
    
    /**
     * 从组件名称提取组件键值（用于生成文档URL）
     * 
     * @param componentName 组件名称
     * @return 组件键值
     */
    private static String extractComponentKey(String componentName) {
        if (componentName == null) {
            return "";
        }
        
        // 移除组件前缀
        String[] prefixes = {"el-", "a-", "v-", "q-", "n-", "p-"};
        for (String prefix : prefixes) {
            if (componentName.startsWith(prefix)) {
                return componentName.substring(prefix.length());
            }
        }
        
        return componentName;
    }
    
    /**
     * 检查组件库是否已下载
     * 
     * @param libraryId 组件库ID
     * @return 是否已下载
     */
    public static boolean isLibraryDownloaded(String libraryId) {
        return getLibraryById(libraryId) != null;
    }
    
    /**
     * 获取所有已下载的组件库
     * 
     * @return 已下载的组件库列表
     */
    public static List<ComponentLibrary> getAllDownloadedLibraries() {
        try {
            return libraryManager.getAllLibraries();
        } catch (Exception e) {
            LOG.warn("获取已下载的组件库失败", e);
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * 从下载的组件库中获取组件前缀
     * 
     * @param libraryId 组件库ID
     * @return 组件前缀，如果找不到则返回 null
     */
    public static String getComponentPrefixFromDownloadedLibrary(String libraryId) {
        if (libraryId == null) {
            return null;
        }
        
        try {
            ComponentLibrary library = getLibraryById(libraryId);
            if (library == null) {
                return null;
            }
            
            // 从组件库信息中获取前缀
            String prefix = library.getComponentPrefix();
            if (prefix != null && !prefix.trim().isEmpty()) {
                return prefix;
            }
            
            // 如果组件库没有直接提供前缀，尝试推断
            return inferComponentPrefix(library);
            
        } catch (Exception e) {
            LOG.debug("从下载的组件库获取前缀失败: " + libraryId, e);
            return null;
        }
    }
}
