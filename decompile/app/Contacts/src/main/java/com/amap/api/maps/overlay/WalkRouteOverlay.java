package com.amap.api.maps.overlay;

import android.content.Context;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkStep;
import java.util.List;

public class WalkRouteOverlay extends b {
    private WalkPath a;

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
        this.a = walkPath;
        this.startPoint = a.a(latLonPoint);
        this.endPoint = a.a(latLonPoint2);
    }

    public void addToMap() {
        try {
            List steps = this.a.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                WalkStep walkStep = (WalkStep) steps.get(i);
                LatLng a = a.a((LatLonPoint) walkStep.getPolyline().get(0));
                if (i >= steps.size() - 1) {
                    a(a.a(a(walkStep)), this.endPoint);
                } else {
                    if (i == 0) {
                        a(this.startPoint, a);
                    }
                    a(walkStep, (WalkStep) steps.get(i + 1));
                }
                a(walkStep, a);
                c(walkStep);
            }
            addStartAndEndMarker();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void a(WalkStep walkStep, WalkStep walkStep2) {
        LatLonPoint a = a(walkStep);
        LatLonPoint b = b(walkStep2);
        if (!a.equals(b)) {
            a(a, b);
        }
    }

    private LatLonPoint a(WalkStep walkStep) {
        return (LatLonPoint) walkStep.getPolyline().get(walkStep.getPolyline().size() - 1);
    }

    private LatLonPoint b(WalkStep walkStep) {
        return (LatLonPoint) walkStep.getPolyline().get(0);
    }

    private void a(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
        a(a.a(latLonPoint), a.a(latLonPoint2));
    }

    private void a(LatLng latLng, LatLng latLng2) {
        addPolyLine(new PolylineOptions().add(latLng, latLng2).color(getWalkColor()).width(getRouteWidth()));
    }

    private void c(WalkStep walkStep) {
        addPolyLine(new PolylineOptions().addAll(a.a(walkStep.getPolyline())).color(getWalkColor()).width(getRouteWidth()));
    }

    private void a(WalkStep walkStep, LatLng latLng) {
        addStationMarker(new MarkerOptions().position(latLng).title("方向:" + walkStep.getAction() + "\n道路:" + walkStep.getRoad()).snippet(walkStep.getInstruction()).visible(this.nodeIconVisible).anchor(0.5f, 0.5f).icon(getWalkBitmapDescriptor()));
    }
}
