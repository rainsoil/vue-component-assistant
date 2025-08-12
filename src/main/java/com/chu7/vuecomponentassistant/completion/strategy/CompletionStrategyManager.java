package com.chu7.vuecomponentassistant.completion.strategy;

import com.chu7.vuecomponentassistant.completion.*;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 补全策略管理器
 * 
 * 负责管理和协调所有的补全策略，提供统一的补全入口：
 * - 策略注册和管理
 * - 策略选择和执行
 * - 性能监控和统计
 * - 策略优先级排序
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class CompletionStrategyManager {
    
    // 单例实例
    private static volatile CompletionStrategyManager instance;
    
    // 策略注册表
    private final Map<CompletionContext.CompletionType, List<CompletionStrategy>> strategies;
    
    // 已排序的策略缓存
    private final Map<CompletionContext.CompletionType, List<CompletionStrategy>> sortedStrategiesCache;
    
    private CompletionStrategyManager() {
        this.strategies = new ConcurrentHashMap<>();
        this.sortedStrategiesCache = new ConcurrentHashMap<>();
        
        // 注册默认策略
        registerDefaultStrategies();
        
        System.out.println("=== 补全策略管理器初始化完成 ===");
    }
    
    /**
     * 获取单例实例
     */
    public static CompletionStrategyManager getInstance() {
        if (instance == null) {
            synchronized (CompletionStrategyManager.class) {
                if (instance == null) {
                    instance = new CompletionStrategyManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 注册默认策略
     */
    private void registerDefaultStrategies() {
        // 注册事件补全策略
        registerStrategy(new EventCompletionStrategy());
        
        System.out.println("✅ 默认策略注册完成");
    }
    
    /**
     * 注册补全策略
     * 
     * @param strategy 要注册的策略
     */
    public void registerStrategy(@NotNull CompletionStrategy strategy) {
        CompletionContext.CompletionType type = strategy.getSupportedType();
        
        strategies.computeIfAbsent(type, k -> new ArrayList<>()).add(strategy);
        
        // 清除缓存，确保下次获取时重新排序
        sortedStrategiesCache.remove(type);
        
        System.out.println("✅ 注册策略: " + strategy.getStrategyName() + " -> " + type);
    }
    
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
    public void executeCompletion(@NotNull CompletionContext context,
                                @NotNull Project project,
                                @NotNull PsiFile file,
                                @NotNull PsiElement element,
                                @NotNull ComponentProvider componentProvider,
                                @NotNull CompletionResultSet result) {
        
        CompletionContext.CompletionType type = context.getType();
        
        System.out.println("=== 执行补全策略 ===");
        System.out.println("补全类型: " + type);
        System.out.println("当前组件: " + context.getCurrentComponent());
        System.out.println("前缀: " + context.getPrefix());
        
        // 获取该类型的所有策略
        List<CompletionStrategy> typeStrategies = getStrategiesForType(type);
        
        if (typeStrategies.isEmpty()) {
            System.out.println("❌ 没有找到类型 " + type + " 的策略");
            return;
        }
        
        System.out.println("✅ 找到 " + typeStrategies.size() + " 个策略");
        
        // 按优先级排序并执行策略
        for (CompletionStrategy strategy : typeStrategies) {
            if (!strategy.isEnabled()) {
                System.out.println("⏭️ 策略已禁用: " + strategy.getStrategyName());
                continue;
            }
            
            if (strategy.canHandle(context, project, file, element)) {
                System.out.println("🔄 执行策略: " + strategy.getStrategyName());
                try {
                    strategy.complete(context, project, file, element, componentProvider, result);
                    System.out.println("✅ 策略执行成功: " + strategy.getStrategyName());
                } catch (Exception e) {
                    System.out.println("❌ 策略执行失败: " + strategy.getStrategyName() + ", 错误: " + e.getMessage());
                }
            } else {
                System.out.println("⏭️ 策略无法处理: " + strategy.getStrategyName());
            }
        }
        
        System.out.println("=== 补全策略执行完成 ===");
    }
    
    /**
     * 获取指定类型的所有策略
     */
    @NotNull
    private List<CompletionStrategy> getStrategiesForType(@NotNull CompletionContext.CompletionType type) {
        return sortedStrategiesCache.computeIfAbsent(type, k -> {
            List<CompletionStrategy> typeStrategies = strategies.getOrDefault(type, new ArrayList<>());
            
            // 按优先级排序（数值越大优先级越高）
            typeStrategies.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
            
            System.out.println("策略排序完成，类型: " + type + ", 数量: " + typeStrategies.size());
            for (CompletionStrategy strategy : typeStrategies) {
                System.out.println("  - " + strategy.getStrategyName() + " (优先级: " + strategy.getPriority() + ")");
            }
            
            return typeStrategies;
        });
    }
    
    /**
     * 获取所有已注册的策略
     */
    @NotNull
    public Map<CompletionContext.CompletionType, List<CompletionStrategy>> getAllStrategies() {
        return new HashMap<>(strategies);
    }
    
    /**
     * 清除策略缓存
     */
    public void clearCache() {
        sortedStrategiesCache.clear();
        System.out.println("✅ 策略缓存已清除");
    }
}