package com.chu7.vuecomponentassistant.remote.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 官方组件库信息
 */
public class OfficialLibrary {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private String version;
    private String framework;
    private String category;
    private String author;
    private String homepage;
    private String downloadUrl;
    private int downloadCount;
    private double rating;
    private List<String> tags;
    private LocalDateTime lastUpdated;

    // 构造函数
    public OfficialLibrary() {}

    public OfficialLibrary(String id, String name, String displayName, String description, 
                          String version, String framework, String category, String author, 
                          String homepage, String downloadUrl) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.version = version;
        this.framework = framework;
        this.category = category;
        this.author = author;
        this.homepage = homepage;
        this.downloadUrl = downloadUrl;
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

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "OfficialLibrary{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", version='" + version + '\'' +
                ", framework='" + framework + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
} 