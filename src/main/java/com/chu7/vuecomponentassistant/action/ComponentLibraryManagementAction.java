package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog;
import org.jetbrains.annotations.NotNull;

/**
 * 组件库管理Action
 * 
 * 功能说明：
 * - 打开组件库管理对话框
 * - 管理已安装的组件库
 * - 导入自定义组件库
 * - 重新加载远程组件库
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class ComponentLibraryManagementAction extends AnAction {
    
    public ComponentLibraryManagementAction() {
        super("组件库管理", "管理Vue组件库", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        // 打开组件库管理对话框
        ComponentLibraryManagementDialog dialog = new ComponentLibraryManagementDialog(project);
        dialog.show();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在有项目时才启用
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
} 