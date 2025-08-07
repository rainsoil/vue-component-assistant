package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Element Plus 插件设置管理
 */
@State(
    name = "com.chu7.vuecomponentassistant.settings.ElementPlusSettings",
    storages = @Storage("ElementPlusAssistantSettings.xml")
)
public class ElementPlusSettings implements PersistentStateComponent<ElementPlusSettings> {
    
    private boolean autoCompletionEnabled = true;
    private boolean documentationEnabled = true;
    private boolean smartContextEnabled = true;
    private boolean rightClickMenuEnabled = true;
    private String customComponentPath = "";
    
    public static ElementPlusSettings getInstance() {
        return ApplicationManager.getApplication().getService(ElementPlusSettings.class);
    }
    
    @Nullable
    @Override
    public ElementPlusSettings getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull ElementPlusSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    // Getter 和 Setter 方法
    public boolean isAutoCompletionEnabled() {
        return autoCompletionEnabled;
    }
    
    public void setAutoCompletionEnabled(boolean autoCompletionEnabled) {
        this.autoCompletionEnabled = autoCompletionEnabled;
    }
    
    public boolean isDocumentationEnabled() {
        return documentationEnabled;
    }
    
    public void setDocumentationEnabled(boolean documentationEnabled) {
        this.documentationEnabled = documentationEnabled;
    }
    
    public boolean isSmartContextEnabled() {
        return smartContextEnabled;
    }
    
    public void setSmartContextEnabled(boolean smartContextEnabled) {
        this.smartContextEnabled = smartContextEnabled;
    }
    
    public boolean isRightClickMenuEnabled() {
        return rightClickMenuEnabled;
    }
    
    public void setRightClickMenuEnabled(boolean rightClickMenuEnabled) {
        this.rightClickMenuEnabled = rightClickMenuEnabled;
    }
    
    public String getCustomComponentPath() {
        return customComponentPath;
    }
    
    public void setCustomComponentPath(String customComponentPath) {
        this.customComponentPath = customComponentPath;
    }
}
