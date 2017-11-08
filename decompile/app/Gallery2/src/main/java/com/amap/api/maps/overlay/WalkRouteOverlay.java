package com.amap.api.maps.overlay;

import android.content.Context;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import java.util.List;

public class WalkRouteOverlay extends b {
    private PolylineOptions a;
    private BitmapDescriptor b = null;
    private WalkPath c;

    public /* bridge */ /* synthetic */ void removeFromMap() {
        super.removeFromMap();
    }

    public /* bridge */ /* synthetic */ void setNodeIconVisibility(boolean z) {
        super.setNodeIconVisibility(z);
    }

    public /* bridge */ /* synthetic */ void zoomToSpan() {
        super.zoomToSpan();
    }

    public WalkRouteOverlay(Context context, AMap aMap, WalkPath walkPath, LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
        super(context);
        this.mAMap = aMap;
        this.c = walkPath;
        this.startPoint = a.a(latLonPoint);
        this.endPoint = a.a(latLonPoint2);
    }

    public void addToMap() {
        a();
        try {
            List steps = this.c.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                WalkStep walkStep = (WalkStep) steps.get(i);
                LatLng a = a.a((LatLonPoint) walkStep.getPolyline().get(0));
                if (i >= steps.size() - 1) {
                    a(a.a(a(walkStep)), this.endPoint);
                } else if (i == 0) {
                    a(this.startPoint, a);
                }
                a(walkStep, a);
                b(walkStep);
            }
            addStartAndEndMarker();
            b();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private LatLonPoint a(WalkStep walkStep) {
        return (LatLonPoint) walkStep.getPolyline().get(walkStep.getPolyline().size() - 1);
    }

    private void a(LatLng latLng, LatLng latLng2) {
        this.a.add(latLng, latLng2);
    }

    private void b(WalkStep walkStep) {
        this.a.addAll(a.a(walkStep.getPolyline()));
    }

    private void a(WalkStep walkStep, LatLng latLng) {
        addStationMarker(new MarkerOptions().position(latLng).title("方向:" + walkStep.getAction() + "\n道路:" + walkStep.getRoad()).snippet(walkStep.getInstruction()).visible(this.nodeIconVisible).anchor(0.5f, 0.5f).icon(this.b));
    }

    private void a() {
        if (this.b == null) {
            this.b = getWalkBitmapDescriptor();
        }
        this.a = null;
        this.a = new PolylineOptions();
        this.a.color(getDriveColor()).width(getRouteWidth());
    }

    private void b() {
        addPolyLine(this.a);
    }
}
