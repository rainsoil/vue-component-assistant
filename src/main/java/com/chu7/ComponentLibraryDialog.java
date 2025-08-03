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
    private JCheckBox enableCheckBox;

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
        
        // 启用/禁用复选框
        enableCheckBox = new JCheckBox("启用此组件库");
        enableCheckBox.addActionListener(e -> toggleLibraryEnabled());
        panel.add(enableCheckBox, BorderLayout.NORTH);
        
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
                    updateEnableCheckBox(selected);
                } else {
                    infoTextArea.setText("");
                    enableCheckBox.setEnabled(false);
                }
            }
        });
        
        return panel;
    }

    private void refreshLibraryList() {
        listModel.clear();
        
        // 添加内置组件库
        addBuiltinLibraries();
        
        // 添加自定义组件库
        addCustomLibraries();
    }

    private void addBuiltinLibraries() {
        // Element Plus
        ComponentLibrary elementPlus = manager.getLibrary(ComponentLibraryManager.ELEMENT_PLUS);
        if (elementPlus != null) {
            listModel.addElement(elementPlus);
        }
        
        // Element UI
        ComponentLibrary elementUI = manager.getLibrary(ComponentLibraryManager.ELEMENT_UI);
        if (elementUI != null) {
            listModel.addElement(elementUI);
        }
        
        // Ant Design Vue
        ComponentLibrary antDesignVue = manager.getLibrary(ComponentLibraryManager.ANT_DESIGN_VUE);
        if (antDesignVue != null) {
            listModel.addElement(antDesignVue);
        }
    }

    private void addCustomLibraries() {
        List<String> customLibraries = manager.getCustomLibraries();
        for (String libraryName : customLibraries) {
            ComponentLibrary library = manager.getLibrary(libraryName);
            if (library != null) {
                listModel.addElement(library);
            }
        }
    }

    private void showLibraryInfo(ComponentLibrary library) {
        StringBuilder info = new StringBuilder();
        info.append("组件库信息：\n\n");
        info.append("名称：").append(library.name).append("\n");
        info.append("描述：").append(library.description).append("\n");
        info.append("版本：").append(library.version).append("\n");
        info.append("作者：").append(library.author).append("\n");
        info.append("官网：").append(library.website).append("\n");
        info.append("文档：").append(library.docUrl).append("\n");
        info.append("组件数量：").append(library.components.size()).append("\n");
        
        if (manager.isBuiltinLibrary(library.name)) {
            info.append("\n类型：内置组件库");
        } else {
            info.append("\n类型：自定义组件库");
        }
        
        infoTextArea.setText(info.toString());
    }

    private void updateEnableCheckBox(ComponentLibrary library) {
        boolean isEnabled = manager.isLibraryEnabled(library.name);
        enableCheckBox.setSelected(isEnabled);
        enableCheckBox.setEnabled(true);
        
        // 内置组件库显示不同的文本
        if (manager.isBuiltinLibrary(library.name)) {
            enableCheckBox.setText("启用此组件库");
        } else {
            enableCheckBox.setText("启用此组件库");
        }
    }

    private void toggleLibraryEnabled() {
        ComponentLibrary selected = libraryList.getSelectedValue();
        if (selected != null) {
            boolean enabled = enableCheckBox.isSelected();
            if (enabled) {
                manager.enableLibrary(selected.name);
            } else {
                manager.disableLibrary(selected.name);
            }
        }
    }

    private void deleteLibrary() {
        ComponentLibrary selected = libraryList.getSelectedValue();
        if (selected == null) {
            Messages.showWarningDialog("请先选择一个组件库", "删除组件库");
            return;
        }
        
        if (manager.isBuiltinLibrary(selected.name)) {
            Messages.showInfoMessage("内置组件库不能删除，只能禁用", "删除组件库");
            return;
        }
        
        int result = Messages.showYesNoDialog(
            "确定要删除组件库 '" + selected.name + "' 吗？", 
            "确认删除", 
            Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            manager.removeLibrary(selected.name);
            refreshLibraryList();
            infoTextArea.setText("");
        }
    }

    @Override
    protected ValidationInfo doValidate() {
        return null;
    }

    private void uploadComponentLibrary() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter(file -> file.getExtension() != null && file.getExtension().equals("json"))
                .withTitle("选择组件库文件")
                .withDescription("选择 JSON 格式的组件库文件");
//        FileChooser.chooseFiles(descriptor, manager.getProject(), null, new FileChooser.FileChooserConsumer() {
//            @Override
//            public void cancelled() {
//            }
//
//            @Override
//            public void consume(List<VirtualFile> virtualFiles) {
//                for (VirtualFile file : virtualFiles) {
//                    if (file != null) {
//                        boolean success = ComponentLibraryUploader.uploadComponentLibrary(manager.getProject(), file);
//                        if (success) {
//                            refreshLibraryList();
//                        }
//                    }
//                }
//            }
//        });
        FileChooser.chooseFiles(descriptor, manager.getProject(), null, new FileChooser.FileChooserConsumer() {
            @Override
            public void cancelled() {
                // 用户取消选择
            }

            @Override
            public void consume(List<VirtualFile> virtualFiles) {
                if (!virtualFiles.isEmpty()) {
                    VirtualFile file = virtualFiles.get(0);
                    boolean success = ComponentLibraryUploader.uploadComponentLibrary(manager.getProject(), file);
                    if (success) {
                        refreshLibraryList();
                    }
                }
            }
        });
    }

    private void downloadTemplate() {
        String template = ComponentLibraryUploader.getComponentLibraryTemplate();
        CopyPasteManager.getInstance().setContents(new StringSelection(template));
        Messages.showInfoMessage("组件库模板已复制到剪贴板", "下载模板");
    }

    private void exportComponentLibrary() {
        ComponentLibrary selected = libraryList.getSelectedValue();
        if (selected == null) {
            Messages.showWarningDialog("请先选择一个组件库", "导出组件库");
            return;
        }
        
        String json = new com.google.gson.Gson().toJson(selected);
        CopyPasteManager.getInstance().setContents(new StringSelection(json));
        Messages.showInfoMessage("组件库数据已复制到剪贴板", "导出组件库");
    }
} 