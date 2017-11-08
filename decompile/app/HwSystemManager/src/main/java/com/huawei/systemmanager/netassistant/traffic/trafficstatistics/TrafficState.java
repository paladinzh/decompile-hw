package com.huawei.systemmanager.netassistant.traffic.trafficstatistics;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.leisuretraffic.LeisureTrafficSetting;
import com.huawei.systemmanager.util.HwLog;

public class TrafficState {
    private static final String TAG = "TafficState";
    public static final int TYPE_LEISURE_TRAFFIC = 302;
    public static final int TYPE_NORMAL_TRAFFIC = 301;
    public static final int TYPE_ROAMING_TRAFFIC = 303;
    public static final int TYPE_WIFI_TRAFFIC = 304;

    public static int getCurrentTrafficState(String imsi) {
        TelephonyManager telephonyManager = TelephonyManager.from(GlobalContext.getContext());
        SubscriptionManager sm = SubscriptionManager.from(GlobalContext.getContext());
        int slotIndex = SimCardManager.getInstance().getSimcardIndex(imsi);
        SubscriptionInfo subscriptionInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(slotIndex);
        if (subscriptionInfo == null) {
            HwLog.i(TAG, "slotIndex = " + slotIndex + " subscriptionInfo = null");
            return 301;
        }
        int defaultSubId = subscriptionInfo.getSubscriptionId();
        HwLog.i(TAG, "default sub id = " + defaultSubId + "slotIndex = " + slotIndex + " subscriptionId = " + subscriptionInfo.getSubscriptionId());
        if (telephonyManager.isNetworkRoaming(defaultSubId)) {
            return 303;
        }
        LeisureTrafficSetting ltSetting = new LeisureTrafficSetting(imsi);
        ltSetting.get();
        if (ltSetting.inLeisureTime()) {
            return 302;
        }
        return 301;
    }

    public static boolean isRoamingState(String imsi) {
        boolean z = false;
        if (TextUtils.isEmpty(imsi)) {
            return false;
        }
        if (303 == getCurrentTrafficState(imsi)) {
            z = true;
        }
        return z;
    }

    public static boolean isCurrentRoaming() {
        return isRoamingState(SimCardManager.getInstance().getPreferredDataSubscriberId());
    }
}
