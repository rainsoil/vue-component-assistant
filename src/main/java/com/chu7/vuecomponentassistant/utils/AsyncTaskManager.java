package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 异步任务管理器
 * 
 * 提供高性能的异步任务执行能力，支持：
 * - 线程池管理
 * - 任务优先级控制
 * - 超时处理
 * - 异常处理
 * - 性能监控
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class AsyncTaskManager {
    
    private static final Logger LOG = Logger.getInstance(AsyncTaskManager.class);
    
    // 线程池配置
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int QUEUE_CAPACITY = 1000;
    
    // 超时配置
    private static final long DEFAULT_TIMEOUT = 30000; // 30秒
    private static final long SHORT_TIMEOUT = 5000;    // 5秒
    private static final long LONG_TIMEOUT = 120000;   // 2分钟
    
    private final ThreadPoolExecutor executor;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger taskCounter;
    
    /**
     * 构造函数
     * 
     * 初始化线程池和调度器
     */
    public AsyncTaskManager() {
        this.executor = createThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.taskCounter = new AtomicInteger(0);
        
        LOG.info("异步任务管理器初始化完成");
    }
    
    /**
     * 创建线程池
     * 
     * @return 配置好的ThreadPoolExecutor
     */
    private ThreadPoolExecutor createThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "VueKit-Async-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setPriority(Thread.NORM_PRIORITY);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // 预热线程池
        executor.prestartAllCoreThreads();
        return executor;
    }
    
    /**
     * 异步执行任务（无返回值）
     * 
     * @param task 要执行的任务
     * @param taskName 任务名称（用于日志）
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> executeAsync(Runnable task, String taskName) {
        return executeAsync(task, taskName, DEFAULT_TIMEOUT);
    }
    
    /**
     * 异步执行任务（无返回值，带超时）
     * 
     * @param task 要执行的任务
     * @param taskName 任务名称
     * @param timeout 超时时间（毫秒）
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> executeAsync(Runnable task, String taskName, long timeout) {
        int taskId = taskCounter.incrementAndGet();
        String fullTaskName = String.format("%s-%d", taskName, taskId);
        
        LOG.debug("提交异步任务: " + fullTaskName);
        
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                task.run();
                long duration = System.currentTimeMillis() - startTime;
                VueKitLogger.performance(LOG, fullTaskName, duration);
                LOG.debug("异步任务完成: " + fullTaskName + ", 耗时: " + duration + "ms");
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                LOG.error("异步任务执行失败: " + fullTaskName + ", 耗时: " + duration + "ms", e);
                throw new CompletionException(e);
            }
        }, executor);
        
        // 添加超时处理
        if (timeout > 0) {
            future = future.orTimeout(timeout, TimeUnit.MILLISECONDS);
        }
        
        return future;
    }
    
    /**
     * 异步执行任务（有返回值）
     * 
     * @param supplier 任务提供者
     * @param taskName 任务名称
     * @param <T> 返回值类型
     * @return CompletableFuture<T>
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String taskName) {
        return executeAsync(supplier, taskName, DEFAULT_TIMEOUT);
    }
    
    /**
     * 异步执行任务（有返回值，带超时）
     * 
     * @param supplier 任务提供者
     * @param taskName 任务名称
     * @param timeout 超时时间（毫秒）
     * @param <T> 返回值类型
     * @return CompletableFuture<T>
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String taskName, long timeout) {
        int taskId = taskCounter.incrementAndGet();
        String fullTaskName = String.format("%s-%d", taskName, taskId);
        
        LOG.debug("提交异步任务: " + fullTaskName);
        
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                T result = supplier.get();
                long duration = System.currentTimeMillis() - startTime;
                VueKitLogger.performance(LOG, fullTaskName, duration);
                LOG.debug("异步任务完成: " + fullTaskName + ", 耗时: " + duration + "ms");
                return result;
            } catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                LOG.error("异步任务执行失败: " + fullTaskName + ", 耗时: " + duration + "ms", e);
                throw new CompletionException(e);
            }
        }, executor);
        
        // 添加超时处理
        if (timeout > 0) {
            future = future.orTimeout(timeout, TimeUnit.MILLISECONDS);
        }
        
        return future;
    }
    
    /**
     * 延迟执行任务
     * 
     * @param task 要执行的任务
     * @param delay 延迟时间
     * @param unit 时间单位
     * @return ScheduledFuture<?>
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOG.error("延迟任务执行失败", e);
            }
        }, delay, unit);
    }
    
    /**
     * 定期执行任务
     * 
     * @param task 要执行的任务
     * @param initialDelay 初始延迟
     * @param period 执行周期
     * @param unit 时间单位
     * @return ScheduledFuture<?>
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOG.error("定期任务执行失败", e);
            }
        }, initialDelay, period, unit);
    }
    
    /**
     * 获取线程池状态
     * 
     * @return 线程池状态信息
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        return new ThreadPoolStatus(
            executor.getCorePoolSize(),
            executor.getMaximumPoolSize(),
            executor.getPoolSize(),
            executor.getActiveCount(),
            executor.getQueue().size(),
            executor.getCompletedTaskCount(),
            executor.getTaskCount()
        );
    }
    
    /**
     * 关闭任务管理器
     * 
     * 优雅关闭所有线程池
     */
    public void shutdown() {
        LOG.info("正在关闭异步任务管理器...");
        
        try {
            // 关闭调度器
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            // 关闭执行器
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            
            LOG.info("异步任务管理器已关闭");
        } catch (InterruptedException e) {
            LOG.warn("关闭异步任务管理器时被中断", e);
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 线程池状态信息
     */
    public static class ThreadPoolStatus {
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final int currentPoolSize;
        private final int activeThreads;
        private final int queuedTasks;
        private final long completedTasks;
        private final long totalTasks;
        
        public ThreadPoolStatus(int corePoolSize, int maximumPoolSize, int currentPoolSize,
                              int activeThreads, int queuedTasks, long completedTasks, long totalTasks) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.currentPoolSize = currentPoolSize;
            this.activeThreads = activeThreads;
            this.queuedTasks = queuedTasks;
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
        }
        
        // Getters
        public int getCorePoolSize() { return corePoolSize; }
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public int getCurrentPoolSize() { return currentPoolSize; }
        public int getActiveThreads() { return activeThreads; }
        public int getQueuedTasks() { return queuedTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getTotalTasks() { return totalTasks; }
        
        @Override
        public String toString() {
            return String.format("ThreadPoolStatus{core=%d, max=%d, current=%d, active=%d, queued=%d, completed=%d, total=%d}",
                corePoolSize, maximumPoolSize, currentPoolSize, activeThreads, queuedTasks, completedTasks, totalTasks);
        }
    }
}
