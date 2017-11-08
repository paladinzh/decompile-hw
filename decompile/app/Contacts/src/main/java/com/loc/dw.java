package com.loc;

import android.location.GpsSatellite;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import com.amap.api.services.geocoder.GeocodeSearch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/* compiled from: Unknown */
public final class dw implements Listener, NmeaListener {
    private long a = 0;
    private long b = 0;
    private boolean c = false;
    private List d = new ArrayList();
    private String e = null;
    private String f = null;
    private String g = null;
    private /* synthetic */ db h;

    protected dw(db dbVar) {
        this.h = dbVar;
    }

    public final void a(String str) {
        if (!(System.currentTimeMillis() - this.b <= 400)) {
            if (this.c && this.d.size() > 0) {
                try {
                    eh ehVar = new eh(this.d, this.e, null, this.g);
                    if (ehVar.a()) {
                        this.h.L = db.a(this.h, ehVar, this.h.I);
                        if (this.h.L > 0) {
                            db.a(this.h, String.format(Locale.CHINA, "&nmea=%.1f|%.1f&g_tp=%d", new Object[]{Double.valueOf(ehVar.c()), Double.valueOf(ehVar.b()), Integer.valueOf(this.h.L)}));
                        }
                    } else {
                        this.h.L = 0;
                    }
                } catch (Exception e) {
                    this.h.L = 0;
                }
                this.d.clear();
                this.g = null;
                this.f = null;
                this.e = null;
                this.c = false;
            }
            if (this.d != null && this.d.size() > 100) {
                this.d.clear();
            }
        }
        if (str.startsWith("$GPGGA")) {
            this.c = true;
            this.e = str.trim();
        } else if (str.startsWith("$GPGSV")) {
            this.d.add(str.trim());
        } else if (str.startsWith("$GPGSA")) {
            this.g = str.trim();
        }
        this.b = System.currentTimeMillis();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onGpsStatusChanged(int i) {
        int i2 = 0;
        try {
            if (this.h.q != null) {
                switch (i) {
                    case 2:
                        this.h.K = 0;
                        break;
                    case 4:
                        if (!db.a) {
                            if ((System.currentTimeMillis() - this.a >= 10000 ? 1 : 0) == 0) {
                                return;
                            }
                        }
                        if (this.h.G != null) {
                            this.h.q.getGpsStatus(this.h.G);
                        } else {
                            this.h.G = this.h.q.getGpsStatus(null);
                        }
                        this.h.H = 0;
                        this.h.I = 0;
                        this.h.J = new HashMap();
                        int i3 = 0;
                        int i4 = 0;
                        for (GpsSatellite gpsSatellite : this.h.G.getSatellites()) {
                            i3++;
                            if (gpsSatellite.usedInFix()) {
                                i4++;
                            }
                            if (gpsSatellite.getSnr() > 0.0f) {
                                i2++;
                            }
                            if (gpsSatellite.getSnr() >= ((float) db.U)) {
                                this.h.I = this.h.I + 1;
                            }
                        }
                        if (this.h.m != -1) {
                            if (i4 < 4 || this.h.m >= 4) {
                                if (i4 < 4) {
                                    if (this.h.m < 4) {
                                    }
                                }
                                this.h.K = i2;
                                this.h.a(this.h.J);
                                if (!db.a) {
                                    if (i4 > 3 || i3 > 15) {
                                        if (this.h.q.getLastKnownLocation(GeocodeSearch.GPS) != null) {
                                            this.a = System.currentTimeMillis();
                                            break;
                                        }
                                    }
                                    break;
                                }
                                return;
                            }
                        }
                        this.h.m = i4;
                        if (i4 >= 4) {
                            if (this.h.r != null) {
                                this.h.r.u();
                            }
                        } else if (this.h.r != null) {
                            this.h.r.v();
                        }
                        this.h.K = i2;
                        this.h.a(this.h.J);
                        if (!db.a) {
                            if (i4 > 3) {
                                break;
                            }
                            if (this.h.q.getLastKnownLocation(GeocodeSearch.GPS) != null) {
                                this.a = System.currentTimeMillis();
                            }
                        } else {
                            return;
                        }
                        break;
                }
            }
        } catch (Exception e) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onNmeaReceived(long j, String str) {
        try {
            if (db.a && str != null && !str.equals("") && str.length() >= 9 && str.length() <= 150 && this.h.B != null) {
                this.h.B.sendMessage(this.h.B.obtainMessage(1, str));
            }
        } catch (Exception e) {
        }
    }
}
