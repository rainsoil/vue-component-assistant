package bak.completion;

import java.util.List;

/**
 * Element Plus 属性数据模型
 */
public class ElementPlusProp {
    private String name;
    private String type;
    private String description;
    private Object defaultValue;
    private boolean required;
    private List<String> options;
    
    // 构造函数
    public ElementPlusProp() {}
    
    public ElementPlusProp(String name, String type, String description, Object defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.required = required;
    }
    
    public ElementPlusProp(String name, String type, String description, Object defaultValue, boolean required, List<String> options) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.required = required;
        this.options = options;
    }
    
    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public String getDefaultValueAsString() {
        if (defaultValue == null) {
            return "";
        }
        return defaultValue.toString();
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    @Override
    public String toString() {
        return "ElementPlusProp{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", required=" + required +
                ", options=" + options +
                '}';
    }
}
