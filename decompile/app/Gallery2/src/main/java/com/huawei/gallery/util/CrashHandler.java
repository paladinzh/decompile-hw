package com.huawei.gallery.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import com.android.gallery3d.util.BusinessRadar;
import com.android.gallery3d.util.BusinessRadar.BugType;
import com.android.gallery3d.util.GalleryLog;
import java.lang.Thread.UncaughtExceptionHandler;

public class CrashHandler implements UncaughtExceptionHandler {
    private static Context mContext;
    private static UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler sInstance;
    private static long sLastCrashTime = 0;

    private CrashHandler() {
    }

    private static synchronized void initInstance() {
        synchronized (CrashHandler.class) {
            if (sInstance == null) {
                sInstance = new CrashHandler();
            }
        }
    }

    public static void init(Context ctx) {
        if (sInstance == null) {
            initInstance();
            sLastCrashTime = System.currentTimeMillis();
            mContext = ctx;
            CrashHandler handler = Thread.getDefaultUncaughtExceptionHandler();
            if (sInstance != handler) {
                mDefaultHandler = handler;
                Thread.setDefaultUncaughtExceptionHandler(sInstance);
            }
        }
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        GalleryLog.e("CrashHandler ", "Fatal  Gallery uncaughtException." + ex.getMessage());
        if (handleException(ex)) {
            GalleryLog.e("CrashHandler ", "Gallery handle uncaughtException");
        } else {
            handlerExceptionDefault(thread, ex);
        }
    }

    private void handlerExceptionDefault(Thread thread, Throwable ex) {
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            GalleryLog.e("CrashHandler ", "mDefaultHandler is null");
        }
    }

    private boolean handleException(Throwable ex) {
        if (ex != null && System.currentTimeMillis() - sLastCrashTime <= 3000) {
            BusinessRadar.report(BugType.CRASH_TOO_OFFEN, ex);
            try {
                clearUserData(mContext.getPackageName(), mContext);
            } catch (Throwable th) {
                GalleryLog.w("CrashHandler ", " clearUserData Error . " + th.getMessage());
            }
        }
        return false;
    }

    private void clearUserData(String pkgName, Context context) {
        if (((ActivityManager) context.getSystemService("activity")).clearApplicationUserData()) {
            GalleryLog.i("CrashHandler ", "Clear data success then reboot!");
            reboot();
            return;
        }
        GalleryLog.i("CrashHandler ", "Clear data failed!");
        handlerExceptionDefault(Thread.currentThread(), null);
    }

    private void reboot() {
        Process.killProcess(Process.myPid());
    }
}
