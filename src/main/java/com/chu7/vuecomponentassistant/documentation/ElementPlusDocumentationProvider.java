package com.chu7.vuecomponentassistant.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusContextAnalyzer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Element Plus 文档提供者
 * 为Element Plus组件提供详细的文档信息
 */
public class ElementPlusDocumentationProvider extends AbstractDocumentationProvider {
    
    private final ElementPlusComponentProvider componentProvider;
    private final ElementPlusContextAnalyzer contextAnalyzer;
    
    public ElementPlusDocumentationProvider() {
        this.componentProvider = new ElementPlusComponentProvider();
        this.contextAnalyzer = new ElementPlusContextAnalyzer();
    }
    
    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element == null || !contextAnalyzer.isInVueTemplate(element)) {
            return null;
        }
        
        // 获取当前组件名称
        String componentName = contextAnalyzer.getCurrentComponent(element);
        if (componentName == null) {
            return null;
        }
        
        // 获取组件信息
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return null;
        }
        
        return generateComponentDocumentation(component);
    }
    
    /**
     * 生成组件文档
     */
    private String generateComponentDocumentation(ElementPlusComponent component) {
        StringBuilder doc = new StringBuilder();
        
        // 组件标题
        doc.append("<h2>").append(component.getName()).append("</h2>");
        doc.append("<p><b>").append(component.getDescription()).append("</b></p>");
        
        // 版本信息
        if (component.getVersion() != null && !component.getVersion().isEmpty()) {
            doc.append("<p><b>版本:</b> ").append(component.getVersion()).append("</p>");
        }
        
        // 示例代码
        if (component.getExample() != null && !component.getExample().isEmpty()) {
            doc.append("<h3>示例:</h3>");
            doc.append("<pre><code>").append(escapeHtml(component.getExample())).append("</code></pre>");
        }
        
        // 属性列表
        if (component.getProps() != null && !component.getProps().isEmpty()) {
            doc.append("<h3>属性:</h3>");
            doc.append("<table border='1' cellpadding='5' cellspacing='0'>");
            doc.append("<tr><th>属性名</th><th>类型</th><th>默认值</th><th>必填</th><th>描述</th></tr>");
            
            for (var prop : component.getProps()) {
                doc.append("<tr>");
                doc.append("<td><code>").append(prop.getName()).append("</code></td>");
                doc.append("<td>").append(prop.getType()).append("</td>");
                                 doc.append("<td>").append(prop.getDefaultValueAsString()).append("</td>");
                doc.append("<td>").append(prop.isRequired() ? "是" : "否").append("</td>");
                doc.append("<td>").append(prop.getDescription()).append("</td>");
                doc.append("</tr>");
            }
            doc.append("</table>");
        }
        
        // 事件列表
        if (component.getEvents() != null && !component.getEvents().isEmpty()) {
            doc.append("<h3>事件:</h3>");
            doc.append("<table border='1' cellpadding='5' cellspacing='0'>");
            doc.append("<tr><th>事件名</th><th>参数</th><th>描述</th></tr>");
            
            for (var event : component.getEvents()) {
                doc.append("<tr>");
                doc.append("<td><code>@").append(event.getName()).append("</code></td>");
                doc.append("<td>").append(event.getParameters() != null ? event.getParameters() : "-").append("</td>");
                doc.append("<td>").append(event.getDescription()).append("</td>");
                doc.append("</tr>");
            }
            doc.append("</table>");
        }
        
        // 插槽列表
        if (component.getSlots() != null && !component.getSlots().isEmpty()) {
            doc.append("<h3>插槽:</h3>");
            doc.append("<table border='1' cellpadding='5' cellspacing='0'>");
            doc.append("<tr><th>插槽名</th><th>描述</th></tr>");
            
            for (var slot : component.getSlots()) {
                doc.append("<tr>");
                doc.append("<td><code>#").append(slot.getName()).append("</code></td>");
                doc.append("<td>").append(slot.getDescription()).append("</td>");
                doc.append("</tr>");
            }
            doc.append("</table>");
        }
        
        // 文档链接
        if (component.getDocUrl() != null && !component.getDocUrl().isEmpty()) {
            doc.append("<p><a href='").append(component.getDocUrl()).append("'>查看官方文档</a></p>");
        }
        
        return doc.toString();
    }
    
    /**
     * 转义HTML字符
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    @Override
    public @Nullable PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        // 为查找项提供文档元素
        return element;
    }
    
    @Override
    public @Nullable PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        // 为链接提供文档元素
        return context;
    }
}
