package com.ma.bitchgiveitback.utils;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceManager {

    private static ActivityManager activityManager;
    private static PackageManager packageManager;
    private static UserManager userManager;
    private static NotificationManager notificationManager;

    private ServiceManager() {
        System.out.println(this.getClass().getSimpleName()+" 执行私有构造方法");
    }
    private static IInterface getService(String service, String type){
        try {
            IBinder binder = getIBinderService(service);
            Class<?> clz = Class.forName(type + "$Stub");
            Method asInterfaceMethod = clz.getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static IBinder getIBinderService(String service){
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            return  (IBinder) getServiceMethod.invoke(null, service);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static IBinder checkService(String name){
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("checkService", String.class);
            return  (IBinder) getServiceMethod.invoke(null, name);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] listServices() {
        try {
            return (String[]) Class.forName("android.os.ServiceManager").getMethod("listServices").invoke(null);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ActivityManager getActivityManager(){
        if (activityManager == null) {
            activityManager = new ActivityManager(getService("activity", "android.app.IActivityManager"));
        }
        return activityManager;
    }

    public static PackageManager getPackageManager(){
        if (packageManager == null){
            packageManager = new PackageManager(getService("package", "android.content.pm.IPackageManager"));
        }
        return packageManager;
    }

    public static UserManager getUserManager(){
        if (userManager == null){
            userManager = new UserManager(getService("user","android.os.IUserManager"));
        }
        return userManager;
    }

    public static NotificationManager getNotificationManager(){
        if (notificationManager == null){
            notificationManager = new NotificationManager(getService("notification","android.app.INotificationManager"));
        }

        return notificationManager;
    }
}
