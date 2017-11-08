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
public class ax {
    public static String a = "";
    public static boolean b = false;
    public static String d = "";
    private static volatile ax k;
    CopyOnWriteArrayList<aw> c = new CopyOnWriteArrayList();
    b e = null;
    public bb f;
    bd g;
    ba h = null;
    private Context i;
    private boolean j = true;
    private a l;
    private bg m;
    private bm n;
    private ExecutorService o = null;
    private ExecutorService p = null;
    private ExecutorService q = null;
    private boolean r = true;

    /* compiled from: OfflineDownloadManager */
    public interface a {
        void a();

        void a(aw awVar);

        void b(aw awVar);

        void c(aw awVar);
    }

    /* compiled from: OfflineDownloadManager */
    class b extends Handler {
        final /* synthetic */ ax a;

        public b(ax axVar, Looper looper) {
            this.a = axVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            try {
                message.getData();
                Object obj = message.obj;
                if (obj instanceof aw) {
                    aw awVar = (aw) obj;
                    bu.a("OfflineMapHandler handleMessage CitObj  name: " + awVar.getCity() + " complete: " + awVar.getcompleteCode() + " status: " + awVar.getState());
                    if (this.a.l != null) {
                        this.a.l.a(awVar);
                        return;
                    }
                    return;
                }
                bu.a("Do not callback by CityObject! ");
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private ax(Context context) {
        this.i = context;
    }

    public static ax a(Context context) {
        if (k == null) {
            synchronized (ax.class) {
                if (k == null) {
                    if (!b) {
                        k = new ax(context.getApplicationContext());
                    }
                }
            }
        }
        return k;
    }

    public void a() {
        this.n = bm.a(this.i.getApplicationContext());
        g();
        this.e = new b(this, this.i.getMainLooper());
        this.f = new bb(this.i, this.e);
        this.m = bg.a(1);
        a = eh.b(this.i);
        h();
        Iterator it = this.f.a().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((OfflineMapProvince) it.next()).getCityList().iterator();
            while (it2.hasNext()) {
                OfflineMapCity offlineMapCity = (OfflineMapCity) it2.next();
                if (offlineMapCity != null) {
                    this.c.add(new aw(this.i, offlineMapCity));
                }
            }
        }
        this.h = new ba(this.i);
        this.h.start();
    }

    private void g() {
        try {
            String str = "000001";
            bh a = this.n.a(str);
            if (a != null) {
                this.n.c(str);
                a.c("100000");
                this.n.a(a);
            }
        } catch (Throwable th) {
            fo.b(th, "OfflineDownloadManager", "changeBadCase");
        }
    }

    private void h() {
        String str = null;
        if (!eh.b(this.i).equals("")) {
            File file = new File(eh.b(this.i) + "offlinemapv4.png");
            if (file.exists()) {
                str = bu.c(file);
                if (str != null) {
                    try {
                        g(str);
                    } catch (Throwable e) {
                        if (file.exists()) {
                            file.delete();
                        }
                        fo.b(e, "MapDownloadManager", "paseJson io");
                        e.printStackTrace();
                    }
                }
            }
            if (!file.exists() || this.f.i() == 0 || r0 == null) {
                str = bu.a(this.i, "offlinemapv4.png");
                if (file.exists()) {
                    file.delete();
                }
                if (str != null) {
                    try {
                        g(str);
                    } catch (Throwable e2) {
                        if (file.exists()) {
                            file.delete();
                        }
                        fo.b(e2, "MapDownloadManager", "paseJson io");
                        e2.printStackTrace();
                    }
                }
            }
        }
    }

    private void g(String str) throws JSONException {
        List a = bu.a(str, this.i.getApplicationContext());
        if (a != null && a.size() != 0) {
            this.f.a(a);
        }
    }

    private void i() {
        Iterator it = this.n.a().iterator();
        while (it.hasNext()) {
            bh bhVar = (bh) it.next();
            if (!(bhVar == null || bhVar.d() == null || bhVar.f().length() < 1)) {
                if (!(bhVar.l == 4 || bhVar.l == 7 || bhVar.l < 0)) {
                    bhVar.l = 3;
                }
                aw h = h(bhVar.d());
                if (h != null) {
                    String e = bhVar.e();
                    if (e != null && a(d, e)) {
                        h.a(7);
                    } else {
                        h.a(bhVar.l);
                        h.setCompleteCode(bhVar.g());
                    }
                    if (bhVar.e().length() > 0) {
                        h.setVersion(bhVar.e());
                    }
                    List<String> b = this.n.b(bhVar.f());
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String append : b) {
                        stringBuffer.append(append);
                        stringBuffer.append(";");
                    }
                    h.a(stringBuffer.toString());
                    this.f.a(h);
                }
            }
        }
    }

    public void a(ArrayList<bh> arrayList) {
        i();
        if (this.l != null) {
            try {
                this.l.a();
            } catch (Throwable th) {
                fo.b(th, "OfflineDownloadManager", "verifyCallBack");
            }
        }
    }

    public void a(final String str) {
        if (str != null) {
            try {
                if (this.o == null) {
                    this.o = Executors.newSingleThreadExecutor();
                }
                this.o.execute(new Runnable(this) {
                    final /* synthetic */ ax b;

                    public void run() {
                        aw a = this.b.h(str);
                        if (a != null) {
                            if (!a.c().equals(a.c)) {
                                if (!a.c().equals(a.e)) {
                                    String pinyin = a.getPinyin();
                                    if (pinyin.length() > 0) {
                                        pinyin = this.b.n.d(pinyin);
                                        if (pinyin == null) {
                                            pinyin = a.getVersion();
                                        }
                                        if (ax.d.length() > 0 && r0 != null && this.b.a(ax.d, r0)) {
                                            a.j();
                                        }
                                    }
                                }
                            }
                            if (this.b.l != null) {
                                synchronized (this.b) {
                                    try {
                                        this.b.l.b(a);
                                    } catch (Throwable th) {
                                        fo.b(th, "OfflineDownloadManager", "checkUpdatefinally");
                                    }
                                }
                            }
                            return;
                        }
                        try {
                            this.b.j();
                            ay ayVar = (ay) new az(this.b.i, ax.d).c();
                            if (this.b.l != null) {
                                if (ayVar == null) {
                                    if (this.b.l != null) {
                                        synchronized (this.b) {
                                            try {
                                                this.b.l.b(a);
                                            } catch (Throwable th2) {
                                                fo.b(th2, "OfflineDownloadManager", "checkUpdatefinally");
                                            }
                                        }
                                    }
                                    return;
                                } else if (ayVar.a()) {
                                    this.b.b();
                                }
                            }
                            if (this.b.l != null) {
                                synchronized (this.b) {
                                    try {
                                        this.b.l.b(a);
                                    } catch (Throwable th22) {
                                        fo.b(th22, "OfflineDownloadManager", "checkUpdatefinally");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            if (this.b.l != null) {
                                synchronized (this.b) {
                                    this.b.l.b(a);
                                }
                            }
                        } catch (Throwable th3) {
                            fo.b(th3, "OfflineDownloadManager", "checkUpdatefinally");
                        }
                    }
                });
            } catch (Throwable th) {
                fo.b(th, "OfflineDownloadManager", "checkUpdate");
            }
            return;
        }
        if (this.l != null) {
            this.l.b(null);
        }
    }

    private void j() throws AMapException {
        if (!eh.c(this.i)) {
            throw new AMapException("http连接失败 - ConnectionException");
        }
    }

    protected void b() throws AMapException {
        be beVar = new be(this.i, "");
        beVar.a(this.i);
        List list = (List) beVar.c();
        if (this.c != null) {
            this.f.a(list);
        }
        Iterator it = this.f.a().iterator();
        while (it.hasNext()) {
            Iterator it2 = ((OfflineMapProvince) it.next()).getCityList().iterator();
            while (it2.hasNext()) {
                OfflineMapCity offlineMapCity = (OfflineMapCity) it2.next();
                Iterator it3 = this.c.iterator();
                while (it3.hasNext()) {
                    aw awVar = (aw) it3.next();
                    if (offlineMapCity.getPinyin().equals(awVar.getPinyin())) {
                        String version = awVar.getVersion();
                        if (awVar.getState() == 4 && d.length() > 0 && a(d, version)) {
                            awVar.j();
                            awVar.setUrl(offlineMapCity.getUrl());
                        } else {
                            awVar.setCity(offlineMapCity.getCity());
                            awVar.setUrl(offlineMapCity.getUrl());
                            awVar.setAdcode(offlineMapCity.getAdcode());
                            awVar.setVersion(offlineMapCity.getVersion());
                            awVar.setSize(offlineMapCity.getSize());
                            awVar.setCode(offlineMapCity.getCode());
                            awVar.setJianpin(offlineMapCity.getJianpin());
                            awVar.setPinyin(offlineMapCity.getPinyin());
                        }
                    }
                }
            }
        }
    }

    private boolean a(String str, String str2) {
        int i = 0;
        while (i < str2.length()) {
            try {
                if (str.charAt(i) > str2.charAt(i)) {
                    return true;
                }
                if (str.charAt(i) < str2.charAt(i)) {
                    return false;
                }
                i++;
            } catch (Throwable th) {
            }
        }
        return false;
    }

    public boolean b(String str) {
        if (h(str) != null) {
            return true;
        }
        return false;
    }

    public void c(String str) {
        aw h = h(str);
        if (h != null) {
            d(h);
            a(h, true);
            return;
        }
        if (this.l != null) {
            try {
                this.l.c(h);
            } catch (Throwable th) {
                fo.b(th, "OfflineDownloadManager", "remove");
            }
        }
    }

    public void a(aw awVar) {
        a(awVar, false);
    }

    private void a(final aw awVar, final boolean z) {
        if (this.g == null) {
            this.g = new bd(this.i);
        }
        if (this.p == null) {
            this.p = Executors.newSingleThreadExecutor();
        }
        try {
            this.p.execute(new Runnable(this) {
                final /* synthetic */ ax c;

                public void run() {
                    try {
                        if (awVar.c().equals(awVar.a)) {
                            if (this.c.l != null) {
                                this.c.l.c(awVar);
                            }
                            return;
                        }
                        if (awVar.getState() != 7) {
                            if (awVar.getState() != -1) {
                                this.c.g.a(awVar);
                                if (this.c.l != null) {
                                    this.c.l.c(awVar);
                                }
                            }
                        }
                        this.c.g.a(awVar);
                        if (z && this.c.l != null) {
                            this.c.l.c(awVar);
                        }
                    } catch (Throwable th) {
                        fo.b(th, "requestDelete", "removeExcecRunnable");
                    }
                }
            });
        } catch (Throwable th) {
            fo.b(th, "requestDelete", "removeExcecRunnable");
        }
    }

    public void b(aw awVar) {
        try {
            this.m.a(awVar, this.i, null);
        } catch (ex e) {
            e.printStackTrace();
        }
    }

    public void c(aw awVar) {
        this.f.a(awVar);
        if (this.e != null) {
            Message obtainMessage = this.e.obtainMessage();
            obtainMessage.obj = awVar;
            this.e.sendMessage(obtainMessage);
        }
    }

    public void c() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            aw awVar = (aw) it.next();
            if (awVar.c().equals(awVar.c) || awVar.c().equals(awVar.b)) {
                awVar.f();
            }
        }
    }

    public void d() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            aw awVar = (aw) it.next();
            if (awVar.c().equals(awVar.c)) {
                awVar.g();
                return;
            }
        }
    }

    public void e() {
        if (!(this.o == null || this.o.isShutdown())) {
            this.o.shutdownNow();
        }
        if (!(this.q == null || this.q.isShutdown())) {
            this.q.shutdownNow();
        }
        if (this.h != null) {
            if (this.h.isAlive()) {
                this.h.interrupt();
            }
            this.h = null;
        }
        if (this.e != null) {
            this.e.removeCallbacksAndMessages(null);
            this.e = null;
        }
        if (this.m != null) {
            this.m.b();
        }
        if (this.f != null) {
            this.f.g();
        }
        k = null;
        b = true;
        this.j = true;
        f();
    }

    private aw h(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            aw awVar = (aw) it.next();
            if (str.equals(awVar.getCity()) || str.equals(awVar.getPinyin())) {
                return awVar;
            }
        }
        return null;
    }

    private aw i(String str) {
        if (str == null || str.length() < 1) {
            return null;
        }
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            aw awVar = (aw) it.next();
            if (str.equals(awVar.getCode())) {
                return awVar;
            }
        }
        return null;
    }

    public void d(String str) throws AMapException {
        aw h = h(str);
        if (str == null || str.length() < 1 || h == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        f(h);
    }

    public void e(String str) throws AMapException {
        aw i = i(str);
        if (i == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        f(i);
    }

    private void f(final aw awVar) throws AMapException {
        j();
        if (awVar != null) {
            if (this.q == null) {
                this.q = Executors.newSingleThreadExecutor();
            }
            try {
                this.q.execute(new Runnable(this) {
                    final /* synthetic */ ax b;

                    public void run() {
                        try {
                            if (this.b.j) {
                                this.b.j();
                                ay ayVar = (ay) new az(this.b.i, ax.d).c();
                                if (ayVar != null) {
                                    this.b.j = false;
                                    if (ayVar.a()) {
                                        this.b.b();
                                    }
                                }
                            }
                            awVar.setVersion(ax.d);
                            awVar.f();
                        } catch (AMapException e) {
                            e.printStackTrace();
                        } catch (Throwable th) {
                            fo.b(th, "OfflineDownloadManager", "startDownloadRunnable");
                        }
                    }
                });
                return;
            } catch (Throwable th) {
                fo.b(th, "startDownload", "downloadExcecRunnable");
                return;
            }
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    public void d(aw awVar) {
        this.m.a((bf) awVar);
    }

    public void e(aw awVar) {
        this.m.b(awVar);
    }

    public void a(a aVar) {
        this.l = aVar;
    }

    public void f() {
        synchronized (this) {
            this.l = null;
        }
    }

    public String f(String str) {
        String str2 = "";
        if (str == null) {
            return str2;
        }
        aw h = h(str);
        if (h != null) {
            return h.getAdcode();
        }
        return str2;
    }
}
