package com.chu7;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

/**
 * 显示文档动作
 * 在右键菜单中添加"显示文档"选项
 */
public class ShowDocumentationAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        
        if (project == null || element == null) {
            return;
        }

        // 检查是否是有效的元素（组件标签或属性）
        if (isValidElement(element)) {
            DocumentationDialog dialog = new DocumentationDialog(project, element);
            dialog.show();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        
        // 只有在选中有效元素时才启用此动作
        e.getPresentation().setEnabledAndVisible(isValidElement(element));
    }

    /**
     * 检查元素是否有效（组件标签或属性）
     */
    private boolean isValidElement(PsiElement element) {
        if (element == null) {
            return false;
        }

        // 检查是否是组件标签
        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            String tagName = tag.getName();
            // 检查是否是已知的组件标签（以 el- 开头）
            return tagName != null && tagName.startsWith("el-");
        }

        // 检查是否是属性或事件
        if (element instanceof XmlAttribute) {
            XmlAttribute attribute = (XmlAttribute) element;
            XmlTag parentTag = attribute.getParent();
            if (parentTag != null) {
                String tagName = parentTag.getName();
                // 检查父标签是否是已知的组件标签
                if (tagName != null && tagName.startsWith("el-")) {
                    String attributeName = attribute.getName();
                    // 检查是否是事件（@开头）、属性（:开头或普通属性）或slot
                    return attributeName.startsWith("@") || attributeName.startsWith(":") || 
                           attributeName.startsWith("slot"); // 包含slot属性
                }
            }
        }

        return false;
    }
} 