package com.chu7.vuecomponentassistant.remote.model;

import java.util.List;

/**
 * 组件信息数据模型
 */
public class ComponentInfo {
    private String name;            // 组件名称
    private String displayName;     // 显示名称
    private String description;     // 组件描述
    private String tag;             // 组件标签
    private List<ComponentProp> props;      // 组件属性
    private List<ComponentEvent> events;    // 组件事件
    private List<ComponentSlot> slots;      // 组件插槽
    private String documentation;   // 组件文档

    // 构造函数
    public ComponentInfo() {}

    public ComponentInfo(String name, String displayName, String description, String tag) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.tag = tag;
    }

    // Getter和Setter方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<ComponentProp> getProps() {
        return props;
    }

    public void setProps(List<ComponentProp> props) {
        this.props = props;
    }

    public List<ComponentEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ComponentEvent> events) {
        this.events = events;
    }

    public List<ComponentSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<ComponentSlot> slots) {
        this.slots = slots;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String toString() {
        return "ComponentInfo{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }

    /**
     * 组件属性
     */
    public static class ComponentProp {
        private String name;
        private String type;
        private String description;
        private String defaultValue;
        private boolean required;

        public ComponentProp() {}

        public ComponentProp(String name, String type, String description, String defaultValue, boolean required) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
            this.required = required;
        }

        // Getter和Setter方法
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
    }

    /**
     * 组件事件
     */
    public static class ComponentEvent {
        private String name;
        private String description;
        private String parameters;

        public ComponentEvent() {}

        public ComponentEvent(String name, String description, String parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        // Getter和Setter方法
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }
    }

    /**
     * 组件插槽
     */
    public static class ComponentSlot {
        private String name;
        private String description;
        private String scope;

        public ComponentSlot() {}

        public ComponentSlot(String name, String description, String scope) {
            this.name = name;
            this.description = description;
            this.scope = scope;
        }

        // Getter和Setter方法
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }
} 