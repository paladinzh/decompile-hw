package com.android.contacts.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import com.android.contacts.hap.utils.BackgroundGenricHandler;
import com.google.android.gms.location.places.Place;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RadarHwExceptionUpload {
    private static Object instance;
    private static Class<?> logException;
    private static Method mMsgMethod;
    private static String sApkVersion = "APK version: ";
    private static Context sContext;
    private static boolean sInitialized = false;
    private static String sPackageName = "Package: ";

    public static void uploadLogExt(final String msg, Exception e, final int level, final int bugType, final int sceneId) {
        log(msg, e);
        BackgroundGenricHandler.getInstance().postDelayed(new Runnable() {
            public void run() {
                RadarHwExceptionUpload.uploadLog(level, bugType, sceneId, msg);
            }
        }, 100);
    }

    private static void uploadLog(int logLevel, int bugType, int sceneDef, String msg) {
        if (!ActivityManager.isUserAMonkey()) {
            if (!sInitialized) {
                sInitialized = true;
                init();
            }
            try {
                Field levelField;
                String header = sPackageName + "\n" + sApkVersion + "\n" + "Bug type: " + bugType + "\n" + "Scene def: " + sceneDef + "\n";
                switch (logLevel) {
                    case Place.TYPE_MOVING_COMPANY /*65*/:
                        levelField = logException.getField("LEVEL_A");
                        break;
                    case Place.TYPE_MUSEUM /*66*/:
                        levelField = logException.getField("LEVEL_B");
                        break;
                    case Place.TYPE_NIGHT_CLUB /*67*/:
                        levelField = logException.getField("LEVEL_C");
                        break;
                    default:
                        return;
                }
                mMsgMethod.invoke(instance, new Object[]{"contacts", levelField.get(null), header, msg});
            } catch (RuntimeException e) {
                log("LogException.msg error: ", e);
            } catch (Exception e2) {
                log("LogException.msg error: ", e2);
            }
        }
    }

    private static void init() {
        try {
            sPackageName += sContext.getPackageName();
            PackageInfo info = sContext.getPackageManager().getPackageInfo(sContext.getPackageName(), 0);
            if (info.versionName != null) {
                sApkVersion += info.versionName;
            }
            logException = Class.forName("android.util.HwLogException");
            instance = logException.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
            mMsgMethod = logException.getMethod("msg", new Class[]{String.class, Integer.TYPE, String.class, String.class});
        } catch (RuntimeException e) {
            log("init error: ", e);
        } catch (Exception e2) {
            log("init error: ", e2);
        }
    }

    private static void log(String msg, Exception e) {
        if (e == null) {
            HwLog.e("RadarHwExceptionUpload", msg);
        } else {
            HwLog.e("RadarHwExceptionUpload", msg, e);
        }
    }
}
