package cn.com.xy.sms.sdk.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public final class p {
    private static final String a = "MemoryCache";
    private Map<String, BitmapDrawable> b;
    private long c;
    private long d;

    public p() {
        this.b = Collections.synchronizedMap(new LinkedHashMap(10, 1.5f, true));
        this.c = 0;
        this.d = 1000000;
        this.d = Runtime.getRuntime().maxMemory() / 10;
        new StringBuilder("MemoryCache will use up to ").append((((double) this.d) / 1024.0d) / 1024.0d).append("MB");
    }

    private static long a(BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable == null) {
            return 0;
        }
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bitmap == null) {
            return 0;
        }
        return (long) (bitmap.getHeight() * bitmap.getRowBytes());
    }

    private BitmapDrawable a(String str) {
        try {
            return this.b.containsKey(str) ? (BitmapDrawable) this.b.get(str) : null;
        } catch (NullPointerException e) {
            return null;
        }
    }

    private void a() {
        if ((this.c <= this.d ? 1 : null) == null) {
            Iterator it = this.b.entrySet().iterator();
            while (it.hasNext()) {
                Object obj;
                this.c -= a((BitmapDrawable) ((Entry) it.next()).getValue());
                it.remove();
                if (this.c <= this.d) {
                    obj = 1;
                    continue;
                } else {
                    obj = null;
                    continue;
                }
                if (obj != null) {
                    return;
                }
            }
        }
    }

    private void a(long j) {
        this.d = j;
        new StringBuilder("MemoryCache will use up to ").append((((double) this.d) / 1024.0d) / 1024.0d).append("MB");
    }

    private void a(String str, BitmapDrawable bitmapDrawable) {
        try {
            if (this.b.containsKey(str)) {
                this.c -= a((BitmapDrawable) this.b.get(str));
            }
            this.b.put(str, bitmapDrawable);
            this.c += a(bitmapDrawable);
            if ((this.c <= this.d ? 1 : null) == null) {
                Iterator it = this.b.entrySet().iterator();
                while (it.hasNext()) {
                    Object obj;
                    this.c -= a((BitmapDrawable) ((Entry) it.next()).getValue());
                    it.remove();
                    if (this.c <= this.d) {
                        obj = 1;
                        continue;
                    } else {
                        obj = null;
                        continue;
                    }
                    if (obj != null) {
                        break;
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    private void b() {
        this.b.clear();
    }
}
