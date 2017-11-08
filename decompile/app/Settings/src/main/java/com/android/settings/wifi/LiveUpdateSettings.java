package com.android.settings.wifi;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.Arrays;
import java.util.List;

public class LiveUpdateSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    private static final String[] LIVE_UPDATE_STATUS_ARRAY = new String[]{"LINK_PLUS_AUTO_UPDATE_POLICY_ALWAYS", "LINK_PLUS_AUTO_UPDATE_POLICY_WIFI_ONLY", "LINK_PLUS_AUTO_UPDATE_POLICY_NEVER"};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230806;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private ListPreference mLinkPlusAutoUpdatePolicyPrefs;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230806);
        initPreferences();
    }

    private void initPreferences() {
        this.mLinkPlusAutoUpdatePolicyPrefs = (ListPreference) findPreference("link_plus_auto_update_policy");
        this.mLinkPlusAutoUpdatePolicyPrefs.setOnPreferenceChangeListener(null);
        if (Utils.isWifiOnly(getActivity())) {
            this.mLinkPlusAutoUpdatePolicyPrefs.setEntries(2131361966);
        }
        String stringPolicy = String.valueOf(Secure.getInt(getContentResolver(), "link_plus_auto_update_policy", 1));
        this.mLinkPlusAutoUpdatePolicyPrefs.setValue(stringPolicy);
        Utils.refreshListPreferenceSummary(this.mLinkPlusAutoUpdatePolicyPrefs, stringPolicy);
        this.mLinkPlusAutoUpdatePolicyPrefs.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("link_plus_auto_update_policy".equals(preference.getKey())) {
            try {
                String stringValue = (String) newValue;
                Secure.putInt(getContentResolver(), "link_plus_auto_update_policy", Integer.parseInt(stringValue));
                Utils.refreshListPreferenceSummary((ListPreference) preference, stringValue);
                ItemUseStat.getInstance().handleClickListPreference(getActivity(), (ListPreference) preference, LIVE_UPDATE_STATUS_ARRAY, stringValue);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
