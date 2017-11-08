package com.amap.api.maps.offlinemap;

import android.content.Context;
import android.os.Handler;
import com.amap.api.mapcore.util.aw;
import com.amap.api.mapcore.util.ax;
import com.amap.api.mapcore.util.ax.a;
import com.amap.api.mapcore.util.bb;
import com.amap.api.mapcore.util.eh;
import com.amap.api.mapcore.util.fo;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import java.util.ArrayList;
import java.util.Iterator;

public final class OfflineMapManager {
    bb a;
    ax b;
    private Context c;
    private OfflineMapDownloadListener d;
    private Handler e = new Handler(this.c.getMainLooper());
    private Handler f = new Handler(this.c.getMainLooper());

    public interface OfflineMapDownloadListener {
        void onCheckUpdate(boolean z, String str);

        void onDownload(int i, int i2, String str);

        void onRemove(boolean z, String str, String str2);
    }

    public OfflineMapManager(Context context, OfflineMapDownloadListener offlineMapDownloadListener) {
        this.d = offlineMapDownloadListener;
        this.c = context.getApplicationContext();
        a(context);
    }

    public OfflineMapManager(Context context, OfflineMapDownloadListener offlineMapDownloadListener, AMap aMap) {
        this.d = offlineMapDownloadListener;
        this.c = context.getApplicationContext();
        a(context);
    }

    private void a(Context context) {
        this.c = context.getApplicationContext();
        ax.b = false;
        this.b = ax.a(this.c);
        this.b.a(new a(this) {
            final /* synthetic */ OfflineMapManager a;

            {
                this.a = r1;
            }

            public void a(final aw awVar) {
                if (this.a.d != null && awVar != null) {
                    this.a.e.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 b;

                        public void run() {
                            this.b.a.d.onDownload(awVar.c().b(), awVar.getcompleteCode(), awVar.getCity());
                        }
                    });
                }
            }

            public void b(final aw awVar) {
                if (this.a.d != null && awVar != null) {
                    this.a.e.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 b;

                        public void run() {
                            if (awVar.c().equals(awVar.g) || awVar.c().equals(awVar.a)) {
                                this.b.a.d.onCheckUpdate(true, awVar.getCity());
                            } else {
                                this.b.a.d.onCheckUpdate(false, awVar.getCity());
                            }
                        }
                    });
                }
            }

            public void c(final aw awVar) {
                if (this.a.d != null && awVar != null) {
                    this.a.e.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 b;

                        public void run() {
                            if (awVar.c().equals(awVar.a)) {
                                this.b.a.d.onRemove(true, awVar.getCity(), "");
                            } else {
                                this.b.a.d.onRemove(false, awVar.getCity(), "");
                            }
                        }
                    });
                }
            }

            public void a() {
            }
        });
        this.b.a();
        this.a = this.b.f;
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
                    this.f.post(new Runnable(this) {
                        final /* synthetic */ OfflineMapManager b;

                        public void run() {
                            try {
                                this.b.b.d(city);
                            } catch (Throwable e) {
                                fo.b(e, "OfflineMapManager", "downloadByProvinceName");
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
                fo.b(th, "OfflineMapManager", "downloadByProvinceName");
            }
        }
    }

    public void remove(String str) {
        if (this.b.b(str)) {
            this.b.c(str);
        } else {
            OfflineMapProvince c = this.a.c(str);
            if (c == null || c.getCityList() == null) {
                if (this.d != null) {
                    this.d.onRemove(false, str, "没有该城市");
                }
                return;
            }
            Iterator it = c.getCityList().iterator();
            while (it.hasNext()) {
                final String city = ((OfflineMapCity) it.next()).getCity();
                this.f.post(new Runnable(this) {
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
        if (!eh.c(this.c)) {
            throw new AMapException("http连接失败 - ConnectionException");
        }
    }

    public void restart() {
    }

    public void stop() {
        this.b.c();
    }

    public void pause() {
        this.b.d();
    }

    public void destroy() {
        if (this.b != null) {
            this.b.e();
        }
        b();
        if (this.e != null) {
            this.e.removeCallbacksAndMessages(null);
        }
        this.e = null;
        if (this.f != null) {
            this.f.removeCallbacksAndMessages(null);
        }
        this.f = null;
    }

    private void b() {
        this.d = null;
    }
}
