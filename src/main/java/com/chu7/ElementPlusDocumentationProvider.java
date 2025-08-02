package com.chu7;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Element Plus 文档提供者
 * 负责在鼠标悬停时显示组件、属性和事件的详细信息
 */
public class ElementPlusDocumentationProvider extends AbstractDocumentationProvider {

    private static final Gson gson = new Gson();
    private static List<ComponentMeta> cachedComponents = null;

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        // 添加调试信息
        System.out.println("ElementPlusDocumentationProvider.generateDoc called");
        System.out.println("Element type: " + (element != null ? element.getClass().getSimpleName() : "null"));
        if (element instanceof XmlAttribute) {
            XmlAttribute attr = (XmlAttribute) element;
            System.out.println("Attribute name: " + attr.getName());
            System.out.println("Attribute value: " + attr.getValue());
        }
        
        if (element == null) return null;

        Project project = element.getProject();
        if (project == null) return null;

        // 加载组件数据
        List<ComponentMeta> components = loadComponents();
        // 处理组件标签
        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            String tagName = tag.getName();
            ComponentMeta component = findComponentByName(tagName, components);

            if (component != null) {
                return generateComponentDoc(component);
            }
        }

        // 处理属性
        if (element instanceof XmlAttribute) {
            XmlAttribute attribute = (XmlAttribute) element;
            String attributeName = attribute.getName();
            XmlTag parentTag = attribute.getParent();

            if (parentTag != null) {
                String tagName = parentTag.getName();

                ComponentMeta component = findComponentByName(tagName, components);

                if (component != null) {
                    if (attributeName.startsWith("@")) {
                        ComponentMeta.ComponentEvent event = findEventByName(attributeName.substring(1), component);
                        return generateEventDoc(event);
                    } else {
                        ComponentMeta.ComponentProp prop = findPropertyByName(attributeName, component);
                        if (prop != null) {
                            return generatePropertyDoc(prop);
                        }
                    }

                }
            }
        }

        return null;
    }

    /**
     * 组成事件文档
     * @since 2025/8/2
     * @param event  事件描述
     * @return 事件文档HTML字符串
     */
    private String generateEventDoc(ComponentMeta.ComponentEvent event) {
        if (event == null) return null;

        StringBuilder doc = new StringBuilder();
        doc.append("<div style='font-family: Arial, sans-serif;'>");

        // 事件名称
        doc.append("<h3 style='color: #409EFF; margin-bottom: 10px;'>@").append(event.name).append("</h3>");

        // 事件描述
        if (event.description != null) {
            doc.append("<p style='margin-bottom: 15px;'>").append(event.description).append("</p>");
        }

        // 参数信息
        if (event.parameters != null && !event.parameters.isEmpty()) {
            doc.append("<h4 style='color: #333; margin-bottom: 10px;'>参数:</h4>");
            doc.append("<ul style='margin-bottom: 15px;'>");
            doc.append("<li><strong>").append(event.parameters).append("</strong>");
//            for (Map.Entry<String, String> param : event.parameters.entrySet()) {
//                doc.append("<li><strong>").append(param.getKey()).append("</strong>");
//                if (param.getValue() != null) {
//                    doc.append(": ").append(param.getValue());
//                }
//                doc.append("</li>");
//            }
            doc.append("</ul>");
        }

        // 使用示例
        doc.append("<div style='background-color: #f5f5f5; padding: 10px; border-radius: 4px;'>");
        doc.append("<strong>使用示例:</strong><br>");
        doc.append("<code style='color: #333;'>@").append(event.name).append("=\"handle").append(capitalizeFirstLetter(event.name)).append("\"</code>");
        doc.append("</div>");

        doc.append("</div>");
        return doc.toString();
    }

    /**
     * 首字母大写
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    /**
     * 生成组件文档
     */
    private String generateComponentDoc(ComponentMeta component) {
        StringBuilder doc = new StringBuilder();
        doc.append("<div style='font-family: Arial, sans-serif;'>");

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
                doc.append("</li>");
            }
            doc.append("</ul>");
        }

        doc.append("</div>");
        return doc.toString();
    }

    /**
     * 生成属性文档
     */
    private String generatePropertyDoc(ComponentMeta.ComponentProp prop) {
        StringBuilder doc = new StringBuilder();
        doc.append("<div style='font-family: Arial, sans-serif;'>");

        // 属性名称和类型
        doc.append("<h3 style='color: #409EFF; margin-bottom: 10px;'>").append(prop.name).append("</h3>");
        if (prop.type != null) {
            doc.append("<p style='color: #666; margin-bottom: 10px;'><strong>类型:</strong> ").append(prop.type).append("</p>");
        }

        // 属性描述
        if (prop.description != null) {
            doc.append("<p style='margin-bottom: 15px;'>").append(prop.description).append("</p>");
        }

        // 默认值
        if (prop.defaultValue != null) {
            doc.append("<p style='margin-bottom: 10px;'><strong>默认值:</strong> <code>").append(prop.defaultValue).append("</code></p>");
        }

        // 是否必需
        if (prop.required) {
            doc.append("<p style='color: #E6A23C; margin-bottom: 10px;'><strong>必需属性</strong></p>");
        }

        // 可选值
        if (prop.options != null && !prop.options.isEmpty()) {
            doc.append("<p style='margin-bottom: 10px;'><strong>可选值:</strong></p>");
            doc.append("<ul style='margin-bottom: 15px;'>");
            for (String option : prop.options) {
                doc.append("<li><code>").append(option).append("</code></li>");
            }
            doc.append("</ul>");
        }

        // 使用示例
        doc.append("<div style='background-color: #f5f5f5; padding: 10px; border-radius: 4px;'>");
        doc.append("<strong>使用示例:</strong><br>");
        if ("boolean".equals(prop.type)) {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"true\"</code>");
        } else if ("string".equals(prop.type) && prop.options != null && !prop.options.isEmpty()) {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"").append(prop.options.get(0)).append("\"</code>");
        } else if ("number".equals(prop.type)) {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"0\"</code>");
        } else if ("function".equals(prop.type)) {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"() => {}\"</code>");
        } else if ("object".equals(prop.type)) {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"{}\"</code>");
        } else if ("array".equals(prop.type)) {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"[]\"</code>");
        } else {
            doc.append("<code style='color: #333;'>").append(prop.name).append("=\"\"</code>");
        }
        doc.append("</div>");

        doc.append("</div>");
        return doc.toString();
    }

    /**
     * 根据名称查找组件
     */
    private ComponentMeta findComponentByName(String name, List<ComponentMeta> components) {
        if (name == null || components == null) return null;

        for (ComponentMeta component : components) {
            if (name.equals(component.name)) {
                return component;
            }
        }
        return null;
    }

    /**
     * 根据名称查找属性
     */
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

    /**
     * 根据名称查找事件
     */
    private ComponentMeta.ComponentEvent findEventByName(String name, ComponentMeta component) {
        if (name == null || component.events == null) return null;
        // 移除可能的 @ 前缀（如果存在）
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

    /**
     * 加载组件数据
     */
    private List<ComponentMeta> loadComponents() {
        if (cachedComponents != null) {
            return cachedComponents;
        }

        List<ComponentMeta> components = new ArrayList<>();

        // 加载 Element Plus 组件
        components.addAll(loadComponentsFromResource("data/element-plus-components.json"));

        // 加载 Element UI 组件
        components.addAll(loadComponentsFromResource("data/element-ui-components.json"));

        // 加载 Ant Design Vue 组件
        components.addAll(loadComponentsFromResource("data/ant-design-vue-components.json"));

        cachedComponents = components;
        return components;
    }

    /**
     * 从资源文件加载组件数据
     */
    private List<ComponentMeta> loadComponentsFromResource(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return new ArrayList<>();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(json, new TypeToken<List<ComponentMeta>>() {
            }.getType());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
} 