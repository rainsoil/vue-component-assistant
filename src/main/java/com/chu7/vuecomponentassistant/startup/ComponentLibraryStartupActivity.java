package com.chu7.vuecomponentassistant.startup;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.PackageJsonAutoDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 组件库配置启动活动
 * 
 * 功能说明：
 * - 在项目启动时自动加载组件库配置
 * - 自动检测项目中的 package.json 文件并匹配组件库
 * - 在首次使用时自动启用匹配到的组件库
 * - 使用 StartupActivity 接口（兼容性支持）
 * - 确保组件库配置在项目完全加载后生效
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class ComponentLibraryStartupActivity implements StartupActivity {
    
    private static final Logger LOG = Logger.getInstance(ComponentLibraryStartupActivity.class);
    
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            VueKitLogger.info(LOG, "=== 启动组件库配置加载 ===");
            VueKitLogger.info(LOG, "项目名称: " + project.getName());
            VueKitLogger.info(LOG, "项目路径: " + project.getBasePath());
            
            // 获取组件库配置管理器
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            
            // 触发项目启动时的配置加载
            configManager.onProjectStarted(project);
            
            // 检查并确保配置文件存在
            ensureProjectConfigFileExists(project);
            
            // 自动检测并启用项目中的组件库
            boolean autoDetected = PackageJsonAutoDetector.autoDetectAndEnableLibraries(project);
            if (autoDetected) {
                VueKitLogger.info(LOG, "✅ 自动检测并启用组件库成功");
            } else {
                VueKitLogger.info(LOG, "ℹ️ 跳过自动检测（配置文件已存在或无匹配的组件库）");
            }
            
            VueKitLogger.info(LOG, "✅ 组件库配置加载完成");
            
        } catch (Exception e) {
            String errorMsg = "启动时加载组件库配置失败";
            VueKitLogger.error(LOG, errorMsg, e);
            // 启动失败不应该阻止项目正常加载，所以只记录日志
        }
    }
    
    /**
     * 确保项目配置文件存在
     * 
     * @param project 项目对象
     */
    private void ensureProjectConfigFileExists(Project project) {
        try {
            VueKitLogger.info(LOG, "检查项目配置文件是否存在...");
            
            // 检查配置文件是否存在且有效
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            if (!configManager.hasValidProjectConfig(project)) {
                VueKitLogger.info(LOG, "项目配置文件不存在或无效，尝试生成...");
                
                // 尝试自动检测并生成配置文件
                boolean success = PackageJsonAutoDetector.autoDetectAndEnableLibraries(project);
                if (success) {
                    VueKitLogger.info(LOG, "✅ 项目配置文件生成成功");
                } else {
                    VueKitLogger.info(LOG, "⚠️ 项目配置文件生成失败，将使用默认配置");
                    // 创建空的配置文件，确保后续操作正常进行
                    createEmptyProjectConfig(project);
                }
            } else {
                VueKitLogger.info(LOG, "✅ 项目配置文件已存在且有效");
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "确保项目配置文件存在时发生错误", e);
        }
    }
    
    /**
     * 创建空的项目配置文件
     * 
     * @param project 项目对象
     */
    private void createEmptyProjectConfig(Project project) {
        try {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            // 创建空的配置
            Set<String> emptyConfig = new HashSet<>();
            configManager.setProjectEnabledLibraryNames(project, emptyConfig);
            VueKitLogger.info(LOG, "已创建空的项目配置文件");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "创建空项目配置文件失败", e);
        }
    }
}
