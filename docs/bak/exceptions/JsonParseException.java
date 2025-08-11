package bak.exceptions;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;

/**
 * JSON解析异常
 * 
 * 用于处理JSON数据解析过程中的异常
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class JsonParseException extends VueKitException {
    
    private final String jsonContent;
    
    public JsonParseException(String message, String jsonContent) {
        super(message);
        this.jsonContent = jsonContent;
    }
    
    public JsonParseException(String message, String jsonContent, Throwable cause) {
        super(message, cause);
        this.jsonContent = jsonContent;
    }
    
    public String getJsonContent() {
        return jsonContent;
    }
}