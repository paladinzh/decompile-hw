package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.Log;
import com.amap.api.mapcore.util.ah.a;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import java.io.File;

/* compiled from: CityObject */
public class g extends OfflineMapCity implements ag, q {
    public static final Creator<g> l = new h();
    public al a;
    public al b;
    public al c;
    public al d;
    public al e;
    public al f;
    public al g;
    public al h;
    al i;
    Context j;
    boolean k;
    private String m;
    private String n;
    private long o;

    public void a(String str) {
        this.n = str;
    }

    public String a() {
        return this.n;
    }

    public String b() {
        return getUrl();
    }

    public g(Context context, OfflineMapCity offlineMapCity) {
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
        s();
    }

    public g(Context context, int i) {
        this.a = new an(6, this);
        this.b = new at(2, this);
        this.c = new ap(0, this);
        this.d = new ar(3, this);
        this.e = new as(1, this);
        this.f = new am(4, this);
        this.g = new aq(7, this);
        this.h = new ao(-1, this);
        this.m = null;
        this.n = "";
        this.k = false;
        this.o = 0;
        this.j = context;
        a(i);
    }

    public void a(int i) {
        switch (i) {
            case -1:
                this.i = this.h;
                break;
            case 0:
                this.i = this.c;
                break;
            case 1:
                this.i = this.e;
                break;
            case 2:
                this.i = this.b;
                break;
            case 3:
                this.i = this.d;
                break;
            case 4:
                this.i = this.f;
                break;
            case 6:
                this.i = this.a;
                break;
            case 7:
                this.i = this.g;
                break;
            default:
                if (i < 0) {
                    this.i = this.h;
                    break;
                }
                break;
        }
        setState(i);
    }

    public void a(al alVar) {
        this.i = alVar;
        setState(alVar.b());
    }

    public al c() {
        return this.i;
    }

    public void d() {
        i a = i.a(this.j);
        if (a != null) {
            a.c(this);
        }
    }

    public void e() {
        i a = i.a(this.j);
        if (a != null) {
            a.e(this);
            d();
        }
    }

    public void f() {
        af.a("CityOperation current State==>" + c().b());
        if (this.i.equals(this.d)) {
            this.i.e();
        } else if (this.i.equals(this.c)) {
            this.i.f();
        } else if (this.i.equals(this.g) || this.i.equals(this.h)) {
            j();
            this.k = true;
        } else {
            c().c();
        }
    }

    public void g() {
        this.i.g();
    }

    public void h() {
        this.i.a();
        if (this.k) {
            this.i.c();
        }
        this.k = false;
    }

    public void i() {
        if (this.i.equals(this.f)) {
            this.i.h();
        } else {
            this.i.h();
        }
    }

    public void j() {
        i a = i.a(this.j);
        if (a != null) {
            a.a(this);
        }
    }

    public void k() {
        i a = i.a(this.j);
        if (a != null) {
            a.b(this);
        }
    }

    public void l() {
        i a = i.a(this.j);
        if (a != null) {
            a.d(this);
        }
    }

    public void m() {
        this.o = 0;
        if (!this.i.equals(this.b)) {
            Log.e("state", "state must be waiting when download onStart");
        }
        this.i.d();
    }

    public void a(long j, long j2) {
        long j3 = (100 * j2) / j;
        if (((int) j3) != getcompleteCode()) {
            setCompleteCode((int) j3);
            d();
        }
    }

    public void n() {
        if (!this.i.equals(this.c)) {
            Log.e("state", "state must be Loading when download onFinish");
        }
        this.i.i();
    }

    public void a(a aVar) {
        if (this.i.equals(this.c) || this.i.equals(this.b)) {
            this.i.g();
        }
    }

    public void o() {
        e();
    }

    public void p() {
        this.o = 0;
        setCompleteCode(0);
        if (this.i.equals(this.e)) {
            this.i.d();
        } else {
            this.i.d();
        }
    }

    public void q() {
        if (this.i.equals(this.e)) {
            this.i.g();
        } else {
            this.i.g();
        }
    }

    public void a(long j) {
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - this.o <= 500 ? 1 : null) == null) {
            if (((int) j) > getcompleteCode()) {
                setCompleteCode((int) j);
                d();
            }
            this.o = currentTimeMillis;
        }
    }

    public void b(String str) {
        Object t;
        Object u;
        if (this.i.equals(this.e)) {
            this.n = str;
            t = t();
            u = u();
        } else {
            this.n = str;
            t = t();
            u = u();
        }
        if (TextUtils.isEmpty(t) || TextUtils.isEmpty(u)) {
            q();
            return;
        }
        File file = new File(u + "/");
        File file2 = new File(bj.a(this.j) + "vmap/");
        File file3 = new File(bj.a(this.j));
        if (!file3.exists()) {
            file3.mkdir();
        }
        if (!file2.exists()) {
            file2.mkdir();
        }
        a(file, file2, t);
    }

    public void r() {
        e();
    }

    protected void s() {
        this.m = i.a + getAdcode() + ".zip" + ".tmp";
    }

    public String t() {
        return !TextUtils.isEmpty(this.m) ? this.m.substring(0, this.m.lastIndexOf(".")) : null;
    }

    public String u() {
        if (TextUtils.isEmpty(this.m)) {
            return null;
        }
        String t = t();
        return t.substring(0, t.lastIndexOf(46));
    }

    private void a(final File file, File file2, final String str) {
        new z().a(file, file2, -1, af.a(file), new z.a(this) {
            final /* synthetic */ g c;

            public void a(String str, String str2, float f) {
                Object obj = null;
                int i = (int) ((((double) f) * 0.39d) + 60.0d);
                if (i - this.c.getcompleteCode() > 0) {
                    if (System.currentTimeMillis() - this.c.o <= 1000) {
                        obj = 1;
                    }
                    if (obj == null) {
                        this.c.setCompleteCode(i);
                        this.c.o = System.currentTimeMillis();
                    }
                }
            }

            public void a(String str, String str2) {
            }

            public void b(String str, String str2) {
                try {
                    new File(str).delete();
                    af.b(file);
                    this.c.setCompleteCode(100);
                    this.c.i.i();
                } catch (Exception e) {
                    this.c.i.g();
                }
            }

            public void a(String str, String str2, int i) {
                this.c.i.g();
            }
        });
    }

    public boolean v() {
        return ((double) af.a()) < (((double) getSize()) * 2.5d) - ((double) (((long) getcompleteCode()) * getSize())) ? false : false;
    }

    public s w() {
        setState(this.i.b());
        s sVar = new s((OfflineMapCity) this, this.j);
        sVar.a(a());
        af.a("vMapFileNames: " + a());
        return sVar;
    }

    public void a(s sVar) {
        a(sVar.l);
        setCity(sVar.e());
        setSize(sVar.i());
        setVersion(sVar.f());
        setCompleteCode(sVar.j());
        setAdcode(sVar.g());
        setUrl(sVar.h());
        String c = sVar.c();
        if (c != null && c.length() > 0) {
            a(c);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(this.n);
    }

    public g(Parcel parcel) {
        super(parcel);
        this.a = new an(6, this);
        this.b = new at(2, this);
        this.c = new ap(0, this);
        this.d = new ar(3, this);
        this.e = new as(1, this);
        this.f = new am(4, this);
        this.g = new aq(7, this);
        this.h = new ao(-1, this);
        this.m = null;
        this.n = "";
        this.k = false;
        this.o = 0;
        this.n = parcel.readString();
    }

    public boolean x() {
        return v();
    }

    public String y() {
        StringBuffer stringBuffer = new StringBuffer(getAdcode());
        stringBuffer.append(".zip");
        return stringBuffer.toString();
    }

    public String z() {
        return getAdcode();
    }

    public String A() {
        return t();
    }

    public String B() {
        return u();
    }
}
