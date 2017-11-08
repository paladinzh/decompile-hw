package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import java.util.ArrayList;
import org.json.JSONObject;

/* compiled from: GeocodingHandler */
public class g extends r<GeocodeQuery, ArrayList<GeocodeAddress>> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public g(Context context, GeocodeQuery geocodeQuery) {
        super(context, geocodeQuery);
    }

    protected ArrayList<GeocodeAddress> a(String str) throws AMapException {
        ArrayList<GeocodeAddress> arrayList = new ArrayList();
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (!jSONObject.has("count")) {
                return arrayList;
            }
            if (jSONObject.getInt("count") > 0) {
                arrayList = j.n(jSONObject);
            }
            return arrayList;
        } catch (Throwable e) {
            d.a(e, "GeocodingHandler", "paseJSONJSONException");
        } catch (Throwable e2) {
            d.a(e2, "GeocodingHandler", "paseJSONException");
        }
    }

    public String b() {
        return c.a() + "/geocode/geo?";
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json").append("&address=").append(c(((GeocodeQuery) this.a).getLocationName()));
        String city = ((GeocodeQuery) this.a).getCity();
        if (!j.h(city)) {
            stringBuffer.append("&city=").append(c(city));
        }
        stringBuffer.append("&key=" + w.f(this.d));
        stringBuffer.append("&language=").append(c.b());
        return stringBuffer.toString();
    }
}
