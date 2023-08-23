package com.hanfei.rpc.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 创建线程池工具类
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ThreadPoolFactory {

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;

    private static final int MAXIMUM_POOL_SIZE_SIZE = 100;

    private static final int KEEP_ALIVE_TIME = 1;

    private static final int BLOCKING_QUEUE_CAPACITY = 100;

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
        // 创建有界队列，存放任务
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        // 创建线程工厂，根据线程名称前缀和是否守护线程进行设置
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
}
