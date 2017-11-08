package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.google.android.gms.common.Scopes;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: ResetPwdByEMailRequest */
public class x extends a {
    private String h = "7";
    private String i;
    private String j = "0";
    private String k = (d() + "/IUserPwdMng/resetPwdByEMail");
    private String l;
    private String m;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "ResetPwdByEMailReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.j);
            t.a(a, "userAccount", this.i);
            t.a(a, "reqClientType", this.h);
            t.a(a, Scopes.EMAIL, this.m);
            a.endTag(null, "ResetPwdByEMailReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.j);
            bundle.putString("userAccount", f.c(this.i));
            bundle.putString("reqClientType", this.h);
            bundle.putString(Scopes.EMAIL, this.m);
            com.huawei.hwid.core.c.b.a.b("ResetPwdByEMailRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("ResetPwdByEMailRequest", e.toString());
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
                        if (!"userEMail".equals(name)) {
                            break;
                        }
                        this.l = a.nextText();
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
        return this.k;
    }

    public void f(String str) {
        this.i = str;
    }

    public void g(String str) {
        this.m = str;
    }

    public void h(String str) {
        this.j = str;
    }

    public Bundle h() {
        return super.h();
    }

    public x(Context context, String str, String str2, int i) {
        f(str2);
        g(str);
        c(i);
        h(d.b(str2));
        d(context.getString(m.a(context, "CS_email_reset_pwd_submit")));
        b(70002001);
        b(70002009);
        b(70002008);
        b(70001201);
        b(70001104);
        b(70001102);
    }
}
