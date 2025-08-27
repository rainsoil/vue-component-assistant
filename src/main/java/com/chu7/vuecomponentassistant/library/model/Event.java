package com.chu7.vuecomponentassistant.library.model;

/**
 * 组件事件数据模型
 * 对应JSON文件中events数组的每个元素
 */
public class Event {
    public String name;
    public String description;
    public Object parameters;
    
    public Event() {}
    
    public Event(String name, String description, Object parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }
} 