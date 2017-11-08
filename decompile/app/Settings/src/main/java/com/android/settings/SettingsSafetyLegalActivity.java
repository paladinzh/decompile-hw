package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;

public class SettingsSafetyLegalActivity extends AlertActivity implements OnCancelListener, OnClickListener {
    private AlertDialog mErrorDialog = null;
    private WebView mWebView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 22) {
            finish();
            return;
        }
        String userSafetylegalUrl = SystemProperties.get("ro.url.safetylegal");
        Configuration configuration = getResources().getConfiguration();
        String language = configuration.locale.getLanguage();
        String country = configuration.locale.getCountry();
        String loc = String.format("locale=%s-%s", new Object[]{language, country});
        userSafetylegalUrl = String.format("%s&%s", new Object[]{userSafetylegalUrl, loc});
        this.mWebView = new WebView(this);
        this.mWebView.getSettings().setJavaScriptEnabled(false);
        if (savedInstanceState == null) {
            this.mWebView.loadUrl(userSafetylegalUrl);
        } else {
            this.mWebView.restoreState(savedInstanceState);
        }
        this.mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                SettingsSafetyLegalActivity.this.mAlert.setTitle(SettingsSafetyLegalActivity.this.getString(2131625519));
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                SettingsSafetyLegalActivity.this.showErrorAndFinish(failingUrl);
            }
        });
        AlertParams p = this.mAlertParams;
        p.mTitle = getString(2131625521);
        p.mView = this.mWebView;
        p.mForceInverseBackground = true;
        setupAlert();
    }

    private void showErrorAndFinish(String url) {
        if (this.mErrorDialog == null) {
            this.mErrorDialog = new Builder(this).setTitle(2131625519).setPositiveButton(17039370, this).setOnCancelListener(this).setCancelable(true).create();
        } else if (this.mErrorDialog.isShowing()) {
            this.mErrorDialog.dismiss();
        }
        this.mErrorDialog.setMessage(getResources().getString(2131625520, new Object[]{url}));
        this.mErrorDialog.show();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mErrorDialog != null) {
            this.mErrorDialog.dismiss();
            this.mErrorDialog = null;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() != 4 || event.getAction() != 0 || !this.mWebView.canGoBack()) {
            return super.dispatchKeyEvent(event);
        }
        this.mWebView.goBack();
        return true;
    }

    public void onClick(DialogInterface dialog, int whichButton) {
        finish();
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }

    public void onSaveInstanceState(Bundle icicle) {
        this.mWebView.saveState(icicle);
        super.onSaveInstanceState(icicle);
    }
}
