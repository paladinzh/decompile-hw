package com.android.settings.wifi;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.wifi.cmcc.Features;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedWifiSettings extends AdvancedWifiSettingsHwBase implements OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230934;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            if (SettingsExtUtils.isGlobalVersion()) {
                keys.add("wifi_cloud_security_check");
                keys.add("wifi_settings_category");
            }
            return keys;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String screenTitle = res.getString(2131625020);
            if (Features.shouldDisplayConnectionSetting()) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627525);
                data.screenTitle = screenTitle;
                result.add(data);
            }
            if (Features.shouldDisplayPrioritySetting()) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627519);
                data.screenTitle = screenTitle;
                data.summaryOff = res.getString(2131627520);
                data.summaryOn = res.getString(2131627520);
                result.add(data);
            }
            if (Features.shouldDisplayCmccWarning()) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627794);
                data.summaryOff = res.getString(2131627795);
                data.summaryOn = res.getString(2131627795);
                data.screenTitle = screenTitle;
                result.add(data);
            }
            if (Features.shouldDisplayWifiToWifiPop()) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627796);
                data.summaryOff = res.getString(2131627797);
                data.summaryOn = res.getString(2131627797);
                data.screenTitle = screenTitle;
                result.add(data);
            }
            return result;
        }
    };
    private static final String[] wifi_sleep_policy = new String[]{"always", "only_when_plugged_in", "never"};
    private HwCustAdvancedWifiSettings mCust;

    public static class WpsFragment extends DialogFragment {
        private static int mWpsSetup;

        public WpsFragment(int wpsSetup) {
            mWpsSetup = wpsSetup;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new WpsDialog(getActivity(), mWpsSetup);
        }
    }

    public AdvancedWifiSettings() {
        super("no_config_wifi");
    }

    protected int getMetricsCategory() {
        return 104;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mCust = (HwCustAdvancedWifiSettings) HwCustUtils.createObj(HwCustAdvancedWifiSettings.class, new Object[]{getActivity()});
        if (this.mCust != null) {
            this.mCust.initCustPreference(this);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getEmptyTextView().setText(2131625026);
        if (this.mUnavailable) {
            getPreferenceScreen().removeAll();
        }
    }

    public void onResume() {
        super.onResume();
        if (!this.mUnavailable) {
            initPreferences();
        }
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(getActivity());
        super.onPause();
    }

    private void initPreferences() {
        Context context = getActivity();
        Intent intent = new Intent("android.credentials.INSTALL_AS_USER");
        intent.setClassName("com.android.certinstaller", "com.android.certinstaller.CertInstallerMain");
        intent.putExtra("install_as_uid", 1010);
        findPreference("install_credentials").setIntent(intent);
        findPreference("wps_push_button").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                new WpsFragment(0).show(AdvancedWifiSettings.this.getFragmentManager(), "wps_push_button");
                ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(AdvancedWifiSettings.this.getActivity(), arg0);
                return true;
            }
        });
        findPreference("wps_pin_entry").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                new WpsFragment(1).show(AdvancedWifiSettings.this.getFragmentManager(), "wps_pin_entry");
                ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(AdvancedWifiSettings.this.getActivity(), arg0);
                return true;
            }
        });
        initWifiSecurityCheckPreference(this);
        if (this.mCust != null) {
            this.mCust.resumeCustPreference(this);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if ("wps_push_button".equals(key)) {
            showDialog(2);
        } else if ("wps_pin_entry".equals(key)) {
            showDialog(3);
        } else if (!"install_credentials".equals(key)) {
            return super.onPreferenceTreeClick(preference);
        } else {
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        if ("wifi_cloud_security_check".equals(key)) {
            int i;
            ContentResolver contentResolver = getContentResolver();
            String str = "wifi_cloud_security_check";
            if (((Boolean) newValue).booleanValue()) {
                i = 1;
            } else {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        } else if (this.mCust != null) {
            this.mCust.onCustPreferenceChange(key, newValue);
        }
        return true;
    }
}
