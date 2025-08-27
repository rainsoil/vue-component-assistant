package com.chu7.vuecomponentassistant.library.model;

import java.util.List;

/**
 * 组件属性数据模型
 * 对应JSON文件中props数组的每个元素
 */
public class Prop {
    public String name;
    public Object type;
    public String description;
    public Object defaultValue;
    public Object defaultJValue; // 可选：用于从 JSON 字段 defaultJValue 读取默认值
    public boolean required;
    public List<String> options;
    
    public Prop() {}
    
    public Prop(String name, Object type, String description, Object defaultValue, 
               boolean required, List<String> options) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.required = required;
        this.options = options;
    }
} 