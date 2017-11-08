package com.amap.api.services.geocoder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.ab;
import com.amap.api.services.core.l;
import com.amap.api.services.core.q;
import com.amap.api.services.core.t;
import com.amap.api.services.core.t.e;
import com.amap.api.services.core.t.i;
import java.util.List;

public final class GeocodeSearch {
    public static final String AMAP = "autonavi";
    public static final String GPS = "gps";
    private Context a;
    private OnGeocodeSearchListener b;
    private Handler c = t.a();

    public interface OnGeocodeSearchListener {
        void onGeocodeSearched(GeocodeResult geocodeResult, int i);

        void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i);
    }

    public GeocodeSearch(Context context) {
        this.a = context.getApplicationContext();
    }

    public RegeocodeAddress getFromLocation(RegeocodeQuery regeocodeQuery) throws AMapException {
        q.a(this.a);
        return (RegeocodeAddress) new ab(this.a, regeocodeQuery).a();
    }

    public List<GeocodeAddress> getFromLocationName(GeocodeQuery geocodeQuery) throws AMapException {
        q.a(this.a);
        return (List) new l(this.a, geocodeQuery).a();
    }

    public void setOnGeocodeSearchListener(OnGeocodeSearchListener onGeocodeSearchListener) {
        this.b = onGeocodeSearchListener;
    }

    public void getFromLocationAsyn(final RegeocodeQuery regeocodeQuery) {
        new Thread(new Runnable(this) {
            final /* synthetic */ GeocodeSearch b;

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                try {
                    obtainMessage.arg1 = 2;
                    obtainMessage.what = 201;
                    i iVar = new i();
                    iVar.b = this.b.b;
                    obtainMessage.obj = iVar;
                    iVar.a = new RegeocodeResult(regeocodeQuery, this.b.getFromLocation(regeocodeQuery));
                    obtainMessage.arg2 = 1000;
                } catch (Throwable e) {
                    com.amap.api.services.core.i.a(e, "GeocodeSearch", "getFromLocationAsyn");
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
                Message obtainMessage = t.a().obtainMessage();
                try {
                    obtainMessage.what = SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE;
                    obtainMessage.arg1 = 2;
                    obtainMessage.arg2 = 1000;
                    e eVar = new e();
                    eVar.b = this.b.b;
                    obtainMessage.obj = eVar;
                    eVar.a = new GeocodeResult(geocodeQuery, this.b.getFromLocationName(geocodeQuery));
                } catch (Throwable e) {
                    com.amap.api.services.core.i.a(e, "GeocodeSearch", "getFromLocationNameAsyn");
                    obtainMessage.arg2 = e.getErrorCode();
                } finally {
                    this.b.c.sendMessage(obtainMessage);
                }
            }
        }).start();
    }
}
