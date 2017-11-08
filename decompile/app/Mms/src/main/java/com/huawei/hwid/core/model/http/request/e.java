package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetActivateEMailURLRequest */
public class e extends a {
    private String h = "7";
    private String i = (d() + "/IUserInfoMng/getActivateEMailURL");
    private String j = "0";
    private String k;
    private String l;
    private String m;

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetActivateEMailURLReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.j);
            t.a(a, "userAccount", this.k);
            t.a(a, "reqClientType", this.h);
            t.a(a, Scopes.EMAIL, this.m);
            a.endTag(null, "GetActivateEMailURLReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.j);
            bundle.putString("userAccount", f.c(this.k));
            bundle.putString("reqClientType", this.h);
            bundle.putString(Scopes.EMAIL, this.m);
            com.huawei.hwid.core.c.b.a.b("GetActivateEMailURLRequest", "packedString:" + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetActivateEMailURLRequest", e.toString());
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
        return this.i;
    }

    public void f(String str) {
        if (TextUtils.isEmpty(str)) {
            this.k = "";
        } else {
            this.k = str.toLowerCase(Locale.getDefault());
        }
    }

    public void g(String str) {
        if (TextUtils.isEmpty(str)) {
            this.m = "";
        } else {
            this.m = str.toLowerCase(Locale.getDefault());
        }
    }

    public void h(String str) {
        this.j = str;
    }

    public Bundle h() {
        return super.h();
    }

    public e(Context context, String str, String str2, Bundle bundle) {
        d(context.getString(m.a(context, "CS_sending_email_waiting_message")));
        f(str);
        g(str2);
        h(d.b(str));
        b(70002008);
        b(70002019);
        b(70001102);
        b(70001104);
    }
}
