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
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供用户友好的通知和错误提示功能</li>
 *   <li>支持多种通知类型：错误、成功、警告、信息</li>
 *   <li>支持带操作的通知，用户可直接执行相关操作</li>
 *   <li>提供进度通知和加载状态提示</li>
 *   <li>集成VueKit日志系统进行通知记录</li>
 *   <li>支持备用通知机制，确保通知功能可靠性</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>单例模式：确保全局唯一的通知管理器实例</li>
 *   <li>线程安全：使用双重检查锁定实现线程安全</li>
 *   <li>类型化通知：支持IntelliJ IDEA的NotificationType枚举</li>
 *   <li>操作集成：支持在通知中添加可点击的操作按钮</li>
 *   <li>错误处理：完善的异常处理和备用机制</li>
 *   <li>日志记录：所有通知操作都会记录到VueKit日志系统</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库加载失败时的错误提示</li>
 *   <li>操作成功后的成功反馈</li>
 *   <li>性能警告和优化建议</li>
 *   <li>插件更新和功能通知</li>
 *   <li>首次使用时的欢迎引导</li>
 *   <li>数据验证错误的用户提示</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.notification.NotificationGroup
 * @see com.intellij.notification.NotificationType
 * @see com.intellij.openapi.actionSystem.AnAction
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class VueKitNotificationManager {
    
    private static final Logger LOG = VueKitLogger.getLogger(VueKitNotificationManager.class);
    
    // 通知组
    private static final NotificationGroup NOTIFICATION_GROUP = 
        NotificationGroupManager.getInstance().getNotificationGroup("VueKit Notifications");
    
    // 单例实例
    private static volatile VueKitNotificationManager instance;
    
    /**
     * 私有构造函数
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>初始化VueKit通知管理器</li>
     *   <li>记录初始化完成日志</li>
     *   <li>支持单例模式实现</li>
     * </ul>
     *
     * <p>设计说明：</p>
     * <ul>
     *   <li>私有访问修饰符确保外部无法直接实例化</li>
     *   <li>使用VueKit日志系统记录初始化状态</li>
     *   <li>为单例模式提供安全的初始化入口</li>
     * </ul>
     *
     * @see #getInstance()
     */
    private VueKitNotificationManager() {
        VueKitLogger.debug(LOG, "VueKit通知管理器初始化完成");
    }
    
    /**
     * 获取单例实例
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>提供全局唯一的VueKit通知管理器实例</li>
     *   <li>支持延迟初始化，避免不必要的资源消耗</li>
     *   <li>确保多线程环境下的线程安全</li>
     * </ul>
     *
     * <p>线程安全：</p>
     * <ul>
     *   <li>使用双重检查锁定（Double-Checked Locking）模式</li>
     *   <li>volatile关键字确保instance变量的可见性</li>
     *   <li>synchronized块确保实例创建的原子性</li>
     * </ul>
     *
     * @return VueKit通知管理器的单例实例
     * @see #VueKitNotificationManager()
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示错误类型的通知消息</li>
     *   <li>自动记录错误日志到VueKit日志系统</li>
     *   <li>使用ERROR通知类型，显示红色错误图标</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>组件库加载失败</li>
     *   <li>配置文件解析错误</li>
     *   <li>网络请求失败</li>
     *   <li>系统异常和错误</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @throws IllegalArgumentException 如果title或content参数为null
     * @see #showNotification(Project, String, String, NotificationType, AnAction)
     * @see com.intellij.notification.NotificationType#ERROR
     */
    public void showError(@Nullable Project project, @NotNull String title, @NotNull String content) {
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        
        showNotification(project, title, content, NotificationType.ERROR, null);
        VueKitLogger.error(LOG, "错误通知: " + title + " - " + content);
    }
    
    /**
     * 显示错误通知（带异常）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示包含异常详细信息的错误通知</li>
     *   <li>自动提取异常消息并添加到通知内容中</li>
     *   <li>记录完整的异常堆栈信息到日志系统</li>
     * </ul>
     *
     * <p>异常处理：</p>
     * <ul>
     *   <li>自动提取异常消息</li>
     *   <li>格式化异常信息为用户友好的格式</li>
     *   <li>记录异常堆栈到VueKit日志系统</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @param throwable 异常对象，不能为null
     * @throws IllegalArgumentException 如果title、content或throwable参数为null
     * @see #showError(Project, String, String)
     * @see #showNotification(Project, String, String, NotificationType, AnAction)
     */
    public void showError(@Nullable Project project, @NotNull String title, @NotNull String content, @NotNull Throwable throwable) {
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        if (throwable == null) {
            throw new IllegalArgumentException("异常对象不能为null");
        }
        
        String detailedContent = content + "\n详细信息: " + throwable.getMessage();
        showNotification(project, title, detailedContent, NotificationType.ERROR, null);
        VueKitLogger.error(LOG, "错误通知: " + title + " - " + content, throwable);
    }
    
    /**
     * 显示成功通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示成功类型的通知消息</li>
     *   <li>自动记录成功日志到VueKit日志系统</li>
     *   <li>使用INFORMATION通知类型，显示蓝色信息图标</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>操作成功完成</li>
     *   <li>设置保存成功</li>
     *   <li>组件库加载成功</li>
     *   <li>缓存清理完成</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @throws IllegalArgumentException 如果title或content参数为null
     * @see #showNotification(Project, String, String, NotificationType, AnAction)
     * @see com.intellij.notification.NotificationType#INFORMATION
     */
    public void showSuccess(@Nullable Project project, @NotNull String title, @NotNull String content) {
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        
        showNotification(project, title, content, NotificationType.INFORMATION, null);
        VueKitLogger.info(LOG, "成功通知: " + title + " - " + content);
    }
    
    /**
     * 显示警告通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示警告类型的通知消息</li>
     *   <li>自动记录警告日志到VueKit日志系统</li>
     *   <li>使用WARNING通知类型，显示黄色警告图标</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>性能警告</li>
     *   <li>配置建议</li>
     *   <li>潜在问题提醒</li>
     *   <li>优化建议</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @throws IllegalArgumentException 如果title或content参数为null
     * @see #showNotification(Project, String, String, NotificationType, AnAction)
     * @see com.intellij.notification.NotificationType#WARNING
     */
    public void showWarning(@Nullable Project project, @NotNull String title, @NotNull String content) {
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        
        showNotification(project, title, content, NotificationType.WARNING, null);
        VueKitLogger.warn(LOG, "警告通知: " + title + " - " + content);
    }
    
    /**
     * 显示信息通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示信息类型的通知消息</li>
     *   <li>自动记录信息日志到VueKit日志系统</li>
     *   <li>使用INFORMATION通知类型，显示蓝色信息图标</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>一般信息提示</li>
     *   <li>状态更新</li>
     *   <li>进度信息</li>
     *   <li>帮助提示</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @throws IllegalArgumentException 如果title或content参数为null
     * @see #showNotification(Project, String, String, NotificationType, AnAction)
     * @see com.intellij.notification.NotificationType#INFORMATION
     */
    public void showInfo(@Nullable Project project, @NotNull String title, @NotNull String content) {
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        
        showNotification(project, title, content, NotificationType.INFORMATION, null);
        VueKitLogger.info(LOG, "信息通知: " + title + " - " + content);
    }
    
    /**
     * 显示带操作的通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示包含可点击操作按钮的通知</li>
     *   <li>支持自定义操作文本和执行逻辑</li>
     *   <li>自动处理操作执行过程中的异常</li>
     *   <li>提供操作失败时的错误反馈</li>
     * </ul>
     *
     * <p>操作特性：</p>
     * <ul>
     *   <li>异常安全：操作执行失败时自动显示错误通知</li>
     *   <li>用户反馈：操作结果通过通知反馈给用户</li>
     *   <li>日志记录：所有操作执行都会记录到日志系统</li>
     *   <li>类型灵活：支持任意NotificationType类型</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @param type 通知类型，不能为null
     * @param actionText 操作按钮文本，不能为null
     * @param action 操作执行逻辑，不能为null
     * @throws IllegalArgumentException 如果任何参数为null
     * @see #showNotification(Project, String, String, NotificationType, AnAction)
     * @see com.intellij.openapi.actionSystem.AnAction
     * @see com.intellij.notification.NotificationType
     */
    public void showNotificationWithAction(@Nullable Project project, 
                                         @NotNull String title, 
                                         @NotNull String content,
                                         @NotNull NotificationType type,
                                         @NotNull String actionText,
                                         @NotNull Runnable action) {
        
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        if (type == null) {
            throw new IllegalArgumentException("通知类型不能为null");
        }
        if (actionText == null) {
            throw new IllegalArgumentException("操作文本不能为null");
        }
        if (action == null) {
            throw new IllegalArgumentException("操作逻辑不能为null");
        }
        
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示组件库加载失败的错误通知</li>
     *   <li>提供"重新检测"操作按钮</li>
     *   <li>自动格式化错误信息为用户友好格式</li>
     *   <li>记录错误日志和用户操作</li>
     * </ul>
     *
     * <p>错误处理：</p>
     * <ul>
     *   <li>格式化错误信息，包含组件库名称和具体错误</li>
     *   <li>提供技术支持联系信息</li>
     *   <li>支持用户触发重新检测操作</li>
     *   <li>记录所有操作到日志系统</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param libraryName 组件库名称，不能为null
     * @param error 错误信息，不能为null
     * @throws IllegalArgumentException 如果libraryName或error参数为null
     * @see #showNotificationWithAction(Project, String, String, NotificationType, String, Runnable)
     */
    public void showComponentLibraryError(@Nullable Project project, @NotNull String libraryName, @NotNull String error) {
        if (libraryName == null) {
            throw new IllegalArgumentException("组件库名称不能为null");
        }
        if (error == null) {
            throw new IllegalArgumentException("错误信息不能为null");
        }
        
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示缓存清理成功的通知</li>
     *   <li>包含清理的缓存项数量信息</li>
     *   <li>提示用户性能已优化</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param clearedItems 清理的缓存项数量
     * @see #showSuccess(Project, String, String)
     */
    public void showCacheClearedNotification(@Nullable Project project, int clearedItems) {
        String title = "缓存已清理";
        String content = String.format("成功清理了 %d 个缓存项，插件性能已优化。", clearedItems);
        showSuccess(project, title, content);
    }
    
    /**
     * 显示设置保存成功通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示设置保存成功的通知</li>
     *   <li>确认设置已应用</li>
     *   <li>提供用户操作反馈</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @see #showSuccess(Project, String, String)
     */
    public void showSettingsSavedNotification(@Nullable Project project) {
        String title = "设置已保存";
        String content = "VueKit 设置已成功保存并应用。";
        showSuccess(project, title, content);
    }
    
    /**
     * 显示自定义组件库添加成功通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示自定义组件库添加成功的通知</li>
     *   <li>包含组件库名称和组件数量信息</li>
     *   <li>确认操作成功完成</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param libraryName 组件库名称，不能为null
     * @param componentCount 组件数量
     * @throws IllegalArgumentException 如果libraryName参数为null
     * @see #showSuccess(Project, String, String)
     */
    public void showCustomLibraryAddedNotification(@Nullable Project project, @NotNull String libraryName, int componentCount) {
        if (libraryName == null) {
            throw new IllegalArgumentException("组件库名称不能为null");
        }
        
        String title = "自定义组件库已添加";
        String content = String.format("成功添加自定义组件库 '%s'，包含 %d 个组件。", libraryName, componentCount);
        showSuccess(project, title, content);
    }
    
    /**
     * 显示性能警告通知
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示操作耗时过长的性能警告</li>
     *   <li>包含具体操作名称和耗时信息</li>
     *   <li>提供"清理缓存"操作按钮</li>
     *   <li>建议用户进行性能优化</li>
     * </ul>
     *
     * <p>性能监控：</p>
     * <ul>
     *   <li>记录操作耗时信息</li>
     *   <li>提供性能优化建议</li>
     *   <li>支持快速缓存清理操作</li>
     *   <li>记录用户优化操作</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param operation 操作名称，不能为null
     * @param duration 操作耗时（毫秒）
     * @throws IllegalArgumentException 如果operation参数为null
     * @see #showNotificationWithAction(Project, String, String, NotificationType, String, Runnable)
     */
    public void showPerformanceWarning(@Nullable Project project, @NotNull String operation, long duration) {
        if (operation == null) {
            throw new IllegalArgumentException("操作名称不能为null");
        }
        
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示插件更新可用的通知</li>
     *   <li>包含当前版本和新版本信息</li>
     *   <li>提供"查看更新"操作按钮</li>
     *   <li>说明新版本的改进内容</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param currentVersion 当前版本，不能为null
     * @param newVersion 新版本，不能为null
     * @throws IllegalArgumentException 如果currentVersion或newVersion参数为null
     * @see #showNotificationWithAction(Project, String, String, NotificationType, String, Runnable)
     */
    public void showUpdateAvailableNotification(@Nullable Project project, @NotNull String currentVersion, @NotNull String newVersion) {
        if (currentVersion == null) {
            throw new IllegalArgumentException("当前版本不能为null");
        }
        if (newVersion == null) {
            throw new IllegalArgumentException("新版本不能为null");
        }
        
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示新用户首次使用时的欢迎通知</li>
     *   <li>介绍VueKit的主要功能</li>
     *   <li>提供"查看设置"操作按钮</li>
     *   <li>帮助用户快速上手</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @see #showNotificationWithAction(Project, String, String, NotificationType, String, Runnable)
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示数据验证失败的错误通知</li>
     *   <li>包含数据类型和具体错误信息</li>
     *   <li>提供"查看详情"操作按钮</li>
     *   <li>说明安全措施（拒绝加载无效数据）</li>
     * </ul>
     *
     * <p>安全特性：</p>
     * <ul>
     *   <li>自动拒绝加载验证失败的数据</li>
     *   <li>提供详细的错误信息查看</li>
     *   <li>记录所有验证失败事件</li>
     *   <li>保护系统免受无效数据影响</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param dataType 数据类型，不能为null
     * @param error 错误信息，不能为null
     * @throws IllegalArgumentException 如果dataType或error参数为null
     * @see #showNotificationWithAction(Project, String, String, NotificationType, String, Runnable)
     * @see #showDetailedErrorDialog(Project, String, String)
     */
    public void showDataValidationError(@Nullable Project project, @NotNull String dataType, @NotNull String error) {
        if (dataType == null) {
            throw new IllegalArgumentException("数据类型不能为null");
        }
        if (error == null) {
            throw new IllegalArgumentException("错误信息不能为null");
        }
        
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示操作正在进行的进度通知</li>
     *   <li>包含具体操作名称</li>
     *   <li>提示用户等待操作完成</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param operation 操作名称，不能为null
     * @throws IllegalArgumentException 如果operation参数为null
     * @see #showInfo(Project, String, String)
     */
    public void showLoadingNotification(@Nullable Project project, @NotNull String operation) {
        if (operation == null) {
            throw new IllegalArgumentException("操作名称不能为null");
        }
        
        String title = "正在加载";
        String content = String.format("正在执行: %s\n请稍候...", operation);
        showInfo(project, title, content);
    }
    
    /**
     * 核心通知显示方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>创建和显示IntelliJ IDEA通知</li>
     *   <li>支持添加操作按钮</li>
     *   <li>设置通知图标和样式</li>
     *   <li>处理通知显示异常</li>
     * </ul>
     *
     * <p>异常处理：</p>
     * <ul>
     *   <li>捕获通知显示异常</li>
     *   <li>自动回退到备用通知机制</li>
     *   <li>记录所有异常到日志系统</li>
     *   <li>确保通知功能可靠性</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局通知）
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @param type 通知类型，不能为null
     * @param action 操作按钮，可能为null
     * @see #fallbackNotification(String, String, NotificationType)
     * @see com.intellij.notification.Notification
     * @see com.intellij.notification.NotificationGroup#createNotification(String, String, NotificationType)
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据通知类型返回相应的图标</li>
     *   <li>支持自定义图标设置</li>
     *   <li>可扩展支持更多图标类型</li>
     * </ul>
     *
     * <p>图标策略：</p>
     * <ul>
     *   <li>当前返回null使用IntelliJ IDEA默认图标</li>
     *   <li>可扩展返回自定义图标资源</li>
     *   <li>支持根据通知类型返回不同图标</li>
     * </ul>
     *
     * @param type 通知类型，不能为null
     * @return 通知图标，当前返回null使用默认图标
     * @throws IllegalArgumentException 如果type参数为null
     * @see com.intellij.notification.NotificationType
     */
    @Nullable
    private javax.swing.Icon getNotificationIcon(@NotNull NotificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("通知类型不能为null");
        }
        
        // 根据通知类型返回相应图标
        // 这里可以返回自定义图标，或者返回null使用默认图标
        return null;
    }
    
    /**
     * 备用通知方法
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>当主要通知机制失败时的备用方案</li>
     *   <li>使用IntelliJ IDEA的Messages类显示对话框</li>
     *   <li>确保通知功能在异常情况下的可用性</li>
     * </ul>
     *
     * <p>备用策略：</p>
     * <ul>
     *   <li>ERROR类型：显示错误对话框</li>
     *   <li>WARNING类型：显示警告对话框</li>
     *   <li>INFORMATION类型：显示信息对话框</li>
     *   <li>包含插件名称前缀</li>
     * </ul>
     *
     * @param title 通知标题，不能为null
     * @param content 通知内容，不能为null
     * @param type 通知类型，不能为null
     * @throws IllegalArgumentException 如果任何参数为null
     * @see com.intellij.openapi.ui.Messages
     * @see VueKitConstants#PLUGIN_DISPLAY_NAME
     */
    private void fallbackNotification(@NotNull String title, @NotNull String content, @NotNull NotificationType type) {
        if (title == null) {
            throw new IllegalArgumentException("通知标题不能为null");
        }
        if (content == null) {
            throw new IllegalArgumentException("通知内容不能为null");
        }
        if (type == null) {
            throw new IllegalArgumentException("通知类型不能为null");
        }
        
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>显示包含详细错误信息的对话框</li>
     *   <li>使用IntelliJ IDEA的Messages类</li>
     *   <li>提供错误详情的查看界面</li>
     * </ul>
     *
     * <p>错误展示：</p>
     * <ul>
     *   <li>模态对话框显示</li>
     *   <li>包含错误标题和详细内容</li>
     *   <li>支持项目上下文</li>
     *   <li>异常安全的错误处理</li>
     * </ul>
     *
     * @param project 项目对象，可能为null（全局对话框）
     * @param title 对话框标题，不能为null
     * @param error 错误详情，不能为null
     * @throws IllegalArgumentException 如果title或error参数为null
     * @see com.intellij.openapi.ui.Messages#showErrorDialog(Project, String, String)
     */
    private void showDetailedErrorDialog(@Nullable Project project, @NotNull String title, @NotNull String error) {
        if (title == null) {
            throw new IllegalArgumentException("对话框标题不能为null");
        }
        if (error == null) {
            throw new IllegalArgumentException("错误详情不能为null");
        }
        
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