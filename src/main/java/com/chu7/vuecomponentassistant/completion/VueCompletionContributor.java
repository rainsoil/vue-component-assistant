package com.chu7.vuecomponentassistant.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import org.jetbrains.annotations.NotNull;

/**
 * Vue代码补全贡献者
 * 
 * 功能说明：
 * - 继承CompletionContributor，符合IntelliJ插件架构要求
 * - 注册UnifiedCompletionProvider作为Vue和HTML的补全提供者
 * - 支持组件、属性、事件、插槽的智能补全
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class VueCompletionContributor extends CompletionContributor {

    public VueCompletionContributor() {
        // 注册Vue和HTML文件补全
        extend(CompletionType.BASIC, 
               PlatformPatterns.psiElement().inside(PlatformPatterns.psiFile()),
               new UnifiedCompletionProvider());
    }
}
