package cn.com.xy.sms.sdk.service.a;

import android.content.Context;
import java.util.Map;
import java.util.concurrent.Callable;

/* compiled from: Unknown */
public final class a implements Callable<Map<String, Object>> {
    Context a;
    String b;
    String c;
    String d;
    long e;
    Map<String, String> f;
    Thread g = null;

    private Map<String, Object> c() {
        cn.com.xy.sms.sdk.a.a.a("xy_baseparse_1", 10);
        this.g = Thread.currentThread();
        return b.a(this.a, this.b, this.c, this.d, this.e, this.f);
    }

    public final void a() {
        this.a = null;
        this.b = null;
        this.c = null;
        this.d = null;
        this.e = 0;
        this.f = null;
        this.g = null;
    }

    public final void a(Context context, String str, String str2, String str3, long j, Map<String, String> map) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = j;
        this.f = map;
    }

    public final void b() {
        try {
            if (this.g != null) {
                this.g.stop();
            }
        } catch (Throwable th) {
        }
    }

    public final /* synthetic */ Object call() {
        cn.com.xy.sms.sdk.a.a.a("xy_baseparse_1", 10);
        this.g = Thread.currentThread();
        return b.a(this.a, this.b, this.c, this.d, this.e, this.f);
    }
}
