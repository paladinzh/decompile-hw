package com.android.keyguard;

import android.content.Context;

public class HwCustKeyguardViewMediator {
    public boolean isSupportPowerInstantLock(Context context) {
        return false;
    }

    public long getCustomLockafterTimeout(Context context, long timeout, int why) {
        return timeout;
    }

    public boolean orangeLockscreenInstalled(Context context) {
        return false;
    }
}
