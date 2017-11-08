package com.huawei.hwid.core.model.http.request;

import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: UserThirdAuthRequest */
public class al extends a {
    private String A;
    private String B;
    private String C;
    private String h;
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private boolean t;
    private String u;
    private String v;
    private String w;
    private int x;
    private String y;
    private int z;

    public void f(String str) {
        this.v = str;
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String a;
        try {
            Bundle bundle;
            a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "UserThirdAuthReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.i);
            t.a(a, "thirdToken", this.j);
            t.a(a, "userAccount", this.k);
            t.a(a, "ifGetDeviceInfoList", "0");
            a.startTag(null, "deviceInfo");
            t.a(a, "deviceID", this.m);
            t.a(a, "deviceType", this.l);
            t.a(a, "terminalType", q.b());
            t.a(a, "deviceAliasName", q.b());
            a.endTag(null, "deviceInfo");
            t.a(a, "reqClientType", this.n);
            t.a(a, "loginChannel", this.o);
            t.a(a, "plmn", this.p);
            t.a(a, "osVersion", this.q);
            t.a(a, "authFlag", String.valueOf(this.z));
            t.a(a, "uuid", this.r);
            String str = "";
            if (!this.t) {
                if (!p.e(this.s)) {
                    str = this.s;
                    t.a(a, "appID", str);
                    a.endTag(null, "UserThirdAuthReq");
                    a.endDocument();
                    a = byteArrayOutputStream.toString("UTF-8");
                    bundle = new Bundle();
                    bundle.putString(NumberInfo.VERSION_KEY, "10000");
                    bundle.putString("accountType", this.i);
                    bundle.putString("thirdToken", this.j);
                    bundle.putString("userAccount", f.c(this.k));
                    bundle.putString("ifGetDeviceInfoList", "0");
                    bundle.putString("deviceID", this.m);
                    bundle.putString("deviceType", this.l);
                    bundle.putString("terminalType", q.b());
                    bundle.putString("deviceAliasName", q.b());
                    bundle.putString("reqClientType", this.n);
                    bundle.putString("loginChannel", this.o);
                    bundle.putString("plmn", this.p);
                    bundle.putString("osVersion", this.q);
                    bundle.putString("uuid", this.r);
                    bundle.putString("appID", str);
                    bundle.putString("authFlag", String.valueOf(this.z));
                    com.huawei.hwid.core.c.b.a.b("UserThirdAuthRequest", "packedString:" + f.a(bundle));
                    return a;
                }
            }
            str = "com.huawei.hwid";
            t.a(a, "appID", str);
            a.endTag(null, "UserThirdAuthReq");
            a.endDocument();
            a = byteArrayOutputStream.toString("UTF-8");
            bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.i);
            bundle.putString("thirdToken", this.j);
            bundle.putString("userAccount", f.c(this.k));
            bundle.putString("ifGetDeviceInfoList", "0");
            bundle.putString("deviceID", this.m);
            bundle.putString("deviceType", this.l);
            bundle.putString("terminalType", q.b());
            bundle.putString("deviceAliasName", q.b());
            bundle.putString("reqClientType", this.n);
            bundle.putString("loginChannel", this.o);
            bundle.putString("plmn", this.p);
            bundle.putString("osVersion", this.q);
            bundle.putString("uuid", this.r);
            bundle.putString("appID", str);
            bundle.putString("authFlag", String.valueOf(this.z));
            com.huawei.hwid.core.c.b.a.b("UserThirdAuthRequest", "packedString:" + f.a(bundle));
            return a;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                a = "UserThirdAuthRequest";
                com.huawei.hwid.core.c.b.a.d(a, e.toString());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            if (eventType == 2) {
                if ("result".equals(name)) {
                    this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                }
                if (this.b != 0) {
                    if ("errorCode".equals(name)) {
                        this.c = Integer.valueOf(a.nextText()).intValue();
                    } else if ("errorDesc".equals(name)) {
                        this.d = a.nextText();
                    }
                } else if ("userID".equals(name)) {
                    this.u = a.nextText();
                } else if ("TGC".equals(name)) {
                    this.e = a.nextText();
                } else if ("siteID".equals(name)) {
                    name = a.nextText();
                    try {
                        this.x = Integer.parseInt(name);
                    } catch (Throwable e) {
                        com.huawei.hwid.core.c.b.a.d("TGC", "pares siteId:" + name + ": err:" + e.toString(), e);
                    }
                } else if ("ServiceToken".equals(name)) {
                    this.y = a.nextText();
                } else if ("userName".equals(name)) {
                    this.w = a.nextText();
                    if (TextUtils.isEmpty(this.w)) {
                        this.w = "ThirdAccount";
                    }
                } else if ("userAccount".equals(name)) {
                    this.C = a.nextText();
                } else if ("accountType".equals(name)) {
                    this.i = a.nextText();
                } else if ("thirdOpenID".equals(name)) {
                    this.A = a.nextText();
                } else if ("thirdAccessToken".equals(name)) {
                    this.B = a.nextText();
                }
            }
        }
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString(NetUtil.REQ_QUERY_TOEKN, !p.e(this.e) ? this.e : this.y);
        h.putString("userId", this.u);
        h.putString("cookie", this.v);
        h.putInt("siteId", this.x);
        h.putString("userName", this.w);
        h.putString("tokenType", this.s);
        h.putString("deviceId", w());
        h.putString("deviceType", this.l);
        h.putString("accountType", this.i);
        h.putString("accountName", this.C);
        h.putString("thirdAccessToken", this.B);
        h.putString("thirdOpenID", this.A);
        return h;
    }

    private String w() {
        String str = "";
        if (!"NULL".equals(this.m) && !TextUtils.isEmpty(this.m)) {
            return this.m;
        }
        if ("NULL".equals(this.r) || TextUtils.isEmpty(this.r)) {
            return str;
        }
        return this.r;
    }

    public String g() {
        return this.h;
    }
}
