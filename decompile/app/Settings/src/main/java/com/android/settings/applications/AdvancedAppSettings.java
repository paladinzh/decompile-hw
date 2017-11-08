package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.PermissionsSummaryHelper.PermissionsResultCallback;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedAppSettings extends SettingsPreferenceFragment implements Callbacks, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230729;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return Utils.getNonIndexable(2131230729, context);
        }
    };
    private Preference mAppDomainURLsPreference;
    private Preference mAppPermsPreference;
    private Preference mHighPowerPreference;
    private final PermissionsResultCallback mPermissionCallback = new PermissionsResultCallback() {
    };
    private Session mSession;
    private Preference mSystemAlertWindowPreference;
    private Preference mWriteSettingsPreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230729);
        Preference permissions = getPreferenceScreen().findPreference("manage_perms");
        Intent intentPermissions = new Intent("android.intent.action.MANAGE_PERMISSIONS");
        if (Utils.hasIntentActivity(getPackageManager(), intentPermissions)) {
            permissions.setIntent(intentPermissions);
        } else {
            Log.e("AdvancedAppSettings", "No has IntentActivity : Intent.ACTION_MANAGE_PERMISSIONS");
        }
        this.mSession = ApplicationsState.getInstance(getActivity().getApplication()).newSession(this);
        this.mAppPermsPreference = findPreference("manage_perms");
        this.mAppDomainURLsPreference = findPreference("domain_urls");
        this.mHighPowerPreference = findPreference("high_power_apps");
        this.mSystemAlertWindowPreference = findPreference("system_alert_window");
        this.mWriteSettingsPreference = findPreference("write_settings_apps");
    }

    public void onDestroy() {
        if (this.mSession != null) {
            this.mSession.release();
        }
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 130;
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onPackageListChanged() {
    }

    public void onRebuildComplete(ArrayList<AppEntry> arrayList) {
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
    }
}
