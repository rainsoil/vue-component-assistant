package bak.loader;

import com.chu7.vuecomponentassistant.cache.CacheManager;
import com.chu7.vuecomponentassistant.completion.ElementPlusComponent;
import com.chu7.vuecomponentassistant.constants.VueKitConstants;
import com.chu7.vuecomponentassistant.settings.PluginSettings;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 智能按需加载管理器
 * 
 * 特性：
 * - 延迟加载组件数据
 * - 智能预加载策略
 * - 异步加载支持
 * - 内存使用优化
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class LazyComponentLoader {
    
    private static final Logger LOG = VueKitLogger.getLogger(LazyComponentLoader.class);
    
    // 单例实例
    private static volatile LazyComponentLoader instance;
    
    // 异步执行器
    private final Executor executor;
    
    // 缓存管理器
    private final CacheManager cacheManager;
    
    // 组件库加载状态
    private final Map<String, LoadingState> libraryLoadingStates;
    
    // 组件数据存储
    private final Map<String, Map<String, ElementPlusComponent>> libraryComponents;
    
    // 预加载队列
    private final Queue<String> preloadQueue;
    
    // 加载统计
    private final Map<String, LoadingStats> loadingStats;
    
    /**
     * 加载状态枚举
     */
    public enum LoadingState {
        NOT_LOADED,    // 未加载
        LOADING,       // 加载中
        LOADED,        // 已加载
        FAILED         // 加载失败
    }
    
    private LazyComponentLoader() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "VueKit-Lazy-Loader");
            t.setDaemon(true);
            return t;
        });
        
        this.cacheManager = CacheManager.getInstance();
        this.libraryLoadingStates = new ConcurrentHashMap<>();
        this.libraryComponents = new ConcurrentHashMap<>();
        this.preloadQueue = new LinkedList<>();
        this.loadingStats = new ConcurrentHashMap<>();
        
        VueKitLogger.info(LOG, "智能按需加载管理器初始化完成");
    }
    
    /**
     * 获取单例实例
     */
    public static LazyComponentLoader getInstance() {
        if (instance == null) {
            synchronized (LazyComponentLoader.class) {
                if (instance == null) {
                    instance = new LazyComponentLoader();
                }
            }
        }
        return instance;
    }
    
    /**
     * 异步加载组件库
     * 
     * @param libraryType 组件库类型
     * @return 加载任务的CompletableFuture
     */
    public CompletableFuture<Map<String, ElementPlusComponent>> loadLibraryAsync(String libraryType) {
        // 检查缓存
        Object cached = cacheManager.getCachedComponentData(libraryType, "all");
        if (cached instanceof Map) {
            VueKitLogger.logCacheOperation(LOG, "hit", "library:" + libraryType);
            return CompletableFuture.completedFuture((Map<String, ElementPlusComponent>) cached);
        }
        
        // 检查是否已经在加载中
        LoadingState currentState = libraryLoadingStates.get(libraryType);
        if (currentState == LoadingState.LOADED) {
            return CompletableFuture.completedFuture(libraryComponents.get(libraryType));
        }
        
        if (currentState == LoadingState.LOADING) {
            // 如果正在加载中，等待加载完成
            return CompletableFuture.supplyAsync(() -> {
                waitForLibraryLoad(libraryType);
                return libraryComponents.get(libraryType);
            }, executor);
        }
        
        // 开始异步加载
        libraryLoadingStates.put(libraryType, LoadingState.LOADING);
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            
            try {
                VueKitLogger.debug(LOG, "开始异步加载组件库: " + libraryType);
                
                Map<String, ElementPlusComponent> components = loadLibrarySync(libraryType);
                
                if (components != null && !components.isEmpty()) {
                    libraryComponents.put(libraryType, components);
                    libraryLoadingStates.put(libraryType, LoadingState.LOADED);
                    
                    // 缓存结果
                    cacheManager.cacheComponentData(libraryType, "all", components);
                    
                    long duration = System.currentTimeMillis() - startTime;
                    recordLoadingStats(libraryType, components.size(), duration, true);
                    
                    VueKitLogger.performance(LOG, "组件库异步加载: " + libraryType, duration);
                    VueKitLogger.logLibraryDetection(LOG, libraryType, components.size());
                    
                    // 触发预加载相关组件库
                    triggerPreload(libraryType);
                    
                    return components;
                } else {
                    libraryLoadingStates.put(libraryType, LoadingState.FAILED);
                    recordLoadingStats(libraryType, 0, System.currentTimeMillis() - startTime, false);
                    VueKitLogger.warn(LOG, "组件库加载失败: " + libraryType);
                    return Collections.emptyMap();
                }
                
            } catch (Exception e) {
                libraryLoadingStates.put(libraryType, LoadingState.FAILED);
                recordLoadingStats(libraryType, 0, System.currentTimeMillis() - startTime, false);
                VueKitLogger.error(LOG, "组件库加载异常: " + libraryType, e);
                return Collections.emptyMap();
            }
        }, executor);
    }
    
    /**
     * 同步加载组件库（内部使用）
     */
    private Map<String, ElementPlusComponent> loadLibrarySync(String libraryType) {
        String dataPath = getDataPathForLibrary(libraryType);
        if (dataPath == null) {
            VueKitLogger.warn(LOG, "未知的组件库类型: " + libraryType);
            return null;
        }
        
        try {
            InputStream inputStream = getClass().getResourceAsStream(dataPath);
            if (inputStream == null) {
                VueKitLogger.error(LOG, "找不到组件数据文件: " + dataPath);
                return null;
            }
            
            byte[] bytes = inputStream.readAllBytes();
            String jsonContent = new String(bytes, StandardCharsets.UTF_8);
            inputStream.close();
            
            VueKitLogger.debug(LOG, "读取组件数据文件: " + dataPath + ", 大小: " + bytes.length + " 字节");
            
            Gson gson = new Gson();
            Type listType = new TypeToken<List<ElementPlusComponent>>(){}.getType();
            List<ElementPlusComponent> componentList = gson.fromJson(jsonContent, listType);
            
            // 转换为Map以提高查找效率
            Map<String, ElementPlusComponent> componentsMap = new HashMap<>();
            for (ElementPlusComponent component : componentList) {
                componentsMap.put(component.getName(), component);
            }
            
            return componentsMap;
            
        } catch (IOException e) {
            VueKitLogger.error(LOG, "读取组件数据文件失败: " + dataPath, e);
            return null;
        } catch (Exception e) {
            VueKitLogger.error(LOG, "解析组件数据失败: " + libraryType, e);
            return null;
        }
    }
    
    /**
     * 按需加载单个组件
     * 
     * @param libraryType 组件库类型
     * @param componentName 组件名称
     * @return 组件数据的CompletableFuture
     */
    public CompletableFuture<ElementPlusComponent> loadComponentAsync(String libraryType, String componentName) {
        // 先检查缓存
        Object cached = cacheManager.getCachedComponentData(libraryType, componentName);
        if (cached instanceof ElementPlusComponent) {
            VueKitLogger.logCacheOperation(LOG, "hit", "component:" + libraryType + ":" + componentName);
            return CompletableFuture.completedFuture((ElementPlusComponent) cached);
        }
        
        // 检查是否整个库已加载
        Map<String, ElementPlusComponent> libraryMap = libraryComponents.get(libraryType);
        if (libraryMap != null) {
            ElementPlusComponent component = libraryMap.get(componentName);
            if (component != null) {
                cacheManager.cacheComponentData(libraryType, componentName, component);
                return CompletableFuture.completedFuture(component);
            }
        }
        
        // 如果库未加载，先加载整个库
        return loadLibraryAsync(libraryType).thenApply(components -> {
            ElementPlusComponent component = components.get(componentName);
            if (component != null) {
                cacheManager.cacheComponentData(libraryType, componentName, component);
            }
            return component;
        });
    }
    
    /**
     * 智能预加载
     * 根据使用模式预加载相关组件库
     */
    public void smartPreload(String primaryLibrary) {
        if (!PluginSettings.getInstance().isEnableCaching()) {
            return;
        }
        
        VueKitLogger.debug(LOG, "启动智能预加载，主要库: " + primaryLibrary);
        
        // 添加到预加载队列
        synchronized (preloadQueue) {
            if (!preloadQueue.contains(primaryLibrary)) {
                preloadQueue.offer(primaryLibrary);
            }
        }
        
        // 异步执行预加载
        CompletableFuture.runAsync(() -> {
            String libraryToPreload;
            while ((libraryToPreload = preloadQueue.poll()) != null) {
                if (libraryLoadingStates.get(libraryToPreload) == LoadingState.NOT_LOADED) {
                    try {
                        loadLibraryAsync(libraryToPreload).get();
                        VueKitLogger.debug(LOG, "预加载完成: " + libraryToPreload);
                    } catch (Exception e) {
                        VueKitLogger.logAndIgnore(LOG, "预加载失败: " + libraryToPreload, e);
                    }
                }
            }
        }, executor);
    }
    
    /**
     * 获取组件库加载状态
     */
    public LoadingState getLibraryLoadingState(String libraryType) {
        return libraryLoadingStates.getOrDefault(libraryType, LoadingState.NOT_LOADED);
    }
    
    /**
     * 获取已加载的组件库列表
     */
    public Set<String> getLoadedLibraries() {
        return libraryComponents.keySet();
    }
    
    /**
     * 获取加载统计信息
     */
    public Map<String, LoadingStats> getLoadingStats() {
        return new HashMap<>(loadingStats);
    }
    
    /**
     * 清理所有数据
     */
    public void clearAll() {
        libraryComponents.clear();
        libraryLoadingStates.clear();
        preloadQueue.clear();
        loadingStats.clear();
        VueKitLogger.info(LOG, "清理所有加载数据");
    }
    
    // ==================== 私有辅助方法 ====================
    
    private String getDataPathForLibrary(String libraryType) {
        switch (libraryType.toLowerCase()) {
            case "element-plus":
                return VueKitConstants.ELEMENT_PLUS_DATA_FILE;
            case "element-ui":
                return VueKitConstants.ELEMENT_UI_DATA_FILE;
            case "ant-design-vue":
                return VueKitConstants.ANT_DESIGN_VUE_DATA_FILE;
            default:
                return null;
        }
    }
    
    private void waitForLibraryLoad(String libraryType) {
        int maxWait = 30; // 最大等待30秒
        int waitCount = 0;
        
        while (libraryLoadingStates.get(libraryType) == LoadingState.LOADING && waitCount < maxWait) {
            try {
                Thread.sleep(1000);
                waitCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void triggerPreload(String loadedLibrary) {
        // 根据已加载的库智能预测可能需要的其他库
        switch (loadedLibrary.toLowerCase()) {
            case "element-plus":
                smartPreload("element-ui"); // Element Plus用户可能也会用Element UI
                break;
            case "element-ui":
                smartPreload("element-plus"); // 反之亦然
                break;
            case "ant-design-vue":
                // Ant Design用户可能不会混用其他库，暂不预加载
                break;
        }
    }
    
    private void recordLoadingStats(String libraryType, int componentCount, long duration, boolean success) {
        LoadingStats stats = loadingStats.computeIfAbsent(libraryType, k -> new LoadingStats());
        stats.recordLoading(componentCount, duration, success);
    }
    
    /**
     * 加载统计信息类
     */
    public static class LoadingStats {
        private int totalLoads = 0;
        private int successfulLoads = 0;
        private long totalDuration = 0;
        private int totalComponents = 0;
        private long lastLoadTime = 0;
        
        public synchronized void recordLoading(int componentCount, long duration, boolean success) {
            totalLoads++;
            totalDuration += duration;
            totalComponents += componentCount;
            lastLoadTime = System.currentTimeMillis();
            
            if (success) {
                successfulLoads++;
            }
        }
        
        public int getTotalLoads() { return totalLoads; }
        public int getSuccessfulLoads() { return successfulLoads; }
        public double getSuccessRate() { 
            return totalLoads == 0 ? 0.0 : (double) successfulLoads / totalLoads; 
        }
        public long getAverageDuration() { 
            return totalLoads == 0 ? 0 : totalDuration / totalLoads; 
        }
        public int getTotalComponents() { return totalComponents; }
        public long getLastLoadTime() { return lastLoadTime; }
        
        @Override
        public String toString() {
            return String.format(
                "LoadingStats{loads=%d, success=%.1f%%, avgDuration=%dms, components=%d}",
                totalLoads, getSuccessRate() * 100, getAverageDuration(), totalComponents
            );
        }
    }
}