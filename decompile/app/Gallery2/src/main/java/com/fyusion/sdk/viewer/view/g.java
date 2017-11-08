package com.fyusion.sdk.viewer.view;

import android.util.Log;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.MapConfig;
import com.fyusion.sdk.common.e;
import com.fyusion.sdk.common.n;
import com.fyusion.sdk.core.a.c;
import com.fyusion.sdk.core.a.d;
import com.fyusion.sdk.viewer.h;
import com.fyusion.sdk.viewer.i;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
class g {
    private static boolean a = true;
    private static boolean b = false;
    private d A;
    private int B;
    private int C;
    private boolean D = false;
    private h E = null;
    private com.fyusion.sdk.viewer.internal.b.c.a F;
    private boolean G;
    private boolean H;
    private final Object c = new Object();
    private a d;
    private boolean e;
    private boolean f;
    private n g;
    private int h;
    private int i;
    private int j;
    private c k;
    private com.fyusion.sdk.common.d l = null;
    private com.fyusion.sdk.common.c.b m;
    private com.fyusion.sdk.common.d n = null;
    private com.fyusion.sdk.common.d o = null;
    private float p;
    private float q;
    private int r;
    private long s;
    private long t;
    private boolean u;
    private long v;
    private double w;
    private e x;
    private d y;
    private d z;

    /* compiled from: Unknown */
    private static class a {
        float a;
        float b;
        float c;
        boolean d;
        float e;
        com.fyusion.sdk.common.d f;
        double g;
        double h;
        float i;
        boolean j;
        double k;
        double l;

        a() {
            this.f = new com.fyusion.sdk.common.d(0.0f, 0.0f);
            this.i = GroundOverlayOptions.NO_DIMENSION;
            this.k = 0.0d;
            this.l = 0.0d;
            this.d = false;
            this.c = GroundOverlayOptions.NO_DIMENSION;
            this.e = 0.0f;
            this.b = GroundOverlayOptions.NO_DIMENSION;
            this.g = 0.0d;
            this.h = 0.0d;
            this.j = false;
            this.k = 0.0d;
            this.l = 0.0d;
        }
    }

    /* compiled from: Unknown */
    static class b {
        boolean a = false;
        boolean b = false;

        b() {
        }

        public void a() {
            this.a = false;
            this.b = false;
        }
    }

    g(boolean z) {
        this.G = z;
        this.E = new h();
        this.k = new c();
    }

    private double a(double d, double d2) {
        if (this.m == null) {
            p();
        }
        if (this.m != null) {
            double d3 = (this.m.a * d2) + (this.m.b * d);
            d = (this.m.c * d2) + (this.m.d * d);
            d2 = d3;
        }
        return (((double) this.g.getIMUDirectionX()) * d2) + (((double) this.g.getIMUDirectionY()) * d);
    }

    private com.fyusion.sdk.common.c.a a(float f, float f2, boolean z) {
        float f3;
        float f4 = 0.0f;
        float f5 = 0.0f;
        if (0.0f > i.a) {
            f5 = i.a;
        } else if (0.0f < (-i.a)) {
            f5 = -i.a;
        }
        if (0.0f > i.a) {
            f4 = i.a;
        } else if (0.0f < (-i.a)) {
            f4 = -i.a;
        }
        boolean isConvex = this.g.isConvex();
        double d = (!isConvex ? -WeightedLatLng.DEFAULT_INTENSITY : WeightedLatLng.DEFAULT_INTENSITY) * -1.0d;
        if (this.g.isConvex()) {
            f3 = f5;
            f5 = f4;
        } else {
            f3 = f5 * GroundOverlayOptions.NO_DIMENSION;
            f5 = f4 * GroundOverlayOptions.NO_DIMENSION;
        }
        com.fyusion.sdk.common.c.a a = com.fyusion.sdk.common.c.a();
        a.l = !isConvex ? -0.006666666666666667d : -0.005d;
        a.l *= 10.0d;
        a = com.fyusion.sdk.common.c.a(a, -0.0d, -0.0d, -d);
        double d2 = !isConvex ? 0.008726646259971648d : 0.017453292519943295d;
        double d3 = ((double) (f3 / i.a)) * 0.017453292519943295d;
        double d4 = ((double) (f5 / i.a)) * d2;
        com.fyusion.sdk.common.c.a a2 = com.fyusion.sdk.common.c.a(com.fyusion.sdk.common.c.a(com.fyusion.sdk.common.c.a(a, d3 / 10.0d, -1.0d, 0.0d, 0.0d), d4 / 10.0d, 0.0d, WeightedLatLng.DEFAULT_INTENSITY, 0.0d), 0.0d, 0.0d, d);
        double sin = -1.0d * (((double) (i.a / MapConfig.MIN_ZOOM)) / Math.sin(0.017453292519943295d));
        double sin2 = -1.0d * (((double) (i.a / MapConfig.MIN_ZOOM)) / Math.sin(d2));
        if (!(this.g.getWidth() == 0 || this.g.getHeight() == 0)) {
            double min = (double) Math.min(((float) this.g.getWidth()) / 720.0f, ((float) this.g.getHeight()) / 406.0f);
            if (min > 0.5d && min < 1.5d) {
                sin *= min;
                sin2 *= min;
            }
        }
        if (!isConvex) {
            sin *= 1.5d;
            sin2 *= 2.5d;
        }
        sin *= 2.0d;
        sin2 *= 2.0d;
        com.fyusion.sdk.common.c.a a3 = !isConvex ? com.fyusion.sdk.common.c.a(com.fyusion.sdk.common.c.b(com.fyusion.sdk.common.c.b(sin2 * Math.sin(d4), sin * Math.sin(d3))), a2) : com.fyusion.sdk.common.c.a(com.fyusion.sdk.common.c.b(com.fyusion.sdk.common.c.b((-sin2) * Math.sin(d4), (-sin) * Math.sin(d3))), a2);
        return !z ? a3 : com.fyusion.sdk.common.c.a(com.fyusion.sdk.common.c.a(-1.5707963267948966d, 0.0d, 0.0d, (double) WeightedLatLng.DEFAULT_INTENSITY), com.fyusion.sdk.common.c.a(a3, com.fyusion.sdk.common.c.a(1.5707963267948966d, 0.0d, 0.0d, (double) WeightedLatLng.DEFAULT_INTENSITY)));
    }

    private void a(com.fyusion.sdk.common.d dVar) {
        dVar.a = ((i.a * 2.0f) / ((float) (Math.exp((double) ((dVar.a * -2.0f) / i.a)) + WeightedLatLng.DEFAULT_INTENSITY))) - i.a;
        dVar.b = ((i.a * 2.0f) / ((float) (Math.exp((double) ((dVar.b * -2.0f) / i.a)) + WeightedLatLng.DEFAULT_INTENSITY))) - i.a;
    }

    private void a(com.fyusion.sdk.common.d dVar, boolean z) {
        float iMUDirectionX = !z ? this.g.getIMUDirectionX() : this.g.getSwipeDirectionX();
        float iMUDirectionY = !z ? this.g.getIMUDirectionY() : this.g.getSwipeDirectionY();
        if (this.d.b == ((float) n())) {
            if (iMUDirectionX > 0.0f) {
                iMUDirectionX = Math.max(dVar.a, 0.0f);
            } else {
                if (iMUDirectionX < 0.0f) {
                    iMUDirectionX = Math.min(dVar.a, 0.0f);
                }
                if (iMUDirectionY < 0.0f) {
                    iMUDirectionY = Math.max(dVar.b, 0.0f);
                } else if (iMUDirectionY > 0.0f) {
                    iMUDirectionY = Math.min(dVar.b, 0.0f);
                } else {
                    return;
                }
            }
            dVar.a = iMUDirectionX;
            if (iMUDirectionY < 0.0f) {
                iMUDirectionY = Math.max(dVar.b, 0.0f);
            } else if (iMUDirectionY > 0.0f) {
                iMUDirectionY = Math.min(dVar.b, 0.0f);
            } else {
                return;
            }
        } else if (this.d.b == ((float) o())) {
            if (iMUDirectionX < 0.0f) {
                iMUDirectionX = Math.max(dVar.a, 0.0f);
            } else {
                if (iMUDirectionX > 0.0f) {
                    iMUDirectionX = Math.min(dVar.a, 0.0f);
                }
                if (iMUDirectionY > 0.0f) {
                    iMUDirectionY = Math.max(dVar.b, 0.0f);
                } else if (iMUDirectionY < 0.0f) {
                    iMUDirectionY = Math.min(dVar.b, 0.0f);
                } else {
                    return;
                }
            }
            dVar.a = iMUDirectionX;
            if (iMUDirectionY > 0.0f) {
                iMUDirectionY = Math.max(dVar.b, 0.0f);
            } else if (iMUDirectionY < 0.0f) {
                iMUDirectionY = Math.min(dVar.b, 0.0f);
            } else {
                return;
            }
        } else if (Math.abs(iMUDirectionX) > Math.abs(iMUDirectionY)) {
            dVar.a = 0.0f;
            return;
        } else {
            dVar.b = 0.0f;
            return;
        }
        dVar.b = iMUDirectionY;
    }

    private float b(float f, float f2) {
        float n = (float) n();
        float o = (float) o();
        return !t() ? f < n - f2 ? ((float) n()) - f2 : f > o + f2 ? ((float) o()) + f2 : f : f < n ? o - (n - f) : f > o ? n + (f - o) : f;
    }

    private d b(int i) {
        com.fyusion.sdk.core.a.h a = this.g == null ? null : this.F.a(i);
        return (a != null && a.b()) ? this.k.a(a, com.fyusion.sdk.core.a.c.b.HINT_1080P) : null;
    }

    private void b(double d, double d2) {
        if (this.w > 0.0d) {
            double d3 = d2 - this.w;
            if (d3 > 0.001d) {
                a aVar;
                d3 = d / d3;
                double d4 = (this.d.g * 0.75d) + (0.25d * d3);
                if (this.d.g > 0.0d && d3 < 0.0d) {
                    aVar = this.d;
                    d4 = Math.min(d4, 0.0d);
                } else if (this.d.g >= 0.0d || d3 <= 0.0d) {
                    aVar = this.d;
                } else {
                    aVar = this.d;
                    d4 = Math.max(d4, 0.0d);
                }
                aVar.g = d4;
                this.d.h = (Math.abs(d3) * 0.25d) + (this.d.h * 0.75d);
            }
        }
        this.w = d2;
    }

    private void b(com.fyusion.sdk.common.d dVar) {
        dVar.a = ((-i.a) / 2.0f) * ((float) Math.log((double) (((i.a * 2.0f) / (dVar.a + i.a)) - WMElement.CAMERASIZEVALUE1B1)));
        dVar.b = ((-i.a) / 2.0f) * ((float) Math.log((double) (((i.a * 2.0f) / (dVar.b + i.a)) - WMElement.CAMERASIZEVALUE1B1)));
    }

    private void c(com.fyusion.sdk.common.d dVar) {
        float sqrt = (float) Math.sqrt((double) ((dVar.a * dVar.a) + (dVar.b * dVar.b)));
        if (sqrt > i.a) {
            dVar.a *= i.a / sqrt;
            dVar.b = (i.a / sqrt) * dVar.b;
        }
    }

    private int e(float f) {
        return this.d.g < 0.0d ? (int) Math.floor((double) f) : (int) Math.ceil((double) f);
    }

    private int f(float f) {
        return this.d.b < 0.0f ? (int) f : ((double) Math.abs(f - this.d.b)) < 0.5d ? (int) this.d.b : (int) f;
    }

    private boolean g(float f) {
        return f >= ((float) n()) && f <= ((float) o());
    }

    private g m() {
        this.d = new a();
        this.d.a = GroundOverlayOptions.NO_DIMENSION;
        this.d.d = false;
        this.d.c = GroundOverlayOptions.NO_DIMENSION;
        this.e = true;
        this.f = true;
        this.y = null;
        this.z = null;
        this.B = -1;
        this.C = -1;
        this.x = null;
        this.D = false;
        return this;
    }

    private int n() {
        return Math.max(this.F.k(), this.h);
    }

    private int o() {
        int l = this.F.l();
        return this.i < 0 ? l : Math.min(l, this.i);
    }

    private void p() {
        this.m = c();
    }

    private void q() {
        if (this.F.g() == null) {
            this.x = null;
        } else {
            this.B = -1;
            this.C = -1;
        }
        this.D = this.g.isLoopClosed();
        this.d.d = false;
        this.d.c = GroundOverlayOptions.NO_DIMENSION;
    }

    private boolean r() {
        if (this.H) {
            return true;
        }
        if (this.F.k() > this.h) {
            return false;
        }
        if (this.i >= 0) {
            return this.F.l() >= this.i;
        } else {
            if (this.F.l() < this.g.getNumProcessedFrames() - 1) {
                return false;
            }
        }
    }

    private float s() {
        return (float) (!e() ? WeightedLatLng.DEFAULT_INTENSITY : 1.059000015258789d);
    }

    private boolean t() {
        return this.D && r();
    }

    private double u() {
        double d = 57.29577951308232d;
        double o = ((double) ((o() - n()) + 1)) / 1.0471975511965976d;
        if (o >= 57.29577951308232d) {
            d = o;
        }
        return d > 171.88733853924697d ? 171.88733853924697d : d;
    }

    float a(e eVar, e eVar2) {
        Object obj;
        if ((eVar.a > eVar.b ? 1 : null) == (eVar2.a > eVar2.b ? 1 : null)) {
            obj = null;
        } else {
            int i = 1;
        }
        return Math.max((float) (eVar2.a / (obj == null ? eVar.a : eVar.b)), (float) (eVar2.b / (obj == null ? eVar.b : eVar.a))) * s();
    }

    float a(com.fyusion.sdk.viewer.c.a aVar, com.fyusion.sdk.common.d dVar, b bVar) {
        bVar.a();
        if (this.F == null || this.F.l() == 0) {
            return GroundOverlayOptions.NO_DIMENSION;
        }
        dVar.a = 0.0f;
        dVar.b = 0.0f;
        long j = aVar.d;
        if (aVar.f) {
            aVar.f = false;
            this.s = j;
            this.d.k = 0.0d;
            this.d.l = 0.0d;
        }
        double d = ((double) (j - this.s)) * 1.0E-9d;
        this.s = j;
        double min = Math.min(d, Math.max(0.01d, aVar.e) * 5.0d);
        double d2 = 0.0d;
        d = 0.0d;
        float f = aVar.b;
        float f2 = aVar.a;
        if (((double) Math.abs(f)) > 0.035d) {
            d2 = ((double) f) * min;
        }
        if (((double) Math.abs(f2)) > 0.035d) {
            d = ((double) f2) * min;
        }
        min = (this.d.k * 0.5d) + (d2 * 0.5d);
        d = (d * 0.5d) + (this.d.l * 0.5d);
        boolean e = e();
        d2 = !e ? 0.001d : 1.0E-4d;
        if (Math.abs(min) < d2 && Math.abs(d) < d2) {
            return GroundOverlayOptions.NO_DIMENSION;
        }
        this.d.k = min;
        this.d.l = d;
        if (this.o == null) {
            this.o = new com.fyusion.sdk.common.d(0.0f, 0.0f);
        }
        this.o.a = this.p;
        this.o.b = this.q;
        b(this.o);
        d2 = u();
        float f3 = (float) (min * d2);
        float a = (float) (d2 * a(min, d));
        dVar.a = this.o.a + (-((float) (d * d2)));
        dVar.b = this.o.b + f3;
        if (this.n == null) {
            this.n = new com.fyusion.sdk.common.d(0.0f, 0.0f);
        }
        this.n.a = dVar.a;
        this.n.b = dVar.b;
        a(this.n, false);
        a(this.n);
        c(this.n);
        dVar.a = this.n.a;
        dVar.b = this.n.b;
        if ((((double) Math.max(Math.abs(dVar.a - this.p), Math.abs(dVar.b - this.q))) > 0.1d ? 1 : null) != null || this.v == 0) {
            bVar.b = true;
        }
        this.p = dVar.a;
        this.q = dVar.b;
        b((double) a, ((double) System.currentTimeMillis()) / 1000.0d);
        float f4 = this.d.a + a;
        if (!t()) {
            f4 = b(f4, 0.0f);
        }
        float f5 = this.d.i;
        this.d.a = f4;
        if (!e) {
            f4 = (float) f(f4);
        }
        if (!((((double) Math.abs(f4 - this.d.c)) > 0.01d ? 1 : null) == null && this.d.d)) {
            if (this.d.b < 0.0f) {
                this.d.b = b(f4, 0.0f);
                if (f5 != this.d.b) {
                    if ((this.v <= 0 ? 1 : null) == null) {
                        bVar.a = true;
                        return this.d.b;
                    }
                }
                return GroundOverlayOptions.NO_DIMENSION;
            }
            this.d.c = f4;
            this.d.d = true;
            this.d.e = Math.abs(f4 - this.d.b) / ((float) ((int) Math.min(Math.ceil(Math.abs((double) (f4 - this.d.b)) / 5.0d), 10.0d)));
        }
        if (!e) {
            this.d.e = (float) ((int) Math.ceil((double) this.d.e));
        }
        if (!this.d.d) {
            return GroundOverlayOptions.NO_DIMENSION;
        }
        a aVar2;
        if (this.d.b < this.d.c) {
            aVar2 = this.d;
            aVar2.b += Math.min(this.d.e, this.d.c - this.d.b);
        } else if (this.d.b > this.d.c) {
            aVar2 = this.d;
            aVar2.b -= Math.min(this.d.e, this.d.b - this.d.c);
        }
        if (!g(this.d.b)) {
            this.d.b = b(this.d.b, 0.0f);
            this.d.a = this.d.b;
            this.d.c = b(this.d.c, 0.0f);
        }
        if (((double) Math.abs(this.d.b - this.d.c)) < 0.05d) {
            this.d.b = this.d.c;
            this.d.d = false;
        }
        this.d.j = e;
        if ((((double) Math.abs(f5 - this.d.b)) > 0.01d ? 1 : null) == null && this.v != 0) {
            return GroundOverlayOptions.NO_DIMENSION;
        }
        bVar.a = true;
        return this.d.b;
    }

    public float a(h hVar, com.fyusion.sdk.viewer.a.b bVar, com.fyusion.sdk.common.d dVar, com.fyusion.sdk.common.d dVar2, b bVar2) {
        if (this.F == null || this.F.l() == 0) {
            return GroundOverlayOptions.NO_DIMENSION;
        }
        float f;
        long currentTimeMillis = System.currentTimeMillis();
        if ((this.t <= 0 ? 1 : null) == null && ((double) (currentTimeMillis - this.t)) * 0.001d > WeightedLatLng.DEFAULT_INTENSITY) {
            i();
        }
        this.t = currentTimeMillis;
        if (bVar == com.fyusion.sdk.viewer.a.b.HAS_STARTED) {
            this.e = false;
            this.d.f.a = 0.0f;
            this.d.f.b = 0.0f;
            this.d.d = false;
        }
        Object obj = null;
        if (!this.u) {
            obj = 1;
            hVar.cancelLongPress();
        }
        this.u = true;
        this.r = hVar.getWidth();
        bVar2.a();
        dVar.a *= GroundOverlayOptions.NO_DIMENSION;
        dVar.b *= GroundOverlayOptions.NO_DIMENSION;
        if (this.m == null) {
            p();
        }
        if (this.m != null) {
            float f2 = (float) ((this.m.a * ((double) dVar.a)) + (this.m.b * ((double) dVar.b)));
            f = (float) ((this.m.c * ((double) dVar.a)) + (this.m.d * ((double) dVar.b)));
            switch (this.j) {
                case 0:
                    dVar.a = f2;
                    dVar.b = f;
                    break;
                case 1:
                    dVar.a = f;
                    dVar.b = f2;
                    break;
                case 2:
                    break;
                case 3:
                    dVar.a = f;
                    dVar.b = -f2;
                    break;
                default:
                    break;
            }
        }
        if (obj != null) {
            this.d.f = dVar;
        }
        if (this.l == null) {
            this.l = new com.fyusion.sdk.common.d(0.0f, 0.0f);
        }
        this.l.a = dVar.a - this.d.f.a;
        this.l.b = dVar.b - this.d.f.b;
        f = d((this.g.getSwipeDirectionX() * this.l.a) + (this.g.getSwipeDirectionY() * this.l.b));
        b((double) f, ((double) currentTimeMillis) / 1000.0d);
        float b = b(this.d.a + f, 0.5f);
        this.d.d = false;
        obj = null;
        int f3 = f(b);
        com.fyusion.sdk.common.d dVar3 = new com.fyusion.sdk.common.d(this.p, this.q);
        b(dVar3);
        float f4 = -c(this.l.a);
        float c = c(this.l.b);
        dVar2.a = dVar3.a + f4;
        dVar2.b = dVar3.b + c;
        a(dVar2, true);
        if (Math.sqrt((double) ((f4 * f4) + (c * c))) > ((double) Math.abs(f))) {
            a(dVar2);
            c(dVar2);
        } else {
            a(dVar2);
            c(dVar2);
        }
        if (((double) Math.max(Math.abs(dVar2.a - this.p), Math.abs(dVar2.b - this.q))) > 0.1d) {
            bVar2.b = true;
        }
        this.p = dVar2.a;
        this.q = dVar2.b;
        if (((float) f3) != this.d.b) {
            this.d.b = (float) f3;
            obj = 1;
        }
        this.d.a = b;
        this.d.f = dVar;
        if (bVar == com.fyusion.sdk.viewer.a.b.HAS_ENDED) {
            this.d.a = b;
            i();
        }
        if (e()) {
            obj = (bVar != com.fyusion.sdk.viewer.a.b.HAS_ENDED && this.d.h <= 10.0d) ? null : 1;
            if (obj == null) {
                this.d.j = true;
                bVar2.a = true;
                bVar2.b = true;
                return b;
            }
            int e;
            if (this.d.j) {
                e = e(b);
                this.d.b = (float) e;
                this.d.a = (float) e;
                this.d.j = false;
            } else {
                e = f3;
            }
            bVar2.a = true;
            bVar2.b = true;
            return (float) e;
        }
        float f5;
        if (obj != null) {
            bVar2.a = true;
            f5 = b;
        } else {
            f5 = GroundOverlayOptions.NO_DIMENSION;
        }
        return (float) ((int) f5);
    }

    com.fyusion.sdk.common.c.a a(float f, float f2) {
        return a(f, f2, false);
    }

    g a(com.fyusion.sdk.viewer.internal.b.c.a aVar) {
        m();
        if (aVar != null) {
            this.g = aVar.m();
            this.h = this.g.getStartFrame();
            this.i = this.g.getEndFrame();
            this.F = aVar;
            this.E.a(aVar.d());
            this.H = aVar.e();
        }
        this.d.a = (float) this.g.getThumbnailIndex();
        this.d.d = false;
        this.d.c = GroundOverlayOptions.NO_DIMENSION;
        q();
        this.m = c();
        return this;
    }

    void a() {
        this.y = null;
        this.z = null;
        this.A = null;
    }

    void a(float f) {
        if (f < 0.0f) {
            f = this.g == null ? 0.0f : (float) this.g.getThumbnailIndex();
        }
        if (this.d != null) {
            this.d.d = false;
            this.d.c = GroundOverlayOptions.NO_DIMENSION;
            this.d.a = f;
            this.d.b = (float) ((int) f);
            this.d.g = 0.0d;
            this.d.k = 0.0d;
            this.d.l = 0.0d;
        }
    }

    void a(int i) {
        this.j = i;
        p();
    }

    void a(boolean z) {
        this.G = z;
    }

    d b(float f) {
        float b = b(f, 0.0f);
        if (b < 0.0f) {
            return null;
        }
        this.E.a((int) b, this.g.getNumProcessedFrames());
        if (!f()) {
            return null;
        }
        this.d.i = b;
        int i;
        if (e() && ((double) Math.abs(b - ((float) Math.round(b)))) >= 0.001d) {
            com.fyusion.sdk.viewer.view.e.a a = this.x.a(b);
            if (a != null) {
                if (a.a == this.C || a.d == this.B) {
                    i = this.B;
                    this.A = this.y;
                    this.B = this.C;
                    this.y = this.z;
                    this.C = i;
                    this.z = this.A;
                }
                if (this.B != a.a) {
                    this.y = b(a.a);
                    if (this.y == null) {
                        return null;
                    }
                    this.B = a.a;
                }
                if (this.C != a.d) {
                    this.z = b(a.d);
                    if (this.z == null) {
                        return null;
                    }
                    this.C = a.d;
                }
                com.fyusion.sdk.common.c.b bVar = a.c;
                com.fyusion.sdk.common.c.b bVar2 = a.f;
                if (this.y == null || this.z == null || this.y.b() <= 0 || this.z.c() <= 0) {
                    return null;
                }
                com.fyusion.sdk.common.c.b a2 = com.fyusion.sdk.common.c.a((double) (this.g.getWidth() / this.y.b()), (double) (this.g.getHeight() / this.y.c()));
                bVar = com.fyusion.sdk.common.c.a(a2, com.fyusion.sdk.common.c.a(bVar, com.fyusion.sdk.common.c.a(a2)));
                bVar2 = com.fyusion.sdk.common.c.a(a2, com.fyusion.sdk.common.c.a(bVar2, com.fyusion.sdk.common.c.a(a2)));
                d dVar = new d();
                dVar.a = this.y;
                dVar.d = a.b;
                dVar.f = bVar;
                dVar.b = this.z;
                dVar.e = a.e;
                dVar.g = bVar2;
                dVar.h = a.a;
                dVar.i = a.d;
                if (dVar.a != null && dVar.b != null) {
                    return dVar;
                }
                if (dVar.a != null) {
                }
                return dVar.b != null ? null : null;
            } else {
                i = f(b);
                d b2 = b(i);
                return b2 != null ? new d(b2, i) : null;
            }
        } else {
            i = f(b);
            this.y = b(i);
            if (this.y == null) {
                return null;
            }
            this.B = i;
            return new d(this.y, i);
        }
    }

    void b() {
        this.v = 0;
        if (this.d != null) {
            this.d.a = GroundOverlayOptions.NO_DIMENSION;
            this.d.d = false;
            this.d.c = GroundOverlayOptions.NO_DIMENSION;
            this.d.b = GroundOverlayOptions.NO_DIMENSION;
            this.d.i = GroundOverlayOptions.NO_DIMENSION;
        }
    }

    void b(boolean z) {
        this.e = z;
    }

    float c(float f) {
        return j() * f;
    }

    com.fyusion.sdk.common.c.b c() {
        if (this.g != null) {
            if (this.G) {
                if (!this.g.isPortrait()) {
                    switch (this.j) {
                        case 0:
                            return com.fyusion.sdk.common.c.a(!this.g.isFromFrontCamera() ? -this.g.getRotationAngleBasedOnGravityX() : this.g.getRotationAngleBasedOnGravityX());
                        case 1:
                            return com.fyusion.sdk.common.c.b();
                        case 2:
                            break;
                        case 3:
                            return com.fyusion.sdk.common.c.a(-3.141592653589793d);
                        default:
                            break;
                    }
                }
                switch (this.j) {
                    case 0:
                        return com.fyusion.sdk.common.c.b();
                    case 1:
                        return com.fyusion.sdk.common.c.a(1.5707963267948966d);
                    case 2:
                        break;
                    case 3:
                        return com.fyusion.sdk.common.c.a(-1.5707963267948966d);
                    default:
                        break;
                }
            } else if (a) {
                return this.g.getEffectiveGravityX() == WMElement.CAMERASIZEVALUE1B1 ? com.fyusion.sdk.common.c.a(3.141592653589793d) : com.fyusion.sdk.common.c.b();
            }
        }
        return com.fyusion.sdk.common.c.b();
    }

    void c(boolean z) {
        this.f = z;
    }

    float d(float f) {
        return j() * f;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int d() {
        int i = 0;
        if (this.g != null) {
            if (this.G) {
                switch (this.g.getRotationMode()) {
                    case 0:
                        return 0;
                    case 1:
                        return 180;
                    case 2:
                        return 90;
                    default:
                        Log.w("PrivateFyuseViewer", "Unknown rotation mode: " + this.g.getRotationMode());
                        break;
                }
            } else if (!this.g.isPortrait() && a) {
                float effectiveGravityX = this.g.getEffectiveGravityX();
                if (b) {
                    if (effectiveGravityX < 0.0f) {
                        i = 1;
                    }
                    if (i == 0) {
                    }
                    return -90;
                }
                if (!b) {
                }
            }
        }
        return 90;
    }

    boolean e() {
        boolean z = false;
        if (!this.g.isConvex() || this.g.isFromFrontCamera()) {
            return false;
        }
        if (this.x == null) {
            e eVar;
            if (this.F.f() != null) {
                eVar = new e(this.F.f(), this.g);
            } else if (this.F.g() != null) {
                eVar = new e(this.F.g(), this.g);
            }
            this.x = eVar;
        }
        if (this.x != null) {
            z = true;
        }
        return z;
    }

    boolean f() {
        long currentTimeMillis = System.currentTimeMillis();
        synchronized (this.c) {
            if ((this.v <= 0) || Math.abs(((double) (currentTimeMillis - this.v)) / 1000.0d) >= 0.03333333333333333d) {
                this.v = currentTimeMillis;
                return true;
            }
            return false;
        }
    }

    boolean g() {
        Object obj = null;
        if (this.u) {
            if (this.t <= 0) {
                obj = 1;
            }
            if (obj == null && ((double) (System.currentTimeMillis() - this.t)) * 0.001d > WeightedLatLng.DEFAULT_INTENSITY) {
                i();
            }
        }
        return this.e;
    }

    public void h() {
        this.E.a();
    }

    void i() {
        this.u = false;
        this.d.d = false;
        this.d.k = 0.0d;
        this.d.l = 0.0d;
        this.t = 0;
        this.d.f.a = 0.0f;
        this.d.f.b = 0.0f;
        this.e = true;
    }

    float j() {
        float o = ((float) ((o() - n()) + 1)) / ((float) (((double) ((float) this.r)) * 0.66d));
        if (((double) o) > 0.25d) {
            o = 0.25f;
        }
        return ((double) o) < 0.1d ? 0.1f : o;
    }

    boolean k() {
        return this.e;
    }

    boolean l() {
        return this.f;
    }
}
