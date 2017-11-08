package com.fyusion.sdk.viewer.ext.localfyuse;

import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.ext.localfyuse.a.a.b;
import com.fyusion.sdk.viewer.internal.b.a.a;
import java.io.File;

/* compiled from: Unknown */
public class e implements a<com.fyusion.sdk.viewer.internal.b.c.a> {
    private b a;
    private File b;

    public e(b bVar, File file) {
        this.a = bVar;
        this.b = file;
    }

    public void a() {
    }

    public void a(d dVar, a.a<? super com.fyusion.sdk.viewer.internal.b.c.a> aVar) {
        try {
            aVar.a(this.a.a(this.b));
        } catch (Exception e) {
            aVar.a(e);
        }
    }

    public void b() {
    }

    public com.fyusion.sdk.viewer.internal.b.a c() {
        return com.fyusion.sdk.viewer.internal.b.a.LOCAL;
    }
}
