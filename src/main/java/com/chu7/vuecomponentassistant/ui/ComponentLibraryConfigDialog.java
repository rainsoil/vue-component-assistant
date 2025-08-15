package com.chu7.vuecomponentassistant.ui;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
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
    private Map<ComponentLibraryDetector.LibraryType, JBCheckBox> libraryCheckBoxes;
    private JBTextField projectNameField;
    private JBTextField globalConfigPathField;

    // 配置数据
    private Set<ComponentLibraryDetector.LibraryType> currentEnabledLibraries;
    private Set<ComponentLibraryDetector.LibraryType> originalEnabledLibraries;

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
        VueKitLogger.debug(LOG, "开始创建中心面板");
        
        // 使用 GridBagLayout 确保内容正确显示
        mainPanel = new JBPanel(new GridBagLayout());
        mainPanel.setBorder(JBUI.Borders.empty(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        createMainInterface();
        
        // 在界面创建完成后加载配置
        loadCurrentConfig();
        
        VueKitLogger.debug(LOG, "中心面板创建完成，主面板大小: " + mainPanel.getSize());
        
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

        // 2. 组件库配置面板
        JBPanel libraryConfigPanel = createLibraryConfigPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(libraryConfigPanel, gbc);

        // 3. 操作按钮面板
        JBPanel actionPanel = createActionPanel();
        gbc.gridx = 0;
        gbc.gridy = 2;
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

        return panel;
    }

    /**
     * 创建组件库复选框
     *
     * @return 复选框面板
     */
    private JBPanel createLibraryCheckBoxes() {
        // 使用 GridLayout 确保复选框可见
        JBPanel panel = new JBPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(JBUI.Borders.customLine(Color.BLACK, 2)); // 添加明显的边框
        panel.setBackground(Color.WHITE); // 设置白色背景

        libraryCheckBoxes = new HashMap<>();

        // 获取所有可用的组件库类型
        ComponentLibraryDetector.LibraryType[] libraryTypes = ComponentLibraryDetector.LibraryType.values();
        
        // 添加调试信息
        VueKitLogger.debug(LOG, "创建组件库复选框，找到 " + libraryTypes.length + " 个组件库类型");

        // 添加标题标签
        JBLabel checkBoxTitle = new JBLabel("可用的组件库：");
        checkBoxTitle.setFont(checkBoxTitle.getFont().deriveFont(Font.BOLD, 14f));
        checkBoxTitle.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
        panel.add(checkBoxTitle);

        for (ComponentLibraryDetector.LibraryType libraryType : libraryTypes) {
            if (libraryType != ComponentLibraryDetector.LibraryType.UNKNOWN) {
                // 创建复选框
                JBCheckBox checkBox = new JBCheckBox(libraryType.getDisplayName());
                checkBox.setToolTipText(getLibraryTooltip(libraryType));
                
                // 设置复选框为可见和启用状态
                checkBox.setVisible(true);
                checkBox.setEnabled(true);
                checkBox.setSelected(true); // 默认选中，确保可见性

                // 添加选择监听器
                checkBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updateConfiguration();
                    }
                });

                libraryCheckBoxes.put(libraryType, checkBox);
                panel.add(checkBox);
                
                VueKitLogger.debug(LOG, "添加复选框: " + libraryType.getDisplayName());
            }
        }
        
        VueKitLogger.debug(LOG, "总共创建了 " + libraryCheckBoxes.size() + " 个复选框");

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
     * 验证复选框是否正确创建
     */
    private void validateCheckBoxes() {
        if (libraryCheckBoxes == null) {
            VueKitLogger.warn(LOG, "libraryCheckBoxes 为 null");
            return;
        }
        
        VueKitLogger.debug(LOG, "验证复选框状态:");
        VueKitLogger.debug(LOG, "- 复选框数量: " + libraryCheckBoxes.size());
        
        for (Map.Entry<ComponentLibraryDetector.LibraryType, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            ComponentLibraryDetector.LibraryType libraryType = entry.getKey();
            JBCheckBox checkBox = entry.getValue();
            
            VueKitLogger.debug(LOG, "- " + libraryType.getDisplayName() + 
                " (可见: " + checkBox.isVisible() + 
                ", 启用: " + checkBox.isEnabled() + 
                ", 文本: " + checkBox.getText() + ")");
        }
    }

    /**
     * 加载当前配置
     */
    private void loadCurrentConfig() {
        try {
            // 获取当前启用的组件库
            currentEnabledLibraries = configManager.getEnabledLibraries(project);
            originalEnabledLibraries = new HashSet<>(currentEnabledLibraries);

            // 更新复选框状态（只有在界面创建完成后才更新）
            if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
                updateCheckBoxes();
            }

            VueKitLogger.debug(LOG, "当前配置已加载，启用的组件库: " +
                    currentEnabledLibraries.stream()
                            .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                            .collect(java.util.stream.Collectors.joining(", ")));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载当前配置失败", e);
            Messages.showErrorDialog(project, "加载配置失败: " + e.getMessage(), "错误");
        }
    }

    /**
     * 更新复选框状态
     */
    private void updateCheckBoxes() {
        // 确保 libraryCheckBoxes 已初始化
        if (libraryCheckBoxes == null || libraryCheckBoxes.isEmpty()) {
            VueKitLogger.debug(LOG, "libraryCheckBoxes 尚未初始化，跳过更新");
            return;
        }
        
        for (Map.Entry<ComponentLibraryDetector.LibraryType, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            ComponentLibraryDetector.LibraryType libraryType = entry.getKey();
            JBCheckBox checkBox = entry.getValue();

            boolean isEnabled = currentEnabledLibraries.contains(libraryType);
            checkBox.setSelected(isEnabled);
        }
    }

    /**
     * 更新配置
     */
    private void updateConfiguration() {
        currentEnabledLibraries.clear();

        // 确保 libraryCheckBoxes 已初始化
        if (libraryCheckBoxes == null || libraryCheckBoxes.isEmpty()) {
            VueKitLogger.debug(LOG, "libraryCheckBoxes 尚未初始化，跳过配置更新");
            return;
        }

        for (Map.Entry<ComponentLibraryDetector.LibraryType, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
            ComponentLibraryDetector.LibraryType libraryType = entry.getKey();
            JBCheckBox checkBox = entry.getValue();

            if (checkBox.isSelected()) {
                currentEnabledLibraries.add(libraryType);
            }
        }
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
     * @param libraryType 组件库类型
     * @return 提示信息
     */
    private String getLibraryTooltip(ComponentLibraryDetector.LibraryType libraryType) {
        switch (libraryType) {
            case ELEMENT_UI:
                return "Element UI - 基于 Vue 2.x 的桌面端组件库";
            case ELEMENT_PLUS:
                return "Element Plus - 基于 Vue 3.x 的桌面端组件库";
            case ANT_DESIGN_VUE:
                return "Ant Design Vue - 基于 Ant Design 设计体系的 Vue 组件库";
            case VUETIFY:
                return "Vuetify - 基于 Material Design 的 Vue 组件库";
            case QUASAR:
                return "Quasar - 基于 Vue 的跨平台 UI 框架";

            default:
                return "未知组件库类型";
        }
    }

    @Override
    protected void doOKAction() {
        try {
            // 更新配置
            updateConfiguration();

            // 保存配置
            configManager.setProjectEnabledLibraries(project, currentEnabledLibraries);

            // 关闭对话框
            super.doOKAction();

            VueKitLogger.info(LOG, "组件库配置已保存，启用的组件库: " +
                    currentEnabledLibraries.stream()
                            .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                            .collect(java.util.stream.Collectors.joining(", ")));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存配置失败", e);
            Messages.showErrorDialog(project, "保存配置失败: " + e.getMessage(), "错误");
        }
    }

    @Override
    protected ValidationInfo doValidate() {
        // 检查是否至少选择了一个组件库
        if (currentEnabledLibraries.isEmpty()) {
            // 确保 libraryCheckBoxes 已初始化且有内容
            if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
                return new ValidationInfo("请至少选择一个组件库", libraryCheckBoxes.values().iterator().next());
            } else {
                return new ValidationInfo("请至少选择一个组件库", null);
            }
        }

        return null;
    }

    @Override
    public void doCancelAction() {
        // 检查是否有未保存的更改
        if (!currentEnabledLibraries.equals(originalEnabledLibraries)) {
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
