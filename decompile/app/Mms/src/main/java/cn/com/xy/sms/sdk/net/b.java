package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/* compiled from: Unknown */
public final class b {
    private SSLSocketFactory a = null;

    public static HttpsURLConnection a(String str) {
        return a(str, 1);
    }

    public static HttpsURLConnection a(String str, int i) {
        HttpsURLConnection httpsURLConnection;
        try {
            httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
            new b().a(httpsURLConnection, i);
            String onLineConfigureData = DexUtil.getOnLineConfigureData(4);
            if (StringUtils.isNull(onLineConfigureData)) {
                onLineConfigureData = "bizport.cn/66dc91e8b78b1c284027a3eb1be0a70e";
            }
            httpsURLConnection.addRequestProperty("referer", onLineConfigureData);
            httpsURLConnection.connect();
            return httpsURLConnection;
        } catch (Throwable th) {
            return httpsURLConnection;
        }
    }

    private void a(URL url) {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        if (httpURLConnection instanceof HttpsURLConnection) {
            a((HttpsURLConnection) httpURLConnection, 1);
        }
    }

    private void a(HttpsURLConnection httpsURLConnection, int i) {
        if (this.a == null) {
            try {
                this.a = d.a(i).a();
            } catch (Throwable th) {
                if (th instanceof IOException) {
                    IOException iOException = (IOException) th;
                } else {
                    IOException iOException2 = new IOException(th);
                }
            }
        }
        if (this.a != null) {
            httpsURLConnection.setSSLSocketFactory(this.a);
        }
        try {
            httpsURLConnection.setHostnameVerifier(d.a(i).b());
        } catch (Throwable th2) {
            iOException2 = new IOException(th2);
        }
    }

    public static HttpsURLConnection b(String str) {
        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(str).openConnection();
            try {
                new b().a(httpsURLConnection, 1);
                return httpsURLConnection;
            } catch (Throwable th) {
                return httpsURLConnection;
            }
        } catch (Throwable th2) {
            return null;
        }
    }
}
