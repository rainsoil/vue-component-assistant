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
        
        // 注册到 ComponentProviderManager（避免重复注册）
        if (!ComponentProviderManager.isProviderRegistered(project)) {
            ComponentProviderManager.registerProvider(project, this);
        }
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

            // 获取所有组件库，包括自定义组件库
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();
            boolean hasLoadedAny = false;

            for (ComponentLibrary library : allLibraries) {
                // 加载所有自定义组件库（本地和远程）
                if (("CUSTOM_LOCAL".equals(library.getSource()) || "CUSTOM_REMOTE".equals(library.getSource())) 
                    && library.getComponents() != null) {
                    
                    VueKitLogger.debug(LOG, "加载自定义组件库: " + library.getName() + ", 组件数量: " + library.getComponents().size());

                    // 加载组件数据
                    for (ComponentInfo component : library.getComponents()) {
                        // 将 ComponentInfo 转换为 ElementPlusComponent
                        ElementPlusComponent elementPlusComponent = convertToElementPlusComponent(component);
                        componentsMap.put(elementPlusComponent.getName(), elementPlusComponent);
                        componentsList.add(elementPlusComponent);
                    }
                    
                    hasLoadedAny = true;
                }
            }

            if (hasLoadedAny) {
                VueKitLogger.debug(LOG, "成功从本地缓存加载自定义组件库");
                return true;
            } else {
                VueKitLogger.debug(LOG, "本地缓存中未找到自定义组件库");
                return false;
            }

        } catch (Exception e) {
            VueKitLogger.error(LOG, "从本地缓存加载组件库失败: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将 ComponentInfo 转换为 ElementPlusComponent
     * 
     * 转换逻辑：
     * 1. 创建新的 ElementPlusComponent 对象
     * 2. 复制基本信息（名称、描述）
     * 3. 转换属性列表（props）
     * 4. 转换事件列表（events）
     * 5. 转换插槽列表（slots）
     * 
     * 注意：此方法用于统一不同组件库的数据结构，确保补全功能的一致性
     * 
     * @param componentInfo 源组件信息对象，不能为 null
     * @return 转换后的 ElementPlusComponent 对象
     * @throws IllegalArgumentException 如果 componentInfo 为 null
     */
    private ElementPlusComponent convertToElementPlusComponent(ComponentInfo componentInfo) {
        if (componentInfo == null) {
            throw new IllegalArgumentException("ComponentInfo 对象不能为 null");
        }
        
        VueKitLogger.debug(LOG, "开始转换组件: " + componentInfo.getName());
        
        ElementPlusComponent component = new ElementPlusComponent();
        component.setName(componentInfo.getName());
        component.setDescription(componentInfo.getDescription());
        
        // 转换属性列表
        List<ElementPlusProp> props = new ArrayList<>();
        if (componentInfo.getProps() != null) {
            for (ComponentInfo.ComponentProp prop : componentInfo.getProps()) {
                ElementPlusProp elementPlusProp = new ElementPlusProp();
                elementPlusProp.setName(prop.getName());
                elementPlusProp.setType(prop.getType());
                elementPlusProp.setDescription(prop.getDescription());
                elementPlusProp.setDefaultValue(prop.getDefaultValue());
                props.add(elementPlusProp);
            }
            VueKitLogger.debug(LOG, "转换了 " + props.size() + " 个属性");
        }
        component.setProps(props);
        
        // 转换事件列表
        List<ElementPlusEvent> events = new ArrayList<>();
        if (componentInfo.getEvents() != null) {
            for (ComponentInfo.ComponentEvent event : componentInfo.getEvents()) {
                ElementPlusEvent elementPlusEvent = new ElementPlusEvent();
                elementPlusEvent.setName(event.getName());
                elementPlusEvent.setDescription(event.getDescription());
                events.add(elementPlusEvent);
            }
            VueKitLogger.debug(LOG, "转换了 " + events.size() + " 个事件");
        }
        component.setEvents(events);
        
        // 转换插槽列表
        List<ElementPlusSlot> slots = new ArrayList<>();
        if (componentInfo.getSlots() != null) {
            for (ComponentInfo.ComponentSlot slot : componentInfo.getSlots()) {
                ElementPlusSlot elementPlusSlot = new ElementPlusSlot();
                elementPlusSlot.setName(slot.getName());
                elementPlusSlot.setDescription(slot.getDescription());
                slots.add(elementPlusSlot);
            }
            VueKitLogger.debug(LOG, "转换了 " + slots.size() + " 个插槽");
        }
        component.setSlots(slots);
        
        VueKitLogger.debug(LOG, "组件转换完成: " + component.getName() + 
            " (属性: " + props.size() + ", 事件: " + events.size() + ", 插槽: " + slots.size() + ")");
        
        return component;
    }

    /**
     * 获取所有可用的组件列表
     * 
     * 组件来源优先级：
     * 1. 内置组件库（Element Plus、Element UI、Ant Design Vue）
     * 2. 用户自定义组件库（本地和远程）
     * 3. 从官方组件库市场下载的组件库
     * 
     * 注意：此方法会合并所有来源的组件，确保用户能够访问到所有可用的组件
     * 
     * @return 包含所有可用组件的列表，如果没有任何组件则返回空列表
     */
    public List<ElementPlusComponent> getAllComponents() {
        VueKitLogger.debug(LOG, "开始获取所有可用组件...");
        
        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 1. 添加内置组件库的组件
        int builtinCount = componentsList.size();
        allComponents.addAll(componentsList);
        VueKitLogger.debug(LOG, "添加内置组件库组件: " + builtinCount + " 个");

        // 2. 添加自定义组件库的组件
        int beforeCustom = allComponents.size();
        addCustomComponents(allComponents);
        int customCount = allComponents.size() - beforeCustom;
        VueKitLogger.debug(LOG, "添加自定义组件库组件: " + customCount + " 个");

        // 3. 添加从官方组件库市场下载的组件库
        int beforeOfficial = allComponents.size();
        addDownloadedOfficialComponents(allComponents);
        int officialCount = allComponents.size() - beforeOfficial;
        VueKitLogger.debug(LOG, "添加官方组件库组件: " + officialCount + " 个");

        VueKitLogger.info(LOG, "总共获取到 " + allComponents.size() + " 个组件 " +
            "(内置: " + builtinCount + ", 自定义: " + customCount + ", 官方: " + officialCount + ")");

        return allComponents;
    }

    /**
     * 根据前缀获取匹配的组件列表
     * 
     * 搜索逻辑：
     * 1. 如果前缀为空，返回所有可用组件
     * 2. 从内置组件库中查找匹配的组件
     * 3. 从自定义组件库中查找匹配的组件
     * 4. 从官方组件库中查找匹配的组件
     * 5. 合并所有匹配结果并返回
     * 
     * 搜索策略：
     * - 使用不区分大小写的匹配
     * - 支持部分匹配（组件名以指定前缀开头）
     * - 按组件库优先级排序结果
     * 
     * @param prefix 要搜索的组件前缀，如果为 null 或空字符串则返回所有组件
     * @return 匹配前缀的组件列表，如果没有匹配的组件则返回空列表
     */
    public List<ElementPlusComponent> getComponentsByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            VueKitLogger.debug(LOG, "前缀为空，返回所有组件");
            return getAllComponents();
        }

        VueKitLogger.debug(LOG, "开始搜索前缀为 '" + prefix + "' 的组件");
        List<ElementPlusComponent> matchingComponents = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();

        // 1. 从内置组件库查找
        int builtinMatches = 0;
        for (ElementPlusComponent component : componentsList) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
                builtinMatches++;
            }
        }
        VueKitLogger.debug(LOG, "内置组件库中找到 " + builtinMatches + " 个匹配组件");

        // 2. 从自定义组件库查找
        List<ElementPlusComponent> customComponents = new ArrayList<>();
        addCustomComponents(customComponents);
        int customMatches = 0;
        for (ElementPlusComponent component : customComponents) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
                customMatches++;
            }
        }
        VueKitLogger.debug(LOG, "自定义组件库中找到 " + customMatches + " 个匹配组件");

        // 3. 从下载的官方组件库查找
        List<ElementPlusComponent> downloadedComponents = new ArrayList<>();
        addDownloadedOfficialComponents(downloadedComponents);
        int officialMatches = 0;
        for (ElementPlusComponent component : downloadedComponents) {
            if (component.getName() != null &&
                    component.getName().toLowerCase().startsWith(lowerPrefix)) {
                matchingComponents.add(component);
                officialMatches++;
            }
        }
        VueKitLogger.debug(LOG, "官方组件库中找到 " + officialMatches + " 个匹配组件");

        VueKitLogger.info(LOG, "前缀 '" + prefix + "' 搜索完成，总共找到 " + matchingComponents.size() + 
            " 个匹配组件 (内置: " + builtinMatches + ", 自定义: " + customMatches + ", 官方: " + officialMatches + ")");

        return matchingComponents;
    }

    /**
     * 根据组件名获取指定的组件
     * 
     * 查找逻辑（按优先级）：
     * 1. 内置组件库：从已加载的组件映射中查找
     * 2. 自定义组件库：从本地缓存的自定义组件库中查找
     * 3. 官方组件库：从已下载的官方组件库中查找
     * 
     * 性能优化：
     * - 内置组件库使用HashMap快速查找
     * - 自定义组件库使用缓存管理器
     * - 官方组件库按需加载
     * 
     * @param componentName 要查找的组件名称，不能为 null 或空字符串
     * @return 找到的组件对象，如果未找到则返回 null
     * @throws IllegalArgumentException 如果组件名称为 null 或空字符串
     */
    public ElementPlusComponent getComponent(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        VueKitLogger.debug(LOG, "开始查找组件: " + componentName);
        
        // 1. 先从内置组件库查找（最快）
        ElementPlusComponent component = componentsMap.get(componentName);
        if (component != null) {
            VueKitLogger.debug(LOG, "在内置组件库中找到组件: " + componentName);
            return component;
        }

        // 2. 从自定义组件库查找
        try {
            ComponentInfo componentInfo = CustomComponentLibraryManager.getCustomComponent(componentName);
            if (componentInfo != null) {
                VueKitLogger.debug(LOG, "在自定义组件库中找到组件: " + componentName);
                return convertToElementPlusComponent(componentInfo);
            }
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "从自定义组件库查找组件时出错: " + e.getMessage());
        }

        // 3. 从下载的官方组件库查找
        try {
            // 每次都重新创建 ComponentLibraryManager 实例，确保获取最新数据
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : allLibraries) {
                // 只处理从官方组件库市场下载的组件库
                if ("OFFICIAL".equals(library.getSource()) && library.getComponents() != null) {
                    for (ComponentInfo info : library.getComponents()) {
                        if (componentName.equals(info.getName())) {
                            VueKitLogger.debug(LOG, "在官方组件库中找到组件: " + componentName);
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

        // 1. 添加内置组件库的组件
        int builtinCount = componentsList.size();
        allComponents.addAll(componentsList);
        VueKitLogger.debug(LOG, "添加内置组件: " + builtinCount + " 个");

        // 2. 添加自定义组件库的组件
        int beforeCustom = allComponents.size();
        addCustomComponents(allComponents);
        int customCount = allComponents.size() - beforeCustom;
        VueKitLogger.debug(LOG, "添加自定义组件: " + customCount + " 个");

        // 3. 添加从官方组件库市场下载的组件库
        int beforeOfficial = allComponents.size();
        addDownloadedOfficialComponents(allComponents);
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
     * 添加自定义组件到组件列表中
     * 
     * 加载逻辑：
     * 1. 从缓存文件重新加载最新的自定义组件库数据
     * 2. 遍历所有自定义组件库
     * 3. 将每个组件转换为 ElementPlusComponent 格式
     * 4. 添加到目标列表中
     * 
     * 性能考虑：
     * - 每次都重新加载确保数据最新
     * - 使用转换方法统一数据格式
     * - 异常处理确保稳定性
     * 
     * @param allComponents 要添加自定义组件的目标列表，不能为 null
     * @throws IllegalArgumentException 如果目标列表为 null
     */
    private void addCustomComponents(List<ElementPlusComponent> allComponents) {
        if (allComponents == null) {
            throw new IllegalArgumentException("目标组件列表不能为 null");
        }
        
        try {
            // 每次都重新从缓存文件加载最新的自定义组件库数据
            // 这样可以确保获取到最新的组件信息
            List<CustomComponentLibraryManager.CustomLibraryConfig> customLibraries =
                    CustomComponentLibraryManager.getAllCustomLibraries();
            
            VueKitLogger.debug(LOG, "开始加载自定义组件库，数量: " + customLibraries.size());
            
            int totalComponents = 0;
            for (CustomComponentLibraryManager.CustomLibraryConfig config : customLibraries) {
                if (config.getComponents() != null) {
                    VueKitLogger.debug(LOG, "处理自定义组件库: " + config.getDisplayName() + 
                        ", 组件数量: " + config.getComponents().size());
                    
                    for (ComponentInfo componentInfo : config.getComponents()) {
                        try {
                            ElementPlusComponent convertedComponent = convertToElementPlusComponent(componentInfo);
                            allComponents.add(convertedComponent);
                            totalComponents++;
                        } catch (Exception e) {
                            VueKitLogger.warn(LOG, "转换组件失败: " + componentInfo.getName() + 
                                ", 错误: " + e.getMessage());
                        }
                    }
                } else {
                    VueKitLogger.warn(LOG, "自定义组件库 '" + config.getDisplayName() + "' 的组件列表为 null");
                }
            }
            
            VueKitLogger.debug(LOG, "成功加载自定义组件库，总共添加 " + totalComponents + " 个组件");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载自定义组件库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加从官方组件库市场下载的组件库
     * 
     * 加载逻辑：
     * 1. 创建新的 ComponentLibraryManager 实例确保数据最新
     * 2. 获取所有已下载的组件库
     * 3. 筛选出官方来源的组件库
     * 4. 将组件转换为统一格式并添加到目标列表
     * 
     * 性能考虑：
     * - 每次都重新创建管理器确保数据最新
     * - 只处理官方来源的组件库
     * - 异常处理确保稳定性
     * 
     * @param allComponents 要添加官方组件的目标列表，不能为 null
     * @throws IllegalArgumentException 如果目标列表为 null
     */
    private void addDownloadedOfficialComponents(List<ElementPlusComponent> allComponents) {
        if (allComponents == null) {
            throw new IllegalArgumentException("目标组件列表不能为 null");
        }
        
        try {
            // 每次都重新创建 ComponentLibraryManager 实例，确保获取最新数据
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();
            
            VueKitLogger.debug(LOG, "开始加载官方组件库，总库数量: " + allLibraries.size());
            
            int totalComponents = 0;
            int officialLibraryCount = 0;
            
            for (ComponentLibrary library : allLibraries) {
                // 只处理从官方组件库市场下载的组件库
                if ("OFFICIAL".equals(library.getSource()) && library.getComponents() != null) {
                    officialLibraryCount++;
                    int libraryComponentCount = library.getComponents().size();
                    VueKitLogger.debug(LOG, "处理官方组件库: " + library.getName() + 
                        ", 组件数量: " + libraryComponentCount);
                    
                    for (ComponentInfo componentInfo : library.getComponents()) {
                        try {
                            ElementPlusComponent convertedComponent = convertToElementPlusComponent(componentInfo);
                            allComponents.add(convertedComponent);
                            totalComponents++;
                        } catch (Exception e) {
                            VueKitLogger.warn(LOG, "转换官方组件失败: " + componentInfo.getName() + 
                                ", 错误: " + e.getMessage());
                        }
                    }
                }
            }
            
            VueKitLogger.debug(LOG, "成功加载官方组件库，库数量: " + officialLibraryCount + 
                ", 组件总数: " + totalComponents);
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载下载的官方组件库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重新加载组件数据
     * 
     * 使用场景：
     * - 组件库发生变化时（添加、删除、更新）
     * - 用户手动刷新组件数据
     * - 系统检测到组件库配置变更
     * 
     * 重新加载流程：
     * 1. 清空现有的组件映射和列表
     * 2. 重新执行组件检测和加载
     * 3. 更新组件库类型和数据路径
     * 4. 重新加载所有来源的组件
     * 
     * 注意：此操作会清空所有已加载的组件数据，请谨慎使用
     */
    public void reloadComponents() {
        VueKitLogger.info(LOG, "=== 开始重新加载组件数据 ===");
        
        // 记录重新加载前的组件数量
        int previousComponentCount = componentsMap.size();
        VueKitLogger.debug(LOG, "重新加载前组件数量: " + previousComponentCount);
        
        // 清空现有数据
        componentsMap.clear();
        componentsList.clear();
        VueKitLogger.debug(LOG, "已清空现有组件数据");
        
        // 重新加载组件数据
        loadComponents();
        
        // 记录重新加载后的组件数量
        int newComponentCount = componentsMap.size();
        VueKitLogger.info(LOG, "=== 组件数据重新加载完成 ===");
        VueKitLogger.info(LOG, "重新加载前: " + previousComponentCount + " 个组件");
        VueKitLogger.info(LOG, "重新加载后: " + newComponentCount + " 个组件");
        VueKitLogger.info(LOG, "组件数量变化: " + (newComponentCount - previousComponentCount));
    }

    /**
     * 获取组件的所有属性
     * 
     * 获取逻辑：
     * 1. 根据组件名查找组件
     * 2. 如果找到组件，返回其属性列表
     * 3. 如果未找到组件，返回空列表
     * 
     * @param componentName 组件名称，不能为 null 或空字符串
     * @return 组件属性列表，如果组件不存在则返回空列表
     * @throws IllegalArgumentException 如果组件名称为 null 或空字符串
     */
    public List<ElementPlusProp> getComponentProps(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        ElementPlusComponent component = getComponent(componentName);
        if (component != null && component.getProps() != null) {
            VueKitLogger.debug(LOG, "获取组件 '" + componentName + "' 的属性，数量: " + component.getProps().size());
            return component.getProps();
        }
        
        VueKitLogger.debug(LOG, "组件 '" + componentName + "' 不存在或无属性，返回空列表");
        return new ArrayList<>();
    }

    /**
     * 获取组件的所有事件
     * 
     * 获取逻辑：
     * 1. 根据组件名查找组件
     * 2. 如果找到组件，返回其事件列表
     * 3. 如果未找到组件，返回空列表
     * 
     * @param componentName 组件名称，不能为 null 或空字符串
     * @return 组件事件列表，如果组件不存在则返回空列表
     * @throws IllegalArgumentException 如果组件名称为 null 或空字符串
     */
    public List<ElementPlusEvent> getComponentEvents(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        ElementPlusComponent component = getComponent(componentName);
        if (component != null && component.getEvents() != null) {
            VueKitLogger.debug(LOG, "获取组件 '" + componentName + "' 的事件，数量: " + component.getEvents().size());
            return component.getEvents();
        }
        
        VueKitLogger.debug(LOG, "组件 '" + componentName + "' 不存在或无事件，返回空列表");
        return new ArrayList<>();
    }

    /**
     * 获取组件的所有插槽
     * 
     * 获取逻辑：
     * 1. 根据组件名查找组件
     * 2. 如果找到组件，返回其插槽列表
     * 3. 如果未找到组件，返回空列表
     * 
     * @param componentName 组件名称，不能为 null 或空字符串
     * @return 组件插槽列表，如果组件不存在则返回空列表
     * @throws IllegalArgumentException 如果组件名称为 null 或空字符串
     */
    public List<ElementPlusSlot> getComponentSlots(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        ElementPlusComponent component = getComponent(componentName);
        if (component != null && component.getSlots() != null) {
            VueKitLogger.debug(LOG, "获取组件 '" + componentName + "' 的插槽，数量: " + component.getSlots().size());
            return component.getSlots();
        }
        
        VueKitLogger.debug(LOG, "组件 '" + componentName + "' 不存在或无插槽，返回空列表");
        return new ArrayList<>();
    }

    /**
     * 检查组件是否存在于内置组件库中
     * 
     * 注意：此方法只检查内置组件库，不检查自定义或官方组件库
     * 如需检查所有来源，请使用 getComponent() 方法
     * 
     * @param componentName 要检查的组件名称，不能为 null 或空字符串
     * @return 如果组件存在于内置组件库中则返回 true，否则返回 false
     * @throws IllegalArgumentException 如果组件名称为 null 或空字符串
     */
    public boolean hasComponent(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        boolean exists = componentsMap.containsKey(componentName);
        VueKitLogger.debug(LOG, "检查组件 '" + componentName + "' 是否存在: " + exists);
        return exists;
    }

    /**
     * 获取内置组件库的组件总数
     * 
     * 注意：此方法只返回内置组件库的组件数量
     * 如需获取所有来源的组件总数，请使用 getAllComponents().size()
     * 
     * @return 内置组件库中的组件数量
     */
    public int getComponentCount() {
        int count = componentsList.size();
        VueKitLogger.debug(LOG, "内置组件库组件总数: " + count);
        return count;
    }

    /**
     * 获取当前检测到的组件库类型
     * 
     * @return 组件库类型枚举值
     */
    public ComponentLibraryDetector.LibraryType getLibraryType() {
        VueKitLogger.debug(LOG, "获取组件库类型: " + libraryType.getDisplayName());
        return libraryType;
    }

    /**
     * 获取当前组件库的显示名称
     * 
     * @return 组件库的友好显示名称
     */
    public String getLibraryDisplayName() {
        String displayName = libraryType.getDisplayName();
        VueKitLogger.debug(LOG, "获取组件库显示名称: " + displayName);
        return displayName;
    }

    /**
     * 获取组件所属的组件库显示名称
     * 
     * 判断逻辑：
     * 1. 首先检查是否是自定义组件库的组件
     * 2. 如果是自定义组件，返回其所属的自定义组件库名称
     * 3. 如果不是自定义组件，返回当前检测到的内置组件库名称
     * 
     * 注意：此方法主要用于UI显示，帮助用户了解组件的来源
     * 
     * @param componentName 要查询的组件名称，不能为 null 或空字符串
     * @return 组件所属的组件库显示名称
     * @throws IllegalArgumentException 如果组件名称为 null 或空字符串
     */
    public String getComponentLibraryDisplayName(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        VueKitLogger.debug(LOG, "查询组件 '" + componentName + "' 所属的组件库");
        
        // 检查是否是自定义组件库的组件
        if (CustomComponentLibraryManager.isCustomComponent(componentName)) {
            String customLibraryName = CustomComponentLibraryManager.getCustomLibraryDisplayName(componentName);
            VueKitLogger.debug(LOG, "组件 '" + componentName + "' 属于自定义组件库: " + customLibraryName);
            return customLibraryName;
        }

        // 返回内置组件库的显示名称
        String builtinLibraryName = libraryType.getDisplayName();
        VueKitLogger.debug(LOG, "组件 '" + componentName + "' 属于内置组件库: " + builtinLibraryName);
        return builtinLibraryName;
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
