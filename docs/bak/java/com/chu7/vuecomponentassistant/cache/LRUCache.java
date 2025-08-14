package com.chu7.vuecomponentassistant.cache;

import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.intellij.openapi.diagnostic.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 高性能 LRU 缓存实现
 * 
 * 特性：
 * - 线程安全的 LRU 算法
 * - 支持缓存命中率统计
 * - 自动过期清理
 * - 内存使用优化
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class LRUCache<K, V> {
    
    private static final Logger LOG = VueKitLogger.getLogger(LRUCache.class);
    
    private final int maxSize;
    private final long expireTimeMs;
    
    // 双向链表节点
    private static class Node<K, V> {
        K key;
        V value;
        long timestamp;
        long accessTime;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.timestamp = System.currentTimeMillis();
            this.accessTime = this.timestamp;
        }
        
        boolean isExpired(long expireTimeMs) {
            return System.currentTimeMillis() - timestamp > expireTimeMs;
        }
    }
    
    // 哈希表用于O(1)查找
    private final ConcurrentHashMap<K, Node<K, V>> cache;
    
    // 双向链表头尾节点
    private final Node<K, V> head;
    private final Node<K, V> tail;
    
    // 读写锁保证线程安全
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // 统计信息
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    /**
     * 构造函数
     * 
     * @param maxSize 最大缓存大小
     * @param expireTimeMs 过期时间（毫秒）
     */
    public LRUCache(int maxSize, long expireTimeMs) {
        this.maxSize = maxSize;
        this.expireTimeMs = expireTimeMs;
        this.cache = new ConcurrentHashMap<>(maxSize);
        
        // 初始化双向链表
        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        this.head.next = this.tail;
        this.tail.prev = this.head;
        
        VueKitLogger.debug(LOG, "创建LRU缓存，最大大小: " + maxSize + ", 过期时间: " + expireTimeMs + "ms");
    }
    
    /**
     * 获取缓存值
     * 
     * @param key 缓存键
     * @return 缓存值，如果不存在或已过期返回null
     */
    public V get(K key) {
        Node<K, V> node = cache.get(key);
        if (node == null) {
            missCount.incrementAndGet();
            VueKitLogger.logCacheOperation(LOG, "miss", String.valueOf(key));
            return null;
        }
        
        // 检查是否过期
        if (node.isExpired(expireTimeMs)) {
            remove(key);
            missCount.incrementAndGet();
            VueKitLogger.logCacheOperation(LOG, "expired", String.valueOf(key));
            return null;
        }
        
        // 更新访问时间并移动到链表头部
        lock.writeLock().lock();
        try {
            node.accessTime = System.currentTimeMillis();
            moveToHead(node);
            hitCount.incrementAndGet();
            VueKitLogger.logCacheOperation(LOG, "hit", String.valueOf(key));
            return node.value;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 添加或更新缓存
     * 
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(K key, V value) {
        Node<K, V> existingNode = cache.get(key);
        
        lock.writeLock().lock();
        try {
            if (existingNode != null) {
                // 更新现有节点
                existingNode.value = value;
                existingNode.timestamp = System.currentTimeMillis();
                existingNode.accessTime = existingNode.timestamp;
                moveToHead(existingNode);
                VueKitLogger.logCacheOperation(LOG, "update", String.valueOf(key));
            } else {
                // 添加新节点
                Node<K, V> newNode = new Node<>(key, value);
                cache.put(key, newNode);
                addToHead(newNode);
                
                // 检查是否超过容量限制
                if (cache.size() > maxSize) {
                    Node<K, V> tail = removeTail();
                    if (tail != null) {
                        cache.remove(tail.key);
                        evictionCount.incrementAndGet();
                        VueKitLogger.logCacheOperation(LOG, "evict", String.valueOf(tail.key));
                    }
                }
                
                VueKitLogger.logCacheOperation(LOG, "put", String.valueOf(key));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 移除缓存项
     * 
     * @param key 缓存键
     * @return 被移除的值
     */
    public V remove(K key) {
        Node<K, V> node = cache.remove(key);
        if (node == null) {
            return null;
        }
        
        lock.writeLock().lock();
        try {
            removeNode(node);
            VueKitLogger.logCacheOperation(LOG, "remove", String.valueOf(key));
            return node.value;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 清理过期条目
     * 
     * @return 清理的条目数量
     */
    public int cleanupExpired() {
        int cleanupCount = 0;
        
        lock.writeLock().lock();
        try {
            Node<K, V> current = tail.prev;
            while (current != head) {
                Node<K, V> prev = current.prev;
                if (current.isExpired(expireTimeMs)) {
                    cache.remove(current.key);
                    removeNode(current);
                    cleanupCount++;
                }
                current = prev;
            }
            
            if (cleanupCount > 0) {
                VueKitLogger.debug(LOG, "清理过期缓存条目: " + cleanupCount);
            }
            
            return cleanupCount;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            head.next = tail;
            tail.prev = head;
            VueKitLogger.debug(LOG, "清空LRU缓存");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * 获取当前缓存大小
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * 检查缓存是否为空
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }
    
    /**
     * 获取缓存命中率
     */
    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total;
    }
    
    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return new CacheStats(
            cache.size(),
            maxSize,
            hitCount.get(),
            missCount.get(),
            evictionCount.get(),
            getHitRate()
        );
    }
    
    /**
     * 重置统计信息
     */
    public void resetStats() {
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
    }
    
    // ==================== 私有辅助方法 ====================
    
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }
    
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }
    
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
    
    private Node<K, V> removeTail() {
        Node<K, V> lastNode = tail.prev;
        if (lastNode == head) {
            return null;
        }
        removeNode(lastNode);
        return lastNode;
    }
    
    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final int currentSize;
        private final int maxSize;
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;
        private final double hitRate;
        
        public CacheStats(int currentSize, int maxSize, long hitCount, 
                         long missCount, long evictionCount, double hitRate) {
            this.currentSize = currentSize;
            this.maxSize = maxSize;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.evictionCount = evictionCount;
            this.hitRate = hitRate;
        }
        
        public int getCurrentSize() { return currentSize; }
        public int getMaxSize() { return maxSize; }
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public long getEvictionCount() { return evictionCount; }
        public double getHitRate() { return hitRate; }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{size=%d/%d, hits=%d, misses=%d, evictions=%d, hitRate=%.2f%%}",
                currentSize, maxSize, hitCount, missCount, evictionCount, hitRate * 100
            );
        }
    }
}