package com.chu7.vuecomponentassistant.completion;

/**
 * Element Plus 插槽数据模型
 */
public class ElementPlusSlot {
    private String name;
    private String description;

    /** 卡槽作用域参数 */
    public String scope;


    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    // 构造函数
    public ElementPlusSlot() {}
    
    public ElementPlusSlot(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "ElementPlusSlot{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
