package com.ma.bitchgiveitback.xposedhook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import com.github.kyuubiran.ezxhelper.EzXHelper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage{
    Context targetContext;
    boolean isHooked = false;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.settings")) return;
        ez_init(lpparam);
        getTargetContext();
    }

    private void getTargetContext() {
        Class<?> app = XposedHelpers.findClass("com.android.settings.SettingsApplication", EzXHelper.getClassLoader());
        XposedBridge.hookAllMethods(app, "attachBaseContext", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                targetContext = (Context) param.thisObject;
                Class<?> clz = XposedHelpers.findClass("com.android.settings.SubSettings", EzXHelper.getClassLoader());
                XposedBridge.hookAllMethods(clz, "isValidFragment", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("碎片有效 --> "+param.args[0]);
                        hook();
                    }
                });
            }
        });
    }

    private void hook() {

        Class<?> preferenceManager = XposedHelpers.findClass("androidx.preference.PreferenceFragmentCompat", EzXHelper.getClassLoader());

        XposedHelpers.findAndHookMethod(preferenceManager,"getPreferenceScreen", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Object preferenceScreen = param.getResult();

                if (preferenceScreen == null) preferenceScreen = XposedHelpers.callMethod(preferenceManager, "createPreferenceScreen", targetContext);

                Object preferenceCategory = XposedHelpers.callMethod(preferenceScreen, "findPreference", "debug_misc_category");
                if (preferenceCategory != null) {

                    if (!isHooked) {
                        XposedHelpers.callMethod(preferenceCategory, "addPreference", buildrunningservices());
                        isHooked = true;
                    }
                }

            }
        });

    }

    private Object buildrunningservices() {

        Class<?> clz = XposedHelpers.findClassIfExists("com.android.settingslib.miuisettings.preference.ValuePreference", EzXHelper.getClassLoader());
        Object o_Instance = XposedHelpers.newInstance(clz, targetContext);
        XposedHelpers.callMethod(o_Instance ,"setKey", "runningservices");
        XposedHelpers.callMethod(o_Instance ,"setTitle", getString("runningservices_settings_title"));
        XposedHelpers.callMethod(o_Instance ,"setSummary" , getString("runningservices_settings_summary"));
        XposedHelpers.callMethod(o_Instance ,"setFragment", "com.android.settings.applications.RunningServices");
        XposedHelpers.callMethod(o_Instance, "setOrder", 9);
        XposedHelpers.callMethod(o_Instance, "setShowRightArrow", true);

        return o_Instance;
    }

    private String getString(String stringName){
        Resources resources = (Resources) XposedHelpers.callMethod(targetContext, "getResources");
        @SuppressLint("DiscouragedApi")
        int resourceId = resources.getIdentifier(stringName, "string", EzXHelper.getHostPackageName());

        if (resourceId != 0) {
            return resources.getString(resourceId);
        } else {
            return null;
        }
    }

    private void ez_init(XC_LoadPackage.LoadPackageParam lpparam){
        try {
            EzXHelper.initHandleLoadPackage(lpparam);
            EzXHelper.setClassLoader(lpparam.classLoader);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }
}
