package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.cloudservice.CloudRequestHandler;
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

/* compiled from: GetIpCountryRequest */
public class i extends a {
    private String h = (c() + "/IUserInfoMng/getIPCountry");
    private String i = "7";
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;
    private int o = 0;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetIPCountryReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "reqClientType", this.i);
            t.a(a, "plmn", this.j);
            a.endTag(null, "GetIPCountryReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("reqClientType", this.i);
            bundle.putString("plmn", this.j);
            com.huawei.hwid.core.c.b.a.b("getIpCountryRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("getIpCountryRequest", e.toString());
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
                        if (!"countryCallingCode".equals(name)) {
                            if (!"countryCode".equals(name)) {
                                if (!"nativeName".equals(name)) {
                                    if (!"englishName".equals(name)) {
                                        if (!"siteID".equals(name)) {
                                            break;
                                        }
                                        String nextText = a.nextText();
                                        try {
                                            this.o = Integer.parseInt(nextText);
                                        } catch (Exception e) {
                                            com.huawei.hwid.core.c.b.a.d("getIpCountryRequest", "rsp siteID[" + nextText + "] is invalid");
                                        }
                                        com.huawei.hwid.core.c.b.a.b("getIpCountryRequest", "mSiteID: " + this.o);
                                        break;
                                    }
                                    this.n = a.nextText();
                                    com.huawei.hwid.core.c.b.a.b("getIpCountryRequest", "mCountryEnglishName: " + this.n);
                                    break;
                                }
                                this.m = a.nextText();
                                com.huawei.hwid.core.c.b.a.b("getIpCountryRequest", "mCountryNativeName: " + this.m);
                                break;
                            }
                            this.l = a.nextText();
                            com.huawei.hwid.core.c.b.a.b("getIpCountryRequest", "mCountryCode: " + this.l);
                            break;
                        }
                        this.k = a.nextText();
                        com.huawei.hwid.core.c.b.a.b("getIpCountryRequest", "mCountryCallingCode: " + this.k);
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

    public void f(String str) {
        this.j = str;
    }

    public String g() {
        return this.h;
    }

    private String x() {
        return this.k;
    }

    private String y() {
        return this.l;
    }

    private String z() {
        return this.m;
    }

    private String A() {
        return this.n;
    }

    public int w() {
        return this.o;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("callingCode", x());
        h.putString("countryCode", y());
        h.putString("englishName", A());
        h.putString("nativeName", z());
        h.putInt("siteID", w());
        return h;
    }

    public i(Context context, Bundle bundle) {
        f(q.a(context, (int) MsgUrlService.RESULT_NOT_IMPL));
        d(2);
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        com.huawei.hwid.core.model.http.i.a(context, aVar, null, a(context, aVar, new j(context)));
    }
}
