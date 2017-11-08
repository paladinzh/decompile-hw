package cn.com.xy.sms.sdk.net;

import java.io.ByteArrayInputStream;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/* compiled from: Unknown */
public class d {
    private static d d = null;
    private static d e = null;
    private SSLContext a;
    private SSLSocketFactory b;
    private HostnameVerifier c;
    private int f = 1;

    private d(int i) {
        this.f = i;
        this.c = new e(this);
    }

    public static d a(int i) {
        synchronized (d.class) {
            if (i != 0) {
                if (d == null) {
                    d = new d(i);
                }
                d dVar = d;
                return dVar;
            }
            if (e == null) {
                e = new d(i);
            }
            dVar = e;
            return dVar;
        }
    }

    private static Certificate a(String str, String str2) {
        return CertificateFactory.getInstance(str2).generateCertificate(new ByteArrayInputStream(str.getBytes()));
    }

    private static X509Certificate a(String str) {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(str.getBytes()));
    }

    private void a(HostnameVerifier hostnameVerifier) {
        this.c = hostnameVerifier;
    }

    private synchronized SSLContext c() {
        if (this.a == null) {
            TrustManagerFactory instance = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            instance.init(null);
            SSLContext instance2 = SSLContext.getInstance("TLS");
            instance2.init(null, instance.getTrustManagers(), new SecureRandom());
            this.a = instance2;
        }
        return this.a;
    }

    public final synchronized SSLSocketFactory a() {
        if (this.b == null) {
            this.b = c().getSocketFactory();
        }
        return this.b;
    }

    public final HostnameVerifier b() {
        return this.c;
    }
}
