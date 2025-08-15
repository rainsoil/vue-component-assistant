package com.chu7.vuecomponentassistant.completion2;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.ErrorHandler;
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
import java.util.Objects;

/**
 * 通用组件数据提供者
 *
 * 功能说明：
 * - 从 JSON 文件中加载组件数据
 * - 支持 Element UI、Element Plus、Ant Design Vue
 * - 提供组件查询功能
 * - 支持组件属性、事件、插槽等信息
 *
 * 特性：
 * - 智能组件库检测：自动识别项目使用的组件库类型
 * - 多级数据源：内置资源、本地缓存、远程组件库
 * - 高性能查询：使用HashMap提供O(1)的组件查找
 * - 内存管理：支持组件数据的懒加载和缓存
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

    /**
     * 构造函数
     * 
     * 初始化组件提供者，检测项目使用的组件库类型并加载相应的组件数据。
     * 
     * @param project IntelliJ项目对象，不能为null
     * @throws IllegalArgumentException 当project为null时抛出
     */
    public ComponentProvider(Project project) {
        Objects.requireNonNull(project, "Project对象不能为null");
        
        VueKitLogger.debug(LOG, "=== ComponentProvider 初始化开始 ===");
        this.componentsMap = new HashMap<>();
        this.componentsList = new ArrayList<>();

        try {
            // 检测项目使用的组件库
            this.libraryType = ComponentLibraryDetector.detectComponentLibrary(project);
            VueKitLogger.debug(LOG, "检测到的组件库类型: " + this.libraryType.getDisplayName());
            VueKitLogger.debug(LOG, "组件库枚举值: " + this.libraryType.name());
            VueKitLogger.debug(LOG, "组件库包名: " + this.libraryType.getPackageName());
            
            this.dataPath = ComponentLibraryDetector.getComponentDataPath(libraryType);
            VueKitLogger.debug(LOG, "数据文件路径: " + this.dataPath);

            // 打印检测信息
            ComponentLibraryDetector.printDetectionInfo(project);
            
            // 额外调试信息
            VueKitLogger.info(LOG, "=== 组件库检测详情 ===");
            VueKitLogger.info(LOG, "检测结果: " + this.libraryType.getDisplayName());
            VueKitLogger.info(LOG, "枚举名称: " + this.libraryType.name());
            VueKitLogger.info(LOG, "包名: " + this.libraryType.getPackageName());
            VueKitLogger.info(LOG, "数据路径: " + this.dataPath);
            VueKitLogger.info(LOG, "========================");

            loadComponents();
            VueKitLogger.debug(LOG, "=== ComponentProvider 初始化完成 ===");
            
            // 注册到 ComponentProviderManager（避免重复注册）
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
     * 
     * 按照优先级顺序加载组件数据：
     * 1. 本地缓存的组件库（下载的组件库）
     * 2. 内置资源文件（仅作为备用）
     */
    private void loadComponents() {
        try {
            VueKitLogger.info(LOG, "=== 开始加载组件数据 ===");
            VueKitLogger.info(LOG, "检测到的组件库类型: " + libraryType.getDisplayName());
            VueKitLogger.info(LOG, "组件库ID: " + getLibraryIdByType(libraryType));
            
            // 首先尝试从本地缓存的组件库加载（下载的组件库）
            if (loadFromLocalCache()) {
                VueKitLogger.info(LOG, "✅ 从本地缓存（下载的组件库）加载成功，组件数量: " + componentsList.size());
                return;
            }

            // 如果本地缓存没有，尝试从远程组件库管理器加载
            if (loadFromRemoteManager()) {
                VueKitLogger.info(LOG, "✅ 从远程组件库管理器加载成功，组件数量: " + componentsList.size());
                return;
            }

            // 最后才从内置资源文件加载（仅作为备用）
            VueKitLogger.warn(LOG, "⚠️ 本地缓存和远程管理器都没有找到组件库，使用内置资源作为备用");
            loadFromBuiltinResources();
            
        } catch (Exception e) {
            String errorMsg = "加载组件数据失败";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
    }

    /**
     * 根据组件库类型获取对应的组件库ID
     * 
     * @param libraryType 组件库类型，不能为null
     * @return 组件库ID字符串，如果类型未知则返回null
     * @throws IllegalArgumentException 当libraryType为null时抛出
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
     * 从内置资源文件加载组件数据
     * 
     * 从插件的内置资源中加载组件数据，这是组件数据的默认来源。
     * 如果加载失败，会记录错误日志但不抛出异常，确保插件的基本功能可用。
     */
    private void loadFromBuiltinResources() {
        try {
            VueKitLogger.debug(LOG, "开始从内置资源加载组件数据");
            
            // 获取内置资源文件路径
            String resourcePath = "/data/" + getBuiltinResourceFileName();
            VueKitLogger.debug(LOG, "内置资源路径: " + resourcePath);
            
            // 加载内置资源
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                VueKitLogger.warn(LOG, "内置资源文件不存在: " + resourcePath);
                return;
            }
            
            // 读取并解析JSON数据
            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            inputStream.close();
            
            Type listType = new TypeToken<List<ComponentInfo>>(){}.getType();
            List<ComponentInfo> componentInfos = new Gson().fromJson(jsonContent, listType);
            
            if (componentInfos != null && !componentInfos.isEmpty()) {
                // 转换为ElementPlusComponent并添加到组件列表
                for (ComponentInfo componentInfo : componentInfos) {
                    try {
                        if (componentInfo != null) {
                            ElementPlusComponent component = convertToElementPlusComponent(componentInfo);
                            if (component != null) {
                                componentsMap.put(component.getName(), component);
                                componentsList.add(component);
                            }
                        }
                    } catch (Exception e) {
                        VueKitLogger.warn(LOG, "转换组件失败: " + 
                            (componentInfo != null ? componentInfo.getName() : "null"), e);
                        VueKitLogger.logAndIgnore(LOG, "跳过损坏的组件数据", e);
                    }
                }
                
                VueKitLogger.info(LOG, "从内置资源成功加载 " + componentsList.size() + " 个组件");
            } else {
                VueKitLogger.warn(LOG, "内置资源文件为空或格式错误");
            }
            
        } catch (IOException e) {
            String errorMsg = "读取内置资源文件失败";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        } catch (Exception e) {
            String errorMsg = "从内置资源加载组件数据时发生未知错误";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
        }
    }

    /**
     * 获取内置资源文件名
     * 
     * 根据检测到的组件库类型返回对应的内置资源文件名。
     * 
     * @return 内置资源文件名
     */
    private String getBuiltinResourceFileName() {
        switch (libraryType) {
            case ELEMENT_UI:
                return "element-ui-components.json";
            case ELEMENT_PLUS:
                return "element-plus-components.json";
            case ANT_DESIGN_VUE:
                return "ant-design-vue-components.json";
            default:
                VueKitLogger.warn(LOG, "未知组件库类型，使用默认资源文件");
                return "element-plus-components.json";
        }
    }

    /**
     * 从本地缓存加载组件库
     * 
     * 扫描磁盘缓存目录，加载所有可用的组件库到内存缓存。
     * 
     * @return 如果成功加载返回true，否则返回false
     */
    private boolean loadFromLocalCache() {
        try {
            VueKitLogger.debug(LOG, "尝试从本地缓存加载组件库");
            
            // 获取组件库ID
            String libraryId = getLibraryIdByType(libraryType);
            if (libraryId == null) {
                VueKitLogger.warn(LOG, "无法确定组件库ID，跳过本地缓存加载");
                return false;
            }
            
            VueKitLogger.debug(LOG, "查找组件库ID: " + libraryId);
            
            // 尝试从本地缓存加载
            ComponentLibraryManager manager = new ComponentLibraryManager();
            List<ComponentLibrary> libraries = manager.getAllLibraries();
            
            VueKitLogger.debug(LOG, "本地缓存管理器返回的组件库数量: " + libraries.size());
            
            for (ComponentLibrary library : libraries) {
                VueKitLogger.debug(LOG, "检查本地缓存组件库: " + library.getId() + " vs " + libraryId);
                
                if (libraryId.equals(library.getId())) {
                    VueKitLogger.info(LOG, "✅ 找到本地缓存的组件库: " + library.getName());
                    
                    // 转换并添加组件
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
                                VueKitLogger.warn(LOG, "转换缓存组件失败: " + 
                                    (componentInfo != null ? componentInfo.getName() : "null"), e);
                                VueKitLogger.logAndIgnore(LOG, "跳过损坏的缓存组件", e);
                            }
                        }
                        
                        int addedCount = componentsList.size() - beforeCount;
                        VueKitLogger.info(LOG, "从本地缓存成功加载 " + addedCount + " 个组件");
                        return true;
                    } else {
                        VueKitLogger.warn(LOG, "本地缓存的组件库没有组件数据: " + library.getName());
                    }
                }
            }
            
            VueKitLogger.debug(LOG, "本地缓存中未找到匹配的组件库: " + libraryId);
            VueKitLogger.debug(LOG, "可用的组件库: " + libraries.stream().map(lib -> lib.getId() + "(" + lib.getName() + ")").collect(java.util.stream.Collectors.joining(", ")));
            return false;
            
        } catch (Exception e) {
            String errorMsg = "从本地缓存加载组件库失败";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
            return false;
        }
    }

    /**
     * 从远程组件库管理器加载组件库
     * 
     * @return 如果成功加载返回true，否则返回false
     */
    private boolean loadFromRemoteManager() {
        try {
            VueKitLogger.debug(LOG, "尝试从远程组件库管理器加载组件库");
            
            // 获取组件库ID
            String libraryId = getLibraryIdByType(libraryType);
            if (libraryId == null) {
                VueKitLogger.warn(LOG, "无法确定组件库ID，跳过远程管理器加载");
                return false;
            }
            
            // 尝试从远程管理器加载
            ComponentLibraryManager manager = new ComponentLibraryManager();
            List<ComponentLibrary> libraries = manager.getAllLibraries();
            
            VueKitLogger.debug(LOG, "远程管理器返回的组件库数量: " + libraries.size());
            
            for (ComponentLibrary library : libraries) {
                VueKitLogger.debug(LOG, "检查组件库: " + library.getId() + " vs " + libraryId);
                
                if (libraryId.equals(library.getId())) {
                    VueKitLogger.info(LOG, "✅ 在远程管理器中找到匹配的组件库: " + library.getName());
                    
                    // 转换并添加组件
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
                                VueKitLogger.warn(LOG, "转换远程组件失败: " + 
                                    (componentInfo != null ? componentInfo.getName() : "null"), e);
                                VueKitLogger.logAndIgnore(LOG, "跳过损坏的远程组件", e);
                            }
                        }
                        
                        int addedCount = componentsList.size() - beforeCount;
                        VueKitLogger.info(LOG, "从远程管理器成功加载 " + addedCount + " 个组件");
                        return true;
                    }
                }
            }
            
            VueKitLogger.debug(LOG, "远程管理器中未找到匹配的组件库: " + libraryId);
            return false;
            
        } catch (Exception e) {
            String errorMsg = "从远程管理器加载组件库失败";
            VueKitLogger.error(LOG, errorMsg, e);
            ErrorHandler.handleException(errorMsg, e, false);
            return false;
        }
    }

    /**
     * 将 ComponentInfo 转换为 ElementPlusComponent
     * 
     * 注意：虽然方法名包含 ElementPlus，但实际支持所有组件库类型
     * 转换后的组件信息会根据检测到的组件库类型进行相应调整
     * 
     * @param componentInfo 要转换的组件信息，不能为 null
     * @return 转换后的ElementPlusComponent对象
     * @throws IllegalArgumentException 当componentInfo为null时抛出
     */
    private ElementPlusComponent convertToElementPlusComponent(ComponentInfo componentInfo) {
        Objects.requireNonNull(componentInfo, "ComponentInfo不能为null");
        
        try {
            VueKitLogger.debug(LOG, "开始转换组件: " + componentInfo.getName());
            
            ElementPlusComponent component = new ElementPlusComponent();
            component.setName(componentInfo.getName());
            
            // 根据检测到的组件库类型调整组件描述
            String adjustedDescription = adjustComponentDescription(componentInfo.getDescription());
            component.setDescription(adjustedDescription);
            
            // 设置版本信息，根据组件库类型
            String version = getComponentVersionInfo();
            component.setVersion(version);
            
            // 设置文档URL模板
            String docUrl = generateDocumentationUrl(componentInfo.getName());
            component.setDocUrl(docUrl);
            
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
                        // 检查是否有isRequired方法，如果没有则默认为false
                        try {
                            elementProp.setRequired(prop.isRequired());
                        } catch (NoSuchMethodError e) {
                            elementProp.setRequired(false);
                        }
                        props.add(elementProp);
                    }
                }
                component.setProps(props);
                VueKitLogger.debug(LOG, "转换属性完成，数量: " + props.size());
            }
            
            // 转换事件
            if (componentInfo.getEvents() != null) {
                List<ElementPlusEvent> events = new ArrayList<>();
                for (ComponentInfo.ComponentEvent event : componentInfo.getEvents()) {
                    if (event != null) {
                        ElementPlusEvent elementEvent = new ElementPlusEvent();
                        elementEvent.setName(event.getName());
                        elementEvent.setDescription(event.getDescription());
                        // 检查是否有getParameters方法，如果没有则使用空字符串
                        try {
                            elementEvent.setParameters(event.getParameters());
                        } catch (NoSuchMethodError e) {
                            elementEvent.setParameters("");
                        }
                        events.add(elementEvent);
                    }
                }
                component.setEvents(events);
                VueKitLogger.debug(LOG, "转换事件完成，数量: " + events.size());
            }
            
            // 转换插槽
            if (componentInfo.getSlots() != null) {
                List<ElementPlusSlot> slots = new ArrayList<>();
                for (ComponentInfo.ComponentSlot slot : componentInfo.getSlots()) {
                    if (slot != null) {
                        ElementPlusSlot elementSlot = new ElementPlusSlot();
                        elementSlot.setName(slot.getName());
                        elementSlot.setDescription(slot.getDescription());
                        // 检查是否有getScope方法，如果没有则使用空字符串
                        try {
                            elementSlot.setScope(slot.getScope());
                        } catch (NoSuchMethodError e) {
                            elementSlot.setScope("");
                        }
                        slots.add(elementSlot);
                    }
                }
                component.setSlots(slots);
                VueKitLogger.debug(LOG, "转换插槽完成，数量: " + slots.size());
            }
            
            VueKitLogger.debug(LOG, "组件转换完成: " + componentInfo.getName() + " (库: " + libraryType.getDisplayName() + ")");
            return component;
            
        } catch (Exception e) {
            String errorMsg = "转换组件失败: " + componentInfo.getName();
            VueKitLogger.error(LOG, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * 根据检测到的组件库类型调整组件描述
     * 
     * @param originalDescription 原始描述
     * @return 调整后的描述
     */
    private String adjustComponentDescription(String originalDescription) {
        if (originalDescription == null || originalDescription.trim().isEmpty()) {
            return "";
        }
        
        // 根据组件库类型添加相应的标识
        switch (libraryType) {
            case ELEMENT_UI:
                return "[Element UI] " + originalDescription;
            case ELEMENT_PLUS:
                return "[Element Plus] " + originalDescription;
            case ANT_DESIGN_VUE:
                return "[Ant Design Vue] " + originalDescription;
            default:
                return originalDescription;
        }
    }
    
    /**
     * 获取组件版本信息
     * 
     * @return 版本信息字符串
     */
    private String getComponentVersionInfo() {
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
