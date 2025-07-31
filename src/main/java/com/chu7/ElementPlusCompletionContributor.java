package com.chu7;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ElementPlusCompletionContributor extends CompletionContributor {
    public ElementPlusCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(com.intellij.lang.xml.XMLLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        result.addElement(LookupElementBuilder.create("el-button"));
                        result.addElement(LookupElementBuilder.create("el-input"));
                        result.addElement(LookupElementBuilder.create("el-table"));
                        result.addElement(LookupElementBuilder.create("el-form"));
                    }
                }
        );
    }
}