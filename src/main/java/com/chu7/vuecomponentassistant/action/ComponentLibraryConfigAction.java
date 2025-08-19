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
 * <p>功能说明：</p>
 * <ul>
 *   <li>在 IntelliJ IDEA 菜单中添加组件库配置选项</li>
 *   <li>点击后打开组件库配置对话框</li>
 *   <li>支持快捷键绑定和上下文感知</li>
 *   <li>智能显示/隐藏菜单项</li>
 *   <li>提供项目级组件库配置管理</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 *   <li>支持项目上下文感知和状态管理</li>
 *   <li>用户友好的操作体验和错误处理</li>
 *   <li>完整的日志记录和异常处理</li>
 *   <li>支持配置的保存和验证</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户需要配置项目组件库的启用状态</li>
 *   <li>项目初始化后的组件库配置</li>
 *   <li>组件库功能的开关控制</li>
 *   <li>项目级组件库管理</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.ui.ComponentLibraryConfigDialog
 * @see com.intellij.openapi.actionSystem.AnAction
 * @see com.intellij.openapi.project.Project
 */
public class ComponentLibraryConfigAction extends AnAction {
    
    /**
     * 日志记录器，用于记录动作执行过程中的关键信息
     */
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigAction.class);
    
    /**
     * 构造函数
     *
     * <p>初始化动作的基本信息：</p>
     * <ul>
     *   <li>动作名称：组件库配置</li>
     *   <li>动作描述：配置 VueKit 组件库的启用状态</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public ComponentLibraryConfigAction() {
        super("组件库配置", "配置 VueKit 组件库的启用状态", null);
    }
    
    /**
     * 动作执行方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取当前项目上下文</li>
     *   <li>创建并显示组件库配置对话框</li>
     *   <li>处理配置保存结果</li>
     *   <li>提供用户操作反馈</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>验证项目上下文的有效性</li>
     *   <li>创建配置对话框实例</li>
     *   <li>显示对话框并等待用户操作</li>
     *   <li>根据用户操作结果提供反馈</li>
     * </ol>
     *
     * @param e 动作事件，包含执行上下文和项目信息
     * @throws RuntimeException 当无法获取项目信息或对话框创建失败时抛出
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据项目上下文动态启用/禁用动作</li>
     *   <li>更新动作的可见性和可用性</li>
     *   <li>动态设置动作描述信息</li>
     * </ul>
     *
     * <p>状态判断逻辑：</p>
     * <ul>
     *   <li>项目存在且已初始化：启用并显示动作</li>
     *   <li>项目不存在或未初始化：禁用并隐藏动作</li>
     *   <li>异常情况：默认禁用动作</li>
     * </ul>
     *
     * @param e 动作事件，包含项目上下文信息
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
     * <p>返回动作的详细功能描述，用于：</p>
     * <ul>
     *   <li>用户界面显示</li>
     *   <li>帮助文档生成</li>
     *   <li>功能说明文档</li>
     * </ul>
     *
     * @return 动作的详细功能描述
     */
    public String getDescription() {
        return "配置 VueKit 组件库的启用状态，支持项目级和全局级配置管理";
    }
}
