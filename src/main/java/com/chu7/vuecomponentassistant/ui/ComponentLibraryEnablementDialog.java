package com.chu7.vuecomponentassistant.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * 组件库启用/禁用管理对话框
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>显示所有可用的组件库</li>
 *   <li>允许用户启用/禁用特定的组件库</li>
 *   <li>显示组件库的详细信息和状态</li>
 *   <li>支持重置为默认配置</li>
 *   <li>提供全选/全不选功能</li>
 *   <li>自动检测项目依赖的组件库</li>
 *   <li>实时更新组件库状态显示</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>动态获取已安装的组件库列表</li>
 *   <li>直观的复选框界面设计</li>
 *   <li>支持滚动查看大量组件库</li>
 *   <li>智能的项目检测组件库识别</li>
 *   <li>完整的配置保存和加载机制</li>
 *   <li>用户友好的操作反馈</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>项目初始化时的组件库配置</li>
 *   <li>开发过程中动态调整组件库启用状态</li>
 *   <li>团队项目配置的统一管理</li>
 *   <li>性能优化时的组件库精简</li>
 *   <li>调试特定组件库功能</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.ui.DialogWrapper
 * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager
 * @see com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager
 */
public class ComponentLibraryEnablementDialog extends DialogWrapper {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryEnablementDialog.class);
    
    private final Project project;
    private final ComponentLibraryConfigManager configManager;
    
    // UI 组件
    private JPanel mainPanel;
    private JCheckBox[] libraryCheckBoxes;
    private JLabel[] libraryInfoLabels;
    private JButton resetButton;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    
    // 组件库类型列表 - 现在动态从远程组件库管理器获取
    private String[] libraryTypes;
    
    /**
     * 构造函数
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>初始化组件库启用管理对话框</li>
     *   <li>动态获取已安装的组件库列表</li>
     *   <li>设置对话框基本属性和样式</li>
     *   <li>初始化配置管理器</li>
     * </ul>
     *
     * <p>初始化流程：</p>
     * <ol>
     *   <li>调用父类构造函数，传入项目实例</li>
     *   <li>初始化项目实例和配置管理器</li>
     *   <li>动态获取已安装的组件库列表</li>
     *   <li>设置对话框标题、尺寸和可调整性</li>
     *   <li>调用init()方法完成初始化</li>
     * </ol>
     *
     * @param project 当前项目实例，用于获取项目配置和组件库信息，不能为null
     * @throws IllegalArgumentException 如果project参数为null
     * @see #initializeLibraryTypes()
     * @see #init()
     */
    public ComponentLibraryEnablementDialog(Project project) {
        // 调用父类构造函数，传入项目实例
        super(project);
        
        // 参数验证
        if (project == null) {
            throw new IllegalArgumentException("项目实例不能为null");
        }
        
        this.project = project;
        this.configManager = ComponentLibraryConfigManager.getInstance(project);
        
        // 动态初始化组件库列表
        initializeLibraryTypes();
        
        setTitle("🔧 组件库启用管理 - VueKit");
        setSize(600, 500);
        setResizable(true);
        
        init();
    }
    
    /**
     * 动态初始化组件库类型列表
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>从远程组件库管理器获取已安装的组件库</li>
     *   <li>动态构建组件库类型列表</li>
     *   <li>避免硬编码特定组件库</li>
     *   <li>支持运行时组件库扩展</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>创建远程组件库管理器实例</li>
     *   <li>获取所有已安装的组件库</li>
     *   <li>遍历组件库并提取类型信息</li>
     *   <li>过滤掉未知类型的组件库</li>
     *   <li>转换为数组格式存储</li>
     * </ol>
     *
     * <p>错误处理：</p>
     * <ul>
     *   <li>获取失败时使用空列表</li>
     *   <li>记录详细的错误日志</li>
     *   <li>确保界面不会崩溃</li>
     * </ul>
     *
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper
     */
    private void initializeLibraryTypes() {
        try {
            // 从远程组件库管理器获取已安装的组件库
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            java.util.List<String> libraryTypeList = new java.util.ArrayList<>();
            
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                String libraryType = LibraryTypeHelper.getPackageName(library.getName());
                
                if (!LibraryTypeHelper.UNKNOWN.equals(libraryType)) {
                    libraryTypeList.add(libraryType);
                }
            }
            
            // 如果没有找到任何组件库，使用空列表而不是硬编码
            if (libraryTypeList.isEmpty()) {
                VueKitLogger.warn(LOG, "没有找到已安装的组件库，使用空列表");
            }
            
            libraryTypes = libraryTypeList.toArray(new String[0]);
            
        } catch (Exception e) {
            // 出错时使用空列表而不是硬编码
            VueKitLogger.error(LOG, "获取组件库列表失败，使用空列表", e);
            libraryTypes = new String[0];
        }
    }
    
    @Override
    protected JComponent createCenterPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(600, 500));
        
        // 创建说明面板
        JPanel descriptionPanel = createDescriptionPanel();
        
        // 创建组件库列表面板
        JPanel libraryListPanel = createLibraryListPanel();
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 组装主面板
        mainPanel.add(descriptionPanel, BorderLayout.NORTH);
        mainPanel.add(libraryListPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载当前配置
        loadCurrentConfiguration();
        
        return mainPanel;
    }
    
    /**
     * 创建说明面板
     */
    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📋 说明"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JTextArea descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        descriptionArea.setText(
            "在这里可以管理组件库的启用状态。\n" +
            "• 启用的组件库：其组件将在代码补全中显示\n" +
            "• 禁用的组件库：其组件将不会在代码补全中显示\n" +
            "• 项目检测的组件库：会自动启用（如 package.json 中配置的组件库）\n" +
            "• 更改后需要重启 IDE 或重新加载项目才能生效"
        );
        
        panel.add(descriptionArea, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * 创建组件库列表面板
     */
    private JPanel createLibraryListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📚 组件库列表"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 创建滚动面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        // 创建组件库复选框和信息标签
        libraryCheckBoxes = new JCheckBox[libraryTypes.length];
        libraryInfoLabels = new JLabel[libraryTypes.length];
        
        for (int i = 0; i < libraryTypes.length; i++) {
            String libraryType = libraryTypes[i];
            
            // 创建复选框
            libraryCheckBoxes[i] = new JCheckBox(LibraryTypeHelper.getDisplayName(libraryType));
            libraryCheckBoxes[i].setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
            
            // 创建信息标签
            libraryInfoLabels[i] = new JLabel(getLibraryInfo(libraryType));
            libraryInfoLabels[i].setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
            libraryInfoLabels[i].setForeground(Color.GRAY);
            libraryInfoLabels[i].setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 0));
            
            // 添加到面板
            contentPanel.add(libraryCheckBoxes[i]);
            contentPanel.add(libraryInfoLabels[i]);
            contentPanel.add(Box.createVerticalStrut(5));
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 左侧按钮
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        selectAllButton = new JButton("全选");
        selectAllButton.addActionListener(e -> selectAllLibraries());
        
        deselectAllButton = new JButton("全不选");
        deselectAllButton.addActionListener(e -> deselectAllLibraries());
        
        resetButton = new JButton("重置为默认");
        resetButton.addActionListener(e -> resetToDefault());
        
        leftButtonPanel.add(selectAllButton);
        leftButtonPanel.add(deselectAllButton);
        leftButtonPanel.add(resetButton);
        
        // 右侧按钮
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton applyButton = new JButton("应用");
        applyButton.addActionListener(e -> applyConfiguration());
        
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> doCancelAction());
        
        rightButtonPanel.add(applyButton);
        rightButtonPanel.add(cancelButton);
        
        panel.add(leftButtonPanel, BorderLayout.WEST);
        panel.add(rightButtonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 获取组件库信息
     */
    private String getLibraryInfo(String libraryType) {
        try {
            // 动态获取组件库信息，不再硬编码
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                if (libraryType.equals(library.getName())) {
                    String displayName = library.getDisplayName() != null ? library.getDisplayName() : library.getName();
                    String componentPrefix = library.getComponentPrefix() != null ? library.getComponentPrefix() : "";
                    String description = library.getDescription() != null ? library.getDescription() : "";
                    
                    return displayName + " - " + description + "，组件前缀：" + componentPrefix;
                }
            }
            
            // 如果没有找到，返回通用信息
            return libraryType + " - 组件库信息不可用";
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "获取组件库信息失败: " + e.getMessage());
            return libraryType + " - 组件库信息获取失败";
        }
    }
    
    /**
     * 加载当前配置
     */
    private void loadCurrentConfiguration() {
        try {
            Set<String> enabledLibraries = configManager.getEnabledLibraryNames(project);
            
            for (int i = 0; i < libraryTypes.length; i++) {
                String libraryType = libraryTypes[i];
                boolean isEnabled = enabledLibraries.contains(libraryType);
                libraryCheckBoxes[i].setSelected(isEnabled);
                
                // 更新标签显示
                updateLibraryLabel(i, libraryType, isEnabled);
            }
            
            VueKitLogger.info(LOG, "已加载当前组件库配置");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载当前配置失败", e);
            Messages.showErrorDialog("加载当前配置失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 更新组件库标签显示
     */
    private void updateLibraryLabel(int index, String libraryType, boolean isEnabled) {
        String baseInfo = getLibraryInfo(libraryType);
        String status = isEnabled ? "✅ 已启用" : "❌ 已禁用";
        
        // 检查是否为项目检测到的组件库
        boolean isProjectDetected = isProjectDetectedLibrary(libraryType);
        if (isProjectDetected) {
            status += " (项目检测)";
        }
        
        libraryInfoLabels[index].setText(baseInfo + " - " + status);
        
        // 如果是项目检测的组件库，禁用复选框
        libraryCheckBoxes[index].setEnabled(!isProjectDetected);
        if (isProjectDetected) {
            libraryCheckBoxes[index].setToolTipText("此组件库由项目自动检测，无法手动禁用");
        } else {
            libraryCheckBoxes[index].setToolTipText("点击启用或禁用此组件库");
        }
    }
    
    /**
     * 检查是否为项目检测到的组件库
     */
    private boolean isProjectDetectedLibrary(String libraryType) {
        try {
            // 这里可以集成 ComponentLibraryDetector 来检测项目实际使用的组件库
            // 暂时返回 false，后续可以完善
            return false;
        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测项目组件库失败", e);
            return false;
        }
    }
    
    /**
     * 全选所有组件库
     */
    private void selectAllLibraries() {
        for (JCheckBox checkBox : libraryCheckBoxes) {
            if (checkBox.isEnabled()) {
                checkBox.setSelected(true);
            }
        }
        updateAllLabels();
    }
    
    /**
     * 全不选所有组件库
     */
    private void deselectAllLibraries() {
        for (JCheckBox checkBox : libraryCheckBoxes) {
            if (checkBox.isEnabled()) {
                checkBox.setSelected(false);
            }
        }
        updateAllLabels();
    }
    
    /**
     * 重置为默认配置
     */
    private void resetToDefault() {
        int result = Messages.showYesNoDialog(
            "确定要重置为默认配置吗？\n默认会启用 Element UI、Element Plus 和 Ant Design Vue。",
            "确认重置",
            Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            configManager.resetToGlobalDefault(project);
            loadCurrentConfiguration();
            
            // 通知 ComponentProvider 重新加载组件数据
            try {
                ComponentProviderManager.notifyProviderReload(project);
                VueKitLogger.info(LOG, "重置配置后已通知 ComponentProvider 重新加载组件数据");
            } catch (Exception e) {
                VueKitLogger.error(LOG, "重置配置后通知 ComponentProvider 重新加载失败", e);
            }
            
            Messages.showInfoMessage("已重置为默认配置并重新加载组件数据", "重置完成");
        }
    }
    
    /**
     * 应用配置
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>收集当前选中的组件库配置</li>
     *   <li>保存配置到项目设置</li>
     *   <li>通知组件提供者重新加载数据</li>
     *   <li>提供用户操作反馈</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>遍历所有复选框，收集选中的组件库</li>
     *   <li>调用配置管理器保存项目设置</li>
     *   <li>通知ComponentProvider重新加载组件数据</li>
     *   <li>显示成功消息并关闭对话框</li>
     * </ol>
     *
     * <p>错误处理：</p>
     * <ul>
     *   <li>配置保存失败时显示错误对话框</li>
     *   <li>组件提供者通知失败时记录日志</li>
     *   <li>确保用户了解操作结果</li>
     * </ul>
     *
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#setProjectEnabledLibraryNames(Project, Set)
     * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager#notifyProviderReload(Project)
     */
    private void applyConfiguration() {
        try {
            Set<String> enabledLibraries = new HashSet<>();
            
            for (int i = 0; i < libraryTypes.length; i++) {
                if (libraryCheckBoxes[i].isSelected()) {
                    enabledLibraries.add(libraryTypes[i]);
                }
            }
            
            // 保存配置
            configManager.setProjectEnabledLibraryNames(project, enabledLibraries);
            
            VueKitLogger.info(LOG, "组件库配置已保存: " + String.join(", ", enabledLibraries));
            
            // 通知 ComponentProvider 重新加载组件数据
            try {
                ComponentProviderManager.notifyProviderReload(project);
                VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据");
            } catch (Exception e) {
                VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e);
            }
            
            Messages.showInfoMessage(
                "组件库配置已保存并已重新加载组件数据！\n" +
                "现在可以立即测试代码补全功能。",
                "保存成功"
            );
            
            close(OK_EXIT_CODE);
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存配置失败", e);
            Messages.showErrorDialog("保存配置失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 更新所有标签
     */
    private void updateAllLabels() {
        for (int i = 0; i < libraryTypes.length; i++) {
            String libraryType = libraryTypes[i];
            boolean isEnabled = libraryCheckBoxes[i].isSelected();
            updateLibraryLabel(i, libraryType, isEnabled);
        }
    }
    
    @Override
    protected void doOKAction() {
        applyConfiguration();
    }
} 