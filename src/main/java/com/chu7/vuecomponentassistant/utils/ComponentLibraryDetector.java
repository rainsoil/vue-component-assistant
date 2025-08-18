package com.chu7.vuecomponentassistant.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 组件库检测器
 * 
 * 功能说明：
 * - 读取项目根目录下的 package.json 文件
 * - 检测项目使用的组件库类型
 * - 完全动态化，不依赖硬编码的组件库信息
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryDetector {
    
    /** 日志记录器 */
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryDetector.class);

    /**
     * 检测项目使用的组件库类型
     * 
     * 检测逻辑：
     * 1. 读取项目根目录下的 package.json 文件
     * 2. 解析 dependencies 和 devDependencies 字段
     * 3. 使用动态配置管理器检查是否匹配已知组件库
     * 4. 如果找到匹配，返回对应的包名
     * 5. 如果未找到，返回 "unknown"
     * 
     * @param project 当前项目，不能为 null
     * @return 检测到的组件库包名，如果未检测到则返回 "unknown"
     * @throws IllegalArgumentException 如果项目对象为 null
     */
    public static String detectComponentLibrary(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为 null");
        }

        VueKitLogger.info(LOG, "=== 开始检测组件库 ===");
        VueKitLogger.info(LOG, "项目: " + project.getName());
        VueKitLogger.info(LOG, "路径: " + project.getBasePath());

        try {
            // 尝试从 dependencies 中检测
            String detectedType = checkDependencies(project, "dependencies");
            if (!"unknown".equals(detectedType)) {
                VueKitLogger.info(LOG, "在 dependencies 中检测到组件库: " + detectedType);
                return detectedType;
            }

            // 尝试从 devDependencies 中检测
            detectedType = checkDependencies(project, "devDependencies");
            if (!"unknown".equals(detectedType)) {
                VueKitLogger.info(LOG, "在 devDependencies 中检测到组件库: " + detectedType);
                return detectedType;
            }

            VueKitLogger.info(LOG, "未检测到支持的组件库");
            return "unknown";

        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测组件库时发生错误", e);
            return "unknown";
        }
    }

    /**
     * 检查指定类型的依赖中是否包含支持的组件库
     * 
     * @param project 当前项目
     * @param dependencyType 依赖类型（"dependencies" 或 "devDependencies"）
     * @return 检测到的组件库包名，如果未检测到则返回 "unknown"
     */
    private static String checkDependencies(Project project, String dependencyType) {
        VueKitLogger.debug(LOG, "  检查 " + dependencyType + "...");

        try {
            // 获取项目根目录
            VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
            if (projectDir == null) {
                VueKitLogger.debug(LOG, "  无法获取项目根目录");
                return "unknown";
            }

            // 查找 package.json 文件
            VirtualFile packageJsonFile = projectDir.findChild("package.json");
            if (packageJsonFile == null) {
                VueKitLogger.debug(LOG, "  未找到 package.json 文件");
                return "unknown";
            }

            // 读取 package.json 内容
            String content = readFileContent(packageJsonFile);
            if (content == null) {
                VueKitLogger.debug(LOG, "  无法读取 package.json 内容");
                return "unknown";
            }

            // 解析 JSON
            JsonObject packageJson = JsonParser.parseString(content).getAsJsonObject();
            if (!packageJson.has(dependencyType)) {
                VueKitLogger.debug(LOG, "  " + dependencyType + " 字段不存在");
                return "unknown";
            }

            JsonObject dependencies = packageJson.getAsJsonObject(dependencyType);
            VueKitLogger.debug(LOG, "  找到 " + dependencies.size() + " 个依赖");

            // 优先检查已下载的组件库
            try {
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                    libraryManager.getAllLibraries();

                for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                    String packageName = library.getName();
                    if (dependencies.has(packageName)) {
                        String version = dependencies.get(packageName).getAsString();
                        VueKitLogger.info(LOG, "    找到已下载的组件库 " + packageName + ": " + version);
                        return packageName;
                    }
                }
            } catch (Exception e) {
                VueKitLogger.debug(LOG, "动态检查组件库失败，使用静态检查: " + e.getMessage());
            }

            // 后备方案：使用动态配置管理器获取已知的组件库
            try {
                DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
                for (String packageName : dependencies.keySet()) {
                    if (configManager.isKnownLibrary(packageName)) {
                        String version = dependencies.get(packageName).getAsString();
                        VueKitLogger.info(LOG, "    找到已知组件库 " + packageName + ": " + version);
                        return packageName;
                    }
                }
            } catch (Exception configException) {
                VueKitLogger.debug(LOG, "动态配置管理器访问失败，跳过后备检查", configException);
            }

        } catch (Exception e) {
            VueKitLogger.debug(LOG, "  检查 " + dependencyType + " 时发生异常: " + e.getMessage());
        }

        VueKitLogger.debug(LOG, "  在 " + dependencyType + " 中未找到支持的组件库");
        return "unknown";
    }

    /**
     * 读取文件内容
     * 
     * @param file 要读取的虚拟文件，不能为 null
     * @return 文件内容字符串，如果读取失败则返回 null
     * @throws IllegalArgumentException 如果文件对象为 null
     */
    private static String readFileContent(VirtualFile file) {
        if (file == null) {
            throw new IllegalArgumentException("文件对象不能为 null");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            VueKitLogger.debug(LOG, "成功读取文件: " + file.getPath() + ", 内容长度: " + content.length());
            return content;
        } catch (IOException e) {
            VueKitLogger.error(LOG, "读取文件失败: " + file.getPath() + ", 错误: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取组件库的组件前缀
     * 
     * 使用动态配置管理器获取组件前缀，完全移除硬编码
     * 
     * @param libraryType 组件库类型，不能为 null
     * @return 对应的组件前缀字符串
     * @throws IllegalArgumentException 如果组件库类型为 null
     */
    public static String getComponentPrefix(String libraryType) {
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }
        
        // 使用动态配置管理器获取组件前缀
        try {
            DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
            return configManager.getComponentPrefix(libraryType);
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "无法获取组件库前缀: " + libraryType + ", 返回空前缀", e);
            return "";
        }
    }

    /**
     * 获取组件库的文档 URL 模板
     * 
     * 使用动态配置管理器获取文档URL模板，完全移除硬编码
     * 
     * @param libraryType 组件库类型，不能为 null
     * @return 对应的文档URL模板字符串
     * @throws IllegalArgumentException 如果组件库类型为 null
     */
    public static String getDocumentationUrlTemplate(String libraryType) {
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }
        
        // 使用动态配置管理器获取文档URL模板
        try {
            DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
            DynamicLibraryConfigManager.LibraryConfig config = configManager.getLibraryConfig(libraryType);
            if (config != null) {
                // 如果配置存在，尝试从下载的组件库中获取文档URL
                // 这里我们只需要模板，所以传入空组件名
                String docUrl = DynamicLibraryInfoProvider.getDocumentationUrl(libraryType, "");
                if (docUrl != null && !docUrl.trim().isEmpty()) {
                    return docUrl;
                }
            }
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "无法获取组件库文档URL模板: " + libraryType, e);
        }
        
        // 如果无法获取，返回空字符串
        return "";
    }

    /**
     * 检查组件是否属于指定的组件库
     * 
     * 检查逻辑：
     * 1. 验证组件名称不为空
     * 2. 获取组件库的组件前缀
     * 3. 检查组件名称是否以该前缀开头
     * 
     * @param componentName 要检查的组件名称，不能为 null 或空字符串
     * @param libraryType 组件库类型，不能为 null
     * @return 如果组件属于该组件库则返回 true，否则返回 false
     * @throws IllegalArgumentException 如果组件名称或组件库类型为 null
     */
    public static boolean isComponentFromLibrary(String componentName, String libraryType) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }

        String prefix = getComponentPrefix(libraryType);
        boolean belongsToLibrary = componentName.startsWith(prefix);
        
        VueKitLogger.debug(LOG, "检查组件 '" + componentName + "' 是否属于 '" + 
            libraryType + "' 组件库: " + belongsToLibrary);
        
        return belongsToLibrary;
    }

    /**
     * 获取组件库的配置文件路径
     * 
     * 注意：此方法已废弃，不再使用内置资源文件
     * 组件库数据现在通过远程组件库管理器动态加载
     * 
     * @param libraryType 组件库类型，不能为 null
     * @return 空字符串（不再使用内置资源文件）
     * @throws IllegalArgumentException 如果组件库类型为 null
     * @deprecated 使用远程组件库管理器替代
     */
    @Deprecated
    public static String getComponentDataPath(String libraryType) {
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }
        
        VueKitLogger.warn(LOG, "getComponentDataPath 方法已废弃，组件库数据现在通过远程组件库管理器动态加载");
        return ""; // 不再使用内置资源文件
    }

    /**
     * 打印检测信息（用于调试）
     * 
     * 调试信息包括：
     * - 项目基本信息（路径、名称等）
     * - 检测到的组件库类型
     * - 组件前缀
     * - 文档URL模板
     * - 数据文件路径
     * 
     * 注意：此方法主要用于开发和调试阶段，生产环境建议使用日志记录
     * 
     * @param project 当前项目，可以为 null
     */
    public static void printDetectionInfo(Project project) {
        VueKitLogger.info(LOG, "=== 组件库检测信息 ===");
        
        if (project == null) {
            VueKitLogger.warn(LOG, "项目对象为 null，无法获取项目信息");
            return;
        }
        
        VueKitLogger.info(LOG, "项目路径: " + project.getBasePath());
        VueKitLogger.info(LOG, "项目名称: " + project.getName());
        
        try {
            String detectedType = detectComponentLibrary(project);
            VueKitLogger.info(LOG, "检测到的组件库: " + LibraryTypeHelper.getDisplayName(detectedType));
            VueKitLogger.info(LOG, "组件前缀: " + LibraryTypeHelper.getComponentPrefix(detectedType));
            VueKitLogger.info(LOG, "文档模板: " + LibraryTypeHelper.getDocumentationUrlTemplate(detectedType));
            VueKitLogger.info(LOG, "数据来源: 远程组件库管理器");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测组件库时发生错误", e);
        }
        
        VueKitLogger.info(LOG, "=====================");
    }
}
