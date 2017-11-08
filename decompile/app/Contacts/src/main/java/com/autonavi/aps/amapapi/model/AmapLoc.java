package com.autonavi.aps.amapapi.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.loc.cw;
import com.loc.e;
import org.json.JSONObject;

@SuppressLint({"NewApi"})
public class AmapLoc implements Parcelable {
    public static final Creator<AmapLoc> CREATOR = new a();
    private String A = "";
    private String B = "";
    private int C = -1;
    private String D = "";
    private String E = "";
    private String F = "";
    private boolean G = true;
    private boolean H = true;
    private JSONObject I = null;
    private String a = "";
    private double b = 0.0d;
    private double c = 0.0d;
    private double d = 0.0d;
    private float e = 0.0f;
    private float f = 0.0f;
    private float g = 0.0f;
    private long h = 0;
    private String i = "new";
    private int j = 0;
    private String k = "success";
    private int l = 0;
    private String m = "";
    private String n = "";
    private String o = "";
    private String p = "";
    private String q = "";
    private String r = "";
    private String s = "";
    private String t = "";
    private String u = "";
    private String v = "";
    private String w = "";
    private String x = "";
    private String y = "";
    private String z = null;

    public AmapLoc(Parcel parcel) {
        boolean z = false;
        this.a = parcel.readString();
        this.i = parcel.readString();
        this.k = parcel.readString();
        this.j = parcel.readInt();
        this.g = parcel.readFloat();
        this.f = parcel.readFloat();
        this.e = parcel.readFloat();
        this.b = parcel.readDouble();
        this.c = parcel.readDouble();
        this.d = parcel.readDouble();
        this.h = parcel.readLong();
        this.n = parcel.readString();
        this.o = parcel.readString();
        this.p = parcel.readString();
        this.q = parcel.readString();
        this.r = parcel.readString();
        this.s = parcel.readString();
        this.t = parcel.readString();
        this.u = parcel.readString();
        this.v = parcel.readString();
        this.w = parcel.readString();
        this.x = parcel.readString();
        this.y = parcel.readString();
        this.z = parcel.readString();
        this.A = parcel.readString();
        this.B = parcel.readString();
        this.D = parcel.readString();
        this.m = parcel.readString();
        this.C = parcel.readInt();
        this.l = parcel.readInt();
        this.E = parcel.readString();
        this.G = parcel.readByte() != (byte) 0;
        if (parcel.readByte() != (byte) 0) {
            z = true;
        }
        this.H = z;
        this.F = parcel.readString();
    }

    public AmapLoc(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                if (cw.a(jSONObject, "provider")) {
                    c(jSONObject.getString("provider"));
                }
                if (cw.a(jSONObject, "lon")) {
                    a(jSONObject.getDouble("lon"));
                }
                if (cw.a(jSONObject, "lat")) {
                    b(jSONObject.getDouble("lat"));
                }
                if (cw.a(jSONObject, "altitude")) {
                    c(jSONObject.getDouble("altitude"));
                }
                if (cw.a(jSONObject, "acc")) {
                    z(jSONObject.getString("acc"));
                }
                if (cw.a(jSONObject, "accuracy")) {
                    a((float) jSONObject.getLong("accuracy"));
                }
                if (cw.a(jSONObject, "speed")) {
                    b((float) jSONObject.getLong("speed"));
                }
                if (cw.a(jSONObject, "dir")) {
                    c((float) jSONObject.getLong("dir"));
                }
                if (cw.a(jSONObject, "bearing")) {
                    c((float) jSONObject.getLong("bearing"));
                }
                if (cw.a(jSONObject, "type")) {
                    f(jSONObject.getString("type"));
                }
                if (cw.a(jSONObject, "retype")) {
                    g(jSONObject.getString("retype"));
                }
                if (cw.a(jSONObject, "citycode")) {
                    i(jSONObject.getString("citycode"));
                }
                if (cw.a(jSONObject, "desc")) {
                    j(jSONObject.getString("desc"));
                }
                if (cw.a(jSONObject, "adcode")) {
                    k(jSONObject.getString("adcode"));
                }
                if (cw.a(jSONObject, DistrictSearchQuery.KEYWORDS_COUNTRY)) {
                    l(jSONObject.getString(DistrictSearchQuery.KEYWORDS_COUNTRY));
                }
                if (cw.a(jSONObject, DistrictSearchQuery.KEYWORDS_PROVINCE)) {
                    m(jSONObject.getString(DistrictSearchQuery.KEYWORDS_PROVINCE));
                }
                if (cw.a(jSONObject, DistrictSearchQuery.KEYWORDS_CITY)) {
                    n(jSONObject.getString(DistrictSearchQuery.KEYWORDS_CITY));
                }
                if (cw.a(jSONObject, "road")) {
                    p(jSONObject.getString("road"));
                }
                if (cw.a(jSONObject, "street")) {
                    q(jSONObject.getString("street"));
                }
                if (cw.a(jSONObject, "number")) {
                    r(jSONObject.getString("number"));
                }
                if (cw.a(jSONObject, "poiname")) {
                    s(jSONObject.getString("poiname"));
                }
                if (cw.a(jSONObject, "aoiname")) {
                    t(jSONObject.getString("aoiname"));
                }
                if (cw.a(jSONObject, "errorCode")) {
                    b(jSONObject.getInt("errorCode"));
                }
                if (cw.a(jSONObject, "errorInfo")) {
                    a(jSONObject.getString("errorInfo"));
                }
                if (cw.a(jSONObject, "locationType")) {
                    a(jSONObject.getInt("locationType"));
                }
                if (cw.a(jSONObject, "locationDetail")) {
                    b(jSONObject.getString("locationDetail"));
                }
                if (cw.a(jSONObject, "cens")) {
                    u(jSONObject.getString("cens"));
                }
                if (cw.a(jSONObject, "poiid")) {
                    v(jSONObject.getString("poiid"));
                }
                if (cw.a(jSONObject, "pid")) {
                    v(jSONObject.getString("pid"));
                }
                if (cw.a(jSONObject, "floor")) {
                    w(jSONObject.getString("floor"));
                }
                if (cw.a(jSONObject, "flr")) {
                    w(jSONObject.getString("flr"));
                }
                if (cw.a(jSONObject, "coord")) {
                    x(jSONObject.getString("coord"));
                }
                if (cw.a(jSONObject, "mcell")) {
                    y(jSONObject.getString("mcell"));
                }
                if (cw.a(jSONObject, "time")) {
                    a(jSONObject.getLong("time"));
                }
                if (cw.a(jSONObject, DistrictSearchQuery.KEYWORDS_DISTRICT)) {
                    o(jSONObject.getString(DistrictSearchQuery.KEYWORDS_DISTRICT));
                }
                if (cw.a(jSONObject, "isOffset")) {
                    a(jSONObject.getBoolean("isOffset"));
                }
                if (cw.a(jSONObject, "isReversegeo")) {
                    b(jSONObject.getBoolean("isReversegeo"));
                }
            } catch (Throwable th) {
                e.a(th, "AmapLoc", "AmapLoc");
            }
        }
    }

    private void A(String str) {
        this.f = Float.parseFloat(str);
        if (this.f > 100.0f) {
            this.f = 0.0f;
        }
    }

    private void B(String str) {
        this.g = Float.parseFloat(str);
    }

    private void z(String str) {
        this.e = Float.parseFloat(str);
    }

    public String A() {
        return this.A;
    }

    public int B() {
        return this.C;
    }

    public String C() {
        return this.D;
    }

    public AmapLoc D() {
        Object C = C();
        if (TextUtils.isEmpty(C)) {
            return null;
        }
        String[] split = C.split(",");
        if (split.length != 3) {
            return null;
        }
        AmapLoc amapLoc = new AmapLoc();
        amapLoc.c(g());
        amapLoc.d(split[0]);
        amapLoc.e(split[1]);
        amapLoc.a(Float.parseFloat(split[2]));
        amapLoc.i(o());
        amapLoc.k(q());
        amapLoc.l(r());
        amapLoc.m(s());
        amapLoc.n(t());
        amapLoc.a(k());
        amapLoc.f(l());
        amapLoc.x(String.valueOf(B()));
        return cw.a(amapLoc) ? amapLoc : null;
    }

    public JSONObject E() {
        return this.I;
    }

    public String F() {
        return c(1);
    }

    public int a() {
        return this.j;
    }

    public void a(double d) {
        d(cw.a(Double.valueOf(d), "#.000000"));
    }

    public void a(float f) {
        z(String.valueOf(Math.round(f)));
    }

    public void a(int i) {
        this.l = i;
    }

    public void a(long j) {
        this.h = j;
    }

    public void a(String str) {
        this.k = str;
    }

    public void a(JSONObject jSONObject) {
        this.I = jSONObject;
    }

    public void a(boolean z) {
        this.G = z;
    }

    public int b() {
        return this.l;
    }

    public void b(double d) {
        e(cw.a(Double.valueOf(d), "#.000000"));
    }

    public void b(float f) {
        A(cw.a(Float.valueOf(f), "#.0"));
    }

    public void b(int i) {
        if (this.j == 0) {
            String str;
            switch (i) {
                case 0:
                    str = "success";
                    break;
                case 1:
                    str = "重要参数为空";
                    break;
                case 2:
                    str = "WIFI信息不足";
                    break;
                case 3:
                    str = "请求参数获取出现异常";
                    break;
                case 4:
                    str = "网络连接异常";
                    break;
                case 5:
                    str = "解析XML出错";
                    break;
                case 6:
                    str = "定位结果错误";
                    break;
                case 7:
                    str = "KEY错误";
                    break;
                case 8:
                    str = "其他错误";
                    break;
                case 9:
                    str = "初始化异常";
                    break;
                case 10:
                    str = "定位服务启动失败";
                    break;
                case 11:
                    str = "错误的基站信息，请检查是否插入SIM卡";
                    break;
                case 12:
                    str = "缺少定位权限";
                    break;
                default:
                    this.j = i;
            }
            this.k = str;
            this.j = i;
        }
    }

    public void b(String str) {
        if (this.m == null || this.m.length() == 0) {
            this.m = str;
        }
    }

    public void b(boolean z) {
        this.H = z;
    }

    public String c() {
        return this.k;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String c(int i) {
        JSONObject jSONObject;
        try {
            jSONObject = new JSONObject();
            switch (i) {
                case 1:
                    jSONObject.put("altitude", this.d);
                    jSONObject.put("speed", (double) this.f);
                    jSONObject.put("bearing", (double) this.g);
                    jSONObject.put("retype", this.n);
                    jSONObject.put("citycode", this.p);
                    jSONObject.put("desc", this.q);
                    jSONObject.put("adcode", this.r);
                    jSONObject.put(DistrictSearchQuery.KEYWORDS_COUNTRY, this.s);
                    jSONObject.put(DistrictSearchQuery.KEYWORDS_PROVINCE, this.t);
                    jSONObject.put(DistrictSearchQuery.KEYWORDS_CITY, this.u);
                    jSONObject.put(DistrictSearchQuery.KEYWORDS_DISTRICT, this.v);
                    jSONObject.put("road", this.w);
                    jSONObject.put("street", this.x);
                    jSONObject.put("number", this.E);
                    jSONObject.put("poiname", this.y);
                    jSONObject.put("cens", this.z);
                    jSONObject.put("poiid", this.A);
                    jSONObject.put("floor", this.B);
                    jSONObject.put("coord", this.C);
                    jSONObject.put("mcell", this.D);
                    jSONObject.put("errorCode", this.j);
                    jSONObject.put("errorInfo", this.k);
                    jSONObject.put("locationType", this.l);
                    jSONObject.put("locationDetail", this.m);
                    jSONObject.put("aoiname", this.F);
                    if (this.I != null) {
                        if (cw.a(jSONObject, "offpct")) {
                            jSONObject.put("offpct", this.I.getString("offpct"));
                            break;
                        }
                    }
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        } catch (Throwable th) {
            e.a(th, "AmapLoc", "toStr");
            jSONObject = null;
        }
        return jSONObject != null ? jSONObject.toString() : null;
    }

    public void c(double d) {
        this.d = d;
    }

    public void c(float f) {
        B(cw.a(Float.valueOf(f), "#.0"));
    }

    public void c(String str) {
        this.a = str;
    }

    public String d() {
        return this.m;
    }

    public void d(String str) {
        this.b = Double.parseDouble(str);
    }

    public int describeContents() {
        return 0;
    }

    public void e(String str) {
        this.c = Double.parseDouble(str);
    }

    public boolean e() {
        return this.G;
    }

    public void f(String str) {
        this.i = str;
    }

    public boolean f() {
        return this.H;
    }

    public String g() {
        return this.a;
    }

    public void g(String str) {
        this.n = str;
    }

    public double h() {
        return this.b;
    }

    public void h(String str) {
        this.o = str;
    }

    public double i() {
        return this.c;
    }

    public void i(String str) {
        this.p = str;
    }

    public float j() {
        return this.e;
    }

    public void j(String str) {
        this.q = str;
    }

    public long k() {
        return this.h;
    }

    public void k(String str) {
        this.r = str;
    }

    public String l() {
        return this.i;
    }

    public void l(String str) {
        this.s = str;
    }

    public String m() {
        return this.n;
    }

    public void m(String str) {
        this.t = str;
    }

    public String n() {
        return this.o;
    }

    public void n(String str) {
        this.u = str;
    }

    public String o() {
        return this.p;
    }

    public void o(String str) {
        this.v = str;
    }

    public String p() {
        return this.q;
    }

    public void p(String str) {
        this.w = str;
    }

    public String q() {
        return this.r;
    }

    public void q(String str) {
        this.x = str;
    }

    public String r() {
        return this.s;
    }

    public void r(String str) {
        this.E = str;
    }

    public String s() {
        return this.t;
    }

    public void s(String str) {
        this.y = str;
    }

    public String t() {
        return this.u;
    }

    public void t(String str) {
        this.F = str;
    }

    public String u() {
        return this.v;
    }

    public void u(String str) {
        if (!TextUtils.isEmpty(str)) {
            for (Object obj : str.split("\\*")) {
                if (!TextUtils.isEmpty(obj)) {
                    String[] split = obj.split(",");
                    a(Double.parseDouble(split[0]));
                    b(Double.parseDouble(split[1]));
                    a((float) Integer.parseInt(split[2]));
                    break;
                }
            }
            this.z = str;
        }
    }

    public String v() {
        return this.w;
    }

    public void v(String str) {
        this.A = str;
    }

    public String w() {
        return this.x;
    }

    public void w(String str) {
        if (!TextUtils.isEmpty(str)) {
            str = str.replace("F", "");
            try {
                Integer.parseInt(str);
            } catch (Throwable th) {
                str = null;
                e.a(th, "AmapLoc", "setFloor");
            }
        }
        this.B = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        byte b = (byte) 0;
        parcel.writeString(this.a);
        parcel.writeString(this.i);
        parcel.writeString(this.k);
        parcel.writeInt(this.j);
        parcel.writeFloat(this.g);
        parcel.writeFloat(this.f);
        parcel.writeFloat(this.e);
        parcel.writeDouble(this.b);
        parcel.writeDouble(this.c);
        parcel.writeDouble(this.d);
        parcel.writeLong(this.h);
        parcel.writeString(this.n);
        parcel.writeString(this.o);
        parcel.writeString(this.p);
        parcel.writeString(this.q);
        parcel.writeString(this.r);
        parcel.writeString(this.s);
        parcel.writeString(this.t);
        parcel.writeString(this.u);
        parcel.writeString(this.v);
        parcel.writeString(this.w);
        parcel.writeString(this.x);
        parcel.writeString(this.y);
        parcel.writeString(this.z);
        parcel.writeString(this.A);
        parcel.writeString(this.B);
        parcel.writeString(this.D);
        parcel.writeString(this.m);
        parcel.writeInt(this.C);
        parcel.writeInt(this.l);
        parcel.writeString(this.E);
        parcel.writeByte(!this.G ? (byte) 0 : (byte) 1);
        if (this.H) {
            b = (byte) 1;
        }
        parcel.writeByte(b);
        parcel.writeString(this.F);
    }

    public String x() {
        return this.E;
    }

    public void x(String str) {
        if (!TextUtils.isEmpty(str)) {
            if (this.a.equals(GeocodeSearch.GPS) || str.equals("0")) {
                this.C = 0;
                return;
            } else if (str.equals(CallInterceptDetails.BRANDED_STATE)) {
                this.C = 1;
                return;
            }
        }
        this.C = -1;
    }

    public String y() {
        return this.y;
    }

    public void y(String str) {
        this.D = str;
    }

    public String z() {
        return this.F;
    }
}
