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
 * 功能说明：
 * - 当鼠标悬浮在 Vue 组件上时，显示详细的组件信息
 * - 包括组件介绍、属性列表、事件列表、插槽列表、文档链接等
 * - 提供完整的中文描述和示例代码
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentDocumentationProvider extends AbstractDocumentationProvider {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentDocumentationProvider.class);

    /**
     * 构造函数
     * 组件提供者将在 generateDoc 中根据项目动态创建
     */
    public ComponentDocumentationProvider() {
        // 组件提供者将在 generateDoc 中根据项目动态创建
        VueKitLogger.info(LOG, VueKitConstants.LOG_DOCUMENTATION_PROVIDER_CREATED);
    }

    /**
     * 生成文档内容
     * 
     * @param element 鼠标悬浮的元素
     * @return 格式化的 HTML 文档内容
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

        // 检查是否是当前组件库的组件
        if (!componentProvider.isComponentFromCurrentLibrary(componentName)) {
            VueKitLogger.debug(LOG, "不是 " + componentProvider.getLibraryDisplayName() + " 组件");
            return generateTestDocumentation(element);
        }

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
     * @param element 元素
     * @return 测试文档内容
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
     * 项目级设置优先于全局设置，如果项目级设置为false则明确禁用
     * 
     * @param project 项目对象
     * @return 是否启用悬停文档
     */
    private boolean isHoverDocumentationEnabled(Project project) {
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
     * @param component 组件信息
     * @return 格式化的 HTML 文档内容
     */
    private String generateComponentDocumentation(ElementPlusComponent component) {
        return DocumentationStyleGenerator.generateHtmlDocumentation(component);
    }
} 