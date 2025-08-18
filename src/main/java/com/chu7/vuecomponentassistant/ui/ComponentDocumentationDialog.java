package com.chu7.vuecomponentassistant.ui;

// IntelliJ IDEA 平台相关导入
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

// Java Swing UI 组件导入
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Java IO 和网络相关导入
import java.io.IOException;
import java.net.URI;

/**
 * 组件文档对话框
 * 
 * 功能说明：
 * - 显示组件的详细文档信息（属性、事件、插槽等）
 * - 支持滚动查看长文档内容
 * - 提供打开官方文档的按钮
 * - 使用 Element Plus 风格的界面设计
 * - 支持IDEA主题背景色适配
 * 
 * 设计特点：
 * - 与鼠标悬浮文档样式保持一致
 * - 使用IDEA原生背景色，支持主题切换
 * - 响应式布局，支持窗口大小调整
 * - 现代化的UI设计风格
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentDocumentationDialog extends DialogWrapper {

    // ==================== 成员变量 ====================
    
    /** 组件名称，用于显示标题和生成文档链接 */
    private final String componentName;
    
    /** HTML格式的文档内容 */
    private final String documentation;
    
    /** 当前项目实例，用于获取项目相关信息 */
    private final Project project;
    
    /** HTML编辑器面板，用于显示格式化的文档内容 */
    private JEditorPane editorPane;

    /**
     * 构造函数
     * 
     * 初始化组件文档对话框，设置基本属性和样式
     * 
     * @param project 当前项目实例，用于获取项目配置和主题信息
     * @param componentName 组件名称，如 "el-table"、"el-button" 等
     * @param documentation HTML格式的文档内容，包含组件的详细说明
     */
    public ComponentDocumentationDialog(Project project, String componentName, String documentation) {
        // 调用父类构造函数，传入项目实例
        super(project);
        
        // 初始化成员变量
        this.project = project;
        this.componentName = componentName;
        this.documentation = documentation;
        
        // 设置对话框基本属性
        setTitle("📚 " + componentName + " 组件文档"); // 设置标题，包含组件名称
        setSize(1000, 1800); // 设置对话框尺寸，提供足够的显示空间
        setResizable(true); // 允许用户调整对话框大小
        
        // 初始化对话框UI组件
        init();
        
        // 调试：测试文档编码是否正确
        testEncoding();
    }

    /**
     * 创建对话框的中心面板
     * 
     * 使用BorderLayout布局管理器，将对话框分为三个区域：
     * - 顶部：标题面板
     * - 中间：文档内容面板（可滚动）
     * - 底部：按钮面板
     * 
     * @return 配置好的主面板组件
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        // 创建主面板，使用BorderLayout布局管理器
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1000, 1800)); // 设置首选尺寸
        mainPanel.setBorder(JBUI.Borders.empty(10)); // 设置10像素的空白边框
        
        // 使用IDEA的背景色，确保与主题保持一致
        mainPanel.setBackground(null); // 设置为null以使用IDEA默认背景色

        // 创建并添加标题面板到顶部
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建并添加文档内容面板到中间（占据大部分空间）
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 创建并添加按钮面板到底部
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * 创建标题面板
     * 
     * 创建一个带有Element Plus主题色的标题栏，显示组件名称
     * 使用蓝色背景和白色文字，提供良好的视觉层次
     * 
     * @return 配置好的标题面板
     */
    private JPanel createTitlePanel() {
        // 创建标题面板，使用BorderLayout布局
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(0, 0, 10, 0)); // 设置底部间距
        panel.setBackground(new Color(64, 158, 255)); // 使用Element Plus主色调（蓝色）

        // 创建标题标签，包含组件图标和名称
        JLabel titleLabel = new JLabel("📦 " + componentName);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20)); // 设置字体和大小
        titleLabel.setForeground(Color.WHITE); // 设置白色文字
        titleLabel.setBorder(JBUI.Borders.empty(20, 25)); // 设置内边距

        // 将标题标签添加到面板中央
        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建内容面板
     * 
     * 创建文档内容显示区域，包含HTML编辑器和滚动面板
     * 设置与鼠标悬浮文档一致的样式，确保视觉统一性
     * 
     * @return 配置好的内容面板
     */
    private JPanel createContentPanel() {
        // 创建内容面板，使用BorderLayout布局
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(0, 0, 10, 0)); // 设置底部间距
        
        // 使用IDEA的背景色，确保与主题保持一致
        panel.setBackground(null); // 设置为null以使用IDEA默认背景色

        // 创建HTML编辑器面板，用于显示格式化的文档内容
        editorPane = new JEditorPane();
        editorPane.setEditable(false); // 设置为只读模式
        editorPane.setContentType("text/html"); // 设置内容类型为HTML
        
        // 使用IDEA的背景色，让文档背景透明以适配IDEA主题
        editorPane.setBackground(null); // 设置为null以使用IDEA默认背景色
        
        // 设置HTML编辑器工具包，用于解析和渲染HTML内容
        HTMLEditorKit kit = new HTMLEditorKit();
        editorPane.setEditorKit(kit);
        
        // 获取样式表对象，用于添加自定义CSS规则
        StyleSheet styleSheet = kit.getStyleSheet();
        
        // ==================== CSS样式规则设置 ====================
        // 注意：这些样式与鼠标悬浮文档保持一致，确保视觉统一性
        
        // 全局样式设置
        styleSheet.addRule("* { box-sizing: border-box; }"); // 设置盒模型为border-box
        
        // 主体样式 - 使用系统字体栈，支持多平台和多语言
        styleSheet.addRule("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background: transparent; }");
        
        // 文档容器样式 - 设置最大宽度和透明背景
        styleSheet.addRule(".ep-doc { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 100%; background: transparent; padding: 8px; border-radius: 6px; }");
        
        // 内容区域样式 - 透明背景和圆角
        styleSheet.addRule(".ep-content { background: transparent; border-radius: 4px; padding: 12px; }");
        
        // 标题样式 - Element Plus蓝色主题
        styleSheet.addRule(".ep-title { color: #409EFF; font-size: 16px; font-weight: 700; margin: 0 0 10px 0; padding-bottom: 6px; border-bottom: 2px solid #409EFF; display: flex; align-items: center; gap: 6px; }");
        styleSheet.addRule(".ep-title::before { content: '📦'; font-size: 18px; }"); // 标题前的图标
        
        // 描述样式 - 带有左侧边框和图标
        styleSheet.addRule(".ep-desc { color: #606266; font-size: 11px; line-height: 1.4; margin: 0 0 12px 0; padding: 8px; background: rgba(64, 158, 255, 0.05); border-radius: 4px; border-left: 3px solid #409EFF; position: relative; }");
        styleSheet.addRule(".ep-desc::before { content: '💡'; position: absolute; top: -4px; left: -4px; background: #409EFF; color: white; border-radius: 50%; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; font-size: 9px; }");
        
        // 章节样式 - 设置章节间距
        styleSheet.addRule(".ep-section { margin: 12px 0; }");
        // 章节标题样式 - 使用深色文字确保在IDEA背景上可见
        styleSheet.addRule(".ep-section-title { color: #2c3e50; font-size: 13px; font-weight: 600; margin: 0 0 6px 0; display: flex; align-items: center; gap: 6px; padding: 4px 0; }");
        styleSheet.addRule(".ep-section-title::before { content: ''; width: 3px; height: 16px; background: #409EFF; border-radius: 2px; }"); // 标题前的蓝色竖线
        styleSheet.addRule(".ep-section-subtitle { font-weight: 600; margin: 6px 0; color: #409EFF; font-size: 11px; }"); // 子标题样式
        
        // ==================== 表格样式设置 ====================
        // 基础表格样式 - 透明背景，无边框
        styleSheet.addRule(".ep-table { width: 100%; border-collapse: collapse; margin: 6px 0; background: transparent; border-radius: 4px; overflow: hidden; }");
        styleSheet.addRule(".ep-table th { background: #409EFF; color: white; font-weight: 600; font-size: 10px; padding: 6px 8px; text-align: left; border: none; }"); // 表头样式
        styleSheet.addRule(".ep-table td { padding: 6px 8px; font-size: 10px; line-height: 1.3; }"); // 表格单元格样式
        
        // ==================== Attributes表格列宽设置 ====================
        // 固定列宽，允许文字换行，确保内容完整显示
        styleSheet.addRule(".ep-table.attributes-table th:nth-child(1), .ep-table.attributes-table td:nth-child(1) { width: 120px; min-width: 120px; max-width: 120px; color: red !important; }"); // 参数列 - 红色突出显示
        styleSheet.addRule(".ep-table.attributes-table th:nth-child(2), .ep-table.attributes-table td:nth-child(2) { width: 300px; min-width: 300px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 说明列 - 允许换行
        styleSheet.addRule(".ep-table.attributes-table th:nth-child(3), .ep-table.attributes-table td:nth-child(3) { width: 100px; min-width: 100px; max-width: 100px; }"); // 类型列 - 固定宽度
        styleSheet.addRule(".ep-table.attributes-table th:nth-child(4), .ep-table.attributes-table td:nth-child(4) { width: 120px; min-width: 120px; max-width: 120px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 可选值列 - 允许换行
        styleSheet.addRule(".ep-table.attributes-table th:nth-child(5), .ep-table.attributes-table td:nth-child(5) { width: 100px; min-width: 100px; max-width: 100px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 默认值列 - 允许换行

        // ==================== Events表格列宽设置 ====================
        // 事件表格的列宽配置
        styleSheet.addRule(".ep-table.events-table th:nth-child(1), .ep-table.events-table td:nth-child(1) { width: 150px; min-width: 150px; max-width: 150px; }"); // 事件名称列
        styleSheet.addRule(".ep-table.events-table th:nth-child(2), .ep-table.events-table td:nth-child(2) { width: 350px; min-width: 350px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 说明列 - 允许换行
        styleSheet.addRule(".ep-table.events-table th:nth-child(3), .ep-table.events-table td:nth-child(3) { width: 200px; min-width: 200px; max-width: 200px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 回调参数列 - 允许换行
        
        // ==================== Slots表格列宽设置 ====================
        // 插槽表格的列宽配置
        styleSheet.addRule(".ep-table.slots-table th:nth-child(1), .ep-table.slots-table td:nth-child(1) { width: 150px; min-width: 150px; max-width: 150px; }"); // 插槽名列
        styleSheet.addRule(".ep-table.slots-table th:nth-child(2), .ep-table.slots-table td:nth-child(2) { width: 350px; min-width: 350px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 说明列 - 允许换行
        styleSheet.addRule(".ep-table.slots-table th:nth-child(3), .ep-table.slots-table td:nth-child(3) { width: 200px; min-width: 200px; max-width: 200px; word-wrap: break-word; word-break: break-all; white-space: normal; }"); // 作用域列 - 允许换行
        
        // ==================== 表格交互和细节样式 ====================
        styleSheet.addRule(".ep-table tr:last-child td { border-bottom: none; }"); // 最后一行不显示底部边框
        styleSheet.addRule(".ep-table tr:hover { background: rgba(64, 158, 255, 0.05); }"); // 鼠标悬停效果
        
        // ==================== 特殊元素样式 ====================
        // 组件名称样式 - 使用等宽字体，便于阅读代码
        styleSheet.addRule(".ep-name { color: #409EFF; font-weight: 600; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 10px; background: rgba(64, 158, 255, 0.1); padding: 2px 4px; border-radius: 2px; display: inline-block; }");
        styleSheet.addRule(".ep-table.attributes-table td:nth-child(1) .ep-name { color: red !important; }"); // 参数列中的名称样式 - 红色突出显示
        
        // 描述文本样式
        styleSheet.addRule(".ep-desc-text { color: #606266; font-size: 10px; line-height: 1.3; }");
        
        // 默认值标签样式
        styleSheet.addRule(".ep-default { color: #909399; font-size: 9px; background: rgba(144, 147, 153, 0.1); padding: 2px 4px; border-radius: 2px; margin-left: 4px; display: inline-block; font-weight: 500; }");
        
        // 作用域标签样式
        styleSheet.addRule(".ep-scope { color: #67C23A; font-size: 9px; background: rgba(103, 194, 58, 0.1); padding: 2px 4px; border-radius: 2px; margin-left: 4px; display: inline-block; font-weight: 500; }");
        
        // ==================== 链接和交互元素样式 ====================
        // 链接样式 - 带有悬停效果
        styleSheet.addRule(".ep-link { color: #409EFF; text-decoration: none; display: inline-flex; align-items: center; gap: 4px; padding: 4px 8px; background: rgba(64, 158, 255, 0.1); border-radius: 4px; transition: all 0.3s; font-weight: 500; font-size: 10px; }");
        styleSheet.addRule(".ep-link:hover { background: #409EFF; color: #ffffff; text-decoration: none; }"); // 悬停时变为蓝色背景
        
        // ==================== 示例代码样式 ====================
        // 示例代码容器样式
        styleSheet.addRule(".ep-example { background: rgba(64, 158, 255, 0.05); border: 1px solid #e4e7ed; border-radius: 4px; padding: 8px; margin: 8px 0; position: relative; }");
        styleSheet.addRule(".ep-example::before { content: '💻'; position: absolute; top: -4px; left: 6px; background: #409EFF; color: white; border-radius: 50%; width: 16px; height: 16px; display: flex; align-items: center; justify-content: center; font-size: 9px; }"); // 示例代码前的图标
        styleSheet.addRule(".ep-example pre { margin: 0; font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace; font-size: 9px; color: #303133; line-height: 1.3; background: rgba(255,255,255,0.8); padding: 6px; border-radius: 2px; }"); // 代码块样式
        
        // ==================== 标签样式 ====================
        // 通用标签样式
        styleSheet.addRule(".ep-badge { display: inline-block; padding: 1px 4px; border-radius: 4px; font-size: 8px; font-weight: 600; margin-left: 4px; text-transform: uppercase; letter-spacing: 0.2px; }");
        styleSheet.addRule(".ep-badge-prop { background: rgba(64, 158, 255, 0.1); color: #409EFF; border: 1px solid rgba(64, 158, 255, 0.2); }"); // 属性标签
        styleSheet.addRule(".ep-badge-event { background: rgba(103, 194, 58, 0.1); color: #67C23A; border: 1px solid rgba(103, 194, 58, 0.2); }"); // 事件标签
        styleSheet.addRule(".ep-badge-slot { background: rgba(230, 162, 60, 0.1); color: #E6A23C; border: 1px solid rgba(230, 162, 60, 0.2); }"); // 插槽标签
        
        // ==================== 页脚和动画样式 ====================
        // 页脚样式
        styleSheet.addRule(".ep-footer { margin-top: 8px; padding-top: 6px; border-top: 1px solid #e4e7ed; text-align: center; }");
        styleSheet.addRule(".ep-footer-text { color: #909399; font-size: 9px; }");
        
        // 淡入动画效果
        styleSheet.addRule("@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }");
        styleSheet.addRule(".ep-doc { animation: fadeIn 0.2s ease-out; }"); // 应用淡入动画
        
        // ==================== 设置文档内容 ====================
        // 将HTML格式的文档内容设置到编辑器中
        if (documentation != null) {
            editorPane.setText(documentation); // 设置文档内容
        } else {
            // 如果文档内容为空，显示错误提示
            editorPane.setText("<html><body><h1>无法加载文档内容</h1></body></html>");
        }
        
        // 将光标位置设置到文档开头
        editorPane.setCaretPosition(0);

        // ==================== 创建滚动面板 ====================
        // 使用JBScrollPane包装编辑器，提供滚动功能
        JBScrollPane scrollPane = new JBScrollPane(editorPane);
        
        // 设置滚动面板边框 - 使用复合边框
        scrollPane.setBorder(JBUI.Borders.compound(
            JBUI.Borders.customLine(new Color(228, 231, 237), 1), // 外边框：浅灰色线条
            JBUI.Borders.empty(5) // 内边距：5像素空白
        ));
        
        // 设置滚动面板的首选尺寸
        scrollPane.setPreferredSize(new Dimension(1980, 1680));
        
        // 使用IDEA的背景色，确保与主题保持一致
        scrollPane.setBackground(null); // 设置为null以使用IDEA默认背景色

        // 将滚动面板添加到内容面板中央
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建按钮面板
     * 
     * 创建包含三个功能按钮的底部面板：
     * - 打开官方文档：在浏览器中打开组件的官方文档
     * - 复制文档：将文档内容复制到剪贴板
     * - 关闭：关闭对话框
     * 
     * @return 配置好的按钮面板
     */
    private JPanel createButtonPanel() {
        // 创建按钮面板，使用右对齐的流式布局
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(JBUI.Borders.empty(10, 0, 0, 0)); // 设置顶部间距

        // ==================== 打开官方文档按钮 ====================
        JButton openDocButton = new JButton("🌐 打开官方文档");
        openDocButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13)); // 设置字体
        openDocButton.setBackground(new Color(64, 158, 255)); // Element Plus蓝色主题
        openDocButton.setForeground(Color.WHITE); // 白色文字
        openDocButton.setBorderPainted(false); // 不绘制边框
        openDocButton.setFocusPainted(false); // 不绘制焦点边框
        openDocButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标
        openDocButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openOfficialDocumentation(); // 点击时打开官方文档
            }
        });

        // ==================== 复制文档按钮 ====================
        JButton copyButton = new JButton("📋 复制文档");
        copyButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13)); // 设置字体
        copyButton.setBackground(new Color(103, 194, 58)); // 绿色主题
        copyButton.setForeground(Color.WHITE); // 白色文字
        copyButton.setBorderPainted(false); // 不绘制边框
        copyButton.setFocusPainted(false); // 不绘制焦点边框
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyDocumentation(); // 点击时复制文档内容
            }
        });

        // ==================== 关闭按钮 ====================
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12)); // 设置字体
        closeButton.setBackground(new Color(144, 147, 153)); // 灰色主题
        closeButton.setForeground(Color.WHITE); // 白色文字
        closeButton.setBorderPainted(false); // 不绘制边框
        closeButton.setFocusPainted(false); // 不绘制焦点边框
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 手型光标
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close(OK_EXIT_CODE); // 点击时关闭对话框
            }
        });

        // 将按钮添加到面板中（从右到左的顺序）
        panel.add(openDocButton);
        panel.add(copyButton);
        panel.add(closeButton);

        return panel;
    }

    /**
     * 打开官方文档
     * 
     * 根据组件名称生成对应的官方文档URL，并在默认浏览器中打开
     * 支持Element Plus和Ant Design Vue等主流组件库
     */
    private void openOfficialDocumentation() {
        try {
            // 根据组件名称生成对应的文档URL
            String docUrl = generateDocumentationUrl();
            
            // 检查URL是否生成成功
            if (docUrl.isEmpty()) {
                Messages.showErrorDialog("无法生成文档链接", "错误");
                return;
            }
            
            // 使用系统默认浏览器打开文档URL
            Desktop.getDesktop().browse(URI.create(docUrl));
        } catch (IOException e) {
            // 如果打开浏览器失败，显示错误信息
            Messages.showErrorDialog(
                "无法打开浏览器: " + e.getMessage(),
                "错误"
            );
        }
    }

    /**
     * 生成文档URL
     * 
     * 根据组件名称的前缀判断组件库类型，并生成对应的官方文档URL
     * 动态获取，避免硬编码特定组件库
     * 
     * @return 生成的文档URL，如果无法识别组件库类型则返回空字符串
     */
    private String generateDocumentationUrl() {
        // 优先从已安装的组件库中获取文档URL模板
        try {
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                // 检查组件是否属于该组件库
                if (isComponentFromLibrary(componentName, library)) {
                    // 如果组件库有自定义的文档URL模板，使用它
                    // 注意：ComponentLibrary 类目前没有 getDocumentationUrlTemplate 方法
                    // 这里可以后续扩展，暂时使用智能推断
                    return inferDocumentationUrl(componentName, library.getName());
                }
            }
        } catch (Exception e) {
            // 如果获取失败，使用智能推断
        }
        
        // 使用智能推断作为后备方案
        return inferDocumentationUrl(componentName, null);
    }
    
    /**
     * 检查组件是否属于指定的组件库
     */
    private boolean isComponentFromLibrary(String componentName, com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library) {
        if (componentName == null || library == null) {
            return false;
        }
        
        // 根据组件库提供的前缀或从名称智能推断，避免硬编码
        String prefix = library.getComponentPrefix();
        if (prefix == null || prefix.trim().isEmpty()) {
            String libName = library.getName();
            if (libName == null) return false;
            String[] parts = libName.split("-");
            if (parts.length > 0) {
                String first = parts[0].toLowerCase();
                prefix = first.length() >= 2 ? first.substring(0, 2) + "-" : first + "-";
            }
        }
        return prefix != null && !prefix.isEmpty() && componentName.startsWith(prefix);
    }
    
    /**
     * 推断文档URL
     */
    private String inferDocumentationUrl(String componentName, String libraryName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            return "";
        }
        
        // 移除组件前缀
        String componentKey = componentName;
        String[] prefixes = {"el-", "a-", "v-", "q-", "n-", "p-"};
        for (String prefix : prefixes) {
            if (componentName.startsWith(prefix)) {
                componentKey = componentName.substring(prefix.length());
                break;
            }
        }
        
        // 根据组件库推断文档URL（避免硬编码，统一由信息提供者推断）
        if (libraryName != null) {
            String base = com.chu7.vuecomponentassistant.utils.DynamicLibraryInfoProvider.getDocumentationBaseUrlFromName(libraryName);
            if (base != null && !base.isEmpty()) {
                return base + componentKey;
            }
        }
        
        // 默认返回空字符串
        return "";
    }

    /**
     * 复制文档内容
     * 
     * 将当前显示的文档内容复制到系统剪贴板
     * 包括选择全部内容、复制到剪贴板、重置光标位置和显示成功提示
     */
    private void copyDocumentation() {
        // 选择编辑器中的所有内容
        editorPane.selectAll();
        
        // 将选中的内容复制到系统剪贴板
        editorPane.copy();
        
        // 将光标位置重置到文档开头
        editorPane.setCaretPosition(0);
        
        // 显示复制成功的提示信息
        Messages.showInfoMessage(
            "文档内容已复制到剪贴板",
            "复制成功"
        );
    }

    /**
     * 测试编码是否正确
     * 
     * 调试方法，用于测试文档内容的编码和字体设置
     * 输出文档长度、内容预览、字体信息等调试信息
     */
    private void testEncoding() {
        System.out.println("=== 编码测试 ===");
        
        // 输出文档内容长度
        System.out.println("文档内容长度: " + (documentation != null ? documentation.length() : 0));
        
        // 输出文档内容前100个字符（用于预览）
        System.out.println("文档内容前100字符: " + (documentation != null ? documentation.substring(0, Math.min(100, documentation.length())) : "null"));
        
        // 输出当前使用的字体名称
        System.out.println("字体名称: " + editorPane.getFont().getName());
        
        // 测试字体是否支持中文字符
        System.out.println("字体是否支持中文: " + editorPane.getFont().canDisplay('中'));
    }

    /**
     * 创建默认按钮
     * 
     * 重写父类方法，返回空数组表示不使用默认的OK/Cancel按钮
     * 我们使用自定义的按钮面板来提供更丰富的功能
     * 
     * @return 空数组，表示不使用默认按钮
     */
    @Override
    protected Action[] createActions() {
        return new Action[0]; // 不使用默认按钮，使用自定义按钮面板
    }
}
