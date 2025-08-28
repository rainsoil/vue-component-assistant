package com.chu7.vuecomponentassistant.provider;

import com.intellij.openapi.project.Project;
import com.chu7.vuecomponentassistant.library.model.ComponentLibrary;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.library.cache.LocalCacheManager;
import com.chu7.vuecomponentassistant.library.custom.CustomLibraryMerger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbService;

public class ProviderManager {
	private static final Logger LOG = Logger.getInstance(ProviderManager.class);
	private static final ConcurrentHashMap<Project, UnifiedComponentProvider> PROVIDER_MAP = new ConcurrentHashMap<>();

	@Nullable
	public static UnifiedComponentProvider getProvider(@NotNull Project project) {
		return PROVIDER_MAP.computeIfAbsent(project, ProviderManager::buildProvider);
	}
	
	public static void notifyReload(@NotNull Project project) {
		PROVIDER_MAP.computeIfPresent(project, (proj, oldProvider) -> buildProvider(proj));
	}
	
	public static void notifyReloadAll() {
		PROVIDER_MAP.replaceAll((project, oldProvider) -> buildProvider(project));
	}
	
	public static void removeProvider(@NotNull Project project) {
		PROVIDER_MAP.remove(project);
	}
	
	@NotNull
	private static UnifiedComponentProvider buildProvider(@NotNull Project project) {
		// 1) 读取项目启用的组件库，若为空则返回空 Provider
		java.util.Set<String> enabled = null;
		try {
			enabled = com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(project).getEnabledLibraryNames(project);
		} catch (Throwable ignored) {}
		if (enabled == null || enabled.isEmpty()) {
			return new UnifiedComponentProvider(createEmptyLibrary("data/element-plus-libraries.json"));
		}
		java.util.Set<String> enabledLower = new java.util.HashSet<>();
		for (String s : enabled) if (s != null) enabledLower.add(s.toLowerCase());

		// 2) 优先从“已安装的组件库”（remote cache）构建数据源
		try {
			com.chu7.vuecomponentassistant.remote.ComponentLibraryManager rManager = new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
			java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installed = rManager.getAllLibraries();
			if (installed != null && !installed.isEmpty()) {
				java.util.List<com.chu7.vuecomponentassistant.library.model.Component> mergedComponents = new java.util.ArrayList<>();
				String chosenId = "";
				String chosenName = "";
				String chosenDisplay = "";
				String chosenPrefix = "";
				String chosenVersion = "";
				String chosenSourceUrl = "";
				for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary lib : installed) {
					if (lib == null || lib.getName() == null) continue;
					String libNameLower = lib.getName().toLowerCase();
					if (!enabledLower.contains(libNameLower)) continue;
					if (chosenId.isEmpty()) {
						chosenId = lib.getId() != null ? lib.getId() : lib.getName();
						chosenName = lib.getName();
						chosenDisplay = lib.getDisplayName() != null ? lib.getDisplayName() : lib.getName();
						chosenPrefix = lib.getComponentPrefix() != null ? lib.getComponentPrefix() : chosenPrefix;
						chosenVersion = lib.getVersion() != null ? lib.getVersion() : "";
						chosenSourceUrl = lib.getSourceUrl() != null ? lib.getSourceUrl() : "";
					} else {
						// 多库启用时，前缀若不同则不作为严格过滤条件，仅用于展示
						if (chosenPrefix == null || chosenPrefix.isEmpty()) chosenPrefix = lib.getComponentPrefix();
					}
					if (lib.getComponents() != null) {
						for (com.chu7.vuecomponentassistant.remote.model.ComponentInfo ci : lib.getComponents()) {
							com.chu7.vuecomponentassistant.library.model.Component mapped = mapComponent(ci);
							if (mapped != null) mergedComponents.add(mapped);
						}
					}
				}
				if (!mergedComponents.isEmpty()) {
					com.chu7.vuecomponentassistant.library.model.ComponentLibrary base = new com.chu7.vuecomponentassistant.library.model.ComponentLibrary();
					base.id = chosenId.isEmpty() ? String.join(",", enabledLower) : chosenId;
					base.name = chosenName.isEmpty() ? String.join(",", enabledLower) : chosenName;
					base.displayName = chosenDisplay.isEmpty() ? base.name : chosenDisplay;
					base.componentPrefix = chosenPrefix;
					base.version = chosenVersion;
					base.sourceUrl = chosenSourceUrl;
					base.components = mergedComponents;
					return new UnifiedComponentProvider(base);
				}
			}
		} catch (Throwable ignored) {}

		// 3) 回退到原先逻辑（bundled + 自定义），但仍按启用集合校验
		PluginSettings settings = PluginSettings.getInstance();
		String desiredId = settings != null ? settings.getSelectedLibraryId() : null;
		String desiredVersion = settings != null ? settings.getSelectedVersion() : null;
		String resource;
		if (enabledLower.contains("element-ui")) {
			resource = "data/element-ui-libraries.json";
		} else if (enabledLower.contains("element-plus")) {
			resource = "data/element-plus-libraries.json";
		} else {
			resource = detectLibraryResource(project, desiredId);
		}
		ComponentLibrary base = loadLibraryFromJson(resource);
		if (base == null || base.components == null || base.components.isEmpty()) {
			base = createEmptyLibrary(resource);
		}
		if (desiredId != null && desiredVersion != null && !desiredVersion.isEmpty()) {
			ComponentLibrary cached = LocalCacheManager.load(desiredId, desiredVersion);
			if (cached != null && cached.components != null && !cached.components.isEmpty()) {
				base = cached;
			}
		}
		List<ComponentLibrary> customs = new ArrayList<>();
		if (settings != null && settings.getCustomLibraryJsonPaths() != null) {
			for (String p : settings.getCustomLibraryJsonPaths()) {
				try {
					if (p == null || p.isEmpty()) continue;
					Path path = Path.of(p);
					if (!Files.exists(path)) continue;
					try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
						Gson gson = new GsonBuilder().create();
						ComponentLibrary lib = gson.fromJson(reader, ComponentLibrary.class);
						if (lib != null && lib.components != null && !lib.components.isEmpty()) {
							customs.add(lib);
						}
					}
				} catch (Throwable ignored) {}
			}
		}
		if (!customs.isEmpty()) {
			base = CustomLibraryMerger.merge(base, customs);
		}
		String id = base.id != null ? base.id : "";
		String name = base.name != null ? base.name : "";
		String display = base.displayName != null ? base.displayName : "";
		boolean allowed = enabledLower.contains(id.toLowerCase()) || enabledLower.contains(name.toLowerCase()) || enabledLower.contains(display.toLowerCase());
		if (!allowed) {
			return new UnifiedComponentProvider(createEmptyLibrary(resource));
		}
		return new UnifiedComponentProvider(base);
	}

	@org.jetbrains.annotations.Nullable
	private static com.chu7.vuecomponentassistant.library.model.Component mapComponent(com.chu7.vuecomponentassistant.remote.model.ComponentInfo ci) {
		if (ci == null || ci.getName() == null || ci.getName().isEmpty()) return null;
		com.chu7.vuecomponentassistant.library.model.Component c = new com.chu7.vuecomponentassistant.library.model.Component();
		c.name = ci.getName();
		c.description = ci.getDescription();
		c.version = ci.getVersion();
		c.example = ci.getExample();
		c.docUrl = ci.getDocUrl();
		// props
		if (ci.getProps() != null) {
			java.util.List<com.chu7.vuecomponentassistant.library.model.Prop> props = new java.util.ArrayList<>();
			for (com.chu7.vuecomponentassistant.remote.model.ComponentInfo.ComponentProp p : ci.getProps()) {
				com.chu7.vuecomponentassistant.library.model.Prop mp = new com.chu7.vuecomponentassistant.library.model.Prop();
				mp.name = p.getName();
				mp.type = p.getType();
				mp.description = p.getDescription();
				mp.defaultValue = p.getDefaultValue();
				mp.required = p.isRequired();
				mp.options = p.getOptions();
				props.add(mp);
			}
			c.props = props;
		}
		// events
		if (ci.getEvents() != null) {
			java.util.List<com.chu7.vuecomponentassistant.library.model.Event> events = new java.util.ArrayList<>();
			for (com.chu7.vuecomponentassistant.remote.model.ComponentInfo.ComponentEvent e : ci.getEvents()) {
				com.chu7.vuecomponentassistant.library.model.Event me = new com.chu7.vuecomponentassistant.library.model.Event();
				me.name = e.getName();
				me.description = e.getDescription();
				me.parameters = e.getParameters();
				events.add(me);
			}
			c.events = events;
		}
		// slots
		if (ci.getSlots() != null) {
			java.util.List<com.chu7.vuecomponentassistant.library.model.Slot> slots = new java.util.ArrayList<>();
			for (com.chu7.vuecomponentassistant.remote.model.ComponentInfo.ComponentSlot s : ci.getSlots()) {
				com.chu7.vuecomponentassistant.library.model.Slot ms = new com.chu7.vuecomponentassistant.library.model.Slot();
				ms.name = s.getName();
				ms.description = s.getDescription();
				ms.scope = s.getScope();
				slots.add(ms);
			}
			c.slots = slots;
		}
		return c;
	}

	@NotNull
	private static String detectLibraryResource(@NotNull Project project, String desiredIdOrNull) {
		if (desiredIdOrNull != null) {
			if ("element-ui".equals(desiredIdOrNull)) return "data/element-ui-libraries.json";
			return "data/element-plus-libraries.json";
		}
		// 索引未就绪或 Dumb 模式下，避免索引访问，直接使用默认 element-plus
		if (DumbService.isDumb(project)) {
			return "data/element-plus-libraries.json";
		}
		boolean hasElementPlus = safeIndexCheck(project, () -> hasDependencyIndicativeFile(project, "element-plus") || hasPackageJsonDependency(project, "element-plus"));
		boolean hasElementUi = safeIndexCheck(project, () -> hasDependencyIndicativeFile(project, "element-ui") || hasPackageJsonDependency(project, "element-ui"));
		if (hasElementUi && !hasElementPlus) return "data/element-ui-libraries.json";
		return "data/element-plus-libraries.json";
	}

	private static boolean safeIndexCheck(@NotNull Project project, @NotNull java.util.concurrent.Callable<Boolean> callable) {
		try {
			return ReadAction.compute(() -> {
				try { return callable.call(); } catch (Exception e) { return false; }
			});
		} catch (Throwable ignored) {
			return false;
		}
	}

	private static boolean hasDependencyIndicativeFile(@NotNull Project project, @NotNull String libName) {
		try {
			var scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project);
			var files = com.intellij.psi.search.FilenameIndex.getVirtualFilesByName(project, libName + ".d.ts", scope);
			if (!files.isEmpty()) return true;
			files = com.intellij.psi.search.FilenameIndex.getVirtualFilesByName(project, libName + ".js", scope);
			return !files.isEmpty();
		} catch (Throwable e) {
			return false;
		}
	}

	private static boolean hasPackageJsonDependency(@NotNull Project project, @NotNull String libName) {
		try {
			var scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project);
			var vFiles = com.intellij.psi.search.FilenameIndex.getVirtualFilesByName(project, "package.json", scope);
			for (var vf : vFiles) {
				try (var is = vf.getInputStream(); var reader = new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8)) {
					var json = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
					if (json.has("dependencies") && json.get("dependencies").getAsJsonObject().has(libName)) return true;
					if (json.has("devDependencies") && json.get("devDependencies").getAsJsonObject().has(libName)) return true;
				}
			}
		} catch (Throwable ignored) {}
		return false;
	}
	
	@Nullable
	private static ComponentLibrary loadLibraryFromJson(@NotNull String resourcePath) {
		try {
			InputStream inputStream = ProviderManager.class.getClassLoader()
					.getResourceAsStream(resourcePath);
			if (inputStream == null) {
				return null;
			}
			Gson gson = new GsonBuilder().create();
			ComponentLibrary library = gson.fromJson(
					new java.io.InputStreamReader(inputStream, "UTF-8"),
					ComponentLibrary.class
			);
			return library;
		} catch (Exception e) {
			return null;
		}
	}
	
	@NotNull
	private static ComponentLibrary createEmptyLibrary(@NotNull String resourcePath) {
		ComponentLibrary library = new ComponentLibrary();
		if (resourcePath.contains("element-ui")) {
			library.id = "element-ui";
			library.name = "Element UI";
			library.componentPrefix = "el-";
			library.displayName = "Element UI";
		} else {
			library.id = "element-plus";
			library.name = "Element Plus";
			library.componentPrefix = "el-";
			library.displayName = "Element Plus";
		}
		library.description = "";
		library.version = "";
		library.sourceUrl = "";
		library.lastUpdated = "";
		library.components = java.util.Collections.emptyList();
		return library;
	}
} 