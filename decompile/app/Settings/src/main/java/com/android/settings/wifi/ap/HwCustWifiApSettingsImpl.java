package com.android.settings.wifi.ap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.support.v7.preference.TwoStatePreference;
import android.telephony.HwTelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.android.settings.MLog;
import com.android.settings.wifi.WifiApEnabler;
import com.huawei.android.provider.SettingsEx.Systemex;
import java.util.Locale;
import java.util.UUID;

public class HwCustWifiApSettingsImpl extends HwCustWifiApSettings {
    private static final String ATT_NAME = "ro.product.att.name";
    private static String First_ENABLE_WIFI_AP = "first_enable_wifi_ap";
    private static final String IS_AP_MODIFIED = "hw_is_ap_modified";
    private static final boolean IS_SHOW_WIFI_AP_WARNING = SystemProperties.getBoolean("ro.config.show_wifi_ap_warning", false);
    private static final String KEY_ENABLE_WIFI_AP = "enable_wifi_ap";
    private static final int MHS_REQUEST = 0;
    private static final String SET_SSID_NAME = "set_hotspot_ssid_name";
    private static final int STATE_IMS_DOMAIN_VOWIFI = 1;
    private static final String TAG = "HwCtWifiApSetImpl";
    private AlertDialog mDialog;
    private TwoStatePreference mEnableWifiAp;
    private WifiApEnabler mWifiApEnabler;
    private WifiApSettings mWifiApSettings;
    private boolean mWifiApState = false;

    public HwCustWifiApSettingsImpl(WifiApSettings wifiApSettings) {
        this.mWifiApSettings = wifiApSettings;
        updateCustPreference();
    }

    private void updateCustPreference() {
        if (this.mWifiApSettings != null) {
            this.mEnableWifiAp = (TwoStatePreference) this.mWifiApSettings.getPreferenceScreen().findPreference(KEY_ENABLE_WIFI_AP);
        }
    }

    public boolean showCustWifiApDialog(WifiApEnabler wifiApEnabler, boolean enable) {
        boolean show = false;
        this.mWifiApEnabler = wifiApEnabler;
        if (enable && isShowWifiApWarning()) {
            show = true;
            if (this.mDialog != null && this.mDialog.isShowing()) {
                return true;
            }
            showWifiApWarning();
        }
        return show;
    }

    private boolean isShowWifiApWarning() {
        if (!IS_SHOW_WIFI_AP_WARNING || System.getInt(this.mWifiApSettings.getContext().getContentResolver(), First_ENABLE_WIFI_AP, -1) >= 0) {
            return false;
        }
        return true;
    }

    private void showWifiApWarning() {
        final View layout = LayoutInflater.from(this.mWifiApSettings.getContext()).inflate(2130969280, null);
        AlertDialog dialog = new Builder(this.mWifiApSettings.getContext()).setCancelable(true).setView(layout).setPositiveButton(2131629126, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwCustWifiApSettingsImpl.this.mWifiApState = true;
                if (((CheckBox) layout.findViewById(2131887560)).isChecked()) {
                    System.putInt(HwCustWifiApSettingsImpl.this.mWifiApSettings.getContext().getContentResolver(), HwCustWifiApSettingsImpl.First_ENABLE_WIFI_AP, 1);
                } else {
                    System.putInt(HwCustWifiApSettingsImpl.this.mWifiApSettings.getContext().getContentResolver(), HwCustWifiApSettingsImpl.First_ENABLE_WIFI_AP, -1);
                }
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwCustWifiApSettingsImpl.this.setWifiApState(HwCustWifiApSettingsImpl.this.mWifiApState);
                HwCustWifiApSettingsImpl.this.mDialog = null;
            }
        }).setNegativeButton(2131629127, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwCustWifiApSettingsImpl.this.mWifiApState = false;
                dialog.dismiss();
            }
        }).create();
        this.mDialog = dialog;
        dialog.show();
    }

    private void setWifiApState(boolean wifiApState) {
        if (this.mWifiApState) {
            String[] appDetails = this.mWifiApSettings.getResources().getStringArray(17235992);
            if (appDetails.length == 2 || this.mWifiApSettings == null || this.mWifiApEnabler == null) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName(appDetails[0], appDetails[1]);
                this.mWifiApSettings.startActivityForResult(intent, 0);
                MLog.w(TAG, "Show hotspot provisioning first,see commit for config_mobile_hotspot_provision_app");
                return;
            }
            this.mWifiApEnabler.setSoftapEnabled(true);
            boolean isSLEntitleSetEnabled = isSLEntitleSet();
            if (this.mEnableWifiAp != null && !isSLEntitleSetEnabled) {
                this.mEnableWifiAp.setChecked(true);
            }
        } else if (this.mWifiApSettings != null && this.mWifiApEnabler != null) {
            this.mWifiApEnabler.setSoftapEnabled(false);
            if (this.mEnableWifiAp != null) {
                this.mEnableWifiAp.setChecked(false);
            }
        }
    }

    private boolean isSLEntitleSet() {
        boolean isSLEntitleSet = SystemProperties.getBoolean("ro.config.isSLEntitleSet", false);
        if (!isSLEntitleSet) {
            return isSLEntitleSet;
        }
        boolean isAttCard = isSLEntitleSet;
        String attPlmn = System.getString(this.mWifiApSettings.getContext().getContentResolver(), "hw_att_operator_numeric");
        String mccmnc = TelephonyManager.getDefault().getSimOperatorNumeric(SubscriptionManager.getDefaultSubscriptionId());
        if (!TextUtils.isEmpty(attPlmn) && !TextUtils.isEmpty(mccmnc)) {
            isAttCard = false;
            String[] custList = attPlmn.split(";");
            for (String equals : custList) {
                isAttCard = equals.equals(mccmnc);
                if (isAttCard) {
                    break;
                }
            }
        }
        return isAttCard;
    }

    public void custWifiConfiguration(WifiConfiguration config) {
        if (1 == Systemex.getInt(this.mWifiApSettings.getContext().getContentResolver(), SET_SSID_NAME, 0) && config != null) {
            if (SystemProperties.getBoolean("ro.config.ap_us_channel", false)) {
                getCustWiFiConfigForUsChannel(config);
                return;
            }
            WifiManager wifiManager = (WifiManager) this.mWifiApSettings.getContext().getSystemService("wifi");
            String phoneModel = SystemProperties.get(ATT_NAME);
            String randStr = UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.US);
            if (SystemProperties.getBoolean("ro.config.6bit_random_ssid", false)) {
                phoneModel = Build.PRODUCT;
                config.SSID = getAppendSsidWithRandomUuid(phoneModel);
            } else {
                config.SSID = phoneModel + "_" + randStr;
            }
            if (SystemProperties.getBoolean("ro.config.ap_with_blank", false)) {
                config.SSID = phoneModel + " " + randStr;
                config.preSharedKey = UUID.randomUUID().toString().substring(0, 8);
            }
            String custHuaweiWifiAP = Systemex.getString(this.mWifiApSettings.getContext().getContentResolver(), "hw_cust_personal_huawei_ap");
            if (!TextUtils.isEmpty(custHuaweiWifiAP)) {
                config.SSID = custHuaweiWifiAP;
            }
            wifiManager.setWifiApConfiguration(config);
            Systemex.putInt(this.mWifiApSettings.getContext().getContentResolver(), SET_SSID_NAME, 0);
        }
    }

    public void compareWithLastWifiApConfig(WifiConfiguration config) {
        WifiConfiguration lastConfig = ((WifiManager) this.mWifiApSettings.getContext().getSystemService("wifi")).getWifiApConfiguration();
        if (SystemProperties.getBoolean("ro.config.hw_cota", false) && config != null && lastConfig != null && lastConfig.allowedKeyManagement != null && lastConfig.preSharedKey != null) {
            if (!(lastConfig.SSID.equals(config.SSID) && lastConfig.allowedKeyManagement.equals(config.allowedKeyManagement) && lastConfig.preSharedKey.equals(config.preSharedKey))) {
                Systemex.putInt(this.mWifiApSettings.getContext().getContentResolver(), IS_AP_MODIFIED, 1);
            }
        }
    }

    private String getAppendSsidWithRandomUuid(String productName) {
        return productName + "-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void getCustWiFiConfigForUsChannel(WifiConfiguration config) {
        WifiManager wifiManager = (WifiManager) this.mWifiApSettings.getContext().getSystemService("wifi");
        String custApName = Systemex.getString(this.mWifiApSettings.getContext().getContentResolver(), "hw_devicenameandimei_custap");
        String randStr = UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.US);
        if (!TextUtils.isEmpty(custApName)) {
            config.SSID = custApName + "_" + randStr;
        }
        config.preSharedKey = UUID.randomUUID().toString().substring(0, 8);
        wifiManager.setWifiApConfiguration(config);
        Systemex.putInt(this.mWifiApSettings.getContext().getContentResolver(), SET_SSID_NAME, 0);
    }

    public void showWifiApNotification() {
        int imsDomain = HwTelephonyManager.getDefault().getImsDomain();
        boolean isShow = SystemProperties.getBoolean("ro.config.hw_is_ee_show_n", false);
        MLog.w(TAG, "ro.config.hw_is_ee_show_n = " + isShow);
        MLog.w(TAG, "imsDomain = " + imsDomain);
        if (isShow && 1 == imsDomain) {
            Context mContext = this.mWifiApSettings.getContext();
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService("notification");
            Notification n = new Notification.Builder(mContext).setContentTitle(mContext.getResources().getString(2131628934)).setContentText(mContext.getResources().getString(2131628935)).setVisibility(0).setWhen(System.currentTimeMillis()).setSmallIcon(2130838302).setAutoCancel(true).build();
            n.flags |= 1;
            n.priority = 1;
            n.defaults |= 2;
            mNotificationManager.notify(2130838302, n);
        }
    }
}
