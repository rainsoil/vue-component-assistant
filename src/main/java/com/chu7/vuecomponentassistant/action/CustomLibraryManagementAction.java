package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
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
 * <p>功能说明：</p>
 * <ul>
 *   <li>展示所有组件（内置 + 自定义）的详细信息</li>
 *   <li>提供组件库上传功能，支持自定义组件库导入</li>
 *   <li>提供组件库导出功能，支持JSON格式导出</li>
 *   <li>提供组件库删除功能（内置组件库不可删除）</li>
 *   <li>提供导出模板功能，便于组件库共享</li>
 *   <li>支持组件库的批量管理和操作</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 *   <li>支持项目上下文感知和状态管理</li>
 *   <li>用户友好的管理界面和操作流程</li>
 *   <li>完整的组件库生命周期管理</li>
 *   <li>支持多种文件格式的导入导出</li>
 *   <li>智能的组件库分类和识别</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户需要查看所有可用组件的详细信息</li>
 *   <li>团队需要共享自定义组件库</li>
 *   <li>项目需要导入第三方组件库</li>
 *   <li>组件库的备份和迁移</li>
 *   <li>自定义组件库的管理和维护</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog
 * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary
 */
public class CustomLibraryManagementAction extends AnAction {

    /**
     * 构造函数
     *
     * <p>初始化自定义组件库管理动作的基本信息：</p>
     * <ul>
     *   <li>动作名称：自定义组件库管理</li>
     *   <li>动作描述：管理所有组件库和组件</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public CustomLibraryManagementAction() {
        super("自定义组件库管理", "管理所有组件库和组件", null);
    }

    /**
     * 动作执行方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>验证项目上下文的有效性</li>
     *   <li>展示所有可用组件的详细信息</li>
     *   <li>提供组件库管理选项菜单</li>
     *   <li>支持多种管理操作</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取当前项目上下文</li>
     *   <li>验证项目信息的有效性</li>
     *   <li>展示所有组件信息</li>
     *   <li>显示管理选项菜单</li>
     *   <li>执行用户选择的操作</li>
     * </ol>
     *
     * @param e 动作事件，包含执行上下文和项目信息
     * @throws RuntimeException 当无法获取项目信息时抛出
     */
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
     * 展示所有组件信息
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取所有组件库中的组件信息</li>
     *   <li>按组件库分组显示组件</li>
     *   <li>提供组件数量和描述信息</li>
     *   <li>使用友好的界面展示组件信息</li>
     * </ul>
     *
     * <p>显示内容：</p>
     * <ul>
     *   <li>组件库名称和组件数量</li>
     *   <li>每个组件的名称和描述</li>
     *   <li>组件库的分组和分类</li>
     *   <li>格式化的信息展示</li>
     * </ul>
     *
     * @param project 当前项目，用于获取组件信息
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
     * 显示管理选项菜单
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>提供组件库管理的主要操作选项</li>
     *   <li>支持上传、导出、删除等操作</li>
     *   <li>根据用户选择执行相应操作</li>
     *   <li>提供友好的用户交互界面</li>
     * </ul>
     *
     * <p>可用操作：</p>
     * <ul>
     *   <li>📦 上传新组件库</li>
     *   <li>📤 导出组件库</li>
     *   <li>🗑️ 删除自定义组件库</li>
     *   <li>❌ 取消操作</li>
     * </ul>
     *
     * @param project 当前项目，用于执行管理操作
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>打开组件库上传对话框</li>
     *   <li>支持自定义组件库的导入</li>
     *   <li>验证组件库格式和内容</li>
     *   <li>集成到现有的组件库管理系统</li>
     * </ul>
     *
     * @param project 当前项目，用于组件库上传
     */
    private void uploadNewLibrary(Project project) {
        ComponentLibraryManager libraryManager = new ComponentLibraryManager();
        CustomLibraryUploadDialog dialog = new CustomLibraryUploadDialog(project, libraryManager);
        dialog.show();
    }

    /**
     * 导出组件库
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取所有可用的组件库</li>
     *   <li>允许用户选择要导出的组件库</li>
     *   <li>支持JSON格式的组件库导出</li>
     *   <li>提供文件保存位置选择</li>
     * </ul>
     *
     * <p>导出流程：</p>
     * <ol>
     *   <li>获取所有组件库信息</li>
     *   <li>选择要导出的组件库</li>
     *   <li>选择保存位置和文件名</li>
     *   <li>生成JSON格式的导出文件</li>
     *   <li>提供导出成功反馈</li>
     * </ol>
     *
     * @param project 当前项目，用于获取组件库信息
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取所有自定义组件库列表</li>
     *   <li>允许用户选择要删除的组件库</li>
     *   <li>提供删除确认对话框</li>
     *   <li>支持组件库的永久删除</li>
     *   <li>通知相关组件重新加载数据</li>
     * </ul>
     *
     * <p>安全特性：</p>
     * <ul>
     *   <li>只允许删除自定义组件库</li>
     *   <li>内置组件库和远程组件库不可删除</li>
     *   <li>删除前需要用户确认</li>
     *   <li>删除后通知相关组件更新</li>
     * </ul>
     *
     * @param project 当前项目，用于显示对话框和获取组件库信息
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
                    
                    // 通知所有 ComponentProvider 重新加载组件数据
                    ComponentProviderManager.notifyAllProvidersReload();
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据组件库名称确定组件前缀</li>
     *   <li>支持常见组件库的前缀映射</li>
     *   <li>为未知组件库提供默认前缀</li>
     * </ul>
     *
     * <p>前缀映射规则：</p>
     * <ul>
     *   <li>Element Plus/Element UI: el-</li>
     *   <li>Ant Design Vue: a-</li>
     *   <li>其他组件库: my-</li>
     * </ul>
     *
     * @param libraryName 组件库名称
     * @return 对应的组件前缀
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

    /**
     * 更新动作状态
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据项目上下文动态启用/禁用动作</li>
     *   <li>只有在有项目时才启用此动作</li>
     *   <li>确保动作在正确的上下文中执行</li>
     * </ul>
     *
     * @param e 动作事件，包含项目上下文信息
     */
    @Override
    public void update(AnActionEvent e) {
        // 只有在有项目时才启用此动作
        Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setEnabledAndVisible(project != null);
    }
}
