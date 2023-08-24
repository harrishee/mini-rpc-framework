package com.hanfei.rpc.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 单例工厂类，用于获取单例对象实例
 *
 * @author: harris
 * @time: 2023
 * @summary: harris-rpc-framework
 */
public class SingletonFactory {

    // 存储不同类对应的单例对象
    private static Map<Class, Object> objectMap = new HashMap<>();

    // 私有构造函数，防止实例化
    private SingletonFactory() {
    }

    /**
     * 获取指定类的单例对象实例
     *
     * @param clazz 要获取单例对象的类
     * @param <T>   泛型参数，表示类的类型
     * @return 指定类的单例对象实例
     */
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz);

        // 防止了多个线程同时创建实例，保持了单例模式的一致性和正确性
        synchronized (clazz) {
            if (instance == null) { // 单例对象还未未创建
                try {
                    // 使用反射创建类的实例并存入 objectMap
                    instance = clazz.newInstance();
                    objectMap.put(clazz, instance);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        // 将实例转型为指定类类型并返回
        return clazz.cast(instance);
    }
}
