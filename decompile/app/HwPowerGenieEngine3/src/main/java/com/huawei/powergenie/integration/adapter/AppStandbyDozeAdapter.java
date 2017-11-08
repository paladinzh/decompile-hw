package com.huawei.powergenie.integration.adapter;

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.IDeviceIdleController;
import android.os.IDeviceIdleController.Stub;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.util.Log;
import java.lang.reflect.Method;

public class AppStandbyDozeAdapter {
    private static IDeviceIdleController mDeviceIdleController = null;
    private static boolean mDeviceIdledByPG = false;
    private static AppStandbyDozeAdapter sInstance;
    private Method mForceIdleMethod = null;
    private Method mIsAppActiveMethod = null;
    private Method mIsDeviceIdleModeMethod = null;
    private PowerManager mPm = null;
    private Method mSetAppInActiveMethod = null;
    private UsageStatsManager mUsm = null;

    public static synchronized AppStandbyDozeAdapter getInstance(Context context) {
        AppStandbyDozeAdapter appStandbyDozeAdapter;
        synchronized (AppStandbyDozeAdapter.class) {
            if (sInstance == null) {
                sInstance = new AppStandbyDozeAdapter(context);
            }
            appStandbyDozeAdapter = sInstance;
        }
        return appStandbyDozeAdapter;
    }

    private AppStandbyDozeAdapter(Context context) {
        this.mUsm = (UsageStatsManager) context.getSystemService("usagestats");
        if (this.mUsm != null) {
            try {
                this.mSetAppInActiveMethod = this.mUsm.getClass().getMethod("setAppInactive", new Class[]{String.class, Boolean.TYPE});
                this.mIsAppActiveMethod = this.mUsm.getClass().getMethod("isAppInactive", new Class[]{String.class});
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "AppStandbyDoze not support:" + e);
            }
        }
        try {
            this.mForceIdleMethod = Class.forName("com.huawei.android.os.DeviceidleEx").getMethod("forceIdle", new Class[0]);
        } catch (Exception e2) {
            Log.i("AppStandbyDozeAdapter", "AppStandbyDoze not support:" + e2);
        }
        this.mPm = (PowerManager) context.getSystemService("power");
        try {
            this.mIsDeviceIdleModeMethod = this.mPm.getClass().getMethod("isDeviceIdleMode", new Class[0]);
        } catch (Exception e22) {
            Log.i("AppStandbyDozeAdapter", "AppStandbyDoze not support:" + e22);
        }
        if (Integer.parseInt(VERSION.SDK) >= 23) {
            this.mPm = (PowerManager) context.getSystemService("power");
            mDeviceIdleController = Stub.asInterface(ServiceManager.getService("deviceidle"));
            if (mDeviceIdleController == null) {
                Log.i("AppStandbyDozeAdapter", "mDeviceIdleController is null");
                return;
            }
            return;
        }
        Log.i("AppStandbyDozeAdapter", "force device idle is disabled");
    }

    public void setAppInactive(String packageName, boolean inactive) {
        if (this.mSetAppInActiveMethod != null) {
            Log.i("AppStandbyDozeAdapter", "setAppInactive " + packageName + ":" + inactive);
            try {
                this.mSetAppInActiveMethod.invoke(this.mUsm, new Object[]{packageName, Boolean.valueOf(inactive)});
                return;
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "setAppInactive fail:" + e);
                return;
            }
        }
        Log.i("AppStandbyDozeAdapter", "setAppInactive not support");
    }

    public void forceDeviceToIdle() {
        if (this.mForceIdleMethod != null) {
            try {
                Log.i("AppStandbyDozeAdapter", "forceDeviceToIdle");
                this.mForceIdleMethod.invoke(null, new Object[0]);
                mDeviceIdledByPG = true;
                return;
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "forceDeviceToIdle fail:" + e);
                return;
            }
        }
        Log.i("AppStandbyDozeAdapter", "forceDeviceToIdle not support");
    }

    public void exitDeviceIdle() {
        if (mDeviceIdledByPG && mDeviceIdleController != null) {
            try {
                Log.i("AppStandbyDozeAdapter", "exitDeviceIdle");
                mDeviceIdleController.exitIdle("ByPG");
                mDeviceIdledByPG = false;
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "exitDeviceIdle fail:" + e);
            }
        }
    }

    public boolean isDeviceIdleMode() {
        if (this.mIsDeviceIdleModeMethod != null) {
            try {
                boolean ret = ((Boolean) this.mIsDeviceIdleModeMethod.invoke(this.mPm, new Object[0])).booleanValue();
                Log.i("AppStandbyDozeAdapter", "isDeviceIdleMode:" + ret);
                return ret;
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "isDeviceIdleMode fail:" + e);
            }
        } else {
            Log.i("AppStandbyDozeAdapter", "isDeviceIdleMode not support");
            return false;
        }
    }

    public void addWhiteList(String pkg) {
        if (mDeviceIdleController != null) {
            try {
                Log.i("AppStandbyDozeAdapter", "addWhiteList:" + pkg);
                mDeviceIdleController.addPowerSaveWhitelistApp(pkg);
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "addWhiteList fail:" + e);
            }
        }
    }

    public void removeWhiteList(String pkg) {
        if (mDeviceIdleController != null) {
            try {
                Log.i("AppStandbyDozeAdapter", "removeWhiteList:" + pkg);
                mDeviceIdleController.removePowerSaveWhitelistApp(pkg);
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "removeWhiteList fail:" + e);
            }
        }
    }

    public String[] getWhiteList() {
        if (mDeviceIdleController != null) {
            try {
                return mDeviceIdleController.getFullPowerWhitelist();
            } catch (Exception e) {
                Log.i("AppStandbyDozeAdapter", "addWhiteList fail:" + e);
            }
        }
        return null;
    }
}
