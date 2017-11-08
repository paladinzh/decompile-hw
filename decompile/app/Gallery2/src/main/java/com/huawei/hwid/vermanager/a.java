package com.huawei.hwid.vermanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.huawei.hwid.core.d.b.e;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class a extends WebViewClient {
    private com.huawei.cloudservice.a a = null;
    private Context b;

    public a(Context context, com.huawei.cloudservice.a aVar) {
        this.a = aVar;
        this.b = context;
    }

    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        e.b("ReleaseAccountCenterWebViewClient", "onPageFinished");
        this.a.a(webView, str);
    }

    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        super.onPageStarted(webView, str, bitmap);
        e.b("ReleaseAccountCenterWebViewClient", "onPageStarted");
        this.a.a(webView, str, bitmap);
    }

    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        byte[] bArr;
        X509Certificate x509Certificate;
        e.a("ReleaseAccountCenterWebViewClient", "get safe https connect");
        e.a("ReleaseAccountCenterWebViewClient", "SSL ERROR");
        Bundle saveState = SslCertificate.saveState(sslError.getCertificate());
        if (saveState == null) {
            bArr = null;
        } else {
            bArr = saveState.getByteArray("x509-certificate");
        }
        if (bArr != null) {
            try {
                x509Certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bArr));
            } catch (CertificateException e) {
                x509Certificate = null;
            }
        } else {
            x509Certificate = null;
        }
        try {
            new com.huawei.hwid.core.b.b.a(this.b).checkServerTrusted(new X509Certificate[]{x509Certificate}, "ECDHE_RSA");
            sslErrorHandler.proceed();
        } catch (CertificateException e2) {
            super.onReceivedSslError(webView, sslErrorHandler, sslError);
        }
    }
}
