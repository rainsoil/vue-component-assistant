package com.chu7.vuecomponentassistant.debug;

import com.chu7.vuecomponentassistant.library.model.Component;
import com.chu7.vuecomponentassistant.library.model.Prop;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 属性调试Action
 * 用于检查属性补全功能的状态
 */
public class AttributeDebugAction extends AnAction {
    
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
            info.append("Attribute Completion Debug Info:\n\n");
            
            // 显示所有组件的属性信息
            var components = provider.getAllComponents();
            for (Component component : components) {
                info.append("=== ").append(component.name).append(" ===\n");
                info.append("Description: ").append(component.description).append("\n");
                
                if (component.props != null && !component.props.isEmpty()) {
                    info.append("Properties (").append(component.props.size()).append("):\n");
                    for (Prop prop : component.props) {
                        info.append("  - ").append(prop.name);
                        if (prop.description != null) {
                            info.append(": ").append(prop.description);
                        }
                        if (prop.defaultValue != null) {
                            info.append(" (default: ").append(prop.defaultValue).append(")");
                        }
                        if (prop.required) {
                            info.append(" [required]");
                        }
                        if (prop.options != null && !prop.options.isEmpty()) {
                            info.append(" [options: ").append(String.join(", ", prop.options)).append("]");
                        }
                        info.append("\n");
                    }
                } else {
                    info.append("No properties\n");
                }
                info.append("\n");
            }
            
            Messages.showInfoMessage(info.toString(), "Attribute Completion Debug");
            
        } catch (Exception ex) {
            Messages.showErrorDialog("Error: " + ex.getMessage(), "Debug Error");
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getData(CommonDataKeys.PROJECT) != null);
    }
} 