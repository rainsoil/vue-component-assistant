package bak.exceptions;

/**
 * VueKit 插件基础异常类
 * 
 * 所有插件相关的异常都应该继承此类
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class VueKitException extends Exception {
    
    public VueKitException(String message) {
        super(message);
    }
    
    public VueKitException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public VueKitException(Throwable cause) {
        super(cause);
    }
}