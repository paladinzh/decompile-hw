package cn.com.xy.sms.sdk.net;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/* compiled from: Unknown */
final class e implements HostnameVerifier {
    private /* synthetic */ d a;

    e(d dVar) {
        this.a = dVar;
    }

    public final boolean verify(String str, SSLSession sSLSession) {
        try {
            if (this.a.f == 0 && str != null) {
                if (str.indexOf("duoqu.in") != -1 || str.indexOf("bizport.cn") != -1) {
                    return true;
                }
            }
            HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
            return defaultHostnameVerifier != null ? defaultHostnameVerifier.verify(str, sSLSession) : false;
        } catch (Throwable th) {
            return false;
        }
    }
}
