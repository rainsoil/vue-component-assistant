package com.chu7;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组件库管理器
 * 负责管理项目中的所有组件库（包括内置组件库和自定义组件库），提供组件的增删改查功能
 * 使用 IntelliJ 的持久化状态组件来保存数据
 */
@State(
        name = "ComponentLibraryManager",
        storages = @Storage("componentLibraries.json")
)
@Service(Service.Level.PROJECT)
public final class ComponentLibraryManager implements PersistentStateComponent<ComponentLibraryManager.State> {
    
    /**
     * 内置组件库名称常量
     */
    public static final String ELEMENT_PLUS = "element-plus";
    public static final String ELEMENT_UI = "element-ui";
    public static final String ANT_DESIGN_VUE = "ant-design-vue";
    
    /**
     * 内部状态类，用于持久化存储
     */
    public static class State {
        /** 组件库映射，以组件库名称为键 */
        public Map<String, ComponentLibrary> libraries = new HashMap<>();
        /** 启用的组件库列表 */
        public List<String> enabledLibraries = new ArrayList<>();
    }

    /** 当前状态 */
    private State state = new State();
    
    /** 项目实例 */
    private Project project;

    /**
     * 获取组件库管理器实例
     * @param project 项目实例
     * @return 组件库管理器实例
     */
    public static ComponentLibraryManager getInstance(@NotNull Project project) {
        ComponentLibraryManager manager = project.getService(ComponentLibraryManager.class);
        manager.project = project;
        return manager;
    }

    /**
     * 获取当前状态
     * @return 当前状态
     */
    @Override
    public State getState() {
        return state;
    }

    /**
     * 加载状态
     * @param state 要加载的状态
     */
    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
        // 确保内置组件库存在
        initializeBuiltinLibraries();
    }

    /**
     * 初始化内置组件库
     */
    private void initializeBuiltinLibraries() {
        // 初始化 Element Plus
        if (!state.libraries.containsKey(ELEMENT_PLUS)) {
            ComponentLibrary elementPlus = createElementPlusLibrary();
            state.libraries.put(ELEMENT_PLUS, elementPlus);
        }
        
        // 初始化 Element UI
        if (!state.libraries.containsKey(ELEMENT_UI)) {
            ComponentLibrary elementUI = createElementUILibrary();
            state.libraries.put(ELEMENT_UI, elementUI);
        }
        
        // 初始化 Ant Design Vue
        if (!state.libraries.containsKey(ANT_DESIGN_VUE)) {
            ComponentLibrary antDesignVue = createAntDesignVueLibrary();
            state.libraries.put(ANT_DESIGN_VUE, antDesignVue);
        }
        
        // 默认启用 Element Plus
        if (state.enabledLibraries.isEmpty()) {
            state.enabledLibraries.add(ELEMENT_PLUS);
        }
    }

    /**
     * 创建 Element Plus 组件库
     */
    private ComponentLibrary createElementPlusLibrary() {
        ComponentLibrary library = new ComponentLibrary();
        library.name = ELEMENT_PLUS;
        library.description = "Element Plus - 基于 Vue 3 的桌面端组件库";
        library.version = "2.x";
        library.author = "Element Plus Team";
        library.website = "https://element-plus.org";
        library.docUrl = "https://element-plus.org/zh-CN/";
        
        // 从 JSON 文件加载组件数据
        loadComponentsFromJson(library, "/data/element-plus-components.json");
        
        return library;
    }

    /**
     * 创建 Element UI 组件库
     */
    private ComponentLibrary createElementUILibrary() {
        ComponentLibrary library = new ComponentLibrary();
        library.name = ELEMENT_UI;
        library.description = "Element UI - 基于 Vue 2 的桌面端组件库";
        library.version = "2.x";
        library.author = "Element UI Team";
        library.website = "https://element.eleme.cn";
        library.docUrl = "https://element.eleme.cn/#/zh-CN";
        
        // 从 JSON 文件加载组件数据
        loadComponentsFromJson(library, "/data/element-ui-components.json");
        
        return library;
    }

    /**
     * 创建 Ant Design Vue 组件库
     */
    private ComponentLibrary createAntDesignVueLibrary() {
        ComponentLibrary library = new ComponentLibrary();
        library.name = ANT_DESIGN_VUE;
        library.description = "Ant Design Vue - 基于 Ant Design 设计体系的 Vue 组件库";
        library.version = "3.x";
        library.author = "Ant Design Vue Team";
        library.website = "https://antdv.com";
        library.docUrl = "https://antdv.com/docs/vue/introduce";
        
        // 从 JSON 文件加载组件数据
        loadComponentsFromJson(library, "/data/ant-design-vue-components.json");
        
        return library;
    }

    /**
     * 从 JSON 文件加载组件数据
     */
    private void loadComponentsFromJson(ComponentLibrary library, String jsonPath) {
        try {
            // 从资源文件加载 JSON 数据
            java.io.InputStream inputStream = getClass().getResourceAsStream(jsonPath);
            if (inputStream != null) {
                String jsonContent = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                List<ComponentMeta> components = new com.google.gson.Gson().fromJson(
                    jsonContent, 
                    new com.google.gson.reflect.TypeToken<List<ComponentMeta>>(){}.getType()
                );
                if (components != null) {
                    library.components = components;
                } else {
                    library.components = new ArrayList<>();
                }
                inputStream.close();
            } else {
                library.components = new ArrayList<>();
            }
        } catch (Exception e) {
            // 如果加载失败，使用空列表
            library.components = new ArrayList<>();
        }
    }

    /**
     * 获取所有组件库
     * @return 组件库映射
     */
    public Map<String, ComponentLibrary> getLibraries() {
        return state.libraries;
    }

    /**
     * 获取启用的组件库
     * @return 启用的组件库列表
     */
    public List<String> getEnabledLibraries() {
        return state.enabledLibraries;
    }

    /**
     * 设置启用的组件库
     * @param enabledLibraries 启用的组件库列表
     */
    public void setEnabledLibraries(List<String> enabledLibraries) {
        state.enabledLibraries = enabledLibraries;
    }

    /**
     * 启用组件库
     * @param libraryName 组件库名称
     */
    public void enableLibrary(String libraryName) {
        if (!state.enabledLibraries.contains(libraryName)) {
            state.enabledLibraries.add(libraryName);
        }
    }

    /**
     * 禁用组件库
     * @param libraryName 组件库名称
     */
    public void disableLibrary(String libraryName) {
        state.enabledLibraries.remove(libraryName);
    }

    /**
     * 检查组件库是否启用
     * @param libraryName 组件库名称
     * @return 是否启用
     */
    public boolean isLibraryEnabled(String libraryName) {
        return state.enabledLibraries.contains(libraryName);
    }

    /**
     * 添加自定义组件库
     * @param library 要添加的组件库
     */
    public void addLibrary(ComponentLibrary library) {
        if (library.name != null) {
            state.libraries.put(library.name, library);
        }
    }

    /**
     * 删除组件库
     * @param libraryName 要删除的组件库名称
     */
    public void removeLibrary(String libraryName) {
        // 不允许删除内置组件库
        if (isBuiltinLibrary(libraryName)) {
            return;
        }
        state.libraries.remove(libraryName);
        state.enabledLibraries.remove(libraryName);
    }

    /**
     * 检查是否为内置组件库
     * @param libraryName 组件库名称
     * @return 是否为内置组件库
     */
    public boolean isBuiltinLibrary(String libraryName) {
        return ELEMENT_PLUS.equals(libraryName) || 
               ELEMENT_UI.equals(libraryName) || 
               ANT_DESIGN_VUE.equals(libraryName);
    }

    /**
     * 根据名称获取组件库
     * @param libraryName 组件库名称
     * @return 组件库实例，如果不存在则返回 null
     */
    public ComponentLibrary getLibrary(String libraryName) {
        return state.libraries.get(libraryName);
    }

    /**
     * 获取所有启用的组件库中的所有组件
     * @return 所有启用的组件的列表
     */
    public List<ComponentMeta> getAllComponents() {
        List<ComponentMeta> allComponents = new ArrayList<>();
        for (String libraryName : state.enabledLibraries) {
            ComponentLibrary library = state.libraries.get(libraryName);
            if (library != null) {
                allComponents.addAll(library.getComponents());
            }
        }
        return allComponents;
    }

    /**
     * 从指定的组件库中获取组件
     * @param libraryNames 组件库名称列表
     * @return 指定组件库中的组件列表
     */
    public List<ComponentMeta> getComponentsFromLibraries(List<String> libraryNames) {
        List<ComponentMeta> components = new ArrayList<>();
        for (String libraryName : libraryNames) {
            ComponentLibrary library = state.libraries.get(libraryName);
            if (library != null) {
                components.addAll(library.getComponents());
            }
        }
        return components;
    }

    /**
     * 获取项目实例
     * @return 项目实例
     */
    public Project getProject() {
        return project;
    }

    /**
     * 导出所有组件库为 JSON 格式
     * @return JSON 字符串
     */
    public String exportLibrariesToJson() {
        return new Gson().toJson(state.libraries.values());
    }

    /**
     * 获取内置组件库列表
     * @return 内置组件库列表
     */
    public List<String> getBuiltinLibraries() {
        List<String> builtinLibraries = new ArrayList<>();
        builtinLibraries.add(ELEMENT_PLUS);
        builtinLibraries.add(ELEMENT_UI);
        builtinLibraries.add(ANT_DESIGN_VUE);
        return builtinLibraries;
    }

    /**
     * 获取自定义组件库列表
     * @return 自定义组件库列表
     */
    public List<String> getCustomLibraries() {
        List<String> customLibraries = new ArrayList<>();
        for (String libraryName : state.libraries.keySet()) {
            if (!isBuiltinLibrary(libraryName)) {
                customLibraries.add(libraryName);
            }
        }
        return customLibraries;
    }
} 