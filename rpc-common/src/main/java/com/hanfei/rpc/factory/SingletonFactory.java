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
        // 从对象映射中获取指定类的单例对象
        Object instance = objectMap.get(clazz);
        // 对类对象进行同步锁，保证线程安全
        synchronized (clazz) {
            if (instance == null) { // 如果单例对象为null，说明还未创建
                try {
                    // 使用反射创建类的实例
                    instance = clazz.newInstance();
                    // 将实例放入对象映射中
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
