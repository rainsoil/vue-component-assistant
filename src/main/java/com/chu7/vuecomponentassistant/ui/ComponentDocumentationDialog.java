package com.chu7.vuecomponentassistant.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

/**
 * 组件文档对话框
 * 
 * 功能说明：
 * - 显示组件的详细文档信息
 * - 支持滚动查看长文档
 * - 提供打开官方文档的按钮
 * - 使用 Element Plus 风格的界面设计
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentDocumentationDialog extends DialogWrapper {

    private final String componentName;
    private final String documentation;
    private final Project project;
    private JEditorPane editorPane;

    /**
     * 构造函数
     * 
     * @param project 当前项目
     * @param componentName 组件名称
     * @param documentation 文档内容
     */
    public ComponentDocumentationDialog(Project project, String componentName, String documentation) {
        super(project);
        this.project = project;
        this.componentName = componentName;
        this.documentation = documentation;
        
        setTitle("📚 " + componentName + " 组件文档");
        setSize(1000, 1800); // 设置更大的对话框尺寸
        setResizable(true);
        init();
        
        // 测试编码
        testEncoding();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1000, 1800));
        mainPanel.setBorder(JBUI.Borders.empty(10));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建文档内容面板
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * 创建标题面板
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
        panel.setBackground(new Color(64, 158, 255)); // Element Plus 主色调

        JLabel titleLabel = new JLabel("📦 " + componentName);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(JBUI.Borders.empty(20, 25));

        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建内容面板
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));

        // 创建 HTML 编辑器面板
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        
        // 设置 HTML 编辑器工具包
        HTMLEditorKit kit = new HTMLEditorKit();
        editorPane.setEditorKit(kit);
        
        // 设置样式表
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: 'Microsoft YaHei', 'SimSun', 'Dialog', sans-serif; font-size: 14px; line-height: 1.6; color: #303133; background-color: #f8f9fa; margin: 0; padding: 20px; }");
        styleSheet.addRule("h1 { color: #409EFF; font-size: 24px; margin-bottom: 20px; border-bottom: 2px solid #409EFF; padding-bottom: 10px; }");
        styleSheet.addRule("h2 { color: #67C23A; font-size: 20px; margin-top: 30px; margin-bottom: 15px; }");
        styleSheet.addRule("h3 { color: #E6A23C; font-size: 18px; margin-top: 25px; margin-bottom: 12px; }");
        styleSheet.addRule("table { width: 100%; border-collapse: collapse; margin: 15px 0; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        styleSheet.addRule("th { background: linear-gradient(135deg, #409EFF, #67C23A); color: white; font-weight: 600; font-size: 13px; padding: 12px 15px; text-align: left; border: none; }");
        styleSheet.addRule("td { padding: 12px 15px; border-bottom: 1px solid #e4e7ed; font-size: 13px; line-height: 1.5; }");
        styleSheet.addRule("tr:last-child td { border-bottom: none; }");
        styleSheet.addRule("tr:hover { background: linear-gradient(135deg, #f8f9fa, #e9ecef); }");
        styleSheet.addRule(".prop-name { color: #409EFF; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 12px; background: rgba(64, 158, 255, 0.1); padding: 4px 8px; border-radius: 4px; }");
        styleSheet.addRule(".event-name { color: #67C23A; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 12px; background: rgba(103, 194, 58, 0.1); padding: 4px 8px; border-radius: 4px; }");
        styleSheet.addRule(".slot-name { color: #E6A23C; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 12px; background: rgba(230, 162, 60, 0.1); padding: 4px 8px; border-radius: 4px; }");
        styleSheet.addRule(".section { margin: 25px 0; padding: 20px; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }");
        styleSheet.addRule(".description { color: #606266; font-size: 14px; line-height: 1.6; margin: 15px 0; padding: 15px; background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-radius: 6px; border-left: 3px solid #409EFF; }");
        styleSheet.addRule(".example { background: linear-gradient(135deg, #f8f9fa, #e9ecef); border: 1px solid #e4e7ed; border-radius: 6px; padding: 15px; margin: 15px 0; }");
        styleSheet.addRule(".example pre { margin: 0; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 12px; color: #303133; line-height: 1.4; background: rgba(255,255,255,0.8); padding: 10px; border-radius: 4px; }");
        styleSheet.addRule(".link { color: #409EFF; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; padding: 8px 12px; background: linear-gradient(135deg, #ecf5ff, #e1f5fe); border-radius: 6px; transition: all 0.3s; font-weight: 500; }");
        styleSheet.addRule(".link:hover { background: linear-gradient(135deg, #409EFF, #67C23A); color: #ffffff; text-decoration: none; }");
        
        // 设置文档内容
        if (documentation != null) {
            editorPane.setText(documentation);
        } else {
            editorPane.setText("<html><body><h1>无法加载文档内容</h1></body></html>");
        }
        
        editorPane.setCaretPosition(0);

        // 创建滚动面板
        JBScrollPane scrollPane = new JBScrollPane(editorPane);
        scrollPane.setBorder(JBUI.Borders.compound(
            JBUI.Borders.customLine(new Color(228, 231, 237), 1),
            JBUI.Borders.empty(5)
        ));
        scrollPane.setPreferredSize(new Dimension(1980, 1680));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(JBUI.Borders.empty(10, 0, 0, 0));

        // 打开官方文档按钮
        JButton openDocButton = new JButton("🌐 打开官方文档");
        openDocButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        openDocButton.setBackground(new Color(64, 158, 255));
        openDocButton.setForeground(Color.WHITE);
        openDocButton.setBorderPainted(false);
        openDocButton.setFocusPainted(false);
        openDocButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openDocButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openOfficialDocumentation();
            }
        });

        // 复制文档按钮
        JButton copyButton = new JButton("📋 复制文档");
        copyButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        copyButton.setBackground(new Color(103, 194, 58));
        copyButton.setForeground(Color.WHITE);
        copyButton.setBorderPainted(false);
        copyButton.setFocusPainted(false);
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyDocumentation();
            }
        });

        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        closeButton.setBackground(new Color(144, 147, 153));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close(OK_EXIT_CODE);
            }
        });

        panel.add(openDocButton);
        panel.add(copyButton);
        panel.add(closeButton);

        return panel;
    }

    /**
     * 打开官方文档
     */
    private void openOfficialDocumentation() {
        try {
            // 根据组件名称生成文档 URL
            String docUrl = generateDocumentationUrl();
            if (docUrl.isEmpty()) {
                Messages.showErrorDialog("无法生成文档链接", "错误");
                return;
            }
            Desktop.getDesktop().browse(URI.create(docUrl));
        } catch (IOException e) {
            Messages.showErrorDialog(
                "无法打开浏览器: " + e.getMessage(),
                "错误"
            );
        }
    }

    /**
     * 生成文档 URL
     */
    private String generateDocumentationUrl() {
        // 根据组件前缀判断组件库类型
        if (componentName.startsWith("el-")) {
            // Element UI 或 Element Plus
            String componentKey = componentName.substring(3);
            // 这里可以根据项目配置进一步判断是 Element UI 还是 Element Plus
            // 暂时使用 Element Plus 的 URL 格式
            return "https://element-plus.org/zh-CN/component/" + componentKey + ".html";
        } else if (componentName.startsWith("a-")) {
            // Ant Design Vue
            String componentKey = componentName.substring(2);
            return "https://antdv.com/components/" + componentKey + "-cn";
        }
        return "";
    }

    /**
     * 复制文档内容
     */
    private void copyDocumentation() {
        editorPane.selectAll();
        editorPane.copy();
        editorPane.setCaretPosition(0);
        Messages.showInfoMessage(
            "文档内容已复制到剪贴板",
            "复制成功"
        );
    }

    /**
     * 测试编码是否正确
     */
    private void testEncoding() {
        System.out.println("=== 编码测试 ===");
        System.out.println("文档内容长度: " + (documentation != null ? documentation.length() : 0));
        System.out.println("文档内容前100字符: " + (documentation != null ? documentation.substring(0, Math.min(100, documentation.length())) : "null"));
        System.out.println("字体名称: " + editorPane.getFont().getName());
        System.out.println("字体是否支持中文: " + editorPane.getFont().canDisplay('中'));
    }

    @Override
    protected Action[] createActions() {
        return new Action[0]; // 不使用默认按钮
    }
}
