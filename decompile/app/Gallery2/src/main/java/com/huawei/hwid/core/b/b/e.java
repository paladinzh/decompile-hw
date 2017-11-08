package com.huawei.hwid.core.b.b;

import android.content.Context;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class e {
    private static ClientConnectionManager a;
    private static final ConnPerRoute b = new f();

    public static ClientConnectionManager a(Context context) {
        if (a == null) {
            SocketFactory gVar;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            try {
                gVar = new g(null, context);
            } catch (Throwable e) {
                com.huawei.hwid.core.d.b.e.d("OtaHttpClientConnetManager", "getConnectionManager Exception KeyManagementException", e);
                gVar = null;
            } catch (Throwable e2) {
                com.huawei.hwid.core.d.b.e.d("OtaHttpClientConnetManager", "getConnectionManager Exception NoSuchAlgorithmException", e2);
                gVar = null;
            } catch (Throwable e22) {
                com.huawei.hwid.core.d.b.e.d("OtaHttpClientConnetManager", "getConnectionManager Exception KeyStoreException", e22);
                gVar = null;
            } catch (Throwable e222) {
                com.huawei.hwid.core.d.b.e.d("OtaHttpClientConnetManager", "getConnectionManager Exception UnrecoverableKeyException", e222);
                gVar = null;
            }
            if (gVar != null) {
                com.huawei.hwid.core.d.b.e.a("OtaHttpClientConnetManager", "mysslSocketFactory is not null");
                gVar.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", gVar, 443));
            }
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 8080));
            HttpParams basicHttpParams = new BasicHttpParams();
            basicHttpParams.setIntParameter("http.conn-manager.max-total", 25);
            basicHttpParams.setParameter("http.conn-manager.max-per-route", b);
            a(new ThreadSafeClientConnManager(basicHttpParams, schemeRegistry));
        }
        return a;
    }

    private static synchronized void a(ClientConnectionManager clientConnectionManager) {
        synchronized (e.class) {
            a = clientConnectionManager;
        }
    }

    public static void a(SSLSocket sSLSocket) {
        if (sSLSocket != null) {
            com.huawei.hwid.core.d.b.e.b("OtaHttpClientConnetManager", "enter setEnableSafeCipherSuites");
            String[] enabledCipherSuites = sSLSocket.getEnabledCipherSuites();
            com.huawei.hwid.core.d.b.e.b("OtaHttpClientConnetManager", " current EnabledCipherSuites size" + enabledCipherSuites.length);
            List arrayList = new ArrayList();
            for (String str : enabledCipherSuites) {
                if (!(str.contains("RC4") || str.contains("DES") || str.contains("3DES") || str.contains("aNULL") || str.contains("eNULL"))) {
                    arrayList.add(str);
                }
            }
            com.huawei.hwid.core.d.b.e.b("OtaHttpClientConnetManager", "get safe EnabledCipherSuites list size =" + arrayList.size());
            String[] strArr = (String[]) arrayList.toArray(new String[arrayList.size()]);
            com.huawei.hwid.core.d.b.e.b("OtaHttpClientConnetManager", "get safe EnabledCipherSuites Array length =" + strArr.length);
            sSLSocket.setEnabledCipherSuites(strArr);
            return;
        }
        com.huawei.hwid.core.d.b.e.d("OtaHttpClientConnetManager", "socket error");
    }

    public static void a(Socket socket) {
        if (socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});
        }
    }
}
