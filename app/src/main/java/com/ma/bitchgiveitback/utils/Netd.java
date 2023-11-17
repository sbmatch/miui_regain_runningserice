package com.ma.bitchgiveitback.utils;

import android.os.IBinder;
import android.os.IInterface;

public class Netd {
    private IInterface manager;
    public Netd(IInterface netd) {
        this.manager = netd;
    }

    public IBinder getOemNetd() {
       try {
           return (IBinder) manager.getClass().getMethod("getOemNetd").invoke(manager);
       }catch (Throwable e){
           throw new SecurityException(e);
       }
    }
}
