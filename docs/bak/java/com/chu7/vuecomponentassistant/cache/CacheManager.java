package com.chu7.vuecomponentassistant.cache;

import com.chu7.vuecomponentassistant.completion.CompletionContext;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统一缓存管理器
 * 
 * 管理所有类型的缓存：
 * - 补全结果缓存
 * - 上下文分析缓存
 * - 组件数据缓存
 * - 文档缓存
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class CacheManager {
    
    private static final Logger LOG = VueKitLogger.getLogger(CacheManager.class);
    
    // 单例实例
    private static volatile CacheManager instance;
    
    // 不同类型的缓存
    private final LRUCache<String, CompletionResultSet> completionCache;
    private final LRUCache<String, CompletionContext> contextCache;
    private final LRUCache<String, Object> componentDataCache;
    private final LRUCache<String, String> documentationCache;
    
    // 定时清理任务
    private final ScheduledExecutorService cleanupExecutor;
    
    private CacheManager() {
        PluginSettings settings = PluginSettings.getInstance();
        
        // 初始化各种缓存
        this.completionCache = new LRUCache<>(
            settings.getMaxCompletionCacheSize(), 
            settings.getCompletionCacheExpireTime()
        );
        
        this.contextCache = new LRUCache<>(
            settings.getMaxContextCacheSize(),
            settings.getContextCacheExpireTime()
        );
        
        this.componentDataCache = new LRUCache<>(
            settings.getMaxComponentDataCacheSize(),
            settings.getComponentDataCacheExpireTime()
        );
        
        this.documentationCache = new LRUCache<>(
            settings.getMaxDocumentationCacheSize(),
            settings.getDocumentationCacheExpireTime()
        );
        
        // 启动定时清理任务
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VueKit-Cache-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        startCleanupTask();
        
        VueKitLogger.info(LOG, "缓存管理器初始化完成");
    }
    
    /**
     * 获取单例实例
     */
    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }
    
    // ==================== 补全缓存 ====================
    
    /**
     * 获取缓存的补全结果
     */
    public CompletionResultSet getCachedCompletion(PsiFile file, PsiElement element, String prefix, String libraryType) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return null;
        }
        
        String key = generateCompletionKey(file, element, prefix, libraryType);
        return completionCache.get(key);
    }
    
    /**
     * 缓存补全结果
     */
    public void cacheCompletion(PsiFile file, PsiElement element, String prefix, String libraryType, CompletionResultSet result) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return;
        }
        
        String key = generateCompletionKey(file, element, prefix, libraryType);
        completionCache.put(key, result);
    }
    
    private String generateCompletionKey(PsiFile file, PsiElement element, String prefix, String libraryType) {
        return String.format("completion:%s:%d:%s:%s", 
            file.getVirtualFile().getPath(),
            element.getTextOffset(),
            prefix != null ? prefix : "",
            libraryType
        );
    }
    
    // ==================== 上下文缓存 ====================
    
    /**
     * 获取缓存的上下文分析结果
     */
    public CompletionContext getCachedContext(PsiFile file, PsiElement element) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return null;
        }
        
        String key = generateContextKey(file, element);
        return contextCache.get(key);
    }
    
    /**
     * 缓存上下文分析结果
     */
    public void cacheContext(PsiFile file, PsiElement element, CompletionContext context) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return;
        }
        
        String key = generateContextKey(file, element);
        contextCache.put(key, context);
    }
    
    private String generateContextKey(PsiFile file, PsiElement element) {
        return String.format("context:%s:%d:%s", 
            file.getVirtualFile().getPath(),
            element.getTextOffset(),
            element.getText().hashCode()
        );
    }
    
    // ==================== 组件数据缓存 ====================
    
    /**
     * 获取缓存的组件数据
     */
    public Object getCachedComponentData(String libraryType, String componentName) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return null;
        }
        
        String key = generateComponentDataKey(libraryType, componentName);
        return componentDataCache.get(key);
    }
    
    /**
     * 缓存组件数据
     */
    public void cacheComponentData(String libraryType, String componentName, Object data) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return;
        }
        
        String key = generateComponentDataKey(libraryType, componentName);
        componentDataCache.put(key, data);
    }
    
    private String generateComponentDataKey(String libraryType, String componentName) {
        return String.format("component:%s:%s", libraryType, componentName);
    }
    
    // ==================== 文档缓存 ====================
    
    /**
     * 获取缓存的文档
     */
    public String getCachedDocumentation(String componentName, String libraryType) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return null;
        }
        
        String key = generateDocumentationKey(componentName, libraryType);
        return documentationCache.get(key);
    }
    
    /**
     * 缓存文档
     */
    public void cacheDocumentation(String componentName, String libraryType, String documentation) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return;
        }
        
        String key = generateDocumentationKey(componentName, libraryType);
        documentationCache.put(key, documentation);
    }
    
    private String generateDocumentationKey(String componentName, String libraryType) {
        return String.format("doc:%s:%s", libraryType, componentName);
    }
    
    // ==================== 缓存管理 ====================
    
    /**
     * 获取所有缓存的统计信息
     */
    public CacheStatsSummary getAllCacheStats() {
        return new CacheStatsSummary(
            completionCache.getStats(),
            contextCache.getStats(),
            componentDataCache.getStats(),
            documentationCache.getStats()
        );
    }
    
    /**
     * 清理所有过期缓存
     */
    public void cleanupAllExpiredCache() {
        long startTime = System.currentTimeMillis();
        
        int totalCleaned = 0;
        totalCleaned += completionCache.cleanupExpired();
        totalCleaned += contextCache.cleanupExpired();
        totalCleaned += componentDataCache.cleanupExpired();
        totalCleaned += documentationCache.cleanupExpired();
        
        long duration = System.currentTimeMillis() - startTime;
        
        if (totalCleaned > 0) {
            VueKitLogger.performance(LOG, "缓存清理", duration);
            VueKitLogger.debug(LOG, "清理过期缓存条目总数: " + totalCleaned);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        completionCache.clear();
        contextCache.clear();
        componentDataCache.clear();
        documentationCache.clear();
        
        VueKitLogger.info(LOG, "清空所有缓存");
    }
    
    /**
     * 重置所有缓存统计
     */
    public void resetAllStats() {
        completionCache.resetStats();
        contextCache.resetStats();
        componentDataCache.resetStats();
        documentationCache.resetStats();
        
        VueKitLogger.debug(LOG, "重置所有缓存统计");
    }
    
    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        PluginSettings settings = PluginSettings.getInstance();
        long cleanupInterval = settings.getCacheCleanupInterval();
        
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanupAllExpiredCache,
            cleanupInterval,
            cleanupInterval,
            TimeUnit.MILLISECONDS
        );
        
        VueKitLogger.debug(LOG, "启动缓存清理任务，间隔: " + cleanupInterval + "ms");
    }
    
    /**
     * 关闭缓存管理器
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        clearAllCache();
        VueKitLogger.info(LOG, "缓存管理器已关闭");
    }
    
    /**
     * 缓存统计汇总类
     */
    public static class CacheStatsSummary {
        private final LRUCache.CacheStats completionStats;
        private final LRUCache.CacheStats contextStats;
        private final LRUCache.CacheStats componentDataStats;
        private final LRUCache.CacheStats documentationStats;
        
        public CacheStatsSummary(LRUCache.CacheStats completionStats,
                                LRUCache.CacheStats contextStats,
                                LRUCache.CacheStats componentDataStats,
                                LRUCache.CacheStats documentationStats) {
            this.completionStats = completionStats;
            this.contextStats = contextStats;
            this.componentDataStats = componentDataStats;
            this.documentationStats = documentationStats;
        }
        
        public LRUCache.CacheStats getCompletionStats() { return completionStats; }
        public LRUCache.CacheStats getContextStats() { return contextStats; }
        public LRUCache.CacheStats getComponentDataStats() { return componentDataStats; }
        public LRUCache.CacheStats getDocumentationStats() { return documentationStats; }
        
        /**
         * 计算总体命中率
         */
        public double getOverallHitRate() {
            long totalHits = completionStats.getHitCount() + contextStats.getHitCount() + 
                           componentDataStats.getHitCount() + documentationStats.getHitCount();
            long totalRequests = totalHits + completionStats.getMissCount() + contextStats.getMissCount() + 
                               componentDataStats.getMissCount() + documentationStats.getMissCount();
            
            return totalRequests == 0 ? 0.0 : (double) totalHits / totalRequests;
        }
        
        /**
         * 获取总缓存大小
         */
        public int getTotalSize() {
            return completionStats.getCurrentSize() + contextStats.getCurrentSize() + 
                   componentDataStats.getCurrentSize() + documentationStats.getCurrentSize();
        }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStatsSummary{" +
                "totalSize=%d, overallHitRate=%.2f%%, " +
                "completion=%s, context=%s, componentData=%s, documentation=%s}",
                getTotalSize(), getOverallHitRate() * 100,
                completionStats, contextStats, componentDataStats, documentationStats
            );
        }
    }
}