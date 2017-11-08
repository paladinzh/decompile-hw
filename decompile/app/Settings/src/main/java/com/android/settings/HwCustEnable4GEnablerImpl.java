package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.telephony.HuaweiTelephonyManagerCustEx;

public class HwCustEnable4GEnablerImpl extends HwCustEnable4GEnabler {
    private static final boolean SKYTONE_SWITCH = SystemProperties.getBoolean("ro.radio.vsim_support", false);
    private static final String TAG = "Enable4GEnabler";

    public HwCustEnable4GEnablerImpl(Context context) {
        super(context);
    }

    public boolean isVSimEnabled() {
        if (!SKYTONE_SWITCH || HuaweiTelephonyManagerCustEx.getVSimSubId() == -1) {
            return false;
        }
        return true;
    }
}
