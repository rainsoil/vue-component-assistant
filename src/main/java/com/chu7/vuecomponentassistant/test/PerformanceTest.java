package com.chu7.vuecomponentassistant.test;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能测试和监控类
 * 
 * 提供性能监控、测试和优化功能
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class PerformanceTest {
    
    private static final Logger LOG = Logger.getInstance(PerformanceTest.class);
    
    // 性能指标存储
    private static final ConcurrentHashMap<String, AtomicLong> metrics = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();
    
    // 性能阈值
    private static final long COMPLETION_THRESHOLD = 100; // 补全响应时间阈值（毫秒）
    private static final long DOCUMENTATION_THRESHOLD = 200; // 文档加载时间阈值（毫秒）
    private static final long CACHE_THRESHOLD = 50; // 缓存操作时间阈值（毫秒）
    
    /**
     * 开始性能测试
     * 
     * @param testName 测试名称
     */
    public static void startTest(String testName) {
        startTimes.put(testName, System.currentTimeMillis());
        LOG.debug("开始性能测试: " + testName);
    }
    
    /**
     * 结束性能测试
     * 
     * @param testName 测试名称
     */
    public static void endTest(String testName) {
        Long startTime = startTimes.remove(testName);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            metrics.computeIfAbsent(testName, k -> new AtomicLong(0)).addAndGet(duration);
            
            LOG.debug(String.format("性能测试完成: %s, 耗时: %dms", testName, duration));
            
            // 检查性能阈值
            checkPerformanceThreshold(testName, duration);
        }
    }
    
    /**
     * 检查性能阈值
     */
    private static void checkPerformanceThreshold(String testName, long duration) {
        long threshold = getThreshold(testName);
        if (duration > threshold) {
            LOG.warn(String.format("性能警告: %s 耗时 %dms，超过阈值 %dms", testName, duration, threshold));
        }
    }
    
    /**
     * 获取性能阈值
     */
    private static long getThreshold(String testName) {
        if (testName.contains("completion")) {
            return COMPLETION_THRESHOLD;
        } else if (testName.contains("documentation")) {
            return DOCUMENTATION_THRESHOLD;
        } else if (testName.contains("cache")) {
            return CACHE_THRESHOLD;
        }
        return 1000; // 默认阈值
    }
    
    /**
     * 获取性能指标
     * 
     * @param testName 测试名称
     * @return 平均耗时（毫秒）
     */
    public static double getAverageTime(String testName) {
        AtomicLong total = metrics.get(testName);
        if (total != null) {
            // 这里需要记录调用次数，暂时返回总时间
            return total.get();
        }
        return 0.0;
    }
    
    /**
     * 获取所有性能指标
     */
    public static String getAllMetrics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 性能指标 ===\n");
        
        metrics.forEach((testName, total) -> {
            sb.append(String.format("%s: %dms\n", testName, total.get()));
        });
        
        sb.append("================");
        return sb.toString();
    }
    
    /**
     * 清空性能指标
     */
    public static void clearMetrics() {
        metrics.clear();
        startTimes.clear();
        LOG.info("性能指标已清空");
    }
    
    /**
     * 补全性能测试
     */
    public static void testCompletionPerformance(Project project, String prefix) {
        startTest("completion_" + prefix);
        
        try {
            // 模拟补全操作
            Thread.sleep(10); // 模拟处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            endTest("completion_" + prefix);
        }
    }
    
    /**
     * 文档加载性能测试
     */
    public static void testDocumentationPerformance(String componentName) {
        startTest("documentation_" + componentName);
        
        try {
            // 模拟文档加载
            Thread.sleep(20); // 模拟处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            endTest("documentation_" + componentName);
        }
    }
    
    /**
     * 缓存性能测试
     */
    public static void testCachePerformance(String operation) {
        startTest("cache_" + operation);
        
        try {
            // 模拟缓存操作
            Thread.sleep(5); // 模拟处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            endTest("cache_" + operation);
        }
    }
    
    /**
     * 内存使用测试
     */
    public static void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        LOG.info(String.format("内存使用情况 - 总内存: %dMB, 已用内存: %dMB, 空闲内存: %dMB",
                             totalMemory / 1024 / 1024,
                             usedMemory / 1024 / 1024,
                             freeMemory / 1024 / 1024));
    }
    
    /**
     * 生成性能报告
     */
    public static String generatePerformanceReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== VueKit 性能报告 ===\n\n");
        
        // 性能指标
        report.append("性能指标:\n");
        metrics.forEach((testName, total) -> {
            report.append(String.format("  %s: %dms\n", testName, total.get()));
        });
        
        // 内存使用情况
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        report.append("\n内存使用情况:\n");
        report.append(String.format("  总内存: %dMB\n", totalMemory / 1024 / 1024));
        report.append(String.format("  已用内存: %dMB\n", usedMemory / 1024 / 1024));
        report.append(String.format("  空闲内存: %dMB\n", freeMemory / 1024 / 1024));
        
        // 性能建议
        report.append("\n性能建议:\n");
        metrics.forEach((testName, total) -> {
            long threshold = getThreshold(testName);
            if (total.get() > threshold) {
                report.append(String.format("  %s: 性能较慢，建议优化\n", testName));
            }
        });
        
        report.append("\n================================");
        
        return report.toString();
    }
} 