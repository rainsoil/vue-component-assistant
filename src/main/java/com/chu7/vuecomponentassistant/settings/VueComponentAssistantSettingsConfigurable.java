package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import javax.swing.JButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * VueKit 设置配置页面
 * 
 * 在 Settings/Preferences 中显示插件设置
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class VueComponentAssistantSettingsConfigurable implements Configurable {
    
    private JBCheckBox enableComponentCompletion;
    private JBCheckBox enableAttributeCompletion;
    private JBCheckBox enableEventCompletion;
    private JBCheckBox enableSlotCompletion;
    private JBCheckBox enableHoverDocumentation;
    private JBCheckBox enableRightClickDocumentation;
    private JBCheckBox enableCaching;
    private JBCheckBox enableDebugMode;
    private JButton manageComponentLibrariesButton;
    private JBLabel componentLibrariesInfoLabel;
    private JPanel mainPanel;
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "VueKit";
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        // 补全功能设置
        enableComponentCompletion = new JBCheckBox("启用组件补全", true);
        enableAttributeCompletion = new JBCheckBox("启用属性补全", true);
        enableEventCompletion = new JBCheckBox("启用事件补全", true);
        enableSlotCompletion = new JBCheckBox("启用插槽补全", true);
        
        // 文档功能设置
        enableHoverDocumentation = new JBCheckBox("启用悬停文档", true);
        enableRightClickDocumentation = new JBCheckBox("启用右键文档", true);
        
        // 性能设置
        enableCaching = new JBCheckBox("启用缓存优化", true);
        enableDebugMode = new JBCheckBox("启用调试模式", false);
        
        // 组件库管理按钮
        manageComponentLibrariesButton = new JButton("📚 组件库管理");
        manageComponentLibrariesButton.setPreferredSize(new Dimension(200, 30));
        manageComponentLibrariesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取当前打开的项目
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                if (projects.length > 0) {
                    // 打开组件库管理对话框
                    ComponentLibraryManagementDialog dialog = 
                        new ComponentLibraryManagementDialog(projects[0]);
                    dialog.show();
                    // 更新信息显示
                    updateComponentLibrariesInfo();
                } else {
                    JOptionPane.showMessageDialog(
                        mainPanel,
                        "请先打开一个项目，然后再次尝试。",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });
        
        // 组件库信息标签
        componentLibrariesInfoLabel = new JBLabel("正在加载组件库信息...");
        componentLibrariesInfoLabel.setVerticalAlignment(SwingConstants.TOP);
        updateComponentLibrariesInfo();
        
        // 创建组件库管理面板
        JPanel componentLibraryPanel = new JPanel(new BorderLayout());
        componentLibraryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📚 组件库管理"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 创建头部面板，使用FlowLayout自动换行
        JPanel componentLibraryHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        componentLibraryHeaderPanel.add(new JBLabel("管理所有组件库："));
        componentLibraryHeaderPanel.add(new JBLabel("远程组件库、官方组件库、自定义组件库"));
        componentLibraryHeaderPanel.add(manageComponentLibrariesButton);
        
        // 创建内容面板
        JPanel componentLibraryContentPanel = new JPanel(new BorderLayout());
        componentLibraryContentPanel.add(componentLibrariesInfoLabel, BorderLayout.CENTER);
        
        // 创建底部功能说明面板，使用FlowLayout自动换行
        JPanel functionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        functionPanel.add(new JBLabel("功能："));
        functionPanel.add(new JBLabel("查看、新增、删除、导出组件库，导出模板"));
        componentLibraryContentPanel.add(functionPanel, BorderLayout.SOUTH);
        
        componentLibraryPanel.add(componentLibraryHeaderPanel, BorderLayout.NORTH);
        componentLibraryPanel.add(componentLibraryContentPanel, BorderLayout.CENTER);
        
        // 创建主面板
        mainPanel = FormBuilder.createFormBuilder()
            .addSeparator()
            .addComponent(createSectionLabel("补全功能设置"))
            .addComponent(enableComponentCompletion)
            .addComponent(enableAttributeCompletion)
            .addComponent(enableEventCompletion)
            .addComponent(enableSlotCompletion)
            .addSeparator()
            .addComponent(createSectionLabel("文档功能设置"))
            .addComponent(enableHoverDocumentation)
            .addComponent(enableRightClickDocumentation)
            .addSeparator()
            .addComponent(createSectionLabel("性能优化设置"))
            .addComponent(enableCaching)
            .addComponent(enableDebugMode)
            .addSeparator()
            .addComponent(createSectionLabel("组件库管理"))
            .addComponent(componentLibraryPanel)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        return mainPanel;
    }
    
    /**
     * 更新组件库信息显示
     */
    private void updateComponentLibrariesInfo() {
        try {
            List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
                CustomComponentLibraryManager.getAllCustomLibraries();
            
            StringBuilder info = new StringBuilder();
            info.append("<html><body style='width: 300px;'>");
            info.append("<b>📦 已加载的组件库：</b><br>");
            
            // 内置组件库
            info.append("• 远程组件库 (官方市场)<br>");
            info.append("• 自定义组件库 (本地/远程)<br>");
            info.append("• 官方组件库 (官方维护)<br>");
            
            // 自定义组件库
            if (!customLibraries.isEmpty()) {
                info.append("<br><b>📚 自定义组件库：</b><br>");
                for (CustomComponentLibraryManager.CustomLibraryConfig library : customLibraries) {
                    info.append(String.format("• %s (%s) - %d 个组件<br>", 
                        library.getDisplayName(), 
                        library.getName(), 
                        library.getComponents().size()));
                }
            } else {
                info.append("<br><b>📚 自定义组件库：</b> 暂无<br>");
            }
            
            info.append("</body></html>");
            componentLibrariesInfoLabel.setText(info.toString());
        } catch (Exception e) {
            componentLibrariesInfoLabel.setText("加载组件库信息时出错：" + e.getMessage());
        }
    }
    
    @Override
    public boolean isModified() {
        // 检查设置是否有变更
        PluginSettings settings = PluginSettings.getInstance();
        return enableComponentCompletion.isSelected() != settings.isEnableComponentCompletion() ||
               enableAttributeCompletion.isSelected() != settings.isEnableAttributeCompletion() ||
               enableEventCompletion.isSelected() != settings.isEnableEventCompletion() ||
               enableSlotCompletion.isSelected() != settings.isEnableSlotCompletion() ||
               enableHoverDocumentation.isSelected() != settings.isEnableHoverDocumentation() ||
               enableRightClickDocumentation.isSelected() != settings.isEnableRightClickDocumentation() ||
               enableCaching.isSelected() != settings.isEnableCaching() ||
               enableDebugMode.isSelected() != settings.isEnableDebugMode();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        // 保存设置到 PluginSettings
        PluginSettings settings = PluginSettings.getInstance();
        settings.setEnableComponentCompletion(enableComponentCompletion.isSelected());
        settings.setEnableAttributeCompletion(enableAttributeCompletion.isSelected());
        settings.setEnableEventCompletion(enableEventCompletion.isSelected());
        settings.setEnableSlotCompletion(enableSlotCompletion.isSelected());
        settings.setEnableHoverDocumentation(enableHoverDocumentation.isSelected());
        settings.setEnableRightClickDocumentation(enableRightClickDocumentation.isSelected());
        settings.setEnableCaching(enableCaching.isSelected());
        settings.setEnableDebugMode(enableDebugMode.isSelected());
    }
    
    @Override
    public void reset() {
        // 从 PluginSettings 加载设置
        PluginSettings settings = PluginSettings.getInstance();
        enableComponentCompletion.setSelected(settings.isEnableComponentCompletion());
        enableAttributeCompletion.setSelected(settings.isEnableAttributeCompletion());
        enableEventCompletion.setSelected(settings.isEnableEventCompletion());
        enableSlotCompletion.setSelected(settings.isEnableSlotCompletion());
        enableHoverDocumentation.setSelected(settings.isEnableHoverDocumentation());
        enableRightClickDocumentation.setSelected(settings.isEnableRightClickDocumentation());
        enableCaching.setSelected(settings.isEnableCaching());
        enableDebugMode.setSelected(settings.isEnableDebugMode());
    }
    
    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
    
    /**
     * 创建带样式的章节标签
     */
    private JBLabel createSectionLabel(String text) {
        JBLabel label = new JBLabel(text);
        label.setFont(JBUI.Fonts.label(12));
        return label;
    }
} 