package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
 * @author VueKit Team
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
        ComponentLibraryManager libraryManager = new ComponentLibraryManager();
        List<ComponentInfo> allComponents = new ArrayList<>();

        try {
            // 获取所有组件库中的组件
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            for (ComponentLibrary library : libraries) {
                if (library.getComponents() != null) {
                    allComponents.addAll(library.getComponents());
                }
            }
        } catch (Exception e) {
            Messages.showErrorDialog("获取组件信息失败: " + e.getMessage(), "错误");
            return;
        }

        if (allComponents.isEmpty()) {
            Messages.showInfoMessage("当前没有找到任何组件。", "组件信息");
            return;
        }

        // 按组件库分组
        Map<String, List<ComponentInfo>> componentsByLibrary = new HashMap<>();
        for (ComponentInfo component : allComponents) {
            // 这里需要根据组件信息确定所属的组件库
            String libraryName = "未知组件库";
            componentsByLibrary.computeIfAbsent(libraryName, k -> new java.util.ArrayList<>()).add(component);
        }

        StringBuilder info = new StringBuilder();
        info.append("📚 所有可用组件：\n\n");

        for (Map.Entry<String, List<ComponentInfo>> entry : componentsByLibrary.entrySet()) {
            String libraryName = entry.getKey();
            List<ComponentInfo> components = entry.getValue();

            info.append("🏷️ ").append(libraryName).append(" (").append(components.size()).append(" 个组件)\n");
            info.append("─".repeat(50)).append("\n");

            for (int i = 0; i < components.size(); i++) {
                ComponentInfo component = components.get(i);
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
                "❌ 取消"
        };

        int choice = Messages.showDialog(
                project,
                "请选择要执行的操作：",
                "组件库管理",
                options,
                0,
                Messages.getQuestionIcon()
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
            case 3: // 取消
            default:
                break;
        }
    }

    /**
     * 上传新组件库
     */
    private void uploadNewLibrary(Project project) {
        ComponentLibraryManager libraryManager = new ComponentLibraryManager();
        CustomLibraryUploadDialog dialog = new CustomLibraryUploadDialog(project, libraryManager);
        dialog.show();
    }

    /**
     * 导出组件库
     */
    private void exportLibrary(Project project) {
        // 获取所有组件库
        ComponentLibraryManager libraryManager = new ComponentLibraryManager();
        List<ComponentInfo> allComponents = new ArrayList<>();

        try {
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            for (ComponentLibrary library : libraries) {
                if (library.getComponents() != null) {
                    allComponents.addAll(library.getComponents());
                }
            }
        } catch (Exception e) {
            Messages.showErrorDialog("获取组件信息失败: " + e.getMessage(), "错误");
            return;
        }

        if (allComponents.isEmpty()) {
            Messages.showInfoMessage("当前没有组件可以导出。", "导出组件库");
            return;
        }

        // 按组件库分组
        Map<String, List<ComponentInfo>> componentsByLibrary = new HashMap<>();
        for (ComponentInfo component : allComponents) {
            String libraryName = "未知组件库";
            componentsByLibrary.computeIfAbsent(libraryName, k -> new java.util.ArrayList<>()).add(component);
        }

        // 选择要导出的组件库
        String[] libraryNames = componentsByLibrary.keySet().toArray(new String[0]);
        int choice = Messages.showDialog(
                project,
                "请选择要导出的组件库：",
                "导出组件库",
                libraryNames,
                0,
                Messages.getQuestionIcon()
        );

        if (choice >= 0 && choice < libraryNames.length) {
            String selectedLibrary = libraryNames[choice];
            List<ComponentInfo> components = componentsByLibrary.get(selectedLibrary);

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
                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
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
                    "当前没有加载任何自定义组件库。\n远程组件库和官方组件库不可删除。",
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

        int choice = Messages.showDialog(
                project,
                "请选择要删除的自定义组件库：\n（内置组件库不可删除）",
                "删除自定义组件库",
                options,
                0,
                Messages.getQuestionIcon()
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
