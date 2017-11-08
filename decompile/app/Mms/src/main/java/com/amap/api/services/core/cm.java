package com.amap.api.services.core;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/* compiled from: HttpUrlUtil */
class cm implements HostnameVerifier {
    final /* synthetic */ cg a;

    cm(cg cgVar) {
        this.a = cgVar;
    }

    public boolean verify(String str, SSLSession sSLSession) {
        return HttpsURLConnection.getDefaultHostnameVerifier().verify("*.amap.com", sSLSession);
    }
}
