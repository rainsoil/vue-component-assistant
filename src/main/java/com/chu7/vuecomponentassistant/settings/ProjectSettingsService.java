package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.PROJECT)
@State(name = "VueKitProjectSettings", storages = @Storage("vuekit-project-settings.xml"))
public final class ProjectSettingsService implements PersistentStateComponent<ProjectSettingsService.State> {
	private final Project project;
	private State state = new State();

	public ProjectSettingsService(Project project) {
		this.project = project;
	}

	public static final class State {
		public boolean enableComponentCompletion = true;
		public boolean enableAttributeCompletion = true;
		public boolean enableEventCompletion = true;
		public boolean enableSlotCompletion = true;
		public boolean enableHoverDocumentation = true;
		public boolean enableRightClickDocumentation = true;
	}

	@Override
	public @Nullable State getState() {
		return state;
	}

	@Override
	public void loadState(@NotNull State state) {
		this.state = state;
	}

	public boolean isEnableComponentCompletion() { return state.enableComponentCompletion; }
	public void setEnableComponentCompletion(boolean v) { state.enableComponentCompletion = v; }

	public boolean isEnableAttributeCompletion() { return state.enableAttributeCompletion; }
	public void setEnableAttributeCompletion(boolean v) { state.enableAttributeCompletion = v; }

	public boolean isEnableEventCompletion() { return state.enableEventCompletion; }
	public void setEnableEventCompletion(boolean v) { state.enableEventCompletion = v; }

	public boolean isEnableSlotCompletion() { return state.enableSlotCompletion; }
	public void setEnableSlotCompletion(boolean v) { state.enableSlotCompletion = v; }

	public boolean isEnableHoverDocumentation() { return state.enableHoverDocumentation; }
	public void setEnableHoverDocumentation(boolean v) { state.enableHoverDocumentation = v; }

	public boolean isEnableRightClickDocumentation() { return state.enableRightClickDocumentation; }
	public void setEnableRightClickDocumentation(boolean v) { state.enableRightClickDocumentation = v; }
} 