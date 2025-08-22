package com.chu7.vuecomponentassistant.completion2;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.ErrorHandler;
import com.chu7.vuecomponentassistant.utils.SmartComponentFilter;
import com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager;
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
 * 通用组件数据提供者
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>从远程组件库管理器加载组件数据</li>
 *   <li>支持 Element UI、Element Plus、Ant Design Vue</li>
 *   <li>提供组件查询功能</li>
 *   <li>支持组件属性、事件、插槽等信息</li>
 *   <li>智能组件过滤和缓存管理</li>
 *   <li>多数据源加载策略</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>多级数据加载：本地缓存 -> 远程管理器 -> 自定义库</li>
 *   <li>智能过滤：根据项目上下文过滤相关组件</li>
 *   <li>自动注册：初始化时自动注册到管理器</li>
 *   <li>错误处理：完善的异常处理和日志记录</li>
 *   <li>性能优化：支持缓存和增量更新</li>
 * </ul>
 * 
 * <p>数据加载策略：</p>
 * <ol>
 *   <li>优先从本地缓存加载（最快）</li>
 *   <li>从远程组件库管理器加载（中等速度）</li>
 *   <li>从自定义组件库加载（后备方案）</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全功能的数据源</li>
 *   <li>组件文档显示</li>
 *   <li>组件信息查询</li>
 *   <li>智能组件推荐</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 3.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.utils.SmartComponentFilter
 */
public class ComponentProvider {

    /**
     * 日志记录器
     * 用于记录组件数据加载和管理过程中的关键信息
     */
    private static final Logger LOG = VueKitLogger.getLogger(ComponentProvider.class);

    /**
     * 组件名称到组件对象的映射表
     * 用于快速查找和访问组件信息
     */
    private final Map<String, ElementPlusComponent> componentsMap;

    /**
     * 组件对象列表
     * 保持组件的原始顺序，用于遍历和过滤
     */
    private final List<ElementPlusComponent> componentsList;

    /**
     * 组件库类型
     * 通过项目依赖检测获得，如 "element-plus"、"ant-design-vue" 等
     */
    private final String libraryType;

    /**
     * 关联的项目对象
     * 用于项目级别的配置和过滤
     */
    private final Project project;

    /**
     * 构造函数
     * 
     * <p>初始化组件提供者，执行以下操作：</p>
     * <ol>
     *   <li>验证项目对象的有效性</li>
     *   <li>初始化内部数据结构</li>
     *   <li>检测项目使用的组件库类型</li>
     *   <li>加载组件数据</li>
     *   <li>注册到ComponentProviderManager</li>
     * </ol>
     * 
     * <p>初始化流程：</p>
     * <ul>
     *   <li>创建组件映射表和列表</li>
     *   <li>调用ComponentLibraryDetector检测组件库</li>
     *   <li>调用loadComponents()加载数据</li>
     *   <li>自动注册到管理器</li>
     * </ul>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>项目对象不能为null</li>
     *   <li>初始化失败会抛出RuntimeException</li>
     *   <li>自动注册避免重复创建</li>
     * </ul>
     * 
     * @param project 项目对象，不能为null
     * @throws IllegalArgumentException 如果project为null
     * @throws RuntimeException 如果初始化失败
     * 
     * @see com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector#detectComponentLibrary(Project)
     * @see #loadComponents()
     * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager#registerProvider(Project, ComponentProvider)
     */
    public ComponentProvider(Project project) {
        Objects.requireNonNull(project, "Project对象不能为null");

        VueKitLogger.debug(LOG, "=== ComponentProvider 初始化开始 ===");
        this.componentsMap = new HashMap<>();
        this.componentsList = new ArrayList<>();
        this.project = project;

        try {
            // 检测项目使用的组件库
            String detectedType = ComponentLibraryDetector.detectComponentLibrary(project);
            this.libraryType = detectedType;
            VueKitLogger.debug(LOG, "检测到的组件库类型: " + LibraryTypeHelper.getDisplayName(detectedType));

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
     * 
     * <p>该方法会按照优先级顺序尝试从不同数据源加载组件数据：</p>
     * <ol>
     *   <li>本地缓存（最高优先级，最快速度）</li>
     *   <li>远程组件库管理器（中等优先级，中等速度）</li>
     *   <li>自定义组件库（最低优先级，后备方案）</li>
     * </ol>
     * 
     * <p>加载策略：</p>
     * <ul>
     *   <li>优先使用缓存数据，提高性能</li>
     *   <li>远程数据作为主要数据源</li>
     *   <li>自定义库支持扩展功能</li>
     *   <li>加载成功后应用智能过滤</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录日志</li>
     *   <li>使用ErrorHandler统一处理错误</li>
     *   <li>不会中断初始化流程</li>
     * </ul>
     * 
     * @see #loadFromLocalCache()
     * @see #loadFromRemoteManager()
     * @see #applySmartFiltering()
     * @see com.chu7.vuecomponentassistant.utils.ErrorHandler#handleException(String, Exception, boolean)
     */
    private void loadComponents() {
        try {
            VueKitLogger.info(LOG, "=== 开始加载组件数据 ===");
            VueKitLogger.info(LOG, "检测到的组件库类型: " + LibraryTypeHelper.getDisplayName(libraryType));

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
     * 
     * <p>该方法使用SmartComponentFilter对加载的组件进行智能过滤，
     * 根据项目上下文和用户偏好过滤出最相关的组件。</p>
     * 
     * <p>过滤流程：</p>
     * <ol>
     *   <li>记录过滤前的组件数量</li>
     *   <li>创建智能过滤器实例</li>
     *   <li>应用项目相关的过滤规则</li>
     *   <li>更新组件列表和映射表</li>
     *   <li>记录过滤结果统计</li>
     * </ol>
     * 
     * <p>过滤效果：</p>
     * <ul>
     *   <li>减少无关组件的干扰</li>
     *   <li>提高补全建议的准确性</li>
     *   <li>优化内存使用</li>
     *   <li>提升用户体验</li>
     * </ul>
     * 
     * @see com.chu7.vuecomponentassistant.utils.SmartComponentFilter#filterComponentsByProject(List, Project)
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
     * 
     * <p>该方法会尝试多种方式获取组件库ID：</p>
     * <ol>
     *   <li>直接匹配已安装的组件库</li>
     *   <li>使用动态配置管理器查找</li>
     *   <li>返回找到的组件库ID或null</li>
     * </ol>
     * 
     * <p>匹配策略：</p>
     * <ul>
     *   <li>优先检查已安装的组件库</li>
     *   <li>支持名称和ID的精确匹配</li>
     *   <li>使用配置管理器作为后备方案</li>
     *   <li>记录匹配过程的日志信息</li>
     * </ul>
     * 
     * @param libraryType 组件库类型，不能为null
     * @return 对应的组件库ID，如果未找到则返回null
     * @throws IllegalArgumentException 如果libraryType为null
     * 
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager#getAllLibraries()
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager#getLibraryIdByPackageName(String)
     */
    private String getLibraryIdByType(String libraryType) {
        Objects.requireNonNull(libraryType, "组件库类型不能为null");

        // 首先尝试直接匹配（对于自定义组件库）
        ComponentLibraryManager manager = new ComponentLibraryManager();
        List<ComponentLibrary> libraries = manager.getAllLibraries();
        
        for (ComponentLibrary library : libraries) {
            if (libraryType.equals(library.getName()) || libraryType.equals(library.getId())) {
                VueKitLogger.info(LOG, "直接匹配到组件库: " + library.getName() + " (ID: " + library.getId() + ")");
                return library.getId();
            }
        }
        
        // 使用动态配置管理器获取组件库ID
        DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
        String libraryId = configManager.getLibraryIdByPackageName(libraryType);
        
        if (libraryId == null) {
            VueKitLogger.warn(LOG, "未知的组件库类型: " + libraryType);
        }
        
        return libraryId;
    }

    /**
     * 从本地缓存加载组件库
     * 
     * <p>该方法尝试从本地缓存的组件库中加载组件数据，
     * 这是最快的数据加载方式。</p>
     * 
     * <p>加载流程：</p>
     * <ol>
     *   <li>获取组件库类型对应的ID</li>
     *   <li>查找本地缓存的组件库</li>
     *   <li>转换组件信息为ElementPlusComponent</li>
     *   <li>更新内部数据结构</li>
     * </ol>
     * 
     * <p>成功条件：</p>
     * <ul>
     *   <li>找到对应的本地组件库</li>
     *   <li>组件库包含有效的组件数据</li>
     *   <li>数据转换成功</li>
     * </ul>
     * 
     * @return 如果成功加载则返回true，否则返回false
     * 
     * @see #getLibraryIdByType(String)
     * @see #convertToElementPlusComponent(ComponentInfo)
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
     * 
     * <p>该方法尝试从远程组件库管理器中加载组件数据，
     * 作为本地缓存的后备数据源。</p>
     * 
     * <p>加载流程：</p>
     * <ol>
     *   <li>获取组件库类型对应的ID</li>
     *   <li>从远程管理器查找匹配的组件库</li>
     *   <li>转换组件信息为ElementPlusComponent</li>
     *   <li>更新内部数据结构</li>
     * </ol>
     * 
     * <p>成功条件：</p>
     * <ul>
     *   <li>远程管理器包含对应的组件库</li>
     *   <li>组件库包含有效的组件数据</li>
     *   <li>数据转换成功</li>
     * </ul>
     * 
     * @return 如果成功加载则返回true，否则返回false
     * 
     * @see #getLibraryIdByType(String)
     * @see #convertToElementPlusComponent(ComponentInfo)
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
     * 
     * <p>该方法负责将远程组件库的 ComponentInfo 对象转换为
     * 本地使用的 ElementPlusComponent 对象。</p>
     * 
     * <p>转换内容：</p>
     * <ul>
     *   <li>基本组件信息（名称、描述、版本、文档URL）</li>
     *   <li>组件属性列表（props）</li>
     *   <li>组件事件列表（events）</li>
     *   <li>组件插槽列表（slots）</li>
     * </ul>
     * 
     * <p>转换策略：</p>
     * <ul>
     *   <li>逐个转换每个属性、事件和插槽</li>
     *   <li>处理可能的兼容性问题（如NoSuchMethodError）</li>
     *   <li>保持数据结构的完整性</li>
     * </ul>
     * 
     * @param componentInfo 要转换的ComponentInfo对象，不能为null
     * @return 转换后的ElementPlusComponent对象
     * @throws IllegalArgumentException 如果componentInfo为null
     * @throws RuntimeException 如果转换过程中发生错误
     * 
     * @see com.chu7.vuecomponentassistant.remote.model.ComponentInfo
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
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
     * 
     * <p>该方法尝试获取组件的实际版本信息，优先从项目的 package.json 中读取，
     * 如果无法获取则返回通用版本信息。</p>
     * 
     * <p>版本获取策略：</p>
     * <ol>
     *   <li>尝试从项目的 package.json 中读取实际版本</li>
     *   <li>如果无法获取实际版本，返回通用版本信息</li>
     *   <li>包含组件库的显示名称</li>
     * </ol>
     * 
     * @return 组件的版本信息字符串，格式为"组件库名称 版本号"
     * 
     * @see #getActualLibraryVersion()
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#getDisplayName(String)
     */
    private String getComponentVersionInfo() {
        // 尝试从项目的 package.json 中读取实际版本
        String actualVersion = getActualLibraryVersion();
        if (actualVersion != null && !actualVersion.isEmpty()) {
            return actualVersion;
        }

        // 如果无法获取实际版本，返回通用版本信息
        return LibraryTypeHelper.getDisplayName(libraryType) + " (版本信息不可用)";
    }

    /**
     * 从项目的 package.json 中获取实际的组件库版本
     * 
     * <p>该方法尝试从项目的 package.json 文件中读取指定组件库的实际版本号，
     * 提供比静态配置更准确的版本信息。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>获取组件库对应的包名</li>
     *   <li>在 package.json 中查找该包的版本信息</li>
     *   <li>返回格式化的版本字符串</li>
     * </ol>
     * 
     * <p>版本格式：</p>
     * <ul>
     *   <li>成功：返回"组件库名称 版本号"格式</li>
     *   <li>失败：返回null</li>
     * </ul>
     * 
     * @return 格式化的版本信息字符串，如果无法获取则返回null
     * 
     * @see #getPackageNameByLibraryType()
     * @see #findPackageVersion(String)
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#getDisplayName(String)
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
                return LibraryTypeHelper.getDisplayName(libraryType) + " " + version;
            }

        } catch (Exception e) {
            VueKitLogger.warn(LOG, "获取实际版本失败: " + e.getMessage());
        }

        return null;
    }

    /**
     * 根据组件库类型获取对应的包名
     * 
     * <p>该方法通过动态配置管理器获取组件库类型对应的 npm 包名，
     * 用于在 package.json 中查找版本信息。</p>
     * 
     * <p>获取策略：</p>
     * <ol>
     *   <li>使用动态配置管理器查找配置信息</li>
     *   <li>如果找到配置，返回配置中的包名</li>
     *   <li>如果找不到配置，直接返回 libraryType（可能是包名）</li>
     * </ol>
     * 
     * @return 对应的 npm 包名，如果无法确定则返回 libraryType
     * 
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager#getInstance()
     * @see com.chu7.vuecomponentassistant.utils.DynamicLibraryConfigManager.LibraryConfig#getPackageName()
     */
    private String getPackageNameByLibraryType() {
        // 使用动态配置管理器获取包名
        DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
        DynamicLibraryConfigManager.LibraryConfig config = configManager.getLibraryConfig(libraryType);
        
        if (config != null) {
            return config.getPackageName();
        }
        
        // 如果找不到配置，直接返回 libraryType（可能是包名）
        return libraryType;
    }

    /**
     * 从项目的 package.json 中查找指定包的版本
     * 
     * <p>该方法在项目的 package.json 文件中查找指定 npm 包的版本信息，
     * 支持 dependencies 和 devDependencies 中的版本查找。</p>
     * 
     * <p>查找流程：</p>
     * <ol>
     *   <li>获取项目根目录</li>
     *   <li>查找 package.json 文件</li>
     *   <li>读取文件内容</li>
     *   <li>使用正则表达式提取版本信息</li>
     * </ol>
     * 
     * <p>支持格式：</p>
     * <ul>
     *   <li>双引号格式："package-name": "version"</li>
     *   <li>单引号格式：'package-name': 'version'</li>
     * </ul>
     * 
     * @param packageName 要查找的 npm 包名
     * @return 包的版本号，如果找不到则返回null
     * 
     * @see com.chu7.vuecomponentassistant.utils.ProjectPathHelper#getProjectRoot(Project)
     * @see #extractVersionFromJson(String, String)
     */
    private String findPackageVersion(String packageName) {
        try {
            // 查找 package.json 文件
            com.intellij.openapi.vfs.VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
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
     * 
     * <p>该方法使用正则表达式从 package.json 的 JSON 内容中提取指定包的版本信息，
     * 支持多种 JSON 格式的版本提取。</p>
     * 
     * <p>提取策略：</p>
     * <ol>
     *   <li>定义多种 JSON 格式的正则表达式</li>
     *   <li>逐个尝试匹配每种格式</li>
     *   <li>返回第一个成功匹配的版本号</li>
     * </ol>
     * 
     * <p>支持的正则表达式：</p>
     * <ul>
     *   <li>双引号格式：`"package-name"\s*:\s*"([^"]+)"`</li>
     *   <li>单引号格式：`'package-name'\s*:\s*'([^']+)'`</li>
     * </ul>
     * 
     * @param jsonContent package.json 的 JSON 内容字符串
     * @param packageName 要提取版本的包名
     * @return 包的版本号，如果找不到则返回null
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
     * 
     * <p>该方法返回项目中所有可用的组件，包括内置组件库、自定义组件库和官方组件库，
     * 根据项目的组件库启用配置进行过滤。</p>
     * 
     * <p>组件来源：</p>
     * <ol>
     *   <li>内置组件库组件（根据配置过滤）</li>
     *   <li>自定义组件库组件（根据配置过滤）</li>
     *   <li>官方组件库组件（根据配置过滤）</li>
     * </ol>
     * 
     * <p>过滤逻辑：</p>
     * <ul>
     *   <li>只返回启用的组件库中的组件</li>
     *   <li>跳过未启用的组件库</li>
     *   <li>记录加载过程的详细信息</li>
     * </ul>
     * 
     * @return 所有可用组件的列表，如果没有启用任何组件库则返回空列表
     * 
     * @see #getEnabledLibrariesForProject()
     * @see #addCustomComponents(List, Set)
     * @see #addDownloadedOfficialComponents(List, Set)
     */
    public List<ElementPlusComponent> getAllComponents() {
        VueKitLogger.debug(LOG, "开始获取所有可用组件...");

        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 获取项目启用的组件库配置
        Set<String> enabledLibraries = getEnabledLibrariesForProject();

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
     * 
     * <p>该方法从项目的组件库配置管理器中获取当前启用的组件库列表，
     * 用于过滤和加载相应的组件数据。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>获取项目级别的组件库配置管理器</li>
     *   <li>查询启用的组件库名称列表</li>
     *   <li>记录启用的组件库信息</li>
     *   <li>返回启用列表</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>返回空集合而不是抛出异常</li>
     *   <li>确保方法调用的稳定性</li>
     * </ul>
     * 
     * @return 启用的组件库名称集合，如果获取失败则返回空集合
     * 
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#getInstance(Project)
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#getEnabledLibraryNames(Project)
     */
    private Set<String> getEnabledLibrariesForProject() {
        try {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            Set<String> enabledLibraries = configManager.getEnabledLibraryNames(project);
            
            VueKitLogger.debug(LOG, "获取到项目启用的组件库: " + 
                (enabledLibraries.isEmpty() ? "无" : enabledLibraries.stream()
                    .map(LibraryTypeHelper::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", "))));
            
            return enabledLibraries;
        } catch (com.intellij.openapi.progress.ProcessCanceledException e) {
            // ProcessCanceledException 是正常的控制流异常，不应该记录为错误
            VueKitLogger.debug(LOG, "获取项目启用的组件库配置被取消");
            return new HashSet<>();
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取项目启用的组件库配置失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 根据前缀获取匹配的组件列表
     * 
     * <p>该方法根据指定的前缀字符串，从所有启用的组件库中查找匹配的组件，
     * 使用精确的前缀匹配（startsWith）策略。</p>
     * 
     * <p>搜索范围：</p>
     * <ol>
     *   <li>内置组件库（根据配置过滤）</li>
     *   <li>自定义组件库（根据配置过滤）</li>
     *   <li>官方组件库（根据配置过滤）</li>
     * </ol>
     * 
     * <p>匹配策略：</p>
     * <ul>
     *   <li>使用 toLowerCase() 进行不区分大小写的匹配</li>
     *   <li>使用 startsWith() 进行精确前缀匹配</li>
     *   <li>只返回启用的组件库中的组件</li>
     * </ul>
     * 
     * @param prefix 要搜索的前缀字符串，如果为null或空字符串则返回所有组件
     * @return 匹配前缀的组件列表
     * 
     * @see #getAllComponents()
     * @see #addCustomComponents(List, Set)
     * @see #addDownloadedOfficialComponents(List, Set)
     */
    public List<ElementPlusComponent> getComponentsByPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAllComponents();
        }

        VueKitLogger.debug(LOG, "开始搜索前缀为 '" + prefix + "' 的组件");
        List<ElementPlusComponent> matchingComponents = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();

        // 获取项目启用的组件库配置
        Set<String> enabledLibraries = getEnabledLibrariesForProject();

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
     * <p>该方法使用模糊匹配策略搜索组件，与 getComponentsByPrefix 的精确前缀匹配不同，
     * 该方法使用 contains 关系进行搜索，提供更灵活的组件查找功能。</p>
     * 
     * <p>搜索逻辑：</p>
     * <ol>
     *   <li>收集所有可用的组件（内置、自定义、官方）</li>
     *   <li>如果前缀为空，返回所有组件</li>
     *   <li>使用模糊匹配（包含关系）而不是精确前缀匹配</li>
     *   <li>不区分大小写</li>
     * </ol>
     * 
     * <p>与 getComponentsByPrefix 的区别：</p>
     * <ul>
     *   <li>getComponentsByPrefix: 精确前缀匹配（startsWith）</li>
     *   <li>searchComponents: 模糊匹配（contains）</li>
     * </ul>
     * 
     * <p>搜索范围：</p>
     * <ul>
     *   <li>内置组件库（根据配置过滤）</li>
     *   <li>自定义组件库（根据配置过滤）</li>
     *   <li>官方组件库（根据配置过滤）</li>
     * </ul>
     * 
     * @param prefix 要搜索的前缀，如果为 null 或空字符串则返回所有组件
     * @return 匹配的组件列表，如果没有匹配的组件则返回空列表
     * 
     * @see #getComponentsByPrefix(String)
     * @see #getAllComponents()
     * @see #addCustomComponents(List, Set)
     * @see #addDownloadedOfficialComponents(List, Set)
     */
    public List<ElementPlusComponent> searchComponents(String prefix) {
        VueKitLogger.debug(LOG, "开始模糊搜索组件，前缀: '" + prefix + "'");

        List<ElementPlusComponent> allComponents = new ArrayList<>();

        // 获取项目启用的组件库配置
        Set<String> enabledLibraries = getEnabledLibrariesForProject();

        // 1. 添加内置组件库的组件（只添加启用的组件库）
        int builtinCount = 0;
        if (enabledLibraries.contains(libraryType)) {
            allComponents.addAll(componentsList);
            builtinCount = componentsList.size();
            VueKitLogger.debug(LOG, "添加内置组件: " + builtinCount + " 个");
        } else {
            VueKitLogger.debug(LOG, "跳过内置组件库 (类型: " + LibraryTypeHelper.getDisplayName(libraryType) + " 未启用)");
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
        
        // 调试：显示所有启用的组件库
        VueKitLogger.debug(LOG, "启用的组件库: " + String.join(", ", enabledLibraries));
        VueKitLogger.debug(LOG, "总组件数量: " + allComponents.size());

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
     * 
     * <p>该方法根据组件名称从所有启用的组件库中查找指定的组件，
     * 支持内置组件库、自定义组件库和官方组件库的查找。</p>
     * 
     * <p>查找策略：</p>
     * <ol>
     *   <li>先从内置组件库查找（最快）</li>
     *   <li>从自定义组件库查找</li>
     *   <li>从官方组件库查找（包括下载的组件库）</li>
     * </ol>
     * 
     * <p>查找范围：</p>
     * <ul>
     *   <li>只查找启用的组件库中的组件</li>
     *   <li>跳过未启用的组件库</li>
     *   <li>记录查找过程的详细信息</li>
     * </ul>
     * 
     * @param componentName 要查找的组件名称，不能为null或空字符串
     * @return 找到的组件对象，如果未找到则返回null
     * @throws IllegalArgumentException 如果componentName为null或空字符串
     * 
     * @see #getEnabledLibrariesForProject()
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#getCustomComponent(String)
     * @see #convertToElementPlusComponent(ComponentInfo)
     */
    public ElementPlusComponent getComponent(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        VueKitLogger.debug(LOG, "开始查找组件: " + componentName);

        // 获取项目启用的组件库配置
        Set<String> enabledLibraries = getEnabledLibrariesForProject();

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

        // 从下载的组件库查找（包括官方和自定义）
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                String libraryType = getLibraryTypeByName(library.getName());
                if (libraryType == null || !enabledLibraries.contains(libraryType)) {
                    continue; // 跳过未启用的组件库
                }

                // 查找所有启用的组件库（包括官方和自定义）
                if (library.getComponents() != null) {
                    for (ComponentInfo info : library.getComponents()) {
                        if (componentName.equals(info.getName())) {
                            VueKitLogger.debug(LOG, "找到组件: " + componentName + " 在组件库: " + library.getName() + " (类型: " + library.getSource() + ")");
                            return convertToElementPlusComponent(info);
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "从组件库查找组件失败: " + e.getMessage(), e);
        }

        VueKitLogger.debug(LOG, "未找到组件: " + componentName);
        return null;
    }

    /**
     * 添加自定义组件到组件列表中
     * 
     * <p>该方法将自定义组件库中的组件添加到指定的组件列表中，
     * 只有在启用了相应组件库时才会添加。</p>
     * 
     * <p>添加流程：</p>
     * <ol>
     *   <li>验证目标组件列表的有效性</li>
     *   <li>检查是否有启用的组件库</li>
     *   <li>获取所有自定义组件库配置</li>
     *   <li>转换并添加组件到目标列表</li>
     * </ol>
     * 
     * <p>过滤逻辑：</p>
     * <ul>
     *   <li>如果没有启用任何组件库，跳过添加</li>
     *   <li>只添加启用的自定义组件库中的组件</li>
     *   <li>处理组件转换过程中的异常</li>
     * </ul>
     * 
     * @param allComponents 目标组件列表，不能为null
     * @param enabledLibraries 启用的组件库名称集合
     * @throws IllegalArgumentException 如果allComponents为null
     * 
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#getAllCustomLibraries()
     * @see #convertToElementPlusComponent(ComponentInfo)
     */
    private void addCustomComponents(List<ElementPlusComponent> allComponents, Set<String> enabledLibraries) {
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
     * 
     * <p>该方法将官方组件库市场下载的组件库中的组件添加到指定的组件列表中，
     * 只有在启用了相应组件库时才会添加。</p>
     * 
     * <p>添加流程：</p>
     * <ol>
     *   <li>验证目标组件列表的有效性</li>
     *   <li>检查是否有启用的组件库</li>
     *   <li>获取所有组件库（包括官方和自定义）</li>
     *   <li>过滤启用的组件库</li>
     *   <li>转换并添加组件到目标列表</li>
     * </ol>
     * 
     * <p>过滤逻辑：</p>
     * <ul>
     *   <li>如果没有启用任何组件库，跳过添加</li>
     *   <li>只添加启用的组件库中的组件</li>
     *   <li>记录添加过程的详细信息</li>
     *   <li>处理组件转换过程中的异常</li>
     * </ul>
     * 
     * @param allComponents 目标组件列表，不能为null
     * @param enabledLibraries 启用的组件库名称集合
     * @throws IllegalArgumentException 如果allComponents为null
     * 
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager#getAllLibraries()
     * @see #getLibraryTypeByName(String)
     * @see #convertToElementPlusComponent(ComponentInfo)
     */
    private void addDownloadedOfficialComponents(List<ElementPlusComponent> allComponents, Set<String> enabledLibraries) {
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
                String libraryType = getLibraryTypeByName(library.getName());
                if (libraryType == null || !enabledLibraries.contains(libraryType)) {
                    VueKitLogger.debug(LOG, "跳过未启用的组件库: " + library.getName() + " (类型: " + library.getSource() + ")");
                    continue;
                }

                // 添加所有启用的组件库（包括官方和自定义）
                if (library.getComponents() != null) {
                    VueKitLogger.debug(LOG, "添加启用的组件库: " + library.getName() + " (类型: " + library.getSource() + ", 版本: " + library.getVersion() + ")");
                    for (ComponentInfo componentInfo : library.getComponents()) {
                        try {
                            ElementPlusComponent convertedComponent = convertToElementPlusComponent(componentInfo);
                            allComponents.add(convertedComponent);
                            VueKitLogger.debug(LOG, "  添加组件: " + componentInfo.getName());
                        } catch (Exception e) {
                            VueKitLogger.warn(LOG, "转换组件失败: " + componentInfo.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "加载组件库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 重新加载组件数据
     * 
     * <p>该方法清空现有的组件数据并重新从所有数据源加载组件，
     * 用于刷新组件数据或解决数据不一致问题。</p>
     * 
     * <p>重新加载流程：</p>
     * <ol>
     *   <li>记录重新加载前的组件数量</li>
     *   <li>清空现有的组件映射表和列表</li>
     *   <li>调用 loadComponents() 重新加载数据</li>
     *   <li>记录重新加载后的组件数量</li>
     *   <li>输出重新加载统计信息</li>
     * </ol>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>组件库配置变更后刷新数据</li>
     *   <li>解决数据不一致问题</li>
     *   <li>手动刷新组件数据</li>
     *   <li>调试和测试目的</li>
     * </ul>
     * 
     * @see #loadComponents()
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
     * 
     * <p>该方法根据组件名称获取指定组件的所有属性信息，
     * 包括属性名称、类型、描述、默认值、是否必需等。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>验证组件名称参数的有效性</li>
     *   <li>调用 getComponent() 获取组件对象</li>
     *   <li>提取组件的属性列表</li>
     *   <li>返回属性列表或空列表</li>
     * </ol>
     * 
     * <p>返回结果：</p>
     * <ul>
     *   <li>如果找到组件且有属性，返回属性列表</li>
     *   <li>如果找不到组件或没有属性，返回空列表</li>
     *   <li>不会返回null，确保调用方的安全性</li>
     * </ul>
     * 
     * @param componentName 组件名称，不能为null或空字符串
     * @return 组件的属性列表，如果没有属性则返回空列表
     * @throws IllegalArgumentException 如果componentName为null或空字符串
     * 
     * @see #getComponent(String)
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent#getProps()
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
     * 
     * <p>该方法根据组件名称获取指定组件的所有事件信息，
     * 包括事件名称、描述、参数等。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>验证组件名称参数的有效性</li>
     *   <li>调用 getComponent() 获取组件对象</li>
     *   <li>提取组件的事件列表</li>
     *   <li>返回事件列表或空列表</li>
     * </ol>
     * 
     * <p>返回结果：</p>
     * <ul>
     *   <li>如果找到组件且有事件，返回事件列表</li>
     *   <li>如果找不到组件或没有事件，返回空列表</li>
     *   <li>不会返回null，确保调用方的安全性</li>
     * </ul>
     * 
     * @param componentName 组件名称，不能为null或空字符串
     * @return 组件的事件列表，如果没有事件则返回空列表
     * @throws IllegalArgumentException 如果componentName为null或空字符串
     * 
     * @see #getComponent(String)
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent#getEvents()
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
     * 
     * <p>该方法根据组件名称获取指定组件的所有插槽信息，
     * 包括插槽名称、描述、作用域等。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>验证组件名称参数的有效性</li>
     *   <li>调用 getComponent() 获取组件对象</li>
     *   <li>提取组件的插槽列表</li>
     *   <li>返回插槽列表或空列表</li>
     * </ol>
     * 
     * <p>返回结果：</p>
     * <ul>
     *   <li>如果找到组件且有插槽，返回插槽列表</li>
     *   <li>如果找不到组件或没有插槽，返回空列表</li>
     *   <li>不会返回null，确保调用方的安全性</li>
     * </ul>
     * 
     * @param componentName 组件名称，不能为null或空字符串
     * @return 组件的插槽列表，如果没有插槽则返回空列表
     * @throws IllegalArgumentException 如果componentName为null或空字符串
     * 
     * @see #getComponent(String)
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent#getSlots()
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
     * 
     * <p>该方法检查指定的组件是否存在于当前内置组件库中，
     * 使用组件映射表进行快速查找。</p>
     * 
     * <p>检查逻辑：</p>
     * <ul>
     *   <li>验证组件名称参数的有效性</li>
     *   <li>在 componentsMap 中查找组件名称</li>
     *   <li>返回是否存在的结果</li>
     * </ul>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>只检查内置组件库，不包括自定义和官方组件库</li>
     *   <li>使用 HashMap 进行 O(1) 时间复杂度的查找</li>
     *   <li>不区分大小写（取决于映射表的键值）</li>
     * </ul>
     * 
     * @param componentName 要检查的组件名称，不能为null或空字符串
     * @return 如果组件存在于内置组件库中则返回true，否则返回false
     * @throws IllegalArgumentException 如果componentName为null或空字符串
     */
    public boolean hasComponent(String componentName) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }

        return componentsMap.containsKey(componentName);
    }

    /**
     * 获取内置组件库的组件总数
     * 
     * <p>该方法返回当前内置组件库中已加载的组件总数，
     * 用于统计和监控组件库的使用情况。</p>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>返回 componentsList 的当前大小</li>
     *   <li>如果组件库为空，返回 0</li>
     *   <li>不包括自定义和官方组件库的组件</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>显示组件库统计信息</li>
     *   <li>监控组件加载状态</li>
     *   <li>调试和测试目的</li>
     *   <li>性能分析</li>
     * </ul>
     * 
     * @return 内置组件库的组件总数
     */
    public int getComponentCount() {
        return componentsList.size();
    }

    /**
     * 获取当前检测到的组件库类型
     * 
     * <p>该方法返回在初始化时检测到的组件库类型，
     * 通常是通过分析项目的 package.json 依赖获得的。</p>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>返回检测到的组件库类型字符串</li>
     *   <li>例如："element-plus"、"ant-design-vue"、"element-ui"</li>
     *   <li>如果检测失败，可能返回默认值或"unknown"</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>确定当前项目使用的组件库</li>
     *   <li>配置组件库相关的功能</li>
     *   <li>生成正确的文档链接</li>
     *   <li>调试和配置目的</li>
     * </ul>
     * 
     * @return 当前检测到的组件库类型字符串
     * 
     * @see com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector#detectComponentLibrary(Project)
     */
    public String getLibraryType() {
        return libraryType;
    }

    /**
     * 获取当前组件库的显示名称
     * 
     * <p>该方法返回当前组件库的用户友好的显示名称，
     * 通过 LibraryTypeHelper 将内部类型转换为可读的名称。</p>
     * 
     * <p>转换示例：</p>
     * <ul>
     *   <li>"element-plus" → "Element Plus"</li>
     *   <li>"ant-design-vue" → "Ant Design Vue"</li>
     *   <li>"element-ui" → "Element UI"</li>
     *   <li>"unknown" → "未知组件库"</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>在用户界面中显示组件库名称</li>
     *   <li>生成用户友好的错误消息</li>
     *   <li>日志记录和调试信息</li>
     *   <li>配置界面的显示</li>
     * </ul>
     * 
     * @return 组件库的用户友好显示名称
     * 
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#getDisplayName(String)
     */
    public String getLibraryDisplayName() {
        return LibraryTypeHelper.getDisplayName(libraryType);
    }

    /**
     * 获取组件前缀
     * 
     * <p>该方法返回当前组件库的组件前缀，用于识别和过滤组件名称。
     * 通过 LibraryTypeHelper 获取组件库的标准前缀。</p>
     * 
     * <p>前缀示例：</p>
     * <ul>
     *   <li>Element Plus: "el-"</li>
     *   <li>Ant Design Vue: "a-"</li>
     *   <li>Element UI: "el-"</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>识别组件是否属于特定组件库</li>
     *   <li>生成正确的组件名称</li>
     *   <li>过滤和分类组件</li>
     *   <li>文档链接生成</li>
     * </ul>
     * 
     * @return 组件库的组件前缀字符串
     * 
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#getComponentPrefix(String)
     */
    public String getComponentPrefix() {
        return LibraryTypeHelper.getComponentPrefix(libraryType);
    }

    /**
     * 获取文档 URL 模板
     * 
     * <p>该方法返回当前组件库的文档 URL 模板，用于生成组件的官方文档链接。
     * 通过 LibraryTypeHelper 获取组件库的标准文档模板。</p>
     * 
     * <p>URL 模板示例：</p>
     * <ul>
     *   <li>Element Plus: "https://element-plus.org/en-US/component/{0}.html"</li>
     *   <li>Ant Design Vue: "https://antdv.com/components/{0}"</li>
     *   <li>Element UI: "https://element.eleme.io/#/en-US/component/{0}"</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>生成组件的官方文档链接</li>
     *   <li>在 IDE 中提供文档跳转功能</li>
     *   <li>帮助用户了解组件的使用方法</li>
     *   <li>集成到帮助系统中</li>
     * </ul>
     * 
     * @return 组件库的文档 URL 模板字符串
     * 
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#getDocumentationUrlTemplate(String)
     */
    public String getDocumentationUrlTemplate() {
        return LibraryTypeHelper.getDocumentationUrlTemplate(libraryType);
    }

    /**
     * 检查组件是否属于当前组件库
     * 
     * <p>该方法检查指定的组件是否属于当前检测到的组件库，
     * 通过组件名称前缀和自定义组件库检查来判断。</p>
     * 
     * <p>检查逻辑：</p>
     * <ol>
     *   <li>检查是否是内置组件库的组件（通过前缀匹配）</li>
     *   <li>检查是否是自定义组件库的组件</li>
     *   <li>返回检查结果</li>
     * </ol>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>确定组件的来源</li>
     *   <li>过滤和分类组件</li>
     *   <li>生成正确的文档链接</li>
     *   <li>组件库管理功能</li>
     * </ul>
     * 
     * @param componentName 要检查的组件名称
     * @return 如果组件属于当前组件库则返回true，否则返回false
     * 
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#isComponentFromLibrary(String, String)
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#isCustomComponent(String)
     */
    public boolean isComponentFromCurrentLibrary(String componentName) {
        // 检查是否是内置组件库的组件
        if (LibraryTypeHelper.isComponentFromLibrary(componentName, libraryType)) {
            return true;
        }

        // 检查是否是自定义组件库的组件
        return CustomComponentLibraryManager.isCustomComponent(componentName);
    }

    /**
     * 生成组件的文档 URL
     * 
     * <p>该方法根据组件名称生成对应的官方文档链接，
     * 支持内置组件库和自定义组件库的文档生成。</p>
     * 
     * <p>生成策略：</p>
     * <ol>
     *   <li>检查是否是自定义组件库的组件</li>
     *   <li>如果是自定义组件，使用自定义文档生成逻辑</li>
     *   <li>如果是内置组件，使用标准模板生成</li>
     * </ol>
     * 
     * <p>URL 生成流程：</p>
     * <ul>
     *   <li>获取文档 URL 模板</li>
     *   <li>移除组件前缀（如果存在）</li>
     *   <li>使用 String.format 格式化 URL</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>在 IDE 中提供文档跳转功能</li>
     *   <li>生成组件的帮助链接</li>
     *   <li>集成到帮助系统中</li>
     *   <li>用户学习和参考</li>
     * </ul>
     * 
     * @param componentName 要生成文档链接的组件名称
     * @return 组件的官方文档 URL，如果无法生成则返回空字符串
     * 
     * @see #getDocumentationUrlTemplate()
     * @see #getComponentPrefix()
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#generateCustomDocumentationUrl(String)
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
     * <p>该方法根据组件名称确定组件所属的组件库，并返回该组件库的用户友好显示名称。
     * 支持内置组件库、自定义组件库和官方组件库的识别。</p>
     * 
     * <p>识别策略：</p>
     * <ol>
     *   <li>检查是否是自定义组件库的组件</li>
     *   <li>检查是否是内置组件库的组件</li>
     *   <li>检查是否来自其他官方组件库</li>
     *   <li>返回对应的组件库显示名称</li>
     * </ol>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>自定义组件：返回自定义组件库的显示名称</li>
     *   <li>内置组件：返回当前组件库的显示名称</li>
     *   <li>官方组件：返回官方组件库的名称</li>
     *   <li>未知组件：返回"未知组件库"</li>
     * </ul>
     * 
     * @param componentName 组件名称，不能为null或空字符串
     * @return 组件所属组件库的显示名称，如果无法确定则返回"未知组件库"
     * 
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#isCustomComponent(String)
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#getCustomLibraryDisplayName(String)
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#isComponentFromLibrary(String, String)
     * @see #getLibraryDisplayName()
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
        if (LibraryTypeHelper.isComponentFromLibrary(componentName, libraryType)) {
            return LibraryTypeHelper.getDisplayName(libraryType);
        }

        // 检查是否来自其他官方组件库
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                String libraryType = getLibraryTypeByName(library.getName());
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
     * <p>该方法根据组件名称确定组件所属的组件库，并返回该组件库的版本号。
     * 支持内置组件库、自定义组件库和官方组件库的版本获取。</p>
     * 
     * <p>版本获取策略：</p>
     * <ol>
     *   <li>检查是否是自定义组件库的组件</li>
     *   <li>检查是否是内置组件库的组件</li>
     *   <li>检查是否来自其他官方组件库</li>
     *   <li>返回对应的组件库版本</li>
     * </ol>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>自定义组件：返回自定义组件库的版本</li>
     *   <li>内置组件：返回当前组件库的版本信息</li>
     *   <li>官方组件：返回官方组件库的版本</li>
     *   <li>未知组件：返回"未知版本"</li>
     * </ul>
     * 
     * @param componentName 组件名称，不能为null或空字符串
     * @return 组件所属组件库的版本号，如果无法确定则返回"未知版本"
     * 
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#isCustomComponent(String)
     * @see com.chu7.vuecomponentassistant.utils.CustomComponentLibraryManager#getCustomLibraryVersion(String)
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#isComponentFromLibrary(String, String)
     * @see #getComponentVersionInfo()
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
        if (LibraryTypeHelper.isComponentFromLibrary(componentName, libraryType)) {
            return getComponentVersionInfo();
        }

        // 检查是否来自其他官方组件库
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> allLibraries = libraryManager.getAllLibraries();

            for (ComponentLibrary library : allLibraries) {
                // 检查该组件库是否在项目的启用列表中
                String libraryType = getLibraryTypeByName(library.getName());
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
     * 根据组件库名称获取对应的组件库类型
     * 
     * <p>该方法根据组件库名称获取对应的组件库类型字符串，
     * 用于在组件库管理中进行类型识别和匹配。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>验证组件库名称参数的有效性</li>
     *   <li>移除版本号部分，只保留组件库名称</li>
     *   <li>尝试使用 LibraryTypeHelper 获取包名</li>
     *   <li>如果无法识别，直接返回组件库名称</li>
     * </ol>
     * 
     * <p>版本号处理：</p>
     * <ul>
     *   <li>使用正则表达式移除版本号部分</li>
     *   <li>例如："element-plus (2.3.0)" → "element-plus"</li>
     *   <li>支持括号和空格格式的版本号</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果 LibraryTypeHelper 能识别：返回对应的包名</li>
     *   <li>如果无法识别：返回清理后的组件库名称</li>
     *   <li>如果参数无效：返回 null</li>
     * </ul>
     * 
     * @param libraryName 组件库名称，可能包含版本号
     * @return 对应的组件库类型字符串，如果找不到则返回 null
     * 
     * @see com.chu7.vuecomponentassistant.utils.LibraryTypeHelper#getPackageName(String)
     */
    private String getLibraryTypeByName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return null;
        }
        
        // 移除版本号部分，只保留组件库名称
        String cleanName = libraryName.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
        
        // 对于自定义组件库，直接返回组件库名称
        // 对于官方组件库，尝试使用 LibraryTypeHelper
        String packageName = LibraryTypeHelper.getPackageName(cleanName);
        if (packageName != null && !packageName.equals("unknown")) {
            return packageName;
        }
        
        // 如果 LibraryTypeHelper 无法识别，直接返回组件库名称
        return cleanName;
    }
}
