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

    // 简单的组件标签模式
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");

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

        // 1. 检查是否在事件位置 (@开头)
        if (cleanCurrentText.startsWith("@") || beforeText.endsWith("@")) {
            handleEventCompletion(beforeText, cleanCurrentText, result);
            return;
        }
        
        // 2. 检查是否在卡槽位置 (#开头，支持sl前缀)
        if (cleanCurrentText.startsWith("#") || beforeText.endsWith("#")) {
            handleSlotCompletion(beforeText, cleanCurrentText, result);
            return;
        }

        // 2. 检查是否在属性位置 (空格后，没有@和#)
        if (isInAttributePosition(beforeText, cleanCurrentText)) {
            handleAttributeCompletion(beforeText, cleanCurrentText, result);
            return;
        }

        // 3. 检查是否在组件位置 (<开头)
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            handleComponentCompletion(cleanCurrentText, result);
            return;
        }

        System.out.println("未匹配到任何补全场景");
    }

    /**
     * 处理事件补全
     */
    private void handleEventCompletion(String beforeText, String currentText, CompletionResultSet result) {
        System.out.println("=== 处理事件补全 ===");
        
        // 获取当前组件
        String componentName = getCurrentComponent(beforeText);
        if (componentName == null) {
            System.out.println("❌ 找不到当前组件");
            return;
        }
        
        System.out.println("当前组件: " + componentName);
        
        // 获取事件前缀
        final String eventPrefix;
        if (currentText.startsWith("@")) {
            eventPrefix = currentText.substring(1);
        } else {
            eventPrefix = "";
        }
        
        System.out.println("事件前缀: '" + eventPrefix + "'");
        
        // 获取组件事件
        List<ComponentInfo.ComponentEvent> events = componentProvider.getComponentEvents(componentName);
        if (events == null || events.isEmpty()) {
            System.out.println("❌ 组件没有事件");
            return;
        }
        
        System.out.println("找到事件数量: " + events.size());
        
        // 过滤事件
        List<ComponentInfo.ComponentEvent> filteredEvents = events.stream()
                .filter(event -> eventPrefix.isEmpty() || 
                        event.getName().toLowerCase().startsWith(eventPrefix.toLowerCase()))
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
                        
                        // 生成事件处理函数名：handle + 首字母大写的函数名
                        String eventName = event.getName();
                        String handlerName = "handle" + eventName.substring(0, 1).toUpperCase() + eventName.substring(1);
                        
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
        
        // 延迟一下，确保UI有时间更新
        try {
            Thread.sleep(200);
            System.out.println("✅ 延迟200ms完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ 延迟被中断");
        }
        
        // 再次强制刷新
        result.stopHere();
        System.out.println("✅ 二次强制刷新完成");
        
        // 尝试使用反射强制刷新UI
        try {
            System.out.println("=== 尝试反射强制刷新UI ===");
            var refreshMethod = result.getClass().getDeclaredMethod("refresh");
            if (refreshMethod != null) {
                refreshMethod.setAccessible(true);
                refreshMethod.invoke(result);
                System.out.println("✅ 反射刷新方法调用成功");
            } else {
                System.out.println("❌ 找不到refresh方法");
            }
        } catch (Exception e) {
            System.out.println("❌ 反射刷新失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 事件补全强制显示完成 ===");
    }

    /**
     * 处理属性补全
     */
    private void handleAttributeCompletion(String beforeText, String currentText, CompletionResultSet result) {
        System.out.println("=== 处理属性补全 ===");
        
        // 获取当前组件
        String componentName = getCurrentComponent(beforeText);
        if (componentName == null) {
            System.out.println("❌ 找不到当前组件");
            return;
        }
        
        System.out.println("当前组件: " + componentName);
        
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
        
        // 获取组件属性
        List<ComponentInfo.ComponentProp> props = componentProvider.getComponentProps(componentName);
        if (props == null || props.isEmpty()) {
            System.out.println("❌ 组件没有属性");
            return;
        }
        
        System.out.println("找到属性数量: " + props.size());
        
        // 过滤属性
        List<ComponentInfo.ComponentProp> filteredProps = props.stream()
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
        
        // 延迟一下，确保UI有时间更新
        try {
            Thread.sleep(200);
            System.out.println("✅ 延迟200ms完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ 延迟被中断");
        }
        
        // 再次强制刷新
        result.stopHere();
        System.out.println("✅ 二次强制刷新完成");
        
        // 尝试使用反射强制刷新UI
        try {
            System.out.println("=== 尝试反射强制刷新UI ===");
            var refreshMethod = result.getClass().getDeclaredMethod("refresh");
            if (refreshMethod != null) {
                refreshMethod.setAccessible(true);
                refreshMethod.invoke(result);
                System.out.println("✅ 反射刷新方法调用成功");
            } else {
                System.out.println("❌ 找不到refresh方法");
            }
        } catch (Exception e) {
            System.out.println("❌ 反射刷新失败: " + e.getMessage());
            e.printStackTrace();
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
        
        // 获取当前组件
        String componentName = getCurrentComponent(beforeText);
        if (componentName == null) {
            System.out.println("❌ 找不到当前组件");
            return;
        }
        
        System.out.println("当前组件: " + componentName);
        
        // 获取卡槽前缀
        final String slotPrefix;
        if (currentText.startsWith("#")) {
            slotPrefix = currentText.substring(1);
        } else {
            slotPrefix = "";
        }
        
        System.out.println("卡槽前缀: '" + slotPrefix + "'");
        
        // 获取组件卡槽
        List<ComponentInfo.ComponentSlot> slots = componentProvider.getComponentSlots(componentName);
        if (slots == null || slots.isEmpty()) {
            System.out.println("❌ 组件没有卡槽");
            return;
        }
        
        System.out.println("找到卡槽数量: " + slots.size());
        
        // 过滤卡槽 - 支持sl前缀匹配
        List<ComponentInfo.ComponentSlot> filteredSlots = slots.stream()
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
        
        // 延迟一下，确保UI有时间更新
        try {
            Thread.sleep(200);
            System.out.println("✅ 延迟200ms完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("❌ 延迟被中断");
        }
        
        // 再次强制刷新
        result.stopHere();
        System.out.println("✅ 二次强制刷新完成");
        
        // 尝试使用反射强制刷新UI
        try {
            System.out.println("=== 尝试反射强制刷新UI ===");
            var refreshMethod = result.getClass().getDeclaredMethod("refresh");
            if (refreshMethod != null) {
                refreshMethod.setAccessible(true);
                refreshMethod.invoke(result);
                System.out.println("✅ 反射刷新方法调用成功");
            } else {
                System.out.println("❌ 找不到refresh方法");
            }
        } catch (Exception e) {
            System.out.println("❌ 反射刷新失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 卡槽补全强制显示完成 ===");
    }

    /**
     * 检查是否在属性位置
     */
    private boolean isInAttributePosition(String beforeText, String currentText) {
        // 检查是否包含组件标签
        if (!beforeText.contains("<")) {
            return false;
        }
        
        // 检查是否在空格后
        if (!beforeText.contains(" ")) {
            return false;
        }
        
        // 检查是否已经包含@或#
        if (beforeText.contains("@") || beforeText.contains("#")) {
            return false;
        }
        
        // 检查当前输入是否为属性名格式
        return currentText.matches("[a-zA-Z][a-zA-Z0-9-]*") || currentText.isEmpty();
    }

    /**
     * 获取当前组件
     */
    private String getCurrentComponent(String beforeText) {
        Matcher matcher = COMPONENT_PATTERN.matcher(beforeText);
        String lastComponent = null;
        
        while (matcher.find()) {
            lastComponent = matcher.group(1);
        }
        
        if (lastComponent != null) {
            // 支持各种组件前缀
            if (lastComponent.startsWith("el-") || lastComponent.startsWith("a-") || 
                lastComponent.startsWith("ant-") || lastComponent.startsWith("my-") ||
                lastComponent.startsWith("custom-")) {
                System.out.println("✅ 检测到组件: " + lastComponent);
                return lastComponent;
            }
            
            System.out.println("✅ 检测到可能的组件: " + lastComponent);
            return lastComponent;
        }
        
        System.out.println("❌ 未找到组件");
        return null;
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