package com.chu7.vuecomponentassistant.completion;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 补全缓存管理器
 * 提供高性能的补全结果缓存机制
 * 
 * @author Vue Component Assistant Team
 * @version 2.0.0
 */
public class CompletionCache {
    
    private static final Logger LOG = Logger.getInstance(CompletionCache.class);
    
    // 缓存大小限制
    private static final int MAX_CACHE_SIZE = 1000;
    
    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟
    
    // 补全结果缓存
    private static final ConcurrentHashMap<String, CachedCompletionResult> completionCache = new ConcurrentHashMap<>();
    
    // 上下文分析缓存
    private static final ConcurrentHashMap<String, CachedContextAnalysis> contextCache = new ConcurrentHashMap<>();
    
    // 定时清理任务
    private static final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    static {
        // 启动定时清理任务
        cleanupExecutor.scheduleAtFixedRate(CompletionCache::cleanupExpiredCache, 
                                          CACHE_EXPIRE_TIME, CACHE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 缓存键生成器
     */
    public static class CacheKey {
        private final String filePath;
        private final int offset;
        private final String prefix;
        private final String libraryType;
        
        public CacheKey(PsiFile file, PsiElement element, String prefix, String libraryType) {
            this.filePath = file.getVirtualFile().getPath();
            this.offset = element.getTextOffset();
            this.prefix = prefix;
            this.libraryType = libraryType;
        }
        
        @Override
        public String toString() {
            return String.format("%s:%d:%s:%s", filePath, offset, prefix, libraryType);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CacheKey cacheKey = (CacheKey) obj;
            return offset == cacheKey.offset &&
                   filePath.equals(cacheKey.filePath) &&
                   prefix.equals(cacheKey.prefix) &&
                   libraryType.equals(cacheKey.libraryType);
        }
        
        @Override
        public int hashCode() {
            return filePath.hashCode() * 31 + offset * 31 + prefix.hashCode() * 31 + libraryType.hashCode();
        }
    }
    
    /**
     * 缓存的补全结果
     */
    public static class CachedCompletionResult {
        private final Object result;
        private final long timestamp;
        
        public CachedCompletionResult(Object result) {
            this.result = result;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Object getResult() {
            return result;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }
    
    /**
     * 缓存的上下文分析结果
     */
    public static class CachedContextAnalysis {
        private final CompletionContext context;
        private final long timestamp;
        
        public CachedContextAnalysis(CompletionContext context) {
            this.context = context;
            this.timestamp = System.currentTimeMillis();
        }
        
        public CompletionContext getContext() {
            return context;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }
    
    /**
     * 获取缓存的补全结果
     */
    public static CachedCompletionResult getCompletionResult(CacheKey key) {
        CachedCompletionResult cached = completionCache.get(key.toString());
        if (cached != null && !cached.isExpired()) {
            LOG.debug("命中补全缓存: " + key);
            return cached;
        }
        return null;
    }
    
    /**
     * 缓存补全结果
     */
    public static void cacheCompletionResult(CacheKey key, Object result) {
        if (completionCache.size() >= MAX_CACHE_SIZE) {
            cleanupExpiredCache();
        }
        
        completionCache.put(key.toString(), new CachedCompletionResult(result));
        LOG.debug("缓存补全结果: " + key);
    }
    
    /**
     * 获取缓存的上下文分析结果
     */
    public static CachedContextAnalysis getContextAnalysis(CacheKey key) {
        CachedContextAnalysis cached = contextCache.get(key.toString());
        if (cached != null && !cached.isExpired()) {
            LOG.debug("命中上下文缓存: " + key);
            return cached;
        }
        return null;
    }
    
    /**
     * 缓存上下文分析结果
     */
    public static void cacheContextAnalysis(CacheKey key, CompletionContext context) {
        if (contextCache.size() >= MAX_CACHE_SIZE) {
            cleanupExpiredCache();
        }
        
        contextCache.put(key.toString(), new CachedContextAnalysis(context));
        LOG.debug("缓存上下文分析: " + key);
    }
    
    /**
     * 清理过期缓存
     */
    private static void cleanupExpiredCache() {
        int beforeSize = completionCache.size() + contextCache.size();
        
        completionCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        contextCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        int afterSize = completionCache.size() + contextCache.size();
        if (beforeSize != afterSize) {
            LOG.info("清理过期缓存: " + (beforeSize - afterSize) + " 个条目");
        }
    }
    
    /**
     * 清空所有缓存
     */
    public static void clearAllCache() {
        completionCache.clear();
        contextCache.clear();
        LOG.info("清空所有缓存");
    }
    
    /**
     * 获取缓存统计信息
     */
    public static String getCacheStats() {
        return String.format("补全缓存: %d, 上下文缓存: %d", 
                           completionCache.size(), contextCache.size());
    }
    
    /**
     * 关闭缓存管理器
     */
    public static void shutdown() {
        cleanupExecutor.shutdown();
        clearAllCache();
    }
} 