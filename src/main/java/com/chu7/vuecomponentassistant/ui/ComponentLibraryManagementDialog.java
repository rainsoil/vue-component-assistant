package com.chu7.vuecomponentassistant.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ImportResult;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryTemplateGenerator;
import com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 组件库管理对话框 - 远程组件库版本（简化版）
 * 
 * 功能说明：
 * - 查看已安装的组件库（官方 + 自定义本地 + 自定义远程）
 * - 对组件库进行删除、查看、导出等操作
 * - 导入自定义组件库（本地JSON文件和远程URL）
 * - 重新加载远程组件库
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class ComponentLibraryManagementDialog extends DialogWrapper {
    
    private final Project project;
    private final ComponentLibraryManager libraryManager;
    private JList<ComponentLibrary> libraryList;
    private DefaultListModel<ComponentLibrary> listModel;
    private JTextArea detailArea;
    private JButton viewButton;
    private JButton exportButton;
    private JButton deleteButton;
    private JButton reloadButton;
    private JButton importCustomButton;
    private JButton officialMarketButton;
    private JButton exportTemplateButton;
    private JButton refreshButton;
    private JLabel statsLabel;
    
    public ComponentLibraryManagementDialog(Project project) {
        super(project);
        this.project = project;
        this.libraryManager = new ComponentLibraryManager();
        setTitle("📚 组件库管理 - VueKit");
        setSize(1200, 800);
        setResizable(true);
        init();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1200, 800));
        
        // 创建顶部工具栏
        JPanel toolbarPanel = createToolbarPanel();
        
        // 创建左侧组件库列表
        JPanel leftPanel = createLibraryListPanel();
        
        // 创建右侧详情面板
        JPanel rightPanel = createDetailPanel();
        
        // 创建底部按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 组装主面板
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载组件库列表
        loadLibraryList();
        
        return mainPanel;
    }
    
    /**
     * 创建顶部工具栏面板
     */
    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 导入自定义组件库按钮
        importCustomButton = new JButton("📁 导入自定义库");
        importCustomButton.addActionListener(e -> importCustomLibrary());
        
        // 官方组件库市场按钮
        officialMarketButton = new JButton("🌐 官网组件库");
        officialMarketButton.addActionListener(e -> openOfficialMarket());
        
        // 导出模板按钮
        exportTemplateButton = new JButton("📋 导出模板");
        exportTemplateButton.addActionListener(e -> exportTemplate());
        
        // 刷新按钮
        refreshButton = new JButton("🔄 刷新");
        refreshButton.addActionListener(e -> refreshLibraryList());
        
        // 统计信息标签
        statsLabel = new JLabel("正在加载...");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        panel.add(importCustomButton);
        panel.add(officialMarketButton);
        panel.add(exportTemplateButton);
        panel.add(refreshButton);
        panel.add(statsLabel);
        
        return panel;
    }
    
    /**
     * 创建组件库列表面板
     */
    private JPanel createLibraryListPanel() {
        listModel = new DefaultListModel<>();
        libraryList = new JList<>(listModel);
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryList.setCellRenderer(new ComponentLibraryListCellRenderer());
        libraryList.setPreferredSize(new Dimension(400, 600));
        
        // 添加选择监听器
        libraryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
                viewLibraryDetails();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(libraryList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("已安装的组件库"));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(420, 700));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建详情面板
     */
    private JPanel createDetailPanel() {
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("组件库详情"));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建底部按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 查看详情按钮
        viewButton = new JButton("👁️ 查看详情");
        viewButton.addActionListener(e -> viewLibraryDetails());
        
        // 重新加载按钮（仅对远程组件库显示）
        reloadButton = new JButton("🔄 重新加载");
        reloadButton.addActionListener(e -> reloadLibrary());
        
        // 导出按钮
        exportButton = new JButton("📤 导出");
        exportButton.addActionListener(e -> exportLibrary());
        
        // 删除按钮
        deleteButton = new JButton("🗑️ 删除");
        deleteButton.addActionListener(e -> deleteLibrary());
        
        panel.add(viewButton);
        panel.add(reloadButton);
        panel.add(exportButton);
        panel.add(deleteButton);
        
        return panel;
    }
    
    /**
     * 加载组件库列表
     */
    private void loadLibraryList() {
        listModel.clear();
        
        try {
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            for (ComponentLibrary library : libraries) {
                listModel.addElement(library);
            }
            
            updateStatsLabel();
            
        } catch (Exception e) {
            Messages.showErrorDialog("加载组件库列表失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 刷新组件库列表
     */
    private void refreshLibraryList() {
        loadLibraryList();
        Messages.showInfoMessage("组件库列表已刷新", "刷新完成");
    }
    
    /**
     * 更新统计信息标签
     */
    private void updateStatsLabel() {
        try {
            Map<String, Object> stats = libraryManager.getLibraryStats();
            String statsText = String.format("📊 统计: %d个组件库, %d个组件", 
                stats.get("totalLibraries"), stats.get("totalComponents"));
            statsLabel.setText(statsText);
        } catch (Exception e) {
            statsLabel.setText("📊 统计: 加载失败");
        }
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        ComponentLibrary selectedLibrary = libraryList.getSelectedValue();
        boolean hasSelection = selectedLibrary != null;
        
        viewButton.setEnabled(hasSelection);
        exportButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        
        // 重新加载按钮仅对远程自定义组件库启用
        reloadButton.setEnabled(hasSelection && 
                            "CUSTOM_REMOTE".equals(selectedLibrary.getSource()));
    }
    
    /**
     * 查看组件库详情
     */
    private void viewLibraryDetails() {
        ComponentLibrary library = libraryList.getSelectedValue();
        if (library == null) {
            detailArea.setText("请选择一个组件库查看详情");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("组件库详情\n");
        details.append("==========\n\n");
        details.append("名称: ").append(library.getDisplayName()).append("\n");
        details.append("ID: ").append(library.getId()).append("\n");
        details.append("版本: ").append(library.getVersion()).append("\n");
        details.append("来源: ").append(library.getSourceAsEnum() != null ? library.getSourceAsEnum().getDisplayName() : library.getSource()).append("\n");
        details.append("描述: ").append(library.getDescription()).append("\n");
        details.append("最后更新: ").append(library.getLastUpdated()).append("\n");
        
        if (library.getSourceUrl() != null) {
            details.append("源URL: ").append(library.getSourceUrl()).append("\n");
        }
        
        if (library.getComponents() != null) {
            details.append("\n组件列表 (").append(library.getComponents().size()).append("个):\n");
            details.append("----------\n");
            for (int i = 0; i < Math.min(library.getComponents().size(), 10); i++) {
                details.append(i + 1).append(". ").append(library.getComponents().get(i).getName()).append("\n");
            }
            if (library.getComponents().size() > 10) {
                details.append("... 还有 ").append(library.getComponents().size() - 10).append(" 个组件\n");
            }
        }
        
        detailArea.setText(details.toString());
    }
    
    /**
     * 导入自定义组件库
     */
    private void importCustomLibrary() {
        CustomLibraryUploadDialog dialog = new CustomLibraryUploadDialog(project, libraryManager);
        dialog.show();
        
        // 刷新列表
        loadLibraryList();
    }
    
    /**
     * 打开官方组件库市场
     */
    private void openOfficialMarket() {
        OfficialLibraryMarketDialog dialog = new OfficialLibraryMarketDialog(project, libraryManager);
        dialog.show();
        // 刷新组件库列表（可能下载了新的组件库）
        loadLibraryList();
    }
    
    /**
     * 重新加载组件库
     */
    private void reloadLibrary() {
        ComponentLibrary library = libraryList.getSelectedValue();
        if (library == null) {
            return;
        }
        
                    if (!"CUSTOM_REMOTE".equals(library.getSource())) {
            Messages.showWarningDialog("只能重新加载远程自定义组件库", "操作限制");
            return;
        }
        
        int result = Messages.showYesNoDialog(
            "确定要重新加载组件库 '" + library.getName() + "' 吗？\n" +
            "这将从原始URL重新下载最新版本。",
            "重新加载组件库",
            Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            try {
                ImportResult importResult = libraryManager.reloadRemoteLibrary(library.getId());
                if (importResult.isSuccess()) {
                    Messages.showInfoMessage("组件库重新加载成功: " + library.getName(), "成功");
                    loadLibraryList();
                } else {
                    Messages.showErrorDialog("重新加载失败: " + importResult.getMessage(), "错误");
                }
            } catch (Exception e) {
                Messages.showErrorDialog("重新加载失败: " + e.getMessage(), "错误");
            }
        }
    }
    
    /**
     * 导出组件库
     */
    private void exportLibrary() {
        ComponentLibrary library = libraryList.getSelectedValue();
        if (library == null) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出组件库");
        fileChooser.setSelectedFile(new File(library.getName() + ".json"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON文件", "json"));
        
        if (fileChooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(library);
                
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    writer.write(json);
                }
                
                Messages.showInfoMessage("组件库导出成功: " + file.getName(), "成功");
                
            } catch (IOException e) {
                Messages.showErrorDialog("导出失败: " + e.getMessage(), "错误");
            }
        }
    }
    
    /**
     * 导出组件库模板
     */
    private void exportTemplate() {
        // 选择保存位置
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出组件库JSON模板");
        fileChooser.setSelectedFile(new File("component-library-template.json"));
        
        if (fileChooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // 使用模板生成器导出模板
                ComponentLibraryTemplateGenerator.generateAndExportTemplate(file);
                
                // 显示成功消息和详细说明
                StringBuilder message = new StringBuilder();
                message.append("✅ 组件库JSON模板已成功导出！\n\n");
                message.append("📁 保存位置：\n");
                message.append(file.getAbsolutePath()).append("\n\n");
                message.append("📋 模板包含：\n");
                message.append("• 完整的组件库结构定义\n");
                message.append("• 示例按钮组件（包含属性、事件、插槽）\n");
                message.append("• 示例输入框组件（包含属性、事件、插槽）\n");
                message.append("• 示例卡片组件（包含属性、事件、插槽）\n");
                message.append("• 示例模态框组件（包含属性、事件、插槽）\n");
                message.append("• 符合VueKit远程组件库规范\n\n");
                message.append("💡 使用说明：\n");
                message.append("1. 修改模板中的组件库信息\n");
                message.append("2. 添加或修改组件定义\n");
                message.append("3. 保存为JSON文件\n");
                message.append("4. 通过\"导入自定义库\"功能导入\n\n");
                message.append("🔗 支持本地文件和远程URL两种导入方式");
                
                Messages.showInfoMessage(message.toString(), "模板导出成功");
            } catch (IOException ex) {
                Messages.showErrorDialog(
                    "❌ 模板导出失败：\n" + ex.getMessage(),
                    "导出错误"
                );
            }
        }
    }
    
    /**
     * 删除组件库
     */
    private void deleteLibrary() {
        ComponentLibrary library = libraryList.getSelectedValue();
        if (library == null) {
            return;
        }
        
        int result = Messages.showYesNoDialog(
            "确定要删除组件库 '" + library.getName() + "' 吗？\n" +
            "此操作不可撤销。",
            "删除组件库",
            Messages.getWarningIcon()
        );
        
        if (result == Messages.YES) {
            try {
                boolean removed = libraryManager.removeLibraryById(library.getId());
                if (removed) {
                    Messages.showInfoMessage("组件库删除成功: " + library.getName(), "成功");
                    loadLibraryList();
                } else {
                    Messages.showErrorDialog("删除失败", "错误");
                }
            } catch (Exception e) {
                Messages.showErrorDialog("删除失败: " + e.getMessage(), "错误");
            }
        }
    }
    
    /**
     * 组件库列表单元格渲染器
     */
    private static class ComponentLibraryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                     boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof ComponentLibrary) {
                ComponentLibrary library = (ComponentLibrary) value;
                String displayText = String.format("%s %s [%s]", 
                    library.getDisplayName(), 
                    library.getVersion(),
                    library.getSourceAsEnum() != null ? library.getSourceAsEnum().getDisplayName() : library.getSource()
                );
                setText(displayText);
                
                // 根据来源设置不同的图标
                String source = library.getSource();
                if ("OFFICIAL".equals(source)) {
                    setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/component.svg")));
                } else if ("CUSTOM_LOCAL".equals(source)) {
                    setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/property.svg")));
                } else if ("CUSTOM_REMOTE".equals(source)) {
                    setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/event.svg")));
                }
            }
            
            return this;
        }
    }
}
