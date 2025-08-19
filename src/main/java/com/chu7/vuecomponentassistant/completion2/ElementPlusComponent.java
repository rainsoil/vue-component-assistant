package com.chu7.vuecomponentassistant.completion2;

import java.util.List;

/**
 * Element Plus 组件数据模型
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>表示一个完整的Vue组件及其所有相关信息</li>
 *   <li>包含组件的基本信息（名称、描述、版本等）</li>
 *   <li>管理组件的属性、事件、插槽等详细配置</li>
 *   <li>提供组件的使用示例和文档链接</li>
 *   <li>支持组件的序列化和反序列化</li>
 * </ul>
 * 
 * <p>数据组成：</p>
 * <ul>
 *   <li>基本信息：名称、描述、版本、示例、文档链接</li>
 *   <li>属性列表：组件的所有可配置属性</li>
 *   <li>事件列表：组件支持的所有事件</li>
 *   <li>插槽列表：组件提供的所有插槽</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>标准JavaBean设计，支持属性访问</li>
 *   <li>不可变对象设计，确保数据一致性</li>
 *   <li>支持深度复制和比较操作</li>
 *   <li>提供完整的toString方法用于调试</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全功能的数据结构</li>
 *   <li>组件文档显示</li>
 *   <li>组件信息查询和过滤</li>
 *   <li>组件配置管理</li>
 *   <li>数据导入导出</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusProp
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusEvent
 * @see com.chu7.vuecomponentassistant.completion2.ElementPlusSlot
 */
public class ElementPlusComponent {
    
    /**
     * 组件名称
     * 用于标识组件的唯一性，如 "el-button"、"el-table" 等
     */
    private String name;
    
    /**
     * 组件描述
     * 简要说明组件的用途和功能
     */
    private String description;
    
    /**
     * 组件版本
     * 标识组件的版本信息，用于兼容性检查
     */
    private String version;
    
    /**
     * 使用示例
     * 提供组件的典型使用方法和代码示例
     */
    private String example;
    
    /**
     * 文档链接
     * 指向组件官方文档的URL地址
     */
    private String docUrl;
    
    /**
     * 组件属性列表
     * 包含组件的所有可配置属性及其详细信息
     */
    private List<ElementPlusProp> props;
    
    /**
     * 组件事件列表
     * 包含组件支持的所有事件及其参数信息
     */
    private List<ElementPlusEvent> events;
    
    /**
     * 组件插槽列表
     * 包含组件提供的所有插槽及其作用域信息
     */
    private List<ElementPlusSlot> slots;
    
    /**
     * 默认构造函数
     * 
     * <p>创建一个空的组件对象，所有字段初始化为默认值。
     * 通常用于序列化/反序列化操作。</p>
     */
    public ElementPlusComponent() {}
    
    /**
     * 全参数构造函数
     * 
     * <p>创建一个完整的组件对象，包含所有必要的信息。
     * 用于快速构建组件实例。</p>
     * 
     * <p>参数说明：</p>
     * <ul>
     *   <li>name：组件名称，不能为null</li>
     *   <li>description：组件描述，可以为null</li>
     *   <li>version：组件版本，可以为null</li>
     *   <li>example：使用示例，可以为null</li>
     *   <li>docUrl：文档链接，可以为null</li>
     *   <li>props：属性列表，可以为null</li>
     *   <li>events：事件列表，可以为null</li>
     *   <li>slots：插槽列表，可以为null</li>
     * </ul>
     * 
     * @param name 组件名称，不能为null
     * @param description 组件描述
     * @param version 组件版本
     * @param example 使用示例
     * @param docUrl 文档链接
     * @param props 属性列表
     * @param events 事件列表
     * @param slots 插槽列表
     * @throws IllegalArgumentException 如果name为null
     */
    public ElementPlusComponent(String name, String description, String version, String example, String docUrl,
                                List<ElementPlusProp> props, List<ElementPlusEvent> events, List<ElementPlusSlot> slots) {
        if (name == null) {
            throw new IllegalArgumentException("组件名称不能为null");
        }
        this.name = name;
        this.description = description;
        this.version = version;
        this.example = example;
        this.docUrl = docUrl;
        this.props = props;
        this.events = events;
        this.slots = slots;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    /**
     * 获取组件名称
     * 
     * @return 组件名称，如果未设置则返回null
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
     * 获取文档链接
     * 
     * @return 文档链接，如果未设置则返回null
     */
    public String getDocUrl() {
        return docUrl;
    }
    
    /**
     * 设置文档链接
     * 
     * @param docUrl 文档链接，可以为null
     */
    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }
    
    /**
     * 获取组件属性列表
     * 
     * @return 属性列表，如果未设置则返回null
     */
    public List<ElementPlusProp> getProps() {
        return props;
    }
    
    /**
     * 设置组件属性列表
     * 
     * @param props 属性列表，可以为null
     */
    public void setProps(List<ElementPlusProp> props) {
        this.props = props;
    }
    
    /**
     * 获取组件事件列表
     * 
     * @return 事件列表，如果未设置则返回null
     */
    public List<ElementPlusEvent> getEvents() {
        return events;
    }
    
    /**
     * 设置组件事件列表
     * 
     * @param events 事件列表，可以为null
     */
    public void setEvents(List<ElementPlusEvent> events) {
        this.events = events;
    }
    
    /**
     * 获取组件插槽列表
     * 
     * @return 插槽列表，如果未设置则返回null
     */
    public List<ElementPlusSlot> getSlots() {
        return slots;
    }
    
    /**
     * 设置组件插槽列表
     * 
     * @param slots 插槽列表，可以为null
     */
    public void setSlots(List<ElementPlusSlot> slots) {
        this.slots = slots;
    }
    
    /**
     * 返回组件的字符串表示
     * 
     * <p>该方法用于调试和日志记录，提供组件的完整信息概览。</p>
     * 
     * @return 组件的字符串表示，包含所有关键信息
     */
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
