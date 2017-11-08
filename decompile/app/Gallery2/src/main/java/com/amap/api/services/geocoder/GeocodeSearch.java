package com.amap.api.services.geocoder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.d;
import com.amap.api.services.core.g;
import com.amap.api.services.core.l;
import com.amap.api.services.core.p;
import com.amap.api.services.core.p.c;
import com.amap.api.services.core.p.f;
import com.amap.api.services.core.t;
import java.util.List;

public final class GeocodeSearch {
    public static final String AMAP = "autonavi";
    public static final String GPS = "gps";
    private Context a;
    private OnGeocodeSearchListener b;
    private Handler c = p.a();

    public interface OnGeocodeSearchListener {
        void onGeocodeSearched(GeocodeResult geocodeResult, int i);

        void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i);
    }

    public GeocodeSearch(Context context) {
        this.a = context.getApplicationContext();
    }

    public RegeocodeAddress getFromLocation(RegeocodeQuery regeocodeQuery) throws AMapException {
        l.a(this.a);
        return (RegeocodeAddress) new t(this.a, regeocodeQuery).g();
    }

    public List<GeocodeAddress> getFromLocationName(GeocodeQuery geocodeQuery) throws AMapException {
        l.a(this.a);
        return (List) new g(this.a, geocodeQuery).g();
    }

    public void setOnGeocodeSearchListener(OnGeocodeSearchListener onGeocodeSearchListener) {
        this.b = onGeocodeSearchListener;
    }

    public void getFromLocationAsyn(final RegeocodeQuery regeocodeQuery) {
        new Thread(new Runnable(this) {
            final /* synthetic */ GeocodeSearch b;

            public void run() {
                Message obtainMessage = p.a().obtainMessage();
                try {
                    obtainMessage.arg1 = 2;
                    obtainMessage.what = 21;
                    RegeocodeAddress fromLocation = this.b.getFromLocation(regeocodeQuery);
                    obtainMessage.arg2 = 0;
                    f fVar = new f();
                    fVar.b = this.b.b;
                    fVar.a = new RegeocodeResult(regeocodeQuery, fromLocation);
                    obtainMessage.obj = fVar;
                } catch (Throwable e) {
                    d.a(e, "GeocodeSearch", "getFromLocationAsyn");
                    obtainMessage.arg2 = e.getErrorCode();
                } finally {
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }).start();
    }

    public void getFromLocationNameAsyn(final GeocodeQuery geocodeQuery) {
        new Thread(new Runnable(this) {
            final /* synthetic */ GeocodeSearch b;

            public void run() {
                Message obtainMessage = p.a().obtainMessage();
                try {
                    obtainMessage.what = 20;
                    obtainMessage.arg1 = 2;
                    List fromLocationName = this.b.getFromLocationName(geocodeQuery);
                    obtainMessage.arg2 = 0;
                    c cVar = new c();
                    cVar.b = this.b.b;
                    cVar.a = new GeocodeResult(geocodeQuery, fromLocationName);
                    obtainMessage.obj = cVar;
                } catch (Throwable e) {
                    d.a(e, "GeocodeSearch", "getFromLocationNameAsyn");
                    obtainMessage.arg2 = e.getErrorCode();
                } finally {
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }).start();
    }
}
