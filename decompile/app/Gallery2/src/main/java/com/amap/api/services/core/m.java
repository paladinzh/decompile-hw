package com.amap.api.services.core;

import android.os.HandlerThread;
import android.os.Message;

/* compiled from: ManifestConfig */
class m extends HandlerThread {
    final /* synthetic */ l a;

    m(l lVar, String str) {
        this.a = lVar;
        super(str);
    }

    public void run() {
        String str = "run";
        Thread.currentThread().setName("ManifestConfigThread");
        Message message = new Message();
        try {
            message.obj = new n(l.c).a();
            message.what = 3;
            if (this.a.d != null) {
                this.a.d.sendMessage(message);
            }
        } catch (Throwable th) {
            message.what = 3;
            if (this.a.d != null) {
                this.a.d.sendMessage(message);
            }
        }
        try {
            sleep(10000);
        } catch (Throwable th2) {
            d.a(th2, "ManifestConfig", "mVerfy");
        }
    }
}
