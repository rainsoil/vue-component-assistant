package com.chu7;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

@State(
        name = "ElementLibrarySettings",
        storages = @Storage("elementLibrarySettings.xml")
)
@Service(Service.Level.PROJECT)
public final class ElementLibrarySettings implements PersistentStateComponent<ElementLibrarySettings.State> {
    public static class State {
        public List<String> selectedLibraries = new ArrayList<>(); // 选中的组件库列表
        public boolean autoDetect = true; // 是否自动检测
    }

    private State state = new State();

    public static ElementLibrarySettings getInstance(@NotNull Project project) {
        return project.getService(ElementLibrarySettings.class);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public List<String> getSelectedLibraries() {
        return state.selectedLibraries;
    }

    public void setSelectedLibraries(List<String> libraries) {
        state.selectedLibraries = libraries;
    }

    public boolean isAutoDetect() {
        return state.autoDetect;
    }

    public void setAutoDetect(boolean autoDetect) {
        state.autoDetect = autoDetect;
    }

    // 兼容旧版本的方法
    public String getLibrary() {
        if (state.selectedLibraries.isEmpty()) {
            return "auto";
        }
        return state.selectedLibraries.get(0);
    }

    public void setLibrary(String lib) {
        state.selectedLibraries.clear();
        if (!"auto".equals(lib)) {
            state.selectedLibraries.add(lib);
        }
    }
}