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
 * @author Vue Component Assistant Team
 * @version 1.0.0
 */
public class CustomComponentLibraryManager {
    
    private static final Logger LOG = Logger.getInstance(CustomComponentLibraryManager.class);
    
    /**
     * 自定义组件库配置类
     */
    public static class CustomLibraryConfig {
        private String name;                    // 组件库名称
        private String displayName;            // 显示名称
        private String version;                // 版本
        private String description;            // 描述
        private String componentPrefix;        // 组件前缀（如 el-、a-、custom-）
        private String documentationUrlTemplate; // 文档URL模板
        private List<ElementPlusComponent> components; // 组件列表
        
        // 默认构造函数，用于序列化
        public CustomLibraryConfig() {
            this.components = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getComponentPrefix() { return componentPrefix; }
        public void setComponentPrefix(String componentPrefix) { this.componentPrefix = componentPrefix; }
        
        public String getDocumentationUrlTemplate() { return documentationUrlTemplate; }
        public void setDocumentationUrlTemplate(String documentationUrlTemplate) { this.documentationUrlTemplate = documentationUrlTemplate; }
        
        public List<ElementPlusComponent> getComponents() { 
            if (components == null) {
                components = new ArrayList<>();
            }
            return components; 
        }
        public void setComponents(List<ElementPlusComponent> components) { this.components = components; }
    }
    
    // 内存数据
    private static Map<String, CustomLibraryConfig> customLibraries = new HashMap<>();
    private static Map<String, Map<String, ElementPlusComponent>> componentMaps = new HashMap<>();
    
    // 缓存文件路径
    private static final String CACHE_FILE_NAME = "custom_component_libraries.json";
    private static Path cacheFilePath;
    
    static {
        // 初始化缓存文件路径
        try {
            String userHome = System.getProperty("user.home");
            Path cacheDir = Paths.get(userHome, ".intellij_idea_system", "vue-component-assistant");
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }
            cacheFilePath = cacheDir.resolve(CACHE_FILE_NAME);
            System.out.println("自定义组件库缓存文件路径: " + cacheFilePath);
            
            // 启动时加载缓存数据
            loadFromCache();
        } catch (Exception e) {
            System.err.println("初始化缓存目录失败: " + e.getMessage());
        }
    }
    
    /**
     * 从缓存文件加载数据
     */
    private static void loadFromCache() {
        try {
            if (Files.exists(cacheFilePath)) {
                String jsonContent = new String(Files.readAllBytes(cacheFilePath), StandardCharsets.UTF_8);
                System.out.println("从缓存文件加载数据: " + jsonContent);
                
                JsonElement jsonElement = JsonParser.parseString(jsonContent);
                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        if (element.isJsonObject()) {
                            loadSingleLibraryFromJson(element.getAsJsonObject());
                        }
                    }
                }
                
                System.out.println("成功从缓存加载 " + customLibraries.size() + " 个自定义组件库");
            }
        } catch (Exception e) {
            System.err.println("从缓存文件加载数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存数据到缓存文件
     */
    private static void saveToCache() {
        try {
            Gson gson = new Gson();
            String jsonContent = gson.toJson(customLibraries.values());
            Files.write(cacheFilePath, jsonContent.getBytes(StandardCharsets.UTF_8));
            System.out.println("成功保存数据到缓存文件: " + cacheFilePath);
        } catch (Exception e) {
            System.err.println("保存数据到缓存文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 从JSON对象加载单个组件库
     */
    private static void loadSingleLibraryFromJson(JsonObject configJson) {
        try {
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
            
            // 添加到内存中
            customLibraries.put(config.getName(), config);
            
            // 构建组件映射
            Map<String, ElementPlusComponent> componentMap = new HashMap<>();
            for (ElementPlusComponent component : components) {
                componentMap.put(component.getName(), component);
            }
            componentMaps.put(config.getName(), componentMap);
            
            System.out.println("从缓存加载组件库: " + config.getDisplayName() + " (" + components.size() + " 个组件)");
        } catch (Exception e) {
            System.err.println("从JSON加载组件库失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有自定义组件库
     */
    public static List<CustomLibraryConfig> getAllCustomLibraries() {
        return new ArrayList<>(customLibraries.values());
    }
    
    /**
     * 加载自定义组件库
     * 
     * @param jsonContent JSON配置内容
     * @return 是否加载成功
     */
    public static boolean loadCustomLibrary(String jsonContent) {
        try {
            System.out.println("=== 开始加载自定义组件库 ===");
            
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
                        System.err.println("数组元素 " + (i + 1) + " 不是有效的对象格式");
                        allSuccess = false;
                    }
                }
                return allSuccess;
            } else {
                System.err.println("JSON 格式错误：必须是对象或数组格式");
                return false;
            }
        } catch (Exception e) {
            System.err.println("加载自定义组件库失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 加载单个组件库
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
                System.err.println("组件库名称不能为空");
                return false;
            }
            
            if (components.isEmpty()) {
                System.err.println("组件库 " + config.getName() + " 没有组件");
                return false;
            }
            
            // 添加到内存中
            customLibraries.put(config.getName(), config);
            
            // 构建组件映射
            Map<String, ElementPlusComponent> componentMap = new HashMap<>();
            for (ElementPlusComponent component : components) {
                componentMap.put(component.getName(), component);
            }
            componentMaps.put(config.getName(), componentMap);
            
            // 保存到缓存文件
            saveToCache();
            
            System.out.println("成功加载自定义组件库: " + config.getDisplayName());
            System.out.println("组件数量: " + components.size());
            for (ElementPlusComponent component : components) {
                System.out.println("  - " + component.getName());
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("解析组件库配置失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从文件加载自定义组件库
     */
    public static boolean loadCustomLibraryFromFile(VirtualFile file) {
        try {
            String jsonContent = readFileContent(file);
            return loadCustomLibrary(jsonContent);
        } catch (Exception e) {
            System.err.println("从文件加载自定义组件库失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 检查是否为自定义组件
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
     */
    public static boolean removeCustomLibrary(String libraryName) {
        if (customLibraries.containsKey(libraryName)) {
            customLibraries.remove(libraryName);
            componentMaps.remove(libraryName);
            
            // 保存到缓存文件
            saveToCache();
            
            return true;
        }
        return false;
    }
    
    /**
     * 清空所有自定义组件库
     */
    public static void clearAllCustomLibraries() {
        customLibraries.clear();
        componentMaps.clear();
        
        // 保存到缓存文件
        saveToCache();
    }
    
    /**
     * 生成自定义组件文档URL
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
     */
    public static String getCustomLibraryDisplayName(String componentName) {
        CustomLibraryConfig config = getCustomLibraryForComponent(componentName);
        if (config != null) {
            return config.getDisplayName();
        }
        return "自定义组件库";
    }
    
    /**
     * 验证自定义组件库配置
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
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public String getErrorMessage() {
            if (errors.isEmpty()) {
                return "验证通过";
            }
            return String.join("; ", errors);
        }
    }
    
    /**
     * 读取文件内容
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
     */
    private static String getStringValue(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }
    
    /**
     * 打印自定义组件库信息
     */
    public static void printCustomLibrariesInfo() {
        System.out.println("=== 自定义组件库信息 ===");
        List<CustomLibraryConfig> libraries = getAllCustomLibraries();
        System.out.println("组件库数量: " + libraries.size());
        
        for (CustomLibraryConfig config : libraries) {
            System.out.println("组件库: " + config.getDisplayName() + " (" + config.getName() + ")");
            System.out.println("  前缀: " + config.getComponentPrefix());
            System.out.println("  组件数量: " + config.getComponents().size());
            for (ElementPlusComponent component : config.getComponents()) {
                System.out.println("    - " + component.getName());
            }
        }
        System.out.println("========================");
    }
}
