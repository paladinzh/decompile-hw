package com.huawei.hwid.core.b.b;

import android.content.Context;
import com.huawei.hwid.core.d.b.e;
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

public class b {
    private static ClientConnectionManager a;
    private static final ConnPerRoute b = new c();

    public static ClientConnectionManager a(Context context) {
        if (a == null) {
            SocketFactory dVar;
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            try {
                dVar = new d(null, context);
            } catch (Throwable e) {
                e.d("HttpClientConnetManager", "getConnectionManager Exception KeyManagementException", e);
                dVar = null;
            } catch (Throwable e2) {
                e.d("HttpClientConnetManager", "getConnectionManager Exception NoSuchAlgorithmException", e2);
                dVar = null;
            } catch (Throwable e22) {
                e.d("HttpClientConnetManager", "getConnectionManager Exception KeyStoreException", e22);
                dVar = null;
            } catch (Throwable e222) {
                e.d("HttpClientConnetManager", "getConnectionManager Exception UnrecoverableKeyException", e222);
                dVar = null;
            }
            if (dVar != null) {
                e.a("HttpClientConnetManager", "mysslSocketFactory is not null");
                dVar.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                schemeRegistry.register(new Scheme("https", dVar, 443));
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
        synchronized (b.class) {
            a = clientConnectionManager;
        }
    }

    public static void a(SSLSocket sSLSocket) {
        if (sSLSocket != null) {
            e.b("HttpClientConnetManager", "enter setEnableSafeCipherSuites");
            String[] enabledCipherSuites = sSLSocket.getEnabledCipherSuites();
            e.b("HttpClientConnetManager", " current EnabledCipherSuites size" + enabledCipherSuites.length);
            List arrayList = new ArrayList();
            for (String str : enabledCipherSuites) {
                if (!(str.contains("RC4") || str.contains("DES") || str.contains("3DES") || str.contains("aNULL") || str.contains("eNULL"))) {
                    arrayList.add(str);
                }
            }
            e.b("HttpClientConnetManager", "get safe EnabledCipherSuites list size =" + arrayList.size());
            String[] strArr = (String[]) arrayList.toArray(new String[arrayList.size()]);
            e.b("HttpClientConnetManager", "get safe EnabledCipherSuites Array length =" + strArr.length);
            sSLSocket.setEnabledCipherSuites(strArr);
            return;
        }
        e.d("HttpClientConnetManager", "socket error");
    }
}
