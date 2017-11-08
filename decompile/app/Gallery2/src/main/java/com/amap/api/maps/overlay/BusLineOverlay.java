package com.amap.api.maps.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import com.amap.api.mapcore.util.ef;
import com.amap.api.mapcore.util.eh;
import com.amap.api.mapcore.util.g;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BusLineOverlay {
    private BusLineItem a;
    private AMap b;
    private ArrayList<Marker> c = new ArrayList();
    private Polyline d;
    private List<BusStationItem> e;
    private BitmapDescriptor f;
    private BitmapDescriptor g;
    private BitmapDescriptor h;
    private Context i;

    public BusLineOverlay(Context context, AMap aMap, BusLineItem busLineItem) {
        this.i = context;
        this.a = busLineItem;
        this.b = aMap;
        this.e = this.a.getBusStations();
    }

    public void addToMap() {
        int i = 1;
        try {
            this.d = this.b.addPolyline(new PolylineOptions().addAll(a.a(this.a.getDirectionsCoordinates())).color(getBusColor()).width(getBuslineWidth()));
            if (this.e.size() >= 1) {
                while (i < this.e.size() - 1) {
                    this.c.add(this.b.addMarker(a(i)));
                    i++;
                }
                this.c.add(this.b.addMarker(a(0)));
                this.c.add(this.b.addMarker(a(this.e.size() - 1)));
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void removeFromMap() {
        if (this.d != null) {
            this.d.remove();
        }
        try {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                ((Marker) it.next()).remove();
            }
            a();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void a() {
        if (this.f != null) {
            this.f.recycle();
            this.f = null;
        }
        if (this.g != null) {
            this.g.recycle();
            this.g = null;
        }
        if (this.h != null) {
            this.h.recycle();
            this.h = null;
        }
    }

    public void zoomToSpan() {
        if (this.b != null) {
            try {
                List directionsCoordinates = this.a.getDirectionsCoordinates();
                if (directionsCoordinates != null && directionsCoordinates.size() > 0) {
                    this.b.moveCamera(CameraUpdateFactory.newLatLngBounds(a(directionsCoordinates), 5));
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private LatLngBounds a(List<LatLonPoint> list) {
        Builder builder = LatLngBounds.builder();
        for (int i = 0; i < list.size(); i++) {
            builder.include(new LatLng(((LatLonPoint) list.get(i)).getLatitude(), ((LatLonPoint) list.get(i)).getLongitude()));
        }
        return builder.build();
    }

    private MarkerOptions a(int i) {
        MarkerOptions snippet = new MarkerOptions().position(new LatLng(((BusStationItem) this.e.get(i)).getLatLonPoint().getLatitude(), ((BusStationItem) this.e.get(i)).getLatLonPoint().getLongitude())).title(getTitle(i)).snippet(getSnippet(i));
        if (i == 0) {
            snippet.icon(getStartBitmapDescriptor());
        } else if (i != this.e.size() - 1) {
            snippet.anchor(0.5f, 0.5f);
            snippet.icon(getBusBitmapDescriptor());
        } else {
            snippet.icon(getEndBitmapDescriptor());
        }
        return snippet;
    }

    protected BitmapDescriptor getStartBitmapDescriptor() {
        this.f = a("amap_start.png");
        return this.f;
    }

    protected BitmapDescriptor getEndBitmapDescriptor() {
        this.g = a("amap_end.png");
        return this.g;
    }

    protected BitmapDescriptor getBusBitmapDescriptor() {
        this.h = a("amap_bus.png");
        return this.h;
    }

    protected String getTitle(int i) {
        return ((BusStationItem) this.e.get(i)).getBusStationName();
    }

    protected String getSnippet(int i) {
        return "";
    }

    public int getBusStationIndex(Marker marker) {
        for (int i = 0; i < this.c.size(); i++) {
            if (((Marker) this.c.get(i)).equals(marker)) {
                return i;
            }
        }
        return -1;
    }

    public BusStationItem getBusStationItem(int i) {
        if (i >= 0 && i < this.e.size()) {
            return (BusStationItem) this.e.get(i);
        }
        return null;
    }

    protected int getBusColor() {
        return Color.parseColor("#537edc");
    }

    protected float getBuslineWidth() {
        return 18.0f;
    }

    private BitmapDescriptor a(String str) {
        InputStream open;
        IOException e;
        BitmapDescriptor fromBitmap;
        Throwable th;
        Bitmap bitmap = null;
        try {
            open = ef.a(this.i).open(str);
            try {
                bitmap = eh.a(BitmapFactory.decodeStream(open), g.a);
                if (open != null) {
                    try {
                        open.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (IOException e3) {
                e2 = e3;
                try {
                    e2.printStackTrace();
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    fromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
                    bitmap.recycle();
                    return fromBitmap;
                } catch (Throwable th2) {
                    th = th2;
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                th.printStackTrace();
                if (open != null) {
                    try {
                        open.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
                fromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
                bitmap.recycle();
                return fromBitmap;
            }
        } catch (IOException e5) {
            e222 = e5;
            open = bitmap;
            e222.printStackTrace();
            if (open != null) {
                open.close();
            }
            fromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            bitmap.recycle();
            return fromBitmap;
        } catch (Throwable th4) {
            th = th4;
            open = bitmap;
            if (open != null) {
                open.close();
            }
            throw th;
        }
        fromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
        bitmap.recycle();
        return fromBitmap;
    }
}
