package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;

public class HwCustNotificationReceiver {
    public boolean isRcsSwitchOn() {
        return false;
    }

    public boolean sendOrSaveMessage(Context context, Intent intent, boolean send) {
        return false;
    }

    public boolean viewMessage(Context context, Intent intent) {
        return false;
    }
}
