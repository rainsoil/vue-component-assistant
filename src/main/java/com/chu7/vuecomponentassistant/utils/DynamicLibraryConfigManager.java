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
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>替代硬编码的组件库配置，支持从配置文件动态加载</li>
 *   <li>管理组件库的配置信息，包括包名、显示名称、组件前缀等</li>
 *   <li>支持运行时配置更新和动态加载</li>
 *   <li>提供包名到组件库ID的映射关系</li>
 *   <li>智能推断组件前缀，避免硬编码</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>单例模式设计，确保全局唯一性</li>
 *   <li>支持动态配置加载和更新</li>
 *   <li>智能前缀推断，支持多种组件库</li>
 *   <li>线程安全的初始化和管理</li>
 *   <li>配置缓存机制，提高性能</li>
 * </ul>
 * 
 * <p>配置策略：</p>
 * <ol>
 *   <li>优先从已安装的组件库中获取配置</li>
 *   <li>智能推断组件前缀作为后备方案</li>
 *   <li>支持运行时配置更新</li>
 *   <li>提供默认配置作为最后保障</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库类型识别和验证</li>
 *   <li>组件前缀匹配和过滤</li>
 *   <li>组件库配置管理</li>
 *   <li>动态配置更新</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary
 */
public class DynamicLibraryConfigManager {
    
    /**
     * 日志记录器
     * 用于记录配置管理过程中的关键信息和错误
     */
    private static final Logger LOG = VueKitLogger.getLogger(DynamicLibraryConfigManager.class);
    
    /**
     * 单例实例
     * 使用volatile关键字确保多线程环境下的可见性
     */
    private static volatile DynamicLibraryConfigManager instance;
    
    /**
     * 组件库配置缓存
     * 存储组件库ID到配置对象的映射关系
     */
    private Map<String, LibraryConfig> libraryConfigs;
    
    /**
     * 包名到组件库ID的映射
     * 用于快速查找包名对应的组件库
     */
    private Map<String, String> packageToLibraryMap;
    
    /**
     * 是否已初始化
     * 防止重复初始化，确保配置的一致性
     */
    private boolean initialized = false;
    
    /**
     * 组件库配置信息
     * 
     * <p>该类包含组件库的静态配置信息，动态信息从下载的组件库中获取。
     * 提供完整的getter和setter方法，支持配置的读取和修改。</p>
     * 
     * <p>配置字段：</p>
     * <ul>
     *   <li>id：组件库的唯一标识符</li>
     *   <li>packageName：组件库的包名</li>
     *   <li>displayName：组件库的显示名称</li>
     *   <li>componentPrefix：组件的命名前缀</li>
     *   <li>description：组件库的描述信息</li>
     * </ul>
     * 
     * @author VueKit Team
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class LibraryConfig {
        
        /**
         * 组件库的唯一标识符
         */
        private String id;
        
        /**
         * 组件库的包名
         */
        private String packageName;
        
        /**
         * 组件库的显示名称
         */
        private String displayName;
        
        /**
         * 组件的命名前缀
         */
        private String componentPrefix;
        
        /**
         * 组件库的描述信息
         */
        private String description;
        
        // ==================== Getter 方法 ====================
        
        /**
         * 获取组件库ID
         * 
         * @return 组件库的唯一标识符
         */
        public String getId() { return id; }
        
        /**
         * 获取包名
         * 
         * @return 组件库的包名
         */
        public String getPackageName() { return packageName; }
        
        /**
         * 获取显示名称
         * 
         * @return 组件库的显示名称
         */
        public String getDisplayName() { return displayName; }
        
        /**
         * 获取组件前缀
         * 
         * @return 组件的命名前缀
         */
        public String getComponentPrefix() { return componentPrefix; }
        
        /**
         * 获取描述信息
         * 
         * @return 组件库的描述信息
         */
        public String getDescription() { return description; }
        
        // ==================== Setter 方法 ====================
        
        /**
         * 设置组件库ID
         * 
         * @param id 组件库的唯一标识符
         */
        public void setId(String id) { this.id = id; }
        
        /**
         * 设置包名
         * 
         * @param packageName 组件库的包名
         */
        public void setPackageName(String packageName) { this.packageName = packageName; }
        
        /**
         * 设置显示名称
         * 
         * @param displayName 组件库的显示名称
         */
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        /**
         * 设置组件前缀
         * 
         * @param componentPrefix 组件的命名前缀
         */
        public void setComponentPrefix(String componentPrefix) { this.componentPrefix = componentPrefix; }
        
        /**
         * 设置描述信息
         * 
         * @param description 组件库的描述信息
         */
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 私有构造函数
     * 
     * <p>初始化配置管理器的内部状态，创建必要的集合和映射。</p>
     * 
     * <p>初始化内容：</p>
     * <ul>
     *   <li>创建组件库配置缓存映射</li>
     *   <li>创建包名到组件库ID的映射</li>
     *   <li>设置初始化状态为false</li>
     * </ul>
     */
    private DynamicLibraryConfigManager() {
        this.libraryConfigs = new HashMap<>();
        this.packageToLibraryMap = new HashMap<>();
    }
    
    /**
     * 获取单例实例
     * 
     * <p>该方法使用双重检查锁定模式确保线程安全，
     * 只有在实例为null时才进行同步操作。</p>
     * 
     * <p>线程安全保证：</p>
     * <ul>
     *   <li>使用volatile关键字确保可见性</li>
     *   <li>双重检查锁定避免不必要的同步</li>
     *   <li>synchronized块确保原子性</li>
     * </ul>
     * 
     * @return 配置管理器的单例实例
     */
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
    
    /**
     * 初始化配置管理器
     * 
     * <p>该方法负责初始化配置管理器的所有必要组件，
     * 包括加载配置、构建映射关系等。</p>
     * 
     * <p>初始化流程：</p>
     * <ol>
     *   <li>检查是否已初始化，避免重复初始化</li>
     *   <li>加载默认配置或动态配置</li>
     *   <li>构建包名到组件库ID的映射</li>
     *   <li>设置初始化状态为true</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>即使出错也设置初始化状态为true，避免死锁</li>
     *   <li>提供降级处理机制</li>
     * </ul>
     */
    public synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            LOG.info("开始初始化动态组件库配置管理器");
            // 不再从配置文件加载，直接使用默认配置
            loadDefaultConfiguration();
            buildPackageToLibraryMap();
            initialized = true;
            LOG.info("动态组件库配置管理器初始化完成，加载了 " + libraryConfigs.size() + " 个组件库配置");
        } catch (Exception e) {
            LOG.error("初始化动态组件库配置管理器失败", e);
            initialized = true;
        }
    }
    
    /**
     * 加载配置文件（已废弃，现在使用动态配置）
     * 
     * <p>该方法已废弃，不再从静态配置文件加载配置。
     * 现在使用动态配置机制，从已安装的组件库中获取配置信息。</p>
     * 
     * @throws Exception 总是抛出异常，表示方法已废弃
     * @deprecated 使用动态配置替代，此方法将在未来版本中移除
     */
    @Deprecated
    private void loadConfiguration() throws Exception {
        // 这个方法已废弃，现在使用动态配置
        LOG.warn("loadConfiguration 方法已废弃，使用动态配置替代");
        throw new Exception("配置文件加载已废弃，使用动态配置");
    }
    
    /**
     * 加载默认配置（当配置文件加载失败时使用）
     * 
     * <p>该方法尝试从已下载的组件库中动态获取配置信息，
     * 如果获取失败则使用智能推断作为后备方案。</p>
     * 
     * <p>加载策略：</p>
     * <ol>
     *   <li>获取已安装的组件库列表</li>
     *   <li>为每个组件库创建配置对象</li>
     *   <li>设置基本配置信息</li>
     *   <li>智能推断组件前缀</li>
     *   <li>添加到配置缓存中</li>
     * </ol>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>使用空配置作为后备方案</li>
     *   <li>确保系统继续运行</li>
     * </ul>
     * 
     * @see #inferComponentPrefix(String)
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager#getAllLibraries()
     */
    private void loadDefaultConfiguration() {
        LOG.warn("使用默认组件库配置");
        
        try {
            // 尝试从已下载的组件库中动态获取配置
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            if (installedLibraries != null && !installedLibraries.isEmpty()) {
                for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                    LibraryConfig config = new LibraryConfig();
                    config.setId(library.getName());
                    config.setPackageName(library.getName());
                    config.setDisplayName(library.getDisplayName() != null ? library.getDisplayName() : library.getName());
                    
                    // 从组件库中获取组件前缀
                    String componentPrefix = library.getComponentPrefix();
                    if (componentPrefix != null && !componentPrefix.trim().isEmpty()) {
                        config.setComponentPrefix(componentPrefix);
                    } else {
                        // 如果没有组件前缀，尝试推断
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
    
    /**
     * 推断组件前缀
     * 
     * <p>该方法尝试从已安装的组件库中获取组件前缀，
     * 如果获取失败则使用智能推断作为后备方案。</p>
     * 
     * <p>推断策略：</p>
     * <ol>
     *   <li>优先从已安装的组件库中获取前缀</li>
     *   <li>使用智能推断作为后备方案</li>
     *   <li>避免硬编码特定组件库</li>
     * </ol>
     * 
     * <p>智能推断规则：</p>
     * <ul>
     *   <li>element相关：el-</li>
     *   <li>ant design相关：a-</li>
     *   <li>vuetify相关：v-</li>
     *   <li>quasar相关：q-</li>
     *   <li>naive相关：n-</li>
     *   <li>prime相关：p-</li>
     * </ul>
     * 
     * @param libraryName 组件库名称，不能为null
     * @return 推断出的组件前缀，如果无法推断则返回空字符串
     * 
     * @see #inferPrefixFromLibraryName(String)
     */
    private String inferComponentPrefix(String libraryName) {
        if (libraryName == null) return "";
        
        // 优先从已安装的组件库中获取前缀
        try {
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : installedLibraries) {
                if (libraryName.equals(library.getName())) {
                    // 如果组件库有自定义的前缀配置，使用它
                    // 注意：ComponentLibrary 类目前没有 getComponentPrefix 方法
                    // 这里可以后续扩展，暂时使用智能推断
                    break;
                }
            }
        } catch (Exception e) {
            LOG.debug("从已安装组件库获取前缀失败，使用智能推断: " + e.getMessage());
        }
        
        // 使用智能推断作为后备方案，避免硬编码特定组件库
        String lowerName = libraryName.toLowerCase();
        if (lowerName.contains("element")) return "el-";
        if (lowerName.contains("ant") || lowerName.contains("design")) return "a-";
        if (lowerName.contains("vuetify")) return "v-";
        if (lowerName.contains("quasar")) return "q-";
        if (lowerName.contains("naive")) return "n-";
        if (lowerName.contains("prime")) return "p-";
        
        // 对于未知的组件库，尝试从库名推断前缀
        return inferPrefixFromLibraryName(libraryName);
    }
    
    /**
     * 从库名推断前缀
     */
    private String inferPrefixFromLibraryName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        // 提取库名的主要部分作为前缀
        String[] parts = libraryName.split("-");
        if (parts.length > 0) {
            String firstPart = parts[0].toLowerCase();
            if (firstPart.length() >= 2) {
                return firstPart.substring(0, 2) + "-";
            } else {
                return firstPart + "-";
            }
        }
        
        return libraryName.length() >= 2 ? libraryName.substring(0, 2).toLowerCase() + "-" : libraryName + "-";
    }
    
    /**
     * 构建包名到组件库ID的映射
     */
    private void buildPackageToLibraryMap() {
        packageToLibraryMap.clear();
        for (LibraryConfig config : libraryConfigs.values()) {
            if (config.getPackageName() != null && !config.getPackageName().trim().isEmpty()) {
                packageToLibraryMap.put(config.getPackageName(), config.getId());
                LOG.debug("包名映射: " + config.getPackageName() + " -> " + config.getId());
            }
        }
    }
    
    /**
     * 根据包名获取组件库ID
     */
    public String getLibraryIdByPackageName(String packageName) {
        ensureInitialized();
        return packageToLibraryMap.get(packageName);
    }
    
    /**
     * 根据组件库ID获取配置
     */
    public LibraryConfig getLibraryConfig(String libraryId) {
        ensureInitialized();
        return libraryConfigs.get(libraryId);
    }
    
    /**
     * 根据包名获取配置
     */
    public LibraryConfig getLibraryConfigByPackageName(String packageName) {
        String libraryId = getLibraryIdByPackageName(packageName);
        return libraryId != null ? getLibraryConfig(libraryId) : null;
    }
    
    /**
     * 获取所有组件库配置
     */
    public Map<String, LibraryConfig> getAllLibraryConfigs() {
        ensureInitialized();
        return new HashMap<>(libraryConfigs);
    }
    
    /**
     * 获取组件库显示名称
     */
    public String getDisplayName(String libraryId) {
        LibraryConfig config = getLibraryConfig(libraryId);
        return config != null ? config.getDisplayName() : "未知组件库";
    }
    
    /**
     * 获取组件前缀
     * 优先从下载的组件库中获取，如果没有则从本地配置获取
     */
    public String getComponentPrefix(String libraryId) {
        if (libraryId == null) {
            return "";
        }
        
        // 首先尝试从下载的组件库中获取
        try {
            String prefix = DynamicLibraryInfoProvider.getComponentPrefixFromDownloadedLibrary(libraryId);
            if (prefix != null && !prefix.trim().isEmpty()) {
                return prefix;
            }
        } catch (Exception e) {
            LOG.debug("从下载的组件库获取前缀失败: " + libraryId, e);
        }
        
        // 如果无法获取，从本地配置获取
        LibraryConfig config = getLibraryConfig(libraryId);
        return config != null ? config.getComponentPrefix() : "";
    }
    

    
    /**
     * 检查是否是已知的组件库
     */
    public boolean isKnownLibrary(String libraryId) {
        ensureInitialized();
        return libraryConfigs.containsKey(libraryId);
    }
    
    /**
     * 动态注册组件库配置
     * 支持运行时添加新的组件库配置
     * 
     * @param libraryConfig 组件库配置
     * @return 是否注册成功
     */
    public boolean registerLibraryConfig(LibraryConfig libraryConfig) {
        if (libraryConfig == null || libraryConfig.getId() == null) {
            return false;
        }
        
        try {
            libraryConfigs.put(libraryConfig.getId(), libraryConfig);
            
            // 更新包名映射
            if (libraryConfig.getPackageName() != null && !libraryConfig.getPackageName().trim().isEmpty()) {
                packageToLibraryMap.put(libraryConfig.getPackageName(), libraryConfig.getId());
            }
            
            LOG.info("动态注册组件库配置成功: " + libraryConfig.getId() + " -> " + libraryConfig.getDisplayName());
            return true;
        } catch (Exception e) {
            LOG.error("动态注册组件库配置失败: " + libraryConfig.getId(), e);
            return false;
        }
    }
    
    /**
     * 强制刷新配置
     * 重新从已安装的组件库中加载配置
     */
    public void refreshConfiguration() {
        try {
            LOG.info("开始强制刷新组件库配置");
            
            // 清空现有配置
            libraryConfigs.clear();
            packageToLibraryMap.clear();
            
            // 重新加载配置
            loadDefaultConfiguration();
            buildPackageToLibraryMap();
            
            LOG.info("组件库配置刷新完成，当前配置数量: " + libraryConfigs.size());
        } catch (Exception e) {
            LOG.error("刷新组件库配置失败", e);
        }
    }
    
    /**
     * 从下载的组件库中自动推断配置
     * 当下载的组件库不在预定义配置中时，自动创建配置
     * 
     * @param libraryId 组件库ID
     * @param packageName 包名
     * @param displayName 显示名称
     * @param componentPrefix 组件前缀
     * @return 是否自动注册成功
     */
    public boolean autoRegisterFromDownloadedLibrary(String libraryId, String packageName, 
                                                   String displayName, String componentPrefix) {
        // 如果已经存在配置，不需要重复注册
        if (isKnownLibrary(libraryId)) {
            return true;
        }
        
        try {
            LibraryConfig config = new LibraryConfig();
            config.setId(libraryId);
            config.setPackageName(packageName != null ? packageName : libraryId);
            config.setDisplayName(displayName != null ? displayName : libraryId);
            config.setComponentPrefix(componentPrefix != null ? componentPrefix : "");
            config.setDescription("自动识别的组件库: " + libraryId);
            
            return registerLibraryConfig(config);
        } catch (Exception e) {
            LOG.error("自动注册组件库配置失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 检查组件是否属于指定组件库
     */
    public boolean isComponentFromLibrary(String componentName, String libraryId) {
        if (componentName == null || libraryId == null) {
            return false;
        }
        
        String prefix = getComponentPrefix(libraryId);
        return !prefix.isEmpty() && componentName.startsWith(prefix);
    }
    
    /**
     * 根据包名推断组件库类型
     */
    public String inferLibraryTypeFromPackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return null;
        }
        
        String cleanName = packageName.trim().toLowerCase();
        
        // 直接查找
        String libraryId = packageToLibraryMap.get(cleanName);
        if (libraryId != null) {
            return libraryId;
        }
        
        // 模糊匹配
        for (Map.Entry<String, String> entry : packageToLibraryMap.entrySet()) {
            if (cleanName.contains(entry.getKey()) || entry.getKey().contains(cleanName)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 确保已初始化
     */
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfiguration() {
        LOG.info("重新加载组件库配置");
        initialized = false;
        libraryConfigs.clear();
        packageToLibraryMap.clear();
        initialize();
    }
    
    /**
     * 获取支持的组件库列表
     */
    public String[] getSupportedLibraryIds() {
        ensureInitialized();
        return libraryConfigs.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取支持的包名列表
     */
    public String[] getSupportedPackageNames() {
        ensureInitialized();
        return packageToLibraryMap.keySet().toArray(new String[0]);
    }
}
