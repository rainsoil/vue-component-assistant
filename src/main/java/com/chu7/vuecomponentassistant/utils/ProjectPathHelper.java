package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;

/**
 * 项目路径帮助工具类
 * 
 * 替代已废弃的 Project.getBaseDir() 方法，提供兼容的 API
 * 支持不同版本的 IntelliJ IDEA
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ProjectPathHelper {
    
    private static final Logger LOG = VueKitLogger.getLogger(ProjectPathHelper.class);
    
    /**
     * 获取项目根目录
     * 
     * 替代已废弃的 Project.getBaseDir() 方法
     * 优先使用新的 API，如果不可用则回退到旧 API
     * 
     * @param project 项目对象
     * @return 项目根目录的 VirtualFile，如果获取失败则返回 null
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
     * @param project 项目对象
     * @return 项目根目录的路径字符串，如果获取失败则返回 null
     */
    public static String getProjectRootPath(Project project) {
        VirtualFile projectRoot = getProjectRoot(project);
        return projectRoot != null ? projectRoot.getPath() : null;
    }
    
    /**
     * 查找项目中的特定文件或目录
     * 
     * @param project 项目对象
     * @param relativePath 相对于项目根目录的路径
     * @return 找到的文件或目录的 VirtualFile，如果未找到则返回 null
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
     * @param project 项目对象
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
