package com.chu7;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

import com.intellij.openapi.ui.Messages;

public class CustomComponentDialog extends DialogWrapper {
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField versionField;
    private JTextField exampleField;
    private JTextField docUrlField;
    private JList<ComponentMeta> componentList;
    private DefaultListModel<ComponentMeta> listModel;
    private CustomComponentManager manager;

    public CustomComponentDialog(Project project) {
        super(project);
        this.manager = CustomComponentManager.getInstance(project);
        init();
        setTitle("自定义组件管理");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 左侧：组件列表
        listModel = new DefaultListModel<>();
        componentList = new JList<>(listModel);
        refreshComponentList();

        JScrollPane scrollPane = new JScrollPane(componentList);
        scrollPane.setPreferredSize(new Dimension(300, 400));

        // 右侧：编辑面板
        JPanel editPanel = createEditPanel();

        panel.add(scrollPane, BorderLayout.WEST);
        panel.add(editPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEditPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 组件名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("组件名称:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        // 描述
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField(20);
        panel.add(descriptionField, gbc);

        // 版本
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("版本:"), gbc);
        gbc.gridx = 1;
        versionField = new JTextField(20);
        panel.add(versionField, gbc);

        // 示例
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("示例:"), gbc);
        gbc.gridx = 1;
        exampleField = new JTextField(20);
        panel.add(exampleField, gbc);

        // 文档链接
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("文档链接:"), gbc);
        gbc.gridx = 1;
        docUrlField = new JTextField(20);
        panel.add(docUrlField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加");
        JButton updateButton = new JButton("更新");
        JButton deleteButton = new JButton("删除");
        JButton clearButton = new JButton("清空");

        addButton.addActionListener(e -> addComponent());
        updateButton.addActionListener(e -> updateComponent());
        deleteButton.addActionListener(e -> deleteComponent());
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // 上传组件库面板
        JPanel uploadPanel = new JPanel();
        JButton uploadButton = new JButton("上传组件库");
        JButton templateButton = new JButton("下载模板");
        JButton exportButton = new JButton("导出组件库");

        uploadButton.addActionListener(e -> uploadComponentLibrary());
        templateButton.addActionListener(e -> downloadTemplate());
        exportButton.addActionListener(e -> exportComponentLibrary());

        uploadPanel.add(uploadButton);
        uploadPanel.add(templateButton);
        uploadPanel.add(exportButton);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(uploadPanel, gbc);

        // 列表选择监听
        componentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ComponentMeta selected = componentList.getSelectedValue();
                if (selected != null) {
                    loadComponentToFields(selected);
                }
            }
        });

        return panel;
    }

    private void refreshComponentList() {
        listModel.clear();
        List<ComponentMeta> components = manager.getCustomComponents();
        for (ComponentMeta component : components) {
            listModel.addElement(component);
        }
    }

    private void addComponent() {
        ComponentMeta component = createComponentFromFields();
        if (component != null) {
            manager.addCustomComponent(component);
            refreshComponentList();
            clearFields();
        }
    }

    private void updateComponent() {
        ComponentMeta selected = componentList.getSelectedValue();
        if (selected != null) {
            ComponentMeta updated = createComponentFromFields();
            if (updated != null) {
                manager.removeCustomComponent(selected.name);
                manager.addCustomComponent(updated);
                refreshComponentList();
                clearFields();
            }
        }
    }

    private void deleteComponent() {
        ComponentMeta selected = componentList.getSelectedValue();
        if (selected != null) {
            manager.removeCustomComponent(selected.name);
            refreshComponentList();
            clearFields();
        }
    }

    private void clearFields() {
        nameField.setText("");
        descriptionField.setText("");
        versionField.setText("");
        exampleField.setText("");
        docUrlField.setText("");
    }

    private void loadComponentToFields(ComponentMeta component) {
        nameField.setText(component.name);
        descriptionField.setText(component.description);
        versionField.setText(component.version);
        exampleField.setText(component.example);
        docUrlField.setText(component.docUrl);
    }

    private ComponentMeta createComponentFromFields() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(getContentPane(), "组件名称不能为空！");
            return null;
        }

        ComponentMeta component = new ComponentMeta();
        component.name = name;
        component.description = descriptionField.getText().trim();
        component.version = versionField.getText().trim();
        component.example = exampleField.getText().trim();
        component.docUrl = docUrlField.getText().trim();

        return component;
    }

    @Override
    protected ValidationInfo doValidate() {
        return null;
    }

    private void uploadComponentLibrary() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter(file -> file.getExtension() != null && file.getExtension().equals("json"))
                .withTitle("选择组件库 JSON 文件")
                .withDescription("请选择包含组件定义的 JSON 文件");
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
                            refreshComponentList();
                        }
                    }
                }
            }
        });

    }

    private void downloadTemplate() {
        String template = ComponentLibraryUploader.getComponentLibraryTemplate();
        CopyPasteManager.getInstance().setContents(new StringSelection(template));
        Messages.showInfoMessage(getContentPane(),
                "模板已复制到剪贴板，请粘贴到文本编辑器中保存为 .json 文件",
                "模板下载成功");
    }

    private void exportComponentLibrary() {
        List<ComponentMeta> components = manager.getCustomComponents();
        if (components.isEmpty()) {
            Messages.showWarningDialog(manager.getProject(),
                    "当前没有自定义组件可导出",
                    "导出失败");
            return;
        }

        String json = manager.exportCustomComponentsToJson();
        CopyPasteManager.getInstance().setContents(new StringSelection(json));
        Messages.showInfoMessage(getContentPane(),
                String.format("已导出 %d 个组件到剪贴板，请粘贴到文本编辑器中保存", components.size()),
                "导出成功");
    }
} 