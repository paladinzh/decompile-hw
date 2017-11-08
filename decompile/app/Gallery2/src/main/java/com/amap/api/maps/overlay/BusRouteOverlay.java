package com.amap.api.maps.overlay;

import android.content.Context;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.WalkStep;
import com.autonavi.amap.mapcore.MapConfig;
import java.util.List;

public class BusRouteOverlay extends b {
    private BusPath a;
    private LatLng b;

    public /* bridge */ /* synthetic */ void removeFromMap() {
        super.removeFromMap();
    }

    public /* bridge */ /* synthetic */ void setNodeIconVisibility(boolean z) {
        super.setNodeIconVisibility(z);
    }

    public /* bridge */ /* synthetic */ void zoomToSpan() {
        super.zoomToSpan();
    }

    public BusRouteOverlay(Context context, AMap aMap, BusPath busPath, LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
        super(context);
        this.a = busPath;
        this.startPoint = a.a(latLonPoint);
        this.endPoint = a.a(latLonPoint2);
        this.mAMap = aMap;
    }

    public void addToMap() {
        try {
            List steps = this.a.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                BusStep busStep = (BusStep) steps.get(i);
                if (i < steps.size() - 1) {
                    BusStep busStep2 = (BusStep) steps.get(i + 1);
                    if (busStep.getWalk() != null) {
                        if (busStep.getBusLine() != null) {
                            b(busStep);
                        }
                    }
                    if (!(busStep.getBusLine() == null || busStep2.getWalk() == null)) {
                        c(busStep, busStep2);
                    }
                    if (!(busStep.getBusLine() == null || busStep2.getWalk() != null || busStep2.getBusLine() == null)) {
                        b(busStep, busStep2);
                    }
                    if (!(busStep.getBusLine() == null || busStep2.getWalk() != null || busStep2.getBusLine() == null)) {
                        a(busStep, busStep2);
                    }
                }
                if (busStep.getWalk() != null && busStep.getWalk().getSteps().size() > 0) {
                    a(busStep);
                } else if (busStep.getBusLine() == null) {
                    a(this.b, this.endPoint);
                }
                if (busStep.getBusLine() != null) {
                    RouteBusLineItem busLine = busStep.getBusLine();
                    a(busLine);
                    b(busLine);
                }
            }
            addStartAndEndMarker();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void a(BusStep busStep) {
        List steps = busStep.getWalk().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            WalkStep walkStep = (WalkStep) steps.get(i);
            if (i == 0) {
                a(a.a((LatLonPoint) walkStep.getPolyline().get(0)), walkStep.getRoad(), c(steps));
            }
            List a = a.a(walkStep.getPolyline());
            this.b = (LatLng) a.get(a.size() - 1);
            b(a);
            if (i < steps.size() - 1) {
                LatLng latLng = (LatLng) a.get(a.size() - 1);
                LatLng a2 = a.a((LatLonPoint) ((WalkStep) steps.get(i + 1)).getPolyline().get(0));
                if (!latLng.equals(a2)) {
                    a(latLng, a2);
                }
            }
        }
    }

    private void a(RouteBusLineItem routeBusLineItem) {
        a(routeBusLineItem.getPolyline());
    }

    private void a(List<LatLonPoint> list) {
        if (list.size() >= 1) {
            addPolyLine(new PolylineOptions().width(getRouteWidth()).color(getBusColor()).addAll(a.a((List) list)));
        }
    }

    private void a(LatLng latLng, String str, String str2) {
        addStationMarker(new MarkerOptions().position(latLng).title(str).snippet(str2).anchor(0.5f, 0.5f).visible(this.nodeIconVisible).icon(getWalkBitmapDescriptor()));
    }

    private void b(RouteBusLineItem routeBusLineItem) {
        LatLng a = a.a(routeBusLineItem.getDepartureBusStation().getLatLonPoint());
        String busLineName = routeBusLineItem.getBusLineName();
        addStationMarker(new MarkerOptions().position(a).title(busLineName).snippet(c(routeBusLineItem)).anchor(0.5f, 0.5f).visible(this.nodeIconVisible).icon(getBusBitmapDescriptor()));
    }

    private void a(BusStep busStep, BusStep busStep2) {
        LatLng a = a.a(e(busStep));
        LatLng a2 = a.a(f(busStep2));
        if ((a2.latitude - a.latitude > 1.0E-4d ? 1 : null) != null || a2.longitude - a.longitude > 1.0E-4d) {
            drawLineArrow(a, a2);
        }
    }

    private void b(BusStep busStep, BusStep busStep2) {
        LatLng a = a.a(e(busStep));
        LatLng a2 = a.a(f(busStep2));
        if (!a.equals(a2)) {
            drawLineArrow(a, a2);
        }
    }

    private void c(BusStep busStep, BusStep busStep2) {
        LatLonPoint e = e(busStep);
        LatLonPoint c = c(busStep2);
        if (!e.equals(c)) {
            a(e, c);
        }
    }

    private void b(BusStep busStep) {
        LatLonPoint d = d(busStep);
        LatLonPoint f = f(busStep);
        if (!d.equals(f)) {
            a(d, f);
        }
    }

    private LatLonPoint c(BusStep busStep) {
        return (LatLonPoint) ((WalkStep) busStep.getWalk().getSteps().get(0)).getPolyline().get(0);
    }

    private void a(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
        a(a.a(latLonPoint), a.a(latLonPoint2));
    }

    private void a(LatLng latLng, LatLng latLng2) {
        addPolyLine(new PolylineOptions().add(latLng, latLng2).width(getRouteWidth()).color(getWalkColor()));
    }

    private void b(List<LatLng> list) {
        addPolyLine(new PolylineOptions().addAll(list).color(getWalkColor()).width(getRouteWidth()));
    }

    private String c(List<WalkStep> list) {
        float f = 0.0f;
        for (WalkStep distance : list) {
            f = distance.getDistance() + f;
        }
        return "步行" + f + "米";
    }

    public void drawLineArrow(LatLng latLng, LatLng latLng2) {
        addPolyLine(new PolylineOptions().add(latLng, latLng2).width(MapConfig.MIN_ZOOM).color(getBusColor()).width(getRouteWidth()));
    }

    private String c(RouteBusLineItem routeBusLineItem) {
        return "(" + routeBusLineItem.getDepartureBusStation().getBusStationName() + "-->" + routeBusLineItem.getArrivalBusStation().getBusStationName() + ") 经过" + (routeBusLineItem.getPassStationNum() + 1) + "站";
    }

    private LatLonPoint d(BusStep busStep) {
        List steps = busStep.getWalk().getSteps();
        steps = ((WalkStep) steps.get(steps.size() - 1)).getPolyline();
        return (LatLonPoint) steps.get(steps.size() - 1);
    }

    private LatLonPoint e(BusStep busStep) {
        List polyline = busStep.getBusLine().getPolyline();
        return (LatLonPoint) polyline.get(polyline.size() - 1);
    }

    private LatLonPoint f(BusStep busStep) {
        return (LatLonPoint) busStep.getBusLine().getPolyline().get(0);
    }
}
