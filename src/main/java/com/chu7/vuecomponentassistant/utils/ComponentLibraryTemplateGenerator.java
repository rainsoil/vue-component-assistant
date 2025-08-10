package com.chu7.vuecomponentassistant.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 组件库模板生成器
 * 
 * 功能说明：
 * - 生成标准的组件库JSON模板
 * - 支持多种组件类型示例
 * - 提供完整的属性、事件、插槽定义
 * - 符合VueKit远程组件库规范
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentLibraryTemplateGenerator {
    
    private static final Logger LOG = Logger.getInstance(ComponentLibraryTemplateGenerator.class);
    
    /**
     * 生成并导出组件库模板
     */
    public static void generateAndExportTemplate(File outputFile) throws IOException {
        Map<String, Object> template = createTemplateData();
        
        // 使用Gson格式化输出
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
        
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            gson.toJson(template, writer);
        }
        
        LOG.info("组件库模板已导出到: " + outputFile.getAbsolutePath());
    }
    
    /**
     * 创建模板数据
     */
    private static Map<String, Object> createTemplateData() {
        Map<String, Object> template = new LinkedHashMap<>();
        
        // 组件库基本信息
        template.put("id", "my-component-library");
        template.put("name", "my-component-library");
        template.put("displayName", "我的组件库");
        template.put("description", "这是一个自定义组件库的示例，您可以参考此模板创建自己的组件库");
        template.put("version", "1.0.0");
        template.put("source", "CUSTOM_LOCAL");
        template.put("sourceUrl", "");
        template.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 组件列表
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createButtonComponent());
        components.add(createInputComponent());
        components.add(createCardComponent());
        components.add(createModalComponent());
        
        template.put("components", components);
        
        return template;
    }
    
    /**
     * 创建按钮组件示例
     */
    private static Map<String, Object> createButtonComponent() {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("name", "my-button");
        component.put("displayName", "自定义按钮");
        component.put("description", "一个功能丰富的自定义按钮组件，支持多种类型和状态");
        component.put("tag", "my-button");
        component.put("documentation", "这是一个自定义按钮组件，支持多种类型和状态。包括主要按钮、成功按钮、警告按钮、危险按钮等。");
        
        // 属性
        List<Map<String, Object>> props = new ArrayList<>();
        props.add(createProp("type", "string", "按钮类型", "default", false, 
            Arrays.asList("primary", "success", "warning", "danger", "info", "default")));
        props.add(createProp("size", "string", "按钮尺寸", "medium", false, 
            Arrays.asList("large", "medium", "small", "mini")));
        props.add(createProp("disabled", "boolean", "是否禁用按钮", "false", false, null));
        props.add(createProp("loading", "boolean", "是否显示加载状态", "false", false, null));
        props.add(createProp("round", "boolean", "是否圆角按钮", "false", false, null));
        props.add(createProp("plain", "boolean", "是否朴素按钮", "false", false, null));
        component.put("props", props);
        
        // 事件
        List<Map<String, Object>> events = new ArrayList<>();
        events.add(createEvent("click", "点击按钮时触发", "event"));
        events.add(createEvent("focus", "按钮获得焦点时触发", "event"));
        events.add(createEvent("blur", "按钮失去焦点时触发", "event"));
        component.put("events", events);
        
        // 插槽
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(createSlot("default", "按钮内容", ""));
        slots.add(createSlot("icon", "按钮图标", ""));
        component.put("slots", slots);
        
        return component;
    }
    
    /**
     * 创建输入框组件示例
     */
    private static Map<String, Object> createInputComponent() {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("name", "my-input");
        component.put("displayName", "自定义输入框");
        component.put("description", "一个功能丰富的自定义输入框组件，支持多种类型和验证");
        component.put("tag", "my-input");
        component.put("documentation", "这是一个自定义输入框组件，支持多种类型和验证。包括文本输入、密码输入、数字输入等。");
        
        // 属性
        List<Map<String, Object>> props = new ArrayList<>();
        props.add(createProp("value", "string", "输入框的值", "", false, null));
        props.add(createProp("placeholder", "string", "输入框占位符文本", "", false, null));
        props.add(createProp("type", "string", "输入框类型", "text", false, 
            Arrays.asList("text", "password", "number", "email", "tel", "url")));
        props.add(createProp("disabled", "boolean", "是否禁用输入框", "false", false, null));
        props.add(createProp("readonly", "boolean", "是否只读", "false", false, null));
        props.add(createProp("clearable", "boolean", "是否可清空", "false", false, null));
        props.add(createProp("maxlength", "number", "最大输入长度", "", false, null));
        props.add(createProp("minlength", "number", "最小输入长度", "", false, null));
        component.put("props", props);
        
        // 事件
        List<Map<String, Object>> events = new ArrayList<>();
        events.add(createEvent("input", "输入内容时触发", "value"));
        events.add(createEvent("change", "值改变时触发", "value"));
        events.add(createEvent("focus", "获得焦点时触发", "event"));
        events.add(createEvent("blur", "失去焦点时触发", "event"));
        events.add(createEvent("clear", "清空时触发", ""));
        component.put("events", events);
        
        // 插槽
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(createSlot("prefix", "输入框前缀内容", ""));
        slots.add(createSlot("suffix", "输入框后缀内容", ""));
        component.put("slots", slots);
        
        return component;
    }
    
    /**
     * 创建卡片组件示例
     */
    private static Map<String, Object> createCardComponent() {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("name", "my-card");
        component.put("displayName", "自定义卡片");
        component.put("description", "一个灵活的卡片组件，用于展示内容块");
        component.put("tag", "my-card");
        component.put("documentation", "这是一个自定义卡片组件，用于展示内容块。支持头部、内容、底部等区域。");
        
        // 属性
        List<Map<String, Object>> props = new ArrayList<>();
        props.add(createProp("header", "string", "卡片头部文本", "", false, null));
        props.add(createProp("shadow", "string", "卡片阴影效果", "always", false, 
            Arrays.asList("always", "hover", "never")));
        props.add(createProp("bodyStyle", "object", "卡片内容区域样式", "", false, null));
        component.put("props", props);
        
        // 事件
        List<Map<String, Object>> events = new ArrayList<>();
        events.add(createEvent("header-click", "点击卡片头部时触发", ""));
        component.put("events", events);
        
        // 插槽
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(createSlot("header", "卡片头部内容", ""));
        slots.add(createSlot("default", "卡片内容", ""));
        component.put("slots", slots);
        
        return component;
    }
    
    /**
     * 创建模态框组件示例
     */
    private static Map<String, Object> createModalComponent() {
        Map<String, Object> component = new LinkedHashMap<>();
        component.put("name", "my-modal");
        component.put("displayName", "自定义模态框");
        component.put("description", "一个功能完整的模态框组件，用于弹窗展示");
        component.put("tag", "my-modal");
        component.put("documentation", "这是一个自定义模态框组件，用于弹窗展示。支持标题、内容、按钮等配置。");
        
        // 属性
        List<Map<String, Object>> props = new ArrayList<>();
        props.add(createProp("visible", "boolean", "是否显示模态框", "false", false, null));
        props.add(createProp("title", "string", "模态框标题", "", false, null));
        props.add(createProp("width", "string", "模态框宽度", "50%", false, null));
        props.add(createProp("closeOnClickMask", "boolean", "点击遮罩是否关闭", "true", false, null));
        props.add(createProp("showClose", "boolean", "是否显示关闭按钮", "true", false, null));
        component.put("props", props);
        
        // 事件
        List<Map<String, Object>> events = new ArrayList<>();
        events.add(createEvent("open", "模态框打开时触发", ""));
        events.add(createEvent("close", "模态框关闭时触发", ""));
        events.add(createEvent("confirm", "点击确定按钮时触发", ""));
        events.add(createEvent("cancel", "点击取消按钮时触发", ""));
        component.put("events", events);
        
        // 插槽
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(createSlot("header", "模态框头部内容", ""));
        slots.add(createSlot("default", "模态框内容", ""));
        slots.add(createSlot("footer", "模态框底部内容", ""));
        component.put("slots", slots);
        
        return component;
    }
    
    /**
     * 创建属性定义
     */
    private static Map<String, Object> createProp(String name, String type, String description, 
                                                 String defaultValue, boolean required, List<String> options) {
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("name", name);
        prop.put("type", type);
        prop.put("description", description);
        prop.put("defaultValue", defaultValue);
        prop.put("required", required);
        if (options != null) {
            prop.put("options", options);
        }
        return prop;
    }
    
    /**
     * 创建事件定义
     */
    private static Map<String, Object> createEvent(String name, String description, String parameters) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("name", name);
        event.put("description", description);
        event.put("parameters", parameters);
        return event;
    }
    
    /**
     * 创建插槽定义
     */
    private static Map<String, Object> createSlot(String name, String description, String scope) {
        Map<String, Object> slot = new LinkedHashMap<>();
        slot.put("name", name);
        slot.put("description", description);
        slot.put("scope", scope);
        return slot;
    }
} 