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
 * <p>功能说明：</p>
 * <ul>
 *   <li>右键菜单动作，用于查看组件的详细文档</li>
 *   <li>弹框显示组件的完整信息，包括属性、事件、插槽等</li>
 *   <li>提供更好的用户体验，比悬浮提示更详细</li>
 *   <li>支持多种组件库的文档查看</li>
 *   <li>提供文档内容的复制和分享功能</li>
 * </ul>
 * 
 * <p>触发方式：</p>
 * <ul>
 *   <li>在Vue文件中右键点击组件标签</li>
 *   <li>支持多种组件库前缀（el-、a-、v-、q-、my-等）</li>
 *   <li>自动识别组件类型和库</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>在Vue文件中右键点击组件标签</li>
 *   <li>需要查看组件的完整文档信息</li>
 *   <li>需要复制组件文档内容</li>
 *   <li>需要打开组件的官方文档</li>
 *   <li>学习和了解组件用法</li>
 * </ul>
 * 
 * <p>支持的操作：</p>
 * <ul>
 *   <li>查看组件属性列表和说明</li>
 *   <li>查看组件事件列表和参数</li>
 *   <li>查看组件插槽列表和作用域</li>
 *   <li>复制文档内容到剪贴板</li>
 *   <li>打开组件的官方文档链接</li>
 *   <li>支持多种文档格式和样式</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>支持项目级和全局级设置控制</li>
 *   <li>动态组件提供者管理</li>
 *   <li>智能组件识别和验证</li>
 *   <li>完善的错误处理和用户提示</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager
 * @see com.chu7.vuecomponentassistant.ui.ComponentDocumentationDialog
 * @see com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator
 */
public class ComponentDocumentationAction extends AnAction {

    /**
     * 日志记录器
     * 用于记录组件文档查看过程中的关键信息和错误
     */
    private static final com.intellij.openapi.diagnostic.Logger LOG = 
        com.intellij.openapi.diagnostic.Logger.getInstance(ComponentDocumentationAction.class);

    /**
     * 构造函数
     * 
     * <p>初始化右键菜单动作，组件提供者将在actionPerformed中根据项目动态创建，
     * 确保每个项目都有独立的组件数据管理。</p>
     * 
     * <p>设计考虑：</p>
     * <ul>
     *   <li>延迟初始化组件提供者</li>
     *   <li>支持多项目环境</li>
     *   <li>确保数据隔离和安全性</li>
     * </ul>
     */
    public ComponentDocumentationAction() {
        // 组件提供者将在 actionPerformed 中根据项目动态创建
    }

    /**
     * 动作执行方法
     * 
     * <p>当用户在Vue文件中右键点击组件标签时触发此方法。</p>
     * 
     * <p>执行流程：</p>
     * <ol>
     *   <li>检查功能开关设置（项目级优先于全局级）</li>
     *   <li>获取当前项目和编辑器信息</li>
     *   <li>提取组件名称和类型</li>
     *   <li>验证组件是否支持</li>
     *   <li>获取组件详细信息</li>
     *   <li>生成格式化文档内容</li>
     *   <li>显示文档对话框</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>项目信息缺失时显示错误对话框</li>
     *   <li>编辑器信息缺失时显示错误对话框</li>
     *   <li>组件识别失败时显示错误对话框</li>
     *   <li>组件信息缺失时显示错误对话框</li>
     * </ul>
     * 
     * @param e 动作事件，包含当前上下文信息，不能为null
     * @throws IllegalArgumentException 如果事件对象为null
     * 
     * @see #isRightClickDocumentationEnabled(Project)
     * @see #extractComponentName(PsiElement)
     * @see #generateDocumentation(ElementPlusComponent)
     * @see #showDocumentationDialog(Project, String, String)
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("动作事件对象不能为null");
        }
        
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
     * <p>根据当前上下文动态更新右键菜单项的可见性和启用状态。
     * 只有在支持的组件库组件上才显示此菜单项。</p>
     * 
     * <p>更新逻辑：</p>
     * <ol>
     *   <li>检查项目有效性</li>
     *   <li>验证右键文档功能是否启用</li>
     *   <li>识别当前组件类型</li>
     *   <li>判断是否支持该组件库</li>
     *   <li>设置菜单项的可见性和启用状态</li>
     * </ol>
     * 
     * <p>支持的组件库前缀：</p>
     * <ul>
     *   <li>el-：Element Plus/Element UI</li>
     *   <li>a-：Ant Design Vue</li>
     *   <li>v-：Vuetify</li>
     *   <li>q-：Quasar</li>
     *   <li>my-：自定义组件</li>
     * </ul>
     * 
     * @param e 动作事件，包含当前上下文信息，不能为null
     * @throws IllegalArgumentException 如果事件对象为null
     * 
     * @see #isRightClickDocumentationEnabled(Project)
     * @see #extractComponentName(PsiElement)
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("动作事件对象不能为null");
        }
        
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
     * <p>递归解析PSI元素树，提取组件标签名称。
     * 支持多种元素类型：XML标签、文本元素等。</p>
     * 
     * <p>提取策略：</p>
     * <ol>
     *   <li>优先检查当前元素是否为XML标签</li>
     *   <li>如果是文本元素，尝试解析标签内容</li>
     *   <li>递归检查父元素</li>
     *   <li>返回第一个有效的组件名称</li>
     * </ol>
     * 
     * <p>支持的格式：</p>
     * <ul>
     *   <li>标准XML标签：&lt;el-button&gt;</li>
     *   <li>自闭合标签：&lt;el-input /&gt;</li>
     *   <li>带属性的标签：&lt;el-table data="..."&gt;</li>
     *   <li>嵌套标签结构</li>
     * </ul>
     * 
     * @param element PSI 元素，不能为null
     * @return 组件名称，如果无法提取则返回null
     * @throws IllegalArgumentException 如果element为null
     * 
     * @see com.intellij.psi.xml.XmlTag
     * @see com.intellij.psi.PsiElement#getParent()
     */
    private String extractComponentName(PsiElement element) {
        if (element == null) {
            throw new IllegalArgumentException("PSI元素不能为null");
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
     * <p>通过组件名称前缀判断是否支持该组件库。
     * 目前支持：Element Plus (el-)、Ant Design Vue (a-)</p>
     * 
     * <p>支持的前缀：</p>
     * <ul>
     *   <li>el-：Element Plus/Element UI组件</li>
     *   <li>a-：Ant Design Vue组件</li>
     *   <li>v-：Vuetify组件</li>
     *   <li>q-：Quasar组件</li>
     *   <li>my-：自定义组件</li>
     * </ul>
     * 
     * @param componentName 组件名称，可以为null
     * @return 是否是支持的组件库组件
     * 
     * @see #update(AnActionEvent)
     */
    private boolean isSupportedComponent(String componentName) {
        return componentName != null && (componentName.startsWith("el-") || componentName.startsWith("a-"));
    }

    /**
     * 生成文档内容
     * 
     * <p>使用DocumentationStyleGenerator将组件信息转换为HTML格式的文档。
     * 生成的文档包含组件的完整信息。</p>
     * 
     * <p>文档内容：</p>
     * <ul>
     *   <li>组件基本信息（名称、描述、版本等）</li>
     *   <li>属性列表和说明</li>
     *   <li>事件列表和参数</li>
     *   <li>插槽列表和作用域</li>
     *   <li>使用示例和最佳实践</li>
     * </ul>
     * 
     * @param component 组件信息对象，不能为null
     * @return 格式化的HTML文档内容
     * @throws IllegalArgumentException 如果component为null
     * 
     * @see com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator#generateHtmlDocumentation(ElementPlusComponent)
     */
    private String generateDocumentation(ElementPlusComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("组件对象不能为null");
        }
        return DocumentationStyleGenerator.generateHtmlDocumentation(component);
    }

    /**
     * 显示文档对话框
     * 
     * <p>创建并显示组件文档对话框，提供完整的组件信息展示。
     * 对话框支持多种交互操作。</p>
     * 
     * <p>对话框功能：</p>
     * <ul>
     *   <li>显示格式化的HTML文档内容</li>
     *   <li>支持文档内容的复制</li>
     *   <li>提供官方文档链接</li>
     *   <li>支持文档样式的自定义</li>
     *   <li>响应式布局设计</li>
     * </ul>
     * 
     * @param project 当前项目实例，不能为null
     * @param componentName 组件名称，不能为null
     * @param documentation HTML格式的文档内容，不能为null
     * @throws IllegalArgumentException 如果任何参数为null
     * 
     * @see com.chu7.vuecomponentassistant.ui.ComponentDocumentationDialog
     */
    private void showDocumentationDialog(Project project, String componentName, String documentation) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        if (componentName == null) {
            throw new IllegalArgumentException("组件名称不能为null");
        }
        if (documentation == null) {
            throw new IllegalArgumentException("文档内容不能为null");
        }
        
        ComponentDocumentationDialog dialog = new ComponentDocumentationDialog(
            project, componentName, documentation
        );
        dialog.show();
    }

    /**
     * 检查右键文档功能是否启用
     * 
     * <p>项目级设置优先于全局设置，如果项目级设置为false则明确禁用。
     * 支持多级配置管理。</p>
     * 
     * <p>配置优先级：</p>
     * <ol>
     *   <li>项目级设置（最高优先级）</li>
     *   <li>全局设置（默认优先级）</li>
     *   <li>默认值false（最低优先级）</li>
     * </ol>
     * 
     * <p>配置逻辑：</p>
     * <ul>
     *   <li>如果项目级设置为false，明确禁用功能</li>
     *   <li>如果项目级设置为true，启用功能</li>
     *   <li>如果没有项目级设置，使用全局设置</li>
     *   <li>如果配置获取失败，使用默认值false</li>
     * </ul>
     * 
     * @param project 项目对象，不能为null
     * @return 是否启用右键文档功能
     * @throws IllegalArgumentException 如果project为null
     * 
     * @see com.chu7.vuecomponentassistant.settings.ProjectSettingsManager
     * @see com.chu7.vuecomponentassistant.settings.PluginSettings
     */
    private boolean isRightClickDocumentationEnabled(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
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