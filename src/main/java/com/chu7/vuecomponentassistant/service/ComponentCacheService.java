package com.chu7.vuecomponentassistant.service;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusEvent;
import com.chu7.vuecomponentassistant.completion2.ElementPlusProp;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.BasicXmlAttributeDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VueKit 高性能组件缓存服务
 */
@Service(Service.Level.PROJECT)
public final class ComponentCacheService {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentCacheService.class);

    private final Project project;
    
    // 三级缓存
    private final Map<String, ElementPlusComponent> componentCache = new ConcurrentHashMap<>();
    private final Map<String, String> htmlDocumentCache = new ConcurrentHashMap<>();
    private final Map<String, XmlAttributeDescriptor[]> attributeDescriptorCache = new ConcurrentHashMap<>();
    
    // 缓存统计
    private final Map<String, Integer> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, Integer> cacheMisses = new ConcurrentHashMap<>();
    
    // 缓存配置
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long CACHE_TTL = 30 * 60 * 1000; // 30分钟
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    public ComponentCacheService(@NotNull Project project) {
        this.project = project;
    }

    /**
     * 获取服务实例
     */
    public static ComponentCacheService getInstance(@NotNull Project project) {
        return project.getService(ComponentCacheService.class);
    }

    /**
     * 获取组件对象（带缓存）
     */
    @Nullable
    public ElementPlusComponent getComponent(String componentName) {
        if (componentName == null || componentName.isEmpty()) {
            return null;
        }

        // 检查缓存
        ElementPlusComponent cached = componentCache.get(componentName);
        if (cached != null && !isCacheExpired(componentName)) {
            incrementCacheHits("component");
            return cached;
        }

        incrementCacheMisses("component");
        
        // 从组件提供者获取
        try {
            ComponentProvider componentProvider = ComponentProviderManager.getProvider(project);
            if (componentProvider == null) {
                return null;
            }

            ElementPlusComponent component = componentProvider.getComponent(componentName);
            if (component != null) {
                // 缓存组件
                cacheComponent(componentName, component);
                return component;
            }
        } catch (Exception e) {
            LOG.error("Error getting component: " + componentName, e);
        }

        return null;
    }

    /**
     * 获取组件的HTML文档（带缓存）
     */
    @Nullable
    public String getComponentHtml(String componentName) {
        if (componentName == null || componentName.isEmpty()) {
            return null;
        }

        // 检查缓存
        String cached = htmlDocumentCache.get(componentName);
        if (cached != null && !isCacheExpired(componentName)) {
            incrementCacheHits("html");
            return cached;
        }

        incrementCacheMisses("html");
        
        // 生成HTML文档
        try {
            ElementPlusComponent component = getComponent(componentName);
            if (component != null) {
                String html = generateComponentHtml(component);
                if (html != null) {
                    // 缓存HTML文档
                    cacheHtmlDocument(componentName, html);
                    return html;
                }
            }
        } catch (Exception e) {
            LOG.error("Error generating HTML for component: " + componentName, e);
        }

        return null;
    }

    /**
     * 获取组件的属性描述符（带缓存）
     */
    public XmlAttributeDescriptor[] getComponentAttributes(String componentName) {
        if (componentName == null || componentName.isEmpty()) {
            return XmlAttributeDescriptor.EMPTY;
        }

        // 检查缓存
        XmlAttributeDescriptor[] cached = attributeDescriptorCache.get(componentName);
        if (cached != null && !isCacheExpired(componentName)) {
            incrementCacheHits("attribute");
            return cached;
        }

        incrementCacheMisses("attribute");
        
        // 生成属性描述符
        try {
            ElementPlusComponent component = getComponent(componentName);
            if (component != null) {
                XmlAttributeDescriptor[] descriptors = generateAttributeDescriptors(component);
                if (descriptors != null) {
                    // 缓存属性描述符
                    cacheAttributeDescriptors(componentName, descriptors);
                    return descriptors;
                }
            }
        } catch (Exception e) {
            LOG.error("Error generating attributes for component: " + componentName, e);
        }

        return XmlAttributeDescriptor.EMPTY;
    }

    /**
     * 获取特定属性的描述符
     */
    @Nullable
    public XmlAttributeDescriptor getAttributeDescriptor(String componentName, String attributeName) {
        if (componentName == null || attributeName == null) {
            return null;
        }

        XmlAttributeDescriptor[] descriptors = getComponentAttributes(componentName);
        for (XmlAttributeDescriptor descriptor : descriptors) {
            if (attributeName.equals(descriptor.getName())) {
                return descriptor;
            }
        }

        return null;
    }

    /**
     * 缓存组件对象
     */
    private void cacheComponent(String componentName, ElementPlusComponent component) {
        if (componentCache.size() >= MAX_CACHE_SIZE) {
            evictOldestCache();
        }
        componentCache.put(componentName, component);
        cacheTimestamps.put(componentName, System.currentTimeMillis());
        LOG.debug("Cached component: " + componentName);
    }

    /**
     * 缓存HTML文档
     */
    private void cacheHtmlDocument(String componentName, String html) {
        if (htmlDocumentCache.size() >= MAX_CACHE_SIZE) {
            evictOldestCache();
        }
        htmlDocumentCache.put(componentName, html);
        cacheTimestamps.put(componentName, System.currentTimeMillis());
        LOG.debug("Cached HTML document for: " + componentName);
    }

    /**
     * 缓存属性描述符
     */
    private void cacheAttributeDescriptors(String componentName, XmlAttributeDescriptor[] descriptors) {
        if (attributeDescriptorCache.size() >= MAX_CACHE_SIZE) {
            evictOldestCache();
        }
        attributeDescriptorCache.put(componentName, descriptors);
        cacheTimestamps.put(componentName, System.currentTimeMillis());
        LOG.debug("Cached attribute descriptors for: " + componentName);
    }

    /**
     * 检查缓存是否过期
     */
    private boolean isCacheExpired(String key) {
        Long timestamp = cacheTimestamps.get(key);
        if (timestamp == null) {
            return true;
        }
        return System.currentTimeMillis() - timestamp > CACHE_TTL;
    }

    /**
     * 淘汰最旧的缓存
     */
    private void evictOldestCache() {
        if (cacheTimestamps.isEmpty()) {
            return;
        }

        String oldestKey = cacheTimestamps.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (oldestKey != null) {
            componentCache.remove(oldestKey);
            htmlDocumentCache.remove(oldestKey);
            attributeDescriptorCache.remove(oldestKey);
            cacheTimestamps.remove(oldestKey);
            LOG.debug("Evicted cache for: " + oldestKey);
        }
    }

    /**
     * 生成组件的HTML文档
     */
    private String generateComponentHtml(ElementPlusComponent component) {
        if (component == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<style>");
        sb.append("table { width: 600px; border-collapse: collapse; margin: 10px 0; }");
        sb.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        sb.append("th { background-color: #f2f2f2; }");
        sb.append("h3 { color: #333; margin: 20px 0 10px 0; }");
        sb.append("</style>");
        sb.append("<body>");

        // 组件名称
        sb.append("<h2>").append(component.getName()).append("</h2>");

        // 属性表格
        if (component.getProps() != null && !component.getProps().isEmpty()) {
            sb.append("<h3>Properties</h3>");
            sb.append("<table>");
            sb.append("<thead><tr><th>Name</th><th>Type</th><th>Description</th><th>Default</th></tr></thead>");
            sb.append("<tbody>");
            for (ElementPlusProp prop : component.getProps()) {
                sb.append("<tr>");
                sb.append("<td>").append(prop.getName()).append("</td>");
                sb.append("<td>").append(prop.getType()).append("</td>");
                sb.append("<td>").append(prop.getDescription()).append("</td>");
                sb.append("<td>").append(prop.getDefaultValue()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        // 事件表格
        if (component.getEvents() != null && !component.getEvents().isEmpty()) {
            sb.append("<h3>Events</h3>");
            sb.append("<table>");
            sb.append("<thead><tr><th>Name</th><th>Description</th><th>Parameters</th></tr></thead>");
            sb.append("<tbody>");
            for (ElementPlusEvent event : component.getEvents()) {
                sb.append("<tr>");
                sb.append("<td>@").append(event.getName()).append("</td>");
                sb.append("<td>").append(event.getDescription()).append("</td>");
                sb.append("<td>").append(event.getParameters()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        // 插槽表格
        if (component.getSlots() != null && !component.getSlots().isEmpty()) {
            sb.append("<h3>Slots</h3>");
            sb.append("<table>");
            sb.append("<thead><tr><th>Name</th><th>Description</th></tr></thead>");
            sb.append("<tbody>");
            for (var slot : component.getSlots()) {
                sb.append("<tr>");
                sb.append("<td>").append(slot.getName()).append("</td>");
                sb.append("<td>").append(slot.getDescription()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * 生成属性描述符数组
     */
    private XmlAttributeDescriptor[] generateAttributeDescriptors(ElementPlusComponent component) {
        if (component == null) {
            return XmlAttributeDescriptor.EMPTY;
        }

        List<XmlAttributeDescriptor> descriptors = new ArrayList<>();

        // 添加属性描述符
        if (component.getProps() != null) {
            for (ElementPlusProp prop : component.getProps()) {
                descriptors.add(new VueKitAttributeDescriptor(
                    prop.getName(),
                    String.valueOf(prop.getType()),
                    String.valueOf(prop.getDescription()),
                    String.valueOf(prop.getDefaultValue()),
                    AttributeType.PROPERTY
                ));
            }
        }

        // 添加事件描述符
        if (component.getEvents() != null) {
            for (ElementPlusEvent event : component.getEvents()) {
                descriptors.add(new VueKitAttributeDescriptor(
                    "@" + event.getName(),
                    "Event",
                    event.getDescription(),
                    event.getParameters(),
                    AttributeType.EVENT
                ));
            }
        }

        return descriptors.toArray(new XmlAttributeDescriptor[0]);
    }

    /**
     * 增加缓存命中计数
     */
    private void incrementCacheHits(String cacheType) {
        cacheHits.merge(cacheType, 1, Integer::sum);
    }

    /**
     * 增加缓存未命中计数
     */
    private void incrementCacheMisses(String cacheType) {
        cacheMisses.merge(cacheType, 1, Integer::sum);
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("componentCacheSize", componentCache.size());
        stats.put("htmlCacheSize", htmlDocumentCache.size());
        stats.put("attributeCacheSize", attributeDescriptorCache.size());
        stats.put("cacheHits", new HashMap<>(cacheHits));
        stats.put("cacheMisses", new HashMap<>(cacheMisses));
        return stats;
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        componentCache.clear();
        htmlDocumentCache.clear();
        attributeDescriptorCache.clear();
        cacheTimestamps.clear();
        cacheHits.clear();
        cacheMisses.clear();
        LOG.info("All caches cleared");
    }

    /**
     * 属性类型枚举
     */
    public enum AttributeType {
        PROPERTY("Property"),
        EVENT("Event");

        private final String displayName;

        AttributeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * VueKit 属性描述符实现
     */
    private static class VueKitAttributeDescriptor extends BasicXmlAttributeDescriptor {
        private final String name;
        private final String type;
        private final String description;
        private final String defaultValue;
        private final AttributeType attributeType;

        public VueKitAttributeDescriptor(String name, String type, String description, 
                                      String defaultValue, AttributeType attributeType) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
            this.attributeType = attributeType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean hasIdType() {
            return false;
        }

        @Override
        public boolean hasIdRefType() {
            return false;
        }

        @Override
        public boolean isEnumerated() {
            return false;
        }

        @Override
        public boolean isFixed() {
            return false;
        }

        @Override
        public String[] getEnumeratedValues() {
            return new String[0];
        }

        @Override
        public com.intellij.psi.PsiElement getDeclaration() {
            return null;
        }

        @Override
        public void init(@NotNull com.intellij.psi.PsiElement element) {
            // 不需要特殊初始化
        }
    }
}
