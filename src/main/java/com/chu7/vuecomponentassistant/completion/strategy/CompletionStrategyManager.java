package com.chu7.vuecomponentassistant.completion.strategy;

import com.chu7.vuecomponentassistant.completion.*;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.diagnostic.Logger;
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
    
    private static final Logger LOG = VueKitLogger.getLogger(CompletionStrategyManager.class);
    
    // 单例实例
    private static volatile CompletionStrategyManager instance;
    
    // 策略注册表
    private final Map<CompletionContext.CompletionType, List<CompletionStrategy>> strategies;
    
    // 策略统计信息
    private final Map<String, StrategyStats> strategyStats;
    
    // 已排序的策略缓存
    private final Map<CompletionContext.CompletionType, List<CompletionStrategy>> sortedStrategiesCache;
    
    private CompletionStrategyManager() {
        this.strategies = new ConcurrentHashMap<>();
        this.strategyStats = new ConcurrentHashMap<>();
        this.sortedStrategiesCache = new ConcurrentHashMap<>();
        
        // 注册默认策略
        registerDefaultStrategies();
        
        VueKitLogger.info(LOG, "补全策略管理器初始化完成");
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
        // 注册组件补全策略
        registerStrategy(new ComponentCompletionStrategy());
        
        // 注册属性补全策略
        registerStrategy(new AttributeCompletionStrategy());
        
        // 注册事件补全策略
        registerStrategy(new EventCompletionStrategy());
        
        VueKitLogger.debug(LOG, "默认策略注册完成");
    }
    
    /**
     * 注册补全策略
     * 
     * @param strategy 要注册的策略
     */
    public void registerStrategy(@NotNull CompletionStrategy strategy) {
        CompletionContext.CompletionType type = strategy.getSupportedType();
        
        strategies.computeIfAbsent(type, k -> new ArrayList<>()).add(strategy);
        
        // 清除缓存，强制重新排序
        sortedStrategiesCache.remove(type);
        
        VueKitLogger.debug(LOG, "注册策略: " + strategy.getStrategyName() + " -> " + type);
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
        
        long startTime = System.currentTimeMillis();
        CompletionContext.CompletionType type = context.getType();
        
        VueKitLogger.debug(LOG, "执行补全，类型: " + type + ", 前缀: " + context.getPrefix());
        
        // 获取适用的策略
        List<CompletionStrategy> applicableStrategies = getApplicableStrategies(type);
        
        if (applicableStrategies.isEmpty()) {
            VueKitLogger.debug(LOG, "没有找到适用的策略: " + type);
            return;
        }
        
        // 执行策略
        for (CompletionStrategy strategy : applicableStrategies) {
            try {
                if (strategy.canHandle(context, project, file, element)) {
                    long strategyStartTime = System.currentTimeMillis();
                    
                    strategy.complete(context, project, file, element, componentProvider, result);
                    
                    long strategyDuration = System.currentTimeMillis() - strategyStartTime;
                    updateStrategyStats(strategy.getStrategyName(), strategyDuration);
                    
                    VueKitLogger.debug(LOG, "策略执行完成: " + strategy.getStrategyName() + 
                                   " (耗时: " + strategyDuration + "ms)");
                }
            } catch (Exception e) {
                VueKitLogger.logAndIgnore(LOG, "策略执行失败: " + strategy.getStrategyName(), e);
            }
        }
        
        long totalDuration = System.currentTimeMillis() - startTime;
        VueKitLogger.performanceWithThreshold(LOG, "补全执行", totalDuration, VueKitConstants.COMPLETION_THRESHOLD_MS);
    }
    
    /**
     * 获取适用的策略
     */
    @NotNull
    private List<CompletionStrategy> getApplicableStrategies(@NotNull CompletionContext.CompletionType type) {
        return sortedStrategiesCache.computeIfAbsent(type, k -> {
            List<CompletionStrategy> typeStrategies = strategies.get(type);
            if (typeStrategies == null) {
                return new ArrayList<>();
            }
            
            // 按优先级排序
            typeStrategies.sort((s1, s2) -> {
                int priority1 = s1.getPriority();
                int priority2 = s2.getPriority();
                return Integer.compare(priority2, priority1); // 降序排列
            });
            
            return new ArrayList<>(typeStrategies);
        });
    }
    
    /**
     * 更新策略统计信息
     */
    private void updateStrategyStats(String strategyName, long duration) {
        strategyStats.compute(strategyName, (k, v) -> {
            if (v == null) {
                return new StrategyStats(strategyName, 1, duration, duration, duration);
            } else {
                v.incrementCount();
                v.updateDuration(duration);
                return v;
            }
        });
    }
    
    /**
     * 获取策略统计信息
     */
    public Map<String, StrategyStats> getStrategyStats() {
        return new HashMap<>(strategyStats);
    }
    
    /**
     * 策略统计信息
     */
    public static class StrategyStats {
        private final String strategyName;
        private int executionCount;
        private long totalDuration;
        private long minDuration;
        private long maxDuration;
        
        public StrategyStats(String strategyName, int executionCount, long totalDuration, 
                           long minDuration, long maxDuration) {
            this.strategyName = strategyName;
            this.executionCount = executionCount;
            this.totalDuration = totalDuration;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }
        
        public void incrementCount() {
            executionCount++;
        }
        
        public void updateDuration(long duration) {
            totalDuration += duration;
            minDuration = Math.min(minDuration, duration);
            maxDuration = Math.max(maxDuration, duration);
        }
        
        public String getStrategyName() { return strategyName; }
        public int getExecutionCount() { return executionCount; }
        public long getTotalDuration() { return totalDuration; }
        public long getMinDuration() { return minDuration; }
        public long getMaxDuration() { return maxDuration; }
        public double getAverageDuration() { 
            return executionCount > 0 ? (double) totalDuration / executionCount : 0; 
        }
    }
}