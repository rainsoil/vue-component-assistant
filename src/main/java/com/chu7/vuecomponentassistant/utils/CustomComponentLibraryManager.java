package com.chu7.vuecomponentassistant.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;

import java.io.*;
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
 * - 缓存文件位置：用户主目录/.intellij_idea_system/vue-component-assistant/custom_component_libraries.json
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
 * @author Vue Component Assistant Team
 * @version 2.0.0
 * @since 1.0.0
 */
public class CustomComponentLibraryManager {
    
    /** 日志记录器 */
    private static final Logger LOG = Logger.getInstance(CustomComponentLibraryManager.class);
    
    /**
     * 自定义组件库配置类
     * 
     * 用于存储单个自定义组件库的完整配置信息，
     * 包括基本信息、组件列表等。
     * 
     * @author Vue Component Assistant Team
     * @version 1.0.0
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
     * 功能：
     * 1. 创建缓存目录
     * 2. 设置缓存文件路径
     * 3. 启动时加载缓存数据
     */
    private static void initializeCache() {
        try {
            // 获取用户主目录
            String userHome = System.getProperty("user.home");
            
            // 创建缓存目录：用户主目录/.intellij_idea_system/vue-component-assistant
            Path cacheDir = Paths.get(userHome, ".intellij_idea_system", "vue-component-assistant");
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
     * 功能：
     * 1. 检查缓存文件是否存在
     * 2. 读取JSON内容
     * 3. 解析并加载到内存中
     * 4. 重建组件映射表
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
     * 功能：
     * 1. 将内存中的组件库数据序列化为JSON
     * 2. 写入到缓存文件
     * 3. 确保数据持久化
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
     * @param configJson JSON配置对象
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
     * @return 自定义组件库配置列表
     */
    public static List<CustomLibraryConfig> getAllCustomLibraries() {
        return new ArrayList<>(customLibraries.values());
    }
    
    /**
     * 加载自定义组件库
     * 
     * 支持两种JSON格式：
     * 1. 单个组件库对象格式
     * 2. 多个组件库数组格式
     * 
     * @param jsonContent JSON配置内容
     * @return 是否加载成功
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
     * @param configJson JSON配置对象
     * @return 是否加载成功
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
     * @param file 组件库配置文件
     * @return 是否加载成功
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
     * @param componentName 组件名称
     * @return 是否为自定义组件
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
     * @param componentName 组件名称
     * @return 自定义组件库配置，如果不属于任何自定义库则返回null
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
     * @param componentName 组件名称
     * @return 组件对象，如果不存在则返回null
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
     * @param libraryName 组件库名称
     * @return 组件列表
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
     * @param libraryName 组件库名称
     * @return 是否删除成功
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
     * 注意：此操作会删除所有已加载的自定义组件库
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
     * @param componentName 组件名称
     * @return 文档URL，如果没有配置模板则返回null
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
     * @param componentName 组件名称
     * @return 组件库显示名称
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
     * @param jsonContent JSON配置内容
     * @return 验证结果
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
     * @param configJson JSON配置对象
     * @param result 验证结果对象
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
     * 用于存储验证过程中的错误和警告信息
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
     * @param file 虚拟文件对象
     * @return 文件内容字符串
     * @throws RuntimeException 如果读取失败
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
     * @param json JSON对象
     * @param key 键名
     * @param defaultValue 默认值
     * @return 字符串值
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
     * 用于调试和日志记录，输出所有已加载的自定义组件库信息
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
