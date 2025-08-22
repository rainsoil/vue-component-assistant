package com.chu7.vuecomponentassistant.documentation;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;

/**
 * 测试文档提供者 - 用于调试文档提供者是否被正确调用
 */
public class TestDocumentationProvider extends AbstractDocumentationProvider {

    private static final Logger LOG = VueKitLogger.getLogger(TestDocumentationProvider.class);
    
    static {
        // 使用System.out确保日志一定输出
        System.out.println("=== TestDocumentationProvider 类被加载 ===");
        LOG.info("=== TestDocumentationProvider 类被加载 ===");
    }
    
    /**
     * 构造函数
     */
    public TestDocumentationProvider() {
        System.out.println("=== TestDocumentationProvider 实例被创建 ===");
        LOG.info("=== TestDocumentationProvider 实例被创建 ===");
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        System.out.println("=== TestDocumentationProvider.generateDoc 被调用 ===");
        System.out.println("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        System.out.println("originalElement = " + (originalElement != null ? originalElement.getClass().getSimpleName() : "null"));
        
        LOG.info("=== TestDocumentationProvider.generateDoc 被调用 ===");
        LOG.info("element = " + (element != null ? element.getClass().getSimpleName() : "null"));
        LOG.info("originalElement = " + (originalElement != null ? originalElement.getClass().getSimpleName() : "null"));
        
        if (element != null) {
            System.out.println("element text = " + element.getText());
            System.out.println("element full class name = " + element.getClass().getName());
            LOG.info("element text = " + element.getText());
            LOG.info("element full class name = " + element.getClass().getName());
        }
        
        // 返回简单的测试文档
        String result = "<h1>测试文档提供者工作正常！</h1><p>元素类型: " + 
               (element != null ? element.getClass().getSimpleName() : "null") + "</p>";
        System.out.println("返回文档: " + result);
        return result;
    }

    @Override
    public java.util.List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        System.out.println("=== TestDocumentationProvider.getUrlFor 被调用 ===");
        LOG.info("=== TestDocumentationProvider.getUrlFor 被调用 ===");
        return java.util.Collections.emptyList();
    }
}
