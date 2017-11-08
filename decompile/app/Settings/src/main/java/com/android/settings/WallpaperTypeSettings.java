package com.android.settings;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.search.Indexable;
import java.util.List;

public class WallpaperTypeSettings extends SettingsPreferenceFragment implements Indexable {
    protected int getMetricsCategory() {
        return 101;
    }

    protected int getHelpResource() {
        return 2131626526;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230931);
        populateWallpaperTypes();
    }

    private void populateWallpaperTypes() {
        Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> rList = pm.queryIntentActivities(intent, 65536);
        PreferenceScreen parent = getPreferenceScreen();
        parent.setOrderingAsAdded(false);
        for (ResolveInfo info : rList) {
            Preference pref = new Preference(getPrefContext());
            pref.setLayoutResource(2130968996);
            Intent prefIntent = new Intent(intent);
            prefIntent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
            pref.setIntent(prefIntent);
            CharSequence label = info.loadLabel(pm);
            if (label == null) {
                label = info.activityInfo.packageName;
            }
            pref.setTitle(label);
            pref.setIcon(info.loadIcon(pm));
            parent.addPreference(pref);
        }
    }
}
