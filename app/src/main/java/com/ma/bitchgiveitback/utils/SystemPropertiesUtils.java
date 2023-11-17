package com.ma.bitchgiveitback.utils;

import java.lang.reflect.Method;

public class SystemPropertiesUtils {
    private static Class<?> clazz;
    private static Method getMethod;
    private static Method getBooleanMethod;
    private SystemPropertiesUtils(){

    }

    private static Class<?> getClazz() throws ClassNotFoundException {
        if (clazz == null) clazz = Class.forName("android.os.SystemProperties");
        return clazz;
    }

    private static Method getGetMethod() throws ClassNotFoundException, NoSuchMethodException {
        if (getMethod == null) getMethod = getClazz().getMethod("get", String.class);
        return getMethod;
    }

    private static Method getGetBooleanMethod() throws ClassNotFoundException, NoSuchMethodException {
        if (getBooleanMethod == null) getBooleanMethod = getClazz().getMethod("getBoolean", String.class, boolean.class);
        return getBooleanMethod;
    }

    public static String get(String key){
        try {
            return (String) getGetMethod().invoke(null, key);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public static boolean getBoolean(String key, boolean def){
        try {
            return (boolean) getGetBooleanMethod().invoke(null, key, def);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
