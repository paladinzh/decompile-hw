package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: ResetPwdBySMSRequest */
public class y extends a {
    private String h = (d() + "/IUserPwdMng/resetPwdBySMS");
    private String i = "7";
    private String j = "0";
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "ResetPwdBySMSReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.j);
            t.a(a, "userAccount", this.m);
            t.a(a, "newPassword", this.k);
            t.a(a, "smsAuthCode", this.l);
            t.a(a, "reqClientType", this.i);
            t.a(a, "deviceType", this.p);
            t.a(a, "deviceID", this.o);
            t.a(a, "plmn", this.q);
            a.endTag(null, "ResetPwdBySMSReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.j);
            bundle.putString("userAccount", f.c(this.m));
            bundle.putString("newPassword", this.k);
            bundle.putString("smsAuthCode", this.l);
            bundle.putString("reqClientType", this.i);
            bundle.putString("deviceType", this.p);
            bundle.putString("deviceID", this.o);
            bundle.putString("plmn", this.q);
            com.huawei.hwid.core.c.b.a.b("ResetPwdBySMSRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("ResetPwdBySMSRequest", e.toString());
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
                            break;
                        }
                        this.n = a.nextText();
                        com.huawei.hwid.core.c.b.a.a("ResetPwdBySMSRequest", "mUserID: " + f.a(this.n));
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

    public void f(String str) {
        this.j = str;
    }

    public void g(String str) {
        this.k = str;
    }

    public void h(String str) {
        this.l = str;
    }

    public void i(String str) {
        this.m = str;
    }

    public void j(String str) {
        this.o = str;
    }

    public void k(String str) {
        this.p = str;
    }

    public void l(String str) {
        this.q = str;
    }

    public Bundle h() {
        return super.h();
    }

    public y(Context context, String str, String str2, String str3, int i, Bundle bundle) {
        f(d.b(str));
        i(str);
        g(str2);
        h(str3);
        j(q.b(context));
        l(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        c(i);
        k(q.a(context, q.b(context)));
        b(70002028);
        b(70002039);
        b(70001201);
        b(70002001);
    }
}
