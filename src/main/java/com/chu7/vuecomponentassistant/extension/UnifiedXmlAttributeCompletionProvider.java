package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一XML属性补全提供者
 * 实现属性名称补全和属性值枚举功能
 */
public class UnifiedXmlAttributeCompletionProvider implements XmlAttributeDescriptorsProvider {
    private static final Logger LOG = Logger.getInstance(UnifiedXmlAttributeCompletionProvider.class);
    
    @Override
    public @Nullable XmlAttributeDescriptor[] getAttributeDescriptors(@Nullable XmlTag context) {
        if (context == null) {
            return new XmlAttributeDescriptor[0];
        }
        
        try {
            var provider = ProviderManager.getProvider(context.getProject());
            if (provider == null || !provider.supportsTag(context.getName())) {
                return new XmlAttributeDescriptor[0];
            }
            
            Component component = provider.getComponent(context.getName());
            if (component == null) {
                return new XmlAttributeDescriptor[0];
            }
            
            List<UnifiedXmlAttributeDescriptor> descriptors = new ArrayList<>();
            
            if (component.props != null) {
                for (Prop prop : component.props) {
                    if (prop.name != null && !prop.name.trim().isEmpty()) {
                        descriptors.add(toDescriptor(prop));
                    }
                }
            }
            
            return descriptors.toArray(new UnifiedXmlAttributeDescriptor[0]);
            
        } catch (Exception e) {
            LOG.warn("Error getting attribute descriptors for tag: " + context.getName(), e);
            return new XmlAttributeDescriptor[0];
        }
    }
    
    @Override
    public @Nullable XmlAttributeDescriptor getAttributeDescriptor(@Nullable String attributeName, @Nullable XmlTag context) {
        if (context == null || attributeName == null) {
            return null;
        }
        
        try {
            var provider = ProviderManager.getProvider(context.getProject());
            if (provider == null || !provider.supportsTag(context.getName())) {
                return null;
            }
            
            Component component = provider.getComponent(context.getName());
            if (component == null || component.props == null) {
                return null;
            }
            
            for (Prop prop : component.props) {
                if (attributeName.equals(prop.name)) {
                    return toDescriptor(prop);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            LOG.warn("Error getting attribute descriptor for: " + attributeName, e);
            return null;
        }
    }
    

    
    /**
     * 将Prop转换为UnifiedXmlAttributeDescriptor
     */
    @NotNull
    private UnifiedXmlAttributeDescriptor toDescriptor(@NotNull Prop prop) {
        String[] options = null;
        if (prop.options != null && !prop.options.isEmpty()) {
            options = prop.options.toArray(new String[0]);
        }
        
        String defaultValue = null;
        if (prop.defaultValue != null) {
            defaultValue = prop.defaultValue.toString();
        }
        
        return new UnifiedXmlAttributeDescriptor(
                prop.name,
                prop.description,
                options,
                defaultValue,
                AttributeType.PARAM
        );
    }
}
