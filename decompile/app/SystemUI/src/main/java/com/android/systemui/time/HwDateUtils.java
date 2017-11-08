package com.android.systemui.time;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import fyusion.vislib.BuildConfig;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import libcore.icu.DateIntervalFormat;

public class HwDateUtils extends DateUtils {
    public static String formatChinaDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags, String timeZone) {
        String date = formatDateRange(context, formatter, startMillis, endMillis, flags, timeZone).toString();
        if (DateFormat.is24HourFormat(context, UserSwitchUtils.getCurrentUser())) {
            HwLog.i("HwDateUtils", " is 24 hour format: date:" + date);
            return date;
        }
        String chinaDate = formatChinaDateTime(context, date);
        HwLog.i("HwDateUtils", "begin of formatChinaDateRange chinaDate:" + chinaDate);
        return chinaDate;
    }

    public static Formatter formatDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags, String timeZone) {
        if ((flags & 193) == 1) {
            flags |= DateFormat.is24HourFormat(context, UserSwitchUtils.getCurrentUser()) ? 128 : 64;
        }
        try {
            formatter.out().append(DateIntervalFormat.formatDateRange(startMillis, endMillis, flags, timeZone));
            return formatter;
        } catch (IOException impossible) {
            throw new AssertionError(impossible);
        }
    }

    public static String formatChinaDateRange(Context context, Formatter formatter, long startMillis, long endMillis, int flags) {
        return formatChinaDateRange(context, formatter, startMillis, endMillis, flags, null);
    }

    public static String formatChinaDateRange(Context context, long startMillis, long endMillis, int flags) {
        return formatChinaDateRange(context, new Formatter(new StringBuilder(50), Locale.getDefault()), startMillis, endMillis, flags);
    }

    public static String formatChinaDateTime(Context context, long millis, int flags) {
        return formatChinaDateRange(context, millis, millis, flags);
    }

    public static String formatChinaDateTime(Context context, String normalTime) {
        Locale defaultLocale = Locale.getDefault();
        Locale chinaLocale = new Locale("zh", "CN");
        Resources resources = context.getResources();
        try {
            String[] normal12Time = resources.getStringArray(33816581);
            String[] chinaTime = resources.getStringArray(33816582);
            if (defaultLocale == null) {
                HwLog.e("HwDateUtils", "defaultLocale == null, return !");
                return BuildConfig.FLAVOR;
            } else if (chinaLocale.getCountry().equals(defaultLocale.getCountry()) && chinaLocale.getLanguage().equals(defaultLocale.getLanguage())) {
                HwLog.i("HwDateUtils", " formatChinaDateTime equals.");
                for (int i = 0; i < normal12Time.length; i++) {
                    if (normalTime.contains(normal12Time[i])) {
                        normalTime = normalTime.replace(normal12Time[i], chinaTime[i]);
                        break;
                    }
                }
                HwLog.i("HwDateUtils", "formatChinaDateTime, change to China normalTime:" + normalTime);
                return normalTime;
            } else {
                HwLog.i("HwDateUtils", " formatChinaDateTime not equals.");
                HwLog.i("HwDateUtils", "formatChinaDateTime, change to China normalTime:" + normalTime);
                return normalTime;
            }
        } catch (NotFoundException e) {
            HwLog.i("HwDateUtils", "formatChinaDateTime Resources.NotFoundException ");
        }
    }
}
