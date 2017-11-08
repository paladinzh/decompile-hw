package com.huawei.hwid.core.model.http;

import android.content.Context;
import android.net.Proxy;
import android.os.Build.VERSION;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.huawei.hwid.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
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

/* compiled from: HttpUtil */
public class h {
    public static HttpResponse a(Context context, a aVar, String str, HwAccount hwAccount) throws UnsupportedEncodingException, IllegalArgumentException, IllegalStateException, IOException, SSLPeerUnverifiedException {
        String property;
        int parseInt;
        boolean z = false;
        Object httpPost = new HttpPost(aVar.s());
        HttpClient a = a.a().a(context, 18080, 18443);
        if (a != null) {
            HttpEntity stringEntity;
            com.huawei.hwid.core.c.b.a.b("HttpUtil", "the post request URI is:" + f.a(aVar.s()));
            String name = aVar.getClass().getName();
            com.huawei.hwid.core.c.b.a.b("HttpUtil", "GlobalSiteId = " + aVar.r() + ", request = " + name.substring(name.lastIndexOf(".") + 1));
            httpPost.addHeader("Connection", "Keep-Alive");
            if (e.URLType.equals(aVar.a())) {
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            } else {
                httpPost.addHeader("Content-Type", "text/html; charset=UTF-8");
            }
            a(context, httpPost, aVar, str, hwAccount);
            httpPost.getParams().setIntParameter("http.socket.timeout", 20000);
            httpPost.getParams().setIntParameter("http.connection.timeout", 20000);
            HttpClientParams.setRedirecting(httpPost.getParams(), false);
            if (aVar.a().equals(e.URLType)) {
                stringEntity = new StringEntity(aVar.f(), "UTF-8");
            } else {
                stringEntity = new StringEntity(aVar.e(), "UTF-8");
            }
            httpPost.setEntity(stringEntity);
            try {
                com.huawei.hwid.core.c.b.a.e("HttpUtil", "direct connect start!");
                return a.execute(httpPost);
            } catch (Throwable e) {
                if (VERSION.SDK_INT >= 14) {
                    z = true;
                }
                if (z) {
                    property = System.getProperty("http.proxyHost");
                    String property2 = System.getProperty("http.proxyPort");
                    if (property2 == null) {
                        property2 = ThemeUtil.SET_NULL_STR;
                    }
                    parseInt = Integer.parseInt(property2);
                } else {
                    property = Proxy.getHost(context);
                    parseInt = Proxy.getPort(context);
                }
                com.huawei.hwid.core.c.b.a.b("HttpUtil", "proxyHost:" + property + " proxyPort:" + parseInt);
                if (property != null && property.length() > 0 && parseInt != -1 && 1 != d.b(context)) {
                    a.getParams().setParameter("http.route.default-proxy", new HttpHost(property, parseInt));
                    try {
                        com.huawei.hwid.core.c.b.a.b("HttpUtil", "have set the proxy, connect with the proxy");
                        return a.execute(httpPost);
                    } catch (NullPointerException e2) {
                        com.huawei.hwid.core.c.b.a.d("HttpUtil", "ERR=" + e.getMessage(), e);
                        throw new UnknownHostException("set proxy");
                    } catch (Throwable e3) {
                        com.huawei.hwid.core.c.b.a.d("HttpUtil", "set proxy  and  get http exception" + e3.getMessage(), e3);
                        if ((e3 instanceof SSLPeerUnverifiedException) || (e3 instanceof SSLException) || (e3 instanceof SSLHandshakeException)) {
                            com.huawei.hwid.core.c.b.a.d("HttpUtil", e3.getMessage(), e3);
                            throw new SSLPeerUnverifiedException("SSL Exception");
                        }
                        com.huawei.hwid.core.c.b.a.d("HttpUtil", e3.getMessage(), e3);
                        throw new UnknownHostException("set proxy");
                    }
                } else if (e3 instanceof UnsupportedEncodingException) {
                    throw new UnsupportedEncodingException("UnsupportedEncodingException[don't set proxy]:" + e3.getMessage());
                } else if ((e3 instanceof SSLPeerUnverifiedException) || (e3 instanceof SSLHandshakeException) || (e3 instanceof SSLException)) {
                    com.huawei.hwid.core.c.b.a.d("HttpUtil", e3.getMessage(), e3);
                    throw new SSLPeerUnverifiedException("SSL Exception");
                } else if (e3 instanceof IllegalArgumentException) {
                    throw new IllegalArgumentException("IllegalArgumentException[don't set proxy]:" + e3.getMessage());
                } else if (e3 instanceof IllegalStateException) {
                    throw new IllegalStateException("IllegalStateException[don't set proxy]:" + e3.getMessage());
                } else if (e3 instanceof IOException) {
                    com.huawei.hwid.core.c.b.a.d("HttpUtil", e3.getMessage(), e3);
                    throw new IOException("IOException[don't set proxy]:" + e3.getMessage());
                } else {
                    throw new UnknownHostException("don't set proxy");
                }
            }
        }
        com.huawei.hwid.core.c.b.a.d("HttpUtil", "httpClient init Failed");
        throw new UnknownHostException("ERROR");
    }

    private static void a(Context context, HttpPost httpPost, a aVar, String str, HwAccount hwAccount) {
        String str2 = null;
        switch (aVar.p()) {
            case 0:
                httpPost.addHeader("Authorization", String.valueOf(System.currentTimeMillis()));
                if (aVar.q() != null) {
                    HwAccount c = com.huawei.hwid.manager.f.a(context).c(context, str, null);
                    if (c != null) {
                        hwAccount = c;
                    } else {
                        com.huawei.hwid.core.c.b.a.a("HttpUtil", "use cached account to authorize");
                    }
                    if (hwAccount != null) {
                        str2 = hwAccount.e();
                    } else {
                        str2 = "";
                    }
                    if (!p.e(str2)) {
                        httpPost.addHeader("Cookie", str2);
                        return;
                    }
                    return;
                }
                return;
            case 1:
                if (aVar.u()) {
                    str2 = com.huawei.hwid.manager.f.a(context).c(context, str, null);
                }
                if (str2 != null) {
                    hwAccount = str2;
                } else {
                    com.huawei.hwid.core.c.b.a.a("HttpUtil", "use cached account to authorize");
                }
                if (hwAccount == null) {
                    com.huawei.hwid.core.c.b.a.c("HttpUtil", "account is null ");
                    return;
                }
                Object f = hwAccount.f();
                String c2 = hwAccount.c();
                String e = hwAccount.e();
                com.huawei.hwid.core.c.b.a.a("HttpUtil", "userId = " + f.a(c2));
                if (!(TextUtils.isEmpty(c2) || TextUtils.isEmpty(f))) {
                    String str3 = System.currentTimeMillis() + ":" + new SecureRandom().nextInt(1000);
                    httpPost.addHeader("Authorization", "Digest user=" + c2 + "," + "nonce" + "=" + str3 + "," + "response" + "=" + com.huawei.hwid.core.encrypt.d.a(str3 + ":" + aVar.q(), f));
                }
                if (!p.e(e)) {
                    httpPost.addHeader("Cookie", e);
                    return;
                }
                return;
            default:
                return;
        }
    }
}
