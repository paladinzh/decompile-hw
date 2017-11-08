package com.google.android.gms.internal;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Message;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.ConsoleMessage.MessageLevel;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebView.WebViewTransport;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import tmsdk.common.module.update.UpdateConfig;

/* compiled from: Unknown */
public class df extends WebChromeClient {
    private final dd ng;

    /* compiled from: Unknown */
    /* renamed from: com.google.android.gms.internal.df$7 */
    static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] qr = new int[MessageLevel.values().length];

        static {
            try {
                qr[MessageLevel.ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                qr[MessageLevel.WARNING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                qr[MessageLevel.LOG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                qr[MessageLevel.TIP.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                qr[MessageLevel.DEBUG.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public df(dd ddVar) {
        this.ng = ddVar;
    }

    private static void a(Builder builder, String str, final JsResult jsResult) {
        builder.setMessage(str).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                jsResult.confirm();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                jsResult.cancel();
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                jsResult.cancel();
            }
        }).create().show();
    }

    private static void a(Context context, Builder builder, String str, String str2, final JsPromptResult jsPromptResult) {
        View linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(1);
        View textView = new TextView(context);
        textView.setText(str);
        final View editText = new EditText(context);
        editText.setText(str2);
        linearLayout.addView(textView);
        linearLayout.addView(editText);
        builder.setView(linearLayout).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                jsPromptResult.confirm(editText.getText().toString());
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                jsPromptResult.cancel();
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                jsPromptResult.cancel();
            }
        }).create().show();
    }

    private static boolean a(Context context, String str, String str2, String str3, JsResult jsResult, JsPromptResult jsPromptResult, boolean z) {
        Builder builder = new Builder(context);
        builder.setTitle(str);
        if (z) {
            a(context, builder, str2, str3, jsPromptResult);
        } else {
            a(builder, str2, jsResult);
        }
        return true;
    }

    protected final void a(View view, int i, CustomViewCallback customViewCallback) {
        bo ba = this.ng.ba();
        if (ba != null) {
            ba.a(view, customViewCallback);
            ba.setRequestedOrientation(i);
            return;
        }
        da.w("Could not get ad overlay when showing custom view.");
        customViewCallback.onCustomViewHidden();
    }

    public final void onCloseWindow(WebView webView) {
        if (webView instanceof dd) {
            bo ba = ((dd) webView).ba();
            if (ba != null) {
                ba.close();
                return;
            } else {
                da.w("Tried to close an AdWebView not associated with an overlay.");
                return;
            }
        }
        da.w("Tried to close a WebView that wasn't an AdWebView.");
    }

    public final boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        String str = "JS: " + consoleMessage.message() + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")";
        switch (AnonymousClass7.qr[consoleMessage.messageLevel().ordinal()]) {
            case 1:
                da.t(str);
                break;
            case 2:
                da.w(str);
                break;
            case 3:
            case 4:
                da.u(str);
                break;
            case 5:
                da.s(str);
                break;
            default:
                da.u(str);
                break;
        }
        return super.onConsoleMessage(consoleMessage);
    }

    public final boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebViewTransport webViewTransport = (WebViewTransport) resultMsg.obj;
        WebView webView = new WebView(view.getContext());
        webView.setWebViewClient(this.ng.bb());
        webViewTransport.setWebView(webView);
        resultMsg.sendToTarget();
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, QuotaUpdater quotaUpdater) {
        long j = 5242880 - totalUsedQuota;
        if ((j > 0 ? 1 : null) == null) {
            quotaUpdater.updateQuota(currentQuota);
            return;
        }
        if (currentQuota == 0) {
            if ((estimatedSize > j ? 1 : null) == null) {
            }
            estimatedSize = 0;
        } else if (estimatedSize == 0) {
            estimatedSize = Math.min(Math.min(UpdateConfig.UPDATE_FLAG_CAMERA_SOFTWARE_LIST, j) + currentQuota, 1048576);
        } else {
            if ((estimatedSize > Math.min(1048576 - currentQuota, j) ? 1 : null) == null) {
                currentQuota += estimatedSize;
            }
            estimatedSize = currentQuota;
        }
        quotaUpdater.updateQuota(estimatedSize);
    }

    public final void onHideCustomView() {
        bo ba = this.ng.ba();
        if (ba != null) {
            ba.aq();
        } else {
            da.w("Could not get ad overlay when hiding custom view.");
        }
    }

    public final boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
        return a(webView.getContext(), url, message, null, result, null, false);
    }

    public final boolean onJsBeforeUnload(WebView webView, String url, String message, JsResult result) {
        return a(webView.getContext(), url, message, null, result, null, false);
    }

    public final boolean onJsConfirm(WebView webView, String url, String message, JsResult result) {
        return a(webView.getContext(), url, message, null, result, null, false);
    }

    public final boolean onJsPrompt(WebView webView, String url, String message, String defaultValue, JsPromptResult result) {
        return a(webView.getContext(), url, message, defaultValue, null, result, true);
    }

    public final void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, QuotaUpdater quotaUpdater) {
        long j = UpdateConfig.UPDATE_FLAG_CAMERA_SOFTWARE_LIST + spaceNeeded;
        if ((5242880 - totalUsedQuota >= j ? 1 : null) == null) {
            quotaUpdater.updateQuota(0);
        } else {
            quotaUpdater.updateQuota(j);
        }
    }

    public final void onShowCustomView(View view, CustomViewCallback customViewCallback) {
        a(view, -1, customViewCallback);
    }
}
