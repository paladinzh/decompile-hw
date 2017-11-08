package com.android.settings;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.util.Log;
import android.webkit.IWebViewUpdateService;
import android.webkit.IWebViewUpdateService.Stub;
import android.webkit.WebViewProviderInfo;
import java.util.ArrayList;

public class WebViewImplementation extends InstrumentedActivity implements OnCancelListener, OnDismissListener {
    private IWebViewUpdateService mWebViewUpdateService;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserManager.get(this).isAdminUser()) {
            this.mWebViewUpdateService = Stub.asInterface(ServiceManager.getService("webviewupdate"));
            try {
                WebViewProviderInfo[] providers = this.mWebViewUpdateService.getValidWebViewPackages();
                if (providers == null) {
                    Log.e("WebViewImplementation", "No WebView providers available");
                    finish();
                    return;
                }
                String currentValue = this.mWebViewUpdateService.getCurrentWebViewPackageName();
                if (currentValue == null) {
                    currentValue = "";
                }
                int currentIndex = -1;
                ArrayList<String> options = new ArrayList();
                final ArrayList<String> values = new ArrayList();
                for (WebViewProviderInfo provider : providers) {
                    if (Utils.isPackageEnabled(this, provider.packageName)) {
                        options.add(provider.description);
                        values.add(provider.packageName);
                        if (currentValue.contentEquals(provider.packageName)) {
                            currentIndex = values.size() - 1;
                        }
                    }
                }
                new Builder(this).setTitle(2131624192).setSingleChoiceItems((CharSequence[]) options.toArray(new String[0]), currentIndex, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            WebViewImplementation.this.mWebViewUpdateService.changeProviderAndSetting((String) values.get(which));
                        } catch (RemoteException e) {
                            Log.w("WebViewImplementation", "Problem reaching webviewupdate service", e);
                        }
                        WebViewImplementation.this.finish();
                    }
                }).setNegativeButton(17039360, null).setOnCancelListener(this).setOnDismissListener(this).show();
            } catch (RemoteException e) {
                Log.w("WebViewImplementation", "Problem reaching webviewupdate service", e);
                finish();
            }
        } else {
            finish();
        }
    }

    protected int getMetricsCategory() {
        return 405;
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
