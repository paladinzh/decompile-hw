package com.android.contacts.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Time;
import com.amap.api.services.core.AMapException;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.google.android.gms.R;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class LunarUtils {
    private static SimpleDateFormat NO_YEAR_DATE_FORMAT;
    private static String[] lunarDayArray;
    private static String[] lunarMonthArray;
    private static HashMap<Integer, String[]> lunarMonthsMap = new HashMap();
    private static String[] lunarWeekArray;
    private static String[] lunarYearArray;
    private static Context mContext;

    public static void init(Context aContext, int year) {
        setContext(aContext);
        initYearAndWeek(aContext);
        setLunarMonthsMap();
        setLunarDayArray();
        getLunarMonthScope(year);
    }

    public static void initYearAndWeek(Context aContext) {
        setContext(aContext);
        setLunarYearArray();
        setLunarWeekArray();
    }

    private static void setContext(Context aContext) {
        if (aContext != null) {
            mContext = aContext.getApplicationContext();
        }
    }

    public static boolean hasYear(Context context, String string) {
        if (string == null) {
            return false;
        }
        string = string.trim();
        if (string.length() == 0) {
            return false;
        }
        boolean noYearParsed;
        ParsePosition parsePosition = new ParsePosition(0);
        NO_YEAR_DATE_FORMAT = new SimpleDateFormat("--MM-dd", Locale.US);
        synchronized (NO_YEAR_DATE_FORMAT) {
            NO_YEAR_DATE_FORMAT.setTimeZone(DateUtils.UTC_TIMEZONE);
        }
        if ("--02-29".equals(string)) {
            noYearParsed = true;
        } else {
            synchronized (NO_YEAR_DATE_FORMAT) {
                NO_YEAR_DATE_FORMAT.parse(string, parsePosition);
            }
            noYearParsed = parsePosition.getIndex() == string.length();
        }
        HwLog.i("LunarUtils", "the input date has year to parse or not : noYearParsed = " + noYearParsed);
        return noYearParsed;
    }

    public static String solarToLunar(String data) {
        int[] time = subSolarFormat(data);
        if (time == null) {
            return getDefaultLunarDate();
        }
        int solarYear = time[0];
        int solarMonth = time[1] + 1;
        int solarDay = time[2];
        LunarDate a = new LunarDate();
        a.set(solarYear, solarMonth, solarDay, true);
        boolean isLunarMonth = a.islunarLeapMonth();
        int lunarYear = a.getLunarYear();
        int lunarMonth = a.getLunarMonth();
        int lunarDay = a.getLunarDay();
        int lunarIndex = LunarDate.getLeapMonthOfLunar(lunarYear);
        if (lunarIndex != 0 && lunarIndex < lunarMonth) {
            lunarMonth++;
        } else if (lunarIndex != 0 && lunarIndex == lunarMonth && isLunarMonth) {
            lunarMonth++;
        }
        return addFormat(lunarYear, lunarMonth, lunarDay);
    }

    public static String lunarToSolar(String data) {
        int[] time = subFormat(data);
        if (time == null) {
            return getDefaultSolarDate();
        }
        int lunarYear = time[0];
        int lunarMonth = time[1] + 1;
        int lunarDay = time[2];
        LunarDate a = new LunarDate();
        int lunarIndex = LunarDate.getLeapMonthOfLunar(lunarYear);
        boolean isLeap = false;
        if (lunarIndex != 0 && lunarIndex == lunarMonth - 1) {
            lunarMonth--;
            isLeap = true;
        } else if (lunarIndex != 0 && lunarIndex < lunarMonth - 1) {
            lunarMonth--;
        }
        a.set(lunarYear, lunarMonth, lunarDay, isLeap, true);
        return addFormat(a.getSolarYear(), a.getSolarMonth(), a.getSolarDay());
    }

    public static String addFormat(int aYear, int aMonth, int aDay) {
        StringBuilder sb = new StringBuilder();
        String monthOfYear = String.valueOf(aMonth);
        String dayOfMonth = String.valueOf(aDay);
        if (monthOfYear.length() == 1) {
            monthOfYear = "0" + monthOfYear;
        }
        if (dayOfMonth.length() == 1) {
            dayOfMonth = "0" + dayOfMonth;
        }
        sb.append(aYear).append("-").append(monthOfYear).append("-").append(dayOfMonth);
        return sb.toString();
    }

    public static int[] subFormat(String data) {
        if (data == null) {
            return null;
        }
        if (data.startsWith("-")) {
            data = getCurrentYear() + data.substring(1, data.length());
        }
        if (data.length() > 10) {
            data = data.substring(0, 10);
        }
        if (isValidDate(data)) {
            String[] lunarDate;
            if (data.contains("/")) {
                lunarDate = data.split("/");
            } else if (data.contains("-")) {
                lunarDate = data.split("-");
            } else {
                HwLog.i("LunarUtils", "Don't understand the date? Let's not show any dialog");
                return null;
            }
            if (lunarDate.length < 3) {
                HwLog.i("LunarUtils", "lunarDate length is wrong: " + lunarDate.length);
                return null;
            }
            try {
                int[] time = new int[3];
                time[0] = Integer.parseInt(lunarDate[0]);
                String aMonth = lunarDate[1];
                String aDay = lunarDate[2];
                if (aMonth.startsWith("0")) {
                    aMonth = aMonth.substring(1, aMonth.length());
                }
                if (aDay.startsWith("0")) {
                    aDay = aDay.substring(1, aDay.length());
                }
                time[1] = Integer.parseInt(aMonth) - 1;
                time[2] = Integer.parseInt(aDay);
                return time;
            } catch (NumberFormatException ne) {
                HwLog.e("LunarUtils", " !!! Dirty data !!!", ne);
                return null;
            }
        }
        HwLog.i("LunarUtils", "Not valid date format.");
        return null;
    }

    private static boolean isValidDate(String date) {
        boolean z = false;
        if (TextUtils.isEmpty(date)) {
            return false;
        }
        date = date.replace("-", "").replace("/", "");
        if (!TextUtils.isEmpty(date)) {
            z = date.matches("[0-9]+");
        }
        return z;
    }

    public static int[] subSolarFormat(String oldValue) {
        if (oldValue == null) {
            return null;
        }
        SimpleDateFormat dateFormatWithYear = DateUtils.getDateAndTimeFormat();
        Calendar calendar = Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
        int defaultYear = calendar.get(1);
        int[] time = new int[3];
        Date date1 = dateFormatWithYear.parse(oldValue, new ParsePosition(0));
        if (date1 == null) {
            date1 = DateUtils.parseDate(oldValue);
        }
        if (date1 != null) {
            calendar.setTime(date1);
            time[0] = calendar.get(1);
            time[1] = calendar.get(2);
            time[2] = calendar.get(5);
        } else if ("--02-29".equals(oldValue)) {
            time[0] = defaultYear;
            time[1] = 1;
            time[2] = 29;
        } else {
            if (oldValue.startsWith("--")) {
                oldValue = oldValue.substring(2, oldValue.length());
            }
            Date date2 = DateUtils.parseNoYearDate(oldValue);
            if (date2 == null) {
                HwLog.i("LunarUtils", "Don't understand the date? Let's not show any dialog");
                return null;
            }
            calendar.setTime(date2);
            time[0] = defaultYear;
            time[1] = calendar.get(2);
            time[2] = calendar.get(5);
        }
        return time;
    }

    public static String titleSolarToLunar(Context mContext, String data) {
        int[] time = subFormat(data);
        if (time == null) {
            time = subFormat(getDefaultSolarDate());
        }
        if (time == null) {
            return "";
        }
        int solarYear = time[0];
        int solarMonth = time[1] + 1;
        int solarDay = time[2];
        LunarDate a = new LunarDate();
        a.set(solarYear, solarMonth, solarDay, true);
        int lunarIndex = LunarDate.getLeapMonthOfLunar(a.getLunarYear());
        int lunarMonth = a.getLunarMonth();
        boolean isLunarMonth = a.islunarLeapMonth();
        if (lunarIndex != 0 && lunarIndex < lunarMonth) {
            lunarMonth++;
        } else if (lunarIndex != 0 && lunarIndex == lunarMonth && isLunarMonth) {
            lunarMonth++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(updateLunarTitle(mContext, a.getLunarYear(), lunarMonth, a.getLunarDay(), false));
        return sb.toString();
    }

    public static String updateLunarTitle(Context aContext, int year, int month, int day, boolean needCurrentWeek) {
        StringBuilder lunarTitle = new StringBuilder();
        String title = aContext.getString(R.string.event_lunar_title);
        int lunarIndex = LunarDate.getLeapMonthOfLunar(year);
        lunarTitle.append(getLunarMonthArray(aContext, lunarIndex)[month - 1]).append(HwCustPreloadContacts.EMPTY_STRING).append(getLunarDayArray()[day - 1]);
        boolean isLeapMonth = false;
        if (lunarIndex != 0 && month == lunarIndex + 1) {
            isLeapMonth = true;
        }
        if (lunarIndex != 0 && month >= lunarIndex + 1) {
            month--;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(title, new Object[]{Integer.valueOf(year), lunarTitle.toString()}));
        if (needCurrentWeek) {
            LunarDate a = new LunarDate();
            Calendar current = Calendar.getInstance();
            a.set(year, month, day, isLeapMonth, true);
            current.set(a.getSolarYear(), a.getSolarMonth() - 1, a.getSolarDay());
            sb.append(HwCustPreloadContacts.EMPTY_STRING).append(getLunarWeekArray()[current.get(7) - 1]);
        }
        return sb.toString();
    }

    public static String[] getLunarMonthScope(int mYear) {
        if (mYear == 0) {
            mYear = getCurrentYear();
        }
        int lunarMonthIndex = LunarDate.getLeapMonthOfLunar(mYear);
        String[] lunarMonths = (String[]) lunarMonthsMap.get(Integer.valueOf(lunarMonthIndex));
        if (lunarMonths != null) {
            return lunarMonths;
        }
        setLunarMonthsMap();
        return (String[]) lunarMonthsMap.get(Integer.valueOf(lunarMonthIndex));
    }

    public static String getValidityLunarDate(String oldValue) {
        int[] time = subFormat(oldValue);
        if (time == null) {
            return getDefaultLunarDate();
        }
        int solarYear = time[0];
        int solarMonth = time[1] + 1;
        int solarDay = time[2];
        if (solarYear < AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION) {
            return getDefaultLunarDate();
        }
        if (solarYear == AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION && solarMonth < 2) {
            return getDefaultLunarDate();
        }
        if (solarYear == AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION && solarMonth == 2 && solarDay < 8) {
            return getDefaultLunarDate();
        }
        if (solarYear > 2036) {
            return getDefaultLunarDate();
        }
        if (solarYear == 2036 && solarMonth > 1) {
            return getDefaultLunarDate();
        }
        if (solarYear == 2036 && solarMonth == 1 && solarDay > 27) {
            return getDefaultLunarDate();
        }
        return oldValue;
    }

    private static String getDefaultLunarDate() {
        return lunarToSolar(getDefaultSolarDate());
    }

    private static String getDefaultSolarDate() {
        int year = getCurrentYear();
        if (year > 2035) {
            year = 2035;
        } else if (year < AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION) {
            year = AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION;
        }
        return addFormat(year, 1, 1);
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(1);
    }

    private static void setLunarWeekArray() {
        lunarWeekArray = mContext.getResources().getStringArray(R.array.lunar_week_day);
    }

    public static String[] getLunarWeekArray() {
        if (lunarWeekArray == null) {
            setLunarWeekArray();
        }
        return (String[]) lunarWeekArray.clone();
    }

    private static void setLunarYearArray() {
        String yearTag = mContext.getString(R.string.event_lunar_year);
        lunarYearArray = new String[134];
        for (int i = 0; i < 134; i++) {
            lunarYearArray[i] = String.format(yearTag, new Object[]{Integer.valueOf(i + AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION)});
        }
    }

    public static String[] getLunarYearArray() {
        if (lunarYearArray == null) {
            setLunarYearArray();
        }
        return (String[]) lunarYearArray.clone();
    }

    private static void setLunarDayArray() {
        lunarDayArray = mContext.getResources().getStringArray(R.array.lunar_birthday_day);
    }

    public static String[] getLunarDayArray() {
        if (lunarDayArray == null) {
            setLunarDayArray();
        }
        return (String[]) lunarDayArray.clone();
    }

    public static synchronized String[] getLunarMonthArray(Context aContext, int lunarIndex) {
        String[] aLunarMonthArray;
        synchronized (LunarUtils.class) {
            lunarMonthArray = (String[]) lunarMonthsMap.get(Integer.valueOf(lunarIndex));
            if (lunarMonthArray == null) {
                setContext(aContext);
                setLunarMonthsMap();
                lunarMonthArray = (String[]) lunarMonthsMap.get(Integer.valueOf(lunarIndex));
            }
            aLunarMonthArray = (String[]) lunarMonthArray.clone();
        }
        return aLunarMonthArray;
    }

    private static void setLunarMonthsMap() {
        lunarMonthsMap.put(Integer.valueOf(0), mContext.getResources().getStringArray(R.array.lunar_basic_month));
        int index = 1 + 1;
        lunarMonthsMap.put(Integer.valueOf(1), mContext.getResources().getStringArray(R.array.lunar_one_month));
        int index2 = index + 1;
        lunarMonthsMap.put(Integer.valueOf(index), mContext.getResources().getStringArray(R.array.lunar_two_month));
        index = index2 + 1;
        lunarMonthsMap.put(Integer.valueOf(index2), mContext.getResources().getStringArray(R.array.lunar_three_month));
        index2 = index + 1;
        lunarMonthsMap.put(Integer.valueOf(index), mContext.getResources().getStringArray(R.array.lunar_four_month));
        index = index2 + 1;
        lunarMonthsMap.put(Integer.valueOf(index2), mContext.getResources().getStringArray(R.array.lunar_five_month));
        index2 = index + 1;
        lunarMonthsMap.put(Integer.valueOf(index), mContext.getResources().getStringArray(R.array.lunar_six_month));
        index = index2 + 1;
        lunarMonthsMap.put(Integer.valueOf(index2), mContext.getResources().getStringArray(R.array.lunar_seven_month));
        index2 = index + 1;
        lunarMonthsMap.put(Integer.valueOf(index), mContext.getResources().getStringArray(R.array.lunar_eight_month));
        index = index2 + 1;
        lunarMonthsMap.put(Integer.valueOf(index2), mContext.getResources().getStringArray(R.array.lunar_nine_month));
        index2 = index + 1;
        lunarMonthsMap.put(Integer.valueOf(index), mContext.getResources().getStringArray(R.array.lunar_ten_month));
        index = index2 + 1;
        lunarMonthsMap.put(Integer.valueOf(index2), mContext.getResources().getStringArray(R.array.lunar_eleven_month));
        lunarMonthsMap.put(Integer.valueOf(index), mContext.getResources().getStringArray(R.array.lunar_twelve_month));
    }

    public static Intent getEventIntent(long millis) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse("content://com.android.calendar/time/" + millis));
        intent.putExtra("com.android.calendar.widget", true);
        intent.putExtra("VIEW", "MONTH");
        intent.setFlags(805437440);
        return intent;
    }

    public static long getNextSolarBirthday(String data, boolean currentYear) {
        if (data.length() > 10) {
            data = data.substring(0, 10);
        }
        Calendar current = Calendar.getInstance();
        int[] time = subSolarFormat(data);
        if (time == null) {
            return 0;
        }
        int solarYear = time[0];
        int solarMonth = time[1] + 1;
        int solarDay = time[2];
        if (solarYear > 2037) {
            return -1;
        }
        int currentY = current.get(1);
        int currentM = current.get(2) + 1;
        int currentD = current.get(5);
        int increase = currentY % 4;
        if (currentYear && currentY >= solarYear) {
            solarYear = (solarMonth == 2 && solarDay == 29) ? increase == 0 ? currentM > solarMonth ? currentY + 4 : currentY : currentY + (4 - increase) : currentM > solarMonth ? currentY + 1 : currentM == solarMonth ? currentD > solarDay ? currentY + 1 : currentY : currentY;
        }
        if (new Date(solarYear, solarMonth - 1, solarDay).after(new Date(2037, 11, 31))) {
            return -1;
        }
        current.set(solarYear, solarMonth - 1, solarDay);
        long millis = current.getTimeInMillis();
        if (HwLog.HWDBG) {
            HwLog.d("LunarUtils", "getDateStringToMillis : millis = " + millis);
        }
        return millis;
    }

    public static Long getNextLunarBirthday(String data) {
        int[] times = subSolarFormat(data);
        if (times == null) {
            return Long.valueOf(0);
        }
        LunarDate a = new LunarDate();
        a.set(times[0], times[1] + 1, times[2], true);
        boolean isLeaf = a.islunarLeapMonth();
        int year = a.getLunarYear();
        int month = a.getLunarMonth();
        int monthDay = a.getLunarDay();
        if (year > 2035) {
            return Long.valueOf(-1);
        }
        LunarDate nextBirthday = new LunarDate();
        nextBirthday.lunarMonth = month;
        nextBirthday.lunarDay = monthDay;
        nextBirthday.lunarYear = year;
        nextBirthday.lunarLeapMonth = isLeaf;
        nextBirthday.lunarLeapMonth = false;
        boolean isThirtyDay = monthDay == 30;
        Time time = new Time();
        time.setToNow();
        time.normalize(true);
        Date date = new Date(2036, 0, 27);
        if (new Date(time.year, time.month, time.monthDay).after(date)) {
            return Long.valueOf(-1);
        }
        LunarDate nowLunarDate = new LunarDate();
        nowLunarDate.set(time.year, time.month + 1, time.monthDay, true);
        if (nowLunarDate.lunarYear < year) {
            nextBirthday.set(year, month, monthDay, isLeaf, false);
            convertTime(time, nextBirthday);
            return Long.valueOf(time.toMillis(true));
        }
        nextBirthday.lunarYear = nowLunarDate.lunarYear;
        if (month < nowLunarDate.lunarMonth) {
            nextBirthday.lunarYear++;
        } else if (month != nowLunarDate.lunarMonth || monthDay >= nowLunarDate.lunarDay) {
            if (nowLunarDate.lunarYear == year) {
                if (month == nowLunarDate.lunarMonth && nowLunarDate.lunarLeapMonth && !isLeaf) {
                    nextBirthday.lunarYear++;
                } else {
                    nextBirthday.lunarLeapMonth = isLeaf;
                }
            } else if (month == nowLunarDate.lunarMonth && nowLunarDate.lunarLeapMonth) {
                nextBirthday.lunarLeapMonth = true;
            }
        } else if (nowLunarDate.lunarYear <= year) {
            nextBirthday.lunarYear++;
        } else if (nowLunarDate.lunarLeapMonth) {
            nextBirthday.lunarYear++;
        } else if (LunarDate.hasLeapOfLunarMonth(nextBirthday.lunarYear, nextBirthday.lunarMonth)) {
            nextBirthday.lunarLeapMonth = true;
        } else {
            nextBirthday.lunarYear++;
        }
        if (nextBirthday.lunarYear > 2035) {
            return Long.valueOf(-1);
        }
        if (!isThirtyDay || nextBirthday.validateLunarDate()) {
            nextBirthday.set(nextBirthday.lunarYear, nextBirthday.lunarMonth, nextBirthday.lunarDay, nextBirthday.lunarLeapMonth, false);
        } else {
            nextBirthday.set(nextBirthday.lunarYear, nextBirthday.lunarMonth, 29, nextBirthday.lunarLeapMonth, false);
        }
        if (!nextBirthday.validateLunarDate()) {
            while (nextBirthday.solarYear > AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION) {
                nextBirthday.solarYear--;
                if (nextBirthday.validateLunarDate()) {
                    break;
                }
            }
        }
        convertTime(time, nextBirthday);
        if (new Date(time.year, time.month, time.monthDay).after(date)) {
            return Long.valueOf(-1);
        }
        return Long.valueOf(time.toMillis(true));
    }

    private static void convertTime(Time time, LunarDate lunarDate) {
        time.year = lunarDate.solarYear;
        time.month = lunarDate.solarMonth - 1;
        time.monthDay = lunarDate.solarDay;
        time.normalize(true);
    }

    public static boolean supportLunarAccount(String lAccountType, Context context) {
        if (!isChineseRegion(context)) {
            return false;
        }
        if (lAccountType == null) {
            return true;
        }
        return lAccountType.equalsIgnoreCase("com.android.huawei.phone");
    }

    public static boolean isChineseRegion(Context context) {
        if (context == null || context.getResources() == null) {
            return false;
        }
        Configuration configuration = context.getResources().getConfiguration();
        String language = configuration.locale.getLanguage();
        String country = configuration.locale.getCountry();
        if (language.equals("zh") && (country.equals("CN") || country.equals("TW") || country.equals("HK"))) {
            return true;
        }
        return false;
    }

    public static boolean checkTimeValidity(boolean isLunarBirthday, String oldValue) {
        boolean z = true;
        boolean z2 = false;
        int[] time = isLunarBirthday ? subFormat(oldValue) : subSolarFormat(oldValue);
        if ((!isLunarBirthday && subFormat(oldValue) == null) || time == null || time.length != 3) {
            return false;
        }
        int year = time[0];
        if (isLunarBirthday) {
            if (year < AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION || year > 2035) {
                z = false;
            }
            return z;
        }
        if (year >= AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR && year <= 2037) {
            z2 = true;
        }
        return z2;
    }
}
