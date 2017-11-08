package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/* compiled from: OfflineDBOperation */
public class bm {
    private static volatile bm a;
    private static fu b;
    private Context c;

    public static bm a(Context context) {
        if (a == null) {
            synchronized (bm.class) {
                if (a == null) {
                    a = new bm(context);
                }
            }
        }
        return a;
    }

    private bm(Context context) {
        this.c = context;
        b = b(this.c);
    }

    private fu b(Context context) {
        try {
            return new fu(context, bl.a());
        } catch (Throwable th) {
            fo.b(th, "OfflineDB", "getDB");
            th.printStackTrace();
            return null;
        }
    }

    private boolean b() {
        if (b == null) {
            b = b(this.c);
        }
        if (b != null) {
            return true;
        }
        return false;
    }

    public ArrayList<bh> a() {
        ArrayList<bh> arrayList = new ArrayList();
        if (!b()) {
            return arrayList;
        }
        for (bh add : b.b("", bh.class)) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public synchronized bh a(String str) {
        if (!b()) {
            return null;
        }
        List b = b.b(bk.e(str), bh.class);
        if (b.size() <= 0) {
            return null;
        }
        return (bh) b.get(0);
    }

    public synchronized void a(bh bhVar) {
        if (b()) {
            b.a((Object) bhVar, bk.f(bhVar.h()));
            a(bhVar.f(), bhVar.b());
        }
    }

    private void a(String str, String str2) {
        if (str2 != null && str2.length() > 0) {
            String a = bj.a(str);
            if (b.b(a, bj.class).size() > 0) {
                b.a(a, bj.class);
            }
            String[] split = str2.split(";");
            List arrayList = new ArrayList();
            for (String bjVar : split) {
                arrayList.add(new bj(str, bjVar));
            }
            b.a(arrayList);
        }
    }

    public synchronized List<String> b(String str) {
        List<String> arrayList = new ArrayList();
        if (!b()) {
            return arrayList;
        }
        arrayList.addAll(a(b.b(bj.a(str), bj.class)));
        return arrayList;
    }

    private List<String> a(List<bj> list) {
        List arrayList = new ArrayList();
        if (list.size() > 0) {
            for (bj a : list) {
                arrayList.add(a.a());
            }
        }
        return arrayList;
    }

    public synchronized void c(String str) {
        if (b()) {
            b.a(bk.e(str), bk.class);
            b.a(bj.a(str), bj.class);
            b.a(bi.a(str), bi.class);
        }
    }

    public synchronized void b(bh bhVar) {
        if (b()) {
            b.a(bk.f(bhVar.h()), bk.class);
            b.a(bj.a(bhVar.f()), bj.class);
            b.a(bi.a(bhVar.f()), bi.class);
        }
    }

    public void a(String str, int i, long j, long j2, long j3) {
        if (b()) {
            a(str, i, j, new long[]{j2, 0, 0, 0, 0}, new long[]{j3, 0, 0, 0, 0});
        }
    }

    public synchronized void a(String str, int i, long j, long[] jArr, long[] jArr2) {
        if (b()) {
            b.a(new bi(str, j, i, jArr[0], jArr2[0]), bi.a(str));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String d(String str) {
        String str2 = null;
        synchronized (this) {
            if (b()) {
                List b = b.b(bk.f(str), bk.class);
                if (b.size() > 0) {
                    str2 = ((bk) b.get(0)).e();
                }
            } else {
                return null;
            }
        }
    }
}
