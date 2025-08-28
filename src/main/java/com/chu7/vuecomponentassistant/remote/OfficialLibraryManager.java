package com.chu7.vuecomponentassistant.remote;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.OfficialLibrary;
import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OfficialLibraryManager {
	private static final Logger LOG = Logger.getInstance(OfficialLibraryManager.class);
	private static final String DEFAULT_OFFICIAL_REGISTRY_URL = "https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json";
	private final LocalCacheManager cacheManager;
	private final RemoteLibraryManager remoteManager;
	public OfficialLibraryManager() { this.cacheManager = new LocalCacheManager(); this.remoteManager = new RemoteLibraryManager(); }

	public CompletableFuture<List<OfficialLibrary>> fetchOfficialLibraries() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				LOG.info("开始获取官方组件库列表（实时刷新）");
				String registryUrl = getOfficialRegistryUrl();
				String json = HttpClient.downloadJson(registryUrl);
				List<OfficialLibrary> libraries = parseOfficialLibrariesJson(json);
				LOG.info("官方组件库列表获取完成，数量: " + libraries.size());
				return libraries;
			} catch (Exception e) { LOG.error("获取官方组件库列表失败", e); return new java.util.ArrayList<>(); }
		});
	}

	public CompletableFuture<ComponentLibrary> downloadOfficialLibrary(String libraryId) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				LOG.info("开始下载官方组件库: " + libraryId);
				List<OfficialLibrary> officialLibraries = fetchOfficialLibraries().get();
				OfficialLibrary officialLibrary = officialLibraries.stream().filter(lib -> lib.getId().equals(libraryId)).findFirst().orElseThrow(() -> new VueKitException("未找到官方组件库: " + libraryId));
				ComponentLibrary library = remoteManager.downloadLibrary(officialLibrary.getDownloadUrl()).get();
				library.setId(officialLibrary.getId());
				library.setName(officialLibrary.getName());
				library.setDisplayName(officialLibrary.getDisplayName());
				library.setDescription(officialLibrary.getDescription());
				library.setVersion(officialLibrary.getVersion());
				LOG.info("设置官方组件库信息完成");
				LOG.info("官方组件库下载完成: " + library.getName());
				return library;
			} catch (Exception e) { LOG.error("下载官方组件库失败: " + libraryId, e); throw new RuntimeException("下载官方组件库失败: " + e.getMessage(), e); }
		});
	}

	public CompletableFuture<List<OfficialLibrary>> searchOfficialLibraries(String keyword) {
		return fetchOfficialLibraries().thenApply(libraries -> {
			if (keyword == null || keyword.trim().isEmpty()) { return libraries; }
			String lowerKeyword = keyword.toLowerCase();
			return libraries.stream().filter(library -> library.getName().toLowerCase().contains(lowerKeyword) || library.getDisplayName().toLowerCase().contains(lowerKeyword) || library.getDescription().toLowerCase().contains(lowerKeyword) || (library.getTags() != null && library.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword)))).collect(Collectors.toList());
		});
	}

	public CompletableFuture<List<OfficialLibrary>> filterOfficialLibrariesByCategory(String category) {
		return fetchOfficialLibraries().thenApply(libraries -> {
			if (category == null || category.trim().isEmpty() || "全部".equals(category)) { return libraries; }
			return libraries.stream().filter(library -> category.equals(library.getCategory())).collect(Collectors.toList());
		});
	}

	public CompletableFuture<List<OfficialLibrary>> filterOfficialLibrariesByFramework(String framework) {
		return fetchOfficialLibraries().thenApply(libraries -> {
			if (framework == null || framework.trim().isEmpty() || "全部".equals(framework)) { return libraries; }
			return libraries.stream().filter(library -> framework.equals(library.getFramework())).collect(Collectors.toList());
		});
	}

	public CompletableFuture<List<String>> getAllCategories() {
		return fetchOfficialLibraries().thenApply(libraries -> libraries.stream().map(OfficialLibrary::getCategory).distinct().sorted().collect(Collectors.toList()));
	}

	public CompletableFuture<List<String>> getAllFrameworks() {
		return fetchOfficialLibraries().thenApply(libraries -> libraries.stream().map(OfficialLibrary::getFramework).distinct().sorted().collect(Collectors.toList()));
	}

	public boolean isOfficialLibraryInstalled(String libraryId) {
		try { ComponentLibrary library = cacheManager.loadLibrary(libraryId); return library != null && "OFFICIAL".equals(library.getSource()); }
		catch (VueKitException e) { LOG.warn("检查官方组件库安装状态失败: " + libraryId, e); return false; }
	}

	public List<OfficialLibrary> getInstalledOfficialLibraries() {
		try { List<OfficialLibrary> allOfficialLibraries = fetchOfficialLibraries().get(); return allOfficialLibraries.stream().filter(library -> isOfficialLibraryInstalled(library.getId())).collect(Collectors.toList()); }
		catch (Exception e) { LOG.error("获取已安装的官方组件库失败", e); return new java.util.ArrayList<>(); }
	}

	public CompletableFuture<List<OfficialLibrary>> refreshOfficialLibraries() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				LOG.info("开始刷新官方组件库列表");
				try { cacheManager.clearOfficialLibrariesCache(); } catch (Exception e) { LOG.warn("清除官方组件库缓存失败", e); }
				String registryUrl = getOfficialRegistryUrl();
				String json = HttpClient.downloadJson(registryUrl);
				List<OfficialLibrary> libraries = parseOfficialLibrariesJson(json);
				if (!libraries.isEmpty()) { cacheManager.saveOfficialLibraries(libraries); }
				LOG.info("官方组件库列表刷新完成，数量: " + libraries.size());
				return libraries;
			} catch (Exception e) { LOG.error("刷新官方组件库列表失败", e); return new java.util.ArrayList<>(); }
		});
	}

	private List<OfficialLibrary> parseOfficialLibrariesJson(String json) throws VueKitException {
		try { Gson gson = new Gson(); Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType(); List<OfficialLibrary> libraries = gson.fromJson(json, listType); if (libraries == null) { LOG.warn("解析官方组件库JSON返回null，返回空列表"); return new java.util.ArrayList<>(); } LOG.info("成功解析官方组件库JSON，数量: " + libraries.size()); return libraries; }
		catch (Exception e) { LOG.error("解析官方组件库JSON失败，返回空列表", e); return new java.util.ArrayList<>(); }
	}

	private String getOfficialRegistryUrl() {
		return DEFAULT_OFFICIAL_REGISTRY_URL;
	}
} 