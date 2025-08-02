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
 * 负责管理项目中的所有自定义组件库，提供组件的增删改查功能
 * 使用 IntelliJ 的持久化状态组件来保存数据
 */
@State(
        name = "ComponentLibraryManager",
        storages = @Storage("componentLibraries.json")
)
@Service(Service.Level.PROJECT)
public final class ComponentLibraryManager implements PersistentStateComponent<ComponentLibraryManager.State> {
    
    /**
     * 内部状态类，用于持久化存储
     */
    public static class State {
        /** 组件库映射，以组件库名称为键 */
        public Map<String, ComponentLibrary> libraries = new HashMap<>();
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
    }

    /**
     * 获取所有组件库
     * @return 组件库映射
     */
    public Map<String, ComponentLibrary> getLibraries() {
        return state.libraries;
    }

    /**
     * 添加组件库
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
        state.libraries.remove(libraryName);
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
     * 获取所有组件库中的所有组件
     * @return 所有组件的列表
     */
    public List<ComponentMeta> getAllComponents() {
        List<ComponentMeta> allComponents = new ArrayList<>();
        for (ComponentLibrary library : state.libraries.values()) {
            allComponents.addAll(library.getComponents());
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
} 