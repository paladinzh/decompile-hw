package com.a.a;

import android.os.Handler;
import java.util.concurrent.Executor;

/* compiled from: Unknown */
public class e implements o {
    private final Executor a;

    /* compiled from: Unknown */
    private class a implements Runnable {
        final /* synthetic */ e a;
        private final l b;
        private final n c;
        private final Runnable d;

        public a(e eVar, l lVar, n nVar, Runnable runnable) {
            this.a = eVar;
            this.b = lVar;
            this.c = nVar;
            this.d = runnable;
        }

        public void run() {
            if (this.b.g()) {
                this.b.b("canceled-at-delivery");
                return;
            }
            if (this.c.a()) {
                this.b.a(this.c.a);
            } else {
                this.b.b(this.c.c);
            }
            if (this.c.d) {
                this.b.a("intermediate-response");
            } else {
                this.b.b("done");
            }
            if (this.d != null) {
                this.d.run();
            }
        }
    }

    public e(final Handler handler) {
        this.a = new Executor(this) {
            final /* synthetic */ e b;

            public void execute(Runnable runnable) {
                handler.post(runnable);
            }
        };
    }

    public void a(l<?> lVar, n<?> nVar) {
        a(lVar, nVar, null);
    }

    public void a(l<?> lVar, n<?> nVar, Runnable runnable) {
        lVar.u();
        lVar.a("post-response");
        this.a.execute(new a(this, lVar, nVar, runnable));
    }

    public void a(l<?> lVar, s sVar) {
        lVar.a("post-error");
        this.a.execute(new a(this, lVar, n.a(sVar), null));
    }
}
