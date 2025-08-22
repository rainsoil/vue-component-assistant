package com.chu7.vuecomponentassistant.documentation;

import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * 测试PSI引用贡献者 - 用于调试
 */
public class TestPsiReferenceContributor extends PsiReferenceContributor {

    static {
        System.out.println("=== TestPsiReferenceContributor 类被加载 ===");
    }

    public TestPsiReferenceContributor() {
        System.out.println("=== TestPsiReferenceContributor 实例被创建 ===");
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        System.out.println("=== TestPsiReferenceContributor.registerReferenceProviders 被调用 ===");
        System.out.println("=== TestPsiReferenceContributor 注册完成 ===");
    }
}
