package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.datatype.n;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.e;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: LoginRequest */
public class t extends a {
    private n A;
    private String B;
    private String C;
    private String D;
    private int E;
    private String F;
    private boolean G;
    private int h = 0;
    private int i = -2;
    private String j = "7";
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private String u;
    private String v;
    private String w;
    private String x;
    private String y;
    private String z;

    public t() {
        String str;
        if (t()) {
            str = "/IUserInfoMng/login";
        } else {
            str = "/IUserInfoMng/userLoginAuth";
        }
        this.k = str;
        this.l = d() + this.k;
        this.m = "0";
        this.A = null;
        this.E = -1;
        this.F = "";
        this.G = true;
        this.g = 0;
        a(e.URLType);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        return null;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
    }

    protected String f() {
        String str = "";
        if (this.G || p.e(this.y)) {
            str = "com.huawei.hwid";
        } else {
            str = this.y;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ver=").append("10000").append("&").append("acT=").append(this.m).append("&").append("ac=").append(this.n).append("&").append("pw=").append(this.o).append("&").append("dvT=").append(this.q).append("&").append("dvID=").append(y()).append("&").append("tmT=").append(q.a()).append("&").append("clT=").append(this.j).append("&").append("cn=").append(this.u).append("&").append("os=").append(this.v).append("&").append("pl=").append(this.w).append("&").append("app=").append(str).append("&").append("dvN=").append(q.a()).append("&").append("uuid=").append(this.t);
        if (!(TextUtils.isEmpty(this.s) || "NULL".equals(this.s))) {
            stringBuffer.append("&").append("dvID2=").append(this.s);
        }
        if (!(TextUtils.isEmpty(this.B) || TextUtils.isEmpty(this.D) || TextUtils.isEmpty(this.C))) {
            stringBuffer.append("&").append("vCode=").append(this.B);
            stringBuffer.append("&").append("vAcT=").append(this.D);
            stringBuffer.append("&").append("vAc=").append(this.C);
        }
        if (n.g() && this.A != null && this.A.a()) {
            stringBuffer.append("&").append("sc=").append(this.A.d()).append("&").append("emID=").append(this.A.e()).append("&").append("scT=").append(n.b()).append("&").append("C=").append(this.A.c());
        }
        Bundle bundle = new Bundle();
        bundle.putString("acT", this.m);
        bundle.putString("ac", f.c(this.n));
        bundle.putString("pw", this.o);
        bundle.putString("dvT", this.q);
        bundle.putString("dvID", y());
        bundle.putString("tmT", q.a());
        bundle.putString("clT", this.j);
        bundle.putString("cn", this.u);
        bundle.putString("os", this.v);
        bundle.putString("pl", this.w);
        bundle.putString("app", str);
        bundle.putString("dvN", q.a());
        bundle.putString("uuid", this.t);
        bundle.putString("dvID2", this.s);
        bundle.putString("vCode", this.B);
        bundle.putString("vAcT", this.D);
        bundle.putString("vAc", this.C);
        if (n.g() && this.A != null && this.A.a()) {
            bundle.putString("sc", this.A.d());
            bundle.putString("emID", this.A.e());
            bundle.putString("scT", String.valueOf(n.b()));
            bundle.putString("C", this.A.c());
        }
        com.huawei.hwid.core.c.b.a.b("LoginRequest", "postString:" + f.a(bundle));
        return stringBuffer.toString();
    }

    protected void b(String str) {
        String str2;
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("&");
            HashMap hashMap = new HashMap();
            Object obj = "";
            int length = split.length;
            int i = 0;
            while (i < length) {
                Object obj2;
                str2 = split[i];
                com.huawei.hwid.core.c.b.a.e("LoginRequest", "infolist item:" + f.a(str2));
                String[] split2 = str2.split("=");
                if (split2.length <= 1) {
                    obj2 = obj;
                } else {
                    hashMap.put(split2[0], split2[1]);
                    obj2 = split2[0];
                }
                i++;
                obj = obj2;
            }
            if (hashMap.containsKey("resultCode")) {
                this.b = Integer.valueOf((String) hashMap.get("resultCode")).intValue();
            }
            if (this.b != 0) {
                this.c = this.b;
                this.d = (String) hashMap.get(obj);
                com.huawei.hwid.core.c.b.a.e("LoginRequest", "mErrorCode:" + this.c + ",mErrorDesc:" + f.a(this.d));
            } else {
                this.p = (String) hashMap.get("userID");
                this.e = (String) hashMap.get("TGC");
                this.z = (String) hashMap.get("agrFlags");
                str2 = (String) hashMap.get("siteID");
                try {
                    this.h = Integer.parseInt(str2);
                } catch (Throwable e) {
                    com.huawei.hwid.core.c.b.a.d("TGC", "pares siteId:" + str2 + ", err:" + e.toString(), e);
                }
                str2 = (String) hashMap.get("userState");
                try {
                    this.i = Integer.parseInt(str2);
                } catch (Throwable e2) {
                    com.huawei.hwid.core.c.b.a.d("TGC", "pares mUserState:" + str2 + ", err:" + e2.toString(), e2);
                }
                if (n.g()) {
                    str2 = (String) hashMap.get("rightsID");
                    try {
                        this.E = Integer.parseInt(str2);
                    } catch (Exception e3) {
                        com.huawei.hwid.core.c.b.a.d("TGC", "pares mRightsID:" + str2 + ", err:" + e3.toString());
                    }
                    this.F = (String) hashMap.get("expiredDate");
                }
            }
        }
    }

    public String g() {
        return this.l;
    }

    public void f(String str) {
        this.m = str;
    }

    public void g(String str) {
        this.n = str;
    }

    public void h(String str) {
        this.o = str;
    }

    public void i(String str) {
        this.j = str;
    }

    public void j(String str) {
        this.q = str;
    }

    public void k(String str) {
        this.r = str;
    }

    public void l(String str) {
        this.t = str;
    }

    public void m(String str) {
        this.u = str;
    }

    public void n(String str) {
        this.v = str;
    }

    public void o(String str) {
        this.w = str;
    }

    public void p(String str) {
        this.x = str;
    }

    public int w() {
        return this.h;
    }

    public int x() {
        return this.i;
    }

    public void q(String str) {
        this.y = str;
    }

    public void c(boolean z) {
        this.G = z;
    }

    public Bundle h() {
        boolean z = false;
        Bundle h = super.h();
        h.putString(NetUtil.REQ_QUERY_TOEKN, this.e);
        h.putString("userId", this.p);
        h.putString("cookie", this.x);
        h.putInt("siteId", w());
        h.putInt("userState", x());
        h.putString("userName", this.n);
        h.putString("tokenType", this.y);
        h.putString("deviceId", y());
        h.putString("deviceType", this.q);
        h.putString("accountType", this.m);
        if (n.g()) {
            h.putInt("rightsID", this.E);
            h.putString("vipExpiredDate", this.F);
            String str = "isVipRequest";
            if (this.A != null) {
                z = true;
            }
            h.putBoolean(str, z);
        }
        h.putString("agrFlags", this.z);
        return h;
    }

    private String y() {
        String str = "";
        if (!"NULL".equals(this.r) && !TextUtils.isEmpty(this.r)) {
            return this.r;
        }
        if ("NULL".equals(this.t) || TextUtils.isEmpty(this.t)) {
            return str;
        }
        return this.t;
    }

    private void a(Context context, String str, String str2, String str3, Bundle bundle) {
        boolean z = false;
        if ("cloud".equalsIgnoreCase(str3)) {
            str3 = "com.huawei.hwid";
        }
        g(str.toLowerCase(Locale.ENGLISH));
        f(d.b(str));
        h(str2);
        k(q.c(context));
        l(q.i(context));
        q(str3);
        j(q.a(context));
        d(context.getString(m.a(context, "CS_logining_message")));
        n(q.c());
        o(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        if (com.huawei.hwid.core.a.a.b()) {
            m("8000000");
        } else {
            m(b.a(context, str3));
        }
        i(b.b(context, str3));
        this.s = q.e(context);
        if (bundle != null) {
            z = bundle.getBoolean("needActivateVip", false);
        }
        com.huawei.hwid.core.c.b.a.a("LoginRequest", "in LoginRequest shouldActivateVIP:" + z);
        if (z) {
            this.A = new n(context);
        }
    }

    private void z() {
        b(70002003);
        b(70001201);
        b(70002001);
        b(70001104);
        b(70002067);
        b(70002068);
        b(70002069);
        b(70002072);
        b(70002071);
        b(70002039);
        b(70002002);
        b(70002058);
    }

    public t(Context context, String str, String str2, String str3, Bundle bundle) {
        String str4;
        if (t()) {
            str4 = "/IUserInfoMng/login";
        } else {
            str4 = "/IUserInfoMng/userLoginAuth";
        }
        this.k = str4;
        this.l = d() + this.k;
        this.m = "0";
        this.A = null;
        this.E = -1;
        this.F = "";
        this.G = true;
        this.g = 0;
        a(e.URLType);
        a(context, str, str2, str3, bundle);
        z();
        c(d.h(context));
    }

    public static boolean a(Context context, Bundle bundle) {
        String string = bundle.getString(NetUtil.REQ_QUERY_TOEKN);
        String string2 = bundle.getString("userId");
        String string3 = bundle.getString("cookie");
        int i = bundle.getInt("siteId");
        String string4 = bundle.getString("userName");
        String string5 = bundle.getString("tokenType");
        String string6 = bundle.getString("deviceId");
        String string7 = bundle.getString("deviceType");
        String string8 = bundle.getString("accountType");
        com.huawei.hwid.core.c.b.a.b("LoginRequest", "BaseLoginCallback:" + f.a(bundle));
        HwAccount a = d.a(string4, string5, string, string2, i, string3, string6, string7, string8);
        if (d.a(a)) {
            return com.huawei.hwid.manager.f.a(context).a(context, a);
        }
        return false;
    }
}
