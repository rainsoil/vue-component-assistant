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
 * 提供统一的设置管理功能，包括：
 * - 默认组件库设置
 * - 补全功能开关
 * - 文档显示设置
 * - 性能优化设置
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
@State(
    name = "VueKitSettings",
    storages = @Storage("vuekit-settings.xml")
)
public class PluginSettings implements PersistentStateComponent<PluginSettings> {
    
    // 默认组件库设置
    private String defaultComponentLibrary = "element-plus";
    
    // 补全功能设置
    private boolean enableComponentCompletion = true;
    private boolean enableAttributeCompletion = true;
    private boolean enableEventCompletion = true;
    private boolean enableSlotCompletion = true;
    
    // 文档显示设置
    private boolean enableHoverDocumentation = true;
    private boolean enableRightClickDocumentation = true;
    private boolean enableTableFormatDocumentation = true;
    
    // 性能优化设置
    private boolean enableCaching = true;
    private int maxCacheSize = 1000;
    private int cacheExpireTime = 300; // 5分钟
    
    // 高级缓存配置
    private int maxCompletionCacheSize = 500;
    private int maxContextCacheSize = 300;
    private int maxComponentDataCacheSize = 100;
    private int maxDocumentationCacheSize = 200;
    
    private long completionCacheExpireTime = 5 * 60 * 1000; // 5分钟
    private long contextCacheExpireTime = 3 * 60 * 1000; // 3分钟
    private long componentDataCacheExpireTime = 30 * 60 * 1000; // 30分钟
    private long documentationCacheExpireTime = 10 * 60 * 1000; // 10分钟
    
    private long cacheCleanupInterval = 2 * 60 * 1000; // 2分钟清理间隔
    
    // 调试设置
    private boolean enableDebugMode = false;
    private boolean enablePerformanceLogging = false;
    
    // 自定义组件库设置
    private boolean enableCustomLibrarySupport = true;
    private boolean autoDetectCustomLibraries = true;
    
    // 官方组件库市场设置
    private String officialLibraryMarketUrl = "https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json";
    private boolean enableOfficialLibraryMarket = true;
    private int officialLibraryCacheExpireTime = 60 * 60; // 1小时
    
    public static PluginSettings getInstance() {
        return ApplicationManager.getApplication().getService(PluginSettings.class);
    }
    
    public static PluginSettings getInstance(@NotNull Project project) {
        return project.getService(PluginSettings.class);
    }
    
    @Nullable
    @Override
    public PluginSettings getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull PluginSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // ==================== 默认组件库设置 ====================
    
    public String getDefaultComponentLibrary() {
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
        defaultComponentLibrary = "element-plus";
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