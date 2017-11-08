package cn.com.xy.sms.sdk.ui.popu.util;

import cn.com.xy.sms.sdk.util.StringUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateAndTimeUtils {
    public static long timeStrTolong(String timeStr) {
        if (StringUtils.isNull(timeStr) || !timeStr.contains(":")) {
            return 0;
        }
        String[] time = timeStr.split(":");
        return (((Long.parseLong(time[0]) * 60) + Long.parseLong(time[1])) * 60) * 1000;
    }

    public static long convert2long(String date) {
        if (!StringUtils.isNull(date)) {
            try {
                return new SimpleDateFormat("MM-dd HH:mm").parse(date).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
