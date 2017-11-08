package com.huawei.hwid.b;

import android.content.Context;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.model.a.b;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/* compiled from: ReleaseVersionManager */
public final class c extends b {
    private static c o;
    private HttpClient p;

    private c() {
    }

    public static synchronized c k() {
        c cVar;
        synchronized (c.class) {
            if (o == null) {
                o = new c();
                l();
            }
            cVar = o;
        }
        return cVar;
    }

    private static void l() {
        a = "http://setting.hicloud.com:8080/AccountServer";
        b = "https://setting.hicloud.com:443/AccountServer";
        c = b + "/IUserInfoMng/updateHeadPic?Version=" + "10000";
        d = "https";
        e = "login.vmall.com";
        f = d + "://" + e + "/" + "oauth2/authorize";
        g = "https://api.vmall.com/rest.php";
        h = d + "://" + e + "/" + "oauth2/oob#";
        i = "http://oobe.vmall.com/";
        j = "https://hwid1.vmall.com/oauth2/portal/stHideLogin.jsp";
        k = "https://hwid1.vmall.com/oauth2/web/wapBindPhoneNumber.jsp";
        l = "https://hwid1.vmall.com/oauth2/mobile/login.jsp";
        m = "wapBindPhoneNumberTip.jsp?";
        n = "209207";
    }

    public HttpClient a(Context context, int i, int i2) {
        a.b("ReleaseVersionManager", "getSafeHttpClient");
        this.p = new DefaultHttpClient(b.a(context), null);
        return this.p;
    }
}
