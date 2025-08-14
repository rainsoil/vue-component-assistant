package com.chu7.vuecomponentassistant.exceptions;

/**
 * 组件库相关异常
 * 
 * 用于处理组件库加载、解析、管理过程中的异常。
 * 继承自VueKitException，专门处理组件库操作相关的错误。
 * 
 * 使用场景：
 * - 组件库文件读取失败
 * - 组件库JSON格式错误
 * - 组件库版本不兼容
 * - 组件库依赖缺失
 * - 组件库缓存损坏
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryException extends VueKitException {
    
    /**
     * 构造函数 - 仅包含错误消息
     * 
     * @param message 组件库操作相关的错误描述
     */
    public ComponentLibraryException(String message) {
        super(message);
    }
    
    /**
     * 构造函数 - 包含错误消息和原因异常
     * 
     * @param message 组件库操作相关的错误描述
     * @param cause 导致此异常的原始异常（如IOException、JsonParseException等）
     */
    public ComponentLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数 - 仅包含原因异常
     * 
     * @param cause 导致此异常的原始异常
     */
    public ComponentLibraryException(Throwable cause) {
        super(cause);
    }
}