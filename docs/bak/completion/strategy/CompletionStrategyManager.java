package bak.completion.strategy;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.completion.ComponentProvider;
import com.chu7.vuecomponentassistant.completion.strategy.AttributeCompletionStrategy;
import com.chu7.vuecomponentassistant.completion.strategy.CompletionStrategy;
import com.chu7.vuecomponentassistant.completion.strategy.ComponentCompletionStrategy;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
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
        
        // TODO: 注册事件补全策略
        // registerStrategy(new EventCompletionStrategy());
        
        // TODO: 注册插槽补全策略
        // registerStrategy(new SlotCompletionStrategy());
        
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
        
        // 初始化统计信息
        strategyStats.put(strategy.getStrategyName(), new StrategyStats(strategy.getStrategyName()));
        
        VueKitLogger.debug(LOG, "注册策略: " + strategy.getStrategyName() + " (类型: " + type + ")");
    }
    
    /**
     * 注销补全策略
     * 
     * @param strategy 要注销的策略
     */
    public void unregisterStrategy(@NotNull CompletionStrategy strategy) {
        CompletionContext.CompletionType type = strategy.getSupportedType();
        List<CompletionStrategy> typeStrategies = strategies.get(type);
        
        if (typeStrategies != null) {
            typeStrategies.remove(strategy);
            sortedStrategiesCache.remove(type);
            strategyStats.remove(strategy.getStrategyName());
            
            VueKitLogger.debug(LOG, "注销策略: " + strategy.getStrategyName());
        }
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
        
        VueKitLogger.debug(LOG, "开始执行补全，类型: " + context.getType());
        
        // 获取适用的策略
        List<CompletionStrategy> applicableStrategies = getApplicableStrategies(context, project, file, element);
        
        if (applicableStrategies.isEmpty()) {
            VueKitLogger.debug(LOG, "没有找到适用的补全策略");
            return;
        }
        
        VueKitLogger.debug(LOG, "找到 " + applicableStrategies.size() + " 个适用策略");
        
        int totalCompletions = 0;
        
        // 执行每个适用的策略
        for (CompletionStrategy strategy : applicableStrategies) {
            long strategyStartTime = System.currentTimeMillis();
            
            try {
                VueKitLogger.debug(LOG, "执行策略: " + strategy.getStrategyName());
                
                int beforeCount = result.getPrefixMatcher().getPrefix().length();
                strategy.complete(context, project, file, element, componentProvider, result);
                int afterCount = result.getPrefixMatcher().getPrefix().length();
                
                int addedCompletions = afterCount - beforeCount;
                totalCompletions += addedCompletions;
                
                long strategyDuration = System.currentTimeMillis() - strategyStartTime;
                
                // 记录策略统计
                recordStrategyExecution(strategy.getStrategyName(), strategyDuration, addedCompletions, true);
                
                VueKitLogger.debug(LOG, "策略 " + strategy.getStrategyName() + " 执行完成，" +
                                 "耗时: " + strategyDuration + "ms，添加补全: " + addedCompletions + " 个");
                
            } catch (Exception e) {
                long strategyDuration = System.currentTimeMillis() - strategyStartTime;
                recordStrategyExecution(strategy.getStrategyName(), strategyDuration, 0, false);
                
                VueKitLogger.error(LOG, "策略执行失败: " + strategy.getStrategyName(), e);
            }
        }
        
        long totalDuration = System.currentTimeMillis() - startTime;
        
        VueKitLogger.performanceWithThreshold(LOG, "补全策略执行", totalDuration, VueKitConstants.COMPLETION_THRESHOLD_MS);
        VueKitLogger.logCompletion(LOG, "总补全", totalCompletions, totalDuration);
    }
    
    /**
     * 获取适用的策略列表
     */
    @NotNull
    private List<CompletionStrategy> getApplicableStrategies(@NotNull CompletionContext context,
                                                           @NotNull Project project,
                                                           @NotNull PsiFile file,
                                                           @NotNull PsiElement element) {
        
        CompletionContext.CompletionType type = context.getType();
        
        // 获取已排序的策略列表
        List<CompletionStrategy> sortedStrategies = getSortedStrategies(type);
        
        List<CompletionStrategy> applicableStrategies = new ArrayList<>();
        
        for (CompletionStrategy strategy : sortedStrategies) {
            if (strategy.isEnabled() && strategy.canHandle(context, project, file, element)) {
                applicableStrategies.add(strategy);
                VueKitLogger.debug(LOG, "策略适用: " + strategy.getStrategyName());
            }
        }
        
        return applicableStrategies;
    }
    
    /**
     * 获取已排序的策略列表
     */
    @NotNull
    private List<CompletionStrategy> getSortedStrategies(@NotNull CompletionContext.CompletionType type) {
        return sortedStrategiesCache.computeIfAbsent(type, k -> {
            List<CompletionStrategy> typeStrategies = strategies.get(type);
            if (typeStrategies == null) {
                return Collections.emptyList();
            }
            
            // 按优先级排序（优先级高的在前）
            List<CompletionStrategy> sorted = new ArrayList<>(typeStrategies);
            sorted.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
            
            VueKitLogger.debug(LOG, "策略排序完成，类型: " + type + ", 数量: " + sorted.size());
            
            return Collections.unmodifiableList(sorted);
        });
    }
    
    /**
     * 记录策略执行统计
     */
    private void recordStrategyExecution(@NotNull String strategyName, 
                                       long duration, 
                                       int completionsAdded, 
                                       boolean success) {
        
        StrategyStats stats = strategyStats.get(strategyName);
        if (stats != null) {
            stats.recordExecution(duration, completionsAdded, success);
        }
    }
    
    /**
     * 获取所有注册的策略
     */
    @NotNull
    public Map<CompletionContext.CompletionType, List<CompletionStrategy>> getAllStrategies() {
        Map<CompletionContext.CompletionType, List<CompletionStrategy>> result = new HashMap<>();
        for (Map.Entry<CompletionContext.CompletionType, List<CompletionStrategy>> entry : strategies.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return result;
    }
    
    /**
     * 获取指定类型的策略
     */
    @NotNull
    public List<CompletionStrategy> getStrategies(@NotNull CompletionContext.CompletionType type) {
        List<CompletionStrategy> typeStrategies = strategies.get(type);
        return typeStrategies != null ? Collections.unmodifiableList(typeStrategies) : Collections.emptyList();
    }
    
    /**
     * 获取策略统计信息
     */
    @NotNull
    public Map<String, StrategyStats> getStrategyStats() {
        return Collections.unmodifiableMap(strategyStats);
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        strategyStats.values().forEach(StrategyStats::reset);
        VueKitLogger.debug(LOG, "策略统计信息已重置");
    }
    
    /**
     * 策略统计信息类
     */
    public static class StrategyStats {
        private final String strategyName;
        private long totalExecutions = 0;
        private long successfulExecutions = 0;
        private long totalDuration = 0;
        private long totalCompletions = 0;
        private long lastExecutionTime = 0;
        
        public StrategyStats(@NotNull String strategyName) {
            this.strategyName = strategyName;
        }
        
        public synchronized void recordExecution(long duration, int completions, boolean success) {
            totalExecutions++;
            totalDuration += duration;
            totalCompletions += completions;
            lastExecutionTime = System.currentTimeMillis();
            
            if (success) {
                successfulExecutions++;
            }
        }
        
        public synchronized void reset() {
            totalExecutions = 0;
            successfulExecutions = 0;
            totalDuration = 0;
            totalCompletions = 0;
            lastExecutionTime = 0;
        }
        
        // Getters
        public String getStrategyName() { return strategyName; }
        public long getTotalExecutions() { return totalExecutions; }
        public long getSuccessfulExecutions() { return successfulExecutions; }
        public double getSuccessRate() { 
            return totalExecutions == 0 ? 0.0 : (double) successfulExecutions / totalExecutions; 
        }
        public long getAverageDuration() { 
            return totalExecutions == 0 ? 0 : totalDuration / totalExecutions; 
        }
        public long getTotalCompletions() { return totalCompletions; }
        public double getAverageCompletions() { 
            return totalExecutions == 0 ? 0.0 : (double) totalCompletions / totalExecutions; 
        }
        public long getLastExecutionTime() { return lastExecutionTime; }
        
        @Override
        public String toString() {
            return String.format(
                "StrategyStats{name='%s', executions=%d, success=%.1f%%, avgDuration=%dms, avgCompletions=%.1f}",
                strategyName, totalExecutions, getSuccessRate() * 100, getAverageDuration(), getAverageCompletions()
            );
        }
    }
}