package com.chu7.vuecomponentassistant.xml;

import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

/**
 * VueKit XML 元素描述符提供者 - 用于提供悬停文档
 */
public class VueKitXmlElementDescriptorProvider implements XmlElementDescriptorProvider {

    static {
        System.out.println("=== VueKitXmlElementDescriptorProvider 类被加载 ===");
    }

    public VueKitXmlElementDescriptorProvider() {
        System.out.println("=== VueKitXmlElementDescriptorProvider 实例被创建 ===");
    }

    @Override
    public @Nullable XmlElementDescriptor getDescriptor(XmlTag xmlTag) {
        System.out.println("=== VueKitXmlElementDescriptorProvider.getDescriptor 被调用 ===");
        System.out.println("xmlTag = " + (xmlTag != null ? xmlTag.getClass().getSimpleName() : "null"));
        
        if (xmlTag == null) {
            return null;
        }

        if (!(xmlTag instanceof HtmlTag)) {
            System.out.println("xmlTag 不是 HtmlTag");
            return null;
        }

        HtmlTag htmlTag = (HtmlTag) xmlTag;
        String tagName = htmlTag.getName();
        System.out.println("HtmlTag 标签名: " + tagName);

        // 检查是否是 Element 组件
        if (tagName != null && tagName.startsWith("el-")) {
            System.out.println("检测到 Element 组件: " + tagName);
            
            // 创建自定义的元素描述符
            VueKitXmlElementDescriptor descriptor = new VueKitXmlElementDescriptor(null, null, null, tagName);
            System.out.println("创建了 VueKitXmlElementDescriptor: " + descriptor);
            return descriptor;
        }

        System.out.println("不是 Element 组件，返回 null");
        return null;
    }
}
