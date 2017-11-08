package com.huawei.systemmanager.power.data.xml;

import android.content.Context;
import com.google.android.collect.Maps;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;

public class PowerWarningParam {
    private static int APP_SHOW_LEVEL_DFT = 1;
    private static final String APP_SHOW_LEVEL_KEY = "app_show_level";
    private static int AVERAGE_CURRENT_FG_DFT = 350;
    private static final String AVERAGE_CURRENT_FG_KEY = "average_current_fg";
    private static int COUNT_FREQUENCY_DFT = 60;
    private static final String COUNT_FREQUENCY_K = "count_frequency";
    private static int DANGEROUS_CURRENT_LEVEL_DFT = 30;
    private static final String DANGEROUS_CURRENT_LEVEL_KEY = "dangerous_current_level";
    private static int DANGEROUS_LEVEL_STANDARD_DFT = 25;
    private static final String DANGEROUS_LEVEL_STANDARD_KEY = "dangerous_level_standard";
    private static int HIGH_LEVEL_FOR_FREAQPP_STANDARD_DFT = 100;
    private static final String HIGH_LEVEL_FOR_FREQAPP_STANDARD_KEY = "high_level_for_freqapp_standard";
    private static int HIGH_LEVEL_STANDARD_DFT = 10;
    private static final String HIGH_LEVEL_STANDARD_KEY = "high_level_standard";
    private static int MIN_INTERVAL_TIME_DFT = 20;
    private static final String MIN_INTERVAL_TIME_KEY = "min_interval_time";
    private static int MSG_SEND_INTERVAL_DFT = 240;
    private static final String MSG_SEND_INTERVAL_KEY = "msg_send_interval";
    private static final String TAG = PowerWarningParam.class.getSimpleName();
    private static Map<String, String> mParamMap = Maps.newHashMap();

    public static int getApp_show_level(Context ctx) {
        return getValue(ctx, APP_SHOW_LEVEL_KEY, APP_SHOW_LEVEL_DFT);
    }

    public static int getAverage_current_fg(Context ctx) {
        return getValue(ctx, AVERAGE_CURRENT_FG_KEY, AVERAGE_CURRENT_FG_DFT);
    }

    public static int getCount_frequency(Context ctx) {
        return getValue(ctx, COUNT_FREQUENCY_K, COUNT_FREQUENCY_DFT);
    }

    public static int getDangerous_current_level(Context ctx) {
        return getValue(ctx, DANGEROUS_CURRENT_LEVEL_KEY, DANGEROUS_CURRENT_LEVEL_DFT);
    }

    public static int getDangerous_level_standard(Context ctx) {
        return getValue(ctx, DANGEROUS_LEVEL_STANDARD_KEY, DANGEROUS_LEVEL_STANDARD_DFT);
    }

    public static int getHigh_level_standard(Context ctx) {
        return getValue(ctx, HIGH_LEVEL_STANDARD_KEY, HIGH_LEVEL_STANDARD_DFT);
    }

    public static int getHigh_level_for_freqapp_standard(Context ctx) {
        return getValue(ctx, HIGH_LEVEL_FOR_FREQAPP_STANDARD_KEY, HIGH_LEVEL_FOR_FREAQPP_STANDARD_DFT);
    }

    public static int getMin_interval_time(Context ctx) {
        return getValue(ctx, MIN_INTERVAL_TIME_KEY, MIN_INTERVAL_TIME_DFT);
    }

    public static int getMsgSendInterval(Context ctx) {
        return getValue(ctx, MSG_SEND_INTERVAL_KEY, MSG_SEND_INTERVAL_DFT);
    }

    private static synchronized int getValue(Context ctx, String key, int dftValue) {
        synchronized (PowerWarningParam.class) {
            if (mParamMap.isEmpty()) {
                mParamMap = parsePowerWarningParameter(ctx);
            }
            if (mParamMap.containsKey(key)) {
                try {
                    int parseInt = Integer.parseInt((String) mParamMap.get(key));
                    return parseInt;
                } catch (RuntimeException e) {
                    HwLog.w(TAG, "getValue value RuntimeException", e);
                }
            }
            HwLog.w(TAG, "getValue value not exist in XML: " + key);
            return dftValue;
        }
    }

    private static Map<String, String> parsePowerWarningParameter(Context context) {
        Context context2 = context;
        Map<String, String> result = XmlParsers.xmlAttrsToMap(context2, "/data/cust/xml/hw_powersaving_powerwarning_parameter.xml", (int) R.xml.powerwarning_parameter, XmlParsers.getTagAttrMatchPredicate2("Parameter", "name", DBHelper.VALUE), XmlParsers.getRowToAttrValueFunc("name"), XmlParsers.getRowToAttrValueFunc(DBHelper.VALUE));
        HwLog.d(TAG, "parsePowerWarningParameter result:" + result);
        return result;
    }
}
