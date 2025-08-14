package com.chu7.vuecomponentassistant.exceptions;

/**
 * VueKit 插件基础异常类
 * 
 * 所有插件相关的异常都应该继承此类，提供统一的异常处理机制。
 * 此类作为插件异常体系的根类，定义了基本的异常构造方法。
 * 
 * 使用场景：
 * - 插件初始化失败
 * - 配置加载错误
 * - 组件库操作异常
 * - 补全功能异常
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class VueKitException extends Exception {
    
    /**
     * 构造函数 - 仅包含错误消息
     * 
     * @param message 错误描述信息
     */
    public VueKitException(String message) {
        super(message);
    }
    
    /**
     * 构造函数 - 包含错误消息和原因异常
     * 
     * @param message 错误描述信息
     * @param cause 导致此异常的原始异常
     */
    public VueKitException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数 - 仅包含原因异常
     * 
     * @param cause 导致此异常的原始异常
     */
    public VueKitException(Throwable cause) {
        super(cause);
    }
}