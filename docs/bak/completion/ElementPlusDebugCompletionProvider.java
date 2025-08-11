package bak.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
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
 * Element Plus 调试补全提供者
 * 添加详细日志用于调试事件补全问题
 */
public class ElementPlusDebugCompletionProvider extends CompletionProvider<CompletionParameters> {
    
    private final ElementPlusComponentProvider componentProvider;
    
    public ElementPlusDebugCompletionProvider() {
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
            components = componentProvider.searchComponents(prefix);
        } else {
            components = componentProvider.getAllComponents();
        }
        
        int count = 0;
        int maxCount = 10; // 限制数量便于调试
        
        for (ElementPlusComponent component : components) {
            if (count >= maxCount) break;
            
            LookupElementBuilder element = LookupElementBuilder.create(component.getName())
                    .withTypeText("Element Plus Component")
                    .withTailText(" " + component.getDescription())
                    .withInsertHandler((insertContext, item) -> {
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        editor.getDocument().insertString(offset, "></" + component.getName() + ">");
                        editor.getCaretModel().moveToOffset(offset);
                    });
            
            result.addElement(element);
            count++;
        }
        
        System.out.println("添加了 " + count + " 个组件补全");
    }
    
    /**
     * 添加属性补全
     */
    private void addAttributeCompletions(CompletionResultSet result, String componentName, String prefix) {
        if (componentName == null) {
            System.out.println("属性补全: 组件名为空");
            return;
        }
        
        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            System.out.println("属性补全: 找不到组件 " + componentName);
            return;
        }
        
        System.out.println("属性补全: 为组件 " + componentName + " 添加属性");
        System.out.println("组件属性数量: " + component.getProps().size());
        
        int count = 0;
        for (ElementPlusProp prop : component.getProps()) {
            // 根据前缀过滤属性
            if (prefix != null && !prefix.isEmpty() && !prop.getName().toLowerCase().contains(prefix.toLowerCase())) {
                continue;
            }
            
            LookupElementBuilder propElement = LookupElementBuilder.create(prop.getName())
                    .withTypeText("Property")
                    .withTailText(" " + prop.getDescription())
                    .withInsertHandler((insertContext, item) -> {
                        Editor editor = insertContext.getEditor();
                        int offset = insertContext.getTailOffset();
                        String defaultValue = prop.getDefaultValueAsString();
                        editor.getDocument().insertString(offset, "=\"" + defaultValue + "\"");
                        editor.getCaretModel().moveToOffset(offset - 1);
                    });
            
            result.addElement(propElement);
            count++;
        }
        
        System.out.println("添加了 " + count + " 个属性补全");
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
        
        int count = 0;
        for (ElementPlusEvent event : component.getEvents()) {
            // 根据前缀过滤事件
            if (prefix != null && !prefix.isEmpty() && !event.getName().toLowerCase().contains(prefix.toLowerCase())) {
                continue;
            }
            
            LookupElementBuilder eventElement = LookupElementBuilder.create("@" + event.getName())
                    .withTypeText("Event")
                    .withTailText(" " + event.getDescription())
                    .withInsertHandler((insertContext, item) -> {
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
        
        System.out.println("分析上下文 - beforeText: " + beforeText);
        
        // 检查是否在组件标签位置
        if (beforeText.endsWith("<") || beforeText.matches(".*<\\s*$")) {
            System.out.println("检测到组件位置");
            return new CompletionContext(CompletionType.COMPONENT, null, null);
        }
        
        // 检查是否在事件位置（在组件标签内，@开头）
        String currentComponent = getCurrentComponent(beforeText);
        System.out.println("当前组件: " + currentComponent);
        System.out.println("是否在组件标签内: " + isInComponentTag(beforeText));
        System.out.println("是否包含@: " + beforeText.contains("@"));
        
        if (currentComponent != null && isInComponentTag(beforeText) && beforeText.contains("@")) {
            String eventPrefix = getEventPrefix(beforeText);
            System.out.println("事件前缀: " + eventPrefix);
            if (eventPrefix != null) {
                System.out.println("检测到事件位置");
                return new CompletionContext(CompletionType.EVENT, currentComponent, eventPrefix);
            }
        }
        
        // 检查是否在属性位置（在组件标签内，有空格但没有=）
        if (currentComponent != null && isInComponentTag(beforeText)) {
            String prefix = getAttributePrefix(beforeText);
            System.out.println("属性前缀: " + prefix);
            if (prefix != null) {
                System.out.println("检测到属性位置");
                return new CompletionContext(CompletionType.ATTRIBUTE, currentComponent, prefix);
            }
        }
        
        // 检查是否在组件名称位置
        String componentPrefix = getComponentPrefix(beforeText);
        if (componentPrefix != null) {
            System.out.println("检测到组件名称位置，前缀: " + componentPrefix);
            return new CompletionContext(CompletionType.COMPONENT, null, componentPrefix);
        }
        
        System.out.println("默认返回组件位置");
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
        
        Pattern elementPlusPattern = Pattern.compile("el-[a-zA-Z-]+");
        if (lastComponent != null && elementPlusPattern.matcher(lastComponent).matches()) {
            return lastComponent;
        }
        
        return null;
    }
    
    /**
     * 检查是否在组件标签内
     */
    private boolean isInComponentTag(String beforeText) {
        int lastOpenTag = beforeText.lastIndexOf('<');
        int lastCloseTag = beforeText.lastIndexOf('>');
        
        if (lastOpenTag > lastCloseTag) {
            String tagContent = beforeText.substring(lastOpenTag);
            return tagContent.contains("el-");
        }
        
        return false;
    }
    
    /**
     * 获取属性前缀
     */
    private String getAttributePrefix(String beforeText) {
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            
            if (afterOpenTag.contains(" ")) {
                int lastSpace = afterOpenTag.lastIndexOf(' ');
                if (lastSpace >= 0) {
                    String afterLastSpace = afterOpenTag.substring(lastSpace + 1);
                    
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
        if (lastAt >= 0) {
            String afterAt = beforeText.substring(lastAt + 1);
            
            // 如果没有=，说明正在输入事件名
            if (!afterAt.contains("=")) {
                // 检查@符号后是否为空或者是有效的事件名
                if (afterAt.isEmpty() || afterAt.matches("^[a-zA-Z][a-zA-Z0-9-]*$")) {
                    return afterAt;
                }
            }
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
        
        int lastOpenTag = beforeText.lastIndexOf('<');
        if (lastOpenTag >= 0) {
            String afterOpenTag = beforeText.substring(lastOpenTag + 1);
            if (!afterOpenTag.contains(" ") && !afterOpenTag.contains(">")) {
                return afterOpenTag;
            }
        }
        
        return null;
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
