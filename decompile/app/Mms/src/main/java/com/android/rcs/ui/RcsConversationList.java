package com.android.rcs.ui;

import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsProfile;

public class RcsConversationList {
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();

    public void onCreate() {
        if (this.isRcsEnable) {
            MLog.d("RcsConversationList", "onCreate");
            RcsProfile.startService();
        }
    }

    public void onNewIntent() {
        if (this.isRcsEnable) {
            MLog.d("RcsConversationList", "onNewIntent");
            RcsProfile.startService();
        }
    }
}
