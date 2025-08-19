package com.chu7.vuecomponentassistant.documentation;

import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlTag;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Vue Component 组件文档提供者
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>当鼠标悬浮在 Vue 组件上时，显示详细的组件信息</li>
 *   <li>包括组件介绍、属性列表、事件列表、插槽列表、文档链接等</li>
 *   <li>提供完整的中文描述和示例代码</li>
 *   <li>支持项目级和全局级的悬停文档开关控制</li>
 *   <li>智能识别XML标签和文本元素中的组件名称</li>
 *   <li>集成VueKit日志系统进行调试和性能监控</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>继承IntelliJ IDEA的AbstractDocumentationProvider</li>
 *   <li>动态获取最新的组件提供者实例</li>
 *   <li>支持多级设置优先级（项目级 > 全局级）</li>
 *   <li>智能的组件名称提取和匹配算法</li>
 *   <li>完善的异常处理和降级机制</li>
 *   <li>性能监控和调试信息记录</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>开发者在Vue模板中查看组件信息</li>
 *   <li>组件属性和事件的快速查阅</li>
 *   <li>组件使用示例和文档链接获取</li>
 *   <li>开发调试和问题排查</li>
 *   <li>团队协作中的组件文档共享</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.intellij.lang.documentation.AbstractDocumentationProvider
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProvider
 * @see com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator
 */
public class ComponentDocumentationProvider extends AbstractDocumentationProvider {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentDocumentationProvider.class);

    /**
     * 构造函数
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>初始化组件文档提供者实例</li>
     *   <li>记录创建日志信息</li>
     *   <li>组件提供者将在 generateDoc 中根据项目动态创建</li>
     * </ul>
     *
     * <p>设计说明：</p>
     * <ul>
     *   <li>采用延迟初始化策略，避免不必要的资源消耗</li>
     *   <li>使用VueKit日志系统记录创建事件</li>
     *   <li>支持多项目环境下的动态组件提供者管理</li>
     * </ul>
     *
     * @see VueKitConstants#LOG_DOCUMENTATION_PROVIDER_CREATED
     */
    public ComponentDocumentationProvider() {
        // 组件提供者将在 generateDoc 中根据项目动态创建
        VueKitLogger.info(LOG, VueKitConstants.LOG_DOCUMENTATION_PROVIDER_CREATED);
    }

    /**
     * 生成文档内容
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据鼠标悬浮的元素生成相应的组件文档</li>
     *   <li>智能识别元素类型并提取组件名称</li>
     *   <li>检查悬停文档功能的启用状态</li>
     *   <li>动态获取最新的组件提供者实例</li>
     *   <li>生成格式化的HTML文档内容</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取当前项目对象</li>
     *   <li>检查悬停文档功能是否启用</li>
     *   <li>提取组件名称</li>
     *   <li>获取组件详细信息</li>
     *   <li>生成HTML格式的组件文档</li>
     *   <li>记录性能统计信息</li>
     * </ol>
     *
     * @param element 鼠标悬浮的元素，可能为null
     * @param originalElement 原始元素，可能为null
     * @return 格式化的 HTML 文档内容，如果无法生成则返回null
     * @see #isHoverDocumentationEnabled(Project)
     * @see #extractComponentName(PsiElement)
     * @see #generateComponentDocumentation(ElementPlusComponent)
     * @see #generateTestDocumentation(PsiElement)
     */
    @Nullable
    @Override
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        long startTime = System.currentTimeMillis();
        
        // 获取当前项目
        Project project = element != null ? element.getProject() : null;
        if (project == null) {
            VueKitLogger.warn(LOG, VueKitConstants.ERROR_NO_PROJECT);
            return generateTestDocumentation(element);
        }
        
        // 检查悬停文档设置（优先使用项目级设置，如果没有则使用全局设置）
        boolean hoverDocumentationEnabled = isHoverDocumentationEnabled(project);
        if (!hoverDocumentationEnabled) {
            VueKitLogger.debug(LOG, "悬停文档功能已禁用");
            return null; // 返回 null 表示不显示文档
        }
        
        // 添加调试信息
        VueKitLogger.debug(LOG, "=== 文档提供者被调用 ===");
        VueKitLogger.debug(LOG, "元素类型: " + (element != null ? element.getClass().getSimpleName() : "null"));
        VueKitLogger.debug(LOG, "元素文本: " + (element != null ? element.getText() : "null"));

        // 每次都获取最新的组件提供者实例
        ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
        
        // 尝试从不同元素类型中提取组件名称
        String componentName = extractComponentName(element);
        if (componentName == null) {
            VueKitLogger.debug(LOG, "无法提取组件名称");
            return generateTestDocumentation(element);
        }
        
        VueKitLogger.debug(LOG, "提取到组件名称: " + componentName);

        // 检查是否是当前组件库的组件（放宽检查，确保文档功能正常工作）
        boolean isFromCurrentLibrary = componentProvider.isComponentFromCurrentLibrary(componentName);
        VueKitLogger.debug(LOG, "组件 " + componentName + " 是否来自当前库: " + isFromCurrentLibrary);
        
        // 即使不是当前库的组件，也尝试获取组件信息
        // 这样可以确保文档功能能够正常工作

        VueKitLogger.debug(LOG, "检测到 " + componentProvider.getLibraryDisplayName() + " 组件: " + componentName);

        // 获取组件详细信息
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            VueKitLogger.warn(LOG, "找不到组件信息: " + componentName);
            return generateTestDocumentation(element);
        }

        // 生成组件文档
        String documentation = generateComponentDocumentation(component);
        
        // 记录性能信息
        long endTime = System.currentTimeMillis();
        VueKitLogger.debug(LOG, "文档生成耗时: " + (endTime - startTime) + "ms");
        
        return documentation;
    }

    /**
     * 生成测试文档（用于调试）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>当无法获取组件信息时生成调试文档</li>
     *   <li>显示元素的类型、文本和项目信息</li>
     *   <li>帮助开发者诊断文档提供者问题</li>
     *   <li>提供友好的调试界面和说明</li>
     * </ul>
     *
     * <p>调试信息包括：</p>
     * <ul>
     *   <li>元素类型：显示PSI元素的Java类名</li>
     *   <li>元素文本：显示元素的原始文本内容</li>
     *   <li>项目信息：显示元素所属的项目名称</li>
     *   <li>调试说明：提供功能说明和使用指导</li>
     * </ul>
     *
     * @param element 需要调试的元素，可能为null
     * @return 格式化的HTML调试文档内容
     * @see #generateDoc(PsiElement, PsiElement)
     */
    private String generateTestDocumentation(PsiElement element) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; padding: 10px;'>");
        html.append("<h3>🔍 调试信息</h3>");
        html.append("<p><strong>元素类型:</strong> ").append(element != null ? element.getClass().getSimpleName() : "null").append("</p>");
        html.append("<p><strong>元素文本:</strong> ").append(element != null ? element.getText() : "null").append("</p>");
        html.append("<p><strong>项目:</strong> ").append(element != null && element.getProject() != null ? element.getProject().getName() : "null").append("</p>");
        html.append("<hr>");
        html.append("<p><em>这是测试文档，用于调试文档提供者功能。</em></p>");
        html.append("</div>");
        return html.toString();
    }

    /**
     * 检查悬停文档功能是否启用
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查项目级和全局级的悬停文档设置</li>
     *   <li>项目级设置优先于全局设置</li>
     *   <li>如果项目级设置为false则明确禁用功能</li>
     *   <li>提供完整的设置状态日志记录</li>
     * </ul>
     *
     * <p>设置优先级：</p>
     * <ol>
     *   <li>项目级设置：如果存在则优先使用</li>
     *   <li>全局级设置：作为项目级设置的备选</li>
     *   <li>默认值：false（安全默认值）</li>
     * </ol>
     *
     * <p>安全特性：</p>
     * <ul>
     *   <li>完善的异常处理，避免设置获取失败</li>
     *   <li>详细的日志记录，便于问题诊断</li>
     *   <li>安全的默认值，确保功能稳定性</li>
     * </ul>
     *
     * @param project 项目对象，不能为null
     * @return 是否启用悬停文档功能
     * @throws IllegalArgumentException 如果project参数为null
     * @see com.chu7.vuecomponentassistant.settings.ProjectSettingsManager#getInstance(Project)
     * @see com.chu7.vuecomponentassistant.settings.PluginSettings#getInstance()
     */
    private boolean isHoverDocumentationEnabled(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
        try {
            // 首先尝试获取项目级设置
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
            
            if (projectSettings != null) {
                boolean projectEnabled = projectSettings.isEnableHoverDocumentation();
                VueKitLogger.debug(LOG, "项目级悬停文档设置: " + projectEnabled);
                
                // 如果项目级设置为false，明确禁用，不检查全局设置
                if (!projectEnabled) {
                    VueKitLogger.debug(LOG, "项目级设置为false，明确禁用悬停文档");
                    return false;
                }
                
                // 如果项目级设置为true，直接返回true
                return true;
            }
            
            // 如果没有项目级设置，使用全局设置
            PluginSettings globalSettings = PluginSettings.getInstance();
            boolean globalEnabled = globalSettings.isEnableHoverDocumentation();
            VueKitLogger.debug(LOG, "无项目级设置，使用全局悬停文档设置: " + globalEnabled);
            return globalEnabled;
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "获取悬停文档设置失败，使用默认值: false", e);
            return false; // 默认禁用
        }
    }

    /**
     * 从 PSI 元素中提取组件名称
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>智能识别不同类型的PSI元素</li>
     *   <li>支持XML标签和文本元素的组件名称提取</li>
     *   <li>递归检查父元素以找到组件名称</li>
     *   <li>提供详细的提取过程日志记录</li>
     * </ul>
     *
     * <p>提取策略：</p>
     * <ol>
     *   <li>优先检查XML标签元素</li>
     *   <li>解析文本元素中的标签内容</li>
     *   <li>递归检查父元素</li>
     *   <li>返回第一个找到的有效组件名称</li>
     * </ol>
     *
     * <p>支持的元素类型：</p>
     * <ul>
     *   <li>XmlTag：直接获取标签名称</li>
     *   <li>文本元素：解析包含标签的文本内容</li>
     *   <li>其他PSI元素：递归检查父元素</li>
     * </ul>
     *
     * @param element PSI 元素，可能为null
     * @return 提取到的组件名称，如果无法提取则返回null
     * @see com.intellij.psi.xml.XmlTag
     * @see com.intellij.psi.PsiElement#getParent()
     */
    private String extractComponentName(PsiElement element) {
        if (element == null) {
            return null;
        }

        // 如果是 XML 标签
        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            String tagName = tag.getName();
            VueKitLogger.debug(LOG, "从 XML 标签提取到组件名称: " + tagName);
            return tagName;
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
                String componentName = text.substring(start + 1, end).trim();
                VueKitLogger.debug(LOG, "从文本元素提取到组件名称: " + componentName);
                return componentName;
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
     * 生成组件文档
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>调用DocumentationStyleGenerator生成HTML格式的组件文档</li>
     *   <li>将组件信息转换为用户友好的文档格式</li>
     *   <li>支持组件属性、事件、插槽等详细信息展示</li>
     * </ul>
     *
     * <p>文档内容：</p>
     * <ul>
     *   <li>组件基本信息和描述</li>
     *   <li>属性列表及其类型和默认值</li>
     *   <li>事件列表及其参数说明</li>
     *   <li>插槽列表及其作用域</li>
     *   <li>使用示例和文档链接</li>
     * </ul>
     *
     * @param component 组件信息对象，不能为null
     * @return 格式化的 HTML 文档内容
     * @throws IllegalArgumentException 如果component参数为null
     * @see com.chu7.vuecomponentassistant.documentation.DocumentationStyleGenerator#generateHtmlDocumentation(ElementPlusComponent)
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
     */
    private String generateComponentDocumentation(ElementPlusComponent component) {
        if (component == null) {
            throw new IllegalArgumentException("组件对象不能为null");
        }
        
        return DocumentationStyleGenerator.generateHtmlDocumentation(component);
    }
} 