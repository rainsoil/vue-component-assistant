package com.chu7.vuecomponentassistant.remote;

import java.util.List;

public class OfficialLibrary {
	private String id;
	private String name;
	private String displayName;
	private String description;
	private String version;
	private String category;
	private String framework;
	private String downloadUrl;
	private List<String> tags;

	public String getId() { return id; }
	public String getName() { return name; }
	public String getDisplayName() { return displayName; }
	public String getDescription() { return description; }
	public String getVersion() { return version; }
	public String getCategory() { return category; }
	public String getFramework() { return framework; }
	public String getDownloadUrl() { return downloadUrl; }
	public List<String> getTags() { return tags; }
} 