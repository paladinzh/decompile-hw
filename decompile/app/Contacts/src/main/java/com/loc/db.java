package com.loc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.amap.api.services.geocoder.GeocodeSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class db {
    private static float M = 1.1f;
    private static float N = 2.2f;
    private static float O = 2.3f;
    private static float P = 3.8f;
    private static int Q = 3;
    private static int R = 10;
    private static int S = 2;
    private static int T = 7;
    private static int U = 20;
    private static int V = 70;
    private static int W = 120;
    protected static boolean a = false;
    protected static boolean b = true;
    private static int d = 10;
    private static int e = 2;
    private static int f = 10;
    private static int g = 10;
    private static int h = 50;
    private static int i = 200;
    private static Object j = new Object();
    private static db k;
    private dw A = null;
    private volatile Handler B = null;
    private dx C = new dx(this);
    private LocationListener D = new dr(this);
    private BroadcastReceiver E = new ds(this);
    private BroadcastReceiver F = new dt(this);
    private GpsStatus G = null;
    private int H = 0;
    private int I = 0;
    private HashMap J = null;
    private int K = 0;
    private int L = 0;
    Object c = new Object();
    private boolean l = false;
    private int m = -1;
    private int n = 0;
    private int o = 0;
    private Context p;
    private LocationManager q;
    private dl r;
    private dz s;
    private ef t;
    private di u;
    private ee v;
    private dy w;
    private dc x;
    private Thread y = null;
    private Looper z = null;

    private db(Context context) {
        this.p = context;
        this.r = dl.a(context);
        this.x = new dc();
        this.s = new dz(this.r);
        this.u = new di(context);
        this.t = new ef(this.u);
        this.v = new ee(this.u);
        this.q = (LocationManager) this.p.getSystemService("location");
        this.w = dy.a(this.p);
        this.w.a(this.C);
        o();
        List allProviders = this.q.getAllProviders();
        boolean z = allProviders != null && allProviders.contains(GeocodeSearch.GPS) && allProviders.contains("passive");
        this.l = z;
        if (context == null) {
            Log.d(dl.a, "Error: No SD Card!");
        } else {
            dl.a = context.getPackageName();
        }
    }

    static /* synthetic */ int a(db dbVar, eh ehVar, int i) {
        if (dbVar.K >= R) {
            return 1;
        }
        if (dbVar.K <= Q) {
            return 4;
        }
        double c = ehVar.c();
        if (c <= ((double) M)) {
            return 1;
        }
        if (c >= ((double) N)) {
            return 4;
        }
        c = ehVar.b();
        return c <= ((double) O) ? 1 : c >= ((double) P) ? 4 : i < T ? i > S ? dbVar.J == null ? 3 : dbVar.a(dbVar.J) : 4 : 1;
    }

    private int a(HashMap hashMap) {
        if (this.H > 4) {
            int i;
            List arrayList = new ArrayList();
            List arrayList2 = new ArrayList();
            int i2 = 0;
            Iterator it = hashMap.entrySet().iterator();
            while (true) {
                i = i2;
                if (!it.hasNext()) {
                    break;
                }
                List list = (List) ((Entry) it.next()).getValue();
                if (list == null) {
                    i2 = i;
                } else {
                    Object a = a(list);
                    if (a == null) {
                        i2 = i;
                    } else {
                        arrayList.add(a);
                        i2 = i + 1;
                        arrayList2.add(Integer.valueOf(i));
                    }
                }
            }
            if (!arrayList.isEmpty()) {
                double[] dArr = new double[2];
                int size = arrayList.size();
                for (int i3 = 0; i3 < size; i3++) {
                    double[] dArr2 = (double[]) arrayList.get(i3);
                    i = ((Integer) arrayList2.get(i3)).intValue();
                    dArr2[0] = dArr2[0] * ((double) i);
                    dArr2[1] = dArr2[1] * ((double) i);
                    dArr[0] = dArr[0] + dArr2[0];
                    dArr[1] = dArr[1] + dArr2[1];
                }
                dArr[0] = dArr[0] / ((double) size);
                dArr[1] = dArr[1] / ((double) size);
                double d = dArr[0];
                double d2 = dArr[1];
                double toDegrees = d2 == 0.0d ? d > 0.0d ? 90.0d : d < 0.0d ? 270.0d : 0.0d : Math.toDegrees(Math.atan(d / d2));
                double[] dArr3 = new double[]{Math.sqrt((d * d) + (d2 * d2)), toDegrees};
                String.format(Locale.CHINA, "%d,%d,%d,%d", new Object[]{Long.valueOf(Math.round(dArr[0] * 100.0d)), Long.valueOf(Math.round(dArr[1] * 100.0d)), Long.valueOf(Math.round(dArr3[0] * 100.0d)), Long.valueOf(Math.round(dArr3[1] * 100.0d))});
                if (dArr3[0] <= ((double) V)) {
                    return 1;
                }
                if (dArr3[0] >= ((double) W)) {
                    return 4;
                }
            }
        }
        return 3;
    }

    public static db a(Context context) {
        if (k == null) {
            synchronized (j) {
                if (k == null) {
                    k = new db(context);
                }
            }
        }
        return k;
    }

    static /* synthetic */ String a(db dbVar, String str) {
        return str;
    }

    public static String a(String str) {
        return !str.equals("version") ? !str.equals("date") ? null : "COL.15.0929r" : "V1.0.0r";
    }

    static /* synthetic */ void a(db dbVar, Location location, int i, long j) {
        da a;
        Long valueOf;
        System.currentTimeMillis();
        boolean a2 = dbVar.s.a(location);
        if (a2) {
            dbVar.s.b.b = new Location(location);
        }
        boolean b = dbVar.s.b(location);
        if (b) {
            dbVar.s.a.b = new Location(location);
        }
        int i2 = 0;
        if (a2) {
            i2 = 1;
            if (b) {
                i2 = 3;
            }
        } else if (b) {
            i2 = 2;
        }
        try {
            dc dcVar = dbVar.x;
            a = dc.a(location, dbVar.r, i2, (byte) dbVar.L, j, false);
        } catch (Exception e) {
            a = null;
        }
        if (!(a == null || dbVar.r == null)) {
            List m = dbVar.r.m();
            valueOf = Long.valueOf(0);
            if (m != null && m.size() > 0) {
                valueOf = (Long) m.get(0);
            }
            dbVar.t.a(valueOf.longValue(), a.a());
        }
        if (dbVar.p != null && dbVar.x != null) {
            SharedPreferences sharedPreferences = dbVar.p.getSharedPreferences("app_pref", 0);
            if (!sharedPreferences.getString("get_sensor", "").equals("true")) {
                try {
                    dcVar = dbVar.x;
                    a = dc.a(null, dbVar.r, i2, (byte) dbVar.L, j, true);
                } catch (Exception e2) {
                    a = null;
                }
                if (!(a == null || dbVar.r == null)) {
                    List m2 = dbVar.r.m();
                    valueOf = Long.valueOf(0);
                    if (m2 != null && m2.size() > 0) {
                        valueOf = (Long) m2.get(0);
                    }
                    dbVar.t.a(valueOf.longValue(), a.a());
                    sharedPreferences.edit().putString("get_sensor", "true").commit();
                }
            }
        }
    }

    private double[] a(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        double[] dArr = new double[2];
        for (GpsSatellite gpsSatellite : list) {
            if (gpsSatellite != null) {
                double elevation = (double) (90.0f - gpsSatellite.getElevation());
                double azimuth = (double) gpsSatellite.getAzimuth();
                double[] dArr2 = new double[]{Math.sin(Math.toRadians(azimuth)) * elevation, elevation * Math.cos(Math.toRadians(azimuth))};
                dArr[0] = dArr[0] + dArr2[0];
                dArr[1] = dArr[1] + dArr2[1];
            }
        }
        int size = list.size();
        dArr[0] = dArr[0] / ((double) size);
        dArr[1] = dArr[1] / ((double) size);
        return dArr;
    }

    private void o() {
        this.n = this.w.b() * 1000;
        this.o = this.w.c();
        dz dzVar = this.s;
        int i = this.n;
        i = this.o;
        dz.a();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a() {
        dl.b = true;
        if (this.l && this.r != null && !a) {
            a = true;
            IntentFilter intentFilter = new IntentFilter("android.location.GPS_ENABLED_CHANGE");
            intentFilter.addAction("android.location.GPS_FIX_CHANGE");
            b = true;
            this.p.registerReceiver(this.F, intentFilter);
            intentFilter = new IntentFilter();
            intentFilter.setPriority(1000);
            intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            intentFilter.addAction("android.intent.action.MEDIA_MOUNTED");
            intentFilter.addAction("android.intent.action.MEDIA_EJECT");
            intentFilter.addDataScheme("file");
            this.p.registerReceiver(this.E, intentFilter);
            String str = "";
            this.q.removeGpsStatusListener(this.A);
            this.q.removeNmeaListener(this.A);
            this.A = null;
            this.q.removeUpdates(this.D);
            if (this.z != null) {
                this.z.quit();
                this.z = null;
            }
            if (this.y != null) {
                this.y.interrupt();
                this.y = null;
            }
            this.y = new du(this, str);
            this.y.start();
            this.r.a();
        }
    }

    public void a(int i) {
        if (i == 256 || i == 8736 || i == 768) {
            this.u.a(i);
            return;
        }
        throw new RuntimeException("invalid Size! must be COLLECTOR_SMALL_SIZE or COLLECTOR_BIG_SIZE or COLLECTOR_MEDIUM_SIZE");
    }

    public void a(dh dhVar, String str) {
        if (!dl.c) {
            boolean a = this.w.a(str);
            if (dhVar != null) {
                byte[] a2 = dhVar.a();
                if (a && a2 != null) {
                    NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.p.getSystemService("connectivity")).getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        if (activeNetworkInfo.getType() != 1) {
                            this.w.b(a2.length + this.w.f());
                        } else {
                            this.w.a(a2.length + this.w.e());
                        }
                    }
                }
                dhVar.a(a);
                this.v.a(dhVar);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b() {
        dl.b = false;
        this.B = null;
        dl.c = false;
        if (this.l && this.r != null && a) {
            if (this.F != null) {
                try {
                    this.p.unregisterReceiver(this.F);
                    this.p.unregisterReceiver(this.E);
                } catch (Exception e) {
                }
            }
            if (this.r != null) {
                this.r.v();
            }
            synchronized (this.c) {
                a = false;
                this.q.removeGpsStatusListener(this.A);
                this.q.removeNmeaListener(this.A);
                this.A = null;
                this.q.removeUpdates(this.D);
                if (this.z != null) {
                    this.z.quit();
                    this.z = null;
                }
                if (this.y != null) {
                    this.y.interrupt();
                    this.y = null;
                }
            }
            this.r.b();
        }
    }

    public void b(int i) {
        if (this.r != null) {
            this.r.a(i);
        }
    }

    public void c() {
        if (this.l) {
            b();
        }
    }

    public boolean d() {
        return a;
    }

    public dh e() {
        if (this.v == null) {
            return null;
        }
        f();
        return (!this.w.a() || dl.c) ? null : this.v.a(this.w.d());
    }

    public boolean f() {
        if (this.r != null) {
            List m = this.r.m();
            if (m != null && m.size() > 0) {
                return this.u.b(((Long) m.get(0)).longValue());
            }
        }
        return false;
    }

    public int g() {
        return this.v == null ? 0 : this.v.a();
    }
}
