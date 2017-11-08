package com.fyusion.sdk.viewer;

import com.fyusion.sdk.viewer.internal.a.a.c;

/* compiled from: Unknown */
public class h {
    private int a = 0;
    private int b = 0;
    private a c = a.TILT_DIRECTION_UNDECIDED;
    private boolean d;
    private int e;
    private int f;
    private boolean g;
    private boolean h;
    private c i;

    /* compiled from: Unknown */
    private enum a {
        TILT_DIRECTION_UNDECIDED,
        TILT_DIRECTION_FORWARD,
        TILT_DIRECTION_BACKWARD
    }

    public void a() {
        com.fyusion.sdk.viewer.internal.a.a.a().a(this.i);
    }

    public void a(int i, int i2) {
        if (this.d) {
            int i3;
            if (this.c == a.TILT_DIRECTION_UNDECIDED) {
                if (this.g) {
                    this.e = Math.max(this.e, i);
                } else {
                    this.e = i;
                    this.g = true;
                }
                if (this.h) {
                    this.f = Math.min(this.f, i);
                } else {
                    this.f = i;
                    this.h = true;
                }
            }
            int i4;
            if (this.c == a.TILT_DIRECTION_UNDECIDED && this.a != i) {
                i3 = this.b;
                i4 = this.a;
                this.b = i3 + (i - i4);
            } else {
                if (this.a >= i) {
                    if (this.a > i) {
                        if (this.c != a.TILT_DIRECTION_FORWARD) {
                            if (this.c == a.TILT_DIRECTION_BACKWARD) {
                                i3 = this.b;
                                i4 = this.a;
                                this.b = i3 + (i - i4);
                            }
                        }
                    }
                } else if (this.c != a.TILT_DIRECTION_BACKWARD) {
                    if (this.c == a.TILT_DIRECTION_FORWARD) {
                        i3 = this.b;
                        i4 = this.a;
                        this.b = i3 + (i - i4);
                    }
                }
                this.b = 0;
            }
            i3 = 10;
            if (i2 > 0) {
                i3 = (int) Math.min(10.0d, Math.ceil((double) (((float) i2) * 0.8f)));
            }
            if (Math.abs(this.b) + 1 < i3) {
                if (this.c == a.TILT_DIRECTION_UNDECIDED) {
                    if ((this.e - this.f) + 1 < i3) {
                    }
                }
                this.a = i;
            }
            this.c = this.b >= 0 ? a.TILT_DIRECTION_BACKWARD : a.TILT_DIRECTION_FORWARD;
            this.i.a();
            this.b = 0;
            this.d = false;
            this.e = 0;
            this.f = 0;
            this.g = false;
            this.h = false;
            this.a = i;
        }
        this.a = i;
        this.d = true;
    }

    public void a(String str) {
        com.fyusion.sdk.viewer.internal.a.a a = com.fyusion.sdk.viewer.internal.a.a.a();
        a.a(this.i);
        this.i = a.a(str);
        this.a = 0;
        this.b = 0;
        this.c = a.TILT_DIRECTION_UNDECIDED;
        this.d = false;
        this.e = 0;
        this.f = 0;
        this.h = false;
        this.g = false;
    }
}
