package com.chu7.vuecomponentassistant.exceptions;

/**
 * 文件操作异常
 * 
 * 用于处理文件读取、写入、创建等操作中的异常
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class FileOperationException extends VueKitException {
    
    private final String filePath;
    
    public FileOperationException(String message, String filePath) {
        super(message);
        this.filePath = filePath;
    }
    
    public FileOperationException(String message, String filePath, Throwable cause) {
        super(message, cause);
        this.filePath = filePath;
    }
    
    public String getFilePath() {
        return filePath;
    }
}