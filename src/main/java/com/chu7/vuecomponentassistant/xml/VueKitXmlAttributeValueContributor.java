package com.chu7.vuecomponentassistant.xml;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.XmlPatterns;
import org.jetbrains.annotations.NotNull;

/**
 * VueKit XML 属性值补全贡献者
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 */
public class VueKitXmlAttributeValueContributor extends CompletionContributor {

    public VueKitXmlAttributeValueContributor() {
        // 为XML属性值提供补全
        extend(CompletionType.BASIC, 
               PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()),
               new VueKitXmlAttributeValueProvider());
        
        // 为Vue模板中的属性值提供补全
        extend(CompletionType.BASIC, 
               PlatformPatterns.psiElement().inside(XmlPatterns.xmlAttributeValue()),
               new VueKitXmlAttributeValueProvider());
    }
}
