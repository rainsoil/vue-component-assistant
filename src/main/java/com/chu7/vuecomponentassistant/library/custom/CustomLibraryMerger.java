package com.chu7.vuecomponentassistant.library.custom;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.ComponentLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CustomLibraryMerger {
	@NotNull
	public static ComponentLibrary merge(@NotNull ComponentLibrary base, @NotNull List<ComponentLibrary> customs) {
		Map<String, Component> byName = new LinkedHashMap<>();
		if (base.components != null) {
			for (Component c : base.components) {
				if (c != null && c.name != null) byName.put(c.name, c);
			}
		}
		for (ComponentLibrary lib : customs) {
			if (lib == null || lib.components == null) continue;
			for (Component c : lib.components) {
				if (c != null && c.name != null) byName.put(c.name, c);
			}
		}
		ComponentLibrary merged = new ComponentLibrary();
		merged.id = base.id;
		merged.name = base.name;
		merged.displayName = base.displayName;
		merged.version = base.version;
		merged.sourceUrl = base.sourceUrl;
		merged.componentPrefix = base.componentPrefix;
		merged.description = base.description;
		merged.lastUpdated = base.lastUpdated;
		merged.components = new ArrayList<>(byName.values());
		return merged;
	}
} 