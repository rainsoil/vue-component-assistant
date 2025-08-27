package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class UnifiedXmlAttributeValueProvider extends CompletionContributor {
	public UnifiedXmlAttributeValueProvider() {
		extend(CompletionType.BASIC, 
		       PlatformPatterns.psiElement().withParent(XmlAttributeValue.class),
		       new CompletionProvider<CompletionParameters>() {
		           @Override
		           protected void addCompletions(@NotNull CompletionParameters parameters,
																		 @NotNull ProcessingContext context,
																		 @NotNull CompletionResultSet result) {
		               try {
		                   PsiElement position = parameters.getPosition();
		                   if (!isInAttributeValuePosition(position)) {
		                       return;
		                   }
		                   XmlAttribute attribute = getAttributeFromPosition(position);
		                   if (attribute == null) {
		                       return;
		                   }
		                   XmlTag tag = attribute.getParent();
		                   if (tag == null) {
		                       return;
		                   }
		                   var provider = ProviderManager.getProvider(tag.getProject());
		                   if (provider == null || !provider.supportsTag(tag.getName())) {
		                       return;
		                   }
		                   Component component = provider.getComponent(tag.getName());
		                   if (component == null || component.props == null) {
		                       return;
		                   }
		                   String attributeName = attribute.getName();
		                   boolean added = false;
		                   for (Prop prop : component.props) {
		                       if (attributeName.equals(prop.name) && prop.options != null && !prop.options.isEmpty()) {
		                           for (String option : prop.options) {
		                               LookupElement element = LookupElementBuilder.create(option)
		                                       .withTypeText(prop.description != null ? prop.description : "可选值")
		                                       .withIcon(com.intellij.icons.AllIcons.Nodes.Enum)
		                                       .withPresentableText(option);
		                               result.addElement(element);
		                               added = true;
		                           }
		                           break;
		                       }
		                   }
		                   if (added) {
		                   	// 阻止后续贡献者（包含 IDEA 内置）的候选
		                   	result.stopHere();
		                   }
		               } catch (Exception ignored) {
		               }
		           }
		       });
	}
	
	private boolean isInAttributeValuePosition(@NotNull PsiElement position) {
		PsiElement parent = position.getParent();
		if (parent instanceof XmlAttribute) {
			return true;
		}
		if (parent instanceof XmlAttributeValue) {
			PsiElement maybeAttribute = parent.getParent();
			return maybeAttribute instanceof XmlAttribute;
		}
		return false;
	}
	
	private XmlAttribute getAttributeFromPosition(@NotNull PsiElement position) {
		PsiElement parent = position.getParent();
		if (parent instanceof XmlAttribute) {
			return (XmlAttribute) parent;
		}
		if (parent instanceof XmlAttributeValue) {
			PsiElement maybeAttribute = parent.getParent();
			if (maybeAttribute instanceof XmlAttribute) {
				return (XmlAttribute) maybeAttribute;
			}
		}
		return null;
	}
}
