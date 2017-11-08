package com.amap.api.maps.model;

import com.amap.api.mapcore.util.ay;
import com.autonavi.amap.mapcore.DPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* compiled from: PointQuadTree */
class c {
    private final ay a;
    private final int b;
    private List<WeightedLatLng> c;
    private List<c> d;

    protected c(ay ayVar) {
        this(ayVar, 0);
    }

    private c(double d, double d2, double d3, double d4, int i) {
        this(new ay(d, d2, d3, d4), i);
    }

    private c(ay ayVar, int i) {
        this.d = null;
        this.a = ayVar;
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
                ((c) this.d.get(0)).a(d, d2, weightedLatLng);
            } else {
                ((c) this.d.get(1)).a(d, d2, weightedLatLng);
            }
        } else if (d < this.a.e) {
            ((c) this.d.get(2)).a(d, d2, weightedLatLng);
        } else {
            ((c) this.d.get(3)).a(d, d2, weightedLatLng);
        }
    }

    private void a() {
        this.d = new ArrayList(4);
        this.d.add(new c(this.a.a, this.a.e, this.a.b, this.a.f, this.b + 1));
        this.d.add(new c(this.a.e, this.a.c, this.a.b, this.a.f, this.b + 1));
        this.d.add(new c(this.a.a, this.a.e, this.a.f, this.a.d, this.b + 1));
        this.d.add(new c(this.a.e, this.a.c, this.a.f, this.a.d, this.b + 1));
        List<WeightedLatLng> list = this.c;
        this.c = null;
        for (WeightedLatLng weightedLatLng : list) {
            a(weightedLatLng.getPoint().x, weightedLatLng.getPoint().y, weightedLatLng);
        }
    }

    protected Collection<WeightedLatLng> a(ay ayVar) {
        Collection<WeightedLatLng> arrayList = new ArrayList();
        a(ayVar, arrayList);
        return arrayList;
    }

    private void a(ay ayVar, Collection<WeightedLatLng> collection) {
        if (this.a.a(ayVar)) {
            if (this.d != null) {
                for (c a : this.d) {
                    a.a(ayVar, collection);
                }
            } else if (this.c != null) {
                if (ayVar.b(this.a)) {
                    collection.addAll(this.c);
                } else {
                    for (WeightedLatLng weightedLatLng : this.c) {
                        if (ayVar.a(weightedLatLng.getPoint())) {
                            collection.add(weightedLatLng);
                        }
                    }
                }
            }
        }
    }
}
