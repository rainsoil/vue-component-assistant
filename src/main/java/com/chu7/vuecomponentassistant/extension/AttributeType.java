package com.chu7.vuecomponentassistant.extension;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 属性类型枚举
 * 定义不同类型的属性（参数、事件等）
 */
public enum AttributeType {
    /**
     * 普通参数属性
     */
    PARAM("参数", AllIcons.Nodes.Parameter),
    
    /**
     * 事件属性
     */
    EVENT("事件", AllIcons.Nodes.Method),
    
    /**
     * 插槽属性
     */
    SLOT("插槽", AllIcons.Nodes.Class);
    
    private final String displayName;
    private final Icon icon;
    
    AttributeType(@NotNull String displayName, @NotNull Icon icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    
    /**
     * 获取显示名称
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取图标
     */
    @NotNull
    public Icon getIcon() {
        return icon;
    }
} 