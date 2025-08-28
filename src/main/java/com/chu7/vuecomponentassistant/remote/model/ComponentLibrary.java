package com.chu7.vuecomponentassistant.remote.model;

import java.util.List;

/**
 * 组件库数据模型
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>表示一个完整的组件库及其所有相关信息</li>
 *   <li>包含组件库的基本信息（ID、名称、描述、版本等）</li>
 *   <li>管理组件库的来源类型和远程URL</li>
 *   <li>提供组件前缀和组件列表管理</li>
 *   <li>支持多种来源类型的组件库</li>
 * </ul>
 * 
 * <p>数据组成：</p>
 * <ul>
 *   <li>基本信息：ID、名称、显示名称、描述、版本</li>
 *   <li>来源信息：来源类型、远程URL、最后更新时间</li>
 *   <li>配置信息：组件前缀、组件列表</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>标准JavaBean设计，支持属性访问</li>
 *   <li>支持多种构造函数重载</li>
 *   <li>提供枚举类型和字符串类型的来源支持</li>
 *   <li>自动设置最后更新时间</li>
 *   <li>完整的toString方法用于调试</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库的导入和管理</li>
 *   <li>组件库信息的存储和传输</li>
 *   <li>组件库配置的管理</li>
 *   <li>组件库数据的序列化和反序列化</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentInfo
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary.LibrarySource
 */
public class ComponentLibrary {
	
	/**
	 * 组件库唯一标识
	 * 用于在系统中唯一标识一个组件库
	 */
	private String id;
	
	/**
	 * 组件库名称
	 * 通常是包名，如 "element-plus"、"ant-design-vue" 等
	 */
	private String name;
	
	/**
	 * 显示名称
	 * 在用户界面中显示的友好名称
	 */
	private String displayName;
	
	/**
	 * 组件库描述
	 * 简要说明组件库的用途和功能
	 */
	private String description;
	
	/**
	 * 组件库版本
	 * 标识组件库的版本信息
	 */
	private String version;
	
	/**
	 * 来源类型（字符串形式）
	 * 用于JSON反序列化，支持向后兼容
	 */
	private String source;
	
	/**
	 * 远程URL
	 * 如果组件库来自远程源，则包含下载URL
	 */
	private String sourceUrl;
	
	/**
	 * 最后更新时间
	 * 记录组件库信息的最后更新时间
	 */
	private String lastUpdated;
	
	/**
	 * 组件前缀
	 * 组件的命名前缀，如 "el-"、"a-" 等
	 */
	private String componentPrefix;
	
	/**
	 * 组件列表
	 * 该组件库包含的所有组件信息
	 */
	private List<ComponentInfo> components;

	/**
	 * 组件库来源类型枚举
	 * 
	 * <p>定义了组件库的三种主要来源类型：</p>
	 * <ul>
	 *   <li>OFFICIAL：官方组件库，由VueKit团队维护</li>
	 *   <li>CUSTOM_LOCAL：自定义本地组件库，用户本地创建</li>
	 *   <li>CUSTOM_REMOTE：自定义远程组件库，从远程源下载</li>
	 * </ul>
	 * 
	 * @author VueKit Team
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	public enum LibrarySource {
		
		/**
		 * 官方组件库
		 * 由VueKit团队维护和更新的标准组件库
		 */
		OFFICIAL("官方组件库"),
		
		/**
		 * 自定义本地组件库
		 * 用户在本地创建和管理的组件库
		 */
		CUSTOM_LOCAL("自定义本地"),
		
		/**
		 * 自定义远程组件库
		 * 从远程源下载和管理的组件库
		 */
		CUSTOM_REMOTE("自定义远程");

		/**
		 * 来源类型的显示名称
		 * 用于在用户界面中显示友好的中文名称
		 */
		private final String displayName;

		/**
		 * 枚举构造函数
		 * 
		 * @param displayName 来源类型的显示名称
		 */
		LibrarySource(String displayName) {
			this.displayName = displayName;
		}

		/**
		 * 获取来源类型的显示名称
		 * 
		 * @return 来源类型的友好显示名称
		 */
		public String getDisplayName() {
			return displayName;
		}
	}

	// ==================== 构造函数 ====================
	
	/**
	 * 默认构造函数
	 * 
	 * <p>创建一个空的组件库对象，所有字段初始化为默认值。
	 * 通常用于序列化/反序列化操作。</p>
	 */
	public ComponentLibrary() {}

	/**
	 * 基础构造函数
	 * 
	 * <p>创建一个基本的组件库对象，不包含组件前缀。
	 * 适用于大多数标准组件库。</p>
	 * 
	 * @param id 组件库唯一标识，不能为null
	 * @param name 组件库名称，不能为null
	 * @param displayName 显示名称，可以为null
	 * @param description 描述，可以为null
	 * @param version 版本，可以为null
	 * @param source 来源类型，可以为null
	 * @param sourceUrl 远程URL，可以为null
	 * @throws IllegalArgumentException 如果id或name为null
	 */
	public ComponentLibrary(String id, String name, String displayName, String description, 
						  String version, LibrarySource source, String sourceUrl) {
		if (id == null) {
			throw new IllegalArgumentException("组件库ID不能为null");
		}
		if (name == null) {
			throw new IllegalArgumentException("组件库名称不能为null");
		}
		
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.version = version;
		this.source = source != null ? source.name() : null;
		this.sourceUrl = sourceUrl;
		this.lastUpdated = java.time.LocalDateTime.now().toString();
	}
	
	/**
	 * 字符串来源类型构造函数
	 * 
	 * <p>创建一个使用字符串来源类型的组件库对象。
	 * 主要用于JSON反序列化和向后兼容。</p>
	 * 
	 * @param id 组件库唯一标识，不能为null
	 * @param name 组件库名称，不能为null
	 * @param displayName 显示名称，可以为null
	 * @param description 描述，可以为null
	 * @param version 版本，可以为null
	 * @param source 来源类型字符串，可以为null
	 * @param sourceUrl 远程URL，可以为null
	 * @throws IllegalArgumentException 如果id或name为null
	 */
	public ComponentLibrary(String id, String name, String displayName, String description, 
						  String version, String source, String sourceUrl) {
		if (id == null) {
			throw new IllegalArgumentException("组件库ID不能为null");
		}
		if (name == null) {
			throw new IllegalArgumentException("组件库名称不能为null");
		}
		
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.version = version;
		this.source = source;
		this.sourceUrl = sourceUrl;
		this.lastUpdated = java.time.LocalDateTime.now().toString();
	}
	
	/**
	 * 完整构造函数
	 * 
	 * <p>创建一个完整的组件库对象，包含所有字段信息。
	 * 适用于需要完整配置的组件库。</p>
	 * 
	 * @param id 组件库唯一标识，不能为null
	 * @param name 组件库名称，不能为null
	 * @param displayName 显示名称，可以为null
	 * @param description 描述，可以为null
	 * @param version 版本，可以为null
	 * @param source 来源类型字符串，可以为null
	 * @param sourceUrl 远程URL，可以为null
	 * @param componentPrefix 组件前缀，可以为null
	 * @throws IllegalArgumentException 如果id或name为null
	 */
	public ComponentLibrary(String id, String name, String displayName, String description, 
						  String version, String source, String sourceUrl, String componentPrefix) {
		if (id == null) {
			throw new IllegalArgumentException("组件库ID不能为null");
		}
		if (name == null) {
			throw new IllegalArgumentException("组件库名称不能为null");
		}
		
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.version = version;
		this.source = source;
		this.sourceUrl = sourceUrl;
		this.componentPrefix = componentPrefix;
		this.lastUpdated = java.time.LocalDateTime.now().toString();
	}

	// ==================== Getter和Setter方法 ====================
	
	/**
	 * 获取组件库ID
	 * 
	 * @return 组件库的唯一标识
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置组件库ID
	 * 
	 * @param id 组件库的唯一标识，不能为null
	 * @throws IllegalArgumentException 如果id为null
	 */
	public void setId(String id) {
		if (id == null) {
			throw new IllegalArgumentException("组件库ID不能为null");
		}
		this.id = id;
	}

	/**
	 * 获取组件库名称
	 * 
	 * @return 组件库名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置组件库名称
	 * 
	 * @param name 组件库名称，不能为null
	 * @throws IllegalArgumentException 如果name为null
	 */
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("组件库名称不能为null");
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
	 * 获取组件库描述
	 * 
	 * @return 组件库描述，如果未设置则返回null
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 设置组件库描述
	 * 
	 * @param description 组件库描述，可以为null
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 获取组件库版本
	 * 
	 * @return 组件库版本，如果未设置则返回null
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * 设置组件库版本
	 * 
	 * @param version 组件库版本，可以为null
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * 获取来源类型（字符串形式）
	 * 
	 * @return 来源类型字符串，如果未设置则返回null
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 设置来源类型（字符串形式）
	 * 
	 * @param source 来源类型字符串，可以为null
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	/**
	 * 获取来源类型（枚举形式）
	 * 
	 * <p>该方法提供枚举类型的来源类型访问，用于向后兼容。
	 * 如果来源类型字符串无法转换为枚举，返回null。</p>
	 * 
	 * @return 来源类型枚举，如果无法转换则返回null
	 * 
	 * @see LibrarySource
	 */
	public LibrarySource getSourceAsEnum() {
		if (source == null) {
			return null;
		}
		try {
			return LibrarySource.valueOf(source.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * 获取远程URL
	 * 
	 * @return 远程URL，如果未设置则返回null
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}

	/**
	 * 设置远程URL
	 * 
	 * @param sourceUrl 远程URL，可以为null
	 */
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	/**
	 * 获取组件前缀
	 * 
	 * @return 组件前缀，如果未设置则返回null
	 */
	public String getComponentPrefix() {
		return componentPrefix;
	}
	
	/**
	 * 设置组件前缀
	 * 
	 * @param componentPrefix 组件前缀，可以为null
	 */
	public void setComponentPrefix(String componentPrefix) {
		this.componentPrefix = componentPrefix;
	}

	/**
	 * 获取最后更新时间
	 * 
	 * @return 最后更新时间字符串，如果未设置则返回null
	 */
	public String getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * 设置最后更新时间
	 * 
	 * @param lastUpdated 最后更新时间字符串，可以为null
	 */
	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * 获取组件列表
	 * 
	 * @return 组件列表，如果未设置则返回null
	 */
	public List<ComponentInfo> getComponents() {
		return components;
	}

	/**
	 * 设置组件列表
	 * 
	 * @param components 组件列表，可以为null
	 */
	public void setComponents(List<ComponentInfo> components) {
		this.components = components;
	}

	/**
	 * 返回组件库的字符串表示
	 * 
	 * <p>该方法用于调试和日志记录，提供组件库的关键信息概览。</p>
	 * 
	 * @return 组件库的字符串表示，包含主要字段信息
	 */
	@Override
	public String toString() {
		return "ComponentLibrary{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", displayName='" + displayName + '\'' +
				", version='" + version + '\'' +
				", source=" + source +
				'}';
	}
} 