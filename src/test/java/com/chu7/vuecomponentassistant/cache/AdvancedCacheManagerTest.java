package com.chu7.vuecomponentassistant.cache;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import java.util.HashMap;
import java.util.Map;

/**
 * AdvancedCacheManager 单元测试
 * 
 * 测试多级缓存管理器的各种功能，包括：
 * - 基本的缓存操作（get/put/remove）
 * - 多级缓存策略
 * - 过期策略
 * - 统计信息
 * - 缓存预热
 * - 内存管理
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class AdvancedCacheManagerTest {
    
    private AdvancedCacheManager cacheManager;
    private int testCount = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    
    /**
     * 运行所有测试
     */
    public void runAllTests() {
        System.out.println("=== 开始运行 AdvancedCacheManager 测试 ===");
        
        try {
            testBasicCacheOperations();
            testCacheExpiration();
            testMultiLevelCache();
            testCacheStatistics();
            testCacheRemoval();
            testCacheClear();
            testCacheWarmUp();
            testTimeToLive();
            testNullKeyAndValue();
            testConcurrentAccess();
            testMemoryManagement();
            testCachePromotion();
            testShutdown();
            testPerformanceMetrics();
            
        } catch (Exception e) {
            System.err.println("测试执行过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 输出测试结果摘要
        System.out.println("=== 测试结果摘要 ===");
        System.out.println("总测试数: " + testCount);
        System.out.println("通过: " + passedTests);
        System.out.println("失败: " + failedTests);
        System.out.println("成功率: " + String.format("%.2f%%", (double) passedTests / testCount * 100));
    }
    
    /**
     * 断言方法
     */
    private void assertTrue(boolean condition, String message) {
        testCount++;
        if (condition) {
            passedTests++;
            System.out.println("✓ " + message);
        } else {
            failedTests++;
            System.err.println("✗ " + message);
        }
    }
    
    private void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
    
    private void assertEquals(Object expected, Object actual, String message) {
        testCount++;
        if (expected == null ? actual == null : expected.equals(actual)) {
            passedTests++;
            System.out.println("✓ " + message + " (期望: " + expected + ", 实际: " + actual + ")");
        } else {
            failedTests++;
            System.err.println("✗ " + message + " (期望: " + expected + ", 实际: " + actual + ")");
        }
    }
    
    private void assertEquals(double expected, double actual, double delta, String message) {
        testCount++;
        if (Math.abs(expected - actual) <= delta) {
            passedTests++;
            System.out.println("✓ " + message + " (期望: " + expected + ", 实际: " + actual + ")");
        } else {
            failedTests++;
            System.err.println("✗ " + message + " (期望: " + expected + ", 实际: " + actual + ")");
        }
    }
    
    private void assertNull(Object object, String message) {
        testCount++;
        if (object == null) {
            passedTests++;
            System.out.println("✓ " + message);
        } else {
            failedTests++;
            System.err.println("✗ " + message + " (实际: " + object + ")");
        }
    }
    
    private void assertNotNull(Object object, String message) {
        testCount++;
        if (object != null) {
            passedTests++;
            System.out.println("✓ " + message);
        } else {
            failedTests++;
            System.err.println("✗ " + message);
        }
    }
    
    /**
     * 设置测试环境
     */
    private void setUp() {
        // 使用较短的过期时间进行测试
        cacheManager = new AdvancedCacheManager(
            100,           // 最大内存缓存项数
            1000,          // 内存缓存过期时间：1秒
            5000           // 磁盘缓存过期时间：5秒
        );
    }
    
    /**
     * 清理测试环境
     */
    private void tearDown() {
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
    }
    
    /**
     * 测试基本的缓存操作
     */
    public void testBasicCacheOperations() {
        setUp();
        
        try {
            // 测试基本的put和get操作
            cacheManager.put("key1", "value1");
            cacheManager.put("key2", "value2");
            
            assertEquals("value1", cacheManager.get("key1"), "测试key1的值");
            assertEquals("value2", cacheManager.get("key2"), "测试key2的值");
            assertNull(cacheManager.get("nonexistent"), "测试不存在的键返回null");
            
            // 测试contains方法
            assertTrue(cacheManager.contains("key1"), "测试key1存在");
            assertFalse(cacheManager.contains("nonexistent"), "测试不存在的键不存在");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试缓存过期
     */
    public void testCacheExpiration() {
        setUp();
        
        try {
            // 测试缓存过期
            cacheManager.put("expiring_key", "expiring_value", 100); // 100ms过期
            
            // 立即访问应该成功
            assertEquals("expiring_value", cacheManager.get("expiring_key"), "测试过期前访问");
            
            // 等待过期
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 过期后应该返回null
            assertNull(cacheManager.get("expiring_key"), "测试过期后访问返回null");
            assertFalse(cacheManager.contains("expiring_key"), "测试过期后键不存在");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试多级缓存策略
     */
    public void testMultiLevelCache() {
        setUp();
        
        try {
            // 测试多级缓存策略
            cacheManager.put("multi_level_key", "multi_level_value");
            
            // 第一次访问应该从L1缓存获取
            Object value1 = cacheManager.get("multi_level_key");
            assertEquals("multi_level_value", value1, "测试多级缓存值");
            
            // 检查缓存统计
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            assertEquals(1L, stats.getL1Hits(), "测试L1缓存命中数");
            assertEquals(0L, stats.getL2Hits(), "测试L2缓存命中数");
            assertEquals(0L, stats.getMisses(), "测试缓存未命中数");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试缓存统计信息
     */
    public void testCacheStatistics() {
        setUp();
        
        try {
            // 测试统计信息记录
            cacheManager.put("stat_key1", "stat_value1");
            cacheManager.put("stat_key2", "stat_value2");
            
            // 访问缓存项
            cacheManager.get("stat_key1");
            cacheManager.get("stat_key2");
            cacheManager.get("stat_key1"); // 重复访问
            cacheManager.get("nonexistent_key");
            
            // 检查统计信息
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            assertEquals(3L, stats.getTotalAccesses(), "测试总访问数");
            assertEquals(2L, stats.getL1Hits(), "测试L1命中数");
            assertEquals(0L, stats.getL2Hits(), "测试L2命中数");
            assertEquals(1L, stats.getMisses(), "测试未命中数");
            assertEquals(2L, stats.getPuts(), "测试设置操作数");
            
            // 检查命中率
            double expectedHitRate = 2.0 / 3.0;
            assertEquals(expectedHitRate, stats.getHitRate(), 0.001, "测试总体命中率");
            assertEquals(expectedHitRate, stats.getL1HitRate(), 0.001, "测试L1命中率");
            assertEquals(0.0, stats.getL2HitRate(), 0.001, "测试L2命中率");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试缓存项删除
     */
    public void testCacheRemoval() {
        setUp();
        
        try {
            // 测试缓存项删除
            cacheManager.put("remove_key", "remove_value");
            assertTrue(cacheManager.contains("remove_key"), "测试删除前键存在");
            
            // 删除缓存项
            assertTrue(cacheManager.remove("remove_key"), "测试删除操作成功");
            assertFalse(cacheManager.contains("remove_key"), "测试删除后键不存在");
            
            // 删除不存在的键应该返回false
            assertFalse(cacheManager.remove("nonexistent_key"), "测试删除不存在的键");
            
            // 检查统计信息
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            assertEquals(1L, stats.getRemoves(), "测试删除操作统计");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试清空缓存
     */
    public void testCacheClear() {
        setUp();
        
        try {
            // 测试清空缓存
            cacheManager.put("clear_key1", "clear_value1");
            cacheManager.put("clear_key2", "clear_value2");
            
            assertEquals(2, cacheManager.getCacheSizes().get("total"), "测试清空前缓存大小");
            
            cacheManager.clear();
            
            assertEquals(0, cacheManager.getCacheSizes().get("total"), "测试清空后缓存大小");
            assertFalse(cacheManager.contains("clear_key1"), "测试清空后key1不存在");
            assertFalse(cacheManager.contains("clear_key2"), "测试清空后key2不存在");
            
            // 检查统计信息是否重置
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            assertEquals(0L, stats.getTotalAccesses(), "测试统计信息重置");
            assertEquals(0L, stats.getPuts(), "测试设置操作统计重置");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试缓存预热
     */
    public void testCacheWarmUp() {
        setUp();
        
        try {
            // 测试缓存预热
            Map<String, Object> warmUpData = new HashMap<>();
            warmUpData.put("warm_key1", "warm_value1");
            warmUpData.put("warm_key2", "warm_value2");
            warmUpData.put("warm_key3", "warm_value3");
            
            cacheManager.warmUp(warmUpData, 10000); // 10秒过期
            
            // 验证预热的数据是否可用
            assertEquals("warm_value1", cacheManager.get("warm_key1"), "测试预热key1");
            assertEquals("warm_value2", cacheManager.get("warm_key2"), "测试预热key2");
            assertEquals("warm_value3", cacheManager.get("warm_key3"), "测试预热key3");
            
            // 检查缓存大小
            assertEquals(3, cacheManager.getCacheSizes().get("total"), "测试预热后缓存大小");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试剩余生存时间
     */
    public void testTimeToLive() {
        setUp();
        
        try {
            // 测试剩余生存时间
            cacheManager.put("ttl_key", "ttl_value", 2000); // 2秒过期
            
            long ttl = cacheManager.getTimeToLive("ttl_key");
            assertTrue(ttl > 0 && ttl <= 2000, "测试TTL初始值");
            
            // 等待一段时间后检查TTL
            try {
                Thread.sleep(1000);
                ttl = cacheManager.getTimeToLive("ttl_key");
                assertTrue(ttl > 0 && ttl <= 1000, "测试TTL减少");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 过期后TTL应该为0
            try {
                Thread.sleep(1500);
                ttl = cacheManager.getTimeToLive("ttl_key");
                assertEquals(0L, ttl, "测试过期后TTL为0");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试null键和值的处理
     */
    public void testNullKeyAndValue() {
        setUp();
        
        try {
            // 测试null键和值的处理
            cacheManager.put(null, "value");
            cacheManager.put("key", null);
            cacheManager.put("", "value");
            
            // 这些操作应该被忽略，不会抛出异常
            assertNull(cacheManager.get(null), "测试null键返回null");
            assertNull(cacheManager.get(""), "测试空键返回null");
            assertFalse(cacheManager.contains(null), "测试null键不存在");
            assertFalse(cacheManager.contains(""), "测试空键不存在");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试并发访问
     */
    public void testConcurrentAccess() {
        setUp();
        
        try {
            // 测试并发访问
            int threadCount = 5; // 减少线程数以避免测试时间过长
            int operationsPerThread = 20;
            
            Thread[] threads = new Thread[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "concurrent_key_" + threadId + "_" + j;
                        String value = "concurrent_value_" + threadId + "_" + j;
                        
                        cacheManager.put(key, value);
                        cacheManager.get(key);
                        
                        if (j % 10 == 0) {
                            cacheManager.remove(key);
                        }
                    }
                });
            }
            
            // 启动所有线程
            for (Thread thread : threads) {
                thread.start();
            }
            
            // 等待所有线程完成
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // 验证没有异常发生，缓存管理器仍然可用
            assertNotNull(cacheManager.getStatistics(), "测试并发后统计信息可用");
            assertNotNull(cacheManager.getCacheSizes(), "测试并发后缓存大小信息可用");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试内存管理
     */
    public void testMemoryManagement() {
        setUp();
        
        try {
            // 测试内存管理（清理策略）
            // 添加超过最大内存大小的缓存项
            for (int i = 0; i < 150; i++) {
                cacheManager.put("memory_key_" + i, "memory_value_" + i);
            }
            
            // 检查内存缓存大小是否被限制
            Map<String, Integer> sizes = cacheManager.getCacheSizes();
            assertTrue(sizes.get("memoryCache") <= 100, "测试内存缓存大小限制");
            
            // 验证一些缓存项仍然可用
            assertNotNull(cacheManager.get("memory_key_0"), "测试内存管理后key0可用");
            assertNotNull(cacheManager.get("memory_key_100"), "测试内存管理后key100可用");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试缓存项提升
     */
    public void testCachePromotion() {
        setUp();
        
        try {
            // 测试缓存项从L2提升到L1的逻辑
            // 通过多次访问某些键来增加访问频率
            cacheManager.put("promote_key", "promote_value");
            
            // 多次访问同一个键
            for (int i = 0; i < 20; i++) {
                cacheManager.get("promote_key");
            }
            
            // 检查统计信息
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            assertTrue(stats.getL1Hits() > 0, "测试缓存提升后L1命中");
            
            // 验证缓存项仍然可用
            assertEquals("promote_value", cacheManager.get("promote_key"), "测试提升后缓存项可用");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 测试关闭功能
     */
    public void testShutdown() {
        setUp();
        
        try {
            // 测试关闭功能
            cacheManager.put("shutdown_key", "shutdown_value");
            
            // 关闭缓存管理器
            cacheManager.shutdown();
            
            // 关闭后应该无法访问缓存
            assertNull(cacheManager.get("shutdown_key"), "测试关闭后无法访问缓存");
            assertFalse(cacheManager.contains("shutdown_key"), "测试关闭后键不存在");
            
            // 统计信息应该被重置
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            assertEquals(0L, stats.getTotalAccesses(), "测试关闭后统计信息重置");
            
        } finally {
            // 不需要tearDown，因为已经shutdown了
        }
    }
    
    /**
     * 测试性能指标
     */
    public void testPerformanceMetrics() {
        setUp();
        
        try {
            // 测试性能指标记录
            long startTime = System.currentTimeMillis();
            
            cacheManager.put("perf_key", "perf_value");
            Object value = cacheManager.get("perf_key");
            
            long endTime = System.currentTimeMillis();
            
            assertEquals("perf_value", value, "测试性能指标值正确");
            
            // 检查访问时间是否被记录
            AdvancedCacheManager.CacheStatistics stats = cacheManager.getStatistics();
            Map<String, Long> accessTimes = stats.getAccessTimes();
            
            // 验证访问时间被记录
            assertFalse(accessTimes.isEmpty(), "测试访问时间被记录");
            
        } finally {
            tearDown();
        }
    }
    
    /**
     * 主方法，用于直接运行测试
     */
    public static void main(String[] args) {
        AdvancedCacheManagerTest test = new AdvancedCacheManagerTest();
        test.runAllTests();
    }
}
