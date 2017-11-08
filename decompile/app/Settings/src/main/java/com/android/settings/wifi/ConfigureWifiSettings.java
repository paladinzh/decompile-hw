package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppManager;
import android.net.NetworkScorerAppManager.NetworkScorerAppData;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.AppListSwitchPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.wifi.cmcc.Features;
import com.android.settings.wifi.cmcc.WifiExt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConfigureWifiSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = 2131230939;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = new ArrayList();
            keys.add("saved_networks");
            if (!Features.shouldDisplayNetmask()) {
                keys.add("wifi_gateway");
                keys.add("wifi_network_mask");
            }
            Collection<NetworkScorerAppData> scorers = NetworkScorerAppManager.getAllValidScorers(context);
            if (!UserManager.get(context).isAdminUser() || scorers.isEmpty()) {
                keys.add("wifi_assistant");
            }
            return keys;
        }
    };
    private IntentFilter mFilter;
    private NetworkScoreManager mNetworkScoreManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.LINK_CONFIGURATION_CHANGED") || action.equals("android.net.wifi.STATE_CHANGE")) {
                ConfigureWifiSettings.this.refreshWifiInfo();
            }
        }
    };
    private AppListSwitchPreference mWifiAssistantPreference;
    private WifiExt mWifiExt;
    private WifiManager mWifiManager;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230939);
        this.mWifiExt = new WifiExt(getActivity());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mNetworkScoreManager = (NetworkScoreManager) getSystemService("network_score");
        this.mWifiExt.refreshNetworkInfoView(getPreferenceScreen());
    }

    public void onResume() {
        super.onResume();
        initPreferences();
        getActivity().registerReceiver(this.mReceiver, this.mFilter);
        refreshWifiInfo();
    }

    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mReceiver);
    }

    private void initPreferences() {
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            removePreference("saved_networks");
        }
        Context context = getActivity();
        this.mWifiAssistantPreference = (AppListSwitchPreference) findPreference("wifi_assistant");
        Collection<NetworkScorerAppData> scorers = NetworkScorerAppManager.getAllValidScorers(context);
        if (UserManager.get(context).isAdminUser() && !scorers.isEmpty()) {
            this.mWifiAssistantPreference.setOnPreferenceChangeListener(this);
            initWifiAssistantPreference(scorers);
        } else if (this.mWifiAssistantPreference != null) {
            getPreferenceScreen().removePreference(this.mWifiAssistantPreference);
        }
        ListPreference sleepPolicyPref = (ListPreference) findPreference("sleep_policy");
        if (sleepPolicyPref != null) {
            if (Utils.isWifiOnly(context)) {
                sleepPolicyPref.setEntries(2131361858);
            }
            sleepPolicyPref.setOnPreferenceChangeListener(this);
            String stringValue = String.valueOf(Global.getInt(getContentResolver(), "wifi_sleep_policy", 2));
            sleepPolicyPref.setValue(stringValue);
            updateSleepPolicySummary(sleepPolicyPref, stringValue);
        }
    }

    private void updateSleepPolicySummary(Preference sleepPolicyPref, String value) {
        if (value != null) {
            String[] values = getResources().getStringArray(2131361859);
            String[] summaries = getResources().getStringArray(Utils.isWifiOnly(getActivity()) ? 2131361858 : 2131361857);
            int i = 0;
            while (i < values.length) {
                if (!value.equals(values[i]) || i >= summaries.length) {
                    i++;
                } else {
                    sleepPolicyPref.setSummary(summaries[i]);
                    return;
                }
            }
        }
        sleepPolicyPref.setSummary((CharSequence) "");
        Log.e("ConfigureWifiSettings", "Invalid sleep policy value: " + value);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Context context = getActivity();
        String key = preference.getKey();
        if ("wifi_assistant".equals(key)) {
            NetworkScorerAppData wifiAssistant = NetworkScorerAppManager.getScorer(context, (String) newValue);
            if (wifiAssistant == null) {
                this.mNetworkScoreManager.setActiveScorer(null);
                return true;
            }
            Intent intent = new Intent();
            if (wifiAssistant.mConfigurationActivityClassName != null) {
                intent.setClassName(wifiAssistant.mPackageName, wifiAssistant.mConfigurationActivityClassName);
            } else {
                intent.setAction("android.net.scoring.CHANGE_ACTIVE");
                intent.putExtra("packageName", wifiAssistant.mPackageName);
            }
            startActivity(intent);
            return false;
        }
        if ("sleep_policy".equals(key)) {
            try {
                String stringValue = (String) newValue;
                Global.putInt(getContentResolver(), "wifi_sleep_policy", Integer.parseInt(stringValue));
                updateSleepPolicySummary(preference, stringValue);
            } catch (NumberFormatException e) {
                Toast.makeText(context, 2131627279, 0).show();
                return false;
            }
        }
        return true;
    }

    private void refreshWifiInfo() {
        CharSequence macAddress = null;
        Context context = getActivity();
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        Preference wifiMacAddressPref = findPreference("mac_address");
        if (wifiInfo != null) {
            macAddress = wifiInfo.getMacAddress();
        }
        if (TextUtils.isEmpty(macAddress)) {
            macAddress = context.getString(2131625250);
        }
        wifiMacAddressPref.setSummary(macAddress);
        Preference wifiIpAddressPref = findPreference("current_ip_address");
        CharSequence ipAddress = Utils.getWifiIpAddresses(context);
        if (ipAddress == null) {
            ipAddress = context.getString(2131625250);
        }
        wifiIpAddressPref.setSummary(ipAddress);
    }

    private void initWifiAssistantPreference(Collection<NetworkScorerAppData> scorers) {
        String[] packageNames = new String[scorers.size()];
        int i = 0;
        for (NetworkScorerAppData scorer : scorers) {
            packageNames[i] = scorer.mPackageName;
            i++;
        }
        this.mWifiAssistantPreference.setPackageNames(packageNames, this.mNetworkScoreManager.getActiveScorerPackage());
    }

    protected int getMetricsCategory() {
        return 338;
    }
}
