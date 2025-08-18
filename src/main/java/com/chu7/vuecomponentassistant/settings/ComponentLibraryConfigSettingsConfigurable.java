package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import java.util.Set;
import java.util.HashSet;

/**
 * 组件库配置设置页面
 *
 * 功能说明：
 * - 作为 Vue Kit 设置组下的子项
 * - 提供组件库配置功能
 * - 集成到 Settings/Tools/Vue Kit/组件库配置 目录下
 *
 * @author VueKit Team
 * @version 3.0.0
 */
public class ComponentLibraryConfigSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JLabel descriptionLabel;
    private Map<String, JCheckBox> libraryCheckBoxes;
    private JPanel configPanel;
    
    // 功能设置复选框
    private JCheckBox enableComponentCompletion;
    private JCheckBox enableAttributeCompletion;
    private JCheckBox enableEventCompletion;
    private JCheckBox enableSlotCompletion;
    private JCheckBox enableHoverDocumentation;
    private JCheckBox enableRightClickDocumentation;
    private JCheckBox enableCaching;
    private JCheckBox enableDebugMode;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "组件库配置";
    }

    @Override
    public @Nullable JComponent createComponent() {
        // 每次进入页面都重新创建，确保数据是最新的
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建描述面板
        JPanel descriptionPanel = createDescriptionPanel();
        mainPanel.add(descriptionPanel, BorderLayout.CENTER);

        // 创建功能设置面板
        JPanel functionSettingsPanel = createFunctionSettingsPanel();
        mainPanel.add(functionSettingsPanel, BorderLayout.CENTER);

        // 创建配置面板
        configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.SOUTH);

        // 初始化配置
        initializeConfig();
        initializeFunctionSettings();
        
        // 打印调试信息
        System.out.println("组件库配置页面已创建，当前项目: " + getCurrentProject().getName());

        return mainPanel;
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("组件库配置");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(new Color(51, 51, 51));
        panel.add(titleLabel);
        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("功能说明"));

        descriptionLabel = new JLabel("<html><div style='width: 400px;'>" +

                "</div></html>");

        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(Font.PLAIN, 12f));
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(descriptionLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("已安装的组件库配置"));

        // 创建复选框面板
        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1, 5, 2)); // 减小垂直间距从5改为2
        checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 初始化复选框映射
        libraryCheckBoxes = new HashMap<>();

        // 获取已安装的组件库列表
        List<ComponentLibrary> installedLibraries = getInstalledLibraries();

        if (installedLibraries.isEmpty()) {
            // 如果没有已安装的组件库，显示提示信息
            JLabel noLibraryLabel = new JLabel("暂无已安装的组件库");
            noLibraryLabel.setFont(noLibraryLabel.getFont().deriveFont(Font.PLAIN, 12f));
            noLibraryLabel.setForeground(Color.GRAY);
            noLibraryLabel.setHorizontalAlignment(SwingConstants.CENTER);
            checkBoxPanel.add(noLibraryLabel);
        } else {
            // 为每个已安装的组件库创建复选框
            for (ComponentLibrary library : installedLibraries) {
                // 显示格式：组件库名称 (版本号)
                String displayText = library.getName();
                if (library.getVersion() != null && !library.getVersion().trim().isEmpty()) {
                    displayText += " (" + library.getVersion() + ")";
                }

                JCheckBox checkBox = new JCheckBox(displayText);
                checkBox.setBorder(BorderFactory.createEmptyBorder(-5, 10, -5, 10)); // 减小上下边距从-10改为-5
                checkBox.setFont(checkBox.getFont().deriveFont(Font.PLAIN, 12f));
                checkBox.setToolTipText("版本: " + library.getVersion() +
                        (library.getDescription() != null ? "\n描述: " + library.getDescription() : ""));
                libraryCheckBoxes.put(library.getName(), checkBox);
                checkBoxPanel.add(checkBox);
            }
        }

        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        panel.add(scrollPane, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // 添加刷新按钮
        JButton refreshButton = new JButton("🔄 刷新");
        refreshButton.setFont(refreshButton.getFont().deriveFont(Font.PLAIN, 12f));
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.setBackground(new Color(34, 139, 34)); // 绿色
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshLibraries());

        JButton saveButton = new JButton("保存配置");
        saveButton.setFont(saveButton.getFont().deriveFont(Font.PLAIN, 12f));
        saveButton.setPreferredSize(new Dimension(100, 30));
        saveButton.setBackground(new Color(100, 149, 237));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveCurrentConfig());

        JButton resetButton = new JButton("重置");
        resetButton.setFont(resetButton.getFont().deriveFont(Font.PLAIN, 12f));
        resetButton.setPreferredSize(new Dimension(100, 30));
        resetButton.setBackground(new Color(169, 169, 169));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> performReset());

        buttonPanel.add(refreshButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建功能设置面板
     */
    private JPanel createFunctionSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("功能设置"));

        // 创建功能设置复选框
        enableComponentCompletion = new JCheckBox("启用组件补全", true);
        enableAttributeCompletion = new JCheckBox("启用属性补全", true);
        enableEventCompletion = new JCheckBox("启用事件补全", true);
        enableSlotCompletion = new JCheckBox("启用插槽补全", true);
        enableHoverDocumentation = new JCheckBox("启用悬停文档", true);
        enableRightClickDocumentation = new JCheckBox("启用右键文档", true);
        enableCaching = new JCheckBox("启用缓存优化", true);
        enableDebugMode = new JCheckBox("启用调试模式", false);

        // 设置字体和样式
        Font checkboxFont = new Font("Dialog", Font.PLAIN, 12);
        enableComponentCompletion.setFont(checkboxFont);
        enableAttributeCompletion.setFont(checkboxFont);
        enableEventCompletion.setFont(checkboxFont);
        enableSlotCompletion.setFont(checkboxFont);
        enableHoverDocumentation.setFont(checkboxFont);
        enableRightClickDocumentation.setFont(checkboxFont);
        enableCaching.setFont(checkboxFont);
        enableDebugMode.setFont(checkboxFont);

        // 创建补全功能设置面板
        JPanel completionPanel = new JPanel(new GridLayout(0, 1, 5, 2));
        completionPanel.setBorder(BorderFactory.createTitledBorder("补全功能设置"));
        completionPanel.add(enableComponentCompletion);
        completionPanel.add(enableAttributeCompletion);
        completionPanel.add(enableEventCompletion);
        completionPanel.add(enableSlotCompletion);

        // 创建文档功能设置面板
        JPanel documentationPanel = new JPanel(new GridLayout(0, 1, 5, 2));
        documentationPanel.setBorder(BorderFactory.createTitledBorder("文档功能设置"));
        documentationPanel.add(enableHoverDocumentation);
        documentationPanel.add(enableRightClickDocumentation);

        // 创建性能优化设置面板
        JPanel performancePanel = new JPanel(new GridLayout(0, 1, 5, 2));
        performancePanel.setBorder(BorderFactory.createTitledBorder("性能优化设置"));
        performancePanel.add(enableCaching);
        performancePanel.add(enableDebugMode);

        // 创建左侧面板（补全和文档功能）
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        leftPanel.add(completionPanel);
        leftPanel.add(documentationPanel);

        // 创建右侧面板（性能优化）
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(performancePanel, BorderLayout.NORTH);

        // 创建主功能设置面板
        JPanel mainFunctionPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainFunctionPanel.add(leftPanel);
        mainFunctionPanel.add(rightPanel);

        // 添加说明文字
        JLabel infoLabel = new JLabel("<html><div style='width: 400px; color: #666;'>" +
            "<b>说明：</b>以上设置仅对当前项目生效，配置将保存到项目根目录的 .vuekit-project-settings.json 文件中。" +
            "</div></html>");
        infoLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(mainFunctionPanel, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void initializeConfig() {
        // 加载已保存的配置
        if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(currentProject);
                Set<String> enabledLibraries = configManager.getEnabledLibraryNames(currentProject);
                
                System.out.println("初始化配置，启用的组件库: " + 
                    String.join(", ", enabledLibraries));
                
                for (Map.Entry<String, JCheckBox> entry : libraryCheckBoxes.entrySet()) {
                    String libraryName = entry.getKey();
                    JCheckBox checkBox = entry.getValue();
                    
                    // 直接使用字符串比较
                    boolean isEnabled = enabledLibraries.contains(libraryName);
                    checkBox.setSelected(isEnabled);
                    System.out.println("设置 " + libraryName + " 为 " + (isEnabled ? "选中" : "未选中"));
                }
            } else {
                System.out.println("当前项目为空，无法加载配置");
            }
        } else {
            System.out.println("libraryCheckBoxes 为空或为空，跳过配置初始化");
        }
    }

    /**
     * 初始化功能设置
     */
    private void initializeFunctionSettings() {
        try {
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(currentProject);
                ProjectSettingsManager.ProjectSettings settings = settingsManager.getProjectSettings(currentProject);
                
                // 设置复选框状态
                enableComponentCompletion.setSelected(settings.isEnableComponentCompletion());
                enableAttributeCompletion.setSelected(settings.isEnableAttributeCompletion());
                enableEventCompletion.setSelected(settings.isEnableEventCompletion());
                enableSlotCompletion.setSelected(settings.isEnableSlotCompletion());
                enableHoverDocumentation.setSelected(settings.isEnableHoverDocumentation());
                enableRightClickDocumentation.setSelected(settings.isEnableRightClickDocumentation());
                enableCaching.setSelected(settings.isEnableCaching());
                enableDebugMode.setSelected(settings.isEnableDebugMode());
                
                System.out.println("功能设置已初始化，项目: " + currentProject.getName());
            }
        } catch (Exception e) {
            System.err.println("初始化功能设置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<ComponentLibrary> getInstalledLibraries() {
        try {
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                ComponentLibraryManager libraryManager = new ComponentLibraryManager();
                // 获取已安装的组件库列表
                List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
                System.out.println("获取到已安装组件库数量: " + (libraries != null ? libraries.size() : 0));
                if (libraries != null && !libraries.isEmpty()) {
                    // 打印每个组件库的信息
                    for (ComponentLibrary lib : libraries) {
                        System.out.println("组件库: " + lib.getName() + " (版本: " + lib.getVersion() + ")");
                    }
                    return libraries;
                } else {
                    System.out.println("没有找到已安装的组件库，返回空列表");
                }
            } else {
                System.out.println("当前没有打开的项目");
            }
        } catch (Exception e) {
            // 如果获取失败，记录错误并返回空列表
            System.err.println("获取已安装组件库失败: " + e.getMessage());
            e.printStackTrace();
        }
        // 如果没有已安装的库或获取失败，返回空列表（不显示默认组件）
        System.out.println("返回空组件库列表");
        return new ArrayList<>();
    }



    private Project getCurrentProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return projects[0]; // 返回第一个打开的项目
        }
        return null;
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(mainPanel, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * 刷新组件库列表
     */
    private void refreshLibraries() {
        try {
            // 清空现有的复选框映射
            libraryCheckBoxes.clear();

            // 重新获取已安装的组件库列表
            List<ComponentLibrary> installedLibraries = getInstalledLibraries();

            // 重新创建配置面板
            if (configPanel != null) {
                mainPanel.remove(configPanel);
                configPanel = createConfigPanel();
                mainPanel.add(configPanel, BorderLayout.SOUTH);

                // 重新初始化配置
                initializeConfig();

                // 刷新UI
                mainPanel.revalidate();
                mainPanel.repaint();

                // 显示成功消息
                JOptionPane.showMessageDialog(mainPanel,
                        "组件库列表已刷新！\n当前已安装 " + installedLibraries.size() + " 个组件库",
                        "刷新成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            showError("刷新失败", "刷新组件库列表时发生错误: " + e.getMessage());
        }
    }

    @Override
    public boolean isModified() {
        // 检查是否有配置更改
        Project currentProject = getCurrentProject();
        if (currentProject != null) {
            // 检查组件库配置是否有变更
            boolean libraryConfigModified = isLibraryConfigModified(currentProject);
            
            // 检查功能设置是否有变更
            boolean functionSettingsModified = isFunctionSettingsModified(currentProject);
            
            return libraryConfigModified || functionSettingsModified;
        }
        return false;
    }
    
    /**
     * 检查组件库配置是否有变更
     */
    private boolean isLibraryConfigModified(Project currentProject) {
        if (libraryCheckBoxes != null) {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(currentProject);
            Set<String> savedEnabledLibraries = configManager.getEnabledLibraryNames(currentProject);
            Set<String> currentEnabledLibraries = new HashSet<>();
            
            for (Map.Entry<String, JCheckBox> entry : libraryCheckBoxes.entrySet()) {
                String libraryName = entry.getKey();
                JCheckBox checkBox = entry.getValue();
                
                if (checkBox.isSelected()) {
                    currentEnabledLibraries.add(libraryName);
                }
            }
            
            // 比较当前状态与保存的状态
            return !savedEnabledLibraries.equals(currentEnabledLibraries);
        }
        return false;
    }
    
    /**
     * 检查功能设置是否有变更
     */
    private boolean isFunctionSettingsModified(Project currentProject) {
        try {
            ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(currentProject);
            ProjectSettingsManager.ProjectSettings settings = settingsManager.getProjectSettings(currentProject);
            
            return enableComponentCompletion.isSelected() != settings.isEnableComponentCompletion() ||
                   enableAttributeCompletion.isSelected() != settings.isEnableAttributeCompletion() ||
                   enableEventCompletion.isSelected() != settings.isEnableEventCompletion() ||
                   enableSlotCompletion.isSelected() != settings.isEnableSlotCompletion() ||
                   enableHoverDocumentation.isSelected() != settings.isEnableHoverDocumentation() ||
                   enableRightClickDocumentation.isSelected() != settings.isEnableRightClickDocumentation() ||
                   enableCaching.isSelected() != settings.isEnableCaching() ||
                   enableDebugMode.isSelected() != settings.isEnableDebugMode();
        } catch (Exception e) {
            System.err.println("检查功能设置变更失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void apply() {
        // 保存当前配置
        saveCurrentConfig();
    }

    @Override
    public void reset() {
        // 重置为默认配置 - 只在用户明确点击重置按钮时执行
        // 这里不执行实际的重置操作，避免 IntelliJ 设置系统自动调用时重置用户配置
        System.out.println("reset() 方法被调用，但不执行重置操作以避免意外重置");
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
    
    /**
     * 执行重置操作（用户明确点击重置按钮时调用）
     */
    private void performReset() {
        try {
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                // 重置组件库配置
                ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(currentProject);
                configManager.resetToGlobalDefault(currentProject);
                
                // 重置功能设置
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(currentProject);
                settingsManager.resetToDefault(currentProject);
                
                // 重新加载配置到界面
                initializeConfig();
                initializeFunctionSettings();
                
                System.out.println("配置已重置为默认值");
                
                // 显示成功消息
                JOptionPane.showMessageDialog(mainPanel, 
                    "配置已重置为默认值！",
                    "重置成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("重置配置失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, 
                "重置配置失败: " + e.getMessage(),
                "重置失败", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    

    
    /**
     * 保存当前配置
     */
    private void saveCurrentConfig() {
        try {
            Project currentProject = getCurrentProject();
            if (currentProject != null) {
                // 保存组件库配置
                saveComponentLibraryConfig(currentProject);
                
                // 保存功能设置
                saveFunctionSettings(currentProject);
                
                // 显示成功消息
                JOptionPane.showMessageDialog(mainPanel, 
                    "配置已保存！\n包括组件库配置和功能设置。",
                    "保存成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("保存配置失败: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, 
                "保存配置失败: " + e.getMessage(),
                "保存失败", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 保存组件库配置
     */
    private void saveComponentLibraryConfig(Project currentProject) {
        if (libraryCheckBoxes != null) {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(currentProject);
            Set<String> enabledLibraries = new HashSet<>();
            
            for (Map.Entry<String, JCheckBox> entry : libraryCheckBoxes.entrySet()) {
                String libraryName = entry.getKey();
                JCheckBox checkBox = entry.getValue();
                
                if (checkBox.isSelected()) {
                    enabledLibraries.add(libraryName);
                }
            }
            
            // 保存配置
            configManager.setProjectEnabledLibraryNames(currentProject, enabledLibraries);
            
            System.out.println("组件库配置已保存，启用的组件库: " + String.join(", ", enabledLibraries));
        }
    }
    
    /**
     * 保存功能设置
     */
    private void saveFunctionSettings(Project currentProject) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(currentProject);
        ProjectSettingsManager.ProjectSettings settings = settingsManager.getProjectSettings(currentProject);
        
        // 更新设置
        settings.setEnableComponentCompletion(enableComponentCompletion.isSelected());
        settings.setEnableAttributeCompletion(enableAttributeCompletion.isSelected());
        settings.setEnableEventCompletion(enableEventCompletion.isSelected());
        settings.setEnableSlotCompletion(enableSlotCompletion.isSelected());
        settings.setEnableHoverDocumentation(enableHoverDocumentation.isSelected());
        settings.setEnableRightClickDocumentation(enableRightClickDocumentation.isSelected());
        settings.setEnableCaching(enableCaching.isSelected());
        settings.setEnableDebugMode(enableDebugMode.isSelected());
        
        // 保存设置
        settingsManager.saveProjectSettings(currentProject, settings);
        
        System.out.println("功能设置已保存，项目: " + currentProject.getName());
    }
}
