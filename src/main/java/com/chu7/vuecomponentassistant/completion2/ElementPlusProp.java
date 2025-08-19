package com.chu7.vuecomponentassistant.completion2;

import java.util.List;

/**
 * Element Plus 属性数据模型
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>表示Vue组件的一个可配置属性</li>
 *   <li>包含属性的名称、类型、描述等基本信息</li>
 *   <li>管理属性的默认值、是否必需等配置</li>
 *   <li>支持枚举值选项的配置</li>
 *   <li>提供属性的完整元数据信息</li>
 * </ul>
 * 
 * <p>数据组成：</p>
 * <ul>
 *   <li>基本信息：名称、类型、描述</li>
 *   <li>配置信息：默认值、是否必需</li>
 *   <li>选项信息：枚举值列表（可选）</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>标准JavaBean设计，支持属性访问</li>
 *   <li>支持多种构造函数重载</li>
 *   <li>提供默认值的字符串表示</li>
 *   <li>完整的toString方法用于调试</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全中的属性提示</li>
 *   <li>属性文档显示</li>
 *   <li>属性验证和类型检查</li>
 *   <li>组件配置管理</li>
 *   <li>代码生成和模板</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
 */
public class ElementPlusProp {
    
    /**
     * 属性名称
     * 用于标识属性的唯一性，如 "size"、"type"、"disabled" 等
     */
    private String name;
    
    /**
     * 属性类型
     * 描述属性的数据类型，如 "String"、"Number"、"Boolean"、"Array" 等
     */
    private String type;
    
    /**
     * 属性描述
     * 详细说明属性的用途、作用和行为
     */
    private String description;
    
    /**
     * 默认值
     * 属性的默认值，可以是任意类型
     */
    private Object defaultValue;
    
    /**
     * 是否必需
     * 标识该属性是否为必需属性
     */
    private boolean required;
    
    /**
     * 选项列表
     * 当属性为枚举类型时，提供可选的值列表
     */
    private List<String> options;
    
    /**
     * 默认构造函数
     * 
     * <p>创建一个空的属性对象，所有字段初始化为默认值。
     * 通常用于序列化/反序列化操作。</p>
     */
    public ElementPlusProp() {}
    
    /**
     * 基础构造函数
     * 
     * <p>创建一个基本的属性对象，不包含选项列表。
     * 适用于大多数标准属性。</p>
     * 
     * @param name 属性名称，不能为null
     * @param type 属性类型，不能为null
     * @param description 属性描述，可以为null
     * @param defaultValue 默认值，可以为null
     * @param required 是否必需
     * @throws IllegalArgumentException 如果name或type为null
     */
    public ElementPlusProp(String name, String type, String description, Object defaultValue, boolean required) {
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
    
    /**
     * 完整构造函数
     * 
     * <p>创建一个完整的属性对象，包含所有字段信息。
     * 适用于需要枚举选项的属性。</p>
     * 
     * @param name 属性名称，不能为null
     * @param type 属性类型，不能为null
     * @param description 属性描述，可以为null
     * @param defaultValue 默认值，可以为null
     * @param required 是否必需
     * @param options 选项列表，可以为null
     * @throws IllegalArgumentException 如果name或type为null
     */
    public ElementPlusProp(String name, String type, String description, Object defaultValue, boolean required, List<String> options) {
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
        this.options = options;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    /**
     * 获取属性名称
     * 
     * @return 属性名称，如果未设置则返回null
     */
    public String getName() {
        return name;
    }
    
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
     * @return 属性类型，如果未设置则返回null
     */
    public String getType() {
        return type;
    }
    
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
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置属性描述
     * 
     * @param description 属性描述，可以为null
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 获取默认值
     * 
     * @return 默认值，如果未设置则返回null
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * 设置默认值
     * 
     * @param defaultValue 默认值，可以为null
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * 获取默认值的字符串表示
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
     * 检查属性是否必需
     * 
     * @return 如果属性必需则返回true，否则返回false
     */
    public boolean isRequired() {
        return required;
    }
    
    /**
     * 设置属性是否必需
     * 
     * @param required 是否必需
     */
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    /**
     * 获取选项列表
     * 
     * @return 选项列表，如果未设置则返回null
     */
    public List<String> getOptions() {
        return options;
    }
    
    /**
     * 设置选项列表
     * 
     * @param options 选项列表，可以为null
     */
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    /**
     * 返回属性的字符串表示
     * 
     * <p>该方法用于调试和日志记录，提供属性的完整信息概览。</p>
     * 
     * @return 属性的字符串表示，包含所有关键信息
     */
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
