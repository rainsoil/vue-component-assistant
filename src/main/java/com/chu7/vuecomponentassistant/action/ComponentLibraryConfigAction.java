package com.chu7.vuecomponentassistant.action;

import com.chu7.vuecomponentassistant.ui.ComponentLibraryConfigDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;

/**
 * 组件库配置菜单动作
 * 
 * 功能说明：
 * - 在 IntelliJ IDEA 菜单中添加组件库配置选项
 * - 点击后打开组件库配置对话框
 * - 支持快捷键绑定
 * - 智能显示/隐藏菜单项
 * 
 * 特性：
 * - 集成到 IntelliJ IDEA 菜单系统
 * - 支持项目上下文感知
 * - 用户友好的操作体验
 * - 错误处理和用户反馈
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryConfigAction extends AnAction {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigAction.class);
    
    /**
     * 构造函数
     */
    public ComponentLibraryConfigAction() {
        super("组件库配置", "配置 VueKit 组件库的启用状态", null);
    }
    
    /**
     * 动作执行
     * 
     * @param e 动作事件
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            // 获取项目对象
            Project project = e.getData(CommonDataKeys.PROJECT);
            if (project == null) {
                VueKitLogger.warn(LOG, "无法获取项目对象");
                Messages.showErrorDialog("无法获取项目信息，请确保在项目上下文中执行此操作。", "错误");
                return;
            }
            
            VueKitLogger.info(LOG, "用户请求打开组件库配置对话框，项目: " + project.getName());
            
            // 创建并显示配置对话框
            ComponentLibraryConfigDialog dialog = new ComponentLibraryConfigDialog(project);
            
            if (dialog.showAndGet()) {
                VueKitLogger.info(LOG, "组件库配置已保存");
                Messages.showInfoMessage(project, "组件库配置已保存！", "成功");
            } else {
                VueKitLogger.debug(LOG, "用户取消了组件库配置");
            }
            
        } catch (Exception ex) {
            VueKitLogger.error(LOG, "执行组件库配置动作失败", ex);
            Messages.showErrorDialog("打开组件库配置失败: " + ex.getMessage(), "错误");
        }
    }
    
    /**
     * 更新动作状态
     * 
     * @param e 动作事件
     */
    @Override
    public void update(AnActionEvent e) {
        try {
            // 获取项目对象
            Project project = e.getData(CommonDataKeys.PROJECT);
            
            // 只有在项目上下文中才启用此动作
            if (project != null && project.isInitialized()) {
                e.getPresentation().setEnabledAndVisible(true);
                
                // 设置动作描述
                e.getPresentation().setDescription("配置 " + project.getName() + " 项目的组件库");
                
            } else {
                e.getPresentation().setEnabledAndVisible(false);
            }
            
        } catch (Exception ex) {
            VueKitLogger.error(LOG, "更新组件库配置动作状态失败", ex);
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
    

    
    /**
     * 获取动作描述
     * 
     * @return 动作描述
     */
    public String getDescription() {
        return "配置 VueKit 组件库的启用状态，支持项目级和全局级配置管理";
    }
}
