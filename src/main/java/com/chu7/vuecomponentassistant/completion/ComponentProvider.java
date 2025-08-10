package com.chu7.vuecomponentassistant.completion;

import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用组件数据提供者
 *
 * 功能说明：
 * - 从 JSON 文件中加载组件数据
 * - 支持 Element UI、Element Plus、Ant Design Vue
 * - 提供组件查询功能
 * - 支持组件属性、事件、插槽等信息
 *
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentProvider {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentProvider.class);

    private final Map<String, ComponentInfo> componentsMap;
    private final List<ComponentInfo> componentsList;
    private final ComponentLibraryDetector.LibraryType libraryType;
    private final String dataPath;

    public ComponentProvider(Project project) {
        VueKitLogger.debug(LOG, "=== ComponentProvider 初始化开始 ===");
        this.componentsMap = new HashMap<>();
        this.componentsList = new ArrayList<>();

        // 检测项目使用的组件库
        this.libraryType = ComponentLibraryDetector.detectComponentLibrary(project);
        VueKitLogger.debug(LOG, "检测到的组件库类型: " + this.libraryType.getDisplayName());
        this.dataPath = ComponentLibraryDetector.getComponentDataPath(libraryType);
        VueKitLogger.debug(LOG, "数据文件路径: " + this.dataPath);

        // 打印检测信息
        ComponentLibraryDetector.printDetectionInfo(project);

        loadComponents();
        VueKitLogger.debug(LOG, "=== ComponentProvider 初始化完成 ===");
    }

    /**
     * 加载组件数据
     */
    private void loadComponents() {
        long startTime = System.currentTimeMillis();

        try {
            InputStream inputStream = getClass().getResourceAsStream(dataPath);
            if (inputStream == null) {
                VueKitLogger.error(LOG, "Cannot find components data file: " + dataPath);
                return;
            }

            // 使用标准 Java 方法读取文件，确保 UTF-8 编码
            byte[] bytes = inputStream.readAllBytes();
            String jsonContent = new String(bytes, StandardCharsets.UTF_8);
            inputStream.close();

            // 添加编码调试信息
            VueKitLogger.debug(LOG, "=== 组件数据加载信息 ===");
            VueKitLogger.debug(LOG, "组件库类型: " + libraryType.getDisplayName());
            VueKitLogger.debug(LOG, "数据文件路径: " + dataPath);
            VueKitLogger.debug(LOG, "文件大小: " + bytes.length + " 字节");
            VueKitLogger.debug(LOG, "内容长度: " + jsonContent.length() + " 字符");
            VueKitLogger.debug(LOG, "内容前200字符: " + jsonContent.substring(0, Math.min(200, jsonContent.length())));
            VueKitLogger.debug(LOG, "是否包含中文字符: " + jsonContent.contains("按钮"));
            VueKitLogger.debug(LOG, "是否包含emoji: " + jsonContent.contains("📦"));

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ComponentInfo>>() {
            }.getType();
            List<ComponentInfo> components = gson.fromJson(jsonContent, listType);

            for (ComponentInfo component : components) {
                componentsMap.put(component.getName(), component);
                componentsList.add(component);
            }

            VueKitLogger.logLibraryDetection(LOG, libraryType.getDisplayName(), components.size());

            // 记录性能日志
            long duration = System.currentTimeMillis() - startTime;
            VueKitLogger.performance(LOG, "组件数据加载", duration);

        } catch (IOException e) {
            VueKitLogger.error(LOG, "Failed to load " + libraryType.getDisplayName() + " components data", e);
        }
    }

    /**
     * 获取所有组件列表
     */
    public List<ComponentInfo> getAllComponents() {
        List<ComponentInfo> allComponents = new ArrayList<>();

        // 添加内置组件库的组件
        allComponents.addAll(componentsList);

        // 添加自定义组件库的组件
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries =
                CustomComponentLibraryManager.getAllCustomLibraries();
        for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
            allComponents.addAll(config.getComponents());
        }

        return allComponents;
    }

    /**
     * 根据前缀获取组件列表
     */
    public List<ComponentInfo> getComponentsByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAllComponents();
        }

        List<ComponentInfo> matchingComponents = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();

        // 从内置组件库查找
        for (ComponentInfo component : componentsList) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
            }
        }

        // 从自定义组件库查找
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries =
                CustomComponentLibraryManager.getAllCustomLibraries();
        for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
            for (ComponentInfo component : config.getComponents()) {
                if (component.getName() != null &&
                        component.getName().toLowerCase().startsWith(lowerPrefix)) {
                    matchingComponents.add(component);
                }
            }
        }

        return matchingComponents;
    }

    /**
     * 根据组件名获取组件
     */
    public ComponentInfo getComponent(String componentName) {
        // 先从内置组件库查找
        ComponentInfo component = componentsMap.get(componentName);
        if (component != null) {
            return component;
        }

        // 从自定义组件库查找
        return CustomComponentLibraryManager.getCustomComponent(componentName);
    }

    /**
     * 根据前缀搜索组件
     */
    public List<ComponentInfo> searchComponents(String prefix) {
        List<ComponentInfo> allComponents = new ArrayList<>();

        // 添加内置组件库的组件
        allComponents.addAll(componentsList);

        // 添加自定义组件库的组件
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries =
                CustomComponentLibraryManager.getAllCustomLibraries();
        for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
            allComponents.addAll(config.getComponents());
        }

        if (prefix == null || prefix.isEmpty()) {
            return allComponents;
        }

        return allComponents.stream()
                .filter(component -> component.getName().toLowerCase().contains(prefix.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 获取组件的所有属性
     */
    public List<ComponentInfo.ComponentProp> getComponentProps(String componentName) {
        ComponentInfo component = getComponent(componentName);
        return component != null ? component.getProps() : new ArrayList<>();
    }

    /**
     * 获取组件的所有事件
     */
    public List<ComponentInfo.ComponentEvent> getComponentEvents(String componentName) {
        ComponentInfo component = getComponent(componentName);
        return component != null ? component.getEvents() : new ArrayList<>();
    }

    /**
     * 获取组件的所有插槽
     */
    public List<ComponentInfo.ComponentSlot> getComponentSlots(String componentName) {
        ComponentInfo component = getComponent(componentName);
        return component != null ? component.getSlots() : new ArrayList<>();
    }

    /**
     * 检查组件是否存在
     */
    public boolean hasComponent(String componentName) {
        return componentsMap.containsKey(componentName);
    }

    /**
     * 获取组件总数
     */
    public int getComponentCount() {
        return componentsList.size();
    }

    /**
     * 获取当前组件库类型
     */
    public ComponentLibraryDetector.LibraryType getLibraryType() {
        return libraryType;
    }

    /**
     * 获取组件库显示名称
     */
    public String getLibraryDisplayName() {
        return libraryType.getDisplayName();
    }

    /**
     * 获取组件所属的组件库显示名称
     */
    public String getComponentLibraryDisplayName(String componentName) {
        // 检查是否是自定义组件库的组件
        if (CustomComponentLibraryManager.isCustomComponent(componentName)) {
            return CustomComponentLibraryManager.getCustomLibraryDisplayName(componentName);
        }

        // 返回内置组件库的显示名称
        return libraryType.getDisplayName();
    }

    /**
     * 获取组件前缀
     */
    public String getComponentPrefix() {
        return ComponentLibraryDetector.getComponentPrefix(libraryType);
    }

    /**
     * 获取文档 URL 模板
     */
    public String getDocumentationUrlTemplate() {
        return ComponentLibraryDetector.getDocumentationUrlTemplate(libraryType);
    }

    /**
     * 检查组件是否属于当前组件库
     */
    public boolean isComponentFromCurrentLibrary(String componentName) {
        // 检查是否是内置组件库的组件
        if (ComponentLibraryDetector.isComponentFromLibrary(componentName, libraryType)) {
            return true;
        }

        // 检查是否是自定义组件库的组件
        return CustomComponentLibraryManager.isCustomComponent(componentName);
    }

    /**
     * 生成组件的文档 URL
     */
    public String generateDocumentationUrl(String componentName) {
        // 检查是否是自定义组件库的组件
        if (CustomComponentLibraryManager.isCustomComponent(componentName)) {
            return CustomComponentLibraryManager.generateCustomDocumentationUrl(componentName);
        }

        // 生成内置组件库的文档URL
        String template = getDocumentationUrlTemplate();
        if (template.isEmpty()) {
            return "";
        }

        // 移除组件前缀
        String componentKey = componentName;
        String prefix = getComponentPrefix();
        if (componentName.startsWith(prefix)) {
            componentKey = componentName.substring(prefix.length());
        }

        return String.format(template, componentKey);
    }
}
