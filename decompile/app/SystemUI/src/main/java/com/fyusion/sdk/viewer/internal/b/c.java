package com.fyusion.sdk.viewer.internal.b;

import android.media.MediaFormat;
import android.util.Log;
import com.fyusion.sdk.core.a.b.b;
import com.fyusion.sdk.core.a.g;
import com.fyusion.sdk.core.a.h;

/* compiled from: Unknown */
public class c {
    MediaFormat a = new MediaFormat();
    g b;
    private volatile boolean c = false;

    /* compiled from: Unknown */
    public interface a {
        void b();
    }

    private g a(int i) {
        if (this.b == null || !this.b.a(i)) {
            g cVar;
            switch (i) {
                case 0:
                    cVar = new com.fyusion.sdk.core.a.a.c();
                    break;
                case 1:
                    cVar = new b();
                    break;
                default:
                    Log.w("FyuseEncoder", "Unknown buffer type: " + i);
                    break;
            }
            this.b = cVar;
        }
        return this.b;
    }

    public void a() {
        this.c = true;
    }

    public void a(com.fyusion.sdk.core.a.b bVar, h hVar, a aVar) {
        try {
            if (!this.c) {
                if (bVar != null) {
                    if (bVar.a() != null) {
                        a(bVar.b()).a(bVar, hVar);
                    }
                }
                Log.w("FyuseEncoder", "Unable to encode frames, data: " + bVar + " or buffer: " + bVar.a());
            }
        } catch (Throwable e) {
            Log.e("FyuseEncoder", "image encoding failed", e);
        }
        aVar.b();
    }

    public void b() {
        this.c = false;
    }
}
