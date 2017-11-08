package com.huawei.mms.util;

import android.content.Context;
import com.huawei.bd.Reporter;
import java.text.SimpleDateFormat;

public class StatisticalHelper {
    public static void incrementReportCount(Context context, int eventID) {
        Reporter.c(context, eventID);
    }

    public static void reportEvent(Context context, int eventId, String eventMsg) {
        Reporter.e(context, eventId, eventMsg);
    }

    public static void reportTwoStateEvent(Context context, int eventId, boolean isOn) {
        Reporter.e(context, eventId, isOn ? "on" : "off");
    }

    public static String getFormatTime(long timeMillis) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(timeMillis));
    }
}
