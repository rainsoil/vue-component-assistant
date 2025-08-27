package com.chu7.vuecomponentassistant.library.model;

import java.util.List;

/**
 * 组件数据模型
 * 对应JSON文件中components数组的每个元素
 */
public class Component {
    public String name;
    public String description;
    public String version;
    public String example;
    public String docUrl;
    public List<Prop> props;
    public List<Event> events;
    public List<Slot> slots;
    
    public Component() {}
    
    public Component(String name, String description, String version, String example, 
                   String docUrl, List<Prop> props, List<Event> events, List<Slot> slots) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.example = example;
        this.docUrl = docUrl;
        this.props = props;
        this.events = events;
        this.slots = slots;
    }
} 