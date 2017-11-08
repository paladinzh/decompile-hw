package com.android.settings.wifi.cmcc;

import android.app.ActionBar;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.SettingsPreferenceFragment;
import java.util.List;

public class WifiPrioritySettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private int configuredApCount;
    private List<WifiConfiguration> mConfigs;
    PreferenceCategory mConfiguredAps;
    private int[] mPriorityOrder;
    private WifiManager mWifiManager;

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230944);
        this.mConfiguredAps = (PreferenceCategory) findPreference("configured_ap_list");
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        initPage();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    public void initPage() {
        if (this.mWifiManager == null) {
            Log.e("WifiPrioritySettings", "Fail to get Wifi Manager service");
            return;
        }
        this.mConfigs = this.mWifiManager.getConfiguredNetworks();
        if (!(this.mConfigs == null || this.mConfiguredAps == null)) {
            int i;
            this.mConfiguredAps.removeAll();
            this.configuredApCount = this.mConfigs.size();
            CharSequence[] priorityEntries = new String[this.configuredApCount];
            for (i = 0; i < this.configuredApCount; i++) {
                priorityEntries[i] = String.valueOf(i + 1);
            }
            for (i = 0; i < this.configuredApCount; i++) {
                Log.e("WifiPrioritySettings", "Before sorting: priority array=" + ((WifiConfiguration) this.mConfigs.get(i)).priority);
            }
            this.mPriorityOrder = calculateInitPriority(this.mConfigs);
            String summaryPreStr = getResources().getString(2131627524) + ": ";
            for (i = 0; i < this.configuredApCount; i++) {
                Log.e("WifiPrioritySettings", "After sorting: order array=" + this.mPriorityOrder[i]);
                if (this.mPriorityOrder[i] >= 1) {
                    String ssidStr;
                    WifiConfiguration config = (WifiConfiguration) this.mConfigs.get(i);
                    if (config.priority != (this.configuredApCount - this.mPriorityOrder[i]) + 1) {
                        config.priority = (this.configuredApCount - this.mPriorityOrder[i]) + 1;
                        this.mWifiManager.updateNetwork(config);
                    }
                    if (config.SSID == null) {
                        ssidStr = "";
                    } else {
                        ssidStr = WifiInfo.removeDoubleQuotes(config.SSID);
                    }
                    ListPreference pref = new ListPreference(getContext());
                    pref.setOnPreferenceChangeListener(this);
                    pref.setTitle((CharSequence) ssidStr);
                    pref.setDialogTitle((CharSequence) ssidStr);
                    pref.setSummary(summaryPreStr + this.mPriorityOrder[i]);
                    pref.setWidgetLayoutResource(2130968998);
                    pref.setEntries(priorityEntries);
                    pref.setEntryValues(priorityEntries);
                    pref.setValueIndex(this.mPriorityOrder[i] - 1);
                    this.mConfiguredAps.addPreference(pref);
                }
            }
            this.mWifiManager.saveConfiguration();
        }
    }

    public int[] calculateInitPriority(List<WifiConfiguration> configs) {
        if (configs == null) {
            return new int[0];
        }
        for (WifiConfiguration config : configs) {
            WifiConfiguration config2;
            if (config2 == null) {
                config2 = new WifiConfiguration();
                config2.SSID = "ERROR";
                config2.priority = 0;
            }
        }
        int totalSize = configs.size();
        int[] result = new int[totalSize];
        for (int i = 0; i < totalSize; i++) {
            int biggestPoz = 0;
            for (int j = 1; j < totalSize; j++) {
                if (!formerHasHigherPriority((WifiConfiguration) configs.get(biggestPoz), (WifiConfiguration) configs.get(j))) {
                    biggestPoz = j;
                }
            }
            result[biggestPoz] = i + 1;
            ((WifiConfiguration) configs.get(biggestPoz)).priority = -1;
        }
        return result;
    }

    private boolean formerHasHigherPriority(WifiConfiguration former, WifiConfiguration backer) {
        boolean z = true;
        if (former == null) {
            return false;
        }
        if (backer == null || former.priority > backer.priority) {
            return true;
        }
        if (former.priority < backer.priority) {
            return false;
        }
        String formerSSID;
        String backerSSID;
        if (former.SSID == null) {
            formerSSID = "";
        } else {
            formerSSID = WifiInfo.removeDoubleQuotes(former.SSID);
        }
        if (backer.SSID == null) {
            backerSSID = "";
        } else {
            backerSSID = WifiInfo.removeDoubleQuotes(backer.SSID);
        }
        if ("CMCC-AUTO".equals(formerSSID)) {
            Log.d("WifiPrioritySettings", "WifiSettingsExt formerHasHigherPriority() same true");
            return true;
        } else if ("CMCC".equals(formerSSID)) {
            if ("CMCC-AUTO".equals(backerSSID)) {
                Log.d("WifiPrioritySettings", "WifiSettingsExt formerHasHigherPriority() same false");
                return false;
            }
            Log.d("WifiPrioritySettings", "WifiSettingsExt formerHasHigherPriority() same true");
            return true;
        } else if ("CMCC".equals(backerSSID) || "CMCC-AUTO".equals(backerSSID)) {
            Log.d("WifiPrioritySettings", "WifiSettingsExt formerHasHigherPriority() same false");
            return false;
        } else {
            if (formerSSID.compareTo(backerSSID) > 0) {
                z = false;
            }
            return z;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            int oldOrder = 0;
            int newOrder = 0;
            try {
                oldOrder = Integer.parseInt(((ListPreference) preference).getValue());
                newOrder = Integer.parseInt((String) newValue);
            } catch (NumberFormatException e) {
                Log.e("WifiPrioritySettings", "Error happens when modify priority manually");
                e.printStackTrace();
            }
            Log.e("WifiPrioritySettings", "Priority old value=" + oldOrder + ", new value=" + newOrder);
            Toast.makeText(getContext(), getString(2131627521, new Object[]{pref.getValue(), (String) newValue}), 0).show();
            if (!(oldOrder == newOrder || this.mPriorityOrder == null)) {
                int i;
                WifiConfiguration config;
                int[] iArr;
                if (oldOrder > newOrder) {
                    i = 0;
                    while (i < this.mPriorityOrder.length) {
                        config = (WifiConfiguration) this.mConfigs.get(i);
                        if (this.mPriorityOrder[i] >= newOrder && this.mPriorityOrder[i] < oldOrder) {
                            iArr = this.mPriorityOrder;
                            iArr[i] = iArr[i] + 1;
                            config.priority = (this.configuredApCount - this.mPriorityOrder[i]) + 1;
                            this.mWifiManager.updateNetwork(config);
                        } else if (this.mPriorityOrder[i] == oldOrder) {
                            this.mPriorityOrder[i] = newOrder;
                            config.priority = (this.configuredApCount - newOrder) + 1;
                            this.mWifiManager.updateNetwork(config);
                        }
                        i++;
                    }
                } else {
                    i = 0;
                    while (i < this.mPriorityOrder.length) {
                        config = (WifiConfiguration) this.mConfigs.get(i);
                        if (this.mPriorityOrder[i] <= newOrder && this.mPriorityOrder[i] > oldOrder) {
                            iArr = this.mPriorityOrder;
                            iArr[i] = iArr[i] - 1;
                            config.priority = (this.configuredApCount - this.mPriorityOrder[i]) + 1;
                            this.mWifiManager.updateNetwork(config);
                        } else if (this.mPriorityOrder[i] == oldOrder) {
                            this.mPriorityOrder[i] = newOrder;
                            config.priority = (this.configuredApCount - newOrder) + 1;
                            this.mWifiManager.updateNetwork(config);
                        }
                        i++;
                    }
                }
                this.mWifiManager.saveConfiguration();
                updateUI();
            }
        }
        return true;
    }

    public void updateUI() {
        for (int i = 0; i < this.mPriorityOrder.length; i++) {
            Preference pref = this.mConfiguredAps.getPreference(i);
            if (pref != null) {
                pref.setSummary((getResources().getString(2131627524) + ": ") + this.mPriorityOrder[i]);
            }
            if (pref instanceof ListPreference) {
                ((ListPreference) pref).setValue(String.valueOf(this.mPriorityOrder[i]));
            }
        }
    }
}
