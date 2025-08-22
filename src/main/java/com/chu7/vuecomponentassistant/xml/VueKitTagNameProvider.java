package com.chu7.vuecomponentassistant.xml;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;

/**
 * 基于 elementPlugin 实现的标签名称提供者
 * 参考 ElementUIXmlElementDescriptorProvider.kt
 */
public class VueKitTagNameProvider implements XmlElementDescriptorProvider {

    private static final Logger LOG = Logger.getInstance(VueKitTagNameProvider.class);

    @Override
    public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
        try {
            if (xmlTag == null) {
                return null;
            }

            String tagName = xmlTag.getName();
            if (tagName == null || !tagName.startsWith("el-")) {
                return null;
            }

            LOG.debug("=== VueKitTagNameProvider.getDescriptor 被调用 ===");
            LOG.debug("tagName: " + tagName);

            Project project = xmlTag.getProject();
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                LOG.debug("ComponentProvider 为 null");
                return null;
            }

            ElementPlusComponent component = componentProvider.getComponent(tagName);
            if (component == null) {
                LOG.debug("未找到组件: " + tagName);
                return null;
            }

            // 简化命名空间处理，避免 ProcessCanceledException
            XmlElementDescriptor originalDescriptor = null;
            XmlNSDescriptor nsDescriptor = null;
            
            try {
                // 尝试获取命名空间信息，但不强制要求
                nsDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
                if (nsDescriptor != null) {
                    originalDescriptor = nsDescriptor.getElementDescriptor(xmlTag);
                }
            } catch (com.intellij.openapi.progress.ProcessCanceledException e) {
                // ProcessCanceledException 是正常的控制流异常，不应该记录
                LOG.debug("获取命名空间信息被取消");
            } catch (Exception e) {
                // 其他异常记录为调试信息
                LOG.debug("获取命名空间信息时发生异常: " + e.getMessage());
            }

            LOG.debug("找到组件: " + tagName + ", 创建描述符");
            // 传入原有的描述符，避免完全覆盖
            return new VueKitXmlElementDescriptor(component, originalDescriptor, nsDescriptor, tagName);

        } catch (Exception e) {
            LOG.error("获取标签描述符时发生错误", e);
            return null;
        }
    }
} 