package com.chu7.vuecomponentassistant.completion2;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义组件库管理器
 * 
 * 功能说明：
 * - 支持用户上传自定义组件库JSON文件
 * - 解析自定义组件库配置
 * - 管理多个自定义组件库
 * - 提供组件数据查询功能
 * - 支持数据持久化，重启后不丢失
 * 
 * 持久化机制：
 * - 使用文件系统进行数据持久化
 * - 缓存文件位置：用户主目录/.intellij_idea_system/vuekit/custom_component_libraries.json
 * - 启动时自动加载缓存数据
 * - 数据变更时自动保存到缓存文件
 * 
 * 支持的JSON格式：
 * 1. 单个组件库对象格式：
 *    {
 *      "name": "组件库名称",
 *      "displayName": "显示名称",
 *      "version": "版本号",
 *      "description": "描述",
 *      "componentPrefix": "组件前缀",
 *      "documentationUrlTemplate": "文档URL模板",
 *      "components": [...]
 *    }
 * 
 * 2. 多个组件库数组格式：
 *    [
 *      {组件库1配置},
 *      {组件库2配置}
 *    ]
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 */
public class CustomComponentLibraryManager {
    
    /** 日志记录器 */
    private static final Logger LOG = Logger.getInstance(CustomComponentLibraryManager.class);
    
    /**
     * 自定义组件库配置类
     * 
     * <p>用于存储单个自定义组件库的完整配置信息，
     * 包括基本信息、组件列表等。</p>
     * 
     * <p>配置信息包括：</p>
     * <ul>
     *   <li>基本信息：名称、显示名称、版本、描述</li>
     *   <li>组件配置：前缀、文档URL模板</li>
     *   <li>组件列表：具体的组件定义</li>
     * </ul>
     * 
     * <p>设计特点：</p>
     * <ul>
     *   <li>支持JSON序列化和反序列化</li>
     *   <li>提供完整的getter和setter方法</li>
     *   <li>自动初始化组件列表为空数组</li>
     *   <li>支持默认值处理</li>
     * </ul>
     * 
     * @author VueKit Team
     * @version 1.0.0
     * @since 1.0.0
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
     */
    public static class CustomLibraryConfig {
        /** 组件库唯一标识名称 */
        private String name;
        
        /** 组件库显示名称，用于UI展示 */
        private String displayName;
        
        /** 组件库版本号 */
        private String version;
        
        /** 组件库描述信息 */
        private String description;
        
        /** 组件前缀，如 "el-"、"a-"、"custom-" 等 */
        private String componentPrefix;
        
        /** 文档URL模板，支持 %s 占位符 */
        private String documentationUrlTemplate;
        
        /** 组件列表 */
        private List<ElementPlusComponent> components;
        
        /**
         * 默认构造函数，用于JSON序列化
         * 初始化组件列表为空数组
         */
        public CustomLibraryConfig() {
            this.components = new ArrayList<>();
        }
        
        // ==================== Getter 和 Setter 方法 ====================
        
        /**
         * 获取组件库名称
         * @return 组件库唯一标识名称
         */
        public String getName() { 
            return name; 
        }
        
        /**
         * 设置组件库名称
         * @param name 组件库唯一标识名称
         */
        public void setName(String name) { 
            this.name = name; 
        }
        
        /**
         * 获取组件库显示名称
         * @return 用于UI展示的显示名称
         */
        public String getDisplayName() { 
            return displayName; 
        }
        
        /**
         * 设置组件库显示名称
         * @param displayName 用于UI展示的显示名称
         */
        public void setDisplayName(String displayName) { 
            this.displayName = displayName; 
        }
        
        /**
         * 获取组件库版本号
         * @return 版本号字符串
         */
        public String getVersion() { 
            return version; 
        }
        
        /**
         * 设置组件库版本号
         * @param version 版本号字符串
         */
        public void setVersion(String version) { 
            this.version = version; 
        }
        
        /**
         * 获取组件库描述信息
         * @return 描述信息
         */
        public String getDescription() { 
            return description; 
        }
        
        /**
         * 设置组件库描述信息
         * @param description 描述信息
         */
        public void setDescription(String description) { 
            this.description = description; 
        }
        
        /**
         * 获取组件前缀
         * @return 组件前缀，如 "el-"、"a-" 等
         */
        public String getComponentPrefix() { 
            return componentPrefix; 
        }
        
        /**
         * 设置组件前缀
         * @param componentPrefix 组件前缀
         */
        public void setComponentPrefix(String componentPrefix) { 
            this.componentPrefix = componentPrefix; 
        }
        
        /**
         * 获取文档URL模板
         * @return 文档URL模板，支持 %s 占位符
         */
        public String getDocumentationUrlTemplate() { 
            return documentationUrlTemplate; 
        }
        
        /**
         * 设置文档URL模板
         * @param documentationUrlTemplate 文档URL模板
         */
        public void setDocumentationUrlTemplate(String documentationUrlTemplate) { 
            this.documentationUrlTemplate = documentationUrlTemplate; 
        }
        
        /**
         * 获取组件列表
         * 如果组件列表为null，则初始化为空列表
         * @return 组件列表
         */
        public List<ElementPlusComponent> getComponents() { 
            if (components == null) {
                components = new ArrayList<>();
            }
            return components; 
        }
        
        /**
         * 设置组件列表
         * @param components 组件列表
         */
        public void setComponents(List<ElementPlusComponent> components) { 
            this.components = components; 
        }
    }
    
    // ==================== 静态数据成员 ====================
    
    /** 内存中的自定义组件库配置映射表 */
    private static Map<String, CustomLibraryConfig> customLibraries = new HashMap<>();
    
    /** 组件名称到组件的快速查找映射表 */
    private static Map<String, Map<String, ElementPlusComponent>> componentMaps = new HashMap<>();
    
    /** 缓存文件名 */
    private static final String CACHE_FILE_NAME = "custom_component_libraries.json";
    
    /** 缓存文件路径 */
    private static Path cacheFilePath;
    
    // ==================== 静态初始化块 ====================
    
    static {
        // 初始化缓存文件路径和加载缓存数据
        initializeCache();
    }
    
    /**
     * 初始化缓存系统
     * 
     * <p>该方法在类加载时自动执行，负责初始化自定义组件库的缓存机制。</p>
     * 
     * <p>初始化流程：</p>
     * <ol>
     *   <li>获取用户主目录路径</li>
     *   <li>创建缓存目录结构</li>
     *   <li>设置缓存文件路径</li>
     *   <li>启动时自动加载缓存数据</li>
     * </ol>
     * 
     * <p>缓存目录结构：</p>
     * <ul>
     *   <li>基础路径：用户主目录/.intellij_idea_system/vuekit</li>
     *   <li>缓存文件：custom_component_libraries.json</li>
     *   <li>自动创建不存在的目录</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>不会中断类的初始化过程</li>
     *   <li>提供详细的错误信息</li>
     * </ul>
     * 
     * @see #loadFromCache()
     */
    private static void initializeCache() {
        try {
            // 获取用户主目录
            String userHome = System.getProperty("user.home");
            
                    // 创建缓存目录：用户主目录/.intellij_idea_system/vuekit
        Path cacheDir = Paths.get(userHome, ".intellij_idea_system", "vuekit");
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
                LOG.info("创建缓存目录: " + cacheDir);
            }
            
            // 设置缓存文件路径
            cacheFilePath = cacheDir.resolve(CACHE_FILE_NAME);
            LOG.info("自定义组件库缓存文件路径: " + cacheFilePath);
            
            // 启动时加载缓存数据
            loadFromCache();
        } catch (Exception e) {
            LOG.error("初始化缓存目录失败: " + e.getMessage(), e);
        }
    }
    
    // ==================== 缓存操作方法 ====================
    
    /**
     * 从缓存文件加载数据
     * 
     * <p>该方法从持久化缓存文件中加载自定义组件库数据，
     * 在系统启动时自动执行，确保数据不丢失。</p>
     * 
     * <p>加载流程：</p>
     * <ol>
     *   <li>检查缓存文件是否存在</li>
     *   <li>读取JSON文件内容</li>
     *   <li>解析JSON数据</li>
     *   <li>加载到内存中</li>
     *   <li>重建组件映射表</li>
     * </ol>
     * 
     * <p>支持的JSON格式：</p>
     * <ul>
     *   <li>单个组件库对象格式</li>
     *   <li>多个组件库数组格式</li>
     *   <li>自动识别格式类型</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>不会中断加载过程</li>
     *   <li>提供详细的加载统计信息</li>
     * </ul>
     * 
     * @see #loadSingleLibraryFromJson(JsonObject)
     * @see #saveToCache()
     */
    private static void loadFromCache() {
        try {
            if (Files.exists(cacheFilePath)) {
                // 读取缓存文件内容
                String jsonContent = new String(Files.readAllBytes(cacheFilePath), StandardCharsets.UTF_8);
                LOG.info("从缓存文件加载数据: " + jsonContent);
                
                // 解析JSON内容
                JsonElement jsonElement = JsonParser.parseString(jsonContent);
                if (jsonElement.isJsonArray()) {
                    // 处理数组格式的JSON
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        if (element.isJsonObject()) {
                            loadSingleLibraryFromJson(element.getAsJsonObject());
                        }
                    }
                }
                
                LOG.info("成功从缓存加载 " + customLibraries.size() + " 个自定义组件库");
            } else {
                LOG.info("缓存文件不存在，跳过加载");
            }
        } catch (Exception e) {
            LOG.error("从缓存文件加载数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存数据到缓存文件
     * 
     * <p>该方法将内存中的自定义组件库数据序列化并保存到缓存文件，
     * 确保数据在系统重启后不丢失。</p>
     * 
     * <p>保存流程：</p>
     * <ol>
     *   <li>将内存中的组件库数据序列化为JSON</li>
     *   <li>使用UTF-8编码写入缓存文件</li>
     *   <li>确保数据持久化到磁盘</li>
     *   <li>记录保存操作的日志信息</li>
     * </ol>
     * 
     * <p>序列化策略：</p>
     * <ul>
     *   <li>使用Gson进行JSON序列化</li>
     *   <li>保存所有组件库配置信息</li>
     *   <li>保持数据的完整性和一致性</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>不会中断保存过程</li>
     *   <li>提供详细的错误信息</li>
     * </ul>
     * 
     * @see #loadFromCache()
     * @see com.google.gson.Gson#toJson(Object)
     */
    private static void saveToCache() {
        try {
            Gson gson = new Gson();
            // 将组件库配置列表序列化为JSON
            String jsonContent = gson.toJson(customLibraries.values());
            
            // 写入缓存文件
            Files.write(cacheFilePath, jsonContent.getBytes(StandardCharsets.UTF_8));
            LOG.info("成功保存数据到缓存文件: " + cacheFilePath);
        } catch (Exception e) {
            LOG.error("保存数据到缓存文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从JSON对象加载单个组件库
     * 
     * <p>该方法从JSON配置对象中解析并加载单个自定义组件库，
     * 包括基本配置信息和组件列表。</p>
     * 
     * <p>解析流程：</p>
     * <ol>
     *   <li>创建组件库配置对象</li>
     *   <li>解析基本信息字段</li>
     *   <li>解析组件列表</li>
     *   <li>添加到内存中</li>
     *   <li>构建组件映射表</li>
     * </ol>
     * 
     * <p>字段解析：</p>
     * <ul>
     *   <li>基本信息：名称、显示名称、版本、描述</li>
     *   <li>组件配置：前缀、文档URL模板</li>
     *   <li>组件列表：使用Gson反序列化</li>
     * </ul>
     * 
     * <p>数据验证：</p>
     * <ul>
     *   <li>验证组件名称不为null</li>
     *   <li>过滤无效的组件对象</li>
     *   <li>记录加载统计信息</li>
     * </ul>
     * 
     * @param configJson JSON配置对象，不能为null
     * 
     * @see #getStringValue(JsonObject, String, String)
     * @see com.google.gson.Gson#fromJson(JsonElement, Class)
     */
    private static void loadSingleLibraryFromJson(JsonObject configJson) {
        try {
            // 创建组件库配置对象
            CustomLibraryConfig config = new CustomLibraryConfig();
            
            // 解析基本信息
            config.setName(getStringValue(configJson, "name", ""));
            config.setDisplayName(getStringValue(configJson, "displayName", config.getName()));
            config.setVersion(getStringValue(configJson, "version", "1.0.0"));
            config.setDescription(getStringValue(configJson, "description", ""));
            config.setComponentPrefix(getStringValue(configJson, "componentPrefix", ""));
            config.setDocumentationUrlTemplate(getStringValue(configJson, "documentationUrlTemplate", ""));
            
            // 解析组件列表
            List<ElementPlusComponent> components = new ArrayList<>();
            if (configJson.has("components") && configJson.get("components").isJsonArray()) {
                JsonArray componentsArray = configJson.getAsJsonArray("components");
                Gson gson = new Gson();
                
                for (JsonElement componentElement : componentsArray) {
                    if (componentElement.isJsonObject()) {
                        ElementPlusComponent component = gson.fromJson(componentElement, ElementPlusComponent.class);
                        if (component != null && component.getName() != null) {
                            components.add(component);
                        }
                    }
                }
            }
            config.setComponents(components);
            
            // 添加到内存中
            customLibraries.put(config.getName(), config);
            
            // 构建组件映射表
            Map<String, ElementPlusComponent> componentMap = new HashMap<>();
            for (ElementPlusComponent component : components) {
                componentMap.put(component.getName(), component);
            }
            componentMaps.put(config.getName(), componentMap);
            
            LOG.info("从缓存加载组件库: " + config.getDisplayName() + " (" + components.size() + " 个组件)");
        } catch (Exception e) {
            LOG.error("从JSON加载组件库失败: " + e.getMessage(), e);
        }
    }
    
    // ==================== 公共API方法 ====================
    
    /**
     * 获取所有自定义组件库
     * 
     * <p>该方法返回当前已加载的所有自定义组件库配置信息，
     * 用于查询和管理自定义组件库。</p>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>返回所有已加载的自定义组件库配置</li>
     *   <li>如果没有任何自定义组件库，返回空列表</li>
     *   <li>返回的是配置对象的副本，不会影响原始数据</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>显示自定义组件库列表</li>
     *   <li>管理自定义组件库</li>
     *   <li>统计自定义组件库数量</li>
     *   <li>调试和监控目的</li>
     * </ul>
     * 
     * @return 自定义组件库配置列表，如果没有则返回空列表
     * 
     * @see CustomLibraryConfig
     */
    public static List<CustomLibraryConfig> getAllCustomLibraries() {
        return new ArrayList<>(customLibraries.values());
    }
    
    /**
     * 加载自定义组件库
     * 
     * <p>该方法解析JSON配置内容并加载自定义组件库，
     * 支持两种JSON格式的自动识别和处理。</p>
     * 
     * <p>支持的JSON格式：</p>
     * <ol>
     *   <li>单个组件库对象格式：直接加载单个组件库</li>
     *   <li>多个组件库数组格式：逐个加载每个组件库</li>
     * </ol>
     * 
     * <p>加载流程：</p>
     * <ul>
     *   <li>解析JSON内容并识别格式类型</li>
     *   <li>根据格式类型调用相应的加载方法</li>
     *   <li>验证每个组件库的加载结果</li>
     *   <li>返回整体加载状态</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录错误日志</li>
     *   <li>对于数组格式，单个失败不影响其他组件库</li>
     *   <li>提供详细的错误信息</li>
     * </ul>
     * 
     * @param jsonContent JSON配置内容，不能为null或空字符串
     * @return 如果所有组件库都加载成功则返回true，否则返回false
     * 
     * @see #loadSingleLibrary(JsonObject)
     * @see com.google.gson.JsonParser#parseString(String)
     */
    public static boolean loadCustomLibrary(String jsonContent) {
        try {
            LOG.info("=== 开始加载自定义组件库 ===");
            
            // 解析JSON配置
            JsonElement jsonElement = JsonParser.parseString(jsonContent);
            
            if (jsonElement.isJsonObject()) {
                // 对象格式：单个组件库配置
                return loadSingleLibrary(jsonElement.getAsJsonObject());
            } else if (jsonElement.isJsonArray()) {
                // 数组格式：多个组件库配置
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                boolean allSuccess = true;
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonElement element = jsonArray.get(i);
                    if (element.isJsonObject()) {
                        boolean success = loadSingleLibrary(element.getAsJsonObject());
                        if (!success) {
                            allSuccess = false;
                        }
                    } else {
                        LOG.error("数组元素 " + (i + 1) + " 不是有效的对象格式");
                        allSuccess = false;
                    }
                }
                return allSuccess;
            } else {
                LOG.error("JSON 格式错误：必须是对象或数组格式");
                return false;
            }
        } catch (Exception e) {
            LOG.error("加载自定义组件库失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 加载单个组件库
     * 
     * <p>该方法解析单个组件库的JSON配置并加载到内存中，
     * 包括配置验证、数据解析和缓存保存。</p>
     * 
     * <p>加载流程：</p>
     * <ol>
     *   <li>解析组件库配置信息</li>
     *   <li>解析组件列表</li>
     *   <li>验证配置的有效性</li>
     *   <li>添加到内存中</li>
     *   <li>构建组件映射表</li>
     *   <li>保存到缓存文件</li>
     * </ol>
     * 
     * <p>配置验证：</p>
     * <ul>
     *   <li>验证组件库名称不为空</li>
     *   <li>验证组件列表不为空</li>
     *   <li>过滤无效的组件对象</li>
     * </ul>
     * 
     * <p>数据管理：</p>
     * <ul>
     *   <li>更新内存中的组件库映射</li>
     *   <li>重建组件名称到组件的映射表</li>
     *   <li>自动保存到持久化缓存</li>
     * </ul>
     * 
     * @param configJson JSON配置对象，不能为null
     * @return 如果加载成功则返回true，否则返回false
     * 
     * @see #getStringValue(JsonObject, String, String)
     * @see #saveToCache()
     * @see com.google.gson.Gson#fromJson(JsonElement, Class)
     */
    private static boolean loadSingleLibrary(JsonObject configJson) {
        try {
            // 解析组件库配置
            CustomLibraryConfig config = new CustomLibraryConfig();
            config.setName(getStringValue(configJson, "name", ""));
            config.setDisplayName(getStringValue(configJson, "displayName", config.getName()));
            config.setVersion(getStringValue(configJson, "version", "1.0.0"));
            config.setDescription(getStringValue(configJson, "description", ""));
            config.setComponentPrefix(getStringValue(configJson, "componentPrefix", ""));
            config.setDocumentationUrlTemplate(getStringValue(configJson, "documentationUrlTemplate", ""));
            
            // 解析组件列表
            List<ElementPlusComponent> components = new ArrayList<>();
            if (configJson.has("components") && configJson.get("components").isJsonArray()) {
                JsonArray componentsArray = configJson.getAsJsonArray("components");
                Gson gson = new Gson();
                
                for (JsonElement componentElement : componentsArray) {
                    if (componentElement.isJsonObject()) {
                        ElementPlusComponent component = gson.fromJson(componentElement, ElementPlusComponent.class);
                        if (component != null && component.getName() != null) {
                            components.add(component);
                        }
                    }
                }
            }
            config.setComponents(components);
            
            // 验证配置
            if (config.getName().isEmpty()) {
                LOG.error("组件库名称不能为空");
                return false;
            }
            
            if (components.isEmpty()) {
                LOG.error("组件库 " + config.getName() + " 没有组件");
                return false;
            }
            
            // 添加到内存中
            customLibraries.put(config.getName(), config);
            
            // 构建组件映射表
            Map<String, ElementPlusComponent> componentMap = new HashMap<>();
            for (ElementPlusComponent component : components) {
                componentMap.put(component.getName(), component);
            }
            componentMaps.put(config.getName(), componentMap);
            
            // 保存到缓存文件
            saveToCache();
            
            LOG.info("成功加载自定义组件库: " + config.getDisplayName());
            LOG.info("组件数量: " + components.size());
            for (ElementPlusComponent component : components) {
                LOG.info("  - " + component.getName());
            }
            
            return true;
        } catch (Exception e) {
            LOG.error("解析组件库配置失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从文件加载自定义组件库
     * 
     * <p>该方法从指定的虚拟文件对象中读取JSON内容并加载自定义组件库，
     * 提供文件级别的组件库加载功能。</p>
     * 
     * <p>加载流程：</p>
     * <ol>
     *   <li>读取文件内容</li>
     *   <li>解析JSON内容</li>
     *   <li>调用 loadCustomLibrary 方法加载</li>
     *   <li>返回加载结果</li>
     * </ol>
     * 
     * <p>文件支持：</p>
     * <ul>
     *   <li>支持 IntelliJ IDEA 的 VirtualFile 对象</li>
     *   <li>自动处理文件编码（UTF-8）</li>
     *   <li>支持任意位置的JSON文件</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获文件读取异常</li>
     *   <li>记录详细的错误日志</li>
     *   <li>返回加载失败状态</li>
     * </ul>
     * 
     * @param file 组件库配置文件，不能为null
     * @return 如果加载成功则返回true，否则返回false
     * 
     * @see #readFileContent(VirtualFile)
     * @see #loadCustomLibrary(String)
     */
    public static boolean loadCustomLibraryFromFile(VirtualFile file) {
        try {
            String jsonContent = readFileContent(file);
            return loadCustomLibrary(jsonContent);
        } catch (Exception e) {
            LOG.error("从文件加载自定义组件库失败: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 检查是否为自定义组件
     * 
     * <p>该方法检查指定的组件名称是否存在于任何已加载的自定义组件库中，
     * 用于区分自定义组件和内置组件。</p>
     * 
     * <p>检查逻辑：</p>
     * <ul>
     *   <li>遍历所有自定义组件库的组件映射表</li>
     *   <li>检查组件名称是否存在于任何映射表中</li>
     *   <li>返回检查结果</li>
     * </ul>
     * 
     * <p>性能特点：</p>
     * <ul>
     *   <li>使用 HashMap 进行 O(1) 时间复杂度的查找</li>
     *   <li>支持快速组件类型识别</li>
     *   <li>适合频繁调用的场景</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>组件补全功能中的类型识别</li>
     *   <li>文档生成中的组件分类</li>
     *   <li>组件库管理功能</li>
     *   <li>调试和监控目的</li>
     * </ul>
     * 
     * @param componentName 要检查的组件名称，如果为null则返回false
     * @return 如果组件存在于任何自定义组件库中则返回true，否则返回false
     * 
     * @see #getCustomComponent(String)
     * @see #getCustomLibraryForComponent(String)
     */
    public static boolean isCustomComponent(String componentName) {
        if (componentName == null) {
            return false;
        }
        
        for (Map<String, ElementPlusComponent> componentMap : componentMaps.values()) {
            if (componentMap.containsKey(componentName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取组件所属的自定义组件库
     * 
     * <p>该方法根据组件名称查找该组件所属的自定义组件库配置，
     * 用于获取组件的详细库信息。</p>
     * 
     * <p>查找逻辑：</p>
     * <ol>
     *   <li>遍历所有自定义组件库的组件映射表</li>
     *   <li>查找包含指定组件名称的映射表</li>
     *   <li>返回对应的组件库配置对象</li>
     * </ol>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果找到组件：返回包含该组件的组件库配置</li>
     *   <li>如果未找到：返回null</li>
     *   <li>返回的配置对象包含完整的库信息</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>获取组件的库信息（名称、版本、描述等）</li>
     *   <li>生成组件的文档链接</li>
     *   <li>组件库管理功能</li>
     *   <li>调试和监控目的</li>
     * </ul>
     * 
     * @param componentName 要查找的组件名称，如果为null则返回null
     * @return 包含该组件的自定义组件库配置，如果不属于任何自定义库则返回null
     * 
     * @see CustomLibraryConfig
     * @see #isCustomComponent(String)
     * @see #getCustomComponent(String)
     */
    public static CustomLibraryConfig getCustomLibraryForComponent(String componentName) {
        if (componentName == null) {
            return null;
        }
        
        for (Map.Entry<String, Map<String, ElementPlusComponent>> entry : componentMaps.entrySet()) {
            if (entry.getValue().containsKey(componentName)) {
                return customLibraries.get(entry.getKey());
            }
        }
        return null;
    }
    
    /**
     * 获取自定义组件
     * 
     * <p>该方法根据组件名称从所有已加载的自定义组件库中查找并返回组件对象，
     * 提供快速的自定义组件访问功能。</p>
     * 
     * <p>查找逻辑：</p>
     * <ol>
     *   <li>遍历所有自定义组件库的组件映射表</li>
     *   <li>在每个映射表中查找指定组件名称</li>
     *   <li>返回找到的第一个组件对象</li>
     * </ol>
     * 
     * <p>性能特点：</p>
     * <ul>
     *   <li>使用 HashMap 进行 O(1) 时间复杂度的查找</li>
     *   <li>支持快速组件访问</li>
     *   <li>适合频繁调用的场景</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果找到组件：返回完整的组件对象</li>
     *   <li>如果未找到：返回null</li>
     *   <li>返回的组件对象包含所有属性、事件、插槽等信息</li>
     * </ul>
     * 
     * @param componentName 要查找的组件名称，如果为null则返回null
     * @return 找到的组件对象，如果不存在则返回null
     * 
     * @see com.chu7.vuecomponentassistant.completion2.ElementPlusComponent
     * @see #isCustomComponent(String)
     * @see #getCustomLibraryForComponent(String)
     */
    public static ElementPlusComponent getCustomComponent(String componentName) {
        if (componentName == null) {
            return null;
        }
        
        for (Map<String, ElementPlusComponent> componentMap : componentMaps.values()) {
            ElementPlusComponent component = componentMap.get(componentName);
            if (component != null) {
                return component;
            }
        }
        return null;
    }
    
    /**
     * 获取指定组件库的所有组件
     * 
     * <p>该方法根据组件库名称获取指定自定义组件库中的所有组件，
     * 用于查询特定组件库的组件列表。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>根据组件库名称查找对应的配置对象</li>
     *   <li>提取配置对象中的组件列表</li>
     *   <li>返回组件的副本列表</li>
     * </ol>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果找到组件库：返回该库的所有组件列表</li>
     *   <li>如果未找到组件库：返回空列表</li>
     *   <li>返回的是组件对象的副本，不会影响原始数据</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>显示特定组件库的组件列表</li>
     *   <li>组件库管理功能</li>
     *   <li>组件统计和分析</li>
     *   <li>调试和监控目的</li>
     * </ul>
     * 
     * @param libraryName 组件库名称，不能为null
     * @return 指定组件库的组件列表，如果组件库不存在则返回空列表
     * 
     * @see CustomLibraryConfig#getComponents()
     * @see #getAllCustomLibraries()
     */
    public static List<ElementPlusComponent> getCustomComponents(String libraryName) {
        CustomLibraryConfig config = customLibraries.get(libraryName);
        if (config != null) {
            return new ArrayList<>(config.getComponents());
        }
        return new ArrayList<>();
    }
    
    /**
     * 删除自定义组件库
     * 
     * <p>该方法根据组件库名称删除指定的自定义组件库，
     * 包括从内存中移除和更新持久化缓存。</p>
     * 
     * <p>删除流程：</p>
     * <ol>
     *   <li>检查组件库是否存在</li>
     *   <li>从内存映射表中移除组件库配置</li>
     *   <li>从组件映射表中移除对应的映射</li>
     *   <li>保存更新后的数据到缓存文件</li>
     *   <li>记录删除操作的日志信息</li>
     * </ol>
     * 
     * <p>数据一致性：</p>
     * <ul>
     *   <li>同时更新内存数据和持久化缓存</li>
     *   <li>确保数据的一致性</li>
     *   <li>删除后立即生效</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果组件库存在且删除成功：返回true</li>
     *   <li>如果组件库不存在：返回false</li>
     *   <li>删除操作不可逆</li>
     * </ul>
     * 
     * @param libraryName 要删除的组件库名称，不能为null
     * @return 如果删除成功则返回true，否则返回false
     * 
     * @see #saveToCache()
     * @see #clearAllCustomLibraries()
     */
    public static boolean removeCustomLibrary(String libraryName) {
        if (customLibraries.containsKey(libraryName)) {
            customLibraries.remove(libraryName);
            componentMaps.remove(libraryName);
            
            // 保存到缓存文件
            saveToCache();
            
            LOG.info("成功删除自定义组件库: " + libraryName);
            return true;
        }
        return false;
    }
    
    /**
     * 清空所有自定义组件库
     * 
     * <p>该方法清空所有已加载的自定义组件库，包括内存数据和持久化缓存，
     * 这是一个不可逆的操作，会删除所有自定义组件库数据。</p>
     * 
     * <p>清空流程：</p>
     * <ol>
     *   <li>清空内存中的组件库配置映射表</li>
     *   <li>清空组件名称到组件的映射表</li>
     *   <li>保存空数据到缓存文件</li>
     *   <li>记录清空操作的日志信息</li>
     * </ol>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>此操作会删除所有已加载的自定义组件库</li>
     *   <li>操作不可逆，请谨慎使用</li>
     *   <li>建议在执行前进行数据备份</li>
     *   <li>清空后需要重新加载组件库</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>系统重置和清理</li>
     *   <li>解决数据一致性问题</li>
     *   <li>测试和调试目的</li>
     *   <li>用户主动清理操作</li>
     * </ul>
     * 
     * @see #saveToCache()
     * @see #removeCustomLibrary(String)
     */
    public static void clearAllCustomLibraries() {
        customLibraries.clear();
        componentMaps.clear();
        
        // 保存到缓存文件
        saveToCache();
        
        LOG.info("已清空所有自定义组件库");
    }
    
    /**
     * 生成自定义组件文档URL
     * 
     * <p>该方法根据组件名称生成对应的自定义组件文档链接，
     * 使用组件所属组件库的文档URL模板。</p>
     * 
     * <p>生成流程：</p>
     * <ol>
     *   <li>查找组件所属的自定义组件库</li>
     *   <li>获取组件库的文档URL模板</li>
     *   <li>使用组件名称替换模板中的占位符</li>
     *   <li>返回生成的文档URL</li>
     * </ol>
     * 
     * <p>URL模板支持：</p>
     * <ul>
     *   <li>支持 %s 占位符，会被组件名称替换</li>
     *   <li>例如：模板 "https://example.com/docs/%s"</li>
     *   <li>组件 "MyComponent" 生成 "https://example.com/docs/MyComponent"</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果找到组件库且有模板：返回生成的文档URL</li>
     *   <li>如果没有配置模板：返回null</li>
     *   <li>如果组件不存在：返回null</li>
     * </ul>
     * 
     * @param componentName 要生成文档链接的组件名称，不能为null
     * @return 生成的文档URL，如果没有配置模板则返回null
     * 
     * @see #getCustomLibraryForComponent(String)
     * @see CustomLibraryConfig#getDocumentationUrlTemplate()
     */
    public static String generateCustomDocumentationUrl(String componentName) {
        CustomLibraryConfig config = getCustomLibraryForComponent(componentName);
        if (config != null && config.getDocumentationUrlTemplate() != null && !config.getDocumentationUrlTemplate().isEmpty()) {
            return config.getDocumentationUrlTemplate().replace("%s", componentName);
        }
        return null;
    }
    
    /**
     * 获取自定义组件库显示名称
     * 
     * <p>该方法根据组件名称获取该组件所属的自定义组件库的显示名称，
     * 用于在用户界面中显示友好的组件库名称。</p>
     * 
     * <p>获取流程：</p>
     * <ol>
     *   <li>查找组件所属的自定义组件库</li>
     *   <li>提取组件库的显示名称</li>
     *   <li>返回显示名称或默认值</li>
     * </ol>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *   <li>如果找到组件库：返回该库的显示名称</li>
     *   <li>如果未找到组件库：返回默认值"自定义组件库"</li>
     *   <li>显示名称通常比内部名称更友好</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>在用户界面中显示组件来源</li>
     *   <li>组件文档和帮助信息</li>
     *   <li>组件库管理界面</li>
     *   <li>调试和日志记录</li>
     * </ul>
     * 
     * @param componentName 要查询的组件名称，不能为null
     * @return 组件所属的自定义组件库显示名称，如果未找到则返回"自定义组件库"
     * 
     * @see #getCustomLibraryForComponent(String)
     * @see CustomLibraryConfig#getDisplayName()
     */
    public static String getCustomLibraryDisplayName(String componentName) {
        CustomLibraryConfig config = getCustomLibraryForComponent(componentName);
        if (config != null) {
            return config.getDisplayName();
        }
        return "自定义组件库";
    }
    
    // ==================== 验证方法 ====================
    
    /**
     * 验证自定义组件库配置
     * 
     * <p>该方法验证JSON配置内容的格式和内容是否符合自定义组件库的要求，
     * 提供配置验证功能，帮助用户发现配置问题。</p>
     * 
     * <p>验证内容：</p>
     * <ul>
     *   <li>JSON格式的正确性</li>
     *   <li>必需字段的存在性</li>
     *   <li>字段值的有效性</li>
     *   <li>组件列表的完整性</li>
     * </ul>
     * 
     * <p>支持的JSON格式：</p>
     * <ol>
     *   <li>单个组件库对象格式</li>
     *   <li>多个组件库数组格式</li>
     *   <li>自动识别格式类型</li>
     * </ol>
     * 
     * <p>验证结果：</p>
     * <ul>
     *   <li>错误信息：阻止配置加载的问题</li>
     *   <li>警告信息：建议修复但不阻止加载的问题</li>
     *   <li>验证通过：可以安全加载的配置</li>
     * </ul>
     * 
     * @param jsonContent JSON配置内容，不能为null或空字符串
     * @return 包含验证结果的ValidationResult对象
     * 
     * @see ValidationResult
     * @see #validateSingleLibraryConfig(JsonObject, ValidationResult)
     * @see com.google.gson.JsonParser#parseString(String)
     */
    public static ValidationResult validateCustomLibraryConfig(String jsonContent) {
        ValidationResult result = new ValidationResult();
        
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonContent);
            
            // 支持两种格式：对象格式和数组格式
            if (jsonElement.isJsonObject()) {
                // 对象格式：单个组件库配置
                validateSingleLibraryConfig(jsonElement.getAsJsonObject(), result);
            } else if (jsonElement.isJsonArray()) {
                // 数组格式：多个组件库配置
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if (jsonArray.size() == 0) {
                    result.addError("JSON 数组不能为空");
                } else {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement element = jsonArray.get(i);
                        if (element.isJsonObject()) {
                            result.addWarning("验证组件库 " + (i + 1) + ":");
                            validateSingleLibraryConfig(element.getAsJsonObject(), result);
                        } else {
                            result.addError("数组元素 " + (i + 1) + " 不是有效的对象格式");
                        }
                    }
                }
            } else {
                result.addError("JSON 格式错误：必须是对象或数组格式");
            }
        } catch (Exception e) {
            result.addError("JSON 解析失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证单个组件库配置
     * 
     * <p>该方法验证单个组件库配置对象的有效性和完整性，
     * 将验证结果添加到指定的ValidationResult对象中。</p>
     * 
     * <p>验证项目：</p>
     * <ul>
     *   <li>必需字段：名称、组件列表</li>
     *   <li>字段格式：组件列表必须是数组</li>
     *   <li>组件验证：每个组件必须有名称</li>
     *   <li>可选字段：前缀格式、描述信息</li>
     * </ul>
     * 
     * <p>验证规则：</p>
     * <ol>
     *   <li>组件库名称不能为空</li>
     *   <li>组件列表不能为空且必须是数组</li>
     *   <li>每个组件必须有名称</li>
     *   <li>组件前缀格式建议（如果提供）</li>
     *   <li>组件描述信息建议（如果提供）</li>
     * </ol>
     * 
     * <p>验证结果：</p>
     * <ul>
     *   <li>错误：阻止配置加载的严重问题</li>
     *   <li>警告：建议修复但不阻止加载的问题</li>
     *   <li>所有验证结果都会添加到result对象中</li>
     * </ul>
     * 
     * @param configJson 要验证的JSON配置对象，不能为null
     * @param result 验证结果对象，用于收集验证信息，不能为null
     * 
     * @see ValidationResult
     * @see #validateCustomLibraryConfig(String)
     */
    private static void validateSingleLibraryConfig(JsonObject configJson, ValidationResult result) {
        // 验证必需字段
        if (!configJson.has("name") || configJson.get("name").getAsString().trim().isEmpty()) {
            result.addError("组件库名称不能为空");
        }
        
        if (!configJson.has("components") || !configJson.get("components").isJsonArray()) {
            result.addError("组件列表不能为空且必须是数组格式");
        } else {
            JsonArray componentsArray = configJson.getAsJsonArray("components");
            if (componentsArray.size() == 0) {
                result.addError("组件列表不能为空");
            } else {
                // 验证每个组件
                for (int i = 0; i < componentsArray.size(); i++) {
                    JsonElement componentElement = componentsArray.get(i);
                    if (!componentElement.isJsonObject()) {
                        result.addError("组件 " + (i + 1) + " 必须是对象格式");
                        continue;
                    }
                    
                    JsonObject componentObj = componentElement.getAsJsonObject();
                    if (!componentObj.has("name") || componentObj.get("name").getAsString().trim().isEmpty()) {
                        result.addError("组件 " + (i + 1) + " 的名称不能为空");
                    }
                    
                    if (!componentObj.has("description")) {
                        result.addWarning("组件 " + (i + 1) + " 缺少描述信息");
                    }
                }
            }
        }
        
        // 验证可选字段
        if (configJson.has("componentPrefix") && !configJson.get("componentPrefix").getAsString().trim().isEmpty()) {
            String prefix = configJson.get("componentPrefix").getAsString();
            if (!prefix.matches("[a-zA-Z][a-zA-Z0-9-]*")) {
                result.addWarning("组件前缀格式不正确，建议使用字母开头的字母数字组合");
            }
        }
    }
    
    /**
     * 验证结果类
     * 
     * <p>用于存储验证过程中的错误和警告信息，提供验证结果的统一管理。</p>
     * 
     * <p>功能特点：</p>
     * <ul>
     *   <li>分别收集错误和警告信息</li>
     *   <li>提供验证状态的快速检查</li>
     *   <li>支持信息的批量添加和查询</li>
     *   <li>提供用户友好的错误信息格式</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>配置验证结果收集</li>
     *   <li>用户界面错误显示</li>
     *   <li>日志记录和调试</li>
     *   <li>配置问题诊断</li>
     * </ul>
     * 
     * @author VueKit Team
     * @version 1.0.0
     * @since 1.0.0
     */
    public static class ValidationResult {
        /** 错误信息列表 */
        private final List<String> errors = new ArrayList<>();
        
        /** 警告信息列表 */
        private final List<String> warnings = new ArrayList<>();
        
        /**
         * 添加错误信息
         * @param error 错误信息
         */
        public void addError(String error) {
            errors.add(error);
        }
        
        /**
         * 添加警告信息
         * @param warning 警告信息
         */
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        /**
         * 检查是否验证通过
         * @return 如果没有错误则返回true
         */
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        /**
         * 获取错误信息列表
         * @return 错误信息列表
         */
        public List<String> getErrors() {
            return errors;
        }
        
        /**
         * 获取警告信息列表
         * @return 警告信息列表
         */
        public List<String> getWarnings() {
            return warnings;
        }
        
        /**
         * 获取错误信息字符串
         * @return 错误信息字符串，如果没有错误则返回"验证通过"
         */
        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "验证通过";
            }
            return String.join("; ", errors);
        }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 读取文件内容
     * 
     * <p>该方法从IntelliJ IDEA的虚拟文件对象中读取文件内容，
     * 支持任意类型的文件读取，自动处理编码。</p>
     * 
     * <p>读取流程：</p>
     * <ol>
     *   <li>获取文件的输入流</li>
     *   <li>读取所有字节数据</li>
     *   <li>使用UTF-8编码转换为字符串</li>
     *   <li>自动关闭输入流</li>
     * </ol>
     * 
     * <p>文件支持：</p>
     * <ul>
     *   <li>支持IntelliJ IDEA的VirtualFile对象</li>
     *   <li>自动处理文件编码（UTF-8）</li>
     *   <li>支持任意大小的文件</li>
     *   <li>使用try-with-resources确保资源释放</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获IOException并包装为RuntimeException</li>
     *   <li>提供详细的错误信息</li>
     *   <li>确保输入流正确关闭</li>
     * </ul>
     * 
     * @param file 要读取的虚拟文件对象，不能为null
     * @return 文件内容的字符串表示
     * @throws RuntimeException 如果文件读取失败
     * 
     * @see com.intellij.openapi.vfs.VirtualFile#getInputStream()
     */
    private static String readFileContent(VirtualFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取JSON字符串值
     * 
     * <p>该方法从JSON对象中安全地获取字符串值，提供默认值支持和空值检查，
     * 避免在解析JSON时出现异常。</p>
     * 
     * <p>获取逻辑：</p>
     * <ol>
     *   <li>检查JSON对象是否包含指定的键</li>
     *   <li>检查键值是否为null</li>
     *   <li>如果键存在且值不为null，返回字符串值</li>
     *   <li>否则返回指定的默认值</li>
     * </ol>
     * 
     * <p>安全特性：</p>
     * <ul>
     *   <li>自动处理缺失的键</li>
     *   <li>自动处理null值</li>
     *   <li>提供默认值支持</li>
     *   <li>避免NullPointerException</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>JSON配置解析</li>
     *   <li>可选字段处理</li>
     *   <li>配置默认值设置</li>
     *   <li>数据验证和清理</li>
     * </ul>
     * 
     * @param json JSON对象，不能为null
     * @param key 要获取的键名，不能为null
     * @param defaultValue 如果键不存在或值为null时返回的默认值
     * @return 字符串值，如果键不存在或值为null则返回defaultValue
     * 
     * @see com.google.gson.JsonObject#has(String)
     * @see com.google.gson.JsonObject#get(String)
     * @see com.google.gson.JsonElement#isJsonNull()
     */
    private static String getStringValue(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }
    
    /**
     * 打印自定义组件库信息
     * 
     * <p>该方法用于调试和日志记录，输出所有已加载的自定义组件库的详细信息，
     * 包括组件库配置和组件列表。</p>
     * 
     * <p>输出内容：</p>
     * <ul>
     *   <li>组件库总数统计</li>
     *   <li>每个组件库的基本信息</li>
     *   <li>组件库的组件数量</li>
     *   <li>每个组件的名称列表</li>
     * </ul>
     * 
     * <p>输出格式：</p>
     * <ul>
     *   <li>使用分隔线清晰区分不同部分</li>
     *   <li>层次化显示组件库和组件信息</li>
     *   <li>使用缩进提高可读性</li>
     *   <li>记录到日志系统中</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>调试和问题诊断</li>
     *   <li>系统状态监控</li>
     *   <li>用户配置验证</li>
     *   <li>开发测试目的</li>
     * </ul>
     * 
     * @see #getAllCustomLibraries()
     * @see CustomLibraryConfig#getDisplayName()
     * @see CustomLibraryConfig#getComponents()
     */
    public static void printCustomLibrariesInfo() {
        LOG.info("=== 自定义组件库信息 ===");
        List<CustomLibraryConfig> libraries = getAllCustomLibraries();
        LOG.info("组件库数量: " + libraries.size());
        
        for (CustomLibraryConfig config : libraries) {
            LOG.info("组件库: " + config.getDisplayName() + " (" + config.getName() + ")");
            LOG.info("  前缀: " + config.getComponentPrefix());
            LOG.info("  组件数量: " + config.getComponents().size());
            for (ElementPlusComponent component : config.getComponents()) {
                LOG.info("    - " + component.getName());
            }
        }
        LOG.info("========================");
    }
}
