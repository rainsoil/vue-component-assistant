package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * VueKit 设置组配置类
 * 
 * 功能说明：
 * - 作为 Vue Kit 设置的主入口
 * - 包含项目级功能设置和组件库管理
 * - 集成到 Settings/Tools/Vue Kit 目录下
 * 
 * @author VueKit Team
 * @version 5.0.0
 */
public class VueKitSettingsGroupConfigurable implements Configurable {

    // 项目级功能设置组件
    private JBCheckBox projectEnableComponentCompletion;
    private JBCheckBox projectEnableAttributeCompletion;
    private JBCheckBox projectEnableEventCompletion;
    private JBCheckBox projectEnableSlotCompletion;
    private JBCheckBox projectEnableHoverDocumentation;
    private JBCheckBox projectEnableRightClickDocumentation;
    
    // 组件库管理组件
    private JButton componentLibraryManagementButton;
    private JButton officialLibraryMarketButton;
    private JButton customLibraryManagementButton;
    
    // 项目组件库配置组件
    private JPanel componentLibraryConfigPanel;
    private java.util.Map<String, JBCheckBox> libraryCheckBoxes;
    
    // 主面板
    private JPanel mainPanel;
    private Project currentProject;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Vue Kit";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (mainPanel == null) {
            currentProject = getCurrentProject();
            
            // 初始化所有组件
            initializeProjectSettingsComponents();
            initializeLibraryManagementComponents();
            
            mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // 创建滚动面板
            JScrollPane scrollPane = new JScrollPane(createContentPanel());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        }
        return mainPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // 组件库管理卡片（最上面）
        contentPanel.add(createLibraryManagementCard());
        contentPanel.add(Box.createVerticalStrut(15));
        
        // 项目组件库配置卡片
        contentPanel.add(createComponentLibraryConfigCard());
        contentPanel.add(Box.createVerticalStrut(15));
        
        // 项目级功能设置卡片
        contentPanel.add(createProjectSettingsCard());
        
        return contentPanel;
    }
    
    private JPanel createProjectSettingsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("⚙️ 项目功能设置"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 创建两列布局
        JPanel settingsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        // 左列
        JPanel leftPanel = FormBuilder.createFormBuilder()
            .addComponent(createSectionLabel("🎯 补全功能设置"))
            .addComponent(projectEnableComponentCompletion)
            .addComponent(projectEnableAttributeCompletion)
            .addComponent(projectEnableEventCompletion)
            .addComponent(projectEnableSlotCompletion)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        // 右列
        JPanel rightPanel = FormBuilder.createFormBuilder()
            .addComponent(createSectionLabel("📖 文档功能设置"))
            .addComponent(projectEnableHoverDocumentation)
            .addComponent(projectEnableRightClickDocumentation)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
        
        settingsPanel.add(leftPanel);
        settingsPanel.add(rightPanel);
        
        // 添加说明标签
        JBLabel noteLabel = new JBLabel("说明：勾选启用对应功能，取消勾选则禁用。每个项目独立配置。");
        noteLabel.setForeground(new Color(128, 128, 128));
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));
        
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.add(noteLabel, BorderLayout.CENTER);
        notePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(settingsPanel, BorderLayout.CENTER);
        mainPanel.add(notePanel, BorderLayout.SOUTH);
        
        card.add(mainPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createLibraryManagementCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📚 组件库管理"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 按钮面板
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonsPanel.add(componentLibraryManagementButton);
        buttonsPanel.add(officialLibraryMarketButton);
        buttonsPanel.add(customLibraryManagementButton);
        
        // 说明标签
        JBLabel noteLabel = new JBLabel("说明：点击按钮打开对应的管理界面，进行组件库的下载、配置、删除等操作");
        noteLabel.setForeground(new Color(128, 128, 128));
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonsPanel, BorderLayout.CENTER);
        mainPanel.add(noteLabel, BorderLayout.SOUTH);
        
        card.add(mainPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createComponentLibraryConfigCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("🗂️ 项目组件库配置"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        componentLibraryConfigPanel = new JPanel();
        componentLibraryConfigPanel.setLayout(new BoxLayout(componentLibraryConfigPanel, BoxLayout.Y_AXIS));
        
        // 说明标签
        JBLabel noteLabel = new JBLabel("说明：勾选启用该组件库的补全和文档功能，取消勾选则禁用");
        noteLabel.setForeground(new Color(128, 128, 128));
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(componentLibraryConfigPanel, BorderLayout.CENTER);
        mainPanel.add(noteLabel, BorderLayout.SOUTH);
        
        card.add(mainPanel, BorderLayout.CENTER);
        
        // 加载组件库配置
        loadComponentLibraryConfig();
        
        return card;
    }
    
    private void initializeProjectSettingsComponents() {
        projectEnableComponentCompletion = new JBCheckBox("启用组件补全", true);
        projectEnableAttributeCompletion = new JBCheckBox("启用属性补全", true);
        projectEnableEventCompletion = new JBCheckBox("启用事件补全", true);
        projectEnableSlotCompletion = new JBCheckBox("启用插槽补全", true);
        projectEnableHoverDocumentation = new JBCheckBox("启用悬停文档", true);
        projectEnableRightClickDocumentation = new JBCheckBox("启用右键文档", true);
    }
    
    private void initializeLibraryManagementComponents() {
        componentLibraryManagementButton = createStyledButton("管理组件库", "管理VueKit组件库");
        officialLibraryMarketButton = createStyledButton("官方组件库", "浏览和下载官方组件库");
        customLibraryManagementButton = createStyledButton("自定义导入组件库", "管理自定义组件库");
        
        componentLibraryManagementButton.addActionListener(e -> openComponentLibraryManagement());
        officialLibraryMarketButton.addActionListener(e -> openOfficialLibraryMarket());
        customLibraryManagementButton.addActionListener(e -> openCustomLibraryManagement());
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
        button.setPreferredSize(new Dimension(100, 30));
        button.setBackground(new Color(240, 248, 255));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setFocusPainted(false);
        return button;
    }

    private JBLabel createSectionLabel(String text) {
        JBLabel label = new JBLabel(text);
        label.setFont(JBUI.Fonts.label(12));
        return label;
    }
    
    private void loadComponentLibraryConfig() {
        if (currentProject == null) {
            componentLibraryConfigPanel.add(new JBLabel("未找到项目"));
            return;
        }
        
        try {
            libraryCheckBoxes = new java.util.HashMap<>();
            
            // 获取已安装的组件库列表
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            if (installedLibraries == null || installedLibraries.isEmpty()) {
                componentLibraryConfigPanel.add(new JBLabel("暂无已安装的组件库"));
                return;
            }
            
            // 获取当前项目的组件库配置
            com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager configManager = 
                com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(currentProject);
            java.util.Set<com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType> enabledLibraries = 
                configManager.getEnabledLibraries(currentProject);
            
            // 为每个已安装的组件库创建复选框
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                String displayText = library.getName();
                if (library.getVersion() != null && !library.getVersion().trim().isEmpty()) {
                    displayText += " (" + library.getVersion() + ")";
                }
                
                JBCheckBox checkBox = new JBCheckBox(displayText);
                checkBox.setFont(checkBox.getFont().deriveFont(Font.PLAIN, 12f));
                checkBox.setToolTipText("版本: " + library.getVersion() + 
                    (library.getDescription() != null ? "\n描述: " + library.getDescription() : ""));
                
                // 检查该组件库是否在当前项目中启用
                com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType libraryType = 
                    getLibraryTypeByName(library.getName());
                if (libraryType != null) {
                    boolean isEnabled = enabledLibraries.contains(libraryType);
                    checkBox.setSelected(isEnabled);
                } else {
                    checkBox.setSelected(false); // 默认不启用
                }
                
                libraryCheckBoxes.put(library.getName(), checkBox);
                componentLibraryConfigPanel.add(checkBox);
            }
            
        } catch (Exception e) {
            componentLibraryConfigPanel.add(new JBLabel("加载组件库配置失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据组件库名称获取对应的 LibraryType
     * 现在从远程组件库管理器动态获取，不再写死
     */
    private com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType getLibraryTypeByName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return null;
        }
        
        // 移除版本号部分，只保留组件库名称
        String cleanName = libraryName.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
        
        try {
            // 从远程组件库管理器获取组件库信息
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            // 查找匹配的组件库
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                if (cleanName.equalsIgnoreCase(library.getName())) {
                    // 根据组件库名称动态创建 LibraryType
                    return createLibraryTypeFromName(library.getName());
                }
            }
            
        } catch (Exception e) {
            System.err.println("获取组件库信息失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 根据组件库名称动态创建 LibraryType
     * 这是一个简化的实现，实际应该从组件库的元数据中获取
     */
    private com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType createLibraryTypeFromName(String libraryName) {
        // 这里应该从组件库的JSON配置文件中读取类型信息
        // 暂时使用名称匹配作为后备方案
        String lowerName = libraryName.toLowerCase();
        
        if (lowerName.contains("element-plus")) {
            return com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType.ELEMENT_PLUS;
        } else if (lowerName.contains("element-ui")) {
            return com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType.ELEMENT_UI;
        } else if (lowerName.contains("ant-design-vue")) {
            return com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType.ANT_DESIGN_VUE;
        } else if (lowerName.contains("vuetify")) {
            return com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType.VUETIFY;
        } else if (lowerName.contains("quasar")) {
            return com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType.QUASAR;
        }
        
        return null;
    }

    private void openComponentLibraryManagement() {
        try {
            if (currentProject != null) {
                com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog dialog = 
                    new com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog(currentProject);
                dialog.show();
                refreshComponentLibraryConfig();
            } else {
                showError("项目未找到", "请确保当前有打开的项目");
            }
        } catch (Exception e) {
            showError("打开组件库管理失败", e.getMessage());
        }
    }

    private void openOfficialLibraryMarket() {
        try {
            if (currentProject != null) {
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog dialog = 
                    new com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog(currentProject, libraryManager);
                dialog.show();
                refreshComponentLibraryConfig();
            } else {
                showError("项目未找到", "请确保当前有打开的项目");
            }
        } catch (Exception e) {
            showError("打开官方组件库市场失败", e.getMessage());
        }
    }

    private void openCustomLibraryManagement() {
        try {
            if (currentProject != null) {
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog dialog = 
                    new com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog(currentProject, libraryManager);
                dialog.show();
                refreshComponentLibraryConfig();
            } else {
                showError("项目未找到", "请确保当前有打开的项目");
            }
        } catch (Exception e) {
            showError("打开自定义组件库管理失败", e.getMessage());
        }
    }
    
    private void refreshComponentLibraryConfig() {
        if (componentLibraryConfigPanel != null) {
            componentLibraryConfigPanel.removeAll();
            loadComponentLibraryConfig();
            componentLibraryConfigPanel.revalidate();
            componentLibraryConfigPanel.repaint();
        }
    }

    private Project getCurrentProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return projects[0];
        }
        return null;
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(mainPanel, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean isModified() {
        // 检查项目级功能设置是否有变更
        boolean projectModified = false;
        if (currentProject != null) {
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(currentProject);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(currentProject);
            projectModified = projectEnableComponentCompletion.isSelected() != projectSettings.isEnableComponentCompletion() ||
                    projectEnableAttributeCompletion.isSelected() != projectSettings.isEnableAttributeCompletion() ||
                    projectEnableEventCompletion.isSelected() != projectSettings.isEnableEventCompletion() ||
                    projectEnableSlotCompletion.isSelected() != projectSettings.isEnableSlotCompletion() ||
                    projectEnableHoverDocumentation.isSelected() != projectSettings.isEnableHoverDocumentation() ||
                    projectEnableRightClickDocumentation.isSelected() != projectSettings.isEnableRightClickDocumentation();
        }
        
        // 检查组件库配置是否有变更
        boolean libraryModified = false;
        if (libraryCheckBoxes != null && currentProject != null) {
            com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager configManager = 
                com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(currentProject);
            java.util.Set<com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType> savedEnabledLibraries = 
                configManager.getEnabledLibraries(currentProject);
            java.util.Set<com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType> currentEnabledLibraries = 
                new java.util.HashSet<>();
            
            for (java.util.Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
                String libraryName = entry.getKey();
                JBCheckBox checkBox = entry.getValue();
                
                if (checkBox.isSelected()) {
                    com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType libraryType = 
                        getLibraryTypeByName(libraryName);
                    if (libraryType != null) {
                        currentEnabledLibraries.add(libraryType);
                    }
                }
            }
            
            libraryModified = !savedEnabledLibraries.equals(currentEnabledLibraries);
        }
        
        return projectModified || libraryModified;
    }

    @Override
    public void apply() {
        // 保存项目级功能设置
        if (currentProject != null) {
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(currentProject);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(currentProject);
            
            // 更新项目级设置
            projectSettings.setEnableComponentCompletion(projectEnableComponentCompletion.isSelected());
            projectSettings.setEnableAttributeCompletion(projectEnableAttributeCompletion.isSelected());
            projectSettings.setEnableEventCompletion(projectEnableEventCompletion.isSelected());
            projectSettings.setEnableSlotCompletion(projectEnableSlotCompletion.isSelected());
            projectSettings.setEnableHoverDocumentation(projectEnableHoverDocumentation.isSelected());
            projectSettings.setEnableRightClickDocumentation(projectEnableRightClickDocumentation.isSelected());
            
            // 保存到文件
            projectSettingsManager.saveProjectSettings(currentProject, projectSettings);
            System.out.println("项目功能设置已保存: " + currentProject.getName());
        }
        
        // 保存组件库配置
        if (libraryCheckBoxes != null && currentProject != null) {
            com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager configManager = 
                com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(currentProject);
            java.util.Set<com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType> enabledLibraries = 
                new java.util.HashSet<>();
            
            for (java.util.Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
                String libraryName = entry.getKey();
                JBCheckBox checkBox = entry.getValue();
                
                if (checkBox.isSelected()) {
                    com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType libraryType = 
                        getLibraryTypeByName(libraryName);
                    if (libraryType != null) {
                        enabledLibraries.add(libraryType);
                    }
                }
            }
            
            // 保存配置
            configManager.setProjectEnabledLibraries(currentProject, enabledLibraries);
            System.out.println("组件库配置已保存，启用的组件库: " + 
                enabledLibraries.stream()
                    .map(com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector.LibraryType::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
    }

    @Override
    public void reset() {
        // 重置项目级功能设置
        if (currentProject != null) {
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(currentProject);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(currentProject);
            
            // 项目级设置：直接使用保存的值
            projectEnableComponentCompletion.setSelected(projectSettings.isEnableComponentCompletion());
            projectEnableAttributeCompletion.setSelected(projectSettings.isEnableAttributeCompletion());
            projectEnableEventCompletion.setSelected(projectSettings.isEnableEventCompletion());
            projectEnableSlotCompletion.setSelected(projectSettings.isEnableSlotCompletion());
            projectEnableHoverDocumentation.setSelected(projectSettings.isEnableHoverDocumentation());
            projectEnableRightClickDocumentation.setSelected(projectSettings.isEnableRightClickDocumentation());
        }
        
        // 重置组件库配置
        if (componentLibraryConfigPanel != null) {
            componentLibraryConfigPanel.removeAll();
            loadComponentLibraryConfig();
            componentLibraryConfigPanel.revalidate();
            componentLibraryConfigPanel.repaint();
        }
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        libraryCheckBoxes = null;
    }
}