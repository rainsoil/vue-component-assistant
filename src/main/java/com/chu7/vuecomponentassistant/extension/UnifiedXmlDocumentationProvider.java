package com.chu7.vuecomponentassistant.extension;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.library.model.Event;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.chu7.vuecomponentassistant.documentation.DocumentationHtmlBuilder;

public class UnifiedXmlDocumentationProvider extends AbstractDocumentationProvider {
	private static final Logger LOG = Logger.getInstance(UnifiedXmlDocumentationProvider.class);
	
	@Override
	public @Nullable PsiElement getCustomDocumentationElement(@NotNull Editor editor,
				@NotNull PsiFile file,
				@Nullable PsiElement contextElement,
				int targetOffset) {
		try {
			if (contextElement == null) return null;
			// 若位于属性上，直接返回属性；否则返回标签
			XmlAttribute attribute = PsiTreeUtil.getParentOfType(contextElement, XmlAttribute.class, false);
			if (attribute != null) return attribute;
			XmlTag tag = PsiTreeUtil.getParentOfType(contextElement, XmlTag.class, false);
			if (tag == null) {
				PsiElement at = file.findElementAt(Math.max(0, Math.min(targetOffset, file.getTextLength() - 1)));
				tag = PsiTreeUtil.getParentOfType(at, XmlTag.class, false);
			}
			return tag;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
		try {
			XmlAttribute attr = element instanceof XmlAttribute ? (XmlAttribute) element : PsiTreeUtil.getParentOfType(element, XmlAttribute.class, false);
			if (attr != null) {
				XmlTag tag = attr.getParent();
				if (tag == null) return null;
				var provider = ProviderManager.getProvider(tag.getProject());
				if (provider == null || !provider.supportsTag(tag.getName())) return null;
				Component component = provider.getComponent(tag.getName());
				if (component == null) return null;
				String name = attr.getName();
				if (name.startsWith("@") && component.events != null) {
					String eventName = name.substring(1);
					for (Event e : component.events) {
						if (eventName.equals(e.name)) {
							return DocumentationHtmlBuilder.buildEventHtml(e);
						}
					}
				}
				if (component.props != null) {
					for (Prop p : component.props) {
						if (name.equals(p.name)) {
							return DocumentationHtmlBuilder.buildPropHtml(p);
						}
					}
				}
				return null;
			}
			// 否则组件级
			XmlTag tagCandidate = element instanceof XmlTag ? (XmlTag) element : PsiTreeUtil.getParentOfType(element, XmlTag.class, false);
			if (tagCandidate == null) return null;
			var provider = ProviderManager.getProvider(tagCandidate.getProject());
			if (provider == null || !provider.supportsTag(tagCandidate.getName())) return null;
			Component component = provider.getComponent(tagCandidate.getName());
			if (component == null) return null;
			return DocumentationHtmlBuilder.buildComponentHtml(component);
		} catch (Exception ignored) {
		}
		return null;
	}
}
