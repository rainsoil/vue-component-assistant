package com.chu7.vuecomponentassistant.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element Plus 智能补全提供者
 * 提供更精确的上下文分析和智能补全
 */
public class ElementPlusSmartCompletionProvider extends CompletionProvider<CompletionParameters> {
    
    private final ElementPlusComponentProvider componentProvider;
    private final ElementPlusContextAnalyzer contextAnalyzer;
    
    // 更精确的模式匹配
    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern EVENT_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern SLOT_PATTERN = Pattern.compile("#([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern ELEMENT_PLUS_PATTERN = Pattern.compile("el-[a-zA-Z-]+");
    
    public ElementPlusSmartCompletionProvider() {
        this.componentProvider = new ElementPlusComponentProvider();
        this.contextAnalyzer = new ElementPlusContextAnalyzer();
    }
    
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                 @NotNull ProcessingContext context,
                                 @NotNull CompletionResultSet result) {
        
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();
        
        if (file == null || !isVueFile(file)) {
            return;
        }
        
        String fileText = file.getText();
        int offset = element.getTextOffset();
        
        // 分析当前位置的上下文
        CompletionContext completionContext = analyzeContext(fileText, offset);
        
        switch (completionContext.getType()) {
            case COMPONENT:
                addComponentCompletions(result, completionContext.getPrefix());
                break;
            case ATTRIBUTE:
                addAttributeCompletions(result, completionContext.getCurrentComponent(), completionContext.getPrefix());
                break;
            case EVENT:
                addEventCompletions(result, completionContext.getCurrentComponent(), completionContext.getPrefix());
                break;
            case SLOT:
                addSlotCompletions(result, completionContext.getCurrentComponent(), completionContext.getPrefix());
                break;
        }
    }
    
    /**
     * 添加组件补全
     */
    private void addComponentCompletions(CompletionResultSet result, String prefix) {
        List<ElementPlusComponent> components;
        
        if (prefix != null && !prefix.isEmpty()) {
            // 根据前缀过滤组件
            components = componentProvider.searchComponents(prefix);
        } else {
            // 显示所有组件
            components = componentProvider.getAllComponents();
        }
        
        for (ElementPlusComponent component : components) {
            LookupElementBuilder element = LookupElementBuilder.create(component.getName())
                    .withTypeText("Element Plus Component")
                    .withTailText(" " + component.getDescription())
                    .withIcon(ElementPlusIcons.COMPONENT_ICON)
                    .withInsertHandler((insertContext, item) -> {
                        // 插入组件标签
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        editor.getDocument().insertString(offset, "></" + component.getName() + ">");
                        editor.getCaretModel().moveToOffset(offset);
                    });
            
            result.addElement(element);
        }
    }
    
    /**
     * 添加属性补全
     */
    private void addAttributeCompletions(CompletionResultSet result, String componentName, String prefix) {
        if (componentName == null) {
            return;
        }
        
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }
        
        for (ElementPlusProp prop : component.getProps()) {
            // 根据前缀过滤属性
            if (prefix != null && !prefix.isEmpty() && !prop.getName().toLowerCase().contains(prefix.toLowerCase())) {
                continue;
            }
            
            LookupElementBuilder propElement = LookupElementBuilder.create(prop.getName())
                    .withTypeText("Property")
                    .withTailText(" " + prop.getDescription())
                    .withIcon(ElementPlusIcons.PROPERTY_ICON)
                    .withInsertHandler((insertContext, item) -> {
                        // 插入属性名和等号
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                                                 String defaultValue = prop.getDefaultValueAsString();
                        editor.getDocument().insertString(offset, "=\"" + defaultValue + "\"");
                        editor.getCaretModel().moveToOffset(offset - 1);
                    });
            
            result.addElement(propElement);
        }
    }
    
    /**
     * 添加事件补全
     */
    private void addEventCompletions(CompletionResultSet result, String componentName, String prefix) {
        if (componentName == null) {
            return;
        }
        
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }
        
        for (ElementPlusEvent event : component.getEvents()) {
            // 根据前缀过滤事件
            if (prefix != null && !prefix.isEmpty() && !event.getName().toLowerCase().contains(prefix.toLowerCase())) {
                continue;
            }
            
            LookupElementBuilder eventElement = LookupElementBuilder.create("@" + event.getName())
                    .withTypeText("Event")
                    .withTailText(" " + event.getDescription())
                    .withIcon(ElementPlusIcons.EVENT_ICON)
                    .withInsertHandler((insertContext, item) -> {
                        // 插入事件处理函数
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        String handlerName = "handle" + capitalize(event.getName());
                        editor.getDocument().insertString(offset, "=\"" + handlerName + "\"");
                        editor.getCaretModel().moveToOffset(offset - 1);
                    });
            
            result.addElement(eventElement);
        }
    }
    
    /**
     * 添加插槽补全
     */
    private void addSlotCompletions(CompletionResultSet result, String componentName, String prefix) {
        if (componentName == null) {
            return;
        }
        
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }
        
        for (ElementPlusSlot slot : component.getSlots()) {
            // 根据前缀过滤插槽
            if (prefix != null && !prefix.isEmpty() && !slot.getName().toLowerCase().contains(prefix.toLowerCase())) {
                continue;
            }
            
            LookupElementBuilder slotElement = LookupElementBuilder.create("#" + slot.getName())
                    .withTypeText("Slot")
                    .withTailText(" " + slot.getDescription())
                    .withIcon(ElementPlusIcons.SLOT_ICON)
                    .withInsertHandler((insertContext, item) -> {
                        // 插入插槽模板
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        editor.getDocument().insertString(offset, "=\"slotName\">");
                        editor.getCaretModel().moveToOffset(offset - 1);
                    });
            
            result.addElement(slotElement);
        }
    }
    
    /**
     * 分析补全上下文
     */
    private CompletionContext analyzeContext(String fileText, int offset) {
        String beforeText = fileText.substring(0, offset);
        
        // 检查是否在组件标签位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            return new CompletionContext(CompletionType.COMPONENT, null, null);
        }
        
        // 检查是否在属性位置
        Matcher attrMatcher = ATTRIBUTE_PATTERN.matcher(beforeText);
        if (attrMatcher.find()) {
            String currentComponent = getCurrentComponent(beforeText);
            String prefix = getAttributePrefix(beforeText);
            return new CompletionContext(CompletionType.ATTRIBUTE, currentComponent, prefix);
        }
        
        // 检查是否在事件位置
        Matcher eventMatcher = EVENT_PATTERN.matcher(beforeText);
        if (eventMatcher.find()) {
            String currentComponent = getCurrentComponent(beforeText);
            String prefix = getEventPrefix(beforeText);
            return new CompletionContext(CompletionType.EVENT, currentComponent, prefix);
        }
        
        // 检查是否在插槽位置
        Matcher slotMatcher = SLOT_PATTERN.matcher(beforeText);
        if (slotMatcher.find()) {
            String currentComponent = getCurrentComponent(beforeText);
            String prefix = getSlotPrefix(beforeText);
            return new CompletionContext(CompletionType.SLOT, currentComponent, prefix);
        }
        
        // 检查是否在组件名称位置
        String componentPrefix = getComponentPrefix(beforeText);
        if (componentPrefix != null) {
            return new CompletionContext(CompletionType.COMPONENT, null, componentPrefix);
        }
        
        return new CompletionContext(CompletionType.COMPONENT, null, null);
    }
    
    /**
     * 获取当前组件名称
     */
    private String getCurrentComponent(String beforeText) {
        Matcher matcher = COMPONENT_TAG_PATTERN.matcher(beforeText);
        String lastComponent = null;
        while (matcher.find()) {
            lastComponent = matcher.group(1);
        }
        
        // 检查是否是Element Plus组件
        if (lastComponent != null && ELEMENT_PLUS_PATTERN.matcher(lastComponent).matches()) {
            return lastComponent;
        }
        
        return null;
    }
    
    /**
     * 获取组件前缀
     */
    private String getComponentPrefix(String beforeText) {
        if (beforeText.endsWith("<")) {
            return "";
        }
        
        // 查找最近的<符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            if (!afterOpenTag.contains(" ") && !afterOpenTag.contains(">")) {
                return afterOpenTag;
            }
        }
        
        return null;
    }
    
    /**
     * 获取属性前缀
     */
    private String getAttributePrefix(String beforeText) {
        // 查找最近的属性名
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(beforeText);
        if (matcher.find()) {
            String attrName = matcher.group(1);
            // 检查是否正在输入属性名
            if (beforeText.endsWith(attrName)) {
                return attrName;
            }
        }
        return null;
    }
    
    /**
     * 获取事件前缀
     */
    private String getEventPrefix(String beforeText) {
        // 查找最近的事件名
        Matcher matcher = EVENT_PATTERN.matcher(beforeText);
        if (matcher.find()) {
            String eventName = matcher.group(1);
            // 检查是否正在输入事件名
            if (beforeText.endsWith("@" + eventName)) {
                return eventName;
            }
        }
        return null;
    }
    
    /**
     * 获取插槽前缀
     */
    private String getSlotPrefix(String beforeText) {
        // 查找最近的插槽名
        Matcher matcher = SLOT_PATTERN.matcher(beforeText);
        if (matcher.find()) {
            String slotName = matcher.group(1);
            // 检查是否正在输入插槽名
            if (beforeText.endsWith("#" + slotName)) {
                return slotName;
            }
        }
        return null;
    }
    
    /**
     * 检查是否是Vue文件
     */
    private boolean isVueFile(PsiFile file) {
        if (file == null) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        String fileType = file.getFileType().getName().toLowerCase();
        
        // 检查文件扩展名
        boolean isVueExtension = fileName.endsWith(".vue") || fileName.endsWith(".html");
        
        // 检查文件类型
        boolean isVueType = fileType.contains("vue") || fileType.contains("html");
        
        return isVueExtension || isVueType;
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 补全上下文类型
     */
    private enum CompletionType {
        COMPONENT, ATTRIBUTE, EVENT, SLOT
    }
    
    /**
     * 补全上下文
     */
    private static class CompletionContext {
        private final CompletionType type;
        private final String currentComponent;
        private final String prefix;
        
        public CompletionContext(CompletionType type, String currentComponent, String prefix) {
            this.type = type;
            this.currentComponent = currentComponent;
            this.prefix = prefix;
        }
        
        public CompletionType getType() {
            return type;
        }
        
        public String getCurrentComponent() {
            return currentComponent;
        }
        
        public String getPrefix() {
            return prefix;
        }
    }
}
