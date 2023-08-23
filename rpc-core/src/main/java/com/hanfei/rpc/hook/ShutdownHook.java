package com.hanfei.rpc.hook;

import com.hanfei.rpc.util.NacosUtil;
import com.hanfei.rpc.factory.ThreadPoolFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 关闭钩子，用于在 JVM 关闭时执行清理操作
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    // 获取单例 ShutdownHook 对象
    @Getter
    private static final ShutdownHook shutdownHook = new ShutdownHook();

    /**
     * 增加清理钩子，用于在 JVM 关闭时执行清理操作
     */
    public void addClearAllHook() {
        logger.info("关闭后将自动注销所有服务");

        // 创建并启动一个新线程，用于注册 JVM 关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 调用 NacosUtil 类的静态方法清理服务注册
            NacosUtil.clearRegistry();
            // 调用 ThreadPoolFactory 类的静态方法关闭所有线程池
            ThreadPoolFactory.shutDownAll();
        }));
    }
}
