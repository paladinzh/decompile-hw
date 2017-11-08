package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: ReverseGeocodingHandler */
public class t extends r<RegeocodeQuery, RegeocodeAddress> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public t(Context context, RegeocodeQuery regeocodeQuery) {
        super(context, regeocodeQuery);
    }

    public String b() {
        return c.a() + "/geocode/regeo?";
    }

    protected RegeocodeAddress a(String str) throws AMapException {
        RegeocodeAddress regeocodeAddress = new RegeocodeAddress();
        try {
            JSONObject optJSONObject = new JSONObject(str).optJSONObject("regeocode");
            if (optJSONObject == null) {
                return regeocodeAddress;
            }
            regeocodeAddress.setFormatAddress(j.b(optJSONObject, "formatted_address"));
            JSONObject optJSONObject2 = optJSONObject.optJSONObject("addressComponent");
            if (optJSONObject2 != null) {
                j.a(optJSONObject2, regeocodeAddress);
            }
            regeocodeAddress.setPois(j.c(optJSONObject));
            JSONArray optJSONArray = optJSONObject.optJSONArray("roads");
            if (optJSONArray != null) {
                j.b(optJSONArray, regeocodeAddress);
            }
            JSONArray optJSONArray2 = optJSONObject.optJSONArray("roadinters");
            if (optJSONArray2 != null) {
                j.a(optJSONArray2, regeocodeAddress);
            }
            return regeocodeAddress;
        } catch (Throwable e) {
            d.a(e, "ReverseGeocodingHandler", "paseJSON");
        }
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json").append("&extensions=all").append("&location=").append(((RegeocodeQuery) this.a).getPoint().getLongitude()).append(",").append(((RegeocodeQuery) this.a).getPoint().getLatitude());
        stringBuffer.append("&radius=").append(((RegeocodeQuery) this.a).getRadius());
        stringBuffer.append("&coordsys=").append(((RegeocodeQuery) this.a).getLatLonType());
        stringBuffer.append("&key=" + w.f(this.d));
        stringBuffer.append("&language=").append(c.b());
        return stringBuffer.toString();
    }
}
