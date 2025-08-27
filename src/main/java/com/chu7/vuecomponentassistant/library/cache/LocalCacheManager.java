package com.chu7.vuecomponentassistant.library.cache;

import com.chu7.vuecomponentassistant.library.model.ComponentLibrary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalCacheManager {
	@Nullable
	public static ComponentLibrary load(@NotNull String libraryId, @NotNull String version) {
		try {
			Path file = getCacheFile(libraryId, version);
			if (!Files.exists(file)) return null;
			try (var is = Files.newInputStream(file); var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				Gson gson = new GsonBuilder().create();
				return gson.fromJson(reader, ComponentLibrary.class);
			}
		} catch (Throwable ignored) {
			return null;
		}
	}
	
	public static void save(@NotNull ComponentLibrary library) {
		try {
			String id = library.id != null ? library.id : "unknown";
			String ver = library.version != null && !library.version.isEmpty() ? library.version : "latest";
			Path file = getCacheFile(id, ver);
			Files.createDirectories(file.getParent());
			try (var os = Files.newOutputStream(file); var writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				gson.toJson(library, writer);
			}
		} catch (IOException ignored) {
		}
	}
	
	private static Path getCacheFile(@NotNull String libraryId, @NotNull String version) {
		String base = PathManager.getSystemPath();
		return Path.of(base, "vuekit-cache", sanitize(libraryId), sanitize(version), "library.json");
	}
	
	private static String sanitize(String s) {
		return s.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
} 