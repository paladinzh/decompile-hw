package com.amap.api.services.core;

import java.util.List;

/* compiled from: ExceptionLogWriter */
class bc extends bg {
    private a a;

    /* compiled from: ExceptionLogWriter */
    class a implements cc {
        final /* synthetic */ bc a;
        private bq b;

        a(bc bcVar, bq bqVar) {
            this.a = bcVar;
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

    bc() {
    }

    protected int a() {
        return 1;
    }

    protected String a(String str) {
        return ap.b(str);
    }

    protected String b() {
        return bd.b;
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
