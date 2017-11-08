package com.android.rcs.transaction;

import android.content.Context;
import android.content.Intent;
import com.android.rcs.RcsCommonConfig;
import com.huawei.rcs.utils.RcsFileStatusUpdateService;

public class RcsMmsSystemEventReceiver {
    private static final boolean mIsRcsOn = RcsCommonConfig.isRCSSwitchOn();

    public void fileStatusUpdateService(Context context) {
        if (mIsRcsOn) {
            context.startService(new Intent(context, RcsFileStatusUpdateService.class));
        }
    }
}
