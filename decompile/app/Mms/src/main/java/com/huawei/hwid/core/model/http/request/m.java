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

/* compiled from: GetSMSAuthCodeRequest */
public class m extends a {
    private String h = (d() + "/IUserInfoMng/getSMSAuthCode");
    private String i = "0";
    private String j;
    private String k = "7";
    private String l;
    private String m;
    private int n = 0;
    private String o;
    private String p;
    private String q;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "SMSAuthCodeReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.i);
            t.a(a, "userAccount", this.j);
            t.a(a, "reqClientType", this.k);
            t.a(a, "languageCode", this.m);
            t.a(a, "smsReqType", this.l);
            t.a(a, "mobilePhone", this.p);
            t.a(a, "plmn", this.q);
            a.endTag(null, "SMSAuthCodeReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.i);
            bundle.putString("userAccount", f.c(this.j));
            bundle.putString("reqClientType", this.k);
            bundle.putString("languageCode", this.m);
            bundle.putString("smsReqType", this.l);
            bundle.putString("mobilePhone", this.p);
            bundle.putString("plmn", this.q);
            com.huawei.hwid.core.c.b.a.b("GetSMSAuthCodeRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetSMSAuthCodeRequest", e.toString());
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
                            if (!"siteID".equals(name)) {
                                break;
                            }
                            String nextText = a.nextText();
                            try {
                                this.n = Integer.parseInt(nextText);
                            } catch (Exception e) {
                                com.huawei.hwid.core.c.b.a.b("GetSMSAuthCodeRequest", "rsp siteId[" + nextText + "] is invalid");
                            }
                            com.huawei.hwid.core.c.b.a.b("GetSMSAuthCodeRequest", "mSiteId: " + this.n);
                            break;
                        }
                        this.o = a.nextText();
                        com.huawei.hwid.core.c.b.a.a("GetSMSAuthCodeRequest", "mUserId: " + f.a(this.o));
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
        this.i = str;
    }

    public void g(String str) {
        this.j = str;
    }

    public void h(String str) {
        this.l = str;
    }

    public void i(String str) {
        this.m = str;
    }

    public void j(String str) {
        this.p = str;
    }

    public void k(String str) {
        this.q = str;
    }

    public Bundle h() {
        return super.h();
    }

    public m(Context context, String str, String str2, String str3) {
        g(str);
        i(d.g(context));
        h(str3);
        f(d.b(str));
        j(d.d(str2));
        k(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        d(context.getString(com.huawei.hwid.core.c.m.a(context, "CS_waiting_progress_message")));
        b(70002011);
        b(70002002);
        b(70002001);
        b(70002028);
        b(70001201);
        b(70002030);
        b(70001104);
        b(70001102);
    }
}
