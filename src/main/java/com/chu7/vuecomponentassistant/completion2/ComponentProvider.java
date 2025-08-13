package com.chu7.vuecomponentassistant.completion2;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

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

    private final Map<String, ElementPlusComponent> componentsMap;
    private final List<ElementPlusComponent> componentsList;
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
        
        // 注册到 ComponentProviderManager
        ComponentProviderManager.registerProvider(project, this);
    }

    /**
     * 加载组件数据
     */
    private void loadComponents() {
        // 首先尝试从本地缓存的组件库加载
        if (loadFromLocalCache()) {
            VueKitLogger.debug(LOG, "从本地缓存加载组件库成功");
            return;
        }

        // 如果本地缓存没有，则从内置资源文件加载
        VueKitLogger.debug(LOG, "本地缓存未找到，从内置资源文件加载");
        loadFromBuiltinResources();
    }

    /**
     * 根据组件库类型获取对应的组件库ID
     */
    private String getLibraryIdByType(ComponentLibraryDetector.LibraryType libraryType) {
        switch (libraryType) {
            case ELEMENT_UI:
                return "element-ui";
            case ELEMENT_PLUS:
                return "element-plus";
            case ANT_DESIGN_VUE:
                return "ant-design-vue";
            default:
                return null;
        }
    }

    /**
     * 从内置资源文件加载组件数据
     */
    private void loadFromBuiltinResources() {
        try {
            VueKitLogger.debug(LOG, "从内置资源文件加载组件数据: " + dataPath);

            InputStream inputStream = getClass().getResourceAsStream(dataPath);
            if (inputStream == null) {
                VueKitLogger.error(LOG, "Cannot find components data file: " + dataPath);
                return;
            }

            // 使用标准 Java 方法读取文件，确保 UTF-8 编码
            byte[] bytes = inputStream.readAllBytes();
            String jsonContent = new String(bytes, StandardCharsets.UTF_8);
            inputStream.close();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ElementPlusComponent>>() {
            }.getType();
            List<ElementPlusComponent> components = gson.fromJson(jsonContent, listType);

            for (ElementPlusComponent component : components) {
                componentsMap.put(component.getName(), component);
                componentsList.add(component);
            }

            VueKitLogger.logLibraryDetection(LOG, libraryType.getDisplayName(), components.size());
            VueKitLogger.debug(LOG, "从内置资源文件成功加载 " + components.size() + " 个组件");

        } catch (IOException e) {
            VueKitLogger.error(LOG, "Failed to load " + libraryType.getDisplayName() + " components data", e);
        }
    }

    /**
     * 从本地缓存的组件库加载组件数据
     */
    private boolean loadFromLocalCache() {
        try {
            VueKitLogger.debug(LOG, "尝试从本地缓存加载组件库: " + libraryType.getDisplayName());

            ComponentLibraryManager libraryManager = new ComponentLibraryManager();

            // 根据检测到的组件库类型查找对应的本地组件库
            String libraryId = getLibraryIdByType(libraryType);
            if (libraryId == null) {
                VueKitLogger.debug(LOG, "无法确定组件库ID: " + libraryType.getDisplayName());
                return false;
            }

            // 使用正确的方法名：getLibraryById
            ComponentLibrary library = libraryManager.getLibraryById(libraryId);
            if (library == null) {
                VueKitLogger.debug(LOG, "本地缓存中未找到组件库: " + libraryId);
                return false;
            }

            VueKitLogger.debug(LOG, "找到本地组件库: " + library.getName() + ", 组件数量: " + library.getComponents().size());

            // 加载组件数据
            for (ComponentInfo component : library.getComponents()) {
                // 将 ComponentInfo 转换为 ElementPlusComponent
                ElementPlusComponent elementPlusComponent = convertToElementPlusComponent(component);
                componentsMap.put(elementPlusComponent.getName(), elementPlusComponent);
                componentsList.add(elementPlusComponent);
            }

            VueKitLogger.logLibraryDetection(LOG, libraryType.getDisplayName(), library.getComponents().size());
            return true;

        } catch (Exception e) {
            VueKitLogger.error(LOG, "从本地缓存加载组件库失败: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将 ComponentInfo 转换为 ElementPlusComponent
     */
    private ElementPlusComponent convertToElementPlusComponent(ComponentInfo componentInfo) {
        ElementPlusComponent component = new ElementPlusComponent();
        component.setName(componentInfo.getName());
        component.setDescription(componentInfo.getDescription());
        
        // 转换属性
        List<ElementPlusProp> props = new ArrayList<>();
        for (ComponentInfo.ComponentProp prop : componentInfo.getProps()) {
            ElementPlusProp elementPlusProp = new ElementPlusProp();
            elementPlusProp.setName(prop.getName());
            elementPlusProp.setType(prop.getType());
            elementPlusProp.setDescription(prop.getDescription());
            elementPlusProp.setDefaultValue(prop.getDefaultValue());
            props.add(elementPlusProp);
        }
        component.setProps(props);
        
        // 转换事件
        List<ElementPlusEvent> events = new ArrayList<>();
        for (ComponentInfo.ComponentEvent event : componentInfo.getEvents()) {
            ElementPlusEvent elementPlusEvent = new ElementPlusEvent();
            elementPlusEvent.setName(event.getName());
            elementPlusEvent.setDescription(event.getDescription());
            events.add(elementPlusEvent);
        }
        component.setEvents(events);
        
        // 转换插槽
        List<ElementPlusSlot> slots = new ArrayList<>();
        for (ComponentInfo.ComponentSlot slot : componentInfo.getSlots()) {
            ElementPlusSlot elementPlusSlot = new ElementPlusSlot();
            elementPlusSlot.setName(slot.getName());
            elementPlusSlot.setDescription(slot.getDescription());
            slots.add(elementPlusSlot);
        }
        component.setSlots(slots);
        
        return component;
    }

    /**
     * 获取所有组件列表
     */
    public List<ElementPlusComponent> getAllComponents() {
        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 添加内置组件库的组件
        allComponents.addAll(componentsList);

        // 添加自定义组件库的组件
        addCustomComponents(allComponents);

        // 添加从官方组件库市场下载的组件库
        addDownloadedOfficialComponents(allComponents);

        return allComponents;
    }

    /**
     * 根据前缀获取组件列表
     */
    public List<ElementPlusComponent> getComponentsByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAllComponents();
        }

        List<ElementPlusComponent> matchingComponents = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();

        // 从内置组件库查找
        for (ElementPlusComponent component : componentsList) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
            }
        }

        // 从自定义组件库查找
        List<ElementPlusComponent> customComponents = new ArrayList<>();
        addCustomComponents(customComponents);
        for (ElementPlusComponent component : customComponents) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
            }
        }

        // 从下载的官方组件库查找
        List<ElementPlusComponent> downloadedComponents = new ArrayList<>();
        addDownloadedOfficialComponents(downloadedComponents);
        for (ElementPlusComponent component : downloadedComponents) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
            }
        }

        return matchingComponents;
    }

    /**
     * 根据组件名获取组件
     */
    public ElementPlusComponent getComponent(String componentName) {
        // 先从内置组件库查找
        ElementPlusComponent component = componentsMap.get(componentName);
        if (component != null) {
            return component;
        }

        // 从自定义组件库查找
        ComponentInfo componentInfo = CustomComponentLibraryManager.getCustomComponent(componentName);
        if (componentInfo != null) {
            return convertToElementPlusComponent(componentInfo);
        }

        // 从下载的官方组件库查找
        try {
            // 每次都重新创建 ComponentLibraryManager 实例，确保获取最新数据
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : allLibraries) {
                // 只处理从官方组件库市场下载的组件库
                if ("OFFICIAL".equals(library.getSource()) && library.getComponents() != null) {
                    for (ComponentInfo info : library.getComponents()) {
                        if (componentName.equals(info.getName())) {
                            return convertToElementPlusComponent(info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "从下载的官方组件库查找组件失败: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * 根据前缀搜索组件
     */
    public List<ElementPlusComponent> searchComponents(String prefix) {
        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 添加内置组件库的组件
        allComponents.addAll(componentsList);

        // 添加自定义组件库的组件
        addCustomComponents(allComponents);

        // 添加从官方组件库市场下载的组件库
        addDownloadedOfficialComponents(allComponents);

        if (prefix == null || prefix.isEmpty()) {
            return allComponents;
        }

        return allComponents.stream()
                .filter(component -> component.getName().toLowerCase().contains(prefix.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 添加自定义组件到列表中
     */
    private void addCustomComponents(List<ElementPlusComponent> allComponents) {
        List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries =
                CustomComponentLibraryManager.getAllCustomLibraries();
        for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
            for (ComponentInfo componentInfo : config.getComponents()) {
                allComponents.add(convertToElementPlusComponent(componentInfo));
            }
        }
    }

    /**
     * 添加从官方组件库市场下载的组件库
     */
    private void addDownloadedOfficialComponents(List<ElementPlusComponent> allComponents) {
        try {
            // 每次都重新创建 ComponentLibraryManager 实例，确保获取最新数据
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : allLibraries) {
                // 只处理从官方组件库市场下载的组件库
                if ("OFFICIAL".equals(library.getSource()) && library.getComponents() != null) {
                    for (ComponentInfo componentInfo : library.getComponents()) {
                        allComponents.add(convertToElementPlusComponent(componentInfo));
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载下载的官方组件库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重新加载组件数据
     * 当组件库发生变化时（添加、删除、更新）调用此方法
     */
    public void reloadComponents() {
        VueKitLogger.debug(LOG, "=== 重新加载组件数据 ===");
        
        // 清空现有数据
        componentsMap.clear();
        componentsList.clear();
        
        // 重新加载组件数据
        loadComponents();
        
        VueKitLogger.debug(LOG, "=== 组件数据重新加载完成 ===");
    }

    /**
     * 获取组件的所有属性
     */
    public List<ElementPlusProp> getComponentProps(String componentName) {
        ElementPlusComponent component = getComponent(componentName);
        return component != null ? component.getProps() : new ArrayList<>();
    }

    /**
     * 获取组件的所有事件
     */
    public List<ElementPlusEvent> getComponentEvents(String componentName) {
        ElementPlusComponent component = getComponent(componentName);
        return component != null ? component.getEvents() : new ArrayList<>();
    }

    /**
     * 获取组件的所有插槽
     */
    public List<ElementPlusSlot> getComponentSlots(String componentName) {
        ElementPlusComponent component = getComponent(componentName);
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
