package com.fyusion.sdk.viewer;

import android.content.Context;
import android.util.Log;
import com.fyusion.sdk.common.a;
import com.fyusion.sdk.viewer.internal.b;
import com.fyusion.sdk.viewer.internal.b.b.a.h;
import com.fyusion.sdk.viewer.internal.b.b.a.j;
import com.fyusion.sdk.viewer.internal.b.b.a.l;
import com.fyusion.sdk.viewer.internal.b.b.f;
import com.fyusion.sdk.viewer.internal.c.d;
import com.fyusion.sdk.viewer.internal.f.e;
import com.fyusion.sdk.viewer.internal.request.c;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class FyuseViewer {
    private static volatile FyuseViewer a;
    private static f g;
    private final d b;
    private final b c;
    private final f d;
    private final List<RequestManager> e = new ArrayList();
    private Context f;

    protected FyuseViewer(Context context, f fVar, d dVar, c cVar) {
        this.f = context;
        this.b = dVar;
        this.d = new f();
        if (a.a().c("viewer", "remote")) {
            Log.w("FyuseSDK", "remote component is disabled");
        } else {
            this.d.a(String.class, com.fyusion.sdk.viewer.internal.b.c.a.class, new com.fyusion.sdk.viewer.internal.b.c.f.a(fVar.a()));
        }
        this.c = new b(context, this.d, cVar, fVar);
    }

    public static FyuseViewer get(Context context) {
        if (a == null) {
            synchronized (FyuseViewer.class) {
                if (a == null) {
                    Context applicationContext = context.getApplicationContext();
                    g = new f(new j(10), new l(context), new h(context), com.fyusion.sdk.viewer.internal.b.b.l.a(), com.fyusion.sdk.viewer.internal.b.b.l.b(), com.fyusion.sdk.viewer.internal.b.b.l.c(), com.fyusion.sdk.viewer.internal.b.b.l.d());
                    List<com.fyusion.sdk.viewer.internal.d.b> a = new com.fyusion.sdk.viewer.internal.d.a(applicationContext).a();
                    a = new FyuseViewer(applicationContext, g, new com.fyusion.sdk.viewer.internal.c.f(), new c());
                    for (com.fyusion.sdk.viewer.internal.d.b a2 : a) {
                        a2.a(applicationContext, a.d, g);
                    }
                }
            }
        }
        return a;
    }

    public static RequestManager with(Context context) {
        return com.fyusion.sdk.viewer.internal.c.l.a().a(context);
    }

    d a() {
        return this.b;
    }

    void a(RequestManager requestManager) {
        synchronized (this.e) {
            if (this.e.contains(requestManager)) {
                throw new IllegalStateException("Cannot register already registered manager");
            }
            this.e.add(requestManager);
        }
    }

    void a(Target<?> target) {
        synchronized (this.e) {
            for (RequestManager a : this.e) {
                if (a.a((Target) target)) {
                    return;
                }
            }
            throw new IllegalStateException("Failed to remove target from managers");
        }
    }

    b b() {
        return this.c;
    }

    void b(RequestManager requestManager) {
        synchronized (this.e) {
            if (this.e.contains(requestManager)) {
                this.e.remove(requestManager);
            } else {
                throw new IllegalStateException("Cannot register not yet registered manager");
            }
        }
    }

    public void clearMemory() {
        e.a();
        com.fyusion.sdk.core.a.e.a.a();
        com.fyusion.sdk.core.util.a.b.a.a();
        com.fyusion.sdk.core.util.a.c.a.a();
        com.fyusion.sdk.core.util.a.d.a.a();
    }

    public void registerModule(com.fyusion.sdk.viewer.internal.d.b bVar) {
        bVar.a(this.f.getApplicationContext(), a.d, g);
    }
}
