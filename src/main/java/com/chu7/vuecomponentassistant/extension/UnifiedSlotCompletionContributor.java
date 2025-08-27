package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Slot;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class UnifiedSlotCompletionContributor extends CompletionContributor {
	public UnifiedSlotCompletionContributor() {
		extend(CompletionType.BASIC,
				PlatformPatterns.psiElement().withParent(XmlText.class),
				new CompletionProvider<CompletionParameters>() {
					@Override
					protected void addCompletions(@NotNull CompletionParameters parameters,
													 @NotNull ProcessingContext context,
													 @NotNull CompletionResultSet result) {
						PsiElement position = parameters.getPosition();
						XmlTag parentTag = getEnclosingTag(position);
						if (parentTag == null) return;
						var provider = ProviderManager.getProvider(parentTag.getProject());
						if (provider == null || !provider.supportsTag(parentTag.getName())) return;
						Component component = provider.getComponent(parentTag.getName());
						if (component == null || component.slots == null || component.slots.isEmpty()) return;
						boolean added = false;
						for (Slot slot : component.slots) {
							if (slot.name == null || slot.name.trim().isEmpty()) continue;
							String slotName = slot.name;
							boolean hasScope = slot.scope != null && !slot.scope.trim().isEmpty();
							Set<String> lookupStrings = buildLookupStrings(slotName);
							if (hasScope) {
								String scopeVar = slot.scope.trim();
								LookupElementBuilder scoped = LookupElementBuilder.create(slotName)
									.withPresentableText(slotName)
									.withTypeText(slot.description, true)
									.withTailText("  插槽 (scoped: " + scopeVar + ")", true)
									.withLookupStrings(lookupStrings)
									.withInsertHandler((InsertionContext ctx, LookupElement item) -> insertTemplate(ctx, slotName, scopeVar))
									.withIcon(com.intellij.icons.AllIcons.Nodes.Class);
								scoped.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
								result.addElement(scoped);
								added = true;
							} else {
								LookupElementBuilder basic = LookupElementBuilder.create(slotName)
									.withPresentableText(slotName)
									.withTypeText(slot.description, true)
									.withTailText("  插槽", true)
									.withLookupStrings(lookupStrings)
									.withInsertHandler((InsertionContext ctx, LookupElement item) -> insertTemplate(ctx, slotName, null))
									.withIcon(com.intellij.icons.AllIcons.Nodes.Class);
								basic.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
								result.addElement(basic);
								added = true;
							}
						}
						if (added) {
							result.stopHere();
						}
					}
				});
	}
	
	private Set<String> buildLookupStrings(@NotNull String slotName) {
		Set<String> s = new HashSet<>();
		s.add(slotName);
		s.add("sl");
		s.add("slot");
		s.add("template #" + slotName);
		return s;
	}
	
	private void insertTemplate(@NotNull InsertionContext context, @NotNull String slotName, String scopeVarOrNull) {
		Editor editor = context.getEditor();
		Document document = editor.getDocument();
		int start = context.getStartOffset();
		int end = context.getTailOffset();
		String snippet;
		if (scopeVarOrNull != null) {
			snippet = "<template #" + slotName + "=\"" + scopeVarOrNull + "\">\n \n</template>";
		} else {
			snippet = "<template #" + slotName + ">\n \n</template>";
		}
		document.replaceString(start, end, snippet);
		int caretOffset = start + snippet.indexOf('\n') + 2;
		if (caretOffset < start) caretOffset = start + snippet.length();
		editor.getCaretModel().moveToOffset(caretOffset);
	}
	
	private XmlTag getEnclosingTag(PsiElement element) {
		PsiElement current = element;
		while (current != null && !(current instanceof XmlTag)) {
			current = current.getParent();
		}
		return current instanceof XmlTag ? (XmlTag) current : null;
	}
} 