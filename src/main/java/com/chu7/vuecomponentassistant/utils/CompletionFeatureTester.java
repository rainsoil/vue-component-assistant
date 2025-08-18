package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.settings.ProjectSettingsManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * 补全功能测试工具类
 * 
 * 用于验证各种补全功能是否正常工作，包括：
 * - 组件补全
 * - 属性补全
 * - 事件补全
 * - 插槽补全
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class CompletionFeatureTester {
    
    private static final Logger LOG = VueKitLogger.getLogger(CompletionFeatureTester.class);
    
    /**
     * 测试所有补全功能的启用状态
     * 
     * @param project 项目对象
     * @return 测试结果字符串
     */
    public static String testAllCompletionFeatures(Project project) {
        if (project == null) {
            return "❌ 项目对象为 null";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("=== 补全功能测试报告 ===\n\n");
        
        // 测试全局设置
        result.append("🌍 全局设置:\n");
        PluginSettings globalSettings = PluginSettings.getInstance();
        result.append("  组件补全: ").append(globalSettings.isEnableComponentCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
        result.append("  属性补全: ").append(globalSettings.isEnableAttributeCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
        result.append("  事件补全: ").append(globalSettings.isEnableEventCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
        result.append("  插槽补全: ").append(globalSettings.isEnableSlotCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
        result.append("  悬停文档: ").append(globalSettings.isEnableHoverDocumentation() ? "✅ 启用" : "❌ 禁用").append("\n");
        result.append("  右键文档: ").append(globalSettings.isEnableRightClickDocumentation() ? "✅ 启用" : "❌ 禁用").append("\n\n");
        
        // 测试项目设置
        result.append("📁 项目设置:\n");
        try {
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
            
            if (projectSettings != null) {
                result.append("  组件补全: ").append(projectSettings.isEnableComponentCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
                result.append("  属性补全: ").append(projectSettings.isEnableAttributeCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
                result.append("  事件补全: ").append(projectSettings.isEnableEventCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
                result.append("  插槽补全: ").append(projectSettings.isEnableSlotCompletion() ? "✅ 启用" : "❌ 禁用").append("\n");
                result.append("  悬停文档: ").append(projectSettings.isEnableHoverDocumentation() ? "✅ 启用" : "❌ 禁用").append("\n");
                result.append("  右键文档: ").append(projectSettings.isEnableRightClickDocumentation() ? "✅ 启用" : "❌ 禁用").append("\n");
            } else {
                result.append("  ❌ 项目设置对象为 null\n");
            }
        } catch (Exception e) {
            result.append("  ❌ 获取项目设置失败: ").append(e.getMessage()).append("\n");
        }
        
        result.append("\n=== 功能状态分析 ===\n");
        
        // 分析功能状态
        boolean globalComponentEnabled = globalSettings.isEnableComponentCompletion();
        boolean globalAttributeEnabled = globalSettings.isEnableAttributeCompletion();
        boolean globalEventEnabled = globalSettings.isEnableEventCompletion();
        boolean globalSlotEnabled = globalSettings.isEnableSlotCompletion();
        
        if (globalComponentEnabled) {
            result.append("✅ 组件补全功能已启用\n");
        } else {
            result.append("❌ 组件补全功能被禁用（全局设置）\n");
        }
        
        if (globalAttributeEnabled) {
            result.append("✅ 属性补全功能已启用\n");
        } else {
            result.append("❌ 属性补全功能被禁用（全局设置）\n");
        }
        
        if (globalEventEnabled) {
            result.append("✅ 事件补全功能已启用\n");
        } else {
            result.append("❌ 事件补全功能被禁用（全局设置）\n");
        }
        
        if (globalSlotEnabled) {
            result.append("✅ 插槽补全功能已启用\n");
        } else {
            result.append("❌ 插槽补全功能被禁用（全局设置）\n");
        }
        
        result.append("\n=== 建议 ===\n");
        
        if (!globalComponentEnabled || !globalAttributeEnabled || !globalEventEnabled || !globalSlotEnabled) {
            result.append("🔧 建议在设置中启用被禁用的功能\n");
            result.append("   路径: Settings > Tools > Vue Kit > 功能设置\n");
        } else {
            result.append("✅ 所有补全功能都已启用，如果仍有问题，请检查：\n");
            result.append("   1. 项目是否正确配置了组件库\n");
            result.append("   2. 是否在正确的文件类型中使用（.vue文件）\n");
            result.append("   3. 光标位置是否正确（组件标签内、属性位置等）\n");
        }
        
        return result.toString();
    }
    
    /**
     * 重置所有补全功能为默认启用状态
     * 
     * @param project 项目对象
     * @return 是否重置成功
     */
    public static boolean resetAllCompletionFeatures(Project project) {
        if (project == null) {
            return false;
        }
        
        try {
            // 重置全局设置
            PluginSettings globalSettings = PluginSettings.getInstance();
            globalSettings.setEnableComponentCompletion(true);
            globalSettings.setEnableAttributeCompletion(true);
            globalSettings.setEnableEventCompletion(true);
            globalSettings.setEnableSlotCompletion(true);
            globalSettings.setEnableHoverDocumentation(true);
            globalSettings.setEnableRightClickDocumentation(true);
            
            // 重置项目设置
            ProjectSettingsManager projectSettingsManager = ProjectSettingsManager.getInstance(project);
            ProjectSettingsManager.ProjectSettings projectSettings = projectSettingsManager.getProjectSettings(project);
            
            if (projectSettings != null) {
                projectSettings.setEnableComponentCompletion(true);
                projectSettings.setEnableAttributeCompletion(true);
                projectSettings.setEnableEventCompletion(true);
                projectSettings.setEnableSlotCompletion(true);
                projectSettings.setEnableHoverDocumentation(true);
                projectSettings.setEnableRightClickDocumentation(true);
                
                // 保存项目设置
                projectSettingsManager.saveProjectSettings(project, projectSettings);
            }
            
            LOG.info("所有补全功能已重置为启用状态");
            return true;
            
        } catch (Exception e) {
            LOG.error("重置补全功能失败", e);
            return false;
        }
    }
}
