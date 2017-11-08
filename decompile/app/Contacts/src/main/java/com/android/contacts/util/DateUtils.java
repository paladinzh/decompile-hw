package com.android.contacts.util;

import android.content.Context;
import com.android.contacts.model.dataitem.DataKind;
import com.google.android.gms.R;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    private static final SimpleDateFormat[] DATE_FORMATS = new SimpleDateFormat[]{CommonDateUtils.FULL_DATE_FORMAT, CommonDateUtils.DATE_AND_TIME_FORMAT, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US), new SimpleDateFormat("yyyy/MM/dd", Locale.US), new SimpleDateFormat("yyyyMMdd", Locale.US), new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'", Locale.US), new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US), new SimpleDateFormat("yyyyMMdd'T'HHmm'Z'", Locale.US)};
    private static DateFormat FORMAT_WITHOUT_YEAR_DAY_FIRST;
    private static DateFormat FORMAT_WITHOUT_YEAR_MONTH_FIRST;
    private static final SimpleDateFormat[] NO_YEAR_DATE_FORMATS = new SimpleDateFormat[]{CommonDateUtils.NO_YEAR_DATE_FORMAT, CommonDateUtils.NO_YEAR_DATE_AND_TIME_FORMAT, new SimpleDateFormat("-/MM/dd", Locale.US), new SimpleDateFormat("MMdd", Locale.US)};
    public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
    static Object lock = new Object();

    static {
        SimpleDateFormat format;
        int i = 0;
        synchronized (DateUtils.class) {
        }
        for (SimpleDateFormat format2 : DATE_FORMATS) {
            synchronized (format2) {
                format2.setLenient(true);
                format2.setTimeZone(UTC_TIMEZONE);
            }
        }
        SimpleDateFormat[] simpleDateFormatArr = NO_YEAR_DATE_FORMATS;
        int length = simpleDateFormatArr.length;
        while (i < length) {
            format2 = simpleDateFormatArr[i];
            synchronized (format2) {
                format2.setLenient(true);
                format2.setTimeZone(UTC_TIMEZONE);
            }
            i++;
        }
        synchronized (CommonDateUtils.NO_YEAR_DATE_FORMAT) {
            CommonDateUtils.NO_YEAR_DATE_FORMAT.setTimeZone(UTC_TIMEZONE);
        }
        synchronized (CommonDateUtils.NO_YEAR_DATE_AND_TIME_FORMAT) {
            CommonDateUtils.NO_YEAR_DATE_AND_TIME_FORMAT.setTimeZone(UTC_TIMEZONE);
        }
    }

    public static SimpleDateFormat getNoYearDateFormat() {
        SimpleDateFormat simpleDateFormat;
        synchronized (CommonDateUtils.NO_YEAR_DATE_FORMAT) {
            simpleDateFormat = CommonDateUtils.NO_YEAR_DATE_FORMAT;
        }
        return simpleDateFormat;
    }

    public static SimpleDateFormat getFullDateFormat() {
        SimpleDateFormat simpleDateFormat;
        synchronized (CommonDateUtils.FULL_DATE_FORMAT) {
            simpleDateFormat = CommonDateUtils.FULL_DATE_FORMAT;
        }
        return simpleDateFormat;
    }

    public static SimpleDateFormat getDateAndTimeFormat() {
        SimpleDateFormat simpleDateFormat;
        synchronized (CommonDateUtils.DATE_AND_TIME_FORMAT) {
            simpleDateFormat = CommonDateUtils.DATE_AND_TIME_FORMAT;
        }
        return simpleDateFormat;
    }

    public static SimpleDateFormat getNoYearDateAndTimeFormat() {
        SimpleDateFormat simpleDateFormat;
        synchronized (CommonDateUtils.NO_YEAR_DATE_AND_TIME_FORMAT) {
            simpleDateFormat = CommonDateUtils.NO_YEAR_DATE_AND_TIME_FORMAT;
        }
        return simpleDateFormat;
    }

    public static void onLocaleChange() {
        synchronized (lock) {
            FORMAT_WITHOUT_YEAR_MONTH_FIRST = new SimpleDateFormat("MMM d", Locale.getDefault());
            FORMAT_WITHOUT_YEAR_MONTH_FIRST.setTimeZone(TimeZone.getTimeZone("UTC"));
            FORMAT_WITHOUT_YEAR_DAY_FIRST = new SimpleDateFormat("d MMM", Locale.getDefault());
            FORMAT_WITHOUT_YEAR_DAY_FIRST.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Date parseDate(String string) {
        ParsePosition parsePosition = new ParsePosition(0);
        int i = 0;
        while (i < DATE_FORMATS.length) {
            SimpleDateFormat f = DATE_FORMATS[i];
            synchronized (f) {
                parsePosition.setIndex(0);
                Date date = f.parse(string, parsePosition);
                if (parsePosition.getIndex() == string.length()) {
                    return date;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Date parseNoYearDate(String string) {
        ParsePosition parsePosition = new ParsePosition(0);
        int i = 0;
        while (i < NO_YEAR_DATE_FORMATS.length) {
            SimpleDateFormat f = NO_YEAR_DATE_FORMATS[i];
            synchronized (f) {
                parsePosition.setIndex(0);
                Date date = f.parse(string, parsePosition);
                if (parsePosition.getIndex() == string.length()) {
                    return date;
                }
            }
        }
        return null;
    }

    private static final Date getUtcDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance(UTC_TIMEZONE, Locale.US);
        calendar.set(1, year);
        calendar.set(2, month);
        calendar.set(5, dayOfMonth);
        return calendar.getTime();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String formatDate(Context context, String string) {
        Throwable th;
        if (string == null) {
            return null;
        }
        string = string.trim();
        if (string.length() == 0) {
            return string;
        }
        Date date;
        boolean noYearParsed;
        ParsePosition parsePosition = new ParsePosition(0);
        if ("--02-29".equals(string)) {
            date = getUtcDate(0, 1, 29);
            noYearParsed = true;
        } else {
            synchronized (CommonDateUtils.NO_YEAR_DATE_FORMAT) {
                date = CommonDateUtils.NO_YEAR_DATE_FORMAT.parse(string, parsePosition);
            }
            noYearParsed = parsePosition.getIndex() == string.length();
        }
        DateFormat outFormat;
        String format;
        if (noYearParsed) {
            if (FORMAT_WITHOUT_YEAR_MONTH_FIRST == null) {
                onLocaleChange();
            }
            if (isMonthBeforeDay(context)) {
                outFormat = FORMAT_WITHOUT_YEAR_MONTH_FIRST;
            } else {
                outFormat = FORMAT_WITHOUT_YEAR_DAY_FIRST;
            }
            synchronized (outFormat) {
                try {
                    if (!Locale.getDefault().getLanguage().equals(Locale.CHINA.getLanguage())) {
                        format = outFormat.format(date);
                        return format;
                    } else if (isMonthBeforeDay(context) || outFormat != FORMAT_WITHOUT_YEAR_DAY_FIRST) {
                        format = new SimpleDateFormat("M" + context.getResources().getString(R.string.chinese_month) + "d" + context.getResources().getString(R.string.chinese_day)).format(date);
                        return format;
                    } else {
                        try {
                            format = new SimpleDateFormat("d" + context.getResources().getString(R.string.chinese_day) + "M" + context.getResources().getString(R.string.chinese_month)).format(date);
                            return format;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    DateFormat dateFormat = outFormat;
                    throw th;
                }
            }
        }
        int i = 0;
        while (i < DATE_FORMATS.length) {
            SimpleDateFormat f = DATE_FORMATS[i];
            synchronized (f) {
                parsePosition.setIndex(0);
                date = f.parse(string, parsePosition);
                if (parsePosition.getIndex() != string.length() || date == null) {
                } else {
                    outFormat = android.text.format.DateFormat.getMediumDateFormat(context);
                    outFormat.setTimeZone(UTC_TIMEZONE);
                    format = outFormat.format(date);
                    return format;
                }
            }
        }
        return string;
    }

    public static boolean isMonthBeforeDay(Context context) {
        String dateFormatOrder = ((SimpleDateFormat) android.text.format.DateFormat.getLongDateFormat(context)).toPattern();
        for (int i = 0; i < dateFormatOrder.length(); i++) {
            char c = dateFormatOrder.charAt(i);
            if (c == 'd') {
                return false;
            }
            if (c == 'M') {
                return true;
            }
        }
        return false;
    }

    public static void setDataKindDateFormat(DataKind kind, boolean isDateWithTime) {
        if (isDateWithTime) {
            kind.dateFormatWithoutYear = CommonDateUtils.NO_YEAR_DATE_AND_TIME_FORMAT;
            kind.dateFormatWithYear = CommonDateUtils.DATE_AND_TIME_FORMAT;
            return;
        }
        kind.dateFormatWithoutYear = CommonDateUtils.NO_YEAR_DATE_FORMAT;
        kind.dateFormatWithYear = CommonDateUtils.FULL_DATE_FORMAT;
    }

    public static int getDefaultDateFormat(boolean showYear) {
        if (showYear) {
            return 98326;
        }
        return 65560;
    }

    public static int getDefaultDateTimeFormat() {
        return 231957;
    }

    public static int getHourTimeFormat() {
        return 2561;
    }

    public static int getYearTimeFormat() {
        return 231940;
    }

    public static int getMonthDateFormat() {
        return 231936;
    }

    public static boolean isinitialized() {
        if (CommonDateUtils.NO_YEAR_DATE_FORMAT == null) {
            return false;
        }
        return true;
    }

    public static String convertDateToVersion(long date) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(date);
        int year = mCalendar.get(1);
        if (year > 2015) {
            year -= 2015;
        } else {
            year = 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append((year / 10) + 1).append('.').append(year % 10).append('.').append(mCalendar.get(2) + 1).append('.').append(mCalendar.get(5));
        return sb.toString();
    }
}
