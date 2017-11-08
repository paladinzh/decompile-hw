package com.huawei.hwid.vermanager;

import android.content.Context;
import com.huawei.hwid.core.b.b.b;
import com.huawei.hwid.core.d.b.e;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

public final class VersionManager extends c {
    private static VersionManager B;
    private HttpClient C;

    public static synchronized VersionManager h() {
        VersionManager versionManager;
        synchronized (VersionManager.class) {
            if (B == null) {
                B = new VersionManager();
            }
            versionManager = B;
        }
        return versionManager;
    }

    private VersionManager() {
        i();
    }

    private void i() {
        this.a = "http://setting{0}.hicloud.com:8080/AccountServer";
        this.b = "https://setting{0}.hicloud.com:443/AccountServer";
        this.c = this.b + "/IUserInfoMng/updateHeadPic?Version=" + "10002";
        this.d = "https://setting.hicloud.com/AccountServer/globalSiteCountryList.xml?Version=10002";
        this.j = "https://hwid{0}.vmall.com:443/CAS/mobile/delUser.html?";
        this.k = "https://login.vmall.com/oauth2/authorize";
        this.l = "https://api.vmall.com/rest.php";
        this.m = "https://login.vmall.com/oauth2/oob#";
        this.o = "https://login.vmall.com/oauth2/v2/";
        this.n = "https://login.vmall.com/oauth2/v2/authorize";
        this.p = "https://login.vmall.com/connect/v2/logout";
        this.q = "http://oobe.vmall.com/";
        this.r = "https://hwid1.vmall.com/oauth2/portal/stHideLogin.jsp";
        this.s = "https://hwid1.vmall.com/oauth2/web/wapBindPhoneNumber.jsp";
        this.t = "https://hwid1.vmall.com/oauth2/mobile/login.jsp";
        this.u = "wapBindPhoneNumberTip.jsp?";
        this.w = "https://query.hicloud.com/hwid/v2/";
        this.v = "209207";
        this.A = "https://hwid{0}.vmall.com";
        this.y = "/CAS/mobile/standard/welcome.html";
        this.z = "/CAS/portal/userCenter/index.html";
        this.x = "/CAS/mobile/stLogin.html";
    }

    public String a() {
        return this.v;
    }

    public String b() {
        return this.a;
    }

    public String c() {
        return this.b;
    }

    public String d() {
        return this.c;
    }

    public String e() {
        return this.d;
    }

    public HttpClient a(Context context, int i, int i2) {
        e.b("ReleaseVersionManager", "getSafeHttpClient");
        this.C = new DefaultHttpClient(b.a(context), null);
        return this.C;
    }

    public String g() {
        return this.w;
    }

    public HttpClient a(Context context) {
        return new DefaultHttpClient(com.huawei.hwid.core.b.b.e.a(context), null);
    }
}
