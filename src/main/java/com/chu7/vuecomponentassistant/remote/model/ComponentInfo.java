package com.chu7.vuecomponentassistant.remote.model;

import java.util.List;

/**
 * 组件信息数据模型
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>表示一个Vue组件的完整信息</li>
 *   <li>包含组件的基本信息（名称、描述、标签、版本等）</li>
 *   <li>管理组件的属性、事件、插槽等详细配置</li>
 *   <li>提供组件的使用示例和文档链接</li>
 *   <li>支持组件的序列化和反序列化</li>
 * </ul>
 * 
 * <p>数据组成：</p>
 * <ul>
 *   <li>基本信息：名称、显示名称、描述、标签、版本</li>
 *   <li>使用信息：示例、文档URL、组件文档</li>
 *   <li>配置信息：属性列表、事件列表、插槽列表</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>标准JavaBean设计，支持属性访问</li>
 *   <li>支持多种构造函数重载</li>
 *   <li>包含内部类表示组件的不同部分</li>
 *   <li>提供完整的toString方法用于调试</li>
 *   <li>支持多种数据类型的默认值</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全中的组件信息</li>
 *   <li>组件文档显示</li>
 *   <li>组件配置管理</li>
 *   <li>数据导入导出</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentInfo.ComponentProp
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentInfo.ComponentEvent
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentInfo.ComponentSlot
 */
public class ComponentInfo {
    
    /**
     * 组件名称
     * 用于标识组件的唯一性，如 "el-button"、"a-table" 等
     */
    private String name;
    
    /**
     * 显示名称
     * 在用户界面中显示的友好名称
     */
    private String displayName;
    
    /**
     * 组件描述
     * 详细说明组件的用途、作用和行为
     */
    private String description;
    
    /**
     * 组件标签
     * 用于分类和过滤组件的标签
     */
    private String tag;
    
    /**
     * 组件版本
     * 标识组件的版本信息
     */
    private String version;
    
    /**
     * 使用示例
     * 提供组件的典型使用方法和代码示例
     */
    private String example;
    
    /**
     * 文档URL
     * 指向组件官方文档的URL地址
     */
    private String docUrl;
    
    /**
     * 组件属性列表
     * 包含组件的所有可配置属性及其详细信息
     */
    private List<ComponentProp> props;
    
    /**
     * 组件事件列表
     * 包含组件支持的所有事件及其参数信息
     */
    private List<ComponentEvent> events;
    
    /**
     * 组件插槽列表
     * 包含组件提供的所有插槽及其作用域信息
     */
    private List<ComponentSlot> slots;
    
    /**
     * 组件文档
     * 组件的详细文档内容
     */
    private String documentation;

    // ==================== 构造函数 ====================
    
    /**
     * 默认构造函数
     * 
     * <p>创建一个空的组件信息对象，所有字段初始化为默认值。
     * 通常用于序列化/反序列化操作。</p>
     */
    public ComponentInfo() {}

    /**
     * 基础构造函数
     * 
     * <p>创建一个基本的组件信息对象，包含核心字段信息。</p>
     * 
     * @param name 组件名称，不能为null
     * @param displayName 显示名称，可以为null
     * @param description 组件描述，可以为null
     * @param tag 组件标签，可以为null
     * @throws IllegalArgumentException 如果name为null
     */
    public ComponentInfo(String name, String displayName, String description, String tag) {
        if (name == null) {
            throw new IllegalArgumentException("组件名称不能为null");
        }
        
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.tag = tag;
    }

    // ==================== Getter和Setter方法 ====================
    
    /**
     * 获取组件名称
     * 
     * @return 组件名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置组件名称
     * 
     * @param name 组件名称，不能为null
     * @throws IllegalArgumentException 如果name为null
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("组件名称不能为null");
        }
        this.name = name;
    }

    /**
     * 获取显示名称
     * 
     * @return 显示名称，如果未设置则返回null
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称
     * 
     * @param displayName 显示名称，可以为null
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取组件描述
     * 
     * @return 组件描述，如果未设置则返回null
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置组件描述
     * 
     * @param description 组件描述，可以为null
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取组件标签
     * 
     * @return 组件标签，如果未设置则返回null
     */
    public String getTag() {
        return tag;
    }

    /**
     * 设置组件标签
     * 
     * @param tag 组件标签，可以为null
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 获取组件属性列表
     * 
     * @return 属性列表，如果未设置则返回null
     */
    public List<ComponentProp> getProps() {
        return props;
    }

    /**
     * 设置组件属性列表
     * 
     * @param props 属性列表，可以为null
     */
    public void setProps(List<ComponentProp> props) {
        this.props = props;
    }

    /**
     * 获取组件事件列表
     * 
     * @return 事件列表，如果未设置则返回null
     */
    public List<ComponentEvent> getEvents() {
        return events;
    }

    /**
     * 设置组件事件列表
     * 
     * @param events 事件列表，可以为null
     */
    public void setEvents(List<ComponentEvent> events) {
        this.events = events;
    }

    /**
     * 获取组件插槽列表
     * 
     * @return 插槽列表，如果未设置则返回null
     */
    public List<ComponentSlot> getSlots() {
        return slots;
    }

    /**
     * 设置组件插槽列表
     * 
     * @param slots 插槽列表，可以为null
     */
    public void setSlots(List<ComponentSlot> slots) {
        this.slots = slots;
    }

    /**
     * 获取组件文档
     * 
     * @return 组件文档，如果未设置则返回null
     */
    public String getDocumentation() {
        return documentation;
    }

    /**
     * 设置组件文档
     * 
     * @param documentation 组件文档，可以为null
     */
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    /**
     * 获取组件版本
     * 
     * @return 组件版本，如果未设置则返回null
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置组件版本
     * 
     * @param version 组件版本，可以为null
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取使用示例
     * 
     * @return 使用示例，如果未设置则返回null
     */
    public String getExample() {
        return example;
    }

    /**
     * 设置使用示例
     * 
     * @param example 使用示例，可以为null
     */
    public void setExample(String example) {
        this.example = example;
    }

    /**
     * 获取文档URL
     * 
     * @return 文档URL，如果未设置则返回null
     */
    public String getDocUrl() {
        return docUrl;
    }

    /**
     * 设置文档URL
     * 
     * @param docUrl 文档URL，可以为null
     */
    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    /**
     * 返回组件的字符串表示
     * 
     * <p>该方法用于调试和日志记录，提供组件的关键信息概览。</p>
     * 
     * @return 组件的字符串表示，包含主要字段信息
     */
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
     * 
     * <p>表示组件的一个可配置属性，包含属性的名称、类型、描述、
     * 默认值、是否必需等信息。支持多种数据类型的默认值。</p>
     * 
     * <p>设计特点：</p>
     * <ul>
     *   <li>支持多种数据类型的默认值</li>
     *   <li>提供类型转换方法</li>
     *   <li>支持枚举值选项</li>
     *   <li>完整的属性验证</li>
     * </ul>
     * 
     * @author VueKit Team
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class ComponentProp {
        
        /**
         * 属性名称
         */
        private String name;
        
        /**
         * 属性类型
         */
        private String type;
        
        /**
         * 属性描述
         */
        private String description;
        
        /**
         * 默认值
         * 支持多种类型：String, Boolean, Number
         */
        private Object defaultValue;
        
        /**
         * 是否必需
         */
        private boolean required;
        
        /**
         * 选项列表
         * 当属性为枚举类型时，提供可选的值列表
         */
        private List<String> options;

        /**
         * 默认构造函数
         */
        public ComponentProp() {}

        /**
         * 全参数构造函数
         * 
         * @param name 属性名称，不能为null
         * @param type 属性类型，不能为null
         * @param description 属性描述，可以为null
         * @param defaultValue 默认值，可以为null
         * @param required 是否必需
         * @throws IllegalArgumentException 如果name或type为null
         */
        public ComponentProp(String name, String type, String description, Object defaultValue, boolean required) {
            if (name == null) {
                throw new IllegalArgumentException("属性名称不能为null");
            }
            if (type == null) {
                throw new IllegalArgumentException("属性类型不能为null");
            }
            
            this.name = name;
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
            this.required = required;
        }

        // ==================== Getter和Setter方法 ====================
        
        /**
         * 获取属性名称
         * 
         * @return 属性名称
         */
        public String getName() { return name; }
        
        /**
         * 设置属性名称
         * 
         * @param name 属性名称，不能为null
         * @throws IllegalArgumentException 如果name为null
         */
        public void setName(String name) { 
            if (name == null) {
                throw new IllegalArgumentException("属性名称不能为null");
            }
            this.name = name; 
        }
        
        /**
         * 获取属性类型
         * 
         * @return 属性类型
         */
        public String getType() { return type; }
        
        /**
         * 设置属性类型
         * 
         * @param type 属性类型，不能为null
         * @throws IllegalArgumentException 如果type为null
         */
        public void setType(String type) { 
            if (type == null) {
                throw new IllegalArgumentException("属性类型不能为null");
            }
            this.type = type; 
        }
        
        /**
         * 获取属性描述
         * 
         * @return 属性描述，如果未设置则返回null
         */
        public String getDescription() { return description; }
        
        /**
         * 设置属性描述
         * 
         * @param description 属性描述，可以为null
         */
        public void setDescription(String description) { this.description = description; }
        
        /**
         * 获取原始默认值
         * 
         * @return 默认值，如果未设置则返回null
         */
        public Object getDefaultValue() { return defaultValue; }
        
        /**
         * 设置默认值
         * 
         * @param defaultValue 默认值，可以为null
         */
        public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }
        
        /**
         * 获取字符串形式的默认值（用于显示）
         * 
         * <p>该方法将默认值转换为字符串，用于显示和比较。
         * 如果默认值为null，返回空字符串。</p>
         * 
         * @return 默认值的字符串表示，如果默认值为null则返回空字符串
         */
        public String getDefaultValueAsString() {
            if (defaultValue == null) {
                return "";
            }
            return defaultValue.toString();
        }
        
        /**
         * 获取布尔形式的默认值
         * 
         * <p>该方法尝试将默认值转换为布尔类型，支持多种输入格式：
         * 布尔值、字符串（"true"/"false"、"1"/"0"）。</p>
         * 
         * @return 布尔值，如果无法转换则返回null
         */
        public Boolean getDefaultValueAsBoolean() {
            if (defaultValue == null) {
                return null;
            }
            if (defaultValue instanceof Boolean) {
                return (Boolean) defaultValue;
            }
            if (defaultValue instanceof String) {
                String str = ((String) defaultValue).toLowerCase().trim();
                if ("true".equals(str) || "1".equals(str)) {
                    return true;
                }
                if ("false".equals(str) || "0".equals(str)) {
                    return false;
                }
            }
            return null;
        }
        
        /**
         * 获取数字形式的默认值
         * 
         * <p>该方法尝试将默认值转换为数字类型，支持整数和小数。
         * 如果字符串包含小数点，则转换为Double；否则转换为Long。</p>
         * 
         * @return 数字值，如果无法转换则返回null
         */
        public Number getDefaultValueAsNumber() {
            if (defaultValue == null) {
                return null;
            }
            if (defaultValue instanceof Number) {
                return (Number) defaultValue;
            }
            if (defaultValue instanceof String) {
                try {
                    String str = ((String) defaultValue).trim();
                    if (str.contains(".")) {
                        return Double.parseDouble(str);
                    } else {
                        return Long.parseLong(str);
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        /**
         * 检查默认值是否为布尔类型
         * 
         * @return 如果默认值是布尔类型则返回true，否则返回false
         */
        public boolean isDefaultValueBoolean() {
            return defaultValue instanceof Boolean;
        }
        
        /**
         * 检查默认值是否为数字类型
         * 
         * @return 如果默认值是数字类型则返回true，否则返回false
         */
        public boolean isDefaultValueNumber() {
            return defaultValue instanceof Number;
        }
        
        /**
         * 检查默认值是否为字符串类型
         * 
         * @return 如果默认值是字符串类型则返回true，否则返回false
         */
        public boolean isDefaultValueString() {
            return defaultValue instanceof String;
        }
        
        /**
         * 检查属性是否必需
         * 
         * @return 如果属性必需则返回true，否则返回false
         */
        public boolean isRequired() { return required; }
        
        /**
         * 设置属性是否必需
         * 
         * @param required 是否必需
         */
        public void setRequired(boolean required) { this.required = required; }
        
        /**
         * 获取选项列表
         * 
         * @return 选项列表，如果未设置则返回null
         */
        public List<String> getOptions() { return options; }
        
        /**
         * 设置选项列表
         * 
         * @param options 选项列表，可以为null
         */
        public void setOptions(List<String> options) { this.options = options; }
    }

    /**
     * 组件事件
     * 
     * <p>表示组件支持的一个事件，包含事件的名称、描述和参数信息。</p>
     * 
     * @author VueKit Team
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class ComponentEvent {
        
        /**
         * 事件名称
         */
        private String name;
        
        /**
         * 事件描述
         */
        private String description;
        
        /**
         * 事件参数
         */
        private String parameters;

        /**
         * 默认构造函数
         */
        public ComponentEvent() {}

        /**
         * 全参数构造函数
         * 
         * @param name 事件名称，不能为null
         * @param description 事件描述，可以为null
         * @param parameters 事件参数，可以为null
         * @throws IllegalArgumentException 如果name为null
         */
        public ComponentEvent(String name, String description, String parameters) {
            if (name == null) {
                throw new IllegalArgumentException("事件名称不能为null");
            }
            
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        // ==================== Getter和Setter方法 ====================
        
        /**
         * 获取事件名称
         * 
         * @return 事件名称
         */
        public String getName() { return name; }
        
        /**
         * 设置事件名称
         * 
         * @param name 事件名称，不能为null
         * @throws IllegalArgumentException 如果name为null
         */
        public void setName(String name) { 
            if (name == null) {
                throw new IllegalArgumentException("事件名称不能为null");
            }
            this.name = name; 
        }
        
        /**
         * 获取事件描述
         * 
         * @return 事件描述，如果未设置则返回null
         */
        public String getDescription() { return description; }
        
        /**
         * 设置事件描述
         * 
         * @param description 事件描述，可以为null
         */
        public void setDescription(String description) { this.description = description; }
        
        /**
         * 获取事件参数
         * 
         * @return 事件参数，如果未设置则返回null
         */
        public String getParameters() { return parameters; }
        
        /**
         * 设置事件参数
         * 
         * @param parameters 事件参数，可以为null
         */
        public void setParameters(String parameters) { this.parameters = parameters; }
    }

    /**
     * 组件插槽
     * 
     * <p>表示组件提供的一个插槽，包含插槽的名称、描述和作用域信息。</p>
     * 
     * @author VueKit Team
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class ComponentSlot {
        
        /**
         * 插槽名称
         */
        private String name;
        
        /**
         * 插槽描述
         */
        private String description;
        
        /**
         * 插槽作用域
         */
        private String scope;

        /**
         * 默认构造函数
         */
        public ComponentSlot() {}

        /**
         * 全参数构造函数
         * 
         * @param name 插槽名称，不能为null
         * @param description 插槽描述，可以为null
         * @param scope 插槽作用域，可以为null
         * @throws IllegalArgumentException 如果name为null
         */
        public ComponentSlot(String name, String description, String scope) {
            if (name == null) {
                throw new IllegalArgumentException("插槽名称不能为null");
            }
            
            this.name = name;
            this.description = description;
            this.scope = scope;
        }

        // ==================== Getter和Setter方法 ====================
        
        /**
         * 获取插槽名称
         * 
         * @return 插槽名称
         */
        public String getName() { return name; }
        
        /**
         * 设置插槽名称
         * 
         * @param name 插槽名称，不能为null
         * @throws IllegalArgumentException 如果name为null
         */
        public void setName(String name) { 
            if (name == null) {
                throw new IllegalArgumentException("插槽名称不能为null");
            }
            this.name = name; 
        }
        
        /**
         * 获取插槽描述
         * 
         * @return 插槽描述，如果未设置则返回null
         */
        public String getDescription() { return description; }
        
        /**
         * 设置插槽描述
         * 
         * @param description 插槽描述，可以为null
         */
        public void setDescription(String description) { this.description = description; }
        
        /**
         * 获取插槽作用域
         * 
         * @return 插槽作用域，如果未设置则返回null
         */
        public String getScope() { return scope; }
        
        /**
         * 设置插槽作用域
         * 
         * @param scope 插槽作用域，可以为null
         */
        public void setScope(String scope) { this.scope = scope; }
    }
} 