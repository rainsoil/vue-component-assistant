package com.chu7;

import java.util.ArrayList;
import java.util.List;

/**
 * 组件库类
 * 表示一个完整的组件库，包含组件库的基本信息和所有组件
 */
public class ComponentLibrary {
    /** 组件库名称 */
    public String name;
    
    /** 组件库描述 */
    public String description;
    
    /** 组件库版本 */
    public String version;
    
    /** 作者信息 */
    public String author;
    
    /** 官网链接 */
    public String website;
    
    /** 文档链接 */
    public String docUrl;
    
    /** 组件列表 */
    public List<ComponentMeta> components = new ArrayList<>();
    
    /**
     * 默认构造函数
     */
    public ComponentLibrary() {}
    
    /**
     * 带参数的构造函数
     * @param name 组件库名称
     * @param description 组件库描述
     * @param version 组件库版本
     */
    public ComponentLibrary(String name, String description, String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }
    
    /**
     * 添加组件到组件库
     * @param component 要添加的组件
     */
    public void addComponent(ComponentMeta component) {
        if (components == null) {
            components = new ArrayList<>();
        }
        components.add(component);
    }
    
    /**
     * 获取组件库中的所有组件
     * @return 组件列表
     */
    public List<ComponentMeta> getComponents() {
        return components != null ? components : new ArrayList<>();
    }
    
    /**
     * 重写 toString 方法，用于在列表中显示组件库信息
     * @return 格式化的组件库显示字符串
     */
    @Override
    public String toString() {
        return name + (description != null && !description.isEmpty() ? " - " + description : "");
    }
} 