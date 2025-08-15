package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 性能监控系统
 * 
 * 提供全面的性能指标收集和分析功能，包括：
 * - 操作耗时统计
 * - 调用次数统计
 * - 性能趋势分析
 * - 性能报告生成
 * - 阈值告警
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class PerformanceMonitor {
    
    private static final Logger LOG = Logger.getInstance(PerformanceMonitor.class);
    
    // 性能阈值配置
    private static final long SLOW_OPERATION_THRESHOLD = 1000; // 1秒
    private static final long VERY_SLOW_OPERATION_THRESHOLD = 5000; // 5秒
    private static final long CRITICAL_OPERATION_THRESHOLD = 10000; // 10秒
    
    // 统计信息存储
    private final Map<String, OperationStats> operationStats;
    private final Map<String, LongAdder> errorCounts;
    private final AtomicLong totalOperations;
    private final AtomicLong totalErrors;
    
    // 性能监控开关
    private volatile boolean enabled = true;
    
    /**
     * 构造函数
     */
    public PerformanceMonitor() {
        this.operationStats = new ConcurrentHashMap<>();
        this.errorCounts = new ConcurrentHashMap<>();
        this.totalOperations = new AtomicLong(0);
        this.totalErrors = new AtomicLong(0);
        
        LOG.info("性能监控系统初始化完成");
    }
    
    /**
     * 开始监控操作
     * 
     * @param operationName 操作名称
     * @return 监控上下文
     */
    public MonitorContext startMonitoring(String operationName) {
        if (!enabled) {
            return new MonitorContext(operationName, 0);
        }
        
        return new MonitorContext(operationName, System.currentTimeMillis());
    }
    
    /**
     * 完成操作监控
     * 
     * @param context 监控上下文
     * @param success 是否成功
     */
    public void finishMonitoring(MonitorContext context, boolean success) {
        if (!enabled || context.startTime == 0) {
            return;
        }
        
        long duration = System.currentTimeMillis() - context.startTime;
        String operationName = context.operationName;
        
        // 更新统计信息
        operationStats.computeIfAbsent(operationName, k -> new OperationStats())
                     .recordOperation(duration, success);
        
        totalOperations.incrementAndGet();
        
        if (!success) {
            errorCounts.computeIfAbsent(operationName, k -> new LongAdder()).increment();
            totalErrors.incrementAndGet();
        }
        
        // 性能告警
        checkPerformanceThreshold(operationName, duration);
        
        // 记录性能日志
        VueKitLogger.performance(LOG, operationName, duration);
    }
    
    /**
     * 检查性能阈值
     * 
     * @param operationName 操作名称
     * @param duration 耗时
     */
    private void checkPerformanceThreshold(String operationName, long duration) {
        if (duration >= CRITICAL_OPERATION_THRESHOLD) {
            LOG.error("性能告警 - 操作 " + operationName + " 耗时 " + duration + "ms，超过严重阈值 " + CRITICAL_OPERATION_THRESHOLD + "ms");
        } else if (duration >= VERY_SLOW_OPERATION_THRESHOLD) {
            LOG.warn("性能告警 - 操作 " + operationName + " 耗时 " + duration + "ms，超过警告阈值 " + VERY_SLOW_OPERATION_THRESHOLD + "ms");
        } else if (duration >= SLOW_OPERATION_THRESHOLD) {
            LOG.info("性能提示 - 操作 " + operationName + " 耗时 " + duration + "ms，超过提示阈值 " + SLOW_OPERATION_THRESHOLD + "ms");
        }
    }
    
    /**
     * 获取操作统计信息
     * 
     * @param operationName 操作名称
     * @return 操作统计信息
     */
    public OperationStats getOperationStats(String operationName) {
        return operationStats.getOrDefault(operationName, new OperationStats());
    }
    
    /**
     * 获取所有操作统计信息
     * 
     * @return 所有操作的统计信息
     */
    public Map<String, OperationStats> getAllOperationStats() {
        return new ConcurrentHashMap<>(operationStats);
    }
    
    /**
     * 获取总体统计信息
     * 
     * @return 总体统计信息
     */
    public OverallStats getOverallStats() {
        long totalDuration = operationStats.values().stream()
                .mapToLong(OperationStats::getTotalDuration)
                .sum();
        
        long avgDuration = totalOperations.get() > 0 ? totalDuration / totalOperations.get() : 0;
        
        return new OverallStats(
            totalOperations.get(),
            totalErrors.get(),
            totalDuration,
            avgDuration,
            operationStats.size()
        );
    }
    
    /**
     * 生成性能报告
     * 
     * @return 格式化的性能报告
     */
    public String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== VueKit 性能监控报告 ===\n");
        report.append("生成时间: ").append(java.time.LocalDateTime.now()).append("\n\n");
        
        // 总体统计
        OverallStats overall = getOverallStats();
        report.append("总体统计:\n");
        report.append("  总操作数: ").append(overall.totalOperations).append("\n");
        report.append("  总错误数: ").append(overall.totalErrors).append("\n");
        report.append("  总耗时: ").append(overall.totalDuration).append("ms\n");
        report.append("  平均耗时: ").append(overall.averageDuration).append("ms\n");
        report.append("  监控操作数: ").append(overall.monitoredOperations).append("\n\n");
        
        // 各操作统计
        report.append("操作详情:\n");
        
        // 创建排序后的操作列表
        java.util.List<java.util.Map.Entry<String, OperationStats>> sortedEntries = 
            new java.util.ArrayList<>(operationStats.entrySet());
        
        // 按平均耗时降序排序
        java.util.Collections.sort(sortedEntries, new java.util.Comparator<java.util.Map.Entry<String, OperationStats>>() {
            @Override
            public int compare(java.util.Map.Entry<String, OperationStats> e1, 
                             java.util.Map.Entry<String, OperationStats> e2) {
                double avg1 = e1.getValue().getAverageDuration();
                double avg2 = e2.getValue().getAverageDuration();
                return Double.compare(avg2, avg1); // 降序排序
            }
        });
        
        // 生成报告
        for (java.util.Map.Entry<String, OperationStats> entry : sortedEntries) {
            String operationName = entry.getKey();
            OperationStats stats = entry.getValue();
            report.append("  ").append(operationName).append(":\n");
            report.append("    调用次数: ").append(stats.getCallCount()).append("\n");
            report.append("    成功次数: ").append(stats.getSuccessCount()).append("\n");
            report.append("    失败次数: ").append(stats.getFailureCount()).append("\n");
            report.append("    平均耗时: ").append(String.format("%.2f", stats.getAverageDuration())).append("ms\n");
            report.append("    最小耗时: ").append(stats.getMinDuration()).append("ms\n");
            report.append("    最大耗时: ").append(stats.getMaxDuration()).append("ms\n");
            report.append("    总耗时: ").append(stats.getTotalDuration()).append("ms\n");
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        operationStats.clear();
        errorCounts.clear();
        totalOperations.set(0);
        totalErrors.set(0);
        LOG.info("性能监控统计信息已重置");
    }
    
    /**
     * 启用/禁用性能监控
     * 
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOG.info("性能监控系统已" + (enabled ? "启用" : "禁用"));
    }
    
    /**
     * 是否启用
     * 
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 监控上下文
     */
    public static class MonitorContext {
        private final String operationName;
        private final long startTime;
        
        private MonitorContext(String operationName, long startTime) {
            this.operationName = operationName;
            this.startTime = startTime;
        }
    }
    
    /**
     * 操作统计信息
     */
    public static class OperationStats {
        private final LongAdder callCount;
        private final LongAdder successCount;
        private final LongAdder failureCount;
        private final LongAdder totalDuration;
        private volatile long minDuration = Long.MAX_VALUE;
        private volatile long maxDuration = 0;
        
        public OperationStats() {
            this.callCount = new LongAdder();
            this.successCount = new LongAdder();
            this.failureCount = new LongAdder();
            this.totalDuration = new LongAdder();
        }
        
        public void recordOperation(long duration, boolean success) {
            callCount.increment();
            totalDuration.add(duration);
            
            if (success) {
                successCount.increment();
            } else {
                failureCount.increment();
            }
            
            // 更新最小/最大耗时
            updateMinMaxDuration(duration);
        }
        
        private void updateMinMaxDuration(long duration) {
            long currentMin = minDuration;
            while (duration < currentMin && !minDurationUpdater.compareAndSet(this, currentMin, duration)) {
                currentMin = minDuration;
            }
            
            long currentMax = maxDuration;
            while (duration > currentMax && !maxDurationUpdater.compareAndSet(this, currentMax, duration)) {
                currentMax = maxDuration;
            }
        }
        
        // Getters
        public long getCallCount() { return callCount.sum(); }
        public long getSuccessCount() { return successCount.sum(); }
        public long getFailureCount() { return failureCount.sum(); }
        public long getTotalDuration() { return totalDuration.sum(); }
        public long getMinDuration() { return minDuration == Long.MAX_VALUE ? 0 : minDuration; }
        public long getMaxDuration() { return maxDuration; }
        public double getAverageDuration() { 
            long count = callCount.sum();
            return count > 0 ? (double) totalDuration.sum() / count : 0.0;
        }
        
        private static final java.util.concurrent.atomic.AtomicLongFieldUpdater<OperationStats> minDurationUpdater =
            java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater(OperationStats.class, "minDuration");
        private static final java.util.concurrent.atomic.AtomicLongFieldUpdater<OperationStats> maxDurationUpdater =
            java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater(OperationStats.class, "maxDuration");
    }
    
    /**
     * 总体统计信息
     */
    public static class OverallStats {
        private final long totalOperations;
        private final long totalErrors;
        private final long totalDuration;
        private final long averageDuration;
        private final int monitoredOperations;
        
        public OverallStats(long totalOperations, long totalErrors, long totalDuration, 
                          long averageDuration, int monitoredOperations) {
            this.totalOperations = totalOperations;
            this.totalErrors = totalErrors;
            this.totalDuration = totalDuration;
            this.averageDuration = averageDuration;
            this.monitoredOperations = monitoredOperations;
        }
        
        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getTotalErrors() { return totalErrors; }
        public long getTotalDuration() { return totalDuration; }
        public long getAverageDuration() { return averageDuration; }
        public int getMonitoredOperations() { return monitoredOperations; }
    }
}
