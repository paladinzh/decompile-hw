package com.avast.android.sdk.shield.fileshield;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/* compiled from: Unknown */
class a extends BroadcastReceiver {
    final /* synthetic */ FileShieldService a;

    a(FileShieldService fileShieldService) {
        this.a = fileShieldService;
    }

    public void onReceive(Context context, Intent intent) {
        this.a.j.a(intent);
    }
}
