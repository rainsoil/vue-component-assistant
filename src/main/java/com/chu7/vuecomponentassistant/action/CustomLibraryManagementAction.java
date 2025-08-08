package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 自定义组件库管理动作
 * 
 * 功能说明：
 * - 首先展示所有组件（内置 + 自定义）
 * - 提供组件库上传功能
 * - 提供组件库导出功能
 * - 提供组件库删除功能（内置组件库不可删除）
 * - 提供导出模板功能
 * 
 * @author Vue Component Assistant Team
 * @version 1.0.0
 */
public class CustomLibraryManagementAction extends AnAction {
    
    public CustomLibraryManagementAction() {
        super("自定义组件库管理", "管理所有组件库和组件", null);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        // 首先展示所有组件
        showAllComponents(project);
        
        // 然后显示管理选项
        showManagementOptions(project);
    }
    
    /**
     * 展示所有组件
     */
    private void showAllComponents(Project project) {
        ComponentProvider componentProvider = new ComponentProvider(project);
        List<ElementPlusComponent> allComponents = componentProvider.searchComponents("");
        
        if (allComponents.isEmpty()) {
            Messages.showInfoMessage("当前没有找到任何组件。", "组件信息");
            return;
        }
        
        // 按组件库分组
        Map<String, List<ElementPlusComponent>> componentsByLibrary = new HashMap<>();
        for (ElementPlusComponent component : allComponents) {
            String libraryName = componentProvider.getComponentLibraryDisplayName(component.getName());
            componentsByLibrary.computeIfAbsent(libraryName, k -> new java.util.ArrayList<>()).add(component);
        }
        
        StringBuilder info = new StringBuilder();
        info.append("📚 所有可用组件：\n\n");
        
        for (Map.Entry<String, List<ElementPlusComponent>> entry : componentsByLibrary.entrySet()) {
            String libraryName = entry.getKey();
            List<ElementPlusComponent> components = entry.getValue();
            
            info.append("🏷️ ").append(libraryName).append(" (").append(components.size()).append(" 个组件)\n");
            info.append("─".repeat(50)).append("\n");
            
            for (int i = 0; i < components.size(); i++) {
                ElementPlusComponent component = components.get(i);
                info.append(String.format("%2d. %-20s - %s\n", 
                    i + 1, 
                    component.getName(), 
                    component.getDescription() != null ? component.getDescription() : "无描述"
                ));
            }
            info.append("\n");
        }
        
        // 显示组件信息对话框
        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        
        JOptionPane.showMessageDialog(
            null,
            scrollPane,
            "📚 所有组件信息",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * 显示管理选项
     */
    private void showManagementOptions(Project project) {
        String[] options = {
            "📦 上传新组件库",
            "📤 导出组件库",
            "🗑️ 删除自定义组件库",
            "📋 导出组件库模板",
            "❌ 取消"
        };
        
        int choice = Messages.showChooseDialog(
            project,
            "请选择要执行的操作：",
            "组件库管理",
            Messages.getQuestionIcon(),
            options,
            options[0]
        );
        
        switch (choice) {
            case 0: // 上传新组件库
                uploadNewLibrary(project);
                break;
            case 1: // 导出组件库
                exportLibrary(project);
                break;
            case 2: // 删除自定义组件库
                deleteCustomLibrary(project);
                break;
            case 3: // 导出模板
                exportTemplate(project);
                break;
            case 4: // 取消
            default:
                break;
        }
    }
    
    /**
     * 上传新组件库
     */
    private void uploadNewLibrary(Project project) {
        CustomLibraryUploadDialog dialog = new CustomLibraryUploadDialog(project);
        dialog.show();
    }
    
    /**
     * 导出组件库
     */
    private void exportLibrary(Project project) {
        // 获取所有组件库（内置 + 自定义）
        ComponentProvider componentProvider = new ComponentProvider(project);
        List<ElementPlusComponent> allComponents = componentProvider.searchComponents("");
        
        if (allComponents.isEmpty()) {
            Messages.showInfoMessage("当前没有组件可以导出。", "导出组件库");
            return;
        }
        
        // 按组件库分组
        Map<String, List<ElementPlusComponent>> componentsByLibrary = new HashMap<>();
        for (ElementPlusComponent component : allComponents) {
            String libraryName = componentProvider.getComponentLibraryDisplayName(component.getName());
            componentsByLibrary.computeIfAbsent(libraryName, k -> new java.util.ArrayList<>()).add(component);
        }
        
        // 选择要导出的组件库
        String[] libraryNames = componentsByLibrary.keySet().toArray(new String[0]);
        int choice = Messages.showChooseDialog(
            project,
            "请选择要导出的组件库：",
            "导出组件库",
            Messages.getQuestionIcon(),
            libraryNames,
            libraryNames[0]
        );
        
        if (choice >= 0 && choice < libraryNames.length) {
            String selectedLibrary = libraryNames[choice];
            List<ElementPlusComponent> components = componentsByLibrary.get(selectedLibrary);
            
            // 选择保存位置
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择保存位置");
            fileChooser.setSelectedFile(new File(selectedLibrary + "-components.json"));
            
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    // 构建导出数据
                    Map<String, Object> exportData = new HashMap<>();
                    exportData.put("name", selectedLibrary.toLowerCase().replace(" ", "-"));
                    exportData.put("displayName", selectedLibrary);
                    exportData.put("version", "1.0.0");
                    exportData.put("description", selectedLibrary + " 组件库导出");
                    exportData.put("componentPrefix", getComponentPrefix(selectedLibrary));
                    exportData.put("components", components);
                    
                    // 导出为JSON
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try (FileWriter writer = new FileWriter(file)) {
                        gson.toJson(exportData, writer);
                    }
                    
                    Messages.showInfoMessage(
                        "组件库 \"" + selectedLibrary + "\" 已成功导出到：\n" + file.getAbsolutePath(),
                        "导出成功"
                    );
                } catch (IOException ex) {
                    Messages.showErrorDialog(
                        "导出失败：" + ex.getMessage(),
                        "导出错误"
                    );
                }
            }
        }
    }
    
    /**
     * 删除自定义组件库
     */
    private void deleteCustomLibrary(Project project) {
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries = 
            CustomComponentLibraryManager.getAllCustomLibraries();
        
        if (customLibraries.isEmpty()) {
            Messages.showInfoMessage(
                "当前没有加载任何自定义组件库。\n内置组件库（Element Plus、Element UI、Ant Design Vue）不可删除。",
                "删除组件库"
            );
            return;
        }
        
        // 构建选项列表（只包含自定义组件库）
        String[] options = new String[customLibraries.size() + 1];
        for (int i = 0; i < customLibraries.size(); i++) {
            CustomComponentLibraryManager.CustomLibraryConfig config = customLibraries.get(i);
            options[i] = config.getDisplayName() + " (" + config.getName() + ")";
        }
        options[customLibraries.size()] = "取消";
        
        int choice = Messages.showChooseDialog(
            project,
            "请选择要删除的自定义组件库：\n（内置组件库不可删除）",
            "删除自定义组件库",
            Messages.getQuestionIcon(),
            options,
            options[0]
        );
        
        if (choice >= 0 && choice < customLibraries.size()) {
            CustomComponentLibraryManager.CustomLibraryConfig config = customLibraries.get(choice);
            
            int confirm = Messages.showYesNoDialog(
                project,
                "确定要删除自定义组件库 \"" + config.getDisplayName() + "\" 吗？\n" +
                "删除后将无法恢复。",
                "确认删除",
                Messages.getQuestionIcon()
            );
            
            if (confirm == Messages.YES) {
                boolean success = CustomComponentLibraryManager.removeCustomLibrary(config.getName());
                if (success) {
                    Messages.showInfoMessage(
                        "自定义组件库 \"" + config.getDisplayName() + "\" 已成功删除。",
                        "删除成功"
                    );
                } else {
                    Messages.showErrorDialog(
                        "删除自定义组件库失败。",
                        "删除失败"
                    );
                }
            }
        }
    }
    
    /**
     * 导出组件库模板
     */
    private void exportTemplate(Project project) {
        // 选择保存位置
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择模板保存位置");
        fileChooser.setSelectedFile(new File("component-library-template.json"));
        
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
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
        java.util.List<Map<String, Object>> props = new java.util.ArrayList<>();
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
        java.util.List<Map<String, Object>> events = new java.util.ArrayList<>();
        Map<String, Object> event1 = new HashMap<>();
        event1.put("name", "click");
        event1.put("description", "点击事件");
        event1.put("parameters", "event");
        events.add(event1);
        exampleComponent.put("events", events);
        
        // 示例插槽
        java.util.List<Map<String, Object>> slots = new java.util.ArrayList<>();
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
    
    @Override
    public void update(AnActionEvent e) {
        // 只有在有项目时才启用此动作
        Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
