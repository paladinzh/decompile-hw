package com.huawei.hwid;

import android.content.Intent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: ApplicationContext */
public final class a {
    private static a a;
    private Map b = new HashMap();
    private Map c = new HashMap();
    private Map d = new HashMap();
    private String e = null;
    private ArrayList f = new ArrayList();
    private Intent g = null;
    private String h = null;
    private Map i = new HashMap();
    private Map j = new HashMap();
    private Map k = new HashMap();

    private a() {
    }

    public static synchronized a a() {
        a aVar;
        synchronized (a.class) {
            if (a == null) {
                a = new a();
            }
            aVar = a;
        }
        return aVar;
    }

    public List b() {
        return (List) this.c.get("packageNamesNotUseApk");
    }

    public void a(List list) {
        this.c.put("packageNamesNotUseApk", list);
    }

    public ArrayList c() {
        return (ArrayList) this.d.get("accountMap");
    }

    public void a(ArrayList arrayList) {
        this.d.put("accountMap", arrayList);
    }

    public String d() {
        return this.e;
    }

    public void a(String str) {
        this.e = str;
    }

    public String e() {
        return this.h;
    }

    public void b(String str) {
        this.h = str;
    }

    public void a(String str, boolean z) {
        this.j.put(str, Boolean.valueOf(z));
    }

    public int e(String str) {
        if (this.k.containsKey(str)) {
            return ((Integer) this.k.get(str)).intValue();
        }
        return 0;
    }

    public void a(String str, int i) {
        this.k.put(str, Integer.valueOf(i));
    }
}
