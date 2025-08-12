package com.chu7.vuecomponentassistant.completion;

import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 统一补全提供者 - 简化版本
 * 
 * 专注于：
 * - 属性补全
 * - 事件补全
 * - 简单直接的逻辑
 *
 * @author VueKit Team
 * @version 3.0.0
 */
public class UnifiedCompletionProvider extends CompletionProvider<CompletionParameters> {

    /**
     * 组件数据提供者
     */
    private ComponentProvider componentProvider;



    public UnifiedCompletionProvider() {
        // 构造函数
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {

        // 获取项目信息
        Project project = parameters.getEditor().getProject();
        if (project == null) {
            return;
        }

        // 初始化组件提供者
        componentProvider = new ComponentProvider(project);

        // 获取当前元素和文件
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();

        if (file == null || !isVueFile(file)) {
            return;
        }

        System.out.println("=== 开始补全分析 ===");

        // 获取文件文本和光标位置
        String fileText = file.getText();
        int offset = element.getTextOffset();
        String beforeText = fileText.substring(0, offset);
        String currentText = element.getText();

        // 清理当前文本中的IntellijIdeaRulezzz
        String cleanCurrentText = currentText.replace("IntellijIdeaRulezzz", "");

        System.out.println("当前文本: '" + currentText + "'");
        System.out.println("清理后文本: '" + cleanCurrentText + "'");
        System.out.println("光标前文本: '" + beforeText.substring(Math.max(0, beforeText.length() - 50)) + "'");
        System.out.println("光标前文本长度: " + beforeText.length());
        System.out.println("光标位置: " + offset);

        // 1. 检查是否在事件位置 (@开头) - 优先级最高
        if (cleanCurrentText.startsWith("@")) {
            System.out.println("✅ 检测到事件补全场景: @开头");
            System.out.println("当前文本长度: " + cleanCurrentText.length());
            System.out.println("事件前缀: '" + cleanCurrentText.substring(1) + "'");
            handleEventCompletion(beforeText, cleanCurrentText, result);
            return;
        }
        
        // 2. 检查是否在卡槽位置 (#开头，支持sl前缀)
        if (cleanCurrentText.startsWith("#")) {
            System.out.println("✅ 检测到卡槽补全场景: #开头");
            handleSlotCompletion(beforeText, cleanCurrentText, result);
            return;
        }

        // 3. 检查是否在组件位置 (<开头)
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            handleComponentCompletion(cleanCurrentText, result);
            return;
        }

        // 4. 默认处理属性补全 (空格后，没有@和#)
        System.out.println("✅ 检测到属性补全场景");
        handleAttributeCompletion(beforeText, cleanCurrentText, result);

        System.out.println("未匹配到任何补全场景");
    }

    /**
     * 处理事件补全
     */
    private void handleEventCompletion(String beforeText, String currentText, CompletionResultSet result) {
        System.out.println("=== 处理事件补全 ===");
        
        // 获取事件前缀
        final String eventPrefix;
        if (currentText.startsWith("@")) {
            eventPrefix = currentText.substring(1);
        } else {
            eventPrefix = "";
        }
        
        System.out.println("事件前缀: '" + eventPrefix + "'");
        
        // 获取所有可用的事件（不依赖特定组件）
        List<ComponentInfo.ComponentEvent> allEvents = getAllAvailableEvents();
        if (allEvents.isEmpty()) {
            System.out.println("❌ 没有找到可用事件");
            return;
        }
        
        System.out.println("找到事件数量: " + allEvents.size());
        
        // 过滤事件
        System.out.println("开始过滤事件，前缀: '" + eventPrefix + "'");
        List<ComponentInfo.ComponentEvent> filteredEvents = allEvents.stream()
                .filter(event -> {
                    boolean matches = eventPrefix.isEmpty() || 
                            event.getName().toLowerCase().startsWith(eventPrefix.toLowerCase());
                    if (eventPrefix.length() > 0) {
                        System.out.println("检查事件: " + event.getName() + " 匹配 " + eventPrefix + " = " + matches);
                    }
                    return matches;
                })
                .limit(20)
                .collect(Collectors.toList());
        
        System.out.println("过滤后事件数量: " + filteredEvents.size());
        
        // 创建补全选项
        for (ComponentInfo.ComponentEvent event : filteredEvents) {
            LookupElementBuilder completion = LookupElementBuilder.create(event.getName())
                    .withTypeText("Event")
                    .withTailText(" - " + (event.getDescription() != null ? event.getDescription() : "事件"))
                    .withBoldness(true)
                    .withInsertHandler((insertContext, item) -> {
                        // 自动插入 ="handleEventName"
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        
                        // 根据事件类型插入不同的处理函数
                        String eventHandler = generateEventHandler(event);
                        
                        editor.getDocument().insertString(offset, "=\"" + eventHandler + "\"");
                        
                        // 将光标移动到引号中间，方便用户修改
                        editor.getCaretModel().moveToOffset(offset + 2);
                    });
            
            result.addElement(completion);
            System.out.println("✅ 添加事件: " + event.getName());
        }
        
        result.stopHere();
        System.out.println("=== 事件补全完成 ===");
        
        // 强制显示补全提示
        System.out.println("=== 尝试强制显示补全提示 ===");
        
        // 使用兼容的方式强制刷新
        try {
            // 调用 stopHere() 来确保补全结果被处理
            result.stopHere();
            System.out.println("✅ 强制刷新完成");
        } catch (Exception e) {
            System.out.println("❌ 强制刷新失败: " + e.getMessage());
            // 不打印堆栈跟踪，避免日志污染
        }
        
        System.out.println("=== 事件补全强制显示完成 ===");
    }

    /**
     * 处理属性补全
     */
    private void handleAttributeCompletion(String beforeText, String currentText, CompletionResultSet result) {
        System.out.println("=== 处理属性补全 ===");
        
        // 获取属性前缀
        final String attributePrefix;
        if (!currentText.isEmpty()) {
            attributePrefix = currentText;
        } else {
            // 从beforeText中提取最后一个空格后的内容
            int lastSpaceIndex = beforeText.lastIndexOf(" ");
            if (lastSpaceIndex > 0) {
                attributePrefix = beforeText.substring(lastSpaceIndex + 1);
            } else {
                attributePrefix = "";
            }
        }
        
        System.out.println("属性前缀: '" + attributePrefix + "'");
        
        // 获取所有可用的属性（不依赖特定组件）
        List<ComponentInfo.ComponentProp> allProps = getAllAvailableProps();
        if (allProps.isEmpty()) {
            System.out.println("❌ 没有找到可用属性");
            return;
        }
        
        System.out.println("找到属性数量: " + allProps.size());
        
        // 过滤属性
        List<ComponentInfo.ComponentProp> filteredProps = allProps.stream()
                .filter(prop -> attributePrefix.isEmpty() || 
                        prop.getName().toLowerCase().startsWith(attributePrefix.toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
        
        System.out.println("过滤后属性数量: " + filteredProps.size());
        
        // 创建补全选项
        for (ComponentInfo.ComponentProp prop : filteredProps) {
            LookupElementBuilder completion = LookupElementBuilder.create(prop.getName())
                    .withTypeText("Property")
                    .withTailText(" - " + (prop.getDescription() != null ? prop.getDescription() : "属性"))
                    .withBoldness(true)
                    .withInsertHandler((insertContext, item) -> {
                        // 自动插入 ="defaultValue"
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        
                        // 根据属性类型和默认值生成合适的值
                        String defaultValue = generateDefaultValue(prop);
                        
                        editor.getDocument().insertString(offset, "=\"" + defaultValue + "\"");
                        
                        // 将光标移动到引号中间，方便用户修改
                        editor.getCaretModel().moveToOffset(offset + 2);
                    });
            
            result.addElement(completion);
            System.out.println("✅ 添加属性: " + prop.getName());
        }
        
        result.stopHere();
        System.out.println("=== 属性补全完成 ===");
        
        // 强制显示补全提示
        System.out.println("=== 尝试强制显示补全提示 ===");
        
        // 使用兼容的方式强制刷新
        try {
            // 调用 stopHere() 来确保补全结果被处理
            result.stopHere();
            System.out.println("✅ 强制刷新完成");
        } catch (Exception e) {
            System.out.println("❌ 强制刷新失败: " + e.getMessage());
            // 不打印堆栈跟踪，避免日志污染
        }
        
        System.out.println("=== 属性补全强制显示完成 ===");
    }

    /**
     * 处理组件补全
     */
    private void handleComponentCompletion(String currentText, CompletionResultSet result) {
        System.out.println("=== 处理组件补全 ===");
        
        String componentPrefix = currentText;
        System.out.println("组件前缀: '" + componentPrefix + "'");
        
        // 获取所有组件
        List<ComponentInfo> components = componentProvider.getAllComponents();
        if (components.isEmpty()) {
            System.out.println("❌ 没有找到组件");
            return;
        }
        
        System.out.println("找到组件数量: " + components.size());
        
        // 过滤组件
        List<ComponentInfo> filteredComponents = components.stream()
                .filter(comp -> componentPrefix.isEmpty() || 
                        comp.getName().toLowerCase().startsWith(componentPrefix.toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
        
        System.out.println("过滤后组件数量: " + filteredComponents.size());
        
        // 创建补全选项
        for (ComponentInfo component : filteredComponents) {
            LookupElementBuilder completion = LookupElementBuilder.create(component.getName())
                    .withTypeText("Component")
                    .withTailText(" - " + (component.getDescription() != null ? component.getDescription() : "组件"))
                    .withBoldness(true);
            
            result.addElement(completion);
            System.out.println("✅ 添加组件: " + component.getName());
        }
        
        result.stopHere();
        System.out.println("=== 组件补全完成 ===");
    }
    
    /**
     * 处理卡槽补全
     */
    private void handleSlotCompletion(String beforeText, String currentText, CompletionResultSet result) {
        System.out.println("=== 处理卡槽补全 ===");
        
        // 获取卡槽前缀
        final String slotPrefix;
        if (currentText.startsWith("#")) {
            slotPrefix = currentText.substring(1);
        } else {
            slotPrefix = "";
        }
        
        System.out.println("卡槽前缀: '" + slotPrefix + "'");
        
        // 获取所有可用的卡槽（不依赖特定组件）
        List<ComponentInfo.ComponentSlot> allSlots = getAllAvailableSlots();
        if (allSlots.isEmpty()) {
            System.out.println("❌ 没有找到可用卡槽");
            return;
        }
        
        System.out.println("找到卡槽数量: " + allSlots.size());
        
        // 过滤卡槽 - 支持sl前缀匹配
        List<ComponentInfo.ComponentSlot> filteredSlots = allSlots.stream()
                .filter(slot -> {
                    String slotName = slot.getName();
                    // 如果前缀是sl，则显示所有卡槽（特殊快捷方式）
                    if (slotPrefix.equals("sl")) {
                        return true; // 显示所有卡槽
                    }
                    // 否则进行普通前缀匹配
                    return slotPrefix.isEmpty() || 
                           slotName.toLowerCase().startsWith(slotPrefix.toLowerCase());
                })
                .limit(20)
                .collect(Collectors.toList());
        
        System.out.println("过滤后卡槽数量: " + filteredSlots.size());
        
        // 创建补全选项
        for (ComponentInfo.ComponentSlot slot : filteredSlots) {
            LookupElementBuilder completion = LookupElementBuilder.create(slot.getName())
                    .withTypeText("Slot")
                    .withTailText(" - " + (slot.getDescription() != null ? slot.getDescription() : "卡槽"))
                    .withBoldness(true)
                    .withInsertHandler((insertContext, item) -> {
                        // 自动插入 ="slotName"
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        
                        // 生成卡槽名称
                        String slotName = slot.getName();
                        
                        editor.getDocument().insertString(offset, "=\"" + slotName + "\"");
                        
                        // 将光标移动到引号中间，方便用户修改
                        editor.getCaretModel().moveToOffset(offset + 2);
                    });
            
            result.addElement(completion);
            System.out.println("✅ 添加卡槽: " + slot.getName());
        }
        
        result.stopHere();
        System.out.println("=== 卡槽补全完成 ===");
        
        // 强制显示补全提示
        System.out.println("=== 尝试强制显示补全提示 ===");
        
        // 使用兼容的方式强制刷新
        try {
            // 调用 stopHere() 来确保补全结果被处理
            result.stopHere();
            System.out.println("✅ 强制刷新完成");
        } catch (Exception e) {
            System.out.println("❌ 强制刷新失败: " + e.getMessage());
            // 不打印堆栈跟踪，避免日志污染
        }
        
        System.out.println("=== 卡槽补全强制显示完成 ===");
    }

    /**
     * 检查是否为Vue文件
     */
    private boolean isVueFile(PsiFile file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
    }

    /**
     * 根据属性类型和默认值生成合适的值
     */
    private String generateDefaultValue(ComponentInfo.ComponentProp prop) {
        String defaultValue = prop.getDefaultValue();
        if (defaultValue != null && !defaultValue.isEmpty()) {
            return defaultValue;
        }

        switch (prop.getType()) {
            case "String":
                return "\"\"";
            case "Number":
                return "0";
            case "Boolean":
                return "false";
            case "Array":
                return "[]";
            case "Object":
                return "{}";
            default:
                return "\"\""; // 默认值
        }
    }

    /**
     * 获取所有可用的事件
     */
    private List<ComponentInfo.ComponentEvent> getAllAvailableEvents() {
        List<ComponentInfo.ComponentEvent> allEvents = new ArrayList<>();
        
        // 从所有可用的组件库中获取事件
        if (componentProvider != null) {
            // 获取所有组件
            List<ComponentInfo> allComponents = componentProvider.getAllComponents();
            
            // 收集所有组件的事件
            for (ComponentInfo component : allComponents) {
                List<ComponentInfo.ComponentEvent> componentEvents = componentProvider.getComponentEvents(component.getName());
                if (componentEvents != null) {
                    allEvents.addAll(componentEvents);
                }
            }
        }
        
        // 如果没有找到事件，返回一些常用的事件
        if (allEvents.isEmpty()) {
            allEvents.add(new ComponentInfo.ComponentEvent("click", "点击事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("change", "值改变事件", "$event"));
            allEvents.add(new ComponentInfo.ComponentEvent("input", "输入事件", "value"));
            allEvents.add(new ComponentInfo.ComponentEvent("blur", "失去焦点事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("focus", "获得焦点事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("submit", "提交事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("scroll", "滚动事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("resize", "尺寸改变事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("error", "错误事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("load", "加载完成事件", ""));
            // 添加更多常用事件，包括以cu开头的事件
            allEvents.add(new ComponentInfo.ComponentEvent("current-change", "当前行改变事件", "$event"));
            allEvents.add(new ComponentInfo.ComponentEvent("current-row-change", "当前行改变事件", "$event"));
            allEvents.add(new ComponentInfo.ComponentEvent("custom-event", "自定义事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("cut", "剪切事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("copy", "复制事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("contextmenu", "右键菜单事件", "$event"));
            allEvents.add(new ComponentInfo.ComponentEvent("close", "关闭事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("cancel", "取消事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("confirm", "确认事件", ""));
            allEvents.add(new ComponentInfo.ComponentEvent("complete", "完成事件", ""));
        }
        
        return allEvents;
    }

    /**
     * 获取所有可用的属性
     */
    private List<ComponentInfo.ComponentProp> getAllAvailableProps() {
        List<ComponentInfo.ComponentProp> allProps = new ArrayList<>();
        
        // 从所有可用的组件库中获取属性
        if (componentProvider != null) {
            // 获取所有组件
            List<ComponentInfo> allComponents = componentProvider.getAllComponents();
            
            // 收集所有组件的属性
            for (ComponentInfo component : allComponents) {
                List<ComponentInfo.ComponentProp> componentProps = componentProvider.getComponentProps(component.getName());
                if (componentProps != null) {
                    allProps.addAll(componentProps);
                }
            }
        }
        
        // 如果没有找到属性，返回一些常用的属性
        if (allProps.isEmpty()) {
            allProps.add(new ComponentInfo.ComponentProp("id", "String", "唯一标识符", "", false));
            allProps.add(new ComponentInfo.ComponentProp("class", "String", "CSS类名", "", false));
            allProps.add(new ComponentInfo.ComponentProp("style", "String", "内联样式", "", false));
            allProps.add(new ComponentInfo.ComponentProp("disabled", "Boolean", "是否禁用", "false", false));
            allProps.add(new ComponentInfo.ComponentProp("readonly", "Boolean", "是否只读", "false", false));
            allProps.add(new ComponentInfo.ComponentProp("placeholder", "String", "占位符文本", "", false));
            allProps.add(new ComponentInfo.ComponentProp("value", "String", "当前值", "", false));
            allProps.add(new ComponentInfo.ComponentProp("size", "String", "尺寸大小", "default", false));
            allProps.add(new ComponentInfo.ComponentProp("type", "String", "类型", "text", false));
            allProps.add(new ComponentInfo.ComponentProp("name", "String", "名称", "", false));
        }
        
        return allProps;
    }

    /**
     * 获取所有可用的卡槽
     */
    private List<ComponentInfo.ComponentSlot> getAllAvailableSlots() {
        List<ComponentInfo.ComponentSlot> allSlots = new ArrayList<>();
        
        // 从所有可用的组件库中获取卡槽
        if (componentProvider != null) {
            // 获取所有组件
            List<ComponentInfo> allComponents = componentProvider.getAllComponents();
            
            // 收集所有组件的卡槽
            for (ComponentInfo component : allComponents) {
                List<ComponentInfo.ComponentSlot> componentSlots = componentProvider.getComponentSlots(component.getName());
                if (componentSlots != null) {
                    allSlots.addAll(componentSlots);
                }
            }
        }
        
        // 如果没有找到卡槽，返回一些常用的卡槽
        if (allSlots.isEmpty()) {
            allSlots.add(new ComponentInfo.ComponentSlot("default", "默认卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("header", "头部卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("footer", "底部卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("content", "内容卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("title", "标题卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("description", "描述卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("extra", "额外内容卡槽", ""));
            allSlots.add(new ComponentInfo.ComponentSlot("action", "操作卡槽", ""));
        }
        
        return allSlots;
    }

    /**
     * 根据事件类型生成合适的事件处理函数
     */
    private String generateEventHandler(ComponentInfo.ComponentEvent event) {
        String eventName = event.getName();
        String handlerName = "handle" + eventName.substring(0, 1).toUpperCase() + eventName.substring(1);

        // 根据事件名称判断是否需要参数
        if (eventName.contains("change") || eventName.contains("input") || eventName.contains("blur") || 
            eventName.contains("focus") || eventName.contains("submit") || eventName.contains("scroll") || 
            eventName.contains("resize") || eventName.contains("error") || eventName.contains("mouse") || 
            eventName.contains("key")) {
            return handlerName + "($event)";
        } else {
            return handlerName + "()";
        }
    }
} 