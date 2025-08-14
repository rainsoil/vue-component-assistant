package com.chu7.vuecomponentassistant.notification;

import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * VueKit 通知管理器
 * 
 * 提供用户友好的通知和错误提示功能：
 * - 错误通知
 * - 成功通知
 * - 警告通知
 * - 信息通知
 * - 带操作的通知
 * - 进度通知
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class VueKitNotificationManager {
    
    private static final Logger LOG = VueKitLogger.getLogger(VueKitNotificationManager.class);
    
    // 通知组
    private static final NotificationGroup NOTIFICATION_GROUP = 
        NotificationGroupManager.getInstance().getNotificationGroup("VueKit Notifications");
    
    // 单例实例
    private static volatile VueKitNotificationManager instance;
    
    private VueKitNotificationManager() {
        VueKitLogger.debug(LOG, "VueKit通知管理器初始化完成");
    }
    
    /**
     * 获取单例实例
     */
    public static VueKitNotificationManager getInstance() {
        if (instance == null) {
            synchronized (VueKitNotificationManager.class) {
                if (instance == null) {
                    instance = new VueKitNotificationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 显示错误通知
     * 
     * @param project 项目
     * @param title 标题
     * @param content 内容
     */
    public void showError(@Nullable Project project, @NotNull String title, @NotNull String content) {
        showNotification(project, title, content, NotificationType.ERROR, null);
        VueKitLogger.error(LOG, "错误通知: " + title + " - " + content);
    }
    
    /**
     * 显示错误通知（带异常）
     */
    public void showError(@Nullable Project project, @NotNull String title, @NotNull String content, @NotNull Throwable throwable) {
        String detailedContent = content + "\n详细信息: " + throwable.getMessage();
        showNotification(project, title, detailedContent, NotificationType.ERROR, null);
        VueKitLogger.error(LOG, "错误通知: " + title + " - " + content, throwable);
    }
    
    /**
     * 显示成功通知
     */
    public void showSuccess(@Nullable Project project, @NotNull String title, @NotNull String content) {
        showNotification(project, title, content, NotificationType.INFORMATION, null);
        VueKitLogger.info(LOG, "成功通知: " + title + " - " + content);
    }
    
    /**
     * 显示警告通知
     */
    public void showWarning(@Nullable Project project, @NotNull String title, @NotNull String content) {
        showNotification(project, title, content, NotificationType.WARNING, null);
        VueKitLogger.warn(LOG, "警告通知: " + title + " - " + content);
    }
    
    /**
     * 显示信息通知
     */
    public void showInfo(@Nullable Project project, @NotNull String title, @NotNull String content) {
        showNotification(project, title, content, NotificationType.INFORMATION, null);
        VueKitLogger.info(LOG, "信息通知: " + title + " - " + content);
    }
    
    /**
     * 显示带操作的通知
     */
    public void showNotificationWithAction(@Nullable Project project, 
                                         @NotNull String title, 
                                         @NotNull String content,
                                         @NotNull NotificationType type,
                                         @NotNull String actionText,
                                         @NotNull Runnable action) {
        
        AnAction notificationAction = new AnAction(actionText) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                try {
                    action.run();
                } catch (Exception ex) {
                    VueKitLogger.error(LOG, "通知操作执行失败", ex);
                    showError(project, "操作失败", "执行操作时发生错误: " + ex.getMessage());
                }
            }
        };
        
        showNotification(project, title, content, type, notificationAction);
    }
    
    /**
     * 显示组件库检测错误
     */
    public void showComponentLibraryError(@Nullable Project project, @NotNull String libraryName, @NotNull String error) {
        String title = "组件库加载失败";
        String content = String.format("无法加载组件库 '%s'。\n错误: %s\n\n请检查项目配置或联系技术支持。", libraryName, error);
        
        showNotificationWithAction(project, title, content, NotificationType.ERROR, "重新检测", () -> {
            // 触发重新检测组件库
            VueKitLogger.info(LOG, "用户触发重新检测组件库");
            // TODO: 实现重新检测逻辑
        });
    }
    
    /**
     * 显示缓存清理通知
     */
    public void showCacheClearedNotification(@Nullable Project project, int clearedItems) {
        String title = "缓存已清理";
        String content = String.format("成功清理了 %d 个缓存项，插件性能已优化。", clearedItems);
        showSuccess(project, title, content);
    }
    
    /**
     * 显示设置保存成功通知
     */
    public void showSettingsSavedNotification(@Nullable Project project) {
        String title = "设置已保存";
        String content = "VueKit 设置已成功保存并应用。";
        showSuccess(project, title, content);
    }
    
    /**
     * 显示自定义组件库添加成功通知
     */
    public void showCustomLibraryAddedNotification(@Nullable Project project, @NotNull String libraryName, int componentCount) {
        String title = "自定义组件库已添加";
        String content = String.format("成功添加自定义组件库 '%s'，包含 %d 个组件。", libraryName, componentCount);
        showSuccess(project, title, content);
    }
    
    /**
     * 显示性能警告通知
     */
    public void showPerformanceWarning(@Nullable Project project, @NotNull String operation, long duration) {
        String title = "性能警告";
        String content = String.format("操作 '%s' 耗时较长 (%d ms)。\n建议清理缓存或检查项目配置以提高性能。", operation, duration);
        
        showNotificationWithAction(project, title, content, NotificationType.WARNING, "清理缓存", () -> {
            // 触发缓存清理
            VueKitLogger.info(LOG, "用户触发缓存清理");
            // TODO: 实现缓存清理逻辑
        });
    }
    
    /**
     * 显示更新可用通知
     */
    public void showUpdateAvailableNotification(@Nullable Project project, @NotNull String currentVersion, @NotNull String newVersion) {
        String title = "VueKit 更新可用";
        String content = String.format("发现新版本 %s（当前版本 %s）。\n新版本包含性能改进和bug修复。", newVersion, currentVersion);
        
        showNotificationWithAction(project, title, content, NotificationType.INFORMATION, "查看更新", () -> {
            // 打开更新页面
            VueKitLogger.info(LOG, "用户查看更新信息");
            // TODO: 实现打开更新页面逻辑
        });
    }
    
    /**
     * 显示首次使用欢迎通知
     */
    public void showWelcomeNotification(@Nullable Project project) {
        String title = "欢迎使用 VueKit";
        String content = "感谢使用 VueKit！\n这是一个强大的 Vue 组件开发助手，提供智能补全、文档查看等功能。";
        
        showNotificationWithAction(project, title, content, NotificationType.INFORMATION, "查看设置", () -> {
            // 打开设置页面
            VueKitLogger.info(LOG, "用户打开设置页面");
            // TODO: 实现打开设置页面逻辑
        });
    }
    
    /**
     * 显示数据验证错误通知
     */
    public void showDataValidationError(@Nullable Project project, @NotNull String dataType, @NotNull String error) {
        String title = "数据验证失败";
        String content = String.format("数据类型: %s\n错误: %s\n\n为了安全起见，已拒绝加载此数据。", dataType, error);
        
        showNotificationWithAction(project, title, content, NotificationType.ERROR, "查看详情", () -> {
            // 显示详细错误信息
            VueKitLogger.info(LOG, "用户查看数据验证错误详情");
            showDetailedErrorDialog(project, "数据验证错误详情", error);
        });
    }
    
    /**
     * 显示加载进度通知
     */
    public void showLoadingNotification(@Nullable Project project, @NotNull String operation) {
        String title = "正在加载";
        String content = String.format("正在执行: %s\n请稍候...", operation);
        showInfo(project, title, content);
    }
    
    /**
     * 核心通知显示方法
     */
    private void showNotification(@Nullable Project project, 
                                @NotNull String title, 
                                @NotNull String content,
                                @NotNull NotificationType type,
                                @Nullable AnAction action) {
        try {
            Notification notification = NOTIFICATION_GROUP.createNotification(title, content, type);
            
            if (action != null) {
                notification.addAction(action);
            }
            
            // 设置通知图标
            notification.setIcon(getNotificationIcon(type));
            
            // 显示通知
            notification.notify(project);
            
            VueKitLogger.debug(LOG, String.format("显示通知: %s - %s (类型: %s)", title, content, type));
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "显示通知失败", e);
            // 作为fallback，使用系统默认通知
            fallbackNotification(title, content, type);
        }
    }
    
    /**
     * 获取通知图标
     */
    @Nullable
    private javax.swing.Icon getNotificationIcon(@NotNull NotificationType type) {
        // 根据通知类型返回相应图标
        // 这里可以返回自定义图标，或者返回null使用默认图标
        return null;
    }
    
    /**
     * 备用通知方法
     */
    private void fallbackNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type) {
        try {
            // 使用IntelliJ的Messages类显示简单对话框
            switch (type) {
                case ERROR:
                    com.intellij.openapi.ui.Messages.showErrorDialog(
                        content, 
                        VueKitConstants.PLUGIN_DISPLAY_NAME + " - " + title
                    );
                    break;
                case WARNING:
                    com.intellij.openapi.ui.Messages.showWarningDialog(
                        content, 
                        VueKitConstants.PLUGIN_DISPLAY_NAME + " - " + title
                    );
                    break;
                case INFORMATION:
                default:
                    com.intellij.openapi.ui.Messages.showInfoMessage(
                        content, 
                        VueKitConstants.PLUGIN_DISPLAY_NAME + " - " + title
                    );
                    break;
            }
        } catch (Exception e) {
            VueKitLogger.error(LOG, "备用通知也失败了", e);
        }
    }

    
    /**
     * 显示详细错误对话框
     */
    private void showDetailedErrorDialog(@Nullable Project project, @NotNull String title, @NotNull String error) {
        try {
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                error,
                title
            );
        } catch (Exception e) {
            VueKitLogger.error(LOG, "显示详细错误对话框失败", e);
        }
    }
}