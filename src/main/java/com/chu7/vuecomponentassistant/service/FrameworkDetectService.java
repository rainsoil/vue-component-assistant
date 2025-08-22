package com.chu7.vuecomponentassistant.service;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import org.jetbrains.annotations.NotNull;



/**
 * VueKit 智能框架检测服务
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>自动检测项目使用的 UI 框架</li>
 *   <li>支持 Element Plus、Element UI、Ant Design Vue 等</li>
 *   <li>动态选择对应的组件数据和文档</li>
 *   <li>提供框架版本信息</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service(Service.Level.PROJECT)
public final class FrameworkDetectService {

    private static final Logger LOG = VueKitLogger.getLogger(FrameworkDetectService.class);

    private final Project project;
    private FrameworkType detectedFramework;
    private String frameworkVersion;
    private boolean isInitialized = false;

    public FrameworkDetectService(@NotNull Project project) {
        this.project = project;
    }

    /**
     * 获取服务实例
     */
    public static FrameworkDetectService getInstance(@NotNull Project project) {
        return project.getService(FrameworkDetectService.class);
    }

    /**
     * 检测项目使用的 UI 框架
     */
    public synchronized FrameworkType detectFramework() {
        if (isInitialized) {
            return detectedFramework;
        }

        try {
            // 直接扫描项目根目录下的 package.json 文件
            VirtualFile projectRoot = project.getBaseDir();
            VirtualFile packageJsonFile = projectRoot.findChild("package.json");

            if (packageJsonFile == null || !packageJsonFile.exists()) {
                LOG.debug("No package.json file found in project root");
                detectedFramework = FrameworkType.UNKNOWN;
                isInitialized = true;
                return detectedFramework;
            }

            // 处理单个 package.json 文件
            String content = VfsUtil.loadText(packageJsonFile);
            
            if (content.contains("element-plus")) {
                detectedFramework = FrameworkType.ELEMENT_PLUS;
                frameworkVersion = extractVersion(content, "element-plus");
                LOG.info("Detected Element Plus version: " + frameworkVersion);
            } else if (content.contains("element-ui")) {
                detectedFramework = FrameworkType.ELEMENT_UI;
                frameworkVersion = extractVersion(content, "element-ui");
                LOG.info("Detected Element UI version: " + frameworkVersion);
            } else if (content.contains("ant-design-vue")) {
                detectedFramework = FrameworkType.ANT_DESIGN_VUE;
                frameworkVersion = extractVersion(content, "ant-design-vue");
                LOG.info("Detected Ant Design Vue version: " + frameworkVersion);
            }

            if (detectedFramework == null) {
                detectedFramework = FrameworkType.UNKNOWN;
                LOG.debug("No supported UI framework detected");
            }

            isInitialized = true;
            return detectedFramework;

        } catch (Exception e) {
            LOG.error("Error detecting framework", e);
            detectedFramework = FrameworkType.UNKNOWN;
            isInitialized = true;
            return detectedFramework;
        }
    }

    /**
     * 获取检测到的框架类型
     */
    public FrameworkType getDetectedFramework() {
        if (!isInitialized) {
            detectFramework();
        }
        return detectedFramework;
    }

    /**
     * 获取框架版本
     */
    public String getFrameworkVersion() {
        if (!isInitialized) {
            detectFramework();
        }
        return frameworkVersion;
    }

    /**
     * 检查是否支持检测到的框架
     */
    public boolean isSupportedFramework() {
        FrameworkType framework = getDetectedFramework();
        return framework != FrameworkType.UNKNOWN;
    }

    /**
     * 获取框架显示名称
     */
    public String getFrameworkDisplayName() {
        FrameworkType framework = getDetectedFramework();
        switch (framework) {
            case ELEMENT_PLUS:
                return "Element Plus (" + (frameworkVersion != null ? frameworkVersion : "Latest") + ")";
            case ELEMENT_UI:
                return "Element UI (" + (frameworkVersion != null ? frameworkVersion : "Latest") + ")";
            case ANT_DESIGN_VUE:
                return "Ant Design Vue (" + (frameworkVersion != null ? frameworkVersion : "Latest") + ")";
            default:
                return "Unknown Framework";
        }
    }

    /**
     * 获取框架图标
     */
    public String getFrameworkIcon() {
        FrameworkType framework = getDetectedFramework();
        switch (framework) {
            case ELEMENT_PLUS:
                return "element-plus.svg";
            case ELEMENT_UI:
                return "element-ui.svg";
            case ANT_DESIGN_VUE:
                return "ant-design-vue.svg";
            default:
                return "component.svg";
        }
    }

    /**
     * 获取官方文档链接
     */
    public String getOfficialDocumentationUrl(String componentName) {
        FrameworkType framework = getDetectedFramework();
        switch (framework) {
            case ELEMENT_PLUS:
                return "https://cn.element-plus.org/zh-CN/component/" + componentName + ".html";
            case ELEMENT_UI:
                return "https://element.eleme.cn/#/zh-CN/component/" + componentName;
            case ANT_DESIGN_VUE:
                return "https://antdv.com/components/" + componentName;
            default:
                return null;
        }
    }

    /**
     * 从 package.json 内容中提取版本号
     */
    private String extractVersion(String content, String packageName) {
        try {
            // 简单的版本提取逻辑，可以根据需要优化
            int startIndex = content.indexOf("\"" + packageName + "\"");
            if (startIndex == -1) {
                return null;
            }
            
            int versionStart = content.indexOf("\"", startIndex + packageName.length() + 2);
            if (versionStart == -1) {
                return null;
            }
            
            int versionEnd = content.indexOf("\"", versionStart + 1);
            if (versionEnd == -1) {
                return null;
            }
            
            return content.substring(versionStart + 1, versionEnd);
        } catch (Exception e) {
            LOG.debug("Error extracting version for " + packageName, e);
            return null;
        }
    }

    /**
     * 框架类型枚举
     */
    public enum FrameworkType {
        ELEMENT_PLUS("Element Plus"),
        ELEMENT_UI("Element UI"),
        ANT_DESIGN_VUE("Ant Design Vue"),
        UNKNOWN("Unknown");

        private final String displayName;

        FrameworkType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
