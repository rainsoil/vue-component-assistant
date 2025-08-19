package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.completion2.ComponentProvider;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.SmartComponentFilter;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.ConfigMigrationUtil;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;
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
 * <p>功能说明：</p>
 * <ul>
 *   <li>显示当前组件库配置状态和启用情况</li>
 *   <li>显示 ComponentProvider 的初始化和组件数量状态</li>
 *   <li>显示检测到的项目组件库信息</li>
 *   <li>分析组件过滤逻辑和问题诊断</li>
 *   <li>提供配置文件路径信息和迁移建议</li>
 *   <li>帮助开发者诊断组件库相关问题</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>全面的调试信息收集和分析</li>
 *   <li>结构化的信息展示和分类</li>
 *   <li>智能的问题诊断和建议提供</li>
 *   <li>用户友好的界面和信息展示</li>
 *   <li>完整的异常处理和错误反馈</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库功能异常时的诊断和排查</li>
 *   <li>组件过滤逻辑问题的分析</li>
 *   <li>配置状态验证和检查</li>
 *   <li>开发调试和问题复现</li>
 *   <li>用户配置问题的技术支持</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProvider
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProviderManager
 * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager
 * @see com.chu7.vuecomponentassistant.utils.SmartComponentFilter
 */
public class ComponentLibraryDebugAction extends AnAction {
    
    /**
     * 日志记录器，用于记录调试动作执行过程中的关键信息和错误
     */
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryDebugAction.class);
    
    /**
     * 构造函数
     *
     * <p>初始化调试动作的基本信息：</p>
     * <ul>
     *   <li>动作名称：🔍 组件库调试</li>
     *   <li>动作描述：显示组件库配置和过滤状态</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public ComponentLibraryDebugAction() {
        super("🔍 组件库调试", "显示组件库配置和过滤状态", null);
    }
    
    /**
     * 动作执行方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>收集项目组件库配置状态信息</li>
     *   <li>分析组件提供者状态和组件分布</li>
     *   <li>检测项目中的组件库</li>
     *   <li>分析过滤逻辑和潜在问题</li>
     *   <li>提供诊断建议和解决方案</li>
     * </ul>
     *
     * <p>调试信息收集流程：</p>
     * <ol>
     *   <li>验证项目上下文的有效性</li>
     *   <li>检查用户配置的启用组件库</li>
     *   <li>检测项目中的组件库</li>
     *   <li>分析 ComponentProvider 状态</li>
     *   <li>分析过滤逻辑和问题</li>
     *   <li>提供配置路径信息和建议</li>
     * </ol>
     *
     * <p>调试信息分类：</p>
     * <ul>
     *   <li>用户配置状态：启用的组件库列表</li>
     *   <li>项目检测状态：自动检测到的组件库</li>
     *   <li>组件提供者状态：初始化状态和组件数量</li>
     *   <li>过滤逻辑分析：问题诊断和原因分析</li>
     *   <li>配置路径信息：配置文件位置和迁移状态</li>
     *   <li>解决建议：针对发现问题的具体建议</li>
     * </ul>
     *
     * @param e 动作事件，包含执行上下文和项目信息
     * @throws RuntimeException 当无法获取项目信息或调试信息生成失败时抛出
     */
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
            Set<String> enabledLibraries = configManager.getEnabledLibraryNames(project);
            if (enabledLibraries.isEmpty()) {
                debugInfo.append("   ❌ 没有启用任何组件库\n");
            } else {
                for (String library : enabledLibraries) {
                    debugInfo.append("   ✅ ").append(LibraryTypeHelper.getDisplayName(library)).append(" (").append(library).append(")\n");
                }
            }
            debugInfo.append("\n");
            
            // 2. 检查检测到的项目组件库
            debugInfo.append("2. 检测到的项目组件库:\n");
            SmartComponentFilter filter = new SmartComponentFilter();
            Set<String> projectLibraries = filter.detectProjectLibraries(project);
            if (projectLibraries.isEmpty()) {
                debugInfo.append("   ❌ 没有检测到项目组件库\n");
            } else {
                for (String library : projectLibraries) {
                    debugInfo.append("   ✅ ").append(LibraryTypeHelper.getDisplayName(library)).append(" (").append(library).append(")\n");
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