package com.chu7.vuecomponentassistant.exceptions;

/**
 * 文件操作异常
 * 
 * 用于处理文件读取、写入、创建等操作中的异常。
 * 继承自VueKitException，专门处理文件系统操作相关的错误。
 * 包含文件路径信息，便于调试和错误定位。
 * 
 * 使用场景：
 * - 配置文件读取失败
 * - 组件库文件写入失败
 * - 缓存文件创建失败
 * - 临时文件删除失败
 * - 文件权限不足
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class FileOperationException extends VueKitException {
    
    /** 发生异常的文件路径 */
    private final String filePath;
    
    /**
     * 构造函数 - 包含错误消息和文件路径
     * 
     * @param message 文件操作相关的错误描述
     * @param filePath 发生异常的文件路径
     */
    public FileOperationException(String message, String filePath) {
        super(message);
        this.filePath = filePath;
    }
    
    /**
     * 构造函数 - 包含错误消息、文件路径和原因异常
     * 
     * @param message 文件操作相关的错误描述
     * @param filePath 发生异常的文件路径
     * @param cause 导致此异常的原始异常（如IOException、SecurityException等）
     */
    public FileOperationException(String message, String filePath, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
    }
    
    /**
     * 获取发生异常的文件路径
     * 
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }
}