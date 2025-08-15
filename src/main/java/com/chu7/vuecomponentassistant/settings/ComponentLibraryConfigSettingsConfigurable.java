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
        
        // 创建配置面板
        configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.SOUTH);
        
        // 初始化配置
        initializeConfig();
        
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
            "<p><b>组件库配置功能说明：</b></p>" +
            "<p>• 启用/禁用项目中的组件库</p>" +
            "<p>• 配置组件库的优先级</p>" +
            "<p>• 管理组件库的同步状态</p>" +
            "<p>• 查看组件库的详细信息</p>" +
            "<p>• 支持 Element Plus、Element UI、Ant Design Vue 等主流组件库</p>" +
            "<p>• 支持 Vuetify、Quasar 等现代化组件库</p>" +
            "<p>• 支持自定义组件库配置</p>" +
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
        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1, 5, 5));
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
        
        JButton resetButton = new JButton("重置");
        resetButton.setFont(resetButton.getFont().deriveFont(Font.PLAIN, 12f));
        resetButton.setPreferredSize(new Dimension(100, 30));
        resetButton.setBackground(new Color(169, 169, 169));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void initializeConfig() {
        // 这里可以加载已保存的配置
        // 暂时设置为默认值
        if (libraryCheckBoxes != null && !libraryCheckBoxes.isEmpty()) {
            for (JCheckBox checkBox : libraryCheckBoxes.values()) {
                checkBox.setSelected(true); // 默认启用所有组件库
            }
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
                    System.out.println("没有找到已安装的组件库，将显示示例数据");
                }
            } else {
                System.out.println("当前没有打开的项目");
            }
        } catch (Exception e) {
            // 如果获取失败，记录错误并返回示例数据
            System.err.println("获取已安装组件库失败: " + e.getMessage());
            e.printStackTrace();
        }
        // 如果没有已安装的库或获取失败，返回示例数据
        System.out.println("返回示例组件库数据");
        return getSampleLibraries();
    }
    
    private List<ComponentLibrary> getSampleLibraries() {
        List<ComponentLibrary> libraries = new ArrayList<>();
        
        // 创建示例组件库数据
        ComponentLibrary elementPlus = new ComponentLibrary();
        elementPlus.setName("Element Plus");
        elementPlus.setVersion("2.4.0");
        elementPlus.setDescription("基于 Vue 3 的组件库");
        libraries.add(elementPlus);
        
        ComponentLibrary antDesignVue = new ComponentLibrary();
        antDesignVue.setName("Ant Design Vue");
        antDesignVue.setVersion("4.0.0");
        antDesignVue.setDescription("企业级 UI 设计语言和 React 组件库");
        libraries.add(antDesignVue);
        
        ComponentLibrary vuetify = new ComponentLibrary();
        vuetify.setName("Vuetify");
        vuetify.setVersion("3.4.0");
        vuetify.setDescription("Material Design 组件框架");
        libraries.add(vuetify);
        
        return libraries;
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
        if (libraryCheckBoxes != null) {
            for (JCheckBox checkBox : libraryCheckBoxes.values()) {
                // 这里可以比较当前状态与保存的状态
                // 暂时返回 false
            }
        }
        return false;
    }

    @Override
    public void apply() {
        // 不需要应用更改
    }

    @Override
    public void reset() {
        // 不需要重置
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
}
