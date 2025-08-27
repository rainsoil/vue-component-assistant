package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.documentation.DocumentationHtmlBuilder;
import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ShowComponentDocumentationAction extends AnAction {
	public ShowComponentDocumentationAction() {
		super("显示组件文档");
	}
	
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
		if (project == null || editor == null || file == null) return;
		PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());
		XmlTag tag = PsiTreeUtil.getParentOfType(at, XmlTag.class, false);
		if (tag == null) return;
		var provider = ProviderManager.getProvider(project);
		if (provider == null || !provider.supportsTag(tag.getName())) return;
		Component component = provider.getComponent(tag.getName());
		if (component == null) return;
		String html = DocumentationHtmlBuilder.buildComponentHtml(component);
		showHtmlDialog(project, tag.getName(), html);
	}
	
	private void showHtmlDialog(Project project, String title, String html) {
		JDialog dialog = new JDialog();
		dialog.setTitle("组件文档 - " + title);
		dialog.setModal(false);
		JEditorPane pane = new JEditorPane("text/html", html);
		pane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(pane);
		scrollPane.setPreferredSize(new Dimension(800, 560));
		dialog.getContentPane().add(scrollPane);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
} 