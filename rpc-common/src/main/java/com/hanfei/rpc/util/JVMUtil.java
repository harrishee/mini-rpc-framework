package com.hanfei.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JVMUtil {
    public static void addShutdownHook() {
        log.info("JVM关闭钩子，已添加");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 清理Nacos注册中心中的所有服务
            NacosUtil.clearRegistry();
            // 关闭应用中所有的线程池
            ThreadPoolFactory.shutdownAllThreadPool();
            log.info("JVM关闭钩子，所有服务已清理");
        }));
    }
}
