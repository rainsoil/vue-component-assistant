package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.library.model.Event;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一XML属性描述符提供者
 * 实现XmlAttributeDescriptorsProvider接口，提供组件属性补全功能
 */
public class UnifiedXmlAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
    private static final Logger LOG = Logger.getInstance(UnifiedXmlAttributeDescriptorsProvider.class);
    
    @Override
    public @Nullable XmlAttributeDescriptor[] getAttributeDescriptors(@Nullable XmlTag context) {
        if (context == null) {
            return new XmlAttributeDescriptor[0];
        }
        
        try {
            // 获取项目级别的组件提供者
            var provider = ProviderManager.getProvider(context.getProject());
            if (provider == null || !provider.supportsTag(context.getName())) {
                return new XmlAttributeDescriptor[0];
            }
            
            // 获取组件信息
            Component component = provider.getComponent(context.getName());
            if (component == null) {
                return new XmlAttributeDescriptor[0];
            }
            
            // 构建属性描述符列表
            List<UnifiedXmlAttributeDescriptor> descriptors = new ArrayList<>();
            
            // props
            if (component.props != null) {
                for (Prop prop : component.props) {
                    if (prop.name != null && !prop.name.trim().isEmpty()) {
                        descriptors.add(toPropDescriptor(prop));
                    }
                }
            }
            
            // events (use '@' + name)
            if (component.events != null) {
                for (Event event : component.events) {
                    if (event.name != null && !event.name.trim().isEmpty()) {
                        String eventAttrName = "@" + event.name;
                        descriptors.add(new UnifiedXmlAttributeDescriptor(
                            eventAttrName,
                            event.description,
                            new String[0],
                            stringifyParameters(event.parameters),
                            AttributeType.EVENT
                        ));
                    }
                }
            }
            
            return descriptors.toArray(new UnifiedXmlAttributeDescriptor[0]);
            
        } catch (ProcessCanceledException pce) {
            throw pce;
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
            // 获取项目级别的组件提供者
            var provider = ProviderManager.getProvider(context.getProject());
            if (provider == null || !provider.supportsTag(context.getName())) {
                return null;
            }
            
            // 获取组件信息
            Component component = provider.getComponent(context.getName());
            if (component == null) {
                return null;
            }
            
            // props exact match
            if (component.props != null) {
                for (Prop prop : component.props) {
                    if (attributeName.equals(prop.name)) {
                        return toPropDescriptor(prop);
                    }
                }
            }
            
            // events match '@name'
            if (attributeName.startsWith("@") && component.events != null) {
                String eventName = attributeName.substring(1);
                for (Event event : component.events) {
                    if (eventName.equals(event.name)) {
                        return new UnifiedXmlAttributeDescriptor(
                                attributeName,
                                event.description,
                                new String[0],
                                stringifyParameters(event.parameters),
                                AttributeType.EVENT
                        );
                    }
                }
            }
            
            return null;
            
        } catch (ProcessCanceledException pce) {
            throw pce;
        } catch (Exception e) {
            LOG.warn("Error getting attribute descriptor for: " + attributeName, e);
            return null;
        }
    }
    
    /**
     * 将Prop转换为UnifiedXmlAttributeDescriptor
     */
    @NotNull
    private UnifiedXmlAttributeDescriptor toPropDescriptor(@NotNull Prop prop) {
        // 转换options为字符串数组
        String[] options = null;
        if (prop.options != null && !prop.options.isEmpty()) {
            options = prop.options.toArray(new String[0]);
        }
        
        // 转换默认值为字符串
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
    
    private String stringifyParameters(Object parameters) {
        if (parameters == null) return null;
        return String.valueOf(parameters);
    }
} 