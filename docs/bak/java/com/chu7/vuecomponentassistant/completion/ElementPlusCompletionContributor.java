package com.chu7.vuecomponentassistant.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Element Plus 组件补全贡献者
 * 提供智能的组件、属性和事件补全功能
 */
public class ElementPlusCompletionContributor extends CompletionContributor {

    private final ElementPlusComponentProvider componentProvider;
    private final ElementPlusContextAnalyzer contextAnalyzer;

    public ElementPlusCompletionContributor() {
        this.componentProvider = new ElementPlusComponentProvider();
        this.contextAnalyzer = new ElementPlusContextAnalyzer();
        
        // 注册补全提供者
        registerComponentCompletions();
        registerAttributeCompletions();
        registerEventCompletions();
    }

    /**
     * 注册组件补全
     */
    private void registerComponentCompletions() {
        // 使用测试版本，包含调试日志
        extend(CompletionType.BASIC, 
               PlatformPatterns.psiElement(),
               new ElementPlusTestCompletionProvider());
    }

    /**
     * 注册属性补全
     */
    private void registerAttributeCompletions() {
        // 属性补全已经集成到VueCompletionProvider中
    }

    /**
     * 注册事件补全
     */
    private void registerEventCompletions() {
        // 事件补全已经集成到VueCompletionProvider中
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
