package com.chu7.vuecomponentassistant.xml;

import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion2.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 elementPlugin 实现的属性描述符提供者
 * 参考 ElementUIXmlAttributeDescriptorsProvider.kt
 */
public class VueKitXmlAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
    
    private static final Logger LOG = Logger.getInstance(VueKitXmlAttributeDescriptorsProvider.class);

    @Override
    public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
        try {
            if (!(xmlTag instanceof HtmlTag)) {
                return XmlAttributeDescriptor.EMPTY;
            }

            String tagName = xmlTag.getName();
            if (!tagName.startsWith("el-")) {
                return XmlAttributeDescriptor.EMPTY;
            }

            LOG.debug("=== VueKitXmlAttributeDescriptorsProvider.getAttributeDescriptors 被调用 ===");
            LOG.debug("tagName: " + tagName);

            Project project = xmlTag.getProject();
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                LOG.debug("ComponentProvider 为 null");
                return XmlAttributeDescriptor.EMPTY;
            }

            ElementPlusComponent component = componentProvider.getComponent(tagName);
            if (component == null) {
                LOG.debug("未找到组件: " + tagName);
                return XmlAttributeDescriptor.EMPTY;
            }

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
                        AttributeType.PARAM
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
                        event.getParameters(),
                        AttributeType.EVENT
                    );
                    descriptors.add(descriptor);
                }
            }

            LOG.debug("生成了 " + descriptors.size() + " 个属性描述符");
            return descriptors.toArray(new VueKitXmlAttributeDescriptor[0]);

        } catch (Exception e) {
            LOG.error("获取属性描述符时发生错误", e);
            return XmlAttributeDescriptor.EMPTY;
        }
    }

    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, XmlTag xmlTag) {
        try {
            if (!(xmlTag instanceof HtmlTag)) {
                return null;
            }

            String tagName = xmlTag.getName();
            if (!tagName.startsWith("el-")) {
                return null;
            }

            LOG.debug("=== VueKitXmlAttributeDescriptorsProvider.getAttributeDescriptor 被调用 ===");
            LOG.debug("tagName: " + tagName + ", attributeName: " + attributeName);

            Project project = xmlTag.getProject();
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                return null;
            }

            ElementPlusComponent component = componentProvider.getComponent(tagName);
            if (component == null) {
                return null;
            }

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
                            AttributeType.PARAM
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
                            AttributeType.EVENT
                        );
                    }
                }
            }

            return null;

        } catch (Exception e) {
            LOG.error("获取属性描述符时发生错误", e);
            return null;
        }
    }

    /**
     * 属性类型枚举 - 参考 elementPlugin
     */
    public enum AttributeType {
        PARAM,    // 属性
        EVENT     // 事件
    }
}
