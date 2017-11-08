package com.loc;

import android.text.TextUtils;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.autonavi.aps.amapapi.model.AmapLoc;

/* compiled from: LocFilter */
public class bz {
    private static bz a = null;
    private AmapLoc b = null;
    private long c = 0;
    private long d = 0;

    private bz() {
    }

    public static synchronized bz a() {
        bz bzVar;
        synchronized (bz.class) {
            if (a == null) {
                a = new bz();
            }
            bzVar = a;
        }
        return bzVar;
    }

    private AmapLoc c(AmapLoc amapLoc) {
        if (cw.a(amapLoc)) {
            if (amapLoc.b() == 5 || amapLoc.b() == 6) {
                amapLoc.a(2);
            }
        }
        return amapLoc;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized AmapLoc a(AmapLoc amapLoc) {
        if (cw.a(this.b)) {
            if (cw.a(amapLoc)) {
                if (amapLoc.k() == this.b.k() && amapLoc.j() < 300.0f) {
                    return amapLoc;
                }
                if (amapLoc.g().equals(GeocodeSearch.GPS)) {
                    this.c = cw.b();
                    this.b = amapLoc;
                    return this.b;
                } else if (amapLoc.B() == this.b.B()) {
                    if (!amapLoc.A().equals(this.b.A())) {
                        if (!TextUtils.isEmpty(amapLoc.A())) {
                            this.c = cw.b();
                            this.b = amapLoc;
                            return this.b;
                        }
                    }
                    float a = cw.a(amapLoc, this.b);
                    float j = this.b.j();
                    float j2 = amapLoc.j();
                    float f = j2 - j;
                    long b = cw.b();
                    long j3 = b - this.c;
                    if (j < 101.0f) {
                    }
                    if (j <= 299.0f || j2 <= 299.0f) {
                        if (j2 >= 100.0f || j <= 299.0f) {
                            if (j2 <= 299.0f) {
                                this.d = 0;
                            }
                            if (a >= 10.0f || ((double) a) <= 0.1d) {
                                if (f >= 300.0f) {
                                    if ((j3 < 30000 ? 1 : null) == null) {
                                        this.c = cw.b();
                                        this.b = amapLoc;
                                        return this.b;
                                    }
                                    this.b = c(this.b);
                                    return this.b;
                                }
                                this.c = cw.b();
                                this.b = amapLoc;
                                return this.b;
                            } else if (f >= -300.0f) {
                                this.b = c(this.b);
                                return this.b;
                            } else if (j / j2 >= 2.0f) {
                                this.c = b;
                                this.b = amapLoc;
                                return this.b;
                            } else {
                                this.b = c(this.b);
                                return this.b;
                            }
                        }
                        this.c = b;
                        this.b = amapLoc;
                        this.d = 0;
                        return this.b;
                    }
                    if (this.d == 0) {
                        this.d = b;
                    } else {
                        if ((b - this.d <= 30000 ? 1 : null) == null) {
                            this.c = b;
                            this.b = amapLoc;
                            this.d = 0;
                            return this.b;
                        }
                    }
                    this.b = c(this.b);
                    return this.b;
                } else {
                    this.c = cw.b();
                    this.b = amapLoc;
                    return this.b;
                }
            }
        }
        this.c = cw.b();
        this.b = amapLoc;
        return this.b;
    }

    public AmapLoc b(AmapLoc amapLoc) {
        return amapLoc;
    }

    public synchronized void b() {
        this.b = null;
        this.c = 0;
        this.d = 0;
    }
}
