package com.huawei.gallery.story.utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.util.MyPrinter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class StoryAlbumDateUtils {
    private static final MyPrinter LOG = new MyPrinter("Clustering_DateUtils");
    private static final Map<Integer, DateRange> sHolidayRangeMap = new HashMap();
    private static Map<Integer, Integer> sHolidayStringMap = new HashMap();
    private static Map<Integer, String> sWeekendStringMap = new HashMap();

    private static class DateRange {
        public Calendar begin;
        public Calendar end;

        DateRange(Calendar begin, Calendar end) {
            this.begin = begin;
            this.end = end;
        }
    }

    public static class DateTaken {
        public long max;
        public long min;

        public DateTaken(long min, long max) {
            this.min = min;
            this.max = max;
        }

        public DateTaken(Cursor c) {
            this.max = c.getLong(0);
            this.min = c.getLong(1);
        }
    }

    static {
        sHolidayStringMap.put(Integer.valueOf(1), Integer.valueOf(R.string.story_album_title_new_year));
        sHolidayStringMap.put(Integer.valueOf(2), Integer.valueOf(R.string.story_album_title_valentine_day));
        sHolidayStringMap.put(Integer.valueOf(4), Integer.valueOf(R.string.story_album_title_labor_day));
        sHolidayStringMap.put(Integer.valueOf(8), Integer.valueOf(R.string.story_album_title_children_day));
        sHolidayStringMap.put(Integer.valueOf(16), Integer.valueOf(R.string.story_album_title_national_day));
        sHolidayStringMap.put(Integer.valueOf(32), Integer.valueOf(R.string.story_album_title_christmas_day));
        sHolidayStringMap.put(Integer.valueOf(SmsCheckResult.ESCT_192), Integer.valueOf(R.string.story_album_title_weekend));
        Calendar newYearBegin = Calendar.getInstance();
        newYearBegin.set(0, 11, 31);
        Calendar newYearEnd = Calendar.getInstance();
        newYearEnd.set(0, 0, 3);
        Calendar valentineDay = Calendar.getInstance();
        valentineDay.set(0, 1, 14);
        Calendar laborDayBegin = Calendar.getInstance();
        laborDayBegin.set(0, 3, 30);
        Calendar laborDayEnd = Calendar.getInstance();
        laborDayEnd.set(0, 4, 3);
        Calendar childrenDay = Calendar.getInstance();
        childrenDay.set(0, 5, 1);
        Calendar nationalDayBegin = Calendar.getInstance();
        nationalDayBegin.set(0, 8, 30);
        Calendar nationalDayEnd = Calendar.getInstance();
        nationalDayEnd.set(0, 9, 7);
        Calendar christmasDay = Calendar.getInstance();
        christmasDay.set(0, 11, 25);
        sHolidayRangeMap.put(Integer.valueOf(1), new DateRange(newYearBegin, newYearEnd));
        sHolidayRangeMap.put(Integer.valueOf(2), new DateRange(valentineDay, valentineDay));
        sHolidayRangeMap.put(Integer.valueOf(4), new DateRange(laborDayBegin, laborDayEnd));
        sHolidayRangeMap.put(Integer.valueOf(8), new DateRange(childrenDay, childrenDay));
        if (GalleryUtils.IS_CHINESE_VERSION) {
            sHolidayRangeMap.put(Integer.valueOf(16), new DateRange(nationalDayBegin, nationalDayEnd));
        }
        sHolidayRangeMap.put(Integer.valueOf(32), new DateRange(christmasDay, christmasDay));
    }

    public static boolean inHolidayRange(long millisec) {
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(millisec);
        for (DateRange dateRange : prepareHolidays(day.get(1)).values()) {
            if (dateCompare(day, dateRange.begin) >= 0 && dateCompare(day, dateRange.end) <= 0) {
                return true;
            }
        }
        return false;
    }

    public static String getStoryAlbumHolidayDateString(long minMillisec, long maxMillisec, Context context) {
        int holidayCode;
        Calendar beginDay = Calendar.getInstance();
        beginDay.setTimeInMillis(minMillisec);
        Calendar endDay = Calendar.getInstance();
        endDay.setTimeInMillis(maxMillisec);
        LOG.d(beginDay.get(1) + "/" + (beginDay.get(2) + 1) + "/" + beginDay.get(5) + " ~ " + endDay.get(1) + "/" + (endDay.get(2) + 1) + "/" + endDay.get(5));
        if (beginDay.get(1) == endDay.get(1)) {
            holidayCode = getHolidayCode(beginDay, endDay, prepareHolidays(beginDay.get(1))) | 0;
            if (holidayCode == 0) {
                holidayCode |= getWeekendCode(beginDay, endDay);
            }
        } else {
            holidayCode = (getHolidayCode(beginDay, null, prepareHolidays(beginDay.get(1))) | 0) | getHolidayCode(null, endDay, prepareHolidays(endDay.get(1)));
            if (holidayCode == 0) {
                holidayCode |= getWeekendCode(beginDay, endDay);
            }
        }
        LOG.d("holiday code = " + holidayCode);
        if (holidayCode != 0) {
            String holidayStr = getHolidayStringByCode(holidayCode, context.getResources());
            LOG.d("holidayStr = " + holidayStr);
            return holidayStr;
        }
        LOG.d("none holiday string ...");
        return "";
    }

    public static int getDayCount(long minDateTaken, long maxDateTaken) {
        Calendar beginDay = Calendar.getInstance();
        beginDay.setTimeInMillis(minDateTaken);
        Calendar endDay = Calendar.getInstance();
        endDay.setTimeInMillis(maxDateTaken);
        int dayCount = 0;
        while (dateCompare(beginDay, endDay) <= 0) {
            dayCount++;
            beginDay.add(6, 1);
        }
        return dayCount;
    }

    public static String getDateString(long millisec, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(millisec));
    }

    public static String getDateString(long minMillisec, long maxMillisec, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern, Locale.getDefault());
        String dateStr1 = df.format(new Date(minMillisec));
        String dateStr2 = df.format(new Date(maxMillisec));
        if (TextUtils.isEmpty(dateStr1) || !dateStr1.equalsIgnoreCase(dateStr2)) {
            return dateStr1 + " - " + dateStr2;
        }
        return dateStr1;
    }

    private static Map<Integer, DateRange> prepareHolidays(int year) {
        for (Entry<Integer, DateRange> entry : sHolidayRangeMap.entrySet()) {
            int i;
            Calendar begin = ((DateRange) entry.getValue()).begin;
            Calendar end = ((DateRange) entry.getValue()).end;
            if (((Integer) entry.getKey()).intValue() == 1) {
                i = year - 1;
            } else {
                i = year;
            }
            begin.set(1, i);
            end.set(1, year);
        }
        return sHolidayRangeMap;
    }

    private static String getHolidayStringByCode(int holidayCode, Resources resource) {
        String holidayStr = "";
        for (Integer holidayIndex : sHolidayRangeMap.keySet()) {
            if (holidayIndex.intValue() == (holidayIndex.intValue() & holidayCode)) {
                holidayStr = holidayStr.concat(resource.getString(((Integer) sHolidayStringMap.get(holidayIndex)).intValue()));
            }
        }
        if (!TextUtils.isEmpty(holidayStr)) {
            return holidayStr;
        }
        if (SmsCheckResult.ESCT_192 == (holidayCode & SmsCheckResult.ESCT_192)) {
            return holidayStr.concat(resource.getString(((Integer) sHolidayStringMap.get(Integer.valueOf(SmsCheckResult.ESCT_192))).intValue()));
        }
        if (64 == (holidayCode & 64)) {
            return holidayStr.concat((String) sWeekendStringMap.get(Integer.valueOf(64)));
        }
        if (128 == (holidayCode & 128)) {
            return holidayStr.concat((String) sWeekendStringMap.get(Integer.valueOf(128)));
        }
        return holidayStr;
    }

    private static int getHolidayCode(Calendar begin, Calendar end, Map<Integer, DateRange> holidays) {
        if ((begin == null && end == null) || holidays == null || holidays.size() == 0) {
            return 0;
        }
        int holidayCode = 0;
        for (Entry<Integer, DateRange> entry : holidays.entrySet()) {
            int holidayIndex = ((Integer) entry.getKey()).intValue();
            if ((begin == null || dateCompare(begin, ((DateRange) holidays.get(Integer.valueOf(holidayIndex))).end) <= 0) && (end == null || dateCompare(end, ((DateRange) holidays.get(Integer.valueOf(holidayIndex))).begin) >= 0)) {
                holidayCode |= holidayIndex;
            }
        }
        return holidayCode;
    }

    private static int getWeekendCode(Calendar begin, Calendar end) {
        Calendar day = (Calendar) begin.clone();
        int weekendCode = 0;
        DateFormat dateFormat = new SimpleDateFormat("EE", Locale.getDefault());
        while (dateCompare(day, end) <= 0) {
            Date date = new Date(day.getTimeInMillis());
            if (7 == day.get(7)) {
                weekendCode |= 64;
                sWeekendStringMap.put(Integer.valueOf(64), dateFormat.format(date));
            } else if (1 == day.get(7)) {
                weekendCode |= 128;
                sWeekendStringMap.put(Integer.valueOf(128), dateFormat.format(date));
            }
            day.add(6, 1);
        }
        return weekendCode;
    }

    private static int dateCompare(Calendar date1, Calendar date2) {
        int year1 = date1.get(1);
        int year2 = date2.get(1);
        int month1 = date1.get(2);
        int month2 = date2.get(2);
        int day1 = date1.get(5);
        int day2 = date2.get(5);
        if (year1 > year2) {
            return 1;
        }
        if (year1 < year2) {
            return -1;
        }
        if (month1 > month2) {
            return 1;
        }
        if (month1 < month2) {
            return -1;
        }
        if (day1 > day2) {
            return 1;
        }
        if (day1 < day2) {
            return -1;
        }
        return 0;
    }
}
