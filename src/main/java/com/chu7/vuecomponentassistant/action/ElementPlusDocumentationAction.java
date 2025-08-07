package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponentProvider;
import com.chu7.vuecomponentassistant.completion.ElementPlusProp;
import com.chu7.vuecomponentassistant.completion.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion.ElementPlusSlot;
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
    private final ElementPlusComponentProvider componentProvider;

    /**
     * 构造函数
     */
    public ElementPlusDocumentationAction() {
        this.componentProvider = new ElementPlusComponentProvider();
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
            element = editor.getDocument().getPsiFile(project).findElementAt(offset);
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

        // 检查是否是 Element Plus 组件
        if (!isElementPlusComponent(componentName)) {
            Messages.showErrorDialog("不是 Element Plus 组件: " + componentName, "提示");
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
        showDocumentationDialog(componentName, documentation);
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
        
        // 检查是否是 Element Plus 组件
        boolean isElementPlusComponent = false;
        if (element != null) {
            String componentName = extractComponentName(element);
            isElementPlusComponent = isElementPlusComponent(componentName);
        }
        
        // 只有在 Element Plus 组件上才启用此动作
        e.getPresentation().setEnabledAndVisible(isElementPlusComponent);
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
     * 检查是否是 Element Plus 组件
     * 
     * @param componentName 组件名称
     * @return 是否是 Element Plus 组件
     */
    private boolean isElementPlusComponent(String componentName) {
        return componentName != null && componentName.startsWith("el-");
    }

    /**
     * 生成文档内容
     * 
     * @param component 组件信息
     * @return 格式化的文档内容
     */
    private String generateDocumentation(ElementPlusComponent component) {
        StringBuilder doc = new StringBuilder();
        
        // 组件标题
        doc.append("📋 ").append(component.getName()).append("\n");
        doc.append("=".repeat(50)).append("\n\n");
        
        // 组件描述
        if (component.getDescription() != null && !component.getDescription().isEmpty()) {
            doc.append("📝 组件描述:\n");
            doc.append(component.getDescription()).append("\n\n");
        }

        // 属性列表
        List<ElementPlusProp> props = component.getProps();
        if (props != null && !props.isEmpty()) {
            doc.append("🔧 属性列表:\n");
            doc.append("-".repeat(30)).append("\n");
            for (ElementPlusProp prop : props) {
                doc.append("• ").append(prop.getName());
                if (prop.getDescription() != null && !prop.getDescription().isEmpty()) {
                    doc.append(" - ").append(prop.getDescription());
                }
                if (prop.getDefaultValue() != null) {
                    doc.append(" (默认值: ").append(prop.getDefaultValueAsString()).append(")");
                }
                doc.append("\n");
            }
            doc.append("\n");
        }

        // 事件列表
        List<ElementPlusEvent> events = component.getEvents();
        if (events != null && !events.isEmpty()) {
            doc.append("🎯 事件列表:\n");
            doc.append("-".repeat(30)).append("\n");
            for (ElementPlusEvent event : events) {
                doc.append("• @").append(event.getName());
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    doc.append(" - ").append(event.getDescription());
                }
                doc.append("\n");
            }
            doc.append("\n");
        }

        // 插槽列表
        List<ElementPlusSlot> slots = component.getSlots();
        if (slots != null && !slots.isEmpty()) {
            doc.append("🔌 插槽列表:\n");
            doc.append("-".repeat(30)).append("\n");
            for (ElementPlusSlot slot : slots) {
                doc.append("• #").append(slot.getName());
                if (slot.getDescription() != null && !slot.getDescription().isEmpty()) {
                    doc.append(" - ").append(slot.getDescription());
                }
                if (slot.getScope() != null && !slot.getScope().isEmpty()) {
                    doc.append(" (作用域: ").append(slot.getScope()).append(")");
                }
                doc.append("\n");
            }
            doc.append("\n");
        }

        // 使用示例
        doc.append("💡 使用示例:\n");
        doc.append("-".repeat(30)).append("\n");
        doc.append("<").append(component.getName()).append(">\n");
        doc.append("  <!-- 组件内容 -->\n");
        doc.append("</").append(component.getName()).append(">\n\n");

        // 文档链接
        doc.append("📖 相关文档:\n");
        doc.append("-".repeat(30)).append("\n");
        doc.append("官方文档: https://element-plus.org/zh-CN/component/")
           .append(component.getName().substring(3)).append(".html\n");

        return doc.toString();
    }

    /**
     * 显示文档对话框
     * 
     * @param componentName 组件名称
     * @param documentation 文档内容
     */
    private void showDocumentationDialog(String componentName, String documentation) {
        Messages.showInfoMessage(
            documentation,
            "📚 " + componentName + " 组件文档"
        );
    }
}
