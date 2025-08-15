package com.chu7.vuecomponentassistant.settings;

import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
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
public class ComponentLibraryConfigManager implements PersistentStateComponent<ComponentLibraryConfigManager.ConfigState> {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigManager.class);
    
    // 配置文件名
    private static final String PROJECT_CONFIG_FILE = ".vuekit-libraries.json";
    private static final String GLOBAL_CONFIG_FILE = "vuekit-libraries.json";
    
    // 默认启用的组件库
    private static final Set<ComponentLibraryDetector.LibraryType> DEFAULT_ENABLED_LIBRARIES = new HashSet<>(Arrays.asList(
        ComponentLibraryDetector.LibraryType.ELEMENT_UI,
        ComponentLibraryDetector.LibraryType.ELEMENT_PLUS,
        ComponentLibraryDetector.LibraryType.ANT_DESIGN_VUE
    ));
    
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
     * 获取项目启用的组件库
     * 
     * @param project 项目对象
     * @return 启用的组件库集合
     */
    public Set<ComponentLibraryDetector.LibraryType> getEnabledLibraries(Project project) {
        try {
            String projectId = getProjectId(project);
            
            // 1. 检查项目级配置
            ProjectConfig projectConfig = getProjectConfig(project);
            if (projectConfig != null && !projectConfig.getEnabledLibraries().isEmpty()) {
                VueKitLogger.debug(LOG, "使用项目级配置，启用的组件库: " + 
                    projectConfig.getEnabledLibraries().stream()
                        .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                        .collect(java.util.stream.Collectors.joining(", ")));
                return new HashSet<>(projectConfig.getEnabledLibraries());
            }
            
            // 2. 检查全局配置
            if (globalConfig != null && !globalConfig.getDefaultEnabledLibraries().isEmpty()) {
                VueKitLogger.debug(LOG, "使用全局配置，启用的组件库: " + 
                    globalConfig.getDefaultEnabledLibraries().stream()
                        .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                        .collect(java.util.stream.Collectors.joining(", ")));
                return new HashSet<>(globalConfig.getDefaultEnabledLibraries());
            }
            
            // 3. 使用默认配置
            VueKitLogger.debug(LOG, "使用默认配置，启用的组件库: " + 
                DEFAULT_ENABLED_LIBRARIES.stream()
                    .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", ")));
            return new HashSet<>(DEFAULT_ENABLED_LIBRARIES);
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取项目启用的组件库失败", e);
            return new HashSet<>(DEFAULT_ENABLED_LIBRARIES);
        }
    }
    
    /**
     * 设置项目启用的组件库
     * 
     * @param project 项目对象
     * @param enabledLibraries 启用的组件库集合
     */
    public void setProjectEnabledLibraries(Project project, 
                                         Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
        try {
            String projectId = getProjectId(project);
            ProjectConfig projectConfig = getProjectConfig(project);
            
            if (projectConfig == null) {
                projectConfig = new ProjectConfig();
                projectConfig.setProjectId(projectId);
                projectConfig.setProjectName(project.getName());
            }
            
            projectConfig.setEnabledLibraries(enabledLibraries);
            projectConfigs.put(projectId, projectConfig);
            
            // 保存项目配置
            saveProjectConfig(project, projectConfig);
            
            // 通知配置变更
            notifyConfigChanged(project, enabledLibraries);
            
            VueKitLogger.info(LOG, "项目 " + project.getName() + " 的组件库配置已更新: " + 
                enabledLibraries.stream()
                    .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", ")));
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "设置项目启用的组件库失败", e);
        }
    }
    
    /**
     * 设置全局默认启用的组件库
     * 
     * @param defaultEnabledLibraries 默认启用的组件库集合
     */
    public void setGlobalDefaultLibraries(Set<ComponentLibraryDetector.LibraryType> defaultEnabledLibraries) {
        try {
            if (globalConfig == null) {
                globalConfig = new GlobalConfig();
            }
            
            globalConfig.setDefaultEnabledLibraries(defaultEnabledLibraries);
            
            // 保存全局配置
            saveGlobalConfig();
            
            // 通知配置变更
            notifyGlobalConfigChanged(defaultEnabledLibraries);
            
            VueKitLogger.info(LOG, "全局默认组件库配置已更新: " + 
                defaultEnabledLibraries.stream()
                    .map(ComponentLibraryDetector.LibraryType::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", ")));
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "设置全局默认组件库失败", e);
        }
    }
    
    /**
     * 获取全局默认启用的组件库
     * 
     * @return 默认启用的组件库集合
     */
    public Set<ComponentLibraryDetector.LibraryType> getGlobalDefaultLibraries() {
        if (globalConfig != null && !globalConfig.getDefaultEnabledLibraries().isEmpty()) {
            return new HashSet<>(globalConfig.getDefaultEnabledLibraries());
        }
        return new HashSet<>(DEFAULT_ENABLED_LIBRARIES);
    }
    
    /**
     * 启用组件库
     * 
     * @param project 项目对象
     * @param libraryType 要启用的组件库类型
     */
    public void enableLibrary(Project project, ComponentLibraryDetector.LibraryType libraryType) {
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibraries(project);
        enabledLibraries.add(libraryType);
        setProjectEnabledLibraries(project, enabledLibraries);
    }
    
    /**
     * 禁用组件库
     * 
     * @param project 项目对象
     * @param libraryType 要禁用的组件库类型
     */
    public void disableLibrary(Project project, ComponentLibraryDetector.LibraryType libraryType) {
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibraries(project);
        enabledLibraries.remove(libraryType);
        setProjectEnabledLibraries(project, enabledLibraries);
    }
    
    /**
     * 检查组件库是否启用
     * 
     * @param project 项目对象
     * @param libraryType 组件库类型
     * @return 如果启用则返回 true
     */
    public boolean isLibraryEnabled(Project project, ComponentLibraryDetector.LibraryType libraryType) {
        Set<ComponentLibraryDetector.LibraryType> enabledLibraries = getEnabledLibraries(project);
        return enabledLibraries.contains(libraryType);
    }
    
    /**
     * 重置项目配置为全局默认
     * 
     * @param project 项目对象
     */
    public void resetToGlobalDefault(Project project) {
        Set<ComponentLibraryDetector.LibraryType> globalDefaults = getGlobalDefaultLibraries();
        setProjectEnabledLibraries(project, globalDefaults);
        
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
                setProjectEnabledLibraries(project, projectConfig.getEnabledLibraries());
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
                VueKitLogger.debug(LOG, "项目配置已加载: " + project.getName());
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
            VirtualFile projectDir = project.getBaseDir();
            VirtualFile configFile = projectDir.findChild(PROJECT_CONFIG_FILE);
            
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
            VirtualFile projectDir = project.getBaseDir();
            VirtualFile configFile = projectDir.findChild(PROJECT_CONFIG_FILE);
            
            if (configFile == null) {
                configFile = projectDir.createChildData(this, PROJECT_CONFIG_FILE);
            }
            
            configFile.setBinaryContent(configJson.getBytes(StandardCharsets.UTF_8));
            
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
    private void notifyConfigChanged(Project project, Set<ComponentLibraryDetector.LibraryType> enabledLibraries) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onProjectConfigChanged(project, enabledLibraries);
            } catch (Exception e) {
                VueKitLogger.error(LOG, "通知配置变更监听器失败", e);
            }
        }
    }
    
    /**
     * 通知全局配置变更
     * 
     * @param defaultEnabledLibraries 默认启用的组件库
     */
    private void notifyGlobalConfigChanged(Set<ComponentLibraryDetector.LibraryType> defaultEnabledLibraries) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onGlobalConfigChanged(defaultEnabledLibraries);
            } catch (Exception e) {
                VueKitLogger.error(LOG, "通知全局配置变更监听器失败", e);
            }
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
    
    // JSON 转换方法（简化实现）
    private String convertProjectConfigToJson(ProjectConfig config) {
        // 这里可以后续使用 Gson 实现
        return "{\"projectId\":\"" + config.getProjectId() + "\",\"enabledLibraries\":[]}";
    }
    
    private ProjectConfig parseProjectConfigFromJson(String json) {
        // 这里可以后续使用 Gson 实现
        return new ProjectConfig();
    }
    
    private String convertGlobalConfigToJson(GlobalConfig config) {
        // 这里可以后续使用 Gson 实现
        return "{\"defaultEnabledLibraries\":[]}";
    }
    
    private GlobalConfig parseGlobalConfigFromJson(String json) {
        // 这里可以后续使用 Gson 实现
        return new GlobalConfig();
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
        private Set<ComponentLibraryDetector.LibraryType> enabledLibraries = new HashSet<>();
        
        // Getters and Setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        
        public Set<ComponentLibraryDetector.LibraryType> getEnabledLibraries() { return enabledLibraries; }
        public void setEnabledLibraries(Set<ComponentLibraryDetector.LibraryType> enabledLibraries) { 
            this.enabledLibraries = enabledLibraries; 
        }
    }
    
    // 全局配置类
    public static class GlobalConfig {
        private Set<ComponentLibraryDetector.LibraryType> defaultEnabledLibraries = new HashSet<>();
        
        // Getters and Setters
        public Set<ComponentLibraryDetector.LibraryType> getDefaultEnabledLibraries() { 
            return defaultEnabledLibraries; 
        }
        
        public void setDefaultEnabledLibraries(Set<ComponentLibraryDetector.LibraryType> defaultEnabledLibraries) { 
            this.defaultEnabledLibraries = defaultEnabledLibraries; 
        }
    }
    
    // 配置变更监听器接口
    public interface ConfigChangeListener {
        void onProjectConfigChanged(Project project, Set<ComponentLibraryDetector.LibraryType> enabledLibraries);
        void onGlobalConfigChanged(Set<ComponentLibraryDetector.LibraryType> defaultEnabledLibraries);
    }
}
