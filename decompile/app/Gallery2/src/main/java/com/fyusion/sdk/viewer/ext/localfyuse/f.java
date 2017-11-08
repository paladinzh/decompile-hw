package com.fyusion.sdk.viewer.ext.localfyuse;

import com.fyusion.sdk.viewer.ext.localfyuse.a.a.b;
import com.fyusion.sdk.viewer.internal.b.c.g;
import com.fyusion.sdk.viewer.internal.b.c.h;
import com.fyusion.sdk.viewer.internal.b.c.j;
import com.fyusion.sdk.viewer.internal.b.c.k;
import java.io.File;

/* compiled from: Unknown */
public class f implements g<File, com.fyusion.sdk.viewer.internal.b.c.a> {
    private b a;

    /* compiled from: Unknown */
    public static class a implements h<File, com.fyusion.sdk.viewer.internal.b.c.a> {
        private com.fyusion.sdk.common.ext.b a;

        public a(com.fyusion.sdk.common.ext.b bVar) {
            this.a = bVar;
        }

        public g<File, com.fyusion.sdk.viewer.internal.b.c.a> a(j jVar) {
            return new f(new b(this.a));
        }
    }

    public f(b bVar) {
        this.a = bVar;
    }

    public com.fyusion.sdk.viewer.internal.b.c.g.a<com.fyusion.sdk.viewer.internal.b.c.a> a(File file, boolean z) {
        return new com.fyusion.sdk.viewer.internal.b.c.g.a(new k(file), new e(this.a, file));
    }

    public boolean a(File file) {
        return file.exists();
    }
}
