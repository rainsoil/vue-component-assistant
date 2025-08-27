package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 统一XML元素描述符
 * 用于表示自定义组件的XML元素描述
 */
public class UnifiedXmlElementDescriptor implements XmlElementDescriptor {
    private final Component component;
    private final XmlElementDescriptor originalDescriptor;
    private final XmlNSDescriptor nsDescriptor;
    private final String tagName;
    
    public UnifiedXmlElementDescriptor(@NotNull Component component, 
                                     @Nullable XmlElementDescriptor originalDescriptor,
                                     @Nullable XmlNSDescriptor nsDescriptor,
                                     @NotNull String tagName) {
        this.component = component;
        this.originalDescriptor = originalDescriptor;
        this.nsDescriptor = nsDescriptor;
        this.tagName = tagName;
    }
    
    @Override
    public @NotNull String getName() {
        return tagName;
    }
    
    @Override
    public @NotNull String getQualifiedName() {
        return tagName;
    }
    
    @Override
    public @NotNull String getDefaultName() {
        return tagName;
    }
    
    @Override
    public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
        // 返回空数组，因为我们只处理叶子组件
        return XmlElementDescriptor.EMPTY_ARRAY;
    }
    
    @Override
    public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
        // 返回null，因为我们只处理叶子组件
        return null;
    }
    
    @Override
    public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
        // 使用UnifiedXmlAttributeDescriptorsProvider获取属性描述符
        if (context != null) {
            var provider = new UnifiedXmlAttributeDescriptorsProvider();
            return provider.getAttributeDescriptors(context);
        }
        return new XmlAttributeDescriptor[0];
    }
    
    @Override
    public @Nullable XmlAttributeDescriptor getAttributeDescriptor(@Nullable String attributeName, @Nullable XmlTag context) {
        // 使用UnifiedXmlAttributeDescriptorsProvider获取属性描述符
        if (context != null && attributeName != null) {
            var provider = new UnifiedXmlAttributeDescriptorsProvider();
            return provider.getAttributeDescriptor(attributeName, context);
        }
        return null;
    }
    

    
    @Override
    public @Nullable XmlAttributeDescriptor getAttributeDescriptor(@Nullable XmlAttribute attribute) {
        // 返回null，属性由UnifiedXmlAttributeDescriptorsProvider处理
        return null;
    }
    
    @Override
    public @Nullable XmlNSDescriptor getNSDescriptor() {
        return nsDescriptor;
    }

    @Override
    public @Nullable XmlElementsGroup getTopGroup() {
        return null;
    }


    @Override
    public int getContentType() {
        return CONTENT_TYPE_ANY;
    }
    
    @Override
    public @Nullable String getDefaultValue() {
        return null;
    }
    
    @Override
    public @Nullable PsiElement getDeclaration() {
        // 返回null，确保文档提供者被调用
        return null;
    }
    
    @Override
    public @NotNull String getName(PsiElement context) {
        return tagName;
    }
    
    @Override
    public void init(PsiElement element) {
        // 不需要初始化
    }
    

    
    /**
     * 获取关联的组件
     */
    @NotNull
    public Component getComponent() {
        return component;
    }
    
    /**
     * 获取原始描述符
     */
    @Nullable
    public XmlElementDescriptor getOriginalDescriptor() {
        return originalDescriptor;
    }
} 