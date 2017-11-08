package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.q;
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

/* compiled from: UpdateUserPwdByOldRequest */
public class ag extends a {
    private String h = "7";
    private String i;
    private String j = "0";
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p = (d() + "/IUserPwdMng/updateUserPwdByOld");

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "UpdateUserPwdByOldReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.j);
            t.a(a, "userAccount", this.i);
            t.a(a, "oldPassword", this.l);
            t.a(a, "newPassword", this.k);
            t.a(a, "reqClientType", this.h);
            t.a(a, "reloginFlag", this.m);
            t.a(a, "deviceType", this.o);
            t.a(a, "deviceID", this.n);
            a.endTag(null, "UpdateUserPwdByOldReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.j);
            bundle.putString("userAccount", f.c(this.i));
            bundle.putString("oldPassword", this.l);
            bundle.putString("newPassword", this.k);
            bundle.putString("reqClientType", this.h);
            bundle.putString("reloginFlag", this.m);
            bundle.putString("deviceType", this.o);
            bundle.putString("deviceID", this.n);
            com.huawei.hwid.core.c.b.a.b("UpdateUserPwdByOldRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("UpdateUserPwdByOldRequest", e.toString());
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

    public String g() {
        return this.p;
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
        this.k = str;
    }

    public void j(String str) {
        this.n = str;
    }

    public void k(String str) {
        this.o = str;
    }

    public Bundle h() {
        return super.h();
    }

    public ag(Context context, String str, String str2, String str3, Bundle bundle) {
        f(str);
        g(d.b(str));
        h(str2);
        i(str3);
        j(q.b(context));
        k(q.a(context, q.b(context)));
        b(70002023);
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        i.a(context, aVar, str, a(context, aVar, new ah(context, cloudRequestHandler)));
    }
}
