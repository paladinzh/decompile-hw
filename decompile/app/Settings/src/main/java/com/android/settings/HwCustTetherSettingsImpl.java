package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.telephony.TelephonyManager;
import com.huawei.android.provider.SettingsEx.Systemex;
import java.io.File;
import java.util.Random;

public class HwCustTetherSettingsImpl extends HwCustTetherSettings {
    private static final String AP_PRESHAREDKEY_VALUE = "ap_presharedkey_value";
    private static final String AP_SSID_VALUE = "ap_ssid_value";
    private static final String DEFAULT_AP = "set_defalut_ap";
    private static final int DEFAULT_PASSWORD_LENGTH = 8;
    public static final String DMPROPERTY_DIRECTORY = "/data/OtaSave/Extensions/";
    public static final String DMPROPERTY_USB_DATA = "usb_data.disable";
    private static final String ENABLE_BLUETOOTH_TETHERING = "enable_bluetooth_tethering";
    private static final String WIFI_AP_SETTINGS = "wifi_ap_settings";
    private static final boolean isWifiHotspotPwdAlphanum8 = SystemProperties.getBoolean("ro.config.WifiHotspotPwd_8chars", false);
    private static final boolean isWifiHotspotSSID = SystemProperties.getBoolean("ro.config.WifiHotspotSSID", false);
    private String custContry = SystemProperties.get("ro.config.hw_opta", "");
    private String custOperator = SystemProperties.get("ro.config.hw_optb", "");

    public HwCustTetherSettingsImpl(TetherSettings tetherSettings) {
        super(tetherSettings);
    }

    public boolean setBluetoothTetheringVisibility(Context context, boolean isBluetoothTetheringOn) {
        if (!SystemProperties.getBoolean("ro.config.hide_bt_tethering", false)) {
            return isBluetoothTetheringOn;
        }
        PreferenceScreen root = this.mTetherSettings.getPreferenceScreen();
        Preference bluetoothTethering = root.findPreference(ENABLE_BLUETOOTH_TETHERING);
        if (bluetoothTethering != null) {
            root.removePreference(bluetoothTethering);
            isBluetoothTetheringOn = false;
        }
        return isBluetoothTetheringOn;
    }

    public void configureDefaultWifiHotspotName(Context context) {
        boolean equals;
        if (this.custContry.equals("109")) {
            equals = this.custOperator.equals("724");
        } else {
            equals = false;
        }
        boolean isNeedSetDefaultApConfig = System.getInt(context.getContentResolver(), DEFAULT_AP, 1) != 0;
        if ((equals || isWifiHotspotSSID) && isNeedSetDefaultApConfig) {
            setDefaultApConfiguration(context);
        }
    }

    private String getPasswordofSSID() {
        int i;
        int passwordLength;
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        int[] tempRandom = new int[12];
        for (i = 0; i < 12; i++) {
            tempRandom[i] = random.nextInt(10);
        }
        if (isWifiHotspotSSID) {
            passwordLength = 8;
        } else {
            passwordLength = tempRandom.length;
        }
        for (i = 0; i < passwordLength; i++) {
            buf.append(tempRandom[i]);
        }
        return buf.toString();
    }

    private void setDefaultApConfiguration(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService("wifi");
        WifiConfiguration wifiCmonfig = wifiManager.getWifiApConfiguration();
        ContentResolver resolver = mContext.getContentResolver();
        String ssid = System.getString(resolver, AP_SSID_VALUE);
        String preSharedKey = System.getString(resolver, AP_PRESHAREDKEY_VALUE);
        String IMEIStr = ((TelephonyManager) mContext.getSystemService("phone")).getDeviceId();
        String last4StrofIMEI = "";
        if (IMEIStr != null && IMEIStr.length() > 4) {
            last4StrofIMEI = IMEIStr.substring(IMEIStr.length() - 4);
        }
        if (isWifiHotspotSSID) {
            ssid = SystemProperties.get("ro.product.model", "") + "_" + last4StrofIMEI;
        } else if (ssid == null) {
            ssid = "AndroidAP" + last4StrofIMEI;
        }
        if (preSharedKey == null) {
            if (isWifiHotspotPwdAlphanum8) {
                preSharedKey = wifiCmonfig.preSharedKey;
                if (preSharedKey != null && preSharedKey.length() > 8) {
                    preSharedKey = preSharedKey.substring(0, 8);
                }
            } else {
                preSharedKey = getPasswordofSSID();
            }
        }
        wifiCmonfig.SSID = ssid;
        wifiCmonfig.preSharedKey = preSharedKey;
        wifiManager.setWifiApConfiguration(wifiCmonfig);
        System.putInt(resolver, DEFAULT_AP, 0);
    }

    public void customizePreferenceScreen(PreferenceScreen prefRoot) {
        if (HwCustSettingsUtils.IS_SPRINT) {
            if (isRemoveTetherPreference(WIFI_AP_SETTINGS)) {
                prefRoot.removePreference((PreferenceScreen) prefRoot.findPreference(WIFI_AP_SETTINGS));
            }
            if (isRemoveTetherPreference(ENABLE_BLUETOOTH_TETHERING)) {
                prefRoot.removePreference((TwoStatePreference) prefRoot.findPreference(ENABLE_BLUETOOTH_TETHERING));
            }
        }
        if (this.mTetherSettings.mWifiRegexs.length == 0) {
            PreferenceScreen wifiAp = (PreferenceScreen) prefRoot.findPreference(WIFI_AP_SETTINGS);
            if (wifiAp != null) {
                prefRoot.removePreference(wifiAp);
            }
        }
    }

    private boolean isRemoveTetherPreference(String tetherType) {
        boolean z = true;
        String DMPROPERTY_DIR_PATH = "/data/OtaSave/Extensions/";
        String GLOBAL_WIFI = "global_wifi.disable";
        String GLOBAL_BLUETOOTH = "global_bluetooth.disable";
        String TETHER_BLUETOOTH = "global_tether_bluetooth.disable";
        if (WIFI_AP_SETTINGS.equals(tetherType)) {
            if (!new File(DMPROPERTY_DIR_PATH + GLOBAL_WIFI).exists()) {
                z = false;
            }
            return z;
        } else if (!ENABLE_BLUETOOTH_TETHERING.equals(tetherType)) {
            return false;
        } else {
            if (new File(DMPROPERTY_DIR_PATH + GLOBAL_BLUETOOTH).exists()) {
                return true;
            }
            if (!new File(DMPROPERTY_DIR_PATH + TETHER_BLUETOOTH).exists()) {
                z = false;
            }
            return z;
        }
    }

    public boolean hideSettingsUsbTether() {
        if (this.mTetherSettings == null || this.mTetherSettings.getContentResolver() == null) {
            return false;
        }
        return "true".equals(Systemex.getString(this.mTetherSettings.getContentResolver(), "hw_hide_usb_tether"));
    }

    public void custUsbTetherDisable(TwoStatePreference usbTether, CharSequence summary) {
        if (UtilsCustEx.IS_SPRINT && isUsbDataRestricted()) {
            usbTether.setSummary(summary);
            usbTether.setEnabled(false);
            usbTether.setChecked(false);
        }
    }

    private boolean isUsbDataRestricted() {
        if (new File("/data/OtaSave/Extensions/usb_data.disable").exists()) {
            return true;
        }
        return false;
    }

    public boolean isRemoveTethringSettings() {
        return isRemoveTetherPreference(ENABLE_BLUETOOTH_TETHERING) ? isRemoveTetherPreference(WIFI_AP_SETTINGS) : false;
    }
}
