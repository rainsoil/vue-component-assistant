package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Vue Component 插件设置管理
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理Vue组件助手的核心功能开关</li>
 *   <li>提供用户可配置的插件行为设置</li>
 *   <li>支持设置的持久化存储和恢复</li>
 *   <li>集成IntelliJ IDEA的设置系统</li>
 *   <li>提供全局单例访问点</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>持久化组件：实现PersistentStateComponent接口</li>
 *   <li>XML存储：使用XML格式保存设置到磁盘</li>
 *   <li>单例模式：通过ApplicationManager提供全局访问</li>
 *   <li>自动序列化：使用XmlSerializerUtil自动处理序列化</li>
 *   <li>状态管理：支持设置的加载和保存</li>
 * </ul>
 * 
 * <p>核心功能设置：</p>
 * <ol>
 *   <li>自动补全：控制代码补全功能的启用状态</li>
 *   <li>文档显示：控制组件文档的显示功能</li>
 *   <li>智能上下文：控制上下文分析功能的启用</li>
 *   <li>右键菜单：控制右键菜单功能的启用</li>
 *   <li>自定义组件路径：指定自定义组件的搜索路径</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>插件功能开关控制</li>
 *   <li>用户偏好设置管理</li>
 *   <li>插件行为配置</li>
 *   <li>设置界面的数据绑定</li>
 *   <li>插件状态的持久化</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.components.PersistentStateComponent
 * @see com.intellij.openapi.components.State
 * @see com.intellij.openapi.components.Storage
 * @see com.intellij.openapi.application.ApplicationManager
 * @see com.intellij.util.xmlb.XmlSerializerUtil
 */
@State(
    name = "com.chu7.vuecomponentassistant.settings.VueComponentSettings",
    storages = @Storage("VueComponentAssistantSettings.xml")
)
public class VueComponentSettings implements PersistentStateComponent<VueComponentSettings> {
    
    /**
     * 自动补全功能启用状态
     * 控制Vue组件的代码补全功能是否启用，默认为true
     */
    private boolean autoCompletionEnabled = true;
    
    /**
     * 文档显示功能启用状态
     * 控制组件文档的显示功能是否启用，默认为true
     */
    private boolean documentationEnabled = true;
    
    /**
     * 智能上下文分析功能启用状态
     * 控制基于上下文的智能分析功能是否启用，默认为true
     */
    private boolean smartContextEnabled = true;
    
    /**
     * 右键菜单功能启用状态
     * 控制右键菜单中的Vue组件相关功能是否启用，默认为true
     */
    private boolean rightClickMenuEnabled = true;
    
    /**
     * 自定义组件搜索路径
     * 指定用户自定义组件的搜索路径，默认为空字符串
     */
    private String customComponentPath = "";
    
    /**
     * 获取VueComponentSettings的单例实例
     * 
     * <p>该方法通过IntelliJ IDEA的ApplicationManager获取VueComponentSettings服务实例。
     * 确保整个应用程序中只有一个设置实例，支持全局访问。</p>
     * 
     * <p>获取策略：</p>
     * <ul>
     *   <li>使用ApplicationManager.getApplication()获取应用实例</li>
     *   <li>通过getService方法获取VueComponentSettings服务</li>
     *   <li>返回全局唯一的设置实例</li>
     * </ul>
     * 
     * @return VueComponentSettings的单例实例，不会为null
     * 
     * @see com.intellij.openapi.application.ApplicationManager#getApplication()
     * @see com.intellij.openapi.application.Application#getService(Class)
     */
    public static VueComponentSettings getInstance() {
        return ApplicationManager.getApplication().getService(VueComponentSettings.class);
    }
    
    /**
     * 获取当前设置状态
     * 
     * <p>该方法实现PersistentStateComponent接口，返回当前对象的引用。
     * IntelliJ IDEA使用此方法获取需要持久化的设置状态。</p>
     * 
     * @return 当前设置对象的引用
     * 
     * @see com.intellij.openapi.components.PersistentStateComponent#getState()
     */
    @Nullable
    @Override
    public VueComponentSettings getState() {
        return this;
    }
    
    /**
     * 加载设置状态
     * 
     * <p>该方法实现PersistentStateComponent接口，用于从持久化存储中恢复设置状态。
     * 使用XmlSerializerUtil将传入的状态对象复制到当前对象。</p>
     * 
     * @param state 要加载的设置状态对象，不能为null
     * 
     * @see com.intellij.openapi.components.PersistentStateComponent#loadState(Object)
     * @see com.intellij.util.xmlb.XmlSerializerUtil#copyBean(Object, Object)
     */
    @Override
    public void loadState(@NotNull VueComponentSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    /**
     * 检查自动补全功能是否启用
     * 
     * @return 如果自动补全功能启用则返回true，否则返回false
     */
    public boolean isAutoCompletionEnabled() {
        return autoCompletionEnabled;
    }
    
    /**
     * 设置自动补全功能的启用状态
     * 
     * @param autoCompletionEnabled 是否启用自动补全功能
     */
    public void setAutoCompletionEnabled(boolean autoCompletionEnabled) {
        this.autoCompletionEnabled = autoCompletionEnabled;
    }
    
    /**
     * 检查文档显示功能是否启用
     * 
     * @return 如果文档显示功能启用则返回true，否则返回false
     */
    public boolean isDocumentationEnabled() {
        return documentationEnabled;
    }
    
    /**
     * 设置文档显示功能的启用状态
     * 
     * @param documentationEnabled 是否启用文档显示功能
     */
    public void setDocumentationEnabled(boolean documentationEnabled) {
        this.documentationEnabled = documentationEnabled;
    }
    
    /**
     * 检查智能上下文分析功能是否启用
     * 
     * @return 如果智能上下文分析功能启用则返回true，否则返回false
     */
    public boolean isSmartContextEnabled() {
        return smartContextEnabled;
    }
    
    /**
     * 设置智能上下文分析功能的启用状态
     * 
     * @param smartContextEnabled 是否启用智能上下文分析功能
     */
    public void setSmartContextEnabled(boolean smartContextEnabled) {
        this.smartContextEnabled = smartContextEnabled;
    }
    
    /**
     * 检查右键菜单功能是否启用
     * 
     * @return 如果右键菜单功能启用则返回true，否则返回false
     */
    public boolean isRightClickMenuEnabled() {
        return rightClickMenuEnabled;
    }
    
    /**
     * 设置右键菜单功能的启用状态
     * 
     * @param rightClickMenuEnabled 是否启用右键菜单功能
     */
    public void setRightClickMenuEnabled(boolean rightClickMenuEnabled) {
        this.rightClickMenuEnabled = rightClickMenuEnabled;
    }
    
    /**
     * 获取自定义组件搜索路径
     * 
     * @return 自定义组件的搜索路径，如果未设置则返回空字符串
     */
    public String getCustomComponentPath() {
        return customComponentPath;
    }
    
    /**
     * 设置自定义组件搜索路径
     * 
     * @param customComponentPath 自定义组件的搜索路径，可以为null或空字符串
     */
    public void setCustomComponentPath(String customComponentPath) {
        this.customComponentPath = customComponentPath;
    }
} 