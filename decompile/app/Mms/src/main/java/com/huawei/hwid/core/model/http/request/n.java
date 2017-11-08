package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.SMSCountryInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.i;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetSMSCountryRequest */
public class n extends a {
    private String h = (c() + "/IUserInfoMng/getSMSCountry");
    private String i = "7";
    private ArrayList j = null;
    private SMSCountryInfo k;
    private boolean l;
    private Context m;
    private boolean n;

    public String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetSMSCountryReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "reqClientType", this.i);
            a.endTag(null, "GetSMSCountryReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            com.huawei.hwid.core.c.b.a.b("GetSMSCountryRequest", "packedString:" + f.a(byteArrayOutputStream2, true));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetSMSCountryRequest", e.toString());
            }
        }
    }

    public void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            switch (eventType) {
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b == 0) {
                        if (!"countryInfoList".equals(name)) {
                            if (!"CountryInfo".equals(name)) {
                                if (!this.l) {
                                    break;
                                }
                                com.huawei.hwid.core.helper.a.a.a(a, this.k, name, this.m);
                                break;
                            }
                            this.k = new SMSCountryInfo();
                            this.l = true;
                            break;
                        }
                        this.j = new ArrayList();
                        break;
                    }
                    this.n = true;
                    if (!"errorCode".equals(name)) {
                        if (!"errorDesc".equals(name)) {
                            break;
                        }
                        this.d = a.nextText();
                        break;
                    }
                    this.c = Integer.valueOf(a.nextText()).intValue();
                    break;
                case 3:
                    if (!"CountryInfo".equals(name)) {
                        break;
                    }
                    this.j.add(this.k);
                    f(str);
                    break;
                default:
                    break;
            }
        }
    }

    private void f(String str) {
        k.a(this.m, "countryInfolist.xml", str);
        k.a(this.m, r());
    }

    public String g() {
        return this.h;
    }

    public ArrayList w() {
        return this.j;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putParcelableArrayList("smsCountryList", w());
        return h;
    }

    public n(Context context, Bundle bundle) {
        this.m = context;
        if (bundle != null) {
            try {
                d(2);
                c(bundle.getInt("siteId"));
            } catch (Throwable e) {
                com.huawei.hwid.core.c.b.a.d("GetSMSCountryRequest", "NumberFormatException / " + e.toString(), e);
            }
        }
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        com.huawei.hwid.core.c.b.a.b("GetSMSCountryRequest", "request time ===== " + aVar.v());
        i.a(context, aVar, null, a(context, aVar, new o(context)));
    }
}
