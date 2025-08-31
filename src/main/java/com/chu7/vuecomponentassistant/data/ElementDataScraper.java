package com.chu7.vuecomponentassistant.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element UI/Plus 数据抓取器
 * 从官网抓取组件信息并生成JSON配置文件
 */
public class ElementDataScraper {
    private static final Logger LOG = Logger.getLogger(ElementDataScraper.class.getName());
    
    static {
        // 配置日志格式
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        LOG.addHandler(handler);
        LOG.setLevel(Level.INFO);
    }
    
    // Element Plus 官网
    private static final String ELEMENT_PLUS_BASE_URL = "https://element-plus.org";
    private static final String ELEMENT_PLUS_COMPONENTS_URL = "https://element-plus.org/en-US/component/";
    
    // Element UI 官网
    private static final String ELEMENT_UI_BASE_URL = "https://element.eleme.io";
    private static final String ELEMENT_UI_COMPONENTS_URL = "https://element.eleme.io/#/en-US/component/";
    
    // 输出目录
    private static final String OUTPUT_DIR = "src/main/resources/data2";
    
    public static void main(String[] args) {
        ElementDataScraper scraper = new ElementDataScraper();
        
        try {
            LOG.info("开始抓取 Element Plus 数据...");
            scraper.scrapeElementPlus();
            
            LOG.info("开始抓取 Element UI 数据...");
            scraper.scrapeElementUI();
            
            LOG.info("数据抓取完成！");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "数据抓取失败", e);
        }
    }
    
    /**
     * 抓取 Element Plus 数据
     */
    public void scrapeElementPlus() throws Exception {
        LOG.info("正在抓取 Element Plus 组件数据...");
        
        // 创建基础库信息
        JsonObject library = createBaseLibrary("element-plus", "Element Plus", "el-", 
            "Element Plus is a Vue 3 based component library for developers, designers and product managers");
        
        // 抓取组件列表
        List<ComponentInfo> components = scrapeElementPlusComponents();
        
        // 转换为JSON格式
        JsonArray componentsArray = new JsonArray();
        for (ComponentInfo component : components) {
            JsonObject compObj = new JsonObject();
            compObj.addProperty("name", component.name);
            compObj.addProperty("tag", component.tag);
            compObj.addProperty("description", component.description);
            compObj.addProperty("category", component.category);
            compObj.addProperty("url", component.url);
            
            // 添加属性
            if (component.attributes != null && !component.attributes.isEmpty()) {
                JsonArray attrsArray = new JsonArray();
                for (AttributeInfo attr : component.attributes) {
                    JsonObject attrObj = new JsonObject();
                    attrObj.addProperty("name", attr.name);
                    attrObj.addProperty("type", attr.type);
                    attrObj.addProperty("description", attr.description);
                    attrObj.addProperty("default", attr.defaultValue);
                    attrObj.addProperty("required", attr.required);
                    attrsArray.add(attrObj);
                }
                compObj.add("attributes", attrsArray);
            }
            
            // 添加事件
            if (component.events != null && !component.events.isEmpty()) {
                JsonArray eventsArray = new JsonArray();
                for (EventInfo event : component.events) {
                    JsonObject eventObj = new JsonObject();
                    eventObj.addProperty("name", event.name);
                    eventObj.addProperty("description", event.description);
                    eventObj.addProperty("parameters", event.parameters);
                    eventsArray.add(eventObj);
                }
                compObj.add("events", eventsArray);
            }
            
            // 添加插槽
            if (component.slots != null && !component.slots.isEmpty()) {
                JsonArray slotsArray = new JsonArray();
                for (SlotInfo slot : component.slots) {
                    JsonObject slotObj = new JsonObject();
                    slotObj.addProperty("name", slot.name);
                    slotObj.addProperty("description", slot.description);
                    slotObj.addProperty("scope", slot.scope);
                    slotsArray.add(slotObj);
                }
                compObj.add("slots", slotsArray);
            }
            
            componentsArray.add(compObj);
        }
        
        library.add("components", componentsArray);
        library.addProperty("componentCount", components.size());
        
        // 保存到文件
        saveToFile(library, "element-plus-libraries.json");
        LOG.info("Element Plus 数据抓取完成，共抓取 " + components.size() + " 个组件");
    }
    
    /**
     * 抓取 Element UI 数据
     */
    public void scrapeElementUI() throws Exception {
        LOG.info("正在抓取 Element UI 组件数据...");
        
        // 创建基础库信息
        JsonObject library = createBaseLibrary("element-ui", "Element UI", "el-", 
            "Element UI is a Vue 2.0 based component library for developers, designers and product managers");
        
        // 抓取组件列表
        List<ComponentInfo> components = scrapeElementUIComponents();
        
        // 转换为JSON格式
        JsonArray componentsArray = new JsonArray();
        for (ComponentInfo component : components) {
            JsonObject compObj = new JsonObject();
            compObj.addProperty("name", component.name);
            compObj.addProperty("tag", component.tag);
            compObj.addProperty("description", component.description);
            compObj.addProperty("category", component.category);
            compObj.addProperty("url", component.url);
            
            // 添加属性
            if (component.attributes != null && !component.attributes.isEmpty()) {
                JsonArray attrsArray = new JsonArray();
                for (AttributeInfo attr : component.attributes) {
                    JsonObject attrObj = new JsonObject();
                    attrObj.addProperty("name", attr.name);
                    attrObj.addProperty("type", attr.type);
                    attrObj.addProperty("description", attr.description);
                    attrObj.addProperty("default", attr.defaultValue);
                    attrObj.addProperty("required", attr.required);
                    attrsArray.add(attrObj);
                }
                compObj.add("attributes", attrsArray);
            }
            
            // 添加事件
            if (component.events != null && !component.events.isEmpty()) {
                JsonArray eventsArray = new JsonArray();
                for (EventInfo event : component.events) {
                    JsonObject eventObj = new JsonObject();
                    eventObj.addProperty("name", event.name);
                    eventObj.addProperty("description", event.description);
                    eventObj.addProperty("parameters", event.parameters);
                    eventsArray.add(eventObj);
                }
                compObj.add("events", eventsArray);
            }
            
            // 添加插槽
            if (component.slots != null && !component.slots.isEmpty()) {
                JsonArray slotsArray = new JsonArray();
                for (SlotInfo slot : component.slots) {
                    JsonObject slotObj = new JsonObject();
                    slotObj.addProperty("name", slot.name);
                    slotObj.addProperty("description", slot.description);
                    slotObj.addProperty("scope", slot.scope);
                    slotsArray.add(slotObj);
                }
                compObj.add("slots", slotsArray);
            }
            
            componentsArray.add(compObj);
        }
        
        library.add("components", componentsArray);
        library.addProperty("componentCount", components.size());
        
        // 保存到文件
        saveToFile(library, "element-ui-libraries.json");
        LOG.info("Element UI 数据抓取完成，共抓取 " + components.size() + " 个组件");
    }
    
    /**
     * 抓取 Element Plus 组件列表
     */
    private List<ComponentInfo> scrapeElementPlusComponents() throws Exception {
        List<ComponentInfo> components = new ArrayList<>();
        
        // Element Plus 主要组件类别
        String[] categories = {
            "basic", "form", "data", "navigation", "feedback", "other"
        };
        
        for (String category : categories) {
            try {
                String categoryUrl = ELEMENT_PLUS_COMPONENTS_URL + category + ".html";
                String html = downloadHtml(categoryUrl);
                
                // 解析组件列表
                List<ComponentInfo> categoryComponents = parseElementPlusComponents(html, category);
                components.addAll(categoryComponents);
                
                LOG.info("抓取 " + category + " 类别完成，共 " + categoryComponents.size() + " 个组件");
                
                // 避免请求过于频繁
                Thread.sleep(1000);
                
            } catch (Exception e) {
                LOG.warning("抓取 " + category + " 类别失败: " + e.getMessage());
            }
        }
        
        return components;
    }
    
    /**
     * 抓取 Element UI 组件列表
     */
    private List<ComponentInfo> scrapeElementUIComponents() throws Exception {
        List<ComponentInfo> components = new ArrayList<>();
        
        // Element UI 主要组件类别
        String[] categories = {
            "basic", "form", "data", "notice", "navigation", "other"
        };
        
        for (String category : categories) {
            try {
                String categoryUrl = ELEMENT_UI_COMPONENTS_URL + category;
                String html = downloadHtml(categoryUrl);
                
                // 解析组件列表
                List<ComponentInfo> categoryComponents = parseElementUIComponents(html, category);
                components.addAll(categoryComponents);
                
                LOG.info("抓取 " + category + " 类别完成，共 " + categoryComponents.size() + " 个组件");
                
                // 避免请求过于频繁
                Thread.sleep(1000);
                
            } catch (Exception e) {
                LOG.warning("抓取 " + category + " 类别失败: " + e.getMessage());
            }
        }
        
        return components;
    }
    
    /**
     * 解析 Element Plus 组件信息
     */
    private List<ComponentInfo> parseElementPlusComponents(String html, String category) {
        List<ComponentInfo> components = new ArrayList<>();
        
        // 使用正则表达式解析组件信息
        Pattern componentPattern = Pattern.compile(
            "<a[^>]*href=\"([^\"]*)\"[^>]*>\\s*<h3[^>]*>([^<]+)</h3>\\s*<p[^>]*>([^<]+)</p>", 
            Pattern.DOTALL
        );
        
        Matcher matcher = componentPattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(1);
            String name = matcher.group(2).trim();
            String description = matcher.group(3).trim();
            
            // 生成组件标签
            String tag = "el-" + name.toLowerCase().replaceAll("\\s+", "-");
            
            ComponentInfo component = new ComponentInfo();
            component.name = name;
            component.tag = tag;
            component.description = description;
            component.category = category;
            component.url = ELEMENT_PLUS_BASE_URL + url;
            
            // 尝试抓取详细属性信息
            try {
                scrapeComponentDetails(component, component.url);
            } catch (Exception e) {
                LOG.fine("抓取组件 " + name + " 详细信息失败: " + e.getMessage());
            }
            
            components.add(component);
        }
        
        return components;
    }
    
    /**
     * 解析 Element UI 组件信息
     */
    private List<ComponentInfo> parseElementUIComponents(String html, String category) {
        List<ComponentInfo> components = new ArrayList<>();
        
        // 使用正则表达式解析组件信息
        Pattern componentPattern = Pattern.compile(
            "<a[^>]*href=\"([^\"]*)\"[^>]*>\\s*<h3[^>]*>([^<]+)</h3>\\s*<p[^>]*>([^<]+)</p>", 
            Pattern.DOTALL
        );
        
        Matcher matcher = componentPattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(1);
            String name = matcher.group(2).trim();
            String description = matcher.group(3).trim();
            
            // 生成组件标签
            String tag = "el-" + name.toLowerCase().replaceAll("\\s+", "-");
            
            ComponentInfo component = new ComponentInfo();
            component.name = name;
            component.tag = tag;
            component.description = description;
            component.category = category;
            component.url = ELEMENT_UI_BASE_URL + url;
            
            // 尝试抓取详细属性信息
            try {
                scrapeComponentDetails(component, component.url);
            } catch (Exception e) {
                LOG.fine("抓取组件 " + name + " 详细信息失败: " + e.getMessage());
            }
            
            components.add(component);
        }
        
        return components;
    }
    
    /**
     * 抓取组件详细信息（属性、事件、插槽）
     */
    private void scrapeComponentDetails(ComponentInfo component, String url) throws Exception {
        String html = downloadHtml(url);
        
        // 解析属性
        component.attributes = parseAttributes(html);
        
        // 解析事件
        component.events = parseEvents(html);
        
        // 解析插槽
        component.slots = parseSlots(html);
    }
    
    /**
     * 解析属性信息
     */
    private List<AttributeInfo> parseAttributes(String html) {
        List<AttributeInfo> attributes = new ArrayList<>();
        
        // 查找属性表格
        Pattern tablePattern = Pattern.compile(
            "<table[^>]*class=\"[^\"]*el-table[^\"]*\"[^>]*>(.*?)</table>", 
            Pattern.DOTALL
        );
        
        Matcher tableMatcher = tablePattern.matcher(html);
        if (tableMatcher.find()) {
            String tableHtml = tableMatcher.group(1);
            
            // 解析表格行
            Pattern rowPattern = Pattern.compile(
                "<tr[^>]*>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*</tr>", 
                Pattern.DOTALL
            );
            
            Matcher rowMatcher = rowPattern.matcher(tableHtml);
            while (rowMatcher.find()) {
                AttributeInfo attr = new AttributeInfo();
                attr.name = rowMatcher.group(1).trim();
                attr.type = rowMatcher.group(2).trim();
                attr.description = rowMatcher.group(3).trim();
                attr.defaultValue = rowMatcher.group(4).trim();
                attr.required = "true".equalsIgnoreCase(rowMatcher.group(5).trim());
                
                if (!attr.name.isEmpty() && !attr.name.equals("参数")) {
                    attributes.add(attr);
                }
            }
        }
        
        return attributes;
    }
    
    /**
     * 解析事件信息
     */
    private List<EventInfo> parseEvents(String html) {
        List<EventInfo> events = new ArrayList<>();
        
        // 查找事件表格
        Pattern tablePattern = Pattern.compile(
            "<table[^>]*class=\"[^\"]*el-table[^\"]*\"[^>]*>(.*?)</table>", 
            Pattern.DOTALL
        );
        
        Matcher tableMatcher = tablePattern.matcher(html);
        if (tableMatcher.find()) {
            String tableHtml = tableMatcher.group(1);
            
            // 解析表格行
            Pattern rowPattern = Pattern.compile(
                "<tr[^>]*>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*</tr>", 
                Pattern.DOTALL
            );
            
            Matcher rowMatcher = rowPattern.matcher(tableHtml);
            while (rowMatcher.find()) {
                EventInfo event = new EventInfo();
                event.name = rowMatcher.group(1).trim();
                event.description = rowMatcher.group(2).trim();
                event.parameters = rowMatcher.group(3).trim();
                
                if (!event.name.isEmpty() && !event.name.equals("事件名称")) {
                    events.add(event);
                }
            }
        }
        
        return events;
    }
    
    /**
     * 解析插槽信息
     */
    private List<SlotInfo> parseSlots(String html) {
        List<SlotInfo> slots = new ArrayList<>();
        
        // 查找插槽表格
        Pattern tablePattern = Pattern.compile(
            "<table[^>]*class=\"[^\"]*el-table[^\"]*\"[^>]*>(.*?)</table>", 
            Pattern.DOTALL
        );
        
        Matcher tableMatcher = tablePattern.matcher(html);
        if (tableMatcher.find()) {
            String tableHtml = tableMatcher.group(1);
            
            // 解析表格行
            Pattern rowPattern = Pattern.compile(
                "<tr[^>]*>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*>([^<]+)</td>\\s*</tr>", 
                Pattern.DOTALL
            );
            
            Matcher rowMatcher = rowPattern.matcher(tableHtml);
            while (rowMatcher.find()) {
                SlotInfo slot = new SlotInfo();
                slot.name = rowMatcher.group(1).trim();
                slot.description = rowMatcher.group(2).trim();
                slot.scope = rowMatcher.group(3).trim();
                
                if (!slot.name.isEmpty() && !slot.name.equals("插槽名")) {
                    slots.add(slot);
                }
            }
        }
        
        return slots;
    }
    
    /**
     * 创建基础库信息
     */
    private JsonObject createBaseLibrary(String id, String name, String prefix, String description) {
        JsonObject library = new JsonObject();
        library.addProperty("id", id);
        library.addProperty("name", name);
        library.addProperty("componentPrefix", prefix);
        library.addProperty("displayName", name);
        library.addProperty("description", description);
        library.addProperty("version", "latest");
        library.addProperty("sourceUrl", id.equals("element-plus") ? ELEMENT_PLUS_BASE_URL : ELEMENT_UI_BASE_URL);
        library.addProperty("lastUpdated", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        library.addProperty("license", "MIT");
        library.addProperty("author", "Element Team");
        return library;
    }
    
    /**
     * 下载HTML内容
     */
    private String downloadHtml(String urlString) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(urlString).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP " + responseCode + " when GET " + urlString);
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
    
    /**
     * 保存到文件
     */
    private void saveToFile(JsonObject data, String filename) throws Exception {
        // 确保输出目录存在
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        
        // 保存文件
        Path filePath = outputPath.resolve(filename);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);
        
        Files.write(filePath, json.getBytes(StandardCharsets.UTF_8));
        LOG.info("数据已保存到: " + filePath.toAbsolutePath());
    }
    
    // 数据模型类
    public static class ComponentInfo {
        public String name;
        public String tag;
        public String description;
        public String category;
        public String url;
        public List<AttributeInfo> attributes;
        public List<EventInfo> events;
        public List<SlotInfo> slots;
    }
    
    public static class AttributeInfo {
        public String name;
        public String type;
        public String description;
        public String defaultValue;
        public boolean required;
    }
    
    public static class EventInfo {
        public String name;
        public String description;
        public String parameters;
    }
    
    public static class SlotInfo {
        public String name;
        public String description;
        public String scope;
    }
} 