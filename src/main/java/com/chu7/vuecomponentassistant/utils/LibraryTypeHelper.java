package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager;

/**
 * 组件库类型辅助工具类
 * 使用动态配置管理器，支持从配置文件动态加载组件库信息
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class LibraryTypeHelper {
    
    /** 日志记录器 */
    private static final Logger LOG = Logger.getInstance(LibraryTypeHelper.class);
    
    /** 未知组件库标识 */
    public static final String UNKNOWN = "unknown";
    
    /** 未知组件库显示名称 */
    public static final String UNKNOWN_DISPLAY = "未知组件库";
    
    /** 动态配置管理器 */
    private static final DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
    
    /**
     * 从组件库名称获取包名
     * 
     * @param libraryName 组件库名称
     * @return 包名
     */
    public static String getPackageName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        // 使用动态配置管理器获取包名
        DynamicLibraryConfigManager.LibraryConfig config = configManager.getLibraryConfig(libraryName);
        if (config != null && config.getPackageName() != null) {
            return config.getPackageName();
        }
        
        // 如果找不到配置，尝试模糊匹配
        String inferredType = configManager.inferLibraryTypeFromPackageName(libraryName);
        if (inferredType != null) {
            return inferredType;
        }
        
        // 如果还是找不到，尝试从下载的组件库中自动推断
        try {
            if (DynamicLibraryInfoProvider.autoInferLibraryConfig(libraryName)) {
                // 重新尝试获取配置
                config = configManager.getLibraryConfig(libraryName);
                if (config != null && config.getPackageName() != null) {
                    return config.getPackageName();
                }
            }
        } catch (Exception e) {
            LOG.debug("自动推断组件库配置失败: " + libraryName, e);
        }
        
        return UNKNOWN;
    }
    
    /**
     * 从组件库名称获取显示名称
     * 
     * @param libraryName 组件库名称
     * @return 显示名称
     */
    public static String getDisplayName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return UNKNOWN_DISPLAY;
        }
        
        // 使用动态配置管理器获取显示名称
        DynamicLibraryConfigManager.LibraryConfig config = configManager.getLibraryConfig(libraryName);
        if (config != null && config.getDisplayName() != null) {
            return config.getDisplayName();
        }
        
        // 如果找不到配置，尝试模糊匹配
        String inferredType = configManager.inferLibraryTypeFromPackageName(libraryName);
        if (inferredType != null) {
            config = configManager.getLibraryConfig(inferredType);
            if (config != null && config.getDisplayName() != null) {
                return config.getDisplayName();
            }
        }
        
        return UNKNOWN_DISPLAY;
    }
    
    /**
     * 检查是否是已知的组件库
     * 
     * @param libraryName 组件库名称
     * @return 是否是已知组件库
     */
    public static boolean isKnownLibrary(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return false;
        }
        
        // 使用动态配置管理器检查
        return configManager.isKnownLibrary(libraryName);
    }
    
    /**
     * 获取组件前缀
     * 
     * @param libraryName 组件库名称
     * @return 组件前缀
     */
    public static String getComponentPrefix(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        // 使用动态配置管理器获取组件前缀
        return configManager.getComponentPrefix(libraryName);
    }
    
    /**
     * 获取文档URL模板
     * 从下载的组件库中获取，而不是从静态配置中获取
     * 
     * @param libraryName 组件库名称
     * @return 文档URL模板
     */
    public static String getDocumentationUrlTemplate(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        // 文档URL模板应该从下载的组件库中获取
        // 这里返回空字符串，表示需要从动态加载的组件库中获取
        return "";
    }
    
    /**
     * 检查组件是否属于指定组件库
     * 
     * @param componentName 组件名称
     * @param libraryName 组件库名称
     * @return 是否属于指定组件库
     */
    public static boolean isComponentFromLibrary(String componentName, String libraryName) {
        if (componentName == null || libraryName == null) {
            return false;
        }
        
        String prefix = getComponentPrefix(libraryName);
        if (prefix.isEmpty()) {
            return false;
        }
        
        return componentName.startsWith(prefix);
    }
    
    /**
     * 获取组件数据路径
     * 从下载的组件库中获取，而不是从静态配置中获取
     * 
     * @param libraryName 组件库名称
     * @return 组件数据路径
     */
    public static String getComponentDataPath(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        // 组件数据路径应该从下载的组件库中获取
        // 这里返回空字符串，表示需要从动态加载的组件库中获取
        return "";
    }
}
