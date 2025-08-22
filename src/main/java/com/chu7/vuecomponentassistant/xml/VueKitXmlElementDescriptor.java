package com.chu7.vuecomponentassistant.xml;

import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion2.ElementPlusEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 elementPlugin 实现的 XML 元素描述符
 * 参考 ElementUIXmlElementDescriptor.kt
 */
public class VueKitXmlElementDescriptor implements XmlElementDescriptor {

    private static final Logger LOG = Logger.getInstance(VueKitXmlElementDescriptor.class);

    private final ElementPlusComponent component;
    private final XmlElementDescriptor originalDescriptor;
    private final XmlNSDescriptor nsDescriptor;
    private final String tagName;

    public VueKitXmlElementDescriptor(ElementPlusComponent component, XmlElementDescriptor originalDescriptor, XmlNSDescriptor nsDescriptor, String tagName) {
        this.component = component;
        this.originalDescriptor = originalDescriptor;
        this.nsDescriptor = nsDescriptor;
        this.tagName = tagName;
        
        LOG.debug("=== VueKitXmlElementDescriptor 被创建 ===");
        LOG.debug("tagName: " + tagName);
        LOG.debug("originalDescriptor: " + (originalDescriptor != null ? originalDescriptor.getName() : "null"));
    }

    @Override
    public String getQualifiedName() {
        return tagName;
    }

    @Override
    public String getDefaultName() {
        return tagName;
    }

    @Override
    public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
        // 如果有原有描述符，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getElementsDescriptors(context);
        }
        return XmlElementDescriptor.EMPTY_ARRAY;
    }

    @Override
    public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
        // 如果有原有描述符，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getElementDescriptor(childTag, contextTag);
        }
        return null;
    }

    @Override
    public XmlAttributeDescriptor[] getAttributesDescriptors(XmlTag context) {
        if (component == null) {
            // 如果有原有描述符，使用原有的
            if (originalDescriptor != null) {
                return originalDescriptor.getAttributesDescriptors(context);
            }
            return XmlAttributeDescriptor.EMPTY;
        }

        LOG.debug("=== VueKitXmlElementDescriptor.getAttributesDescriptors 被调用 ===");
        
        // 合并我们的属性和原有属性
        List<XmlAttributeDescriptor> allDescriptors = new ArrayList<>();
        
        // 添加我们的属性描述符
        XmlAttributeDescriptor[] ourDescriptors = generateAttributeDescriptors(component);
        if (ourDescriptors != null) {
            for (XmlAttributeDescriptor descriptor : ourDescriptors) {
                allDescriptors.add(descriptor);
            }
        }
        
        // 如果有原有描述符，也添加原有的属性
        if (originalDescriptor != null) {
            XmlAttributeDescriptor[] originalDescriptors = originalDescriptor.getAttributesDescriptors(context);
            if (originalDescriptors != null) {
                for (XmlAttributeDescriptor descriptor : originalDescriptors) {
                    allDescriptors.add(descriptor);
                }
            }
        }
        
        LOG.debug("总共生成了 " + allDescriptors.size() + " 个属性描述符");
        return allDescriptors.toArray(new XmlAttributeDescriptor[0]);
    }

    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
        if (component == null || attribute == null) {
            // 如果有原有描述符，使用原有的
            if (originalDescriptor != null) {
                return originalDescriptor.getAttributeDescriptor(attribute);
            }
            return null;
        }

        LOG.debug("=== VueKitXmlElementDescriptor.getAttributeDescriptor(XmlAttribute) 被调用 ===");
        LOG.debug("attributeName: " + attribute.getName());
        
        // 先尝试我们的属性描述符
        XmlAttributeDescriptor ourDescriptor = generateSpecificAttributeDescriptor(component, attribute.getName());
        if (ourDescriptor != null) {
            return ourDescriptor;
        }
        
        // 如果没有找到，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getAttributeDescriptor(attribute);
        }
        
        return null;
    }

    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, XmlTag context) {
        if (component == null) {
            // 如果有原有描述符，使用原有的
            if (originalDescriptor != null) {
                return originalDescriptor.getAttributeDescriptor(attributeName, context);
            }
            return null;
        }

        LOG.debug("=== VueKitXmlElementDescriptor.getAttributeDescriptor(String, XmlTag) 被调用 ===");
        LOG.debug("attributeName: " + attributeName);
        
        // 先尝试我们的属性描述符
        XmlAttributeDescriptor ourDescriptor = generateSpecificAttributeDescriptor(component, attributeName);
        if (ourDescriptor != null) {
            return ourDescriptor;
        }
        
        // 如果没有找到，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getAttributeDescriptor(attributeName, context);
        }
        
        return null;
    }

    @Override
    public XmlNSDescriptor getNSDescriptor() {
        return nsDescriptor;
    }

    @Override
    public XmlElementsGroup getTopGroup() {
        // 如果有原有描述符，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getTopGroup();
        }
        return null;
    }

    @Override
    public int getContentType() {
        // 返回 ANY 类型，让 IDEA 能够识别为通用元素
        // 这样不会阻止 lang.documentationProvider 的调用
        return CONTENT_TYPE_ANY;
    }

    @Override
    public String getDefaultValue() {
        // 如果有原有描述符，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getDefaultValue();
        }
        return null;
    }

    @Override
    public PsiElement getDeclaration() {
        // 返回 null，让 IDEA 能够调用 lang.documentationProvider
        // 这样 ElementUIDocumentProvider 就能正常工作
        return null;
    }

    @Override
    public String getName(PsiElement context) {
        return tagName;
    }

    @Override
    public String getName() {
        return tagName;
    }

    @Override
    public void init(PsiElement element) {
        // 如果有原有描述符，使用原有的
        if (originalDescriptor != null) {
            originalDescriptor.init(element);
        }
    }

    @Override
    public Object[] getDependences() {
        // 如果有原有描述符，使用原有的
        if (originalDescriptor != null) {
            return originalDescriptor.getDependences();
        }
        return new Object[0];
    }

    /**
     * 生成组件的属性描述符（包括属性和事件）
     */
    private XmlAttributeDescriptor[] generateAttributeDescriptors(ElementPlusComponent component) {
        List<VueKitXmlAttributeDescriptor> descriptors = new ArrayList<>();

        // 添加属性描述符
        if (component.getProps() != null) {
            for (ElementPlusProp prop : component.getProps()) {
                VueKitXmlAttributeDescriptor descriptor = new VueKitXmlAttributeDescriptor(
                    prop.getName(),
                    prop.getType() + " " + prop.getDescription(),
                    prop.getOptions() != null ? prop.getOptions().toArray(new String[0]) : new String[0],
                    prop.getDescription(),
                    prop.getDefaultValue() != null ? prop.getDefaultValue().toString() : "",
                    VueKitXmlAttributeDescriptorsProvider.AttributeType.PARAM
                );
                descriptors.add(descriptor);
            }
        }

        // 添加事件描述符
        if (component.getEvents() != null) {
            for (ElementPlusEvent event : component.getEvents()) {
                VueKitXmlAttributeDescriptor descriptor = new VueKitXmlAttributeDescriptor(
                    event.getName(),
                    event.getDescription(),
                    new String[0],
                    "",
                    event.getParameters() != null ? event.getParameters().toString() : "",
                    VueKitXmlAttributeDescriptorsProvider.AttributeType.EVENT
                );
                descriptors.add(descriptor);
            }
        }

        LOG.debug("生成了 " + descriptors.size() + " 个属性描述符");
        return descriptors.toArray(new VueKitXmlAttributeDescriptor[0]);
    }

    /**
     * 生成特定属性的描述符
     */
    private XmlAttributeDescriptor generateSpecificAttributeDescriptor(ElementPlusComponent component, String attributeName) {
        // 查找属性
        if (component.getProps() != null) {
            for (ElementPlusProp prop : component.getProps()) {
                if (attributeName.equals(prop.getName())) {
                    return new VueKitXmlAttributeDescriptor(
                        prop.getName(),
                        prop.getType() + " " + prop.getDescription(),
                        prop.getOptions() != null ? prop.getOptions().toArray(new String[0]) : new String[0],
                        prop.getDescription(),
                        prop.getDefaultValue() != null ? prop.getDefaultValue().toString() : "",
                        VueKitXmlAttributeDescriptorsProvider.AttributeType.PARAM
                    );
                }
            }
        }

        // 查找事件
        if (component.getEvents() != null) {
            for (ElementPlusEvent event : component.getEvents()) {
                if (attributeName.equals(event.getName())) {
                    return new VueKitXmlAttributeDescriptor(
                        event.getName(),
                        event.getDescription(),
                        new String[0],
                        "",
                        event.getParameters() != null ? event.getParameters().toString() : "",
                        VueKitXmlAttributeDescriptorsProvider.AttributeType.EVENT
                    );
                }
            }
        }

        return null;
    }
} 