package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.hwid.core.c.b;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: AddLoginAcctRequest */
public class a extends com.huawei.hwid.core.model.http.a {
    private String h = (d() + "/IUserInfoMng/addLoginAcct");
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;

    public a(Context context, String str, String str2, String str3, String str4, String str5) {
        b(true);
        this.i = str5;
        this.j = b.b(context, "com.huawei.hwid");
        this.k = str;
        this.l = str2;
        this.m = str3;
        this.n = str4;
        a(true);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "AddLoginAcctReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "userID", this.i);
            t.a(a, "accountType", this.k);
            t.a(a, "userAccount", this.l);
            t.a(a, "thirdOpenID", this.m);
            t.a(a, "reqClientType", this.j);
            t.a(a, "thirdAccessToken", this.n);
            a.endTag(null, "AddLoginAcctReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            com.huawei.hwid.core.c.b.a.b("AddLoginAcctRequest", "packedString XMLContent = " + f.a(byteArrayOutputStream2, true));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (Throwable e) {
                com.huawei.hwid.core.c.b.a.d("AddLoginAcctRequest", e.toString(), e);
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
                    com.huawei.hwid.core.c.b.a.a("AddLoginAcctRequest", "uid = " + nextText + ", mUid = " + this.i);
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
