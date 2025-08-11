package bak.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;

/**
 * 统一错误处理器
 * 
 * 提供统一的错误处理和日志记录机制
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ErrorHandler {
    
    private static final Logger LOG = Logger.getInstance(ErrorHandler.class);
    
    /**
     * 处理异常并记录日志
     * 
     * @param message 错误消息
     * @param exception 异常对象
     * @param showUserMessage 是否显示用户消息
     */
    public static void handleException(String message, Exception exception, boolean showUserMessage) {
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
     * @param message 错误消息
     * @param exception 异常对象
     */
    public static void handleException(String message, Exception exception) {
        handleException(message, exception, false);
    }
    
    /**
     * 显示错误消息对话框
     * 
     * @param title 对话框标题
     * @param message 错误消息
     */
    public static void showErrorMessage(String title, String message) {
        Messages.showErrorDialog(message, title);
    }
    
    /**
     * 显示警告消息对话框
     * 
     * @param title 对话框标题
     * @param message 警告消息
     */
    public static void showWarningMessage(String title, String message) {
        Messages.showWarningDialog(message, title);
    }
    
    /**
     * 显示信息消息对话框
     * 
     * @param title 对话框标题
     * @param message 信息消息
     */
    public static void showInfoMessage(String title, String message) {
        Messages.showInfoMessage(message, title);
    }
    
    /**
     * 记录调试信息
     * 
     * @param message 调试消息
     */
    public static void logDebug(String message) {
        LOG.debug(message);
    }
    
    /**
     * 记录信息日志
     * 
     * @param message 信息消息
     */
    public static void logInfo(String message) {
        LOG.info(message);
    }
    
    /**
     * 记录警告日志
     * 
     * @param message 警告消息
     */
    public static void logWarning(String message) {
        LOG.warn(message);
    }
    
    /**
     * 记录错误日志
     * 
     * @param message 错误消息
     */
    public static void logError(String message) {
        LOG.error(message);
    }
    
    /**
     * 记录错误日志（带异常）
     * 
     * @param message 错误消息
     * @param exception 异常对象
     */
    public static void logError(String message, Throwable exception) {
        LOG.error(message, exception);
    }
} 