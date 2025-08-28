package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

public class LibraryTypeHelper {
	private static final Logger LOG = Logger.getInstance(LibraryTypeHelper.class);
	public static final String UNKNOWN = "unknown";
	public static final String UNKNOWN_DISPLAY = "未知组件库";
	private static final DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();

	public static String getPackageName(String libraryName) {
		if (libraryName == null || libraryName.trim().isEmpty()) return UNKNOWN;
		DynamicLibraryConfigManager.LibraryConfig config = configManager.getLibraryConfig(libraryName);
		if (config != null && config.getPackageName() != null) return config.getPackageName();
		String inferred = configManager.inferLibraryTypeFromPackageName(libraryName);
		return inferred != null ? inferred : UNKNOWN;
	}

	public static String getDisplayName(String libraryName) {
		if (libraryName == null || libraryName.trim().isEmpty()) return UNKNOWN_DISPLAY;
		DynamicLibraryConfigManager.LibraryConfig config = configManager.getLibraryConfig(libraryName);
		if (config != null && config.getDisplayName() != null) return config.getDisplayName();
		String inferred = configManager.inferLibraryTypeFromPackageName(libraryName);
		if (inferred != null) {
			DynamicLibraryConfigManager.LibraryConfig c = configManager.getLibraryConfig(inferred);
			if (c != null && c.getDisplayName() != null) return c.getDisplayName();
		}
		return UNKNOWN_DISPLAY;
	}

	public static boolean isKnownLibrary(String libraryName) {
		if (libraryName == null || libraryName.trim().isEmpty()) return false;
		return configManager.isKnownLibrary(libraryName);
	}

	public static String getComponentPrefix(String libraryName) {
		if (libraryName == null || libraryName.trim().isEmpty()) return "";
		return configManager.getComponentPrefix(libraryName);
	}
} 