package com.chu7.vuecomponentassistant.xml;

import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.intellij.psi.PsiElement;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;

/**
 * VueKit XML 元素描述符
 * 
 * <p>简化版本，提供基本的 XML 元素描述符功能</p>
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class VueKitXmlElementDescriptor implements XmlElementDescriptor {

    private final ElementPlusComponent component;
    private final XmlNSDescriptor nsDescriptor;
    private final String tagName;

    /**
     * 构造函数
     * 
     * @param component Vue 组件对象
     * @param nsDescriptor 命名空间描述符
     * @param tagName 标签名称
     */
    public VueKitXmlElementDescriptor(ElementPlusComponent component, XmlNSDescriptor nsDescriptor, String tagName) {
        this.component = component;
        this.nsDescriptor = nsDescriptor;
        this.tagName = tagName;
    }

    @Override
    public String getName() {
        return tagName;
    }

    @Override
    public String getName(PsiElement context) {
        return tagName;
    }

    @Override
    public void init(PsiElement element) {
        // 不需要特殊初始化
    }

    @Override
    public Object[] getDependences() {
        return new Object[0];
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
    public XmlElementDescriptor[] getElementsDescriptors(com.intellij.psi.xml.XmlTag context) {
        return new XmlElementDescriptor[0];
    }

    @Override
    public XmlElementDescriptor getElementDescriptor(com.intellij.psi.xml.XmlTag childTag, com.intellij.psi.xml.XmlTag contextTag) {
        return null;
    }

    @Override
    public com.intellij.xml.XmlAttributeDescriptor[] getAttributesDescriptors(com.intellij.psi.xml.XmlTag context) {
        return new com.intellij.xml.XmlAttributeDescriptor[0];
    }

    @Override
    public com.intellij.xml.XmlAttributeDescriptor getAttributeDescriptor(com.intellij.psi.xml.XmlAttribute attribute) {
        return null;
    }

    @Override
    public com.intellij.xml.XmlAttributeDescriptor getAttributeDescriptor(String attributeName, com.intellij.psi.xml.XmlTag context) {
        return null;
    }

    @Override
    public XmlNSDescriptor getNSDescriptor() {
        return nsDescriptor;
    }

    @Override
    public XmlElementsGroup getTopGroup() {
        return null;
    }

    @Override
    public int getContentType() {
        return CONTENT_TYPE_MIXED;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public PsiElement getDeclaration() {
        return null;
    }

    /**
     * 获取组件对象
     * 
     * @return Vue 组件对象
     */
    public ElementPlusComponent getComponent() {
        return component;
    }
} 