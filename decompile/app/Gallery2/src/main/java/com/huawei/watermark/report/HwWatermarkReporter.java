package com.huawei.watermark.report;

import android.content.Context;
import android.util.Log;
import com.huawei.bd.Reporter;
import java.util.Locale;

public class HwWatermarkReporter {
    public static void reportSlidingSelectWatermark(Context context) {
        report(context, 2002);
    }

    public static void reportWatermarkSelectCategory(Context context, String categoryName) {
        report(context, 2001, String.format(Locale.US, "{category:%s}", new Object[]{categoryName}));
    }

    public static void reportHwWatermarkEdit(Context context) {
        report(context, 2000);
    }

    private static boolean report(Context context, int eventID, String eventMsg) {
        Log.v("HwWatermarkReporter", "HwWatermarkReporter report type:" + eventID + " msg:" + eventMsg);
        return Reporter.e(context, eventID, eventMsg, 20);
    }

    private static boolean report(Context context, int eventID) {
        Log.v("HwWatermarkReporter", "HwWatermarkReporter report type:" + eventID);
        return Reporter.c(context, eventID);
    }

    public static void reportConfirmLocationAndWeatherInWaterMarkMode(Context context, String result) {
        report(context, 2003, String.format(Locale.US, "{result:%s}", new Object[]{result}));
    }
}
