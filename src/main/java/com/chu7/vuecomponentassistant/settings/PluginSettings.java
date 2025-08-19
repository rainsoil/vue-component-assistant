package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 插件设置管理类
 * 
 * <p>提供统一的设置管理功能，包括：</p>
 * <ul>
 *   <li>默认组件库设置</li>
 *   <li>补全功能开关</li>
 *   <li>文档显示设置</li>
 *   <li>性能优化设置</li>
 *   <li>缓存配置管理</li>
 *   <li>调试和日志设置</li>
 *   <li>自定义组件库支持</li>
 *   <li>官方组件库市场配置</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>使用IntelliJ IDEA的持久化组件机制</li>
 *   <li>支持全局和项目级设置</li>
 *   <li>动态获取默认值，避免硬编码</li>
 *   <li>完善的配置验证和默认值处理</li>
 *   <li>支持设置的导入导出</li>
 * </ul>
 * 
 * <p>配置分类：</p>
 * <ul>
 *   <li>基础功能设置：补全、文档等核心功能</li>
 *   <li>性能优化设置：缓存大小、过期时间等</li>
 *   <li>高级功能设置：自定义库、官方市场等</li>
 *   <li>调试设置：调试模式、性能日志等</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.components.PersistentStateComponent
 * @see com.intellij.openapi.application.ApplicationManager
 */
@State(
    name = "VueKitSettings",
    storages = @Storage("vuekit-settings.xml")
)
public class PluginSettings implements PersistentStateComponent<PluginSettings> {
    
    /**
     * 默认组件库设置
     * 动态获取，避免硬编码，支持运行时更新
     */
    private String defaultComponentLibrary = null;
    
    /**
     * 补全功能设置
     * 控制各种类型补全功能的启用状态
     */
    private boolean enableComponentCompletion = true;      // 组件补全
    private boolean enableAttributeCompletion = true;      // 属性补全
    private boolean enableEventCompletion = true;          // 事件补全
    private boolean enableSlotCompletion = true;           // 插槽补全
    
    /**
     * 文档显示设置
     * 控制各种文档显示功能的启用状态
     */
    private boolean enableHoverDocumentation = true;       // 悬浮文档
    private boolean enableRightClickDocumentation = true;  // 右键文档
    private boolean enableTableFormatDocumentation = true; // 表格格式文档
    
    /**
     * 性能优化设置
     * 控制缓存和性能相关的配置
     */
    private boolean enableCaching = true;                  // 启用缓存
    private int maxCacheSize = 1000;                       // 最大缓存大小
    private int cacheExpireTime = 300;                     // 缓存过期时间（秒）
    
    /**
     * 高级缓存配置
     * 针对不同类型数据的专门缓存设置
     */
    private int maxCompletionCacheSize = 500;              // 补全缓存大小
    private int maxContextCacheSize = 300;                 // 上下文缓存大小
    private int maxComponentDataCacheSize = 100;           // 组件数据缓存大小
    private int maxDocumentationCacheSize = 200;           // 文档缓存大小
    
    /**
     * 缓存过期时间配置（毫秒）
     * 不同类型数据使用不同的过期时间策略
     */
    private long completionCacheExpireTime = 5 * 60 * 1000;        // 补全缓存：5分钟
    private long contextCacheExpireTime = 3 * 60 * 1000;           // 上下文缓存：3分钟
    private long componentDataCacheExpireTime = 30 * 60 * 1000;    // 组件数据缓存：30分钟
    private long documentationCacheExpireTime = 10 * 60 * 1000;    // 文档缓存：10分钟
    
    /**
     * 缓存清理间隔（毫秒）
     * 定期清理过期缓存，释放内存
     */
    private long cacheCleanupInterval = 2 * 60 * 1000;             // 2分钟清理间隔
    
    /**
     * 调试设置
     * 控制调试模式和性能日志的启用状态
     */
    private boolean enableDebugMode = false;               // 调试模式
    private boolean enablePerformanceLogging = false;      // 性能日志
    
    /**
     * 自定义组件库设置
     * 控制自定义组件库功能的启用状态
     */
    private boolean enableCustomLibrarySupport = true;     // 自定义库支持
    private boolean autoDetectCustomLibraries = true;     // 自动检测自定义库
    
    /**
     * 官方组件库市场设置
     * 配置官方组件库市场的相关参数
     */
    private String officialLibraryMarketUrl = "https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json";
    private boolean enableOfficialLibraryMarket = true;   // 启用官方市场
    private int officialLibraryCacheExpireTime = 60 * 60; // 官方库缓存过期时间（秒）

    /**
     * 获取全局插件设置实例
     * 
     * <p>该方法用于获取应用级别的全局设置实例，
     * 这些设置会影响所有项目。</p>
     * 
     * <p>获取方式：</p>
     * <ul>
     *   <li>通过应用服务获取</li>
     *   <li>支持全局级别的设置管理</li>
     *   <li>自动创建和管理实例</li>
     * </ul>
     *
     * @return 全局插件设置实例
     * 
     * @see com.intellij.openapi.application.ApplicationManager#getApplication()
     */
    public static PluginSettings getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettings.class);
    }
    
    /**
     * 获取项目级插件设置实例
     * 
     * <p>该方法用于获取项目级别的设置实例，
     * 这些设置只影响特定项目。</p>
     * 
     * <p>获取方式：</p>
     * <ul>
     *   <li>通过项目服务获取</li>
     *   <li>支持项目级别的设置隔离</li>
     *   <li>与全局设置分离</li>
     * </ul>
     *
     * @param project 项目对象，不能为null
     * @return 项目级插件设置实例
     * @throws IllegalArgumentException 如果project为null
     * 
     * @see com.intellij.openapi.project.Project#getService(Class)
     */
    public static PluginSettings getInstance(@NotNull Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        return project.getService(PluginSettings.class);
    }
    
    /**
     * 获取当前设置状态
     * 
     * <p>实现PersistentStateComponent接口的方法，
     * 返回当前设置对象用于持久化。</p>
     *
     * @return 当前设置对象
     */
    @Nullable
    @Override
    public PluginSettings getState() {
        return this;
    }
    
    /**
     * 加载设置状态
     * 
     * <p>实现PersistentStateComponent接口的方法，
     * 从持久化存储中加载设置到当前对象。</p>
     * 
     * <p>加载过程：</p>
     * <ul>
     *   <li>使用XmlSerializerUtil复制属性</li>
     *   <li>保持对象引用不变</li>
     *   <li>支持增量更新</li>
     * </ul>
     *
     * @param state 要加载的设置状态，不能为null
     * @throws IllegalArgumentException 如果state为null
     * 
     * @see com.intellij.util.xmlb.XmlSerializerUtil#copyBean(Object, Object)
     */
    @Override
    public void loadState(@NotNull PluginSettings state) {
        if (state == null) {
            throw new IllegalArgumentException("设置状态不能为null");
        }
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // ==================== 默认组件库设置 ====================
    
    /**
     * 获取默认组件库
     * 
     * <p>如果还没有设置默认组件库，会动态获取第一个可用的组件库。
     * 支持运行时更新和自动发现。</p>
     * 
     * <p>获取策略：</p>
     * <ol>
     *   <li>检查是否已设置默认值</li>
     *   <li>如果未设置，从已安装的组件库中获取第一个</li>
     *   <li>返回可用的组件库名称</li>
     * </ol>
     *
     * @return 默认组件库名称，如果没有可用的则返回null
     * 
     * @see #setDefaultComponentLibrary(String)
     */
    public String getDefaultComponentLibrary() {
        // 如果还没有设置默认组件库，动态获取第一个可用的组件库
        if (defaultComponentLibrary == null || defaultComponentLibrary.trim().isEmpty()) {
            try {
                // 从已安装的组件库中获取第一个作为默认值
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                    libraryManager.getAllLibraries();
                
                if (installedLibraries != null && !installedLibraries.isEmpty()) {
                    defaultComponentLibrary = installedLibraries.get(0).getName();
                } else {
                    // 如果没有已安装的组件库，返回空字符串
                    defaultComponentLibrary = "";
                }
            } catch (Exception e) {
                // 如果获取失败，返回空字符串
                defaultComponentLibrary = "";
            }
        }
        return defaultComponentLibrary;
    }
    
    public void setDefaultComponentLibrary(String defaultComponentLibrary) {
        this.defaultComponentLibrary = defaultComponentLibrary;
    }
    
    // ==================== 补全功能设置 ====================
    
    public boolean isEnableComponentCompletion() {
        return enableComponentCompletion;
    }
    
    public void setEnableComponentCompletion(boolean enableComponentCompletion) {
        this.enableComponentCompletion = enableComponentCompletion;
    }
    
    public boolean isEnableAttributeCompletion() {
        return enableAttributeCompletion;
    }
    
    public void setEnableAttributeCompletion(boolean enableAttributeCompletion) {
        this.enableAttributeCompletion = enableAttributeCompletion;
    }
    
    public boolean isEnableEventCompletion() {
        return enableEventCompletion;
    }
    
    public void setEnableEventCompletion(boolean enableEventCompletion) {
        this.enableEventCompletion = enableEventCompletion;
    }
    
    public boolean isEnableSlotCompletion() {
        return enableSlotCompletion;
    }
    
    public void setEnableSlotCompletion(boolean enableSlotCompletion) {
        this.enableSlotCompletion = enableSlotCompletion;
    }
    
    // ==================== 文档显示设置 ====================
    
    public boolean isEnableHoverDocumentation() {
        return enableHoverDocumentation;
    }
    
    public void setEnableHoverDocumentation(boolean enableHoverDocumentation) {
        this.enableHoverDocumentation = enableHoverDocumentation;
    }
    
    public boolean isEnableRightClickDocumentation() {
        return enableRightClickDocumentation;
    }
    
    public void setEnableRightClickDocumentation(boolean enableRightClickDocumentation) {
        this.enableRightClickDocumentation = enableRightClickDocumentation;
    }
    
    public boolean isEnableTableFormatDocumentation() {
        return enableTableFormatDocumentation;
    }
    
    public void setEnableTableFormatDocumentation(boolean enableTableFormatDocumentation) {
        this.enableTableFormatDocumentation = enableTableFormatDocumentation;
    }
    
    // ==================== 性能优化设置 ====================
    
    public boolean isEnableCaching() {
        return enableCaching;
    }
    
    public void setEnableCaching(boolean enableCaching) {
        this.enableCaching = enableCaching;
    }
    
    public int getMaxCacheSize() {
        return maxCacheSize;
    }
    
    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }
    
    public int getCacheExpireTime() {
        return cacheExpireTime;
    }
    
    public void setCacheExpireTime(int cacheExpireTime) {
        this.cacheExpireTime = cacheExpireTime;
    }
    
    // ==================== 高级缓存配置 ====================
    
    public int getMaxCompletionCacheSize() {
        return maxCompletionCacheSize;
    }
    
    public void setMaxCompletionCacheSize(int maxCompletionCacheSize) {
        this.maxCompletionCacheSize = maxCompletionCacheSize;
    }
    
    public int getMaxContextCacheSize() {
        return maxContextCacheSize;
    }
    
    public void setMaxContextCacheSize(int maxContextCacheSize) {
        this.maxContextCacheSize = maxContextCacheSize;
    }
    
    public int getMaxComponentDataCacheSize() {
        return maxComponentDataCacheSize;
    }
    
    public void setMaxComponentDataCacheSize(int maxComponentDataCacheSize) {
        this.maxComponentDataCacheSize = maxComponentDataCacheSize;
    }
    
    public int getMaxDocumentationCacheSize() {
        return maxDocumentationCacheSize;
    }
    
    public void setMaxDocumentationCacheSize(int maxDocumentationCacheSize) {
        this.maxDocumentationCacheSize = maxDocumentationCacheSize;
    }
    
    public long getCompletionCacheExpireTime() {
        return completionCacheExpireTime;
    }
    
    public void setCompletionCacheExpireTime(long completionCacheExpireTime) {
        this.completionCacheExpireTime = completionCacheExpireTime;
    }
    
    public long getContextCacheExpireTime() {
        return contextCacheExpireTime;
    }
    
    public void setContextCacheExpireTime(long contextCacheExpireTime) {
        this.contextCacheExpireTime = contextCacheExpireTime;
    }
    
    public long getComponentDataCacheExpireTime() {
        return componentDataCacheExpireTime;
    }
    
    public void setComponentDataCacheExpireTime(long componentDataCacheExpireTime) {
        this.componentDataCacheExpireTime = componentDataCacheExpireTime;
    }
    
    public long getDocumentationCacheExpireTime() {
        return documentationCacheExpireTime;
    }
    
    public void setDocumentationCacheExpireTime(long documentationCacheExpireTime) {
        this.documentationCacheExpireTime = documentationCacheExpireTime;
    }
    
    public long getCacheCleanupInterval() {
        return cacheCleanupInterval;
    }
    
    public void setCacheCleanupInterval(long cacheCleanupInterval) {
        this.cacheCleanupInterval = cacheCleanupInterval;
    }
    
    // ==================== 调试设置 ====================
    
    public boolean isEnableDebugMode() {
        return enableDebugMode;
    }
    
    public void setEnableDebugMode(boolean enableDebugMode) {
        this.enableDebugMode = enableDebugMode;
    }
    
    public boolean isEnablePerformanceLogging() {
        return enablePerformanceLogging;
    }
    
    public void setEnablePerformanceLogging(boolean enablePerformanceLogging) {
        this.enablePerformanceLogging = enablePerformanceLogging;
    }
    
    // ==================== 自定义组件库设置 ====================
    
    public boolean isEnableCustomLibrarySupport() {
        return enableCustomLibrarySupport;
    }
    
    public void setEnableCustomLibrarySupport(boolean enableCustomLibrarySupport) {
        this.enableCustomLibrarySupport = enableCustomLibrarySupport;
    }
    
    public boolean isAutoDetectCustomLibraries() {
        return autoDetectCustomLibraries;
    }
    
    public void setAutoDetectCustomLibraries(boolean autoDetectCustomLibraries) {
        this.autoDetectCustomLibraries = autoDetectCustomLibraries;
    }
    
    // ==================== 官方组件库市场设置 ====================
    
    public String getOfficialLibraryMarketUrl() {
        return officialLibraryMarketUrl;
    }
    
    public void setOfficialLibraryMarketUrl(String officialLibraryMarketUrl) {
        this.officialLibraryMarketUrl = officialLibraryMarketUrl;
    }
    
    public boolean isEnableOfficialLibraryMarket() {
        return enableOfficialLibraryMarket;
    }
    
    public void setEnableOfficialLibraryMarket(boolean enableOfficialLibraryMarket) {
        this.enableOfficialLibraryMarket = enableOfficialLibraryMarket;
    }
    
    public int getOfficialLibraryCacheExpireTime() {
        return officialLibraryCacheExpireTime;
    }
    
    public void setOfficialLibraryCacheExpireTime(int officialLibraryCacheExpireTime) {
        this.officialLibraryCacheExpireTime = officialLibraryCacheExpireTime;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 重置为默认设置
     */
    public void resetToDefaults() {
        // 动态获取默认组件库，避免硬编码
        try {
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            if (installedLibraries != null && !installedLibraries.isEmpty()) {
                defaultComponentLibrary = installedLibraries.get(0).getName();
            } else {
                defaultComponentLibrary = "";
            }
        } catch (Exception e) {
            defaultComponentLibrary = "";
        }
        enableComponentCompletion = true;
        enableAttributeCompletion = true;
        enableEventCompletion = true;
        enableSlotCompletion = true;
        enableHoverDocumentation = true;
        enableRightClickDocumentation = true;
        enableTableFormatDocumentation = true;
        enableCaching = true;
        maxCacheSize = 1000;
        cacheExpireTime = 300;
        enableDebugMode = false;
        enablePerformanceLogging = false;
        enableCustomLibrarySupport = true;
        autoDetectCustomLibraries = true;
    }
    
    /**
     * 获取设置摘要
     */
    public String getSettingsSummary() {
        return String.format(
            "默认组件库: %s\n" +
            "组件补全: %s\n" +
            "属性补全: %s\n" +
            "事件补全: %s\n" +
            "插槽补全: %s\n" +
            "悬停文档: %s\n" +
            "右键文档: %s\n" +
            "表格格式: %s\n" +
            "缓存启用: %s\n" +
            "调试模式: %s",
            defaultComponentLibrary,
            enableComponentCompletion ? "启用" : "禁用",
            enableAttributeCompletion ? "启用" : "禁用",
            enableEventCompletion ? "启用" : "禁用",
            enableSlotCompletion ? "启用" : "禁用",
            enableHoverDocumentation ? "启用" : "禁用",
            enableRightClickDocumentation ? "启用" : "禁用",
            enableTableFormatDocumentation ? "启用" : "禁用",
            enableCaching ? "启用" : "禁用",
            enableDebugMode ? "启用" : "禁用"
        );
    }
} 