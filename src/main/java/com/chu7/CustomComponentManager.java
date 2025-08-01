package com.chu7;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@State(
        name = "CustomComponentManager",
        storages = @Storage("customComponents.json")
)
@Service(Service.Level.PROJECT)
public final class CustomComponentManager implements PersistentStateComponent<CustomComponentManager.State> {
    public static class State {
        public List<ComponentMeta> customComponents = new ArrayList<>();
    }

    private State state = new State();
    private Project project;

    public static CustomComponentManager getInstance(@NotNull Project project) {
        CustomComponentManager manager = project.getService(CustomComponentManager.class);
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

    public List<ComponentMeta> getCustomComponents() {
        return state.customComponents;
    }

    public void addCustomComponent(ComponentMeta component) {
        state.customComponents.add(component);
    }

    public void removeCustomComponent(String componentName) {
        state.customComponents.removeIf(comp -> comp.name.equals(componentName));
    }

    public void saveCustomComponentsToFile(Path filePath) {
        try (FileWriter writer = new FileWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
            new Gson().toJson(state.customComponents, writer);
        } catch (IOException e) {
            // 处理异常
        }
    }

    public void loadCustomComponentsFromFile(Path filePath) {
        try (FileReader reader = new FileReader(filePath.toFile(), StandardCharsets.UTF_8)) {
            List<ComponentMeta> components = new Gson().fromJson(reader, new TypeToken<List<ComponentMeta>>(){}.getType());
            if (components != null) {
                state.customComponents = components;
            }
        } catch (IOException e) {
            // 处理异常
        }
    }
    
    public void exportCustomComponentsToFile(Path filePath) {
        try (FileWriter writer = new FileWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
            new Gson().toJson(state.customComponents, writer);
        } catch (IOException e) {
            // 处理异常
        }
    }
    
    public String exportCustomComponentsToJson() {
        return new Gson().toJson(state.customComponents);
    }
    
    public Project getProject() {
        return project;
    }
} 