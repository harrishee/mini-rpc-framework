package com.hanfei.rpc.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadPoolFactory {
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private static final Map<String, ExecutorService> THREAD_POOL_MAP = new ConcurrentHashMap<>();
    
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }
    
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        ExecutorService pool = THREAD_POOL_MAP.computeIfAbsent(
                threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon)
        );

        // 如果线程池已经关闭，创建一个新的线程池替代
        if (pool.isShutdown() || pool.isTerminated()) {
            THREAD_POOL_MAP.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            THREAD_POOL_MAP.put(threadNamePrefix, pool);
        }

        return pool;
    }
    
    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 创建任务队列，用于存放待执行的任务
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 创建线程工厂，用于创建线程
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        // 创建并返回线程池
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MINUTES, workQueue, threadFactory
        );
    }
    
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        // 如果指定了线程名前缀，则使用自定义的线程工厂
        if (threadNamePrefix != null) {
            
            // 用 guava 的 ThreadFactoryBuilder 创建线程工厂
            return new ThreadFactoryBuilder()
                    .setNameFormat(threadNamePrefix + "-%d")
                    .setDaemon(daemon != null && daemon)
                    .build();
        }

        // 否则使用默认的线程工厂
        return Executors.defaultThreadFactory();
    }
    
    public static void shutdownAllThreadPool() {
        log.info("正在关闭所有线程池...");
        
        THREAD_POOL_MAP.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            try {
                // 等待线程池中的任务完成
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("关闭线程池时出错");
                executorService.shutdownNow();
            }
        });
        log.info("所有线程池已成功关闭");
    }
}
