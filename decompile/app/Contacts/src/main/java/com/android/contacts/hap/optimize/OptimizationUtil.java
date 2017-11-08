package com.android.contacts.hap.optimize;

import android.app.Activity;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.View;
import com.android.contacts.util.LogConfig;

public final class OptimizationUtil {
    public static final boolean DEBUG = LogConfig.HWDBG;
    public static final boolean sIsLoadCompoentDelayedOnInsertContact;

    static {
        boolean z = false;
        if (!SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false)) {
            z = true;
        }
        sIsLoadCompoentDelayedOnInsertContact = z;
    }

    private OptimizationUtil() {
    }

    public static boolean isLoadCompoentDelayedOnInsertContact() {
        return sIsLoadCompoentDelayedOnInsertContact;
    }

    public static void postTaskToRunAferActivitylaunched(Activity activity, final Handler handler, final Runnable task) {
        if (activity != null && task != null && handler != null) {
            View decorView = activity.getWindow().getDecorView();
            if (decorView != null) {
                decorView.post(new Runnable() {
                    public void run() {
                        handler.post(task);
                    }
                });
            }
        }
    }
}
