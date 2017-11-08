package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.huawei.hwid.core.c.b.a;

/* compiled from: PrivacyPolicyActivity */
class z extends WebViewClient {
    final /* synthetic */ PrivacyPolicyActivity a;

    z(PrivacyPolicyActivity privacyPolicyActivity) {
        this.a = privacyPolicyActivity;
    }

    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        this.a.d.setVisibility(0);
        this.a.e.setVisibility(8);
    }

    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        if (!(str == null || !str.startsWith("http://") || this.a.k)) {
            try {
                this.a.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
            } catch (Throwable e) {
                a.d("PrivacyPolicyActivity", "there is no useable browser " + e.getMessage(), e);
            }
        }
        return true;
    }
}
