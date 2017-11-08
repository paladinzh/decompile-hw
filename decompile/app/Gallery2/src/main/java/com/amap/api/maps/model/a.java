package com.amap.api.maps.model;

import com.amap.api.mapcore.util.ds;
import com.autonavi.amap.mapcore.DPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* compiled from: PointQuadTree */
class a {
    private final ds a;
    private final int b;
    private List<WeightedLatLng> c;
    private List<a> d;

    protected a(ds dsVar) {
        this(dsVar, 0);
    }

    private a(double d, double d2, double d3, double d4, int i) {
        this(new ds(d, d2, d3, d4), i);
    }

    private a(ds dsVar, int i) {
        this.d = null;
        this.a = dsVar;
        this.b = i;
    }

    protected void a(WeightedLatLng weightedLatLng) {
        DPoint point = weightedLatLng.getPoint();
        if (this.a.a(point.x, point.y)) {
            a(point.x, point.y, weightedLatLng);
        }
    }

    private void a(double d, double d2, WeightedLatLng weightedLatLng) {
        if (this.d == null) {
            if (this.c == null) {
                this.c = new ArrayList();
            }
            this.c.add(weightedLatLng);
            if (this.c.size() > 50 && this.b < 40) {
                a();
            }
            return;
        }
        if (d2 < this.a.f) {
            if (d < this.a.e) {
                ((a) this.d.get(0)).a(d, d2, weightedLatLng);
            } else {
                ((a) this.d.get(1)).a(d, d2, weightedLatLng);
            }
        } else if (d < this.a.e) {
            ((a) this.d.get(2)).a(d, d2, weightedLatLng);
        } else {
            ((a) this.d.get(3)).a(d, d2, weightedLatLng);
        }
    }

    private void a() {
        this.d = new ArrayList(4);
        this.d.add(new a(this.a.a, this.a.e, this.a.b, this.a.f, this.b + 1));
        this.d.add(new a(this.a.e, this.a.c, this.a.b, this.a.f, this.b + 1));
        this.d.add(new a(this.a.a, this.a.e, this.a.f, this.a.d, this.b + 1));
        this.d.add(new a(this.a.e, this.a.c, this.a.f, this.a.d, this.b + 1));
        List<WeightedLatLng> list = this.c;
        this.c = null;
        for (WeightedLatLng weightedLatLng : list) {
            a(weightedLatLng.getPoint().x, weightedLatLng.getPoint().y, weightedLatLng);
        }
    }

    protected Collection<WeightedLatLng> a(ds dsVar) {
        Collection<WeightedLatLng> arrayList = new ArrayList();
        a(dsVar, arrayList);
        return arrayList;
    }

    private void a(ds dsVar, Collection<WeightedLatLng> collection) {
        if (this.a.a(dsVar)) {
            if (this.d != null) {
                for (a a : this.d) {
                    a.a(dsVar, collection);
                }
            } else if (this.c != null) {
                if (dsVar.b(this.a)) {
                    collection.addAll(this.c);
                } else {
                    for (WeightedLatLng weightedLatLng : this.c) {
                        if (dsVar.a(weightedLatLng.getPoint())) {
                            collection.add(weightedLatLng);
                        }
                    }
                }
            }
        }
    }
}
