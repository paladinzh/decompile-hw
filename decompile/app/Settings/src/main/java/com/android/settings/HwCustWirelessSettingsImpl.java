package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.android.util.NoExtAPIException;

public class HwCustWirelessSettingsImpl extends HwCustWirelessSettings implements OnPreferenceClickListener {
    private static final String ACTION_NETWORK_BOOSTER = "com.huawei.android.downloadbooster.DoubleDownloadOpenSettingActivity";
    private static final boolean IS_SUPPORT_HUAWEI_BEAM = SystemProperties.getBoolean("ro.config.support_huawei_beam", true);
    private static final String KEY_NETWORK_BOOSTER = "dualcard_network_booster";
    private static final String KEY_NFC_ENTRY = "nfc_entry";
    private static final String TAG = "HwCustWirelessSettingsImpl";
    private PreferenceScreen mToggleNetworkBooster;
    private boolean showDualCardIntelligentBooster = SystemProperties.getBoolean("ro.config.hw_download_booster", false);

    public HwCustWirelessSettingsImpl(WirelessSettings wirelessSettings) {
        super(wirelessSettings);
    }

    public boolean isNfcDisabled(Context context) {
        if (SystemProperties.getBoolean("ro.config.nfcdisabled", false)) {
            return true;
        }
        return false;
    }

    public void updateCustPreference(Context context) {
        try {
            Class hwCustCls = Class.forName("com.android.settings.nfc.HwCustAndroidBeam");
            PreferenceScreen nfcEntryPreference = (PreferenceScreen) this.mWirelessSettings.findPreference(KEY_NFC_ENTRY);
            if ((SystemProperties.getBoolean("ro.config.hw_nfc_msimce", false) || (SystemProperties.getBoolean("ro.config.nfc_cardreader", false) && hwCustCls != null)) && nfcEntryPreference != null) {
                nfcEntryPreference.setFragment("com.android.settings.nfc.HwCustAndroidBeam");
            }
            if (!(IS_SUPPORT_HUAWEI_BEAM || nfcEntryPreference == null)) {
                nfcEntryPreference.setSummary(2131628497);
            }
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Can not find Class <HwCustAndroidBeam>, it's ok for platform");
        }
        this.mToggleNetworkBooster = new PreferenceScreen(context, null);
        PreferenceScreen root = this.mWirelessSettings.getPreferenceScreen();
        if (root.findPreference("vpn_settings") != null) {
            buildCustPreference(this.mToggleNetworkBooster, root, KEY_NETWORK_BOOSTER, 2131629190, 2130968998, root.findPreference("vpn_settings").getOrder() + 1);
            if ((!this.showDualCardIntelligentBooster && this.mToggleNetworkBooster != null) || !Utils.hasIntentActivity(context.getPackageManager(), ACTION_NETWORK_BOOSTER)) {
                this.mWirelessSettings.getPreferenceScreen().removePreference(this.mToggleNetworkBooster);
            }
            this.mToggleNetworkBooster.setOnPreferenceClickListener(this);
            if (HwCustSettingsUtils.IS_SPRINT) {
                this.mWirelessSettings.getPreferenceScreen().removePreference((PreferenceScreen) this.mWirelessSettings.findPreference("tether_settings"));
            }
        }
    }

    private void buildCustPreference(Preference preference, PreferenceGroup container, String key, int titleId, int widgetLayoutResId, int order) {
        preference.setKey(key);
        preference.setTitle(titleId);
        preference.setWidgetLayoutResource(widgetLayoutResId);
        container.addPreference(preference);
        preference.setOrder(order);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!KEY_NETWORK_BOOSTER.equals(preference.getKey())) {
            return false;
        }
        this.mWirelessSettings.getActivity().startActivity(new Intent(ACTION_NETWORK_BOOSTER));
        return true;
    }

    public void updateEnable4GPreferenceTitle(CustomSwitchPreference mEnable4GPreference) {
        if (isMccChange4G("hw_show_lte") && mEnable4GPreference != null) {
            mEnable4GPreference.setTitle((CharSequence) mEnable4GPreference.getTitle().toString().replace("4G", "LTE"));
        }
        if (isMccChange4G("hw_show_4_5G_for_mcc") && mEnable4GPreference != null) {
            mEnable4GPreference.setTitle((CharSequence) mEnable4GPreference.getTitle().toString().replace("4G", "4.5G"));
        }
    }

    private boolean isMccChange4G(String configEntry) {
        String configString = System.getString(this.mWirelessSettings.getActivity().getContentResolver(), configEntry);
        if (TextUtils.isEmpty(configString)) {
            return false;
        }
        if ("ALL".equals(configString)) {
            return true;
        }
        boolean result = false;
        int mSwitchDualCardSlot = 0;
        if (Utils.isMultiSimEnabled()) {
            try {
                mSwitchDualCardSlot = TelephonyManagerEx.getDefault4GSlotId();
            } catch (NoExtAPIException e) {
                Log.v(TAG, "TelephonyManagerEx.getDefault4GSlotId()->NoExtAPIException!");
            }
        }
        String mccmnc = TelephonyManager.from(this.mWirelessSettings.getActivity()).getSimOperatorNumericForPhone(mSwitchDualCardSlot);
        if (!TextUtils.isEmpty(mccmnc)) {
            String[] custValues = configString.trim().split(";");
            int size = custValues.length;
            int i = 0;
            while (i < size) {
                if (mccmnc.startsWith(custValues[i]) || mccmnc.equalsIgnoreCase(custValues[i])) {
                    result = true;
                    break;
                }
                i++;
            }
        }
        Log.i(TAG, "mcc_ncc =" + mccmnc + ", configString=" + configString);
        return result;
    }
}
