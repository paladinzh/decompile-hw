package defpackage;

import android.content.Intent;

/* renamed from: f */
final class f implements Runnable {
    private Intent mIntent;
    final /* synthetic */ d o;
    private o p;

    private f(d dVar, o oVar, Intent intent) {
        this.o = dVar;
        this.p = oVar;
        this.mIntent = intent;
    }

    public void run() {
        try {
            this.p.onReceive(this.o.mContext, this.mIntent);
        } catch (Throwable e) {
            aw.d("PushLog2841", "ReceiverDispatcher: call Receiver:" + this.p.getClass().getSimpleName() + ", intent:" + this.mIntent + " failed:" + e.toString(), e);
        }
    }
}
