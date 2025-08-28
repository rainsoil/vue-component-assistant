package com.chu7.vuecomponentassistant.completion2;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class ComponentProvider {
	private static final Logger LOG = VueKitLogger.getLogger(ComponentProvider.class);
	private final Project project;

	public ComponentProvider(Project project) {
		this.project = project;
		ComponentProviderManager.registerProvider(project, this);
	}

	public void reloadComponents() {
		VueKitLogger.debug(LOG, "Reloading components for project: " + (project != null ? project.getName() : "<null>"));
	}
} 