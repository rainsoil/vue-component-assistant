package com.chu7;

import java.util.ArrayList;
import java.util.List;

public class ComponentLibrary {
    public String name;           // 组件库名称
    public String description;    // 组件库描述
    public String version;        // 组件库版本
    public String author;         // 作者
    public String website;        // 官网链接
    public String docUrl;         // 文档链接
    public List<ComponentMeta> components = new ArrayList<>(); // 组件列表
    
    public ComponentLibrary() {}
    
    public ComponentLibrary(String name, String description, String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }
    
    public void addComponent(ComponentMeta component) {
        if (components == null) {
            components = new ArrayList<>();
        }
        components.add(component);
    }
    
    public List<ComponentMeta> getComponents() {
        return components != null ? components : new ArrayList<>();
    }
    
    @Override
    public String toString() {
        return name + (description != null && !description.isEmpty() ? " - " + description : "");
    }
} 