package com.huawei.hwid.core.b.a.a;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.l;
import com.huawei.hwid.core.d.p;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.core.helper.handler.b;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class d extends com.huawei.hwid.core.b.a.a {
    private String h;
    private String i = "0";
    private String j;
    private String k = "";
    private String l = "7";
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private int t;

    static class a extends b {
        private CloudRequestHandler a;

        public a(Context context, CloudRequestHandler cloudRequestHandler) {
            super(context);
            this.a = cloudRequestHandler;
        }

        public void a(Bundle bundle) {
            super.a(bundle);
            bundle.putBoolean("isSuccess", true);
            this.a.onFinish(bundle);
        }

        public void b(Bundle bundle) {
            super.b(bundle);
            ErrorStatus errorStatus = (ErrorStatus) bundle.getParcelable("requestError");
            if (errorStatus == null) {
                this.a.onError(new ErrorStatus(32, "ErrorStatus is null"));
            } else {
                this.a.onError(errorStatus);
            }
        }
    }

    public d() {
        String str;
        if (s()) {
            str = "/IUserInfoMng/stAuth";
        } else {
            str = "/IUserInfoMng/serviceTokenAuth";
        }
        this.r = str;
        this.s = d() + this.r;
        this.t = 1;
        this.g = 0;
        a(com.huawei.hwid.core.b.a.a.d.URLType);
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        String a;
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            String str;
            a = p.a(byteArrayOutputStream);
            a.startDocument(XmlUtils.INPUT_ENCODING, Boolean.valueOf(true));
            a.startTag(null, "ServiceTokenAuthReq");
            p.a(a, "version", "10002");
            p.a(a, "serviceToken", this.j);
            String str2 = "appID";
            if (TextUtils.isEmpty(this.k)) {
                str = "com.huawei.hwid";
            } else {
                str = this.k;
            }
            p.a(a, str2, str);
            a.startTag(null, "deviceInfo");
            p.a(a, "deviceID", this.n);
            p.a(a, "deviceType", this.m);
            p.a(a, "terminalType", l.b());
            a.endTag(null, "deviceInfo");
            p.a(a, "reqClientType", this.l);
            p.a(a, "clientIP", "");
            p.a(a, "loginChannel", this.p);
            p.a(a, "uuid", this.o);
            p.a(a, "chkAcctChange", "0");
            p.a(a, "isGetAccount", "0");
            p.a(a, "isGetAgrVers", this.i);
            a.endTag(null, "ServiceTokenAuthReq");
            a.endDocument();
            a = byteArrayOutputStream.toString(XmlUtils.INPUT_ENCODING);
            Bundle bundle = new Bundle();
            bundle.putString("version", "10002");
            bundle.putString("serviceToken", this.j);
            String str3 = "appID";
            if (TextUtils.isEmpty(this.k)) {
                str = "com.huawei.hwid";
            } else {
                str = this.k;
            }
            bundle.putString(str3, str);
            bundle.putString("deviceID", this.n);
            bundle.putString("deviceType", this.m);
            bundle.putString("terminalType", l.b());
            bundle.putString("reqClientType", this.l);
            bundle.putString("clientIP", "");
            bundle.putString("loginChannel", this.p);
            bundle.putString("uuid", this.o);
            bundle.putString("chkAcctChange", "0");
            bundle.putString("isGetAccount", "0");
            e.b("ServiceTokenAuthRequest", "packedString:" + f.a(bundle));
            return a;
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                a = "ServiceTokenAuthRequest";
                e.d(a, e.getMessage());
            }
        }
    }

    protected void a(String str) throws XmlPullParserException, IOException {
        XmlPullParser a = p.a(str.getBytes(XmlUtils.INPUT_ENCODING));
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

    protected String f() {
        String str;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer append = stringBuffer.append("ver=").append("10002").append("&").append("st=").append(this.j).append("&").append("app=");
        if (TextUtils.isEmpty(this.k)) {
            str = "com.huawei.hwid";
        } else {
            str = this.k;
        }
        append.append(str).append("&").append("dvT=").append(this.m).append("&").append("dvID=").append(v()).append("&").append("tmT=").append(l.a()).append("&").append("clT=").append(this.l).append("&").append("cn=").append(this.p).append("&").append("chg=").append("0").append("&").append("gAc=").append("0").append("&").append("uuid=").append(this.o).append("&").append("agr=").append(this.i);
        Bundle bundle = new Bundle();
        bundle.putString("st", this.j);
        String str2 = "app";
        if (TextUtils.isEmpty(this.k)) {
            str = "com.huawei.hwid";
        } else {
            str = this.k;
        }
        bundle.putString(str2, str);
        bundle.putString("dvT", this.m);
        bundle.putString("dvID", v());
        bundle.putString("tmT", l.a());
        bundle.putString("clT", this.l);
        bundle.putString("cn", this.p);
        bundle.putString("chg", "0");
        bundle.putString("gAc", "0");
        bundle.putString("uuid", this.o);
        bundle.putString("agr", this.i);
        e.b("ServiceTokenAuthRequest", "postString:" + f.a(bundle));
        return stringBuffer.toString();
    }

    protected void b(String str) {
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("&");
            HashMap hashMap = new HashMap();
            Object obj = "";
            for (String str2 : split) {
                e.e("ServiceTokenAuthRequest", "infolist item:" + f.a(str2, true));
                String[] split2 = str2.split("=");
                hashMap.put(split2[0], split2[1]);
                obj = split2[0];
            }
            if (hashMap.containsKey("resultCode")) {
                this.b = Integer.valueOf((String) hashMap.get("resultCode")).intValue();
            }
            if (this.b != 0) {
                this.c = this.b;
                this.d = (String) hashMap.get(obj);
                e.e("ServiceTokenAuthRequest", "mErrorCode:" + this.c + ",mErrorDesc:" + f.a(this.d));
            } else {
                this.q = (String) hashMap.get("userID");
                this.h = (String) hashMap.get("agrFlags");
            }
        }
    }

    public String g() {
        return this.s;
    }

    private void e(String str) {
        this.k = str;
    }

    private void f(String str) {
        this.m = str;
    }

    private void g(String str) {
        this.j = str;
    }

    private void h(String str) {
        this.n = str;
    }

    private void i(String str) {
        this.o = str;
    }

    private void j(String str) {
        this.p = str;
    }

    public String u() {
        return this.q;
    }

    public Bundle h() {
        Bundle h = super.h();
        h.putString("agrFlags", this.h);
        return h;
    }

    private String v() {
        String str = "";
        if (!"NULL".equals(this.n) && !TextUtils.isEmpty(this.n)) {
            return this.n;
        }
        if ("NULL".equals(this.o) || TextUtils.isEmpty(this.o)) {
            return str;
        }
        return this.o;
    }

    public d(Context context, String str, String str2, int i, Bundle bundle) {
        String str3;
        if (s()) {
            str3 = "/IUserInfoMng/stAuth";
        } else {
            str3 = "/IUserInfoMng/serviceTokenAuth";
        }
        this.r = str3;
        this.s = d() + this.r;
        this.t = 1;
        if ("com.huawei.hwid".equalsIgnoreCase(str) || TextUtils.isEmpty(str)) {
            str = "com.huawei.hwid";
        }
        this.g = 0;
        a(com.huawei.hwid.core.b.a.a.d.URLType);
        g(com.huawei.hwid.core.encrypt.e.d(context, str2));
        e(context.getPackageName());
        h(l.c(context));
        i(l.i(context));
        f(l.a(context));
        c(i);
        d(this.t);
        j(com.huawei.hwid.core.d.a.a(context, str));
        a(true);
    }

    public void a(Context context, com.huawei.hwid.core.b.a.a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        com.huawei.hwid.core.b.a.d.a(context, aVar, str, a(context, aVar, new a(context, cloudRequestHandler)));
    }
}
