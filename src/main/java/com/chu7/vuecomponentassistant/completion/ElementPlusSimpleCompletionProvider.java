package com.chu7.vuecomponentassistant.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Element Plus 简化补全提供者
 * 用于测试基本功能
 */
public class ElementPlusSimpleCompletionProvider extends CompletionProvider<CompletionParameters> {
    
    private final ElementPlusComponentProvider componentProvider;
    
    public ElementPlusSimpleCompletionProvider() {
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
        
        // 检查是否是Vue或HTML文件
        if (!isVueFile(file)) {
            return;
        }
        
        // 获取当前输入的前缀
        String prefix = getPrefix(parameters);
        
        // 为所有位置提供组件补全
        addComponentCompletions(result, prefix);
    }
    
    /**
     * 获取当前输入的前缀
     */
    private String getPrefix(CompletionParameters parameters) {
        String text = parameters.getPosition().getText();
        return text != null ? text : "";
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
        int maxCount = 20;
        
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
     * 检查是否是Vue文件
     */
    private boolean isVueFile(PsiFile file) {
        if (file == null) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
    }
}
