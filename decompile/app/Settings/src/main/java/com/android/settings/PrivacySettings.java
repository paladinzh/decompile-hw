package com.android.settings;

import android.app.Activity;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupManager.Stub;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PrivacySettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new PrivacySearchIndexProvider();
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private Intent configIntent;
    private CustomSwitchPreference mAutoRestore;
    private PreferenceScreen mBackup;
    private IBackupManager mBackupManager;
    private PreferenceCategory mCategory;
    private PreferenceScreen mConfigure;
    private boolean mEnabled;
    private RestrictedPreference mFactoryReSet;
    private PreferenceScreen mManageData;
    private OnPreferenceChangeListener preferenceChangeListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean z = true;
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(PrivacySettings.this.getActivity(), preference, newValue);
            if (!(preference instanceof SwitchPreference)) {
                return true;
            }
            boolean nextValue = ((Boolean) newValue).booleanValue();
            boolean result = false;
            if (preference == PrivacySettings.this.mAutoRestore) {
                try {
                    PrivacySettings.this.mBackupManager.setAutoRestore(nextValue);
                    result = true;
                } catch (RemoteException e) {
                    CustomSwitchPreference -get0 = PrivacySettings.this.mAutoRestore;
                    if (nextValue) {
                        z = false;
                    }
                    -get0.setChecked(z);
                }
            }
            return result;
        }
    };

    private static class PrivacySearchIndexProvider extends BaseSearchIndexProvider {
        boolean mIsPrimary;

        public PrivacySearchIndexProvider() {
            boolean z = false;
            if (UserHandle.myUserId() == 0) {
                z = true;
            }
            this.mIsPrimary = z;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (!this.mIsPrimary) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230844;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> nonVisibleKeys = new ArrayList();
            PrivacySettings.getNonVisibleKeys(context, nonVisibleKeys);
            return nonVisibleKeys;
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                IBackupManager backupManager = Stub.asInterface(ServiceManager.getService("backup"));
                try {
                    if (backupManager.isBackupEnabled()) {
                        String configSummary = backupManager.getDestinationString(backupManager.getCurrentTransport());
                        if (configSummary != null) {
                            this.mSummaryLoader.setSummary(this, configSummary);
                            return;
                        } else {
                            this.mSummaryLoader.setSummary(this, this.mContext.getString(2131626157));
                            return;
                        }
                    }
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627104));
                } catch (RemoteException e) {
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 81;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mEnabled = UserManager.get(getActivity()).isAdminUser();
        if (!this.mEnabled) {
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        getActivity().setTitle(2131626146);
        if (this.mEnabled) {
            updatePreferences();
            updateToggles();
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        return super.onPreferenceTreeClick(preference);
    }

    private void updateToggles() {
        boolean manageEnabled;
        boolean z = true;
        ContentResolver res = getContentResolver();
        boolean z2 = false;
        this.configIntent = null;
        String str = null;
        Intent intent = null;
        CharSequence charSequence = null;
        try {
            int i;
            z2 = this.mBackupManager.isBackupEnabled();
            String transport = this.mBackupManager.getCurrentTransport();
            this.configIntent = validatedActivityIntent(this.mBackupManager.getConfigurationIntent(transport), "config");
            str = this.mBackupManager.getDestinationString(transport);
            intent = validatedActivityIntent(this.mBackupManager.getDataManagementIntent(transport), "management");
            charSequence = this.mBackupManager.getDataManagementLabel(transport);
            PreferenceScreen preferenceScreen = this.mBackup;
            if (z2) {
                i = 2131625876;
            } else {
                i = 2131625877;
            }
            preferenceScreen.setSummary(i);
        } catch (RemoteException e) {
            this.mBackup.setEnabled(false);
        }
        if (getPreferenceScreen().findPreference("backup_category") == null) {
            z2 = false;
            this.mBackup.setEnabled(false);
        }
        CustomSwitchPreference customSwitchPreference = this.mAutoRestore;
        if (Secure.getInt(res, "backup_auto_restore", 1) != 1) {
            z = false;
        }
        customSwitchPreference.setChecked(z);
        this.mAutoRestore.setEnabled(z2);
        if (this.configIntent == null) {
            this.mCategory = (PreferenceCategory) getPreferenceScreen().findPreference("backup_category");
            if (this.mCategory != null) {
                this.mCategory.removePreference(this.mConfigure);
            }
        } else {
            this.mConfigure.setEnabled(z2);
            this.mConfigure.setIntent(this.configIntent);
            setConfigureSummary(str);
        }
        if (intent != null) {
            manageEnabled = z2;
        } else {
            manageEnabled = false;
        }
        if (manageEnabled) {
            this.mManageData.setIntent(intent);
            if (charSequence != null) {
                this.mManageData.setTitle(charSequence);
                return;
            }
            return;
        }
        this.mCategory = (PreferenceCategory) getPreferenceScreen().findPreference("backup_category");
        if (this.mCategory != null) {
            this.mCategory.removePreference(this.mManageData);
        }
    }

    private Intent validatedActivityIntent(Intent intent, String logLabel) {
        if (intent == null) {
            return intent;
        }
        List<ResolveInfo> resolved = getPackageManager().queryIntentActivities(intent, 0);
        if (resolved != null && !resolved.isEmpty()) {
            return intent;
        }
        Log.e("PrivacySettings", "Backup " + logLabel + " intent " + null + " fails to resolve; ignoring");
        return null;
    }

    private void setConfigureSummary(String summary) {
        if (summary != null) {
            this.mConfigure.setSummary((CharSequence) summary);
        } else {
            this.mConfigure.setSummary(2131626157);
        }
    }

    protected int getHelpResource() {
        return 2131626546;
    }

    private static void getNonVisibleKeys(Context context, Collection<String> nonVisibleKeys) {
        boolean isServiceActive = false;
        try {
            isServiceActive = Stub.asInterface(ServiceManager.getService("backup")).isBackupServiceActive(UserHandle.myUserId());
        } catch (RemoteException e) {
            Log.w("PrivacySettings", "Failed querying backup manager service activity status. Assuming it is inactive.");
        }
        boolean vendorSpecific = context.getPackageManager().resolveContentProvider("com.google.settings", 0) == null;
        if (vendorSpecific || r5) {
            nonVisibleKeys.add("backup_inactive");
        }
        if (vendorSpecific || !r5 || SettingsExtUtils.isGoogleBackupDisabled(context)) {
            nonVisibleKeys.add("backup_data");
            nonVisibleKeys.add("auto_restore");
            nonVisibleKeys.add("configure_account");
            nonVisibleKeys.add("backup_category");
        }
        boolean disableFactoryReset = RestrictedLockUtils.hasBaseUserRestriction(context, "no_factory_reset", UserHandle.myUserId());
        boolean disableNetworkReset = RestrictedLockUtils.hasBaseUserRestriction(context, "no_network_reset", UserHandle.myUserId());
        boolean disableSettingsReset = UserHandle.myUserId() != 0;
        if (disableFactoryReset) {
            nonVisibleKeys.add("factory_data_reset");
        }
        if (disableNetworkReset || !MdppUtils.isCcModeDisabled()) {
            nonVisibleKeys.add("network_reset");
        }
        if (disableSettingsReset || !MdppUtils.isCcModeDisabled()) {
            nonVisibleKeys.add("factory_reset_settings");
        }
        if (disableFactoryReset && disableNetworkReset && disableSettingsReset) {
            nonVisibleKeys.add("personal_data_category");
        }
        nonVisibleKeys.add("huawei_backup_category");
        nonVisibleKeys.add("huawei_backup_data");
    }

    private void updateFactoryresetPreference(PreferenceScreen screen) {
        int disableFactoryValue = System.getInt(getContentResolver(), "disable_factory_data_reset", 1);
        this.mFactoryReSet = (RestrictedPreference) screen.findPreference("factory_data_reset");
        if (disableFactoryValue == 0) {
            this.mFactoryReSet.setEnabled(false);
        } else {
            this.mFactoryReSet.setEnabled(true);
        }
    }

    private void updatePreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            screen.removeAll();
        }
        addPreferencesFromResource(2131230844);
        screen = getPreferenceScreen();
        this.mBackupManager = Stub.asInterface(ServiceManager.getService("backup"));
        updateFactoryresetPreference(screen);
        this.mBackup = (PreferenceScreen) screen.findPreference("backup_data");
        this.mAutoRestore = (CustomSwitchPreference) screen.findPreference("auto_restore");
        this.mAutoRestore.setOnPreferenceChangeListener(this.preferenceChangeListener);
        this.mConfigure = (PreferenceScreen) screen.findPreference("configure_account");
        this.mManageData = (PreferenceScreen) screen.findPreference("data_management");
        LinkedList<String> keysToRemove = new LinkedList();
        getNonVisibleKeys(getActivity(), keysToRemove);
        PreferenceCategory backupCategory = (PreferenceCategory) findPreference("backup_category");
        PreferenceCategory resetCategory = (PreferenceCategory) findPreference("personal_data_category");
        List<String> backupCategoryKeys = getCategoryPrefKeys(backupCategory);
        List<String> resetCategoryKeys = getCategoryPrefKeys(resetCategory);
        for (String key : keysToRemove) {
            Preference pref = screen.findPreference(key);
            if (!(pref == null || "huawei_backup_category".equals(key) || screen.removePreference(pref))) {
                if (backupCategoryKeys.contains(key)) {
                    backupCategory.removePreference(pref);
                } else if (resetCategoryKeys.contains(key)) {
                    resetCategory.removePreference(pref);
                }
            }
        }
        Intent intent = Utils.getHuaweiBackupIntent(getActivity());
        if (intent == null) {
            removePreference("huawei_backup_category");
            return;
        }
        PreferenceCategory huaweiBackUpCategory = (PreferenceCategory) screen.findPreference("huawei_backup_category");
        if (huaweiBackUpCategory != null) {
            Preference huaweiBackupPref = huaweiBackUpCategory.findPreference("huawei_backup_data");
            if (huaweiBackupPref != null) {
                if (Utils.isWifiOnly(getActivity())) {
                    huaweiBackupPref.setSummary(2131628746);
                }
                huaweiBackupPref.setIntent(intent);
            }
        }
    }

    private List<String> getCategoryPrefKeys(PreferenceCategory category) {
        List<String> keys = new LinkedList();
        int num = category.getPreferenceCount();
        for (int index = 0; index < num; index++) {
            Preference pref = category.getPreference(index);
            if (pref != null) {
                keys.add(pref.getKey());
            }
        }
        return keys;
    }
}
