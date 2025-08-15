package com.chu7.vuecomponentassistant.startup;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 组件库配置启动活动
 * 
 * 功能说明：
 * - 在项目启动时自动加载组件库配置
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
            
            VueKitLogger.info(LOG, "✅ 组件库配置加载完成");
            
        } catch (Exception e) {
            String errorMsg = "启动时加载组件库配置失败";
            VueKitLogger.error(LOG, errorMsg, e);
            // 启动失败不应该阻止项目正常加载，所以只记录日志
        }
    }
}
