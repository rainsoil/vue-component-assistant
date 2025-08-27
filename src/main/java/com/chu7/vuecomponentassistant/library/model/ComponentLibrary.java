package com.chu7.vuecomponentassistant.library.model;

import java.util.List;

/**
 * 组件库数据模型
 * 对应JSON文件中的根结构
 */
public class ComponentLibrary {
    public String id;
    public String name;
    public String componentPrefix;
    public String displayName;
    public String description;
    public String version;
    public String sourceUrl;
    public String lastUpdated;
    public List<Component> components;
    
    public ComponentLibrary() {}
    
    public ComponentLibrary(String id, String name, String componentPrefix, String displayName, 
                          String description, String version, String sourceUrl, String lastUpdated, 
                          List<Component> components) {
        this.id = id;
        this.name = name;
        this.componentPrefix = componentPrefix;
        this.displayName = displayName;
        this.description = description;
        this.version = version;
        this.sourceUrl = sourceUrl;
        this.lastUpdated = lastUpdated;
        this.components = components;
    }
} 