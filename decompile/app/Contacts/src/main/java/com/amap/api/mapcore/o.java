package com.amap.api.mapcore;

import android.content.Context;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.WeightedLatLng;

/* compiled from: CameraAnimator */
public class o {
    private static float J = ((float) (Math.log(0.78d) / Math.log(0.9d)));
    private static final float[] K = new float[101];
    private static final float[] L = new float[101];
    private static float Q = 8.0f;
    private static float R;
    private float A;
    private float B;
    private boolean C;
    private Interpolator D;
    private boolean E;
    private float F;
    private float G;
    private int H;
    private float I;
    private float M;
    private final float N;
    private float O;
    private boolean P;
    private int a;
    private int b;
    private int c;
    private float d;
    private float e;
    private float f;
    private int g;
    private int h;
    private float i;
    private float j;
    private float k;
    private int l;
    private int m;
    private int n;
    private int o;
    private int p;
    private int q;
    private float r;
    private float s;
    private float t;
    private long u;
    private long v;
    private float w;
    private float x;
    private float y;
    private float z;

    static {
        float f = 0.0f;
        int i = 0;
        float f2 = 0.0f;
        while (i < 100) {
            float f3;
            float f4 = ((float) i) / 100.0f;
            float f5 = 1.0f;
            float f6 = f2;
            while (true) {
                f2 = ((f5 - f6) / 2.0f) + f6;
                f3 = (3.0f * f2) * (1.0f - f2);
                float f7 = ((((1.0f - f2) * 0.175f) + (0.35000002f * f2)) * f3) + ((f2 * f2) * f2);
                if (((double) Math.abs(f7 - f4)) < 1.0E-5d) {
                    break;
                } else if (f7 > f4) {
                    f5 = f2;
                } else {
                    f6 = f2;
                }
            }
            K[i] = (f2 * (f2 * f2)) + (f3 * (((1.0f - f2) * 0.5f) + f2));
            f5 = 1.0f;
            while (true) {
                f2 = ((f5 - f) / 2.0f) + f;
                f3 = (3.0f * f2) * (1.0f - f2);
                f7 = ((((1.0f - f2) * 0.5f) + f2) * f3) + ((f2 * f2) * f2);
                if (((double) Math.abs(f7 - f4)) < 1.0E-5d) {
                    break;
                } else if (f7 > f4) {
                    f5 = f2;
                } else {
                    f = f2;
                }
            }
            L[i] = (f2 * (f2 * f2)) + ((((1.0f - f2) * 0.175f) + (0.35000002f * f2)) * f3);
            i++;
            f2 = f6;
        }
        float[] fArr = K;
        L[100] = 1.0f;
        fArr[100] = 1.0f;
        R = 1.0f;
        R = 1.0f / a(1.0f);
    }

    public o(Context context) {
        this(context, null);
    }

    public o(Context context, Interpolator interpolator) {
        boolean z;
        if (context.getApplicationInfo().targetSdkVersion < 11) {
            z = false;
        } else {
            z = true;
        }
        this(context, interpolator, z);
    }

    public o(Context context, Interpolator interpolator, boolean z) {
        this.I = ViewConfiguration.getScrollFriction();
        this.C = true;
        this.D = interpolator;
        this.N = context.getResources().getDisplayMetrics().density * 160.0f;
        this.M = b(ViewConfiguration.getScrollFriction());
        this.E = z;
        this.O = b(0.84f);
    }

    public void a(Interpolator interpolator) {
        this.D = interpolator;
    }

    private float b(float f) {
        return (this.N * 386.0878f) * f;
    }

    public final boolean a() {
        return this.C;
    }

    public final void a(boolean z) {
        this.C = z;
    }

    public final int b() {
        return this.p;
    }

    public final int c() {
        return this.q;
    }

    public final float d() {
        return this.r;
    }

    public final float e() {
        return this.s;
    }

    public final float f() {
        return this.t;
    }

    public float g() {
        if (this.a != 1) {
            return this.F - ((this.M * ((float) i())) / 2000.0f);
        }
        return this.G;
    }

    public boolean h() {
        boolean z = false;
        if (this.C) {
            return false;
        }
        int currentAnimationTimeMillis = (int) (AnimationUtils.currentAnimationTimeMillis() - this.u);
        if (((long) currentAnimationTimeMillis) >= this.v) {
            z = true;
        }
        if (!z) {
            float f;
            switch (this.a) {
                case 1:
                    float f2 = ((float) currentAnimationTimeMillis) / ((float) this.v);
                    int i = (int) (100.0f * f2);
                    float f3 = 1.0f;
                    f = 0.0f;
                    if (i < 100) {
                        f3 = ((float) i) / 100.0f;
                        f = ((float) (i + 1)) / 100.0f;
                        float f4 = K[i];
                        f = (K[i + 1] - f4) / (f - f3);
                        f3 = ((f2 - f3) * f) + f4;
                    }
                    this.G = ((f * ((float) this.H)) / ((float) this.v)) * 1000.0f;
                    this.p = this.b + Math.round(((float) (this.g - this.b)) * f3);
                    this.p = Math.min(this.p, this.m);
                    this.p = Math.max(this.p, this.l);
                    this.q = this.c + Math.round(f3 * ((float) (this.h - this.c)));
                    this.q = Math.min(this.q, this.o);
                    this.q = Math.max(this.q, this.n);
                    if (this.p == this.g && this.q == this.h) {
                        this.C = true;
                        break;
                    }
                case 2:
                    f = ((float) currentAnimationTimeMillis) * this.w;
                    if (this.D != null) {
                        f = this.D.getInterpolation(f);
                    } else {
                        f = a(f);
                    }
                    this.p = this.b + Math.round(this.x * f);
                    this.q = this.c + Math.round(this.y * f);
                    this.r = this.d + (this.z * f);
                    this.s = this.e + (this.A * f);
                    this.t = (f * this.B) + this.f;
                    break;
            }
        }
        this.p = this.g;
        this.q = this.h;
        this.r = this.i;
        this.s = this.j;
        this.t = this.k;
        this.C = true;
        return true;
    }

    public void a(int i, int i2, float f, float f2, float f3, int i3, int i4, float f4, float f5, float f6, long j) {
        this.a = 2;
        this.C = false;
        this.v = j;
        this.u = AnimationUtils.currentAnimationTimeMillis();
        this.b = i;
        this.c = i2;
        this.d = f;
        this.e = f2;
        this.f = f3;
        this.g = i + i3;
        this.h = i2 + i4;
        this.i = f + f4;
        this.j = f2 + f5;
        this.k = f3 + f6;
        this.x = (float) i3;
        this.y = (float) i4;
        this.z = f4;
        this.A = f5;
        this.B = f6;
        this.w = 1.0f / ((float) this.v);
    }

    public void a(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        float g;
        float f;
        float f2;
        if (this.E && !this.C) {
            g = g();
            f = (float) (this.g - this.b);
            f2 = (float) (this.h - this.c);
            float sqrt = (float) Math.sqrt((double) ((f * f) + (f2 * f2)));
            f = (f / sqrt) * g;
            g *= f2 / sqrt;
            if (Math.signum((float) i3) == Math.signum(f) && Math.signum((float) i4) == Math.signum(g)) {
                i3 = (int) (f + ((float) i3));
                i4 = (int) (g + ((float) i4));
            }
        }
        this.a = 1;
        this.C = false;
        f2 = (float) Math.sqrt((double) ((i3 * i3) + (i4 * i4)));
        this.F = f2;
        this.v = (long) d(f2);
        this.u = AnimationUtils.currentAnimationTimeMillis();
        this.b = i;
        this.c = i2;
        g = f2 == 0.0f ? 1.0f : ((float) i3) / f2;
        f = f2 == 0.0f ? 1.0f : ((float) i4) / f2;
        double e = e(f2);
        this.H = (int) (((double) Math.signum(f2)) * e);
        this.l = i5;
        this.m = i6;
        this.n = i7;
        this.o = i8;
        this.g = ((int) Math.round(((double) g) * e)) + i;
        this.g = Math.min(this.g, this.m);
        this.g = Math.max(this.g, this.l);
        this.h = ((int) Math.round(((double) f) * e)) + i2;
        this.h = Math.min(this.h, this.o);
        this.h = Math.max(this.h, this.n);
    }

    private double c(float f) {
        return Math.log((double) ((Math.abs(f) * 0.35f) / (this.I * this.O)));
    }

    private int d(float f) {
        return (int) (Math.exp(c(f) / (((double) J) - WeightedLatLng.DEFAULT_INTENSITY)) * 1000.0d);
    }

    private double e(float f) {
        return Math.exp(c(f) * (((double) J) / (((double) J) - WeightedLatLng.DEFAULT_INTENSITY))) * ((double) (this.I * this.O));
    }

    static float a(float f) {
        float f2 = Q * f;
        if (f2 < 1.0f) {
            f2 -= 1.0f - ((float) Math.exp((double) (-f2)));
        } else {
            f2 = ((1.0f - ((float) Math.exp((double) (1.0f - f2)))) * 0.63212055f) + 0.36787945f;
        }
        return f2 * R;
    }

    public int i() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - this.u);
    }

    public final int j() {
        return this.a;
    }

    public void b(boolean z) {
        this.P = z;
    }

    public boolean k() {
        return this.P;
    }
}
