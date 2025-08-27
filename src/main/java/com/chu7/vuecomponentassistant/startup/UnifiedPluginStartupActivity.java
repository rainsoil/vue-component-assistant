package com.chu7.vuecomponentassistant.startup;

import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 统一插件启动活动
 * 负责在项目启动时预热组件提供者
 */
public class UnifiedPluginStartupActivity implements StartupActivity.DumbAware {
    private static final Logger LOG = Logger.getInstance(UnifiedPluginStartupActivity.class);
    
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            LOG.info("Starting Unified Vue Component Assistant for project: " + project.getName());
            
            // 预热组件提供者
            var provider = ProviderManager.getProvider(project);
            if (provider != null) {
                LOG.info("Provider initialized successfully. Component count: " + provider.getComponentCount());
            } else {
                LOG.warn("Failed to initialize provider for project: " + project.getName());
            }
            
        } catch (Exception e) {
            LOG.error("Error during plugin startup for project: " + project.getName(), e);
        }
    }
} 