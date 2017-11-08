package com.android.huawei.coverscreen;

import android.content.Context;
import android.text.format.DateUtils;
import fyusion.vislib.BuildConfig;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatTime {
    private String mDateTimeStr = BuildConfig.FLAVOR;
    private String[] mValue = new String[5];

    public FormatTime(Context context, Calendar calendar) {
        if (context != null && calendar != null) {
            this.mValue = getTimeString(context, calendar);
            this.mDateTimeStr = getDateString(context, calendar.getTimeInMillis());
        }
    }

    public String getTimeString(int format) {
        String result = BuildConfig.FLAVOR;
        String language = Locale.getDefault().getLanguage();
        switch (format) {
            case 1:
                result = this.mValue[0];
                if (result == null || result.length() == 0) {
                    return this.mValue[4];
                }
                return result;
            case 2:
                if (language.contains("ar") || language.contains("fa") || language.contains("iw")) {
                    return this.mValue[4];
                }
                return this.mValue[0];
            case 3:
                if (language.contains("ar") || language.contains("fa") || language.contains("iw")) {
                    return this.mValue[0];
                }
                return this.mValue[4];
            case 4:
                return this.mValue[1];
            case 5:
                return this.mValue[3];
            case 6:
                return this.mValue[2];
            case 7:
                return this.mValue[1] + this.mValue[2] + this.mValue[3];
            case 8:
                return this.mValue[0] + this.mValue[1] + this.mValue[2] + this.mValue[3] + this.mValue[4];
            case 9:
                return this.mDateTimeStr;
            default:
                return result;
        }
    }

    public static String[] getTimeString(Context context, Calendar calendar) {
        String[] time = new String[5];
        Formatter currentTime = DateUtils.formatDateRange(context, new Formatter(), calendar.getTimeInMillis(), calendar.getTimeInMillis(), 2561, calendar.getTimeZone().getID());
        Matcher matcher = Pattern.compile("(\\D*)(\\d+)(.)(\\d+)(.*)").matcher(currentTime.toString());
        if (matcher.find()) {
            for (int i = 0; i < 5; i++) {
                time[i] = matcher.group(i + 1);
            }
        }
        currentTime.close();
        return time;
    }

    public static String getDateString(Context context, long millis) {
        return DateUtils.formatDateTime(context, millis, 98330);
    }
}
