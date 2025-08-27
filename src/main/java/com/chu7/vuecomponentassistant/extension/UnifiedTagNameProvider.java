package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlTagNameProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 统一标签名提供者
 * 实现XmlElementDescriptorProvider和XmlTagNameProvider接口，提供组件标签补全功能
 */
public class UnifiedTagNameProvider implements XmlElementDescriptorProvider, XmlTagNameProvider {
	private static final Logger LOG = Logger.getInstance(UnifiedTagNameProvider.class);
	
	@Override
	public void addTagNameVariants(@NotNull List<LookupElement> elements, @NotNull XmlTag tag, @NotNull String namePrefix) {
		try {
			var provider = ProviderManager.getProvider(tag.getProject());
			if (provider == null) {
				return;
			}
			String libName = provider.getLibraryName();
			String libVersion = provider.getLibraryVersion();
			for (Component c : provider.getAllComponents()) {
				if (c.name == null || c.name.isEmpty()) continue;
				if (!c.name.startsWith("el-")) continue;
				if (!namePrefix.isEmpty() && !c.name.toLowerCase().startsWith(namePrefix.toLowerCase())) continue;
				String alias = toPascalAlias(c.name);
				LookupElementBuilder builder = LookupElementBuilder.create(c.name)
					.withInsertHandler((InsertionContext ctx, LookupElement item) -> insertPairedTag(ctx, c.name))
					.withTypeText(c.description, true)
					.withTailText("  as " + alias + (libName.isEmpty() ? "" : "  • " + libName + (libVersion.isEmpty() ? "" : "@" + libVersion)), true)
					.withIcon(com.intellij.icons.AllIcons.Nodes.Tag);
				builder.putUserData(UnifiedCompletionWeigher.OURS_KEY, Boolean.TRUE);
				elements.add(builder);
			}
		} catch (Exception e) {
			LOG.warn("Error adding tag name variants", e);
		}
	}
	
	private void insertPairedTag(@NotNull InsertionContext context, @NotNull String tagName) {
		PsiFile file = context.getFile();
		int start = context.getStartOffset();
		int end = context.getTailOffset();
		String snippet = "<" + tagName + "></" + tagName + ">";
		context.getDocument().replaceString(start, end, snippet);
		int caret = start + tagName.length() + 2; // <tag|></tag>
		context.getEditor().getCaretModel().moveToOffset(caret);
	}
	
	private String toPascalAlias(@NotNull String kebab) {
		// el-table-column -> ElTableColumn
		String[] parts = kebab.split("-");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (p.isEmpty()) continue;
			sb.append(Character.toUpperCase(p.charAt(0)));
			if (p.length() > 1) sb.append(p.substring(1));
		}
		return sb.toString();
	}
	
	@Override
	public @Nullable XmlElementDescriptor getDescriptor(@NotNull XmlTag tag) {
		try {
			var provider = ProviderManager.getProvider(tag.getProject());
			if (provider == null) {
				LOG.debug("No provider found for project: " + tag.getProject().getName());
				return null;
			}
			String tagName = tag.getName();
			if (!provider.supportsTag(tagName)) {
				LOG.debug("Tag not supported: " + tagName);
				return null;
			}
			Component component = provider.getComponent(tagName);
			if (component == null) {
				LOG.debug("Component not found: " + tagName);
				return null;
			}
			var nsDescriptor = tag.getNSDescriptor(tag.getNamespace(), false);
			XmlElementDescriptor originalDescriptor = null;
			if (nsDescriptor != null) {
				originalDescriptor = nsDescriptor.getElementDescriptor(tag);
			}
			return new UnifiedXmlElementDescriptor(component, originalDescriptor, nsDescriptor, tagName);
		} catch (Exception e) {
			LOG.warn("Error getting descriptor for tag: " + tag.getName(), e);
			return null;
		}
	}
} 