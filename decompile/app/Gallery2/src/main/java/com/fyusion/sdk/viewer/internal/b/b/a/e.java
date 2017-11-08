package com.fyusion.sdk.viewer.internal.b.b.a;

import com.fyusion.sdk.viewer.internal.b.b.a.a.b;
import java.io.File;

/* compiled from: Unknown */
public class e implements b {
    private final int a;
    private final a b;

    /* compiled from: Unknown */
    public interface a {
        File a();
    }

    public e(a aVar, int i) {
        this.a = i;
        this.b = aVar;
    }

    public a a() {
        File a = this.b.a();
        if (a == null) {
            return null;
        }
        if (!a.mkdirs()) {
            if (!a.exists() || !a.isDirectory()) {
                return null;
            }
        }
        return f.a(a, this.a);
    }
}
