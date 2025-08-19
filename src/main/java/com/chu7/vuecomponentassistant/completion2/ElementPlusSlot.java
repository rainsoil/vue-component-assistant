package com.chu7.vuecomponentassistant.completion2;

/**
 * Element Plus 插槽数据模型
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>表示Vue组件提供的一个插槽</li>
 *   <li>包含插槽的名称、描述、作用域等基本信息</li>
 *   <li>提供插槽的完整元数据信息</li>
 *   <li>支持作用域插槽的参数描述</li>
 *   <li>用于代码补全和文档显示</li>
 * </ul>
 * 
 * <p>数据组成：</p>
 * <ul>
 *   <li>插槽名称：用于标识插槽的唯一性</li>
 *   <li>插槽描述：详细说明插槽的用途和内容</li>
 *   <li>作用域参数：描述作用域插槽的参数信息</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>标准JavaBean设计，支持属性访问</li>
 *   <li>支持多种构造函数重载</li>
 *   <li>提供完整的toString方法用于调试</li>
 *   <li>支持作用域插槽的配置</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全中的插槽提示</li>
 *   <li>插槽文档显示</li>
 *   <li>插槽参数验证</li>
 *   <li>组件插槽管理</li>
 *   <li>代码生成和模板</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
 */
public class ElementPlusSlot {
    
    /**
     * 插槽名称
     * 用于标识插槽的唯一性，如 "default"、"header"、"footer" 等
     */
    private String name;
    
    /**
     * 插槽描述
     * 详细说明插槽的用途、内容和布局
     */
    private String description;

    /**
     * 插槽作用域参数
     * 描述作用域插槽的参数信息，如 "{ row, column, $index }" 等
     */
    public String scope;

    /**
     * 获取插槽作用域参数
     * 
     * @return 作用域参数，如果未设置则返回null
     */
    public String getScope() {
        return scope;
    }

    /**
     * 设置插槽作用域参数
     * 
     * @param scope 作用域参数，可以为null
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * 默认构造函数
     * 
     * <p>创建一个空的插槽对象，所有字段初始化为默认值。
     * 通常用于序列化/反序列化操作。</p>
     */
    public ElementPlusSlot() {}
    
    /**
     * 基础构造函数
     * 
     * <p>创建一个基本的插槽对象，不包含作用域参数。
     * 适用于普通插槽。</p>
     * 
     * @param name 插槽名称，不能为null
     * @param description 插槽描述，可以为null
     * @throws IllegalArgumentException 如果name为null
     */
    public ElementPlusSlot(String name, String description) {
        if (name == null) {
            throw new IllegalArgumentException("插槽名称不能为null");
        }
        this.name = name;
        this.description = description;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    /**
     * 获取插槽名称
     * 
     * @return 插槽名称，如果未设置则返回null
     */
    public String getName() {
        return name;
    }
    
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
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置插槽描述
     * 
     * @param description 插槽描述，可以为null
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 返回插槽的字符串表示
     * 
     * <p>该方法用于调试和日志记录，提供插槽的完整信息概览。</p>
     * 
     * @return 插槽的字符串表示，包含所有关键信息
     */
    @Override
    public String toString() {
        return "ElementPlusSlot{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
