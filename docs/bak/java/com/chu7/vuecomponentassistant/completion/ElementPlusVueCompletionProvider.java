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
 * Element Plus Vue模板补全提供者
 * 专门处理Vue模板中的智能补全
 */
public class ElementPlusVueCompletionProvider extends CompletionProvider<CompletionParameters> {

    private final ElementPlusComponentProvider componentProvider;

    // 匹配Vue模板中的各种模式
    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern EVENT_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9-]*)\\s*=");

    public ElementPlusVueCompletionProvider() {
        this.componentProvider = new ElementPlusComponentProvider();
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
                addComponentCompletions(result);
                break;
            case ATTRIBUTE:
                addAttributeCompletions(result, completionContext.getCurrentComponent());
                break;
            case EVENT:
                addEventCompletions(result, completionContext.getCurrentComponent());
                break;
            case SLOT:
                addSlotCompletions(result, completionContext.getCurrentComponent());
                break;
        }
    }

    /**
     * 添加组件补全
     */
    private void addComponentCompletions(CompletionResultSet result) {
        List<ElementPlusComponent> components = componentProvider.getAllComponents();

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
    private void addAttributeCompletions(CompletionResultSet result, String componentName) {
        if (componentName == null) {
            return;
        }

        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }

        for (ElementPlusProp prop : component.getProps()) {
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
    private void addEventCompletions(CompletionResultSet result, String componentName) {
        if (componentName == null) {
            return;
        }

        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }

        for (ElementPlusEvent event : component.getEvents()) {
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
    private void addSlotCompletions(CompletionResultSet result, String componentName) {
        if (componentName == null) {
            return;
        }

        ElementPlusComponent component = componentProvider.getComponent(componentName);
        if (component == null) {
            return;
        }

        for (ElementPlusSlot slot : component.getSlots()) {
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
            return new CompletionContext(CompletionType.COMPONENT, null);
        }

        // 检查是否在属性位置
        Matcher attrMatcher = ATTRIBUTE_PATTERN.matcher(beforeText);
        if (attrMatcher.find()) {
            String currentComponent = getCurrentComponent(beforeText);
            return new CompletionContext(CompletionType.ATTRIBUTE, currentComponent);
        }

        // 检查是否在事件位置
        Matcher eventMatcher = EVENT_PATTERN.matcher(beforeText);
        if (eventMatcher.find()) {
            String currentComponent = getCurrentComponent(beforeText);
            return new CompletionContext(CompletionType.EVENT, currentComponent);
        }

        // 检查是否在插槽位置
        if (beforeText.contains("#")) {
            String currentComponent = getCurrentComponent(beforeText);
            return new CompletionContext(CompletionType.SLOT, currentComponent);
        }

        return new CompletionContext(CompletionType.COMPONENT, null);
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
        return lastComponent;
    }

    /**
     * 检查是否是Vue文件
     */
    private boolean isVueFile(PsiFile file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
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

        public CompletionContext(CompletionType type, String currentComponent) {
            this.type = type;
            this.currentComponent = currentComponent;
        }

        public CompletionType getType() {
            return type;
        }

        public String getCurrentComponent() {
            return currentComponent;
        }
    }
}
