package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Element Plus 插件设置配置页面
 */
public class ElementPlusSettingsConfigurable implements Configurable {
    
    private JBCheckBox enableAutoCompletion;
    private JBCheckBox enableDocumentation;
    private JBCheckBox enableSmartContext;
    private JBCheckBox enableRightClickMenu;
    private JBTextField customComponentPath;
    private JPanel mainPanel;
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Element Plus Assistant";
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        enableAutoCompletion = new JBCheckBox("启用自动补全", true);
        enableDocumentation = new JBCheckBox("启用文档提示", true);
        enableSmartContext = new JBCheckBox("启用智能上下文分析", true);
        enableRightClickMenu = new JBCheckBox("启用右键菜单", true);
        customComponentPath = new JBTextField();
        
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("功能设置:"), new JPanel())
                .addComponent(enableAutoCompletion)
                .addComponent(enableDocumentation)
                .addComponent(enableSmartContext)
                .addComponent(enableRightClickMenu)
                .addSeparator()
                .addLabeledComponent(new JBLabel("自定义组件库路径:"), customComponentPath)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        
        return mainPanel;
    }
    
    @Override
    public boolean isModified() {
        ElementPlusSettings settings = ElementPlusSettings.getInstance();
        return enableAutoCompletion.isSelected() != settings.isAutoCompletionEnabled() ||
               enableDocumentation.isSelected() != settings.isDocumentationEnabled() ||
               enableSmartContext.isSelected() != settings.isSmartContextEnabled() ||
               enableRightClickMenu.isSelected() != settings.isRightClickMenuEnabled() ||
               !customComponentPath.getText().equals(settings.getCustomComponentPath());
    }
    
    @Override
    public void apply() throws ConfigurationException {
        ElementPlusSettings settings = ElementPlusSettings.getInstance();
        settings.setAutoCompletionEnabled(enableAutoCompletion.isSelected());
        settings.setDocumentationEnabled(enableDocumentation.isSelected());
        settings.setSmartContextEnabled(enableSmartContext.isSelected());
        settings.setRightClickMenuEnabled(enableRightClickMenu.isSelected());
        settings.setCustomComponentPath(customComponentPath.getText());
    }
    
    @Override
    public void reset() {
        ElementPlusSettings settings = ElementPlusSettings.getInstance();
        enableAutoCompletion.setSelected(settings.isAutoCompletionEnabled());
        enableDocumentation.setSelected(settings.isDocumentationEnabled());
        enableSmartContext.setSelected(settings.isSmartContextEnabled());
        enableRightClickMenu.setSelected(settings.isRightClickMenuEnabled());
        customComponentPath.setText(settings.getCustomComponentPath());
    }
    
    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
}
