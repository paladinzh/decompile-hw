package com.android.settingslib;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.Theme;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import java.net.URISyntaxException;
import java.util.Locale;

public class HelpUtils {
    private static final String TAG = HelpUtils.class.getSimpleName();
    private static String sCachedVersionCode = null;

    private HelpUtils() {
    }

    public static boolean prepareHelpMenuItem(Activity activity, Menu menu, String helpUri, String backupContext) {
        return prepareHelpMenuItem(activity, menu.add(0, 101, 0, R$string.help_feedback_label), helpUri, backupContext);
    }

    public static boolean prepareHelpMenuItem(Activity activity, Menu menu, int helpUriResource, String backupContext) {
        return prepareHelpMenuItem(activity, menu.add(0, 101, 0, R$string.help_feedback_label), activity.getString(helpUriResource), backupContext);
    }

    public static boolean prepareHelpMenuItem(final Activity activity, MenuItem helpMenuItem, String helpUriString, String backupContext) {
        if (Global.getInt(activity.getContentResolver(), "device_provisioned", 0) == 0) {
            return false;
        }
        if (TextUtils.isEmpty(helpUriString)) {
            helpMenuItem.setVisible(false);
            return false;
        }
        final Intent intent = getHelpIntent(activity, helpUriString, backupContext);
        if (intent != null) {
            helpMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    try {
                        activity.startActivityForResult(intent, 0);
                    } catch (ActivityNotFoundException e) {
                        Log.e(HelpUtils.TAG, "No activity found for intent: " + intent);
                    }
                    return true;
                }
            });
            helpMenuItem.setShowAsAction(0);
            helpMenuItem.setVisible(true);
            return true;
        }
        helpMenuItem.setVisible(false);
        return false;
    }

    public static Intent getHelpIntent(Context context, String helpUriString, String backupContext) {
        if (Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 0) {
            return null;
        }
        Intent intent;
        try {
            intent = Intent.parseUri(helpUriString, 3);
            addIntentParameters(context, intent, backupContext);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                return intent;
            }
            if (intent.hasExtra("EXTRA_BACKUP_URI")) {
                return getHelpIntent(context, intent.getStringExtra("EXTRA_BACKUP_URI"), backupContext);
            }
            return null;
        } catch (URISyntaxException e) {
            intent = new Intent("android.intent.action.VIEW", uriWithAddedParameters(context, Uri.parse(helpUriString)));
            intent.setFlags(276824064);
            return intent;
        }
    }

    private static void addIntentParameters(Context context, Intent intent, String backupContext) {
        if (!intent.hasExtra("EXTRA_CONTEXT")) {
            intent.putExtra("EXTRA_CONTEXT", backupContext);
        }
        intent.putExtra("EXTRA_THEME", 1);
        Theme theme = context.getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(16843827, typedValue, true);
        intent.putExtra("EXTRA_PRIMARY_COLOR", context.getColor(typedValue.resourceId));
    }

    public static Uri uriWithAddedParameters(Context context, Uri baseUri) {
        Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter("hl", Locale.getDefault().toString());
        if (sCachedVersionCode == null) {
            try {
                sCachedVersionCode = Integer.toString(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
                builder.appendQueryParameter("version", sCachedVersionCode);
            } catch (NameNotFoundException e) {
                Log.wtf(TAG, "Invalid package name for context", e);
            }
        } else {
            builder.appendQueryParameter("version", sCachedVersionCode);
        }
        return builder.build();
    }
}
