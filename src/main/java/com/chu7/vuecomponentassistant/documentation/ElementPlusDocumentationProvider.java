package com.chu7.vuecomponentassistant.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlTag;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion.ElementPlusSlot;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Element Plus 组件文档提供者
 * 
 * 功能说明：
 * - 当鼠标悬浮在 Element Plus 组件上时，显示详细的组件信息
 * - 包括组件介绍、属性列表、事件列表、插槽列表、文档链接等
 * - 提供完整的中文描述和示例代码
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ElementPlusDocumentationProvider extends AbstractDocumentationProvider {

    /** 组件数据提供者，用于获取组件详细信息 */
    private ComponentProvider componentProvider;

    /**
     * 构造函数
     * 组件提供者将在 generateDoc 中根据项目动态创建
     */
    public ElementPlusDocumentationProvider() {
        // 组件提供者将在 generateDoc 中根据项目动态创建
        System.out.println("ElementPlusDocumentationProvider 已初始化");
    }

    /**
     * 生成文档内容
     * 
     * @param element 鼠标悬浮的元素
     * @return 格式化的 HTML 文档内容
     */
    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        // 检查悬停文档设置
        PluginSettings settings = PluginSettings.getInstance();
        if (!settings.isEnableHoverDocumentation()) {
            System.out.println("悬停文档功能已禁用");
            return null; // 返回 null 表示不显示文档
        }
        
        // 添加调试信息
        System.out.println("=== 文档提供者被调用 ===");
        System.out.println("元素类型: " + (element != null ? element.getClass().getSimpleName() : "null"));
        System.out.println("元素文本: " + (element != null ? element.getText() : "null"));
        
        // 获取当前项目
        Project project = element != null ? element.getProject() : null;
        if (project == null) {
            System.out.println("无法获取项目信息，返回测试文档");
            return generateTestDocumentation(element);
        }

        // 根据项目动态创建组件提供者
        if (componentProvider == null) {
            componentProvider = new ComponentProvider(project);
        }
        
        // 尝试从不同元素类型中提取组件名称
        String componentName = extractComponentName(element);
        if (componentName == null) {
            System.out.println("无法提取组件名称，返回测试文档");
            return generateTestDocumentation(element);
        }
        
        System.out.println("提取到组件名称: " + componentName);

        // 检查是否是当前组件库的组件
        if (!componentProvider.isComponentFromCurrentLibrary(componentName)) {
            System.out.println("不是 " + componentProvider.getLibraryDisplayName() + " 组件，返回测试文档");
            return generateTestDocumentation(element);
        }

        System.out.println("检测到 " + componentProvider.getLibraryDisplayName() + " 组件: " + componentName);

        // 获取组件详细信息
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            System.out.println("找不到组件信息: " + componentName + "，返回测试文档");
            return generateTestDocumentation(element);
        }

        System.out.println("找到组件信息，生成文档");
        // 生成完整的文档内容
        return generateComponentDocumentation(component);
    }

    /**
     * 生成测试文档，确保功能被调用
     */
    private String generateTestDocumentation(PsiElement element) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; padding: 10px;'>");
        html.append("<h3 style='color: #409EFF;'>🧪 测试文档</h3>");
        html.append("<p>这是一个测试文档，证明文档提供者正在工作。</p>");
        html.append("<p><strong>元素类型:</strong> " + element.getClass().getSimpleName() + "</p>");
        html.append("<p><strong>元素文本:</strong> " + element.getText() + "</p>");
        html.append("<p style='color: #67C23A;'>✅ 鼠标悬浮功能已激活！</p>");
        html.append("<hr style='margin: 15px 0;'>");
        html.append("<h4 style='color: #303133;'>📚 功能说明</h4>");
        html.append("<ul style='color: #606266;'>");
        html.append("<li>鼠标悬浮显示组件文档</li>");
        html.append("<li>右键菜单查看详细文档</li>");
        html.append("<li>智能补全和提示</li>");
        html.append("</ul>");
        html.append("</div>");
        return html.toString();
    }

    /**
     * 从不同的元素类型中提取组件名称
     * 
     * @param element PSI 元素
     * @return 组件名称，如果无法提取则返回 null
     */
    private String extractComponentName(PsiElement element) {
        if (element == null) {
            return null;
        }

        // 如果是 XML 标签
        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            return tag.getName();
        }

        // 如果是文本元素，尝试解析
        String text = element.getText();
        if (text != null && text.contains("<")) {
            // 简单的标签解析
            int start = text.indexOf('<');
            int end = text.indexOf(' ', start);
            if (end == -1) {
                end = text.indexOf('>', start);
            }
            if (start >= 0 && end > start) {
                String tagName = text.substring(start + 1, end).trim();
                System.out.println("从文本解析到标签: " + tagName);
                return tagName;
            }
        }

        // 检查父元素
        PsiElement parent = element.getParent();
        if (parent != null && parent != element) {
            return extractComponentName(parent);
        }

        return null;
    }



    /**
     * 生成组件文档内容
     * 
     * @param component 组件信息
     * @return 格式化的 HTML 文档内容
     */
    private String generateComponentDocumentation(ElementPlusComponent component) {
        return DocumentationStyleGenerator.generateHtmlDocumentation(component);
    }
}
