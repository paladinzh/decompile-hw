package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.q;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.datatype.DeviceInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: UpdateUserAgrsRequest */
public class af extends a {
    private String h = (d() + "/IUserInfoMng/updateUserAgrs");
    private String i;
    private String j = "7";
    private DeviceInfo k;
    private AgreementVersion[] l;

    public af(Context context, String str, String str2, AgreementVersion[] agreementVersionArr) {
        this.i = str;
        if (agreementVersionArr != null) {
            this.l = (AgreementVersion[]) agreementVersionArr.clone();
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceType(q.a(context));
        deviceInfo.setDeviceIdInDeviceInfo(q.b(context));
        deviceInfo.setTerminalType(q.a());
        this.k = deviceInfo;
        b(true);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "UpdateUserAgrsReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "userID", this.i);
            t.a(a, "reqClientType", this.j);
            if (this.k != null) {
                a.startTag(null, "deviceInfo");
                com.huawei.hwid.core.helper.a.a.a(a, this.k);
                a.endTag(null, "deviceInfo");
            }
            if (this.l != null) {
                if (this.l.length > 0) {
                    a.startTag(null, "userAgrVers").attribute(null, "size", String.valueOf(this.l.length));
                    com.huawei.hwid.core.helper.a.a.a(a, this.l);
                    a.endTag(null, "userAgrVers");
                }
            }
            a.endTag(null, "UpdateUserAgrsReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("userID", f.c(this.i));
            bundle.putString("userAgrVers", d.a(this.l));
            com.huawei.hwid.core.c.b.a.b("UpdateUserAgrsRequest", "packedString XMLContent = " + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("UpdateUserAgrsRequest", e.toString());
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
                            break;
                        }
                        this.i = a.nextText();
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
}
