package com.chu7.vuecomponentassistant.completion;

/**
 * 补全上下文类
 * 
 * 用于封装补全相关的上下文信息，包括：
 * - 补全类型（组件、属性、事件、插槽）
 * - 当前组件名称
 * - 用户输入的前缀
 * 
 * 这个类帮助补全系统理解用户的意图并提供相应的补全选项
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class CompletionContext {
    
    /**
     * 补全类型枚举
     */
    public enum CompletionType {
        COMPONENT,   // 组件补全
        ATTRIBUTE,   // 属性补全
        EVENT,       // 事件补全
        SLOT         // 插槽补全
    }
    
    /** 补全类型 */
    private final CompletionType type;
    
    /** 当前组件名称 */
    private final String currentComponent;
    
    /** 用户输入的前缀，用于过滤补全选项 */
    private final String prefix;
    
    /**
     * 构造函数
     * 
     * @param type 补全类型
     * @param currentComponent 当前组件名称
     * @param prefix 用户输入的前缀
     */
    public CompletionContext(CompletionType type, String currentComponent, String prefix) {
        this.type = type;
        this.currentComponent = currentComponent;
        this.prefix = prefix;
    }
    
    /**
     * 获取补全类型
     * @return 补全类型枚举值
     */
    public CompletionType getType() {
        return type;
    }
    
    /**
     * 获取当前组件名称
     * @return 当前组件名称，可能为 null
     */
    public String getCurrentComponent() {
        return currentComponent;
    }
    
    /**
     * 获取用户输入的前缀
     * @return 用户输入的前缀，用于过滤补全选项
     */
    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public String toString() {
        return String.format("CompletionContext{type=%s, component='%s', prefix='%s'}", 
                           type, currentComponent, prefix);
    }
} 