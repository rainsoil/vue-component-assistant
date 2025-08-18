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
            if (libraryName.contains("element")) {
                return "el-";
            } else if (libraryName.contains("ant") || libraryName.contains("antd")) {
                return "a-";
            } else if (libraryName.contains("vuetify")) {
                return "v-";
            } else if (libraryName.contains("quasar")) {
                return "q-";
            } else if (libraryName.contains("naive")) {
                return "n-";
            }
            
        } catch (Exception e) {
            LOG.debug("推断组件前缀失败", e);
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
        
        // 根据不同的组件库推断文档基础URL
        if (sourceUrl.contains("element-plus")) {
            return "https://element-plus.org/zh-CN/component/";
        } else if (sourceUrl.contains("element-ui")) {
            return "https://element.eleme.cn/#/zh-CN/component/";
        } else if (sourceUrl.contains("ant-design-vue")) {
            return "https://antdv.com/components/";
        } else if (sourceUrl.contains("vuetify")) {
            return "https://vuetifyjs.com/en/components/";
        } else if (sourceUrl.contains("quasar")) {
            return "https://quasar.dev/vue-components/";
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
        String[] prefixes = {"el-", "a-", "v-", "q-"};
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
