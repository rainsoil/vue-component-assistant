package com.chu7.vuecomponentassistant.settings;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import com.intellij.openapi.vfs.VirtualFile;

@Service
@State(
        name = "ComponentLibraryConfigManager",
        storages = @Storage("vuekit-component-library-config.xml")
)
public final class ComponentLibraryConfigManager implements PersistentStateComponent<ComponentLibraryConfigManager.ConfigState> {
	private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigManager.class);
	private static final String PROJECT_CONFIG_FILE = "vuekit-project-config.json";
	private static final String GLOBAL_CONFIG_FILE = "vuekit-libraries.json";

	private static Set<String> DEFAULT_ENABLED_LIBRARIES;
	static {
		initializeDefaultLibraries();
	}

	private static void initializeDefaultLibraries() {
		try {
			com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager =
				new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
			java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries =
				libraryManager.getAllLibraries();
			Set<String> defaultLibraries = new HashSet<>();
			for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
				String libraryType = LibraryTypeHelper.getPackageName(library.getName());
				if (LibraryTypeHelper.isKnownLibrary(libraryType)) {
					defaultLibraries.add(libraryType);
				}
			}
			if (defaultLibraries.isEmpty()) {
				VueKitLogger.warn(LOG, "未找到任何组件库，使用空集合作为默认配置");
			}
			DEFAULT_ENABLED_LIBRARIES = defaultLibraries;
		} catch (Exception e) {
			VueKitLogger.error(LOG, "初始化默认组件库失败，使用空集合", e);
			DEFAULT_ENABLED_LIBRARIES = new HashSet<String>();
		}
	}

	private final Map<String, ProjectConfig> projectConfigs = new ConcurrentHashMap<>();
	private GlobalConfig globalConfig;
	private final List<ConfigChangeListener> listeners = new ArrayList<>();

	public static ComponentLibraryConfigManager getInstance(Project project) {
		if (project == null) {
			throw new IllegalArgumentException("项目对象不能为null");
		}
		return project.getService(ComponentLibraryConfigManager.class);
	}

	public static ComponentLibraryConfigManager getGlobalInstance() {
		return ApplicationManager.getApplication().getService(ComponentLibraryConfigManager.class);
	}

	public ComponentLibraryConfigManager() {
		loadGlobalConfig();
		initializeProjectListener();
	}

	public Set<String> getEnabledLibraryNames(Project project) {
		if (project == null) {
			throw new IllegalArgumentException("项目对象不能为null");
		}
		try {
			ensureProjectConfigFileExists(project);
			ProjectConfig projectConfig = getProjectConfig(project);
			if (projectConfig != null) {
				Set<String> enabledLibraryNames = new HashSet<>(projectConfig.getEnabledLibraryNames());
				Set<String> sanitized = sanitizeAgainstInstalled(project, enabledLibraryNames);
				if (!sanitized.equals(enabledLibraryNames)) {
					projectConfig.setEnabledLibraryNames(sanitized);
					saveProjectConfig(project, projectConfig);
					// 同步通知组件提供者刷新，确保删除后的组件不再出现
					notifyConfigChangedWithNames(project, sanitized);
				}
				return new HashSet<>(projectConfig.getEnabledLibraryNames());
			}
			if (globalConfig != null && !globalConfig.getDefaultEnabledLibraryNames().isEmpty()) {
				return new HashSet<>(globalConfig.getDefaultEnabledLibraryNames());
			}
			return new HashSet<>();
		} catch (Exception e) {
			VueKitLogger.error(LOG, "获取项目启用的组件库失败", e);
			return new HashSet<>();
		}
	}

	public void setProjectEnabledLibraryNames(Project project, Set<String> enabledLibraryNames) {
		try {
			ensureProjectConfigFileExists(project);
			String projectId = getProjectId(project);
			ProjectConfig projectConfig = getProjectConfig(project);
			if (projectConfig == null) {
				projectConfig = new ProjectConfig();
				projectConfig.setProjectId(projectId);
				projectConfig.setProjectName(project.getName());
			}
			Set<String> sanitized = sanitizeAgainstInstalled(project, enabledLibraryNames != null ? enabledLibraryNames : new HashSet<String>());
			projectConfig.setEnabledLibraryNames(sanitized);
			projectConfigs.put(projectId, projectConfig);
			saveProjectConfig(project, projectConfig);
			notifyConfigChangedWithNames(project, sanitized);
			VueKitLogger.info(LOG, "项目 " + project.getName() + " 的组件库配置已更新: " + (sanitized.isEmpty() ? "无" : String.join(", ", sanitized)));
		} catch (Exception e) {
			VueKitLogger.error(LOG, "设置项目启用的组件库失败", e);
		}
	}

	public void setGlobalDefaultLibraries(Set<String> defaultEnabledLibraries) {
		try {
			if (globalConfig == null) {
				globalConfig = new GlobalConfig();
			}
			globalConfig.setDefaultEnabledLibraryNames(defaultEnabledLibraries);
			saveGlobalConfig();
			notifyGlobalConfigChanged(defaultEnabledLibraries);
			VueKitLogger.info(LOG, "全局默认组件库配置已更新: " + String.join(", ", defaultEnabledLibraries));
		} catch (Exception e) {
			VueKitLogger.error(LOG, "设置全局默认组件库失败", e);
		}
	}

	public Set<String> getGlobalDefaultLibraries() {
		if (globalConfig != null && !globalConfig.getDefaultEnabledLibraryNames().isEmpty()) {
			return new HashSet<>(globalConfig.getDefaultEnabledLibraryNames());
		}
		return new HashSet<>(DEFAULT_ENABLED_LIBRARIES);
	}

	public boolean isLibraryEnabled(Project project, String libraryType) {
		Set<String> enabledLibraries = getEnabledLibraryNames(project);
		return enabledLibraries.contains(libraryType);
	}

	public ProjectConfig getProjectConfigForFeatures(Project project) {
		return getProjectConfig(project);
	}

	public void setProjectConfig(Project project, ProjectConfig config) {
		try {
			String projectId = getProjectId(project);
			projectConfigs.put(projectId, config);
			saveProjectConfig(project, config);
			notifyConfigChanged(project, config.getEnabledLibraryNames());
			VueKitLogger.info(LOG, "项目 " + project.getName() + " 的配置已更新");
		} catch (Exception e) {
			VueKitLogger.error(LOG, "设置项目配置失败", e);
		}
	}

	public void resetToGlobalDefault(Project project) {
		Set<String> globalDefaults = getGlobalDefaultLibraries();
		setProjectEnabledLibraryNames(project, globalDefaults);
		VueKitLogger.info(LOG, "项目 " + project.getName() + " 的组件库配置已重置为全局默认");
	}

	public boolean exportProjectConfig(Project project, String exportPath) {
		try {
			ProjectConfig projectConfig = getProjectConfig(project);
			if (projectConfig == null) {
				VueKitLogger.warn(LOG, "项目配置为空，无法导出");
				return false;
			}
			String configJson = convertProjectConfigToJson(projectConfig);
			Path path = Paths.get(exportPath);
			Files.write(path, configJson.getBytes(StandardCharsets.UTF_8));
			VueKitLogger.info(LOG, "项目配置已导出到: " + exportPath);
			return true;
		} catch (Exception e) {
			VueKitLogger.error(LOG, "导出项目配置失败", e);
			return false;
		}
	}

	public boolean importProjectConfig(Project project, String importPath) {
		try {
			Path path = Paths.get(importPath);
			String configJson = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
			ProjectConfig projectConfig = parseProjectConfigFromJson(configJson);
			if (projectConfig != null) {
				setProjectEnabledLibraryNames(project, projectConfig.getEnabledLibraryNames());
				VueKitLogger.info(LOG, "项目配置已从 " + importPath + " 导入");
				return true;
			}
		} catch (Exception e) {
			VueKitLogger.error(LOG, "导入项目配置失败", e);
		}
		return false;
	}

	public void addConfigChangeListener(ConfigChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeConfigChangeListener(ConfigChangeListener listener) {
		listeners.remove(listener);
	}

	private ProjectConfig getProjectConfig(Project project) {
		String projectId = getProjectId(project);
		if (!projectConfigs.containsKey(projectId)) {
			loadProjectConfig(project);
		}
		return projectConfigs.get(projectId);
	}

	private String getProjectId(Project project) {
		return project.getLocationHash();
	}

	private void loadProjectConfig(Project project) {
		try {
			String projectId = getProjectId(project);
			if (projectConfigs.containsKey(projectId)) {
				return;
			}
			ProjectConfig config = loadProjectConfigFromFile(project);
			if (config != null) {
				projectConfigs.put(projectId, config);
				VueKitLogger.debug(LOG, "项目配置已加载: " + project.getName() +
					", 启用的组件库: " + String.join(", ", config.getEnabledLibraryNames()));
			} else {
				VueKitLogger.debug(LOG, "项目 " + project.getName() + " 没有找到配置文件，将使用默认配置");
			}
		} catch (Exception e) {
			VueKitLogger.error(LOG, "加载项目配置失败", e);
		}
	}

	private ProjectConfig loadProjectConfigFromFile(Project project) {
		try {
			VirtualFile ideaDir = getIdeaDirectory(project);
			if (ideaDir == null) {
				VueKitLogger.warn(LOG, "未找到 .idea 目录");
				return null;
			}
			VirtualFile configFile = ideaDir.findChild(PROJECT_CONFIG_FILE);
			if (configFile != null && configFile.exists()) {
				String configJson = new String(configFile.contentsToByteArray(), StandardCharsets.UTF_8);
				return parseProjectConfigFromJson(configJson);
			}
		} catch (Exception e) {
			VueKitLogger.debug(LOG, "从文件加载项目配置失败", e);
		}
		return null;
	}

	private void saveProjectConfig(Project project, ProjectConfig config) {
		try {
			String configJson = convertProjectConfigToJson(config);
			VirtualFile ideaDir = getIdeaDirectory(project);
			if (ideaDir == null) {
				VueKitLogger.error(LOG, "未找到 .idea 目录，无法保存配置");
				return;
			}
			VirtualFile configFile = ideaDir.findChild(PROJECT_CONFIG_FILE);
			ApplicationManager.getApplication().runWriteAction(() -> {
				try {
					VirtualFile targetFile = configFile;
					if (targetFile == null) {
						targetFile = ideaDir.createChildData(this, PROJECT_CONFIG_FILE);
					}
					targetFile.setBinaryContent(configJson.getBytes(StandardCharsets.UTF_8));
					if (targetFile.exists() && targetFile.getLength() > 0) {
						VueKitLogger.info(LOG, "项目配置已保存到: " + targetFile.getPath() + " (大小: " + targetFile.getLength() + " 字节)");
					} else {
						VueKitLogger.error(LOG, "配置文件写入失败：文件不存在或为空");
					}
				} catch (Exception e) {
					VueKitLogger.error(LOG, "写入项目配置文件失败", e);
				}
			});
		} catch (Exception e) {
			VueKitLogger.error(LOG, "保存项目配置失败", e);
			throw new RuntimeException("保存项目配置失败: " + e.getMessage(), e);
		}
	}

	private void loadGlobalConfig() {
		try {
			String userHome = System.getProperty("user.home");
			Path configPath = Paths.get(userHome, ".vuekit", GLOBAL_CONFIG_FILE);
			if (Files.exists(configPath)) {
				String configJson = new String(Files.readAllBytes(configPath), StandardCharsets.UTF_8);
				globalConfig = parseGlobalConfigFromJson(configJson);
				VueKitLogger.debug(LOG, "全局配置已加载");
			}
		} catch (Exception e) {
			VueKitLogger.debug(LOG, "加载全局配置失败，使用默认配置", e);
		}
	}

	private void saveGlobalConfig() {
		try {
			String userHome = System.getProperty("user.home");
			Path configDir = Paths.get(userHome, ".vuekit");
			Path configPath = configDir.resolve(GLOBAL_CONFIG_FILE);
			if (!Files.exists(configDir)) {
				Files.createDirectories(configDir);
			}
			String configJson = convertGlobalConfigToJson(globalConfig);
			Files.write(configPath, configJson.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			VueKitLogger.error(LOG, "保存全局配置失败", e);
		}
	}

	private VirtualFile getIdeaDirectory(Project project) {
		try {
			VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
			VirtualFile ideaDir = projectDir != null ? projectDir.findChild(".idea") : null;
			if (ideaDir != null && ideaDir.exists() && ideaDir.isDirectory()) {
				return ideaDir;
			}
			ApplicationManager.getApplication().invokeLater(() -> {
				ApplicationManager.getApplication().runWriteAction(() -> {
					try {
						if (projectDir != null) {
							VirtualFile createdIdeaDir = projectDir.createChildDirectory(this, ".idea");
							VueKitLogger.info(LOG, ".idea 目录创建成功: " + createdIdeaDir.getPath());
						}
					} catch (Exception e) {
						VueKitLogger.error(LOG, "创建 .idea 目录失败", e);
					}
				});
			});
			VueKitLogger.info(LOG, ".idea 目录创建请求已提交");
		} catch (Exception e) {
			VueKitLogger.error(LOG, "获取 .idea 目录失败", e);
		}
		return null;
	}

	private void initializeProjectListener() {
		VueKitLogger.info(LOG, "初始化项目配置监听器（使用现代API）");
	}

	public void onProjectStarted(Project project) {
		VueKitLogger.info(LOG, "项目启动，加载组件库配置: " + project.getName());
		try {
			ensureIdeaDirectoryExists(project);
			ensureProjectConfigFileExists(project);
			loadProjectConfig(project);
			VueKitLogger.info(LOG, "✅ 项目配置加载完成");
		} catch (Exception e) {
			VueKitLogger.error(LOG, "项目启动时加载配置失败", e);
		}
	}

	private void ensureIdeaDirectoryExists(Project project) {
		try {
			VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
			if (projectDir == null) {
				VueKitLogger.warn(LOG, "无法获取项目根目录");
				return;
			}
			VirtualFile ideaDir = projectDir.findChild(".idea");
			if (ideaDir == null || !ideaDir.exists() || !ideaDir.isDirectory()) {
				VueKitLogger.info(LOG, ".idea 目录不存在，尝试创建...");
				ApplicationManager.getApplication().invokeLater(() -> {
					ApplicationManager.getApplication().runWriteAction(() -> {
						try {
							VirtualFile createdIdeaDir = projectDir.createChildDirectory(this, ".idea");
							VueKitLogger.info(LOG, ".idea 目录创建成功: " + createdIdeaDir.getPath());
						} catch (Exception e) {
							VueKitLogger.error(LOG, "创建 .idea 目录失败", e);
						}
					});
				});
				VueKitLogger.info(LOG, "✅ .idea 目录创建请求已提交");
			}
		} catch (Exception e) {
			VueKitLogger.error(LOG, "确保 .idea 目录存在时发生错误", e);
		}
	}

	private void ensureProjectConfigFileExists(Project project) {
		try {
			VirtualFile ideaDir = getIdeaDirectory(project);
			if (ideaDir == null) {
				return;
			}
			VirtualFile configFile = ideaDir.findChild(PROJECT_CONFIG_FILE);
			if (configFile == null || !configFile.exists()) {
				ProjectConfig defaultConfig = new ProjectConfig();
				defaultConfig.setProjectId(getProjectId(project));
				defaultConfig.setProjectName(project.getName());
				Set<String> autoDetected = detectEnabledLibrariesFromProject(project);
				defaultConfig.setEnabledLibraryNames(autoDetected);
				saveProjectConfig(project, defaultConfig);
			}
		} catch (Exception e) {
			VueKitLogger.error(LOG, "确保项目配置文件存在时发生错误", e);
		}
	}

	private Set<String> sanitizeAgainstInstalled(Project project, Set<String> candidate) {
		try {
			com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager =
				new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
			java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries =
				libraryManager.getAllLibraries();
			Set<String> installedNames = new HashSet<>();
			for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary lib : installedLibraries) {
				installedNames.add(lib.getName());
			}
			Set<String> result = new HashSet<>();
			for (String name : candidate) {
				if (installedNames.contains(name)) {
					result.add(name);
				}
			}
			return result;
		} catch (Exception e) {
			VueKitLogger.debug(LOG, "同步启用库与已安装列表失败，保留原集合", e);
			return new HashSet<>(candidate);
		}
	}

	private String convertProjectConfigToJson(ProjectConfig config) {
		try {
			com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
			return gson.toJson(config);
		} catch (Exception e) {
			VueKitLogger.error(LOG, "转换项目配置到JSON失败", e);
			return "{\"projectId\":\"" + config.getProjectId() + "\",\"enabledLibraries\":[]}";
		}
	}

	private ProjectConfig parseProjectConfigFromJson(String json) {
		try {
			com.google.gson.Gson gson = new com.google.gson.Gson();
			return gson.fromJson(json, ProjectConfig.class);
		} catch (Exception e) {
			VueKitLogger.error(LOG, "解析项目配置JSON失败", e);
			return new ProjectConfig();
		}
	}

	private String convertGlobalConfigToJson(GlobalConfig config) {
		try {
			com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
			return gson.toJson(config);
		} catch (Exception e) {
			VueKitLogger.error(LOG, "转换全局配置到JSON失败", e);
			return "{\"defaultEnabledLibraries\":[]}";
		}
	}

	private GlobalConfig parseGlobalConfigFromJson(String json) {
		try {
			com.google.gson.Gson gson = new com.google.gson.Gson();
			return gson.fromJson(json, GlobalConfig.class);
		} catch (Exception e) {
			VueKitLogger.error(LOG, "解析全局配置JSON失败", e);
			return new GlobalConfig();
		}
	}

	public static class ConfigState {}

	@Override
	public ConfigState getState() { return new ConfigState(); }

	@Override
	public void loadState(ConfigState state) {}

	public static class ProjectConfig {
		private String projectId;
		private String projectName;
		private Set<String> enabledLibraryNames = new HashSet<>();
		private boolean enableComponentCompletion = true;
		private boolean enableAttributeCompletion = true;
		private boolean enableEventCompletion = true;
		private boolean enableSlotCompletion = true;
		private boolean enableHoverDocumentation = true;
		private boolean enableRightClickDocumentation = true;
		private boolean enableCaching = true;
		private boolean enableDebugMode = false;

		public String getProjectId() { return projectId; }
		public void setProjectId(String projectId) { this.projectId = projectId; }
		public String getProjectName() { return projectName; }
		public void setProjectName(String projectName) { this.projectName = projectName; }
		public Set<String> getEnabledLibraryNames() { return enabledLibraryNames; }
		public void setEnabledLibraryNames(Set<String> enabledLibraryNames) { this.enabledLibraryNames = enabledLibraryNames; }
		public boolean isEnableComponentCompletion() { return enableComponentCompletion; }
		public void setEnableComponentCompletion(boolean enableComponentCompletion) { this.enableComponentCompletion = enableComponentCompletion; }
		public boolean isEnableAttributeCompletion() { return enableAttributeCompletion; }
		public void setEnableAttributeCompletion(boolean enableAttributeCompletion) { this.enableAttributeCompletion = enableAttributeCompletion; }
		public boolean isEnableEventCompletion() { return enableEventCompletion; }
		public void setEnableEventCompletion(boolean enableEventCompletion) { this.enableEventCompletion = enableEventCompletion; }
		public boolean isEnableSlotCompletion() { return enableSlotCompletion; }
		public void setEnableSlotCompletion(boolean enableSlotCompletion) { this.enableSlotCompletion = enableSlotCompletion; }
		public boolean isEnableHoverDocumentation() { return enableHoverDocumentation; }
		public void setEnableHoverDocumentation(boolean enableHoverDocumentation) { this.enableHoverDocumentation = enableHoverDocumentation; }
		public boolean isEnableRightClickDocumentation() { return enableRightClickDocumentation; }
		public void setEnableRightClickDocumentation(boolean enableRightClickDocumentation) { this.enableRightClickDocumentation = enableRightClickDocumentation; }
		public boolean isEnableCaching() { return enableCaching; }
		public void setEnableCaching(boolean enableCaching) { this.enableCaching = enableCaching; }
		public boolean isEnableDebugMode() { return enableDebugMode; }
		public void setEnableDebugMode(boolean enableDebugMode) { this.enableDebugMode = enableDebugMode; }
	}

	public static class GlobalConfig {
		private Set<String> defaultEnabledLibraryNames = new HashSet<>();
		public Set<String> getDefaultEnabledLibraryNames() { return defaultEnabledLibraryNames; }
		public void setDefaultEnabledLibraryNames(Set<String> defaultEnabledLibraryNames) { this.defaultEnabledLibraryNames = defaultEnabledLibraryNames; }
	}

	public interface ConfigChangeListener {
		void onProjectConfigChanged(Project project, Set<String> enabledLibraries);
		void onGlobalConfigChanged(Set<String> defaultEnabledLibraries);
	}

	private void notifyConfigChanged(Project project, Set<String> enabledLibraries) {
		for (ConfigChangeListener listener : listeners) {
			try { listener.onProjectConfigChanged(project, enabledLibraries); } catch (Exception e) { VueKitLogger.error(LOG, "通知配置变更监听器失败", e); }
		}
		try { ComponentProviderManager.notifyProviderReload(project); VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据"); } catch (Exception e) { VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e); }
	}

	private void notifyConfigChangedWithNames(Project project, Set<String> enabledLibraryNames) {
		try { ComponentProviderManager.notifyProviderReload(project); VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据"); } catch (Exception e) { VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e); }
	}

	private void notifyGlobalConfigChanged(Set<String> defaultEnabledLibraries) {
		for (ConfigChangeListener listener : listeners) {
			try { listener.onGlobalConfigChanged(defaultEnabledLibraries); } catch (Exception e) { VueKitLogger.error(LOG, "通知全局配置变更监听器失败", e); }
		}
	}

	private Set<String> detectEnabledLibrariesFromProject(Project project) {
		Set<String> result = new HashSet<>();
		try {
			com.intellij.openapi.vfs.VirtualFile projectRoot = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
			if (projectRoot == null) return result;
			com.intellij.openapi.vfs.VirtualFile pkg = projectRoot.findChild("package.json");
			if (pkg == null || !pkg.exists()) return result;
			String content = new String(pkg.contentsToByteArray(), java.nio.charset.StandardCharsets.UTF_8);
			com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(content).getAsJsonObject();
			java.util.Set<String> declared = new java.util.HashSet<>();
			if (json.has("dependencies") && json.get("dependencies").isJsonObject()) {
				for (java.util.Map.Entry<String, com.google.gson.JsonElement> e : json.getAsJsonObject("dependencies").entrySet()) {
					declared.add(e.getKey());
				}
			}
			if (json.has("devDependencies") && json.get("devDependencies").isJsonObject()) {
				for (java.util.Map.Entry<String, com.google.gson.JsonElement> e : json.getAsJsonObject("devDependencies").entrySet()) {
					declared.add(e.getKey());
				}
			}
			// 与已安装组件库对比，只选择匹配到的
			try {
				com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager =
					new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
				java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installed = libraryManager.getAllLibraries();
				java.util.Set<String> installedNames = new java.util.HashSet<>();
				for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary lib : installed) {
					installedNames.add(lib.getName());
				}
				for (String name : declared) {
					if (installedNames.contains(name)) {
						result.add(name);
					}
				}
			} catch (Exception ignore) {}
		} catch (Exception e) {
			VueKitLogger.debug(LOG, "自动检测 package.json 中的组件库失败", e);
		}
		return result;
	}
} 