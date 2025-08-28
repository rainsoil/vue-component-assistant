package com.chu7.vuecomponentassistant.remote;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RemoteLibraryManager {
	private static final Logger LOG = Logger.getInstance(RemoteLibraryManager.class);
	private static final String CORE_LIBRARIES_URL = "https://registry.vuekit.dev/core-libraries.json";
	private final LocalCacheManager cacheManager;
	public RemoteLibraryManager() { this.cacheManager = new LocalCacheManager(); }

	public CompletableFuture<List<ComponentLibrary>> downloadCoreLibraries() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				LOG.info("开始下载核心组件库");
				String json = HttpClient.downloadJson(CORE_LIBRARIES_URL);
				List<ComponentLibrary> coreLibraries = parseCoreLibrariesJson(json);
				for (ComponentLibrary library : coreLibraries) { cacheManager.saveLibrary(library); }
				LOG.info("核心组件库下载完成，数量: " + coreLibraries.size());
				return coreLibraries;
			} catch (Exception e) {
				LOG.error("下载核心组件库失败", e);
				throw new RuntimeException("下载核心组件库失败: " + e.getMessage(), e);
			}
		});
	}

	public CompletableFuture<ComponentLibrary> downloadLibrary(String url) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				LOG.info("开始下载组件库: " + url);
				String json = HttpClient.downloadJsonWithRetry(url, 3).get();
				LOG.debug("下载的JSON内容长度: " + (json != null ? json.length() : "null"));
				if (json != null && json.length() > 0) {
					LOG.debug("下载的JSON前200字符: " + json.substring(0, Math.min(200, json.length())));
					LOG.debug("下载的JSON后200字符: " + json.substring(Math.max(0, json.length() - 200)));
				}
				ComponentLibrary library = parseComponentLibraryJson(json);
				cacheManager.saveLibrary(library);
				LOG.info("组件库下载完成: " + library.getName());
				return library;
			} catch (Exception e) {
				LOG.error("下载组件库失败: " + url, e);
				String errorMsg = "下载组件库失败: " + e.getMessage();
				if (e.getCause() != null) { errorMsg += " (原因: " + e.getCause().getMessage() + ")"; }
				throw new RuntimeException(errorMsg, e);
			}
		});
	}

	public CompletableFuture<ComponentLibrary> reloadRemoteLibrary(ComponentLibrary library) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (!"CUSTOM_REMOTE".equals(library.getSource())) { throw new VueKitException("只能重新加载远程自定义组件库"); }
				String sourceUrl = library.getSourceUrl();
				if (sourceUrl == null || sourceUrl.trim().isEmpty()) { throw new VueKitException("组件库没有有效的源URL"); }
				LOG.info("重新加载远程组件库: " + library.getName() + " -> " + sourceUrl);
				String json = HttpClient.downloadJsonWithRetry(sourceUrl, 3).get();
				ComponentLibrary updatedLibrary = parseComponentLibraryJson(json);
				updatedLibrary.setId(library.getId());
				updatedLibrary.setSource(library.getSource());
				updatedLibrary.setSourceUrl(library.getSourceUrl());
				cacheManager.saveLibrary(updatedLibrary);
				LOG.info("远程组件库重新加载完成: " + updatedLibrary.getName());
				return updatedLibrary;
			} catch (Exception e) {
				LOG.error("重新加载远程组件库失败: " + library.getName(), e);
				throw new RuntimeException("重新加载组件库失败: " + e.getMessage(), e);
			}
		});
	}

	public CompletableFuture<Boolean> validateRemoteUrl(String url) { return HttpClient.checkUrlAccessibleAsync(url); }

	public CompletableFuture<ComponentLibrary> previewRemoteLibrary(String url) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				LOG.info("预览远程组件库: " + url);
				String json = HttpClient.downloadJson(url);
				LOG.debug("预览的JSON内容长度: " + (json != null ? json.length() : "null"));
				if (json != null && json.length() > 0) { LOG.debug("预览的JSON前200字符: " + json.substring(0, Math.min(200, json.length()))); }
				ComponentLibrary library = parseComponentLibraryJson(json);
				LOG.info("远程组件库预览完成: " + library.getName());
				return library;
			} catch (Exception e) {
				LOG.error("预览远程组件库失败: " + url, e);
				String errorMsg = "预览组件库失败: " + e.getMessage();
				if (e.getCause() != null) { errorMsg += " (原因: " + e.getCause().getMessage() + ")"; }
				throw new RuntimeException(errorMsg, e);
			}
		});
	}

	public List<ComponentLibrary> getLocalLibraries() { return cacheManager.getAllLibraries(); }
	public ComponentLibrary getLocalLibrary(String libraryId) { try { return cacheManager.loadLibrary(libraryId); } catch (VueKitException e) { LOG.error("获取本地组件库失败: " + libraryId, e); return null; } }
	public boolean removeLocalLibrary(String libraryId) { return cacheManager.removeLibrary(libraryId); }
	public void cleanExpiredCache() { cacheManager.cleanExpiredCache(); }
	public java.util.Map<String, Object> getCacheStats() { return cacheManager.getCacheStats(); }

	private List<ComponentLibrary> parseCoreLibrariesJson(String json) throws VueKitException {
		try {
			Gson gson = new Gson();
			Type listType = new TypeToken<List<ComponentLibrary>>(){}.getType();
			List<ComponentLibrary> libraries = gson.fromJson(json, listType);
			if (libraries == null) { LOG.warn("解析核心组件库JSON返回null，使用默认列表"); return createDefaultCoreLibraries(); }
			LOG.info("成功解析核心组件库JSON，数量: " + libraries.size());
			return libraries;
		} catch (Exception e) { LOG.error("解析核心组件库JSON失败，使用默认列表", e); return createDefaultCoreLibraries(); }
	}

	private ComponentLibrary parseComponentLibraryJson(String json) throws VueKitException {
		try {
			LOG.debug("开始解析组件库JSON，内容长度: " + json.length());
			LOG.debug("JSON内容前500字符: " + json.substring(0, Math.min(500, json.length())));
			if (json == null || json.trim().isEmpty()) { throw new VueKitException("JSON内容为空"); }
			String cleanedJson = json.trim();
			if (cleanedJson.startsWith("\uFEFF")) { cleanedJson = cleanedJson.substring(1); LOG.debug("检测到BOM标记，已移除"); }
			Gson gson = new GsonBuilder().create();
			try {
				ComponentLibrary library = gson.fromJson(cleanedJson, ComponentLibrary.class);
				if (library != null && library.getName() != null && !library.getName().trim().isEmpty()) {
					if (library.getComponents() == null || library.getComponents().isEmpty()) { LOG.warn("组件库缺少组件列表，尝试修复..."); library.setComponents(new java.util.ArrayList<>()); }
					LOG.info("成功解析组件库JSON: " + library.getName());
					return library;
				} else { LOG.warn("解析为单个对象成功但验证失败，library: " + library); }
			} catch (Exception e) { LOG.debug("尝试解析为单个对象失败: " + e.getMessage()); }
			try {
				Type listType = new TypeToken<List<ComponentLibrary>>(){}.getType();
				List<ComponentLibrary> libraries = gson.fromJson(cleanedJson, listType);
				if (libraries != null && !libraries.isEmpty()) {
					ComponentLibrary library = libraries.get(0);
					if (library.getName() == null || library.getName().trim().isEmpty()) { throw new VueKitException("组件库名称不能为空"); }
					if (library.getComponents() == null || library.getComponents().isEmpty()) { LOG.warn("组件库缺少组件列表，尝试修复..."); library.setComponents(new java.util.ArrayList<>()); }
					LOG.info("成功从数组中解析组件库JSON: " + library.getName());
					return library;
				} else { LOG.warn("解析为数组成功但为空或null"); }
			} catch (Exception e) { LOG.debug("尝试解析为数组失败: " + e.getMessage()); }
			try {
				com.google.gson.JsonElement element = gson.fromJson(cleanedJson, com.google.gson.JsonElement.class);
				if (element != null) {
					LOG.error("JSON结构分析 - 类型: " + element.getClass().getSimpleName());
					if (element.isJsonObject()) { LOG.error("JSON是对象，但解析失败。对象键: " + element.getAsJsonObject().keySet()); }
					else if (element.isJsonArray()) { LOG.error("JSON是数组，但解析失败。数组大小: " + element.getAsJsonArray().size()); }
					else if (element.isJsonPrimitive()) { LOG.error("JSON是基本类型: " + element.getAsString()); }
				}
			} catch (Exception e) { LOG.error("无法分析JSON结构: " + e.getMessage()); }
			try {
				LOG.debug("尝试手动解析JSON...");
				if (cleanedJson.contains("\"id\"") && cleanedJson.contains("\"name\"") && cleanedJson.contains("\"components\"")) {
					String fixedJson = cleanedJson.replaceAll(",\s*}", "}").replaceAll(",\s*]", "]").replaceAll("\s+", " ").trim();
					LOG.debug("尝试使用修复后的JSON重新解析...");
					ComponentLibrary library = gson.fromJson(fixedJson, ComponentLibrary.class);
					if (library != null && library.getName() != null) { LOG.info("使用修复后的JSON成功解析组件库: " + library.getName()); return library; }
				}
			} catch (Exception e) { LOG.debug("手动解析失败: " + e.getMessage()); }
			try {
				LOG.debug("尝试从截断的JSON中提取基本信息...");
				if (cleanedJson.contains("\"id\"") && cleanedJson.contains("\"name\"")) {
					int lastComponentEnd = cleanedJson.lastIndexOf("}");
					if (lastComponentEnd > 0) {
						int componentsStart = cleanedJson.indexOf("\"components\"");
						if (componentsStart > 0) {
							int componentsEnd = cleanedJson.indexOf("]", componentsStart);
							if (componentsEnd > 0) {
								String simplifiedJson = cleanedJson.substring(0, componentsEnd + 1) + "}";
								LOG.debug("构建的简化JSON长度: " + simplifiedJson.length());
								ComponentLibrary library = gson.fromJson(simplifiedJson, ComponentLibrary.class);
								if (library != null && library.getName() != null) { LOG.info("使用简化的JSON成功解析组件库: " + library.getName()); return library; }
							}
						}
					}
				}
			} catch (Exception e) { LOG.debug("从截断JSON提取信息失败: " + e.getMessage()); }
			throw new VueKitException("解析组件库JSON失败: 无法识别的结构");
		} catch (Exception e) { throw new VueKitException("解析组件库JSON失败: " + e.getMessage()); }
	}

	private List<ComponentLibrary> createDefaultCoreLibraries() {
		List<ComponentLibrary> libraries = new java.util.ArrayList<>();
		try {
			com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
			java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
			if (installedLibraries != null && !installedLibraries.isEmpty()) {
				for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
					ComponentLibrary remoteLib = new ComponentLibrary(
						library.getName(),
						library.getDisplayName(),
						library.getDisplayName(),
						library.getDescription() != null ? library.getDescription() : "Vue 组件库",
						library.getVersion() != null ? library.getVersion() : "1.0.0",
						"OFFICIAL",
						""
					);
					libraries.add(remoteLib);
				}
				LOG.info("从已安装的组件库创建默认列表，共 " + libraries.size() + " 个");
				return libraries;
			}
		} catch (Exception e) { LOG.warn("获取已安装组件库失败，使用空列表: " + e.getMessage()); }
		LOG.info("没有找到已安装的组件库，返回空列表");
		return libraries;
	}
} 