package com.amap.api.services.core;

import android.os.HandlerThread;
import android.os.Message;
import com.amap.api.services.core.am.a;

/* compiled from: ManifestConfig */
class r extends HandlerThread {
    final /* synthetic */ q a;

    r(q qVar, String str) {
        this.a = qVar;
        super(str);
    }

    public void run() {
        String str = "run";
        Thread.currentThread().setName("ManifestConfigThread");
        Message message = new Message();
        try {
            a a = am.a(q.c, h.b(false), "common;exception");
            if (a != null) {
                if (a.d != null) {
                    message.obj = new s(a.d.b, a.d.a);
                }
            }
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
            i.a(th2, "ManifestConfig", "mVerfy");
        }
    }
}
