package com.chu7.vuecomponentassistant.validation;

import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.exceptions.JsonParseException;
import com.chu7.vuecomponentassistant.notification.VueKitNotificationManager;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 数据验证器
 * 
 * 提供严格的数据验证功能，确保组件数据的完整性和安全性：
 * - JSON数据结构验证
 * - 组件数据字段验证
 * - 恶意数据检测
 * - 数据完整性检查
 * - 输入数据清理
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class DataValidator {
    
    private static final Logger LOG = VueKitLogger.getLogger(DataValidator.class);
    
    // 单例实例
    private static volatile DataValidator instance;
    
    // 验证规则
    private static final int MAX_STRING_LENGTH = 10000;
    private static final int MAX_ARRAY_SIZE = 1000;
    private static final int MAX_COMPONENT_COUNT = 500;
    
    // 安全模式
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    
    // 允许的字段名模式
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9-_]*$");
    
    private DataValidator() {
        VueKitLogger.debug(LOG, "数据验证器初始化完成");
    }
    
    /**
     * 获取单例实例
     */
    public static DataValidator getInstance() {
        if (instance == null) {
            synchronized (DataValidator.class) {
                if (instance == null) {
                    instance = new DataValidator();
                }
            }
        }
        return instance;
    }
    
    /**
     * 验证JSON字符串
     * 
     * @param jsonContent JSON内容
     * @param dataType 数据类型描述
     * @return 验证结果
     * @throws JsonParseException JSON解析异常
     */
    @NotNull
    public ValidationResult validateJson(@NotNull String jsonContent, @NotNull String dataType) throws JsonParseException {
        VueKitLogger.debug(LOG, "开始验证JSON数据，类型: " + dataType);
        
        ValidationResult result = new ValidationResult();
        
        try {
            // 基本长度检查
            if (jsonContent.length() > MAX_STRING_LENGTH * 10) { // JSON可以更大
                result.addError("JSON内容过长: " + jsonContent.length() + " 字符，最大允许: " + (MAX_STRING_LENGTH * 10));
                return result;
            }
            
            // 恶意内容检查
            validateSecurity(jsonContent, result);
            
            // JSON格式检查
            if (!isValidJsonFormat(jsonContent)) {
                result.addError("JSON格式无效");
                return result;
            }
            
            VueKitLogger.debug(LOG, "JSON数据验证通过");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "JSON验证失败: " + dataType, e);
            throw new JsonParseException("JSON验证失败: " + e.getMessage(), jsonContent, e);
        }
        
        return result;
    }
    
    /**
     * 验证组件列表
     * 
     * @param components 组件列表
     * @param project 项目（用于通知）
     * @return 验证结果
     */
    @NotNull
    public ValidationResult validateComponents(@NotNull List<ComponentInfo> components, @Nullable Project project) {
        VueKitLogger.debug(LOG, "开始验证组件列表，数量: " + components.size());
        
        ValidationResult result = new ValidationResult();
        
        try {
            // 检查组件数量
            if (components.size() > MAX_COMPONENT_COUNT) {
                result.addWarning("组件数量过多: " + components.size() + "，可能影响性能");
            }
            
            // 验证每个组件
            int validComponents = 0;
            for (int i = 0; i < components.size(); i++) {
                ComponentInfo component = components.get(i);
                
                ValidationResult componentResult = validateComponent(component, i);
                result.merge(componentResult);
                
                if (componentResult.isValid()) {
                    validComponents++;
                }
            }
            
            VueKitLogger.info(LOG, String.format("组件验证完成: %d/%d 有效", validComponents, components.size()));
            
            // 如果有严重错误，发送通知
            if (result.hasErrors() && project != null) {
                VueKitNotificationManager.getInstance().showDataValidationError(
                    project, 
                    "组件数据", 
                    "发现 " + result.getErrors().size() + " 个数据错误"
                );
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "组件列表验证失败", e);
            result.addError("组件列表验证异常: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证单个组件
     * 
     * @param component 组件
     * @param index 组件索引
     * @return 验证结果
     */
    @NotNull
    public ValidationResult validateComponent(@NotNull ComponentInfo component, int index) {
        ValidationResult result = new ValidationResult();
        String componentPrefix = "组件[" + index + "]";
        
        try {
            // 验证组件名称
            String name = component.getName();
            if (name == null || name.trim().isEmpty()) {
                result.addError(componentPrefix + " 名称为空");
            } else if (name.length() > 100) {
                result.addError(componentPrefix + " 名称过长: " + name.length());
            } else if (!VALID_NAME_PATTERN.matcher(name).matches()) {
                result.addError(componentPrefix + " 名称格式无效: " + name);
            }
            
            // 验证描述
            String description = component.getDescription();
            if (description != null && description.length() > MAX_STRING_LENGTH) {
                result.addWarning(componentPrefix + " 描述过长: " + description.length());
            }
            
            // 安全检查
            if (description != null) {
                validateSecurity(description, result, componentPrefix + " 描述");
            }
            
            // 验证属性
            if (component.getProps() != null) {
                for (int i = 0; i < component.getProps().size(); i++) {
                    ValidationResult propResult = validateProperty(component.getProps().get(i), componentPrefix + ".属性[" + i + "]");
                    result.merge(propResult);
                }
            }
            
            // 验证事件
            if (component.getEvents() != null) {
                for (int i = 0; i < component.getEvents().size(); i++) {
                    ValidationResult eventResult = validateEvent(component.getEvents().get(i), componentPrefix + ".事件[" + i + "]");
                    result.merge(eventResult);
                }
            }
            
            // 验证插槽
            if (component.getSlots() != null) {
                for (int i = 0; i < component.getSlots().size(); i++) {
                    ValidationResult slotResult = validateSlot(component.getSlots().get(i), componentPrefix + ".插槽[" + i + "]");
                    result.merge(slotResult);
                }
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "组件验证失败: " + componentPrefix, e);
            result.addError(componentPrefix + " 验证异常: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证属性
     */
    @NotNull
    private ValidationResult validateProperty(@NotNull ComponentInfo.ComponentProp prop, @NotNull String prefix) {
        ValidationResult result = new ValidationResult();
        
        // 验证属性名称
        String name = prop.getName();
        if (name == null || name.trim().isEmpty()) {
            result.addError(prefix + " 名称为空");
        } else if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            result.addError(prefix + " 名称格式无效: " + name);
        }
        
        // 验证类型
        String type = prop.getType();
        if (type != null && type.length() > 200) {
            result.addWarning(prefix + " 类型描述过长");
        }
        
        // 验证描述
        String description = prop.getDescription();
        if (description != null) {
            if (description.length() > MAX_STRING_LENGTH) {
                result.addWarning(prefix + " 描述过长");
            }
            validateSecurity(description, result, prefix + " 描述");
        }
        
        return result;
    }
    
    /**
     * 验证事件
     */
    @NotNull
    private ValidationResult validateEvent(@NotNull ComponentInfo.ComponentEvent event, @NotNull String prefix) {
        ValidationResult result = new ValidationResult();
        
        // 验证事件名称
        String name = event.getName();
        if (name == null || name.trim().isEmpty()) {
            result.addError(prefix + " 名称为空");
        } else if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            result.addError(prefix + " 名称格式无效: " + name);
        }
        
        // 验证描述
        String description = event.getDescription();
        if (description != null) {
            if (description.length() > MAX_STRING_LENGTH) {
                result.addWarning(prefix + " 描述过长");
            }
            validateSecurity(description, result, prefix + " 描述");
        }
        
        return result;
    }
    
    /**
     * 验证插槽
     */
    @NotNull
    private ValidationResult validateSlot(@NotNull ComponentInfo.ComponentSlot slot, @NotNull String prefix) {
        ValidationResult result = new ValidationResult();
        
        // 验证插槽名称
        String name = slot.getName();
        if (name == null || name.trim().isEmpty()) {
            result.addError(prefix + " 名称为空");
        } else if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            result.addError(prefix + " 名称格式无效: " + name);
        }
        
        // 验证描述
        String description = slot.getDescription();
        if (description != null) {
            if (description.length() > MAX_STRING_LENGTH) {
                result.addWarning(prefix + " 描述过长");
            }
            validateSecurity(description, result, prefix + " 描述");
        }
        
        return result;
    }
    
    /**
     * 安全验证
     */
    private void validateSecurity(@NotNull String content, @NotNull ValidationResult result) {
        validateSecurity(content, result, "内容");
    }
    
    /**
     * 安全验证（带前缀）
     */
    private void validateSecurity(@NotNull String content, @NotNull ValidationResult result, @NotNull String prefix) {
        // 检查脚本标签
        if (SCRIPT_PATTERN.matcher(content).find()) {
            result.addError(prefix + " 包含潜在危险的脚本标签");
        }
        
        // 检查JavaScript协议
        if (JAVASCRIPT_PATTERN.matcher(content).find()) {
            result.addError(prefix + " 包含JavaScript协议，存在安全风险");
        }
        
        // 检查过多的HTML标签（可能是XSS攻击）
        long htmlTagCount = HTML_PATTERN.matcher(content).results().count();
        if (htmlTagCount > 50) {
            result.addWarning(prefix + " 包含过多HTML标签: " + htmlTagCount);
        }
        
        // 检查异常长度的单行
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.length() > MAX_STRING_LENGTH) {
                result.addWarning(prefix + " 包含异常长的行: " + line.length() + " 字符");
                break;
            }
        }
    }
    
    /**
     * 检查JSON格式
     */
    private boolean isValidJsonFormat(@NotNull String jsonContent) {
        try {
            // 简单的JSON格式检查
            String trimmed = jsonContent.trim();
            
            // 必须以 { 或 [ 开始
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                return false;
            }
            
            // 必须以 } 或 ] 结束
            if (!trimmed.endsWith("}") && !trimmed.endsWith("]")) {
                return false;
            }
            
            // 检查括号匹配
            int braceCount = 0;
            int bracketCount = 0;
            boolean inString = false;
            boolean escaped = false;
            
            for (char c : trimmed.toCharArray()) {
                if (escaped) {
                    escaped = false;
                    continue;
                }
                
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                
                if (c == '"') {
                    inString = !inString;
                    continue;
                }
                
                if (inString) {
                    continue;
                }
                
                switch (c) {
                    case '{':
                        braceCount++;
                        break;
                    case '}':
                        braceCount--;
                        break;
                    case '[':
                        bracketCount++;
                        break;
                    case ']':
                        bracketCount--;
                        break;
                }
            }
            
            return braceCount == 0 && bracketCount == 0;
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "JSON格式检查异常", e);
            return false;
        }
    }
    
    /**
     * 清理输入字符串
     */
    @NotNull
    public String sanitizeInput(@Nullable String input) {
        if (input == null) {
            return "";
        }
        
        // 移除潜在危险字符
        String sanitized = input
            .replaceAll("<script[^>]*>.*?</script>", "") // 移除script标签
            .replaceAll("javascript:", "") // 移除javascript协议
            .replaceAll("[\u0000-\u001f\u007f-\u009f]", ""); // 移除控制字符
        
        // 限制长度
        if (sanitized.length() > MAX_STRING_LENGTH) {
            sanitized = sanitized.substring(0, MAX_STRING_LENGTH);
            VueKitLogger.debug(LOG, "输入字符串被截断到最大长度: " + MAX_STRING_LENGTH);
        }
        
        return sanitized;
    }
}