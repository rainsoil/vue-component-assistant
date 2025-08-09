package com.chu7.vuecomponentassistant.constants;

/**
 * VueKit 插件常量定义
 * 
 * 统一管理插件中使用的所有常量，包括：
 * - 字符串常量
 * - 配置常量
 * - 界面文本
 * - 错误消息
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public final class VueKitConstants {
    
    // 防止实例化
    private VueKitConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
    
    // ==================== 插件基础信息 ====================
    
    /** 插件名称 */
    public static final String PLUGIN_NAME = "VueKit";
    
    /** 插件显示名称 */
    public static final String PLUGIN_DISPLAY_NAME = "Vue Kit";
    
    /** 插件版本 */
    public static final String PLUGIN_VERSION = "2.0.0";
    
    /** 插件作者 */
    public static final String PLUGIN_AUTHOR = "VueKit Team";
    
    // ==================== 组件库相关 ====================
    
    /** Element Plus 组件库名称 */
    public static final String ELEMENT_PLUS_LIBRARY = "element-plus";
    
    /** Element UI 组件库名称 */
    public static final String ELEMENT_UI_LIBRARY = "element-ui";
    
    /** Ant Design Vue 组件库名称 */
    public static final String ANT_DESIGN_VUE_LIBRARY = "ant-design-vue";
    
    /** Element Plus 组件前缀 */
    public static final String ELEMENT_PLUS_PREFIX = "el-";
    
    /** Ant Design Vue 组件前缀 */
    public static final String ANT_DESIGN_VUE_PREFIX = "a-";
    
    // ==================== 文件路径相关 ====================
    
    /** 组件数据文件路径前缀 */
    public static final String DATA_PATH_PREFIX = "/data/";
    
    /** Element Plus 组件数据文件 */
    public static final String ELEMENT_PLUS_DATA_FILE = "/data/element-plus-components.json";
    
    /** Element UI 组件数据文件 */
    public static final String ELEMENT_UI_DATA_FILE = "/data/element-ui-components.json";
    
    /** Ant Design Vue 组件数据文件 */
    public static final String ANT_DESIGN_VUE_DATA_FILE = "/data/ant-design-vue-components.json";
    
    /** 自定义组件库缓存文件名 */
    public static final String CUSTOM_LIBRARY_CACHE_FILE = "custom_component_libraries.json";
    
    /** 插件缓存目录名 */
    public static final String CACHE_DIRECTORY = "vue-component-assistant";
    
    // ==================== IntelliJ 相关 ====================
    
    /** IntelliJ IDEA 补全占位符 */
    public static final String INTELLIJ_COMPLETION_PLACEHOLDER = "IntellijIdeaRulezzz";
    
    /** 设置存储文件名 */
    public static final String SETTINGS_STORAGE_FILE = "vue-component-assistant-settings.xml";
    
    // ==================== 界面文本 ====================
    
    /** 组件补全类型文本 */
    public static final String COMPONENT_TYPE_TEXT = "组件";
    
    /** 属性补全类型文本 */
    public static final String PROPERTY_TYPE_TEXT = "属性";
    
    /** 事件补全类型文本 */
    public static final String EVENT_TYPE_TEXT = "事件";
    
    /** 插槽补全类型文本 */
    public static final String SLOT_TYPE_TEXT = "插槽";
    
    // ==================== 错误消息 ====================
    
    /** 无法获取项目信息错误 */
    public static final String ERROR_NO_PROJECT = "无法获取项目信息";
    
    /** 无法获取编辑器信息错误 */
    public static final String ERROR_NO_EDITOR = "无法获取编辑器信息";
    
    /** 无法获取当前元素错误 */
    public static final String ERROR_NO_ELEMENT = "无法获取当前元素";
    
    /** 无法识别组件错误 */
    public static final String ERROR_COMPONENT_NOT_RECOGNIZED = "无法识别组件";
    
    /** 文件读取失败错误 */
    public static final String ERROR_FILE_READ_FAILED = "文件读取失败";
    
    /** JSON解析失败错误 */
    public static final String ERROR_JSON_PARSE_FAILED = "JSON解析失败";
    
    // ==================== 成功消息 ====================
    
    /** 组件库加载成功 */
    public static final String SUCCESS_LIBRARY_LOADED = "组件库加载成功";
    
    /** 设置保存成功 */
    public static final String SUCCESS_SETTINGS_SAVED = "设置保存成功";
    
    /** 文档生成成功 */
    public static final String SUCCESS_DOCUMENTATION_GENERATED = "文档生成成功";
    
    // ==================== 日志消息 ====================
    
    /** 插件初始化日志 */
    public static final String LOG_PLUGIN_INITIALIZED = "VueKit 插件已初始化";
    
    /** 组件库检测日志 */
    public static final String LOG_LIBRARY_DETECTED = "检测到组件库";
    
    /** 补全提供者创建日志 */
    public static final String LOG_COMPLETION_PROVIDER_CREATED = "补全提供者已创建";
    
    /** 文档提供者创建日志 */
    public static final String LOG_DOCUMENTATION_PROVIDER_CREATED = "文档提供者已创建";
    
    // ==================== 配置默认值 ====================
    
    /** 默认缓存大小 */
    public static final int DEFAULT_CACHE_SIZE = 1000;
    
    /** 默认缓存过期时间（秒） */
    public static final int DEFAULT_CACHE_EXPIRE_TIME = 300;
    
    /** 补全响应时间阈值（毫秒） */
    public static final long COMPLETION_THRESHOLD_MS = 100;
    
    /** 文档加载时间阈值（毫秒） */
    public static final long DOCUMENTATION_THRESHOLD_MS = 200;
    
    /** 最大补全结果数量 */
    public static final int MAX_COMPLETION_RESULTS = 20;
    
    // ==================== 正则表达式 ====================
    
    /** 组件标签匹配正则 */
    public static final String COMPONENT_TAG_REGEX = "<([a-zA-Z][a-zA-Z0-9-]*)\\b";
    
    /** 属性匹配正则 */
    public static final String ATTRIBUTE_REGEX = "\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=";
    
    /** 事件匹配正则 */
    public static final String EVENT_REGEX = "@([a-zA-Z][a-zA-Z0-9-]*)\\s*=";
    
    /** 插槽匹配正则 */
    public static final String SLOT_REGEX = "#([a-zA-Z][a-zA-Z0-9-]*)\\s*=";
    
    /** Element Plus 组件匹配正则 */
    public static final String ELEMENT_PLUS_REGEX = "el-[a-zA-Z-]+";
    
    // ==================== 图标路径 ====================
    
    /** 组件图标路径 */
    public static final String COMPONENT_ICON_PATH = "/icons/component.svg";
    
    /** 属性图标路径 */
    public static final String PROPERTY_ICON_PATH = "/icons/property.svg";
    
    /** 事件图标路径 */
    public static final String EVENT_ICON_PATH = "/icons/event.svg";
    
    /** 插槽图标路径 */
    public static final String SLOT_ICON_PATH = "/icons/slot.svg";
    
    // ==================== HTML 模板 ====================
    
    /** 文档HTML模板开始 */
    public static final String DOC_HTML_START = "<div style='font-family: Arial, sans-serif; padding: 10px;'>";
    
    /** 文档HTML模板结束 */
    public static final String DOC_HTML_END = "</div>";
    
    /** 表格样式 */
    public static final String TABLE_STYLE = "border-collapse: collapse; width: 100%; margin: 10px 0;";
    
    /** 表头样式 */
    public static final String TABLE_HEADER_STYLE = "background-color: #f5f5f5; padding: 8px; border: 1px solid #ddd; font-weight: bold;";
    
    /** 表格单元格样式 */
    public static final String TABLE_CELL_STYLE = "padding: 8px; border: 1px solid #ddd;";
}