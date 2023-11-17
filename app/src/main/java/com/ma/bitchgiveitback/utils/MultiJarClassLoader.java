package com.ma.bitchgiveitback.utils;

import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MultiJarClassLoader extends ClassLoader {
    static MultiJarClassLoader multiJarClassLoader;
    List<DexClassLoader> dexClassLoaders = new ArrayList<>();
    private MultiJarClassLoader(ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    public synchronized static MultiJarClassLoader getInstance(){
        if (multiJarClassLoader == null){
            multiJarClassLoader = new MultiJarClassLoader(ClassLoader.getSystemClassLoader());
        }
        return multiJarClassLoader;
    }

    public void addJar(String jarPath) {
        DexClassLoader dexClassLoader = new DexClassLoader(
                jarPath,
                null,
                null, // 额外的库路径，可以为 null
                getParent() // 父类加载器
        );
        dexClassLoaders.add(dexClassLoader);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        // 遍历所有的 DexClassLoader 实例，尝试加载类
        for (DexClassLoader dexClassLoader : dexClassLoaders) {
            try {
                return dexClassLoader.loadClass(className);
            } catch (ClassNotFoundException ignored) {
                // 忽略类未找到的异常，继续下一个 DexClassLoader
            }
        }
        throw new ClassNotFoundException("Class not found: " + className);
    }
}