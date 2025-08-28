package com.chu7.vuecomponentassistant.remote.cache;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.OfficialLibrary;
import com.chu7.vuecomponentassistant.utils.ErrorHandler;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存管理器
 */
public class LocalCacheManager {
	private static final Logger LOG = Logger.getInstance(LocalCacheManager.class);
	private static final String CACHE_DIR = "vuekit_cache";
	private static final String LIBRARIES_FILE = "libraries.json";
	private static final String OFFICIAL_LIBRARIES_FILE = "official_libraries.json";
	private static final String COMPONENT_LIBRARY_PREFIX = "library_";
	private static final String COMPONENT_LIBRARY_SUFFIX = ".json";
	private static final long CACHE_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000L;

	private final Path cacheDir;
	private final Map<String, ComponentLibrary> memoryCache;
	private final Map<String, OfficialLibrary> officialLibrariesCache;

	public LocalCacheManager() {
		this.cacheDir = getCacheDirectory();
		this.memoryCache = new ConcurrentHashMap<>();
		this.officialLibrariesCache = new ConcurrentHashMap<>();
		ensureCacheDirectoryExists();
	}

	private Path getCacheDirectory() {
		String userHome = System.getProperty("user.home");
		return Paths.get(userHome, ".vuekit", CACHE_DIR);
	}

	private void ensureCacheDirectoryExists() {
		try {
			if (!Files.exists(cacheDir)) {
				Files.createDirectories(cacheDir);
				LOG.info("创建缓存目录: " + cacheDir);
			}
		} catch (IOException e) {
			String errorMsg = "创建缓存目录失败: " + cacheDir;
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
		}
	}

	public void saveLibrary(ComponentLibrary library) throws VueKitException {
		Objects.requireNonNull(library, "组件库不能为null");
		Objects.requireNonNull(library.getId(), "组件库ID不能为null");
		Objects.requireNonNull(library.getName(), "组件库名称不能为null");
		try {
			String fileName = COMPONENT_LIBRARY_PREFIX + library.getId() + COMPONENT_LIBRARY_SUFFIX;
			Path filePath = cacheDir.resolve(fileName);
			String json = convertLibraryToJson(library);
			Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
			memoryCache.put(library.getId(), library);
			LOG.info("保存组件库到缓存: " + library.getName() + " -> " + filePath);
		} catch (IOException e) {
			String errorMsg = "保存组件库到缓存失败: " + library.getName();
			LOG.error(errorMsg, e);
			throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
		} catch (Exception e) {
			String errorMsg = "保存组件库时发生未知错误: " + library.getName();
			LOG.error(errorMsg, e);
			throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
		}
	}

	public ComponentLibrary loadLibrary(String libraryId) throws VueKitException {
		if (libraryId == null || libraryId.trim().isEmpty()) {
			throw new IllegalArgumentException("组件库ID不能为null或空字符串");
		}
		if (memoryCache.containsKey(libraryId)) {
			LOG.debug("从内存缓存加载组件库: " + libraryId);
			return memoryCache.get(libraryId);
		}
		try {
			String fileName = COMPONENT_LIBRARY_PREFIX + libraryId + COMPONENT_LIBRARY_SUFFIX;
			Path filePath = cacheDir.resolve(fileName);
			if (!Files.exists(filePath)) { LOG.debug("组件库缓存文件不存在: " + libraryId); return null; }
			String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
			ComponentLibrary library = convertJsonToLibrary(json);
			memoryCache.put(libraryId, library);
			LOG.info("从磁盘缓存加载组件库: " + library.getName());
			return library;
		} catch (IOException e) {
			String errorMsg = "从缓存加载组件库失败: " + libraryId;
			LOG.error(errorMsg, e);
			throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
		} catch (Exception e) {
			String errorMsg = "加载组件库时发生未知错误: " + libraryId;
			LOG.error(errorMsg, e);
			throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
		}
	}

	public List<ComponentLibrary> getAllLibraries() {
		LOG.info("=== LocalCacheManager.getAllLibraries() 开始 ===");
		List<ComponentLibrary> libraries = new ArrayList<>();
		try {
			LOG.info("缓存目录: " + cacheDir);
			File[] files = cacheDir.toFile().listFiles((dir, name) -> name.startsWith(COMPONENT_LIBRARY_PREFIX) && name.endsWith(COMPONENT_LIBRARY_SUFFIX));
			LOG.info("找到缓存文件数量: " + (files != null ? files.length : 0));
			if (files != null) {
				for (File file : files) {
					LOG.info("处理缓存文件: " + file.getName());
					try {
						String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
						LOG.info("文件内容长度: " + json.length() + " 字符");
						LOG.info("文件内容前100字符: " + json.substring(0, Math.min(100, json.length())));
						ComponentLibrary library = convertJsonToLibrary(json);
						if (library != null) {
							libraries.add(library);
							memoryCache.put(library.getId(), library);
							LOG.info("成功加载组件库: " + library.getName() + " (ID: " + library.getId() + ")");
						} else {
							LOG.warn("转换组件库失败: " + file.getName());
						}
					} catch (Exception e) {
						LOG.warn("加载缓存文件失败: " + file.getName(), e);
						VueKitLogger.logAndIgnore(LOG, "跳过损坏的缓存文件: " + file.getName(), e);
					}
				}
			}
			LOG.info("成功加载 " + libraries.size() + " 个组件库到内存缓存");
			LOG.info("加载的组件库列表:");
			for (ComponentLibrary library : libraries) {
				LOG.info("- " + library.getName() + " (ID: " + library.getId() + ", 描述: " + library.getDescription() + ")");
			}
		} catch (Exception e) {
			String errorMsg = "获取所有组件库失败";
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
		}
		LOG.info("=== LocalCacheManager.getAllLibraries() 结束，返回 " + libraries.size() + " 个组件库 ===");
		return libraries;
	}

	public boolean removeLibrary(String libraryId) {
		Objects.requireNonNull(libraryId, "组件库ID不能为null");
		try {
			String fileName = COMPONENT_LIBRARY_PREFIX + libraryId + COMPONENT_LIBRARY_SUFFIX;
			Path filePath = cacheDir.resolve(fileName);
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				memoryCache.remove(libraryId);
				LOG.info("删除组件库缓存: " + libraryId);
				return true;
			} else {
				LOG.debug("组件库缓存文件不存在: " + libraryId);
				return false;
			}
		} catch (IOException e) {
			String errorMsg = "删除组件库缓存失败: " + libraryId;
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
			return false;
		}
	}

	public void saveOfficialLibraries(List<OfficialLibrary> libraries) throws VueKitException {
		Objects.requireNonNull(libraries, "官方组件库列表不能为null");
		try {
			Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
			String json = convertOfficialLibrariesToJson(libraries);
			Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
			officialLibrariesCache.clear();
			for (OfficialLibrary library : libraries) {
				if (library != null && library.getId() != null) {
					officialLibrariesCache.put(library.getId(), library);
				}
			}
			LOG.info("保存官方组件库列表，数量: " + libraries.size());
		} catch (IOException e) {
			String errorMsg = "保存官方组件库列表失败";
			LOG.error(errorMsg, e);
			throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
		} catch (Exception e) {
			String errorMsg = "保存官方组件库列表时发生未知错误";
			LOG.error(errorMsg, e);
			throw new VueKitException(errorMsg + ": " + e.getMessage(), e);
		}
	}

	public List<OfficialLibrary> loadOfficialLibraries() {
		try {
			Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
			if (!Files.exists(filePath)) { LOG.debug("官方组件库缓存文件不存在"); return new ArrayList<>(); }
			String json = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
			List<OfficialLibrary> libraries = convertJsonToOfficialLibraries(json);
			officialLibrariesCache.clear();
			for (OfficialLibrary library : libraries) {
				if (library != null && library.getId() != null) { officialLibrariesCache.put(library.getId(), library); }
			}
			LOG.info("加载官方组件库列表，数量: " + libraries.size());
			return libraries;
		} catch (Exception e) {
			String errorMsg = "加载官方组件库列表失败";
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
			return new ArrayList<>();
		}
	}

	public void clearOfficialLibrariesCache() {
		try {
			Path filePath = cacheDir.resolve(OFFICIAL_LIBRARIES_FILE);
			if (Files.exists(filePath)) { Files.delete(filePath); LOG.info("删除官方组件库缓存文件"); }
			officialLibrariesCache.clear();
			LOG.info("清除官方组件库内存缓存");
		} catch (Exception e) {
			String errorMsg = "清除官方组件库缓存失败";
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
		}
	}

	public void cleanExpiredCache() {
		try {
			long currentTime = System.currentTimeMillis();
			int deletedCount = 0;
			File[] files = cacheDir.toFile().listFiles();
			if (files != null) {
				for (File file : files) {
					if (currentTime - file.lastModified() > CACHE_EXPIRY_TIME) {
						Files.delete(file.toPath());
						deletedCount++;
						LOG.info("删除过期缓存文件: " + file.getName());
					}
				}
			}
			LOG.info("缓存清理完成，删除了 " + deletedCount + " 个过期文件");
		} catch (Exception e) {
			String errorMsg = "清理过期缓存失败";
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
		}
	}

	public Map<String, Object> getCacheStats() {
		Map<String, Object> stats = new HashMap<>();
		try {
			File[] files = cacheDir.toFile().listFiles();
			int totalFiles = files != null ? files.length : 0;
			long totalSize = 0;
			if (files != null) { for (File file : files) { totalSize += file.length(); } }
			stats.put("cacheDirectory", cacheDir.toString());
			stats.put("totalFiles", totalFiles);
			stats.put("totalSizeBytes", totalSize);
			stats.put("totalSizeMB", String.format("%.2f", totalSize / (1024.0 * 1024.0)));
			stats.put("memoryCacheSize", memoryCache.size());
			stats.put("officialLibrariesCacheSize", officialLibrariesCache.size());
			stats.put("lastCleanupTime", System.currentTimeMillis());
			LOG.debug("获取缓存统计信息: " + stats);
		} catch (Exception e) {
			String errorMsg = "获取缓存统计信息失败";
			LOG.error(errorMsg, e);
			ErrorHandler.handleException(errorMsg, e, false);
		}
		return stats;
	}

	public void refreshMemoryCache() {
		memoryCache.clear();
		officialLibrariesCache.clear();
		LOG.info("内存缓存已刷新");
	}

	private String convertLibraryToJson(ComponentLibrary library) {
		Objects.requireNonNull(library, "组件库不能为null");
		try { Gson gson = new GsonBuilder().setPrettyPrinting().create(); return gson.toJson(library); }
		catch (Exception e) { String errorMsg = "序列化组件库失败: " + library.getName(); LOG.error(errorMsg, e); throw new RuntimeException(errorMsg + ": " + e.getMessage(), e); }
	}

	private ComponentLibrary convertJsonToLibrary(String json) {
		Objects.requireNonNull(json, "JSON字符串不能为null");
		try { Gson gson = new Gson(); return gson.fromJson(json, ComponentLibrary.class); }
		catch (Exception e) { String errorMsg = "反序列化组件库失败"; LOG.error(errorMsg, e); throw new RuntimeException(errorMsg + ": " + e.getMessage(), e); }
	}

	private String convertOfficialLibrariesToJson(List<OfficialLibrary> libraries) {
		Objects.requireNonNull(libraries, "官方组件库列表不能为null");
		try { Gson gson = new GsonBuilder().setPrettyPrinting().create(); return gson.toJson(libraries); }
		catch (Exception e) { String errorMsg = "序列化官方组件库列表失败"; LOG.error(errorMsg, e); throw new RuntimeException(errorMsg + ": " + e.getMessage(), e); }
	}

	private List<OfficialLibrary> convertJsonToOfficialLibraries(String json) {
		Objects.requireNonNull(json, "JSON字符串不能为null");
		try { Gson gson = new Gson(); Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType(); return gson.fromJson(json, listType); }
		catch (Exception e) { String errorMsg = "反序列化官方组件库列表失败"; LOG.error(errorMsg, e); return new ArrayList<>(); }
	}
} 