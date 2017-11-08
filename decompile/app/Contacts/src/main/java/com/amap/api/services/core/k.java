package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;

/* compiled from: DriveRouteSearchHandler */
public class k extends b<DriveRouteQuery, DriveRouteResult> {
    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public k(Context context, DriveRouteQuery driveRouteQuery) {
        super(context, driveRouteQuery);
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(aj.f(this.d));
        stringBuffer.append("&origin=").append(i.a(((DriveRouteQuery) this.a).getFromAndTo().getFrom()));
        if (!n.i(((DriveRouteQuery) this.a).getFromAndTo().getStartPoiID())) {
            stringBuffer.append("&originid=").append(((DriveRouteQuery) this.a).getFromAndTo().getStartPoiID());
        }
        stringBuffer.append("&destination=").append(i.a(((DriveRouteQuery) this.a).getFromAndTo().getTo()));
        if (!n.i(((DriveRouteQuery) this.a).getFromAndTo().getDestinationPoiID())) {
            stringBuffer.append("&destinationid=").append(((DriveRouteQuery) this.a).getFromAndTo().getDestinationPoiID());
        }
        stringBuffer.append("&strategy=").append("" + ((DriveRouteQuery) this.a).getMode());
        stringBuffer.append("&extensions=all");
        if (((DriveRouteQuery) this.a).hasPassPoint()) {
            stringBuffer.append("&waypoints=").append(((DriveRouteQuery) this.a).getPassedPointStr());
        }
        if (((DriveRouteQuery) this.a).hasAvoidpolygons()) {
            stringBuffer.append("&avoidpolygons=").append(((DriveRouteQuery) this.a).getAvoidpolygonsStr());
        }
        if (((DriveRouteQuery) this.a).hasAvoidRoad()) {
            stringBuffer.append("&avoidroad=").append(b(((DriveRouteQuery) this.a).getAvoidRoad()));
        }
        stringBuffer.append("&roadaggregation=true");
        stringBuffer.append("&output=json");
        return stringBuffer.toString();
    }

    protected DriveRouteResult d(String str) throws AMapException {
        return n.b(str);
    }

    public String g() {
        return h.a() + "/direction/driving?";
    }
}
