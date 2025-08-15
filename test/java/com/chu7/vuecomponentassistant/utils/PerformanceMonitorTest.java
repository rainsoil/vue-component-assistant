package com.chu7.vuecomponentassistant.utils;

import java.util.Map;

/**
 * PerformanceMonitor 单元测试
 * 
 * 测试性能监控系统的各种功能
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class PerformanceMonitorTest {
    
    private PerformanceMonitor monitor;
    private int testCount = 0;
    private int passedTests = 0;
    
    /**
     * 运行所有测试
     */
    public static void main(String[] args) {
        PerformanceMonitorTest test = new PerformanceMonitorTest();
        test.runAllTests();
    }
    
    /**
     * 运行所有测试
     */
    public void runAllTests() {
        System.out.println("=== 开始运行 PerformanceMonitor 测试 ===\n");
        
        testBasicMonitoring();
        testPerformanceThresholds();
        testStatisticsCollection();
        testReportGeneration();
        testEnableDisable();
        testResetStats();
        
        System.out.println("\n=== 测试完成 ===");
        System.out.println("总测试数: " + testCount);
        System.out.println("通过测试: " + passedTests);
        System.out.println("失败测试: " + (testCount - passedTests));
    }
    
    /**
     * 测试基本监控功能
     */
    public void testBasicMonitoring() {
        testCount++;
        try {
            monitor = new PerformanceMonitor();
            
            // 测试成功操作
            PerformanceMonitor.MonitorContext context1 = monitor.startMonitoring("测试操作1");
            Thread.sleep(50);
            monitor.finishMonitoring(context1, true);
            
            // 测试失败操作
            PerformanceMonitor.MonitorContext context2 = monitor.startMonitoring("测试操作2");
            Thread.sleep(30);
            monitor.finishMonitoring(context2, false);
            
            // 验证统计信息
            PerformanceMonitor.OverallStats overall = monitor.getOverallStats();
            assertEquals(2L, overall.getTotalOperations(), "总操作数应该是2");
            assertEquals(1L, overall.getTotalErrors(), "总错误数应该是1");
            assertTrue(overall.getTotalDuration() > 0, "总耗时应该大于0");
            
            System.out.println("✓ testBasicMonitoring 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testBasicMonitoring 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试性能阈值
     */
    public void testPerformanceThresholds() {
        testCount++;
        try {
            if (monitor == null) {
                monitor = new PerformanceMonitor();
            }
            
            // 测试慢操作（超过1秒阈值）
            PerformanceMonitor.MonitorContext context = monitor.startMonitoring("慢操作测试");
            Thread.sleep(1100);
            monitor.finishMonitoring(context, true);
            
            // 验证统计信息
            PerformanceMonitor.OperationStats stats = monitor.getOperationStats("慢操作测试");
            assertTrue(stats.getAverageDuration() >= 1000, "慢操作应该被记录");
            
            System.out.println("✓ testPerformanceThresholds 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testPerformanceThresholds 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试统计信息收集
     */
    public void testStatisticsCollection() {
        testCount++;
        try {
            if (monitor == null) {
                monitor = new PerformanceMonitor();
            }
            
            // 执行多个操作
            for (int i = 0; i < 5; i++) {
                PerformanceMonitor.MonitorContext context = monitor.startMonitoring("批量操作");
                Thread.sleep(10);
                monitor.finishMonitoring(context, i % 2 == 0); // 交替成功/失败
            }
            
            // 验证统计信息
            PerformanceMonitor.OperationStats stats = monitor.getOperationStats("批量操作");
            assertEquals(5L, stats.getCallCount(), "调用次数应该是5");
            assertEquals(3L, stats.getSuccessCount(), "成功次数应该是3");
            assertEquals(2L, stats.getFailureCount(), "失败次数应该是2");
            assertTrue(stats.getMinDuration() > 0, "最小耗时应该大于0");
            assertTrue(stats.getMaxDuration() > 0, "最大耗时应该大于0");
            
            System.out.println("✓ testStatisticsCollection 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testStatisticsCollection 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试报告生成
     */
    public void testReportGeneration() {
        testCount++;
        try {
            if (monitor == null) {
                monitor = new PerformanceMonitor();
            }
            
            // 执行一些操作
            PerformanceMonitor.MonitorContext context = monitor.startMonitoring("报告测试操作");
            Thread.sleep(20);
            monitor.finishMonitoring(context, true);
            
            // 生成报告
            String report = monitor.generatePerformanceReport();
            
            // 验证报告内容
            assertNotNull(report, "报告不应该为null");
            assertTrue(report.contains("VueKit 性能监控报告"), "报告应该包含标题");
            assertTrue(report.contains("报告测试操作"), "报告应该包含操作名称");
            assertTrue(report.contains("总操作数"), "报告应该包含总体统计");
            
            System.out.println("✓ testReportGeneration 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testReportGeneration 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试启用/禁用功能
     */
    public void testEnableDisable() {
        testCount++;
        try {
            if (monitor == null) {
                monitor = new PerformanceMonitor();
            }
            
            // 禁用监控
            monitor.setEnabled(false);
            assertFalse(monitor.isEnabled(), "监控应该被禁用");
            
            // 禁用状态下执行操作
            PerformanceMonitor.MonitorContext context = monitor.startMonitoring("禁用状态测试");
            Thread.sleep(10);
            monitor.finishMonitoring(context, true);
            
            // 验证操作没有被记录
            PerformanceMonitor.OverallStats stats = monitor.getOverallStats();
            long currentTotal = stats.getTotalOperations();
            
            // 重新启用
            monitor.setEnabled(true);
            assertTrue(monitor.isEnabled(), "监控应该被启用");
            
            // 启用状态下执行操作
            context = monitor.startMonitoring("启用状态测试");
            Thread.sleep(10);
            monitor.finishMonitoring(context, true);
            
            // 验证操作被记录
            stats = monitor.getOverallStats();
            assertTrue(stats.getTotalOperations() > currentTotal, "启用后操作应该被记录");
            
            System.out.println("✓ testEnableDisable 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testEnableDisable 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试统计信息重置
     */
    public void testResetStats() {
        testCount++;
        try {
            if (monitor == null) {
                monitor = new PerformanceMonitor();
            }
            
            // 执行一些操作
            PerformanceMonitor.MonitorContext context = monitor.startMonitoring("重置测试操作");
            Thread.sleep(10);
            monitor.finishMonitoring(context, true);
            
            // 验证有统计数据
            PerformanceMonitor.OverallStats stats1 = monitor.getOverallStats();
            assertTrue(stats1.getTotalOperations() > 0, "应该有统计数据");
            
            // 重置统计
            monitor.resetStats();
            
            // 验证统计被重置
            PerformanceMonitor.OverallStats stats2 = monitor.getOverallStats();
            assertEquals(0L, stats2.getTotalOperations(), "总操作数应该被重置为0");
            assertEquals(0L, stats2.getTotalErrors(), "总错误数应该被重置为0");
            assertEquals(0L, stats2.getTotalDuration(), "总耗时应该被重置为0");
            
            System.out.println("✓ testResetStats 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testResetStats 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 自定义断言方法
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
    
    private void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
    
    private void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - 期望: " + expected + ", 实际: " + actual);
        }
    }
    
    private void assertNull(Object object, String message) {
        if (object != null) {
            throw new AssertionError(message);
        }
    }
    
    private void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message);
        }
    }
}
