package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import java.util.ArrayList;
import org.json.JSONObject;

/* compiled from: GeocodingHandler */
public class l extends b<GeocodeQuery, ArrayList<GeocodeAddress>> {
    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public l(Context context, GeocodeQuery geocodeQuery) {
        super(context, geocodeQuery);
    }

    protected ArrayList<GeocodeAddress> d(String str) throws AMapException {
        ArrayList<GeocodeAddress> arrayList = new ArrayList();
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("count")) {
                return arrayList;
            }
            if (jSONObject.getInt("count") > 0) {
                arrayList = n.l(jSONObject);
            }
            return arrayList;
        } catch (Throwable e) {
            i.a(e, "GeocodingHandler", "paseJSONJSONException");
        } catch (Throwable e2) {
            i.a(e2, "GeocodingHandler", "paseJSONException");
        }
    }

    public String g() {
        return h.a() + "/geocode/geo?";
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json").append("&address=").append(b(((GeocodeQuery) this.a).getLocationName()));
        String city = ((GeocodeQuery) this.a).getCity();
        if (!n.i(city)) {
            stringBuffer.append("&city=").append(b(city));
        }
        stringBuffer.append("&key=" + aj.f(this.d));
        stringBuffer.append("&language=").append(h.c());
        return stringBuffer.toString();
    }
}
