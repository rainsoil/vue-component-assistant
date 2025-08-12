package com.chu7.vuecomponentassistant.completion;

import com.chu7.vuecomponentassistant.remote.model.ComponentInfo;
import com.chu7.vuecomponentassistant.completion.strategy.CompletionStrategyManager;
import com.chu7.vuecomponentassistant.completion.context.CompletionContextAnalyzer;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 统一补全提供者 - 基于策略模式
 * 
 * 整合了所有补全功能，包括：
 * - 组件补全
 * - 属性补全
 * - 事件补全
 * - 插槽补全
 * - 智能上下文分析
 * - 缓存优化
 *
 * @author VueKit Team
 * @version 3.0.0
 */
public class UnifiedCompletionProvider extends CompletionProvider<CompletionParameters> {

    /**
     * 组件数据提供者
     */
    private ComponentProvider componentProvider;
    
    /**
     * 补全策略管理器
     */
    private final CompletionStrategyManager strategyManager;
    
    /**
     * 上下文分析器
     */
    private final CompletionContextAnalyzer contextAnalyzer;
    
    /**
     * 缓存管理器
     */
    private final CompletionCache cache;

    public UnifiedCompletionProvider() {
        this.strategyManager = CompletionStrategyManager.getInstance();
        this.contextAnalyzer = new CompletionContextAnalyzer();
        this.cache = new CompletionCache();
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

        // 初始化组件提供者
        componentProvider = new ComponentProvider(project);

        // 获取当前元素和文件
        PsiElement element = parameters.getPosition();
        PsiFile file = element.getContainingFile();

        if (file == null || !isVueFile(file)) {
            return;
        }

        // 分析上下文
        CompletionContext completionContext = contextAnalyzer.analyzeContext(file, element);
        
        // 尝试从缓存获取结果
        CompletionCache.CacheKey cacheKey = new CompletionCache.CacheKey(
            file, element, completionContext.getPrefix(), 
            componentProvider.getLibraryDisplayName()
        );
        
        CompletionCache.CachedCompletionResult cachedResult = 
            CompletionCache.getCompletionResult(cacheKey);
        
        if (cachedResult != null) {
            // 使用缓存结果
            addCachedCompletions(result, cachedResult);
            // 暂时注释掉return，确保策略管理器能够执行
            // return;
        }
        
        // 使用策略管理器执行补全
        strategyManager.executeCompletion(
            completionContext, project, file, element, componentProvider, result
        );
        
        // 缓存结果
        CompletionCache.cacheCompletionResult(cacheKey, result);
    }
    

    
    /**
     * 添加缓存的补全结果
     */
    private void addCachedCompletions(CompletionResultSet result, CompletionCache.CachedCompletionResult cachedResult) {
        // 由于缓存存储的是Object类型，这里需要根据实际情况处理
        // 暂时跳过缓存结果的添加，直接使用策略管理器执行补全
        // TODO: 实现更完善的缓存结果处理
    }

    /**
     * 检查是否为Vue文件
     */
    private boolean isVueFile(PsiFile file) {
        return file != null && file.getName().endsWith(".vue");
    }
} 