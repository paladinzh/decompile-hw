package com.fyusion.sdk.viewer.internal.b.b.a;

import android.support.v4.util.Pools$Pool;
import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.a.b;
import com.fyusion.sdk.viewer.internal.f.c;
import java.security.MessageDigest;

/* compiled from: Unknown */
public class m {
    private final c<e, String> a = new c(1000);
    private final Pools$Pool<a> b = com.fyusion.sdk.viewer.internal.f.a.a.b(10, new com.fyusion.sdk.viewer.internal.f.a.a.a<a>(this) {
        final /* synthetic */ m a;

        {
            this.a = r1;
        }

        public a a() {
            try {
                return new a(MessageDigest.getInstance("MD5"));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        public /* synthetic */ Object b() {
            return a();
        }
    });

    /* compiled from: Unknown */
    private static final class a implements com.fyusion.sdk.viewer.internal.f.a.a.c {
        private final MessageDigest a;
        private final b b = b.a();

        a(MessageDigest messageDigest) {
            this.a = messageDigest;
        }

        public b k() {
            return this.b;
        }
    }

    private String b(e eVar) {
        a aVar = (a) this.b.acquire();
        try {
            eVar.a(aVar.a);
            String a = com.fyusion.sdk.viewer.internal.f.e.a(aVar.a.digest());
            return a;
        } finally {
            this.b.release(aVar);
        }
    }

    public String a(e eVar) {
        String str;
        synchronized (this.a) {
            str = (String) this.a.b(eVar);
        }
        if (str == null) {
            str = eVar.d() + "-" + b(eVar);
        }
        synchronized (this.a) {
            this.a.b(eVar, str);
        }
        return str;
    }
}
