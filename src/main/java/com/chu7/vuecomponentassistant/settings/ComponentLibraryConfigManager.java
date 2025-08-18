package com.chu7.vuecomponentassistant.settings;

import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
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
import com.intellij.openapi.project.Project;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * 组件库配置管理器
 *
 * 功能说明：
 * - 管理项目级和全局级组件库配置
 * - 支持组件库启用/禁用开关
 * - 配置导入/导出功能
 * - 自动检测项目依赖的组件库
 * - 配置持久化存储
 *
 * 特性：
 * - 项目级配置：每个项目可以有不同的组件库配置
 * - 全局默认配置：设置默认启用的组件库
 * - 智能检测：自动检测项目使用的组件库
 * - 配置同步：项目配置与全局配置的智能同步
 *
 * @author VueKit Team
 * @version 2.0.0
 */
@Service
@State(
        name = "ComponentLibraryConfigManager",
        storages = @Storage("vuekit-component-library-config.xml")
)
public final class ComponentLibraryConfigManager implements PersistentStateComponent<ComponentLibraryConfigManager.ConfigState> {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigManager.class);

    // 配置文件名
    private static final String PROJECT_CONFIG_FILE = "vuekit-project-config.json";
    private static final String GLOBAL_CONFIG_FILE = "vuekit-libraries.json";

    // 默认启用的组件库 - 现在动态从远程组件库管理器获取
    private static Set<String> DEFAULT_ENABLED_LIBRARIES;
    
    static {
        initializeDefaultLibraries();
    }
    
    /**
     * 动态初始化默认启用的组件库
     */
    private static void initializeDefaultLibraries() {
        try {
            // 从远程组件库管理器获取已安装的组件库
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
            
            // 如果没有找到任何组件库，返回空集合而不是硬编码列表
            if (defaultLibraries.isEmpty()) {
                VueKitLogger.warn(LOG, "未找到任何组件库，使用空集合作为默认配置");
            }
            
            DEFAULT_ENABLED_LIBRARIES = defaultLibraries;
            
        } catch (Exception e) {
            // 出错时使用空集合，而不是硬编码的默认列表
            VueKitLogger.error(LOG, "初始化默认组件库失败，使用空集合", e);
            DEFAULT_ENABLED_LIBRARIES = new HashSet<String>();
        }
    }

    // 项目级配置缓存
    private final Map<String, ProjectConfig> projectConfigs = new ConcurrentHashMap<>();

    // 全局配置
    private GlobalConfig globalConfig;

    // 配置变更监听器
    private final List<ConfigChangeListener> listeners = new ArrayList<>();

    /**
     * 获取组件库配置管理器实例
     *
     * @param project 项目对象
     * @return 配置管理器实例
     */
    public static ComponentLibraryConfigManager getInstance(Project project) {
        return project.getService(ComponentLibraryConfigManager.class);
    }

    /**
     * 获取全局配置管理器实例
     *
     * @return 全局配置管理器实例
     */
    public static ComponentLibraryConfigManager getGlobalInstance() {
        return ApplicationManager.getApplication().getService(ComponentLibraryConfigManager.class);
    }

    /**
     * 构造函数
     */
    public ComponentLibraryConfigManager() {
        loadGlobalConfig();
        initializeProjectListener();
    }

    /**
     * 获取项目启用的组件库名称
     *
     * @param project 项目对象
     * @return 启用的组件库名称集合
     */
    public Set<String> getEnabledLibraryNames(Project project) {
        try {
            String projectId = getProjectId(project);

            // 1. 检查项目级配置
            ProjectConfig projectConfig = getProjectConfig(project);
            if (projectConfig != null) {
                // 如果项目配置存在，直接返回
                Set<String> enabledLibraryNames = projectConfig.getEnabledLibraryNames();
                VueKitLogger.debug(LOG, "使用项目级配置，启用的组件库: " +
                        (enabledLibraryNames.isEmpty() ? "无" : String.join(", ", enabledLibraryNames)));
                return new HashSet<>(enabledLibraryNames);
            }

            // 2. 检查全局配置
            if (globalConfig != null && !globalConfig.getDefaultEnabledLibraryNames().isEmpty()) {
                VueKitLogger.debug(LOG, "使用全局配置，启用的组件库: " +
                        String.join(", ", globalConfig.getDefaultEnabledLibraryNames()));
                return new HashSet<>(globalConfig.getDefaultEnabledLibraryNames());
            }

            // 3. 使用默认配置（空集合）
            VueKitLogger.debug(LOG, "使用默认配置，启用的组件库: 无");
            return new HashSet<>();

        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取项目启用的组件库失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 获取项目启用的组件库（向后兼容）
     *
     * @param project 项目对象
     * @return 启用的组件库集合
     * @deprecated 使用 getEnabledLibraryNames 替代
     */
    @Deprecated
    public Set<ComponentLibraryDetector.LibraryType> getEnabledLibraries(Project project) {
        // 为了向后兼容，返回空集合
        return new HashSet<>();
    }

    /**
     * 设置项目启用的组件库名称
     *
     * @param project 项目对象
     * @param enabledLibraryNames 启用的组件库名称集合
     */
    public void setProjectEnabledLibraryNames(Project project, Set<String> enabledLibraryNames) {
        try {
            String projectId = getProjectId(project);
            ProjectConfig projectConfig = getProjectConfig(project);

            if (projectConfig == null) {
                projectConfig = new ProjectConfig();
                projectConfig.setProjectId(projectId);
                projectConfig.setProjectName(project.getName());
            }

            projectConfig.setEnabledLibraryNames(enabledLibraryNames);
            projectConfigs.put(projectId, projectConfig);

            // 保存项目配置（即使为空集合也要保存，表示用户明确选择不启用任何组件库）
            saveProjectConfig(project, projectConfig);

            // 通知配置变更
            notifyConfigChangedWithNames(project, enabledLibraryNames);

            VueKitLogger.info(LOG, "项目 " + project.getName() + " 的组件库配置已更新: " +
                    (enabledLibraryNames.isEmpty() ? "无" : String.join(", ", enabledLibraryNames)));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "设置项目启用的组件库失败", e);
        }
    }

    /**
     * 设置项目启用的组件库（向后兼容）
     *
     * @param project 项目对象
     * @param enabledLibraries 启用的组件库集合
     * @deprecated 使用 setProjectEnabledLibraryNames 替代
     */
    @Deprecated
    public void setProjectEnabledLibraries(Project project,
                                           Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
        // 为了向后兼容，转换为字符串名称
        Set<String> enabledLibraryNames = new HashSet<>();
        for (ComponentLibraryDetector.LibraryType type : enabledLibraries) {
            enabledLibraryNames.add(type.getPackageName());
        }
        setProjectEnabledLibraryNames(project, enabledLibraryNames);
    }

    /**
     * 设置全局默认启用的组件库
     *
     * @param defaultEnabledLibraries 默认启用的组件库集合
     */
    public void setGlobalDefaultLibraries(Set<String> defaultEnabledLibraries) {
        try {
            if (globalConfig == null) {
                globalConfig = new GlobalConfig();
            }

            globalConfig.setDefaultEnabledLibraryNames(defaultEnabledLibraries);

            // 保存全局配置
            saveGlobalConfig();

            // 通知配置变更
            notifyGlobalConfigChanged(defaultEnabledLibraries);

            VueKitLogger.info(LOG, "全局默认组件库配置已更新: " + String.join(", ", defaultEnabledLibraries));

        } catch (Exception e) {
            VueKitLogger.error(LOG, "设置全局默认组件库失败", e);
        }
    }

    /**
     * 获取全局默认启用的组件库
     *
     * @return 默认启用的组件库集合
     */
    public Set<String> getGlobalDefaultLibraries() {
        if (globalConfig != null && !globalConfig.getDefaultEnabledLibraryNames().isEmpty()) {
            return new HashSet<>(globalConfig.getDefaultEnabledLibraryNames());
        }
        return new HashSet<>(DEFAULT_ENABLED_LIBRARIES);
    }

    /**
     * 启用组件库
     *
     * @param project 项目对象
     * @param libraryType 要启用的组件库类型
     */
    public void enableLibrary(Project project, String libraryType) {
        Set<String> enabledLibraries = getEnabledLibraryNames(project);
        enabledLibraries.add(libraryType);
        setProjectEnabledLibraryNames(project, enabledLibraries);
    }

    /**
     * 禁用组件库
     *
     * @param project 项目对象
     * @param libraryType 要禁用的组件库类型
     */
    public void disableLibrary(Project project, String libraryType) {
        Set<String> enabledLibraries = getEnabledLibraryNames(project);
        enabledLibraries.remove(libraryType);
        setProjectEnabledLibraryNames(project, enabledLibraries);
    }

    /**
     * 检查组件库是否启用
     *
     * @param project 项目对象
     * @param libraryType 组件库类型
     * @return 如果启用则返回 true
     */
    public boolean isLibraryEnabled(Project project, String libraryType) {
        Set<String> enabledLibraries = getEnabledLibraryNames(project);
        return enabledLibraries.contains(libraryType);
    }
    
    /**
     * 获取项目功能开关配置
     *
     * @param project 项目对象
     * @return 项目配置对象
     */
    public ProjectConfig getProjectConfigForFeatures(Project project) {
        return getProjectConfig(project);
    }
    
    /**
     * 设置项目功能开关配置
     *
     * @param project 项目对象
     * @param config 项目配置对象
     */
    public void setProjectConfig(Project project, ProjectConfig config) {
        try {
            String projectId = getProjectId(project);
            projectConfigs.put(projectId, config);
            
            // 保存项目配置
            saveProjectConfig(project, config);
            
            // 通知配置变更
            notifyConfigChanged(project, config.getEnabledLibraryNames());
            
            VueKitLogger.info(LOG, "项目 " + project.getName() + " 的配置已更新");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "设置项目配置失败", e);
        }
    }
    
    /**
     * 获取组件补全开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isComponentCompletionEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableComponentCompletion() : true;
    }
    
    /**
     * 设置组件补全开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setComponentCompletionEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableComponentCompletion(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取属性补全开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isAttributeCompletionEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableAttributeCompletion() : true;
    }
    
    /**
     * 设置属性补全开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setAttributeCompletionEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableAttributeCompletion(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取事件补全开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isEventCompletionEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableEventCompletion() : true;
    }
    
    /**
     * 设置事件补全开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setEventCompletionEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableEventCompletion(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取插槽补全开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isSlotCompletionEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableSlotCompletion() : true;
    }
    
    /**
     * 设置插槽补全开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setSlotCompletionEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableSlotCompletion(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取悬停文档开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isHoverDocumentationEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableHoverDocumentation() : true;
    }
    
    /**
     * 设置悬停文档开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setHoverDocumentationEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableHoverDocumentation(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取右键文档开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isRightClickDocumentationEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableRightClickDocumentation() : true;
    }
    
    /**
     * 设置右键文档开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setRightClickDocumentationEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableRightClickDocumentation(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取缓存开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isCachingEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableCaching() : true;
    }
    
    /**
     * 设置缓存开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setCachingEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableCaching(enabled);
        setProjectConfig(project, config);
    }
    
    /**
     * 获取调试模式开关状态
     *
     * @param project 项目对象
     * @return 如果启用则返回 true
     */
    public boolean isDebugModeEnabled(Project project) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        return config != null ? config.isEnableDebugMode() : false;
    }
    
    /**
     * 设置调试模式开关状态
     *
     * @param project 项目对象
     * @param enabled 是否启用
     */
    public void setDebugModeEnabled(Project project, boolean enabled) {
        ProjectConfig config = getProjectConfigForFeatures(project);
        if (config == null) {
            config = new ProjectConfig();
            config.setProjectId(getProjectId(project));
            config.setProjectName(project.getName());
        }
        config.setEnableDebugMode(enabled);
        setProjectConfig(project, config);
    }

    /**
     * 重置项目配置为全局默认
     *
     * @param project 项目对象
     */
    public void resetToGlobalDefault(Project project) {
        Set<String> globalDefaults = getGlobalDefaultLibraries();
        setProjectEnabledLibraryNames(project, globalDefaults);

        VueKitLogger.info(LOG, "项目 " + project.getName() + " 的组件库配置已重置为全局默认");
    }

    /**
     * 导出项目配置
     *
     * @param project 项目对象
     * @param exportPath 导出路径
     * @return 如果导出成功则返回 true
     */
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

    /**
     * 导入项目配置
     *
     * @param project 项目对象
     * @param importPath 导入路径
     * @return 如果导入成功则返回 true
     */
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

    /**
     * 添加配置变更监听器
     *
     * @param listener 监听器
     */
    public void addConfigChangeListener(ConfigChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 移除配置变更监听器
     *
     * @param listener 监听器
     */
    public void removeConfigChangeListener(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 获取项目配置
     *
     * @param project 项目对象
     * @return 项目配置对象
     */
    private ProjectConfig getProjectConfig(Project project) {
        String projectId = getProjectId(project);

        // 如果缓存中没有，尝试从文件加载
        if (!projectConfigs.containsKey(projectId)) {
            loadProjectConfig(project);
        }

        return projectConfigs.get(projectId);
    }

    /**
     * 获取项目ID
     *
     * @param project 项目对象
     * @return 项目ID
     */
    private String getProjectId(Project project) {
        return project.getLocationHash();
    }

    /**
     * 加载项目配置
     *
     * @param project 项目对象
     */
    private void loadProjectConfig(Project project) {
        try {
            String projectId = getProjectId(project);

            // 检查是否已加载
            if (projectConfigs.containsKey(projectId)) {
                return;
            }

            // 从文件加载配置
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

        /**
     * 从文件加载项目配置
     * 
     * @param project 项目对象
     * @return 项目配置对象
     */
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

        /**
     * 保存项目配置
     * 
     * @param project 项目对象
     * @param config 项目配置
     */
    private void saveProjectConfig(Project project, ProjectConfig config) {
        try {
            String configJson = convertProjectConfigToJson(config);
            VirtualFile ideaDir = getIdeaDirectory(project);
            
            if (ideaDir == null) {
                VueKitLogger.error(LOG, "未找到 .idea 目录，无法保存配置");
                return;
            }
            
            VirtualFile configFile = ideaDir.findChild(PROJECT_CONFIG_FILE);
            
            // 在写操作上下文中执行文件写入
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    VirtualFile targetFile = configFile;
                    if (targetFile == null) {
                        targetFile = ideaDir.createChildData(this, PROJECT_CONFIG_FILE);
                    }
                    targetFile.setBinaryContent(configJson.getBytes(StandardCharsets.UTF_8));
                    VueKitLogger.info(LOG, "项目配置已保存到: " + targetFile.getPath());
                } catch (Exception e) {
                    VueKitLogger.error(LOG, "写入项目配置文件失败", e);
                }
            });
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存项目配置失败", e);
        }
    }

    /**
     * 加载全局配置
     */
    private void loadGlobalConfig() {
        try {
            // 从用户主目录加载全局配置
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

        /**
     * 保存全局配置
     */
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
    
    /**
     * 获取项目的 .idea 目录
     * 
     * @param project 项目对象
     * @return .idea 目录，如果不存在则返回 null
     */
    private VirtualFile getIdeaDirectory(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            VirtualFile ideaDir = projectDir.findChild(".idea");
            
            if (ideaDir != null && ideaDir.exists() && ideaDir.isDirectory()) {
                return ideaDir;
            }
            
            // 如果 .idea 目录不存在，尝试创建它
            ApplicationManager.getApplication().runWriteAction(() -> {
                try {
                    projectDir.createChildDirectory(this, ".idea");
                } catch (Exception e) {
                    VueKitLogger.error(LOG, "创建 .idea 目录失败", e);
                }
            });
            
            // 重新获取 .idea 目录
            ideaDir = projectDir.findChild(".idea");
            if (ideaDir != null && ideaDir.exists() && ideaDir.isDirectory()) {
                return ideaDir;
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取 .idea 目录失败", e);
        }
        
        return null;
    }

    /**
     * 初始化项目监听器
     * 使用现代的 StartupActivity 替代弃用的 ProjectManagerListener
     */
    private void initializeProjectListener() {
        // 注册项目服务监听器，当项目打开时自动加载配置
        // 这种方式比弃用的 ProjectManagerListener 更现代和可靠
        VueKitLogger.info(LOG, "初始化项目配置监听器（使用现代API）");
    }

    /**
     * 通知配置变更
     *
     * @param project 项目对象
     * @param enabledLibraries 启用的组件库
     */
    private void notifyConfigChanged(Project project, Set<String> enabledLibraries) {
        // 通知配置变更监听器
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onProjectConfigChanged(project, enabledLibraries);
            } catch (Exception e) {
                VueKitLogger.error(LOG, "通知配置变更监听器失败", e);
            }
        }

        // 通知 ComponentProvider 重新加载组件数据
        try {
            ComponentProviderManager.notifyProviderReload(project);
            VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e);
        }
    }

    /**
     * 通知配置变更（使用组件库名称）
     *
     * @param project 项目对象
     * @param enabledLibraryNames 启用的组件库名称
     */
    private void notifyConfigChangedWithNames(Project project, Set<String> enabledLibraryNames) {
        // 通知 ComponentProvider 重新加载组件数据
        try {
            ComponentProviderManager.notifyProviderReload(project);
            VueKitLogger.info(LOG, "已通知 ComponentProvider 重新加载组件数据");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "通知 ComponentProvider 重新加载失败", e);
        }
    }

    /**
     * 通知全局配置变更
     *
     * @param defaultEnabledLibraries 默认启用的组件库
     */
    private void notifyGlobalConfigChanged(Set<String> defaultEnabledLibraries) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onGlobalConfigChanged(defaultEnabledLibraries);
            } catch (Exception e) {
                VueKitLogger.error(LOG, "通知全局配置变更监听器失败", e);
            }
        }
    }

    /**
     * 通知全局配置变更（使用组件库名称）
     *
     * @param defaultEnabledLibraries 默认启用的组件库名称
     */
    private void notifyGlobalConfigChangedWithNames(Set<String> defaultEnabledLibraries) {
        // 通知 ComponentProvider 重新加载组件数据
        try {
            // 这里可以添加全局配置变更的通知逻辑
            VueKitLogger.info(LOG, "全局配置已变更，启用的组件库: " + String.join(", ", defaultEnabledLibraries));
        } catch (Exception e) {
            VueKitLogger.error(LOG, "通知全局配置变更失败", e);
        }
    }

    /**
     * 项目启动时加载配置
     * 这个方法由 StartupActivity 调用
     *
     * @param project 项目对象
     */
    public void onProjectStarted(Project project) {
        VueKitLogger.info(LOG, "项目启动，加载组件库配置: " + project.getName());
        loadProjectConfig(project);
    }

    // JSON 转换方法（使用 Gson 实现）
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

    // 配置状态类
    public static class ConfigState {
        // 可以添加需要持久化的状态
    }

    @Override
    public ConfigState getState() {
        return new ConfigState();
    }

    @Override
    public void loadState(ConfigState state) {
        // 加载持久化状态
    }

    // 项目配置类
    public static class ProjectConfig {
        private String projectId;
        private String projectName;
        private Set<String> enabledLibraryNames = new HashSet<>(); // 使用字符串存储组件库名称
        
        // 功能开关配置
        private boolean enableComponentCompletion = true;
        private boolean enableAttributeCompletion = true;
        private boolean enableEventCompletion = true;
        private boolean enableSlotCompletion = true;
        private boolean enableHoverDocumentation = true;
        private boolean enableRightClickDocumentation = true;
        private boolean enableCaching = true;
        private boolean enableDebugMode = false;

        // Getters and Setters
        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public Set<String> getEnabledLibraryNames() {
            return enabledLibraryNames;
        }

        public void setEnabledLibraryNames(Set<String> enabledLibraryNames) {
            this.enabledLibraryNames = enabledLibraryNames;
        }


        
        // 功能开关的 Getters and Setters
        public boolean isEnableComponentCompletion() {
            return enableComponentCompletion;
        }

        public void setEnableComponentCompletion(boolean enableComponentCompletion) {
            this.enableComponentCompletion = enableComponentCompletion;
        }

        public boolean isEnableAttributeCompletion() {
            return enableAttributeCompletion;
        }

        public void setEnableAttributeCompletion(boolean enableAttributeCompletion) {
            this.enableAttributeCompletion = enableAttributeCompletion;
        }

        public boolean isEnableEventCompletion() {
            return enableEventCompletion;
        }

        public void setEnableEventCompletion(boolean enableEventCompletion) {
            this.enableEventCompletion = enableEventCompletion;
        }

        public boolean isEnableSlotCompletion() {
            return enableSlotCompletion;
        }

        public void setEnableSlotCompletion(boolean enableSlotCompletion) {
            this.enableSlotCompletion = enableSlotCompletion;
        }

        public boolean isEnableHoverDocumentation() {
            return enableHoverDocumentation;
        }

        public void setEnableHoverDocumentation(boolean enableHoverDocumentation) {
            this.enableHoverDocumentation = enableHoverDocumentation;
        }

        public boolean isEnableRightClickDocumentation() {
            return enableRightClickDocumentation;
        }

        public void setEnableRightClickDocumentation(boolean enableRightClickDocumentation) {
            this.enableRightClickDocumentation = enableRightClickDocumentation;
        }

        public boolean isEnableCaching() {
            return enableCaching;
        }

        public void setEnableCaching(boolean enableCaching) {
            this.enableCaching = enableCaching;
        }

        public boolean isEnableDebugMode() {
            return enableDebugMode;
        }

        public void setEnableDebugMode(boolean enableDebugMode) {
            this.enableDebugMode = enableDebugMode;
        }
    }

    // 全局配置类
    public static class GlobalConfig {
        private Set<String> defaultEnabledLibraryNames = new HashSet<>(); // 使用字符串存储组件库名称

        // Getters and Setters
        public Set<String> getDefaultEnabledLibraryNames() {
            return defaultEnabledLibraryNames;
        }

        public void setDefaultEnabledLibraryNames(Set<String> defaultEnabledLibraryNames) {
            this.defaultEnabledLibraryNames = defaultEnabledLibraryNames;
        }
    }

    // 配置变更监听器接口
    public interface ConfigChangeListener {
        void onProjectConfigChanged(Project project, Set<String> enabledLibraries);

        void onGlobalConfigChanged(Set<String> defaultEnabledLibraries);
    }
}
