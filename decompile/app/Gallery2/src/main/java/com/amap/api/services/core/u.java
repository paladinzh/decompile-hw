package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkRouteResult;

/* compiled from: WalkRouteSearchHandler */
public class u extends r<WalkRouteQuery, WalkRouteResult> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public u(Context context, WalkRouteQuery walkRouteQuery) {
        super(context, walkRouteQuery);
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(w.f(this.d));
        stringBuffer.append("&origin=").append(d.a(((WalkRouteQuery) this.a).getFromAndTo().getFrom()));
        stringBuffer.append("&destination=").append(d.a(((WalkRouteQuery) this.a).getFromAndTo().getTo()));
        stringBuffer.append("&multipath=0");
        stringBuffer.append("&output=json");
        return stringBuffer.toString();
    }

    protected WalkRouteResult a(String str) throws AMapException {
        return j.d(str);
    }

    public String b() {
        return c.a() + "/direction/walking?";
    }
}
