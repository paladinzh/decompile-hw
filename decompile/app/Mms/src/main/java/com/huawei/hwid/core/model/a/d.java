package com.huawei.hwid.core.model.a;

import android.content.Context;
import com.huawei.hwid.core.c.b.a;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.ssl.SSLSocketFactory;

/* compiled from: HttpClientConnetManager */
class d extends SSLSocketFactory {
    SSLContext a = SSLContext.getInstance("TLS");
    Context b;

    public d(KeyStore keyStore, Context context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        a aVar;
        super(keyStore);
        this.b = context;
        try {
            aVar = new a(this.b);
        } catch (Throwable e) {
            a.d("MySSLSocketFactory", "Initialize AccountX509TrustManager failed.", e);
            aVar = null;
        }
        this.a.init(null, new TrustManager[]{aVar}, new SecureRandom());
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException, UnknownHostException {
        SSLSocket sSLSocket = (SSLSocket) this.a.getSocketFactory().createSocket(socket, str, i, z);
        a.b("MySSLSocketFactory", "host =" + str + "port=" + i + "autoclouse=" + z);
        b.a(sSLSocket);
        if (str.contains("hicloud.com")) {
            getHostnameVerifier().verify(str, sSLSocket);
        }
        return sSLSocket;
    }

    public Socket createSocket() throws IOException {
        SSLSocket sSLSocket = (SSLSocket) this.a.getSocketFactory().createSocket();
        b.a(sSLSocket);
        return sSLSocket;
    }
}
