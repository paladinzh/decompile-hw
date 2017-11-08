package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: ReverseGeocodingHandler */
public class ab extends b<RegeocodeQuery, RegeocodeAddress> {
    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public ab(Context context, RegeocodeQuery regeocodeQuery) {
        super(context, regeocodeQuery);
    }

    public String g() {
        return h.a() + "/geocode/regeo?";
    }

    protected RegeocodeAddress d(String str) throws AMapException {
        RegeocodeAddress regeocodeAddress = new RegeocodeAddress();
        try {
            JSONObject optJSONObject = new JSONObject(str).optJSONObject("regeocode");
            if (optJSONObject == null) {
                return regeocodeAddress;
            }
            regeocodeAddress.setFormatAddress(n.a(optJSONObject, "formatted_address"));
            JSONObject optJSONObject2 = optJSONObject.optJSONObject("addressComponent");
            if (optJSONObject2 != null) {
                n.a(optJSONObject2, regeocodeAddress);
            }
            regeocodeAddress.setPois(n.c(optJSONObject));
            JSONArray optJSONArray = optJSONObject.optJSONArray("roads");
            if (optJSONArray != null) {
                n.b(optJSONArray, regeocodeAddress);
            }
            optJSONArray = optJSONObject.optJSONArray("roadinters");
            if (optJSONArray != null) {
                n.a(optJSONArray, regeocodeAddress);
            }
            JSONArray optJSONArray2 = optJSONObject.optJSONArray("aois");
            if (optJSONArray2 != null) {
                n.c(optJSONArray2, regeocodeAddress);
            }
            return regeocodeAddress;
        } catch (Throwable e) {
            i.a(e, "ReverseGeocodingHandler", "paseJSON");
        }
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json").append("&extensions=all").append("&location=").append(((RegeocodeQuery) this.a).getPoint().getLongitude()).append(",").append(((RegeocodeQuery) this.a).getPoint().getLatitude());
        stringBuffer.append("&radius=").append(((RegeocodeQuery) this.a).getRadius());
        stringBuffer.append("&coordsys=").append(((RegeocodeQuery) this.a).getLatLonType());
        stringBuffer.append("&key=" + aj.f(this.d));
        stringBuffer.append("&language=").append(h.c());
        return stringBuffer.toString();
    }
}
