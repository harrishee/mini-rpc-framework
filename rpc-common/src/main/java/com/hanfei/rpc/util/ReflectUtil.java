package com.hanfei.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 反射工具类，用于动态加载类和资源
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtil {
    
    /**
     * 获取当前调用类的类名
     */
    public static String getCurrentClassName() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        return stack[stack.length - 1].getClassName();
    }
    
    /**
     * 获取给定包名下的所有类
     */
    public static Set<Class<?>> getAllClass(String packageName) {
        // 用LinkedHashSet来保持插入顺序
        Set<Class<?>> classes = new LinkedHashSet<>();
        boolean recursive = true;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {
            // 获取当前线程的ClassLoader中的资源
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    // 以文件的方式扫描整个包下的文件，并添加到集合中
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddAllClass(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件，则解析jar包中的entry
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        // 添加到集合中去
                                        classes.add(Class.forName(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }
    
    /**
     * 以文件的形式来获取包下的所有Class
     */
    private static void findAndAddAllClass(String packageName, String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录，建立一个File
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        // 如果存在，则获取包下的所有文件，包括目录
        File[] dirFiles = dir.listFiles(file -> (recursive && file.isDirectory()) || (file.getName().endsWith(".class")));
        if (dirFiles != null) {
            for (File file : dirFiles) {
                if (file.isDirectory()) {
                    // 如果是目录，则递归扫描
                    findAndAddAllClass(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
                } else {
                    // 如果是java类文件，去掉后面的.class，只留下类名
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        // 添加到集合中去
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
