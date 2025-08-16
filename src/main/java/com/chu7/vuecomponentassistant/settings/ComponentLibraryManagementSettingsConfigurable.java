package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 组件库管理设置页面
 * 
 * 功能说明：
 * - 作为 Vue Kit 设置组下的子项
 * - 上半部分：全局功能设置（补全、文档、性能优化）
 * - 下半部分：组件库管理功能
 * - 集成到 Settings/Tools/Vue Kit/组件库管理 目录下
 * 
 * @author VueKit Team
 * @version 3.2.0
 */
public class ComponentLibraryManagementSettingsConfigurable implements Configurable {

    // 全局功能设置组件
    private JBCheckBox enableComponentCompletion;
    private JBCheckBox enableAttributeCompletion;
    private JBCheckBox enableEventCompletion;
    private JBCheckBox enableSlotCompletion;
    private JBCheckBox enableHoverDocumentation;
    private JBCheckBox enableRightClickDocumentation;
    private JBCheckBox enableCaching;
    private JBCheckBox enableDebugMode;
    
    // 组件库管理组件
    private JPanel mainPanel;
    private JButton componentLibraryManagementButton;
    private JButton officialLibraryMarketButton;
    private JButton customLibraryManagementButton;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "组件库管理";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (mainPanel == null) {
            // 初始化全局功能设置组件
            initializeGlobalSettingsComponents();
            
            mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // 创建全局功能设置面板（上半部分）
            JPanel globalSettingsPanel = createGlobalSettingsPanel();
            mainPanel.add(globalSettingsPanel, BorderLayout.NORTH);
            
            // 创建分隔线
            JSeparator separator = new JSeparator();
            separator.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            mainPanel.add(separator, BorderLayout.CENTER);
            
            // 创建组件库管理面板（下半部分）
            JPanel libraryManagementPanel = createLibraryManagementPanel();
            mainPanel.add(libraryManagementPanel, BorderLayout.SOUTH);
        }
        return mainPanel;
    }
    
    private void initializeGlobalSettingsComponents() {
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
    }
    
    private JPanel createGlobalSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("🌐 全局功能设置"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 创建功能设置表单
        JPanel settingsPanel = FormBuilder.createFormBuilder()
            .addComponent(createSectionLabel("🎯 补全功能设置"))
            .addComponent(enableComponentCompletion)
            .addComponent(enableAttributeCompletion)
            .addComponent(enableEventCompletion)
            .addComponent(enableSlotCompletion)
            .addSeparator()
            .addComponent(createSectionLabel("📖 文档功能设置"))
            .addComponent(enableHoverDocumentation)
            .addComponent(enableRightClickDocumentation)
            .addSeparator()
            .addComponent(createSectionLabel("⚡ 性能优化设置"))
            .addComponent(enableCaching)
            .addComponent(enableDebugMode)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        panel.add(settingsPanel, BorderLayout.CENTER);
        return panel;
    }
    

    
    private JBLabel createSectionLabel(String text) {
        JBLabel label = new JBLabel(text);
        label.setFont(JBUI.Fonts.label(12));
        return label;
    }
    
    private JPanel createLibraryManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📚 组件库管理"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建功能按钮面板
        JPanel buttonsPanel = createButtonsPanel();
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        // 创建说明面板
        JPanel descriptionPanel = createDescriptionPanel();
        panel.add(descriptionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("组件库管理");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(new Color(51, 51, 51));
        panel.add(titleLabel);
        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // 组件库管理按钮
        componentLibraryManagementButton = createStyledButton("🌐 组件库管理", 
            "管理VueKit组件库，包括启用/禁用、同步等功能");
        componentLibraryManagementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openComponentLibraryManagement();
            }
        });
        
        // 官方组件库市场按钮
        officialLibraryMarketButton = createStyledButton("📦 官方组件库市场", 
            "浏览和下载官方组件库");
        officialLibraryMarketButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openOfficialLibraryMarket();
            }
        });
        
        // 自定义组件库管理按钮
        customLibraryManagementButton = createStyledButton("📚 自定义组件库管理", 
            "管理自定义组件库和组件");
        customLibraryManagementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCustomLibraryManagement();
            }
        });
        
        panel.add(componentLibraryManagementButton);
        panel.add(officialLibraryMarketButton);
        panel.add(customLibraryManagementButton);
        
        return panel;
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 14f));
        button.setPreferredSize(new Dimension(200, 80));
        button.setBackground(new Color(240, 248, 255));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("功能说明"));
        
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setText(
            "组件库管理功能提供完整的 Vue.js 组件库管理解决方案：\n\n" +
            "• 组件库管理：启用/禁用、同步、优先级配置等\n" +
            "• 官方市场：浏览和下载官方组件库\n" +
            "• 自定义管理：导入、管理自定义组件库\n" +
            "• 智能检测：自动检测项目中的组件库\n" +
            "• 多库支持：Element Plus、Element UI、Ant Design Vue、Vuetify、Quasar 等\n\n" +
            "点击上方按钮访问相应功能。"
        );
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(Font.PLAIN, 12f));
        
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(600, 150));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void openComponentLibraryManagement() {
        try {
            // 直接打开组件库管理对话框
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog dialog = 
                    new com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog(currentProject);
                dialog.show();
            } else {
                showError("项目未找到", "请确保当前有打开的项目");
            }
        } catch (Exception e) {
            showError("打开组件库管理失败", e.getMessage());
        }
    }

    private void openOfficialLibraryMarket() {
        try {
            // 直接打开官方组件库市场对话框
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog dialog = 
                    new com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog(currentProject, libraryManager);
                dialog.show();
            } else {
                showError("项目未找到", "请确保当前有打开的项目");
            }
        } catch (Exception e) {
            showError("打开官方组件库市场失败", e.getMessage());
        }
    }

    private void openCustomLibraryManagement() {
        try {
            // 使用自定义组件库上传对话框作为替代
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog dialog = 
                    new com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog(currentProject, libraryManager);
                dialog.show();
            } else {
                showError("项目未找到", "请确保当前有打开的项目");
            }
        } catch (Exception e) {
            showError("打开自定义组件库管理失败", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(mainPanel, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private Project getCurrentProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return projects[0]; // 返回第一个打开的项目
        }
        return null;
    }



    @Override
    public boolean isModified() {
        // 检查全局功能设置是否有变更
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
    public void apply() {
        // 保存全局功能设置到 PluginSettings
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
        // 从 PluginSettings 加载全局功能设置
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
}
