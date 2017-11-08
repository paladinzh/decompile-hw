package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/* compiled from: OfflineDBOperation */
public class x {
    private static volatile x a;
    private static ck b;
    private Context c;

    public static x a(Context context) {
        if (a == null) {
            synchronized (x.class) {
                if (a == null) {
                    a = new x(context);
                }
            }
        }
        return a;
    }

    private x(Context context) {
        this.c = context;
        b = b(this.c);
    }

    private ck b(Context context) {
        try {
            return new ck(context, w.a());
        } catch (Throwable th) {
            ce.a(th, "OfflineDB", "getDB");
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

    public ArrayList<s> a() {
        ArrayList<s> arrayList = new ArrayList();
        if (!b()) {
            return arrayList;
        }
        for (s add : b.b("", s.class)) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public synchronized void a(s sVar) {
        if (b()) {
            b.a((Object) sVar, v.d(sVar.g()));
            a(sVar.g(), sVar.c());
        }
    }

    private void a(String str, String str2) {
        if (str2 != null && str2.length() > 0) {
            String a = u.a(str);
            if (b.b(a, u.class).size() > 0) {
                b.a(a, u.class);
            }
            String[] split = str2.split(";");
            List arrayList = new ArrayList();
            for (String uVar : split) {
                arrayList.add(new u(str, uVar));
            }
            b.a(arrayList);
        }
    }

    public synchronized List<String> a(String str) {
        List<String> arrayList = new ArrayList();
        if (!b()) {
            return arrayList;
        }
        arrayList.addAll(a(b.b(u.a(str), u.class)));
        return arrayList;
    }

    public synchronized List<String> b(String str) {
        List<String> arrayList = new ArrayList();
        if (!b()) {
            return arrayList;
        }
        arrayList.addAll(a(b.b(u.b(str), u.class)));
        return arrayList;
    }

    private List<String> a(List<u> list) {
        List arrayList = new ArrayList();
        if (list.size() > 0) {
            for (u a : list) {
                arrayList.add(a.a());
            }
        }
        return arrayList;
    }

    public synchronized void c(String str) {
        if (b()) {
            b.a(v.d(str), v.class);
            b.a(u.a(str), u.class);
            b.a(t.a(str), t.class);
        }
    }

    public void a(String str, int i, long j, long j2, long j3) {
        if (b()) {
            a(str, i, j, new long[]{j2, 0, 0, 0, 0}, new long[]{j3, 0, 0, 0, 0});
        }
    }

    public synchronized void a(String str, int i, long j, long[] jArr, long[] jArr2) {
        if (b()) {
            b.a(new t(str, j, i, jArr[0], jArr2[0]), t.a(str));
        }
    }

    public synchronized long[] a(String str, int i) {
        long j = 0;
        synchronized (this) {
            if (b()) {
                long j2;
                List b = b.b(t.a(str), t.class);
                if (b.size() <= 0) {
                    j2 = 0;
                } else {
                    j2 = ((t) b.get(0)).a(i);
                    j = ((t) b.get(0)).b(i);
                }
                long[] jArr = new long[]{j2, j};
                return jArr;
            }
            long[] jArr2 = new long[]{0, 0};
            return jArr2;
        }
    }

    public synchronized int d(String str) {
        if (!b()) {
            return 0;
        }
        List b = b.b(t.a(str), t.class);
        long j = 0;
        if (b.size() > 0) {
            j = ((t) b.get(0)).a();
        }
        return (int) j;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String e(String str) {
        String str2 = null;
        synchronized (this) {
            if (b()) {
                List b = b.b(v.d(str), v.class);
                if (b.size() > 0) {
                    str2 = ((v) b.get(0)).f();
                }
            } else {
                return null;
            }
        }
    }

    public synchronized boolean f(String str) {
        if (!b()) {
            return false;
        }
        if (b.b(t.a(str), t.class).size() <= 0) {
            return false;
        }
        return true;
    }
}
