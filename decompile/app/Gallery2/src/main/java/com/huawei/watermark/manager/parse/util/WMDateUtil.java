package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class WMDateUtil {
    public static SimpleDateFormat consDateFormatFromString(String format) {
        if (WMStringUtil.isEmptyString(format)) {
            return null;
        }
        try {
            return new SimpleDateFormat(format);
        } catch (Exception e) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    }

    public static String getDate(Context context, String originFormat, SimpleDateFormat simpleDateFormat) {
        if ("systemymd".equals(originFormat)) {
            return DateUtils.formatDateTime(context, Calendar.getInstance().getTimeInMillis(), 131092);
        }
        if ("systemym".equals(originFormat)) {
            return DateUtils.formatDateTime(context, Calendar.getInstance().getTimeInMillis(), 131124);
        }
        if (!"systema systemh:mm".equals(originFormat)) {
            return simpleDateFormat.format(new Date());
        }
        int flag = 131073;
        if (DateFormat.is24HourFormat(context)) {
            flag = 131201;
        }
        return DateUtils.formatDateTime(context, Calendar.getInstance().getTimeInMillis(), flag);
    }

    public static String getDateAndTimeFormatStringFromSystem(Context context, String formattemp) {
        String result = formattemp;
        String ymd = getDateFormatStringFromSystem(context);
        if (ymd == null) {
            return null;
        }
        return trans12OR24TimeFormatStringFromSystem(context, transYMDTimeFormatStringFromSystem(formattemp, ymd));
    }

    private static String getDateFormatStringFromSystem(Context context) {
        char[] dates = DateUtils.formatDateTime(context, Calendar.getInstance().getTimeInMillis(), 131092).toCharArray();
        char[] dateOrders = DateFormat.getDateFormatOrder(context);
        if (WMCollectionUtil.isEmptyCollection(dates) || WMCollectionUtil.isEmptyCollection(dateOrders) || dateOrders.length > dates.length) {
            return null;
        }
        int j = 0;
        boolean isNotNum = false;
        for (int i = 0; i < dates.length; i++) {
            boolean isNum = false;
            int i1 = Character.getNumericValue(dates[i]);
            if (i1 >= 0 && i1 <= 9) {
                isNum = true;
            }
            if (isNum) {
                if (isNotNum) {
                    j++;
                    if (j >= dateOrders.length) {
                        break;
                    }
                }
                isNotNum = false;
                dates[i] = dateOrders[j];
            } else {
                isNotNum = true;
            }
        }
        String format = String.valueOf(dates);
        if (format.endsWith(".")) {
            format = format.substring(0, dates.length - 1);
        }
        return format;
    }

    private static String trans12OR24TimeFormatStringFromSystem(Context context, String formattemp) {
        String result = formattemp;
        boolean typeis12 = !DateFormat.is24HourFormat(context);
        if (formattemp.contains("systema")) {
            if (formattemp.contains(" systema")) {
                result = formattemp.replaceAll(" systema", typeis12 ? " a" : "");
            } else if (formattemp.contains("systema ")) {
                result = formattemp.replaceAll("systema ", typeis12 ? "a " : "");
            } else {
                result = formattemp.replaceAll("systema", typeis12 ? "a" : "");
            }
        }
        if (typeis12) {
            return result.replaceAll("systemh", "h");
        }
        return result.replaceAll("systemh", "H");
    }

    private static String transYMDTimeFormatStringFromSystem(String formattemp, String ymdSystem) {
        if (ymdSystem == null) {
            return formattemp;
        }
        if ("systemymd".equals(formattemp)) {
            return ymdSystem;
        }
        int i;
        char[] dates = ymdSystem.toCharArray();
        char systemSplitChar = '/';
        for (char valueOf : dates) {
            String temp = String.valueOf(valueOf);
            if (!isEqualsIgnoreCaseYMD(temp)) {
                systemSplitChar = temp.charAt(0);
                break;
            }
        }
        if ("systemym".equals(formattemp)) {
            String formatYM = ymdSystem.replaceAll("d", "").replaceAll("D", "");
            formatYM = formatYM.substring(Math.min(getmStart(formatYM), getyStart(formatYM)));
            formatYM = formatYM.substring(0, Math.max(getmEnd(formatYM), getyEnd(formatYM)) + 1);
            if (formatYM.indexOf(systemSplitChar) != formatYM.lastIndexOf(systemSplitChar)) {
                formatYM = formatYM.replaceFirst(String.valueOf(systemSplitChar), "");
            }
            return formatYM;
        }
        int systemYMDTagIndex = formattemp.indexOf("systemymd");
        if (systemYMDTagIndex == -1 || "systemymd".length() + systemYMDTagIndex >= formattemp.length()) {
            return formattemp;
        }
        int split_startindex = systemYMDTagIndex + "systemymd".length();
        String split = formattemp.substring(split_startindex, split_startindex + 1);
        char split_char = getSplitChar(systemSplitChar, split);
        Vector<Integer> datesRes = new Vector();
        getDatesRes(dates, split_char, datesRes);
        char[] res = new char[datesRes.size()];
        for (i = 0; i < datesRes.size(); i++) {
            res[i] = (char) ((Integer) datesRes.elementAt(i)).intValue();
        }
        return formattemp.replaceAll("systemymd" + split, String.valueOf(res));
    }

    private static void getDatesRes(char[] dates, char split_char, Vector<Integer> datesRes) {
        int i = 0;
        while (i < dates.length) {
            if (isEqualsIgnoreCaseYMD(String.valueOf(dates[i]))) {
                datesRes.add(Integer.valueOf(dates[i]));
            } else {
                dates[i] = split_char;
                if (i <= 0 || dates[i - 1] != split_char) {
                    datesRes.add(Integer.valueOf(split_char));
                }
            }
            i++;
        }
    }

    private static char getSplitChar(char systemSplitChar, String split) {
        char split_char = split.charAt(0);
        if (split_char == '#') {
            return systemSplitChar;
        }
        return split_char;
    }

    private static int getyEnd(String formatYM) {
        int yEnd = formatYM.lastIndexOf("y");
        if (yEnd == -1) {
            return formatYM.lastIndexOf("Y");
        }
        return yEnd;
    }

    private static int getmEnd(String formatYM) {
        int mEnd = formatYM.lastIndexOf("m");
        if (mEnd == -1) {
            return formatYM.lastIndexOf("M");
        }
        return mEnd;
    }

    private static int getyStart(String formatYM) {
        int yStart = formatYM.indexOf("y");
        if (yStart == -1) {
            return formatYM.indexOf("Y");
        }
        return yStart;
    }

    private static int getmStart(String formatYM) {
        int mStart = formatYM.indexOf("m");
        if (mStart == -1) {
            return formatYM.indexOf("M");
        }
        return mStart;
    }

    private static boolean isEqualsIgnoreCaseYMD(String temp) {
        return (temp.equalsIgnoreCase("y") || temp.equalsIgnoreCase("m")) ? true : temp.equalsIgnoreCase("d");
    }
}
