package cn.com.xy.sms.sdk.iccid;

/* compiled from: Unknown */
final class c implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;

    c(String str, String str2, String str3) {
        this.a = str;
        this.b = str2;
        this.c = str3;
    }

    public final void run() {
        try {
            IccidLocationUtil.b(this.a, this.b, this.c, false);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
