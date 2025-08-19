package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * 统一错误处理器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供统一的错误处理和日志记录机制</li>
 *   <li>支持异常捕获、日志记录和用户提示</li>
 *   <li>提供多种消息对话框类型</li>
 *   <li>支持不同级别的日志记录</li>
 *   <li>统一的错误处理接口</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>工具类设计，所有方法都是静态方法</li>
 *   <li>支持可选的用户消息显示</li>
 *   <li>统一的日志记录格式</li>
 *   <li>多种消息对话框类型支持</li>
 *   <li>线程安全的静态方法</li>
 * </ul>
 * 
 * <p>错误处理策略：</p>
 * <ol>
 *   <li>记录详细的错误日志</li>
 *   <li>可选显示用户友好的错误消息</li>
 *   <li>支持不同级别的日志记录</li>
 *   <li>统一的异常处理流程</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库加载失败处理</li>
 *   <li>网络请求异常处理</li>
 *   <li>文件操作错误处理</li>
 *   <li>配置解析错误处理</li>
 *   <li>用户操作错误提示</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.ui.Messages
 * @see com.intellij.openapi.diagnostic.Logger
 */
public class ErrorHandler {
    
    /**
     * 日志记录器
     * 用于记录错误处理过程中的关键信息和异常
     */
    private static final Logger LOG = Logger.getInstance(ErrorHandler.class);
    
    /**
     * 处理异常并记录日志
     * 
     * <p>该方法提供完整的异常处理流程：</p>
     * <ol>
     *   <li>记录详细的错误日志（包含异常堆栈）</li>
     *   <li>根据参数决定是否显示用户消息</li>
     *   <li>使用统一的错误处理格式</li>
     * </ol>
     * 
     * <p>处理流程：</p>
     * <ul>
     *   <li>使用LOG.error记录错误日志</li>
     *   <li>检查showUserMessage参数</li>
     *   <li>如果需要，调用showErrorMessage显示用户消息</li>
     * </ul>
     * 
     * @param message 错误消息，不能为null
     * @param exception 异常对象，不能为null
     * @param showUserMessage 是否显示用户消息
     * @throws IllegalArgumentException 如果message或exception为null
     * 
     * @see #showErrorMessage(String, String)
     * @see com.intellij.openapi.diagnostic.Logger#error(String, Throwable)
     */
    public static void handleException(String message, Exception exception, boolean showUserMessage) {
        if (message == null) {
            throw new IllegalArgumentException("错误消息不能为null");
        }
        if (exception == null) {
            throw new IllegalArgumentException("异常对象不能为null");
        }
        
        // 记录错误日志
        LOG.error(message, exception);
        
        // 如果需要，显示用户友好的错误消息
        if (showUserMessage) {
            showErrorMessage("VueKit", message);
        }
    }
    
    /**
     * 处理异常并记录日志（不显示用户消息）
     * 
     * <p>该方法用于处理不需要向用户显示消息的异常。
     * 只记录日志，不显示对话框。</p>
     * 
     * @param message 错误消息，不能为null
     * @param exception 异常对象，不能为null
     * @throws IllegalArgumentException 如果message或exception为null
     * 
     * @see #handleException(String, Exception, boolean)
     */
    public static void handleException(String message, Exception exception) {
        handleException(message, exception, false);
    }
    
    /**
     * 显示错误消息对话框
     * 
     * <p>该方法用于向用户显示错误消息，使用IntelliJ IDEA的
     * 标准错误对话框，提供一致的用户体验。</p>
     * 
     * @param title 对话框标题，不能为null
     * @param message 错误消息，不能为null
     * @throws IllegalArgumentException 如果title或message为null
     * 
     * @see com.intellij.openapi.ui.Messages#showErrorDialog(String, String)
     */
    public static void showErrorMessage(String title, String message) {
        if (title == null) {
            throw new IllegalArgumentException("对话框标题不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("错误消息不能为null");
        }
        
        Messages.showErrorDialog(message, title);
    }
    
    /**
     * 显示警告消息对话框
     * 
     * <p>该方法用于向用户显示警告消息，使用IntelliJ IDEA的
     * 标准警告对话框，提供一致的用户体验。</p>
     * 
     * @param title 对话框标题，不能为null
     * @param message 警告消息，不能为null
     * @throws IllegalArgumentException 如果title或message为null
     * 
     * @see com.intellij.openapi.ui.Messages#showWarningDialog(String, String)
     */
    public static void showWarningMessage(String title, String message) {
        if (title == null) {
            throw new IllegalArgumentException("对话框标题不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("警告消息不能为null");
        }
        
        Messages.showWarningDialog(message, title);
    }
    
    /**
     * 显示信息消息对话框
     * 
     * <p>该方法用于向用户显示信息消息，使用IntelliJ IDEA的
     * 标准信息对话框，提供一致的用户体验。</p>
     * 
     * @param title 对话框标题，不能为null
     * @param message 信息消息，不能为null
     * @throws IllegalArgumentException 如果title或message为null
     * 
     * @see com.intellij.openapi.ui.Messages#showInfoMessage(String, String)
     */
    public static void showInfoMessage(String title, String message) {
        if (title == null) {
            throw new IllegalArgumentException("对话框标题不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("信息消息不能为null");
        }
        
        Messages.showInfoMessage(message, title);
    }
    
    // ==================== 日志记录方法 ====================
    
    /**
     * 记录调试信息
     * 
     * <p>该方法用于记录调试级别的日志信息，
     * 通常在开发调试时使用。</p>
     * 
     * @param message 调试消息，不能为null
     * @throws IllegalArgumentException 如果message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#debug(String)
     */
    public static void logDebug(String message) {
        if (message == null) {
            throw new IllegalArgumentException("调试消息不能为null");
        }
        LOG.debug(message);
    }
    
    /**
     * 记录信息日志
     * 
     * <p>该方法用于记录信息级别的日志，
     * 记录正常的操作流程和状态信息。</p>
     * 
     * @param message 信息消息，不能为null
     * @throws IllegalArgumentException 如果message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#info(String)
     */
    public static void logInfo(String message) {
        if (message == null) {
            throw new IllegalArgumentException("信息消息不能为null");
        }
        LOG.info(message);
    }
    
    /**
     * 记录警告日志
     * 
     * <p>该方法用于记录警告级别的日志，
     * 记录需要注意但不影响功能的问题。</p>
     * 
     * @param message 警告消息，不能为null
     * @throws IllegalArgumentException 如果message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#warn(String)
     */
    public static void logWarning(String message) {
        if (message == null) {
            throw new IllegalArgumentException("警告消息不能为null");
        }
        LOG.warn(message);
    }
    
    /**
     * 记录错误日志
     * 
     * <p>该方法用于记录错误级别的日志，
     * 记录功能异常和错误信息。</p>
     * 
     * @param message 错误消息，不能为null
     * @throws IllegalArgumentException 如果message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#error(String)
     */
    public static void logError(String message) {
        if (message == null) {
            throw new IllegalArgumentException("错误消息不能为null");
        }
        LOG.error(message);
    }
    
    /**
     * 记录错误日志（带异常）
     * 
     * <p>该方法用于记录错误级别的日志，包含异常对象。
     * 提供完整的错误信息和堆栈跟踪。</p>
     * 
     * @param message 错误消息，不能为null
     * @param exception 异常对象，不能为null
     * @throws IllegalArgumentException 如果message或exception为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#error(String, Throwable)
     */
    public static void logError(String message, Throwable exception) {
        if (message == null) {
            throw new IllegalArgumentException("错误消息不能为null");
        }
        if (exception == null) {
            throw new IllegalArgumentException("异常对象不能为null");
        }
        
        LOG.error(message, exception);
    }
} 