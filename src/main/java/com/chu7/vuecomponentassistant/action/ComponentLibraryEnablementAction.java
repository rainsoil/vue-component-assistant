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
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供组件库启用/禁用管理功能</li>
 *   <li>允许用户控制哪些组件库的组件会在代码补全中显示</li>
 *   <li>支持项目级和全局级配置管理</li>
 *   <li>提供直观的组件库状态管理界面</li>
 *   <li>支持批量启用/禁用操作</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 *   <li>支持项目上下文感知和状态管理</li>
 *   <li>用户友好的配置管理界面</li>
 *   <li>支持配置的实时预览和验证</li>
 *   <li>完整的错误处理和用户反馈</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户需要控制代码补全中显示的组件</li>
 *   <li>项目初始化后的组件库配置</li>
 *   <li>特定组件库的启用/禁用控制</li>
 *   <li>项目级组件库管理</li>
 *   <li>团队开发环境的组件库配置</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.ui.ComponentLibraryEnablementDialog
 * @see com.intellij.openapi.actionSystem.AnAction
 * @see com.intellij.openapi.project.Project
 */
public class ComponentLibraryEnablementAction extends AnAction {

    /**
     * 构造函数
     *
     * <p>初始化启用管理动作的基本信息：</p>
     * <ul>
     *   <li>动作名称：组件库启用管理</li>
     *   <li>动作描述：管理组件库的启用/禁用状态</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public ComponentLibraryEnablementAction() {
        super("组件库启用管理", "管理组件库的启用/禁用状态", null);
    }

    /**
     * 动作执行方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>验证项目上下文的有效性</li>
     *   <li>创建并显示组件库启用管理对话框</li>
     *   <li>提供组件库状态管理界面</li>
     *   <li>支持配置的保存和验证</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取当前项目上下文</li>
     *   <li>验证项目信息的有效性</li>
     *   <li>创建启用管理对话框实例</li>
     *   <li>显示对话框供用户操作</li>
     * </ol>
     *
     * @param e 动作事件，包含执行上下文和项目信息
     * @throws RuntimeException 当无法获取项目信息时抛出
     */
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