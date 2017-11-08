package com.huawei.systemmanager.comm.misc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static long getTodayStartTime() {
        return getTodayStartCalendar().getTimeInMillis();
    }

    public static Calendar getTodayStartCalendar() {
        return getDateStartCalendar(Calendar.getInstance());
    }

    public static long getDayStartTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return getDateStartCalendar(calendar).getTimeInMillis();
    }

    private static Calendar getDateStartCalendar(Calendar calendar) {
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar;
    }

    public static String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}
