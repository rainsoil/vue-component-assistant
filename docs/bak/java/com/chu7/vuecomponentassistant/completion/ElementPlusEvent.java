package com.chu7.vuecomponentassistant.completion;

/**
 * Element Plus 事件数据模型
 */
public class ElementPlusEvent {
    private String name;
    private String description;
    private String parameters;
    
    // 构造函数
    public ElementPlusEvent() {}
    
    public ElementPlusEvent(String name, String description, String parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
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
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public String toString() {
        return "ElementPlusEvent{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameters='" + parameters + '\'' +
                '}';
    }
}
