package com.hanfei.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 确保 PendingRequests 和 RpcRequestHandler 的全局唯一性
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SingletonFactory {
    private static final Map<Class<?>, Object> SINGLETON_CACHE = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz) {
        return (T) SINGLETON_CACHE.computeIfAbsent(clazz, key -> {
            try {
                return key.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                String errMsg = "创建单例失败: " + e.getMessage();
                log.error(errMsg);
                throw new RuntimeException(errMsg);
            }
        });
    }
}
