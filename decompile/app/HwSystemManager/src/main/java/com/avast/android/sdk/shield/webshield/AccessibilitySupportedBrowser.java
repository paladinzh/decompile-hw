package com.avast.android.sdk.shield.webshield;

import android.content.Intent;
import android.net.Uri;
import com.huawei.permissionmanager.utils.ShareCfg;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public enum AccessibilitySupportedBrowser {
    CHROME("com.android.chrome", "com.google.android.apps.chrome.Main");
    
    private static final Map<String, AccessibilitySupportedBrowser> a = null;
    private final String b;
    private final String c;

    static {
        a = new HashMap();
        Iterator it = EnumSet.allOf(AccessibilitySupportedBrowser.class).iterator();
        while (it.hasNext()) {
            AccessibilitySupportedBrowser accessibilitySupportedBrowser = (AccessibilitySupportedBrowser) it.next();
            a.put(accessibilitySupportedBrowser.getId(), accessibilitySupportedBrowser);
        }
    }

    private AccessibilitySupportedBrowser(String str, String str2) {
        this.b = str;
        this.c = str2;
    }

    public static AccessibilitySupportedBrowser get(String str) {
        return (AccessibilitySupportedBrowser) a.get(str);
    }

    Intent a(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW", uri);
        intent.setClassName(getId(), a());
        intent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        return intent;
    }

    final String a() {
        return this.c;
    }

    public final String getId() {
        return this.b;
    }
}
