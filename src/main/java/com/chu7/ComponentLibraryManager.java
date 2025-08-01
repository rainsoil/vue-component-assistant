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

@State(
        name = "ComponentLibraryManager",
        storages = @Storage("componentLibraries.json")
)
@Service(Service.Level.PROJECT)
public final class ComponentLibraryManager implements PersistentStateComponent<ComponentLibraryManager.State> {
    public static class State {
        public Map<String, ComponentLibrary> libraries = new HashMap<>(); // 组件库映射
    }

    private State state = new State();
    private Project project;

    public static ComponentLibraryManager getInstance(@NotNull Project project) {
        ComponentLibraryManager manager = project.getService(ComponentLibraryManager.class);
        manager.project = project;
        return manager;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public Map<String, ComponentLibrary> getLibraries() {
        return state.libraries;
    }

    public void addLibrary(ComponentLibrary library) {
        if (library.name != null) {
            state.libraries.put(library.name, library);
        }
    }

    public void removeLibrary(String libraryName) {
        state.libraries.remove(libraryName);
    }

    public ComponentLibrary getLibrary(String libraryName) {
        return state.libraries.get(libraryName);
    }

    public List<ComponentMeta> getAllComponents() {
        List<ComponentMeta> allComponents = new ArrayList<>();
        for (ComponentLibrary library : state.libraries.values()) {
            allComponents.addAll(library.getComponents());
        }
        return allComponents;
    }

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

    public Project getProject() {
        return project;
    }

    public String exportLibrariesToJson() {
        return new Gson().toJson(state.libraries.values());
    }
} 