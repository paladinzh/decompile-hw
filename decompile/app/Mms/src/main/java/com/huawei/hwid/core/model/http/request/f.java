package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetAgrVersRequest */
public class f extends a {
    private String h = (d() + "/IUserInfoMng/getAgrVers");
    private String i;
    private String[] j;
    private ArrayList k;

    public f(Context context, String[] strArr) {
        if (strArr != null) {
            this.j = (String[]) strArr.clone();
        }
        this.i = d.f(context);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetAgrVersReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "countryCode", this.i);
            if (this.j != null) {
                if (this.j.length > 0) {
                    a.startTag(null, "agrIdList").attribute(null, "size", String.valueOf(this.j.length));
                    for (String a2 : this.j) {
                        t.a(a, "id", a2);
                    }
                    a.endTag(null, "agrIdList");
                }
            }
            a.endTag(null, "GetAgrVersReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            com.huawei.hwid.core.c.b.a.b("GetAgrVersRequest", "packedString XMLContent = " + com.huawei.hwid.core.encrypt.f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetAgrVersRequest", e.toString());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        Object obj = null;
        AgreementVersion agreementVersion = null;
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            switch (eventType) {
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b == 0) {
                        if (!"curVers".equals(name)) {
                            if (!"AgrVer".equals(name)) {
                                if (obj == null) {
                                    break;
                                }
                                com.huawei.hwid.core.helper.a.a.a(a, agreementVersion, name);
                                break;
                            }
                            agreementVersion = new AgreementVersion();
                            break;
                        }
                        this.k = new ArrayList();
                        int i = 1;
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
                case 3:
                    if (!"AgrVer".equals(name)) {
                        if (!"curVers".equals(name)) {
                            break;
                        }
                        obj = null;
                        break;
                    }
                    this.k.add(agreementVersion);
                    break;
                default:
                    break;
            }
        }
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putParcelableArrayList("new_agrs", this.k);
        return h;
    }

    public String g() {
        return this.h;
    }
}
