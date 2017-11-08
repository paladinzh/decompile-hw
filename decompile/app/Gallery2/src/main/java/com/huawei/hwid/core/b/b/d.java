package com.huawei.hwid.core.b.b;

import android.content.Context;
import android.os.Build.VERSION;
import com.huawei.hwid.core.d.b.e;
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

class d extends SSLSocketFactory {
    private static Object c = new Object();
    SSLContext a = SSLContext.getInstance("TLS");
    Context b;

    public d(KeyStore keyStore, Context context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        a aVar;
        super(keyStore);
        this.b = context;
        try {
            aVar = new a(this.b);
        } catch (Throwable e) {
            e.d("MySSLSocketFactory", "Initialize AccountX509TrustManager failed.", e);
            aVar = null;
        }
        this.a.init(null, new TrustManager[]{aVar}, new SecureRandom());
    }

    public Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException, UnknownHostException {
        SSLSocket sSLSocket;
        Socket createSocket = this.a.getSocketFactory().createSocket(socket, str, i, z);
        if (createSocket instanceof SSLSocket) {
            sSLSocket = (SSLSocket) createSocket;
        } else {
            sSLSocket = null;
        }
        e.b("MySSLSocketFactory", "host =" + str + "port=" + i + "autoclouse=" + z);
        b.a(sSLSocket);
        if (str.contains("hicloud.com")) {
            if (VERSION.SDK_INT >= 22) {
                getHostnameVerifier().verify(str, sSLSocket);
            } else {
                synchronized (c) {
                    getHostnameVerifier().verify(str, sSLSocket);
                }
            }
        }
        return sSLSocket;
    }

    public Socket createSocket() throws IOException {
        SSLSocket sSLSocket;
        Socket createSocket = this.a.getSocketFactory().createSocket();
        if (createSocket instanceof SSLSocket) {
            sSLSocket = (SSLSocket) createSocket;
        } else {
            sSLSocket = null;
        }
        b.a(sSLSocket);
        return sSLSocket;
    }
}
