package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import java.util.ArrayList;
import java.util.List;

public class HwCustWifiCallingSettingsImpl extends HwCustWifiCallingSettings {
    private static final String BUTTON_WFC_MODE = "wifi_calling_mode";
    private static final boolean IS_REMOVE_WIFI_CALLING_MODE = SystemProperties.getBoolean("ro.config.is_remove_wfc_button", false);
    private static final String KEY_WIFI_CALLING_SWITCH = "wifi_calling_switch";
    private static final String SUMMARY = "description_preference";
    private final String CUST_MCCMNC_DOMESTIC = SystemProperties.get("ro.config.vowifi_pref_domestic", "");
    private final String CUST_MCCMNC_PREF_WIFI_CELL = SystemProperties.get("ro.config.vowifi_pref_wifi_cell", "");
    private final String CUST_MCCMNC_ROAMING = SystemProperties.get("ro.config.vowifi_pref_roaming", "");
    private final String CUST_MCCMNC_TITLE = SystemProperties.get("ro.config.vowifi_title", "");
    private final String CUST_MCCMNC_TITLE_MCCMNC = SystemProperties.get("ro.config.vowifi_title_mccmnc", "");
    private ListPreference mButtonWfcMode;
    private BroadcastReceiver mCarrierConfigReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.telephony.action.CARRIER_CONFIG_CHANGED".equals(action)) {
                HwCustWifiCallingSettingsImpl.this.carrierConfigChange();
            }
        }
    };
    private List<String> mCustMccMncsDomestic = null;
    private List<String> mCustMccMncsPrefWifiCell = null;
    private List<String> mCustMccMncsRoaming = null;
    private List<String> mCustMccMncsTitle = null;
    private List<String> mCustMccMncsTitleMccMnc = null;
    private boolean mEditableWfcMode;
    private PreferenceScreen mPreferenceScreen;
    private SwitchPreference mWfcSwtichPrefs;

    public void removeWifiCallingModeIfNeeded(PreferenceFragment mPreferenceFragment) {
        if (IS_REMOVE_WIFI_CALLING_MODE && mPreferenceFragment != null) {
            PreferenceScreen preferenceScreen = mPreferenceFragment.getPreferenceScreen();
            if (preferenceScreen != null) {
                Preference mButtonWfcMode = preferenceScreen.findPreference(BUTTON_WFC_MODE);
                Preference mSummary = preferenceScreen.findPreference(SUMMARY);
                if (!(mButtonWfcMode == null || mSummary == null)) {
                    preferenceScreen.removePreference(mButtonWfcMode);
                    mSummary.setSummary(mPreferenceFragment.getActivity().getString(2131628926));
                }
            }
        }
    }

    public void init(PreferenceFragment preferenceFragment, boolean editableWfcMode) {
        if (!isBtnTitleNormal() || !isBtnShowNormal() || !isNotPrefWifiCell()) {
            loadTitleMccMncs();
            loadShowDynMccMncs();
            loadPrefWifiCellMccMncs();
            if (preferenceFragment != null) {
                this.mEditableWfcMode = editableWfcMode;
                this.mPreferenceScreen = preferenceFragment.getPreferenceScreen();
                if (this.mPreferenceScreen != null) {
                    this.mButtonWfcMode = (ListPreference) this.mPreferenceScreen.findPreference(BUTTON_WFC_MODE);
                    this.mWfcSwtichPrefs = (SwitchPreference) this.mPreferenceScreen.findPreference(KEY_WIFI_CALLING_SWITCH);
                    updatePrefData();
                }
            }
        }
    }

    private boolean isBtnTitleNormal() {
        return TextUtils.isEmpty(this.CUST_MCCMNC_TITLE_MCCMNC);
    }

    private boolean isBtnShowNormal() {
        return TextUtils.isEmpty(this.CUST_MCCMNC_ROAMING) ? TextUtils.isEmpty(this.CUST_MCCMNC_DOMESTIC) : false;
    }

    private boolean isNotPrefWifiCell() {
        return TextUtils.isEmpty(this.CUST_MCCMNC_PREF_WIFI_CELL);
    }

    private String getMccMnc(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager.getSimState() == 5) {
            return telephonyManager.getSimOperator();
        }
        return null;
    }

    private void loadCustData(List<String> list, String data) {
        if (list != null && !TextUtils.isEmpty(data)) {
            String[] lMccMncArray = data.split(";");
            if (lMccMncArray != null && lMccMncArray.length > 0) {
                for (String mccMnc : lMccMncArray) {
                    list.add(mccMnc);
                }
            }
        }
    }

    private void loadTitleMccMncs() {
        if (this.mCustMccMncsTitle == null) {
            this.mCustMccMncsTitle = new ArrayList();
            loadCustData(this.mCustMccMncsTitle, this.CUST_MCCMNC_TITLE);
        }
        if (this.mCustMccMncsTitleMccMnc == null) {
            this.mCustMccMncsTitleMccMnc = new ArrayList();
            loadCustData(this.mCustMccMncsTitleMccMnc, this.CUST_MCCMNC_TITLE_MCCMNC);
        }
    }

    private void loadShowDynMccMncs() {
        if (this.mCustMccMncsRoaming == null) {
            this.mCustMccMncsRoaming = new ArrayList();
            loadCustData(this.mCustMccMncsRoaming, this.CUST_MCCMNC_ROAMING);
        }
        if (this.mCustMccMncsDomestic == null) {
            this.mCustMccMncsDomestic = new ArrayList();
            loadCustData(this.mCustMccMncsDomestic, this.CUST_MCCMNC_DOMESTIC);
        }
    }

    private void loadPrefWifiCellMccMncs() {
        if (this.mCustMccMncsPrefWifiCell == null) {
            this.mCustMccMncsPrefWifiCell = new ArrayList();
            loadCustData(this.mCustMccMncsPrefWifiCell, this.CUST_MCCMNC_PREF_WIFI_CELL);
        }
    }

    private void loadNormalPrefData() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPreferenceScreen.getContext().getSystemService("carrier_config");
        boolean z = true;
        boolean isCellularSupported = false;
        if (configManager != null) {
            PersistableBundle b = configManager.getConfig();
            if (b != null) {
                z = b.getBoolean("carrier_wfc_supports_wifi_only_bool", true);
                isCellularSupported = b.getBoolean("carrier_wfc_supports_cellular_preferred_bool", false);
            }
        }
        if (!isCellularSupported && !z) {
            this.mButtonWfcMode.setEntries(2131362006);
            this.mButtonWfcMode.setEntryValues(2131362007);
        } else if (!z) {
            this.mButtonWfcMode.setEntries(2131362002);
            this.mButtonWfcMode.setEntryValues(2131362003);
        } else if (isCellularSupported) {
            this.mButtonWfcMode.setEntries(2131362000);
            this.mButtonWfcMode.setEntryValues(2131362001);
        } else {
            this.mButtonWfcMode.setEntries(2131362004);
            this.mButtonWfcMode.setEntryValues(2131362005);
        }
    }

    public void updatePrefData() {
        if (!(isNotPrefWifiCell() || this.mButtonWfcMode == null || this.mPreferenceScreen == null)) {
            String mccMnc = getMccMnc(this.mPreferenceScreen.getContext());
            if (mccMnc == null || !this.mCustMccMncsPrefWifiCell.contains(mccMnc)) {
                loadNormalPrefData();
            } else {
                this.mButtonWfcMode.setEntries(2131361998);
                this.mButtonWfcMode.setEntryValues(2131361999);
            }
            int wfcMode = ImsManager.getWfcMode(this.mPreferenceScreen.getContext(), TelephonyManager.getDefault().isNetworkRoaming());
            this.mButtonWfcMode.setValue(Integer.toString(wfcMode));
            Utils.refreshListPreferenceSummary(this.mButtonWfcMode, String.valueOf(wfcMode));
        }
    }

    public void updateSwitchBtnTitle() {
        if (!(isBtnTitleNormal() || this.mWfcSwtichPrefs == null || this.mPreferenceScreen == null)) {
            String mccMnc = getMccMnc(this.mPreferenceScreen.getContext());
            int index = -1;
            if (mccMnc != null) {
                index = this.mCustMccMncsTitleMccMnc.indexOf(mccMnc);
            }
            if (index < 0 || index >= this.mCustMccMncsTitle.size()) {
                this.mWfcSwtichPrefs.setTitle(2131628511);
            } else {
                this.mWfcSwtichPrefs.setTitle((CharSequence) this.mCustMccMncsTitle.get(index));
            }
        }
    }

    public boolean removeOrAddPrefBtnDyn() {
        if (this.mButtonWfcMode != null) {
            return removeOrAddPrefBtnDyn(this.mWfcSwtichPrefs.isChecked());
        }
        return false;
    }

    public boolean removeOrAddPrefBtnDyn(boolean enable) {
        if (isBtnShowNormal()) {
            return false;
        }
        if (this.mPreferenceScreen != null) {
            if (enable && this.mEditableWfcMode) {
                String mccMnc = getMccMnc(this.mPreferenceScreen.getContext());
                if (mccMnc == null) {
                    this.mPreferenceScreen.addPreference(this.mButtonWfcMode);
                } else if (TelephonyManager.getDefault().isNetworkRoaming()) {
                    if (this.mCustMccMncsRoaming.contains(mccMnc)) {
                        this.mPreferenceScreen.removePreference(this.mButtonWfcMode);
                    } else {
                        this.mPreferenceScreen.addPreference(this.mButtonWfcMode);
                    }
                } else if (this.mCustMccMncsDomestic.contains(mccMnc)) {
                    this.mPreferenceScreen.removePreference(this.mButtonWfcMode);
                } else {
                    this.mPreferenceScreen.addPreference(this.mButtonWfcMode);
                }
            } else {
                this.mPreferenceScreen.removePreference(this.mButtonWfcMode);
            }
        }
        return true;
    }

    private void carrierConfigChange() {
        updateSwitchBtnTitle();
        if (this.mPreferenceScreen != null) {
            if (IS_REMOVE_WIFI_CALLING_MODE) {
                this.mPreferenceScreen.removePreference(this.mButtonWfcMode);
            } else if (Utils.isHideWfcPreferenceForMcc(this.mPreferenceScreen.getContext())) {
                this.mPreferenceScreen.removePreference(this.mButtonWfcMode);
            } else if (!removeOrAddPrefBtnDyn()) {
                if (this.mWfcSwtichPrefs.isChecked() && this.mEditableWfcMode) {
                    this.mPreferenceScreen.addPreference(this.mButtonWfcMode);
                } else {
                    this.mPreferenceScreen.removePreference(this.mButtonWfcMode);
                }
            }
        }
        updatePrefData();
    }

    public void registerCarrierReceiver() {
        if (!((isBtnTitleNormal() && isBtnShowNormal() && isNotPrefWifiCell()) || this.mPreferenceScreen == null)) {
            this.mPreferenceScreen.getContext().registerReceiver(this.mCarrierConfigReceiver, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"));
        }
    }

    public void unregisterCarrierReceiver() {
        if (!((isBtnTitleNormal() && isBtnShowNormal() && isNotPrefWifiCell()) || this.mPreferenceScreen == null)) {
            this.mPreferenceScreen.getContext().unregisterReceiver(this.mCarrierConfigReceiver);
        }
    }
}
