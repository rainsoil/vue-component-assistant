package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlTag;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion.ElementPlusSlot;
import com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator;
import com.chu7.vuecomponentassistant.ui.ComponentDocumentationDialog;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Element Plus 文档查看动作
 * 
 * 功能说明：
 * - 右键菜单动作，用于查看组件的详细文档
 * - 弹框显示组件的完整信息，包括属性、事件、插槽等
 * - 提供更好的用户体验，比悬浮提示更详细
 * 
 * @author Vue Component Assistant Team
 * @version 1.0.0
 */
public class ElementPlusDocumentationAction extends AnAction {

    /** 组件数据提供者 */
    private ComponentProvider componentProvider;

    /**
     * 构造函数
     */
    public ElementPlusDocumentationAction() {
        // 组件提供者将在 actionPerformed 中根据项目动态创建
    }

    /**
     * 动作执行方法
     * 
     * @param e 动作事件
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }

        // 根据项目动态创建组件提供者
        if (componentProvider == null) {
            componentProvider = new ComponentProvider(project);
        }

        // 获取当前编辑器
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            Messages.showErrorDialog("无法获取编辑器信息", "错误");
            return;
        }

        // 获取当前光标位置的 PSI 元素
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        if (element == null) {
            // 尝试从编辑器获取元素
            int offset = editor.getCaretModel().getOffset();
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (psiFile != null) {
                element = psiFile.findElementAt(offset);
            }
        }

        if (element == null) {
            Messages.showErrorDialog("无法获取当前元素", "错误");
            return;
        }

        // 提取组件名称
        String componentName = extractComponentName(element);
        if (componentName == null) {
            Messages.showErrorDialog("无法识别组件", "错误");
            return;
        }

        // 检查是否是当前组件库的组件
        if (!componentProvider.isComponentFromCurrentLibrary(componentName)) {
            Messages.showErrorDialog("不是 " + componentProvider.getLibraryDisplayName() + " 组件: " + componentName, "提示");
            return;
        }

        // 获取组件信息
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            Messages.showErrorDialog("找不到组件信息: " + componentName, "错误");
            return;
        }

        // 生成并显示文档
        String documentation = generateDocumentation(component);
        showDocumentationDialog(project, componentName, documentation);
    }

    /**
     * 更新动作状态
     * 
     * @param e 动作事件
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 获取当前元素
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        
        // 检查是否是当前组件库的组件
        boolean isCurrentLibraryComponent = false;
        if (element != null) {
            String componentName = extractComponentName(element);
            if (componentName != null) {
                // 检查组件前缀
                String prefix = "";
                if (componentName.startsWith("el-")) {
                    prefix = "el-";
                } else if (componentName.startsWith("a-")) {
                    prefix = "a-";
                }
                isCurrentLibraryComponent = !prefix.isEmpty();
            }
        }
        
        // 只有在支持的组件库组件上才启用此动作
        e.getPresentation().setEnabledAndVisible(isCurrentLibraryComponent);
    }

    /**
     * 从 PSI 元素中提取组件名称
     * 
     * @param element PSI 元素
     * @return 组件名称
     */
    private String extractComponentName(PsiElement element) {
        if (element == null) {
            return null;
        }

        // 如果是 XML 标签
        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            return tag.getName();
        }

        // 如果是文本元素，尝试解析
        String text = element.getText();
        if (text != null && text.contains("<")) {
            // 简单的标签解析
            int start = text.indexOf('<');
            int end = text.indexOf(' ', start);
            if (end == -1) {
                end = text.indexOf('>', start);
            }
            if (start >= 0 && end > start) {
                return text.substring(start + 1, end).trim();
            }
        }

        // 检查父元素
        PsiElement parent = element.getParent();
        if (parent != null && parent != element) {
            return extractComponentName(parent);
        }

        return null;
    }

    /**
     * 检查是否是支持的组件库组件
     * 
     * @param componentName 组件名称
     * @return 是否是支持的组件库组件
     */
    private boolean isSupportedComponent(String componentName) {
        return componentName != null && (componentName.startsWith("el-") || componentName.startsWith("a-"));
    }

    /**
     * 生成文档内容
     * 
     * @param component 组件信息
     * @return 格式化的文档内容
     */
    private String generateDocumentation(ElementPlusComponent component) {
        return DocumentationStyleGenerator.generateHtmlDocumentation(component);
    }

    /**
     * 显示文档对话框
     * 
     * @param project 当前项目
     * @param componentName 组件名称
     * @param documentation 文档内容
     */
    private void showDocumentationDialog(Project project, String componentName, String documentation) {
        ComponentDocumentationDialog dialog = new ComponentDocumentationDialog(
            project, componentName, documentation
        );
        dialog.show();
    }
}
