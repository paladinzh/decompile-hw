package cn.com.xy.sms.util;

import java.util.Map;

/* compiled from: Unknown */
final class r extends Thread {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ long e;
    private final /* synthetic */ boolean f;
    private final /* synthetic */ boolean g;
    private final /* synthetic */ Map h;
    private final /* synthetic */ Map i;

    r(String str, String str2, String str3, String str4, long j, boolean z, boolean z2, Map map, Map map2) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
        this.e = j;
        this.f = z;
        this.g = z2;
        this.h = map;
        this.i = map2;
    }

    public final void run() {
        try {
            setName("xiaoyuan-parseSmsToBubble");
            ParseSmsToBubbleUtil.b(this.a, this.b, this.c, this.d, this.e, 3, this.f, this.g, this.h, this.i);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
