package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import com.avast.android.sdk.engine.ScanResultStructure;
import com.avast.android.sdk.engine.internal.c.b;
import com.avast.android.sdk.engine.obfuscated.ap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/* compiled from: Unknown */
public class e {
    private static final HashMap<String, List<ScanResultStructure>> a = new HashMap();
    private static final HashMap<String, List<ScanResultStructure>> b = new HashMap();
    @SuppressLint({"NewApi"})
    private static final ap<String, List<ScanResultStructure>> c = new ap(1024);
    private static final HashMap<b, List<c>> d = new HashMap();

    public static synchronized List<c> a(b bVar) {
        synchronized (e.class) {
            List list = (List) d.get(bVar);
            if (list != null) {
                List linkedList = new LinkedList(list);
                return linkedList;
            }
            return null;
        }
    }

    public static synchronized List<ScanResultStructure> a(String str) {
        synchronized (e.class) {
            List list = (List) a.get(str);
            if (list != null) {
                List linkedList = new LinkedList(list);
                return linkedList;
            }
            return null;
        }
    }

    @SuppressLint({"NewApi"})
    public static synchronized void a() {
        synchronized (e.class) {
            a.clear();
            b.clear();
            c.a();
            d.clear();
        }
    }

    public static synchronized void a(b bVar, List<c> list) {
        synchronized (e.class) {
            if (list != null) {
                d.put(bVar, new LinkedList(list));
            } else {
                d.put(bVar, null);
            }
        }
    }

    public static synchronized void a(String str, List<ScanResultStructure> list) {
        synchronized (e.class) {
            if (list != null) {
                a.put(str, new LinkedList(list));
            } else {
                a.put(str, null);
            }
        }
    }

    @SuppressLint({"NewApi"})
    public static synchronized List<ScanResultStructure> b(String str) {
        synchronized (e.class) {
            List list = (List) c.a((Object) str);
            if (list != null) {
                List linkedList = new LinkedList(list);
                return linkedList;
            }
            return null;
        }
    }

    public static synchronized void b(String str, List<ScanResultStructure> list) {
        synchronized (e.class) {
            if (list != null) {
                b.put(str, new LinkedList(list));
            } else {
                b.put(str, null);
            }
        }
    }

    @SuppressLint({"NewApi"})
    public static synchronized void c(String str, List<ScanResultStructure> list) {
        synchronized (e.class) {
            if (list != null) {
                c.a(str, new LinkedList(list));
            } else {
                c.a(str, null);
            }
        }
    }
}
