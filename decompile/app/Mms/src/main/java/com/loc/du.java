package com.loc;

import android.os.Looper;

/* compiled from: Unknown */
final class du extends Thread {
    final /* synthetic */ db a;

    du(db dbVar, String str) {
        this.a = dbVar;
        super(str);
    }

    public final void run() {
        try {
            synchronized (this.a.c) {
                if (db.a) {
                    Looper.prepare();
                    this.a.z = Looper.myLooper();
                    try {
                        this.a.A = new dw(this.a);
                        this.a.q.addGpsStatusListener(this.a.A);
                        this.a.q.addNmeaListener(this.a.A);
                    } catch (Throwable th) {
                    }
                    this.a.B = new dv(this);
                    try {
                        this.a.q.requestLocationUpdates("passive", 1000, (float) db.d, this.a.D);
                    } catch (Throwable th2) {
                    }
                }
            }
            if (db.a) {
                Looper.loop();
            }
        } catch (Throwable th3) {
        }
    }
}
