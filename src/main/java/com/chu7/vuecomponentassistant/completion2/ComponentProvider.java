package com.chu7.vuecomponentassistant.completion2;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.ErrorHandler;
import com.chu7.vuecomponentassistant.utils.SmartComponentFilter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;

/**
 * 通用组件数据提供者 - 简化版
 * 
 * 功能说明：
 * - 从远程组件库管理器加载组件数据
 * - 支持 Element UI、Element Plus、Ant Design Vue
 * - 提供组件查询功能
 * - 支持组件属性、事件、插槽等信息
 *
 * @author VueKit Team
 * @version 3.0.0
 */
public class ComponentProvider {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentProvider.class);

    private final Map<String, ElementPlusComponent> componentsMap;
    private final List<ElementPlusComponent> componentsList;
    private final ComponentLibraryDetector.LibraryType libraryType;
    private final Project project;

    /**
     * 构造函数
     */
    public ComponentProvider(Project project) {
        Objects.requireNonNull(project, "Project对象不能为null");

        VueKitLogger.debug(LOG, "=== ComponentProvider 初始化开始 ===");
        this.componentsMap = new HashMap<>();
        this.componentsList = new ArrayList<>();
        this.project = project;

        try {
            // 检测项目使用的组件库
            this.libraryType = ComponentLibraryDetector.detectComponentLibrary(project);
            VueKitLogger.debug(LOG, "检测到的组件库类型: " + this.libraryType.getDisplayName());

            loadComponents();
            VueKitLogger.debug(LOG, "=== ComponentProvider 初始化完成 ===");

            // 注册到 ComponentProviderManager
            if (!ComponentProviderManager.isProviderRegistered(project)) {
                ComponentProviderManager.registerProvider(project, this);
            }
        } catch (Exception e) {
            String errorMsg = "ComponentProvider初始化失败";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * 加载组件数据
     */
    private void loadComponents() {
        try {
            VueKitLogger.info(LOG, "=== 开始加载组件数据 ===");
            VueKitLogger.info(LOG, "检测到的组件库类型: " + libraryType.getDisplayName());

            // 尝试从本地缓存加载
            if (loadFromLocalCache()) {
                VueKitLogger.info(LOG, "✅ 从本地缓存加载成功，组件数量: " + componentsList.size());
                applySmartFiltering();
                return;
            }

            // 尝试从远程组件库管理器加载
            if (loadFromRemoteManager()) {
                VueKitLogger.info(LOG, "✅ 从远程组件库管理器加载成功，组件数量: " + componentsList.size());
                applySmartFiltering();
                return;
            }

            VueKitLogger.warn(LOG, "⚠️ 无法从任何数据源加载组件库数据");

        } catch (Exception e) {
            String errorMsg = "加载组件数据失败";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
    }

    /**
     * 应用智能过滤
     */
    private void applySmartFiltering() {
        try {
            VueKitLogger.info(LOG, "=== 开始应用智能过滤 ===");

            int beforeFilterCount = componentsList.size();

            // 创建智能过滤器
            SmartComponentFilter filter = new SmartComponentFilter();

            // 应用智能过滤
            List<ElementPlusComponent> filteredComponents = filter.filterComponentsByProject(componentsList, project);

            // 更新组件列表
            componentsList.clear();
            componentsList.addAll(filteredComponents);

            // 更新组件映射
            componentsMap.clear();
            for (ElementPlusComponent component : filteredComponents) {
                componentsMap.put(component.getName(), component);
            }

            int afterFilterCount = componentsList.size();
            int filteredOutCount = beforeFilterCount - afterFilterCount;

            VueKitLogger.info(LOG, "智能过滤完成:");
            VueKitLogger.info(LOG, "  过滤前组件数量: " + beforeFilterCount);
            VueKitLogger.info(LOG, "  过滤后组件数量: " + afterFilterCount);
            VueKitLogger.info(LOG, "  过滤掉组件数量: " + filteredOutCount);

        } catch (Exception e) {
            VueKitLogger.error(LOG, "应用智能过滤失败", e);
        }
    }

    /**
     * 根据组件库类型获取对应的组件库ID
     */
    private String getLibraryIdByType(ComponentLibraryDetector.LibraryType libraryType) {
        Objects.requireNonNull(libraryType, "组件库类型不能为null");

        switch (libraryType) {
            case ELEMENT_UI:
                return "element-ui";
            case ELEMENT_PLUS:
                return "element-plus";
            case ANT_DESIGN_VUE:
                return "ant-design-vue";
            default:
                VueKitLogger.warn(LOG, "未知的组件库类型: " + libraryType);
                return null;
        }
    }

    /**
     * 从本地缓存加载组件库
     */
    private boolean loadFromLocalCache() {
        try {
            VueKitLogger.debug(LOG, "尝试从本地缓存加载组件库");

            String libraryId = getLibraryIdByType(libraryType);
            if (libraryId == null) {
                return false;
            }

            ComponentLibraryManager manager = new ComponentLibraryManager();
            List<ComponentLibrary> libraries = manager.getAllLibraries();

            for (ComponentLibrary library : libraries) {
                if (libraryId.equals(library.getId())) {
                    VueKitLogger.info(LOG, "✅ 找到本地缓存的组件库: " + library.getName());

                    if (library.getComponents() != null) {
                        int beforeCount = componentsList.size();

                        for (ComponentInfo componentInfo : library.getComponents()) {
                            try {
                                if (componentInfo != null) {
                                    ElementPlusComponent component = convertToElementPlusComponent(componentInfo);
                                    if (component != null) {
                                        componentsMap.put(component.getName(), component);
                                        componentsList.add(component);
                                    }
                                }
                            } catch (Exception e) {
                                VueKitLogger.warn(LOG, "转换缓存组件失败: " + componentInfo.getName(), e);
                            }
                        }

                        int addedCount = componentsList.size() - beforeCount;
                        VueKitLogger.info(LOG, "从本地缓存成功加载 " + addedCount + " 个组件");
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            VueKitLogger.error(LOG, "从本地缓存加载组件库失败", e);
            return false;
        }
    }

    /**
     * 从远程组件库管理器加载组件库
     */
    private boolean loadFromRemoteManager() {
        try {
            VueKitLogger.debug(LOG, "尝试从远程组件库管理器加载组件库");

            String libraryId = getLibraryIdByType(libraryType);
            if (libraryId == null) {
                return false;
            }

            ComponentLibraryManager manager = new ComponentLibraryManager();
            List<ComponentLibrary> libraries = manager.getAllLibraries();

            for (ComponentLibrary library : libraries) {
                if (libraryId.equals(library.getId())) {
                    VueKitLogger.info(LOG, "✅ 在远程管理器中找到匹配的组件库: " + library.getName());

                    if (library.getComponents() != null) {
                        int beforeCount = componentsList.size();

                        for (ComponentInfo componentInfo : library.getComponents()) {
                            try {
                                if (componentInfo != null) {
                                    ElementPlusComponent component = convertToElementPlusComponent(componentInfo);
                                    if (component != null) {
                                        componentsMap.put(component.getName(), component);
                                        componentsList.add(component);
                                    }
                                }
                            } catch (Exception e) {
                                VueKitLogger.warn(LOG, "转换远程组件失败: " + componentInfo.getName(), e);
                            }
                        }

                        int addedCount = componentsList.size() - beforeCount;
                        VueKitLogger.info(LOG, "从远程管理器成功加载 " + addedCount + " 个组件");
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            VueKitLogger.error(LOG, "从远程管理器加载组件库失败", e);
            return false;
        }
    }

    /**
     * 将 ComponentInfo 转换为 ElementPlusComponent
     */
    private ElementPlusComponent convertToElementPlusComponent(ComponentInfo componentInfo) {
        Objects.requireNonNull(componentInfo, "ComponentInfo不能为null");

        try {
            ElementPlusComponent component = new ElementPlusComponent();
            component.setName(componentInfo.getName());
            component.setDescription(componentInfo.getDescription());
            component.setVersion(getComponentVersionInfo());
            component.setDocUrl(generateDocumentationUrl(componentInfo.getName()));

            // 转换属性
            if (componentInfo.getProps() != null) {
                List<ElementPlusProp> props = new ArrayList<>();
                for (ComponentInfo.ComponentProp prop : componentInfo.getProps()) {
                    if (prop != null) {
                        ElementPlusProp elementProp = new ElementPlusProp();
                        elementProp.setName(prop.getName());
                        elementProp.setType(prop.getType());
                        elementProp.setDescription(prop.getDescription());
                        elementProp.setDefaultValue(prop.getDefaultValue());
                        try {
                            elementProp.setRequired(prop.isRequired());
                        } catch (NoSuchMethodError e) {
                            elementProp.setRequired(false);
                        }
                        props.add(elementProp);
                    }
                }
                component.setProps(props);
            }

            // 转换事件
            if (componentInfo.getEvents() != null) {
                List<ElementPlusEvent> events = new ArrayList<>();
                for (ComponentInfo.ComponentEvent event : componentInfo.getEvents()) {
                    if (event != null) {
                        ElementPlusEvent elementEvent = new ElementPlusEvent();
                        elementEvent.setName(event.getName());
                        elementEvent.setDescription(event.getDescription());
                        try {
                            elementEvent.setParameters(event.getParameters());
                        } catch (NoSuchMethodError e) {
                            elementEvent.setParameters("");
                        }
                        events.add(elementEvent);
                    }
                }
                component.setEvents(events);
            }

            // 转换插槽
            if (componentInfo.getSlots() != null) {
                List<ElementPlusSlot> slots = new ArrayList<>();
                for (ComponentInfo.ComponentSlot slot : componentInfo.getSlots()) {
                    if (slot != null) {
                        ElementPlusSlot elementSlot = new ElementPlusSlot();
                        elementSlot.setName(slot.getName());
                        elementSlot.setDescription(slot.getDescription());
                        try {
                            elementSlot.setScope(slot.getScope());
                        } catch (NoSuchMethodError e) {
                            elementSlot.setScope("");
                        }
                        slots.add(elementSlot);
                    }
                }
                component.setSlots(slots);
            }

            return component;

        } catch (Exception e) {
            VueKitLogger.error(LOG, "转换组件失败: " + componentInfo.getName(), e);
            throw new RuntimeException("转换组件失败: " + componentInfo.getName(), e);
        }
    }

    /**
     * 获取组件版本信息
     */
    private String getComponentVersionInfo() {
        // 尝试从项目的 package.json 中读取实际版本
        String actualVersion = getActualLibraryVersion();
        if (actualVersion != null && !actualVersion.isEmpty()) {
            return actualVersion;
        }

        // 如果无法获取实际版本，返回默认版本信息
        switch (libraryType) {
            case ELEMENT_UI:
                return "Element UI >=2.0.0";
            case ELEMENT_PLUS:
                return "Element Plus >=1.0.0";
            case ANT_DESIGN_VUE:
                return "Ant Design Vue >=2.0.0";
            default:
                return "Unknown";
        }
    }

    /**
     * 从项目的 package.json 中获取实际的组件库版本
     */
    private String getActualLibraryVersion() {
        try {
            // 获取组件库对应的包名
            String packageName = getPackageNameByLibraryType();
            if (packageName == null) {
                return null;
            }

            // 从 package.json 中查找版本
            String version = findPackageVersion(packageName);
            if (version != null) {
                return libraryType.getDisplayName() + " " + version;
            }

        } catch (Exception e) {
            VueKitLogger.warn(LOG, "获取实际版本失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 根据组件库类型获取对应的包名
     */
    private String getPackageNameByLibraryType() {
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
     * 从项目的 package.json 中查找指定包的版本
     */
    private String findPackageVersion(String packageName) {
        try {
            // 查找 package.json 文件
            com.intellij.openapi.vfs.VirtualFile projectDir = project.getBaseDir();
            com.intellij.openapi.vfs.VirtualFile packageJson = projectDir.findChild("package.json");
            
            if (packageJson == null || !packageJson.exists()) {
                return null;
            }

            // 读取 package.json 内容
            String content = new String(packageJson.contentsToByteArray(), java.nio.charset.StandardCharsets.UTF_8);
            
            // 简单的 JSON 解析（查找版本信息）
            String version = extractVersionFromJson(content, packageName);
            if (version != null) {
                VueKitLogger.debug(LOG, "找到包 " + packageName + " 的版本: " + version);
                return version;
            }

        } catch (Exception e) {
            VueKitLogger.warn(LOG, "查找包版本失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 从 JSON 字符串中提取指定包的版本
     */
    private String extractVersionFromJson(String jsonContent, String packageName) {
        try {
            // 查找 dependencies 和 devDependencies 中的版本信息
            String[] searchPatterns = {
                "\"" + packageName + "\"\\s*:\\s*\"([^\"]+)\"",
                "'" + packageName + "'\\s*:\\s*'([^']+)'"
            };

            for (String pattern : searchPatterns) {
                java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher matcher = regex.matcher(jsonContent);
                
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }

        } catch (Exception e) {
            VueKitLogger.warn(LOG, "提取版本信息失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 获取所有可用的组件列表
     */
    public List<ElementPlusComponent> getAllComponents() {
        VueKitLogger.debug(LOG, "开始获取所有可用组件...");

        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 获取项目启用的组件库配置
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibrariesForProject();

        // 如果没有启用任何组件库，返回空列表
        if (enabledLibraries.isEmpty()) {
            VueKitLogger.debug(LOG, "没有启用任何组件库，返回空组件列表");
            return allComponents;
        }

        // 添加内置组件库的组件（根据配置过滤）
        if (enabledLibraries.contains(libraryType)) {
            allComponents.addAll(componentsList);
            VueKitLogger.debug(LOG, "添加内置组件库组件: " + componentsList.size() + " 个");
        }

        // 添加自定义组件库的组件（只有在启用相应组件库时才添加）
        addCustomComponents(allComponents, enabledLibraries);

        // 添加从官方组件库市场下载的组件库（只有在启用相应组件库时才添加）
        addDownloadedOfficialComponents(allComponents, enabledLibraries);

        VueKitLogger.info(LOG, "总共获取到 " + allComponents.size() + " 个组件");
        return allComponents;
    }

    /**
     * 获取项目启用的组件库配置
     */
    private Set<ComponentLibraryDetector.LibraryType> getEnabledLibrariesForProject() {
        try {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            Set<ComponentLibraryDetector.LibraryType> enabledLibraries = configManager.getEnabledLibraries(project);
            
            VueKitLogger.debug(LOG, "获取到项目启用的组件库: " + 
                (enabledLibraries.isEmpty() ? "无" : enabledLibraries.stream()
                    .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", "))));
            
            return enabledLibraries;
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取项目启用的组件库配置失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 根据前缀获取匹配的组件列表
     */
    public List<ElementPlusComponent> getComponentsByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAllComponents();
        }

        VueKitLogger.debug(LOG, "开始搜索前缀为 '" + prefix + "' 的组件");
        List<ElementPlusComponent> matchingComponents = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();

        // 获取项目启用的组件库配置
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibrariesForProject();

        // 从内置组件库查找
        if (enabledLibraries.contains(libraryType)) {
            for (ElementPlusComponent component : componentsList) {
                if (component.getName() != null &&
                        component.getName().toLowerCase().startsWith(lowerPrefix)) {
                    matchingComponents.add(component);
                }
            }
        }

        // 从自定义组件库查找
        List<ElementPlusComponent> customComponents = new ArrayList<>();
        addCustomComponents(customComponents, enabledLibraries);
        for (ElementPlusComponent component : customComponents) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
            }
        }

        // 从下载的官方组件库查找
        List<ElementPlusComponent> downloadedComponents = new ArrayList<>();
        addDownloadedOfficialComponents(downloadedComponents, enabledLibraries);
        for (ElementPlusComponent component : downloadedComponents) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
            }
        }

        VueKitLogger.info(LOG, "前缀 '" + prefix + "' 搜索完成，找到 " + matchingComponents.size() + " 个匹配组件");
        return matchingComponents;
    }

    /**
     * 根据前缀搜索组件（模糊匹配）
     * 
     * 搜索逻辑：
     * 1. 收集所有可用的组件（内置、自定义、官方）
     * 2. 如果前缀为空，返回所有组件
     * 3. 使用模糊匹配（包含关系）而不是精确前缀匹配
     * 4. 不区分大小写
     * 
     * 与 getComponentsByPrefix 的区别：
     * - getComponentsByPrefix: 精确前缀匹配（startsWith）
     * - searchComponents: 模糊匹配（contains）
     *
     * @param prefix 要搜索的前缀，如果为 null 或空字符串则返回所有组件
     * @return 匹配的组件列表，如果没有匹配的组件则返回空列表
     */
    public List<ElementPlusComponent> searchComponents(String prefix) {
        VueKitLogger.debug(LOG, "开始模糊搜索组件，前缀: '" + prefix + "'");

        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 获取项目启用的组件库配置
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibrariesForProject();

        // 1. 添加内置组件库的组件（只添加启用的组件库）
        int builtinCount = 0;
        if (enabledLibraries.contains(libraryType)) {
            allComponents.addAll(componentsList);
            builtinCount = componentsList.size();
            VueKitLogger.debug(LOG, "添加内置组件: " + builtinCount + " 个");
        } else {
            VueKitLogger.debug(LOG, "跳过内置组件库 (类型: " + libraryType.getDisplayName() + " 未启用)");
        }

        // 2. 添加自定义组件库的组件
        int beforeCustom = allComponents.size();
        addCustomComponents(allComponents, enabledLibraries);
        int customCount = allComponents.size() - beforeCustom;
        VueKitLogger.debug(LOG, "添加自定义组件: " + customCount + " 个");

        // 3. 添加从官方组件库市场下载的组件库
        int beforeOfficial = allComponents.size();
        addDownloadedOfficialComponents(allComponents, enabledLibraries);
        int officialCount = allComponents.size() - beforeOfficial;
        VueKitLogger.debug(LOG, "添加官方组件: " + officialCount + " 个");

        if (prefix == null || prefix.isEmpty()) {
            VueKitLogger.debug(LOG, "前缀为空，返回所有 " + allComponents.size() + " 个组件");
            return allComponents;
        }

        // 4. 执行模糊搜索
        String lowerPrefix = prefix.toLowerCase();
        List<ElementPlusComponent> matchingComponents = allComponents.stream()
                .filter(component -> component.getName() != null &&
                        component.getName().toLowerCase().contains(lowerPrefix))
                .collect(java.util.stream.Collectors.toList());

        VueKitLogger.info(LOG, "模糊搜索完成，前缀 '" + prefix + "' 找到 " +
                matchingComponents.size() + " 个匹配组件，总共 " + allComponents.size() + " 个组件");

        return matchingComponents;
    }

    /**
     * 根据组件名获取指定的组件
     */
    public ElementPlusComponent getComponent(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        VueKitLogger.debug(LOG, "开始查找组件: " + componentName);

        // 获取项目启用的组件库配置
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibrariesForProject();

        // 先从内置组件库查找
        if (enabledLibraries.contains(libraryType)) {
            ElementPlusComponent component = componentsMap.get(componentName);
            if (component != null) {
                return component;
            }
        }

        // 从自定义组件库查找
        try {
            ComponentInfo componentInfo = CustomComponentLibraryManager.getCustomComponent(componentName);
            if (componentInfo != null) {
                return convertToElementPlusComponent(componentInfo);
            }
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "从自定义组件库查找组件时出错: " + e.getMessage());
        }

        // 从下载的官方组件库查找
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                ComponentLibraryDetector.LibraryType libraryType = getLibraryTypeByName(library.getName());
                if (libraryType == null || !enabledLibraries.contains(libraryType)) {
                    continue; // 跳过未启用的组件库
                }

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

        VueKitLogger.debug(LOG, "未找到组件: " + componentName);
        return null;
    }

    /**
     * 添加自定义组件到组件列表中
     */
    private void addCustomComponents(List<ElementPlusComponent> allComponents, Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
        if (allComponents == null) {
            throw new IllegalArgumentException("目标组件列表不能为 null");
        }

        // 如果没有启用任何组件库，不添加自定义组件
        if (enabledLibraries.isEmpty()) {
            VueKitLogger.debug(LOG, "没有启用任何组件库，跳过自定义组件");
            return;
        }

        try {
            List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries =
                    CustomComponentLibraryManager.getAllCustomLibraries();

            for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
                if (config.getComponents() != null) {
                    for (ComponentInfo componentInfo : config.getComponents()) {
                        try {
                            ElementPlusComponent convertedComponent = convertToElementPlusComponent(componentInfo);
                            allComponents.add(convertedComponent);
                        } catch (Exception e) {
                            VueKitLogger.warn(LOG, "转换组件失败: " + componentInfo.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载自定义组件库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加从官方组件库市场下载的组件库
     */
    private void addDownloadedOfficialComponents(List<ElementPlusComponent> allComponents, Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
        if (allComponents == null) {
            throw new IllegalArgumentException("目标组件列表不能为 null");
        }

        // 如果没有启用任何组件库，不添加官方组件
        if (enabledLibraries.isEmpty()) {
            VueKitLogger.debug(LOG, "没有启用任何组件库，跳过官方组件");
            return;
        }

        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                ComponentLibraryDetector.LibraryType libraryType = getLibraryTypeByName(library.getName());
                if (libraryType == null || !enabledLibraries.contains(libraryType)) {
                    VueKitLogger.debug(LOG, "跳过未启用的官方组件库: " + library.getName());
                    continue;
                }

                if ("OFFICIAL".equals(library.getSource()) && library.getComponents() != null) {
                    VueKitLogger.debug(LOG, "添加启用的官方组件库: " + library.getName() + " (版本: " + library.getVersion() + ")");
                    for (ComponentInfo componentInfo : library.getComponents()) {
                        try {
                            ElementPlusComponent convertedComponent = convertToElementPlusComponent(componentInfo);
                            allComponents.add(convertedComponent);
                        } catch (Exception e) {
                            VueKitLogger.warn(LOG, "转换官方组件失败: " + componentInfo.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载下载的官方组件库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重新加载组件数据
     */
    public void reloadComponents() {
        VueKitLogger.info(LOG, "=== 开始重新加载组件数据 ===");

        int previousComponentCount = componentsMap.size();
        VueKitLogger.debug(LOG, "重新加载前组件数量: " + previousComponentCount);

        // 清空现有数据
        componentsMap.clear();
        componentsList.clear();

        // 重新加载组件数据
        loadComponents();

        int newComponentCount = componentsMap.size();
        VueKitLogger.info(LOG, "=== 组件数据重新加载完成 ===");
        VueKitLogger.info(LOG, "重新加载前: " + previousComponentCount + " 个组件");
        VueKitLogger.info(LOG, "重新加载后: " + newComponentCount + " 个组件");
    }

    /**
     * 获取组件的所有属性
     */
    public List<ElementPlusProp> getComponentProps(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        ElementPlusComponent component = getComponent(componentName);
        if (component != null && component.getProps() != null) {
            return component.getProps();
        }

        return new ArrayList<>();
    }

    /**
     * 获取组件的所有事件
     */
    public List<ElementPlusEvent> getComponentEvents(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        ElementPlusComponent component = getComponent(componentName);
        if (component != null && component.getEvents() != null) {
            return component.getEvents();
        }

        return new ArrayList<>();
    }

    /**
     * 获取组件的所有插槽
     */
    public List<ElementPlusSlot> getComponentSlots(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        ElementPlusComponent component = getComponent(componentName);
        if (component != null && component.getSlots() != null) {
            return component.getSlots();
        }

        return new ArrayList<>();
    }

    /**
     * 检查组件是否存在于内置组件库中
     */
    public boolean hasComponent(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        return componentsMap.containsKey(componentName);
    }

    /**
     * 获取内置组件库的组件总数
     */
    public int getComponentCount() {
        return componentsList.size();
    }

    /**
     * 获取当前检测到的组件库类型
     */
    public ComponentLibraryDetector.LibraryType getLibraryType() {
        return libraryType;
    }

    /**
     * 获取当前组件库的显示名称
     */
    public String getLibraryDisplayName() {
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

    /**
     * 获取组件所属的组件库显示名称
     *
     * @param componentName 组件名称
     * @return 组件库的显示名称
     */
    public String getComponentLibraryDisplayName(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            return "未知组件库";
        }

        // 检查是否是自定义组件库的组件
        if (CustomComponentLibraryManager.isCustomComponent(componentName)) {
            return CustomComponentLibraryManager.getCustomLibraryDisplayName(componentName);
        }

        // 检查是否是内置组件库的组件
        if (ComponentLibraryDetector.isComponentFromLibrary(componentName, libraryType)) {
            return libraryType.getDisplayName();
        }

        // 检查是否来自其他官方组件库
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                ComponentLibraryDetector.LibraryType libraryType = getLibraryTypeByName(library.getName());
                if (libraryType == null || !getEnabledLibrariesForProject().contains(libraryType)) {
                    continue; // 跳过未启用的组件库
                }

                if (library.getComponents() != null) {
                    for (ComponentInfo info : library.getComponents()) {
                        if (componentName.equals(info.getName())) {
                            return library.getName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "获取组件库显示名称失败: " + e.getMessage());
        }

        return "未知组件库";
    }

    /**
     * 获取组件所属的组件库版本
     *
     * @param componentName 组件名称
     * @return 组件库的版本号
     */
    public String getComponentLibraryVersion(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            return "未知版本";
        }

        // 检查是否是自定义组件库的组件
        if (CustomComponentLibraryManager.isCustomComponent(componentName)) {
            return CustomComponentLibraryManager.getCustomLibraryVersion(componentName);
        }

        // 检查是否是内置组件库的组件
        if (ComponentLibraryDetector.isComponentFromLibrary(componentName, libraryType)) {
            return getComponentVersionInfo();
        }

        // 检查是否来自其他官方组件库
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                ComponentLibraryDetector.LibraryType libraryType = getLibraryTypeByName(library.getName());
                if (libraryType == null || !getEnabledLibrariesForProject().contains(libraryType)) {
                    continue; // 跳过未启用的组件库
                }

                if (library.getComponents() != null) {
                    for (ComponentInfo info : library.getComponents()) {
                        if (componentName.equals(info.getName())) {
                            return library.getVersion();
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "获取组件库版本失败: " + e.getMessage());
        }

        return "未知版本";
    }

    /**
     * 根据组件库名称获取对应的 LibraryType
     * 
     * @param libraryName 组件库名称
     * @return 对应的 LibraryType，如果找不到则返回 null
     */
    private ComponentLibraryDetector.LibraryType getLibraryTypeByName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return null;
        }
        
        // 移除版本号部分，只保留组件库名称
        String cleanName = libraryName.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
        
        // 映射组件库名称到 LibraryType
        switch (cleanName.toLowerCase()) {
            case "element plus":
            case "element-plus":
                return ComponentLibraryDetector.LibraryType.ELEMENT_PLUS;
            case "element ui":
            case "element-ui":
                return ComponentLibraryDetector.LibraryType.ELEMENT_UI;
            case "ant design vue":
            case "ant-design-vue":
                return ComponentLibraryDetector.LibraryType.ANT_DESIGN_VUE;
            case "vuetify":
                return ComponentLibraryDetector.LibraryType.VUETIFY;
            case "quasar":
                return ComponentLibraryDetector.LibraryType.QUASAR;
            default:
                return null;
        }
    }
}
