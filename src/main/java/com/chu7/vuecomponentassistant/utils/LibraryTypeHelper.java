package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager;

/**
 * 组件库类型辅助工具类
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供组件库类型识别和转换功能</li>
 *   <li>支持从组件库名称获取包名、显示名称等</li>
 *   <li>检查组件库是否为已知类型</li>
 *   <li>获取组件前缀和文档URL模板</li>
 *   <li>验证组件是否属于指定组件库</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>使用动态配置管理器，支持运行时配置更新</li>
 *   <li>支持模糊匹配和自动推断</li>
 *   <li>完善的错误处理和默认值处理</li>
 *   <li>线程安全的静态方法设计</li>
 * </ul>
 * 
 * <p>配置策略：</p>
 * <ol>
 *   <li>优先使用动态配置管理器</li>
 *   <li>尝试模糊匹配和自动推断</li>
 *   <li>使用默认值作为后备方案</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库类型识别和验证</li>
 *   <li>组件前缀匹配和过滤</li>
 *   <li>文档链接生成</li>
 *   <li>组件归属判断</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 3.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager
 * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryInfoProvider
 */
public class LibraryTypeHelper {
    
    /**
     * 日志记录器
     * 用于记录组件库类型识别过程中的关键信息和错误
     */
    private static final Logger LOG = Logger.getInstance(LibraryTypeHelper.class);
    
    /**
     * 未知组件库标识
     * 当无法识别组件库类型时使用的默认值
     */
    public static final String UNKNOWN = "unknown";
    
    /**
     * 未知组件库显示名称
     * 在用户界面中显示的未知组件库名称
     */
    public static final String UNKNOWN_DISPLAY = "未知组件库";
    
    /**
     * 动态配置管理器
     * 用于获取组件库的配置信息，支持运行时更新
     */
    private static final DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
    
    /**
     * 从组件库名称获取包名
     * 
     * <p>该方法会尝试多种方式获取组件库的包名：</p>
     * <ol>
     *   <li>使用动态配置管理器获取精确配置</li>
     *   <li>尝试模糊匹配和自动推断</li>
     *   <li>自动推断组件库配置</li>
     *   <li>返回找到的包名或UNKNOWN</li>
     * </ol>
     * 
     * <p>获取策略：</p>
     * <ul>
     *   <li>优先使用已配置的包名</li>
     *   <li>支持模糊匹配和自动推断</li>
     *   <li>自动更新组件库配置</li>
     *   <li>完善的错误处理机制</li>
     * </ul>
     * 
     * @param libraryName 组件库名称，不能为null或空字符串
     * @return 对应的包名，如果无法获取则返回UNKNOWN
     * 
     * @see #UNKNOWN
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager#getLibraryConfig(String)
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryInfoProvider#autoInferLibraryConfig(String)
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
     * <p>该方法用于获取组件库在用户界面中显示的友好名称。
     * 支持多种获取策略和后备方案。</p>
     * 
     * <p>获取策略：</p>
     * <ol>
     *   <li>使用动态配置管理器获取精确配置</li>
     *   <li>尝试模糊匹配和自动推断</li>
     *   <li>使用UNKNOWN_DISPLAY作为后备方案</li>
     * </ol>
     * 
     * @param libraryName 组件库名称，不能为null或空字符串
     * @return 对应的显示名称，如果无法获取则返回UNKNOWN_DISPLAY
     * 
     * @see #UNKNOWN_DISPLAY
     * @see #getPackageName(String)
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
     * <p>该方法用于验证组件库是否为系统已知的类型。
     * 通过动态配置管理器进行检查。</p>
     * 
     * @param libraryName 组件库名称，不能为null或空字符串
     * @return 如果是已知组件库则返回true，否则返回false
     * 
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager#isKnownLibrary(String)
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
     * <p>该方法用于获取组件库的组件前缀，如 "el-"、"a-" 等。
     * 用于识别组件是否属于特定组件库。</p>
     * 
     * @param libraryName 组件库名称，不能为null或空字符串
     * @return 组件前缀，如果无法获取则返回空字符串
     * 
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager#getComponentPrefix(String)
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
     * 
     * <p>该方法用于获取组件库的文档URL模板。
     * 注意：文档URL模板应该从下载的组件库中获取，而不是从静态配置中获取。</p>
     * 
     * <p>当前实现：</p>
     * <ul>
     *   <li>返回空字符串，表示需要从动态加载的组件库中获取</li>
     *   <li>为未来扩展预留接口</li>
     *   <li>支持运行时配置更新</li>
     * </ul>
     * 
     * @param libraryName 组件库名称，不能为null或空字符串
     * @return 文档URL模板，当前返回空字符串
     * 
     * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary#getDocumentationUrl()
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
     * <p>该方法通过组件前缀判断组件是否属于指定的组件库。
     * 支持多种组件库的识别。</p>
     * 
     * <p>判断逻辑：</p>
     * <ol>
     *   <li>获取组件库的组件前缀</li>
     *   <li>检查组件名称是否以该前缀开头</li>
     *   <li>返回归属判断结果</li>
     * </ol>
     * 
     * @param componentName 组件名称，不能为null
     * @param libraryName 组件库名称，不能为null
     * @return 如果组件属于指定组件库则返回true，否则返回false
     * 
     * @see #getComponentPrefix(String)
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
     * 
     * <p>该方法用于获取组件库的组件数据路径。
     * 注意：组件数据路径应该从下载的组件库中获取，而不是从静态配置中获取。</p>
     * 
     * <p>当前实现：</p>
     * <ul>
     *   <li>返回空字符串，表示需要从动态加载的组件库中获取</li>
     *   <li>为未来扩展预留接口</li>
     *   <li>支持运行时配置更新</li>
     * </ul>
     * 
     * @param libraryName 组件库名称，不能为null或空字符串
     * @return 组件数据路径，当前返回空字符串
     * 
     * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary#getComponentDataPath()
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
