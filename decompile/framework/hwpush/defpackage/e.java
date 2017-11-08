package defpackage;

import android.os.MessageQueue.IdleHandler;

/* renamed from: e */
class e implements IdleHandler {
    final /* synthetic */ d o;

    e(d dVar) {
        this.o = dVar;
    }

    public boolean queueIdle() {
        this.o.h();
        return true;
    }
}
