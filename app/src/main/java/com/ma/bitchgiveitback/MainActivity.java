package com.ma.bitchgiveitback;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ma.bitchgiveitback.utils.PackageManager;
import com.ma.bitchgiveitback.utils.ServiceManager;

public class MainActivity extends Activity {

    PackageManager packageManager = ServiceManager.getPackageManager();
    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}