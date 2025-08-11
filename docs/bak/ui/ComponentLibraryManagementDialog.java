package bak.ui;

import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组件库管理对话框
 * 
 * 功能说明：
 * - 查看现有的组件库（内置 + 自定义）
 * - 对组件库进行删除、查看、导出等操作
 * - 新增组件库（上传JSON文件）
 * - 导出模板功能
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryManagementDialog extends DialogWrapper {
    
    private final Project project;
    private JList<String> libraryList;
    private DefaultListModel<String> listModel;
    private JTextArea detailArea;
    private JButton viewButton;
    private JButton exportButton;
    private JButton deleteButton;
    private JButton addButton;
    private JButton templateButton;
    
    public ComponentLibraryManagementDialog(Project project) {
        super(project);
        this.project = project;
        setTitle("📚 组件库管理");
        setSize(1000, 700);
        setResizable(true);
        init();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1000, 700));
        
        // 创建左侧组件库列表
        JPanel leftPanel = createLibraryListPanel();
        
        // 创建右侧详情面板
        JPanel rightPanel = createDetailPanel();
        
        // 创建底部按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 组装主面板
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载组件库列表
        loadLibraryList();
        
        return mainPanel;
    }
    
    /**
     * 创建组件库列表面板
     */
    private JPanel createLibraryListPanel() {
        listModel = new DefaultListModel<>();
        libraryList = new JList<>(listModel);
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryList.setPreferredSize(new Dimension(300, 500));
        
        JScrollPane scrollPane = new JScrollPane(libraryList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("组件库列表"));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(320, 600));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建详情面板
     */
    private JPanel createDetailPanel() {
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("组件库详情"));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        viewButton = new JButton("👁️ 查看详情");
        exportButton = new JButton("📤 导出组件库");
        deleteButton = new JButton("🗑️ 删除组件库");
        addButton = new JButton("➕ 新增组件库");
        templateButton = new JButton("📋 导出模板");
        
        // 设置按钮事件
        viewButton.addActionListener(e -> viewLibraryDetails());
        exportButton.addActionListener(e -> exportLibrary());
        deleteButton.addActionListener(e -> deleteLibrary());
        addButton.addActionListener(e -> addNewLibrary());
        templateButton.addActionListener(e -> exportTemplate());
        
        // 初始状态下禁用操作按钮
        viewButton.setEnabled(false);
        exportButton.setEnabled(false);
        deleteButton.setEnabled(false);
        
        // 监听列表选择变化
        libraryList.addListSelectionListener(e -> {
            boolean hasSelection = !libraryList.isSelectionEmpty();
            viewButton.setEnabled(hasSelection);
            exportButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
            
            if (hasSelection) {
                viewLibraryDetails();
            }
        });
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.add(viewButton);
        panel.add(exportButton);
        panel.add(deleteButton);
        panel.add(addButton);
        panel.add(templateButton);
        
        return panel;
    }
    
    /**
     * 加载组件库列表
     */
    private void loadLibraryList() {
        listModel.clear();
        
        // 添加内置组件库
        listModel.addElement("🏷️ Element Plus (内置)");
        listModel.addElement("🏷️ Element UI (内置)");
        listModel.addElement("🏷️ Ant Design Vue (内置)");
        
        // 添加自定义组件库
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
            CustomComponentLibraryManager.getAllCustomLibraries();
        
        for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
            listModel.addElement("📦 " + config.getDisplayName() + " (自定义)");
        }
        
        if (listModel.size() > 0) {
            libraryList.setSelectedIndex(0);
        }
    }
    
    /**
     * 查看组件库详情
     */
    private void viewLibraryDetails() {
        int selectedIndex = libraryList.getSelectedIndex();
        if (selectedIndex == -1) {
            detailArea.setText("请选择一个组件库");
            return;
        }
        
        String selectedItem = listModel.getElementAt(selectedIndex);
        StringBuilder details = new StringBuilder();
        
        if (selectedItem.contains("Element Plus")) {
            showBuiltinLibraryDetails("Element Plus", details);
        } else if (selectedItem.contains("Element UI")) {
            showBuiltinLibraryDetails("Element UI", details);
        } else if (selectedItem.contains("Ant Design Vue")) {
            showBuiltinLibraryDetails("Ant Design Vue", details);
        } else {
            // 自定义组件库
            showCustomLibraryDetails(selectedIndex - 3, details); // 减去3个内置组件库
        }
        
        detailArea.setText(details.toString());
    }
    
    /**
     * 显示内置组件库详情
     */
    private void showBuiltinLibraryDetails(String libraryName, StringBuilder details) {
        ComponentProvider componentProvider = new ComponentProvider(project);
        List<ElementPlusComponent> components = componentProvider.searchComponents("");
        
        // 过滤出当前组件库的组件
        List<ElementPlusComponent> libraryComponents = new java.util.ArrayList<>();
        for (ElementPlusComponent component : components) {
            String componentLibrary = componentProvider.getComponentLibraryDisplayName(component.getName());
            if (libraryName.equals(componentLibrary)) {
                libraryComponents.add(component);
            }
        }
        
        details.append("📚 ").append(libraryName).append(" 组件库\n");
        details.append("=".repeat(50)).append("\n\n");
        details.append("📋 基本信息：\n");
        details.append("• 类型：内置组件库\n");
        details.append("• 组件数量：").append(libraryComponents.size()).append(" 个\n");
        details.append("• 前缀：").append(getComponentPrefix(libraryName)).append("\n");
        details.append("• 状态：已启用\n\n");
        
        details.append("🔧 组件列表：\n");
        details.append("-".repeat(50)).append("\n");
        
        for (int i = 0; i < Math.min(libraryComponents.size(), 20); i++) {
            ElementPlusComponent component = libraryComponents.get(i);
            details.append(String.format("%2d. %-20s - %s\n", 
                i + 1, 
                component.getName(), 
                component.getDescription() != null ? component.getDescription() : "无描述"
            ));
        }
        
        if (libraryComponents.size() > 20) {
            details.append("... 还有 ").append(libraryComponents.size() - 20).append(" 个组件\n");
        }
        
        details.append("\n💡 说明：内置组件库不可删除，但可以导出。");
    }
    
    /**
     * 显示自定义组件库详情
     */
    private void showCustomLibraryDetails(int customIndex, StringBuilder details) {
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
            CustomComponentLibraryManager.getAllCustomLibraries();
        
        if (customIndex >= 0 && customIndex < customLibraries.size()) {
            CustomComponentLibraryManager.CustomLibraryConfig config = customLibraries.get(customIndex);
            
            details.append("📚 ").append(config.getDisplayName()).append(" 组件库\n");
            details.append("=".repeat(50)).append("\n\n");
            details.append("📋 基本信息：\n");
            details.append("• 类型：自定义组件库\n");
            details.append("• 名称：").append(config.getName()).append("\n");
            details.append("• 版本：").append(config.getVersion()).append("\n");
            details.append("• 组件数量：").append(config.getComponents().size()).append(" 个\n");
            details.append("• 前缀：").append(config.getComponentPrefix()).append("\n");
            if (config.getDescription() != null && !config.getDescription().isEmpty()) {
                details.append("• 描述：").append(config.getDescription()).append("\n");
            }
            details.append("• 状态：已加载\n\n");
            
            details.append("🔧 组件列表：\n");
            details.append("-".repeat(50)).append("\n");
            
            for (int i = 0; i < config.getComponents().size(); i++) {
                ElementPlusComponent component = config.getComponents().get(i);
                details.append(String.format("%2d. %-20s - %s\n", 
                    i + 1, 
                    component.getName(), 
                    component.getDescription() != null ? component.getDescription() : "无描述"
                ));
            }
            
            details.append("\n💡 说明：自定义组件库可以删除和导出。");
        }
    }
    
    /**
     * 导出组件库
     */
    private void exportLibrary() {
        int selectedIndex = libraryList.getSelectedIndex();
        if (selectedIndex == -1) {
            Messages.showInfoMessage("请选择一个组件库", "提示");
            return;
        }
        
        String selectedItem = listModel.getElementAt(selectedIndex);
        String libraryName = selectedItem.replaceAll("^[📦🏷️]\\s*", "").replaceAll("\\s*\\(.*\\)$", "");
        
        // 选择保存位置
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出组件库");
        fileChooser.setSelectedFile(new File(libraryName + "-components.json"));
        
        if (fileChooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (selectedItem.contains("(内置)")) {
                    exportBuiltinLibrary(libraryName, file);
                } else {
                    exportCustomLibrary(selectedIndex - 3, file);
                }
                
                Messages.showInfoMessage(
                    "组件库 \"" + libraryName + "\" 已成功导出到：\n" + file.getAbsolutePath(),
                    "导出成功"
                );
            } catch (Exception ex) {
                Messages.showErrorDialog(
                    "导出失败：" + ex.getMessage(),
                    "导出错误"
                );
            }
        }
    }
    
    /**
     * 导出内置组件库
     */
    private void exportBuiltinLibrary(String libraryName, File file) throws IOException {
        ComponentProvider componentProvider = new ComponentProvider(project);
        List<ElementPlusComponent> allComponents = componentProvider.searchComponents("");
        
        // 过滤出当前组件库的组件
        List<ElementPlusComponent> libraryComponents = new java.util.ArrayList<>();
        for (ElementPlusComponent component : allComponents) {
            String componentLibrary = componentProvider.getComponentLibraryDisplayName(component.getName());
            if (libraryName.equals(componentLibrary)) {
                libraryComponents.add(component);
            }
        }
        
        // 构建导出数据
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("name", libraryName.toLowerCase().replace(" ", "-"));
        exportData.put("displayName", libraryName);
        exportData.put("version", "1.0.0");
        exportData.put("description", libraryName + " 组件库导出");
        exportData.put("componentPrefix", getComponentPrefix(libraryName));
        exportData.put("components", libraryComponents);
        
        // 导出为JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(exportData, writer);
        }
    }
    
    /**
     * 导出自定义组件库
     */
    private void exportCustomLibrary(int customIndex, File file) throws IOException {
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
            CustomComponentLibraryManager.getAllCustomLibraries();
        
        if (customIndex >= 0 && customIndex < customLibraries.size()) {
            CustomComponentLibraryManager.CustomLibraryConfig config = customLibraries.get(customIndex);
            
            // 构建导出数据
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("name", config.getName());
            exportData.put("displayName", config.getDisplayName());
            exportData.put("version", config.getVersion());
            exportData.put("description", config.getDescription());
            exportData.put("componentPrefix", config.getComponentPrefix());
            exportData.put("components", config.getComponents());
            
            // 导出为JSON
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(exportData, writer);
            }
        }
    }
    
    /**
     * 删除组件库
     */
    private void deleteLibrary() {
        int selectedIndex = libraryList.getSelectedIndex();
        if (selectedIndex == -1) {
            Messages.showInfoMessage("请选择一个组件库", "提示");
            return;
        }
        
        String selectedItem = listModel.getElementAt(selectedIndex);
        
        // 内置组件库不可删除
        if (selectedItem.contains("(内置)")) {
            Messages.showInfoMessage("内置组件库不可删除", "提示");
            return;
        }
        
        // 确认删除
        int confirm = Messages.showYesNoDialog(
            "确定要删除组件库 \"" + selectedItem.replaceAll("^[📦🏷️]\\s*", "").replaceAll("\\s*\\(.*\\)$", "") + "\" 吗？\n" +
            "删除后将无法恢复。",
            "确认删除",
            Messages.getQuestionIcon()
        );
        
        if (confirm == Messages.YES) {
            int customIndex = selectedIndex - 3; // 减去3个内置组件库
            List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
                CustomComponentLibraryManager.getAllCustomLibraries();
            
            if (customIndex >= 0 && customIndex < customLibraries.size()) {
                CustomComponentLibraryManager.CustomLibraryConfig config = customLibraries.get(customIndex);
                boolean success = CustomComponentLibraryManager.removeCustomLibrary(config.getName());
                
                if (success) {
                    Messages.showInfoMessage(
                        "组件库 \"" + config.getDisplayName() + "\" 已成功删除。",
                        "删除成功"
                    );
                    loadLibraryList(); // 重新加载列表
                } else {
                    Messages.showErrorDialog(
                        "删除组件库失败。",
                        "删除失败"
                    );
                }
            }
        }
    }
    
    /**
     * 新增组件库
     */
    private void addNewLibrary() {
        com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog dialog = new CustomLibraryUploadDialog(project);
        dialog.show();
        
        // 如果成功添加，重新加载列表
        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            loadLibraryList();
        }
    }
    
    /**
     * 导出模板
     */
    private void exportTemplate() {
        // 选择保存位置
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出组件库模板");
        fileChooser.setSelectedFile(new File("component-library-template.json"));
        
        if (fileChooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // 创建模板数据
                Map<String, Object> template = createTemplateData();
                
                // 导出为JSON
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(template, writer);
                }
                
                Messages.showInfoMessage(
                    "组件库模板已成功导出到：\n" + file.getAbsolutePath() + "\n\n" +
                    "您可以参考此模板创建自己的组件库。",
                    "模板导出成功"
                );
            } catch (IOException ex) {
                Messages.showErrorDialog(
                    "模板导出失败：" + ex.getMessage(),
                    "导出错误"
                );
            }
        }
    }
    
    /**
     * 创建模板数据
     */
    private Map<String, Object> createTemplateData() {
        Map<String, Object> template = new HashMap<>();
        template.put("name", "my-component-library");
        template.put("displayName", "我的组件库");
        template.put("version", "1.0.0");
        template.put("description", "这是一个自定义组件库的示例");
        template.put("componentPrefix", "my-");
        
        // 示例组件
        Map<String, Object> exampleComponent = new HashMap<>();
        exampleComponent.put("name", "my-button");
        exampleComponent.put("description", "自定义按钮组件");
        exampleComponent.put("version", "1.0.0");
        exampleComponent.put("example", "<my-button type=\"primary\">按钮</my-button>");
        exampleComponent.put("docUrl", "https://example.com/my-button");
        
        // 示例属性
        List<Map<String, Object>> props = new java.util.ArrayList<>();
        Map<String, Object> prop1 = new HashMap<>();
        prop1.put("name", "type");
        prop1.put("type", "string");
        prop1.put("description", "按钮类型");
        prop1.put("defaultValue", "default");
        prop1.put("required", false);
        prop1.put("options", java.util.Arrays.asList("primary", "success", "warning", "danger", "info", "default"));
        props.add(prop1);
        exampleComponent.put("props", props);
        
        // 示例事件
        List<Map<String, Object>> events = new java.util.ArrayList<>();
        Map<String, Object> event1 = new HashMap<>();
        event1.put("name", "click");
        event1.put("description", "点击事件");
        event1.put("parameters", "event");
        events.add(event1);
        exampleComponent.put("events", events);
        
        // 示例插槽
        List<Map<String, Object>> slots = new java.util.ArrayList<>();
        Map<String, Object> slot1 = new HashMap<>();
        slot1.put("name", "default");
        slot1.put("description", "按钮内容");
        slots.add(slot1);
        exampleComponent.put("slots", slots);
        
        template.put("components", java.util.Arrays.asList(exampleComponent));
        
        return template;
    }
    
    /**
     * 获取组件前缀
     */
    private String getComponentPrefix(String libraryName) {
        switch (libraryName.toLowerCase()) {
            case "element plus":
                return "el-";
            case "element ui":
                return "el-";
            case "ant design vue":
                return "a-";
            default:
                return "my-";
        }
    }
}
