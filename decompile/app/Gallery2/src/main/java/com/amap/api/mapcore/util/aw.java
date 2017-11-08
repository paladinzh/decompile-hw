package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.amap.api.mapcore.util.bw.a;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import java.io.File;

/* compiled from: CityObject */
public class aw extends OfflineMapCity implements bf, bv {
    public static final Creator<aw> o = new Creator<aw>() {
        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return a(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return a(i);
        }

        public aw a(Parcel parcel) {
            return new aw(parcel);
        }

        public aw[] a(int i) {
            return new aw[i];
        }
    };
    public ca a;
    public ca b;
    public ca c;
    public ca d;
    public ca e;
    public ca f;
    public ca g;
    public ca h;
    public ca i;
    public ca j;
    public ca k;
    ca l;
    Context m;
    boolean n;
    private String p;
    private String q;
    private long r;

    /* compiled from: CityObject */
    /* renamed from: com.amap.api.mapcore.util.aw$3 */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] a = new int[a.values().length];

        static {
            try {
                a[a.amap_exception.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[a.file_io_exception.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[a.network_exception.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public void a(String str) {
        this.q = str;
    }

    public String a() {
        return this.q;
    }

    public String b() {
        return getUrl();
    }

    public aw(Context context, OfflineMapCity offlineMapCity) {
        this(context, offlineMapCity.getState());
        setCity(offlineMapCity.getCity());
        setUrl(offlineMapCity.getUrl());
        setState(offlineMapCity.getState());
        setCompleteCode(offlineMapCity.getcompleteCode());
        setAdcode(offlineMapCity.getAdcode());
        setVersion(offlineMapCity.getVersion());
        setSize(offlineMapCity.getSize());
        setCode(offlineMapCity.getCode());
        setJianpin(offlineMapCity.getJianpin());
        setPinyin(offlineMapCity.getPinyin());
        t();
    }

    public aw(Context context, int i) {
        this.a = new cc(6, this);
        this.b = new ci(2, this);
        this.c = new ce(0, this);
        this.d = new cg(3, this);
        this.e = new ch(1, this);
        this.f = new cb(4, this);
        this.g = new cf(7, this);
        this.h = new cd(-1, this);
        this.i = new cd(101, this);
        this.j = new cd(102, this);
        this.k = new cd(OfflineMapStatus.EXCEPTION_SDCARD, this);
        this.p = null;
        this.q = "";
        this.n = false;
        this.r = 0;
        this.m = context;
        a(i);
    }

    public void a(int i) {
        switch (i) {
            case -1:
                this.l = this.h;
                break;
            case 0:
                this.l = this.c;
                break;
            case 1:
                this.l = this.e;
                break;
            case 2:
                this.l = this.b;
                break;
            case 3:
                this.l = this.d;
                break;
            case 4:
                this.l = this.f;
                break;
            case 6:
                this.l = this.a;
                break;
            case 7:
                this.l = this.g;
                break;
            case 101:
                this.l = this.i;
                break;
            case 102:
                this.l = this.j;
                break;
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                this.l = this.k;
                break;
            default:
                if (i < 0) {
                    this.l = this.h;
                    break;
                }
                break;
        }
        setState(i);
    }

    public void a(ca caVar) {
        this.l = caVar;
        setState(caVar.b());
    }

    public ca c() {
        return this.l;
    }

    public void d() {
        ax a = ax.a(this.m);
        if (a != null) {
            a.c(this);
        }
    }

    public void e() {
        ax a = ax.a(this.m);
        if (a != null) {
            a.e(this);
            d();
        }
    }

    public void f() {
        bu.a("CityOperation current State==>" + c().b());
        if (this.l.equals(this.d)) {
            this.l.e();
        } else if (this.l.equals(this.c)) {
            this.l.f();
        } else if (this.l.equals(this.g) || this.l.equals(this.h)) {
            k();
            this.n = true;
        } else if (this.l.equals(this.j) || this.l.equals(this.i) || this.l.a(this.k)) {
            this.l.d();
        } else {
            c().c();
        }
    }

    public void g() {
        this.l.f();
    }

    public void h() {
        this.l.a(this.k.b());
    }

    public void i() {
        this.l.a();
        if (this.n) {
            this.l.c();
        }
        this.n = false;
    }

    public void j() {
        if (this.l.equals(this.f)) {
            this.l.g();
        } else {
            this.l.g();
        }
    }

    public void k() {
        ax a = ax.a(this.m);
        if (a != null) {
            a.a(this);
        }
    }

    public void l() {
        ax a = ax.a(this.m);
        if (a != null) {
            a.b(this);
        }
    }

    public void m() {
        ax a = ax.a(this.m);
        if (a != null) {
            a.d(this);
        }
    }

    public void n() {
        this.r = 0;
        if (!this.l.equals(this.b)) {
            bu.a("state must be waiting when download onStart");
        }
        this.l.d();
    }

    public void a(long j, long j2) {
        long j3 = (100 * j2) / j;
        if (((int) j3) != getcompleteCode()) {
            setCompleteCode((int) j3);
            d();
        }
    }

    public void o() {
        if (!this.l.equals(this.c)) {
            bu.a("state must be Loading when download onFinish");
        }
        this.l.h();
    }

    public void a(a aVar) {
        int i = 6;
        switch (AnonymousClass3.a[aVar.ordinal()]) {
            case 1:
                i = this.j.b();
                break;
            case 2:
                i = this.k.b();
                break;
            case 3:
                i = this.i.b();
                break;
        }
        if (this.l.equals(this.c) || this.l.equals(this.b)) {
            this.l.a(i);
        }
    }

    public void p() {
        e();
    }

    public void q() {
        this.r = 0;
        setCompleteCode(0);
        if (this.l.equals(this.e)) {
            this.l.d();
        } else {
            this.l.d();
        }
    }

    public void r() {
        if (this.l.equals(this.e)) {
            this.l.a(this.h.b());
        } else {
            this.l.a(this.h.b());
        }
    }

    public void a(long j) {
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - this.r <= 500 ? 1 : null) == null) {
            if (((int) j) > getcompleteCode()) {
                setCompleteCode((int) j);
                d();
            }
            this.r = currentTimeMillis;
        }
    }

    public void b(String str) {
        Object u;
        Object v;
        if (this.l.equals(this.e)) {
            this.q = str;
            u = u();
            v = v();
        } else {
            this.q = str;
            u = u();
            v = v();
        }
        if (TextUtils.isEmpty(u) || TextUtils.isEmpty(v)) {
            r();
            return;
        }
        File file = new File(v + "/");
        File file2 = new File(eh.a(this.m) + "vmap/");
        File file3 = new File(eh.a(this.m));
        if (!file3.exists()) {
            file3.mkdir();
        }
        if (!file2.exists()) {
            file2.mkdir();
        }
        a(file, file2, u);
    }

    public void s() {
        e();
    }

    protected void t() {
        this.p = ax.a + getPinyin() + ".zip" + ".tmp";
    }

    public String u() {
        return !TextUtils.isEmpty(this.p) ? this.p.substring(0, this.p.lastIndexOf(".")) : null;
    }

    public String v() {
        if (TextUtils.isEmpty(this.p)) {
            return null;
        }
        String u = u();
        return u.substring(0, u.lastIndexOf(46));
    }

    private void a(final File file, File file2, final String str) {
        new bo().a(file, file2, -1, bu.a(file), new bo.a(this) {
            final /* synthetic */ aw c;

            public void a(String str, String str2, float f) {
                Object obj = null;
                int i = (int) ((((double) f) * 0.39d) + 60.0d);
                if (i - this.c.getcompleteCode() > 0) {
                    if (System.currentTimeMillis() - this.c.r <= 1000) {
                        obj = 1;
                    }
                    if (obj == null) {
                        this.c.setCompleteCode(i);
                        this.c.r = System.currentTimeMillis();
                    }
                }
            }

            public void a(String str, String str2) {
            }

            public void b(String str, String str2) {
                try {
                    new File(str).delete();
                    bu.b(file);
                    this.c.setCompleteCode(100);
                    this.c.l.h();
                } catch (Exception e) {
                    this.c.l.a(this.c.k.b());
                }
            }

            public void a(String str, String str2, int i) {
                this.c.l.a(this.c.k.b());
            }
        });
    }

    public boolean w() {
        return ((double) bu.a()) < (((double) getSize()) * 2.5d) - ((double) (((long) getcompleteCode()) * getSize())) ? false : false;
    }

    public bh x() {
        setState(this.l.b());
        bh bhVar = new bh((OfflineMapCity) this, this.m);
        bhVar.a(a());
        bu.a("vMapFileNames: " + a());
        return bhVar;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.q);
    }

    public aw(Parcel parcel) {
        super(parcel);
        this.a = new cc(6, this);
        this.b = new ci(2, this);
        this.c = new ce(0, this);
        this.d = new cg(3, this);
        this.e = new ch(1, this);
        this.f = new cb(4, this);
        this.g = new cf(7, this);
        this.h = new cd(-1, this);
        this.i = new cd(101, this);
        this.j = new cd(102, this);
        this.k = new cd(OfflineMapStatus.EXCEPTION_SDCARD, this);
        this.p = null;
        this.q = "";
        this.n = false;
        this.r = 0;
        this.q = parcel.readString();
    }

    public boolean y() {
        return w();
    }

    public String z() {
        StringBuffer stringBuffer = new StringBuffer(getPinyin());
        stringBuffer.append(".zip");
        return stringBuffer.toString();
    }

    public String A() {
        return getAdcode();
    }

    public String B() {
        return u();
    }

    public String C() {
        return v();
    }

    public ca b(int i) {
        switch (i) {
            case 101:
                return this.i;
            case 102:
                return this.j;
            case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                return this.k;
            default:
                return this.h;
        }
    }
}
