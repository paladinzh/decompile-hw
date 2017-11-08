package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkRouteResult;

/* compiled from: WalkRouteSearchHandler */
public class ad extends b<WalkRouteQuery, WalkRouteResult> {
    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public ad(Context context, WalkRouteQuery walkRouteQuery) {
        super(context, walkRouteQuery);
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(aj.f(this.d));
        stringBuffer.append("&origin=").append(i.a(((WalkRouteQuery) this.a).getFromAndTo().getFrom()));
        stringBuffer.append("&destination=").append(i.a(((WalkRouteQuery) this.a).getFromAndTo().getTo()));
        stringBuffer.append("&multipath=0");
        stringBuffer.append("&output=json");
        return stringBuffer.toString();
    }

    protected WalkRouteResult d(String str) throws AMapException {
        return n.c(str);
    }

    public String g() {
        return h.a() + "/direction/walking?";
    }
}
