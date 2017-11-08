package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.e;
import com.huawei.hwid.core.model.http.i;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: UIDVerifyPasswordRequest */
public class ad extends a {
    private String h;
    private String i;
    private String j = "com.huawei.hwid";
    private String k;
    private int l = 7;
    private String m;
    private String n;
    private String o;
    private int p;
    private String q;
    private String r = "/IUserPwdMng/uidVerifyPassword";
    private String s = (d() + this.r);
    private Context t;

    public ad(Context context, String str, String str2, String str3, String str4, int i) {
        com.huawei.hwid.core.c.b.a.b("UidVerifyPasswordRequest", "userId = " + f.a(str) + "appId = " + str2);
        this.t = context;
        a(e.URLType);
        f(str3);
        if (!TextUtils.isEmpty(str2)) {
            k(str2);
        }
        g(str);
        h(str4);
        String b = q.b(context);
        i(b);
        j(q.a(context, b));
        b(70002003);
        b(70002058);
    }

    private void k(String str) {
        this.j = str;
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        return null;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
    }

    protected String f() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ver=").append("10000").append("&").append("uid=").append(this.h).append("&").append("pw=").append(this.i).append("&").append("clT=").append(this.l).append("&").append("app=").append(this.j).append("&").append("dvT=").append(this.o).append("&").append("dvID=").append(this.n).append("&").append("fg=").append(this.k);
        Bundle bundle = new Bundle();
        bundle.putString("uid", this.h);
        bundle.putString("pw", this.i);
        bundle.putString("clT", String.valueOf(this.l));
        bundle.putString("app", this.j);
        bundle.putString("dvT", this.o);
        bundle.putString("dvID", this.n);
        bundle.putString("fg", this.k);
        com.huawei.hwid.core.c.b.a.b("UidVerifyPasswordRequest", "postString:" + f.a(bundle));
        return stringBuffer.toString();
    }

    protected void b(String str) {
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("&");
            HashMap hashMap = new HashMap();
            Object obj = "";
            for (String str2 : split) {
                com.huawei.hwid.core.c.b.a.e("UidVerifyPasswordRequest", "infolist item:" + f.a(str2));
                String[] split2 = str2.split("=");
                hashMap.put(split2[0], split2[1]);
                obj = split2[0];
            }
            if (hashMap.containsKey("resultCode")) {
                this.b = Integer.valueOf((String) hashMap.get("resultCode")).intValue();
            }
            if (this.b != 0) {
                this.c = this.b;
                this.d = (String) hashMap.get(obj);
                com.huawei.hwid.core.c.b.a.e("UidVerifyPasswordRequest", "mErrorCode:" + this.c + ",mErrorDesc:" + f.a(this.d));
            } else {
                this.q = (String) hashMap.get("userID");
                this.m = (String) hashMap.get("st");
                String str3 = (String) hashMap.get("siteID");
                try {
                    this.p = Integer.parseInt(str3);
                } catch (Throwable e) {
                    com.huawei.hwid.core.c.b.a.d("TGC", "pares siteId:" + str3 + ", err:" + e.toString(), e);
                }
                com.huawei.hwid.core.c.b.a.a("UidVerifyPasswordRequest", "mRtnUserId:" + f.a(this.q) + ",mServiceToken:" + f.a(this.m) + ",mSiteId:" + f.a(Integer.valueOf(this.p)));
            }
        }
    }

    public void f(String str) {
        this.i = str;
    }

    public void g(String str) {
        this.h = str;
    }

    public void h(String str) {
        this.k = str;
    }

    public void i(String str) {
        this.n = str;
    }

    public void j(String str) {
        this.o = str;
    }

    public String g() {
        return this.s;
    }

    public Bundle h() {
        this.m = com.huawei.hwid.core.encrypt.e.e(this.t, this.m);
        Bundle h = super.h();
        h.putString("serviceToken", this.m);
        h.putString("userID", this.q);
        h.putInt("siteID", this.p);
        return h;
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        i.a(context, aVar, str, a(context, aVar, new ae(context, cloudRequestHandler)));
    }
}
