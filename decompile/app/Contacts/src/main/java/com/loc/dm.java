package com.loc;

import android.os.Looper;
import java.util.Timer;

/* compiled from: Unknown */
final class dm extends Thread {
    private /* synthetic */ dl a;

    dm(dl dlVar, String str) {
        this.a = dlVar;
        super(str);
    }

    public final void run() {
        try {
            synchronized (this.a.d) {
                Looper.prepare();
                this.a.H = Looper.myLooper();
                this.a.F = new Timer();
                this.a.A = new dn(this.a);
                dl.a(this.a, this.a.A);
                this.a.B = new do(this.a);
                try {
                    dl.a(this.a, this.a.B);
                } catch (Exception e) {
                }
            }
            if (this.a.e) {
                Looper.loop();
            }
        } catch (Exception e2) {
        }
    }
}
