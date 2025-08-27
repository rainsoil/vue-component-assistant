package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.library.model.Event;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlTagNameProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnifiedXmlAttributeNameProvider implements XmlTagNameProvider {
	private static final Logger LOG = Logger.getInstance(UnifiedXmlAttributeNameProvider.class);
	
	@Override
	public void addTagNameVariants(@NotNull List<LookupElement> elements, @NotNull XmlTag tag, @NotNull String namePrefix) {
		try {
			if (!isInAttributePosition(tag, namePrefix)) {
				return;
			}
			var provider = ProviderManager.getProvider(tag.getProject());
			if (provider == null || !provider.supportsTag(tag.getName())) {
				return;
			}
			Component component = provider.getComponent(tag.getName());
			if (component == null) {
				return;
			}
			// props
			if (component.props != null) {
				for (Prop prop : component.props) {
					if (prop.name != null && !prop.name.trim().isEmpty()) {
						if (namePrefix.isEmpty() || prop.name.toLowerCase().startsWith(namePrefix.toLowerCase())) {
							String propName = prop.name;
							String defaultText = getDefaultText(prop);
							LookupElementBuilder builder = LookupElementBuilder.create(propName)
								.withTypeText(prop.description, true)
								.withTailText(prop.defaultValue != null ? ("  默认:" + prop.defaultValue) : null, true)
								.withIcon(com.intellij.icons.AllIcons.Nodes.Parameter)
								.withInsertHandler((InsertionContext ctx, LookupElement item) -> setAttributeValueViaPsi(ctx, propName, defaultText));
							if (prop.required) builder = builder.withBoldness(true);
							builder.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
							elements.add(builder);
						}
					}
				}
			}
			// events '@event'
			if (component.events != null) {
				for (Event event : component.events) {
					String eventAttr = "@" + event.name;
					if (namePrefix.isEmpty() || eventAttr.toLowerCase().startsWith(namePrefix.toLowerCase())) {
						LookupElementBuilder builder = LookupElementBuilder.create(eventAttr)
							.withTypeText(event.description, true)
							.withIcon(com.intellij.icons.AllIcons.Nodes.Method)
							.withInsertHandler((InsertionContext ctx, LookupElement item) -> setAttributeValueViaPsi(ctx, eventAttr, ""));
						if (event.parameters != null) {
							builder = builder.withTailText("  参数:" + String.valueOf(event.parameters), true);
						}
						builder.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
						elements.add(builder);
					}
				}
			}
		} catch (Exception e) {
			LOG.warn("Error adding attribute/event variants for tag: " + tag.getName(), e);
		}
	}
	
	private String getDefaultText(@NotNull Prop prop) {
		if (prop.defaultJValue != null) return String.valueOf(prop.defaultJValue);
		if (prop.defaultValue != null) return String.valueOf(prop.defaultValue);
		return "";
	}
	
	private void setAttributeValueViaPsi(@NotNull InsertionContext context, @NotNull String propName, @NotNull String defaultText) {
		Project project = context.getProject();
		PsiDocumentManager.getInstance(project).commitDocument(context.getDocument());
		PsiElement at = context.getFile().findElementAt(Math.max(0, Math.min(context.getTailOffset(), context.getDocument().getTextLength() - 1)));
		XmlTag tag = PsiTreeUtil.getParentOfType(at, XmlTag.class, false);
		if (tag == null) return;
		WriteCommandAction.runWriteCommandAction(project, () -> {
			XmlAttribute attr = tag.getAttribute(propName);
			if (attr == null) {
				attr = tag.setAttribute(propName, defaultText);
			} else {
				attr.setValue(defaultText);
			}
			XmlAttributeValue value = attr.getValueElement();
			if (value == null) {
				// 无引号值时补全为 attr="value"
				String insertText = "\"" + defaultText + "\"";
				int offset = attr.getTextRange().getEndOffset();
				context.getDocument().insertString(offset, "=\"\"");
				value = attr.getValueElement();
			}
			if (value != null) {
				int start = value.getTextRange().getStartOffset();
				int caret = start + 1 + defaultText.length();
				context.getEditor().getCaretModel().moveToOffset(caret);
			}
		});
	}
	
	private boolean isInAttributePosition(@NotNull XmlTag tag, @NotNull String namePrefix) {
		return !tag.getName().isEmpty() && tag.getName().startsWith("el-") && !namePrefix.isEmpty();
	}
}
