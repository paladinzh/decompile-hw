package com.avast.android.sdk.shield.appinstallshield;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.avast.android.sdk.internal.c;
import com.avast.android.sdk.internal.c.a;

/* compiled from: Unknown */
public abstract class AppInstallReceiver extends BroadcastReceiver {
    public static final String EXTRA_SCAN_APP = "avast.sdk.shield.scanApp";

    public abstract Bundle onAppInstall(Context context, String str);

    public abstract Bundle onAppUpdate(Context context, String str);

    public final void onReceive(Context context, Intent intent) {
        Uri uri = null;
        if (intent != null) {
            uri = intent.getData();
        }
        if (uri != null) {
            Bundle onAppInstall;
            boolean equals = "android.intent.action.PACKAGE_ADDED".equals(intent.getAction());
            boolean equals2 = "android.intent.action.PACKAGE_REPLACED".equals(intent.getAction());
            String substring = uri.toString().substring(intent.getDataString().indexOf(":") + 1);
            if (equals && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                onAppInstall = onAppInstall(context, substring);
            } else if (equals2) {
                onAppInstall = onAppUpdate(context, substring);
            } else {
                return;
            }
            Intent intent2 = new Intent();
            intent2.setComponent(c.a(a.APP_INSTALL_SERVICE));
            intent2.setPackage(context.getPackageName());
            if (onAppInstall != null && onAppInstall.getBoolean(EXTRA_SCAN_APP, true)) {
                intent2.putExtras(onAppInstall);
                intent2.putExtras(intent);
                intent2.setData(intent.getData());
                context.startService(intent2);
            }
        }
    }
}
