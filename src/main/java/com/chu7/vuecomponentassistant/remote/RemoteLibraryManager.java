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

/**
 * 远程组件库管理器
 */
public class RemoteLibraryManager {
    private static final Logger LOG = Logger.getInstance(RemoteLibraryManager.class);
    private static final String CORE_LIBRARIES_URL = "https://registry.vuekit.dev/core-libraries.json";

    private final LocalCacheManager cacheManager;

    public RemoteLibraryManager() {
        this.cacheManager = new LocalCacheManager();
    }

    /**
     * 异步下载核心组件库
     */
    public CompletableFuture<List<ComponentLibrary>> downloadCoreLibraries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始下载核心组件库");

                // 下载核心组件库列表
                String json = HttpClient.downloadJson(CORE_LIBRARIES_URL);
                List<ComponentLibrary> coreLibraries = parseCoreLibrariesJson(json);

                // 保存到缓存
                for (ComponentLibrary library : coreLibraries) {
                    cacheManager.saveLibrary(library);
                }

                LOG.info("核心组件库下载完成，数量: " + coreLibraries.size());
                return coreLibraries;

            } catch (Exception e) {
                LOG.error("下载核心组件库失败", e);
                throw new RuntimeException("下载核心组件库失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 异步下载指定组件库
     */
    public CompletableFuture<ComponentLibrary> downloadLibrary(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始下载组件库: " + url);

                // 下载组件库JSON
                String json = HttpClient.downloadJsonWithRetry(url, 3).get();

                // 记录下载的JSON内容用于调试
                LOG.debug("下载的JSON内容长度: " + (json != null ? json.length() : "null"));
                if (json != null && json.length() > 0) {
                    LOG.debug("下载的JSON前200字符: " + json.substring(0, Math.min(200, json.length())));
                    LOG.debug("下载的JSON后200字符: " + json.substring(Math.max(0, json.length() - 200)));
                }

                ComponentLibrary library = parseComponentLibraryJson(json);

                // 保存到缓存
                cacheManager.saveLibrary(library);

                LOG.info("组件库下载完成: " + library.getName());
                return library;

            } catch (Exception e) {
                LOG.error("下载组件库失败: " + url, e);
                // 提供更详细的错误信息
                String errorMsg = "下载组件库失败: " + e.getMessage();
                if (e.getCause() != null) {
                    errorMsg += " (原因: " + e.getCause().getMessage() + ")";
                }
                throw new RuntimeException(errorMsg, e);
            }
        });
    }

    /**
     * 重新加载远程组件库
     */
    public CompletableFuture<ComponentLibrary> reloadRemoteLibrary(ComponentLibrary library) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!"CUSTOM_REMOTE".equals(library.getSource())) {
                    throw new VueKitException("只能重新加载远程自定义组件库");
                }

                String sourceUrl = library.getSourceUrl();
                if (sourceUrl == null || sourceUrl.trim().isEmpty()) {
                    throw new VueKitException("组件库没有有效的源URL");
                }

                LOG.info("重新加载远程组件库: " + library.getName() + " -> " + sourceUrl);

                // 重新下载
                String json = HttpClient.downloadJsonWithRetry(sourceUrl, 3).get();
                ComponentLibrary updatedLibrary = parseComponentLibraryJson(json);

                // 保持原有的ID和来源信息
                updatedLibrary.setId(library.getId());
                updatedLibrary.setSource(library.getSource());
                updatedLibrary.setSourceUrl(library.getSourceUrl());

                // 保存到缓存
                cacheManager.saveLibrary(updatedLibrary);

                LOG.info("远程组件库重新加载完成: " + updatedLibrary.getName());
                return updatedLibrary;

            } catch (Exception e) {
                LOG.error("重新加载远程组件库失败: " + library.getName(), e);
                throw new RuntimeException("重新加载组件库失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 验证远程URL
     */
    public CompletableFuture<Boolean> validateRemoteUrl(String url) {
        return HttpClient.checkUrlAccessibleAsync(url);
    }

    /**
     * 预览远程组件库
     */
    public CompletableFuture<ComponentLibrary> previewRemoteLibrary(String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("预览远程组件库: " + url);

                // 下载并解析，但不保存到缓存
                String json = HttpClient.downloadJson(url);

                // 记录预览的JSON内容用于调试
                LOG.debug("预览的JSON内容长度: " + (json != null ? json.length() : "null"));
                if (json != null && json.length() > 0) {
                    LOG.debug("预览的JSON前200字符: " + json.substring(0, Math.min(200, json.length())));
                }

                ComponentLibrary library = parseComponentLibraryJson(json);

                LOG.info("远程组件库预览完成: " + library.getName());
                return library;

            } catch (Exception e) {
                LOG.error("预览远程组件库失败: " + url, e);
                // 提供更详细的错误信息
                String errorMsg = "预览组件库失败: " + e.getMessage();
                if (e.getCause() != null) {
                    errorMsg += " (原因: " + e.getCause().getMessage() + ")";
                }
                throw new RuntimeException(errorMsg, e);
            }
        });
    }

    /**
     * 获取本地缓存的所有组件库
     */
    public List<ComponentLibrary> getLocalLibraries() {
        return cacheManager.getAllLibraries();
    }

    /**
     * 从本地缓存获取组件库
     */
    public ComponentLibrary getLocalLibrary(String libraryId) {
        try {
            return cacheManager.loadLibrary(libraryId);
        } catch (VueKitException e) {
            LOG.error("获取本地组件库失败: " + libraryId, e);
            return null;
        }
    }

    /**
     * 删除本地组件库
     */
    public boolean removeLocalLibrary(String libraryId) {
        return cacheManager.removeLibrary(libraryId);
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
        cacheManager.cleanExpiredCache();
    }

    /**
     * 获取缓存统计信息
     */
    public java.util.Map<String, Object> getCacheStats() {
        return cacheManager.getCacheStats();
    }

    // 解析核心组件库JSON
    private List<ComponentLibrary> parseCoreLibrariesJson(String json) throws VueKitException {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ComponentLibrary>>() {
            }.getType();
            List<ComponentLibrary> libraries = gson.fromJson(json, listType);

            if (libraries == null) {
                LOG.warn("解析核心组件库JSON返回null，使用默认列表");
                return createDefaultCoreLibraries();
            }

            LOG.info("成功解析核心组件库JSON，数量: " + libraries.size());
            return libraries;

        } catch (Exception e) {
            LOG.error("解析核心组件库JSON失败，使用默认列表", e);
            return createDefaultCoreLibraries();
        }
    }

    // 解析组件库JSON
    private ComponentLibrary parseComponentLibraryJson(String json) throws VueKitException {
        try {
            // 记录原始JSON内容用于调试
            LOG.debug("开始解析组件库JSON，内容长度: " + json.length());
            LOG.debug("JSON内容前500字符: " + json.substring(0, Math.min(500, json.length())));

            // 检查JSON是否为空或null
            if (json == null || json.trim().isEmpty()) {
                throw new VueKitException("JSON内容为空");
            }

            // 尝试清理JSON内容（移除可能的BOM和特殊字符）
            String cleanedJson = json.trim();
            if (cleanedJson.startsWith("\uFEFF")) {
                cleanedJson = cleanedJson.substring(1);
                LOG.debug("检测到BOM标记，已移除");
            }

            Gson gson = new GsonBuilder()
                    .create();

            // 首先尝试解析为单个对象
            try {
                ComponentLibrary library = gson.fromJson(cleanedJson, ComponentLibrary.class);

                if (library != null && library.getName() != null && !library.getName().trim().isEmpty()) {
                    // 验证必要字段
                    if (library.getComponents() == null || library.getComponents().isEmpty()) {
                        LOG.warn("组件库缺少组件列表，尝试修复...");
                        // 尝试创建一个空的组件列表而不是直接失败
                        library.setComponents(new java.util.ArrayList<>());
                    }

                    LOG.info("成功解析组件库JSON: " + library.getName());
                    return library;
                } else {
                    LOG.warn("解析为单个对象成功但验证失败，library: " + library);
                }
            } catch (Exception e) {
                LOG.debug("尝试解析为单个对象失败: " + e.getMessage());
                LOG.debug("错误详情: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            // 如果单个对象解析失败，尝试解析为数组并取第一个元素
            try {
                Type listType = new TypeToken<List<ComponentLibrary>>() {
                }.getType();
                List<ComponentLibrary> libraries = gson.fromJson(cleanedJson, listType);

                if (libraries != null && !libraries.isEmpty()) {
                    ComponentLibrary library = libraries.get(0);

                    // 验证必要字段
                    if (library.getName() == null || library.getName().trim().isEmpty()) {
                        throw new VueKitException("组件库名称不能为空");
                    }

                    if (library.getComponents() == null || library.getComponents().isEmpty()) {
                        LOG.warn("组件库缺少组件列表，尝试修复...");
                        library.setComponents(new java.util.ArrayList<>());
                    }

                    LOG.info("成功从数组中解析组件库JSON: " + library.getName());
                    return library;
                } else {
                    LOG.warn("解析为数组成功但为空或null");
                }
            } catch (Exception e) {
                LOG.debug("尝试解析为数组失败: " + e.getMessage());
                LOG.debug("错误详情: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            // 尝试解析为JsonElement来获取更详细的错误信息
            try {
                com.google.gson.JsonElement element = gson.fromJson(cleanedJson, com.google.gson.JsonElement.class);
                if (element != null) {
                    LOG.error("JSON结构分析 - 类型: " + element.getClass().getSimpleName());
                    if (element.isJsonObject()) {
                        LOG.error("JSON是对象，但解析失败。对象键: " + element.getAsJsonObject().keySet());
                    } else if (element.isJsonArray()) {
                        LOG.error("JSON是数组，但解析失败。数组大小: " + element.getAsJsonArray().size());
                    } else if (element.isJsonPrimitive()) {
                        LOG.error("JSON是基本类型: " + element.getAsString());
                    }
                }
            } catch (Exception e) {
                LOG.error("无法分析JSON结构: " + e.getMessage());
            }

            // 尝试手动解析关键信息
            try {
                LOG.debug("尝试手动解析JSON...");
                if (cleanedJson.contains("\"id\"") && cleanedJson.contains("\"name\"") && cleanedJson.contains("\"components\"")) {
                    LOG.debug("JSON包含必要的字段，可能是格式问题");

                    // 尝试修复常见的JSON格式问题
                    String fixedJson = cleanedJson
                            .replaceAll(",\\s*}", "}") // 移除尾随逗号
                            .replaceAll(",\\s*]", "]") // 移除数组尾随逗号
                            .replaceAll("\\s+", " ") // 规范化空白字符
                            .trim();

                    LOG.debug("尝试使用修复后的JSON重新解析...");
                    ComponentLibrary library = gson.fromJson(fixedJson, ComponentLibrary.class);
                    if (library != null && library.getName() != null) {
                        LOG.info("使用修复后的JSON成功解析组件库: " + library.getName());
                        return library;
                    }
                }
            } catch (Exception e) {
                LOG.debug("手动解析失败: " + e.getMessage());
            }

            // 尝试从截断的JSON中提取基本信息
            try {
                LOG.debug("尝试从截断的JSON中提取基本信息...");
                if (cleanedJson.contains("\"id\"") && cleanedJson.contains("\"name\"")) {
                    // 尝试找到最后一个完整的组件定义
                    int lastComponentEnd = cleanedJson.lastIndexOf("}");
                    if (lastComponentEnd > 0) {
                        // 尝试找到components数组的开始
                        int componentsStart = cleanedJson.indexOf("\"components\"");
                        if (componentsStart > 0) {
                            // 尝试找到components数组的结束
                            int componentsEnd = cleanedJson.indexOf("]", componentsStart);
                            if (componentsEnd > 0) {
                                // 构建一个简化的JSON，只包含基本信息和一个组件
                                String simplifiedJson = cleanedJson.substring(0, componentsEnd + 1) + "}";
                                LOG.debug("构建的简化JSON长度: " + simplifiedJson.length());

                                ComponentLibrary library = gson.fromJson(simplifiedJson, ComponentLibrary.class);
                                if (library != null && library.getName() != null) {
                                    LOG.info("使用简化的JSON成功解析组件库: " + library.getName());
                                    return library;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.debug("从截断JSON提取信息失败: " + e.getMessage());
            }

            // 提供更详细的错误信息
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append("无法解析组件库JSON，既不是有效的单个对象也不是数组格式。请检查JSON格式是否正确。\n");
            errorDetails.append("JSON长度: ").append(json.length()).append("\n");
            errorDetails.append("前200字符: ").append(json.substring(0, Math.min(200, json.length()))).append("\n");
            errorDetails.append("后200字符: ").append(json.substring(Math.max(0, json.length() - 200))).append("\n");

            // 检查JSON是否被截断
            if (!json.trim().endsWith("}")) {
                errorDetails.append("警告: JSON可能被截断，末尾不是 '}'\n");
            }

            // 检查是否包含必要的字段
            if (!json.contains("\"id\"")) {
                errorDetails.append("错误: 缺少 'id' 字段\n");
            }
            if (!json.contains("\"name\"")) {
                errorDetails.append("错误: 缺少 'name' 字段\n");
            }
            if (!json.contains("\"components\"")) {
                errorDetails.append("错误: 缺少 'components' 字段\n");
            }

            // 检查JSON结构
            int openBraces = 0, closeBraces = 0;
            int openBrackets = 0, closeBrackets = 0;
            for (char c : json.toCharArray()) {
                if (c == '{') openBraces++;
                else if (c == '}') closeBraces++;
                else if (c == '[') openBrackets++;
                else if (c == ']') closeBrackets++;
            }
            errorDetails.append("JSON结构分析:\n");
            errorDetails.append("  大括号: {=").append(openBraces).append(", }=").append(closeBraces).append("\n");
            errorDetails.append("  方括号: [=").append(openBrackets).append(", ]=").append(closeBrackets).append("\n");

            if (openBraces != closeBraces) {
                errorDetails.append("  警告: 大括号不匹配\n");
            }
            if (openBrackets != closeBrackets) {
                errorDetails.append("  警告: 方括号不匹配\n");
            }

            throw new VueKitException(errorDetails.toString());

        } catch (Exception e) {
            throw new VueKitException("解析组件库JSON失败: " + e.getMessage());
        }
    }

    // 创建默认核心组件库列表
    private List<ComponentLibrary> createDefaultCoreLibraries() {
        List<ComponentLibrary> libraries = new java.util.ArrayList<>();
        
        try {
            // 尝试从远程组件库管理器获取已安装的组件库
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            if (installedLibraries != null && !installedLibraries.isEmpty()) {
                // 如果已有已安装的组件库，直接返回
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
        } catch (Exception e) {
            LOG.warn("获取已安装组件库失败，使用空列表: " + e.getMessage());
        }
        
        // 如果没有已安装的组件库，返回空列表
        LOG.info("没有找到已安装的组件库，返回空列表");
        return libraries;
    }
} 