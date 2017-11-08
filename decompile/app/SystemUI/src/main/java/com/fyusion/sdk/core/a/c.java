package com.fyusion.sdk.core.a;

import android.graphics.Bitmap;
import android.util.Log;
import org.mtnwrw.pdqimg.PDQImage;

/* compiled from: Unknown */
public class c<T> {
    private f<T> a;

    /* compiled from: Unknown */
    public enum a {
        RGBA8888(4),
        RGB565(2),
        GRAYSCALE(1);
        
        private final int d;

        private a(int i) {
            this.d = i;
        }

        public int a() {
            return this.d;
        }
    }

    /* compiled from: Unknown */
    public enum b {
        FULL_RESOLUTION,
        HINT_1080P
    }

    private f<T> a(int i) {
        if (this.a == null || !this.a.a(i)) {
            f bVar;
            switch (i) {
                case 0:
                    bVar = new com.fyusion.sdk.core.a.a.b();
                    break;
                case 1:
                    bVar = new com.fyusion.sdk.core.a.b.a();
                    break;
                default:
                    Log.w("FrameExtractor", "unknown encode type: " + i);
                    break;
            }
            this.a = bVar;
        }
        return this.a;
    }

    public d a(h hVar, a aVar) {
        Object obj = null;
        d a = e.a.a(hVar.a());
        if (a == null) {
            Class cls;
            int h = hVar.h();
            int e = hVar.e();
            int f = hVar.f();
            f a2 = a(hVar.h());
            switch (h) {
                case 0:
                    cls = Bitmap.class;
                    break;
                case 1:
                    cls = PDQImage.class;
                    break;
                default:
                    cls = null;
                    break;
            }
            if (a2 != null) {
                a = e.a.a(e, f, aVar.a(), cls);
                if (a != null) {
                    obj = a.a();
                }
                d a3 = a(hVar.h()).a(hVar, obj, e, f);
                e.a.a(hVar.a(), a3);
                return a3;
            }
        }
        return a;
    }

    public d a(h hVar, b bVar) {
        Object obj = null;
        d a = e.a.a(hVar.a());
        if (a == null) {
            Class cls;
            int h = hVar.h();
            int e = hVar.e();
            int f = hVar.f();
            int i = 0;
            f a2 = a(hVar.h());
            switch (h) {
                case 0:
                    cls = Bitmap.class;
                    i = 2;
                    break;
                case 1:
                    cls = PDQImage.class;
                    i = 1;
                    if (bVar == b.HINT_1080P) {
                        int i2 = e;
                        while (f > 1080) {
                            e = f >> 1;
                            if (e <= 1080) {
                                if (1080 - e < f - 1080) {
                                    int i3 = e;
                                    e = i2 >> 1;
                                    f = i3;
                                    break;
                                }
                                e = i2;
                                break;
                            }
                            i2 >>= 1;
                            f = e;
                        }
                        e = i2;
                    }
                    break;
                default:
                    cls = null;
                    break;
            }
            if (a2 != null) {
                a = e.a.a(e, f, i, cls);
                if (a != null) {
                    obj = a.a();
                }
                d a3 = a(hVar.h()).a(hVar, obj, e, f);
                e.a.a(hVar.a(), a3);
                return a3;
            }
        }
        return a;
    }
}
