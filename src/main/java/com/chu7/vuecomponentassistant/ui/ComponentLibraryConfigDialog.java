package com.chu7.vuecomponentassistant.ui;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.StringNormalizer;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.chu7.vuecomponentassistant.utils.PackageJsonAutoDetector;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBPanel;

import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 组件库配置对话框
 * <p>
 * 功能说明：
 * - 提供组件库启用/禁用开关
 * - 支持项目级配置管理
 * - 配置导入/导出功能
 * - 智能检测项目依赖的组件库
 * - 用户友好的配置界面
 * <p>
 * 特性：
 * - 直观的复选框界面
 * - 实时配置预览
 * - 一键导入/导出
 * - 智能默认值设置
 * - 配置验证和错误提示
 *
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryConfigDialog extends DialogWrapper {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigDialog.class);

    private final Project project;
    private final ComponentLibraryConfigManager configManager;

    // UI 组件
    private JBPanel mainPanel;
    private Map<String, JBCheckBox> libraryCheckBoxes;
    private JBTextField projectNameField;
    private JBTextField globalConfigPathField;
    private JBCheckBox debugModeCheckBox; // 添加调试模式复选框

    // 配置数据
    private Set<String> currentEnabledLibraryNames;
    private Set<String> originalEnabledLibraryNames;

    /**
     * 构造函数
     *
     * @param project 项目对象
     */
    public ComponentLibraryConfigDialog(Project project) {
        super(project);
        this.project = project;
        this.configManager = ComponentLibraryConfigManager.getInstance(project);

        setTitle("VueKit 组件库配置");
        setSize(600, 500);

        init();
        // 注意：loadCurrentConfig() 将在 createCenterPanel() 之后调用
    }

    /**
     * 初始化对话框
     */
    protected void init() {
        // 设置按钮
        setOKButtonText("保存");
        setCancelButtonText("取消");
    }

    @Override
    protected JComponent createCenterPanel() {
        VueKitLogger.info(LOG, project, "开始创建中心面板");
        
        // 使用 GridBagLayout 确保内容正确显示
        mainPanel = new JBPanel(new GridBagLayout());
        mainPanel.setBorder(JBUI.Borders.empty(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        createMainInterface();
        
        // 在界面创建完成后加载配置
        loadCurrentConfig();
        
        VueKitLogger.info(LOG, project, "中心面板创建完成，主面板大小: " + mainPanel.getSize());
        
        // 强制设置面板尺寸，确保内容可见
        mainPanel.setPreferredSize(new Dimension(500, 400));
        mainPanel.setMinimumSize(new Dimension(400, 300));
        
        // 验证复选框是否正确创建
        validateCheckBoxes();
        
        return mainPanel;
    }

    /**
     * 创建主界面
     */
    private void createMainInterface() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 1. 项目信息面板
        JBPanel projectInfoPanel = createProjectInfoPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        mainPanel.add(projectInfoPanel, gbc);

        // 2. 自动检测状态面板
        JBPanel autoDetectPanel = createAutoDetectStatusPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        mainPanel.add(autoDetectPanel, gbc);

        // 3. 组件库配置面板
        VueKitLogger.info(LOG, project, "开始创建组件库配置面板...");
        JBPanel libraryConfigPanel = createLibraryConfigPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(libraryConfigPanel, gbc);
        VueKitLogger.info(LOG, project, "组件库配置面板已添加到主界面");

        // 4. 操作按钮面板
        JBPanel actionPanel = createActionPanel();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(actionPanel, gbc);
    }

    /**
     * 创建项目信息面板
     *
     * @return 项目信息面板
     */
    private JBPanel createProjectInfoPanel() {
        JBPanel panel = new JBPanel(new GridLayout(2, 1, 0, 5));
        panel.setBorder(JBUI.Borders.empty(10, 10, 5, 10));

        // 项目名称
        JBLabel projectLabel = new JBLabel("项目名称:");
        projectNameField = new JBTextField(project.getName());
        projectNameField.setEditable(false);

        JBPanel projectPanel = new JBPanel(new FlowLayout(FlowLayout.LEFT));
        projectPanel.add(projectLabel);
        projectPanel.add(projectNameField);

        // 全局配置路径
        JBLabel globalPathLabel = new JBLabel("全局配置路径:");
        globalConfigPathField = new JBTextField(getGlobalConfigPath());
        globalConfigPathField.setEditable(false);

        JBPanel globalPathPanel = new JBPanel(new FlowLayout(FlowLayout.LEFT));
        globalPathPanel.add(globalPathLabel);
        globalPathPanel.add(globalConfigPathField);

        panel.add(projectPanel);
        panel.add(globalPathPanel);

        return panel;
    }

    /**
     * 创建自动检测状态面板
     *
     * @return 自动检测状态面板
     */
    private JBPanel createAutoDetectStatusPanel() {
        JBPanel panel = new JBPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(5, 10, 5, 10));

        // 检查是否是通过自动检测启用的组件库
        boolean hasAutoDetectedLibraries = checkAutoDetectedLibraries();
        
        if (hasAutoDetectedLibraries) {
            // 显示自动检测成功信息
            JBLabel statusLabel = new JBLabel("✅ 已自动检测并启用项目中的组件库");
            statusLabel.setForeground(new Color(0, 128, 0)); // 绿色
            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));
            
            JBLabel infoLabel = new JBLabel(
                "<html>系统已根据 package.json 中的依赖项自动启用了匹配的组件库。<br>" +
                "您可以在下方调整组件库的启用状态。</html>"
            );
            infoLabel.setForeground(new Color(64, 64, 64));
            
            JBPanel infoPanel = new JBPanel(new BorderLayout());
            infoPanel.add(statusLabel, BorderLayout.NORTH);
            infoPanel.add(infoLabel, BorderLayout.CENTER);
            
            panel.add(infoPanel, BorderLayout.CENTER);
        } else {
            // 显示手动配置信息
            JBLabel statusLabel = new JBLabel("ℹ️ 手动配置模式");
            statusLabel.setForeground(new Color(128, 128, 128)); // 灰色
            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));
            
            JBLabel infoLabel = new JBLabel(
                "<html>未检测到 package.json 中的组件库依赖，或配置文件已存在。<br>" +
                "请手动选择要启用的组件库。</html>"
            );
            infoLabel.setForeground(new Color(64, 64, 64));
            
            JBPanel infoPanel = new JBPanel(new BorderLayout());
            infoPanel.add(statusLabel, BorderLayout.NORTH);
            infoPanel.add(infoLabel, BorderLayout.CENTER);
            
            panel.add(infoPanel, BorderLayout.CENTER);
        }

        return panel;
    }

    /**
     * 检查是否有通过自动检测启用的组件库
     *
     * @return 如果有自动检测的组件库则返回 true
     */
    private boolean checkAutoDetectedLibraries() {
        try {
            // 检查项目配置文件是否存在且不为空
            VirtualFile ideaDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project).findChild(".idea");
            if (ideaDir == null) {
                VueKitLogger.debug(LOG, project, ".idea 目录不存在，将在界面加载完成后尝试自动匹配");
                return false; // 不在这里执行自动匹配，避免时序问题
            }
            
            VirtualFile configFile = ideaDir.findChild("vuekit-project-config.json");
            if (configFile == null || !configFile.exists()) {
                VueKitLogger.debug(LOG, project, "配置文件不存在，将在界面加载完成后尝试自动匹配");
                return false; // 不在这里执行自动匹配，避免时序问题
            }
            
            String configContent = new String(configFile.contentsToByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            if (configContent.trim().isEmpty() || configContent.equals("{}") || configContent.equals("{\"enabledLibraries\":[]}")) {
                VueKitLogger.debug(LOG, project, "配置文件为空，将在界面加载完成后尝试自动匹配");
                return false; // 不在这里执行自动匹配，避免时序问题
            }
            
            // 检查是否有启用的组件库
            Set<String> enabledLibraries = configManager.getEnabledLibraryNames(project);
            if (enabledLibraries.isEmpty()) {
                VueKitLogger.debug(LOG, project, "没有启用的组件库，将在界面加载完成后尝试自动匹配");
                return false; // 不在这里执行自动匹配，避免时序问题
            }
            
            return true;
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, project, "检查自动检测状态失败", e);
            return false; // 不在这里执行自动匹配，避免时序问题
        }
    }

    /**
     * 创建组件库配置面板
     *
     * @return 组件库配置面板
     */
    private JBPanel createLibraryConfigPanel() {
        // 使用简单的垂直布局，确保内容可见
        JBPanel panel = new JBPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(10, 10, 10, 10));

        // 标题
        JBLabel titleLabel = new JBLabel("组件库配置");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(JBUI.Borders.empty(0, 0, 15, 0));
        panel.add(titleLabel);

        // 说明文字
        JBLabel descriptionLabel = new JBLabel(
                "<html>选择要在当前项目中启用的组件库。<br>" +
                        "系统会自动检测项目依赖的组件库并优先显示。</html>"
        );
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setBorder(JBUI.Borders.empty(0, 0, 20, 0));
        panel.add(descriptionLabel);

        // 添加一些间距
        panel.add(Box.createVerticalStrut(10));

        // 组件库复选框
        JBPanel checkBoxPanel = createLibraryCheckBoxes();
        checkBoxPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBoxPanel.setBorder(JBUI.Borders.customLine(Color.LIGHT_GRAY, 1));
        checkBoxPanel.setBackground(Color.WHITE);
        panel.add(checkBoxPanel);

        // 添加一些间距
        panel.add(Box.createVerticalStrut(15));

        // 调试模式选项
        VueKitLogger.info(LOG, project, "开始创建调试模式面板...");
        JBPanel debugPanel = createDebugModePanel();
        debugPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(debugPanel);
        VueKitLogger.info(LOG, project, "调试模式面板已添加到组件库配置面板");
        VueKitLogger.info(LOG, project, "调试模式面板可见: " + debugPanel.isVisible());
        VueKitLogger.info(LOG, project, "调试模式面板启用: " + debugPanel.isEnabled());
        VueKitLogger.info(LOG, project, "组件库配置面板子组件数量: " + panel.getComponentCount());

        return panel;
    }

    /**
     * 创建调试模式面板
     *
     * @return 调试模式面板
     */
    private JBPanel createDebugModePanel() {
        VueKitLogger.info(LOG, project, "=== createDebugModePanel() 开始 ===");
        
        JBPanel panel = new JBPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.customLine(Color.LIGHT_GRAY, 1));
        panel.setBackground(Color.WHITE);
        
        // 设置面板的最小尺寸，确保可见
        panel.setMinimumSize(new Dimension(200, 30));
        panel.setPreferredSize(new Dimension(300, 40));

        // 调试模式复选框
        debugModeCheckBox = new JBCheckBox("启用调试模式");
        debugModeCheckBox.setToolTipText("启用后将在日志中输出详细的调试信息，帮助排查问题");
        
        // 确保复选框可见和启用
        debugModeCheckBox.setVisible(true);
        debugModeCheckBox.setEnabled(true);
        
        // 设置初始状态（这里先设置为false，在loadCurrentConfig中会更新为正确状态）
        debugModeCheckBox.setSelected(false);
        VueKitLogger.info(LOG, project, "调试模式复选框初始状态设置为: false（将在配置加载后更新）");
        
        // 添加监听器
        debugModeCheckBox.addActionListener(e -> {
            boolean enabled = debugModeCheckBox.isSelected();
            configManager.setDebugModeEnabled(project, enabled);
            VueKitLogger.info(LOG, project, "调试模式已" + (enabled ? "启用" : "禁用"));
        });

        panel.add(debugModeCheckBox, BorderLayout.CENTER);
        
        VueKitLogger.info(LOG, project, "调试模式面板创建完成，复选框可见: " + debugModeCheckBox.isVisible());
        VueKitLogger.info(LOG, project, "调试模式面板可见: " + panel.isVisible());
        VueKitLogger.info(LOG, project, "调试模式面板启用: " + panel.isEnabled());
        VueKitLogger.info(LOG, project, "调试模式复选框启用: " + debugModeCheckBox.isEnabled());
        VueKitLogger.info(LOG, project, "=== createDebugModePanel() 结束 ===");

        return panel;
    }

    /**
     * 创建组件库复选框
     *
     * @return 复选框面板
     */
    private JBPanel createLibraryCheckBoxes() {
        VueKitLogger.info(LOG, project, "=== createLibraryCheckBoxes() 开始 ===");
        
        // 使用 GridLayout 确保复选框可见
        JBPanel panel = new JBPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(JBUI.Borders.customLine(Color.BLACK, 2)); // 添加明显的边框
        panel.setBackground(Color.WHITE); // 设置白色背景

        libraryCheckBoxes = new HashMap<>();

        // 动态获取可用的组件库名称
        String[] libraryNames = getAvailableLibraryNames();
        
        // 添加调试信息
        VueKitLogger.info(LOG, project, "创建组件库复选框，找到 " + libraryNames.length + " 个组件库");
        for (String libraryName : libraryNames) {
            VueKitLogger.info(LOG, project, "- " + libraryName);
        }

        // 添加标题标签
        JBLabel checkBoxTitle = new JBLabel("可用的组件库：");
        checkBoxTitle.setFont(checkBoxTitle.getFont().deriveFont(Font.BOLD, 14f));
        checkBoxTitle.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
        panel.add(checkBoxTitle);

        for (String libraryName : libraryNames) {
            // 创建复选框
            JBCheckBox checkBox = new JBCheckBox(libraryName);
            checkBox.setToolTipText(getLibraryTooltip(libraryName));
            
            // 设置复选框为可见和启用状态
            checkBox.setVisible(true);
            checkBox.setEnabled(true);
            
            // 重要：不要默认选中，应该根据配置来决定
            // checkBox.setSelected(true); // 删除这行！
            checkBox.setSelected(false); // 默认不选中
            
            VueKitLogger.info(LOG, project, "创建复选框: " + libraryName + " - 初始状态: 未选中");

            // 添加选择监听器
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    VueKitLogger.info(LOG, project, "复选框 '" + libraryName + "' 状态改变: " + 
                            (checkBox.isSelected() ? "选中" : "未选中"));
                    updateConfiguration();
                }
            });

            libraryCheckBoxes.put(libraryName, checkBox);
            panel.add(checkBox);
            
            VueKitLogger.info(LOG, project, "添加复选框: " + libraryName);
        }
        
        VueKitLogger.info(LOG, project, "总共创建了 " + libraryCheckBoxes.size() + " 个复选框");
        VueKitLogger.info(LOG, project, "=== createLibraryCheckBoxes() 结束 ===");

        return panel;
    }

    /**
     * 创建操作按钮面板
     *
     * @return 操作按钮面板
     */
    private JBPanel createActionPanel() {
        JBPanel panel = new JBPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBorder(JBUI.Borders.empty(5, 10, 10, 10));

        // 导入按钮
        JButton importButton = new JButton("导入配置");
        importButton.addActionListener(e -> importConfiguration());

        // 导出按钮
        JButton exportButton = new JButton("导出配置");
        exportButton.addActionListener(e -> exportConfiguration());

        // 重置按钮
        JButton resetButton = new JButton("重置为全局默认");
        resetButton.addActionListener(e -> resetToGlobalDefault());

        // 检测项目依赖按钮
        JButton detectButton = new JButton("检测项目依赖");
        detectButton.addActionListener(e -> detectProjectDependencies());

        panel.add(importButton);
        panel.add(exportButton);
        panel.add(resetButton);
        panel.add(detectButton);

        return panel;
    }

    /**
     * 动态获取可用的组件库名称
     */
    private String[] getAvailableLibraryNames() {
        VueKitLogger.info(LOG, project, "=== getAvailableLibraryNames() 开始 ===");
        
        try {
            VueKitLogger.info(LOG, project, "=== 获取可用组件库名称 ===");
            
            // 从远程组件库管理器获取已安装的组件库
            VueKitLogger.info(LOG, project, "开始从远程组件库管理器获取组件库...");
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            VueKitLogger.info(LOG, project, "远程组件库管理器返回的组件库数量: " + installedLibraries.size());
            VueKitLogger.info(LOG, project, "远程组件库列表:");
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                VueKitLogger.info(LOG, project, "- " + library.getName() + " (描述: " + library.getDescription() + ")");
            }
            
            java.util.List<String> libraryNameList = new java.util.ArrayList<>();
            
            VueKitLogger.info(LOG, project, "开始收集组件库名称...");
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                VueKitLogger.info(LOG, project, "处理远程组件库: '" + library.getName() + "'");
                libraryNameList.add(library.getName());
                VueKitLogger.info(LOG, project, "已添加到可用组件库列表: " + library.getName());
            }
            
            VueKitLogger.info(LOG, project, "收集到的组件库名称数量: " + libraryNameList.size());
            VueKitLogger.info(LOG, project, "收集到的组件库列表:");
            for (String name : libraryNameList) {
                VueKitLogger.info(LOG, project, "- " + name);
            }
            
            // 如果没有找到任何组件库，返回空数组而不是硬编码列表
            if (libraryNameList.isEmpty()) {
                VueKitLogger.warn(LOG, "未找到任何组件库，返回空列表");
                return new String[0];
            }
            
            String[] result = libraryNameList.toArray(new String[0]);
            VueKitLogger.info(LOG, project, "最终返回的组件库名称: " + String.join(", ", result));
            
            VueKitLogger.info(LOG, project, "=== getAvailableLibraryNames() 结束 ===");
            return result;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取可用组件库名称失败，返回空列表", e);
            // 出错时返回空列表，而不是硬编码的默认列表
            return new String[0];
        }
    }
    
    /**
     * 验证复选框是否正确创建
     */
    private void validateCheckBoxes() {
        if (libraryCheckBoxes == null) {
            VueKitLogger.warn(LOG, "libraryCheckBoxes 为 null");
            return;
        }
        
        VueKitLogger.debug(LOG, project, "验证复选框状态:");
        VueKitLogger.debug(LOG, project, "- 复选框数量: " + libraryCheckBoxes.size());
        
        for (Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            String libraryName = entry.getKey();
            JBCheckBox checkBox = entry.getValue();
            
            VueKitLogger.debug(LOG, project, "- " + libraryName + 
                " (可见: " + checkBox.isVisible() + 
                ", 启用: " + checkBox.isEnabled() + 
                ", 文本: " + checkBox.getText() + ")");
        }
    }

    /**
     * 加载当前配置
     */
    private void loadCurrentConfig() {
        VueKitLogger.info(LOG, project, "=== loadCurrentConfig() 开始 ===");
        
        try {
            VueKitLogger.info(LOG, project, "开始获取当前启用的组件库...");
            
            // 获取当前启用的组件库名称
            currentEnabledLibraryNames = configManager.getEnabledLibraryNames(project);
            originalEnabledLibraryNames = new HashSet<>(currentEnabledLibraryNames);

            VueKitLogger.info(LOG, project, "获取到的组件库配置:");
            VueKitLogger.info(LOG, project, "- currentEnabledLibraryNames 数量: " + currentEnabledLibraryNames.size());
            VueKitLogger.info(LOG, project, "- currentEnabledLibraryNames 内容: " + String.join(", ", currentEnabledLibraryNames));

            // 检查是否需要自动匹配组件库
            if (currentEnabledLibraryNames.isEmpty()) {
                VueKitLogger.info(LOG, project, "没有启用的组件库，尝试自动匹配...");
                boolean autoMatchSuccess = performAutoMatching();
                if (autoMatchSuccess) {
                    VueKitLogger.info(LOG, project, "自动匹配成功，重新获取配置...");
                    // 重新获取配置
                    currentEnabledLibraryNames = configManager.getEnabledLibraryNames(project);
                    originalEnabledLibraryNames = new HashSet<>(currentEnabledLibraryNames);
                    VueKitLogger.info(LOG, project, "自动匹配后的配置: " + String.join(", ", currentEnabledLibraryNames));
                } else {
                    VueKitLogger.info(LOG, project, "自动匹配失败或没有匹配到组件库");
                }
            }

            // 更新复选框状态（只有在界面创建完成后才更新）
            if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
                VueKitLogger.info(LOG, project, "libraryCheckBoxes 已初始化，开始更新复选框状态...");
                updateCheckBoxes();
            } else {
                VueKitLogger.info(LOG, project, "libraryCheckBoxes 尚未初始化，跳过更新");
                VueKitLogger.info(LOG, project, "- libraryCheckBoxes: " + (libraryCheckBoxes == null ? "null" : "empty"));
            }

            // 更新调试模式复选框状态
            if (debugModeCheckBox != null) {
                VueKitLogger.info(LOG, project, "debugModeCheckBox 已初始化，开始更新调试模式状态...");
                boolean debugModeEnabled = configManager.isDebugModeEnabled(project);
                debugModeCheckBox.setSelected(debugModeEnabled);
                VueKitLogger.info(LOG, project, "调试模式复选框状态已更新: " + (debugModeEnabled ? "启用" : "禁用"));
            } else {
                VueKitLogger.info(LOG, project, "debugModeCheckBox 尚未初始化，跳过更新");
            }

            VueKitLogger.info(LOG, project, "当前配置已加载，启用的组件库: " + String.join(", ", currentEnabledLibraryNames));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载当前配置失败", e);
            Messages.showErrorDialog(project, "加载配置失败: " + e.getMessage(), "错误");
        }
        
        VueKitLogger.info(LOG, project, "=== loadCurrentConfig() 结束 ===");
    }

    /**
     * 更新复选框状态
     */
    private void updateCheckBoxes() {
        VueKitLogger.debug(LOG, project, "=== updateCheckBoxes() 开始 ===");
        
        // 确保 libraryCheckBoxes 已初始化
        if (libraryCheckBoxes == null || libraryCheckBoxes.isEmpty()) {
            VueKitLogger.debug(LOG, project, "libraryCheckBoxes 尚未初始化，跳过更新");
            return;
        }
        
        VueKitLogger.debug(LOG, project, "=== 更新复选框状态 ===");
        VueKitLogger.debug(LOG, project, "当前启用的组件库数量: " + currentEnabledLibraryNames.size());
        VueKitLogger.debug(LOG, project, "当前启用的组件库: " + String.join(", ", currentEnabledLibraryNames));
        
        VueKitLogger.debug(LOG, project, "复选框数量: " + libraryCheckBoxes.size());
        VueKitLogger.debug(LOG, project, "复选框列表:");
        for (Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            String libraryName = entry.getKey();
            VueKitLogger.debug(LOG, project, "- " + libraryName);
        }
        
        for (Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            String libraryName = entry.getKey();
            JBCheckBox checkBox = entry.getValue();

            boolean isEnabled = currentEnabledLibraryNames.contains(libraryName);
            checkBox.setSelected(isEnabled);
            
            VueKitLogger.debug(LOG, project, "复选框 '" + libraryName + "' -> " + 
                    (isEnabled ? "选中" : "未选中") + 
                    " (在 currentEnabledLibraryNames 中: " + currentEnabledLibraryNames.contains(libraryName) + ")");
        }
        
        VueKitLogger.debug(LOG, project, "=== updateCheckBoxes() 结束 ===");
    }

    /**
     * 更新配置
     */
    private void updateConfiguration() {
        VueKitLogger.debug(LOG, project, "=== updateConfiguration() 开始 ===");
        
        VueKitLogger.debug(LOG, project, "清空当前启用的组件库列表...");
        currentEnabledLibraryNames.clear();

        // 确保 libraryCheckBoxes 已初始化
        if (libraryCheckBoxes == null || libraryCheckBoxes.isEmpty()) {
            VueKitLogger.debug(LOG, project, "libraryCheckBoxes 尚未初始化，跳过配置更新");
            return;
        }

        VueKitLogger.debug(LOG, project, "开始检查复选框状态...");
        VueKitLogger.debug(LOG, project, "复选框数量: " + libraryCheckBoxes.size());
        
        for (Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            String libraryName = entry.getKey();
            JBCheckBox checkBox = entry.getValue();

            boolean isSelected = checkBox.isSelected();
            VueKitLogger.debug(LOG, project, "复选框 '" + libraryName + "' 状态: " + 
                    (isSelected ? "选中" : "未选中"));

            if (isSelected) {
                currentEnabledLibraryNames.add(libraryName);
                VueKitLogger.debug(LOG, project, "已添加到启用的组件库: " + libraryName);
            }
        }
        
        VueKitLogger.debug(LOG, project, "配置更新完成:");
        VueKitLogger.debug(LOG, project, "- 启用的组件库数量: " + currentEnabledLibraryNames.size());
        VueKitLogger.debug(LOG, project, "- 启用的组件库: " + String.join(", ", currentEnabledLibraryNames));
        
        VueKitLogger.debug(LOG, project, "=== updateConfiguration() 结束 ===");
    }

    /**
     * 导入配置
     */
    private void importConfiguration() {
        try {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                    .withFileFilter(file -> file.getName().endsWith(".json"))
                    .withTitle("选择配置文件")
                    .withDescription("选择要导入的 VueKit 组件库配置文件");

            FileChooser.chooseFile(descriptor, project, null, virtualFile -> {
                if (virtualFile != null) {
                    String importPath = virtualFile.getPath();
                    boolean success = configManager.importProjectConfig(project, importPath);

                    if (success) {
                        Messages.showInfoMessage(project, "配置导入成功！", "成功");
                        loadCurrentConfig(); // 重新加载配置
                    } else {
                        Messages.showErrorDialog(project, "配置导入失败！", "错误");
                    }
                }
            });

        } catch (Exception e) {
            VueKitLogger.error(LOG, "导入配置失败", e);
            Messages.showErrorDialog(project, "导入配置失败: " + e.getMessage(), "错误");
        }
    }

    /**
     * 导出配置
     */
    private void exportConfiguration() {
        try {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false)
                    .withFileFilter(file -> file.getName().endsWith(".json"))
                    .withTitle("保存配置文件")
                    .withDescription("选择保存 VueKit 组件库配置文件的位置");

            FileChooser.chooseFile(descriptor, project, null, virtualFile -> {
                if (virtualFile != null) {
                    String exportPath = virtualFile.getPath();
                    boolean success = configManager.exportProjectConfig(project, exportPath);

                    if (success) {
                        Messages.showInfoMessage(project, "配置导出成功！", "成功");
                    } else {
                        Messages.showErrorDialog(project, "配置导出失败！", "错误");
                    }
                }
            });

        } catch (Exception e) {
            VueKitLogger.error(LOG, "导出配置失败", e);
            Messages.showErrorDialog(project, "导出配置失败: " + e.getMessage(), "错误");
        }
    }

    /**
     * 重置为全局默认
     */
    private void resetToGlobalDefault() {
        try {
            int result = Messages.showYesNoDialog(project,
                    "确定要重置当前项目的组件库配置为全局默认吗？\n这将覆盖当前的所有项目级配置。",
                    "确认重置",
                    Messages.getQuestionIcon());

            if (result == Messages.YES) {
                configManager.resetToGlobalDefault(project);
                loadCurrentConfig(); // 重新加载配置
                Messages.showInfoMessage(project, "配置已重置为全局默认！", "成功");
            }

        } catch (Exception e) {
            VueKitLogger.error(LOG, "重置配置失败", e);
            Messages.showErrorDialog(project, "重置配置失败: " + e.getMessage(), "错误");
        }
    }

    /**
     * 检测项目依赖
     */
    private void detectProjectDependencies() {
        try {
            // 这里可以集成 SmartComponentFilter 的检测逻辑
            // 暂时显示一个信息对话框
            Messages.showInfoMessage(project,
                    "项目依赖检测功能正在开发中...\n" +
                            "当前系统会自动检测项目 package.json 中的组件库依赖。",
                    "功能提示");

        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测项目依赖失败", e);
            Messages.showErrorDialog(project, "检测项目依赖失败: " + e.getMessage(), "错误");
        }
    }

    /**
     * 获取全局配置路径
     *
     * @return 全局配置路径
     */
    private String getGlobalConfigPath() {
        String userHome = System.getProperty("user.home");
        return userHome + File.separator + ".vuekit" + File.separator + "vuekit-libraries.json";
    }

    /**
     * 获取组件库提示信息
     *
     * @param libraryName 组件库名称
     * @return 提示信息
     */
    private String getLibraryTooltip(String libraryName) {
        try {
            // 动态从远程组件库管理器获取组件库信息
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library = 
                libraryManager.getLibrary(libraryName);
            
            if (library != null && library.getDescription() != null && !library.getDescription().trim().isEmpty()) {
                return library.getDescription();
            }
            
            // 如果远程获取失败，使用动态生成的描述
            return generateLibraryDescription(libraryName);
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, project, "动态获取组件库描述失败，使用默认描述: " + e.getMessage());
            return generateLibraryDescription(libraryName);
        }
    }
    
    /**
     * 动态生成组件库描述（智能推断，避免硬编码）
     *
     * @param libraryName 组件库名称
     * @return 生成的描述
     */
    private String generateLibraryDescription(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "Vue 组件库";
        }
        
        // 优先从已安装的组件库中获取描述
        try {
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                if (libraryName.equals(library.getName())) {
                    // 如果组件库有自定义的描述，使用它
                    String description = library.getDescription();
                    if (description != null && !description.trim().isEmpty()) {
                        return libraryName + " - " + description;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // 如果获取失败，使用智能推断
        }
        
        // 使用智能推断作为后备方案，避免硬编码特定组件库
        String normalizedName = libraryName.toLowerCase();
        
        // 基于组件库名称模式智能推断描述
        if (StringNormalizer.contains(normalizedName, "element")) {
            if (StringNormalizer.contains(normalizedName, "plus")) {
                return libraryName + " - 基于 Vue 3.x 的桌面端组件库";
            } else {
                return libraryName + " - 基于 Vue 2.x 的桌面端组件库";
            }
        } else if (StringNormalizer.contains(normalizedName, "ant") || StringNormalizer.contains(normalizedName, "design")) {
            return libraryName + " - 基于 Ant Design 设计体系的 Vue 组件库";
        } else if (StringNormalizer.contains(normalizedName, "vuetify")) {
            return libraryName + " - 基于 Material Design 的 Vue 组件库";
        } else if (StringNormalizer.contains(normalizedName, "quasar")) {
            return libraryName + " - 基于 Vue 的跨平台 UI 框架";
        } else if (StringNormalizer.contains(normalizedName, "naive")) {
            return libraryName + " - 基于 Vue 3.x 的 TypeScript 组件库";
        } else if (StringNormalizer.contains(normalizedName, "prime")) {
            return libraryName + " - 基于 Vue 的丰富 UI 组件库";
        } else {
            // 对于未知的组件库，生成通用描述
            return libraryName + " - Vue 组件库";
        }
    }

    @Override
    protected void doOKAction() {
        VueKitLogger.debug(LOG, project, "=== doOKAction() 开始 ===");
        
        try {
            VueKitLogger.debug(LOG, project, "开始更新配置...");
            
            // 更新配置
            updateConfiguration();
            
            VueKitLogger.debug(LOG, project, "配置更新完成，当前启用的组件库:");
            VueKitLogger.debug(LOG, project, "- 数量: " + currentEnabledLibraryNames.size());
            VueKitLogger.debug(LOG, project, "- 内容: " + String.join(", ", currentEnabledLibraryNames));

            VueKitLogger.debug(LOG, project, "开始保存配置到文件...");
            
            // 保存配置
            configManager.setProjectEnabledLibraryNames(project, currentEnabledLibraryNames);
            
            VueKitLogger.debug(LOG, project, "配置已保存到文件");

            // 关闭对话框
            super.doOKAction();

            VueKitLogger.info(LOG, "组件库配置已保存，启用的组件库: " + String.join(", ", currentEnabledLibraryNames));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存配置失败", e);
            Messages.showErrorDialog(project, "保存配置失败: " + e.getMessage(), "错误");
        }
        
        VueKitLogger.debug(LOG, project, "=== doOKAction() 结束 ===");
    }

    @Override
    protected ValidationInfo doValidate() {
        // 检查是否至少选择了一个组件库
        if (currentEnabledLibraryNames.isEmpty()) {
            // 确保 libraryCheckBoxes 已初始化且有内容
            if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
                return new ValidationInfo("请至少选择一个组件库", libraryCheckBoxes.values().iterator().next());
            } else {
                return new ValidationInfo("请至少选择一个组件库", null);
            }
        }

        return null;
    }

    /**
     * 执行自动匹配组件库
     * 根据 package.json 中的依赖自动匹配组件库
     *
     * @return 如果成功匹配到组件库则返回 true
     */
    private boolean performAutoMatching() {
        VueKitLogger.info(LOG, project, "=== 开始自动匹配组件库 ===");
        
        try {
            // 获取 package.json 中的依赖
            Map<String, String> dependencies = getProjectDependencies(project);
            if (dependencies == null || dependencies.isEmpty()) {
                VueKitLogger.info(LOG, project, "package.json 中没有找到依赖");
                return false;
            }
            
            VueKitLogger.info(LOG, project, "package.json 依赖数量: " + dependencies.size());
            VueKitLogger.info(LOG, project, "依赖列表: " + dependencies);
            
            // 获取可用的组件库
            String[] availableLibraryArray = getAvailableLibraryNames();
            Set<String> availableLibraries = new HashSet<>(Arrays.asList(availableLibraryArray));
            VueKitLogger.info(LOG, project, "可用组件库数量: " + availableLibraries.size());
            VueKitLogger.info(LOG, project, "可用组件库: " + availableLibraries);
            
            // 执行匹配
            Set<String> matchedLibraries = new HashSet<>();
            for (Map.Entry<String, String> entry : dependencies.entrySet()) {
                String packageName = entry.getKey();
                String version = entry.getValue();
                
                VueKitLogger.info(LOG, project, "检查依赖: " + packageName + " (版本: " + version + ")");
                
                // 标准化包名（转小写，替换空格）
                String normalizedPackageName = normalizePackageName(packageName);
                VueKitLogger.info(LOG, project, "标准化后的包名: " + normalizedPackageName);
                
                // 检查是否匹配任何可用组件库
                for (String libraryName : availableLibraries) {
                    String normalizedLibraryName = normalizePackageName(libraryName);
                    VueKitLogger.info(LOG, project, "比较: " + normalizedPackageName + " vs " + normalizedLibraryName);
                    
                    if (normalizedPackageName.equals(normalizedLibraryName)) {
                        VueKitLogger.info(LOG, project, "✅ 匹配成功: " + packageName + " -> " + libraryName);
                        matchedLibraries.add(libraryName);
                        break;
                    }
                }
            }
            
            VueKitLogger.info(LOG, project, "自动匹配结果: " + matchedLibraries);
            
            // 如果匹配到组件库，自动启用它们
            if (!matchedLibraries.isEmpty()) {
                VueKitLogger.info(LOG, project, "自动启用匹配的组件库: " + matchedLibraries);
                
                // 确保配置保存是同步的
                try {
                    configManager.setProjectEnabledLibraryNames(project, matchedLibraries);
                    
                    // 等待一小段时间确保文件写入完成
                    Thread.sleep(100);
                    
                    // 验证配置是否已保存
                    Set<String> savedLibraries = configManager.getEnabledLibraryNames(project);
                    if (savedLibraries.containsAll(matchedLibraries)) {
                        VueKitLogger.info(LOG, project, "配置保存成功，验证通过");
                    } else {
                        VueKitLogger.warn(LOG, project, "配置保存可能不完整，期望: " + matchedLibraries + ", 实际: " + savedLibraries);
                    }
                    
                } catch (Exception e) {
                    VueKitLogger.error(LOG, project, "保存自动匹配的配置失败", e);
                    return false;
                }
                
                // 更新当前配置
                currentEnabledLibraryNames = new HashSet<>(matchedLibraries);
                originalEnabledLibraryNames = new HashSet<>(matchedLibraries);
                
                // 更新复选框状态
                if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
                    updateCheckBoxes();
                }
                
                VueKitLogger.info(LOG, project, "自动匹配完成，启用的组件库: " + matchedLibraries);
                return true;
            } else {
                VueKitLogger.info(LOG, project, "没有匹配到任何组件库");
                return false;
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, project, "自动匹配组件库失败", e);
            return false;
        }
    }

    /**
     * 标准化包名
     * 转小写并替换空格
     *
     * @param packageName 原始包名
     * @return 标准化后的包名
     */
    private String normalizePackageName(String packageName) {
        if (packageName == null) {
            return "";
        }
        return packageName.toLowerCase().replaceAll("\\s+", "");
    }

    /**
     * 获取项目的 package.json 依赖
     *
     * @param project 项目对象
     * @return 依赖映射，键为包名，值为版本
     */
    private Map<String, String> getProjectDependencies(Project project) {
        Map<String, String> dependencies = new HashMap<>();
        
        try {
            // 查找 package.json 文件
            VirtualFile projectRoot = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            if (projectRoot == null) {
                VueKitLogger.warn(LOG, project, "无法获取项目根目录");
                return dependencies;
            }
            
            VirtualFile packageJsonFile = projectRoot.findChild("package.json");
            if (packageJsonFile == null || !packageJsonFile.exists()) {
                VueKitLogger.debug(LOG, project, "未找到 package.json 文件");
                return dependencies;
            }
            
            // 读取并解析 package.json
            String content = new String(packageJsonFile.contentsToByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            com.google.gson.JsonObject packageJson = com.google.gson.JsonParser.parseString(content).getAsJsonObject();
            
            // 检查各种依赖类型
            String[] dependencyTypes = {"dependencies", "devDependencies", "peerDependencies"};
            
            for (String dependencyType : dependencyTypes) {
                if (packageJson.has(dependencyType)) {
                    com.google.gson.JsonObject deps = packageJson.getAsJsonObject(dependencyType);
                    for (String packageName : deps.keySet()) {
                        String version = deps.get(packageName).getAsString();
                        dependencies.put(packageName, version);
                        VueKitLogger.debug(LOG, project, "找到依赖: " + packageName + " (版本: " + version + ")");
                    }
                }
            }
            
            VueKitLogger.info(LOG, project, "总共找到 " + dependencies.size() + " 个依赖");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, project, "解析 package.json 失败", e);
        }
        
        return dependencies;
    }

    @Override
    public void doCancelAction() {
        // 检查是否有未保存的更改
        if (!currentEnabledLibraryNames.equals(originalEnabledLibraryNames)) {
            int result = Messages.showYesNoDialog(project,
                    "您有未保存的配置更改，确定要取消吗？",
                    "确认取消",
                    Messages.getQuestionIcon());

            if (result == Messages.NO) {
                return; // 继续编辑
            }
        }

        super.doCancelAction();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        // 确保 libraryCheckBoxes 已初始化
        if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
            return libraryCheckBoxes.values().iterator().next();
        }
        // 如果还没有初始化，返回主面板
        return mainPanel;
    }
}
