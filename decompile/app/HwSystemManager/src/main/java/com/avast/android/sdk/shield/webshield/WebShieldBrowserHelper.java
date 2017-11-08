package com.avast.android.sdk.shield.webshield;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.avast.android.sdk.engine.obfuscated.ao;

/* compiled from: Unknown */
public class WebShieldBrowserHelper {
    private WebShieldBrowserHelper() {
    }

    private static void a(Context context, Intent intent) {
        if (a(context.getPackageManager(), intent)) {
            try {
                context.startActivity(intent);
            } catch (Throwable e) {
                ao.c("Can't start browser activity.", e);
            }
        }
    }

    private static boolean a(PackageManager packageManager, Intent intent) {
        return packageManager.resolveActivity(intent, 0) != null;
    }

    public static void blockBrowser(Context context, SupportedBrowser supportedBrowser, Uri uri) {
        if (uri == null) {
            uri = Uri.parse(WebShieldAccessibilityService.EMPTY_PAGE);
        }
        Intent a = supportedBrowser.a(uri);
        if (a(context.getPackageManager(), a)) {
            ao.a("Sending block intent to " + a.getComponent().flattenToString());
            context.startActivity(a);
            return;
        }
        ao.a("Can't find activity for browser block intent. Check for STOCK browser.");
        a = SupportedBrowser.STOCK.a(uri);
        if (a(context.getPackageManager(), a)) {
            ao.a("Sending block intent to " + a.getComponent().flattenToString());
            context.startActivity(a);
        }
    }

    public static void redirectBrowserToCorrectUrl(Context context, AccessibilitySupportedBrowser accessibilitySupportedBrowser, Uri uri) {
        ao.a("Redirecting browser to " + uri);
        Intent a = accessibilitySupportedBrowser.a(uri);
        ao.a("Sending display intent to " + a.getComponent().flattenToString());
        a(context, a);
    }

    public static void redirectBrowserToCorrectUrl(Context context, SupportedBrowser supportedBrowser, Uri uri) {
        ao.a("Redirecting browser to " + uri);
        Intent b = supportedBrowser.b(uri);
        ao.a("Sending redirect intent to " + b.getComponent().flattenToString());
        a(context, b);
    }
}
