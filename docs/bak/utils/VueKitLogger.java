package bak.utils;

import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.intellij.openapi.diagnostic.Logger;

/**
 * VueKit 日志工具类
 * 
 * 提供统一的日志记录功能，支持：
 * - 不同级别的日志记录
 * - 调试模式控制
 * - 性能日志记录
 * - 结构化日志输出
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public final class VueKitLogger {
    
    // 防止实例化
    private VueKitLogger() {
        throw new UnsupportedOperationException("Logger utility class cannot be instantiated");
    }
    
    /**
     * 获取指定类的日志记录器
     * 
     * @param clazz 类对象
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getInstance(clazz);
    }
    
    /**
     * 记录调试信息
     * 只在调试模式开启时记录
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     */
    public static void debug(Logger logger, String message) {
        PluginSettings settings = PluginSettings.getInstance();
        if (settings.isEnableDebugMode()) {
            logger.debug(message);
        }
    }
    
    /**
     * 记录调试信息（带异常）
     * 只在调试模式开启时记录
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void debug(Logger logger, String message, Throwable throwable) {
        PluginSettings settings = PluginSettings.getInstance();
        if (settings.isEnableDebugMode()) {
            logger.debug(message, throwable);
        }
    }
    
    /**
     * 记录信息日志
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
    }
    
    /**
     * 记录信息日志（带参数）
     * 
     * @param logger 日志记录器
     * @param format 消息格式
     * @param args 参数
     */
    public static void info(Logger logger, String format, Object... args) {
        logger.info(String.format(format, args));
    }
    
    /**
     * 记录警告日志
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     */
    public static void warn(Logger logger, String message) {
        logger.warn(message);
    }
    
    /**
     * 记录警告日志（带异常）
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void warn(Logger logger, String message, Throwable throwable) {
        logger.warn(message, throwable);
    }
    
    /**
     * 记录错误日志
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     */
    public static void error(Logger logger, String message) {
        logger.error(message);
    }
    
    /**
     * 记录错误日志（带异常）
     * 
     * @param logger 日志记录器
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(Logger logger, String message, Throwable throwable) {
        logger.error(message, throwable);
    }
    
    /**
     * 记录性能日志
     * 只在性能日志开启时记录
     * 
     * @param logger 日志记录器
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     */
    public static void performance(Logger logger, String operation, long duration) {
        PluginSettings settings = PluginSettings.getInstance();
        if (settings.isEnablePerformanceLogging()) {
            logger.info(String.format("性能日志 - 操作: %s, 耗时: %dms", operation, duration));
        }
    }
    
    /**
     * 记录性能日志（带阈值检查）
     * 只有超过阈值时才记录
     * 
     * @param logger 日志记录器
     * @param operation 操作名称
     * @param duration 耗时（毫秒）
     * @param threshold 阈值（毫秒）
     */
    public static void performanceWithThreshold(Logger logger, String operation, long duration, long threshold) {
        PluginSettings settings = PluginSettings.getInstance();
        if (settings.isEnablePerformanceLogging() && duration > threshold) {
            logger.warn(String.format("性能警告 - 操作: %s, 耗时: %dms，超过阈值: %dms", 
                                    operation, duration, threshold));
        }
    }
    
    /**
     * 记录组件库检测日志
     * 
     * @param logger 日志记录器
     * @param libraryName 组件库名称
     * @param componentCount 组件数量
     */
    public static void logLibraryDetection(Logger logger, String libraryName, int componentCount) {
        info(logger, "检测到组件库: %s, 组件数量: %d", libraryName, componentCount);
    }
    
    /**
     * 记录补全操作日志
     * 
     * @param logger 日志记录器
     * @param completionType 补全类型
     * @param resultCount 结果数量
     * @param duration 耗时
     */
    public static void logCompletion(Logger logger, String completionType, int resultCount, long duration) {
        debug(logger, String.format("补全操作 - 类型: %s, 结果数量: %d, 耗时: %dms", 
                                   completionType, resultCount, duration));
    }
    
    /**
     * 记录文档生成日志
     * 
     * @param logger 日志记录器
     * @param componentName 组件名称
     * @param success 是否成功
     */
    public static void logDocumentationGeneration(Logger logger, String componentName, boolean success) {
        if (success) {
            debug(logger, "文档生成成功: " + componentName);
        } else {
            warn(logger, "文档生成失败: " + componentName);
        }
    }
    
    /**
     * 记录缓存操作日志
     * 
     * @param logger 日志记录器
     * @param operation 操作类型（hit/miss/put/evict）
     * @param key 缓存键
     */
    public static void logCacheOperation(Logger logger, String operation, String key) {
        debug(logger, String.format("缓存操作 - %s: %s", operation, key));
    }
    
    /**
     * 记录异常并返回运行时异常
     * 
     * @param logger 日志记录器
     * @param message 错误消息
     * @param cause 原因异常
     * @return RuntimeException
     */
    public static RuntimeException logAndThrow(Logger logger, String message, Throwable cause) {
        error(logger, message, cause);
        return new RuntimeException(message, cause);
    }
    
    /**
     * 记录并忽略异常（用于非关键操作）
     * 
     * @param logger 日志记录器
     * @param message 错误消息
     * @param throwable 异常对象
     */
    public static void logAndIgnore(Logger logger, String message, Throwable throwable) {
        debug(logger, message + " (已忽略)", throwable);
    }
}