package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;

public class HwCustImsPhoneCallTrackerImpl extends HwCustImsPhoneCallTracker {
    private static final String BOARD_PLATFORM_TAG = "ro.board.platform";
    private static final boolean IS_VDF = SystemProperties.get("ro.config.hw_opta", "0").equals("02");
    private static final String PLATFORM_QUALCOMM = "msm";
    private Context mContext;
    private TelephonyManager mTelephonyManager;

    public HwCustImsPhoneCallTrackerImpl(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean checkImsRegistered() {
        if (IS_VDF && isQcomPlatform() && this.mContext != null) {
            this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
            if (this.mTelephonyManager != null) {
                return this.mTelephonyManager.isImsRegistered();
            }
        }
        return true;
    }

    private boolean isQcomPlatform() {
        return SystemProperties.get(BOARD_PLATFORM_TAG, "").startsWith(PLATFORM_QUALCOMM);
    }
}
