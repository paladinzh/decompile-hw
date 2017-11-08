package com.huawei.systemmanager.customize;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Map;

public class OptChecker {
    private static final String CONFIG_ENABLED = "true";
    private static final String TAG = "OptChecker";
    private static final Map<Integer, Integer> mCode2Res = new HashMap();

    private void initCode2ResMap() {
        mCode2Res.put(Integer.valueOf(5), Integer.valueOf(R.string.ad_detect_enabled));
        mCode2Res.put(Integer.valueOf(4), Integer.valueOf(R.string.anti_virus_enabled));
        mCode2Res.put(Integer.valueOf(2), Integer.valueOf(R.string.cloud_message_filter_enabled));
        mCode2Res.put(Integer.valueOf(1), Integer.valueOf(R.string.hold_dialog_enabled));
        mCode2Res.put(Integer.valueOf(6), Integer.valueOf(R.string.region_type));
        mCode2Res.put(Integer.valueOf(7), Integer.valueOf(R.string.cloud_dianxin_db_enabled));
        mCode2Res.put(Integer.valueOf(8), Integer.valueOf(R.string.net_access_dialog));
        mCode2Res.put(Integer.valueOf(20), Integer.valueOf(R.string.huawei_cloud));
        mCode2Res.put(Integer.valueOf(9), Integer.valueOf(R.string.phone_num_location));
        mCode2Res.put(Integer.valueOf(23), Integer.valueOf(R.string.hsm_stat_enable));
        mCode2Res.put(Integer.valueOf(30), Integer.valueOf(R.string.net_assistant_enable));
        mCode2Res.put(Integer.valueOf(3), Integer.valueOf(R.string.hsm_cust_permission));
        mCode2Res.put(Integer.valueOf(40), Integer.valueOf(R.string.trash_tms_support));
    }

    public OptChecker(Context context) {
        initCode2ResMap();
    }

    public boolean isEnableByOpt(Context ctx, int featureCode) {
        if (ctx == null) {
            HwLog.e(TAG, "isEnableByOpt called, but ctx is null! featureCode:" + featureCode);
            return false;
        } else if (!mCode2Res.containsKey(Integer.valueOf(featureCode))) {
            return true;
        } else {
            String str = "";
            try {
                return "true".equals(ctx.getString(((Integer) mCode2Res.get(Integer.valueOf(featureCode))).intValue()));
            } catch (NotFoundException e) {
                HwLog.e(TAG, "isEnableByOpt NotFoundException", e);
                return false;
            } catch (Exception e2) {
                HwLog.e(TAG, "isEnableByOpt Exception", e2);
                return false;
            }
        }
    }

    public int getInt(Context ctx, int featureCode, int defaultValue) {
        String value = getString(ctx, featureCode);
        if (value == null) {
            return defaultValue;
        }
        int res = defaultValue;
        try {
            res = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return res;
    }

    public String getString(Context ctx, int featureCode) {
        if (!mCode2Res.containsKey(Integer.valueOf(featureCode))) {
            return null;
        }
        int resId = ((Integer) mCode2Res.get(Integer.valueOf(featureCode))).intValue();
        if (ctx == null) {
            HwLog.e(TAG, "getString called, but ctx is null! featureCode:" + featureCode);
            return "";
        }
        String custValue = "";
        try {
            custValue = ctx.getString(resId);
        } catch (NotFoundException e) {
            HwLog.e(TAG, "String resource not found");
            e.printStackTrace();
        }
        return custValue;
    }
}
