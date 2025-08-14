package com.chu7.vuecomponentassistant.completion;

import java.util.List;

/**
 * Element Plus 组件数据模型
 */
public class ElementPlusComponent {
    private String name;
    private String description;
    private String version;
    private String example;
    private String docUrl;
    private List<ElementPlusProp> props;
    private List<ElementPlusEvent> events;
    private List<ElementPlusSlot> slots;
    
    // 构造函数
    public ElementPlusComponent() {}
    
    public ElementPlusComponent(String name, String description, String version, String example, String docUrl,
                              List<ElementPlusProp> props, List<ElementPlusEvent> events, List<ElementPlusSlot> slots) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.example = example;
        this.docUrl = docUrl;
        this.props = props;
        this.events = events;
        this.slots = slots;
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
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getExample() {
        return example;
    }
    
    public void setExample(String example) {
        this.example = example;
    }
    
    public String getDocUrl() {
        return docUrl;
    }
    
    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }
    
    public List<ElementPlusProp> getProps() {
        return props;
    }
    
    public void setProps(List<ElementPlusProp> props) {
        this.props = props;
    }
    
    public List<ElementPlusEvent> getEvents() {
        return events;
    }
    
    public void setEvents(List<ElementPlusEvent> events) {
        this.events = events;
    }
    
    public List<ElementPlusSlot> getSlots() {
        return slots;
    }
    
    public void setSlots(List<ElementPlusSlot> slots) {
        this.slots = slots;
    }
    
    @Override
    public String toString() {
        return "ElementPlusComponent{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version='" + version + '\'' +
                ", example='" + example + '\'' +
                ", docUrl='" + docUrl + '\'' +
                ", props=" + props +
                ", events=" + events +
                ", slots=" + slots +
                '}';
    }
}
