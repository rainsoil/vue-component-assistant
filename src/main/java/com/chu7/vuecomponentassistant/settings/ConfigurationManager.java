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
 * 提供统一的配置管理功能，包括：
 * - 配置项注册和管理
 * - 配置持久化
 * - 配置验证
 * - 配置变更通知
 * - 默认值管理
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ConfigurationManager {
    
    private static final Logger LOG = Logger.getInstance(ConfigurationManager.class);
    
    // 配置文件路径
    private static final String CONFIG_DIR = ".vuekit";
    private static final String CONFIG_FILE = "vuekit.properties";
    
    // 配置存储
    private final Map<String, ConfigItem> configItems;
    private final Map<String, String> currentValues;
    private final Path configPath;
    
    // 配置变更监听器
    private final Map<String, ConfigChangeListener> listeners;
    
    // 初始化状态
    private final AtomicBoolean initialized;
    
    /**
     * 构造函数
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
     * @param key 配置键
     * @param defaultValue 默认值
     * @param description 配置描述
     * @param type 配置类型
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
     * @param key 配置键
     * @return 配置值
     */
    public String getConfig(String key) {
        return currentValues.getOrDefault(key, "");
    }
    
    /**
     * 获取配置值（带类型转换）
     * 
     * @param key 配置键
     * @param type 目标类型
     * @param <T> 目标类型
     * @return 转换后的配置值
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
     * @param key 配置键
     * @param value 配置值
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
