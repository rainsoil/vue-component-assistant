package com.chu7.vuecomponentassistant.ui;

import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ImportResult;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * 自定义组件库上传对话框 - 远程组件库版本
 * 
 * 功能说明：
 * - 支持本地JSON文件导入
 * - 支持远程JSON URL导入
 * - 预览组件库信息
 * - 验证组件库格式
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class CustomLibraryUploadDialog extends DialogWrapper {
    
    private final Project project;
    private final ComponentLibraryManager libraryManager;
    
    private JRadioButton localFileRadio;
    private JRadioButton remoteUrlRadio;
    private JTextField filePathField;
    private JTextField urlField;
    private JButton browseButton;
    private JButton validateButton;
    private JButton previewButton;
    private JTextArea previewArea;
    private JCheckBox enableAfterImportCheckBox;
    
    public CustomLibraryUploadDialog(Project project, ComponentLibraryManager libraryManager) {
        super(project);
        this.project = project;
        this.libraryManager = libraryManager;
        setTitle("📁 导入自定义组件库");
        setSize(800, 600);
        setResizable(true);
        init();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(800, 600));
        
        // 创建导入方式选择面板
        JPanel importMethodPanel = createImportMethodPanel();
        
        // 创建输入面板
        JPanel inputPanel = createInputPanel();
        
        // 创建预览面板
        JPanel previewPanel = createPreviewPanel();
        
        // 创建选项面板
        JPanel optionsPanel = createOptionsPanel();
        
        // 组装主面板
        mainPanel.add(importMethodPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.SOUTH);
        
        // 添加选项面板到底部
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(optionsPanel, BorderLayout.NORTH);
        mainPanel.add(bottomPanel, BorderLayout.EAST);
        
        return mainPanel;
    }
    
    /**
     * 创建导入方式选择面板
     */
    private JPanel createImportMethodPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("选择导入方式"));
        
        localFileRadio = new JRadioButton("本地JSON文件");
        remoteUrlRadio = new JRadioButton("远程JSON地址");
        
        ButtonGroup group = new ButtonGroup();
        group.add(localFileRadio);
        group.add(remoteUrlRadio);
        
        // 默认选择本地文件
        localFileRadio.setSelected(true);
        
        // 添加选择监听器
        localFileRadio.addActionListener(e -> updateInputPanel());
        remoteUrlRadio.addActionListener(e -> updateInputPanel());
        
        panel.add(localFileRadio);
        panel.add(remoteUrlRadio);
        
        return panel;
    }
    
    /**
     * 创建输入面板
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("输入信息"));
        
        // 本地文件输入
        JPanel localFilePanel = new JPanel(new BorderLayout());
        localFilePanel.add(new JLabel("文件路径:"), BorderLayout.WEST);
        filePathField = new JTextField();
        localFilePanel.add(filePathField, BorderLayout.CENTER);
        browseButton = new JButton("浏览");
        browseButton.addActionListener(e -> browseFile());
        localFilePanel.add(browseButton, BorderLayout.EAST);
        
        // 远程URL输入
        JPanel remoteUrlPanel = new JPanel(new BorderLayout());
        remoteUrlPanel.add(new JLabel("远程地址:"), BorderLayout.WEST);
        urlField = new JTextField();
        remoteUrlPanel.add(urlField, BorderLayout.CENTER);
        validateButton = new JButton("验证");
        validateButton.addActionListener(e -> validateUrl());
        remoteUrlPanel.add(validateButton, BorderLayout.EAST);
        
        // 预览按钮
        previewButton = new JButton("预览");
        previewButton.addActionListener(e -> previewLibrary());
        
        // 组装面板
        JPanel inputFieldsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        inputFieldsPanel.add(localFilePanel);
        inputFieldsPanel.add(remoteUrlPanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(previewButton);
        
        panel.add(inputFieldsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建预览面板
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("预览信息"));
        panel.setPreferredSize(new Dimension(600, 200));
        
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(previewArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建选项面板
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("导入选项"));
        panel.setPreferredSize(new Dimension(200, 100));
        
        enableAfterImportCheckBox = new JCheckBox("导入后立即启用", true);
        
        panel.add(enableAfterImportCheckBox, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * 更新输入面板显示状态
     */
    private void updateInputPanel() {
        boolean isLocalFile = localFileRadio.isSelected();
        filePathField.setEnabled(isLocalFile);
        browseButton.setEnabled(isLocalFile);
        urlField.setEnabled(!isLocalFile);
        validateButton.setEnabled(!isLocalFile);
    }
    
    /**
     * 浏览文件
     */
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择组件库JSON文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON文件", "json"));
        
        if (fileChooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
        }
    }
    
    /**
     * 验证URL
     */
    private void validateUrl() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            Messages.showWarningDialog("请输入URL", "验证失败");
            return;
        }
        
        try {
            // 显示验证进度
            Messages.showInfoMessage("正在验证URL...", "验证中");
            
            // 检查URL格式
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new RuntimeException("URL必须以http://或https://开头");
            }
            
            // 检查URL是否可访问
            boolean accessible = HttpClient.checkUrlAccessible(url);
            
            if (accessible) {
                // 尝试下载一小部分内容来验证是否为JSON
                String sampleJson = HttpClient.downloadJsonSample(url);
                if (sampleJson != null && sampleJson.trim().startsWith("{")) {
                    Messages.showInfoMessage("URL验证成功！这是一个有效的JSON文件。", "验证成功");
                } else {
                    Messages.showWarningDialog("URL可访问，但可能不是有效的JSON文件。", "验证警告");
                }
            } else {
                throw new RuntimeException("URL不可访问");
            }
            
        } catch (Exception e) {
            Messages.showErrorDialog("URL验证失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 预览组件库
     */
    private void previewLibrary() {
        try {
            ComponentLibrary library = null;
            
            if (localFileRadio.isSelected()) {
                library = loadLocalLibrary();
            } else {
                library = loadRemoteLibrary();
            }
            
            if (library != null) {
                showLibraryPreview(library);
            }
            
        } catch (Exception e) {
            Messages.showErrorDialog("预览失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 加载本地组件库
     */
    private ComponentLibrary loadLocalLibrary() throws IOException {
        String filePath = filePathField.getText().trim();
        if (filePath.isEmpty()) {
            Messages.showWarningDialog("请选择JSON文件", "提示");
            return null;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            Messages.showErrorDialog("文件不存在: " + filePath, "错误");
            return null;
        }
        
        String json = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        return parseComponentLibrary(json);
    }
    
    /**
     * 加载远程组件库
     */
    private ComponentLibrary loadRemoteLibrary() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            Messages.showWarningDialog("请输入URL", "提示");
            return null;
        }
        
        try {
            // 显示加载进度
            Messages.showInfoMessage("正在加载远程组件库...", "加载中");
            
            // 使用 HttpClient 下载 JSON
            String json = HttpClient.downloadJson(url);
            
            // 解析组件库
            ComponentLibrary library = parseComponentLibrary(json);
            
            // 验证组件库
            if (library.getName() == null || library.getName().trim().isEmpty()) {
                throw new RuntimeException("组件库名称不能为空");
            }
            
            if (library.getComponents() == null || library.getComponents().isEmpty()) {
                throw new RuntimeException("组件库必须包含至少一个组件");
            }
            
            Messages.showInfoMessage("远程组件库加载成功！", "成功");
            return library;
            
        } catch (Exception e) {
            Messages.showErrorDialog("远程加载失败: " + e.getMessage(), "错误");
            return null;
        }
    }
    
    /**
     * 解析组件库JSON
     */
    private ComponentLibrary parseComponentLibrary(String json) {
        try {
            Gson gson = new Gson();
            ComponentLibrary library = gson.fromJson(json, ComponentLibrary.class);
            
            if (library == null) {
                throw new JsonSyntaxException("解析结果为空");
            }
            
            return library;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("JSON格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 显示组件库预览
     */
    private void showLibraryPreview(ComponentLibrary library) {
        StringBuilder preview = new StringBuilder();
        preview.append("组件库预览\n");
        preview.append("==========\n\n");
        preview.append("名称: ").append(library.getDisplayName()).append("\n");
        preview.append("版本: ").append(library.getVersion()).append("\n");
        preview.append("描述: ").append(library.getDescription()).append("\n");
        
        if (library.getComponents() != null) {
            preview.append("组件数量: ").append(library.getComponents().size()).append("\n");
            preview.append("\n前5个组件:\n");
            for (int i = 0; i < Math.min(library.getComponents().size(), 5); i++) {
                preview.append(i + 1).append(". ").append(library.getComponents().get(i).getName()).append("\n");
            }
        }
        
        previewArea.setText(preview.toString());
    }
    
    @Override
    protected void doOKAction() {
        try {
            ComponentLibrary library = null;
            
            if (localFileRadio.isSelected()) {
                library = loadLocalLibrary();
            } else {
                library = loadRemoteLibrary();
            }
            
            if (library == null) {
                return;
            }
            
            // 设置来源信息
            if (localFileRadio.isSelected()) {
                library.setSource(ComponentLibrary.LibrarySource.CUSTOM_LOCAL);
                library.setSourceUrl(filePathField.getText().trim());
            } else {
                library.setSource(ComponentLibrary.LibrarySource.CUSTOM_REMOTE);
                library.setSourceUrl(urlField.getText().trim());
            }
            
            // 导入组件库
            ImportResult result = libraryManager.importLibrary(library);
            
            if (result.isSuccess()) {
                Messages.showInfoMessage("组件库导入成功: " + library.getName(), "成功");
                super.doOKAction();
            } else if (result.isConflict()) {
                // 处理冲突
                int choice = Messages.showYesNoDialog(
                    "已存在同名组件库，是否替换？\n" +
                    "现有版本: " + result.getExistingLibrary().getVersion() + "\n" +
                    "新版本: " + library.getVersion(),
                    "组件库冲突",
                    Messages.getQuestionIcon()
                );
                
                if (choice == Messages.YES) {
                    ImportResult replaceResult = libraryManager.replaceLibrary(library, result.getExistingLibrary());
                    if (replaceResult.isSuccess()) {
                        Messages.showInfoMessage("组件库替换成功: " + library.getName(), "成功");
                        super.doOKAction();
                    } else {
                        Messages.showErrorDialog("替换失败: " + replaceResult.getMessage(), "错误");
                    }
                }
            } else {
                Messages.showErrorDialog("导入失败: " + result.getMessage(), "错误");
            }
            
        } catch (Exception e) {
            Messages.showErrorDialog("导入失败: " + e.getMessage(), "错误");
        }
    }
}
