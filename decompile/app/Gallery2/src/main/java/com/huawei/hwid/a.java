package com.huawei.hwid;

import com.huawei.hwid.core.datatype.HwAccount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class a {
    private static a a;
    private Map<String, List<String>> b = new HashMap();
    private Map<String, ArrayList<HwAccount>> c = new HashMap();
    private Map<String, Long> d = new HashMap();
    private Map<String, Boolean> e = new HashMap();
    private Map<String, Integer> f = new HashMap();

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

    public List<String> b() {
        return (List) this.b.get("packageNamesNotUseApk");
    }

    public void a(List<String> list) {
        this.b.put("packageNamesNotUseApk", list);
    }

    public ArrayList<HwAccount> c() {
        return (ArrayList) this.c.get("accountMap");
    }

    public void a(ArrayList<HwAccount> arrayList) {
        this.c.put("accountMap", arrayList);
    }

    public long a(String str) {
        if (this.d.containsKey(str)) {
            return ((Long) this.d.get(str)).longValue();
        }
        return 0;
    }

    public void a(String str, long j) {
        this.d.put(str, Long.valueOf(j));
    }

    public boolean b(String str) {
        if (this.e.containsKey(str)) {
            return ((Boolean) this.e.get(str)).booleanValue();
        }
        return true;
    }

    public void a(String str, boolean z) {
        this.e.put(str, Boolean.valueOf(z));
    }

    public int c(String str) {
        if (this.f.containsKey(str)) {
            return ((Integer) this.f.get(str)).intValue();
        }
        return 0;
    }

    public void a(String str, int i) {
        this.f.put(str, Integer.valueOf(i));
    }
}
