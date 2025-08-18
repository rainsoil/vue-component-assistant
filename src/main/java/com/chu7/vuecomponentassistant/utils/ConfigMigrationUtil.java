package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.HashSet;

/**
 * 配置迁移工具
 * 
 * 功能说明：
 * - 将旧的分散配置文件迁移到新的合并配置文件
 * - 支持从 .vuekit-libraries.json 和 .vuekit-project-settings.json 迁移
 * - 自动创建 .idea 目录和新的合并配置文件
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ConfigMigrationUtil {
    
    private static final Logger LOG = VueKitLogger.getLogger(ConfigMigrationUtil.class);
    
    // 旧配置文件名称
    private static final String OLD_LIBRARIES_CONFIG_FILE = ".vuekit-libraries.json";
    private static final String OLD_PROJECT_SETTINGS_FILE = ".vuekit-project-settings.json";
    
    // 新配置文件名称
    private static final String NEW_CONFIG_FILE = "vuekit-project-config.json";
    
    /**
     * 执行配置迁移
     * 
     * @param project 项目对象
     * @return 是否迁移成功
     */
    public static boolean migrateConfig(Project project) {
        try {
            VueKitLogger.info(LOG, "开始执行配置迁移，项目: " + project.getName());
            
            // 1. 检查是否需要迁移
            if (!needMigration(project)) {
                VueKitLogger.info(LOG, "无需迁移，配置已是最新格式");
                return true;
            }
            
            // 2. 读取旧配置文件
            JsonObject oldLibrariesConfig = readOldLibrariesConfig(project);
            JsonObject oldProjectSettings = readOldProjectSettings(project);
            
            // 3. 合并配置
            JsonObject mergedConfig = mergeConfigs(oldLibrariesConfig, oldProjectSettings, project);
            
            // 4. 保存到新配置文件
            boolean saved = saveMergedConfig(project, mergedConfig);
            
            if (saved) {
                // 5. 删除旧配置文件
                deleteOldConfigFiles(project);
                VueKitLogger.info(LOG, "配置迁移完成");
                return true;
            } else {
                VueKitLogger.error(LOG, "保存合并配置失败");
                return false;
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "配置迁移失败", e);
            return false;
        }
    }
    
    /**
     * 检查是否需要迁移
     * 
     * @param project 项目对象
     * @return 如果需要迁移则返回 true
     */
    private static boolean needMigration(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            
            // 检查是否存在旧配置文件
            VirtualFile oldLibrariesFile = projectDir.findChild(OLD_LIBRARIES_CONFIG_FILE);
            VirtualFile oldSettingsFile = projectDir.findChild(OLD_PROJECT_SETTINGS_FILE);
            
            // 检查是否存在新配置文件
            VirtualFile ideaDir = projectDir.findChild(".idea");
            VirtualFile newConfigFile = null;
            if (ideaDir != null && ideaDir.exists()) {
                newConfigFile = ideaDir.findChild(NEW_CONFIG_FILE);
            }
            
            // 如果存在旧配置文件且不存在新配置文件，则需要迁移
            return (oldLibrariesFile != null && oldLibrariesFile.exists()) || 
                   (oldSettingsFile != null && oldSettingsFile.exists()) &&
                   (newConfigFile == null || !newConfigFile.exists());
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "检查迁移需求失败", e);
            return false;
        }
    }
    
    /**
     * 读取旧的组件库配置文件
     * 
     * @param project 项目对象
     * @return 配置对象
     */
    private static JsonObject readOldLibrariesConfig(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            VirtualFile oldFile = projectDir.findChild(OLD_LIBRARIES_CONFIG_FILE);
            
            if (oldFile != null && oldFile.exists()) {
                String content = new String(oldFile.contentsToByteArray(), StandardCharsets.UTF_8);
                return JsonParser.parseString(content).getAsJsonObject();
            }
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "读取旧组件库配置失败", e);
        }
        
        return new JsonObject();
    }
    
    /**
     * 读取旧的项目设置配置文件
     * 
     * @param project 项目对象
     * @return 配置对象
     */
    private static JsonObject readOldProjectSettings(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            VirtualFile oldFile = projectDir.findChild(OLD_PROJECT_SETTINGS_FILE);
            
            if (oldFile != null && oldFile.exists()) {
                String content = new String(oldFile.contentsToByteArray(), StandardCharsets.UTF_8);
                return JsonParser.parseString(content).getAsJsonObject();
            }
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "读取旧项目设置配置失败", e);
        }
        
        return new JsonObject();
    }
    
    /**
     * 合并配置
     * 
     * @param librariesConfig 组件库配置
     * @param projectSettings 项目设置配置
     * @param project 项目对象
     * @return 合并后的配置
     */
    private static JsonObject mergeConfigs(JsonObject librariesConfig, JsonObject projectSettings, Project project) {
        JsonObject mergedConfig = new JsonObject();
        
        // 基本信息
        mergedConfig.addProperty("projectId", project.getLocationHash());
        mergedConfig.addProperty("projectName", project.getName());
        
        // 从组件库配置中提取启用的组件库
        if (librariesConfig.has("enabledLibraryNames")) {
            mergedConfig.add("enabledLibraryNames", librariesConfig.get("enabledLibraryNames"));
        } else {
            mergedConfig.add("enabledLibraryNames", new com.google.gson.JsonArray());
        }
        
        // 从项目设置中提取功能配置
        if (projectSettings.has("enableComponentCompletion")) {
            mergedConfig.addProperty("enableComponentCompletion", projectSettings.get("enableComponentCompletion").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableComponentCompletion", true);
        }
        
        if (projectSettings.has("enableAttributeCompletion")) {
            mergedConfig.addProperty("enableAttributeCompletion", projectSettings.get("enableAttributeCompletion").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableAttributeCompletion", true);
        }
        
        if (projectSettings.has("enableEventCompletion")) {
            mergedConfig.addProperty("enableEventCompletion", projectSettings.get("enableEventCompletion").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableEventCompletion", true);
        }
        
        if (projectSettings.has("enableSlotCompletion")) {
            mergedConfig.addProperty("enableSlotCompletion", projectSettings.get("enableSlotCompletion").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableSlotCompletion", true);
        }
        
        if (projectSettings.has("enableHoverDocumentation")) {
            mergedConfig.addProperty("enableHoverDocumentation", projectSettings.get("enableHoverDocumentation").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableHoverDocumentation", true);
        }
        
        if (projectSettings.has("enableRightClickDocumentation")) {
            mergedConfig.addProperty("enableRightClickDocumentation", projectSettings.get("enableRightClickDocumentation").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableRightClickDocumentation", true);
        }
        
        if (projectSettings.has("enableCaching")) {
            mergedConfig.addProperty("enableCaching", projectSettings.get("enableCaching").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableCaching", true);
        }
        
        if (projectSettings.has("enableDebugMode")) {
            mergedConfig.addProperty("enableDebugMode", projectSettings.get("enableDebugMode").getAsBoolean());
        } else {
            mergedConfig.addProperty("enableDebugMode", false);
        }
        
        return mergedConfig;
    }
    
    /**
     * 保存合并后的配置
     * 
     * @param project 项目对象
     * @param mergedConfig 合并后的配置
     * @return 是否保存成功
     */
    private static boolean saveMergedConfig(Project project, JsonObject mergedConfig) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            VirtualFile ideaDir = projectDir.findChild(".idea");
            
            // 如果 .idea 目录不存在，创建它
            if (ideaDir == null || !ideaDir.exists()) {
                ideaDir = projectDir.createChildDirectory(null, ".idea");
            }
            
            if (ideaDir == null || !ideaDir.exists()) {
                VueKitLogger.error(LOG, "无法创建 .idea 目录");
                return false;
            }
            
            // 创建新配置文件
            VirtualFile newConfigFile = ideaDir.createChildData(null, NEW_CONFIG_FILE);
            String configJson = new Gson().toJson(mergedConfig);
            newConfigFile.setBinaryContent(configJson.getBytes(StandardCharsets.UTF_8));
            
            VueKitLogger.info(LOG, "合并配置已保存到: " + newConfigFile.getPath());
            return true;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "保存合并配置失败", e);
            return false;
        }
    }
    
    /**
     * 删除旧配置文件
     * 
     * @param project 项目对象
     */
    private static void deleteOldConfigFiles(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            
            // 删除旧的组件库配置文件
            VirtualFile oldLibrariesFile = projectDir.findChild(OLD_LIBRARIES_CONFIG_FILE);
            if (oldLibrariesFile != null && oldLibrariesFile.exists()) {
                oldLibrariesFile.delete(null);
                VueKitLogger.info(LOG, "已删除旧组件库配置文件");
            }
            
            // 删除旧的项目设置配置文件
            VirtualFile oldSettingsFile = projectDir.findChild(OLD_PROJECT_SETTINGS_FILE);
            if (oldSettingsFile != null && oldSettingsFile.exists()) {
                oldSettingsFile.delete(null);
                VueKitLogger.info(LOG, "已删除旧项目设置配置文件");
            }
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "删除旧配置文件失败", e);
        }
    }
    
    /**
     * 获取配置文件路径信息
     * 
     * @param project 项目对象
     * @return 配置文件路径信息
     */
    public static String getConfigPathInfo(Project project) {
        StringBuilder info = new StringBuilder();
        info.append("=== 配置文件路径信息 ===\n\n");
        
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            
            // 旧配置文件
            VirtualFile oldLibrariesFile = projectDir.findChild(OLD_LIBRARIES_CONFIG_FILE);
            VirtualFile oldSettingsFile = projectDir.findChild(OLD_PROJECT_SETTINGS_FILE);
            
            info.append("旧配置文件:\n");
            if (oldLibrariesFile != null && oldLibrariesFile.exists()) {
                info.append("  ✅ ").append(oldLibrariesFile.getPath()).append("\n");
            } else {
                info.append("  ❌ ").append(projectDir.getPath()).append("/").append(OLD_LIBRARIES_CONFIG_FILE).append(" (不存在)\n");
            }
            
            if (oldSettingsFile != null && oldSettingsFile.exists()) {
                info.append("  ✅ ").append(oldSettingsFile.getPath()).append("\n");
            } else {
                info.append("  ❌ ").append(projectDir.getPath()).append("/").append(OLD_PROJECT_SETTINGS_FILE).append(" (不存在)\n");
            }
            
            // 新配置文件
            VirtualFile ideaDir = projectDir.findChild(".idea");
            VirtualFile newConfigFile = null;
            if (ideaDir != null && ideaDir.exists()) {
                newConfigFile = ideaDir.findChild(NEW_CONFIG_FILE);
            }
            
            info.append("\n新配置文件:\n");
            if (newConfigFile != null && newConfigFile.exists()) {
                info.append("  ✅ ").append(newConfigFile.getPath()).append("\n");
            } else {
                info.append("  ❌ ").append(projectDir.getPath()).append("/.idea/").append(NEW_CONFIG_FILE).append(" (不存在)\n");
            }
            
            // 迁移建议
            info.append("\n迁移建议:\n");
            if (needMigration(project)) {
                info.append("  🔄 建议执行配置迁移\n");
            } else {
                info.append("  ✅ 配置已是最新格式，无需迁移\n");
            }
            
        } catch (Exception e) {
            info.append("获取配置路径信息失败: ").append(e.getMessage());
        }
        
        return info.toString();
    }
} 