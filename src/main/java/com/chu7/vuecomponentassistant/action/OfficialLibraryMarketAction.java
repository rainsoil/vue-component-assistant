package com.chu7.vuecomponentassistant.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import org.jetbrains.annotations.NotNull;

/**
 * 官方组件库市场动作
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>打开官方组件库市场对话框</li>
 *   <li>浏览和下载官方维护的组件库</li>
 *   <li>支持组件库的搜索和筛选功能</li>
 *   <li>提供组件库的预览和安装功能</li>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>集成到 IntelliJ IDEA 菜单系统</li>
 *   <li>支持项目上下文感知和状态管理</li>
 *   <li>用户友好的市场浏览界面</li>
 *   <li>支持组件库的在线浏览和下载</li>
 *   <li>完整的错误处理和用户反馈</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>用户需要浏览官方组件库</li>
 *   <li>项目需要安装新的组件库</li>
 *   <li>开发者需要了解可用的组件库</li>
 *   <li>团队需要统一组件库版本</li>
 *   <li>组件库的版本更新和升级</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 3.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.intellij.openapi.actionSystem.AnAction
 * @see com.intellij.openapi.project.Project
 */
public class OfficialLibraryMarketAction extends AnAction {
    
    /**
     * 构造函数
     *
     * <p>初始化官方组件库市场动作的基本信息：</p>
     * <ul>
     *   <li>动作名称：官方组件库市场</li>
     *   <li>动作描述：浏览和下载官方组件库</li>
     *   <li>图标：使用默认图标</li>
     * </ul>
     */
    public OfficialLibraryMarketAction() {
        super("官方组件库市场", "浏览和下载官方组件库", null);
    }
    
    /**
     * 动作执行方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>验证项目上下文的有效性</li>
     *   <li>创建组件库管理器实例</li>
     *   <li>打开官方组件库市场对话框</li>
     *   <li>提供组件库浏览和下载功能</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取当前项目上下文</li>
     *   <li>验证项目信息的有效性</li>
     *   <li>创建组件库管理器</li>
     *   <li>打开市场对话框</li>
     * </ol>
     *
     * @param e 动作事件，包含执行上下文和项目信息
     * @throws RuntimeException 当无法获取项目信息时抛出
     */
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
    
    /**
     * 更新动作状态
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据项目上下文动态启用/禁用动作</li>
     *   <li>只有在有项目时才启用此动作</li>
     *   <li>确保动作在正确的上下文中执行</li>
     * </ul>
     *
     * @param e 动作事件，包含项目上下文信息
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // 只有在有项目时才启用
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
} 