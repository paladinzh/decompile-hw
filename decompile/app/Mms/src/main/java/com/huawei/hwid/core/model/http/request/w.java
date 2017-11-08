package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.datatype.DeviceInfo;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: RegisterRequest */
public class w extends a {
    private String h = "7";
    private String i;
    private String j;
    private String k;
    private String l = "0";
    private String m;
    private String n = (d() + "/IUserInfoMng/registerCloudAccount");
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private DeviceInfo u;
    private AgreementVersion[] v;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "RegisterCloudAccountReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.l);
            t.a(a, "userAccount", this.j);
            t.a(a, "password", this.k);
            t.a(a, "reqClientType", this.h);
            t.a(a, "clientIP", this.i);
            t.a(a, "authCode", this.o);
            t.a(a, "osVersion", this.r);
            t.a(a, "plmn", this.s);
            t.a(a, "registerChannel", this.q);
            t.a(a, "uuid", this.t);
            if (this.u != null) {
                a.startTag(null, "deviceInfo");
                com.huawei.hwid.core.helper.a.a.a(a, this.u);
                a.endTag(null, "deviceInfo");
            }
            if (this.v != null) {
                if (this.v.length > 0) {
                    a.startTag(null, "agrVers").attribute(null, "size", String.valueOf(this.v.length));
                    com.huawei.hwid.core.helper.a.a.a(a, this.v);
                    a.endTag(null, "agrVers");
                }
            }
            t.a(a, "languageCode", this.p);
            a.endTag(null, "RegisterCloudAccountReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.l);
            bundle.putString("userAccount", f.c(this.j));
            bundle.putString("password", this.k);
            bundle.putString("reqClientType", this.h);
            bundle.putString("clientIP", this.i);
            bundle.putString("authCode", this.o);
            bundle.putString("osVersion", this.r);
            bundle.putString("plmn", this.s);
            bundle.putString("registerChannel", this.q);
            bundle.putString("uuid", this.t);
            com.huawei.hwid.core.c.b.a.b("RegisterRequest", "packedString XMLContent = " + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("RegisterRequest", e.toString());
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
                    this.m = a.nextText();
                }
            }
        }
    }

    public void f(String str) {
        this.j = str;
    }

    public void g(String str) {
        this.k = str;
    }

    public void h(String str) {
        this.l = str;
    }

    public String w() {
        return this.m;
    }

    public String g() {
        return this.n;
    }

    public void i(String str) {
        this.o = str;
    }

    public void j(String str) {
        this.q = str;
    }

    public void k(String str) {
        this.r = str;
    }

    public void l(String str) {
        this.s = str;
    }

    public void m(String str) {
        this.t = str;
    }

    public void a(DeviceInfo deviceInfo) {
        this.u = deviceInfo;
    }

    public void a(AgreementVersion[] agreementVersionArr) {
        if (agreementVersionArr != null) {
            this.v = (AgreementVersion[]) agreementVersionArr.clone();
        }
    }

    public void n(String str) {
        this.p = str;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("userId", w());
        return h;
    }

    public w(Context context, String str, String str2, int i, String str3, String str4, Bundle bundle) {
        new UserInfo().setLanguageCode(d.g(context));
        if (!p.e(str4)) {
            i(str4);
        }
        String b = d.b(str);
        f(str);
        h(b);
        g(str2);
        if (p.e(str3) || "cloud".equalsIgnoreCase(str3)) {
            str3 = "com.huawei.hwid";
        }
        if (com.huawei.hwid.core.a.a.b()) {
            j("8000000");
        } else {
            j(b.a(context, str3));
        }
        d(context.getString(m.a(context, "CS_registering_message")));
        k(q.c());
        l(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        m(q.i(context));
        n(d.g(context));
        c(i);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceType(q.a(context));
        deviceInfo.setDeviceIdInDeviceInfo(q.b(context));
        deviceInfo.setTerminalType(q.b());
        deviceInfo.setDeviceAliasName(q.j(context));
        a(deviceInfo);
        b(70002002);
        b(70002067);
        b(70002068);
        b(70002069);
        if ("2".equals(b)) {
            b(70002011);
            b(70002039);
            b(70001201);
        }
    }
}
