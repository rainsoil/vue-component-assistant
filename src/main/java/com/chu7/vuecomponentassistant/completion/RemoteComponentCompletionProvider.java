package com.chu7.vuecomponentassistant.completion;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 远程组件补全提供者
 * 
 * 集成远程组件库管理器，提供：
 * - 从远程组件库获取组件数据
 * - 组件补全
 * - 属性补全
 * - 事件补全
 * - 插槽补全
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class RemoteComponentCompletionProvider extends CompletionProvider<CompletionParameters> {

    private static final Logger LOG = Logger.getInstance(RemoteComponentCompletionProvider.class);
    
    // 正则表达式模式
    private static final Pattern COMPONENT_TAG_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern EVENT_PATTERN = Pattern.compile("@([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    private static final Pattern SLOT_PATTERN = Pattern.compile("#([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
    
    private ComponentLibraryManager libraryManager;
    
    public RemoteComponentCompletionProvider() {
        // 组件库管理器将在 addCompletions 中初始化
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
        
        // 初始化组件库管理器
        if (libraryManager == null) {
            libraryManager = new ComponentLibraryManager();
        }
        
        // 获取当前元素和文件
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();
        
        if (file == null) {
            return;
        }
        
        // 检查文件类型
        if (!isVueFile(file)) {
            return;
        }
        
        // 分析上下文
        CompletionContext completionContext = analyzeContext(file, element);
        
        // 生成补全选项
        List<LookupElementBuilder> completions = generateCompletions(completionContext);
        
        // 添加补全选项
        for (LookupElementBuilder completion : completions) {
            result.addElement(completion);
        }
    }
    
    /**
     * 分析补全上下文
     */
    private CompletionContext analyzeContext(PsiFile file, PsiElement element) {
        String text = file.getText();
        int offset = element.getTextOffset();
        
        // 获取光标前的文本
        String beforeCursor = text.substring(0, offset);
        
        // 检查是否在组件标签内
        Matcher componentMatcher = COMPONENT_TAG_PATTERN.matcher(beforeCursor);
        if (componentMatcher.find()) {
            String componentName = componentMatcher.group(1);
            
            // 检查是否在属性位置
            Matcher attributeMatcher = ATTRIBUTE_PATTERN.matcher(beforeCursor);
            if (attributeMatcher.find()) {
                return new CompletionContext(CompletionContext.CompletionType.ATTRIBUTE, componentName, null);
            }
            
            // 检查是否在事件位置
            Matcher eventMatcher = EVENT_PATTERN.matcher(beforeCursor);
            if (eventMatcher.find()) {
                return new CompletionContext(CompletionContext.CompletionType.EVENT, componentName, null);
            }
            
            // 检查是否在插槽位置
            Matcher slotMatcher = SLOT_PATTERN.matcher(beforeCursor);
            if (slotMatcher.find()) {
                return new CompletionContext(CompletionContext.CompletionType.SLOT, componentName, null);
            }
            
            return new CompletionContext(CompletionContext.CompletionType.COMPONENT, componentName, null);
        }
        
        return new CompletionContext(CompletionContext.CompletionType.COMPONENT, null, null);
    }
    
    /**
     * 生成补全选项
     */
    private List<LookupElementBuilder> generateCompletions(CompletionContext context) {
        switch (context.getType()) {
            case COMPONENT:
                return generateComponentCompletions(context.getPrefix());
            case ATTRIBUTE:
                return generateAttributeCompletions(context.getCurrentComponent(), context.getPrefix());
            case EVENT:
                return generateEventCompletions(context.getCurrentComponent(), context.getPrefix());
            case SLOT:
                return generateSlotCompletions(context.getCurrentComponent(), context.getPrefix());
            default:
                return generateComponentCompletions(context.getPrefix());
        }
    }
    
    /**
     * 生成组件补全选项
     */
    private List<LookupElementBuilder> generateComponentCompletions(String prefix) {
        List<LookupElementBuilder> completions = new ArrayList<>();
        
        try {
            // 从所有组件库获取组件
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : libraries) {
                if (library.getComponents() != null) {
                    for (ComponentInfo component : library.getComponents()) {
                        // 根据前缀过滤
                        if (prefix == null || prefix.isEmpty() || 
                            component.getName().toLowerCase().contains(prefix.toLowerCase())) {
                            
                            LookupElementBuilder lookupElement = LookupElementBuilder.create(component.getName())
                                .withPresentableText(component.getName())
                                .withTypeText(component.getDisplayName())
                                .withTailText(" (" + library.getDisplayName() + ")")
                                .withInsertHandler((context, item) -> {
                                    // 插入完整的组件标签
                                    Editor editor = context.getEditor();
                                    String insertText = component.getName() + "></" + component.getName() + ">";
                                    editor.getDocument().insertString(editor.getCaretModel().getOffset(), insertText);
                                    editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + component.getName().length() + 1);
                                });
                            
                            completions.add(lookupElement);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("生成组件补全选项失败", e);
        }
        
        return completions;
    }
    
    /**
     * 生成属性补全选项
     */
    private List<LookupElementBuilder> generateAttributeCompletions(String componentName, String prefix) {
        List<LookupElementBuilder> completions = new ArrayList<>();
        
        try {
            // 查找组件
            ComponentInfo component = findComponent(componentName);
            if (component != null && component.getProps() != null) {
                for (ComponentInfo.ComponentProp prop : component.getProps()) {
                    // 根据前缀过滤
                    if (prefix == null || prefix.isEmpty() || 
                        prop.getName().toLowerCase().contains(prefix.toLowerCase())) {
                        
                        LookupElementBuilder lookupElement = LookupElementBuilder.create(prop.getName())
                            .withPresentableText(prop.getName())
                            .withTypeText(prop.getType())
                            .withTailText(prop.getDescription())
                            .withInsertHandler((context, item) -> {
                                // 插入属性
                                Editor editor = context.getEditor();
                                String insertText = prop.getName() + "=\"\"";
                                editor.getDocument().insertString(editor.getCaretModel().getOffset(), insertText);
                                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 1);
                            });
                        
                        completions.add(lookupElement);
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("生成属性补全选项失败", e);
        }
        
        return completions;
    }
    
    /**
     * 生成事件补全选项
     */
    private List<LookupElementBuilder> generateEventCompletions(String componentName, String prefix) {
        List<LookupElementBuilder> completions = new ArrayList<>();
        
        try {
            // 查找组件
            ComponentInfo component = findComponent(componentName);
            if (component != null && component.getEvents() != null) {
                for (ComponentInfo.ComponentEvent event : component.getEvents()) {
                    // 根据前缀过滤
                    if (prefix == null || prefix.isEmpty() || 
                        event.getName().toLowerCase().contains(prefix.toLowerCase())) {
                        
                        LookupElementBuilder lookupElement = LookupElementBuilder.create(event.getName())
                            .withPresentableText(event.getName())
                            .withTypeText("事件")
                            .withTailText(event.getDescription())
                            .withInsertHandler((context, item) -> {
                                // 插入事件
                                Editor editor = context.getEditor();
                                String insertText = event.getName() + "=\"\"";
                                editor.getDocument().insertString(editor.getCaretModel().getOffset(), insertText);
                                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 1);
                            });
                        
                        completions.add(lookupElement);
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("生成事件补全选项失败", e);
        }
        
        return completions;
    }
    
    /**
     * 生成插槽补全选项
     */
    private List<LookupElementBuilder> generateSlotCompletions(String componentName, String prefix) {
        List<LookupElementBuilder> completions = new ArrayList<>();
        
        try {
            // 查找组件
            ComponentInfo component = findComponent(componentName);
            if (component != null && component.getSlots() != null) {
                for (ComponentInfo.ComponentSlot slot : component.getSlots()) {
                    // 根据前缀过滤
                    if (prefix == null || prefix.isEmpty() || 
                        slot.getName().toLowerCase().contains(prefix.toLowerCase())) {
                        
                        LookupElementBuilder lookupElement = LookupElementBuilder.create(slot.getName())
                            .withPresentableText(slot.getName())
                            .withTypeText("插槽")
                            .withTailText(slot.getDescription())
                            .withInsertHandler((context, item) -> {
                                // 插入插槽
                                Editor editor = context.getEditor();
                                String insertText = slot.getName() + "=\"\"";
                                editor.getDocument().insertString(editor.getCaretModel().getOffset(), insertText);
                                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 1);
                            });
                        
                        completions.add(lookupElement);
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("生成插槽补全选项失败", e);
        }
        
        return completions;
    }
    
    /**
     * 查找组件
     */
    private ComponentInfo findComponent(String componentName) {
        try {
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            
            for (ComponentLibrary library : libraries) {
                if (library.getComponents() != null) {
                    for (ComponentInfo component : library.getComponents()) {
                        if (component.getName().equals(componentName)) {
                            return component;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            LOG.error("查找组件失败: " + componentName, e);
        }
        
        return null;
    }
    
    /**
     * 检查是否为Vue文件
     */
    private boolean isVueFile(PsiFile file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".js") || fileName.endsWith(".ts");
    }
} 