package com.huawei.systemmanager.sdk.tmsdk;

import android.content.Context;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Map;
import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;

public class TMSdkEngine {
    private static final String TAG = TMSdkEngine.class.getSimpleName();
    private static boolean mHasInit;

    public static void initTMSDK(Context context) {
        init(context);
    }

    private static void init(Context context) {
        TMSDKContext.setDualPhoneInfoFetcher(new IDualPhoneInfoFetcher() {
            public String getIMSI(int simIndex) {
                String imsi = "";
                if (simIndex == 0) {
                    return SimCardManager.getInstance().getSubscriberId(0);
                }
                if (simIndex == 1) {
                    return SimCardManager.getInstance().getSubscriberId(1);
                }
                return imsi;
            }
        });
        TMSDKContext.setAutoConnectionSwitch(false);
        TMSDKContext.setTMSDKLogEnable(false);
        TMSDKContext.init(context, TMSdkSecureService.class, new ITMSApplicaionConfig() {
            public HashMap<String, String> config(Map<String, String> src) {
                return new HashMap(src);
            }
        });
        String versionInfo = TMSDKContext.getSDKVersionInfo();
        mHasInit = true;
        HwLog.i(TAG, "TMSDK init success, version info = " + versionInfo);
    }

    public static void imsiChanged() {
        HwLog.i(TAG, "TMSDK imsiChanged. mHasInit = " + mHasInit);
        if (mHasInit) {
            try {
                TMSDKContext.onImsiChanged();
            } catch (Exception e) {
                HwLog.e(TAG, "TMSDK imsiChanged fail. e = " + e.getMessage());
            }
        }
    }
}
