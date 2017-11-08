package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;

/* compiled from: DriveRouteSearchHandler */
public class f extends r<DriveRouteQuery, DriveRouteResult> {
    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public f(Context context, DriveRouteQuery driveRouteQuery) {
        super(context, driveRouteQuery);
    }

    protected String a_() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(w.f(this.d));
        stringBuffer.append("&origin=").append(d.a(((DriveRouteQuery) this.a).getFromAndTo().getFrom()));
        if (!j.h(((DriveRouteQuery) this.a).getFromAndTo().getStartPoiID())) {
            stringBuffer.append("&originid=").append(((DriveRouteQuery) this.a).getFromAndTo().getStartPoiID());
        }
        stringBuffer.append("&destination=").append(d.a(((DriveRouteQuery) this.a).getFromAndTo().getTo()));
        if (!j.h(((DriveRouteQuery) this.a).getFromAndTo().getDestinationPoiID())) {
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
            stringBuffer.append("&avoidroad=").append(c(((DriveRouteQuery) this.a).getAvoidRoad()));
        }
        stringBuffer.append("&output=json");
        return stringBuffer.toString();
    }

    protected DriveRouteResult a(String str) throws AMapException {
        return j.c(str);
    }

    public String b() {
        return c.a() + "/direction/driving?";
    }
}
