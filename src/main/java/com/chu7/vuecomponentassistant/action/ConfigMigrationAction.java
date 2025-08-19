package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.utils.ConfigMigrationUtil;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * 配置迁移动作
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>将旧的分散配置文件迁移到新的合并配置文件</li>
 *   <li>自动检测迁移需求和配置文件状态</li>
 *   <li>提供迁移状态反馈和结果报告</li>
 *   <li>支持配置文件的备份和恢复</li>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 *   <li>支持项目上下文感知和状态管理</li>
 *   <li>用户友好的迁移流程和确认机制</li>
 *   <li>完整的错误处理和日志记录</li>
 *   <li>支持配置文件的自动检测和迁移</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>项目升级后需要迁移旧配置文件</li>
 *   <li>配置文件格式变更时的数据迁移</li>
 *   <li>团队项目配置的统一化处理</li>
 *   <li>配置文件损坏后的恢复操作</li>
 *   <li>开发环境配置的标准化管理</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.utils.ConfigMigrationUtil
 * @see com.chu7.vuecomponentassistant.utils.ProjectPathHelper
 * @see com.intellij.openapi.actionSystem.AnAction
 * @see com.intellij.openapi.project.Project
 */
public class ConfigMigrationAction extends AnAction {
    
    /**
     * 日志记录器，用于记录配置迁移过程中的关键信息和错误
     */
    private static final Logger LOG = VueKitLogger.getLogger(ConfigMigrationAction.class);
    
    /**
     * 构造函数
     *
     * <p>初始化配置迁移动作的基本信息：</p>
     * <ul>
     *   <li>动作名称：🔄 配置迁移</li>
     *   <li>动作描述：将旧配置文件迁移到新的合并配置文件</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public ConfigMigrationAction() {
        super("🔄 配置迁移", "将旧配置文件迁移到新的合并配置文件", null);
    }
    
    /**
     * 动作执行方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>验证项目上下文的有效性</li>
     *   <li>显示当前配置文件路径信息</li>
     *   <li>询问用户是否执行配置迁移</li>
     *   <li>执行配置迁移操作</li>
     *   <li>提供迁移结果反馈</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取当前项目上下文</li>
     *   <li>验证项目信息的有效性</li>
     *   <li>显示配置文件路径信息</li>
     *   <li>询问用户确认迁移操作</li>
     *   <li>执行配置迁移</li>
     *   <li>显示迁移结果</li>
     * </ol>
     *
     * <p>迁移结果处理：</p>
     * <ul>
     *   <li>成功：显示新配置文件位置和删除旧文件信息</li>
     *   <li>失败：显示错误信息和手动迁移建议</li>
     *   <li>异常：记录错误日志并显示用户友好的错误信息</li>
     * </ul>
     *
     * @param e 动作事件，包含执行上下文和项目信息
     * @throws RuntimeException 当无法获取项目信息或迁移执行失败时抛出
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }
        
        try {
            // 显示配置路径信息
            String pathInfo = ConfigMigrationUtil.getConfigPathInfo(project);
            
            // 询问用户是否执行迁移
            int result = Messages.showYesNoDialog(
                pathInfo + "\n是否执行配置迁移？\n\n注意：迁移后将删除旧的配置文件。",
                "配置迁移",
                "执行迁移",
                "取消",
                Messages.getQuestionIcon()
            );
            
            if (result == Messages.YES) {
                // 执行迁移
                boolean success = ConfigMigrationUtil.migrateConfig(project);
                
                if (success) {
                    Messages.showInfoMessage(
                        "配置迁移成功！\n\n" +
                        "新的配置文件位置：\n" +
                        com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRootPath(project) + "/.idea/vuekit-project-config.json\n\n" +
                        "旧的配置文件已被删除。",
                        "迁移完成"
                    );
                } else {
                    Messages.showErrorDialog(
                        "配置迁移失败！\n\n" +
                        "请检查日志获取详细信息，或手动迁移配置文件。",
                        "迁移失败"
                    );
                }
            }
            
        } catch (Exception ex) {
            VueKitLogger.error(LOG, "配置迁移动作执行失败", ex);
            Messages.showErrorDialog(
                "配置迁移失败: " + ex.getMessage(),
                "错误"
            );
        }
    }
} 