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
 * 功能说明：
 * - 将旧的分散配置文件迁移到新的合并配置文件
 * - 自动检测迁移需求
 * - 提供迁移状态反馈
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ConfigMigrationAction extends AnAction {
    
    private static final Logger LOG = VueKitLogger.getLogger(ConfigMigrationAction.class);
    
    public ConfigMigrationAction() {
        super("🔄 配置迁移", "将旧配置文件迁移到新的合并配置文件", null);
    }
    
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
                        project.getBaseDir().getPath() + "/.idea/vuekit-project-config.json\n\n" +
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