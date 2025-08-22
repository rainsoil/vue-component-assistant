package com.chu7.vuecomponentassistant.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 模仿 elementPlugin 风格的文档提供者
 */
public class ElementStyleDocumentationProvider extends AbstractDocumentationProvider {

    static {
        System.out.println("=== ElementStyleDocumentationProvider 类被加载 ===");
    }

    public ElementStyleDocumentationProvider() {
        System.out.println("=== ElementStyleDocumentationProvider 实例被创建 ===");
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        System.out.println("=== ElementStyleDocumentationProvider.getUrlFor 被调用 ===");
        System.out.println("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        
        if (element == null) {
            return Collections.emptyList();
        }

        PsiElement parent = element.getParent();
        
        String name = null;
        if (element instanceof XmlAttribute && parent instanceof HtmlTag) {
            System.out.println("检测到XmlAttribute，父元素是HtmlTag");
            name = ((XmlAttribute) element).getName();
        } else if (element instanceof HtmlTag) {
            System.out.println("检测到HtmlTag: " + ((HtmlTag) element).getName());
            name = ((HtmlTag) element).getName();
        }

        System.out.println("提取的名称: " + name);
        return Collections.emptyList();
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        System.out.println("=== ElementStyleDocumentationProvider.generateDoc 被调用 ===");
        System.out.println("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        System.out.println("element full class name = " + (element != null ? element.getClass().getName() : "null"));
        System.out.println("element text = " + (element != null ? element.getText() : "null"));
        System.out.println("originalElement = " + (originalElement != null ? originalElement.getClass().getSimpleName() : "null"));

        if (element == null) {
            System.out.println("element is null, returning null");
            return null;
        }

        // 检查是否是 HtmlTag
        if (element instanceof com.intellij.psi.html.HtmlTag) {
            com.intellij.psi.html.HtmlTag htmlTag = (com.intellij.psi.html.HtmlTag) element;
            String tagName = htmlTag.getName();
            System.out.println("HtmlTag detected! 标签名: " + tagName);
            
            // 专门处理 el-table 等父标签
            if (tagName != null && tagName.startsWith("el-")) {
                String result = "<h1>Element组件文档</h1><p>组件名称: " + tagName + "</p>" +
                               "<p>这是一个Element Plus组件</p>" +
                               "<p>元素类型: " + element.getClass().getSimpleName() + "</p>";
                System.out.println("返回Element组件文档: " + result);
                return result;
            }
        }

        // 检查父元素
        PsiElement parent = element.getParent();
        if (parent != null) {
            System.out.println("检查父元素: " + parent.getClass().getSimpleName() + ", text: " + parent.getText());
            if (parent instanceof com.intellij.psi.html.HtmlTag) {
                String parentTagName = ((com.intellij.psi.html.HtmlTag) parent).getName();
                System.out.println("父元素是HtmlTag: " + parentTagName);
                if (parentTagName != null && parentTagName.startsWith("el-")) {
                    String result = "<h1>Element组件文档(通过父元素)</h1><p>组件名称: " + parentTagName + "</p>" +
                                   "<p>这是一个Element Plus组件</p>" +
                                   "<p>元素类型: " + parent.getClass().getSimpleName() + "</p>";
                    System.out.println("通过父元素返回Element组件文档: " + result);
                    return result;
                }
            }
        }

        // 强制返回文档，不管什么元素类型
        String result = "<h1>强制测试文档</h1><p>元素类型: " + element.getClass().getSimpleName() + "</p>" +
                       "<p>元素文本: " + element.getText() + "</p>";
        
        System.out.println("强制返回文档: " + result);
        return result;
    }
}
