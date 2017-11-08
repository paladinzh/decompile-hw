package com.huawei.hwid.core.model.http.request;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.a;
import com.huawei.hwid.core.model.http.e;
import com.huawei.hwid.core.model.http.i;
import java.io.IOException;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: VerifyPasswordRequest */
public class am extends a {
    private String h = "7";
    private String i;
    private String j;
    private String k;
    private String l = "/IUserPwdMng/verifyPassword";
    private String m = (d() + this.l);

    public am() {
        a(e.URLType);
    }

    public am(Context context, String str, String str2, String str3, String str4) {
        a(e.URLType);
        f(str2);
        g(str);
        h(str3);
        i(str4);
        b(70002003);
    }

    protected String f() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ver=").append("10000").append("&").append("acT=").append(this.k).append("&").append("ac=").append(this.i).append("&").append("pw=").append(this.j).append("&").append("clT=").append(this.h);
        Bundle bundle = new Bundle();
        bundle.putString("acT", this.k);
        bundle.putString("ac", f.c(this.i));
        bundle.putString("pw", this.j);
        bundle.putString("clT", this.h);
        com.huawei.hwid.core.c.b.a.b("VerifyPasswordRequest", "postString:" + f.a(bundle));
        return stringBuffer.toString();
    }

    protected void b(String str) {
        if (!TextUtils.isEmpty(str)) {
            String[] split = str.split("&");
            HashMap hashMap = new HashMap();
            Object obj = "";
            for (String str2 : split) {
                com.huawei.hwid.core.c.b.a.e("VerifyPasswordRequest", "infolist item:" + f.a(str2));
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
                com.huawei.hwid.core.c.b.a.e("VerifyPasswordRequest", "mErrorCode:" + this.c + ",mErrorDesc:" + f.a(this.d));
            }
        }
    }

    public void f(String str) {
        this.j = str;
    }

    public void g(String str) {
        this.i = str;
    }

    public void h(String str) {
        this.k = str;
    }

    public void i(String str) {
        this.h = str;
    }

    protected String e() throws IllegalArgumentException, IllegalStateException, IOException {
        return null;
    }

    protected void a(String str) throws XmlPullParserException, IOException {
    }

    public String g() {
        return this.m;
    }

    public void a(Context context, a aVar, String str, CloudRequestHandler cloudRequestHandler) {
        i.a(context, aVar, str, a(context, aVar, new an(context, cloudRequestHandler)));
    }
}
