package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.nearby.NearbySearch.NearbyQuery;
import com.amap.api.services.nearby.NearbySearchResult;
import java.util.List;
import org.json.JSONObject;

/* compiled from: NearbySearchHandler */
public class v extends b<NearbyQuery, NearbySearchResult> {
    private Context h;
    private NearbyQuery i;

    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public v(Context context, NearbyQuery nearbyQuery) {
        super(context, nearbyQuery);
        this.h = context;
        this.i = nearbyQuery;
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(aj.f(this.h));
        LatLonPoint centerPoint = this.i.getCenterPoint();
        stringBuffer.append("&center=").append(centerPoint.getLongitude()).append(",").append(centerPoint.getLatitude());
        stringBuffer.append("&radius=").append(this.i.getRadius());
        stringBuffer.append("&searchtype=").append(this.i.getType());
        stringBuffer.append("&timerange=").append(this.i.getTimeRange());
        return stringBuffer.toString();
    }

    protected NearbySearchResult d(String str) throws AMapException {
        boolean z = true;
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (this.i.getType() != 1) {
                z = false;
            }
            List a = n.a(jSONObject, z);
            NearbySearchResult nearbySearchResult = new NearbySearchResult();
            nearbySearchResult.setNearbyInfoList(a);
            return nearbySearchResult;
        } catch (Throwable e) {
            i.a(e, "NearbySearchHandler", "paseJSON");
            return null;
        }
    }

    public String g() {
        return h.b() + "/nearby/around";
    }
}
