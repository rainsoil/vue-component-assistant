package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.ProjectConfig;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Package.json 自动检测器
 * 
 * 功能说明：
 * - 检测项目中的 package.json 文件
 * - 解析依赖项，匹配已安装的组件库
 * - 自动启用匹配到的组件库
 * - 在项目首次使用时提供智能默认配置
 * 
 * 设计原则：
 * - 完全基于 ComponentLibraryManager 进行动态检测
 * - 移除所有硬编码的组件库信息
 * - 支持组件库的自动扩展和更新
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class PackageJsonAutoDetector {
    
    private static final Logger LOG = VueKitLogger.getLogger(PackageJsonAutoDetector.class);
    
    /**
     * 自动检测并启用项目中的组件库
     * 
     * 检测逻辑：
     * 1. 检查项目配置文件是否为空
     * 2. 读取 package.json 文件
     * 3. 解析依赖项
     * 4. 动态匹配已安装的组件库
     * 5. 自动启用匹配到的组件库
     * 6. 保存配置到项目配置文件
     * 
     * @param project 项目对象
     * @return 是否成功检测并启用了组件库
     */
    public static boolean autoDetectAndEnableLibraries(Project project) {
        try {
            VueKitLogger.info(LOG, "=== 开始自动检测项目组件库 ===");
            VueKitLogger.info(LOG, "项目名称: " + project.getName());
            
            // 检查项目配置文件是否为空
            if (!isProjectConfigEmpty(project)) {
                VueKitLogger.info(LOG, "项目配置文件已存在且有效，跳过自动检测");
                return false;
            }
            
            // 确保 .idea 目录存在
            ensureIdeaDirectoryExists(project);
            
            // 读取 package.json 文件
            VirtualFile packageJsonFile = findPackageJsonFile(project);
            if (packageJsonFile == null) {
                VueKitLogger.info(LOG, "未找到 package.json 文件，跳过自动检测");
                return false;
            }
            
            // 解析 package.json 内容
            JsonObject packageJson = parsePackageJson(packageJsonFile);
            if (packageJson == null) {
                VueKitLogger.warn(LOG, "解析 package.json 失败");
                return false;
            }
            
            // 记录 package.json 的内容用于调试
            VueKitLogger.debug(LOG, "package.json 内容: " + packageJson.toString());
            
            // 动态检测依赖项中的组件库
            Set<String> detectedLibraries = detectLibrariesFromDependencies(packageJson);
            if (detectedLibraries.isEmpty()) {
                VueKitLogger.info(LOG, "未检测到支持的组件库");
                return false;
            }
            
            VueKitLogger.info(LOG, "检测到的组件库: " + String.join(", ", detectedLibraries));
            
            // 启用检测到的组件库
            boolean success = enableDetectedLibraries(project, detectedLibraries);
            
            if (success) {
                VueKitLogger.info(LOG, "✅ 自动检测并启用组件库成功");
            } else {
                VueKitLogger.warn(LOG, "⚠️ 自动检测组件库失败");
            }
            
            return success;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "自动检测组件库时发生错误", e);
            return false;
        }
    }
    
    /**
     * 检查项目配置文件是否为空
     * 
     * @param project 项目对象
     * @return 如果配置文件为空或不存在则返回 true
     */
    private static boolean isProjectConfigEmpty(Project project) {
        try {
            VirtualFile ideaDir = getIdeaDirectory(project);
            if (ideaDir == null) {
                return true;
            }
            
            VirtualFile configFile = ideaDir.findChild("vuekit-project-config.json");
            if (configFile == null || !configFile.exists()) {
                return true;
            }
            
            String configContent = new String(configFile.contentsToByteArray(), StandardCharsets.UTF_8);
            return configContent.trim().isEmpty() || configContent.equals("{}") || configContent.equals("{\"enabledLibraries\":[]}");
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "检查项目配置文件状态失败", e);
            return true;
        }
    }
    
    /**
     * 查找项目中的 package.json 文件
     * 
     * @param project 项目对象
     * @return package.json 文件，如果未找到则返回 null
     */
    private static VirtualFile findPackageJsonFile(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            if (projectDir == null) {
                return null;
            }
            
            VirtualFile packageJsonFile = projectDir.findChild("package.json");
            if (packageJsonFile != null && packageJsonFile.exists() && !packageJsonFile.isDirectory()) {
                return packageJsonFile;
            }
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "查找 package.json 文件失败", e);
        }
        
        return null;
    }
    
    /**
     * 解析 package.json 文件内容
     * 
     * @param packageJsonFile package.json 文件
     * @return 解析后的 JSON 对象，如果解析失败则返回 null
     */
    private static JsonObject parsePackageJson(VirtualFile packageJsonFile) {
        try {
            String content = new String(packageJsonFile.contentsToByteArray(), StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) {
                return null;
            }
            
            return JsonParser.parseString(content).getAsJsonObject();
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "解析 package.json 失败", e);
            return null;
        }
    }
    
    /**
     * 确保 .idea 目录存在
     * 
     * @param project 项目对象
     */
    private static void ensureIdeaDirectoryExists(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            if (projectDir == null) {
                VueKitLogger.warn(LOG, "无法获取项目根目录");
                return;
            }
            
            VirtualFile ideaDir = projectDir.findChild(".idea");
            if (ideaDir == null || !ideaDir.exists() || !ideaDir.isDirectory()) {
                VueKitLogger.info(LOG, ".idea 目录不存在，尝试创建...");
                
                // 避免在读取操作中调用 invokeAndWait，改用异步方式
                com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                    com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction(() -> {
                        try {
                            VirtualFile createdIdeaDir = projectDir.createChildDirectory(PackageJsonAutoDetector.class, ".idea");
                            VueKitLogger.info(LOG, ".idea 目录创建成功: " + createdIdeaDir.getPath());
                        } catch (Exception e) {
                            VueKitLogger.error(LOG, "创建 .idea 目录失败", e);
                        }
                    });
                });
                
                VueKitLogger.info(LOG, "✅ .idea 目录创建请求已提交");
            } else {
                VueKitLogger.debug(LOG, ".idea 目录已存在");
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "确保 .idea 目录存在时发生错误", e);
        }
    }
    
    /**
     * 从依赖项中动态检测组件库
     * 
     * 检测逻辑：
     * 1. 从 ComponentLibraryManager 获取所有已安装的组件库
     * 2. 遍历 package.json 中的所有依赖项
     * 3. 使用标准化匹配算法进行匹配
     * 4. 支持主包名和相关包名的匹配
     * 
     * @param packageJson package.json 的 JSON 对象
     * @return 检测到的组件库名称集合
     */
    private static Set<String> detectLibrariesFromDependencies(JsonObject packageJson) {
        Set<String> detectedLibraries = new HashSet<>();
        
        try {
            // 获取所有已安装的组件库
            ComponentLibraryManager libraryManager = new ComponentLibraryManager();
            List<ComponentLibrary> installedLibraries = libraryManager.getAllLibraries();
            
            VueKitLogger.debug(LOG, "已安装的组件库数量: " + installedLibraries.size());
            for (ComponentLibrary library : installedLibraries) {
                VueKitLogger.debug(LOG, "已安装组件库: " + library.getName());
            }
            
            // 检查各种依赖类型
            String[] dependencyTypes = {"dependencies", "devDependencies", "peerDependencies"};
            
            for (String dependencyType : dependencyTypes) {
                if (packageJson.has(dependencyType)) {
                    JsonObject dependencies = packageJson.getAsJsonObject(dependencyType);
                    VueKitLogger.debug(LOG, "检查 " + dependencyType + ": " + dependencies.toString());
                    
                    // 对每个已安装的组件库进行匹配
                    for (ComponentLibrary library : installedLibraries) {
                        if (isLibraryMatchInDependencies(library, dependencies)) {
                            detectedLibraries.add(library.getName());
                        }
                    }
                } else {
                    VueKitLogger.debug(LOG, dependencyType + " 不存在");
                }
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "动态检测组件库失败", e);
        }
        
        return detectedLibraries;
    }
    
    /**
     * 检查组件库是否在依赖项中匹配
     * 
     * @param library 组件库对象
     * @param dependencies 依赖项对象
     * @return 是否匹配
     */
    private static boolean isLibraryMatchInDependencies(ComponentLibrary library, JsonObject dependencies) {
        // 获取组件库的主包名
        String mainPackageName = library.getName();
        
        // 检查主包名是否匹配
        for (String dependencyName : dependencies.keySet()) {
            if (StringNormalizer.matches(mainPackageName, dependencyName)) {
                String version = dependencies.get(dependencyName).getAsString();
                VueKitLogger.info(LOG, "检测到组件库: " + dependencyName + " (版本: " + version + ") -> 匹配到: " + mainPackageName);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 启用检测到的组件库
     * 
     * @param project 项目对象
     * @param detectedLibraries 检测到的组件库名称集合
     * @return 是否成功启用
     */
    private static boolean enableDetectedLibraries(Project project, Set<String> detectedLibraries) {
        try {
            // 获取组件库配置管理器
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            
            // 直接使用检测到的组件库名称，不再转换为 LibraryType 枚举
            Set<String> enabledLibraryNames = new HashSet<>(detectedLibraries);
            
            VueKitLogger.info(LOG, "检测到的组件库: " + String.join(", ", detectedLibraries));
            
            // 直接设置项目启用的组件库名称
            configManager.setProjectEnabledLibraryNames(project, enabledLibraryNames);
            
            VueKitLogger.info(LOG, "✅ 已设置启用的组件库: " + String.join(", ", enabledLibraryNames));
            
            // 异步更新项目设置管理器中的组件库配置，避免死锁
            com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
                    ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
                    projectSettings.setEnabledLibraryNames(enabledLibraryNames);
                    projectSettingsManager.saveProjectSettings(project, projectSettings);
                    VueKitLogger.info(LOG, "✅ 项目设置已异步更新");
                } catch (Exception e) {
                    VueKitLogger.error(LOG, "异步更新项目设置失败", e);
                }
            });
            
            VueKitLogger.info(LOG, "✅ 已启用组件库: " + String.join(", ", enabledLibraryNames));
            return true;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "启用检测到的组件库失败", e);
            return false;
        }
    }
    
    /**
     * 获取项目的 .idea 目录
     * 
     * @param project 项目对象
     * @return .idea 目录，如果不存在则返回 null
     */
    private static VirtualFile getIdeaDirectory(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            if (projectDir == null) {
                return null;
            }
            
            VirtualFile ideaDir = projectDir.findChild(".idea");
            if (ideaDir != null && ideaDir.exists() && ideaDir.isDirectory()) {
                return ideaDir;
            }
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "获取 .idea 目录失败", e);
        }
        
        return null;
    }
} 