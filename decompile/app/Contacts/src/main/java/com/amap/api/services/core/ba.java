package com.amap.api.services.core;

import java.util.Date;
import java.util.List;

/* compiled from: CrashLogWriter */
class ba extends bg {
    private a a;

    /* compiled from: CrashLogWriter */
    class a implements cc {
        final /* synthetic */ ba a;
        private bq b;

        a(ba baVar, bq bqVar) {
            this.a = baVar;
            this.b = bqVar;
        }

        public void a(String str) {
            try {
                this.b.b(str, this.a.a());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    ba() {
    }

    protected int a() {
        return 0;
    }

    protected String a(String str) {
        return ap.b(str + bh.a(new Date().getTime()));
    }

    protected String b() {
        return bd.c;
    }

    protected cc a(bq bqVar) {
        try {
            if (this.a == null) {
                this.a = new a(this, bqVar);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return this.a;
    }

    protected String a(List<ar> list) {
        return null;
    }
}
