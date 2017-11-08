package com.avast.android.sdk.shield.webshield;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.webkit.WebView;
import com.avast.android.sdk.engine.UrlCheckResultStructure;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.bd;
import com.avast.android.sdk.engine.obfuscated.be;
import com.avast.android.sdk.engine.obfuscated.bf;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@TargetApi(18)
/* compiled from: Unknown */
public abstract class WebShieldAccessibilityService extends AccessibilityService {
    public static final String EMPTY_PAGE = "about:blank";
    private be a;
    private b b;
    private a c;
    private boolean d = false;
    private String e = "";
    private String f = "";
    private AccessibilityNodeInfo g = null;
    private AccessibilityNodeInfo h = null;
    private AccessibilityNodeInfo i = null;
    private bd j;
    private Map<String, bf> k = new HashMap();
    private LinkedList<String> l = new LinkedList();
    private boolean m = false;
    private boolean n = false;

    /* compiled from: Unknown */
    private final class a extends ContentObserver {
        final /* synthetic */ WebShieldAccessibilityService a;
        private final Uri b = Uri.parse("content://com.android.chrome.browser/history");
        private boolean c = false;

        public a(WebShieldAccessibilityService webShieldAccessibilityService) {
            this.a = webShieldAccessibilityService;
            super(new Handler(webShieldAccessibilityService.getMainLooper()));
        }

        public synchronized void a() {
            if (!this.c) {
                this.a.getContentResolver().registerContentObserver(this.b, true, this);
                this.c = true;
            }
        }

        public synchronized void b() {
            if (this.c) {
                this.a.getContentResolver().unregisterContentObserver(this);
                this.c = false;
            }
        }

        public void onChange(boolean z) {
            onChange(z, null);
        }

        public void onChange(boolean z, Uri uri) {
            ao.a("History changed");
            this.a.d = true;
        }
    }

    /* compiled from: Unknown */
    private final class b implements com.avast.android.sdk.engine.obfuscated.be.a {
        final /* synthetic */ WebShieldAccessibilityService a;

        private b(WebShieldAccessibilityService webShieldAccessibilityService) {
            this.a = webShieldAccessibilityService;
        }

        public final UrlAction a(String str) {
            return this.a.onNewUrlDetected(str, AccessibilitySupportedBrowser.CHROME);
        }

        public final void a(String str, List<UrlCheckResultStructure> list) {
            if (list != null) {
                ScannedUrlAction onUrlScanResult = this.a.onUrlScanResult(str, list, AccessibilitySupportedBrowser.CHROME);
                ao.a("Action to take = " + onUrlScanResult);
                switch (a.a[onUrlScanResult.ordinal()]) {
                    case 1:
                        return;
                    case 2:
                    case 3:
                        this.a.k.put(str, new bf(str, onUrlScanResult, list));
                        break;
                }
            }
        }

        public void b(String str) {
            this.a.k.put(str, new bf(str, ScannedUrlAction.BLOCK, Collections.emptyList()));
        }
    }

    private void a(AccessibilityEvent accessibilityEvent) {
        String str = null;
        if (!TextUtils.isEmpty(accessibilityEvent.getPackageName()) && !TextUtils.isEmpty(accessibilityEvent.getClassName())) {
            Object charSequence = accessibilityEvent.getPackageName().toString();
            String charSequence2 = accessibilityEvent.getClassName().toString();
            ao.a(accessibilityEvent.getEventType() + " on " + charSequence2 + " in " + charSequence);
            if (!TextUtils.isEmpty(charSequence)) {
                bf bfVar;
                if (this.m && "com.android.systemui".equals(charSequence) && b()) {
                    bfVar = (bf) this.k.get(this.f);
                    this.k.clear();
                    this.f = "";
                    a(bfVar);
                    return;
                }
                AccessibilitySupportedBrowser accessibilitySupportedBrowser = AccessibilitySupportedBrowser.get(charSequence);
                if (accessibilitySupportedBrowser != null) {
                    switch (accessibilityEvent.getEventType()) {
                        case 32:
                        case 2048:
                            AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
                            if (rootInActiveWindow != null) {
                                AccessibilityNodeInfo a;
                                Object obj;
                                if (this.g == null) {
                                    a = this.j.a(accessibilitySupportedBrowser, rootInActiveWindow);
                                    this.g = a;
                                } else if (!this.g.refresh()) {
                                    this.g.recycle();
                                    a = this.j.a(accessibilitySupportedBrowser, rootInActiveWindow);
                                    this.g = a;
                                }
                                if (this.h == null) {
                                    a = this.j.b(accessibilitySupportedBrowser, rootInActiveWindow);
                                    this.h = a;
                                } else if (!this.h.refresh()) {
                                    this.h.recycle();
                                    a = this.j.b(accessibilitySupportedBrowser, rootInActiveWindow);
                                    this.h = a;
                                }
                                if (this.i == null) {
                                    a = this.j.c(accessibilitySupportedBrowser, rootInActiveWindow);
                                    this.i = a;
                                } else if (!this.i.refresh()) {
                                    this.i.recycle();
                                    a = this.j.c(accessibilitySupportedBrowser, rootInActiveWindow);
                                    this.i = a;
                                }
                                rootInActiveWindow.recycle();
                                if (this.h == null) {
                                    obj = null;
                                } else {
                                    String charSequence3 = this.h.getContentDescription() == null ? null : this.h.getContentDescription().toString();
                                }
                                ao.a("webViewContentDescription = " + obj);
                                if (this.g != null) {
                                    if (!TextUtils.isEmpty(this.g.getText())) {
                                        String charSequence4 = this.g.getText().toString();
                                        if (charSequence4.matches(".*.\\...*")) {
                                            str = charSequence4;
                                        }
                                    }
                                    if (WebView.class.getName().equals(charSequence2) || this.d) {
                                        this.d = false;
                                        if (!(str == null || this.e.equals(str))) {
                                            this.e = str;
                                            ao.a(accessibilitySupportedBrowser + " went to " + str);
                                            this.a.a(str);
                                            if (!TextUtils.isEmpty(obj)) {
                                                this.l.clear();
                                            }
                                        }
                                    }
                                    if (str != null && this.k.containsKey(str)) {
                                        bfVar = (bf) this.k.get(str);
                                        if (this.i != null) {
                                            ao.a("Clicking back to block " + str);
                                            this.i.performAction(16);
                                            a(bfVar);
                                        } else if (!TextUtils.isEmpty(obj) && a()) {
                                            this.f = str;
                                            ao.a("Showing recents, should close window with content description \"" + obj + "\" or older");
                                        }
                                    }
                                    if (!TextUtils.isEmpty(obj)) {
                                        this.l.remove(obj);
                                        this.l.addLast(obj);
                                    }
                                    return;
                                }
                                return;
                            }
                            return;
                        default:
                            return;
                    }
                }
            }
        }
    }

    private void a(bf bfVar) {
        if (bfVar.b()) {
            String c = bfVar.c();
            if (TextUtils.isEmpty(c)) {
                c = EMPTY_PAGE;
            }
            WebShieldBrowserHelper.redirectBrowserToCorrectUrl((Context) this, AccessibilitySupportedBrowser.CHROME, Uri.parse(c));
            onUrlAutocorrected(bfVar.a(), c, AccessibilitySupportedBrowser.CHROME);
            return;
        }
        onUrlBlocked(bfVar.a(), AccessibilitySupportedBrowser.CHROME);
    }

    private synchronized boolean a() {
        if (this.n) {
            return false;
        }
        this.m = true;
        this.n = true;
        performGlobalAction(3);
        return true;
    }

    private boolean b() {
        boolean z;
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            z = false;
        } else {
            z = this.j.a(rootInActiveWindow, this.l);
            rootInActiveWindow.recycle();
        }
        if (z) {
            this.l.clear();
            this.m = false;
            this.n = false;
            this.e = "";
        }
        return z;
    }

    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (VERSION.SDK_INT >= 18 && accessibilityEvent != null) {
            a(accessibilityEvent);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.c != null) {
            this.c.b();
            this.c = null;
        }
        if (this.a != null) {
            this.a.a();
            this.a = null;
        }
    }

    public void onInterrupt() {
    }

    protected abstract UrlAction onNewUrlDetected(String str, AccessibilitySupportedBrowser accessibilitySupportedBrowser);

    protected void onServiceConnected() {
        super.onServiceConnected();
        if (VERSION.SDK_INT >= 18) {
            AccessibilityServiceInfo accessibilityServiceInfo = new AccessibilityServiceInfo();
            accessibilityServiceInfo.eventTypes = 2080;
            accessibilityServiceInfo.feedbackType = 16;
            accessibilityServiceInfo.flags = 16;
            accessibilityServiceInfo.notificationTimeout = 100;
            setServiceInfo(accessibilityServiceInfo);
            if (this.b == null) {
                this.b = new b();
            }
            if (this.a == null) {
                this.a = new be(this, this.b);
                this.a.start();
            }
            if (this.j == null) {
                this.j = new bd();
            }
            if (this.c == null) {
                this.c = new a(this);
            }
            this.c.a();
        }
    }

    protected abstract void onUrlAutocorrected(String str, String str2, AccessibilitySupportedBrowser accessibilitySupportedBrowser);

    protected abstract void onUrlBlocked(String str, AccessibilitySupportedBrowser accessibilitySupportedBrowser);

    protected abstract ScannedUrlAction onUrlScanResult(String str, List<UrlCheckResultStructure> list, AccessibilitySupportedBrowser accessibilitySupportedBrowser);
}
