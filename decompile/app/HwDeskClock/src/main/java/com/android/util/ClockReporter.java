package com.android.util;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.bd.Reporter;

public class ClockReporter {
    private static String TAG = "ClockReporter";

    public static void reportEventMessage(Context context, int eventId, String eventMsg) {
        if (96 >= eventId) {
            if (!TextUtils.isEmpty(eventMsg)) {
                eventMsg = "";
            }
            if (!Reporter.e(context, eventId, eventMsg)) {
                Log.e(TAG, "There has an error!  event: " + eventId);
            }
        }
    }

    public static void reportEventContainMessage(Context context, int eventId, String eventMsg, int values) {
        if (!(96 < eventId || TextUtils.isEmpty(eventMsg) || Reporter.e(context, eventId, eventMsg))) {
            Log.e(TAG, "There has an error!  event: " + eventId);
        }
    }
}
