package com.amap.api.maps.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.amap.api.mapcore.util.ef;
import com.amap.api.mapcore.util.eh;
import com.amap.api.mapcore.util.g;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DrivingRouteOverlay extends b {
    private DrivePath a;
    private List<LatLonPoint> b;
    private List<Marker> c;
    private boolean d;
    private Context e;
    private PolylineOptions f;

    public /* bridge */ /* synthetic */ void setNodeIconVisibility(boolean z) {
        super.setNodeIconVisibility(z);
    }

    public /* bridge */ /* synthetic */ void zoomToSpan() {
        super.zoomToSpan();
    }

    public DrivingRouteOverlay(Context context, AMap aMap, DrivePath drivePath, LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
        this(context, aMap, drivePath, latLonPoint, latLonPoint2, null);
        this.e = context;
    }

    public DrivingRouteOverlay(Context context, AMap aMap, DrivePath drivePath, LatLonPoint latLonPoint, LatLonPoint latLonPoint2, List<LatLonPoint> list) {
        super(context);
        this.c = new ArrayList();
        this.d = true;
        this.mAMap = aMap;
        this.a = drivePath;
        this.startPoint = a.a(latLonPoint);
        this.endPoint = a.a(latLonPoint2);
        this.b = list;
        this.e = context;
    }

    public void addToMap() {
        a();
        try {
            List steps = this.a.getSteps();
            int i = 0;
            while (i < steps.size()) {
                DriveStep driveStep = (DriveStep) steps.get(i);
                LatLng a = a.a(a(driveStep));
                if (i < steps.size() - 1 && i == 0) {
                    a(this.startPoint, a);
                }
                a(driveStep, a);
                c(driveStep);
                if (i == steps.size() - 1) {
                    a(b(driveStep), this.endPoint);
                }
                i++;
            }
            addStartAndEndMarker();
            c();
            b();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void a() {
        this.f = null;
        this.f = new PolylineOptions();
        this.f.color(getDriveColor()).width(getRouteWidth());
    }

    private void b() {
        addPolyLine(this.f);
    }

    private LatLonPoint a(DriveStep driveStep) {
        return (LatLonPoint) driveStep.getPolyline().get(0);
    }

    private LatLonPoint b(DriveStep driveStep) {
        return (LatLonPoint) driveStep.getPolyline().get(driveStep.getPolyline().size() - 1);
    }

    private void a(LatLonPoint latLonPoint, LatLng latLng) {
        a(a.a(latLonPoint), latLng);
    }

    private void a(LatLng latLng, LatLng latLng2) {
        this.f.add(latLng, latLng2);
    }

    private void c(DriveStep driveStep) {
        this.f.addAll(a.a(driveStep.getPolyline()));
    }

    private void a(DriveStep driveStep, LatLng latLng) {
        addStationMarker(new MarkerOptions().position(latLng).title("方向:" + driveStep.getAction() + "\n道路:" + driveStep.getRoad()).snippet(driveStep.getInstruction()).visible(this.nodeIconVisible).anchor(0.5f, 0.5f).icon(getDriveBitmapDescriptor()));
    }

    private void c() {
        if (this.b != null && this.b.size() > 0) {
            for (int i = 0; i < this.b.size(); i++) {
                LatLonPoint latLonPoint = (LatLonPoint) this.b.get(i);
                if (latLonPoint != null) {
                    this.c.add(this.mAMap.addMarker(new MarkerOptions().position(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude())).visible(this.d).icon(d()).title("途经点")));
                }
            }
        }
    }

    protected LatLngBounds getLatLngBounds() {
        Builder builder = LatLngBounds.builder();
        builder.include(new LatLng(this.startPoint.latitude, this.startPoint.longitude));
        builder.include(new LatLng(this.endPoint.latitude, this.endPoint.longitude));
        if (this.b != null && this.b.size() > 0) {
            for (int i = 0; i < this.b.size(); i++) {
                builder.include(new LatLng(((LatLonPoint) this.b.get(i)).getLatitude(), ((LatLonPoint) this.b.get(i)).getLongitude()));
            }
        }
        return builder.build();
    }

    public void setThroughPointIconVisibility(boolean z) {
        try {
            this.d = z;
            if (this.c != null && this.c.size() > 0) {
                for (int i = 0; i < this.c.size(); i++) {
                    ((Marker) this.c.get(i)).setVisible(z);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private BitmapDescriptor d() {
        InputStream open;
        Throwable th;
        BitmapDescriptor fromBitmap;
        Bitmap bitmap = null;
        try {
            open = ef.a(this.e).open("amap_throughpoint.png");
            try {
                bitmap = eh.a(BitmapFactory.decodeStream(open), g.a);
                if (open != null) {
                    try {
                        open.close();
                    } catch (Throwable th2) {
                        th2.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th2 = th3;
                try {
                    th2.printStackTrace();
                    if (open != null) {
                        try {
                            open.close();
                        } catch (Throwable th22) {
                            th22.printStackTrace();
                        }
                    }
                    fromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
                    bitmap.recycle();
                    return fromBitmap;
                } catch (Throwable th4) {
                    th22 = th4;
                    if (open != null) {
                        try {
                            open.close();
                        } catch (Throwable th5) {
                            th5.printStackTrace();
                        }
                    }
                    throw th22;
                }
            }
        } catch (Throwable th6) {
            th22 = th6;
            open = bitmap;
            if (open != null) {
                open.close();
            }
            throw th22;
        }
        fromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
        bitmap.recycle();
        return fromBitmap;
    }

    public void removeFromMap() {
        try {
            super.removeFromMap();
            if (this.c != null && this.c.size() > 0) {
                for (int i = 0; i < this.c.size(); i++) {
                    ((Marker) this.c.get(i)).remove();
                }
                this.c.clear();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
