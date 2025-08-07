package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusContextAnalyzer;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;

import java.awt.Desktop;
import java.net.URI;

/**
 * Element Plus 文档动作
 * 提供右键菜单快速访问组件文档
 */
public class ElementPlusDocumentationAction extends AnAction {
    
    private final ElementPlusComponentProvider componentProvider;
    private final ElementPlusContextAnalyzer contextAnalyzer;
    
    public ElementPlusDocumentationAction() {
        super("查看 Element Plus 文档", "打开当前组件的官方文档", null);
        this.componentProvider = new ElementPlusComponentProvider();
        this.contextAnalyzer = new ElementPlusContextAnalyzer();
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        
        if (project == null || editor == null || file == null) {
            return;
        }
        
        // 获取当前光标位置的元素
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        
        if (element == null) {
            Messages.showInfoMessage(project, "无法获取当前位置的组件信息", "Element Plus 文档");
            return;
        }
        
        // 获取当前组件名称
        String componentName = contextAnalyzer.getCurrentComponent(element);
        if (componentName == null) {
            Messages.showInfoMessage(project, "当前位置不是 Element Plus 组件", "Element Plus 文档");
            return;
        }
        
        // 获取组件信息
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            Messages.showInfoMessage(project, "未找到组件: " + componentName, "Element Plus 文档");
            return;
        }
        
        // 打开文档链接
        openDocumentation(component, project);
    }
    
    @Override
    public void update(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        
        boolean enabled = false;
        
        if (project != null && editor != null && file != null) {
            // 检查是否在Vue文件中
            if (contextAnalyzer.isInVueTemplate(file.findElementAt(editor.getCaretModel().getOffset()))) {
                // 检查是否在Element Plus组件上
                String componentName = contextAnalyzer.getCurrentComponent(
                    file.findElementAt(editor.getCaretModel().getOffset())
                );
                enabled = componentName != null && componentProvider.hasComponent(componentName);
            }
        }
        
        e.getPresentation().setEnabledAndVisible(enabled);
    }
    
    /**
     * 打开文档链接
     */
    private void openDocumentation(ElementPlusComponent component, Project project) {
        String docUrl = component.getDocUrl();
        
        if (StringUtil.isEmpty(docUrl)) {
            Messages.showInfoMessage(project, 
                "组件 " + component.getName() + " 暂无官方文档链接", 
                "Element Plus 文档");
            return;
        }
        
        try {
            // 尝试在默认浏览器中打开文档
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(docUrl));
            } else {
                // 如果不支持，显示链接信息
                Messages.showInfoMessage(project, 
                    "请手动访问文档链接:\n" + docUrl, 
                    "Element Plus 文档");
            }
        } catch (Exception ex) {
            Messages.showErrorDialog(project, 
                "无法打开文档链接: " + ex.getMessage(), 
                "Element Plus 文档");
        }
    }
}
