package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.SiteInfo;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.encrypt.h;
import com.huawei.hwid.core.model.http.a;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: GetUserSiteIdRequest */
public class s extends a {
    private String h;
    private String i;
    private ArrayList j;
    private String k = "0";
    private String l;
    private String m;
    private String n = (d() + "/IUserInfoMng/getUserSiteId");

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            XmlSerializer a = t.a(byteArrayOutputStream);
            a.startDocument("UTF-8", Boolean.valueOf(true));
            a.startTag(null, "GetUserSiteIdReq");
            t.a(a, NumberInfo.VERSION_KEY, "10000");
            t.a(a, "accountType", this.k);
            t.a(a, "accountDigest", this.l);
            t.a(a, "phoneDigest", this.m);
            a.endTag(null, "GetUserSiteIdReq");
            a.endDocument();
            String byteArrayOutputStream2 = byteArrayOutputStream.toString("UTF-8");
            Bundle bundle = new Bundle();
            bundle.putString(NumberInfo.VERSION_KEY, "10000");
            bundle.putString("accountType", this.k);
            bundle.putString("accountDigest", this.l);
            bundle.putString("phoneDigest", this.m);
            com.huawei.hwid.core.c.b.a.b("GetUserSiteIdRequest", "packedString XMLContent = " + f.a(bundle));
            return byteArrayOutputStream2;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                com.huawei.hwid.core.c.b.a.d("GetUserSiteIdRequest", e.toString());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = t.a(str.getBytes("UTF-8"));
        Object obj = null;
        SiteInfo siteInfo = null;
        for (int eventType = a.getEventType(); 1 != eventType; eventType = a.next()) {
            String name = a.getName();
            switch (eventType) {
                case 2:
                    if ("result".equals(name)) {
                        this.b = Integer.valueOf(a.getAttributeValue(null, "resultCode")).intValue();
                    }
                    if (this.b == 0) {
                        if (!"existAccountFlag".equals(name)) {
                            if (!"siteInfoList".equals(name)) {
                                if (!"SiteInfo".equals(name)) {
                                    if (obj == null) {
                                        if (!"siteID".equals(name)) {
                                            break;
                                        }
                                        this.i = a.nextText();
                                        break;
                                    }
                                    com.huawei.hwid.core.helper.a.a.a(a, siteInfo, name);
                                    break;
                                }
                                siteInfo = new SiteInfo();
                                break;
                            }
                            this.j = new ArrayList();
                            int i = 1;
                            break;
                        }
                        this.h = a.nextText();
                        com.huawei.hwid.core.c.b.a.b("GetUserSiteIdRequest", "isExistCloudAccountType: " + this.h);
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
                    if (!"SiteInfo".equals(name)) {
                        if (!"siteInfoList".equals(name)) {
                            break;
                        }
                        obj = null;
                        break;
                    }
                    this.j.add(siteInfo);
                    break;
                default:
                    break;
            }
        }
    }

    public void f(String str) {
        this.k = str;
    }

    public void g(String str) {
        this.l = str;
    }

    public void h(String str) {
        this.m = str;
    }

    private String w() {
        return this.h;
    }

    private String x() {
        return this.i;
    }

    public String g() {
        return this.n;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("isAccountExist", w());
        h.putString("siteID", x());
        h.putParcelableArrayList("siteInfoList", this.j);
        return h;
    }

    public s(Context context, String str) {
        String b = d.b(str);
        f(b);
        if (!"2".equals(b) || str.startsWith("00") || str.startsWith("+")) {
            g(h.a(str.toLowerCase(Locale.ENGLISH)));
        } else {
            h(h.a(str));
        }
        b(70001201);
        b(70002002);
    }
}
