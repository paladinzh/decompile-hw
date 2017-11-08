package com.google.android.gms.internal;

import android.content.Intent;
import android.net.Uri;

/* compiled from: Unknown */
public class ek {
    private static final Uri Cb = Uri.parse("http://plus.google.com/");
    private static final Uri Cc = Cb.buildUpon().appendPath("circles").appendPath("find").build();

    public static Intent af(String str) {
        Uri fromParts = Uri.fromParts("package", str, null);
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(fromParts);
        return intent;
    }

    private static Uri ag(String str) {
        return Uri.parse("market://details").buildUpon().appendQueryParameter("id", str).build();
    }

    public static Intent ah(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(ag(str));
        intent.setPackage("com.android.vending");
        intent.addFlags(524288);
        return intent;
    }

    public static Intent ai(String str) {
        Uri parse = Uri.parse("bazaar://search?q=pname:" + str);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(parse);
        intent.setFlags(524288);
        return intent;
    }

    public static Intent eh() {
        return new Intent("android.settings.DATE_SETTINGS");
    }
}
