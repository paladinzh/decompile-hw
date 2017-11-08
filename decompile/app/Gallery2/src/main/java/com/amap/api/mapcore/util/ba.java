package com.amap.api.mapcore.util;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: OfflineMapDataVerify */
public class ba extends Thread {
    private Context a;
    private bm b;

    public ba(Context context) {
        this.a = context;
        this.b = bm.a(context);
    }

    public void run() {
        a();
    }

    private bh a(File file) {
        String a = eh.a(file);
        bh bhVar = new bh();
        bhVar.b(a);
        return bhVar;
    }

    public void destroy() {
        this.a = null;
        this.b = null;
    }

    private void a() {
        ArrayList a = this.b.a();
        ArrayList b = b();
        ArrayList c = c();
        Iterator it = a.iterator();
        while (it.hasNext()) {
            bh bhVar = (bh) it.next();
            if (!(bhVar == null || bhVar.d() == null)) {
                if (bhVar.l == 4 || bhVar.l == 7) {
                    if (!b.contains(bhVar.h())) {
                        this.b.b(bhVar);
                    }
                } else if (bhVar.l == 0 || bhVar.l == 1) {
                    r1 = (c.contains(bhVar.f()) || c.contains(bhVar.h())) ? 1 : null;
                    if (r1 == null) {
                        this.b.b(bhVar);
                    }
                } else if (bhVar.l == 3 && bhVar.g() != 0) {
                    r1 = (c.contains(bhVar.f()) || c.contains(bhVar.h())) ? 1 : null;
                    if (r1 == null) {
                        this.b.b(bhVar);
                    }
                }
            }
        }
        Iterator it2 = b.iterator();
        while (it2.hasNext()) {
            String str = (String) it2.next();
            if (!a(str, a)) {
                bhVar = a(str);
                if (bhVar != null) {
                    this.b.a(bhVar);
                }
            }
        }
        ax a2 = ax.a(this.a);
        if (a2 != null) {
            a2.a(null);
        }
    }

    private bh a(String str) {
        CharSequence f = ax.a(this.a).f(str);
        File[] listFiles = new File(eh.b(this.a)).listFiles();
        if (listFiles == null) {
            return null;
        }
        bh bhVar = null;
        for (File file : listFiles) {
            if (file.getName().contains(f) || file.getName().contains(str)) {
                if (file.getName().endsWith(".zip.tmp.dt")) {
                    bhVar = a(file);
                    if (!(bhVar == null || bhVar.d() == null)) {
                        return bhVar;
                    }
                }
                continue;
            }
        }
        return bhVar;
    }

    private boolean a(String str, ArrayList<bh> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if (str.equals(((bh) it.next()).h())) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> b() {
        ArrayList<String> arrayList = new ArrayList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(eh.a(this.a));
        stringBuilder.append("vmap/");
        File file = new File(stringBuilder.toString());
        if (!file.exists()) {
            return arrayList;
        }
        File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return arrayList;
        }
        for (File file2 : listFiles) {
            if (file2.getName().endsWith(".dat")) {
                String name = file2.getName();
                int lastIndexOf = name.lastIndexOf(46);
                if (lastIndexOf > -1 && lastIndexOf < name.length()) {
                    arrayList.add(name.substring(0, lastIndexOf));
                }
            }
        }
        return arrayList;
    }

    private ArrayList<String> c() {
        ArrayList<String> arrayList = new ArrayList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(eh.b(this.a));
        File file = new File(stringBuilder.toString());
        if (!file.exists()) {
            return arrayList;
        }
        File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return arrayList;
        }
        for (File file2 : listFiles) {
            if (file2.getName().endsWith(".zip")) {
                String name = file2.getName();
                int lastIndexOf = name.lastIndexOf(46);
                if (lastIndexOf > -1 && lastIndexOf < name.length()) {
                    arrayList.add(name.substring(0, lastIndexOf));
                }
            }
        }
        return arrayList;
    }
}
