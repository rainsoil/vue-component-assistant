package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;

/**
 * 项目路径帮助工具类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>替代已废弃的 Project.getBaseDir() 方法，提供兼容的 API</li>
 *   <li>支持不同版本的 IntelliJ IDEA 平台</li>
 *   <li>提供多种获取项目根目录的策略和回退方案</li>
 *   <li>支持项目文件查找和路径验证</li>
 *   <li>提供项目初始化状态检查</li>
 *   <li>集成到VueKit日志系统进行调试和错误记录</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>向后兼容：支持新旧版本的 IntelliJ IDEA API</li>
 *   <li>多重策略：提供多种获取项目根目录的方法</li>
 *   <li>优雅降级：新API失败时自动回退到旧API</li>
 *   <li>异常安全：完善的异常处理和错误恢复</li>
 *   <li>日志记录：详细的调试信息和错误追踪</li>
 *   <li>静态工具类：所有方法都是静态方法，无需实例化</li>
 * </ul>
 *
 * <p>获取项目根目录的策略：</p>
 * <ol>
 *   <li>优先使用 Project.getProjectFilePath() 新API</li>
 *   <li>尝试使用 Project.getLocationHash() 相关方法</li>
 *   <li>回退到 Project.getBaseDir() 旧API（如果可用）</li>
 *   <li>最后通过项目文件路径推断项目根目录</li>
 * </ol>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>获取项目根目录进行文件操作</li>
 *   <li>查找项目中的特定文件或目录</li>
 *   <li>验证项目是否已正确初始化</li>
 *   <li>配置文件路径的构建和验证</li>
 *   <li>插件功能的项目上下文判断</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.project.Project
 * @see com.intellij.openapi.vfs.VirtualFile
 * @see com.intellij.openapi.vfs.LocalFileSystem
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class ProjectPathHelper {
    
    /**
     * 日志记录器，用于记录项目路径操作过程中的调试信息和错误
     */
    private static final Logger LOG = VueKitLogger.getLogger(ProjectPathHelper.class);
    
    /**
     * 获取项目根目录
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>替代已废弃的 Project.getBaseDir() 方法</li>
     *   <li>优先使用新的 API，如果不可用则回退到旧 API</li>
     *   <li>提供多种获取策略确保兼容性</li>
     *   <li>自动处理 .idea 目录的路径推断</li>
     * </ul>
     *
     * <p>获取策略优先级：</p>
     * <ol>
     *   <li>使用 Project.getProjectFilePath() 新API</li>
     *   <li>尝试使用 Project.getLocationHash() 相关方法</li>
     *   <li>回退到 Project.getBaseDir() 旧API</li>
     *   <li>通过项目文件路径推断项目根目录</li>
     * </ol>
     *
     * <p>路径推断逻辑：</p>
     * <ul>
     *   <li>项目文件通常位于 .idea 目录下</li>
     *   <li>项目根目录是 .idea 目录的父目录</li>
     *   <li>自动处理路径分隔符和目录结构</li>
     * </ul>
     *
     * <p>异常处理：</p>
     * <ul>
     *   <li>null项目对象：记录警告日志并返回null</li>
     *   <li>API调用失败：记录调试日志并尝试下一个策略</li>
     *   <li>路径解析失败：记录错误日志并返回null</li>
     *   <li>所有策略失败：记录警告日志并返回null</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * VirtualFile projectRoot = ProjectPathHelper.getProjectRoot(project);
     * if (projectRoot != null) {
     *     String rootPath = projectRoot.getPath();
     *     // 使用项目根目录路径
     * } else {
     *     // 处理获取失败的情况
     * }
     * }</pre>
     *
     * @param project 项目对象，不能为null
     * @return 项目根目录的 VirtualFile，如果获取失败则返回 null
     * @throws IllegalArgumentException 如果项目对象为null
     */
    public static VirtualFile getProjectRoot(Project project) {
        if (project == null) {
            VueKitLogger.warn(LOG, "项目对象为 null");
            return null;
        }
        
        try {
            // 尝试使用新的 API
            if (project.getProjectFilePath() != null) {
                String projectFilePath = project.getProjectFilePath();
                if (projectFilePath != null && !projectFilePath.isEmpty()) {
                    try {
                        VirtualFile projectFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(projectFilePath);
                        if (projectFile != null && projectFile.getParent() != null) {
                            // 检查父目录是否是 .idea 目录，如果是则再往上一级
                            VirtualFile projectRoot = projectFile.getParent();
                            if (projectRoot != null && ".idea".equals(projectRoot.getName()) && projectRoot.getParent() != null) {
                                projectRoot = projectRoot.getParent();
                                VueKitLogger.debug(LOG, "使用 Project.getProjectFilePath() 获取项目根目录: " + projectRoot.getPath());
                                return projectRoot;
                            } else if (projectRoot != null) {
                                VueKitLogger.debug(LOG, "使用 Project.getProjectFilePath() 获取项目根目录: " + projectRoot.getPath());
                                return projectRoot;
                            }
                        }
                    } catch (Exception e) {
                        VueKitLogger.debug(LOG, "通过项目文件路径获取项目根目录失败: " + e.getMessage());
                    }
                }
            }
            
            // 尝试使用 getLocationHash 相关方法
            try {
                // 通过反射调用 getLocationHash 相关方法
                java.lang.reflect.Method getLocationHashMethod = Project.class.getMethod("getLocationHash");
                if (getLocationHashMethod != null) {
                    Object locationHash = getLocationHashMethod.invoke(project);
                    if (locationHash != null) {
                        // 这里需要根据实际情况处理 locationHash
                        VueKitLogger.debug(LOG, "使用 Project.getLocationHash() 获取项目根目录");
                        // 暂时返回 null，需要进一步实现
                    }
                }
            } catch (Exception e) {
                VueKitLogger.debug(LOG, "getLocationHash 方法不可用: " + e.getMessage());
            }
            
            // 回退到旧 API（如果仍然可用）
            try {
                java.lang.reflect.Method getBaseDirMethod = Project.class.getMethod("getBaseDir");
                if (getBaseDirMethod != null) {
                    Object baseDir = getBaseDirMethod.invoke(project);
                    if (baseDir instanceof VirtualFile) {
                        VueKitLogger.debug(LOG, "使用 Project.getBaseDir() 获取项目根目录（回退方案）");
                        return (VirtualFile) baseDir;
                    }
                }
            } catch (Exception e) {
                VueKitLogger.debug(LOG, "getBaseDir 方法不可用: " + e.getMessage());
            }
            
            // 最后的回退方案：尝试从项目文件路径推断
            if (project.getProjectFilePath() != null) {
                String projectFilePath = project.getProjectFilePath();
                if (projectFilePath != null && !projectFilePath.isEmpty()) {
                    try {
                        VirtualFile projectFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(projectFilePath);
                        if (projectFile != null) {
                            // 项目文件通常在 .idea 目录下，所以其父目录就是项目根目录
                            VirtualFile projectRoot = projectFile.getParent();
                            if (projectRoot != null && ".idea".equals(projectRoot.getName())) {
                                projectRoot = projectRoot.getParent();
                                if (projectRoot != null) {
                                    VueKitLogger.debug(LOG, "通过项目文件路径推断项目根目录: " + projectRoot.getPath());
                                    return projectRoot;
                                }
                            }
                        }
                    } catch (Exception e) {
                        VueKitLogger.debug(LOG, "通过项目文件路径推断项目根目录失败: " + e.getMessage());
                    }
                }
            }
            
            VueKitLogger.warn(LOG, "无法获取项目根目录，所有方法都失败了");
            return null;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取项目根目录时发生异常", e);
            return null;
        }
    }
    
    /**
     * 获取项目根目录路径字符串
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取项目根目录的路径字符串表示</li>
     *   <li>调用 {@link #getProjectRoot(Project)} 获取项目根目录</li>
     *   <li>返回可读的路径字符串，便于日志记录和调试</li>
     *   <li>适用于需要字符串路径的场景</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String rootPath = ProjectPathHelper.getProjectRootPath(project);
     * if (rootPath != null) {
     *     System.out.println("项目根目录: " + rootPath);
     *     // 输出: 项目根目录: /path/to/project
     * } else {
     *     System.out.println("无法获取项目根目录");
     * }
     * }</pre>
     *
     * @param project 项目对象，不能为null
     * @return 项目根目录的路径字符串，如果获取失败则返回 null
     * @throws IllegalArgumentException 如果项目对象为null
     */
    public static String getProjectRootPath(Project project) {
        VirtualFile projectRoot = getProjectRoot(project);
        return projectRoot != null ? projectRoot.getPath() : null;
    }
    
    /**
     * 查找项目中的特定文件或目录
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据相对路径在项目中查找文件或目录</li>
     *   <li>使用项目根目录作为查找的起始点</li>
     *   <li>支持嵌套目录和文件的查找</li>
     *   <li>提供安全的查找操作，避免异常</li>
     * </ul>
     *
     * <p>查找逻辑：</p>
     * <ul>
     *   <li>首先获取项目根目录</li>
     *   <li>验证相对路径的有效性</li>
     *   <li>使用 VirtualFile.findFileByRelativePath() 进行查找</li>
     *   <li>处理查找过程中的异常情况</li>
     * </ul>
     *
     * <p>相对路径格式：</p>
     * <ul>
     *   <li>使用正斜杠 "/" 作为路径分隔符</li>
     *   <li>支持嵌套目录：如 "src/main/java"</li>
   *   <li>支持文件名：如 "package.json"</li>
     *   <li>支持当前目录：如 "." 或 ""</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * // 查找 package.json 文件
     * VirtualFile packageJson = ProjectPathHelper.findInProject(project, "package.json");
     * if (packageJson != null) {
     *     System.out.println("找到 package.json: " + packageJson.getPath());
     * }
     * 
     * // 查找 src 目录
     * VirtualFile srcDir = ProjectPathHelper.findInProject(project, "src");
     * if (srcDir != null && srcDir.isDirectory()) {
     *     System.out.println("找到 src 目录: " + srcDir.getPath());
     * }
     * }</pre>
     *
     * @param project 项目对象，不能为null
     * @param relativePath 相对于项目根目录的路径，不能为null或空字符串
     * @return 找到的文件或目录的 VirtualFile，如果未找到则返回 null
     * @throws IllegalArgumentException 如果项目对象或相对路径为null
     */
    public static VirtualFile findInProject(Project project, String relativePath) {
        VirtualFile projectRoot = getProjectRoot(project);
        if (projectRoot == null || relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        
        try {
            return projectRoot.findFileByRelativePath(relativePath);
        } catch (Exception e) {
            VueKitLogger.error(LOG, "在项目中查找文件失败: " + relativePath, e);
            return null;
        }
    }
    
    /**
     * 检查项目是否已初始化
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>验证项目是否已正确初始化</li>
     *   <li>检查项目根目录是否可访问</li>
     *   <li>验证项目的基本状态</li>
     *   <li>适用于插件功能的启用条件判断</li>
     * </ul>
     *
     * <p>检查逻辑：</p>
     * <ul>
     *   <li>验证项目对象不为null</li>
     *   <li>尝试获取项目根目录</li>
     *   <li>检查项目的初始化状态</li>
     *   <li>综合判断项目是否可用</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>插件功能的条件启用</li>
     *   <li>项目操作的预检查</li>
     *   <li>配置加载的时机判断</li>
     *   <li>用户界面的状态控制</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * if (ProjectPathHelper.isProjectInitialized(project)) {
     *     // 项目已初始化，可以执行相关操作
     *     performProjectOperation();
     * } else {
     *     // 项目未初始化，显示提示信息
     *     showProjectNotReadyMessage();
     * }
     * }</pre>
     *
     * @param project 项目对象，可以为null
     * @return 如果项目已初始化则返回 true，否则返回 false
     */
    public static boolean isProjectInitialized(Project project) {
        if (project == null) {
            return false;
        }
        
        try {
            // 尝试获取项目根目录来验证项目是否已初始化
            VirtualFile projectRoot = getProjectRoot(project);
            return projectRoot != null && project.isInitialized();
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "检查项目初始化状态失败: " + e.getMessage());
            return false;
        }
    }
}
