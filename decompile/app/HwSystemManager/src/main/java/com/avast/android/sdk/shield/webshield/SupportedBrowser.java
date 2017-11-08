package com.avast.android.sdk.shield.webshield;

import android.content.Intent;
import android.net.Uri;
import com.avast.android.sdk.engine.UrlSource;
import com.avast.android.sdk.engine.obfuscated.bc;
import com.huawei.permissionmanager.utils.ShareCfg;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public enum SupportedBrowser {
    STOCK("com.android.browser", bc.a, "com.android.browser.BrowserActivity", bc.b, bc.b[3], bc.b[1], bc.b[4], 2, UrlSource.STOCK),
    STOCK_JB("com.google.android.browser", bc.a, "com.android.browser.BrowserActivity", bc.b, bc.b[3], bc.b[1], bc.b[4], 2, UrlSource.STOCK_JB),
    CHROME("com.android.chrome", Uri.parse("content://com.android.chrome.browser/bookmarks"), "com.google.android.apps.chrome.Main", null, "date", "url", "bookmark", 1, UrlSource.CHROME),
    CHROME_2("com.android.chrome", Uri.parse("content://com.android.chrome.browser/history"), "com.google.android.apps.chrome.Main", null, "date", "url", "bookmark", 1, UrlSource.CHROME),
    DOLPHIN("com.dolphin.browser", Uri.parse("content://com.dolphin.browser.bookmarks/bookmarks"), "com.dolphin.browser.BrowserActivity", null, "visited_date", "url", "type", 1, UrlSource.DOLPHIN_MINI),
    DOLPHIN_HD("mobi.mgeek.TunnyBrowser", null, null, null, null, null, null, 1, UrlSource.DOLPHIN),
    SILK("com.amazon.cloud9", Uri.parse("content://com.amazon.cloud9/pages"), "com.amazon.cloud9.BrowserActivity", null, "visited_on", "url", "bookmarked", 2, UrlSource.SILK),
    BOAT("com.boatbrowser.free", Uri.parse("content://com.boatbrowser.free/bookmarks"), "com.boatbrowser.free.BrowserActivity", null, "date", "url", "bookmark", 1, UrlSource.BOAT),
    BOAT_MINI("com.boatgo.browser", Uri.parse("content://boatbrowser/bookmarks"), "com.boatgo.browser.BrowserActivity", null, "date", "url", "bookmark", 1, UrlSource.BOAT_MINI),
    SBROWSER("com.sec.android.app.sbrowser", Uri.parse("content://com.sec.android.app.sbrowser.browser/history"), "com.sec.android.app.sbrowser.SBrowserMainActivity", null, "date", "url", "bookmark", 1, UrlSource.SBROWSER),
    BOAT_TABLET("com.boatbrowser.tablet", Uri.parse("content://com.boatbrowser.tablet/bookmarks"), "com.boatbrowser.tablet.BrowserActivity", null, "date", "url", "bookmark", 1, UrlSource.BOAT);
    
    private static final Map<String, SupportedBrowser> a = null;
    private static final Object l = null;
    private final String b;
    private final Uri c;
    private final String d;
    private final String[] e;
    private final String f;
    private final String g;
    private final String h;
    private int i;
    private final int j;
    private final UrlSource k;

    static {
        a = new HashMap();
        Iterator it = EnumSet.allOf(SupportedBrowser.class).iterator();
        while (it.hasNext()) {
            SupportedBrowser supportedBrowser = (SupportedBrowser) it.next();
            a.put(supportedBrowser.a(), supportedBrowser);
        }
        l = new Object();
    }

    private SupportedBrowser(String str, Uri uri, String str2, String[] strArr, String str3, String str4, String str5, int i, UrlSource urlSource) {
        int i2 = 0;
        this.b = str;
        this.c = uri;
        this.d = str2;
        if (strArr == null) {
            this.e = new String[3];
            this.e[0] = str3;
            this.e[1] = str4;
            this.e[2] = str5;
            this.i = 0;
            while (i2 < this.e.length) {
                if (this.e[i2] == null) {
                    this.i++;
                }
                i2++;
            }
        } else {
            this.e = strArr;
        }
        this.f = str3;
        this.g = str4;
        this.h = str5;
        this.j = i;
        this.k = urlSource;
    }

    Intent a(Uri uri) {
        Intent b = b(null);
        synchronized (l) {
            if (equals(DOLPHIN) || equals(CHROME) || equals(CHROME_2) || equals(SBROWSER)) {
                b.setData(Uri.parse(WebShieldAccessibilityService.EMPTY_PAGE));
            } else {
                b.setDataAndType(uri, "text/html");
            }
        }
        return b;
    }

    final String a() {
        return this.b;
    }

    Intent b(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.setClassName(a(), c());
        intent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        if (equals(STOCK_JB)) {
            intent.putExtra("com.android.browser.application_id", a());
        }
        if (equals(DOLPHIN)) {
            intent.putExtra("com_dolphin_browser_self", true);
        }
        if (equals(BOAT) || equals(BOAT_MINI)) {
            intent.putExtra("com.android.browser.application_id", BOAT.b);
        }
        if (equals(BOAT_TABLET)) {
            intent.putExtra("com.android.browser.application_id", BOAT_TABLET.b);
        }
        if (equals(SBROWSER)) {
            intent.putExtra("com.android.browser.application_id", SBROWSER.b);
        }
        return intent;
    }

    final Uri b() {
        return this.c;
    }

    final String c() {
        return this.d;
    }

    final String[] d() {
        int i = 0;
        if (this.i == 0) {
            return this.e;
        }
        String[] strArr = new String[(this.e.length - this.i)];
        int i2 = 0;
        while (i < this.e.length) {
            if (this.e[i] != null) {
                int i3 = i2 + 1;
                strArr[i2] = this.e[i];
                i2 = i3;
            }
            i++;
        }
        return strArr;
    }

    final String e() {
        return this.f;
    }

    final String f() {
        return this.g;
    }

    final String g() {
        return this.h;
    }

    final UrlSource h() {
        return this.k;
    }
}
