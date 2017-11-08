package com.huawei.keyguard.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import fyusion.vislib.BuildConfig;
import java.util.Calendar;
import java.util.Locale;

public class LunarCalendarUtils {
    public static String getLunar(Calendar cal, Context context) {
        String chinaDate = BuildConfig.FLAVOR;
        if (cal == null) {
            HwLog.w("LunarCalendarUtils", "getLunar show lunar calendar is null");
            return chinaDate;
        }
        int year = cal.get(1);
        int month = cal.get(2) + 1;
        int day = cal.get(5);
        chinaDate = getHolidayCalendar(month, day, context);
        if (chinaDate.equals(BuildConfig.FLAVOR)) {
            LunarCalendar lunarCal = new LunarCalendar(context);
            lunarCal.getLunarDate(year, month, day);
            return lunarCal.getChineseMonthDay();
        }
        HwLog.w("LunarCalendarUtils", "Showing solar Holiday");
        return chinaDate;
    }

    private static String getHolidayCalendar(int month, int day, Context context) {
        String dateStr = String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(month)}) + String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(day)});
        String holidayStr = BuildConfig.FLAVOR;
        try {
            int holiday;
            if (context.getResources().getConfiguration().locale.getCountry().equals("TW")) {
                holiday = LunarCalendar.getTWHoliday(dateStr);
            } else {
                holiday = LunarCalendar.getCNHoliday(dateStr);
            }
            return context.getResources().getString(holiday);
        } catch (NotFoundException e) {
            HwLog.w("LunarCalendarUtils", "no solar holiday!");
            return BuildConfig.FLAVOR;
        }
    }
}
