package com.chu7.vuecomponentassistant.completion2;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ComponentProvider 管理器
 * 
 * 功能说明：
 * - 管理项目中所有的 ComponentProvider 实例
 * - 当组件库发生变化时，通知所有 ComponentProvider 重新加载数据
 * - 确保组件库的增删改操作能够及时反映到补全提示中
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentProviderManager {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentProviderManager.class);
    
    // 使用 ConcurrentHashMap 确保线程安全
    private static final Map<Project, ComponentProvider> providers = new ConcurrentHashMap<>();
    
    /**
     * 获取或创建项目的 ComponentProvider
     */
    public static ComponentProvider getProvider(Project project) {
        return providers.computeIfAbsent(project, ComponentProvider::new);
    }
    
    /**
     * 注册项目的 ComponentProvider
     */
    public static void registerProvider(Project project, ComponentProvider provider) {
        providers.put(project, provider);
        VueKitLogger.debug(LOG, "注册项目的 ComponentProvider: " + project.getName());
    }
    
    /**
     * 移除项目的 ComponentProvider
     */
    public static void removeProvider(Project project) {
        providers.remove(project);
        VueKitLogger.debug(LOG, "移除项目的 ComponentProvider: " + project.getName());
    }
    
    /**
     * 通知所有 ComponentProvider 重新加载组件数据
     * 当组件库发生变化时调用此方法
     */
    public static void notifyAllProvidersReload() {
        VueKitLogger.debug(LOG, "通知所有 ComponentProvider 重新加载数据");
        
        for (ComponentProvider provider : providers.values()) {
            try {
                provider.reloadComponents();
            } catch (Exception e) {
                VueKitLogger.error(LOG, "重新加载 ComponentProvider 失败: " + e.getMessage(), e);
            }
        }
        
        VueKitLogger.debug(LOG, "所有 ComponentProvider 重新加载完成，共 " + providers.size() + " 个实例");
    }
    
    /**
     * 通知指定项目的 ComponentProvider 重新加载组件数据
     */
    public static void notifyProviderReload(Project project) {
        ComponentProvider provider = providers.get(project);
        if (provider != null) {
            try {
                VueKitLogger.debug(LOG, "通知项目 " + project.getName() + " 的 ComponentProvider 重新加载数据");
                provider.reloadComponents();
            } catch (Exception e) {
                VueKitLogger.error(LOG, "重新加载项目 " + project.getName() + " 的 ComponentProvider 失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取当前管理的 ComponentProvider 数量
     */
    public static int getProviderCount() {
        return providers.size();
    }
    
    /**
     * 清空所有 ComponentProvider
     */
    public static void clearAllProviders() {
        providers.clear();
        VueKitLogger.debug(LOG, "清空所有 ComponentProvider");
    }
} 