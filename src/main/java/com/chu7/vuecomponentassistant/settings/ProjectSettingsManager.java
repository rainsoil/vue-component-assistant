package com.chu7.vuecomponentassistant.settings;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.ApplicationManager;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 项目级设置管理器
 * 
 * 功能说明：
 * - 管理项目特定的功能设置
 * - 支持补全功能、文档功能、性能优化等设置
 * - 配置持久化存储到项目 .idea 目录
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public final class ProjectSettingsManager {
    
    private static final Logger LOG = VueKitLogger.getLogger(ProjectSettingsManager.class);
    
    // 项目配置文件（合并后的配置）
    private static final String PROJECT_CONFIG_FILE = "vuekit-project-config.json";
    
    // 项目级设置缓存
    private static final Map<String, ProjectSettings> projectSettingsCache = new ConcurrentHashMap<>();
    
    /**
     * 获取项目设置管理器实例
     * 
     * @param project 项目对象
     * @return 设置管理器实例
     */
    public static ProjectSettingsManager getInstance(Project project) {
        return new ProjectSettingsManager();
    }
    
    /**
     * 获取项目设置
     * 
     * @param project 项目对象
     * @return 项目设置对象
     */
    public ProjectSettings getProjectSettings(Project project) {
        try {
            String projectId = getProjectId(project);
            
            // 检查缓存
            if (projectSettingsCache.containsKey(projectId)) {
                return projectSettingsCache.get(projectId);
            }
            
            // 从文件加载设置
            ProjectSettings settings = loadProjectSettingsFromFile(project);
            if (settings == null) {
                // 创建默认设置
                settings = createDefaultProjectSettings(project);
            }
            
            // 缓存设置
            projectSettingsCache.put(projectId, settings);
            
            return settings;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取项目设置失败", e);
            return createDefaultProjectSettings(project);
        }
    }
    
    /**
     * 保存项目设置
     * 
     * @param project 项目对象
     * @param settings 项目设置
     */
    public void saveProjectSettings(Project project, ProjectSettings settings) {
        try {
            String projectId = getProjectId(project);
            
            // 更新缓存
            projectSettingsCache.put(projectId, settings);
            
            // 保存到文件
            saveProjectSettingsToFile(project, settings);
            
            VueKitLogger.info(LOG, "项目设置已保存: " + project.getName());
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存项目设置失败", e);
        }
    }
    
    /**
     * 更新项目设置
     * 
     * @param project 项目对象
     * @param settings 项目设置
     */
    public void updateProjectSettings(Project project, ProjectSettings settings) {
        saveProjectSettings(project, settings);
    }
    
    /**
     * 重置项目设置为默认值
     * 
     * @param project 项目对象
     */
    public void resetToDefault(Project project) {
        ProjectSettings defaultSettings = createDefaultProjectSettings(project);
        saveProjectSettings(project, defaultSettings);
        VueKitLogger.info(LOG, "项目设置已重置为默认值: " + project.getName());
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
     * 从文件加载项目设置
     * 
     * @param project 项目对象
     * @return 项目设置对象
     */
    private ProjectSettings loadProjectSettingsFromFile(Project project) {
        try {
            VirtualFile ideaDir = getIdeaDirectory(project);
            if (ideaDir == null) {
                VueKitLogger.warn(LOG, "未找到 .idea 目录");
                return null;
            }
            
            VirtualFile configFile = ideaDir.findChild(PROJECT_CONFIG_FILE);
            
            if (configFile != null && configFile.exists()) {
                String configJson = new String(configFile.contentsToByteArray(), StandardCharsets.UTF_8);
                Gson gson = new Gson();
                return gson.fromJson(configJson, ProjectSettings.class);
            }
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "从文件加载项目设置失败", e);
        }
        
        return null;
    }
    
    /**
     * 保存项目设置到文件
     * 
     * @param project 项目对象
     * @param settings 项目设置
     */
    private void saveProjectSettingsToFile(Project project, ProjectSettings settings) {
        try {
            String settingsJson = new GsonBuilder().setPrettyPrinting().create().toJson(settings);
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
                    targetFile.setBinaryContent(settingsJson.getBytes(StandardCharsets.UTF_8));
                    VueKitLogger.info(LOG, "项目配置已保存到: " + targetFile.getPath());
                } catch (Exception e) {
                    VueKitLogger.error(LOG, "写入项目配置文件失败", e);
                }
            });
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存项目设置到文件失败", e);
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
     * 创建默认项目设置
     * 
     * @param project 项目对象
     * @return 默认项目设置
     */
    private ProjectSettings createDefaultProjectSettings(Project project) {
        ProjectSettings settings = new ProjectSettings();
        settings.setProjectId(getProjectId(project));
        settings.setProjectName(project.getName());
        
        // 使用合理的默认值，不再依赖全局配置
        settings.setEnableComponentCompletion(true);      // 默认启用组件补全
        settings.setEnableAttributeCompletion(true);      // 默认启用属性补全
        settings.setEnableEventCompletion(true);          // 默认启用事件补全
        settings.setEnableSlotCompletion(true);           // 默认启用插槽补全
        settings.setEnableHoverDocumentation(true);       // 默认启用悬停文档
        settings.setEnableRightClickDocumentation(true);  // 默认启用右键文档
        settings.setEnableCaching(true);                  // 默认启用缓存
        settings.setEnableDebugMode(false);               // 默认关闭调试模式
        
        return settings;
    }
    

    
    /**
     * 项目设置类（合并后的配置）
     */
    public static class ProjectSettings {
        private String projectId;
        private String projectName;
        
        // 补全功能设置 - 默认值将在 createDefaultProjectSettings 中从全局设置继承
        private boolean enableComponentCompletion;
        private boolean enableAttributeCompletion;
        private boolean enableEventCompletion;
        private boolean enableSlotCompletion;
        
        // 文档功能设置 - 默认值将在 createDefaultProjectSettings 中从全局设置继承
        private boolean enableHoverDocumentation;
        private boolean enableRightClickDocumentation;
        
        // 性能优化设置 - 默认值将在 createDefaultProjectSettings 中从全局设置继承
        private boolean enableCaching;
        private boolean enableDebugMode;
        
        // 组件库配置（新增）
        private java.util.Set<String> enabledLibraryNames = new java.util.HashSet<>();
        
        // Getters and Setters
        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        
        // 补全功能设置
        public boolean isEnableComponentCompletion() { return enableComponentCompletion; }
        public void setEnableComponentCompletion(boolean enableComponentCompletion) { 
            this.enableComponentCompletion = enableComponentCompletion; 
        }
        
        public boolean isEnableAttributeCompletion() { return enableAttributeCompletion; }
        public void setEnableAttributeCompletion(boolean enableAttributeCompletion) { 
            this.enableAttributeCompletion = enableAttributeCompletion; 
        }
        
        public boolean isEnableEventCompletion() { return enableEventCompletion; }
        public void setEnableEventCompletion(boolean enableEventCompletion) { 
            this.enableEventCompletion = enableEventCompletion; 
        }
        
        public boolean isEnableSlotCompletion() { return enableSlotCompletion; }
        public void setEnableSlotCompletion(boolean enableSlotCompletion) { 
            this.enableSlotCompletion = enableSlotCompletion; 
        }
        
        // 文档功能设置
        public boolean isEnableHoverDocumentation() { return enableHoverDocumentation; }
        public void setEnableHoverDocumentation(boolean enableHoverDocumentation) { 
            this.enableHoverDocumentation = enableHoverDocumentation; 
        }
        
        public boolean isEnableRightClickDocumentation() { return enableRightClickDocumentation; }
        public void setEnableRightClickDocumentation(boolean enableRightClickDocumentation) { 
            this.enableRightClickDocumentation = enableRightClickDocumentation; 
        }
        
        // 性能优化设置
        public boolean isEnableCaching() { return enableCaching; }
        public void setEnableCaching(boolean enableCaching) { 
            this.enableCaching = enableCaching; 
        }
        
        public boolean isEnableDebugMode() { return enableDebugMode; }
        public void setEnableDebugMode(boolean enableDebugMode) { 
            this.enableDebugMode = enableDebugMode; 
        }
        
        // 组件库配置
        public java.util.Set<String> getEnabledLibraryNames() { return enabledLibraryNames; }
        public void setEnabledLibraryNames(java.util.Set<String> enabledLibraryNames) { 
            this.enabledLibraryNames = enabledLibraryNames; 
        }
    }
} 