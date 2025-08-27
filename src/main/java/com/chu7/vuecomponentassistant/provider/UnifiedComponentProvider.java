package com.chu7.vuecomponentassistant.provider;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.ComponentLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统一组件提供者
 * 负责管理组件数据并提供组件查询功能
 */
public class UnifiedComponentProvider {
    private final String componentPrefix;
    private final Map<String, Component> byName;
    private final List<Component> componentsList;
    private final String libraryName;
    private final String libraryVersion;
    private final String librarySourceUrl;
    
    public UnifiedComponentProvider(@NotNull ComponentLibrary library) {
        this.componentPrefix = library.componentPrefix != null ? library.componentPrefix : "el-";
        this.componentsList = library.components != null ? new ArrayList<>(library.components) : new ArrayList<>();
        this.libraryName = library.displayName != null && !library.displayName.isEmpty() ? library.displayName : (library.name != null ? library.name : "");
        this.libraryVersion = library.version != null ? library.version : "";
        this.librarySourceUrl = library.sourceUrl != null ? library.sourceUrl : "";
        
        // 构建名称到组件的映射
        this.byName = this.componentsList.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.name != null && !c.name.trim().isEmpty())
                .collect(Collectors.toMap(
                        c -> c.name,
                        c -> c,
                        (existing, replacement) -> existing, // 保留第一个
                        LinkedHashMap::new
                ));
    }
    
    /**
     * 检查是否支持指定的标签名
     */
    public boolean supportsTag(@Nullable String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        return tagName.startsWith(componentPrefix) && byName.containsKey(tagName);
    }
    
    /**
     * 根据标签名获取组件
     */
    @Nullable
    public Component getComponent(@NotNull String tagName) {
        return byName.get(tagName);
    }
    
    /**
     * 获取所有组件列表
     */
    @NotNull
    public List<Component> getAllComponents() {
        return new ArrayList<>(componentsList);
    }
    
    /**
     * 获取组件前缀
     */
    @NotNull
    public String getComponentPrefix() {
        return componentPrefix;
    }
    
    /** 获取库名 */
    @NotNull
    public String getLibraryName() { return libraryName; }
    /** 获取库版本 */
    @NotNull
    public String getLibraryVersion() { return libraryVersion; }
    /** 获取库源地址 */
    @NotNull
    public String getLibrarySourceUrl() { return librarySourceUrl; }
    
    /**
     * 获取组件数量
     */
    public int getComponentCount() {
        return byName.size();
    }
    
    /**
     * 检查是否为空
     */
    public boolean isEmpty() {
        return byName.isEmpty();
    }
} 