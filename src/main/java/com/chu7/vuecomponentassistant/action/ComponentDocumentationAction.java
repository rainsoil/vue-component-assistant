package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
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

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;

import com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator;
import com.chu7.vuecomponentassistant.ui.ComponentDocumentationDialog;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Vue Component 文档查看动作
 * 
 * 功能说明：
 * - 右键菜单动作，用于查看组件的详细文档
 * - 弹框显示组件的完整信息，包括属性、事件、插槽等
 * - 提供更好的用户体验，比悬浮提示更详细
 * 
 * 使用场景：
 * - 在Vue文件中右键点击组件标签
 * - 需要查看组件的完整文档信息
 * - 需要复制组件文档内容
 * - 需要打开组件的官方文档
 * 
 * 支持的操作：
 * - 查看组件属性列表和说明
 * - 查看组件事件列表和参数
 * - 查看组件插槽列表和作用域
 * - 复制文档内容到剪贴板
 * - 打开组件的官方文档链接
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentDocumentationAction extends AnAction {

    private static final com.intellij.openapi.diagnostic.Logger LOG = 
        com.intellij.openapi.diagnostic.Logger.getInstance(ComponentDocumentationAction.class);

    /**
     * 构造函数
     * 
     * 初始化右键菜单动作，组件提供者将在actionPerformed中根据项目动态创建，
     * 确保每个项目都有独立的组件数据管理。
     */
    public ComponentDocumentationAction() {
        // 组件提供者将在 actionPerformed 中根据项目动态创建
    }

    /**
     * 动作执行方法
     * 
     * 当用户在Vue文件中右键点击组件标签时触发此方法。
     * 执行流程：
     * 1. 检查功能开关设置
     * 2. 获取当前项目和编辑器信息
     * 3. 提取组件名称
     * 4. 验证组件是否支持
     * 5. 生成文档内容
     * 6. 显示文档对话框
     * 
     * @param e 动作事件，包含当前上下文信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        // 检查右键文档设置（优先使用项目级设置，如果没有则使用全局设置）
        if (!isRightClickDocumentationEnabled(project)) {
            Messages.showInfoMessage("右键文档功能已禁用，请在设置中启用", "提示");
            return;
        }

        // 每次都获取最新的组件提供者实例，确保数据是最新的
        ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);

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

        // 检查是否是当前组件库的组件（放宽检查，确保文档功能正常工作）
        boolean isFromCurrentLibrary = componentProvider.isComponentFromCurrentLibrary(componentName);
        LOG.info("组件 " + componentName + " 是否来自当前库: " + isFromCurrentLibrary);
        
        // 即使不是当前库的组件，也尝试获取组件信息
        // 这样可以确保文档功能能够正常工作

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
     * 根据当前上下文动态更新右键菜单项的可见性和启用状态。
     * 只有在支持的组件库组件上才显示此菜单项。
     * 
     * @param e 动作事件
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        
        // 检查右键文档设置（优先使用项目级设置，如果没有则使用全局设置）
        if (!isRightClickDocumentationEnabled(project)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        
        // 获取当前元素
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        
        // 检查是否是当前组件库的组件（放宽检查，确保文档功能正常工作）
        boolean isCurrentLibraryComponent = false;
        if (element != null) {
            String componentName = extractComponentName(element);
            if (componentName != null) {
                // 检查组件前缀，支持更多组件库
                String prefix = "";
                if (componentName.startsWith("el-")) {
                    prefix = "el-";
                } else if (componentName.startsWith("a-")) {
                    prefix = "a-";
                } else if (componentName.startsWith("v-")) {
                    prefix = "v-";
                } else if (componentName.startsWith("q-")) {
                    prefix = "q-";
                } else if (componentName.startsWith("my-")) {
                    prefix = "my-";
                }
                isCurrentLibraryComponent = !prefix.isEmpty();
                
                // 如果没有已知前缀，但看起来像组件名，也启用
                if (!isCurrentLibraryComponent && componentName.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                    isCurrentLibraryComponent = true;
                }
            }
        }
        
        // 启用文档功能，确保用户能够查看组件信息
        e.getPresentation().setEnabledAndVisible(isCurrentLibraryComponent);
    }

    /**
     * 从 PSI 元素中提取组件名称
     * 
     * 递归解析PSI元素树，提取组件标签名称。
     * 支持多种元素类型：XML标签、文本元素等。
     * 
     * @param element PSI 元素
     * @return 组件名称，如果无法提取则返回null
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
     * 通过组件名称前缀判断是否支持该组件库。
     * 目前支持：Element Plus (el-)、Ant Design Vue (a-)
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
     * 使用DocumentationStyleGenerator将组件信息转换为HTML格式的文档。
     * 
     * @param component 组件信息对象
     * @return 格式化的HTML文档内容
     */
    private String generateDocumentation(ElementPlusComponent component) {
        return DocumentationStyleGenerator.generateHtmlDocumentation(component);
    }

    /**
     * 显示文档对话框
     * 
     * 创建并显示组件文档对话框，提供完整的组件信息展示。
     * 
     * @param project 当前项目实例
     * @param componentName 组件名称
     * @param documentation HTML格式的文档内容
     */
    private void showDocumentationDialog(Project project, String componentName, String documentation) {
        ComponentDocumentationDialog dialog = new ComponentDocumentationDialog(
            project, componentName, documentation
        );
        dialog.show();
    }

    /**
     * 检查右键文档功能是否启用
     * 项目级设置优先于全局设置，如果项目级设置为false则明确禁用
     * 
     * @param project 项目对象
     * @return 是否启用右键文档
     */
    private boolean isRightClickDocumentationEnabled(Project project) {
        try {
            // 首先尝试获取项目级设置
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
            
            if (projectSettings != null) {
                boolean projectEnabled = projectSettings.isEnableRightClickDocumentation();
                LOG.debug("项目级右键文档设置: " + projectEnabled);
                
                // 如果项目级设置为false，明确禁用，不检查全局设置
                if (!projectEnabled) {
                    LOG.debug("项目级设置为false，明确禁用右键文档");
                    return false;
                }
                
                // 如果项目级设置为true，直接返回true
                return true;
            }
            
            // 如果没有项目级设置，使用全局设置
            PluginSettings globalSettings = PluginSettings.getInstance();
            boolean globalEnabled = globalSettings.isEnableRightClickDocumentation();
            LOG.debug("无项目级设置，使用全局右键文档设置: " + globalEnabled);
            return globalEnabled;
            
        } catch (Exception e) {
            LOG.warn("获取右键文档设置失败，使用默认值: false", e);
            return false; // 默认禁用
        }
    }
} 