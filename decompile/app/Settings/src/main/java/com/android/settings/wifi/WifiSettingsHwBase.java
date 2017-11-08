package com.android.settings.wifi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.ItemUseStat;
import com.android.settings.ProgressCategory;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.TwoSummaryPreference;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.wifi.cmcc.WifiSettingsExt;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiTracker;
import com.huawei.cust.HwCustUtils;

public class WifiSettingsHwBase extends RestrictedSettingsFragment {
    protected Bundle mAccessPointSavedState;
    protected Preference mAddPreference;
    protected PreferenceCategory mConfigedAPCategory;
    protected WifiConfiguration mConnectedConfig;
    protected int mDialogMode;
    protected HwCustWifiSettingsHwBase mHwCustWifiSettingsHwBase = ((HwCustWifiSettingsHwBase) HwCustUtils.createObj(HwCustWifiSettingsHwBase.class, new Object[]{this}));
    protected boolean mIsScanning = false;
    protected PreferenceCategory mNewAPCategory;
    protected boolean mP2pSupported = true;
    protected Handler mScanHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                WifiSettingsHwBase.this.stopScan();
            }
        }
    };
    protected AccessPoint mSelectedAccessPoint;
    protected boolean mSetupWizardMode;
    protected Preference mSwitchOnMoblieDataPreference;
    protected PreferenceCategory mWifiListCategory;
    protected WifiManager mWifiManager;
    protected Bundle mWifiNfcDialogSavedState;
    protected WifiSettingsExt mWifiSettingsExt;
    protected SwitchPreference mWifiSwitchPreference;
    protected WifiTracker mWifiTracker;

    protected int getMetricsCategory() {
        return 100000;
    }

    public WifiSettingsHwBase(String restrictionKey) {
        super(restrictionKey);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            this.mDialogMode = icicle.getInt("dialog_mode");
            if (icicle.containsKey("wifi_ap_state")) {
                this.mAccessPointSavedState = icicle.getBundle("wifi_ap_state");
            }
            if (icicle.containsKey("wifi_nfc_dlg_state")) {
                this.mWifiNfcDialogSavedState = icicle.getBundle("wifi_nfc_dlg_state");
            }
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        boolean z = true;
        super.onActivityCreated(savedInstanceState);
        if (this.mP2pSupported) {
            if (System.getInt(getContentResolver(), "wifi_p2p_state", 1) == 0) {
                z = false;
            }
            this.mP2pSupported = z;
        }
        AccessPointFilter.initialFilter(getActivity());
    }

    protected void initPreferences(WifiManager wifiManager) {
        this.mWifiSwitchPreference = (SwitchPreference) findPreference("wifi_switch");
        if (this.mWifiSwitchPreference != null) {
            this.mWifiSwitchPreference.setChecked(this.mWifiManager.isWifiEnabled());
        }
        if (this.mWifiSwitchPreference != null) {
            this.mWifiSwitchPreference.setKey("wifi_switch");
        }
        this.mWifiListCategory = (PreferenceCategory) findPreference("wifi_list_category");
        this.mAddPreference = findPreference("add_other_network");
        this.mSwitchOnMoblieDataPreference = findPreference("switch_on_moblie_data");
        if (this.mSetupWizardMode) {
            this.mWifiListCategory.setLayoutResource(2130969271);
            this.mAddPreference.setLayoutResource(2130968907);
            this.mAddPreference.setWidgetLayoutResource(0);
            if (Utils.isWifiOnly(getActivity())) {
                removePreference("switch_on_moblie_data");
            }
        } else {
            removePreference("switch_on_moblie_data");
        }
        this.mConfigedAPCategory = (PreferenceCategory) findPreference("configed_access_points");
        this.mNewAPCategory = (PreferenceCategory) findPreference("new_access_points");
        this.mWifiSettingsExt = new WifiSettingsExt(getActivity(), this.mConfigedAPCategory, this.mNewAPCategory, this.mWifiListCategory);
        this.mWifiSettingsExt.setCategory(this.mConfigedAPCategory, this.mNewAPCategory, this.mWifiListCategory);
        this.mWifiSettingsExt.setAddNetworkPreference(this.mAddPreference);
        this.mWifiSettingsExt.evaluateCmccCategory(getPreferenceScreen());
    }

    protected void invalidateOptionsMenu() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void dismissMenu() {
        Activity activity = getActivity();
        if (activity != null) {
            Log.d("WifiSettingsHwBase", "diamissMenu()-->dismiss pop menu");
            activity.closeContextMenu();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateOptionsMenu();
        dismissMenu();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!this.mSetupWizardMode) {
            boolean z;
            boolean wifiIsEnabled = this.mWifiTracker.isWifiEnabled();
            MenuItem icon = menu.add(0, 6, 0, 2131624938).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_WIFI_SCAN)));
            if (!wifiIsEnabled || this.mIsScanning) {
                z = false;
            } else {
                z = true;
            }
            icon.setEnabled(z).setShowAsAction(1);
            if (this.mP2pSupported) {
                int wifidirectIconId = Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_WIFI_DIRECT);
                if (this.mHwCustWifiSettingsHwBase == null || this.mHwCustWifiSettingsHwBase.isSupportStaP2pCoexist()) {
                    menu.add(0, 3, 0, 2131625037).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), wifidirectIconId)).setEnabled(wifiIsEnabled).setShowAsAction(1);
                } else {
                    menu.add(0, 3, 0, 2131625037).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), wifidirectIconId)).setEnabled(true).setShowAsAction(1);
                }
            }
            menu.add(0, 5, 0, 2131625020).setShowAsAction(0);
            menu.add(0, 11, 0, 2131624940).setIcon(2130838288).setShowAsAction(1);
            menu.add(0, 14, 0, 2131626521).setShowAsAction(0);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (inSuperPowerSavingMode()) {
            grayAllMenu(menu);
        }
    }

    protected void startFragment() {
        if (this.mSelectedAccessPoint == null && this.mAccessPointSavedState != null) {
            this.mSelectedAccessPoint = new AccessPoint(getActivity(), this.mAccessPointSavedState);
        }
        if (this.mAccessPointSavedState == null) {
            this.mAccessPointSavedState = new Bundle();
        }
        if (this.mSelectedAccessPoint != null) {
            this.mSelectedAccessPoint.saveWifiState(this.mAccessPointSavedState);
            this.mAccessPointSavedState.putBoolean("is_add_network", false);
            this.mAccessPointSavedState.putString("key_ori_ssid", this.mSelectedAccessPoint.getOriSsid());
            this.mAccessPointSavedState.putBoolean("is_hi_link_network", this.mSelectedAccessPoint.isHiLinkNetwork());
            this.mAccessPointSavedState.putString("bssid", this.mSelectedAccessPoint.getNonNullBssid());
        } else {
            this.mAccessPointSavedState.putBoolean("is_add_network", true);
        }
        this.mAccessPointSavedState.putInt("dialog_mode", this.mDialogMode);
        ((SettingsActivity) getActivity()).startPreferencePanel(WifiAddFragment.class.getName(), this.mAccessPointSavedState, 2131624902, null, this, 210);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle;
        if (requestCode == 210 && resultCode == -4) {
            bundle = data.getBundleExtra("wifi_config");
            if (bundle != null) {
                this.mConnectedConfig = (WifiConfiguration) bundle.getParcelable("wifi_configuration");
            }
        } else if (requestCode == 210 && resultCode == -3) {
            WifiConfiguration wifiConfiguration = null;
            boolean isEdit = data.getBooleanExtra("wifi_edit", false);
            bundle = data.getBundleExtra("wifi_config");
            if (bundle != null) {
                wifiConfiguration = (WifiConfiguration) bundle.getParcelable("wifi_configuration");
            }
            submit(wifiConfiguration, isEdit);
        } else if (requestCode == 210 && resultCode == -2) {
            forget();
        }
    }

    void forget() {
    }

    void submit(WifiConfiguration config, boolean isEdit) {
    }

    void submit(WifiConfigController configController) {
        submit(configController.getConfig(), configController.getMode() == 2);
    }

    public void onResume() {
        super.onResume();
        if (SystemProperties.getBoolean("ro.config.hw_wifipro_enable", false) && Utils.isOwnerUser()) {
            TwoSummaryPreference wifiPlusPrefs = (TwoSummaryPreference) findPreference("wifi_plus_entry");
            if (wifiPlusPrefs != null) {
                boolean smartNetworkSwitchingOn;
                if (System.getInt(getContentResolver(), "smart_network_switching", 0) == 1) {
                    smartNetworkSwitchingOn = true;
                } else {
                    smartNetworkSwitchingOn = false;
                }
                if (smartNetworkSwitchingOn) {
                    wifiPlusPrefs.setSummary(2131627698);
                } else {
                    wifiPlusPrefs.setSummary(2131627699);
                }
                if (inSuperPowerSavingMode()) {
                    wifiPlusPrefs.setEnabled(false);
                    return;
                } else if (!wifiPlusPrefs.isEnabled()) {
                    wifiPlusPrefs.setEnabled(true);
                    return;
                } else {
                    return;
                }
            }
            return;
        }
        removePreference("wifi_plus_entry");
    }

    protected void handlePreferenceClick(Preference preference) {
        if (preference != this.mWifiSwitchPreference) {
            ItemUseStat stat = ItemUseStat.getInstance();
            if (preference instanceof AccessPointPreference) {
                System.putInt(getActivity().getContentResolver(), "wlan_switch_on", 0);
                stat.handleClick(getActivity(), 2, "wifi_access_point");
                return;
            }
            stat.handleClick(getActivity(), 2, preference.getKey());
        }
    }

    protected void handleMenuItemClick(int menuItem) {
        ItemUseStat stat = ItemUseStat.getInstance();
        Context context = getActivity();
        if (6 == menuItem) {
            stat.handleClick(context, 2, "wifi_scan");
        } else if (3 == menuItem) {
            stat.handleClick(context, 2, "wifi_p2p");
        } else if (12 == menuItem) {
            stat.handleClick(context, 2, "saved_network");
        } else if (5 == menuItem) {
            stat.handleClick(context, 2, "wlan_advance");
        }
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(getActivity());
        super.onPause();
    }

    protected void setProgress(boolean progressOn) {
        if (this.mWifiListCategory instanceof ProgressCategory) {
            ((ProgressCategory) this.mWifiListCategory).setProgress(progressOn);
        }
        if (this.mConfigedAPCategory instanceof ProgressCategory) {
            ((ProgressCategory) this.mConfigedAPCategory).setProgress(progressOn);
        }
        if (this.mNewAPCategory instanceof ProgressCategory) {
            ((ProgressCategory) this.mNewAPCategory).setProgress(progressOn);
        }
    }

    protected void requestJlogEnable(boolean enable) {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).requestJlogEnable(enable);
        }
    }

    protected void startScan() {
        if (!isUiRestricted()) {
            this.mIsScanning = true;
            invalidateOptionsMenu();
            this.mScanHandler.sendEmptyMessageDelayed(1, 2000);
            setProgress(true);
        }
    }

    protected void stopScan() {
        this.mIsScanning = false;
        invalidateOptionsMenu();
        setProgress(false);
    }

    private void grayAllMenu(Menu menu) {
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setEnabled(false);
            }
        }
    }

    private boolean inSuperPowerSavingMode() {
        return SystemProperties.getBoolean("sys.super_power_save", false);
    }
}
