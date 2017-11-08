package com.huawei.hwid.api.common;

import android.graphics.Bitmap;
import android.webkit.WebView;
import com.huawei.cloudservice.a;

class b implements a {
    final /* synthetic */ CloudAccountCenterActivity a;

    b(CloudAccountCenterActivity cloudAccountCenterActivity) {
        this.a = cloudAccountCenterActivity;
    }

    public void a(WebView webView, String str, Bitmap bitmap) {
        if (this.a.c != null) {
            this.a.c.setProgress(0);
            this.a.c.setVisibility(0);
        }
        this.a.e = str;
    }

    public void a(WebView webView, String str) {
        if (this.a.c != null) {
            this.a.c.setProgress(this.a.c.getMax());
            this.a.c.setVisibility(8);
        }
        this.a.e = str;
        this.a.a(webView);
    }
}
