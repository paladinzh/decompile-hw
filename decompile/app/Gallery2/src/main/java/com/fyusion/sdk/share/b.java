package com.fyusion.sdk.share;

import android.content.Context;
import android.net.Uri;
import com.a.a.a.k;
import com.a.a.d;
import com.a.a.l;
import com.a.a.m;
import com.fyusion.sdk.common.a;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: Unknown */
class b {
    private static b a = null;
    private static Map<Integer, i> b = new ConcurrentHashMap();
    private static m c;

    private b() {
    }

    private m a(Context context) {
        if (c == null) {
            c = k.a(context);
            c.a();
        }
        return c;
    }

    protected static b a() {
        if (a == null) {
            a = new b();
        }
        return a;
    }

    int a(Uri uri, boolean z) {
        i a = a(uri);
        if (a == null) {
            return 0;
        }
        a.b().a(z);
        a.c();
        b.remove(Integer.valueOf(uri.getPath().hashCode()));
        return 1;
    }

    protected int a(i iVar) {
        if (iVar == null || iVar.a() == null || iVar.a().getPath() == null) {
            return -1;
        }
        int hashCode = iVar.a().getPath().hashCode();
        if (b.get(Integer.valueOf(hashCode)) != null) {
            return 0;
        }
        b.put(Integer.valueOf(hashCode), iVar);
        return hashCode;
    }

    protected i a(Uri uri) {
        if (uri == null || uri.getPath() == null) {
            return null;
        }
        return (i) b.get(Integer.valueOf(uri.getPath().hashCode()));
    }

    protected <T> void a(Context context, l<T> lVar) {
        lVar.a(new d(30000, 0, WMElement.CAMERASIZEVALUE1B1));
        a(context).a(lVar);
    }

    protected String b() {
        return a.g();
    }

    boolean b(Uri uri) {
        return a(uri) != null;
    }

    protected int c(Uri uri) {
        if (a(uri) == null) {
            return 0;
        }
        b.remove(Integer.valueOf(uri.getPath().hashCode()));
        return 1;
    }

    protected String c() {
        return a.a().l();
    }
}
