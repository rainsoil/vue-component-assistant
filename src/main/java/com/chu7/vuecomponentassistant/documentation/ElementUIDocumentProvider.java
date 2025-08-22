package com.chu7.vuecomponentassistant.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 直接复制 elementPlugin 的 ElementUIDocumentProvider 实现
 * 但返回测试内容而不是真实内容
 */
public class ElementUIDocumentProvider extends AbstractDocumentationProvider {

    static {
        System.out.println("=== ElementUIDocumentProvider 类被加载 ===");
    }

    public ElementUIDocumentProvider() {
        System.out.println("=== ElementUIDocumentProvider 实例被创建 ===");
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        System.out.println("=== ElementUIDocumentProvider.getUrlFor 被调用 ===");
        System.out.println("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        
        if (element == null) {
            return new ArrayList<>();
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
        return new ArrayList<>();
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        System.out.println("=== ElementUIDocumentProvider.generateDoc 被调用 ===");
        System.out.println("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        System.out.println("element full class name = " + (element != null ? element.getClass().getName() : "null"));
        System.out.println("element text = " + (element != null ? element.getText() : "null"));

        if (element == null) {
            System.out.println("element is null, returning null");
            return null;
        }

        try {
            // 检查是否是 HtmlTag
            if (element instanceof HtmlTag) {
                HtmlTag htmlTag = (HtmlTag) element;
                String tagName = htmlTag.getName();
                System.out.println("HtmlTag detected! 标签名: " + tagName);
                
                // 为 Element 组件提供真实的文档
                if (tagName != null && tagName.startsWith("el-")) {
                    System.out.println("=== 准备为 Element 组件生成文档: " + tagName + " ===");
                    String doc = generateComponentDocument(htmlTag.getProject(), tagName);
                    System.out.println("=== 生成的文档长度: " + (doc != null ? doc.length() : 0) + " ===");
                    return doc;
                }
            }

            // 检查是否是属性
            PsiElement parent = element.getParent();
            if (element instanceof XmlAttribute && parent instanceof HtmlTag) {
                String tagName = ((HtmlTag) parent).getName();
                String attrName = ((XmlAttribute) element).getName();
                System.out.println("检测到属性: " + attrName + " 属于标签: " + tagName);
                
                // 为属性提供真实的文档
                if (tagName != null && tagName.startsWith("el-")) {
                    return generateAttributeDocument(((HtmlTag) parent).getProject(), tagName, attrName);
                }
            }

        } catch (Exception e) {
            System.out.println("生成文档时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        // 为其他元素提供通用文档
        String result = "<h1>元素文档</h1>" +
                       "<p><strong>元素类型：</strong>" + element.getClass().getSimpleName() + "</p>" +
                       "<p><strong>元素内容：</strong>" + element.getText() + "</p>";
        
        System.out.println("返回通用文档: " + result);
        return result;
    }

    /**
     * 生成组件文档
     */
    private String generateComponentDocument(Project project, String componentName) {
        try {
            // 尝试获取组件信息，如果失败则使用备用文档
            com.chu7.vuecomponentassistant.completion2.ElementPlusComponent component = null;
            
            try {
                // 获取组件信息
                com.chu7.vuecomponentassistant.completion2.ComponentProvider componentProvider = 
                    com.chu7.vuecomponentassistant.completion2.ComponentProviderManager.getProvider(project);
                
                if (componentProvider != null) {
                    component = componentProvider.getComponent(componentName);
                }
            } catch (Exception e) {
                System.out.println("获取ComponentProvider失败: " + e.getMessage());
                // 继续使用备用文档
            }
            
            if (component == null) {
                System.out.println("Component not found: " + componentName + "，使用备用文档");
                return generateFallbackComponentDocument(componentName);
            }

            // 生成类似 elementPlugin 的详细文档
            StringBuilder sb = new StringBuilder();
            
            // 添加样式 - 参考 Element Plus 官网，使用 IDEA 背景色，整体字体调小，行间距紧凑
            sb.append("<style>");
            sb.append("body { background-color: #2b2b2b; color: #a9b7c6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 11px; }");
            sb.append("table { width: 600px; border-collapse: collapse; margin: 8px 0; border-radius: 6px; overflow: hidden; }");
            sb.append("th, td { padding: 8px 12px; text-align: left; border: none; }");
            sb.append("th { background-color: #3c3f41; font-weight: 600; color: #ffffff; font-size: 11px; }");
            sb.append("td { background-color: #2b2b2b; color: #a9b7c6; font-size: 11px; }");
            sb.append("tr:nth-child(even) td { background-color: #323232; }");
            sb.append("tr:hover td { background-color: #3c3f41; }");
            sb.append("h3 { color: #409EFF; margin: 16px 0 8px 0; font-size: 14px; font-weight: 600; }");
            sb.append("h1 { color: #ffffff; margin: 12px 0 8px 0; font-size: 18px; }");
            sb.append("p { margin: 4px 0; line-height: 1.3; color: #a9b7c6; font-size: 11px; }");
            sb.append("strong { color: #ffffff; font-size: 11px; }");
            sb.append("</style>");
            
            // 组件标题
            sb.append("<h1>").append(component.getName()).append(" 组件</h1>");
            
            // 组件描述
            if (component.getDescription() != null && !component.getDescription().toString().isEmpty()) {
                sb.append("<p><strong>描述：</strong>").append(component.getDescription()).append("</p>");
            }
            
            // Properties 表格
            if (component.getProps() != null && !component.getProps().isEmpty()) {
                sb.append("<h3>Properties</h3>");
                sb.append("<table>");
                sb.append("<thead><tr>");
                sb.append("<th style='width:80px'>属性名</th>");
                sb.append("<th style='width:120px'>描述</th>");
                sb.append("<th style='width:40px'>类型</th>");
                sb.append("<th style='width:60px'>是否可选</th>");
                sb.append("<th style='width:80px'>默认值</th>");
                sb.append("</tr></thead>");
                sb.append("<tbody>");
                
                for (com.chu7.vuecomponentassistant.completion2.ElementPlusProp prop : component.getProps()) {
                    sb.append("<tr>");
                    sb.append("<td>").append(prop.getName()).append("</td>");
                    sb.append("<td>").append(prop.getDescription() != null ? prop.getDescription() : "").append("</td>");
                    sb.append("<td>").append(prop.getType() != null ? prop.getType() : "").append("</td>");
                    sb.append("<td>").append(prop.isRequired() ? "否" : "是").append("</td>");
                    sb.append("<td>").append(prop.getDefaultValue() != null ? prop.getDefaultValue() : "").append("</td>");
                    sb.append("</tr>");
                }
                
                sb.append("</tbody></table>");
            }
            
            // Events 表格
            if (component.getEvents() != null && !component.getEvents().isEmpty()) {
                sb.append("<h3>Events</h3>");
                sb.append("<table>");
                sb.append("<thead><tr>");
                sb.append("<th style='width:120px'>事件名</th>");
                sb.append("<th style='width:120px'>描述</th>");
                sb.append("<th style='width:120px'>参数</th>");
                sb.append("</tr></thead>");
                sb.append("<tbody>");
                
                for (com.chu7.vuecomponentassistant.completion2.ElementPlusEvent event : component.getEvents()) {
                    sb.append("<tr>");
                    sb.append("<td>@").append(event.getName()).append("</td>");
                    sb.append("<td>").append(event.getDescription() != null ? event.getDescription() : "").append("</td>");
                    sb.append("<td>").append(event.getParameters() != null ? event.getParameters() : "").append("</td>");
                    sb.append("</tr>");
                }
                
                sb.append("</tbody></table>");
            }
            
            // Slots 表格
            if (component.getSlots() != null && !component.getSlots().isEmpty()) {
                sb.append("<h3>Slots</h3>");
                sb.append("<table>");
                sb.append("<thead><tr>");
                sb.append("<th style='width:120px'>插槽名</th>");
                sb.append("<th style='width:120px'>描述</th>");
                sb.append("</tr></thead>");
                sb.append("<tbody>");
                
                for (com.chu7.vuecomponentassistant.completion2.ElementPlusSlot slot : component.getSlots()) {
                    sb.append("<tr>");
                    sb.append("<td>").append(slot.getName()).append("</td>");
                    sb.append("<td>").append(slot.getDescription() != null ? slot.getDescription() : "").append("</td>");
                    sb.append("</tr>");
                }
                
                sb.append("</tbody></table>");
            }
            
            String result = sb.toString();
            System.out.println("返回真实组件文档: " + result);
            return result;
            
        } catch (Exception e) {
            System.out.println("生成组件文档时发生错误: " + e.getMessage());
            e.printStackTrace();
            return generateFallbackComponentDocument(componentName);
        }
    }

    /**
     * 生成属性文档
     */
    private String generateAttributeDocument(Project project, String componentName, String attributeName) {
        try {
            // 尝试获取组件信息，如果失败则使用备用文档
            com.chu7.vuecomponentassistant.completion2.ElementPlusComponent component = null;
            
            try {
                // 获取组件信息
                com.chu7.vuecomponentassistant.completion2.ComponentProvider componentProvider = 
                    com.chu7.vuecomponentassistant.completion2.ComponentProviderManager.getProvider(project);
                
                if (componentProvider != null) {
                    component = componentProvider.getComponent(componentName);
                }
            } catch (Exception e) {
                System.out.println("获取ComponentProvider失败: " + e.getMessage());
                // 继续使用备用文档
            }
            
            if (component == null) {
                System.out.println("Component not found: " + componentName + "，使用备用文档");
                return generateFallbackAttributeDocument(componentName, attributeName);
            }

            // 查找属性信息
            if (component.getProps() != null) {
                for (com.chu7.vuecomponentassistant.completion2.ElementPlusProp prop : component.getProps()) {
                    if (attributeName.equals(prop.getName())) {
                        StringBuilder sb = new StringBuilder();
                        
                        // 添加样式 - 参考 Element Plus 官网，使用 IDEA 背景色，整体字体调小，行间距紧凑
                        sb.append("<style>");
                        sb.append("body { background-color: #2b2b2b; color: #a9b7c6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 11px; }");
                        sb.append("table { width: 600px; border-collapse: collapse; margin: 8px 0; border-radius: 6px; overflow: hidden; }");
                        sb.append("th, td { padding: 8px 12px; text-align: left; border: none; }");
                        sb.append("th { background-color: #3c3f41; font-weight: 600; color: #ffffff; font-size: 11px; }");
                        sb.append("td { background-color: #2b2b2b; color: #a9b7c6; font-size: 11px; }");
                        sb.append("tr:nth-child(even) td { background-color: #323232; }");
                        sb.append("tr:hover td { background-color: #3c3f41; }");
                        sb.append("h1 { color: #ffffff; margin: 12px 0 8px 0; font-size: 18px; }");
                        sb.append("p { margin: 4px 0; line-height: 1.3; color: #a9b7c6; font-size: 11px; }");
                        sb.append("strong { color: #ffffff; font-size: 11px; }");
                        sb.append("</style>");
                        
                        sb.append("<h1>").append(attributeName).append(" 属性</h1>");
                        sb.append("<p><strong>所属组件：</strong>").append(componentName).append("</p>");
                        
                        // 属性详细信息表格
                        sb.append("<table>");
                        sb.append("<thead><tr>");
                        sb.append("<th style='width:80px'>属性名</th>");
                        sb.append("<th style='width:120px'>描述</th>");
                        sb.append("<th style='width:40px'>类型</th>");
                        sb.append("<th style='width:60px'>是否可选</th>");
                        sb.append("<th style='width:80px'>默认值</th>");
                        sb.append("</tr></thead>");
                        sb.append("<tbody><tr>");
                        
                        sb.append("<td>").append(attributeName).append("</td>");
                        sb.append("<td>").append(prop.getDescription() != null ? prop.getDescription() : "").append("</td>");
                        sb.append("<td>").append(prop.getType() != null ? prop.getType() : "").append("</td>");
                        sb.append("<td>").append(prop.isRequired() ? "否" : "是").append("</td>");
                        sb.append("<td>").append(prop.getDefaultValue() != null ? prop.getDefaultValue() : "").append("</td>");
                        sb.append("</tr></tbody></table>");
                        
                        String result = sb.toString();
                        System.out.println("返回真实属性文档: " + result);
                        return result;
                    }
                }
            }

            // 查找事件信息
            if (component.getEvents() != null) {
                for (com.chu7.vuecomponentassistant.completion2.ElementPlusEvent event : component.getEvents()) {
                    if (attributeName.equals(event.getName())) {
                        StringBuilder sb = new StringBuilder();
                        
                        // 添加样式 - 参考 Element Plus 官网，使用 IDEA 背景色，整体字体调小，行间距紧凑
                        sb.append("<style>");
                        sb.append("body { background-color: #2b2b2b; color: #a9b7c6; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; font-size: 11px; }");
                        sb.append("table { width: 600px; border-collapse: collapse; margin: 8px 0; border-radius: 6px; overflow: hidden; }");
                        sb.append("th, td { padding: 8px 12px; text-align: left; border: none; }");
                        sb.append("th { background-color: #3c3f41; font-weight: 600; color: #ffffff; font-size: 11px; }");
                        sb.append("td { background-color: #2b2b2b; color: #a9b7c6; font-size: 11px; }");
                        sb.append("tr:nth-child(even) td { background-color: #323232; }");
                        sb.append("tr:hover td { background-color: #3c3f41; }");
                        sb.append("h1 { color: #ffffff; margin: 12px 0 8px 0; font-size: 18px; }");
                        sb.append("p { margin: 4px 0; line-height: 1.3; color: #a9b7c6; font-size: 11px; }");
                        sb.append("strong { color: #ffffff; font-size: 11px; }");
                        sb.append("</style>");
                        
                        sb.append("<h1>@").append(attributeName).append(" 事件</h1>");
                        sb.append("<p><strong>所属组件：</strong>").append(componentName).append("</p>");
                        
                        // 事件详细信息表格
                        sb.append("<table>");
                        sb.append("<thead><tr>");
                        sb.append("<th style='width:120px'>事件名</th>");
                        sb.append("<th style='width:120px'>描述</th>");
                        sb.append("<th style='width:120px'>参数</th>");
                        sb.append("</tr></thead>");
                        sb.append("<tbody><tr>");
                        
                        sb.append("<td>@").append(attributeName).append("</td>");
                        sb.append("<td>").append(event.getDescription() != null ? event.getDescription() : "").append("</td>");
                        sb.append("<td>").append(event.getParameters() != null ? event.getParameters() : "").append("</td>");
                        sb.append("</tr></tbody></table>");
                        
                        String result = sb.toString();
                        System.out.println("返回真实事件文档: " + result);
                        return result;
                    }
                }
            }
            
            return generateFallbackAttributeDocument(componentName, attributeName);
            
        } catch (Exception e) {
            System.out.println("生成属性文档时发生错误: " + e.getMessage());
            e.printStackTrace();
            return generateFallbackAttributeDocument(componentName, attributeName);
        }
    }

    /**
     * 生成备用组件文档
     */
    private String generateFallbackComponentDocument(String componentName) {
        return "<h1>" + componentName + " 组件</h1>" +
               "<p><strong>组件名称：</strong>" + componentName + "</p>" +
               "<p><strong>组件类型：</strong>Element Plus</p>" +
               "<p><strong>描述：</strong>这是一个 Element Plus 组件，提供丰富的 UI 功能。</p>" +
               "<p><strong>使用方式：</strong>&lt;" + componentName + "&gt;...&lt;/" + componentName + "&gt;</p>";
    }

    /**
     * 生成备用属性文档
     */
    private String generateFallbackAttributeDocument(String componentName, String attributeName) {
        return "<h1>" + attributeName + " 属性</h1>" +
               "<p><strong>所属组件：</strong>" + componentName + "</p>" +
               "<p><strong>属性名称：</strong>" + attributeName + "</p>" +
               "<p><strong>属性类型：</strong>Element Plus 属性</p>" +
               "<p><strong>使用方式：</strong>" + componentName + " " + attributeName + "=\"值\"</p>";
    }
}
