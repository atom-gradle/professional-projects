package com.qian.feather.manager;

import com.qian.feather.Utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Qian atom-gradle
 */

public class ThreadManager {
    private static volatile ThreadManager instance;
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int WORK_QUEUE_CAPACITY = 1000;
    private final ThreadPoolExecutor executor;
    private static ThreadPoolExecutor ioExecutor;
    private static ThreadPoolExecutor cpuExecutor;
    static {
        ioExecutor = new ThreadPoolExecutor(
                Utils.cpuCores,
                (Utils.cpuCores << 2),
                30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new MyThreadFactory("IO-ThreadFac-")
        );
        cpuExecutor = new ThreadPoolExecutor(
                Utils.cpuCores,
                (Utils.cpuCores << 1),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new MyThreadFactory("CPU-ThreadFac-")
        );
    }
    static class MyThreadFactory implements ThreadFactory {
        private String tag;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        public MyThreadFactory(String tag) {
            this.tag = tag;
        }
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, tag + threadNumber.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setDaemon(false);
            return thread;
        }
    }
    // 私有构造方法
    private ThreadManager() {

        // 创建线程池
        executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(WORK_QUEUE_CAPACITY),
                new MyThreadFactory("deafult-"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
    }

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    /*
    public static void executeIoTask(Runnable task) {
        ioExecutor.execute(task);
    }

    public static void executeCpuTask(Runnable task) {
        cpuExecutor.execute(task);
    }
     */
    public void execute(Runnable task) {
        if (task != null) {
            executor.execute(task);
        }
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (task != null) {
            return executor.submit(task);
        }
        return null;
    }

    public Future<?> submit(Runnable task) {
        if (task != null) {
            return executor.submit(task);
        }
        return null;
    }

    public int getActiveCount() {
        return executor.getActiveCount();
    }

    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }


    public int getQueueSize() {
        return executor.getQueue().size();
    }


    public void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown(); // 不再接受新任务
            try {
                // 等待现有任务完成
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    // 强制关闭
                    executor.shutdownNow();
                    // 再次等待
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("线程池未能正常关闭");
                    }
                }
            } catch (InterruptedException e) {
                // 重新尝试关闭
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 立即关闭线程池
     */
    public void shutdownNow() {
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    /**
     * 调整核心线程数
     */
    public void setCorePoolSize(int corePoolSize) {
        executor.setCorePoolSize(corePoolSize);
    }

    /**
     * 调整最大线程数
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        executor.setMaximumPoolSize(maximumPoolSize);
    }
}