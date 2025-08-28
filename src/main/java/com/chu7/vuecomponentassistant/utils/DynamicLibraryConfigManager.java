package com.chu7.vuecomponentassistant.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.util.io.FileUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;

/**
 * 动态组件库配置管理器
 */
public class DynamicLibraryConfigManager {
	private static final Logger LOG = VueKitLogger.getLogger(DynamicLibraryConfigManager.class);
	private static volatile DynamicLibraryConfigManager instance;
	private Map<String, LibraryConfig> libraryConfigs;
	private Map<String, String> packageToLibraryMap;
	private boolean initialized = false;

	public static class LibraryConfig {
		private String id;
		private String packageName;
		private String displayName;
		private String componentPrefix;
		private String description;
		public String getId() { return id; }
		public String getPackageName() { return packageName; }
		public String getDisplayName() { return displayName; }
		public String getComponentPrefix() { return componentPrefix; }
		public String getDescription() { return description; }
		public void setId(String id) { this.id = id; }
		public void setPackageName(String packageName) { this.packageName = packageName; }
		public void setDisplayName(String displayName) { this.displayName = displayName; }
		public void setComponentPrefix(String componentPrefix) { this.componentPrefix = componentPrefix; }
		public void setDescription(String description) { this.description = description; }
	}

	private DynamicLibraryConfigManager() {
		this.libraryConfigs = new HashMap<>();
		this.packageToLibraryMap = new HashMap<>();
	}

	public static DynamicLibraryConfigManager getInstance() {
		if (instance == null) {
			synchronized (DynamicLibraryConfigManager.class) {
				if (instance == null) {
					instance = new DynamicLibraryConfigManager();
				}
			}
		}
		return instance;
	}

	public synchronized void initialize() {
		if (initialized) { return; }
		try {
			LOG.info("开始初始化动态组件库配置管理器");
			loadDefaultConfiguration();
			buildPackageToLibraryMap();
			initialized = true;
			LOG.info("动态组件库配置管理器初始化完成，加载了 " + libraryConfigs.size() + " 个组件库配置");
		} catch (Exception e) {
			LOG.error("初始化动态组件库配置管理器失败", e);
			initialized = true;
		}
	}

	@Deprecated
	private void loadConfiguration() throws Exception {
		LOG.warn("loadConfiguration 方法已废弃，使用动态配置替代");
		throw new Exception("配置文件加载已废弃，使用动态配置");
	}

	private void loadDefaultConfiguration() {
		LOG.warn("使用默认组件库配置");
		try {
			ComponentLibraryManager libraryManager = new ComponentLibraryManager();
			List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
			if (installedLibraries != null && !installedLibraries.isEmpty()) {
				for (ComponentLibrary library : installedLibraries) {
					LibraryConfig config = new LibraryConfig();
					config.setId(library.getName());
					config.setPackageName(library.getName());
					config.setDisplayName(library.getDisplayName() != null ? library.getDisplayName() : library.getName());
					String componentPrefix = library.getComponentPrefix();
					if (componentPrefix != null && !componentPrefix.trim().isEmpty()) {
						config.setComponentPrefix(componentPrefix);
					} else {
						config.setComponentPrefix(inferComponentPrefix(library.getName()));
					}
					libraryConfigs.put(library.getName(), config);
					LOG.info("动态加载组件库配置: " + library.getName() + " (前缀: " + config.getComponentPrefix() + ")");
				}
			} else {
				LOG.warn("没有找到已安装的组件库，使用空配置");
			}
		} catch (Exception e) {
			LOG.error("动态加载组件库配置失败，使用空配置", e);
		}
	}

	private String inferComponentPrefix(String libraryName) {
		if (libraryName == null) return "";
		try {
			ComponentLibraryManager libraryManager = new ComponentLibraryManager();
			List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
			for (ComponentLibrary library : installedLibraries) {
				if (libraryName.equals(library.getName())) {
					break;
				}
			}
		} catch (Exception e) { LOG.debug("从已安装组件库获取前缀失败，使用智能推断: " + e.getMessage()); }
		String lowerName = libraryName.toLowerCase();
		if (lowerName.contains("element")) return "el-";
		if (lowerName.contains("ant") || lowerName.contains("design")) return "a-";
		if (lowerName.contains("vuetify")) return "v-";
		if (lowerName.contains("quasar")) return "q-";
		if (lowerName.contains("naive")) return "n-";
		if (lowerName.contains("prime")) return "p-";
		return inferPrefixFromLibraryName(libraryName);
	}

	private String inferPrefixFromLibraryName(String libraryName) {
		if (libraryName == null || libraryName.trim().isEmpty()) { return ""; }
		String[] parts = libraryName.split("-");
		if (parts.length > 0) {
			String firstPart = parts[0].toLowerCase();
			if (firstPart.length() >= 2) { return firstPart.substring(0, 2) + "-"; }
			else { return firstPart + "-"; }
		}
		return libraryName.length() >= 2 ? libraryName.substring(0, 2).toLowerCase() + "-" : libraryName + "-";
	}

	private void buildPackageToLibraryMap() {
		packageToLibraryMap.clear();
		for (LibraryConfig config : libraryConfigs.values()) {
			if (config.getPackageName() != null && !config.getPackageName().trim().isEmpty()) {
				packageToLibraryMap.put(config.getPackageName(), config.getId());
				LOG.debug("包名映射: " + config.getPackageName() + " -> " + config.getId());
			}
		}
	}

	public String getLibraryIdByPackageName(String packageName) { ensureInitialized(); return packageToLibraryMap.get(packageName); }
	public LibraryConfig getLibraryConfig(String libraryId) { ensureInitialized(); return libraryConfigs.get(libraryId); }
	public LibraryConfig getLibraryConfigByPackageName(String packageName) { String libraryId = getLibraryIdByPackageName(packageName); return libraryId != null ? getLibraryConfig(libraryId) : null; }
	public Map<String, LibraryConfig> getAllLibraryConfigs() { ensureInitialized(); return new HashMap<>(libraryConfigs); }
	public String getDisplayName(String libraryId) { LibraryConfig config = getLibraryConfig(libraryId); return config != null ? config.getDisplayName() : "未知组件库"; }
	public String getComponentPrefix(String libraryId) {
		if (libraryId == null) { return ""; }
		LibraryConfig config = getLibraryConfig(libraryId);
		return config != null ? config.getComponentPrefix() : "";
	}

	public boolean isKnownLibrary(String libraryId) { ensureInitialized(); return libraryConfigs.containsKey(libraryId); }
	public boolean registerLibraryConfig(LibraryConfig libraryConfig) {
		if (libraryConfig == null || libraryConfig.getId() == null) { return false; }
		try {
			libraryConfigs.put(libraryConfig.getId(), libraryConfig);
			if (libraryConfig.getPackageName() != null && !libraryConfig.getPackageName().trim().isEmpty()) {
				packageToLibraryMap.put(libraryConfig.getPackageName(), libraryConfig.getId());
			}
			LOG.info("动态注册组件库配置成功: " + libraryConfig.getId() + " -> " + libraryConfig.getDisplayName());
			return true;
		} catch (Exception e) { LOG.error("动态注册组件库配置失败: " + libraryConfig.getId(), e); return false; }
	}

	public void refreshConfiguration() {
		try {
			LOG.info("开始强制刷新组件库配置");
			libraryConfigs.clear();
			packageToLibraryMap.clear();
			loadDefaultConfiguration();
			buildPackageToLibraryMap();
			LOG.info("组件库配置刷新完成，当前配置数量: " + libraryConfigs.size());
		} catch (Exception e) { LOG.error("刷新组件库配置失败", e); }
	}

	public boolean autoRegisterFromDownloadedLibrary(String libraryId, String packageName, String displayName, String componentPrefix) {
		if (isKnownLibrary(libraryId)) { return true; }
		try {
			LibraryConfig config = new LibraryConfig();
			config.setId(libraryId);
			config.setPackageName(packageName != null ? packageName : libraryId);
			config.setDisplayName(displayName != null ? displayName : libraryId);
			config.setComponentPrefix(componentPrefix != null ? componentPrefix : "");
			config.setDescription("自动识别的组件库: " + libraryId);
			return registerLibraryConfig(config);
		} catch (Exception e) { LOG.error("自动注册组件库配置失败: " + libraryId, e); return false; }
	}

	public boolean isComponentFromLibrary(String componentName, String libraryId) {
		if (componentName == null || libraryId == null) { return false; }
		String prefix = getComponentPrefix(libraryId);
		return !prefix.isEmpty() && componentName.startsWith(prefix);
	}

	public String inferLibraryTypeFromPackageName(String packageName) {
		if (packageName == null || packageName.trim().isEmpty()) { return null; }
		String cleanName = packageName.trim().toLowerCase();
		String libraryId = packageToLibraryMap.get(cleanName);
		if (libraryId != null) { return libraryId; }
		for (Map.Entry<String, String> entry : packageToLibraryMap.entrySet()) {
			if (cleanName.contains(entry.getKey()) || entry.getKey().contains(cleanName)) { return entry.getValue(); }
		}
		return null;
	}

	private void ensureInitialized() { if (!initialized) { initialize(); } }
	public void reloadConfiguration() { LOG.info("重新加载组件库配置"); initialized = false; libraryConfigs.clear(); packageToLibraryMap.clear(); initialize(); }
	public String[] getSupportedLibraryIds() { ensureInitialized(); return libraryConfigs.keySet().toArray(new String[0]); }
	public String[] getSupportedPackageNames() { ensureInitialized(); return packageToLibraryMap.keySet().toArray(new String[0]); }
} 