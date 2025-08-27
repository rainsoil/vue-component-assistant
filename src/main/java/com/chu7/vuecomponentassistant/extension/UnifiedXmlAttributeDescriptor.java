package com.chu7.vuecomponentassistant.extension;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 统一XML属性描述符
 * 用于表示组件属性的XML属性描述
 */
public class UnifiedXmlAttributeDescriptor implements XmlAttributeDescriptor {
    private final String name;
    private final String description;
    private final String[] options;
    private final String defaultValue;
    private final AttributeType attributeType;
    
    public UnifiedXmlAttributeDescriptor(@NotNull String name, 
                                       @Nullable String description,
                                       @Nullable String[] options,
                                       @Nullable String defaultValue,
                                       @NotNull AttributeType attributeType) {
        this.name = name;
        this.description = description;
        this.options = options != null ? options : new String[0];
        this.defaultValue = defaultValue;
        this.attributeType = attributeType;
    }
    
    @Override
    public @NotNull String getName() {
        return name;
    }
    
    public @NotNull String getQualifiedName() {
        return name;
    }
    
    public @NotNull String getDefaultName() {
        return name;
    }
    
    @Override
    public @Nullable String getDefaultValue() {
        return defaultValue;
    }
    
    @Override
    public boolean isRequired() {
        return false; // 暂时设为false，后续可以从Prop.required获取
    }
    
    public boolean isRequired(XmlAttribute context) {
        return false; // 暂时设为false，后续可以从Prop.required获取
    }
    
    @Override
    public boolean isFixed() {
        return false;
    }
    
    public boolean hasIdRef() {
        return false;
    }
    
    @Override
    public boolean isEnumerated() {
        return options.length > 0;
    }
    
    @Override
    public String[] getEnumeratedValues() {
        return options;
    }
    
    @Override
    public @Nullable PsiElement getDeclaration() {
        return null;
    }
    
    @Override
    public @NotNull String getName(PsiElement context) {
        return name;
    }
    
    @Override
    public void init(PsiElement element) {
        // 不需要初始化
    }
    
    public @Nullable String getTypeName() {
        return null;
    }
    

    
    public boolean hasIdRef(XmlAttribute context) {
        return false;
    }
    
    public @Nullable String getDefaultValue(XmlAttribute context) {
        return defaultValue;
    }
    
    public boolean isFixed(XmlAttribute context) {
        return false;
    }
    
    public boolean isEnumerated(XmlAttribute context) {
        return isEnumerated();
    }
    
    public String[] getEnumeratedValues(XmlAttribute context) {
        return getEnumeratedValues();
    }
    
    public @Nullable String getTypeName(XmlAttribute context) {
        return getTypeName();
    }
    
    public @Nullable Icon getIcon() {
        return attributeType.getIcon();
    }
    
    public @Nullable String getDocumentation(XmlAttribute context) {
        StringBuilder doc = new StringBuilder();
        if (description != null) {
            doc.append(description);
        }
        if (defaultValue != null) {
            doc.append("\n默认值: ").append(defaultValue);
        }
        if (options.length > 0) {
            doc.append("\n可选值: ").append(String.join(", ", options));
        }
        return doc.toString();
    }
    
    public @Nullable String getDocumentation() {
        return getDocumentation(null);
    }
    
    @Override
    public String validateValue(XmlElement context, String value) {
        return null; // 验证通过返回null
    }
    
    @Override
    public boolean hasIdType() {
        return false;
    }
    
    @Override
    public boolean hasIdRefType() {
        return false;
    }
    
    /**
     * 获取属性类型
     */
    @NotNull
    public AttributeType getAttributeType() {
        return attributeType;
    }
    
    /**
     * 获取属性描述
     */
    @Nullable
    public String getDescription() {
        return description;
    }
} 