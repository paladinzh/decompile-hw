package com.huawei.openalliance.ad.utils.b;

import android.text.TextUtils;
import com.huawei.openalliance.ad.utils.b.g.a;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class e {
    private static Map<String, e> a = new HashMap();
    private static String b = "log.log";
    private static f c = f.INFO;
    private a d = null;
    private String e;
    private final i f = new i();

    private e() {
    }

    private void a(String str, String str2, f fVar, String str3, Throwable th) {
        g a = new a(str, fVar).a(this.e).a(this.f.a()).a();
        if (!TextUtils.isEmpty(str3)) {
            a.a((Object) "[").a((Object) str3).a((Object) "]");
        }
        a.a((Object) str2);
        if (th != null) {
            a.b(th);
        }
        a.a(this.d);
    }

    public void a(String str, String str2) {
        a(str, str2, f.DEBUG, null, null);
    }

    public void b(String str, String str2) {
        a(str, str2, f.INFO, null, null);
    }

    public void c(String str, String str2) {
        a(str, str2, f.WARN, null, null);
    }

    public boolean c(f fVar) {
        return this.d.a(fVar);
    }

    public void d(String str, String str2) {
        a(str, str2, f.ERROR, null, null);
    }
}
