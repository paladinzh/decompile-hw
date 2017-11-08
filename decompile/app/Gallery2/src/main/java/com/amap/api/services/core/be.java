package com.amap.api.services.core;

import java.util.List;

/* compiled from: ExceptionLogWriter */
class be extends bi {
    private a a;

    /* compiled from: ExceptionLogWriter */
    class a implements bn {
        final /* synthetic */ be a;
        private ak b;

        a(be beVar, ak akVar) {
            this.a = beVar;
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

    be() {
    }

    protected int a() {
        return 1;
    }

    protected String a(String str) {
        return ab.b(str);
    }

    protected String b() {
        return bf.b;
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
