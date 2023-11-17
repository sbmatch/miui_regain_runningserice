package com.ma.bitchgiveitback.app_process;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.util.Log;

import com.ma.bitchgiveitback.utils.MultiJarClassLoader;
import com.ma.bitchgiveitback.utils.Netd;
import com.ma.bitchgiveitback.utils.NotificationManager;
import com.ma.bitchgiveitback.utils.PackageManager;
import com.ma.bitchgiveitback.utils.ServiceManager;
import com.ma.bitchgiveitback.utils.ShellUtils;
import com.ma.bitchgiveitback.utils.SystemPropertiesUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {
    static PackageManager packageManager = ServiceManager.getPackageManager();
    static NotificationManager notificationManager = ServiceManager.getNotificationManager();
    static MultiJarClassLoader multiJarClassLoader = MultiJarClassLoader.getInstance();

    public static void main(String[] args) {
        if (Looper.getMainLooper() == null) {
            Looper.prepareMainLooper();
        }

        multiJarClassLoader.addJar("/system/framework/services.jar");
        multiJarClassLoader.addJar("/system/system_ext/framework/miui-services.jar");

        if (Binder.getCallingUid() == 0 || Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 2000) {

            if (Binder.getCallingUid() == 2000){
                if (packageManager.checkPermission("android.permission.MAINLINE_NETWORK_STACK", packageManager.getNameForUid(Binder.getCallingUid())) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    ShellUtils.execCommand("pidof cmdutils_server | xargs kill -9", false, false);
                    throw new SecurityException("uid:"+Binder.getCallingUid()+" does not have of the permission: android.permission.MAINLINE_NETWORK_STACK");
                }
            }

            if (SystemPropertiesUtils.get("ro.miui.cts") != null) {

                if (args.length == 3) {
                    String pkgName = args[0];
                    int uid = packageManager.getApplicationInfo(pkgName).uid;
                    int rule = Integer.parseInt(args[1]);
                    int type = Integer.parseInt(args[2]);

                    try {
                        Netd netd = new Netd(getService("netd", "android.net.INetd"));
                        @SuppressLint("PrivateApi")
                        Class<?> clz = multiJarClassLoader.loadClass("com.android.internal.net.IOemNetd$Stub");
                        Method asInterface = clz.getDeclaredMethod("asInterface", IBinder.class);
                        Object IOemNetdObj = asInterface.invoke(null, netd.getOemNetd());
                        Method setMiuiFirewallRule = IOemNetdObj.getClass().getDeclaredMethod("setMiuiFirewallRule", String.class, int.class, int.class, int.class);

                        boolean result = (boolean) setMiuiFirewallRule.invoke(IOemNetdObj, pkgName, uid, rule, type);

                        System.out.println(type == 0 ? "允许联网" : "禁止联网" +", 操作结果:"+result);

                    } catch (Exception e) {
                        System.err.println(Log.getStackTraceString(e));
                    }
                }

            }else {
                System.err.println("仅支持MIUI系统");
            }

            if (args.length == 1) {
                if (args[0].equals("unlockNCL")){
                    for (String packageName : packageManager.getInstalledApplications()){
                        for (NotificationChannel channel : notificationManager.getNotificationChannelsForPackage(packageName, false)){

                            int uid = packageManager.getApplicationInfo(packageName).uid;

                            notificationManager.setBlockable(channel, true);
                            notificationManager.unlockNotificationChannel(packageName, uid, channel.getId());
                            notificationManager.updateNotificationChannelForPackage(packageName, uid, channel);

                            if (notificationManager.getUserLockedFields(channel) == 0x00000004){
                                notificationManager.unlockFields(channel);
                                notificationManager.updateNotificationChannelForPackage(packageName, uid, channel);
                            }

                            if (!notificationManager.isBlockable(channel)) {
                                notificationManager.setBlockable(channel, true);
                                notificationManager.updateNotificationChannelForPackage(packageName, uid, channel);
                                System.out.println("解除限制 ”"+channel.getName()+"“  channelId: "+channel.getId());
                            }

                            if (notificationManager.isImportanceLocked(packageName, uid)){
                                notificationManager.setImportanceLockedByCriticalDeviceFunction(channel, false);
                                notificationManager.updateNotificationChannelForPackage(packageName, uid, channel);
                            }

                            if (notificationManager.ismBlockableSystem(channel) && !notificationManager.getmBlockableSystem(channel)){
                                System.out.println("修改为可被系统更改 "+channel.getName());
                                notificationManager.setmBlockableSystem(channel, true);
                                notificationManager.updateNotificationChannelForPackage(packageName, uid, channel);
                            }

                        }
                    }
                    System.out.println("已解锁所有锁定的通知, 请前往应用管理页面查看");
                }
            }

            System.exit(0);
            ShellUtils.execCommand("pidof cmdutils_server | xargs kill -9", true,false);
        }

        Looper.loop();
    }

    private static IInterface getService(String service, String type){
        try {
            IBinder binder = ServiceManager.getIBinderService(service);
            Class<?> clz = multiJarClassLoader.loadClass(type + "$Stub");
            Method asInterfaceMethod = clz.getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}