package com.android.mms.transaction;

import android.content.Context;
import com.google.android.mms.pdu.NotificationInd;

public class HwCustPushReiver {
    public HwCustPushReiver(Context context) {
    }

    public void handleRcsStatusSent(long rcsThreadId, Context context) {
    }

    public boolean isRejectAnonymousMms(NotificationInd notificationInd) {
        return false;
    }
}
