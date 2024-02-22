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

    /**
     * use threadNamePrefix to distinguish different thread pools
     * we can think of thread pools with the same threadNamePrefix as serving the same business scenario
     */
    private static final Map<String, ExecutorService> THREAD_POOLS_MAP = new ConcurrentHashMap<>();

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        // create a thread pool if it does not exist
        ExecutorService pool = THREAD_POOLS_MAP.computeIfAbsent(
                threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon)
        );

        // if the thread pool is shutdown, replace it with a new one
        if (pool.isShutdown() || pool.isTerminated()) {
            THREAD_POOLS_MAP.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            THREAD_POOLS_MAP.put(threadNamePrefix, pool);
        }
        return pool;
    }

    /**
     * create a thread factory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon)
                        .build();
            } else {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * create a thread pool
     */
    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // create a blocking queue for holding tasks
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // create a thread factory
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        // create ThreadPoolExecutor with the provided configurations
        return new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MINUTES, workQueue, threadFactory
        );
    }

    public static void shutDownAll() {
        log.info("Shutting down all thread pools...");

        // iterate through all thread pools and shut them down
        THREAD_POOLS_MAP.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error shutting down thread pool");
                executorService.shutdownNow();
            }
        });
        log.info("All thread pools shut down successfully");
    }
}
