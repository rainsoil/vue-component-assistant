package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * VueKit 日志工具类
 * 
 * <p>提供统一的日志记录功能，支持：</p>
 * <ul>
 *   <li>不同级别的日志记录（DEBUG、INFO、WARN、ERROR）</li>
 *   <li>调试模式控制（全局和项目级）</li>
 *   <li>性能日志记录和监控</li>
 *   <li>结构化日志输出</li>
 *   <li>条件化日志记录（根据调试模式）</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>工具类设计，防止实例化</li>
 *   <li>支持全局和项目级调试模式</li>
 *   <li>自动日志级别控制</li>
 *   <li>统一的日志格式和输出</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>开发调试和问题排查</li>
 *   <li>性能监控和优化</li>
 *   <li>用户行为分析</li>
 *   <li>系统状态监控</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.settings.PluginSettings
 * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager
 */
public final class VueKitLogger {
    
    /**
     * 防止实例化
     * 工具类不应该被实例化
     */
    private VueKitLogger() {
        throw new UnsupportedOperationException("Logger utility class cannot be instantiated");
    }
    
    /**
     * 获取指定类的日志记录器
     * 
     * <p>该方法会返回IntelliJ IDEA平台提供的Logger实例，
     * 用于记录VueKit插件的各种日志信息。</p>
     * 
     * @param clazz 类对象，不能为null
     * @return Logger实例，用于记录日志
     * @throws IllegalArgumentException 如果clazz为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#getInstance(Class)
     */
    public static Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("类对象不能为null");
        }
        return Logger.getInstance(clazz);
    }
    
    /**
     * 检查是否启用调试模式（全局或项目级）
     * 
     * <p>调试模式检查优先级：</p>
     * <ol>
     *   <li>首先检查全局调试模式设置</li>
     *   <li>如果全局未启用，检查项目级调试模式</li>
     *   <li>如果项目级检查失败，回退到全局设置</li>
     * </ol>
     * 
     * <p>调试模式用途：</p>
     * <ul>
     *   <li>控制DEBUG级别日志的输出</li>
     *   <li>启用详细的性能监控</li>
     *   <li>显示额外的调试信息</li>
     *   <li>帮助开发者排查问题</li>
     * </ul>
     * 
     * @param project 项目对象，如果为null则只检查全局设置
     * @return 如果启用调试模式则返回true，否则返回false
     * 
     * @see com.chu7.vuecomponentassistant.settings.PluginSettings#isEnableDebugMode()
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#isDebugModeEnabled(Project)
     */
    public static boolean isDebugModeEnabled(Project project) {
        // 首先检查全局调试模式
        PluginSettings globalSettings = PluginSettings.getInstance();
        if (globalSettings.isEnableDebugMode()) {
            return true;
        }
        
        // 如果全局未启用，检查项目级调试模式
        if (project != null) {
            try {
                ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
                return configManager.isDebugModeEnabled(project);
            } catch (Exception e) {
                // 如果获取项目配置失败，返回全局设置
                return globalSettings.isEnableDebugMode();
            }
        }
        
        return false;
    }
    
    /**
     * 记录调试信息
     * 只在调试模式开启时记录（全局或项目级）
     * 
     * <p>该方法会根据调试模式状态决定是否记录日志：</p>
     * <ul>
     *   <li>如果调试模式开启，记录DEBUG级别日志</li>
     *   <li>如果调试模式关闭，忽略日志记录</li>
     * </ul>
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see #isDebugModeEnabled(Project)
     * @see com.intellij.openapi.diagnostic.Logger#debug(String)
     */
    public static void debug(Logger logger, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (isDebugModeEnabled(null)) {
            logger.debug(message);
        }
    }
    
    /**
     * 记录调试信息（带项目上下文）
     * 只在调试模式开启时记录（全局或项目级）
     * 
     * <p>该方法会根据项目特定的调试模式设置决定是否记录日志：</p>
     * <ul>
     *   <li>检查项目级调试模式设置</li>
     *   <li>如果项目级未设置，回退到全局设置</li>
     *   <li>只有在调试模式开启时才记录日志</li>
     * </ul>
     * 
     * @param logger 日志记录器，不能为null
     * @param project 项目对象，可以为null（使用全局设置）
     * @param message 日志消息，不能为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see #isDebugModeEnabled(Project)
     * @see com.intellij.openapi.diagnostic.Logger#debug(String)
     */
    public static void debug(Logger logger, Project project, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (isDebugModeEnabled(project)) {
            logger.debug(message);
        }
    }
    
    /**
     * 记录调试信息（带异常）
     * 只在调试模式开启时记录（全局或项目级）
     * 
     * <p>该方法用于记录调试信息和相关的异常堆栈：</p>
     * <ul>
     *   <li>记录详细的调试消息</li>
     *   <li>包含异常的完整堆栈信息</li>
     *   <li>只在调试模式开启时输出</li>
     * </ul>
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @param throwable 异常对象，可以为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see #isDebugModeEnabled(Project)
     * @see com.intellij.openapi.diagnostic.Logger#debug(String, Throwable)
     */
    public static void debug(Logger logger, String message, Throwable throwable) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (isDebugModeEnabled(null)) {
            if (throwable != null) {
                logger.debug(message, throwable);
            } else {
                logger.debug(message);
            }
        }
    }
    
    /**
     * 记录调试信息（带项目上下文和异常）
     * 只在调试模式开启时记录（全局或项目级）
     * 
     * @param logger 日志记录器，不能为null
     * @param project 项目对象，可以为null（使用全局设置）
     * @param message 日志消息，不能为null
     * @param throwable 异常对象，可以为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see #isDebugModeEnabled(Project)
     * @see com.intellij.openapi.diagnostic.Logger#debug(String, Throwable)
     */
    public static void debug(Logger logger, Project project, String message, Throwable throwable) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (isDebugModeEnabled(project)) {
            if (throwable != null) {
                logger.debug(message, throwable);
            } else {
                logger.debug(message);
            }
        }
    }
    
    /**
     * 记录信息日志
     * 无条件记录，不受调试模式控制
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#info(String)
     */
    public static void info(Logger logger, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        logger.info(message);
    }
    
    /**
     * 记录信息日志（带项目上下文）
     * 无条件记录，不受调试模式控制
     * 
     * @param logger 日志记录器，不能为null
     * @param project 项目对象，可以为null
     * @param message 日志消息，不能为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#info(String)
     */
    public static void info(Logger logger, Project project, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
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
     * 无条件记录，不受调试模式控制
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#warn(String)
     */
    public static void warn(Logger logger, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        logger.warn(message);
    }
    
    /**
     * 记录警告日志（带项目上下文）
     * 
     * @param logger 日志记录器
     * @param project 项目对象
     * @param message 日志消息
     */
    public static void warn(Logger logger, Project project, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        logger.warn(message);
    }
    
    /**
     * 记录警告日志（带异常）
     * 无条件记录，不受调试模式控制
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @param throwable 异常对象，可以为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#warn(String, Throwable)
     */
    public static void warn(Logger logger, String message, Throwable throwable) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (throwable != null) {
            logger.warn(message, throwable);
        } else {
            logger.warn(message);
        }
    }
    
    /**
     * 记录警告日志（带项目上下文和异常）
     * 
     * @param logger 日志记录器
     * @param project 项目对象
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void warn(Logger logger, Project project, String message, Throwable throwable) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (throwable != null) {
            logger.warn(message, throwable);
        } else {
            logger.warn(message);
        }
    }
    
    /**
     * 记录错误日志
     * 无条件记录，不受调试模式控制
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#error(String)
     */
    public static void error(Logger logger, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        logger.error(message);
    }
    
    /**
     * 记录错误日志（带项目上下文）
     * 
     * @param logger 日志记录器
     * @param project 项目对象
     * @param message 日志消息
     */
    public static void error(Logger logger, Project project, String message) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        logger.error(message);
    }
    
    /**
     * 记录错误日志（带异常）
     * 无条件记录，不受调试模式控制
     * 
     * @param logger 日志记录器，不能为null
     * @param message 日志消息，不能为null
     * @param throwable 异常对象，可以为null
     * @throws IllegalArgumentException 如果logger或message为null
     * 
     * @see com.intellij.openapi.diagnostic.Logger#error(String, Throwable)
     */
    public static void error(Logger logger, String message, Throwable throwable) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (throwable != null) {
            logger.error(message, throwable);
        } else {
            logger.error(message);
        }
    }
    
    /**
     * 记录错误日志（带项目上下文和异常）
     * 
     * @param logger 日志记录器
     * @param project 项目对象
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(Logger logger, Project project, String message, Throwable throwable) {
        if (logger == null) {
            throw new IllegalArgumentException("日志记录器不能为null");
        }
        if (message == null) {
            throw new IllegalArgumentException("日志消息不能为null");
        }
        
        if (throwable != null) {
            logger.error(message, throwable);
        } else {
            logger.error(message);
        }
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