package com.chu7;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ide.CopyPasteManager;
import java.awt.datatransfer.StringSelection;
import com.intellij.openapi.ui.Messages;

public class ComponentLibraryDialog extends DialogWrapper {
    private JList<ComponentLibrary> libraryList;
    private DefaultListModel<ComponentLibrary> listModel;
    private ComponentLibraryManager manager;
    private JTextArea infoTextArea;

    public ComponentLibraryDialog(Project project) {
        super(project);
        this.manager = ComponentLibraryManager.getInstance(project);
        init();
        setTitle("组件库管理");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 左侧：组件库列表
        listModel = new DefaultListModel<>();
        libraryList = new JList<>(listModel);
        refreshLibraryList();

        JScrollPane scrollPane = new JScrollPane(libraryList);
        scrollPane.setPreferredSize(new Dimension(300, 400));

        // 右侧：信息面板
        JPanel infoPanel = createInfoPanel();

        panel.add(scrollPane, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 信息显示区域
        infoTextArea = new JTextArea();
        infoTextArea.setEditable(false);
        infoTextArea.setLineWrap(true);
        infoTextArea.setWrapStyleWord(true);
        JScrollPane infoScrollPane = new JScrollPane(infoTextArea);
        infoScrollPane.setPreferredSize(new Dimension(400, 300));
        panel.add(infoScrollPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("删除组件库");
        JButton uploadButton = new JButton("上传组件库");
        JButton templateButton = new JButton("下载模板");
        JButton exportButton = new JButton("导出组件库");
        
        deleteButton.addActionListener(e -> deleteLibrary());
        uploadButton.addActionListener(e -> uploadComponentLibrary());
        templateButton.addActionListener(e -> downloadTemplate());
        exportButton.addActionListener(e -> exportComponentLibrary());
        
        buttonPanel.add(deleteButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(templateButton);
        buttonPanel.add(exportButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 列表选择监听
        libraryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComponentLibrary selected = libraryList.getSelectedValue();
                if (selected != null) {
                    showLibraryInfo(selected);
                } else {
                    infoTextArea.setText("");
                }
            }
        });
        
        return panel;
    }

    private void refreshLibraryList() {
        listModel.clear();
        Map<String, ComponentLibrary> libraries = manager.getLibraries();
        for (ComponentLibrary library : libraries.values()) {
            listModel.addElement(library);
        }
    }

    private void showLibraryInfo(ComponentLibrary library) {
        StringBuilder info = new StringBuilder();
        info.append("组件库信息:\n\n");
        info.append("名称: ").append(library.name).append("\n");
        info.append("描述: ").append(library.description != null ? library.description : "").append("\n");
        info.append("版本: ").append(library.version != null ? library.version : "").append("\n");
        info.append("作者: ").append(library.author != null ? library.author : "").append("\n");
        info.append("官网: ").append(library.website != null ? library.website : "").append("\n");
        info.append("文档: ").append(library.docUrl != null ? library.docUrl : "").append("\n\n");
        
        List<ComponentMeta> components = library.getComponents();
        info.append("包含组件 (").append(components.size()).append(" 个):\n");
        for (ComponentMeta component : components) {
            info.append("• ").append(component.name);
            if (component.description != null && !component.description.isEmpty()) {
                info.append(" - ").append(component.description);
            }
            info.append("\n");
        }
        
        infoTextArea.setText(info.toString());
    }

    private void deleteLibrary() {
        ComponentLibrary selected = libraryList.getSelectedValue();
        if (selected != null) {
            int result = JOptionPane.showConfirmDialog(
                getContentPane(),
                "确定要删除组件库 '" + selected.name + "' 吗？\n这将删除该组件库中的所有组件。",
                "确认删除",
                JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                manager.removeLibrary(selected.name);
                refreshLibraryList();
                infoTextArea.setText("");
            }
        } else {
            Messages.showWarningDialog(getContentPane(), "请先选择一个组件库", "提示");
        }
    }

    @Override
    protected ValidationInfo doValidate() {
        return null;
    }

    private void uploadComponentLibrary() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter(file -> file.getExtension() != null && file.getExtension().equals("json"))
                .withTitle("选择组件库 JSON 文件")
                .withDescription("请选择包含组件库定义的 JSON 文件");

        FileChooser.chooseFiles(descriptor, manager.getProject(), null, new FileChooser.FileChooserConsumer() {
            @Override
            public void cancelled() {
            }

            @Override
            public void consume(List<VirtualFile> virtualFiles) {
                for (VirtualFile file : virtualFiles) {
                    if (file != null) {
                        boolean success = ComponentLibraryUploader.uploadComponentLibrary(manager.getProject(), file);
                        if (success) {
                            refreshLibraryList();
                        }
                    }
                }
            }
        });
    }

    private void downloadTemplate() {
        String template = ComponentLibraryUploader.generateComponentLibraryTemplate();
        CopyPasteManager.getInstance().setContents(new StringSelection(template));
        Messages.showInfoMessage(getContentPane(),
                "模板已复制到剪贴板，请粘贴到文本编辑器中保存为 .json 文件",
                "模板下载成功");
    }

    private void exportComponentLibrary() {
        Map<String, ComponentLibrary> libraries = manager.getLibraries();
        if (libraries.isEmpty()) {
            Messages.showWarningDialog(manager.getProject(),
                    "当前没有组件库可导出",
                    "导出失败");
            return;
        }

        String json = manager.exportLibrariesToJson();
        CopyPasteManager.getInstance().setContents(new StringSelection(json));
        Messages.showInfoMessage(getContentPane(),
                String.format("已导出 %d 个组件库到剪贴板，请粘贴到文本编辑器中保存", libraries.size()),
                "导出成功");
    }
} 