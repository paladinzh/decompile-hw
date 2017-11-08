package com.amap.api.maps.offlinemap;

import android.content.Context;
import android.os.Handler;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.mapcore.util.g;
import com.amap.api.mapcore.util.i;
import com.amap.api.mapcore.util.i.a;
import com.amap.api.mapcore.util.m;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import java.util.ArrayList;
import java.util.Iterator;

public final class OfflineMapManager {
    m a;
    i b;
    private Context c;
    private AMap d;
    private OfflineMapDownloadListener e;
    private Handler f = new Handler();
    private Handler g = new Handler();

    public interface OfflineMapDownloadListener {
        void onCheckUpdate(boolean z, String str);

        void onDownload(int i, int i2, String str);

        void onRemove(boolean z, String str, String str2);
    }

    public OfflineMapManager(Context context, OfflineMapDownloadListener offlineMapDownloadListener) {
        this.e = offlineMapDownloadListener;
        a(context);
    }

    public OfflineMapManager(Context context, OfflineMapDownloadListener offlineMapDownloadListener, AMap aMap) {
        this.e = offlineMapDownloadListener;
        this.d = aMap;
        a(context);
    }

    private void a(Context context) {
        this.c = context.getApplicationContext();
        i.b = false;
        this.b = i.a(context);
        this.a = this.b.f;
        this.b.a(new a(this) {
            final /* synthetic */ OfflineMapManager a;

            {
                this.a = r1;
            }

            public void a(final g gVar) {
                if (!(this.a.e == null || gVar == null)) {
                    this.a.f.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 b;

                        public void run() {
                            this.b.a.e.onDownload(gVar.c().b(), gVar.getcompleteCode(), gVar.getCity());
                        }
                    });
                }
                if (this.a.d != null && gVar.c().a(gVar.f)) {
                    this.a.d.setLoadOfflineData(false);
                    this.a.d.setLoadOfflineData(true);
                }
            }

            public void b(final g gVar) {
                if (this.a.e != null && gVar != null) {
                    this.a.f.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 b;

                        public void run() {
                            if (gVar.c().equals(gVar.g)) {
                                this.b.a.e.onCheckUpdate(true, gVar.getCity());
                            } else {
                                this.b.a.e.onCheckUpdate(false, gVar.getCity());
                            }
                        }
                    });
                }
            }

            public void c(final g gVar) {
                if (this.a.e != null && gVar != null) {
                    this.a.f.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 b;

                        public void run() {
                            if (gVar.c().equals(gVar.a)) {
                                this.b.a.e.onRemove(true, gVar.getCity(), "");
                            } else {
                                this.b.a.e.onRemove(false, gVar.getCity(), "");
                            }
                        }
                    });
                }
            }
        });
    }

    public void downloadByCityCode(String str) throws AMapException {
        this.b.e(str);
    }

    public void downloadByCityName(String str) throws AMapException {
        this.b.d(str);
    }

    public void downloadByProvinceName(String str) throws AMapException {
        try {
            a();
            OfflineMapProvince itemByProvinceName = getItemByProvinceName(str);
            if (itemByProvinceName != null) {
                Iterator it = itemByProvinceName.getCityList().iterator();
                while (it.hasNext()) {
                    final String city = ((OfflineMapCity) it.next()).getCity();
                    this.g.post(new Runnable(this) {
                        final /* synthetic */ OfflineMapManager b;

                        public void run() {
                            try {
                                this.b.b.d(city);
                            } catch (Throwable e) {
                                ce.a(e, "OfflineMapManager", "downloadByProvinceName");
                            }
                        }
                    });
                }
                return;
            }
            throw new AMapException("无效的参数 - IllegalArgumentException");
        } catch (Throwable th) {
            if (th instanceof AMapException) {
                AMapException aMapException = (AMapException) th;
            } else {
                ce.a(th, "OfflineMapManager", "downloadByProvinceName");
            }
        }
    }

    public void remove(String str) {
        if (this.b.b(str)) {
            this.b.c(str);
        } else {
            OfflineMapProvince c = this.a.c(str);
            if (c == null || c.getCityList() == null) {
                if (this.e != null) {
                    this.e.onRemove(false, str, "没有该城市");
                }
                return;
            }
            Iterator it = c.getCityList().iterator();
            while (it.hasNext()) {
                final String city = ((OfflineMapCity) it.next()).getCity();
                this.g.post(new Runnable(this) {
                    final /* synthetic */ OfflineMapManager b;

                    public void run() {
                        this.b.b.c(city);
                    }
                });
            }
        }
    }

    public ArrayList<OfflineMapProvince> getOfflineMapProvinceList() {
        return this.a.a();
    }

    public OfflineMapCity getItemByCityCode(String str) {
        return this.a.a(str);
    }

    public OfflineMapCity getItemByCityName(String str) {
        return this.a.b(str);
    }

    public OfflineMapProvince getItemByProvinceName(String str) {
        return this.a.c(str);
    }

    public ArrayList<OfflineMapCity> getOfflineMapCityList() {
        return this.a.b();
    }

    public ArrayList<OfflineMapCity> getDownloadingCityList() {
        return this.a.e();
    }

    public ArrayList<OfflineMapProvince> getDownloadingProvinceList() {
        return this.a.f();
    }

    public ArrayList<OfflineMapCity> getDownloadOfflineMapCityList() {
        return this.a.c();
    }

    public ArrayList<OfflineMapProvince> getDownloadOfflineMapProvinceList() {
        return this.a.d();
    }

    private void a(String str, String str2) throws AMapException {
        this.b.a(str);
    }

    public void updateOfflineCityByCode(String str) throws AMapException {
        OfflineMapCity itemByCityCode = getItemByCityCode(str);
        if (itemByCityCode == null || itemByCityCode.getCity() == null) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        }
        a(itemByCityCode.getCity(), "cityname");
    }

    public void updateOfflineCityByName(String str) throws AMapException {
        a(str, "cityname");
    }

    public void updateOfflineMapProvinceByName(String str) throws AMapException {
        a(str, "cityname");
    }

    private void a() throws AMapException {
        if (!bj.c(this.c)) {
            throw new AMapException(AMapException.ERROR_CONNECTION);
        }
    }

    public void restart() {
    }

    public void stop() {
        this.b.b();
    }

    public void pause() {
        this.b.c();
    }

    public void destroy() {
        this.b.d();
        b();
        this.d = null;
        this.f.removeCallbacksAndMessages(null);
        this.f = null;
        this.g.removeCallbacksAndMessages(null);
        this.g = null;
    }

    private void b() {
        this.e = null;
    }
}
