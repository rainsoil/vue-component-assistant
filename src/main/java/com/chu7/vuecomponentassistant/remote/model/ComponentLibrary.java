package com.chu7.vuecomponentassistant.remote.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 组件库数据模型
 */
public class ComponentLibrary {
    private String id;              // 组件库唯一标识
    private String name;            // 组件库名称
    private String displayName;     // 显示名称
    private String description;     // 描述
    private String version;         // 版本
    private LibrarySource source;   // 来源类型
    private String sourceUrl;       // 远程URL（如果适用）
    private LocalDateTime lastUpdated; // 最后更新时间
    private List<ComponentInfo> components; // 组件列表

    public enum LibrarySource {
        OFFICIAL("官方组件库"),
        CUSTOM_LOCAL("自定义本地"),
        CUSTOM_REMOTE("自定义远程");

        private final String displayName;

        LibrarySource(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 构造函数
    public ComponentLibrary() {}

    public ComponentLibrary(String id, String name, String displayName, String description, 
                          String version, LibrarySource source, String sourceUrl) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.version = version;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LibrarySource getSource() {
        return source;
    }

    public void setSource(LibrarySource source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<ComponentInfo> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentInfo> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "ComponentLibrary{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", version='" + version + '\'' +
                ", source=" + source +
                '}';
    }
} 