package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.SmartComponentFilter;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.ConfigMigrationUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.util.Set;

/**
 * 组件库调试动作
 * 
 * 功能说明：
 * - 显示当前组件库配置状态
 * - 显示 ComponentProvider 状态
 * - 显示检测到的项目组件库
 * - 帮助诊断组件过滤问题
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentLibraryDebugAction extends AnAction {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryDebugAction.class);
    
    public ComponentLibraryDebugAction() {
        super("🔍 组件库调试", "显示组件库配置和过滤状态", null);
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        try {
            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== 组件库调试信息 ===\n\n");
            
            // 1. 检查用户配置的启用组件库
            debugInfo.append("1. 用户配置的启用组件库:\n");
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            Set<ComponentLibraryDetector.LibraryType> enabledLibraries = configManager.getEnabledLibraries(project);
            if (enabledLibraries.isEmpty()) {
                debugInfo.append("   ❌ 没有启用任何组件库\n");
            } else {
                for (ComponentLibraryDetector.LibraryType library : enabledLibraries) {
                    debugInfo.append("   ✅ ").append(library.getDisplayName()).append(" (").append(library.name()).append(")\n");
                }
            }
            debugInfo.append("\n");
            
            // 2. 检查检测到的项目组件库
            debugInfo.append("2. 检测到的项目组件库:\n");
            SmartComponentFilter filter = new SmartComponentFilter();
            Set<ComponentLibraryDetector.LibraryType> projectLibraries = filter.detectProjectLibraries(project);
            if (projectLibraries.isEmpty()) {
                debugInfo.append("   ❌ 没有检测到项目组件库\n");
            } else {
                for (ComponentLibraryDetector.LibraryType library : projectLibraries) {
                    debugInfo.append("   ✅ ").append(library.getDisplayName()).append(" (").append(library.name()).append(")\n");
                }
            }
            debugInfo.append("\n");
            
            // 3. 检查 ComponentProvider 状态
            debugInfo.append("3. ComponentProvider 状态:\n");
            ComponentProvider provider = ComponentProviderManager.getProvider(project);
            if (provider == null) {
                debugInfo.append("   ❌ ComponentProvider 未初始化\n");
            } else {
                int componentCount = provider.getAllComponents().size();
                debugInfo.append("   ✅ ComponentProvider 已初始化\n");
                debugInfo.append("   📊 当前组件数量: ").append(componentCount).append("\n");
                
                // 检查组件前缀分布
                debugInfo.append("   📋 组件前缀分布:\n");
                int elCount = 0, aCount = 0, vCount = 0, qCount = 0, otherCount = 0;
                for (var component : provider.getAllComponents()) {
                    String name = component.getName();
                    if (name.startsWith("el-")) elCount++;
                    else if (name.startsWith("a-")) aCount++;
                    else if (name.startsWith("v-")) vCount++;
                    else if (name.startsWith("q-")) qCount++;
                    else otherCount++;
                }
                debugInfo.append("      el- 前缀: ").append(elCount).append(" 个\n");
                debugInfo.append("      a- 前缀: ").append(aCount).append(" 个\n");
                debugInfo.append("      v- 前缀: ").append(vCount).append(" 个\n");
                debugInfo.append("      q- 前缀: ").append(qCount).append(" 个\n");
                debugInfo.append("      其他: ").append(otherCount).append(" 个\n");
            }
            debugInfo.append("\n");
            
            // 4. 检查过滤逻辑
            debugInfo.append("4. 过滤逻辑分析:\n");
            if (enabledLibraries.isEmpty()) {
                debugInfo.append("   ⚠️ 没有启用任何组件库，根据 SmartComponentFilter 逻辑，应该显示所有组件\n");
                debugInfo.append("   📝 这是导致问题的原因：用户禁用了所有组件库，但过滤逻辑仍然显示所有组件\n");
            } else {
                debugInfo.append("   ✅ 已启用组件库，过滤逻辑应该正常工作\n");
            }
            debugInfo.append("\n");
            
            // 5. 配置文件路径信息
            debugInfo.append("5. 配置文件路径信息:\n");
            debugInfo.append(ConfigMigrationUtil.getConfigPathInfo(project));
            
            // 6. 建议
            debugInfo.append("6. 建议:\n");
            if (enabledLibraries.isEmpty()) {
                debugInfo.append("   💡 如果不想看到任何组件，请确保在组件库启用管理中明确禁用了所有组件库\n");
                debugInfo.append("   💡 如果想看到特定组件库的组件，请在组件库启用管理中启用相应的组件库\n");
            }
            debugInfo.append("   💡 如果问题仍然存在，请检查 SmartComponentFilter 的过滤逻辑\n");
            debugInfo.append("   💡 如果存在旧配置文件，建议使用配置迁移工具进行迁移\n");
            
            // 显示调试信息
            Messages.showInfoMessage(
                debugInfo.toString(),
                "组件库调试信息 - " + project.getName()
            );
            
        } catch (Exception ex) {
            VueKitLogger.error(LOG, "生成调试信息失败", ex);
            Messages.showErrorDialog(
                "生成调试信息失败: " + ex.getMessage(),
                "错误"
            );
        }
    }
} 