package com.amap.api.services.core;

import java.util.Date;
import java.util.List;

/* compiled from: CrashLogWriter */
class bc extends bi {
    private a a;

    /* compiled from: CrashLogWriter */
    class a implements bn {
        final /* synthetic */ bc a;
        private ak b;

        a(bc bcVar, ak akVar) {
            this.a = bcVar;
            this.b = akVar;
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
        return 0;
    }

    protected String a(String str) {
        return ab.b(str + bj.a(new Date().getTime()));
    }

    protected String b() {
        return bf.c;
    }

    protected bn a(ak akVar) {
        try {
            if (this.a == null) {
                this.a = new a(this, akVar);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return this.a;
    }

    protected String a(List<ad> list) {
        return null;
    }
}
