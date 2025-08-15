package com.chu7.vuecomponentassistant.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AsyncTaskManager 单元测试
 * 
 * 测试异步任务管理器的各种功能
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class AsyncTaskManagerTest {
    
    private AsyncTaskManager taskManager;
    private int testCount = 0;
    private int passedTests = 0;
    
    /**
     * 运行所有测试
     */
    public static void main(String[] args) {
        AsyncTaskManagerTest test = new AsyncTaskManagerTest();
        test.runAllTests();
    }
    
    /**
     * 运行所有测试
     */
    public void runAllTests() {
        System.out.println("=== 开始运行 AsyncTaskManager 测试 ===\n");
        
        testBasicAsyncExecution();
        testAsyncExecutionWithReturnValue();
        testTimeoutHandling();
        testScheduledTasks();
        testThreadPoolStatus();
        testShutdown();
        
        System.out.println("\n=== 测试完成 ===");
        System.out.println("总测试数: " + testCount);
        System.out.println("通过测试: " + passedTests);
        System.out.println("失败测试: " + (testCount - passedTests));
    }
    
    /**
     * 测试基本异步执行
     */
    public void testBasicAsyncExecution() {
        testCount++;
        try {
            taskManager = new AsyncTaskManager();
            
            AtomicInteger counter = new AtomicInteger(0);
            CompletableFuture<Void> future = taskManager.executeAsync(() -> {
                try {
                    Thread.sleep(100);
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "测试任务");
            
            future.get(5, TimeUnit.SECONDS);
            
            assertTrue(counter.get() == 1, "异步任务应该执行一次");
            System.out.println("✓ testBasicAsyncExecution 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testBasicAsyncExecution 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试带返回值的异步执行
     */
    public void testAsyncExecutionWithReturnValue() {
        testCount++;
        try {
            if (taskManager == null) {
                taskManager = new AsyncTaskManager();
            }
            
            CompletableFuture<String> future = taskManager.executeAsync(() -> {
                try {
                    Thread.sleep(50);
                    return "异步任务结果";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "中断";
                }
            }, "测试返回值任务");
            
            String result = future.get(5, TimeUnit.SECONDS);
            
            assertEquals("异步任务结果", result, "异步任务应该返回正确的结果");
            System.out.println("✓ testAsyncExecutionWithReturnValue 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testAsyncExecutionWithReturnValue 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试超时处理
     */
    public void testTimeoutHandling() {
        testCount++;
        try {
            if (taskManager == null) {
                taskManager = new AsyncTaskManager();
            }
            
            CompletableFuture<Void> future = taskManager.executeAsync(() -> {
                try {
                    Thread.sleep(2000); // 2秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "超时测试任务", 1000); // 1秒超时
            
            try {
                future.get(5, TimeUnit.SECONDS);
                assertTrue(false, "应该抛出超时异常");
            } catch (Exception e) {
                // 期望超时异常
                assertTrue(e.getMessage().contains("timeout") || e.getCause() instanceof java.util.concurrent.TimeoutException, 
                          "应该抛出超时异常");
            }
            
            System.out.println("✓ testTimeoutHandling 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testTimeoutHandling 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试定时任务
     */
    public void testScheduledTasks() {
        testCount++;
        try {
            if (taskManager == null) {
                taskManager = new AsyncTaskManager();
            }
            
            AtomicInteger counter = new AtomicInteger(0);
            
            // 延迟执行
            taskManager.schedule(() -> counter.incrementAndGet(), 100, TimeUnit.MILLISECONDS);
            
            // 等待执行完成
            Thread.sleep(200);
            
            assertTrue(counter.get() == 1, "延迟任务应该执行一次");
            System.out.println("✓ testScheduledTasks 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testScheduledTasks 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试线程池状态
     */
    public void testThreadPoolStatus() {
        testCount++;
        try {
            if (taskManager == null) {
                taskManager = new AsyncTaskManager();
            }
            
            AsyncTaskManager.ThreadPoolStatus status = taskManager.getThreadPoolStatus();
            
            assertNotNull(status, "线程池状态不应该为null");
            assertTrue(status.getCorePoolSize() > 0, "核心线程数应该大于0");
            assertTrue(status.getMaximumPoolSize() > 0, "最大线程数应该大于0");
            
            System.out.println("✓ testThreadPoolStatus 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testThreadPoolStatus 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试关闭功能
     */
    public void testShutdown() {
        testCount++;
        try {
            if (taskManager == null) {
                taskManager = new AsyncTaskManager();
            }
            
            taskManager.shutdown();
            
            // 尝试提交新任务应该失败
            try {
                CompletableFuture<Void> future = taskManager.executeAsync(() -> {}, "关闭后测试");
                assertTrue(false, "关闭后不应该能提交新任务");
            } catch (Exception e) {
                // 期望异常
                assertTrue(true, "关闭后提交任务应该失败");
            }
            
            System.out.println("✓ testShutdown 通过");
            passedTests++;
        } catch (Exception e) {
            System.out.println("✗ testShutdown 失败: " + e.getMessage());
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
