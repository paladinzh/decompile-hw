package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;

/* compiled from: OfflineDownloadManager */
public class i {
    public static String a = "";
    public static boolean b = false;
    public static String d = "";
    private static volatile i j;
    CopyOnWriteArrayList<g> c = new CopyOnWriteArrayList();
    b e = null;
    public m f;
    o g;
    l h = null;
    private Context i;
    private a k;
    private r l;
    private x m;
    private ExecutorService n = null;
    private ExecutorService o = null;

    /* compiled from: OfflineDownloadManager */
    public interface a {
        void a(g gVar);

        void b(g gVar);

        void c(g gVar);
    }

    /* compiled from: OfflineDownloadManager */
    class b extends Handler {
        final /* synthetic */ i a;

        public b(i iVar, Looper looper) {
            this.a = iVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            try {
                message.getData();
                Object obj = message.obj;
                if (obj instanceof g) {
                    g gVar = (g) obj;
                    af.a("OfflineMapHandler handleMessage CitObj  name: " + gVar.getCity() + " complete: " + gVar.getcompleteCode() + " status: " + gVar.getState());
                    if (this.a.k != null) {
                        this.a.k.a(gVar);
                        return;
                    }
                    return;
                }
                af.a("Do not callback by CityObject! ");
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private i(Context context) {
        this.i = context;
        f();
    }

    public static i a(Context context) {
        if (j == null) {
            synchronized (i.class) {
                if (j == null) {
                    if (!b) {
                        j = new i(context.getApplicationContext());
                    }
                }
            }
        }
        return j;
    }

    private void f() {
        this.m = x.a(this.i.getApplicationContext());
        this.e = new b(this, this.i.getMainLooper());
        this.f = new m(this.i, this.e);
        this.l = r.a(1);
        a = bj.b(this.i);
        g();
        this.h = new l(this.i);
        this.h.start();
        Iterator it = this.f.a().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((OfflineMapProvince) it.next()).getCityList().iterator();
            while (it2.hasNext()) {
                this.c.add(new g(this.i, (OfflineMapCity) it2.next()));
            }
        }
        h();
    }

    private void g() {
        if (!bj.b(this.i).equals("")) {
            String c;
            File file = new File(bj.b(this.i) + "offlinemapv4.png");
            if (file.exists()) {
                c = af.c(file);
            } else {
                c = af.a(this.i, "offlinemapv4.png");
            }
            if (c != null) {
                try {
                    f(c);
                } catch (Throwable e) {
                    ce.a(e, "MapDownloadManager", "paseJson io");
                    e.printStackTrace();
                }
            }
        }
    }

    private void f(String str) throws JSONException {
        List b = af.b(str);
        if (b != null && b.size() != 0) {
            this.f.a(b);
        }
    }

    private void h() {
        Iterator it = this.m.a().iterator();
        while (it.hasNext()) {
            s sVar = (s) it.next();
            if (!(sVar == null || sVar.e() == null || sVar.g().length() < 1)) {
                if (!(sVar.l == 4 || sVar.l == 7 || sVar.l < 0)) {
                    sVar.l = 3;
                }
                g g = g(sVar.e());
                if (g != null) {
                    String f = sVar.f();
                    if (f == null || f.equals(d)) {
                        g.a(sVar.l);
                        g.setCompleteCode(sVar.j());
                    } else {
                        this.m.c(sVar.g());
                        g.a(7);
                    }
                    List<String> a = this.m.a(sVar.g());
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String append : a) {
                        stringBuffer.append(append);
                        stringBuffer.append(";");
                    }
                    g.a(stringBuffer.toString());
                    this.f.a(g);
                }
            }
        }
    }

    public void a(ArrayList<s> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            s sVar = (s) it.next();
            g g = g(sVar.e());
            if (g != null) {
                g.a(sVar);
                c(g);
            }
        }
    }

    public void a(final String str) {
        if (str != null) {
            if (this.n == null) {
                this.n = Executors.newSingleThreadExecutor();
            }
            this.n.execute(new Runnable(this) {
                final /* synthetic */ i b;

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    g a = this.b.g(str);
                    try {
                        if (a.c().equals(a.f)) {
                            String adcode = a.getAdcode();
                            if (adcode.length() > 0) {
                                adcode = this.b.m.e(adcode);
                                if (i.d.length() > 0 && !adcode.equals(i.d)) {
                                    a.i();
                                    this.b.k.b(a);
                                    return;
                                }
                            }
                            this.b.i();
                            j jVar = (j) new k(this.b.i, i.d).d();
                            if (this.b.k != null) {
                                if (jVar == null) {
                                    this.b.k.b(a);
                                    return;
                                } else if (jVar.a()) {
                                    this.b.a();
                                }
                            }
                            this.b.k.b(a);
                            return;
                        }
                        this.b.k.b(a);
                    } catch (Exception e) {
                    } catch (Throwable th) {
                        this.b.k.b(a);
                    }
                }
            });
            return;
        }
        if (this.k != null) {
            this.k.b(null);
        }
    }

    private void i() throws AMapException {
        if (!bj.c(this.i)) {
            throw new AMapException(AMapException.ERROR_CONNECTION);
        }
    }

    protected void a() throws AMapException {
        p pVar = new p(this.i, "");
        pVar.a(this.i);
        List list = (List) pVar.d();
        if (this.c != null) {
            this.f.a(list);
        }
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            g gVar = (g) it.next();
            String version = gVar.getVersion();
            if (gVar.getState() == 4 && d.length() > 0 && !version.equals(d)) {
                gVar.i();
            }
        }
    }

    public boolean b(String str) {
        if (g(str) != null) {
            return true;
        }
        return false;
    }

    public void c(String str) {
        g g = g(str);
        if (g != null) {
            d(g);
            a(g);
            return;
        }
        if (this.k != null) {
            this.k.c(g);
        }
    }

    public void a(final g gVar) {
        if (this.g == null) {
            this.g = new o(this.i);
        }
        if (this.o == null) {
            this.o = Executors.newSingleThreadExecutor();
        }
        this.o.execute(new Runnable(this) {
            final /* synthetic */ i b;

            public void run() {
                if (gVar.c().equals(gVar.a)) {
                    this.b.k.c(gVar);
                    return;
                }
                if (gVar.getState() == 7 || gVar.getState() == -1) {
                    this.b.g.a(gVar);
                } else {
                    this.b.g.a(gVar);
                    this.b.k.c(gVar);
                }
            }
        });
    }

    public void b(g gVar) {
        try {
            this.l.a(gVar, this.i, null);
        } catch (bk e) {
            e.printStackTrace();
        }
    }

    public void c(g gVar) {
        this.f.a(gVar);
        Message obtainMessage = this.e.obtainMessage();
        obtainMessage.obj = gVar;
        this.e.sendMessage(obtainMessage);
    }

    public void b() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            g gVar = (g) it.next();
            if (gVar.c().equals(gVar.c) || gVar.c().equals(gVar.b)) {
                gVar.f();
            }
        }
    }

    public void c() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            g gVar = (g) it.next();
            if (gVar.c().equals(gVar.c)) {
                gVar.f();
                return;
            }
        }
    }

    public void d() {
        if (!(this.n == null || this.n.isShutdown())) {
            this.n.shutdownNow();
        }
        if (this.h != null) {
            if (this.h.isAlive()) {
                this.h.interrupt();
            }
            this.h = null;
        }
        this.l.b();
        this.f.g();
        e();
        j = null;
        b = true;
    }

    private g g(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            g gVar = (g) it.next();
            if (str.equals(gVar.getCity())) {
                return gVar;
            }
        }
        return null;
    }

    private g h(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            g gVar = (g) it.next();
            if (str.equals(gVar.getCode())) {
                return gVar;
            }
        }
        return null;
    }

    public void d(String str) throws AMapException {
        g g = g(str);
        g.setVersion(d);
        if (g == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        g.f();
    }

    public void e(String str) throws AMapException {
        g h = h(str);
        if (h == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        h.f();
    }

    public void d(g gVar) {
        this.l.a((q) gVar);
    }

    public void e(g gVar) {
        this.l.b(gVar);
    }

    public void a(a aVar) {
        this.k = aVar;
    }

    public void e() {
        this.k = null;
    }
}
