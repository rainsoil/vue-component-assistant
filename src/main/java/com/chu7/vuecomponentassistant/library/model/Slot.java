package com.chu7.vuecomponentassistant.library.model;

/**
 * 组件插槽数据模型
 * 对应JSON文件中slots数组的每个元素
 */
public class Slot {
    public String name;
    public String description;
    public String scope; // 可选：若提供，则使用 <template #name="scope"> 插入
    
    public Slot() {}
    
    public Slot(String name, String description) {
        this.name = name;
        this.description = description;
    }
} 