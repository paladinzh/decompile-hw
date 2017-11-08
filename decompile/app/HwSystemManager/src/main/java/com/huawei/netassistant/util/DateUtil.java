package com.huawei.netassistant.util;

import android.text.TextUtils;
import com.huawei.netassistant.db.NetAssistantDBManager;
import com.huawei.systemmanager.util.HwLog;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private static final long DAYINMILLISEC = 86400000;
    public static final int MIN_DAY_OF_MONTH = 1;
    private static final String TAG = DateUtil.class.getSimpleName();

    public static long getDayStartTimeMills() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(1), calendar.get(2), calendar.get(5), 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndTimeMills() {
        return (86400000 + getDayStartTimeMills()) - 1;
    }

    public static int getCycleDayFromDB(String imsi) {
        int accountDay = NetAssistantDBManager.getInstance().getSettingBeginDate(imsi);
        if (accountDay < 1) {
            return 1;
        }
        if (accountDay > 31) {
            return 31;
        }
        return accountDay;
    }

    public static long getMonthStartTimeMills(String imsi) {
        Calendar calendar = Calendar.getInstance();
        if (TextUtils.isEmpty(imsi)) {
            calendar.setTime(new Date(monthStart(1)));
        } else {
            calendar.setTime(new Date(monthStart(getCycleDayFromDB(imsi))));
        }
        return calendar.getTimeInMillis();
    }

    public static long getCurrentTimeMills() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static boolean isInThisMonth(long timeforcheck, int accountday) {
        if (-1 == timeforcheck || timeforcheck < monthStart(accountday) || timeforcheck > monthEnd(accountday)) {
            return false;
        }
        return true;
    }

    public static boolean isInThisDay(long timeForCheck) {
        if (-1 == timeForCheck || timeForCheck < getDayStartTimeMills() || timeForCheck > getDayEndTimeMills()) {
            return false;
        }
        return true;
    }

    public static long monthStart(int accountDay) {
        Calendar localCalendar = Calendar.getInstance();
        int iYear = localCalendar.get(1);
        int iMonth = localCalendar.get(2);
        int iDay = localCalendar.get(5);
        int iActualDaysThisMonth = localCalendar.getActualMaximum(5);
        if (iDay < accountDay) {
            if (iMonth != 0) {
                iMonth--;
            } else {
                iYear--;
                iMonth = 11;
            }
            localCalendar.set(iYear, iMonth, 1, 0, 0, 0);
            iActualDaysThisMonth = localCalendar.getActualMaximum(5);
        }
        if (accountDay >= iActualDaysThisMonth) {
            accountDay = iActualDaysThisMonth;
        }
        localCalendar.set(iYear, iMonth, accountDay, 0, 0, 0);
        return localCalendar.getTimeInMillis();
    }

    public static long monthEnd(int accountDay) {
        Calendar localCalendar = Calendar.getInstance();
        int iYear = localCalendar.get(1);
        int iMonth = localCalendar.get(2);
        if (localCalendar.get(5) < accountDay) {
            if (iMonth != 0) {
                iMonth--;
            } else {
                iYear--;
                iMonth = 11;
            }
            localCalendar.set(iYear, iMonth, 1, 0, 0, 0);
        }
        if (iMonth != 11) {
            iMonth++;
        } else {
            iYear++;
            iMonth = 0;
        }
        localCalendar.set(iYear, iMonth, 1, 0, 0, 0);
        int iActualDaysThisMonth = localCalendar.getActualMaximum(5);
        if (accountDay > iActualDaysThisMonth) {
            accountDay = iActualDaysThisMonth;
        }
        localCalendar.set(iYear, iMonth, accountDay, 0, 0, 0);
        return localCalendar.getTimeInMillis();
    }

    public static String millisec2String(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(time));
    }

    public static int convertDayStartMillsToDate(long dayStartMills) {
        int date = 1;
        try {
            date = Integer.parseInt(new SimpleDateFormat("dd").format(Long.valueOf(dayStartMills)));
        } catch (NumberFormatException e) {
        }
        return date;
    }

    public static int convertHourMillsToDate(long hourMills) {
        int date = 1;
        try {
            date = Integer.parseInt(new SimpleDateFormat("HH").format(Long.valueOf(hourMills)));
        } catch (NumberFormatException e) {
        }
        return date;
    }

    public static int getYearMonth(String imsi) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        Calendar now = Calendar.getInstance();
        if (now.get(5) < getCycleDayFromDB(imsi)) {
            now.add(2, -1);
        }
        return Integer.parseInt(simpleDateFormat.format(now.getTime()));
    }

    public static int getLastYearMonth() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, -1);
        return Integer.parseInt(simpleDateFormat.format(calendar.getTime()));
    }

    public static long getNetworkUsageDays(String imsi) {
        return (System.currentTimeMillis() - getMonthStartTimeMills(imsi)) / 86400000;
    }

    public static String formatHourMinute(int hourOfDay, int minute) {
        Date date = new Date();
        date.setHours(hourOfDay);
        date.setMinutes(minute);
        return new SimpleDateFormat("HH:mm").format(date);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isBetweenCurrentTime(int startHour, int startMinute, int endHour, int endMinute) {
        Calendar calendar = Calendar.getInstance();
        int curHour = calendar.get(11);
        int curMinute = calendar.get(12);
        HwLog.d(TAG, "leisure time start :" + startHour + " : " + startMinute + " end : " + endHour + " : " + endMinute + " curTime = " + millisec2String(calendar.getTimeInMillis()));
        if (startHour > endHour || (startHour == endHour && startMinute > endMinute)) {
            if (startHour < curHour || ((startHour == curHour && startMinute < curMinute) || endHour > curHour || (endHour == curHour && endMinute > curMinute))) {
                return true;
            }
        } else if ((startHour < curHour || (startHour == curHour && startMinute < curMinute)) && (endHour > curHour || (endHour == curHour && endMinute > curMinute))) {
            return true;
        }
        return false;
    }

    public static long getWeekStartTimeMills(String imsi) {
        Calendar calendar = Calendar.getInstance();
        HwLog.i(TAG, "now is " + millisec2String(calendar.getTimeInMillis()));
        calendar.set(7, calendar.getFirstDayOfWeek());
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        HwLog.i(TAG, "week first day is " + millisec2String(calendar.getTimeInMillis()));
        return calendar.getTimeInMillis();
    }

    public static boolean beforeThisMonth(long initTimeMills, String imsi) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(initTimeMills));
        Calendar now = Calendar.getInstance();
        int cycleDay = getCycleDayFromDB(imsi);
        calendar.add(2, 1);
        calendar.set(5, cycleDay);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        HwLog.i(TAG, "calendar = " + millisec2String(calendar.getTimeInMillis()) + " now = " + millisec2String(now.getTimeInMillis()));
        return now.before(calendar);
    }
}
