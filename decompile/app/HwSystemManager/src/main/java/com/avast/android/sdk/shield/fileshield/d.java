package com.avast.android.sdk.shield.fileshield;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/* compiled from: Unknown */
class d extends Handler {
    final /* synthetic */ FileShieldService a;
    final /* synthetic */ a b;

    d(a aVar, Looper looper, FileShieldService fileShieldService) {
        this.b = aVar;
        this.a = fileShieldService;
        super(looper);
    }

    public void handleMessage(Message message) {
        this.b.b.release();
    }
}
