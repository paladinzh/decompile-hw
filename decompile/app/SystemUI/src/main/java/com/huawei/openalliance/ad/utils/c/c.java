package com.huawei.openalliance.ad.utils.c;

import com.huawei.openalliance.ad.utils.b.d;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/* compiled from: Unknown */
public class c {
    private HttpParams a;
    private ClientConnectionManager b;

    /* compiled from: Unknown */
    static class a extends DefaultConnectionKeepAliveStrategy {
        a() {
        }
    }

    public c() {
        a();
    }

    @SuppressWarnings(justification = "h00193325/There will be a lot of exceptions, but the business does not need to be distinguished, just catch the Exception type", value = {"REC_CATCH_EXCEPTION"})
    private void a() {
        SocketFactory bVar;
        this.a = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(this.a, b());
        ConnManagerParams.setTimeout(this.a, (long) d());
        ConnManagerParams.setMaxConnectionsPerRoute(this.a, new ConnPerRouteBean(c()));
        HttpConnectionParams.setConnectionTimeout(this.a, e());
        HttpConnectionParams.setSoTimeout(this.a, f());
        HttpProtocolParams.setVersion(this.a, HttpVersion.HTTP_1_1);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        try {
            bVar = new b(null);
        } catch (Throwable e) {
            d.a("HttpConnectionManager", "creat ssl socket exception", e);
            bVar = null;
            if (bVar == null) {
                bVar = SSLSocketFactory.getSocketFactory();
                d.c("HttpConnectionManager", "use default ssl socket factory");
            }
            bVar.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", bVar, 443));
            this.b = new ThreadSafeClientConnManager(this.a, schemeRegistry);
        } catch (Throwable e2) {
            d.a("HttpConnectionManager", "creat ssl socket exception", e2);
            bVar = null;
            if (bVar == null) {
                bVar = SSLSocketFactory.getSocketFactory();
                d.c("HttpConnectionManager", "use default ssl socket factory");
            }
            bVar.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", bVar, 443));
            this.b = new ThreadSafeClientConnManager(this.a, schemeRegistry);
        } catch (Throwable e22) {
            d.a("HttpConnectionManager", "creat ssl socket exception", e22);
            bVar = null;
            if (bVar == null) {
                bVar = SSLSocketFactory.getSocketFactory();
                d.c("HttpConnectionManager", "use default ssl socket factory");
            }
            bVar.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", bVar, 443));
            this.b = new ThreadSafeClientConnManager(this.a, schemeRegistry);
        } catch (Throwable e222) {
            d.a("HttpConnectionManager", "creat ssl socket exception", e222);
            bVar = null;
            if (bVar == null) {
                bVar = SSLSocketFactory.getSocketFactory();
                d.c("HttpConnectionManager", "use default ssl socket factory");
            }
            bVar.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", bVar, 443));
            this.b = new ThreadSafeClientConnManager(this.a, schemeRegistry);
        }
        if (bVar == null) {
            bVar = SSLSocketFactory.getSocketFactory();
            d.c("HttpConnectionManager", "use default ssl socket factory");
        }
        bVar.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        schemeRegistry.register(new Scheme("https", bVar, 443));
        this.b = new ThreadSafeClientConnManager(this.a, schemeRegistry);
    }

    protected int b() {
        return 25;
    }

    protected int c() {
        return 10;
    }

    protected int d() {
        return 6000;
    }

    protected int e() {
        return 5000;
    }

    protected int f() {
        return 10000;
    }

    protected HttpRequestRetryHandler g() {
        return new DefaultHttpRequestRetryHandler(2, true);
    }

    public HttpClient h() {
        HttpClient defaultHttpClient = new DefaultHttpClient(this.b, this.a);
        defaultHttpClient.setKeepAliveStrategy(new a());
        defaultHttpClient.setHttpRequestRetryHandler(g());
        return defaultHttpClient;
    }
}
