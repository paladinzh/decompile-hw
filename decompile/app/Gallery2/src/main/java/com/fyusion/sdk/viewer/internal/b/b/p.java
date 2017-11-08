package com.fyusion.sdk.viewer.internal.b.b;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import com.fyusion.sdk.viewer.internal.f.e;

/* compiled from: Unknown */
class p {
    private boolean a;
    private final Handler b = new Handler(Looper.getMainLooper(), new a());

    /* compiled from: Unknown */
    private static class a implements Callback {
        private a() {
        }

        public boolean handleMessage(Message message) {
            if (message.what != 1) {
                return false;
            }
            ((o) message.obj).c();
            return true;
        }
    }

    p() {
    }

    public void a(o<?> oVar) {
        e.a();
        if (this.a) {
            this.b.obtainMessage(1, oVar).sendToTarget();
            return;
        }
        this.a = true;
        oVar.c();
        this.a = false;
    }
}
