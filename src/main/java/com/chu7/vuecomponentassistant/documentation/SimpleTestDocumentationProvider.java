package com.chu7.vuecomponentassistant.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 最简单的测试文档提供者 - 用于调试
 */
public class SimpleTestDocumentationProvider extends AbstractDocumentationProvider {

    static {
        System.out.println("=== SimpleTestDocumentationProvider 类被加载 ===");
    }

    public SimpleTestDocumentationProvider() {
        System.out.println("=== SimpleTestDocumentationProvider 实例被创建 ===");
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        System.out.println("=== SimpleTestDocumentationProvider.generateDoc 被调用 ===");
        System.out.println("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        System.out.println("element full class name = " + (element != null ? element.getClass().getName() : "null"));
        
        if (element != null) {
            System.out.println("element text = " + element.getText());
            System.out.println("element parent = " + (element.getParent() != null ? element.getParent().getClass().getSimpleName() : "null"));
            
            // 检查是否是 HtmlTag
            if (element instanceof com.intellij.psi.html.HtmlTag) {
                com.intellij.psi.html.HtmlTag htmlTag = (com.intellij.psi.html.HtmlTag) element;
                System.out.println("这是一个 HtmlTag! 标签名: " + htmlTag.getName());
                return "<h1>HTML标签文档</h1><p>标签名: " + htmlTag.getName() + "</p>";
            }
        }
        
        return "<h1>简单测试文档</h1><p>元素: " + 
               (element != null ? element.getClass().getSimpleName() : "null") + "</p>";
    }

    @Override
    public @Nullable List<String> getUrlFor(PsiElement element, @Nullable PsiElement originalElement) {
        System.out.println("=== SimpleTestDocumentationProvider.getUrlFor 被调用 ===");
        return Collections.emptyList();
    }
}
