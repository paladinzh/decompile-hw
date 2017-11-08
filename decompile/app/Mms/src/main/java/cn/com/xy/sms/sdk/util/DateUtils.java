package cn.com.xy.sms.sdk.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/* compiled from: Unknown */
public class DateUtils {
    public static final long ONE_DAY_TIME = 86400000;

    public static String addDays(String str, String str2, int i) {
        return getTimeString(str2, getTime(str, str2) + (((long) i) * 86400000));
    }

    public static boolean compareDateString(String str, String str2, String str3) {
        return !((getTime(str, str3) > getTime(str2, str3) ? 1 : (getTime(str, str3) == getTime(str2, str3) ? 0 : -1)) <= 0);
    }

    public static String getCurrentTimeString(String str) {
        String str2 = "";
        try {
            str2 = new SimpleDateFormat(str).format(new Date(System.currentTimeMillis()));
        } catch (Throwable th) {
        }
        return str2;
    }

    public static long getTime(String str, String str2) {
        try {
            return new SimpleDateFormat(str2).parse(str).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public static String getTimeString(String str, long j) {
        String str2 = "";
        try {
            str2 = new SimpleDateFormat(str).format(new Date(j));
        } catch (Throwable th) {
        }
        return str2;
    }
}
