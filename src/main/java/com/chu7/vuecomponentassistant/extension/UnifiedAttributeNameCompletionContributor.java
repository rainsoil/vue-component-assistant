package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.library.model.Event;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class UnifiedAttributeNameCompletionContributor extends CompletionContributor {
	public UnifiedAttributeNameCompletionContributor() {
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().withParent(XmlAttribute.class),
				new CompletionProvider<CompletionParameters>() {
					@Override
					protected void addCompletions(@NotNull CompletionParameters parameters,
													 @NotNull ProcessingContext context,
													 @NotNull CompletionResultSet result) {
						PsiElement position = parameters.getPosition();
						XmlAttribute attribute = (XmlAttribute) position.getParent();
						XmlTag tag = attribute != null ? attribute.getParent() : null;
						if (tag == null) return;
						var provider = ProviderManager.getProvider(tag.getProject());
						if (provider == null || !provider.supportsTag(tag.getName())) return;
						Component component = provider.getComponent(tag.getName());
						if (component == null) return;
						boolean added = false;
						String prefix = result.getPrefixMatcher().getPrefix();
						// properties
						if (component.props != null) {
							for (Prop prop : component.props) {
								if (prop.name == null || prop.name.isEmpty()) continue;
								if (!prefix.isEmpty() && !prop.name.toLowerCase().startsWith(prefix.toLowerCase())) continue;
								String defaultText = prop.defaultValue != null ? String.valueOf(prop.defaultValue) : "";
								LookupElementBuilder builder = LookupElementBuilder.create(prop.name)
									.withTypeText(prop.description, true)
									.withTailText(prop.defaultValue != null ? ("  默认:" + prop.defaultValue) : null, true)
									.withIcon(com.intellij.icons.AllIcons.Nodes.Parameter)
									.withInsertHandler((ctx, item) -> replaceOrInsertAttribute(ctx, prop.name, defaultText));
								builder.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
								result.addElement(builder);
								added = true;
							}
						}
						// events
						if (component.events != null) {
							for (Event event : component.events) {
								String eventAttr = "@" + event.name;
								if (!prefix.isEmpty() && !eventAttr.toLowerCase().startsWith(prefix.toLowerCase())) continue;
								LookupElementBuilder builder = LookupElementBuilder.create(eventAttr)
									.withTypeText(event.description, true)
									.withIcon(com.intellij.icons.AllIcons.Nodes.Method)
									.withInsertHandler((ctx, item) -> replaceOrInsertAttribute(ctx, eventAttr, ""));
								builder.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
								result.addElement(builder);
								added = true;
							}
						}
						if (added) {
							result.stopHere();
						}
					}
				});
	}
	
	static void replaceOrInsertAttribute(@NotNull InsertionContext context, @NotNull String name, @NotNull String defaultText) {
		XmlAttribute currentAttr = PsiTreeUtil.getParentOfType(context.getFile().findElementAt(Math.max(0, Math.min(context.getTailOffset(), context.getDocument().getTextLength() - 1))), XmlAttribute.class, false);
		if (currentAttr == null) return;
		int start = currentAttr.getTextRange().getStartOffset();
		int end = currentAttr.getTextRange().getEndOffset();
		String existing = currentAttr.getText();
		String replacement;
		if (existing.contains("=")) {
			// 已有等号，统一替换为 name="default"
			replacement = name + "=\"" + defaultText + "\"";
		} else {
			replacement = name + "=\"" + defaultText + "\"";
		}
		context.getDocument().replaceString(start, end, replacement);
		int caret = start + name.length() + 2 + defaultText.length();
		context.getEditor().getCaretModel().moveToOffset(caret);
	}
} 