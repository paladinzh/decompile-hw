package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.DeviceInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: UserSMSAuthRequest */
public class ai extends a {
    private String h = (d() + "/IUserInfoMng/userSMSAuth");
    private String i;
    private String j;
    private String k = "7";
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private DeviceInfo r;
    private String s;
    private String t;
    private String u;
    private String v;
    private String w;
    private String x;
    private String y;
    private String z;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "UserSMSAuthReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "mobilePhone", this.i);
            t.a(a, "smsAuthCode", this.j);
            if (this.r != null) {
                a.startTag(null, "deviceInfo");
                t.a(a, "deviceAliasName", this.r.getDeviceAliasName());
                t.a(a, "deviceID", this.r.getDeviceID());
                t.a(a, "deviceType", this.r.getDeviceType());
                t.a(a, "terminalType", q.b());
                a.endTag(null, "deviceInfo");
            }
            t.a(a, "reqClientType", this.k);
            t.a(a, "smsAuthOprType", this.l);
            t.a(a, "clientIP", this.m);
            t.a(a, "loginChannel", this.n);
            t.a(a, "plmn", this.o);
            t.a(a, "osVersion", this.p);
            t.a(a, "uuid", this.s);
            t.a(a, "appID", this.t);
            t.a(a, "password", this.q);
            a.endTag(null, "UserSMSAuthReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("mobilePhone", f.c(this.i));
            bundle.putString("smsAuthCode", this.j);
            if (this.r != null) {
                bundle.putString("deviceAliasName", this.r.getDeviceAliasName());
                bundle.putString("deviceID", this.r.getDeviceID());
                bundle.putString("deviceType", this.r.getDeviceType());
                bundle.putString("terminalType", q.b());
            }
            bundle.putString("reqClientType", this.k);
            bundle.putString("smsAuthOprType", this.l);
            bundle.putString("clientIP", this.m);
            bundle.putString("loginChannel", this.n);
            bundle.putString("plmn", this.o);
            bundle.putString("osVersion", this.p);
            bundle.putString("uuid", this.s);
            bundle.putString("appID", this.t);
            bundle.putString("password", this.q);
            com.huawei.hwid.core.c.b.a.b("UserSMSAuthRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("UserSMSAuthRequest", e.toString());
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
                    if (this.b == 0) {
                        if (!"userID".equals(name)) {
                            if (!"TGC".equals(name)) {
                                if (!"ServiceToken".equals(name)) {
                                    if (!"mobilePhone".equals(name)) {
                                        if (!"siteID".equals(name)) {
                                            if (!"ifSetPassword".equals(name)) {
                                                break;
                                            }
                                            this.y = a.nextText();
                                            break;
                                        }
                                        this.x = a.nextText();
                                        break;
                                    }
                                    this.i = a.nextText();
                                    break;
                                }
                                this.w = a.nextText();
                                break;
                            }
                            this.v = a.nextText();
                            break;
                        }
                        this.u = a.nextText();
                        break;
                    } else if (!"errorCode".equals(name)) {
                        if (!"errorDesc".equals(name)) {
                            break;
                        }
                        this.d = a.nextText();
                        break;
                    } else {
                        this.c = Integer.valueOf(a.nextText()).intValue();
                        break;
                    }
                default:
                    break;
            }
        }
    }

    public String g() {
        return this.h;
    }

    public int p() {
        return 0;
    }

    public void f(String str) {
        this.i = str;
    }

    public void g(String str) {
        this.j = str;
    }

    public void a(DeviceInfo deviceInfo) {
        this.r = deviceInfo;
    }

    public void h(String str) {
        this.l = str;
    }

    public void i(String str) {
        this.n = str;
    }

    public void j(String str) {
        this.q = str;
    }

    public void k(String str) {
        this.s = str;
    }

    public String w() {
        return this.u;
    }

    public String x() {
        return this.v;
    }

    public String y() {
        return this.i;
    }

    public int z() {
        if (TextUtils.isEmpty(this.x)) {
            return 0;
        }
        try {
            return Integer.valueOf(this.x).intValue();
        } catch (Throwable e) {
            com.huawei.hwid.core.c.b.a.d("UserSMSAuthRequest", e.toString(), e);
            return 0;
        }
    }

    public void l(String str) {
        this.t = str;
    }

    public void m(String str) {
        this.z = str;
    }

    public String A() {
        return this.z;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("cookie", A());
        h.putString("mobilePhone", y());
        h.putInt("siteId", z());
        h.putString("upToken", x());
        h.putString("userId", w());
        return h;
    }

    public ai(Context context, ak akVar, String str, String str2, String str3) {
        if ("cloud".equalsIgnoreCase(str2)) {
            str2 = "com.huawei.hwid";
        }
        String c = q.c(context);
        f(str);
        g(str3);
        k(q.i(context));
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceType(q.a(context));
        deviceInfo.setDeviceIdInDeviceInfo(c);
        deviceInfo.setTerminalType(q.a());
        a(deviceInfo);
        l(context.getPackageName());
        if (com.huawei.hwid.core.a.a.b()) {
            i("8000000");
        } else {
            i(b.a(context, str2));
        }
        switch (aj.a[akVar.ordinal()]) {
            case 1:
                h("1");
                break;
            case 2:
                h(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR);
                break;
            case 3:
                h("4");
                break;
        }
        j(null);
        b(70002039);
        b(70001201);
        b(70002001);
        b(70002003);
        b(70001104);
    }
}
