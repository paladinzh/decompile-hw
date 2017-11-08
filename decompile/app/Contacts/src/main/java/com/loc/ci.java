package com.loc;

import android.content.Context;
import android.text.TextUtils;
import com.amap.api.maps.model.WeightedLatLng;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.autonavi.aps.amapapi.model.AmapLoc;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONObject;

/* compiled from: Cache */
public class ci {
    private static ci a = null;
    private Hashtable<String, ArrayList<a>> b = new Hashtable();
    private long c = 0;
    private boolean d = false;

    /* compiled from: Cache */
    public class a {
        final /* synthetic */ ci a;
        private AmapLoc b = null;
        private String c = null;

        protected a(ci ciVar) {
            this.a = ciVar;
        }

        public AmapLoc a() {
            return this.b;
        }

        public void a(AmapLoc amapLoc) {
            this.b = amapLoc;
        }

        public void a(String str) {
            if (TextUtils.isEmpty(str)) {
                this.c = null;
            } else {
                this.c = str.replace("##", "#");
            }
        }

        public String b() {
            return this.c;
        }
    }

    private ci() {
    }

    private synchronized a a(StringBuilder stringBuilder, String str) {
        if (!this.b.isEmpty()) {
            if (!TextUtils.isEmpty(stringBuilder)) {
                if (!this.b.containsKey(str)) {
                    return null;
                }
                a aVar;
                Hashtable hashtable = new Hashtable();
                Hashtable hashtable2 = new Hashtable();
                Hashtable hashtable3 = new Hashtable();
                ArrayList arrayList = (ArrayList) this.b.get(str);
                for (int size = arrayList.size() - 1; size >= 0; size--) {
                    aVar = (a) arrayList.get(size);
                    if (!TextUtils.isEmpty(aVar.b())) {
                        String str2;
                        Object obj = null;
                        if (c(aVar.b(), stringBuilder)) {
                            obj = 1;
                            if (b(aVar.b(), stringBuilder)) {
                                break;
                            }
                        }
                        Object obj2 = obj;
                        a(aVar.b(), hashtable);
                        a(stringBuilder.toString(), hashtable2);
                        hashtable3.clear();
                        for (String str22 : hashtable.keySet()) {
                            hashtable3.put(str22, "");
                        }
                        for (String str222 : hashtable2.keySet()) {
                            hashtable3.put(str222, "");
                        }
                        Set keySet = hashtable3.keySet();
                        double[] dArr = new double[keySet.size()];
                        double[] dArr2 = new double[keySet.size()];
                        Iterator it = keySet.iterator();
                        int i = 0;
                        while (it != null && it.hasNext()) {
                            str222 = (String) it.next();
                            dArr[i] = !hashtable.containsKey(str222) ? 0.0d : WeightedLatLng.DEFAULT_INTENSITY;
                            dArr2[i] = !hashtable2.containsKey(str222) ? 0.0d : WeightedLatLng.DEFAULT_INTENSITY;
                            i++;
                        }
                        keySet.clear();
                        double[] a = a(dArr, dArr2);
                        if (a[0] < 0.800000011920929d && a[1] < 0.618d) {
                            if (obj2 != null && a[0] >= 0.618d) {
                                break;
                            }
                        }
                        break;
                    }
                }
                aVar = null;
                hashtable.clear();
                hashtable2.clear();
                hashtable3.clear();
                return aVar;
            }
        }
        return null;
    }

    public static synchronized ci a() {
        ci ciVar;
        synchronized (ci.class) {
            if (a == null) {
                a = new ci();
            }
            ciVar = a;
        }
        return ciVar;
    }

    private void a(String str, Hashtable<String, String> hashtable) {
        if (!TextUtils.isEmpty(str)) {
            hashtable.clear();
            for (Object obj : str.split("#")) {
                if (!(TextUtils.isEmpty(obj) || obj.contains("|"))) {
                    hashtable.put(obj, "");
                }
            }
        }
    }

    private double[] a(double[] dArr, double[] dArr2) {
        int i;
        double[] dArr3 = new double[3];
        double d = 0.0d;
        double d2 = 0.0d;
        double d3 = 0.0d;
        int i2 = 0;
        int i3 = 0;
        for (i = 0; i < dArr.length; i++) {
            d2 += dArr[i] * dArr[i];
            d3 += dArr2[i] * dArr2[i];
            d += dArr[i] * dArr2[i];
            if (dArr2[i] == WeightedLatLng.DEFAULT_INTENSITY) {
                i2++;
                if (dArr[i] == WeightedLatLng.DEFAULT_INTENSITY) {
                    i3++;
                }
            }
        }
        dArr3[0] = d / (Math.sqrt(d3) * Math.sqrt(d2));
        dArr3[1] = (((double) i3) * WeightedLatLng.DEFAULT_INTENSITY) / ((double) i2);
        dArr3[2] = (double) i3;
        for (i = 0; i < dArr3.length - 1; i++) {
            if (dArr3[i] > WeightedLatLng.DEFAULT_INTENSITY) {
                dArr3[i] = WeightedLatLng.DEFAULT_INTENSITY;
            }
            dArr3[i] = Double.parseDouble(cw.a(Double.valueOf(dArr3[i]), "#.00"));
        }
        return dArr3;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean c(String str, StringBuilder stringBuilder) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(stringBuilder) || !str.contains(",access") || stringBuilder.indexOf(",access") == -1) {
            return false;
        }
        String[] split = str.split(",access");
        Object substring = !split[0].contains("#") ? split[0] : split[0].substring(split[0].lastIndexOf("#") + 1);
        return !TextUtils.isEmpty(substring) ? stringBuilder.toString().contains(substring + ",access") : false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized AmapLoc a(String str, StringBuilder stringBuilder) {
        if (str.contains(GeocodeSearch.GPS)) {
            return null;
        }
        if (b()) {
            c();
            return null;
        } else if (this.b.isEmpty()) {
            return null;
        } else {
            a a;
            String str2 = "found#⊗";
            String str3;
            if (str.contains("cgiwifi")) {
                a = a(stringBuilder, str);
                if (a != null) {
                    str3 = "found#cgiwifi";
                }
            } else if (str.contains("wifi")) {
                a = a(stringBuilder, str);
                if (a != null) {
                    str3 = "found#wifi";
                }
            } else if (str.contains("cgi")) {
                a = !this.b.containsKey(str) ? null : (a) ((ArrayList) this.b.get(str)).get(0);
                if (a != null) {
                    str3 = "found#cgi";
                }
            } else {
                a = null;
            }
            if (a != null && cw.a(a.a())) {
                a.a().f("mem");
                if (TextUtils.isEmpty(e.g)) {
                    e.g = String.valueOf(a.a().B());
                }
                return a.a();
            }
        }
    }

    public void a(Context context) {
        if (!this.d) {
            cw.b();
            try {
                cj.a().a(context);
            } catch (Throwable th) {
                e.a(th, "Cache", "loadDB");
            }
            this.d = true;
        }
    }

    public synchronized void a(String str, StringBuilder stringBuilder, AmapLoc amapLoc, Context context, boolean z) {
        int i = 0;
        synchronized (this) {
            if (!a(str, amapLoc)) {
                return;
            } else if (amapLoc.l().equals("mem")) {
                return;
            } else if (amapLoc.l().equals("file")) {
                return;
            } else if (amapLoc.m().equals("-3")) {
                return;
            } else {
                if (b()) {
                    c();
                }
                JSONObject E = amapLoc.E();
                if (cw.a(E, "offpct")) {
                    E.remove("offpct");
                    amapLoc.a(E);
                }
                if (str.contains("wifi")) {
                    if (TextUtils.isEmpty(stringBuilder)) {
                        return;
                    }
                    if (amapLoc.j() >= 300.0f) {
                        for (String contains : stringBuilder.toString().split("#")) {
                            if (contains.contains(",")) {
                                i++;
                            }
                        }
                        if (i >= 8) {
                            return;
                        }
                    } else if (amapLoc.j() <= 10.0f) {
                        return;
                    }
                    if (str.contains("cgiwifi") && !TextUtils.isEmpty(amapLoc.C())) {
                        String replace = str.replace("cgiwifi", "cgi");
                        AmapLoc D = amapLoc.D();
                        if (cw.a(D)) {
                            a(replace, new StringBuilder(), D, context, true);
                        }
                    }
                } else if (str.contains("cgi")) {
                    if (stringBuilder.indexOf(",") != -1) {
                        return;
                    } else if (amapLoc.m().equals("4")) {
                        return;
                    }
                }
                AmapLoc a = a(str, stringBuilder);
                if (cw.a(a)) {
                    if (a.F().equals(amapLoc.c(3))) {
                        return;
                    }
                }
                this.c = cw.b();
                a aVar = new a(this);
                aVar.a(amapLoc);
                aVar.a(!TextUtils.isEmpty(stringBuilder) ? stringBuilder.toString() : null);
                if (this.b.containsKey(str)) {
                    ((ArrayList) this.b.get(str)).add(aVar);
                } else {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(aVar);
                    this.b.put(str, arrayList);
                }
                if (z) {
                    try {
                        cj.a().a(str, amapLoc, stringBuilder, context);
                    } catch (Throwable th) {
                        e.a(th, "Cache", "add");
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(String str, AmapLoc amapLoc) {
        if (TextUtils.isEmpty(str) || !cw.a(amapLoc) || str.startsWith("#")) {
            return false;
        }
        boolean z = true;
        if (!str.contains("network")) {
            z = false;
        }
        return z;
    }

    public boolean b() {
        long b = cw.b() - this.c;
        if (this.c == 0) {
            return false;
        }
        if (this.b.size() <= 360) {
            if (b <= 36000000) {
                return false;
            }
        }
        return true;
    }

    public boolean b(String str, StringBuilder stringBuilder) {
        String[] split = str.split("#");
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (i < split.length) {
            if (split[i].contains(",nb") || split[i].contains(",access")) {
                arrayList.add(split[i]);
            }
            i++;
        }
        String[] split2 = stringBuilder.toString().split("#");
        i = 0;
        int i2 = 0;
        int i3 = 0;
        while (i < split2.length) {
            if (split2[i].contains(",nb") || split2[i].contains(",access")) {
                i2++;
                if (arrayList.contains(split2[i])) {
                    i3++;
                }
            }
            i++;
        }
        return ((double) (i3 * 2)) >= ((double) (arrayList.size() + i2)) * 0.618d;
    }

    public void c() {
        this.c = 0;
        if (!this.b.isEmpty()) {
            this.b.clear();
        }
        this.d = false;
    }
}
