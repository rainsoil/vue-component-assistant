package com.chu7.vuecomponentassistant.documentation;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusProp;
import com.chu7.vuecomponentassistant.service.FrameworkDetectService;
import com.chu7.vuecomponentassistant.service.ComponentCacheService;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * VueKit 组件文档提供者 - HTML格式文档生成
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供悬停文档和右键文档</li>
 *   <li>HTML格式显示，支持富文本和表格</li>
 *   <li>集成缓存服务，响应速度快</li>
 *   <li>支持组件、属性、事件、插槽的详细文档</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>继承 AbstractDocumentationProvider</li>
 *   <li>动态生成HTML格式的文档</li>
 *   <li>智能识别元素类型</li>
 *   <li>自动链接到官方文档</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.intellij.lang.documentation.AbstractDocumentationProvider
 */
public class ComponentDocumentationProvider extends AbstractDocumentationProvider {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentDocumentationProvider.class);
    
    static {
        LOG.info("=== ComponentDocumentationProvider 类被加载 ===");
    }

    /**
     * 构造函数
     */
    public ComponentDocumentationProvider() {
        LOG.info("=== ComponentDocumentationProvider 实例被创建 ===");
    }
    
    // 文档缓存
    private static final ConcurrentMap<String, String> documentCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Long> documentCacheTimestamps = new ConcurrentHashMap<>();
    private static final long DOCUMENT_CACHE_TTL = 15 * 60 * 1000; // 15分钟缓存

    /**
     * 获取文档的URL链接
     */
    @Override
    public java.util.List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        LOG.info("=== ComponentDocumentationProvider.getUrlFor 被调用 ===");
        LOG.info("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        LOG.info("originalElement = " + (originalElement != null ? originalElement.getClass().getSimpleName() : "null"));
        
        try {
            if (element == null) {
                LOG.info("getUrlFor: element is null");
                return java.util.Collections.emptyList();
            }

            Project project = element.getProject();
            if (project == null) {
                LOG.info("getUrlFor: project is null");
                return java.util.Collections.emptyList();
            }

            // 检查项目设置是否启用组件补全
            if (!isComponentCompletionEnabled(project)) {
                LOG.info("getUrlFor: component completion disabled");
                return java.util.Collections.emptyList();
            }

            // 检查框架检测服务是否支持当前项目
            FrameworkDetectService frameworkDetectService = FrameworkDetectService.getInstance(project);
            if (!frameworkDetectService.isSupportedFramework()) {
                LOG.info("getUrlFor: framework not supported");
                return java.util.Collections.emptyList();
            }

            String componentName = extractComponentName(element);
            LOG.info("getUrlFor: extracted component name: " + componentName);
            if (componentName == null) {
                LOG.info("getUrlFor: no component name found");
                return java.util.Collections.emptyList();
            }

            // 获取官方文档链接
            String officialUrl = frameworkDetectService.getOfficialDocumentationUrl(componentName);
            LOG.info("getUrlFor: official URL: " + officialUrl);
            if (officialUrl != null) {
                return java.util.Collections.singletonList(officialUrl);
            }

        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取文档URL失败", e);
        }

        return java.util.Collections.emptyList();
    }

    /**
     * 生成文档内容
     */
    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        // 强制输出日志，确保能看到是否被调用
        LOG.info("=== ComponentDocumentationProvider.generateDoc 被调用 ===");
        LOG.info("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        LOG.info("originalElement = " + (originalElement != null ? originalElement.getClass().getSimpleName() : "null"));
        
        try {
            if (element == null) {
                LOG.info("generateDoc: element is null");
                return null;
            }

            Project project = element.getProject();
        if (project == null) {
                LOG.info("generateDoc: project is null");
                return null;
            }

            LOG.info("generateDoc: element type = " + element.getClass().getSimpleName() + 
                ", element text = " + element.getText());
            LOG.info("generateDoc: element full class name = " + element.getClass().getName());
            LOG.info("generateDoc: element toString = " + element.toString());

            // 检查项目设置是否启用组件补全
            if (!isComponentCompletionEnabled(project)) {
                LOG.info("generateDoc: component completion disabled");
                return null;
            }

            // 检查框架检测服务是否支持当前项目
            FrameworkDetectService frameworkDetectService = FrameworkDetectService.getInstance(project);
            if (!frameworkDetectService.isSupportedFramework()) {
                LOG.info("generateDoc: framework not supported");
                return null;
            }

            // 根据元素类型生成不同的文档
            if (element instanceof HtmlTag) {
                String tagName = ((HtmlTag) element).getName();
                LOG.info("generateDoc: HtmlTag detected, tagName = " + tagName);
                return generateComponentDocument(project, tagName);
            } else if (element instanceof XmlTag) {
                String tagName = ((XmlTag) element).getName();
                LOG.info("generateDoc: XmlTag detected, tagName = " + tagName);
                return generateComponentDocument(project, tagName);
            } else if (element instanceof XmlAttribute && element.getParent() instanceof HtmlTag) {
                String componentName = ((HtmlTag) element.getParent()).getName();
                String attributeName = ((XmlAttribute) element).getName();
                LOG.info("generateDoc: HtmlTag attribute detected, component = " + componentName + ", attribute = " + attributeName);
                return generateAttributeDocument(project, componentName, attributeName);
            } else if (element instanceof XmlAttribute && element.getParent() instanceof XmlTag) {
                String componentName = ((XmlTag) element.getParent()).getName();
                String attributeName = ((XmlAttribute) element).getName();
                LOG.info("generateDoc: XmlTag attribute detected, component = " + componentName + ", attribute = " + attributeName);
                return generateAttributeDocument(project, componentName, attributeName);
            } else {
                // 尝试通过反射或父元素查找组件标签
                PsiElement parent = element.getParent();
                LOG.info("generateDoc: checking parent element...");
                if (parent != null) {
                    LOG.info("generateDoc: parent type = " + parent.getClass().getSimpleName());
                    LOG.info("generateDoc: parent full class name = " + parent.getClass().getName());
                    
                    // 检查父元素是否为标签
                    if (parent instanceof HtmlTag) {
                        String tagName = ((HtmlTag) parent).getName();
                        if (tagName != null && tagName.startsWith("el-")) {
                            LOG.info("generateDoc: Found component via HtmlTag parent, tagName = " + tagName);
                            return generateComponentDocument(project, tagName);
                        }
                    } else if (parent instanceof XmlTag) {
                        String tagName = ((XmlTag) parent).getName();
                        if (tagName != null && tagName.startsWith("el-")) {
                            LOG.info("generateDoc: Found component via XmlTag parent, tagName = " + tagName);
                            return generateComponentDocument(project, tagName);
                        }
                    }
                }
                
                // 尝试检查元素文本是否包含组件名
                String elementText = element.getText();
                if (elementText != null && elementText.contains("el-")) {
                    LOG.info("generateDoc: element text contains el-, trying to extract component name");
                    // 简单提取 el- 开头的组件名
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<(el-[\\w-]+)");
                    java.util.regex.Matcher matcher = pattern.matcher(elementText);
                    if (matcher.find()) {
                        String componentName = matcher.group(1);
                        LOG.info("generateDoc: extracted component name from text: " + componentName);
                        return generateComponentDocument(project, componentName);
                    }
                }
            }
            
            LOG.info("generateDoc: unsupported element type: " + element.getClass().getSimpleName());
            LOG.info("generateDoc: element parent type: " + (element.getParent() != null ? element.getParent().getClass().getSimpleName() : "null"));
            LOG.info("generateDoc: element text: " + element.getText());
            LOG.info("generateDoc: element parent text: " + (element.getParent() != null ? element.getParent().getText() : "null"));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "生成文档失败", e);
        }

        return null;
    }

    /**
     * 生成组件文档
     */
    private String generateComponentDocument(Project project, String componentName) {
        LOG.info("generateComponentDocument: called for component: " + componentName);
        
        if (componentName == null || componentName.isEmpty()) {
            LOG.info("generateComponentDocument: componentName is null or empty");
            return null;
        }

        // 检查缓存
        String cacheKey = "component:" + componentName;
        String cached = documentCache.get(cacheKey);
        Long timestamp = documentCacheTimestamps.get(cacheKey);
        
        if (cached != null && timestamp != null && 
            System.currentTimeMillis() - timestamp < DOCUMENT_CACHE_TTL) {
            LOG.info("使用缓存的组件文档: " + componentName);
            return cached;
        }
        
        try {
            // 获取组件信息
        ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                LOG.info("generateComponentDocument: ComponentProvider is null");
                return null;
            }

            ElementPlusComponent component = componentProvider.getComponent(componentName);
            if (component == null) {
                LOG.info("generateComponentDocument: component not found: " + componentName);
                return null;
            }

            LOG.info("generateComponentDocument: found component: " + component.getName());

            // 直接生成组件HTML文档
            String html = generateComponentHtml(component);
            if (html != null) {
                // 缓存文档
                documentCache.put(cacheKey, html);
                documentCacheTimestamps.put(cacheKey, System.currentTimeMillis());
                LOG.info("生成了组件文档: " + componentName);
                return html;
            } else {
                LOG.info("generateComponentDocument: failed to generate HTML for component: " + componentName);
            }
            
        } catch (Exception e) {
            LOG.error("生成组件文档失败: " + componentName, e);
        }

        return null;
    }

    /**
     * 生成属性文档
     */
    private String generateAttributeDocument(Project project, String componentName, String attributeName) {
        LOG.info("generateAttributeDocument: called for component: " + componentName + ", attribute: " + attributeName);
        
        if (componentName == null || attributeName == null) {
            LOG.info("generateAttributeDocument: componentName or attributeName is null");
            return null;
        }

        // 检查缓存
        String cacheKey = "attribute:" + componentName + ":" + attributeName;
        String cached = documentCache.get(cacheKey);
        Long timestamp = documentCacheTimestamps.get(cacheKey);
        
        if (cached != null && timestamp != null && 
            System.currentTimeMillis() - timestamp < DOCUMENT_CACHE_TTL) {
            LOG.info("使用缓存的属性文档: " + componentName + "." + attributeName);
            return cached;
        }

        try {
            // 获取组件信息
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                LOG.info("generateAttributeDocument: ComponentProvider is null");
                return null;
            }

        ElementPlusComponent component = componentProvider.getComponent(componentName);
            if (component == null) {
                LOG.info("generateAttributeDocument: component not found: " + componentName);
                return null;
            }

            LOG.info("generateAttributeDocument: found component: " + component.getName());

            // 生成属性文档
            String html = generateAttributeHtml(component, attributeName);
            if (html != null) {
                // 缓存文档
                documentCache.put(cacheKey, html);
                documentCacheTimestamps.put(cacheKey, System.currentTimeMillis());
                LOG.info("生成了属性文档: " + componentName + "." + attributeName);
                return html;
            } else {
                LOG.info("generateAttributeDocument: failed to generate HTML for attribute: " + componentName + "." + attributeName);
            }

        } catch (Exception e) {
            LOG.error("生成属性文档失败: " + componentName + "." + attributeName, e);
        }

        return null;
    }

    /**
     * 生成组件HTML文档
     */
    private String generateComponentHtml(ElementPlusComponent component) {
        LOG.info("generateComponentHtml: called for component: " + (component != null ? component.getName() : "null"));
        
        if (component == null) {
            LOG.info("generateComponentHtml: component is null");
            return null;
        }

        LOG.info("generateComponentHtml: component name = " + component.getName());
        LOG.info("generateComponentHtml: component props count = " + (component.getProps() != null ? component.getProps().size() : 0));
        LOG.info("generateComponentHtml: component events count = " + (component.getEvents() != null ? component.getEvents().size() : 0));
        LOG.info("generateComponentHtml: component slots count = " + (component.getSlots() != null ? component.getSlots().size() : 0));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; }");
        sb.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        sb.append("th { background-color: #f2f2f2; font-weight: bold; }");
        sb.append("h2 { color: #333; margin: 20px 0 10px 0; border-bottom: 2px solid #007acc; }");
        sb.append("h3 { color: #555; margin: 15px 0 8px 0; }");
        sb.append(".section { margin: 20px 0; }");
        sb.append("</style>");
        sb.append("<body>");

        // 组件标题
        sb.append("<h2>").append(component.getName()).append(" 组件</h2>");
        
        // 组件描述
        if (component.getDescription() != null && !component.getDescription().isEmpty()) {
            sb.append("<div class='section'>");
            sb.append("<h3>组件描述</h3>");
            sb.append("<p>").append(component.getDescription()).append("</p>");
            sb.append("</div>");
        }

        // 属性列表
        if (component.getProps() != null && !component.getProps().isEmpty()) {
            sb.append("<div class='section'>");
            sb.append("<h3>属性列表</h3>");
            sb.append("<table>");
            sb.append("<tr><th>属性名</th><th>类型</th><th>描述</th><th>默认值</th></tr>");
            
            for (ElementPlusProp prop : component.getProps()) {
                sb.append("<tr>");
                sb.append("<td><strong>").append(prop.getName()).append("</strong></td>");
                sb.append("<td>").append(prop.getType() != null ? prop.getType() : "string").append("</td>");
                sb.append("<td>").append(prop.getDescription() != null ? prop.getDescription() : "").append("</td>");
                sb.append("<td>").append(prop.getDefaultValue() != null ? prop.getDefaultValue() : "").append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("</div>");
        }

        // 事件列表
        if (component.getEvents() != null && !component.getEvents().isEmpty()) {
            sb.append("<div class='section'>");
            sb.append("<h3>事件列表</h3>");
            sb.append("<table>");
            sb.append("<tr><th>事件名</th><th>描述</th><th>参数</th></tr>");
            
            for (ElementPlusEvent event : component.getEvents()) {
                sb.append("<tr>");
                sb.append("<td><strong>@").append(event.getName()).append("</strong></td>");
                sb.append("<td>").append(event.getDescription() != null ? event.getDescription() : "").append("</td>");
                sb.append("<td>").append(event.getParameters() != null ? event.getParameters() : "").append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("</div>");
        }

        // 插槽列表
        if (component.getSlots() != null && !component.getSlots().isEmpty()) {
            sb.append("<div class='section'>");
            sb.append("<h3>插槽列表</h3>");
            sb.append("<table>");
            sb.append("<tr><th>插槽名</th><th>描述</th></tr>");
            
            for (var slot : component.getSlots()) {
                sb.append("<tr>");
                sb.append("<td><strong>#").append(slot.getName()).append("</strong></td>");
                sb.append("<td>").append(slot.getDescription() != null ? slot.getDescription() : "").append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("</div>");
        }

        sb.append("</body></html>");
        String html = sb.toString();
        LOG.info("generateComponentHtml: generated HTML length = " + html.length());
        LOG.info("generateComponentHtml: HTML preview = " + html.substring(0, Math.min(200, html.length())) + "...");
        return html;
    }

    /**
     * 生成属性HTML文档
     */
    private String generateAttributeHtml(ElementPlusComponent component, String attributeName) {
        if (component == null || attributeName == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        sb.append("table { width: 100%; border-collapse: collapse; margin: 10px 0; }");
        sb.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        sb.append("th { background-color: #f2f2f2; font-weight: bold; }");
        sb.append("h2 { color: #333; margin: 20px 0 10px 0; border-bottom: 2px solid #007acc; }");
        sb.append("h3 { color: #555; margin: 15px 0 8px 0; }");
        sb.append(".section { margin: 20px 0; }");
        sb.append("</style>");
        sb.append("<body>");

        // 检查是否是事件属性
        if (attributeName.startsWith("@")) {
            String eventName = attributeName.substring(1);
            generateEventHtml(sb, component, eventName);
        } else {
            generatePropertyHtml(sb, component, attributeName);
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 生成属性HTML
     */
    private void generatePropertyHtml(StringBuilder sb, ElementPlusComponent component, String propertyName) {
        if (component.getProps() == null) {
            return;
        }

        for (ElementPlusProp prop : component.getProps()) {
            if (propertyName.equals(prop.getName())) {
                sb.append("<h2>").append(component.getName()).append(" - ").append(propertyName).append("</h2>");
                
                // 属性描述
                if (prop.getDescription() != null && !prop.getDescription().isEmpty()) {
                    sb.append("<div class='section'>");
                    sb.append("<h3>属性描述</h3>");
                    sb.append("<p>").append(prop.getDescription()).append("</p>");
                    sb.append("</div>");
                }
                
                // 属性详情表格
                sb.append("<div class='section'>");
                sb.append("<h3>属性详情</h3>");
                sb.append("<table>");
                sb.append("<tr><th>属性</th><th>值</th></tr>");
                sb.append("<tr><td>名称</td><td><strong>").append(prop.getName()).append("</strong></td></tr>");
                sb.append("<tr><td>类型</td><td>").append(prop.getType() != null ? prop.getType() : "string").append("</td></tr>");
                sb.append("<tr><td>描述</td><td>").append(prop.getDescription() != null ? prop.getDescription() : "").append("</td></tr>");
                sb.append("<tr><td>默认值</td><td>").append(prop.getDefaultValue() != null ? prop.getDefaultValue() : "").append("</td></tr>");
                
                // 如果有选项值，也显示出来
                if (prop.getOptions() != null && !prop.getOptions().isEmpty()) {
                    sb.append("<tr><td>可选值</td><td>").append(String.join(", ", prop.getOptions())).append("</td></tr>");
                }
                
                sb.append("</table>");
                sb.append("</div>");
                break;
            }
        }
    }

    /**
     * 生成事件HTML
     */
    private void generateEventHtml(StringBuilder sb, ElementPlusComponent component, String eventName) {
        if (component.getEvents() == null) {
            return;
        }

        for (ElementPlusEvent event : component.getEvents()) {
            if (eventName.equals(event.getName())) {
                sb.append("<h2>").append(component.getName()).append(" - @").append(eventName).append("</h2>");
                
                // 事件描述
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    sb.append("<div class='section'>");
                    sb.append("<h3>事件描述</h3>");
                    sb.append("<p>").append(event.getDescription()).append("</p>");
                    sb.append("</div>");
                }
                
                // 事件详情表格
                sb.append("<div class='section'>");
                sb.append("<h3>事件详情</h3>");
                sb.append("<table>");
                sb.append("<tr><th>事件</th><th>值</th></tr>");
                sb.append("<tr><td>名称</td><td><strong>@").append(event.getName()).append("</strong></td></tr>");
                sb.append("<tr><td>描述</td><td>").append(event.getDescription() != null ? event.getDescription() : "").append("</td></tr>");
                sb.append("<tr><td>参数</td><td>").append(event.getParameters() != null ? event.getParameters() : "").append("</td></tr>");
                sb.append("</table>");
                sb.append("</div>");
                break;
            }
        }
    }

    /**
     * 提取组件名称
     */
    private String extractComponentName(PsiElement element) {
        if (element instanceof HtmlTag) {
            String tagName = ((HtmlTag) element).getName();
            LOG.info("extractComponentName: HtmlTag detected, tagName = " + tagName);
            if (tagName != null && tagName.startsWith("el-")) {
                LOG.info("extractComponentName: returning HtmlTag component name: " + tagName);
                return tagName;
            }
        } else if (element instanceof XmlTag) {
            String tagName = ((XmlTag) element).getName();
            LOG.info("extractComponentName: XmlTag detected, tagName = " + tagName);
            if (tagName != null && tagName.startsWith("el-")) {
                LOG.info("extractComponentName: returning XmlTag component name: " + tagName);
            return tagName;
        }
        } else if (element instanceof XmlAttribute && element.getParent() instanceof HtmlTag) {
            String tagName = ((HtmlTag) element.getParent()).getName();
            LOG.info("extractComponentName: HtmlTag attribute detected, parent tagName = " + tagName);
            if (tagName != null && tagName.startsWith("el-")) {
                LOG.info("extractComponentName: returning HtmlTag attribute component name: " + tagName);
                return tagName;
            }
        } else if (element instanceof XmlAttribute && element.getParent() instanceof XmlTag) {
            String tagName = ((XmlTag) element.getParent()).getName();
            LOG.info("extractComponentName: XmlTag attribute detected, parent tagName = " + tagName);
            if (tagName != null && tagName.startsWith("el-")) {
                LOG.info("extractComponentName: returning XmlTag attribute component name: " + tagName);
                return tagName;
            }
        }
        LOG.info("extractComponentName: no component name found for element type: " + element.getClass().getSimpleName());
        return null;
    }

    /**
     * 检查项目是否启用组件补全
     */
    private boolean isComponentCompletionEnabled(Project project) {
        try {
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
            return projectSettings.isEnableComponentCompletion();
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "检查项目设置失败，默认启用组件补全", e);
            return true; // 默认启用
        }
    }

    /**
     * 清理文档缓存
     */
    public static void clearDocumentCache() {
        documentCache.clear();
        documentCacheTimestamps.clear();
        VueKitLogger.debug(LOG, "清理了文档缓存");
    }

    /**
     * 清理特定组件的文档缓存
     */
    public static void clearComponentDocumentCache(String componentName) {
        if (componentName != null) {
            // 清理组件相关的所有缓存
            documentCache.entrySet().removeIf(entry -> entry.getKey().startsWith("component:" + componentName));
            documentCache.entrySet().removeIf(entry -> entry.getKey().startsWith("attribute:" + componentName));
            documentCacheTimestamps.entrySet().removeIf(entry -> entry.getKey().startsWith("component:" + componentName));
            documentCacheTimestamps.entrySet().removeIf(entry -> entry.getKey().startsWith("attribute:" + componentName));
            VueKitLogger.debug(LOG, "清理了组件 " + componentName + " 的文档缓存");
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static java.util.Map<String, Object> getCacheStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("cachedDocuments", documentCache.size());
        stats.put("cacheSize", documentCache.values().stream()
                .mapToInt(String::length)
                .sum());
        return stats;
    }
} 