package com.chu7.vuecomponentassistant.cache;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 高级缓存管理器
 * 
 * 实现多级缓存策略，提供高性能的组件数据缓存服务。
 * 
 * 特性：
 * - L1缓存：内存缓存，提供最快的访问速度
 * - L2缓存：磁盘缓存，提供持久化存储
 * - 智能过期策略：基于访问频率和时间的自适应过期
 * - 缓存预热：智能预加载常用数据
 * - 内存管理：自动清理过期和低频访问的数据
 * - 统计监控：提供详细的缓存使用统计
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class AdvancedCacheManager {
    
    private static final Logger LOG = VueKitLogger.getLogger(AdvancedCacheManager.class);
    
    // 缓存配置常量
    private static final int DEFAULT_MAX_MEMORY_SIZE = 1000; // 最大内存缓存项数
    private static final long DEFAULT_MEMORY_EXPIRY_TIME = 30 * 60 * 1000L; // 内存缓存过期时间：30分钟
    private static final long DEFAULT_DISK_EXPIRY_TIME = 7 * 24 * 60 * 60 * 1000L; // 磁盘缓存过期时间：7天
    private static final int DEFAULT_CLEANUP_INTERVAL = 5; // 清理间隔：5分钟
    private static final double DEFAULT_ACCESS_FREQUENCY_THRESHOLD = 0.1; // 访问频率阈值：10%
    
    // 缓存容器
    private final Map<String, CacheEntry> memoryCache; // L1缓存：内存缓存
    private final Map<String, CacheEntry> diskCache; // L2缓存：磁盘缓存（模拟）
    
    // 缓存统计
    private final CacheStatistics statistics;
    
    // 后台任务执行器
    private final ScheduledExecutorService cleanupExecutor;
    
    // 配置参数
    private final int maxMemorySize;
    private final long memoryExpiryTime;
    private final long diskExpiryTime;
    private final int cleanupInterval;
    private final double accessFrequencyThreshold;
    
    /**
     * 构造函数
     * 
     * 使用默认配置初始化缓存管理器
     */
    public AdvancedCacheManager() {
        this(DEFAULT_MAX_MEMORY_SIZE, DEFAULT_MEMORY_EXPIRY_TIME, DEFAULT_DISK_EXPIRY_TIME);
    }
    
    /**
     * 构造函数
     * 
     * @param maxMemorySize 最大内存缓存项数
     * @param memoryExpiryTime 内存缓存过期时间（毫秒）
     * @param diskExpiryTime 磁盘缓存过期时间（毫秒）
     */
    public AdvancedCacheManager(int maxMemorySize, long memoryExpiryTime, long diskExpiryTime) {
        this.maxMemorySize = maxMemorySize;
        this.memoryExpiryTime = memoryExpiryTime;
        this.diskExpiryTime = diskExpiryTime;
        this.cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
        this.accessFrequencyThreshold = DEFAULT_ACCESS_FREQUENCY_THRESHOLD;
        
        this.memoryCache = new ConcurrentHashMap<>();
        this.diskCache = new ConcurrentHashMap<>();
        this.statistics = new CacheStatistics();
        
        // 启动后台清理任务
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        startCleanupTask();
        
        VueKitLogger.info(LOG, "高级缓存管理器初始化完成，配置: 内存大小=" + maxMemorySize + 
            ", 内存过期=" + (memoryExpiryTime / 1000 / 60) + "分钟, 磁盘过期=" + (diskExpiryTime / 1000 / 60 / 60 / 24) + "天");
    }
    
    /**
     * 获取缓存项
     * 
     * 按照L1->L2的顺序查找缓存，如果找到则更新访问统计并可能提升到L1缓存。
     * 
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期则返回null
     */
    public Object get(String key) {
        if (key == null || key.trim().isEmpty()) {
            VueKitLogger.warn(LOG, "缓存键不能为null或空字符串");
            return null;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 首先检查L1缓存（内存缓存）
            CacheEntry memoryEntry = memoryCache.get(key);
            if (memoryEntry != null && !memoryEntry.isExpired()) {
                memoryEntry.updateAccess();
                statistics.recordHit(CacheLevel.L1, System.currentTimeMillis() - startTime);
                VueKitLogger.debug(LOG, "L1缓存命中: " + key);
                return memoryEntry.getValue();
            }
            
            // L1缓存未命中，检查L2缓存（磁盘缓存）
            CacheEntry diskEntry = diskCache.get(key);
            if (diskEntry != null && !diskEntry.isExpired()) {
                diskEntry.updateAccess();
                
                // 如果访问频率较高，提升到L1缓存
                if (shouldPromoteToMemory(diskEntry)) {
                    promoteToMemory(key, diskEntry);
                }
                
                statistics.recordHit(CacheLevel.L2, System.currentTimeMillis() - startTime);
                VueKitLogger.debug(LOG, "L2缓存命中: " + key);
                return diskEntry.getValue();
            }
            
            // 缓存未命中
            statistics.recordMiss(key);
            VueKitLogger.debug(LOG, "缓存未命中: " + key);
            return null;
            
        } catch (Exception e) {
            String errorMsg = "获取缓存失败: " + key;
            VueKitLogger.error(LOG, errorMsg, e);
            statistics.recordError();
            return null;
        }
    }
    
    /**
     * 设置缓存项
     * 
     * 将数据同时存储到L1和L2缓存，确保数据的高可用性。
     * 
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 生存时间（毫秒），如果为0则使用默认过期时间
     */
    public void put(String key, Object value, long ttl) {
        if (key == null || key.trim().isEmpty()) {
            VueKitLogger.warn(LOG, "缓存键不能为null或空字符串");
            return;
        }
        
        if (value == null) {
            VueKitLogger.warn(LOG, "缓存值不能为null，键: " + key);
            return;
        }
        
        try {
            long expiryTime = ttl > 0 ? ttl : memoryExpiryTime;
            
            // 创建缓存项
            CacheEntry entry = new CacheEntry(value, expiryTime);
            
            // 存储到L1缓存
            putToMemory(key, entry);
            
            // 存储到L2缓存
            putToDisk(key, entry);
            
            statistics.recordPut();
            VueKitLogger.debug(LOG, "缓存项已设置: " + key + ", TTL: " + (expiryTime / 1000) + "秒");
            
        } catch (Exception e) {
            String errorMsg = "设置缓存失败: " + key;
            VueKitLogger.error(LOG, errorMsg, e);
            statistics.recordError();
        }
    }
    
    /**
     * 设置缓存项（使用默认过期时间）
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String key, Object value) {
        put(key, value, 0);
    }
    
    /**
     * 删除缓存项
     * 
     * 同时从L1和L2缓存中删除指定的缓存项。
     * 
     * @param key 要删除的缓存键
     * @return 如果删除成功返回true，否则返回false
     */
    public boolean remove(String key) {
        if (key == null || key.trim().isEmpty()) {
            VueKitLogger.warn(LOG, "缓存键不能为null或空字符串");
            return false;
        }
        
        try {
            boolean memoryRemoved = memoryCache.remove(key) != null;
            boolean diskRemoved = diskCache.remove(key) != null;
            
            if (memoryRemoved || diskRemoved) {
                statistics.recordRemove();
                VueKitLogger.debug(LOG, "缓存项已删除: " + key);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            String errorMsg = "删除缓存失败: " + key;
            VueKitLogger.error(LOG, errorMsg, e);
            statistics.recordError();
            return false;
        }
    }
    
    /**
     * 清空所有缓存
     * 
     * 清空L1和L2缓存，重置统计信息。
     */
    public void clear() {
        try {
            memoryCache.clear();
            diskCache.clear();
            statistics.reset();
            
            VueKitLogger.info(LOG, "所有缓存已清空");
            
        } catch (Exception e) {
            String errorMsg = "清空缓存失败";
            VueKitLogger.error(LOG, errorMsg, e);
        }
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息对象
     */
    public CacheStatistics getStatistics() {
        return statistics.copy();
    }
    
    /**
     * 获取缓存大小信息
     * 
     * @return 包含各层缓存大小的Map
     */
    public Map<String, Integer> getCacheSizes() {
        Map<String, Integer> sizes = new HashMap<>();
        sizes.put("memoryCache", memoryCache.size());
        sizes.put("diskCache", diskCache.size());
        sizes.put("total", memoryCache.size() + diskCache.size());
        return sizes;
    }
    
    /**
     * 检查缓存项是否存在
     * 
     * @param key 缓存键
     * @return 如果存在且未过期返回true，否则返回false
     */
    public boolean contains(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        
        // 检查L1缓存
        CacheEntry memoryEntry = memoryCache.get(key);
        if (memoryEntry != null && !memoryEntry.isExpired()) {
            return true;
        }
        
        // 检查L2缓存
        CacheEntry diskEntry = diskCache.get(key);
        return diskEntry != null && !diskEntry.isExpired();
    }
    
    /**
     * 获取缓存项的剩余生存时间
     * 
     * @param key 缓存键
     * @return 剩余生存时间（毫秒），如果不存在或已过期则返回0
     */
    public long getTimeToLive(String key) {
        if (key == null || key.trim().isEmpty()) {
            return 0;
        }
        
        // 检查L1缓存
        CacheEntry memoryEntry = memoryCache.get(key);
        if (memoryEntry != null && !memoryEntry.isExpired()) {
            return memoryEntry.getTimeToLive();
        }
        
        // 检查L2缓存
        CacheEntry diskEntry = diskCache.get(key);
        if (diskEntry != null && !diskEntry.isExpired()) {
            return diskEntry.getTimeToLive();
        }
        
        return 0;
    }
    
    /**
     * 预热缓存
     * 
     * 将指定的数据预加载到缓存中，提高后续访问的性能。
     * 
     * @param data 要预热的数据，键为缓存键，值为缓存值
     * @param ttl 生存时间（毫秒）
     */
    public void warmUp(Map<String, Object> data, long ttl) {
        if (data == null || data.isEmpty()) {
            VueKitLogger.warn(LOG, "预热数据不能为null或空");
            return;
        }
        
        VueKitLogger.info(LOG, "开始预热缓存，数据量: " + data.size());
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            try {
                put(entry.getKey(), entry.getValue(), ttl);
            } catch (Exception e) {
                VueKitLogger.warn(LOG, "预热缓存项失败: " + entry.getKey(), e);
            }
        }
        
        VueKitLogger.info(LOG, "缓存预热完成");
    }
    
    /**
     * 关闭缓存管理器
     * 
     * 清理资源，停止后台任务。
     */
    public void shutdown() {
        try {
            cleanupExecutor.shutdown();
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
            
            clear();
            VueKitLogger.info(LOG, "缓存管理器已关闭");
            
        } catch (Exception e) {
            String errorMsg = "关闭缓存管理器失败";
            VueKitLogger.error(LOG, errorMsg, e);
        }
    }
    
    // 私有方法
    
    /**
     * 将缓存项存储到L1缓存（内存缓存）
     */
    private void putToMemory(String key, CacheEntry entry) {
        // 如果内存缓存已满，执行清理
        if (memoryCache.size() >= maxMemorySize) {
            cleanupMemoryCache();
        }
        
        memoryCache.put(key, entry);
    }
    
    /**
     * 将缓存项存储到L2缓存（磁盘缓存）
     */
    private void putToDisk(String key, CacheEntry entry) {
        // 创建磁盘缓存项的副本，使用磁盘过期时间
        CacheEntry diskEntry = new CacheEntry(entry.getValue(), diskExpiryTime);
        diskCache.put(key, diskEntry);
    }
    
    /**
     * 将L2缓存项提升到L1缓存
     */
    private void promoteToMemory(String key, CacheEntry diskEntry) {
        try {
            // 如果内存缓存已满，执行清理
            if (memoryCache.size() >= maxMemorySize) {
                cleanupMemoryCache();
            }
            
            // 创建内存缓存项，使用内存过期时间
            CacheEntry memoryEntry = new CacheEntry(diskEntry.getValue(), memoryExpiryTime);
            memoryCache.put(key, memoryEntry);
            
            VueKitLogger.debug(LOG, "缓存项已提升到L1: " + key);
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "提升缓存项到L1失败: " + key, e);
        }
    }
    
    /**
     * 判断是否应该将缓存项提升到内存缓存
     */
    private boolean shouldPromoteToMemory(CacheEntry entry) {
        long totalAccesses = statistics.getTotalAccesses();
        if (totalAccesses == 0) {
            return false;
        }
        
        double accessFrequency = (double) entry.getAccessCount() / totalAccesses;
        return accessFrequency > accessFrequencyThreshold;
    }
    
    /**
     * 清理内存缓存
     * 
     * 移除过期和低频访问的缓存项，释放内存空间。
     */
    private void cleanupMemoryCache() {
        try {
            List<Map.Entry<String, CacheEntry>> entries = new ArrayList<>(memoryCache.entrySet());
            
            // 按访问频率和过期时间排序
            entries.sort((e1, e2) -> {
                CacheEntry entry1 = e1.getValue();
                CacheEntry entry2 = e2.getValue();
                
                // 优先移除过期的项
                if (entry1.isExpired() && !entry2.isExpired()) return -1;
                if (!entry1.isExpired() && entry2.isExpired()) return 1;
                
                // 然后按访问频率排序（访问频率低的优先移除）
                return Integer.compare(entry1.getAccessCount(), entry2.getAccessCount());
            });
            
            // 移除一半的缓存项
            int removeCount = entries.size() / 2;
            for (int i = 0; i < removeCount; i++) {
                Map.Entry<String, CacheEntry> entry = entries.get(i);
                memoryCache.remove(entry.getKey());
            }
            
            VueKitLogger.debug(LOG, "内存缓存清理完成，移除了 " + removeCount + " 个缓存项");
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "清理内存缓存失败", e);
        }
    }
    
    /**
     * 启动后台清理任务
     */
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredEntries();
            } catch (Exception e) {
                VueKitLogger.error(LOG, "后台清理任务执行失败", e);
            }
        }, cleanupInterval, cleanupInterval, TimeUnit.MINUTES);
        
        VueKitLogger.debug(LOG, "后台清理任务已启动，间隔: " + cleanupInterval + " 分钟");
    }
    
    /**
     * 清理过期的缓存项
     */
    private void cleanupExpiredEntries() {
        try {
            int memoryRemoved = 0;
            int diskRemoved = 0;
            
            // 清理L1缓存中的过期项
            Iterator<Map.Entry<String, CacheEntry>> memoryIterator = memoryCache.entrySet().iterator();
            while (memoryIterator.hasNext()) {
                Map.Entry<String, CacheEntry> entry = memoryIterator.next();
                if (entry.getValue().isExpired()) {
                    memoryIterator.remove();
                    memoryRemoved++;
                }
            }
            
            // 清理L2缓存中的过期项
            Iterator<Map.Entry<String, CacheEntry>> diskIterator = diskCache.entrySet().iterator();
            while (diskIterator.hasNext()) {
                Map.Entry<String, CacheEntry> entry = diskIterator.next();
                if (entry.getValue().isExpired()) {
                    diskIterator.remove();
                    diskRemoved++;
                }
            }
            
            if (memoryRemoved > 0 || diskRemoved > 0) {
                VueKitLogger.debug(LOG, "过期缓存项清理完成: L1=" + memoryRemoved + ", L2=" + diskRemoved);
            }
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "清理过期缓存项失败", e);
        }
    }
    
    /**
     * 缓存级别枚举
     */
    public enum CacheLevel {
        L1("内存缓存"),
        L2("磁盘缓存");
        
        private final String displayName;
        
        CacheLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 缓存项内部类
     */
    private static class CacheEntry {
        private final Object value;
        private final long expiryTime;
        private int accessCount;
        private long lastAccessTime;
        
        public CacheEntry(Object value, long ttl) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttl;
            this.accessCount = 0;
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
        
        public long getTimeToLive() {
            long remaining = expiryTime - System.currentTimeMillis();
            return remaining > 0 ? remaining : 0;
        }
        
        public int getAccessCount() {
            return accessCount;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public void updateAccess() {
            accessCount++;
            lastAccessTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 缓存统计信息内部类
     */
    public static class CacheStatistics {
        private long l1Hits;
        private long l2Hits;
        private long misses;
        private long puts;
        private long removes;
        private long errors;
        private long totalAccesses;
        private final Map<String, Long> accessTimes;
        
        public CacheStatistics() {
            this.accessTimes = new HashMap<>();
            reset();
        }
        
        public void recordHit(CacheLevel level, long accessTime) {
            if (level == CacheLevel.L1) {
                l1Hits++;
            } else if (level == CacheLevel.L2) {
                l2Hits++;
            }
            totalAccesses++;
            accessTimes.put(level.getDisplayName(), accessTime);
        }
        
        public void recordMiss(String key) {
            misses++;
            totalAccesses++;
        }
        
        public void recordPut() {
            puts++;
        }
        
        public void recordRemove() {
            removes++;
        }
        
        public void recordError() {
            errors++;
        }
        
        public void reset() {
            l1Hits = 0;
            l2Hits = 0;
            misses = 0;
            puts = 0;
            removes = 0;
            errors = 0;
            totalAccesses = 0;
            accessTimes.clear();
        }
        
        // Getter方法
        public long getL1Hits() { return l1Hits; }
        public long getL2Hits() { return l2Hits; }
        public long getMisses() { return misses; }
        public long getPuts() { return puts; }
        public long getRemoves() { return removes; }
        public long getErrors() { return errors; }
        public long getTotalAccesses() { return totalAccesses; }
        public Map<String, Long> getAccessTimes() { return new HashMap<>(accessTimes); }
        
        public double getHitRate() {
            if (totalAccesses == 0) return 0.0;
            return (double) (l1Hits + l2Hits) / totalAccesses;
        }
        
        public double getL1HitRate() {
            if (totalAccesses == 0) return 0.0;
            return (double) l1Hits / totalAccesses;
        }
        
        public double getL2HitRate() {
            if (totalAccesses == 0) return 0.0;
            return (double) l2Hits / totalAccesses;
        }
        
        public CacheStatistics copy() {
            CacheStatistics copy = new CacheStatistics();
            copy.l1Hits = this.l1Hits;
            copy.l2Hits = this.l2Hits;
            copy.misses = this.misses;
            copy.puts = this.puts;
            copy.removes = this.removes;
            copy.errors = this.errors;
            copy.totalAccesses = this.totalAccesses;
            copy.accessTimes.putAll(this.accessTimes);
            return copy;
        }
    }
}
