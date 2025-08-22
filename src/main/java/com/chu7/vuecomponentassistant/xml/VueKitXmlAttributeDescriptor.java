package com.chu7.vuecomponentassistant.xml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.xml.XmlElement;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NlsSafe;

import javax.swing.Icon;
import java.util.Objects;

/**
 * 基于 elementPlugin 实现的属性描述符
 * 参考 ElementUIXmlAttributeDescriptor.kt
 */
public class VueKitXmlAttributeDescriptor extends BasicXmlAttributeDescriptor 
    implements XmlAttributeDescriptorEx, PsiPresentableMetaData {
    
    private static final Logger LOG = Logger.getInstance(VueKitXmlAttributeDescriptor.class);
    
    private final String attributeName;
    private final String typeName;
    private final String[] attributeValues;
    private final String rawAttributeValueHtml;
    private final String defaultValue;
    private final VueKitXmlAttributeDescriptorsProvider.AttributeType attributeType;

    public VueKitXmlAttributeDescriptor(
            String attributeName,
            String typeName,
            String[] attributeValues,
            String rawAttributeValueHtml,
            String defaultValue,
            VueKitXmlAttributeDescriptorsProvider.AttributeType attributeType) {
        this.attributeName = attributeName;
        this.typeName = typeName;
        this.attributeValues = attributeValues != null ? attributeValues : new String[0];
        this.rawAttributeValueHtml = rawAttributeValueHtml != null ? rawAttributeValueHtml : "";
        this.defaultValue = defaultValue;
        this.attributeType = attributeType;
        
        LOG.debug("=== VueKitXmlAttributeDescriptor 被创建 ===");
        LOG.debug("attributeName: " + attributeName);
        LOG.debug("typeName: " + typeName);
        LOG.debug("defaultValue: " + defaultValue);
        LOG.debug("attributeType: " + attributeType);
    }

    @Override
    public PsiElement getDeclaration() {
        return null;
    }

    @Override
    public String getName() {
        return attributeName;
    }

    @Override
    public void init(PsiElement element) {
        // 不需要初始化
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean hasIdType() {
        return false;
    }

    @Override
    public boolean hasIdRefType() {
        return false;
    }

    @Override
    public boolean isEnumerated() {
        return attributeValues.length > 0;
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String[] getEnumeratedValues() {
        return attributeValues;
    }

    @Override
    public PsiElement getEnumeratedValueDeclaration(XmlElement xmlElement, String value) {
        return xmlElement;
    }

    @Override
    public String handleTargetRename(String newTargetName) {
        return newTargetName;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public Icon getIcon() {
        // 根据属性类型返回不同的图标
        if (attributeType == VueKitXmlAttributeDescriptorsProvider.AttributeType.EVENT) {
            // 事件图标
            return IconLoader.getIcon("/icons/event.svg", VueKitXmlAttributeDescriptor.class);
        } else {
            // 属性图标
            return IconLoader.getIcon("/icons/property.svg", VueKitXmlAttributeDescriptor.class);
        }
    }

    /**
     * 获取属性的可显示文本
     */
    public String getPresentableText() {
        StringBuilder sb = new StringBuilder();
        sb.append(attributeName);
        
        if (typeName != null && !typeName.isEmpty()) {
            sb.append(" (").append(typeName).append(")");
        }
        
        if (defaultValue != null && !defaultValue.isEmpty()) {
            sb.append(" = ").append(defaultValue);
        }
        
        return sb.toString();
    }

    /**
     * 获取属性的位置字符串
     */
    public String getLocationString() {
        return attributeType == VueKitXmlAttributeDescriptorsProvider.AttributeType.EVENT ? "事件" : "属性";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VueKitXmlAttributeDescriptor that = (VueKitXmlAttributeDescriptor) obj;
        return Objects.equals(attributeName, that.attributeName) &&
               Objects.equals(typeName, that.typeName) &&
               Objects.equals(defaultValue, that.defaultValue) &&
               attributeType == that.attributeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName, typeName, defaultValue, attributeType);
    }

    @Override
    public String toString() {
        return "VueKitXmlAttributeDescriptor{" +
                "attributeName='" + attributeName + '\'' +
                ", typeName='" + typeName + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", attributeType=" + attributeType +
                '}';
    }
}
