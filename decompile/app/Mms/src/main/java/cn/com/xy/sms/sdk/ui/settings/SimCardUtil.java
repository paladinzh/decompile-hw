package cn.com.xy.sms.sdk.ui.settings;

import android.content.Context;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import com.android.mms.ui.MessageUtils;
import java.util.HashMap;
import java.util.Map.Entry;

public class SimCardUtil {
    public static final int FIRST_SIM_INDEX = 0;
    public static final int SECOND_SIM_INDEX = 1;

    public static boolean needUpdateAllIccidInfo(Context ctx) {
        HashMap<String, String[]> iccidAreaCodeMap = IccidLocationUtil.getIccidAreaCodeMap();
        if (iccidAreaCodeMap == null || !SmartSmsSdkUtil.getEnhance(ctx) || !NetUtil.checkAccessNetWork(2) || iccidAreaCodeMap.size() < 2) {
            return false;
        }
        boolean needUpdate = false;
        for (Entry<String, String[]> entry : iccidAreaCodeMap.entrySet()) {
            if (StringUtils.isNull(IccidLocationUtil.getIccidAreaCodeMapValueByIndex((String) entry.getKey(), 0)) && StringUtils.isNull(IccidLocationUtil.getIccidAreaCodeMapValueByIndex((String) entry.getKey(), 3))) {
                needUpdate = true;
                break;
            }
        }
        return needUpdate;
    }

    public static boolean isChangeSimCard() {
        try {
            IccidLocationUtil.changeIccidAreaCode(false);
            String iccid1 = DuoquUtils.getSdkDoAction().getIccidBySimIndex(0);
            String iccid2 = DuoquUtils.getSdkDoAction().getIccidBySimIndex(1);
            int simCardCount = 0;
            if (!StringUtils.isNull(iccid1)) {
                simCardCount = 1;
            }
            if (!StringUtils.isNull(iccid2)) {
                simCardCount++;
            }
            if (simCardCount != IccidLocationUtil.getIccidAreaCodeMap().size()) {
                return true;
            }
            if (StringUtils.isNull(iccid1) || hasIccidInCache(iccid1)) {
                return (StringUtils.isNull(iccid2) || hasIccidInCache(iccid2)) ? false : true;
            } else {
                return true;
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SimCardUtil isChangeSimCard " + e.getMessage(), e);
        }
    }

    public static boolean hasIccidInCache(String compareIccid) {
        HashMap<String, String[]> iccidAreaCodeMap = IccidLocationUtil.getIccidAreaCodeMap();
        if (iccidAreaCodeMap == null) {
            return false;
        }
        for (Entry<String, String[]> entry : iccidAreaCodeMap.entrySet()) {
            if (((String) entry.getKey()).equals(compareIccid)) {
                return true;
            }
        }
        return false;
    }

    public static int getIndexbyOperator(int numOprator, int defaultIndex) {
        HashMap<String, String[]> iccidAreaCodeMap = IccidLocationUtil.getIccidAreaCodeMap();
        if (iccidAreaCodeMap == null || iccidAreaCodeMap.isEmpty()) {
            return defaultIndex;
        }
        for (Entry<String, String[]> entry : iccidAreaCodeMap.entrySet()) {
            if (numOprator == Integer.parseInt(((String[]) entry.getValue())[2])) {
                return getCurrentSimIndex((String) entry.getKey());
            }
        }
        return defaultIndex;
    }

    private static int getCurrentSimIndex(String iccid) {
        String secondIccid = DuoquUtils.getSdkDoAction().getIccidBySimIndex(1);
        if (TextUtils.isEmpty(secondIccid) || !secondIccid.equals(iccid)) {
            return 0;
        }
        return 1;
    }

    public static void loadLocation() {
        try {
            HashMap<String, String[]> iccidMap = IccidLocationUtil.getIccidAreaCodeMap();
            if (iccidMap != null) {
                for (Entry<String, String[]> entry : iccidMap.entrySet()) {
                    ParseManager.loadLocation((String) entry.getKey(), -1, null, false);
                }
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("SimCardUtil loadLocation " + e.getMessage(), e);
        }
    }

    public static int getDefaultSimCardIndex() {
        return (1 != MessageUtils.getIccCardStatus(0) && 1 == MessageUtils.getIccCardStatus(1)) ? 1 : 0;
    }
}
