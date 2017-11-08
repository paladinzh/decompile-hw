package defpackage;

/* renamed from: af */
class af extends Thread {
    final /* synthetic */ ae aQ;

    af(ae aeVar, String str) {
        this.aQ = aeVar;
        super(str);
    }

    public void run() {
        try {
            k y = bx.y(this.aQ.context, this.aQ.p());
            if (y == null) {
                y = new k(this.aQ.context);
            }
            if (y.isValid()) {
                this.aQ.b(y);
            } else {
                aw.i("PushLog2841", "query trs error:" + this.aQ.getResult());
            }
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
        }
    }
}
