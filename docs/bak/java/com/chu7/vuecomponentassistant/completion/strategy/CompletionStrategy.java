package com.chu7.vuecomponentassistant.completion.strategy;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * 补全策略接口
 * 
 * 定义了所有补全策略的统一接口，支持：
 * - 组件补全策略
 * - 属性补全策略
 * - 事件补全策略
 * - 插槽补全策略
 * 
 * 每种策略负责处理特定类型的补全需求，提供模块化和可扩展的架构。
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public interface CompletionStrategy {
    
    /**
     * 获取策略支持的补全类型
     * 
     * @return 支持的补全类型
     */
    @NotNull
    CompletionContext.CompletionType getSupportedType();
    
    /**
     * 检查是否可以处理当前上下文
     * 
     * @param context 补全上下文
     * @param project 当前项目
     * @param file 当前文件
     * @param element 当前元素
     * @return 如果可以处理返回true，否则返回false
     */
    boolean canHandle(@NotNull CompletionContext context, 
                     @NotNull Project project,
                     @NotNull PsiFile file, 
                     @NotNull PsiElement element);
    
    /**
     * 执行补全
     * 
     * @param context 补全上下文
     * @param project 当前项目
     * @param file 当前文件
     * @param element 当前元素
     * @param componentProvider 组件数据提供者
     * @param result 补全结果集
     */
    void complete(@NotNull CompletionContext context,
                 @NotNull Project project,
                 @NotNull PsiFile file,
                 @NotNull PsiElement element,
                 @NotNull ComponentProvider componentProvider,
                 @NotNull CompletionResultSet result);
    
    /**
     * 获取策略优先级
     * 数值越大优先级越高
     * 
     * @return 优先级数值
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    @NotNull
    String getStrategyName();
    
    /**
     * 获取策略描述
     * 
     * @return 策略描述
     */
    @NotNull
    default String getDescription() {
        return "补全策略: " + getStrategyName();
    }
    
    /**
     * 策略是否启用
     * 可以根据设置或其他条件动态决定
     * 
     * @return 如果启用返回true，否则返回false
     */
    default boolean isEnabled() {
        return true;
    }
}