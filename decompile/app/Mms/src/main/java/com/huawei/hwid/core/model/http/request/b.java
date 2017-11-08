package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: AddLoginForThirdRequest */
public class b extends a {
    private String h;
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;

    public b(Context context, String str, String str2, int i, String str3, String str4, String str5, Bundle bundle) {
        this.h = d() + "/IUserInfoMng/bindAcctForThird";
        this.g = 0;
        this.i = str5;
        this.j = com.huawei.hwid.core.c.b.b(context, "com.huawei.hwid");
        String b = d.b(str);
        this.k = b;
        this.l = str;
        this.m = str2;
        if (!p.e(str4)) {
            this.n = str4;
        }
        b(70002002);
        if ("2".equals(b)) {
            b(70002011);
            b(70002039);
            b(70001201);
        }
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "BindAcctForThirdReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "userID", this.i);
            t.a(a, "accountType", this.k);
            t.a(a, "userAccount", this.l);
            t.a(a, "password", this.m);
            t.a(a, "reqClientType", this.j);
            t.a(a, "authCode", this.n);
            a.endTag(null, "BindAcctForThirdReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("userID", this.i);
            bundle.putString("accountType", this.k);
            bundle.putString("userAccount", f.c(this.l));
            bundle.putString("password", this.m);
            bundle.putString("reqClientType", this.j);
            bundle.putString("authCode", this.n);
            com.huawei.hwid.core.c.b.a.b("AddLoginForThirdRequest", "packedString XMLContent = " + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("AddLoginForThirdRequest", e.toString());
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
                    String nextText = a.nextText();
                    com.huawei.hwid.core.c.b.a.b("AddLoginForThirdRequest", "uid = " + nextText + ", mUid = " + this.i);
                    if (!this.i.equals(nextText)) {
                        this.b = NearbyPoint.QUERY_RESULT_RECEIVE;
                    }
                }
            }
        }
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("userName", this.l);
        h.putString("accountType", this.k);
        return h;
    }

    public String g() {
        return this.h;
    }
}
