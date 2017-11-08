package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.localepicker.LocaleListHelper;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class AdvancedSettings extends MoreSettings implements OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(2131625597);
            data.screenTitle = res.getString(2131625597);
            result.add(data);
            if (Utils.hasIntentActivity(context.getPackageManager(), "com.huawei.hicloud.action.OOBE_DATA_MIGRATE")) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131628553);
                data.screenTitle = res.getString(2131625597);
                result.add(data);
            }
            return result;
        }
    };
    private HwCustAdvancedSettings mHwCustAdvancedSettings;
    private Preference mParentCtrlPreference;
    private WildkidsEnabler mWildkidsEnabler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHwCustAdvancedSettings = (HwCustAdvancedSettings) HwCustUtils.createObj(HwCustAdvancedSettings.class, new Object[]{this});
        if (this.mHwCustAdvancedSettings != null) {
            this.mHwCustAdvancedSettings.loadCustHeader();
        }
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        if (this.mHwCustAdvancedSettings != null) {
            this.mHwCustAdvancedSettings.onResume();
        }
        if (this.mWildkidsEnabler != null) {
            this.mWildkidsEnabler.resume();
        }
        updateEngineEnable(getPrefContext());
    }

    public void onPause() {
        super.onPause();
        if (this.mHwCustAdvancedSettings != null) {
            this.mHwCustAdvancedSettings.onPause();
        }
        if (this.mWildkidsEnabler != null) {
            this.mWildkidsEnabler.pause();
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    private void updateEngineEnable(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if (!LocaleListHelper.isDefaultEngineNotSet(context)) {
            return;
        }
        if (SystemProperties.getBoolean("ro.talkback.chn_enable", true)) {
            LocaleListHelper.updateEngine(context);
        } else if (Utils.hasPackageInfo(packageManager, "com.svox.pico")) {
            Secure.putString(context.getContentResolver(), "tts_default_synth", "com.svox.pico");
        }
    }

    protected void updatePreferenceList() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(2131230730);
        if (!(findPreference("privacy_settings") == null || Utils.isOwnerUser())) {
            removePreference("privacy_settings");
        }
        Preference dataTransSettings = findPreference("data_transmission");
        if (dataTransSettings != null) {
            if (Utils.hasIntentActivity(getPackageManager(), dataTransSettings.getIntent())) {
                Intent intent = dataTransSettings.getIntent();
                Bundle bundle = new Bundle();
                bundle.putInt("entry_type", 2);
                intent.putExtras(bundle);
            } else {
                removePreference("data_transmission");
            }
        }
        removeSimpleModeIfNeeded();
        if (!(findPreference("print_settings") == null || getPackageManager().hasSystemFeature("android.software.print"))) {
            removePreference("print_settings");
        }
        Preference userImproveSettings = findPreference("user_experience_improve_plan");
        if (userImproveSettings != null) {
            boolean hasHwUE = Utils.hasPackageInfo(getPackageManager(), "com.huawei.bd");
            boolean hasHwLogUpload = Utils.hasPackageInfo(getPackageManager(), "com.huawei.logupload");
            if (hasHwUE || hasHwLogUpload) {
                userImproveSettings.getIntent().putExtra("portal_key", "setting_access");
            } else {
                removePreference("user_experience_improve_plan");
            }
        }
        if (ParentControl.isParentControlValid(getActivity())) {
            this.mParentCtrlPreference = findPreference("parent_control");
            if (this.mParentCtrlPreference != null) {
                int i;
                Preference preference = this.mParentCtrlPreference;
                if (ParentControl.isChildModeOn(getActivity())) {
                    i = 2131627698;
                } else {
                    i = 2131627699;
                }
                preference.setSummary(i);
            }
        } else {
            removePreference("parent_control");
        }
        Preference wildkidsPrefs = findPreference("wildkids_settings");
        if (wildkidsPrefs != null) {
            intent = wildkidsPrefs.getIntent();
            if (intent != null) {
                intent.setPackage("com.huawei.wildkids");
                wildkidsPrefs.setIntent(intent);
            }
            if (this.mWildkidsEnabler == null) {
                this.mWildkidsEnabler = new WildkidsEnabler(getActivity(), wildkidsPrefs);
            }
        }
        super.updatePreferenceList();
        if (findPreference("user_settings") != null) {
            boolean isUserSettingsAvailable;
            if (!UserManager.supportsMultipleUsers() || Utils.isMonkeyRunning()) {
                isUserSettingsAvailable = false;
            } else {
                isUserSettingsAvailable = !ParentControl.isChildModeOn(getActivity());
            }
            if (!isUserSettingsAvailable) {
                removePreference("user_settings");
            }
        }
        removeCategorySecurityIfNeed();
    }

    private void removeCategorySecurityIfNeed() {
        Preference categorySecurity = findPreference("category_security_user_mode");
        boolean isUserSettingsAvailable = (!UserManager.supportsMultipleUsers() || Utils.isMonkeyRunning()) ? false : !ParentControl.isChildModeOn(getActivity());
        boolean hasSimpleModeSupport = SystemProperties.getBoolean("ro.config.simple_mode", true);
        if (categorySecurity != null && !hasSimpleModeSupport && !isUserSettingsAvailable && !ParentControl.isParentControlValid(getActivity()) && !Utils.hasPackageInfo(getPackageManager(), "com.huawei.wildkids")) {
            removePreference("category_security_user_mode");
        }
    }

    private void removeSimpleModeIfNeeded() {
        if (findPreference("simple_mode") != null && !SystemProperties.getBoolean("ro.config.simple_mode", true)) {
            removePreference("simple_mode");
        }
    }
}
