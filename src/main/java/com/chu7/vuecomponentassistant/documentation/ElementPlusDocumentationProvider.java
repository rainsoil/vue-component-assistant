package com.chu7.vuecomponentassistant.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlTag;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion.ElementPlusSlot;
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
 * @author Vue Component Assistant Team
 * @version 1.0.0
 */
public class ElementPlusDocumentationProvider extends AbstractDocumentationProvider {

    /** 组件数据提供者，用于获取组件详细信息 */
    private final ElementPlusComponentProvider componentProvider;

    /**
     * 构造函数
     * 初始化组件数据提供者
     */
    public ElementPlusDocumentationProvider() {
        this.componentProvider = new ElementPlusComponentProvider();
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
        // 添加调试信息
        System.out.println("=== 文档提供者被调用 ===");
        System.out.println("元素类型: " + (element != null ? element.getClass().getSimpleName() : "null"));
        System.out.println("元素文本: " + (element != null ? element.getText() : "null"));
        
        // 尝试从不同元素类型中提取组件名称
        String componentName = extractComponentName(element);
        if (componentName == null) {
            System.out.println("无法提取组件名称，返回测试文档");
            return generateTestDocumentation(element);
        }
        
        System.out.println("提取到组件名称: " + componentName);

        // 检查是否是 Element Plus 组件
        if (!isElementPlusComponent(componentName)) {
            System.out.println("不是 Element Plus 组件，返回测试文档");
            return generateTestDocumentation(element);
        }

        System.out.println("检测到 Element Plus 组件: " + componentName);

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
     * 检查是否是 Element Plus 组件
     * 
     * @param tagName 标签名称
     * @return 如果是 Element Plus 组件返回 true，否则返回 false
     */
    private boolean isElementPlusComponent(String tagName) {
        boolean isElementPlus = tagName != null && tagName.startsWith("el-");
        System.out.println("检查组件: " + tagName + " -> " + isElementPlus);
        return isElementPlus;
    }

    /**
     * 生成组件文档内容
     * 
     * @param component 组件信息
     * @return 格式化的 HTML 文档内容
     */
    private String generateComponentDocumentation(ElementPlusComponent component) {
        StringBuilder html = new StringBuilder();
        
        // 添加 CSS 样式
        html.append("<style>");
        html.append(".component-doc { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }");
        html.append(".component-title { color: #409EFF; font-size: 18px; font-weight: bold; margin-bottom: 10px; }");
        html.append(".component-desc { color: #606266; margin-bottom: 15px; line-height: 1.5; }");
        html.append(".section-title { color: #303133; font-size: 14px; font-weight: bold; margin: 15px 0 8px 0; }");
        html.append(".prop-item { margin: 5px 0; padding: 5px; background: #f5f7fa; border-radius: 3px; }");
        html.append(".prop-name { color: #409EFF; font-weight: bold; }");
        html.append(".prop-desc { color: #606266; margin-left: 10px; }");
        html.append(".doc-link { color: #409EFF; text-decoration: none; }");
        html.append(".doc-link:hover { text-decoration: underline; }");
        html.append("</style>");

        // 开始文档内容
        html.append("<div class='component-doc'>");
        
        // 组件标题
        html.append("<div class='component-title'>").append(component.getName()).append("</div>");
        
        // 组件描述
        if (component.getDescription() != null && !component.getDescription().isEmpty()) {
            html.append("<div class='component-desc'>").append(component.getDescription()).append("</div>");
        }

        // 属性列表
        List<ElementPlusProp> props = component.getProps();
        if (props != null && !props.isEmpty()) {
            html.append("<div class='section-title'>📋 属性列表</div>");
            for (ElementPlusProp prop : props) {
                html.append("<div class='prop-item'>");
                html.append("<span class='prop-name'>").append(prop.getName()).append("</span>");
                if (prop.getDescription() != null && !prop.getDescription().isEmpty()) {
                    html.append("<span class='prop-desc'>").append(prop.getDescription()).append("</span>");
                }
                if (prop.getDefaultValue() != null) {
                    html.append("<span class='prop-desc'> (默认值: ").append(prop.getDefaultValueAsString()).append(")</span>");
                }
                html.append("</div>");
            }
        }

        // 事件列表
        List<ElementPlusEvent> events = component.getEvents();
        if (events != null && !events.isEmpty()) {
            html.append("<div class='section-title'>🎯 事件列表</div>");
            for (ElementPlusEvent event : events) {
                html.append("<div class='prop-item'>");
                html.append("<span class='prop-name'>@").append(event.getName()).append("</span>");
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    html.append("<span class='prop-desc'>").append(event.getDescription()).append("</span>");
                }
                html.append("</div>");
            }
        }

        // 插槽列表
        List<ElementPlusSlot> slots = component.getSlots();
        if (slots != null && !slots.isEmpty()) {
            html.append("<div class='section-title'>🔌 插槽列表</div>");
            for (ElementPlusSlot slot : slots) {
                html.append("<div class='prop-item'>");
                html.append("<span class='prop-name'>#").append(slot.getName()).append("</span>");
                if (slot.getDescription() != null && !slot.getDescription().isEmpty()) {
                    html.append("<span class='prop-desc'>").append(slot.getDescription()).append("</span>");
                }
                if (slot.getScope() != null && !slot.getScope().isEmpty()) {
                    html.append("<span class='prop-desc'> (作用域: ").append(slot.getScope()).append(")</span>");
                }
                html.append("</div>");
            }
        }

        // 文档链接
        html.append("<div class='section-title'>📖 相关文档</div>");
        html.append("<div class='prop-item'>");
        html.append("<a href='https://element-plus.org/zh-CN/component/").append(component.getName().substring(3)).append(".html' class='doc-link' target='_blank'>");
        html.append("📖 查看官方文档");
        html.append("</a>");
        html.append("</div>");

        // 使用示例
        html.append("<div class='section-title'>💡 使用示例</div>");
        html.append("<div class='prop-item'>");
        html.append("<pre style='background: #f5f7fa; padding: 10px; border-radius: 3px; margin: 0;'>");
        html.append("&lt;").append(component.getName()).append("&gt;");
        html.append("  &lt;!-- 组件内容 --&gt;");
        html.append("&lt;/").append(component.getName()).append("&gt;");
        html.append("</pre>");
        html.append("</div>");

        html.append("</div>");
        
        return html.toString();
    }
}
