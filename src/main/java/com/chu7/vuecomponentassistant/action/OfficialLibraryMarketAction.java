package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import org.jetbrains.annotations.NotNull;

/**
 * 官方组件库市场Action
 * 
 * 功能说明：
 * - 打开官方组件库市场对话框
 * - 浏览和下载官方组件库
 * - 搜索和筛选组件库
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class OfficialLibraryMarketAction extends AnAction {
    
    public OfficialLibraryMarketAction() {
        super("官方组件库市场", "浏览和下载官方组件库", null);
    }
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        // 创建组件库管理器
        ComponentLibraryManager libraryManager = new ComponentLibraryManager();
        
        // 打开官方组件库市场对话框
        OfficialLibraryMarketDialog dialog = new OfficialLibraryMarketDialog(project, libraryManager);
        dialog.show();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在有项目时才启用
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
} 