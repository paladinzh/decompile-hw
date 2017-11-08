package com.avast.android.sdk.shield.fileshield;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* compiled from: Unknown */
class b extends BroadcastReceiver {
    final /* synthetic */ FileShieldService a;

    b(FileShieldService fileShieldService) {
        this.a = fileShieldService;
    }

    public void onReceive(Context context, Intent intent) {
        if (FileShieldService.INTENT_ACTION_SD_CARD_SCAN_STARTED.equals(intent.getAction())) {
            this.a.p = true;
        }
        if (FileShieldService.INTENT_ACTION_SD_CARD_SCAN_STOPPED.equals(intent.getAction())) {
            this.a.p = false;
        }
    }
}
