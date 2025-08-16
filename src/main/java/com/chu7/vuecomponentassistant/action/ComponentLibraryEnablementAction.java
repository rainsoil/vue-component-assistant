package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.chu7.vuecomponentassistant.ui.ComponentLibraryEnablementDialog;

/**
 * 组件库启用管理动作
 *
 * 功能说明：
 * - 提供组件库启用/禁用管理功能
 * - 允许用户控制哪些组件库的组件会在代码补全中显示
 * - 支持项目级和全局级配置
 *
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentLibraryEnablementAction extends AnAction {

    public ComponentLibraryEnablementAction() {
        super("组件库启用管理", "管理组件库的启用/禁用状态", null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            Messages.showErrorDialog("无法获取项目信息", "错误");
            return;
        }

        // 打开组件库启用管理对话框
        ComponentLibraryEnablementDialog dialog = new ComponentLibraryEnablementDialog(project);
        dialog.show();
    }
} 