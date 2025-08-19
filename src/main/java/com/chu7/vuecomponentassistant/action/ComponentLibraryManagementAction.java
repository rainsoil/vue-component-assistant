package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog;
import org.jetbrains.annotations.NotNull;

/**
 * 组件库管理Action
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>打开组件库管理对话框</li>
 *   <li>管理已安装的组件库</li>
 *   <li>导入自定义组件库</li>
 *   <li>重新加载远程组件库</li>
 *   <li>配置组件库设置</li>
 * </ul>
 * 
 * <p>触发方式：</p>
 * <ul>
 *   <li>Tools菜单 -> Vue Kit -> 组件库管理</li>
 *   <li>快捷键：无（可通过设置配置）</li>
 *   <li>右键菜单：无</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>开发者需要管理项目使用的组件库</li>
 *   <li>导入自定义的组件库配置</li>
 *   <li>更新远程组件库信息</li>
 *   <li>配置组件库的优先级和设置</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 3.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog
 * @see com.intellij.openapi.actionSystem.AnAction
 */
public class ComponentLibraryManagementAction extends AnAction {
    
    /**
     * 构造函数
     * 
     * <p>初始化Action的基本信息：</p>
     * <ul>
     *   <li>显示名称：组件库管理</li>
     *   <li>描述：管理Vue组件库</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public ComponentLibraryManagementAction() {
        super("组件库管理", "管理Vue组件库", null);
    }
    
    /**
     * 执行Action的主要逻辑
     * 
     * <p>该方法会执行以下操作：</p>
     * <ol>
     *   <li>获取当前项目对象</li>
     *   <li>验证项目有效性</li>
     *   <li>创建并显示组件库管理对话框</li>
     *   <li>处理用户交互</li>
     * </ol>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>只有在有效项目中才能执行</li>
     *   <li>对话框会阻塞当前操作直到关闭</li>
     *   <li>支持模态和非模态显示</li>
     * </ul>
     * 
     * @param e Action事件对象，包含触发Action的上下文信息，不能为null
     * @throws IllegalArgumentException 如果事件对象为null
     * 
     * @see com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog
     * @see com.intellij.openapi.actionSystem.AnActionEvent
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("Action事件对象不能为null");
        }
        
        // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            // 如果没有项目，记录警告并返回
            return;
        }
        
        // 验证项目是否有效
        if (project.isDisposed()) {
            return;
        }
        
        // 打开组件库管理对话框
        ComponentLibraryManagementDialog dialog = new ComponentLibraryManagementDialog(project);
        dialog.show();
    }
    
    /**
     * 更新Action的可用状态
     * 
     * <p>该方法用于动态控制Action的启用/禁用状态：</p>
     * <ul>
     *   <li>只有在有项目时才启用</li>
     *   <li>项目无效时自动禁用</li>
     *   <li>支持运行时状态更新</li>
     * </ul>
     * 
     * <p>更新逻辑：</p>
     * <ol>
     *   <li>检查是否有有效的项目</li>
     *   <li>设置Action的启用状态</li>
     *   <li>更新显示状态</li>
     * </ol>
     * 
     * @param e Action事件对象，包含当前上下文信息，不能为null
     * @throws IllegalArgumentException 如果事件对象为null
     * 
     * @see com.intellij.openapi.actionSystem.AnActionEvent#getProject()
     * @see com.intellij.openapi.actionSystem.Presentation#setEnabledAndVisible(boolean)
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("Action事件对象不能为null");
        }
        
        // 只有在有项目时才启用
        boolean hasProject = e.getProject() != null;
        e.getPresentation().setEnabledAndVisible(hasProject);
    }
} 