package com.avast.android.sdk.engine.obfuscated;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/* compiled from: Unknown */
class bb extends Handler {
    final /* synthetic */ ba a;

    bb(ba baVar, Looper looper) {
        this.a = baVar;
        super(looper);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                this.a.a();
                return;
            default:
                super.handleMessage(message);
                return;
        }
    }
}
