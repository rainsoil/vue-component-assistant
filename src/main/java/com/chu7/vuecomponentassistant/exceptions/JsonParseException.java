package com.chu7.vuecomponentassistant.exceptions;

/**
 * JSON解析异常
 * 
 * 用于处理JSON数据解析过程中的异常。
 * 继承自VueKitException，专门处理JSON格式错误和解析失败。
 * 包含原始JSON内容，便于调试和错误分析。
 * 
 * 使用场景：
 * - 组件库JSON文件格式错误
 * - 配置文件JSON格式错误
 * - 远程API响应JSON解析失败
 * - 缓存数据JSON格式损坏
 * - JSON语法错误
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class JsonParseException extends VueKitException {
    
    /** 解析失败的JSON内容 */
    private final String jsonContent;
    
    /**
     * 构造函数 - 包含错误消息和JSON内容
     * 
     * @param message JSON解析相关的错误描述
     * @param jsonContent 解析失败的JSON内容
     */
    public JsonParseException(String message, String jsonContent) {
        super(message);
        this.jsonContent = jsonContent;
    }
    
    /**
     * 构造函数 - 包含错误消息、JSON内容和原因异常
     * 
     * @param message JSON解析相关的错误描述
     * @param jsonContent 解析失败的JSON内容
     * @param cause 导致此异常的原始异常（如JsonSyntaxException、IOException等）
     */
    public JsonParseException(String message, String jsonContent, Throwable cause) {
        super(message, cause);
        this.jsonContent = jsonContent;
    }
    
    /**
     * 获取解析失败的JSON内容
     * 
     * @return JSON内容字符串
     */
    public String getJsonContent() {
        return jsonContent;
    }
}