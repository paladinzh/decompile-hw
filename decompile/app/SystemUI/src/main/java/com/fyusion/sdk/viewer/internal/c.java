package com.fyusion.sdk.viewer.internal;

import android.util.Log;
import com.fyusion.sdk.common.j;
import com.fyusion.sdk.core.a.h;
import com.fyusion.sdk.viewer.internal.b.b.f.b;
import com.fyusion.sdk.viewer.internal.b.e;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: Unknown */
public class c implements com.fyusion.sdk.viewer.internal.b.b.a.a.a, com.fyusion.sdk.viewer.internal.b.c.a.c {
    private b a;
    private Map<String, com.fyusion.sdk.viewer.internal.b.c.a> b = new ConcurrentHashMap();

    /* compiled from: Unknown */
    public interface a {
        int a();

        h a(j jVar);

        void a(String str);
    }

    public c(b bVar) {
        this.a = bVar;
        this.a.a().a((com.fyusion.sdk.viewer.internal.b.b.a.a.a) this);
    }

    public com.fyusion.sdk.viewer.internal.b.c.a a(String str, InputStream inputStream, boolean z, com.fyusion.sdk.viewer.internal.b.c.b bVar) throws Exception {
        com.fyusion.sdk.viewer.internal.b.c.a a = bVar.a(inputStream, z, (com.fyusion.sdk.viewer.internal.b.c.a.c) this);
        if (a != null) {
            this.b.put(str, a);
        }
        return a;
    }

    public com.fyusion.sdk.viewer.internal.b.c.a a(String str, boolean z, com.fyusion.sdk.viewer.internal.b.c.b bVar) {
        com.fyusion.sdk.viewer.internal.b.c.a aVar = (com.fyusion.sdk.viewer.internal.b.c.a) this.b.get(str);
        if (aVar == null || aVar.j() == aVar.n().getHeight(z)) {
            return aVar;
        }
        aVar = bVar.a(aVar, z, (com.fyusion.sdk.viewer.internal.b.c.a.c) this);
        this.b.put(str, aVar);
        return aVar;
    }

    public void a(e eVar) {
        Log.d("FyuseDataStore", "schedule to remove files: " + eVar.d());
        this.a.a().b(eVar);
    }

    public void a(e eVar, final a aVar) {
        final com.fyusion.sdk.viewer.internal.b.c.a aVar2 = (com.fyusion.sdk.viewer.internal.b.c.a) this.b.get(eVar.d());
        if (aVar2 == null) {
            Log.w("FyuseDataStore", "Unexpected condition. FyuseData is not found for: " + eVar);
            aVar.a("Unexpected condition. FyuseData is not found for: " + eVar);
            return;
        }
        this.a.a().a(aVar2.o(), new com.fyusion.sdk.viewer.internal.b.b.a.a.c(this) {
            final /* synthetic */ c c;

            public boolean a(File file) {
                int a = aVar.a();
                j b = aVar2.b(file);
                if (b != null) {
                    if (!b.a()) {
                        b.a(com.fyusion.sdk.common.j.a.READ_WRITE, j.b.TRUNCATE);
                    }
                    h a2 = aVar.a(b);
                    if (a2 != null) {
                        aVar2.a(a, a2);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void b(e eVar) {
        if (eVar != null) {
            Log.d("FyuseDataStore", "File " + eVar.d() + " deleted");
            if (eVar instanceof com.fyusion.sdk.viewer.internal.b.c.a.a) {
                eVar = ((com.fyusion.sdk.viewer.internal.b.c.a.a) eVar).a();
            }
            String d = eVar.d();
            if (d != null) {
                try {
                    if (((com.fyusion.sdk.viewer.internal.b.c.a) this.b.get(d)) != null) {
                        j b = ((com.fyusion.sdk.viewer.internal.b.c.a) this.b.get(d)).b(null);
                        if (b != null) {
                            b.g();
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
