package com.huawei.mms.util;

import android.content.Context;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.format.DateUtils;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.huawei.android.text.format.DateUtilsEx;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Formatter;

public class HwSpecialUtils {
    public static final HwCustHwSpecialUtils mCust = ((HwCustHwSpecialUtils) HwCustUtils.createObj(HwCustHwSpecialUtils.class, new Object[0]));
    private static boolean sAllowRsMms = Boolean.parseBoolean(SystemProperties.get("ro.config.hw_allow_rs_mms", "false"));
    private static boolean sIsChinaRegion = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));

    public static class HwDateUtils {
        private static Object CACHED_FORMATTERS;
        private static boolean isHwDateUtilsEx = true;
        private static boolean mUseHwMethod = false;
        private static Method methodFormatDateInterval;
        private static Method methodGetFormatter;
        private static Method methodToSkeleton;
        private static Calendar sCalendar = null;

        public static void init() {
            try {
                Class<?> DateIntervalFormatClazz = Class.forName("libcore.icu.DateIntervalFormat");
                Field sCachedFormater = DateIntervalFormatClazz.getDeclaredField("CACHED_FORMATTERS");
                sCachedFormater.setAccessible(true);
                CACHED_FORMATTERS = sCachedFormater.get(null);
                methodToSkeleton = DateIntervalFormatClazz.getDeclaredMethod("toSkeleton", new Class[]{Calendar.class, Calendar.class, Integer.TYPE});
                methodGetFormatter = DateIntervalFormatClazz.getDeclaredMethod("getFormatter", new Class[]{String.class, String.class, String.class});
                methodFormatDateInterval = DateIntervalFormatClazz.getDeclaredMethod("formatDateInterval", new Class[]{Long.TYPE, Long.TYPE, Long.TYPE});
                methodToSkeleton.setAccessible(true);
                methodGetFormatter.setAccessible(true);
                methodFormatDateInterval.setAccessible(true);
                mUseHwMethod = true;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
            } catch (NoSuchMethodException e3) {
                e3.printStackTrace();
            } catch (IllegalArgumentException e4) {
                e4.printStackTrace();
            } catch (IllegalAccessException e5) {
                e5.printStackTrace();
            } catch (SecurityException e6) {
                e6.printStackTrace();
            }
        }

        public static String formatChinaDateTime(Context context, long when, int flags) {
            if (!isHwDateUtilsEx) {
                return DateUtils.formatDateTime(context, when, flags);
            }
            String dateString;
            try {
                dateString = DateUtilsEx.formatChinaDateTime(context, when, flags);
            } catch (Exception e) {
                dateString = DateUtils.formatDateTime(context, when, flags);
                isHwDateUtilsEx = false;
            }
            return dateString;
        }

        public static String formatChinaDateRange(Context context, long start, long end, int flags) {
            if (!isHwDateUtilsEx) {
                return DateUtils.formatDateRange(context, start, end, flags);
            }
            String dateString;
            try {
                dateString = DateUtilsEx.formatChinaDateRange(context, start, end, flags);
            } catch (Exception e) {
                dateString = DateUtils.formatDateRange(context, start, end, flags);
                isHwDateUtilsEx = false;
            }
            return dateString;
        }

        public static String formatChinaDateRange(Context context, Formatter formatter, long start, long end, int flags, String tz) {
            if (!isHwDateUtilsEx) {
                return DateUtils.formatDateRange(context, formatter, start, end, flags, tz).toString();
            }
            String dateString;
            try {
                dateString = DateUtilsEx.formatChinaDateRange(context, formatter, start, end, flags, tz);
            } catch (Exception e) {
                dateString = DateUtils.formatDateRange(context, formatter, start, end, flags, tz).toString();
                isHwDateUtilsEx = false;
            }
            return dateString;
        }
    }

    public static boolean isAlwaysMms() {
        return sAllowRsMms;
    }

    public static boolean isAlwaysEnableMmsMobileLink(Context context) {
        boolean z = true;
        boolean z2 = false;
        if (!sAllowRsMms) {
            return false;
        }
        if (isChinaVersionInChinaRegion()) {
            if (!isNetworkRoaming()) {
                z2 = true;
            }
            return z2;
        }
        int allowMms = System.getInt(context.getContentResolver(), "enable_always_allow_mms", 0);
        int indexs = PreferenceManager.getDefaultSharedPreferences(context).getInt("alwaysAllowMms", MmsConfig.getDefaultAlwaysAllowMms());
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("HwSpecialUtils", "allowMms = " + allowMms);
            MLog.v("HwSpecialUtils", "indexs = " + indexs);
            MLog.v("HwSpecialUtils", "isNetworkRoaming = " + isNetworkRoaming());
        }
        if (indexs == 0) {
            if (allowMms != 1) {
                z = false;
            }
            return z;
        }
        if (allowMms == 1 && !isNetworkRoaming()) {
            z2 = true;
        }
        return z2;
    }

    public static boolean isAlwaysEnableMmsMobileLink(Context context, int sub) {
        boolean z = true;
        boolean z2 = false;
        if (!sAllowRsMms) {
            return false;
        }
        if (isChinaVersionInChinaRegion()) {
            if (!MessageUtils.isNetworkRoaming(sub)) {
                z2 = true;
            }
            return z2;
        }
        int allowMms = System.getInt(context.getContentResolver(), "enable_always_allow_mms", 0);
        int indexs = PreferenceManager.getDefaultSharedPreferences(context).getInt("alwaysAllowMms", MmsConfig.getDefaultAlwaysAllowMms());
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("HwSpecialUtils", "allowMms = " + allowMms);
            MLog.v("HwSpecialUtils", "indexs = " + indexs);
            MLog.v("HwSpecialUtils", "sub = " + sub);
            MLog.v("HwSpecialUtils", "isNetworkRoaming = " + MessageUtils.isNetworkRoaming(sub));
        }
        if (indexs == 0) {
            if (allowMms != 1) {
                z = false;
            }
            return z;
        }
        if (allowMms == 1 && !MessageUtils.isNetworkRoaming(sub)) {
            z2 = true;
        }
        return z2;
    }

    private static boolean isChinaVersionInChinaRegion() {
        String mcc = MccMncConfig.getDefault().getOperator();
        return (!sIsChinaRegion || mcc == null) ? false : mcc.startsWith("460");
    }

    public static boolean isChinaRegion() {
        return sIsChinaRegion;
    }

    public static boolean isNetworkRoaming() {
        boolean isRoaming = false;
        try {
            isRoaming = MmsApp.getDefaultTelephonyManager().isNetworkRoaming();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRoaming;
    }

    public static void dumpDataServiceSettings(StringBuilder sb) {
        Context context = MmsApp.getApplication();
        String yes = "yes";
        String no = "no";
        sb.append("InChina=").append(sIsChinaRegion ? "yes" : "no").append(";  AllowRsMms=").append(sAllowRsMms ? "yes" : "no").append("  AlwaysEnable=").append(isAlwaysEnableMmsMobileLink(context) ? "yes" : "no").append(";  airplane_on=").append(MessageUtils.isAirplanModeOn(context) ? "yes" : "no");
    }
}
