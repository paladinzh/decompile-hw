package com.android.util;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.MetaballPath;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatTime {
    private String mDateTimeStr = "";
    private String[] mValue = new String[5];

    public FormatTime(Context context, Calendar calendar) {
        if (context != null && calendar != null) {
            this.mValue = getTimeString(context, calendar);
            this.mDateTimeStr = getDateString(context, calendar.getTimeInMillis());
        }
    }

    public String getTimeString(int format) {
        String result = "";
        switch (format) {
            case 1:
                result = this.mValue[0];
                if (result == null || result.length() == 0) {
                    result = this.mValue[4];
                    break;
                }
            case 2:
                return getAMOrPM(2);
            case 3:
                return getAMOrPM(3);
            case MetaballPath.POINT_NUM /*4*/:
                result = this.mValue[1];
                break;
            case 5:
                result = this.mValue[3];
                break;
            case 6:
                result = this.mValue[2];
                break;
            case 7:
                result = this.mValue[1] + this.mValue[2] + this.mValue[3];
                break;
            case 8:
                result = this.mValue[0] + this.mValue[1] + this.mValue[2] + this.mValue[3] + this.mValue[4];
                break;
            case 9:
                result = this.mDateTimeStr;
                break;
        }
        return result;
    }

    public static SpannableStringBuilder getSpanTime(Context context, Calendar calendar) {
        String[] time = new String[5];
        String str = getformatDateRangeSegment(context, calendar);
        Matcher matcher = Pattern.compile("(\\D*)(\\d+)(.)(\\d+)(.*)").matcher(str);
        if (matcher.find()) {
            for (int i = 0; i < 5; i++) {
                time[i] = matcher.group(i + 1);
            }
        }
        String amPmStr = time[0];
        if (amPmStr == null || amPmStr.length() == 0) {
            amPmStr = time[4];
        }
        SpannableStringBuilder style_string = new SpannableStringBuilder(str);
        style_string.setSpan(new AbsoluteSizeSpan((int) context.getResources().getDimension(R.dimen.alarm_clock_item_digital_apmpm_tvfontsize), false), str.indexOf(amPmStr), str.indexOf(amPmStr) + amPmStr.length(), 34);
        style_string.setSpan(new TypefaceSpan("HwChinese-medium"), str.indexOf(amPmStr), str.indexOf(amPmStr) + amPmStr.length(), 34);
        return style_string;
    }

    private String getAMOrPM(int formatLeftOrRight) {
        String language = Locale.getDefault().getLanguage();
        int posLocal = 4;
        int pos = 0;
        if (3 == formatLeftOrRight) {
            posLocal = 0;
            pos = 4;
        }
        if (language.contains("ar") || language.contains("fa") || language.contains("iw")) {
            return this.mValue[posLocal];
        }
        return this.mValue[pos];
    }

    public static String[] getTimeString(Context context, Calendar calendar) {
        String[] time = new String[5];
        Matcher matcher = Pattern.compile("(\\D*)(\\d+)(.)(\\d+)(.*)").matcher(getformatDateRangeSegment(context, calendar));
        if (matcher.find()) {
            for (int i = 0; i < 5; i++) {
                time[i] = matcher.group(i + 1);
            }
        }
        return time;
    }

    public static String getDateString(Context context, long millis) {
        return DateUtils.formatDateTime(context, millis, 26);
    }

    public static Calendar getCalendar(TimeZone timezone) {
        Calendar calendar = Calendar.getInstance();
        if (timezone == null) {
            return calendar;
        }
        long millis = calendar.getTimeInMillis();
        calendar.setTimeInMillis(((long) (timezone.getOffset(millis) - TimeZone.getDefault().getOffset(millis))) + millis);
        return calendar;
    }

    public static String getformatDateRangeSegment(Context context, Calendar calendar) {
        Formatter formatTimeTmp;
        String formatTime = "";
        Formatter formatter = new Formatter();
        try {
            Class<?> clazz = Class.forName("com.huawei.android.text.format.DateUtilsEx");
            formatTime = (String) clazz.getMethod("formatChinaDateRange", new Class[]{Context.class, Formatter.class, Long.TYPE, Long.TYPE, Integer.TYPE, String.class}).invoke(clazz, new Object[]{context, formatter, Long.valueOf(calendar.getTimeInMillis()), Long.valueOf(calendar.getTimeInMillis()), Integer.valueOf(2561), calendar.getTimeZone().getID()});
            formatter.close();
            return formatTime;
        } catch (NoClassDefFoundError e) {
            Log.e("FormatTime", "formatChinaDateRange NoClassDefFoundError:" + e.getMessage());
            formatTimeTmp = DateUtils.formatDateRange(context, formatter, calendar.getTimeInMillis(), calendar.getTimeInMillis(), 2561, calendar.getTimeZone().getID());
            formatTime = formatTimeTmp.toString();
            formatter.close();
            formatTimeTmp.close();
            return formatTime;
        } catch (NoSuchMethodException e2) {
            Log.e("FormatTime", "formatChinaDateRange NoSuchMethodException:" + e2.getMessage());
            formatTimeTmp = DateUtils.formatDateRange(context, formatter, calendar.getTimeInMillis(), calendar.getTimeInMillis(), 2561, calendar.getTimeZone().getID());
            formatTime = formatTimeTmp.toString();
            formatter.close();
            formatTimeTmp.close();
            return formatTime;
        } catch (Exception e3) {
            Log.e("FormatTime", "formatChinaDateRange Exception:" + e3.getMessage());
            formatTimeTmp = DateUtils.formatDateRange(context, formatter, calendar.getTimeInMillis(), calendar.getTimeInMillis(), 2561, calendar.getTimeZone().getID());
            formatTime = formatTimeTmp.toString();
            formatter.close();
            formatTimeTmp.close();
            return formatTime;
        }
    }

    public static String formatNumber(int number) {
        return String.format(Locale.getDefault(), "%02d", new Object[]{Integer.valueOf(number)});
    }

    public static String getFormatTime(Context context, Calendar calendar) {
        Date date = new Date();
        date.setTime(calendar.getTimeInMillis());
        if (DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "Hms")).format(date);
        }
        String[] value = new String[7];
        Matcher matcher = Pattern.compile("(\\D*)(\\d+)(.)(\\d+)(.)(\\d+)(.*)").matcher(new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "hms")).format(date));
        if (matcher.find()) {
            for (int i = 0; i < 7; i++) {
                value[i] = matcher.group(i + 1);
            }
        }
        return value[1] + value[2] + value[3] + value[4] + value[5];
    }

    public static String checkFormatTimeString(String timeString) {
        if (TextUtils.isEmpty(timeString)) {
            return timeString;
        }
        String language = Locale.getDefault().getLanguage();
        if (language.contains("ar") || language.contains("fa") || language.contains("iw")) {
            return timeString.replace("‪", "");
        }
        return timeString;
    }
}
