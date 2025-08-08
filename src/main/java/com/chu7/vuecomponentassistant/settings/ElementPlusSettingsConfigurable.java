package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import javax.swing.JButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.action.CustomLibraryManagementAction;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * Element Plus 插件设置配置页面
 */
public class ElementPlusSettingsConfigurable implements Configurable {
    
    private JBCheckBox enableAutoCompletion;
    private JBCheckBox enableDocumentation;
    private JBCheckBox enableSmartContext;
    private JBCheckBox enableRightClickMenu;
    private JButton manageComponentLibrariesButton;
    private JBLabel componentLibrariesInfoLabel;
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
        
        // 组件库管理按钮
        manageComponentLibrariesButton = new JButton("📚 组件库管理");
        manageComponentLibrariesButton.setPreferredSize(new Dimension(200, 30));
        manageComponentLibrariesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取当前打开的项目
                Project[] projects = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects();
                if (projects.length > 0) {
                    // 打开组件库管理对话框
                    com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog dialog = 
                        new com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog(projects[0]);
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
        updateComponentLibrariesInfo();
        
        // 创建组件库管理面板
        JPanel componentLibraryPanel = new JPanel(new BorderLayout());
        componentLibraryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JPanel componentLibraryHeaderPanel = new JPanel(new BorderLayout());
        componentLibraryHeaderPanel.add(new JBLabel("📚 组件库管理"), BorderLayout.WEST);
        componentLibraryHeaderPanel.add(manageComponentLibrariesButton, BorderLayout.EAST);
        
        JPanel componentLibraryContentPanel = new JPanel(new BorderLayout());
        componentLibraryContentPanel.add(componentLibrariesInfoLabel, BorderLayout.CENTER);
        componentLibraryContentPanel.add(new JBLabel("功能：查看、新增、删除、导出组件库，导出模板"), BorderLayout.SOUTH);
        
        componentLibraryPanel.add(componentLibraryHeaderPanel, BorderLayout.NORTH);
        componentLibraryPanel.add(componentLibraryContentPanel, BorderLayout.CENTER);
        
        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("功能设置:"), new JPanel())
                .addComponent(enableAutoCompletion)
                .addComponent(enableDocumentation)
                .addComponent(enableSmartContext)
                .addComponent(enableRightClickMenu)
                .addSeparator()
                .addComponent(componentLibraryPanel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        
        return mainPanel;
    }
    
    /**
     * 更新组件库信息
     */
    private void updateComponentLibrariesInfo() {
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
            CustomComponentLibraryManager.getAllCustomLibraries();
        
        StringBuilder info = new StringBuilder();
        info.append("📚 组件库状态：\n");
        info.append("• 内置组件库：3个（Element Plus、Element UI、Ant Design Vue）\n");
        info.append("• 自定义组件库：").append(customLibraries.size()).append("个\n");
        
        if (!customLibraries.isEmpty()) {
            info.append("\n📦 已加载的自定义组件库：\n");
            for (int i = 0; i < customLibraries.size(); i++) {
                CustomComponentLibraryManager.CustomLibraryConfig config = customLibraries.get(i);
                info.append("  ").append(i + 1).append(". ").append(config.getDisplayName())
                    .append(" (").append(config.getComponents().size()).append(" 个组件)\n");
            }
        }
        
        componentLibrariesInfoLabel.setText(info.toString());
        componentLibrariesInfoLabel.setForeground(Color.BLACK);
    }
    
    @Override
    public boolean isModified() {
        ElementPlusSettings settings = ElementPlusSettings.getInstance();
        return enableAutoCompletion.isSelected() != settings.isAutoCompletionEnabled() ||
               enableDocumentation.isSelected() != settings.isDocumentationEnabled() ||
               enableSmartContext.isSelected() != settings.isSmartContextEnabled() ||
               enableRightClickMenu.isSelected() != settings.isRightClickMenuEnabled();
    }
    
    @Override
    public void apply() throws ConfigurationException {
        ElementPlusSettings settings = ElementPlusSettings.getInstance();
        settings.setAutoCompletionEnabled(enableAutoCompletion.isSelected());
        settings.setDocumentationEnabled(enableDocumentation.isSelected());
        settings.setSmartContextEnabled(enableSmartContext.isSelected());
        settings.setRightClickMenuEnabled(enableRightClickMenu.isSelected());
    }
    
    @Override
    public void reset() {
        ElementPlusSettings settings = ElementPlusSettings.getInstance();
        enableAutoCompletion.setSelected(settings.isAutoCompletionEnabled());
        enableDocumentation.setSelected(settings.isDocumentationEnabled());
        enableSmartContext.setSelected(settings.isSmartContextEnabled());
        enableRightClickMenu.setSelected(settings.isRightClickMenuEnabled());
        updateComponentLibrariesInfo();
    }
    
    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
}
