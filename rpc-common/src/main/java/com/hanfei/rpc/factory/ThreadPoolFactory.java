package com.hanfei.rpc.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建线程池工具类
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ThreadPoolFactory {

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;

    private static final int MAXIMUM_POOL_SIZE_SIZE = 100;

    private static final int KEEP_ALIVE_TIME = 1;

    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    // 用于存储各个线程池的映射关系
    private static Map<String, ExecutorService> threadPoolsMap = new ConcurrentHashMap<>();

    // 私有构造函数，不允许实例化该工具类
    private ThreadPoolFactory() {
    }

    /**
     * 默认创建线程池，不创建守护线程
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    /**
     * 创建线程池，并可以选择是否创建守护线程
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        ExecutorService pool = threadPoolsMap.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon));
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPoolsMap.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPoolsMap.put(threadNamePrefix, pool);
        }
        return pool;

    }

    /**
     * 创建线程池
     */
    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 创建有界阻塞队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 创建线程工厂
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        // 创建线程池
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 创建线程工厂，根据线程名称前缀和是否守护线程进行设置
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        // 如果线程名称前缀不为空，则创建自定义线程工厂
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        // 若未提供线程名称前缀，则使用默认线程工厂
        return Executors.defaultThreadFactory();
    }

    /**
     * 关闭所有线程池
     */
    public static void shutDownAll() {
        logger.info("关闭所有线程池...");

        // 遍历线程池映射的每个条目，使用并行流进行操作
        threadPoolsMap.entrySet().parallelStream().forEach(entry -> {
            // 获取当前线程池并关闭
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            logger.info("关闭线程池 [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                // 等待线程池终止，最多等待10秒钟
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                logger.error("关闭线程池失败！");
                // 强制中断未完成的任务并关闭线程池
                executorService.shutdownNow();
            }
        });
    }
}
