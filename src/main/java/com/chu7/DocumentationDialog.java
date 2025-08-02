package com.chu7;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 文档显示对话框
 */
public class DocumentationDialog extends DialogWrapper {

    private final PsiElement element;
    private final Project project;
    private JTextPane contentPane;

    public DocumentationDialog(Project project, PsiElement element) {
        super(project);
        this.project = project;
        this.element = element;
        setTitle("组件文档");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(600, 400));

        contentPane = new JTextPane();
        contentPane.setContentType("text/html");
        contentPane.setEditable(false);
        contentPane.setBackground(Color.WHITE);

        loadDocumentation();

        JScrollPane scrollPane = new JScrollPane(contentPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadDocumentation() {
        if (element == null) {
            contentPane.setText("<html><body><h3>错误</h3><p>无法获取文档信息</p></body></html>");
            return;
        }

        List<ComponentMeta> components = ElementPlusDocumentationProvider.loadComponentsStatic();
        String documentation = "";

        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            String tagName = tag.getName();
            ComponentMeta component = findComponentByName(tagName, components);
            if (component != null) {
                documentation = generateComponentDoc(component);
            }
        } else if (element instanceof XmlAttribute) {
            XmlAttribute attribute = (XmlAttribute) element;
            String attributeName = attribute.getName();
            XmlTag parentTag = attribute.getParent();

            if (parentTag != null) {
                String tagName = parentTag.getName();
                ComponentMeta component = findComponentByName(tagName, components);

                if (component != null) {
                    if (attributeName.startsWith("@")) {
                        String eventName = attributeName.substring(1);
                        ComponentMeta.ComponentEvent event = findEventByName(eventName, component);
                        if (event != null) {
                            documentation = generateEventDoc(event);
                        }
                    } else if (attributeName.startsWith(":")) {
                        String propName = attributeName.substring(1);
                        ComponentMeta.ComponentProp prop = findPropertyByName(propName, component);
                        if (prop != null) {
                            documentation = generatePropertyDoc(prop);
                        }
                    } else if (attributeName.startsWith("slot")) {
                        // 处理slot属性
                        String slotName = attributeName;
                        if (attributeName.startsWith("slot:")) {
                            slotName = attributeName.substring(5); // 移除 "slot:" 前缀
                        }
                        ComponentMeta.ComponentSlot slot = findSlotByName(slotName, component);
                        if (slot != null) {
                            documentation = generateSlotDoc(slot);
                        }
                    } else {
                        ComponentMeta.ComponentProp prop = findPropertyByName(attributeName, component);
                        if (prop != null) {
                            documentation = generatePropertyDoc(prop);
                        }
                    }
                }
            }
        }

        if (documentation.isEmpty()) {
            documentation = "<html><body><h3>未找到文档</h3><p>该元素没有可用的文档信息</p></body></html>";
        }

        contentPane.setText(documentation);
    }

    private ComponentMeta findComponentByName(String name, List<ComponentMeta> components) {
        if (name == null || components == null) return null;
        for (ComponentMeta component : components) {
            if (name.equals(component.name)) {
                return component;
            }
        }
        return null;
    }

    private ComponentMeta.ComponentProp findPropertyByName(String name, ComponentMeta component) {
        if (name == null || component.props == null) return null;
        if (name.startsWith(":")) {
            name = name.substring(1);
        }
        for (ComponentMeta.ComponentProp prop : component.props) {
            if (name.equals(prop.name)) {
                return prop;
            }
        }
        return null;
    }

    private ComponentMeta.ComponentEvent findEventByName(String name, ComponentMeta component) {
        if (name == null || component.events == null) return null;
        if (name.startsWith("@")) {
            name = name.substring(1);
        }
        for (ComponentMeta.ComponentEvent event : component.events) {
            if (name.equals(event.name)) {
                return event;
            }
        }
        return null;
    }

    private ComponentMeta.ComponentSlot findSlotByName(String name, ComponentMeta component) {
        if (name == null || component.slots == null) return null;
        for (ComponentMeta.ComponentSlot slot : component.slots) {
            if (name.equals(slot.name)) {
                return slot;
            }
        }
        return null;
    }

    private String generateComponentDoc(ComponentMeta component) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body style='font-family: Arial, sans-serif;'>");

        // 组件名称和版本
        doc.append("<h3 style='color: #409EFF; margin-bottom: 10px;'>").append(component.name).append("</h3>");
        if (component.version != null) {
            doc.append("<p style='color: #666; margin-bottom: 10px;'><strong>版本:</strong> ").append(component.version).append("</p>");
        }

        // 组件描述
        if (component.description != null) {
            doc.append("<p style='margin-bottom: 15px;'>").append(component.description).append("</p>");
        }

        // 使用示例
        if (component.example != null) {
            doc.append("<div style='background-color: #f5f5f5; padding: 10px; border-radius: 4px; margin-bottom: 15px;'>");
            doc.append("<strong>使用示例:</strong><br>");
            doc.append("<code style='color: #333;'>").append(component.example).append("</code>");
            doc.append("</div>");
        }

        // 文档链接
        if (component.docUrl != null) {
            doc.append("<p style='margin-bottom: 10px;'>");
            doc.append("<a href='").append(component.docUrl).append("' style='color: #409EFF; text-decoration: none;'>");
            doc.append("📖 查看官方文档");
            doc.append("</a>");
            doc.append("</p>");
        }

        // 属性列表
        if (component.props != null && !component.props.isEmpty()) {
            doc.append("<h4 style='color: #333; margin-bottom: 10px;'>属性列表:</h4>");
            doc.append("<ul style='margin-bottom: 15px;'>");
            for (ComponentMeta.ComponentProp prop : component.props) {
                doc.append("<li><strong>").append(prop.name).append("</strong>");
                if (prop.type != null) {
                    doc.append(" (").append(prop.type).append(")");
                }
                if (prop.description != null) {
                    doc.append(": ").append(prop.description);
                }
                if (prop.defaultValue != null) {
                    doc.append(" <em style='color: #666;'>[默认: ").append(prop.defaultValue).append("]</em>");
                }
                doc.append("</li>");
            }
            doc.append("</ul>");
        }

        // 事件列表
        if (component.events != null && !component.events.isEmpty()) {
            doc.append("<h4 style='color: #333; margin-bottom: 10px;'>事件列表:</h4>");
            doc.append("<ul style='margin-bottom: 15px;'>");
            for (ComponentMeta.ComponentEvent event : component.events) {
                doc.append("<li><strong>@").append(event.name).append("</strong>");
                if (event.description != null) {
                    doc.append(": ").append(event.description);
                }
                if (event.parameters != null && !event.parameters.isEmpty()) {
                    doc.append(" <em style='color: #666;'>[参数: ").append(event.parameters).append("]</em>");
                }
                doc.append("</li>");
            }
            doc.append("</ul>");
        }

        // 卡槽列表
        if (component.slots != null && !component.slots.isEmpty()) {
            doc.append("<h4 style='color: #333; margin-bottom: 10px;'>卡槽列表:</h4>");
            doc.append("<ul style='margin-bottom: 15px;'>");
            for (ComponentMeta.ComponentSlot slot : component.slots) {
                doc.append("<li><strong>slot: ").append(slot.name).append("</strong>");
                if (slot.description != null) {
                    doc.append(": ").append(slot.description);
                }
                if (slot.scope != null && !slot.scope.isEmpty()) {
                    doc.append(" <em style='color: #666;'>[作用域: ").append(slot.scope).append("]</em>");
                }
                doc.append("</li>");
            }
            doc.append("</ul>");
        }

        doc.append("</body></html>");
        return doc.toString();
    }

    private String generatePropertyDoc(ComponentMeta.ComponentProp prop) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body style='font-family: Arial, sans-serif;'>");
        doc.append("<h3 style='color: #409EFF; margin-bottom: 10px;'>").append(prop.name).append("</h3>");
        if (prop.type != null) {
            doc.append("<p style='color: #666; margin-bottom: 10px;'><strong>类型:</strong> ").append(prop.type).append("</p>");
        }
        if (prop.description != null) {
            doc.append("<p style='margin-bottom: 15px;'>").append(prop.description).append("</p>");
        }
        if (prop.defaultValue != null) {
            doc.append("<p style='margin-bottom: 10px;'><strong>默认值:</strong> <code>").append(prop.defaultValue).append("</code></p>");
        }
        doc.append("</body></html>");
        return doc.toString();
    }

    private String generateEventDoc(ComponentMeta.ComponentEvent event) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body style='font-family: Arial, sans-serif;'>");
        doc.append("<h3 style='color: #409EFF; margin-bottom: 10px;'>@").append(event.name).append("</h3>");
        if (event.description != null) {
            doc.append("<p style='margin-bottom: 15px;'>").append(event.description).append("</p>");
        }
        if (event.parameters != null && !event.parameters.isEmpty()) {
            doc.append("<h4 style='color: #333; margin-bottom: 10px;'>参数:</h4>");
            doc.append("<ul style='margin-bottom: 15px;'>");
            doc.append("<li><strong>").append(event.parameters).append("</strong>");
            doc.append("</ul>");
        }
        doc.append("</body></html>");
        return doc.toString();
    }

    private String generateSlotDoc(ComponentMeta.ComponentSlot slot) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html><body style='font-family: Arial, sans-serif;'>");
        doc.append("<h3 style='color: #409EFF; margin-bottom: 10px;'>slot: ").append(slot.name).append("</h3>");
        if (slot.description != null) {
            doc.append("<p style='margin-bottom: 15px;'>").append(slot.description).append("</p>");
        }
        if (slot.scope != null && !slot.scope.isEmpty()) {
            doc.append("<h4 style='color: #333; margin-bottom: 10px;'>作用域:</h4>");
            doc.append("<p style='margin-bottom: 15px;'>").append(slot.scope).append("</p>");
        }
        doc.append("<div style='background-color: #f5f5f5; padding: 10px; border-radius: 4px;'>");
        doc.append("<strong>使用示例:</strong><br>");
        doc.append("<code style='color: #333;'>slot=\"").append(slot.name).append("\"</code>");
        doc.append("</div>");
        doc.append("</body></html>");
        return doc.toString();
    }
} 