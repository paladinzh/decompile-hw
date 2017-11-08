package com.android.contacts.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.view.MenuItem;
import java.util.Locale;

public class HelpUtils {
    private static final String TAG = HelpUtils.class.getName();
    private static String sCachedVersionCode = null;

    private HelpUtils() {
    }

    public static boolean prepareHelpMenuItem(Context context, MenuItem helpMenuItem, int helpUrlResourceId) {
        return prepareHelpMenuItem(context, helpMenuItem, context.getResources().getString(helpUrlResourceId));
    }

    public static boolean prepareHelpMenuItem(Context context, MenuItem helpMenuItem, String helpUrlString) {
        if (TextUtils.isEmpty(helpUrlString)) {
            helpMenuItem.setVisible(false);
            return false;
        }
        Intent intent = new Intent("android.intent.action.VIEW", uriWithAddedParameters(context, Uri.parse(helpUrlString)));
        intent.setFlags(276824064);
        helpMenuItem.setIntent(intent);
        helpMenuItem.setShowAsAction(0);
        helpMenuItem.setVisible(true);
        return true;
    }

    private static Uri uriWithAddedParameters(Context context, Uri baseUri) {
        Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter("hl", Locale.getDefault().toString());
        if (sCachedVersionCode == null) {
            try {
                sCachedVersionCode = Integer.toString(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
                builder.appendQueryParameter("version", sCachedVersionCode);
            } catch (NameNotFoundException e) {
                HwLog.wtf(TAG, "Invalid package name for context", e);
            }
        } else {
            builder.appendQueryParameter("version", sCachedVersionCode);
        }
        return builder.build();
    }
}
