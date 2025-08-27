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
		PluginSettings settings = PluginSettings.getInstance();
		String desiredId = settings != null ? settings.getSelectedLibraryId() : null;
		String desiredVersion = settings != null ? settings.getSelectedVersion() : null;
		String resource = detectLibraryResource(project, desiredId);
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
		return new UnifiedComponentProvider(base);
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