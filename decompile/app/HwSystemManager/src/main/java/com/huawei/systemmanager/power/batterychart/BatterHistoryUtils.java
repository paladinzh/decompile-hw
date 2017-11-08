package com.huawei.systemmanager.power.batterychart;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.format.DateFormat;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import java.text.NumberFormat;
import libcore.icu.LocaleData;

public class BatterHistoryUtils {

    public interface UpdateCallBack {
        void onDragBarIdle(long j);

        void updateTime(long j, int i);
    }

    public static String formatPercentage(int percentage) {
        return formatPercentage(((double) percentage) / 100.0d);
    }

    private static String formatPercentage(double percentage) {
        return NumberFormat.getPercentInstance().format(percentage);
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService("connectivity");
        if (cm == null || cm.isNetworkSupported(0)) {
            return false;
        }
        return true;
    }

    public static boolean is24Hour() {
        return DateFormat.is24HourFormat(GlobalContext.getContext());
    }

    public static boolean isDayFirst() {
        String value = LocaleData.get(GlobalContext.getContext().getResources().getConfiguration().locale).getDateFormat(3);
        return value.indexOf(77) > value.indexOf(100);
    }
}
