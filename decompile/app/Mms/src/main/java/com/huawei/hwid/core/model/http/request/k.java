package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.i;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetResourceRequest */
public class k extends a {
    String h = "";
    String i;
    int j = 0;
    String k;
    String l;
    private String m = (d() + "/IUserInfoMng/getResource");
    private String n;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetResourceReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "resourceID", this.n);
            if (!TextUtils.isEmpty(this.h)) {
                t.a(a, "ResourceOldVer", this.h);
            }
            if (!TextUtils.isEmpty(this.i)) {
                t.a(a, "languageCode", this.i);
            }
            t.a(a, "reqClientType", String.valueOf(this.j));
            a.endTag(null, "GetResourceReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            com.huawei.hwid.core.c.b.a.b("GetResourceRequest", "packedString:" + f.a(byteArrayOutputStream2, true));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetResourceRequest", e.toString());
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
                        if (!"ResourceContent".equals(name)) {
                            if (!"ResourceVer".equals(name)) {
                                break;
                            }
                            this.l = a.nextText();
                            break;
                        }
                        this.k = a.nextText();
                        com.huawei.hwid.core.c.b.a.b("GetResourceRequest", "mCountryCallingCode: " + this.k);
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

    public String w() {
        return this.k;
    }

    public String x() {
        return this.l;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("ResourceContent", w());
        h.putString("ResourceVer", x());
        return h;
    }

    public k(Context context, int i, Bundle bundle) {
        this.n = String.valueOf(i);
        this.i = d.g(context);
        d(2);
    }

    public static void a(Context context, int i, CloudRequestHandler cloudRequestHandler) {
        switch (i) {
            case 1:
                i = 100000;
                break;
            case 2:
                i = 100100;
                break;
            case 3:
                i = 100200;
                break;
        }
        a kVar = new k(context, i, null);
        Log.d("GetResourceRequest", "enter GetUserRightBaseUrl");
        i.a(context, kVar, null, kVar.a(context, kVar, new l(context, cloudRequestHandler)));
    }

    public String g() {
        return this.m;
    }
}
