package com.fyusion.sdk.viewer;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import com.fyusion.sdk.viewer.internal.c.c;
import com.fyusion.sdk.viewer.internal.c.d;
import com.fyusion.sdk.viewer.internal.c.h;
import com.fyusion.sdk.viewer.internal.c.i;
import com.fyusion.sdk.viewer.internal.c.m;
import com.fyusion.sdk.viewer.internal.c.n;
import com.fyusion.sdk.viewer.internal.c.p;
import com.fyusion.sdk.viewer.internal.f.e;
import com.fyusion.sdk.viewer.internal.request.b;
import com.fyusion.sdk.viewer.internal.request.target.Target;

/* compiled from: Unknown */
public class RequestManager implements i {
    private final FyuseViewer a;
    private final h b;
    private final m c;
    private final n d;
    private final p e;
    private final Runnable f;
    private final Handler g;
    private final c h;

    /* compiled from: Unknown */
    private static class a implements com.fyusion.sdk.viewer.internal.c.c.a {
        private final n a;

        public a(n nVar) {
            this.a = nVar;
        }

        public void a(boolean z) {
            if (!z) {
            }
        }
    }

    public RequestManager(FyuseViewer fyuseViewer, h hVar, m mVar) {
        this(fyuseViewer, hVar, mVar, new n(), fyuseViewer.a());
    }

    RequestManager(FyuseViewer fyuseViewer, h hVar, m mVar, n nVar, d dVar) {
        this.e = new p();
        this.f = new Runnable(this) {
            final /* synthetic */ RequestManager a;

            {
                this.a = r1;
            }

            public void run() {
                this.a.b.a(this.a);
            }
        };
        this.g = new Handler(Looper.getMainLooper());
        this.a = fyuseViewer;
        this.b = hVar;
        this.c = mVar;
        this.d = nVar;
        this.h = dVar.a(fyuseViewer.b().getBaseContext(), new a(nVar));
        if (e.c()) {
            this.g.post(this.f);
        } else {
            hVar.a(this);
        }
        hVar.a(this.h);
        fyuseViewer.a(this);
    }

    private void b(Target<?> target) {
        if (!a((Target) target)) {
            this.a.a((Target) target);
        }
    }

    void a(Target<?> target, b bVar) {
        this.e.a((Target) target);
        this.d.a(bVar);
    }

    boolean a(Target<?> target) {
        b request = target.getRequest();
        if (request == null) {
            return true;
        }
        if (!this.d.b(request)) {
            return false;
        }
        this.e.b(target);
        this.e.a(target.getWrappedObject());
        target.setRequest(null);
        return true;
    }

    public void clear(@Nullable final Target<?> target) {
        if (target != null) {
            if (e.b()) {
                b(target);
            } else {
                this.g.post(new Runnable(this) {
                    final /* synthetic */ RequestManager b;

                    public void run() {
                        this.b.clear(target);
                    }
                });
            }
        }
    }

    public RequestBuilder load(Object obj) {
        return new RequestBuilder(this.a.b(), this).load(obj);
    }

    public void onDestroy() {
        this.e.onDestroy();
        for (Target clear : this.e.a()) {
            clear(clear);
        }
        this.e.b();
        this.d.d();
        this.b.b(this);
        this.b.b(this.h);
        this.g.removeCallbacks(this.f);
        this.a.b(this);
    }

    public void onLowMemory() {
    }

    public void onStart() {
        resumeRequests();
        this.e.onStart();
    }

    public void onStop() {
        pauseRequests();
        this.e.onStop();
    }

    public void onTrimMemory(int i) {
        this.a.b().a(i);
    }

    public void pauseRequests() {
        e.a();
        this.d.b();
    }

    public void resumeRequests() {
        e.a();
        this.d.c();
    }
}
