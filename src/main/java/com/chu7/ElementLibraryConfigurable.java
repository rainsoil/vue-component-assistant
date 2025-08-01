package com.chu7;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ElementLibraryConfigurable implements Configurable {
    private JPanel panel;
    private JCheckBox autoDetectCheckBox;
    private JList<String> availableLibrariesList;
    private JList<String> selectedLibrariesList;
    private DefaultListModel<String> selectedLibrariesModel;

    @Nls
    @Override
    public String getDisplayName() {
        return "Element 组件库选择";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        panel = new JPanel(new BorderLayout());
        
        // 自动检测选项
        autoDetectCheckBox = new JCheckBox("自动检测项目中的组件库");
        panel.add(autoDetectCheckBox, BorderLayout.NORTH);
        
        // 组件库选择面板
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.add(new JLabel("选择要使用的组件库："), BorderLayout.NORTH);
        
        // 可用组件库列表
        availableLibrariesList = new JList<>(new String[]{"Element Plus", "Element UI", "Ant Design Vue"});
        JScrollPane availableScrollPane = new JScrollPane(availableLibrariesList);
        availableScrollPane.setPreferredSize(new Dimension(200, 150));
        selectionPanel.add(availableScrollPane, BorderLayout.WEST);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton(">>");
        JButton removeButton = new JButton("<<");
        addButton.addActionListener(e -> addToSelection());
        removeButton.addActionListener(e -> removeFromSelection());
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        selectionPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 选中的组件库列表
        selectedLibrariesList = new JList<>();
        selectedLibrariesModel = new DefaultListModel<>();
        selectedLibrariesList.setModel(selectedLibrariesModel);
        JScrollPane selectedScrollPane = new JScrollPane(selectedLibrariesList);
        selectedScrollPane.setPreferredSize(new Dimension(200, 150));
        selectionPanel.add(selectedScrollPane, BorderLayout.EAST);
        
        panel.add(selectionPanel, BorderLayout.CENTER);
        
        // 自定义组件库管理按钮
        JButton customButton = new JButton("管理自定义组件库");
        customButton.addActionListener(e -> {
            Project project = getCurrentProject();
            if (project != null) {
                ComponentLibraryDialog dialog = new ComponentLibraryDialog(project);
                dialog.show();
            }
        });
        panel.add(customButton, BorderLayout.SOUTH);
        
        return panel;
    }

    @Override
    public boolean isModified() {
        Project project = getCurrentProject();
        if (project == null) return false;
        
        ElementLibrarySettings settings = ElementLibrarySettings.getInstance(project);
        boolean currentAutoDetect = settings.isAutoDetect();
        List<String> currentSelected = settings.getSelectedLibraries();
        
        boolean newAutoDetect = autoDetectCheckBox.isSelected();
        List<String> newSelected = getSelectedLibraries();
        
        return currentAutoDetect != newAutoDetect || !currentSelected.equals(newSelected);
    }

    @Override
    public void apply() {
        Project project = getCurrentProject();
        if (project == null) return;
        
        ElementLibrarySettings settings = ElementLibrarySettings.getInstance(project);
        settings.setAutoDetect(autoDetectCheckBox.isSelected());
        settings.setSelectedLibraries(getSelectedLibraries());
    }

    @Override
    public void reset() {
        Project project = getCurrentProject();
        if (project == null) return;
        
        ElementLibrarySettings settings = ElementLibrarySettings.getInstance(project);
        autoDetectCheckBox.setSelected(settings.isAutoDetect());
        
        selectedLibrariesModel.clear();
        for (String library : settings.getSelectedLibraries()) {
            selectedLibrariesModel.addElement(libraryToDisplay(library));
        }
    }

    private Project getCurrentProject() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        return projects.length > 0 ? projects[0] : null;
    }

    private void addToSelection() {
        String selected = availableLibrariesList.getSelectedValue();
        if (selected != null && !selectedLibrariesModel.contains(selected)) {
            selectedLibrariesModel.addElement(selected);
        }
    }

    private void removeFromSelection() {
        int selectedIndex = selectedLibrariesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            selectedLibrariesModel.remove(selectedIndex);
        }
    }

    private List<String> getSelectedLibraries() {
        List<String> libraries = new ArrayList<>();
        for (int i = 0; i < selectedLibrariesModel.size(); i++) {
            String display = selectedLibrariesModel.getElementAt(i);
            libraries.add(displayToLibrary(display));
        }
        return libraries;
    }

    private String displayToLibrary(String display) {
        switch (display) {
            case "Element Plus": return "element-plus";
            case "Element UI": return "element-ui";
            case "Ant Design Vue": return "ant-design-vue";
            default: return display;
        }
    }

    private String libraryToDisplay(String lib) {
        switch (lib) {
            case "element-plus": return "Element Plus";
            case "element-ui": return "Element UI";
            case "ant-design-vue": return "Ant Design Vue";
            default: return lib;
        }
    }
}