package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;

/* compiled from: BusRouteSearchHandler */
public class c extends b<BusRouteQuery, BusRouteResult> {
    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public c(Context context, BusRouteQuery busRouteQuery) {
        super(context, busRouteQuery);
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(aj.f(this.d));
        stringBuffer.append("&origin=").append(i.a(((BusRouteQuery) this.a).getFromAndTo().getFrom()));
        stringBuffer.append("&destination=").append(i.a(((BusRouteQuery) this.a).getFromAndTo().getTo()));
        String city = ((BusRouteQuery) this.a).getCity();
        if (!n.i(city)) {
            stringBuffer.append("&city=").append(b(city));
        }
        stringBuffer.append("&strategy=").append("" + ((BusRouteQuery) this.a).getMode());
        stringBuffer.append("&nightflag=").append(((BusRouteQuery) this.a).getNightFlag());
        stringBuffer.append("&output=json");
        return stringBuffer.toString();
    }

    protected BusRouteResult d(String str) throws AMapException {
        return n.a(str);
    }

    public String g() {
        return h.a() + "/direction/transit/integrated?";
    }
}
