package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.e;
import com.huawei.hwid.core.model.http.i;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: ServiceTokenAuthRequest */
public class z extends a {
    private String h;
    private String i = "0";
    private String j;
    private String k = "";
    private String l = "7";
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private int u;

    public z() {
        String str;
        if (t()) {
            str = "/IUserInfoMng/stAuth";
        } else {
            str = "/IUserInfoMng/serviceTokenAuth";
        }
        this.s = str;
        this.t = d() + this.s;
        this.u = 1;
        this.g = 0;
        a(e.URLType);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String a;
        try {
            String str;
            a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "ServiceTokenAuthReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "serviceToken", this.j);
            String str2 = "appID";
            if (TextUtils.isEmpty(this.k)) {
                str = "com.huawei.hwid";
            } else {
                str = this.k;
            }
            t.a(a, str2, str);
            a.startTag(null, "deviceInfo");
            t.a(a, "deviceID", this.n);
            t.a(a, "deviceType", this.m);
            t.a(a, "terminalType", q.b());
            a.endTag(null, "deviceInfo");
            t.a(a, "reqClientType", this.l);
            t.a(a, "clientIP", "");
            t.a(a, "loginChannel", this.p);
            t.a(a, "uuid", this.o);
            t.a(a, "chkAcctChange", "0");
            t.a(a, "isGetAccount", "0");
            t.a(a, "isGetAgrVers", this.i);
            a.endTag(null, "ServiceTokenAuthReq");
            a.endDocument();
            a = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("serviceToken", this.j);
            String str3 = "appID";
            if (TextUtils.isEmpty(this.k)) {
                str = "com.huawei.hwid";
            } else {
                str = this.k;
            }
            bundle.putString(str3, str);
            bundle.putString("deviceID", this.n);
            bundle.putString("deviceType", this.m);
            bundle.putString("terminalType", q.b());
            bundle.putString("reqClientType", this.l);
            bundle.putString("clientIP", "");
            bundle.putString("loginChannel", this.p);
            bundle.putString("uuid", this.o);
            bundle.putString("chkAcctChange", "0");
            bundle.putString("isGetAccount", "0");
            com.huawei.hwid.core.c.b.a.b("ServiceTokenAuthRequest", "packedString:" + f.a(bundle));
            return a;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                a = "ServiceTokenAuthRequest";
                com.huawei.hwid.core.c.b.a.d(a, e.toString());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            switch (eventType) {
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b != 0) {
                        if (!"errorCode".equals(name)) {
                            if (!"errorDesc".equals(name)) {
                                break;
                            }
                            this.d = a.nextText();
                            break;
                        }
                        this.c = Integer.valueOf(a.nextText()).intValue();
                        break;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected String f() {
        String str;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer append = stringBuffer.append("ver=").append("10000").append("&").append("st=").append(this.j).append("&").append("app=");
        if (TextUtils.isEmpty(this.k)) {
            str = "com.huawei.hwid";
        } else {
            str = this.k;
        }
        append.append(str).append("&").append("dvT=").append(this.m).append("&").append("dvID=").append(w()).append("&").append("tmT=").append(q.a()).append("&").append("clT=").append(this.l).append("&").append("cn=").append(this.p).append("&").append("chg=").append("0").append("&").append("gAc=").append("0").append("&").append("uuid=").append(this.o).append("&").append("agr=").append(this.i);
        Bundle bundle = new Bundle();
        bundle.putString("st", this.j);
        String str2 = "app";
        if (TextUtils.isEmpty(this.k)) {
            str = "com.huawei.hwid";
        } else {
            str = this.k;
        }
        bundle.putString(str2, str);
        bundle.putString("dvT", this.m);
        bundle.putString("dvID", w());
        bundle.putString("tmT", q.a());
        bundle.putString("clT", this.l);
        bundle.putString("cn", this.p);
        bundle.putString("chg", "0");
        bundle.putString("gAc", "0");
        bundle.putString("uuid", this.o);
        bundle.putString("agr", this.i);
        com.huawei.hwid.core.c.b.a.b("ServiceTokenAuthRequest", "postString:" + f.a(bundle));
        return stringBuffer.toString();
    }

    protected void b(String str) {
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("&");
            HashMap hashMap = new HashMap();
            Object obj = "";
            for (String str2 : split) {
                com.huawei.hwid.core.c.b.a.e("ServiceTokenAuthRequest", "infolist item:" + f.a(str2));
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
                com.huawei.hwid.core.c.b.a.e("ServiceTokenAuthRequest", "mErrorCode:" + this.c + ",mErrorDesc:" + f.a(this.d));
            } else {
                this.q = (String) hashMap.get("userID");
                this.r = (String) hashMap.get("acctChangedFlag");
                this.h = (String) hashMap.get("agrFlags");
            }
        }
    }

    public String g() {
        return this.t;
    }

    public void f(String str) {
        this.k = str;
    }

    public void g(String str) {
        this.m = str;
    }

    public void h(String str) {
        this.j = str;
    }

    public void i(String str) {
        this.n = str;
    }

    public void j(String str) {
        this.o = str;
    }

    public void k(String str) {
        this.p = str;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("agrFlags", this.h);
        return h;
    }

    private String w() {
        String str = "";
        if (!"NULL".equals(this.n) && !TextUtils.isEmpty(this.n)) {
            return this.n;
        }
        if ("NULL".equals(this.o) || TextUtils.isEmpty(this.o)) {
            return str;
        }
        return this.o;
    }

    public z(Context context, String str, String str2, int i, Bundle bundle) {
        String str3;
        if (t()) {
            str3 = "/IUserInfoMng/stAuth";
        } else {
            str3 = "/IUserInfoMng/serviceTokenAuth";
        }
        this.s = str3;
        this.t = d() + this.s;
        this.u = 1;
        if ("cloud".equalsIgnoreCase(str) || p.e(str)) {
            str = "com.huawei.hwid";
        }
        this.g = 0;
        a(e.URLType);
        h(com.huawei.hwid.core.encrypt.e.d(context, str2));
        f(context.getPackageName());
        i(q.c(context));
        j(q.i(context));
        g(q.a(context));
        c(i);
        d(this.u);
        k(b.a(context, str));
        a(true);
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        i.a(context, aVar, str, a(context, aVar, new aa(context, cloudRequestHandler)));
    }
}
