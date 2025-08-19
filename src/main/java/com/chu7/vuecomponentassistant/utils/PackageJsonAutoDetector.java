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
 * <p>功能说明：</p>
 * <ul>
 *   <li>自动检测项目中的 package.json 文件</li>
 *   <li>智能解析依赖项，匹配已安装的组件库</li>
 *   <li>自动启用匹配到的组件库，无需手动配置</li>
 *   <li>在项目首次使用时提供智能默认配置</li>
 *   <li>支持多种依赖类型（dependencies、devDependencies、peerDependencies）</li>
 *   <li>自动创建和管理 .idea 目录结构</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>完全基于 ComponentLibraryManager 进行动态检测</li>
 *   <li>移除所有硬编码的组件库信息，支持自动扩展</li>
 *   <li>智能的配置文件状态检测和空值判断</li>
 *   <li>异步操作避免UI阻塞和死锁问题</li>
 *   <li>完善的错误处理和日志记录</li>
 *   <li>支持项目级和全局级配置管理</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>新项目初始化时的自动组件库配置</li>
 *   <li>项目依赖更新后的组件库自动检测</li>
 *   <li>团队项目配置的统一化和标准化</li>
 *   <li>开发环境迁移时的配置自动恢复</li>
 *   <li>组件库功能的智能启用管理</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager
 * @see com.chu7.vuecomponentassistant.settings.ProjectSettingsManager
 * @see com.chu7.vuecomponentassistant.utils.StringNormalizer
 */
public class PackageJsonAutoDetector {
    
    private static final Logger LOG = VueKitLogger.getLogger(PackageJsonAutoDetector.class);
    
    /**
     * 自动检测并启用项目中的组件库
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查项目配置文件状态，仅在配置为空时执行检测</li>
     *   <li>自动查找和解析 package.json 文件</li>
     *   <li>智能匹配已安装的组件库与项目依赖</li>
     *   <li>自动启用匹配到的组件库并保存配置</li>
     *   <li>提供详细的检测过程和结果反馈</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>检查项目配置文件是否为空</li>
     *   <li>确保 .idea 目录存在</li>
     *   <li>读取并解析 package.json 文件</li>
     *   <li>动态检测依赖项中的组件库</li>
     *   <li>自动启用检测到的组件库</li>
     *   <li>保存配置到项目配置文件</li>
     * </ol>
     *
     * @param project 项目对象，不能为null
     * @return 是否成功检测并启用了组件库
     * @throws IllegalArgumentException 如果project参数为null
     * @see #isProjectConfigEmpty(Project)
     * @see #findPackageJsonFile(Project)
     * @see #detectLibrariesFromDependencies(JsonObject)
     * @see #enableDetectedLibraries(Project, Set)
     */
    public static boolean autoDetectAndEnableLibraries(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查项目配置文件是否存在和有效</li>
     *   <li>判断配置文件内容是否为空或无效</li>
     *   <li>支持多种空值状态的检测</li>
     * </ul>
     *
     * <p>检测规则：</p>
     * <ul>
     *   <li>配置文件不存在</li>
     *   <li>配置文件内容为空字符串</li>
     *   <li>配置文件内容为空的JSON对象 "{}"</li>
     *   <li>配置文件内容为空的启用库数组 "{\"enabledLibraries\":[]}"</li>
     * </ul>
     *
     * @param project 项目对象，不能为null
     * @return 如果配置文件为空或不存在则返回 true，否则返回 false
     * @throws IllegalArgumentException 如果project参数为null
     * @see #getIdeaDirectory(Project)
     */
    private static boolean isProjectConfigEmpty(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>在项目根目录中查找 package.json 文件</li>
     *   <li>验证文件的存在性和类型</li>
     *   <li>使用 ProjectPathHelper 获取项目根目录</li>
     * </ul>
     *
     * <p>查找策略：</p>
     * <ol>
     *   <li>获取项目根目录</li>
     *   <li>查找 package.json 文件</li>
     *   <li>验证文件存在且不是目录</li>
     * </ol>
     *
     * @param project 项目对象，不能为null
     * @return package.json 文件，如果未找到则返回 null
     * @throws IllegalArgumentException 如果project参数为null
     * @see com.chu7.vuecomponentassistant.utils.ProjectPathHelper#getProjectRoot(Project)
     */
    private static VirtualFile findPackageJsonFile(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>读取 package.json 文件的字节内容</li>
     *   <li>将内容转换为UTF-8字符串</li>
     *   <li>使用Gson解析JSON内容</li>
     *   <li>验证解析结果的完整性</li>
     * </ul>
     *
     * <p>解析流程：</p>
     * <ol>
     *   <li>读取文件字节内容</li>
     *   <li>转换为UTF-8字符串</li>
     *   <li>检查内容是否为空</li>
     *   <li>使用JsonParser解析JSON</li>
     *   <li>返回JsonObject结果</li>
     * </ol>
     *
     * @param packageJsonFile package.json 文件，不能为null
     * @return 解析后的 JSON 对象，如果解析失败则返回 null
     * @throws IllegalArgumentException 如果packageJsonFile参数为null
     * @see com.google.gson.JsonParser#parseString(String)
     */
    private static JsonObject parsePackageJson(VirtualFile packageJsonFile) {
        if (packageJsonFile == null) {
            throw new IllegalArgumentException("package.json文件不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查项目根目录下的 .idea 目录是否存在</li>
     *   <li>如果不存在则异步创建该目录</li>
     *   <li>避免在读取操作中调用同步写入方法</li>
     * </ul>
     *
     * <p>创建策略：</p>
     * <ul>
     *   <li>使用 invokeLater 异步提交创建请求</li>
     *   <li>在 runWriteAction 中执行目录创建</li>
     *   <li>提供创建结果的日志反馈</li>
     * </ul>
     *
     * @param project 项目对象，不能为null
     * @throws IllegalArgumentException 如果project参数为null
     * @see com.intellij.openapi.application.ApplicationManager#getApplication()
     * @see com.chu7.vuecomponentassistant.utils.ProjectPathHelper#getProjectRoot(Project)
     */
    private static void ensureIdeaDirectoryExists(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>从 ComponentLibraryManager 获取所有已安装的组件库</li>
     *   <li>遍历 package.json 中的所有依赖项类型</li>
     *   <li>使用标准化匹配算法进行智能匹配</li>
     *   <li>支持主包名和相关包名的匹配</li>
     * </ul>
     *
     * <p>检测逻辑：</p>
     * <ol>
     *   <li>获取所有已安装的组件库列表</li>
     *   <li>检查三种依赖类型：dependencies、devDependencies、peerDependencies</li>
     *   <li>对每个已安装的组件库进行依赖项匹配</li>
     *   <li>收集所有匹配成功的组件库名称</li>
     * </ol>
     *
     * @param packageJson package.json 的 JSON 对象，不能为null
     * @return 检测到的组件库名称集合
     * @throws IllegalArgumentException 如果packageJson参数为null
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager#getAllLibraries()
     * @see #isLibraryMatchInDependencies(ComponentLibrary, JsonObject)
     */
    private static Set<String> detectLibrariesFromDependencies(JsonObject packageJson) {
        if (packageJson == null) {
            throw new IllegalArgumentException("package.json对象不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>使用 StringNormalizer 进行标准化字符串匹配</li>
     *   <li>支持大小写不敏感的包名匹配</li>
     *   <li>记录匹配成功的详细信息</li>
     * </ul>
     *
     * <p>匹配规则：</p>
     * <ul>
     *   <li>获取组件库的主包名</li>
     *   <li>遍历所有依赖项名称</li>
     *   <li>使用 StringNormalizer.matches 进行标准化匹配</li>
     *   <li>记录匹配成功的包名和版本信息</li>
     * </ul>
     *
     * @param library 组件库对象，不能为null
     * @param dependencies 依赖项对象，不能为null
     * @return 是否匹配成功
     * @throws IllegalArgumentException 如果library或dependencies参数为null
     * @see com.chu7.vuecomponentassistant.utils.StringNormalizer#matches(String, String)
     */
    private static boolean isLibraryMatchInDependencies(ComponentLibrary library, JsonObject dependencies) {
        if (library == null) {
            throw new IllegalArgumentException("组件库对象不能为null");
        }
        if (dependencies == null) {
            throw new IllegalArgumentException("依赖项对象不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>使用 ComponentLibraryConfigManager 设置项目启用的组件库</li>
     *   <li>异步更新项目设置管理器中的组件库配置</li>
     *   <li>避免死锁和UI阻塞问题</li>
     *   <li>提供完整的启用状态反馈</li>
     * </ul>
     *
     * <p>启用流程：</p>
     * <ol>
     *   <li>获取组件库配置管理器实例</li>
     *   <li>设置项目启用的组件库名称</li>
     *   <li>异步更新项目设置管理器</li>
     *   <li>保存项目设置到配置文件</li>
     * </ol>
     *
     * @param project 项目对象，不能为null
     * @param detectedLibraries 检测到的组件库名称集合，不能为null
     * @return 是否成功启用
     * @throws IllegalArgumentException 如果project或detectedLibraries参数为null
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#setProjectEnabledLibraryNames(Project, Set)
     * @see com.chu7.vuecomponentassistant.settings.ProjectSettingsManager#getInstance(Project)
     */
    private static boolean enableDetectedLibraries(Project project, Set<String> detectedLibraries) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        if (detectedLibraries == null) {
            throw new IllegalArgumentException("检测到的组件库集合不能为null");
        }
        
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>在项目根目录下查找 .idea 目录</li>
     *   <li>验证目录的存在性和类型</li>
     *   <li>使用 ProjectPathHelper 获取项目根目录</li>
     * </ul>
     *
     * <p>查找策略：</p>
     * <ol>
     *   <li>获取项目根目录</li>
     *   <li>查找 .idea 子目录</li>
     *   <li>验证目录存在且是目录类型</li>
     * </ol>
     *
     * @param project 项目对象，不能为null
     * @return .idea 目录，如果不存在则返回 null
     * @throws IllegalArgumentException 如果project参数为null
     * @see com.chu7.vuecomponentassistant.utils.ProjectPathHelper#getProjectRoot(Project)
     */
    private static VirtualFile getIdeaDirectory(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
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