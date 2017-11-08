package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;

/* compiled from: BusRouteSearchHandler */
public class a extends r<BusRouteQuery, BusRouteResult> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public a(Context context, BusRouteQuery busRouteQuery) {
        super(context, busRouteQuery);
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(w.f(this.d));
        stringBuffer.append("&origin=").append(d.a(((BusRouteQuery) this.a).getFromAndTo().getFrom()));
        stringBuffer.append("&destination=").append(d.a(((BusRouteQuery) this.a).getFromAndTo().getTo()));
        String city = ((BusRouteQuery) this.a).getCity();
        if (!j.h(city)) {
            stringBuffer.append("&city=").append(c(city));
        }
        stringBuffer.append("&strategy=").append("" + ((BusRouteQuery) this.a).getMode());
        stringBuffer.append("&nightflag=").append(((BusRouteQuery) this.a).getNightFlag());
        stringBuffer.append("&output=json");
        return stringBuffer.toString();
    }

    protected BusRouteResult a(String str) throws AMapException {
        return j.b(str);
    }

    public String b() {
        return c.a() + "/direction/transit/integrated?";
    }
}
