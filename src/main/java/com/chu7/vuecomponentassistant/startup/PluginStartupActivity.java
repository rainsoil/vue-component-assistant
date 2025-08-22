package com.chu7.vuecomponentassistant.startup;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 插件启动活动 - 确认插件正确加载
 */
public class PluginStartupActivity implements StartupActivity {

    private static final Logger LOG = VueKitLogger.getLogger(PluginStartupActivity.class);

    static {
        System.out.println("=== PluginStartupActivity 类被加载 ===");
        LOG.info("=== PluginStartupActivity 类被加载 ===");
    }

    @Override
    public void runActivity(@NotNull Project project) {
        System.out.println("=== VueKit 插件启动成功 for project: " + project.getName() + " ===");
        LOG.info("=== VueKit 插件启动成功 for project: " + project.getName() + " ===");
    }
}
