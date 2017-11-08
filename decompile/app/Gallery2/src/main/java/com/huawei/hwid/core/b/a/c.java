package com.huawei.hwid.core.b.a;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Proxy;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.b.a;
import com.huawei.hwid.core.b.a.a.d;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.vermanager.b;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;

public class c {
    public static HttpResponse a(Context context, a aVar, String str) throws UnsupportedEncodingException, IllegalArgumentException, IllegalStateException, IOException, SSLPeerUnverifiedException {
        HttpPost httpPost = new HttpPost(aVar.a(context));
        HttpClient a = b.a().a(context, 18080, 18443);
        if (a != null) {
            HttpEntity stringEntity;
            e.b("HttpUtil", "the post request URI is:" + f.a(aVar.a(context)));
            String name = aVar.getClass().getName();
            e.b("HttpUtil", "GlobalSiteId = " + aVar.r() + ", request = " + name.substring(name.lastIndexOf(".") + 1));
            httpPost.addHeader("Connection", "Keep-Alive");
            if (d.URLType.equals(aVar.a())) {
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            } else {
                httpPost.addHeader("Content-Type", "text/html; charset=UTF-8");
            }
            a(context, httpPost, aVar, str);
            httpPost.getParams().setIntParameter("http.socket.timeout", 20000);
            httpPost.getParams().setIntParameter("http.connection.timeout", 20000);
            HttpClientParams.setRedirecting(httpPost.getParams(), false);
            if (aVar.a().equals(d.URLType)) {
                stringEntity = new StringEntity(aVar.f(), XmlUtils.INPUT_ENCODING);
                e.b("HttpUtil", "request.urlencode() = " + f.a(aVar.f(), true));
            } else {
                stringEntity = new StringEntity(aVar.e(), XmlUtils.INPUT_ENCODING);
                e.b("HttpUtil", "request.pack() = " + f.a(aVar.e(), true));
            }
            httpPost.setEntity(stringEntity);
            try {
                e.e("HttpUtil", "direct connect start!");
                return a.execute(httpPost);
            } catch (Exception e) {
                String property;
                int parseInt;
                if (VERSION.SDK_INT >= 14) {
                    property = System.getProperty("http.proxyHost");
                    name = System.getProperty("http.proxyPort");
                    if (name == null) {
                        name = "-1";
                    }
                    parseInt = Integer.parseInt(name);
                } else {
                    property = Proxy.getHost(context);
                    parseInt = Proxy.getPort(context);
                }
                e.b("HttpUtil", "proxyHost:" + property + " proxyPort:" + parseInt);
                return a(context, httpPost, a, e, property, parseInt);
            }
        }
        e.d("HttpUtil", "httpClient init Failed");
        throw new UnknownHostException("ERROR");
    }

    private static HttpResponse a(Context context, HttpPost httpPost, HttpClient httpClient, Exception exception, String str, int i) throws UnsupportedEncodingException, NullPointerException, IOException {
        if (str != null && str.length() > 0 && i != -1 && 1 != com.huawei.hwid.core.d.b.b(context)) {
            httpClient.getParams().setParameter("http.route.default-proxy", new HttpHost(str, i));
            try {
                e.b("HttpUtil", "have set the proxy, connect with the proxy");
                return httpClient.execute(httpPost);
            } catch (NullPointerException e) {
                e.d("HttpUtil", "ERR=" + exception.getMessage());
                throw new UnknownHostException("set proxy");
            } catch (Throwable e2) {
                e.d("HttpUtil", "set proxy  and  get http exception" + e2.getMessage(), e2);
                if ((e2 instanceof SSLPeerUnverifiedException) || (e2 instanceof SSLException) || (e2 instanceof SSLHandshakeException)) {
                    e.d("HttpUtil", e2.getMessage(), e2);
                    throw new SSLPeerUnverifiedException("SSL Exception");
                }
                e.d("HttpUtil", e2.getMessage(), e2);
                throw new UnknownHostException("set proxy");
            }
        } else if (exception instanceof UnsupportedEncodingException) {
            throw new UnsupportedEncodingException("UnsupportedEncodingException[don't set proxy]:" + exception.getMessage());
        } else if ((exception instanceof SSLPeerUnverifiedException) || (exception instanceof SSLHandshakeException) || (exception instanceof SSLException)) {
            e.d("HttpUtil", exception.getMessage(), exception);
            throw new SSLPeerUnverifiedException("SSL Exception");
        } else if (exception instanceof IllegalArgumentException) {
            throw new IllegalArgumentException("IllegalArgumentException[don't set proxy]:" + exception.getMessage());
        } else if (exception instanceof IllegalStateException) {
            throw new IllegalStateException("IllegalStateException[don't set proxy]:" + exception.getMessage());
        } else if (exception instanceof IOException) {
            e.d("HttpUtil", exception.getMessage(), exception);
            throw new IOException("IOException[don't set proxy]:" + exception.getMessage());
        } else {
            throw new UnknownHostException("don't set proxy");
        }
    }

    @SuppressLint({"TrulyRandom"})
    private static void a(Context context, HttpPost httpPost, a aVar, String str) {
        String str2 = "";
        HwAccount b = a.a(context).b();
        if (b == null) {
            b = a.a(context).c();
        }
        if (b != null) {
            str2 = a.a(context).a(b.d());
        }
        e.a("HttpUtil", "addHeader cookie= " + f.a(str2, true));
        httpPost.addHeader("Cookie", str2);
        switch (aVar.p()) {
            case 0:
                httpPost.addHeader("Authorization", String.valueOf(System.currentTimeMillis()));
                return;
            case 1:
                if (b == null) {
                    e.c("HttpUtil", "account is null ");
                    return;
                }
                Object g = b.g();
                String d = b.d();
                e.a("HttpUtil", "userId = " + f.a(d, true));
                if (!TextUtils.isEmpty(d) && !TextUtils.isEmpty(g)) {
                    String str3 = System.currentTimeMillis() + ":" + new SecureRandom().nextInt(1000);
                    httpPost.addHeader("Authorization", "Digest user=" + d + "," + "nonce" + "=" + str3 + "," + "response" + "=" + com.huawei.hwid.core.encrypt.d.a(str3 + ":" + aVar.q(), g));
                    return;
                }
                return;
            default:
                return;
        }
    }
}
