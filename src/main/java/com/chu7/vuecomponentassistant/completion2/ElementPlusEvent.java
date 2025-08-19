package com.chu7.vuecomponentassistant.completion2;

/**
 * Element Plus 事件数据模型
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>表示Vue组件支持的一个事件</li>
 *   <li>包含事件的名称、描述、参数等基本信息</li>
 *   <li>提供事件的完整元数据信息</li>
 *   <li>支持事件参数的描述和说明</li>
 *   <li>用于代码补全和文档显示</li>
 * </ul>
 * 
 * <p>数据组成：</p>
 * <ul>
 *   <li>事件名称：用于标识事件的唯一性</li>
 *   <li>事件描述：详细说明事件的触发条件和作用</li>
 *   <li>事件参数：描述事件回调函数的参数信息</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>标准JavaBean设计，支持属性访问</li>
 *   <li>支持多种构造函数重载</li>
 *   <li>提供完整的toString方法用于调试</li>
 *   <li>不可变对象设计，确保数据一致性</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全中的事件提示</li>
 *   <li>事件文档显示</li>
 *   <li>事件参数验证</li>
 *   <li>组件事件管理</li>
 *   <li>代码生成和模板</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
 */
public class ElementPlusEvent {
    
    /**
     * 事件名称
     * 用于标识事件的唯一性，如 "click"、"change"、"input" 等
     */
    private String name;
    
    /**
     * 事件描述
     * 详细说明事件的触发条件、作用和行为
     */
    private String description;
    
    /**
     * 事件参数
     * 描述事件回调函数的参数信息，如 "(event)"、"(value, oldValue)" 等
     */
    private String parameters;
    
    /**
     * 默认构造函数
     * 
     * <p>创建一个空的事件对象，所有字段初始化为默认值。
     * 通常用于序列化/反序列化操作。</p>
     */
    public ElementPlusEvent() {}
    
    /**
     * 全参数构造函数
     * 
     * <p>创建一个完整的事件对象，包含所有必要的信息。
     * 用于快速构建事件实例。</p>
     * 
     * @param name 事件名称，不能为null
     * @param description 事件描述，可以为null
     * @param parameters 事件参数，可以为null
     * @throws IllegalArgumentException 如果name为null
     */
    public ElementPlusEvent(String name, String description, String parameters) {
        if (name == null) {
            throw new IllegalArgumentException("事件名称不能为null");
        }
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    /**
     * 获取事件名称
     * 
     * @return 事件名称，如果未设置则返回null
     */
    public String getName() {
        return name;
    }
    
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
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置事件描述
     * 
     * @param description 事件描述，可以为null
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 获取事件参数
     * 
     * @return 事件参数，如果未设置则返回null
     */
    public String getParameters() {
        return parameters;
    }
    
    /**
     * 设置事件参数
     * 
     * @param parameters 事件参数，可以为null
     */
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    /**
     * 返回事件的字符串表示
     * 
     * <p>该方法用于调试和日志记录，提供事件的完整信息概览。</p>
     * 
     * @return 事件的字符串表示，包含所有关键信息
     */
    @Override
    public String toString() {
        return "ElementPlusEvent{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parameters='" + parameters + '\'' +
                '}';
    }
}
