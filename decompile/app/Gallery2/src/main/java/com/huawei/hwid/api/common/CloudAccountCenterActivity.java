package com.huawei.hwid.api.common;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import com.huawei.android.app.ActionBarEx;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.j;
import com.huawei.hwid.core.d.m;
import java.util.Locale;

public class CloudAccountCenterActivity extends Activity {
    private String a;
    private WebView b;
    private ProgressBar c;
    private ActionBar d;
    private String e;
    private String f = "";
    private String g = "";
    private String h = "";
    private com.huawei.cloudservice.a i = new b(this);

    private class a extends WebChromeClient {
        final /* synthetic */ CloudAccountCenterActivity a;

        private a(CloudAccountCenterActivity cloudAccountCenterActivity) {
            this.a = cloudAccountCenterActivity;
        }

        public void onProgressChanged(WebView webView, int i) {
            super.onProgressChanged(webView, i);
            this.a.c.setProgress(i);
        }

        public void onReceivedTitle(WebView webView, String str) {
            super.onReceivedTitle(webView, str);
            this.a.a(str);
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(j.d(this, "cs_webview"));
        b();
        a();
        this.e = this.a;
        a("");
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    private void a() {
        this.d = getActionBar();
        if (this.d != null) {
            if (m.a && b.g("com.huawei.android.app.ActionBarEx")) {
                try {
                    ActionBarEx.setStartIcon(this.d, true, null, new c(this));
                    this.d.setDisplayHomeAsUpEnabled(false);
                } catch (Exception e) {
                    this.d.setDisplayHomeAsUpEnabled(true);
                }
            } else {
                this.d.setDisplayHomeAsUpEnabled(true);
            }
        }
        this.c = (ProgressBar) findViewById(j.e(this, "wvProgressbar"));
        this.b = (WebView) findViewById(j.e(this, "webView"));
        this.b.setWebViewClient(com.huawei.hwid.vermanager.b.a(this, this.i));
        this.b.setWebChromeClient(new a());
        WebSettings settings = this.b.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setSupportZoom(false);
        settings.setSavePassword(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        this.b.removeJavascriptInterface("searchBoxJavaBridge_");
        this.b.removeJavascriptInterface("accessibility");
        this.b.removeJavascriptInterface("accessibilityTraversal");
        this.b.loadUrl(this.a);
    }

    private void b() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                try {
                    this.a = extras.getString("url");
                    this.f = extras.getString("key_app_id_to_web");
                    this.g = extras.getString("key_user_account_to_web");
                    this.h = extras.getString("key_service_token_to_web");
                } catch (Exception e) {
                    e.c("CloudAccountCenterActivity", e.getMessage());
                }
                if (TextUtils.isEmpty(this.a)) {
                    finish();
                    return;
                }
                String toLowerCase = this.a.toLowerCase(Locale.US);
                Object obj = (toLowerCase.startsWith("http://") || toLowerCase.startsWith("https://")) ? 1 : null;
                if (obj == null) {
                    this.a = "http://" + this.a;
                }
                return;
            }
            finish();
            return;
        }
        finish();
    }

    private void a(String str) {
        if (this.d != null) {
            this.d.setTitle(str);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (b.h()) {
            getMenuInflater().inflate(j.f(this, "cs_webview_menu_emui5"), menu);
        } else {
            getMenuInflater().inflate(j.f(this, "cs_webview_menu"), menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == 16908332) {
            finish();
        } else if (itemId != j.e(this, "menu_wv_goback")) {
            if (itemId == j.e(this, "menu_wv_copy_link")) {
                d();
            } else if (itemId == j.e(this, "menu_wv_open_in_browser")) {
                e();
            }
        } else if (!c()) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    protected void onPause() {
        super.onPause();
        if (this.b != null) {
            this.b.onPause();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.b != null) {
            this.b.setVisibility(8);
            this.b.removeAllViews();
            this.b.destroy();
        }
        this.b = null;
    }

    public void onBackPressed() {
        if (!c()) {
            super.onBackPressed();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private boolean c() {
        if (this.b == null || !this.b.canGoBack()) {
            return false;
        }
        this.b.goBack();
        return true;
    }

    private void d() {
        if (this.e != null) {
            ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("hwid_url", this.e));
        } else {
            e.d("CloudAccountCenterActivity", "url is null, copy failed.");
        }
    }

    private void e() {
        if (this.e != null) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(this.e)));
            } catch (ActivityNotFoundException e) {
                e.d("CloudAccountCenterActivity", "no browser app installed, open failed.");
            }
            return;
        }
        e.d("CloudAccountCenterActivity", "url is null, open failed.");
    }

    protected void onResume() {
        super.onResume();
        if (this.b != null) {
            this.b.onResume();
        }
    }

    private void a(WebView webView) {
        e.b("CloudAccountCenterActivity", "autoLogin");
        this.b.loadUrl("javascript:autoLogin(\"" + this.f + "\",\"" + this.g + "\",\"" + this.h + "\")");
    }
}
