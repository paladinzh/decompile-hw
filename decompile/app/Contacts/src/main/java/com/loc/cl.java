package com.loc;

import android.content.Context;
import android.text.TextUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import org.json.JSONObject;

/* compiled from: HeatMap */
public class cl {
    private static cl a = null;
    private Hashtable<String, JSONObject> b = new Hashtable();
    private boolean c = false;

    private cl() {
    }

    public static synchronized cl a() {
        cl clVar;
        synchronized (cl.class) {
            if (a == null) {
                a = new cl();
            }
            clVar = a;
        }
        return clVar;
    }

    private void d() {
        if (!this.b.isEmpty()) {
            this.b.clear();
        }
    }

    public void a(Context context) {
        if (by.a && !this.c) {
            cw.b();
            try {
                cj.a().b(context);
            } catch (Throwable th) {
                e.a(th, "HeatMap", "loadDB");
            }
            this.c = true;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(Context context, String str, AmapLoc amapLoc) {
        String str2 = null;
        synchronized (this) {
            if (cw.a(amapLoc) && context != null) {
                if (by.a) {
                    if (this.b.size() > VTMCDataCache.MAXSIZE) {
                        str2 = cb.a(amapLoc.i(), amapLoc.h());
                        if (!this.b.containsKey(str2)) {
                            return;
                        }
                    }
                    if (str2 == null) {
                        str2 = cb.a(amapLoc.i(), amapLoc.h());
                    }
                    JSONObject jSONObject = new JSONObject();
                    try {
                        jSONObject.put("key", str);
                        jSONObject.put("lat", amapLoc.i());
                        jSONObject.put("lon", amapLoc.h());
                        a(context, str2, jSONObject.toString(), 1, cw.a(), true);
                    } catch (Throwable th) {
                        e.a(th, "HeatMap", "update");
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(Context context, String str, String str2, int i, long j, boolean z) {
        if (context != null) {
            if (!TextUtils.isEmpty(str)) {
                if (by.a) {
                    JSONObject jSONObject = (JSONObject) this.b.get(str);
                    if (jSONObject == null) {
                        jSONObject = new JSONObject();
                    }
                    try {
                        jSONObject.put("x", str2);
                        jSONObject.put("time", j);
                        if (this.b.containsKey(str)) {
                            jSONObject.put("num", jSONObject.getInt("num") + i);
                        } else {
                            jSONObject.put("num", i);
                        }
                    } catch (Throwable th) {
                        e.a(th, "HeatMap", "update1");
                    }
                    this.b.put(str, jSONObject);
                    if (z) {
                        try {
                            cj.a().a(context, str, str2, j);
                        } catch (Throwable th2) {
                            e.a(th2, "HeatMap", "update");
                        }
                    }
                }
            }
        }
    }

    public synchronized ArrayList<ck> b() {
        ArrayList<ck> arrayList = new ArrayList();
        if (this.b.isEmpty()) {
            return arrayList;
        }
        Hashtable hashtable = this.b;
        ArrayList arrayList2 = new ArrayList(hashtable.keySet());
        Iterator it = arrayList2.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            try {
                JSONObject jSONObject = (JSONObject) hashtable.get(str);
                int i = jSONObject.getInt("num");
                String string = jSONObject.getString("x");
                long j = jSONObject.getLong("time");
                if (i >= 120) {
                    arrayList.add(new ck(str, j, i, string));
                }
            } catch (Throwable th) {
                e.a(th, "HeatMap", "hot");
            }
        }
        Collections.sort(arrayList, new Comparator<ck>(this) {
            final /* synthetic */ cl a;

            {
                this.a = r1;
            }

            public int a(ck ckVar, ck ckVar2) {
                return ckVar2.b() - ckVar.b();
            }

            public /* synthetic */ int compare(Object obj, Object obj2) {
                return a((ck) obj, (ck) obj2);
            }
        });
        arrayList2.clear();
        return arrayList;
    }

    public void c() {
        a().d();
        this.c = false;
    }
}
