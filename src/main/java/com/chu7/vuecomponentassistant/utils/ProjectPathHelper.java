package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;

public class ProjectPathHelper {
	private static final Logger LOG = VueKitLogger.getLogger(ProjectPathHelper.class);

	public static VirtualFile getProjectRoot(Project project) {
		if (project == null) {
			VueKitLogger.warn(LOG, "项目对象为 null");
			return null;
		}
		try {
			if (project.getProjectFilePath() != null) {
				String projectFilePath = project.getProjectFilePath();
				if (projectFilePath != null && !projectFilePath.isEmpty()) {
					try {
						VirtualFile projectFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(projectFilePath);
						if (projectFile != null && projectFile.getParent() != null) {
							VirtualFile projectRoot = projectFile.getParent();
							if (projectRoot != null && ".idea".equals(projectRoot.getName()) && projectRoot.getParent() != null) {
								projectRoot = projectRoot.getParent();
								return projectRoot;
							} else if (projectRoot != null) {
								return projectRoot;
							}
						}
					} catch (Exception ignored) {}
				}
			}
			try {
				java.lang.reflect.Method getBaseDirMethod = Project.class.getMethod("getBaseDir");
				if (getBaseDirMethod != null) {
					Object baseDir = getBaseDirMethod.invoke(project);
					if (baseDir instanceof VirtualFile) {
						return (VirtualFile) baseDir;
					}
				}
			} catch (Exception ignored) {}
			if (project.getProjectFilePath() != null) {
				String projectFilePath = project.getProjectFilePath();
				if (projectFilePath != null && !projectFilePath.isEmpty()) {
					try {
						VirtualFile projectFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(projectFilePath);
						if (projectFile != null) {
							VirtualFile projectRoot = projectFile.getParent();
							if (projectRoot != null && ".idea".equals(projectRoot.getName())) {
								projectRoot = projectRoot.getParent();
								if (projectRoot != null) {
									return projectRoot;
								}
							}
						}
					} catch (Exception ignored) {}
				}
			}
			return null;
		} catch (Exception e) {
			VueKitLogger.error(LOG, "获取项目根目录时发生异常", e);
			return null;
		}
	}
} 