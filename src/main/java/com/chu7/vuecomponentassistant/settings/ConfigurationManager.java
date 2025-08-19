package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Properties;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配置管理器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供统一的配置管理功能，支持多种配置类型</li>
 *   <li>管理配置项的注册、验证和持久化</li>
 *   <li>支持配置变更通知和监听器机制</li>
 *   <li>提供配置的默认值管理和重置功能</li>
 *   <li>支持配置文件的加载和保存</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>线程安全：使用ConcurrentHashMap和AtomicBoolean确保并发安全</li>
 *   <li>类型安全：支持多种数据类型的配置值</li>
 *   <li>验证机制：配置值设置前进行类型和有效性验证</li>
 *   <li>监听器模式：支持配置变更的实时通知</li>
 *   <li>持久化存储：配置自动保存到用户主目录</li>
 * </ul>
 * 
 * <p>配置类型支持：</p>
 * <ol>
 *   <li>STRING：字符串类型配置</li>
 *   <li>BOOLEAN：布尔类型配置</li>
 *   <li>INTEGER：整数类型配置</li>
 *   <li>LONG：长整数类型配置</li>
 *   <li>DOUBLE：双精度浮点数类型配置</li>
 * </ol>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>插件全局配置管理</li>
 *   <li>性能监控配置</li>
 *   <li>缓存策略配置</li>
 *   <li>日志级别配置</li>
 *   <li>异步处理配置</li>
 *   <li>UI界面配置</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see java.util.concurrent.ConcurrentHashMap
 * @see java.util.concurrent.atomic.AtomicBoolean
 * @see java.nio.file.Files
 * @see java.util.Properties
 */
public class ConfigurationManager {
    
    /**
     * 日志记录器
     * 用于记录配置管理过程中的关键信息和错误
     */
    private static final Logger LOG = Logger.getInstance(ConfigurationManager.class);
    
    /**
     * 配置目录名称
     * 在用户主目录下创建的配置文件夹名称
     */
    private static final String CONFIG_DIR = ".vuekit";
    
    /**
     * 配置文件名称
     * 存储所有配置项的属性文件名
     */
    private static final String CONFIG_FILE = "vuekit.properties";
    
    /**
     * 配置项存储
     * 存储所有已注册的配置项定义，包括键、默认值、描述和类型
     */
    private final Map<String, ConfigItem> configItems;
    
    /**
     * 当前配置值存储
     * 存储所有配置项的当前值，支持运行时修改
     */
    private final Map<String, String> currentValues;
    
    /**
     * 配置文件路径
     * 指向用户主目录下的.vuekit/vuekit.properties文件
     */
    private final Path configPath;
    
    /**
     * 配置变更监听器
     * 存储配置键到监听器的映射，支持配置变更通知
     */
    private final Map<String, ConfigChangeListener> listeners;
    
    /**
     * 初始化状态
     * 使用AtomicBoolean确保多线程环境下的原子性操作
     */
    private final AtomicBoolean initialized;
    
    /**
     * 构造函数
     * 
     * <p>初始化配置管理器，创建必要的存储容器和注册默认配置项。</p>
     * 
     * <p>初始化流程：</p>
     * <ol>
     *   <li>创建配置项存储容器</li>
     *   <li>创建当前值存储容器</li>
     *   <li>创建监听器存储容器</li>
     *   <li>设置初始化状态为false</li>
     *   <li>确定配置文件路径</li>
     *   <li>注册默认配置项</li>
     * </ol>
     * 
     * <p>默认配置项：</p>
     * <ul>
     *   <li>性能监控相关配置</li>
     *   <li>缓存策略相关配置</li>
     *   <li>日志级别相关配置</li>
     *   <li>异步处理相关配置</li>
     *   <li>组件库相关配置</li>
     *   <li>UI界面相关配置</li>
     * </ul>
     * 
     * @see #registerDefaultConfigs()
     * @see java.nio.file.Paths#get(String, String...)
     * @see java.lang.System#getProperty(String)
     */
    public ConfigurationManager() {
        this.configItems = new ConcurrentHashMap<>();
        this.currentValues = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentHashMap<>();
        this.initialized = new AtomicBoolean(false);
        
        // 确定配置文件路径
        String userHome = System.getProperty("user.home");
        this.configPath = Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
        
        // 注册默认配置项
        registerDefaultConfigs();
        
        LOG.info("配置管理器初始化完成，配置文件路径: " + configPath);
    }
    
    /**
     * 注册默认配置项
     * 
     * <p>该方法注册VueKit插件的所有默认配置项，包括性能监控、缓存策略、
     * 日志级别、异步处理、组件库管理和UI界面等各个方面的配置。</p>
     * 
     * <p>配置分类：</p>
     * <ol>
     *   <li>性能监控配置：启用状态、阈值设置</li>
     *   <li>缓存策略配置：内存大小、磁盘大小、过期时间</li>
     *   <li>日志级别配置：日志级别、性能日志、调试日志</li>
     *   <li>异步处理配置：线程池大小、队列容量</li>
     *   <li>组件库配置：自动检测、自动更新、更新间隔</li>
     *   <li>UI界面配置：对话框尺寸、补全条目数</li>
     * </ol>
     * 
     * <p>配置项格式：</p>
     * <ul>
     *   <li>键：使用点分隔的层次结构</li>
     *   <li>默认值：根据配置类型设置合适的默认值</li>
     *   <li>描述：提供配置项的中文说明</li>
     *   <li>类型：指定配置值的数据类型</li>
     * </ul>
     * 
     * @see #registerConfig(String, String, String, ConfigType)
     * @see ConfigType
     */
    private void registerDefaultConfigs() {
        // 性能相关配置
        registerConfig("performance.monitoring.enabled", "true", "启用性能监控", ConfigType.BOOLEAN);
        registerConfig("performance.monitoring.threshold.slow", "1000", "慢操作阈值(毫秒)", ConfigType.LONG);
        registerConfig("performance.monitoring.threshold.critical", "5000", "严重操作阈值(毫秒)", ConfigType.LONG);
        
        // 缓存相关配置
        registerConfig("cache.memory.max_size", "100", "内存缓存最大条目数", ConfigType.INTEGER);
        registerConfig("cache.disk.max_size_mb", "500", "磁盘缓存最大大小(MB)", ConfigType.INTEGER);
        registerConfig("cache.expiry.hours", "168", "缓存过期时间(小时)", ConfigType.INTEGER);
        
        // 日志相关配置
        registerConfig("logging.level", "INFO", "日志级别", ConfigType.STRING);
        registerConfig("logging.performance.enabled", "true", "启用性能日志", ConfigType.BOOLEAN);
        registerConfig("logging.debug.enabled", "false", "启用调试日志", ConfigType.BOOLEAN);
        
        // 异步处理配置
        registerConfig("async.core_pool_size", "2", "异步处理核心线程数", ConfigType.INTEGER);
        registerConfig("async.max_pool_size", "8", "异步处理最大线程数", ConfigType.INTEGER);
        registerConfig("async.queue_capacity", "1000", "异步处理队列容量", ConfigType.INTEGER);
        
        // 组件库配置
        registerConfig("component.library.auto_detect", "true", "自动检测组件库", ConfigType.BOOLEAN);
        registerConfig("component.library.auto_update", "false", "自动更新组件库", ConfigType.BOOLEAN);
        registerConfig("component.library.update_interval_hours", "24", "组件库更新间隔(小时)", ConfigType.INTEGER);
        
        // UI相关配置
        registerConfig("ui.dialog.max_width", "800", "对话框最大宽度", ConfigType.INTEGER);
        registerConfig("ui.dialog.max_height", "600", "对话框最大高度", ConfigType.INTEGER);
        registerConfig("ui.completion.max_items", "100", "补全最大条目数", ConfigType.INTEGER);
    }
    
    /**
     * 注册配置项
     * 
     * <p>该方法用于注册新的配置项，包括配置键、默认值、描述和类型。
     * 注册后的配置项可以正常使用getConfig和setConfig方法。</p>
     * 
     * <p>注册流程：</p>
     * <ol>
     *   <li>创建ConfigItem对象</li>
     *   <li>添加到配置项存储</li>
     *   <li>设置当前值为默认值</li>
     *   <li>记录调试日志</li>
     * </ol>
     * 
     * <p>配置键规范：</p>
     * <ul>
     *   <li>使用点分隔的层次结构，如 "performance.monitoring.enabled"</li>
     *   <li>键名应该具有描述性和一致性</li>
     *   <li>避免使用特殊字符和空格</li>
     * </ul>
     * 
     * @param key 配置键，不能为null，建议使用层次结构命名
     * @param defaultValue 默认值，不能为null，应该与类型匹配
     * @param description 配置描述，不能为null，提供配置项的用途说明
     * @param type 配置类型，不能为null，决定配置值的数据类型
     * 
     * @see ConfigItem
     * @see ConfigType
     */
    public void registerConfig(String key, String defaultValue, String description, ConfigType type) {
        ConfigItem item = new ConfigItem(key, defaultValue, description, type);
        configItems.put(key, item);
        currentValues.put(key, defaultValue);
        
        LOG.debug("注册配置项: " + key + " = " + defaultValue + " (" + type + ")");
    }
    
    /**
     * 获取配置值
     * 
     * <p>该方法返回指定配置键的当前值。如果配置项不存在，返回空字符串。</p>
     * 
     * <p>获取策略：</p>
     * <ul>
     *   <li>优先从currentValues中获取</li>
     *   <li>如果不存在，返回空字符串</li>
     *   <li>不进行类型转换，返回原始字符串值</li>
     * </ul>
     * 
     * @param key 配置键，不能为null
     * @return 配置值，如果不存在则返回空字符串
     * 
     * @see #getConfig(String, Class)
     */
    public String getConfig(String key) {
        return currentValues.getOrDefault(key, "");
    }
    
    /**
     * 获取配置值（带类型转换）
     * 
     * <p>该方法返回指定配置键的当前值，并自动转换为指定的目标类型。
     * 支持BOOLEAN、INTEGER、LONG、DOUBLE和STRING类型转换。</p>
     * 
     * <p>类型转换规则：</p>
     * <ul>
     *   <li>BOOLEAN：支持"true"/"false"字符串转换</li>
     *   <li>INTEGER：支持整数字符串转换</li>
     *   <li>LONG：支持长整数字符串转换</li>
     *   <li>DOUBLE：支持浮点数字符串转换</li>
     *   <li>STRING：直接返回字符串值</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>配置项不存在时返回null</li>
     *   <li>类型转换失败时记录警告日志并返回null</li>
     *   <li>使用泛型确保类型安全</li>
     * </ul>
     * 
     * @param key 配置键，不能为null
     * @param type 目标类型，不能为null
     * @param <T> 目标类型的泛型参数
     * @return 转换后的配置值，如果转换失败则返回null
     * 
     * @see #getConfig(String)
     * @see ConfigType
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, Class<T> type) {
        String value = getConfig(key);
        ConfigItem item = configItems.get(key);
        
        if (item == null) {
            return null;
        }
        
        try {
            switch (item.type) {
                case BOOLEAN:
                    return (T) Boolean.valueOf(value);
                case INTEGER:
                    return (T) Integer.valueOf(value);
                case LONG:
                    return (T) Long.valueOf(value);
                case DOUBLE:
                    return (T) Double.valueOf(value);
                case STRING:
                default:
                    return (T) value;
            }
        } catch (NumberFormatException e) {
            LOG.warn("配置值类型转换失败: " + key + " = " + value + ", 目标类型: " + type, e);
            return null;
        }
    }
    
    /**
     * 设置配置值
     * 
     * <p>该方法设置指定配置键的值，包括值验证、变更通知和日志记录。</p>
     * 
     * <p>设置流程：</p>
     * <ol>
     *   <li>验证配置项是否存在</li>
     *   <li>验证配置值的有效性</li>
     *   <li>更新当前值</li>
     *   <li>通知配置变更监听器</li>
     *   <li>记录调试日志</li>
     * </ol>
     * 
     * <p>验证机制：</p>
     * <ul>
     *   <li>检查配置项是否已注册</li>
     *   <li>根据配置类型验证值的有效性</li>
     *   <li>无效值时记录警告日志并拒绝设置</li>
     * </ul>
     * 
     * @param key 配置键，不能为null
     * @param value 配置值，不能为null，必须与配置类型匹配
     * 
     * @see #validateConfigValue(ConfigItem, String)
     * @see #notifyConfigChange(String, String, String)
     */
    public void setConfig(String key, String value) {
        ConfigItem item = configItems.get(key);
        if (item == null) {
            LOG.warn("尝试设置未注册的配置项: " + key);
            return;
        }
        
        // 验证配置值
        if (!validateConfigValue(item, value)) {
            LOG.warn("配置值验证失败: " + key + " = " + value);
            return;
        }
        
        String oldValue = currentValues.get(key);
        currentValues.put(key, value);
        
        // 通知配置变更
        notifyConfigChange(key, oldValue, value);
        
        LOG.debug("配置值已更新: " + key + " = " + value);
    }
    
    /**
     * 验证配置值
     * 
     * @param item 配置项
     * @param value 配置值
     * @return 是否有效
     */
    private boolean validateConfigValue(ConfigItem item, String value) {
        try {
            switch (item.type) {
                case BOOLEAN:
                    return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
                case INTEGER:
                    Integer.parseInt(value);
                    return true;
                case LONG:
                    Long.parseLong(value);
                    return true;
                case DOUBLE:
                    Double.parseDouble(value);
                    return true;
                case STRING:
                default:
                    return value != null && !value.trim().isEmpty();
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 重置配置到默认值
     * 
     * @param key 配置键
     */
    public void resetConfig(String key) {
        ConfigItem item = configItems.get(key);
        if (item != null) {
            setConfig(key, item.defaultValue);
        }
    }
    
    /**
     * 重置所有配置到默认值
     */
    public void resetAllConfigs() {
        configItems.values().forEach(item -> setConfig(item.key, item.defaultValue));
        LOG.info("所有配置已重置到默认值");
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                LOG.info("配置文件不存在，使用默认配置");
                return;
            }
            
            Properties props = new Properties();
            try (InputStream input = Files.newInputStream(configPath)) {
                props.load(input);
            }
            
            // 加载配置值
            props.forEach((key, value) -> {
                String keyStr = key.toString();
                if (configItems.containsKey(keyStr)) {
                    setConfig(keyStr, value.toString());
                }
            });
            
            LOG.info("配置文件加载完成: " + configPath);
        } catch (IOException e) {
            LOG.error("加载配置文件失败: " + configPath, e);
        }
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            // 确保配置目录存在
            Path configDir = configPath.getParent();
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            Properties props = new Properties();
            currentValues.forEach(props::setProperty);
            
            try (OutputStream output = Files.newOutputStream(configPath)) {
                props.store(output, "VueKit Configuration");
            }
            
            LOG.info("配置文件保存完成: " + configPath);
        } catch (IOException e) {
            LOG.error("保存配置文件失败: " + configPath, e);
        }
    }
    
    /**
     * 添加配置变更监听器
     * 
     * @param key 配置键
     * @param listener 监听器
     */
    public void addConfigChangeListener(String key, ConfigChangeListener listener) {
        listeners.put(key, listener);
    }
    
    /**
     * 移除配置变更监听器
     * 
     * @param key 配置键
     */
    public void removeConfigChangeListener(String key) {
        listeners.remove(key);
    }
    
    /**
     * 通知配置变更
     * 
     * @param key 配置键
     * @param oldValue 旧值
     * @param newValue 新值
     */
    private void notifyConfigChange(String key, String oldValue, String newValue) {
        ConfigChangeListener listener = listeners.get(key);
        if (listener != null) {
            try {
                listener.onConfigChanged(key, oldValue, newValue);
            } catch (Exception e) {
                LOG.error("配置变更监听器执行失败: " + key, e);
            }
        }
    }
    
    /**
     * 获取所有配置项
     * 
     * @return 配置项映射
     */
    public Map<String, ConfigItem> getAllConfigItems() {
        return new ConcurrentHashMap<>(configItems);
    }
    
    /**
     * 获取当前配置值
     * 
     * @return 当前配置值映射
     */
    public Map<String, String> getCurrentValues() {
        return new ConcurrentHashMap<>(currentValues);
    }
    
    /**
     * 配置项
     */
    public static class ConfigItem {
        private final String key;
        private final String defaultValue;
        private final String description;
        private final ConfigType type;
        
        public ConfigItem(String key, String defaultValue, String description, ConfigType type) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.description = description;
            this.type = type;
        }
        
        // Getters
        public String getKey() { return key; }
        public String getDefaultValue() { return defaultValue; }
        public String getDescription() { return description; }
        public ConfigType getType() { return type; }
    }
    
    /**
     * 配置类型
     */
    public enum ConfigType {
        STRING, BOOLEAN, INTEGER, LONG, DOUBLE
    }
    
    /**
     * 配置变更监听器
     */
    public interface ConfigChangeListener {
        void onConfigChanged(String key, String oldValue, String newValue);
    }
}
