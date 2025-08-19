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

/**
 * 官方组件库管理器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理VueKit官方维护的组件库列表</li>
 *   <li>提供官方组件库的搜索、筛选和下载功能</li>
 *   <li>支持按分类、框架等条件筛选组件库</li>
 *   <li>管理官方组件库的安装状态和更新</li>
 *   <li>与远程组件库管理器协同工作</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>异步操作：使用CompletableFuture支持非阻塞操作</li>
 *   <li>实时刷新：默认不使用缓存，确保数据最新</li>
 *   <li>智能筛选：支持多维度的高级筛选功能</li>
 *   <li>错误容错：网络失败时优雅降级处理</li>
 *   <li>配置灵活：支持自定义官方组件库源</li>
 * </ul>
 * 
 * <p>核心功能：</p>
 * <ol>
 *   <li>官方组件库列表获取：从官方源获取最新组件库信息</li>
 *   <li>组件库搜索：支持关键词、分类、框架等多维度搜索</li>
 *   <li>官方组件库下载：下载并安装官方组件库</li>
 *   <li>安装状态管理：跟踪官方组件库的安装状态</li>
 *   <li>列表刷新：强制刷新官方组件库列表</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>官方组件库市场展示</li>
 *   <li>用户搜索和筛选组件库</li>
 *   <li>官方组件库的安装和管理</li>
 *   <li>组件库分类和框架管理</li>
 *   <li>官方组件库的更新检查</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager
 * @see com.chu7.vuecomponentassistant.remote.model.OfficialLibrary
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary
 * @see com.chu7.vuecomponentassistant.remote.utils.HttpClient
 * @see java.util.concurrent.CompletableFuture
 */
public class OfficialLibraryManager {
    
    /**
     * 日志记录器
     * 用于记录官方组件库管理过程中的关键信息和错误
     */
    private static final Logger LOG = Logger.getInstance(OfficialLibraryManager.class);
    
    /**
     * 默认官方组件库注册表URL
     * 当用户配置的URL无效时使用的备用URL
     */
    private static final String DEFAULT_OFFICIAL_REGISTRY_URL = "https://gitee.com/rainsoil/vuekit-repo/raw/master/libraries.json";
    
    /**
     * 本地缓存管理器
     * 负责管理官方组件库的本地缓存和存储
     */
    private final LocalCacheManager cacheManager;
    
    /**
     * 远程组件库管理器
     * 负责实际下载官方组件库文件
     */
    private final RemoteLibraryManager remoteManager;
    
    /**
     * 构造函数
     * 
     * <p>初始化官方组件库管理器，创建必要的依赖组件。</p>
     * 
     * <p>初始化内容：</p>
     * <ul>
     *   <li>创建LocalCacheManager实例</li>
     *   <li>创建RemoteLibraryManager实例</li>
     *   <li>准备缓存和远程下载功能</li>
     * </ul>
     * 
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager
     * @see com.chu7.vuecomponentassistant.remote.RemoteLibraryManager
     */
    public OfficialLibraryManager() {
        this.cacheManager = new LocalCacheManager();
        this.remoteManager = new RemoteLibraryManager();
    }
    
    /**
     * 异步获取官方组件库列表（实时刷新，不使用缓存）
     * 
     * <p>该方法从官方源实时获取最新的组件库列表，确保数据的时效性。
     * 每次调用都会从远程获取最新数据，不使用本地缓存。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>获取配置的官方组件库注册表URL</li>
     *   <li>从远程下载最新的组件库列表JSON</li>
     *   <li>解析JSON为OfficialLibrary对象列表</li>
     *   <li>返回解析后的组件库列表</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>网络错误：返回空列表，不显示默认组件库</li>
     *   <li>JSON解析错误：返回空列表，避免显示错误数据</li>
     *   <li>配置错误：使用默认URL作为备用方案</li>
     * </ul>
     * 
     * @return 包含官方组件库列表的CompletableFuture，失败时返回空列表
     * 
     * @see #getOfficialRegistryUrl()
     * @see #parseOfficialLibrariesJson(String)
     * @see com.chu7.vuecomponentassistant.remote.utils.HttpClient#downloadJson(String)
     */
    public CompletableFuture<List<OfficialLibrary>> fetchOfficialLibraries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始获取官方组件库列表（实时刷新）");
                
                // 直接从远程获取，不使用缓存
                String registryUrl = getOfficialRegistryUrl();
                String json = HttpClient.downloadJson(registryUrl);
                List<OfficialLibrary> libraries = parseOfficialLibrariesJson(json);
                
                LOG.info("官方组件库列表获取完成，数量: " + libraries.size());
                return libraries;
                
            } catch (Exception e) {
                LOG.error("获取官方组件库列表失败", e);
                // 返回空列表，不显示默认组件库
                return new java.util.ArrayList<>();
            }
        });
    }
    
    /**
     * 异步下载官方组件库
     * 
     * <p>该方法根据组件库ID下载指定的官方组件库，包括组件库的完整信息和组件数据。
     * 下载完成后会自动设置官方组件库的相关信息。</p>
     * 
     * <p>下载流程：</p>
     * <ol>
     *   <li>获取官方组件库列表</li>
     *   <li>根据ID查找对应的官方组件库信息</li>
     *   <li>使用远程管理器下载组件库文件</li>
     *   <li>设置官方组件库的元数据信息</li>
     *   <li>返回完整的ComponentLibrary对象</li>
     * </ol>
     * 
     * <p>信息设置：</p>
     * <ul>
     *   <li>ID：使用官方组件库的ID</li>
     *   <li>名称：使用官方组件库的名称</li>
     *   <li>显示名称：使用官方组件库的显示名称</li>
     *   <li>描述：使用官方组件库的描述</li>
     *   <li>版本：使用官方组件库的版本</li>
     * </ul>
     * 
     * @param libraryId 官方组件库的唯一ID，不能为null
     * @return 包含下载结果的CompletableFuture，成功时返回ComponentLibrary对象
     * @throws RuntimeException 如果下载失败
     * @throws VueKitException 如果未找到指定的官方组件库
     * 
     * @see #fetchOfficialLibraries()
     * @see com.chu7.vuecomponentassistant.remote.RemoteLibraryManager#downloadLibrary(String)
     */
    public CompletableFuture<ComponentLibrary> downloadOfficialLibrary(String libraryId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始下载官方组件库: " + libraryId);
                
                // 获取官方组件库信息
                List<OfficialLibrary> officialLibraries = fetchOfficialLibraries().get();
                OfficialLibrary officialLibrary = officialLibraries.stream()
                    .filter(lib -> lib.getId().equals(libraryId))
                    .findFirst()
                    .orElseThrow(() -> new VueKitException("未找到官方组件库: " + libraryId));
                
                // 下载组件库
                ComponentLibrary library = remoteManager.downloadLibrary(officialLibrary.getDownloadUrl()).get();
                
                // 设置官方组件库信息（不直接设置source，让ComponentLibraryManager处理）
                library.setId(officialLibrary.getId());
                library.setName(officialLibrary.getName());
                library.setDisplayName(officialLibrary.getDisplayName());
                library.setDescription(officialLibrary.getDescription());
                library.setVersion(officialLibrary.getVersion());
                LOG.info("设置官方组件库信息完成");
                
                LOG.info("官方组件库下载完成: " + library.getName());
                return library;
                
            } catch (Exception e) {
                LOG.error("下载官方组件库失败: " + libraryId, e);
                throw new RuntimeException("下载官方组件库失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 搜索官方组件库
     * 
     * <p>该方法根据关键词在官方组件库中进行全文搜索，支持多字段匹配。
     * 搜索范围包括组件库名称、显示名称、描述和标签。</p>
     * 
     * <p>搜索策略：</p>
     * <ul>
     *   <li>关键词为空时返回所有组件库</li>
     *   <li>搜索不区分大小写</li>
     *   <li>支持部分匹配和模糊搜索</li>
     *   <li>多字段联合搜索，提高匹配率</li>
     * </ul>
     * 
     * <p>搜索字段：</p>
     * <ol>
     *   <li>组件库名称（name）</li>
     *   <li>显示名称（displayName）</li>
     *   <li>描述信息（description）</li>
     *   <li>标签列表（tags）</li>
     * </ol>
     * 
     * @param keyword 搜索关键词，可以为null或空字符串
     * @return 包含搜索结果的CompletableFuture，匹配的官方组件库列表
     * 
     * @see #fetchOfficialLibraries()
     */
    public CompletableFuture<List<OfficialLibrary>> searchOfficialLibraries(String keyword) {
        return fetchOfficialLibraries().thenApply(libraries -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return libraries;
            }
            
            String lowerKeyword = keyword.toLowerCase();
            return libraries.stream()
                .filter(library -> 
                    library.getName().toLowerCase().contains(lowerKeyword) ||
                    library.getDisplayName().toLowerCase().contains(lowerKeyword) ||
                    library.getDescription().toLowerCase().contains(lowerKeyword) ||
                    (library.getTags() != null && library.getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(lowerKeyword)))
                )
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 按分类筛选官方组件库
     * 
     * <p>该方法根据指定的分类筛选官方组件库，支持"全部"分类显示所有组件库。</p>
     * 
     * <p>筛选逻辑：</p>
     * <ul>
     *   <li>分类为空或"全部"时返回所有组件库</li>
     *   <li>精确匹配分类名称</li>
     *   <li>区分大小写</li>
     * </ul>
     * 
     * @param category 分类名称，可以为null、"全部"或具体分类名
     * @return 包含筛选结果的CompletableFuture，指定分类的官方组件库列表
     * 
     * @see #fetchOfficialLibraries()
     */
    public CompletableFuture<List<OfficialLibrary>> filterOfficialLibrariesByCategory(String category) {
        return fetchOfficialLibraries().thenApply(libraries -> {
            if (category == null || category.trim().isEmpty() || "全部".equals(category)) {
                return libraries;
            }
            
            return libraries.stream()
                .filter(library -> category.equals(library.getCategory()))
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 按框架筛选官方组件库
     * 
     * <p>该方法根据指定的框架筛选官方组件库，支持"全部"框架显示所有组件库。</p>
     * 
     * <p>筛选逻辑：</p>
     * <ul>
     *   <li>框架为空或"全部"时返回所有组件库</li>
     *   <li>精确匹配框架名称</li>
     *   <li>区分大小写</li>
     * </ul>
     * 
     * @param framework 框架名称，可以为null、"全部"或具体框架名
     * @return 包含筛选结果的CompletableFuture，指定框架的官方组件库列表
     * 
     * @see #fetchOfficialLibraries()
     */
    public CompletableFuture<List<OfficialLibrary>> filterOfficialLibrariesByFramework(String framework) {
        return fetchOfficialLibraries().thenApply(libraries -> {
            if (framework == null || framework.trim().isEmpty() || "全部".equals(framework)) {
                return libraries;
            }
            
            return libraries.stream()
                .filter(library -> framework.equals(library.getFramework()))
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 获取所有分类
     * 
     * <p>该方法返回官方组件库中所有可用的分类列表，用于构建分类筛选器。</p>
     * 
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>从所有官方组件库中提取分类信息</li>
     *   <li>去重并排序</li>
     *   <li>返回唯一的分类名称列表</li>
     * </ul>
     * 
     * @return 包含所有分类的CompletableFuture，分类名称列表（已排序）
     * 
     * @see #fetchOfficialLibraries()
     */
    public CompletableFuture<List<String>> getAllCategories() {
        return fetchOfficialLibraries().thenApply(libraries -> 
            libraries.stream()
                .map(OfficialLibrary::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 获取所有框架
     * 
     * <p>该方法返回官方组件库中所有可用的框架列表，用于构建框架筛选器。</p>
     * 
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>从所有官方组件库中提取框架信息</li>
     *   <li>去重并排序</li>
     *   <li>返回唯一的框架名称列表</li>
     * </ul>
     * 
     * @return 包含所有框架的CompletableFuture，框架名称列表（已排序）
     * 
     * @see #fetchOfficialLibraries()
     */
    public CompletableFuture<List<String>> getAllFrameworks() {
        return fetchOfficialLibraries().thenApply(libraries -> 
            libraries.stream()
                .map(OfficialLibrary::getFramework)
                .distinct()
                .sorted()
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 检查官方组件库是否已安装
     * 
     * <p>该方法检查指定的官方组件库是否已经安装在本地系统中。</p>
     * 
     * <p>检查逻辑：</p>
     * <ol>
     *   <li>从本地缓存加载组件库</li>
     *   <li>验证组件库是否存在</li>
     *   <li>检查来源类型是否为"OFFICIAL"</li>
     * </ol>
     * 
     * @param libraryId 官方组件库的唯一ID，不能为null
     * @return 如果官方组件库已安装则返回true，否则返回false
     * 
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#loadLibrary(String)
     */
    public boolean isOfficialLibraryInstalled(String libraryId) {
        try {
            ComponentLibrary library = cacheManager.loadLibrary(libraryId);
            return library != null && "OFFICIAL".equals(library.getSource());
        } catch (VueKitException e) {
            LOG.warn("检查官方组件库安装状态失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 获取已安装的官方组件库
     * 
     * <p>该方法返回所有已经安装在本地系统中的官方组件库列表。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>获取所有官方组件库列表</li>
     *   <li>筛选已安装的官方组件库</li>
     *   <li>返回已安装的官方组件库列表</li>
     * </ol>
     * 
     * @return 已安装的官方组件库列表，如果没有则返回空列表
     * 
     * @see #fetchOfficialLibraries()
     * @see #isOfficialLibraryInstalled(String)
     */
    public List<OfficialLibrary> getInstalledOfficialLibraries() {
        try {
            List<OfficialLibrary> allOfficialLibraries = fetchOfficialLibraries().get();
            return allOfficialLibraries.stream()
                .filter(library -> isOfficialLibraryInstalled(library.getId()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("获取已安装的官方组件库失败", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 刷新官方组件库列表
     * 
     * <p>该方法强制刷新官方组件库列表，清除本地缓存并从远程获取最新数据。
     * 用于确保组件库信息的时效性和准确性。</p>
     * 
     * <p>刷新流程：</p>
     * <ol>
     *   <li>清除官方组件库的本地缓存</li>
     *   <li>强制从远程获取最新数据</li>
     *   <li>更新本地缓存</li>
     *   <li>返回最新的组件库列表</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>缓存清除失败：记录警告但不中断流程</li>
     *   <li>远程获取失败：返回空列表</li>
     *   <li>缓存更新失败：记录错误但不影响返回结果</li>
     * </ul>
     * 
     * @return 包含刷新结果的CompletableFuture，最新的官方组件库列表
     * 
     * @see #getOfficialRegistryUrl()
     * @see #parseOfficialLibrariesJson(String)
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#clearOfficialLibrariesCache()
     * @see com.chu7.vuecomponentassistant.remote.cache.LocalCacheManager#saveOfficialLibraries(List)
     */
    public CompletableFuture<List<OfficialLibrary>> refreshOfficialLibraries() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("开始刷新官方组件库列表");
                
                // 清除缓存
                try {
                    cacheManager.clearOfficialLibrariesCache();
                } catch (Exception e) {
                    LOG.warn("清除官方组件库缓存失败", e);
                }
                
                // 强制从远程获取最新数据
                String registryUrl = getOfficialRegistryUrl();
                String json = HttpClient.downloadJson(registryUrl);
                List<OfficialLibrary> libraries = parseOfficialLibrariesJson(json);
                
                // 更新缓存
                if (!libraries.isEmpty()) {
                    cacheManager.saveOfficialLibraries(libraries);
                }
                
                LOG.info("官方组件库列表刷新完成，数量: " + libraries.size());
                return libraries;
                
            } catch (Exception e) {
                LOG.error("刷新官方组件库列表失败", e);
                // 返回空列表，不显示默认组件库
                return new java.util.ArrayList<>();
            }
        });
    }
    
    // 解析官方组件库JSON
    private List<OfficialLibrary> parseOfficialLibrariesJson(String json) throws VueKitException {
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OfficialLibrary>>(){}.getType();
            List<OfficialLibrary> libraries = gson.fromJson(json, listType);
            
            if (libraries == null) {
                LOG.warn("解析官方组件库JSON返回null，返回空列表");
                return new java.util.ArrayList<>();
            }
            
            LOG.info("成功解析官方组件库JSON，数量: " + libraries.size());
            return libraries;
            
        } catch (Exception e) {
            LOG.error("解析官方组件库JSON失败，返回空列表", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 获取官方组件库注册表URL
     */
    private String getOfficialRegistryUrl() {
        try {
            PluginSettings settings = PluginSettings.getInstance();
            String url = settings.getOfficialLibraryMarketUrl();
            if (url != null && !url.trim().isEmpty()) {
                return url.trim();
            }
        } catch (Exception e) {
            LOG.warn("获取官方组件库市场URL配置失败，使用默认URL", e);
        }
        return DEFAULT_OFFICIAL_REGISTRY_URL;
    }
    
    /**
     * 创建默认官方组件库列表（当远程获取失败时的备用方案）
     * 注意：这个方法现在只作为备用方案，主要依赖远程配置
     */
    private List<OfficialLibrary> createDefaultOfficialLibraries() {
        List<OfficialLibrary> libraries = new java.util.ArrayList<>();
        
        // 当远程配置获取失败时，返回空列表，避免硬编码
        // 这样可以确保所有组件库都来自动态配置
        LOG.warn("远程官方组件库配置获取失败，返回空列表以避免硬编码");
        
        return libraries;
    }
} 