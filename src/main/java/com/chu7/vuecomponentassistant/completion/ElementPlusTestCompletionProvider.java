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
 * Element Plus 测试补全提供者
 * 用于调试和测试基本功能
 */
public class ElementPlusTestCompletionProvider extends CompletionProvider<CompletionParameters> {
    
    private final ElementPlusComponentProvider componentProvider;
    
    public ElementPlusTestCompletionProvider() {
        this.componentProvider = new ElementPlusComponentProvider();
    }
    
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                 @NotNull ProcessingContext context,
                                 @NotNull CompletionResultSet result) {
        
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();
        
        if (file == null) {
            return;
        }
        
        // 分析当前位置的上下文
        CompletionContext completionContext = analyzeContext(file, element);
        
        // 添加调试信息
        System.out.println("=== 补全调试信息 ===");
        System.out.println("文件: " + file.getName());
        System.out.println("位置: " + element.getTextOffset());
        System.out.println("当前文本: " + element.getText());
        System.out.println("上下文类型: " + completionContext.getType());
        System.out.println("当前组件: " + completionContext.getCurrentComponent());
        System.out.println("前缀: " + completionContext.getPrefix());
        System.out.println("==================");
        
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
        
        // 限制显示数量，避免过多选项
        int count = 0;
        int maxCount = 30;
        
        for (ElementPlusComponent component : components) {
            if (count >= maxCount) break;
            
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
            count++;
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
            System.out.println("事件补全: 组件名为空");
            return;
        }
        
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            System.out.println("事件补全: 找不到组件 " + componentName);
            return;
        }
        
        System.out.println("事件补全: 为组件 " + componentName + " 添加事件");
        System.out.println("组件事件数量: " + component.getEvents().size());
        System.out.println("前缀: '" + prefix + "'");
        
        int count = 0;
        for (ElementPlusEvent event : component.getEvents()) {
            // 根据前缀过滤事件
            if (prefix != null && !prefix.isEmpty()) {
                String eventName = event.getName().toLowerCase();
                String prefixLower = prefix.toLowerCase();
                if (!eventName.contains(prefixLower)) {
                    System.out.println("事件过滤: " + event.getName() + " 不包含前缀 '" + prefix + "'");
                    continue;
                } else {
                    System.out.println("事件匹配: " + event.getName() + " 包含前缀 '" + prefix + "'");
                }
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
            count++;
        }
        
        System.out.println("添加了 " + count + " 个事件补全");
    }
    
    /**
     * 分析补全上下文
     */
    private CompletionContext analyzeContext(PsiFile file, PsiElement element) {
        String fileText = file.getText();
        int offset = element.getTextOffset();
        String beforeText = fileText.substring(0, offset);
        String currentText = element.getText();
        
        System.out.println("分析上下文 - beforeText: '" + beforeText + "'");
        System.out.println("当前文本: '" + currentText + "'");
        
        // 检查当前文本是否以@开头
        if (currentText.startsWith("@")) {
            System.out.println("当前文本以@开头，直接检查事件");
            String currentComponent = getCurrentComponent(beforeText);
            // 去掉@符号，并处理IntelliJ IDEA的后缀
            String eventPrefix = extractValidEventPrefix(currentText.substring(1));
            
            if (currentComponent != null && eventPrefix != null) {
                System.out.println("返回事件上下文: " + currentComponent + " 前缀: " + eventPrefix);
                return new CompletionContext(CompletionType.EVENT, currentComponent, eventPrefix);
            } else {
                System.out.println("事件检测失败 - 组件: " + currentComponent + ", 前缀: " + eventPrefix);
            }
        }
        
        // 检查beforeText是否包含@符号
        if (beforeText.contains("@")) {
            System.out.println("beforeText包含@符号，检查事件");
            String currentComponent = getCurrentComponent(beforeText);
            String eventPrefix = getEventPrefix(beforeText);
            
            if (currentComponent != null && eventPrefix != null) {
                System.out.println("返回事件上下文: " + currentComponent + " 前缀: " + eventPrefix);
                return new CompletionContext(CompletionType.EVENT, currentComponent, eventPrefix);
            } else {
                System.out.println("事件检测失败 - 组件: " + currentComponent + ", 前缀: " + eventPrefix);
            }
        }
        
        // 检查是否在组件标签位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            System.out.println("检测到组件标签位置");
            return new CompletionContext(CompletionType.COMPONENT, null, null);
        }
        
        // 获取当前组件
        String currentComponent = getCurrentComponent(beforeText);
        System.out.println("当前组件: " + currentComponent);
        
        // 检查是否在属性位置（在组件标签内，有空格但没有=）
        if (currentComponent != null && isInComponentTag(beforeText)) {
            String prefix = getAttributePrefix(beforeText);
            System.out.println("检测到属性位置，前缀: '" + prefix + "'");
            return new CompletionContext(CompletionType.ATTRIBUTE, currentComponent, prefix);
        }
        
        // 检查是否在组件名称位置
        String componentPrefix = getComponentPrefix(beforeText);
        if (componentPrefix != null) {
            System.out.println("检测到组件名称位置，前缀: '" + componentPrefix + "'");
            return new CompletionContext(CompletionType.COMPONENT, null, componentPrefix);
        }
        
        System.out.println("默认返回组件上下文");
        return new CompletionContext(CompletionType.COMPONENT, null, null);
    }
    
    /**
     * 获取当前组件名称
     */
    private String getCurrentComponent(String beforeText) {
        Pattern componentPattern = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
        Matcher matcher = componentPattern.matcher(beforeText);
        String lastComponent = null;
        while (matcher.find()) {
            lastComponent = matcher.group(1);
        }
        
        // 检查是否是Element Plus组件
        Pattern elementPlusPattern = Pattern.compile("el-[a-zA-Z-]+");
        if (lastComponent != null && elementPlusPattern.matcher(lastComponent).matches()) {
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
     * 检查是否在组件标签内
     */
    private boolean isInComponentTag(String beforeText) {
        // 查找最近的<和>符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        int lastCloseTag = beforeText.lastIndexOf('>');
        
        System.out.println("检查是否在组件标签内 - lastOpenTag: " + lastOpenTag + ", lastCloseTag: " + lastCloseTag);
        
        // 如果最近的<在>之后，说明在标签内
        if (lastOpenTag > lastCloseTag) {
            // 检查是否在Element Plus组件标签内
            String tagContent = beforeText.substring(lastOpenTag);
            System.out.println("标签内容: '" + tagContent + "'");
            boolean containsEl = tagContent.contains("el-");
            System.out.println("包含el-: " + containsEl);
            return containsEl;
        }
        
        System.out.println("不在组件标签内");
        return false;
    }
    
    /**
     * 获取属性前缀
     */
    private String getAttributePrefix(String beforeText) {
        // 查找最近的<符号
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            
            // 如果已经输入了空格，说明在属性位置
            if (afterOpenTag.contains(" ")) {
                // 获取最后一个空格后的内容
                int lastSpace = afterOpenTag.lastIndexOf(' ');
                if (lastSpace >= 0) {
                    String afterLastSpace = afterOpenTag.substring(lastSpace + 1);
                    
                    // 如果没有=，说明正在输入属性名
                    if (!afterLastSpace.contains("=")) {
                        return afterLastSpace;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 获取事件前缀
     */
    private String getEventPrefix(String beforeText) {
        // 查找最近的@符号
        int lastAt = beforeText.lastIndexOf('@');
        System.out.println("查找@符号 - lastAt: " + lastAt);
        
        if (lastAt >= 0) {
            String afterAt = beforeText.substring(lastAt + 1);
            System.out.println("@符号后的内容: '" + afterAt + "'");
            
            // 如果没有=，说明正在输入事件名
            if (!afterAt.contains("=")) {
                // 简化逻辑：直接提取有效的事件名前缀
                String validPrefix = extractValidEventPrefix(afterAt);
                if (validPrefix != null) {
                    System.out.println("提取到有效事件前缀: '" + validPrefix + "'");
                    return validPrefix;
                } else {
                    System.out.println("无法提取有效事件前缀");
                }
            } else {
                System.out.println("事件前缀检测失败 - afterAt: '" + afterAt + "' 包含等号");
            }
        } else {
            System.out.println("事件前缀检测失败 - 未找到@符号");
        }
        return null;
    }
    
    /**
     * 提取有效的事件名前缀
     */
    private String extractValidEventPrefix(String text) {
        if (text == null || text.isEmpty()) {
            return text; // 返回空字符串而不是null
        }
        
        System.out.println("提取事件前缀 - 原始文本: '" + text + "'");
        
        // 直接替换掉IntelliJ IDEA的后缀
        String cleanText = text.replace("IntellijIdeaRulezzz", "");
        System.out.println("清理后的文本: '" + cleanText + "'");
        
        // 如果清理后为空，返回空字符串
        if (cleanText.isEmpty()) {
            return cleanText;
        }
        
        // 查找第一个非字母数字字符的位置
        for (int i = 0; i < cleanText.length(); i++) {
            char c = cleanText.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-') {
                if (i > 0) {
                    String result = cleanText.substring(0, i);
                    System.out.println("提取到事件前缀: '" + result + "'");
                    return result;
                }
                return null;
            }
        }
        
        // 如果全部都是有效字符，返回整个字符串
        System.out.println("返回完整事件前缀: '" + cleanText + "'");
        return cleanText;
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
        COMPONENT, ATTRIBUTE, EVENT
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
