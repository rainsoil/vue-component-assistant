package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(name = "VueKitPluginSettings", storages = @Storage("vuekit.xml"))
public class PluginSettings implements PersistentStateComponent<PluginSettings.State> {
	public static class State {
		public String selectedLibraryId = "element-plus";
		public String selectedVersion = ""; // 为空则使用内置或自动检测
		public String remoteBaseUrl = "https://element-plus.org"; // 示例占位，可在后续设置页调整
		public boolean offlineMode = false;
		public List<String> customLibraryJsonPaths = new ArrayList<>();
	}
	
	private State state = new State();
	
	public static PluginSettings getInstance() {
		return ServiceManager.getService(PluginSettings.class);
	}
	
	@Override
	public @Nullable State getState() {
		return state;
	}
	
	@Override
	public void loadState(@NotNull State state) {
		this.state = state;
	}
	
	// 便捷访问器
	public String getSelectedLibraryId() { return state.selectedLibraryId; }
	public String getSelectedVersion() { return state.selectedVersion; }
	public String getRemoteBaseUrl() { return state.remoteBaseUrl; }
	public boolean isOfflineMode() { return state.offlineMode; }
	public List<String> getCustomLibraryJsonPaths() { return state.customLibraryJsonPaths; }
} 