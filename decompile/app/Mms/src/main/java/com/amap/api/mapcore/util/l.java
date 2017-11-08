package com.amap.api.mapcore.util;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: OfflineMapDataVerify */
public class l extends Thread {
    private Context a;
    private x b;

    public l(Context context) {
        this.a = context;
        this.b = x.a(context);
    }

    public void run() {
        a();
    }

    private void a() {
        ArrayList arrayList;
        ArrayList arrayList2 = new ArrayList();
        ArrayList a = this.b.a();
        if (a.size() >= 1) {
            arrayList = a;
            Object obj = null;
        } else {
            arrayList = a(this.a);
            int i = 1;
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            s sVar = (s) it.next();
            if (!(sVar == null || sVar.e() == null || sVar.g().length() < 1)) {
                if (Thread.interrupted()) {
                    break;
                }
                if (obj != null) {
                    arrayList2.add(sVar);
                }
                if (sVar.l == 4 || sVar.l == 7) {
                    if (!a(sVar.g())) {
                        sVar.b();
                        try {
                            af.a(sVar.g(), this.a);
                        } catch (Exception e) {
                        }
                        arrayList2.add(sVar);
                    }
                }
            }
        }
        i a2 = i.a(this.a);
        if (a2 != null) {
            a2.a(arrayList2);
        }
    }

    private ArrayList<s> a(Context context) {
        ArrayList<s> arrayList = new ArrayList();
        File file = new File(bj.b(context));
        if (!file.exists()) {
            return arrayList;
        }
        File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return arrayList;
        }
        for (File file2 : listFiles) {
            if (file2.getName().endsWith(".zip.tmp.dt")) {
                s a = a(file2);
                if (!(a == null || a.e() == null)) {
                    arrayList.add(a);
                    this.b.a(a);
                }
            }
        }
        return arrayList;
    }

    private s a(File file) {
        String a = bj.a(file);
        s sVar = new s();
        sVar.b(a);
        return sVar;
    }

    private boolean a(String str) {
        List<String> a = this.b.a(str);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(bj.a(this.a));
        stringBuilder.append("vmap/");
        int length = stringBuilder.length();
        for (String replace : a) {
            stringBuilder.replace(length, stringBuilder.length(), replace);
            if (!new File(stringBuilder.toString()).exists()) {
                return false;
            }
        }
        return true;
    }

    public void destroy() {
        this.a = null;
        this.b = null;
    }
}
