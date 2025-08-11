package bak.exceptions;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;

/**
 * 组件库相关异常
 * 
 * 用于处理组件库加载、解析、管理过程中的异常
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryException extends VueKitException {
    
    public ComponentLibraryException(String message) {
        super(message);
    }
    
    public ComponentLibraryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ComponentLibraryException(Throwable cause) {
        super(cause);
    }
}