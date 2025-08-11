package bak.ui;

import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 自定义组件库上传对话框
 * 
 * 功能说明：
 * - 允许用户选择JSON文件上传自定义组件库
 * - 支持直接输入JSON配置内容
 * - 提供配置验证功能
 * - 显示上传结果和错误信息
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class CustomLibraryUploadDialog extends DialogWrapper {
    
    private final Project project;
    private JTextArea jsonContentArea;
    private JLabel filePathLabel;
    private JButton selectFileButton;
    private JTextArea resultArea;
    private JButton validateButton;
    private JButton loadButton;
    
    public CustomLibraryUploadDialog(Project project) {
        super(project);
        this.project = project;
        setTitle("上传自定义组件库");
        setSize(800, 600);
        init();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(800, 600));
        
        // 创建文件选择区域
        JPanel filePanel = createFileSelectionPanel();
        
        // 创建JSON内容编辑区域
        JPanel contentPanel = createContentPanel();
        
        // 创建操作按钮区域
        JPanel buttonPanel = createButtonPanel();
        
        // 创建结果显示区域
        JPanel resultPanel = createResultPanel();
        
        // 组装主面板
        mainPanel.add(filePanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(resultPanel, BorderLayout.EAST);
        
        return mainPanel;
    }
    
    /**
     * 创建文件选择面板
     */
    private JPanel createFileSelectionPanel() {
        filePathLabel = new JLabel("未选择文件");
        filePathLabel.setBorder(JBUI.Borders.empty(5));
        
        selectFileButton = new JButton("选择JSON文件");
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectJsonFile();
            }
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.compound(
            JBUI.Borders.customLine(Color.GRAY, 1),
            JBUI.Borders.empty(10)
        ));
        panel.add(new JLabel("📁 选择组件库配置文件："), BorderLayout.NORTH);
        panel.add(filePathLabel, BorderLayout.CENTER);
        panel.add(selectFileButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建内容编辑面板
     */
    private JPanel createContentPanel() {
        jsonContentArea = new JTextArea();
        // 设置支持中文的字体
        Font font = new Font("Microsoft YaHei", Font.PLAIN, 12);
        if (!font.getFamily().equals("Microsoft YaHei")) {
            font = new Font("SimSun", Font.PLAIN, 12);
        }
        if (!font.getFamily().equals("SimSun")) {
            font = new Font("Dialog", Font.PLAIN, 12);
        }
        jsonContentArea.setFont(font);
        jsonContentArea.setLineWrap(true);
        jsonContentArea.setWrapStyleWord(true);
        
        // 设置示例内容
        jsonContentArea.setText(getExampleJsonContent());
        
        JScrollPane scrollPane = new JBScrollPane(jsonContentArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.compound(
            JBUI.Borders.customLine(Color.GRAY, 1),
            JBUI.Borders.empty(10)
        ));
        panel.add(new JLabel("📝 组件库配置内容（JSON格式）："), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        validateButton = new JButton("🔍 验证配置");
        validateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateConfiguration();
            }
        });
        
        loadButton = new JButton("📦 加载组件库");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCustomLibrary();
            }
        });
        
        JButton clearButton = new JButton("🗑️ 清空内容");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonContentArea.setText("");
            }
        });
        
        JButton exampleButton = new JButton("📋 加载示例");
        exampleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonContentArea.setText(getExampleJsonContent());
            }
        });
        
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(validateButton);
        panel.add(loadButton);
        panel.add(clearButton);
        panel.add(exampleButton);
        
        return panel;
    }
    
    /**
     * 创建结果显示面板
     */
    private JPanel createResultPanel() {
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        resultArea.setBackground(new Color(248, 249, 250));
        
        JScrollPane scrollPane = new JBScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(250, 400));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.compound(
            JBUI.Borders.customLine(Color.GRAY, 1),
            JBUI.Borders.empty(10)
        ));
        panel.add(new JLabel("📊 操作结果："), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 选择JSON文件
     */
    private void selectJsonFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            
            @Override
            public String getDescription() {
                return "JSON Files (*.json)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this.getContentPane());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathLabel.setText(selectedFile.getAbsolutePath());
            
            try {
                // 读取文件内容
                String content = readFileContent(selectedFile);
                jsonContentArea.setText(content);
                appendResult("✅ 文件加载成功：" + selectedFile.getName());
            } catch (IOException e) {
                appendResult("❌ 文件读取失败：" + e.getMessage());
            }
        }
    }
    
    /**
     * 验证配置
     */
    private void validateConfiguration() {
        String jsonContent = jsonContentArea.getText().trim();
        if (jsonContent.isEmpty()) {
            appendResult("❌ 配置内容为空");
            return;
        }
        
        try {
            CustomComponentLibraryManager.ValidationResult result = 
                CustomComponentLibraryManager.validateCustomLibraryConfig(jsonContent);
            
            if (result.isValid()) {
                appendResult("✅ 配置验证通过");
                if (!result.getWarnings().isEmpty()) {
                    appendResult("⚠️ 警告信息：");
                    for (String warning : result.getWarnings()) {
                        appendResult("   - " + warning);
                    }
                }
            } else {
                appendResult("❌ 配置验证失败：");
                for (String error : result.getErrors()) {
                    appendResult("   - " + error);
                }
            }
        } catch (Exception e) {
            appendResult("❌ 验证过程出错：" + e.getMessage());
        }
    }
    
    /**
     * 加载自定义组件库
     */
    private void loadCustomLibrary() {
        String jsonContent = jsonContentArea.getText().trim();
        if (jsonContent.isEmpty()) {
            appendResult("❌ 配置内容为空");
            return;
        }
        
        try {
            // 先验证配置
            CustomComponentLibraryManager.ValidationResult validationResult = 
                CustomComponentLibraryManager.validateCustomLibraryConfig(jsonContent);
            
            if (!validationResult.isValid()) {
                appendResult("❌ 配置验证失败，无法加载：");
                for (String error : validationResult.getErrors()) {
                    appendResult("   - " + error);
                }
                return;
            }
            
            // 加载组件库
            boolean success = CustomComponentLibraryManager.loadCustomLibrary(jsonContent);
            
            if (success) {
                appendResult("✅ 自定义组件库加载成功！");
                appendResult("📋 已加载的组件库信息：");
                CustomComponentLibraryManager.printCustomLibrariesInfo();
                
                // 显示成功消息
                Messages.showInfoMessage(
                    "自定义组件库加载成功！\n现在可以在代码中使用这些组件了。",
                    "加载成功"
                );
                
                // 关闭对话框
                close(OK_EXIT_CODE);
            } else {
                appendResult("❌ 组件库加载失败");
            }
            
        } catch (Exception e) {
            appendResult("❌ 加载过程出错：" + e.getMessage());
        }
    }
    
    /**
     * 添加结果信息
     */
    private void appendResult(String message) {
        resultArea.append(message + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }
    
    /**
     * 读取文件内容
     */
    private String readFileContent(File file) throws IOException {
        try (InputStream inputStream = new java.io.FileInputStream(file)) {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
    
    /**
     * 获取示例JSON内容
     */
    private String getExampleJsonContent() {
        return "{\n" +
               "  \"name\": \"my-custom-library\",\n" +
               "  \"displayName\": \"我的自定义组件库\",\n" +
               "  \"version\": \"1.0.0\",\n" +
               "  \"description\": \"这是一个示例自定义组件库\",\n" +
               "  \"componentPrefix\": \"my-\",\n" +
               "  \"documentationUrlTemplate\": \"https://example.com/docs/%s\",\n" +
               "  \"components\": [\n" +
               "    {\n" +
               "      \"name\": \"my-button\",\n" +
               "      \"description\": \"自定义按钮组件\",\n" +
               "      \"props\": [\n" +
               "        {\n" +
               "          \"name\": \"type\",\n" +
               "          \"description\": \"按钮类型\",\n" +
               "          \"type\": \"string\",\n" +
               "          \"options\": [\"primary\", \"secondary\", \"danger\"],\n" +
               "          \"defaultValue\": \"primary\"\n" +
               "        },\n" +
               "        {\n" +
               "          \"name\": \"size\",\n" +
               "          \"description\": \"按钮大小\",\n" +
               "          \"type\": \"string\",\n" +
               "          \"options\": [\"small\", \"medium\", \"large\"],\n" +
               "          \"defaultValue\": \"medium\"\n" +
               "        }\n" +
               "      ],\n" +
               "      \"events\": [\n" +
               "        {\n" +
               "          \"name\": \"click\",\n" +
               "          \"description\": \"点击事件\",\n" +
               "          \"parameters\": \"(event: MouseEvent)\"\n" +
               "        }\n" +
               "      ],\n" +
               "      \"slots\": [\n" +
               "        {\n" +
               "          \"name\": \"default\",\n" +
               "          \"description\": \"按钮内容\",\n" +
               "          \"scope\": null\n" +
               "        }\n" +
               "      ]\n" +
               "    },\n" +
               "    {\n" +
               "      \"name\": \"my-input\",\n" +
               "      \"description\": \"自定义输入框组件\",\n" +
               "      \"props\": [\n" +
               "        {\n" +
               "          \"name\": \"placeholder\",\n" +
               "          \"description\": \"占位符文本\",\n" +
               "          \"type\": \"string\",\n" +
               "          \"defaultValue\": \"\"\n" +
               "        },\n" +
               "        {\n" +
               "          \"name\": \"disabled\",\n" +
               "          \"description\": \"是否禁用\",\n" +
               "          \"type\": \"boolean\",\n" +
               "          \"defaultValue\": false\n" +
               "        }\n" +
               "      ],\n" +
               "      \"events\": [\n" +
               "        {\n" +
               "          \"name\": \"input\",\n" +
               "          \"description\": \"输入事件\",\n" +
               "          \"parameters\": \"(value: string)\"\n" +
               "        },\n" +
               "        {\n" +
               "          \"name\": \"change\",\n" +
               "          \"description\": \"值改变事件\",\n" +
               "          \"parameters\": \"(value: string)\"\n" +
               "        }\n" +
               "      ],\n" +
               "      \"slots\": []\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
}
