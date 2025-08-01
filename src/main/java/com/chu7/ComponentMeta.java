package com.chu7;

import java.util.ArrayList;
import java.util.List;

/**
 * 组件元数据类
 * 用于存储组件的基本信息，包括属性、事件、卡槽等
 */
public class ComponentMeta {
    /** 组件名称，如 el-button */
    public String name;
    
    /** 组件描述 */
    public String description;
    
    /** 组件版本 */
    public String version;
    
    /** 使用示例 */
    public String example;
    
    /** 官方文档链接 */
    public String docUrl;
    
    /** 组件属性列表 */
    public List<ComponentProp> props = new ArrayList<>();
    
    /** 组件事件列表 */
    public List<ComponentEvent> events = new ArrayList<>();
    
    /** 组件卡槽列表 */
    public List<ComponentSlot> slots = new ArrayList<>();
    
    /**
     * 组件属性类
     */
    public static class ComponentProp {
        /** 属性名称 */
        public String name;
        
        /** 属性类型 */
        public String type;
        
        /** 属性描述 */
        public String description;
        
        /** 默认值 */
        public String defaultValue;
        
        /** 是否必需 */
        public boolean required;
        
        /** 可选值列表（用于枚举类型） */
        public List<String> options;
        
        public ComponentProp() {}
        
        public ComponentProp(String name, String type, String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }
    }
    
    /**
     * 组件事件类
     */
    public static class ComponentEvent {
        /** 事件名称 */
        public String name;
        
        /** 事件描述 */
        public String description;
        
        /** 事件参数 */
        public String parameters;
        
        public ComponentEvent() {}
        
        public ComponentEvent(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    
    /**
     * 组件卡槽类
     */
    public static class ComponentSlot {
        /** 卡槽名称 */
        public String name;
        
        /** 卡槽描述 */
        public String description;
        
        /** 卡槽作用域参数 */
        public String scope;
        
        public ComponentSlot() {}
        
        public ComponentSlot(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}