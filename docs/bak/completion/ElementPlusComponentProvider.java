package bak.completion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Element Plus 组件数据提供者
 * 负责加载和管理组件数据
 */
public class ElementPlusComponentProvider {
    
    private static final Logger LOG = Logger.getInstance(ElementPlusComponentProvider.class);
    private static final String COMPONENTS_DATA_PATH = "/data/element-plus-components.json";
    
    private final Map<String, ElementPlusComponent> componentsMap;
    private final List<ElementPlusComponent> componentsList;
    
    public ElementPlusComponentProvider() {
        this.componentsMap = new HashMap<>();
        this.componentsList = new ArrayList<>();
        loadComponents();
    }
    
    /**
     * 加载组件数据
     */
    private void loadComponents() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(COMPONENTS_DATA_PATH);
            if (inputStream == null) {
                LOG.error("Cannot find Element Plus components data file: " + COMPONENTS_DATA_PATH);
                return;
            }
            
            // 使用标准 Java 方法读取文件，确保 UTF-8 编码
            byte[] bytes = inputStream.readAllBytes();
            String jsonContent = new String(bytes, StandardCharsets.UTF_8);
            inputStream.close();
            
            // 添加编码调试信息
            System.out.println("=== JSON 文件编码测试 ===");
            System.out.println("文件大小: " + bytes.length + " 字节");
            System.out.println("内容长度: " + jsonContent.length() + " 字符");
            System.out.println("内容前200字符: " + jsonContent.substring(0, Math.min(200, jsonContent.length())));
            System.out.println("是否包含中文字符: " + jsonContent.contains("按钮"));
            System.out.println("是否包含emoji: " + jsonContent.contains("📦"));
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ElementPlusComponent>>(){}.getType();
            List<ElementPlusComponent> components = gson.fromJson(jsonContent, listType);
            
            for (ElementPlusComponent component : components) {
                componentsMap.put(component.getName(), component);
                componentsList.add(component);
            }
            
            LOG.info("Loaded " + components.size() + " Element Plus components");
            
        } catch (IOException e) {
            LOG.error("Failed to load Element Plus components data", e);
        }
    }
    
    /**
     * 获取所有组件列表
     */
    public List<ElementPlusComponent> getAllComponents() {
        return new ArrayList<>(componentsList);
    }
    
    /**
     * 根据组件名获取组件
     */
    public ElementPlusComponent getComponent(String componentName) {
        return componentsMap.get(componentName);
    }
    
    /**
     * 根据前缀搜索组件
     */
    public List<ElementPlusComponent> searchComponents(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return getAllComponents();
        }
        
        return componentsList.stream()
                .filter(component -> component.getName().toLowerCase().contains(prefix.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取组件的所有属性
     */
    public List<ElementPlusProp> getComponentProps(String componentName) {
        ElementPlusComponent component = getComponent(componentName);
        return component != null ? component.getProps() : new ArrayList<>();
    }
    
    /**
     * 获取组件的所有事件
     */
    public List<ElementPlusEvent> getComponentEvents(String componentName) {
        ElementPlusComponent component = getComponent(componentName);
        return component != null ? component.getEvents() : new ArrayList<>();
    }
    
    /**
     * 获取组件的所有插槽
     */
    public List<ElementPlusSlot> getComponentSlots(String componentName) {
        ElementPlusComponent component = getComponent(componentName);
        return component != null ? component.getSlots() : new ArrayList<>();
    }
    
    /**
     * 检查组件是否存在
     */
    public boolean hasComponent(String componentName) {
        return componentsMap.containsKey(componentName);
    }
    
    /**
     * 获取组件总数
     */
    public int getComponentCount() {
        return componentsList.size();
    }
}
