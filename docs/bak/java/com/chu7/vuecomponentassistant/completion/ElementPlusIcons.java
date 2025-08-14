package com.chu7.vuecomponentassistant.completion;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Element Plus 图标常量
 */
public class ElementPlusIcons {
    
    // 组件图标
    public static final Icon COMPONENT_ICON = IconLoader.getIcon("/icons/component.svg", ElementPlusIcons.class);
    
    // 属性图标
    public static final Icon PROPERTY_ICON = IconLoader.getIcon("/icons/property.svg", ElementPlusIcons.class);
    
    // 事件图标
    public static final Icon EVENT_ICON = IconLoader.getIcon("/icons/event.svg", ElementPlusIcons.class);
    
    // 插槽图标
    public static final Icon SLOT_ICON = IconLoader.getIcon("/icons/slot.svg", ElementPlusIcons.class);
    
    // 如果图标文件不存在，使用默认图标
    static {
        if (COMPONENT_ICON == null) {
            // 使用默认图标
        }
    }
}
