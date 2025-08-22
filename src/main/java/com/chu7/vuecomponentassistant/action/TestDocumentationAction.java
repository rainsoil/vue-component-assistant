package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.documentation.ElementStyleDocumentationProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.ui.Messages;

/**
 * 测试文档提供者的动作
 */
public class TestDocumentationAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取项目", "错误");
            return;
        }

        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null) {
            Messages.showErrorDialog("无法获取PSI元素", "错误");
            return;
        }

        System.out.println("=== TestDocumentationAction 被触发 ===");
        System.out.println("element = " + element.getClass().getSimpleName());
        System.out.println("element text = " + element.getText());

        // 手动调用文档提供者
        ElementStyleDocumentationProvider provider = new ElementStyleDocumentationProvider();
        String doc = provider.generateDoc(element, element);
        
        if (doc != null) {
            Messages.showInfoMessage(doc, "文档测试结果");
        } else {
            Messages.showErrorDialog("文档生成失败", "错误");
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
