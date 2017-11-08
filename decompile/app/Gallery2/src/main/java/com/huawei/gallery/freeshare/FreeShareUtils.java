package com.huawei.gallery.freeshare;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class FreeShareUtils {
    private static int sWindowHeight;
    private static int sWindowWidth;

    public static void init(Context context) {
        WindowManager winmgr = (WindowManager) context.getSystemService("window");
        DisplayMetrics metrics = new DisplayMetrics();
        winmgr.getDefaultDisplay().getRealMetrics(metrics);
        if (context.getResources().getConfiguration().orientation == 2) {
            sWindowHeight = metrics.widthPixels;
            sWindowWidth = metrics.heightPixels;
            return;
        }
        sWindowHeight = metrics.heightPixels;
        sWindowWidth = metrics.widthPixels;
    }
}
