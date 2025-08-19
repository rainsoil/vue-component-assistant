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
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供全面的性能指标收集和分析功能</li>
 *   <li>监控操作耗时统计和调用次数统计</li>
 *   <li>支持性能趋势分析和报告生成</li>
 *   <li>提供性能阈值告警和监控开关</li>
 *   <li>支持多线程环境下的安全统计</li>
 *   <li>集成到VueKit日志系统</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>线程安全：使用ConcurrentHashMap和原子类确保并发安全</li>
 *   <li>高性能：使用LongAdder进行高并发计数统计</li>
 *   <li>可配置：支持性能阈值配置和监控开关</li>
 *   <li>实时监控：支持实时性能数据收集和分析</li>
 *   <li>报告生成：提供格式化的性能报告输出</li>
 *   <li>告警机制：支持多级性能告警</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库加载性能监控</li>
 *   <li>代码补全响应时间监控</li>
 *   <li>配置文件操作性能监控</li>
 *   <li>网络请求性能监控</li>
 *   <li>插件整体性能分析</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see java.util.concurrent.ConcurrentHashMap
 * @see java.util.concurrent.atomic.LongAdder
 * @see java.util.concurrent.atomic.AtomicLong
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class PerformanceMonitor {
    
    /**
     * 日志记录器，用于记录性能监控过程中的关键信息和告警
     */
    private static final Logger LOG = Logger.getInstance(PerformanceMonitor.class);
    
    /**
     * 慢操作阈值，超过此时间将记录性能提示（毫秒）
     */
    private static final long SLOW_OPERATION_THRESHOLD = 1000; // 1秒
    
    /**
     * 很慢操作阈值，超过此时间将记录性能警告（毫秒）
     */
    private static final long VERY_SLOW_OPERATION_THRESHOLD = 5000; // 5秒
    
    /**
     * 严重操作阈值，超过此时间将记录性能错误（毫秒）
     */
    private static final long CRITICAL_OPERATION_THRESHOLD = 10000; // 10秒
    
    /**
     * 操作统计信息存储，线程安全的Map结构
     */
    private final Map<String, OperationStats> operationStats;
    
    /**
     * 错误计数统计，线程安全的Map结构
     */
    private final Map<String, LongAdder> errorCounts;
    
    /**
     * 总操作数统计，线程安全的原子计数器
     */
    private final AtomicLong totalOperations;
    
    /**
     * 总错误数统计，线程安全的原子计数器
     */
    private final AtomicLong totalErrors;
    
    /**
     * 性能监控开关，支持运行时启用/禁用
     */
    private volatile boolean enabled = true;
    
    /**
     * 构造函数
     *
     * <p>初始化性能监控系统：</p>
     * <ul>
     *   <li>创建线程安全的统计信息存储</li>
     *   <li>初始化操作统计和错误计数</li>
     *   <li>设置默认监控状态为启用</li>
     *   <li>记录初始化完成日志</li>
     * </ul>
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>创建操作监控上下文</li>
     *   <li>记录操作开始时间</li>
     *   <li>支持监控开关控制</li>
     *   <li>返回监控上下文对象</li>
     * </ul>
     *
     * <p>使用方式：</p>
     * <pre>{@code
     * MonitorContext context = monitor.startMonitoring("组件库加载");
     * try {
     *     // 执行操作
     *     performOperation();
     *     monitor.finishMonitoring(context, true);
     * } catch (Exception e) {
     *     monitor.finishMonitoring(context, false);
     * }
     * }</pre>
     *
     * @param operationName 操作名称，用于标识和统计，不能为null
     * @return 监控上下文对象，包含操作名称和开始时间
     * @throws IllegalArgumentException 如果操作名称为null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>计算操作执行耗时</li>
     *   <li>更新操作统计信息</li>
     *   <li>记录成功/失败状态</li>
     *   <li>检查性能阈值并触发告警</li>
     *   <li>记录性能日志</li>
     * </ul>
     *
     * <p>统计更新：</p>
     * <ul>
     *   <li>操作调用次数</li>
     *   <li>成功/失败次数</li>
     *   <li>总耗时和平均耗时</li>
     *   <li>最小/最大耗时</li>
     *   <li>错误计数</li>
     * </ul>
     *
     * @param context 监控上下文，不能为null
     * @param success 操作是否成功执行
     * @throws IllegalArgumentException 如果监控上下文为null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>根据操作耗时检查性能阈值</li>
     *   <li>触发不同级别的性能告警</li>
     *   <li>记录告警日志信息</li>
     * </ul>
     *
     * <p>告警级别：</p>
     * <ul>
     *   <li>提示级别：超过1秒（SLOW_OPERATION_THRESHOLD）</li>
     *   <li>警告级别：超过5秒（VERY_SLOW_OPERATION_THRESHOLD）</li>
     *   <li>错误级别：超过10秒（CRITICAL_OPERATION_THRESHOLD）</li>
     * </ul>
     *
     * @param operationName 操作名称，用于告警信息标识
     * @param duration 操作耗时（毫秒）
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取指定操作的详细统计信息</li>
     *   <li>如果操作不存在，返回空的统计对象</li>
     *   <li>支持实时查询操作性能数据</li>
     * </ul>
     *
     * @param operationName 操作名称，不能为null
     * @return 操作统计信息对象，包含调用次数、耗时等详细数据
     * @throws IllegalArgumentException 如果操作名称为null
     */
    public OperationStats getOperationStats(String operationName) {
        return operationStats.getOrDefault(operationName, new OperationStats());
    }
    
    /**
     * 获取所有操作统计信息
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取所有被监控操作的统计信息</li>
     *   <li>返回线程安全的副本，避免并发修改</li>
     *   <li>支持批量性能数据分析</li>
     * </ul>
     *
     * @return 所有操作统计信息的Map副本，键为操作名称，值为统计信息
     */
    public Map<String, OperationStats> getAllOperationStats() {
        return new ConcurrentHashMap<>(operationStats);
    }
    
    /**
     * 获取总体统计信息
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>计算所有操作的汇总统计信息</li>
     *   <li>包括总操作数、总错误数、总耗时等</li>
     *   <li>计算平均耗时和监控操作数量</li>
     * </ul>
     *
     * @return 总体统计信息对象，包含汇总的性能指标
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>生成格式化的性能监控报告</li>
     *   <li>包含总体统计和各操作详细统计</li>
     *   <li>按平均耗时降序排序操作</li>
     *   <li>提供时间戳和格式化输出</li>
     * </ul>
     *
     * <p>报告内容：</p>
     * <ul>
     *   <li>报告生成时间和标题</li>
     *   <li>总体统计信息（总操作数、错误数、耗时等）</li>
     *   <li>各操作详细统计（调用次数、成功/失败、耗时分布等）</li>
     *   <li>按性能排序的操作列表</li>
     * </ul>
     *
     * @return 格式化的性能报告字符串
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>清空所有操作统计信息</li>
     *   <li>重置总操作数和错误数</li>
     *   <li>清空错误计数统计</li>
     *   <li>记录重置操作日志</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>性能测试前的数据清理</li>
     *   <li>定期性能数据归档</li>
     *   <li>性能问题排查后的重新监控</li>
     * </ul>
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>动态控制性能监控的启用状态</li>
     *   <li>禁用时不会收集性能数据</li>
     *   <li>支持运行时切换监控状态</li>
     *   <li>记录状态变更日志</li>
     * </ul>
     *
     * @param enabled 是否启用性能监控
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOG.info("性能监控系统已" + (enabled ? "启用" : "禁用"));
    }
    
    /**
     * 检查性能监控是否启用
     *
     * @return 如果性能监控已启用返回true，否则返回false
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 监控上下文
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>封装操作监控的上下文信息</li>
     *   <li>包含操作名称和开始时间</li>
     *   <li>用于性能监控的开始和结束</li>
     *   <li>不可变对象，确保线程安全</li>
     * </ul>
     */
    public static class MonitorContext {
        /**
         * 操作名称，用于标识被监控的操作
         */
        private final String operationName;
        
        /**
         * 操作开始时间戳（毫秒）
         */
        private final long startTime;
        
        /**
         * 构造函数
         *
         * @param operationName 操作名称
         * @param startTime 开始时间戳
         */
        private MonitorContext(String operationName, long startTime) {
            this.operationName = operationName;
            this.startTime = startTime;
        }
    }
    
    /**
     * 操作统计信息
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>记录单个操作的详细统计信息</li>
     *   <li>支持高并发环境下的安全统计</li>
     *   <li>提供完整的性能指标数据</li>
     *   <li>使用原子操作确保数据一致性</li>
     * </ul>
     *
     * <p>统计指标：</p>
     * <ul>
     *   <li>调用次数：总调用次数统计</li>
     *   <li>成功/失败次数：操作执行结果统计</li>
     *   <li>耗时统计：总耗时、平均耗时、最小/最大耗时</li>
     * </ul>
     */
    public static class OperationStats {
        /**
         * 调用次数统计器，线程安全
         */
        private final LongAdder callCount;
        
        /**
         * 成功次数统计器，线程安全
         */
        private final LongAdder successCount;
        
        /**
         * 失败次数统计器，线程安全
         */
        private final LongAdder failureCount;
        
        /**
         * 总耗时统计器，线程安全
         */
        private final LongAdder totalDuration;
        
        /**
         * 最小耗时，使用volatile确保可见性
         */
        private volatile long minDuration = Long.MAX_VALUE;
        
        /**
         * 最大耗时，使用volatile确保可见性
         */
        private volatile long maxDuration = 0;
        
        /**
         * 构造函数
         *
         * <p>初始化所有统计计数器：</p>
         * <ul>
         *   <li>创建线程安全的LongAdder计数器</li>
         *   <li>设置最小耗时的初始值</li>
         *   <li>设置最大耗时的初始值</li>
         * </ul>
         */
        public OperationStats() {
            this.callCount = new LongAdder();
            this.successCount = new LongAdder();
            this.failureCount = new LongAdder();
            this.totalDuration = new LongAdder();
        }
        
        /**
         * 记录操作执行结果
         *
         * <p>功能说明：</p>
         * <ul>
         *   <li>更新调用次数统计</li>
         *   <li>记录操作耗时</li>
         *   <li>更新成功/失败计数</li>
         *   <li>维护最小/最大耗时</li>
         * </ul>
         *
         * @param duration 操作耗时（毫秒）
         * @param success 操作是否成功
         */
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
        
        /**
         * 更新最小/最大耗时
         *
         * <p>使用CAS操作确保线程安全：</p>
         * <ul>
         *   <li>原子更新最小耗时</li>
         *   <li>原子更新最大耗时</li>
         *   <li>避免并发更新冲突</li>
         * </ul>
         *
         * @param duration 新的操作耗时
         */
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
        
        /**
         * 获取调用次数
         *
         * @return 总调用次数
         */
        public long getCallCount() { return callCount.sum(); }
        
        /**
         * 获取成功次数
         *
         * @return 成功执行次数
         */
        public long getSuccessCount() { return successCount.sum(); }
        
        /**
         * 获取失败次数
         *
         * @return 失败执行次数
         */
        public long getFailureCount() { return failureCount.sum(); }
        
        /**
         * 获取总耗时
         *
         * @return 所有操作的总耗时（毫秒）
         */
        public long getTotalDuration() { return totalDuration.sum(); }
        
        /**
         * 获取最小耗时
         *
         * @return 单次操作的最小耗时（毫秒），如果没有操作返回0
         */
        public long getMinDuration() { return minDuration == Long.MAX_VALUE ? 0 : minDuration; }
        
        /**
         * 获取最大耗时
         *
         * @return 单次操作的最大耗时（毫秒）
         */
        public long getMaxDuration() { return maxDuration; }
        
        /**
         * 获取平均耗时
         *
         * @return 操作的平均耗时（毫秒），如果没有操作返回0.0
         */
        public double getAverageDuration() { 
            long count = callCount.sum();
            return count > 0 ? (double) totalDuration.sum() / count : 0.0;
        }
        
        /**
         * 最小耗时原子更新器，用于线程安全的CAS操作
         */
        private static final java.util.concurrent.atomic.AtomicLongFieldUpdater<OperationStats> minDurationUpdater =
            java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater(OperationStats.class, "minDuration");
        
        /**
         * 最大耗时原子更新器，用于线程安全的CAS操作
         */
        private static final java.util.concurrent.atomic.AtomicLongFieldUpdater<OperationStats> maxDurationUpdater =
            java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater(OperationStats.class, "maxDuration");
    }
    
    /**
     * 总体统计信息
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>封装所有操作的汇总统计信息</li>
     *   <li>提供系统级别的性能指标</li>
     *   <li>用于性能报告和监控面板</li>
     *   <li>不可变对象，确保数据一致性</li>
     * </ul>
     */
    public static class OverallStats {
        /**
         * 总操作数
         */
        private final long totalOperations;
        
        /**
         * 总错误数
         */
        private final long totalErrors;
        
        /**
         * 总耗时（毫秒）
         */
        private final long totalDuration;
        
        /**
         * 平均耗时（毫秒）
         */
        private final long averageDuration;
        
        /**
         * 被监控的操作数量
         */
        private final int monitoredOperations;
        
        /**
         * 构造函数
         *
         * @param totalOperations 总操作数
         * @param totalErrors 总错误数
         * @param totalDuration 总耗时
         * @param averageDuration 平均耗时
         * @param monitoredOperations 被监控的操作数量
         */
        public OverallStats(long totalOperations, long totalErrors, long totalDuration, 
                          long averageDuration, int monitoredOperations) {
            this.totalOperations = totalOperations;
            this.totalErrors = totalErrors;
            this.totalDuration = totalDuration;
            this.averageDuration = averageDuration;
            this.monitoredOperations = monitoredOperations;
        }
        
        /**
         * 获取总操作数
         *
         * @return 总操作数
         */
        public long getTotalOperations() { return totalOperations; }
        
        /**
         * 获取总错误数
         *
         * @return 总错误数
         */
        public long getTotalErrors() { return totalErrors; }
        
        /**
         * 获取总耗时
         *
         * @return 总耗时（毫秒）
         */
        public long getTotalDuration() { return totalDuration; }
        
        /**
         * 获取平均耗时
         *
         * @return 平均耗时（毫秒）
         */
        public long getAverageDuration() { return averageDuration; }
        
        /**
         * 获取被监控的操作数量
         *
         * @return 被监控的操作数量
         */
        public int getMonitoredOperations() { return monitoredOperations; }
    }
}
