package com.chu7.vuecomponentassistant.debug;

import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 组件调试Action
 * 用于检查组件提供者的状态
 */
public class ComponentDebugAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            Messages.showErrorDialog("No project found", "Debug Error");
            return;
        }
        
        try {
            var provider = ProviderManager.getProvider(project);
            if (provider == null) {
                Messages.showErrorDialog("Provider is null", "Debug Error");
                return;
            }
            
            StringBuilder info = new StringBuilder();
            info.append("Component Provider Debug Info:\n\n");
            info.append("Component Prefix: ").append(provider.getComponentPrefix()).append("\n");
            info.append("Component Count: ").append(provider.getComponentCount()).append("\n");
            info.append("Is Empty: ").append(provider.isEmpty()).append("\n\n");
            
            info.append("Available Components:\n");
            var components = provider.getAllComponents();
            for (var component : components) {
                info.append("- ").append(component.name).append(": ").append(component.description).append("\n");
            }
            
            Messages.showInfoMessage(info.toString(), "Component Provider Debug");
            
        } catch (Exception ex) {
            Messages.showErrorDialog("Error: " + ex.getMessage(), "Debug Error");
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(CommonDataKeys.PROJECT) != null);
    }
} 