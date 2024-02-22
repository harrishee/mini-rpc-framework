package com.hanfei.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SingletonFactory {
    // One kind of clazz can only
    private static final Map<Class, Object> OBJECT_MAP = new HashMap<>();

    /**
     * get the singleton object instance, making sure that only one instance of the object exists in the JVM
     */
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = OBJECT_MAP.get(clazz);

        // synchronize on the class to ensure thread safety during instance creation
        synchronized (clazz) {
            if (instance == null) {
                try { // create a new instance using reflection and store it
                    instance = clazz.newInstance();
                    OBJECT_MAP.put(clazz, instance);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return clazz.cast(instance);
    }
}
