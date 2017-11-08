package com.android.settings.deviceinfo;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.MSimTelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import com.android.settings.MLog;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.WirelessUtils;
import com.huawei.cust.HwCustUtils;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class MSimStatus extends SettingsPreferenceFragment {
    private static String sUnknown;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            MSimStatus.this.enableOrDisableNetworkStatus();
        }
    };
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                if (MSimStatus.this.mBatteryLevel != null) {
                    MSimStatus.this.mBatteryLevel.setSummary(Utils.getBatteryPercentage(intent));
                }
                if (MSimStatus.this.mBatteryStatus != null) {
                    MSimStatus.this.mBatteryStatus.setSummary(com.android.settingslib.Utils.getBatteryStatus(MSimStatus.this.getResources(), intent));
                }
            }
        }
    };
    private Preference mBatteryLevel;
    private Preference mBatteryStatus;
    private ConnectivityManager mCM;
    private Handler mHandler;
    private HwCustMSimStatus mHwCustMSimStatus = null;
    private Preference mNetwork;
    private Resources mRes;
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                MSimStatus.this.enableOrDisableNetworkStatus();
            }
        }
    };
    private Preference mUptime;

    private static class MyHandler extends Handler {
        private WeakReference<MSimStatus> mStatus;

        public MyHandler(MSimStatus activity) {
            this.mStatus = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            MSimStatus status = (MSimStatus) this.mStatus.get();
            if (status != null) {
                switch (msg.what) {
                    case 500:
                        status.updateTimes();
                        sendEmptyMessageDelayed(500, 1000);
                        break;
                }
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mHwCustMSimStatus = (HwCustMSimStatus) HwCustUtils.createObj(HwCustMSimStatus.class, new Object[]{this});
        this.mHandler = new MyHandler(this);
        this.mCM = (ConnectivityManager) getSystemService("connectivity");
        if (MSimTelephonyManager.getDefault().isMultiSimEnabled()) {
            addPreferencesFromResource(2131230817);
            if (Utils.isWifiOnly(getContext())) {
                removePreferenceFromScreen("network");
            } else {
                this.mNetwork = findPreference("network");
                enableOrDisableNetworkStatus();
            }
            this.mBatteryLevel = findPreference("battery_level");
            this.mBatteryStatus = findPreference("battery_status");
            PreferenceScreen selectSub = (PreferenceScreen) findPreference("button_aboutphone_msim_status");
            if (selectSub != null) {
                Intent intent = selectSub.getIntent();
                intent.putExtra("PACKAGE", "com.android.settings");
                intent.putExtra("TARGET_CLASS", "com.android.settings.deviceinfo.MSimSubscriptionStatus");
            }
            this.mRes = getResources();
            if (sUnknown == null) {
                sUnknown = this.mRes.getString(2131624355);
            }
            this.mUptime = findPreference("up_time");
            if (!(Utils.isWifiOnly(getContext()) || this.mHwCustMSimStatus == null)) {
                this.mHwCustMSimStatus.getAndShowMultiSimStatus();
            }
            setWifiStatus();
            setBtStatus();
            String serial = Build.SERIAL;
            if (serial == null || serial.equals("")) {
                removePreferenceFromScreen("serial_number");
            } else {
                setSummaryText("serial_number", serial.toUpperCase(Locale.US));
            }
            removePreferenceFromScreen("wimax_mac_address");
            if (this.mHwCustMSimStatus != null) {
                this.mHwCustMSimStatus.showHardwarePreference(getContext());
            }
            return;
        }
        MLog.i("MSimStatus", "MismStatus activity finished,this is a single card phone.");
        getActivity().finish();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onResume() {
        super.onResume();
        if (!Utils.isWifiOnly(getContext().getApplicationContext())) {
            getContext().registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
        getContext().registerReceiver(this.mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        this.mHandler.sendEmptyMessage(500);
        getContext().getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
    }

    public void onPause() {
        super.onPause();
        if (!Utils.isWifiOnly(getContext().getApplicationContext())) {
            getContext().unregisterReceiver(this.mSimStateReceiver);
        }
        getContext().unregisterReceiver(this.mBatteryInfoReceiver);
        this.mHandler.removeMessages(500);
        getContext().getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
    }

    protected int getMetricsCategory() {
        return 43;
    }

    private void enableOrDisableNetworkStatus() {
        if (this.mNetwork != null) {
            boolean enable;
            if (WirelessUtils.isAirplaneModeOn(getContext()) || !(Utils.isCardReady(0) || Utils.isCardReady(1))) {
                enable = false;
            } else {
                enable = Utils.isOwnerUser();
            }
            this.mNetwork.setEnabled(enable);
        }
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    private void setSummaryText(String preference, String text) {
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = sUnknown;
        }
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text2);
        }
    }

    private void setWifiStatus() {
        CharSequence string;
        String macAddress = null;
        WifiInfo wifiInfo = ((WifiManager) getContext().getApplicationContext().getSystemService("wifi")).getConnectionInfo();
        Preference wifiMacAddressPref = findPreference("wifi_mac_address");
        if (wifiInfo != null) {
            macAddress = wifiInfo.getMacAddress();
        }
        if (TextUtils.isEmpty(macAddress)) {
            string = getString(2131625250);
        } else {
            string = macAddress.toUpperCase(Locale.US);
        }
        wifiMacAddressPref.setSummary(string);
        Preference wifiIpAddressPref = findPreference("wifi_ip_address");
        CharSequence ipAddress = Utils.getDefaultIpAddresses(this.mCM);
        if (ipAddress != null) {
            wifiIpAddressPref.setSummary(ipAddress);
        } else {
            wifiIpAddressPref.setSummary(getResources().getString(2131625250));
        }
    }

    private void setBtStatus() {
        String address = null;
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        Preference btAddressPref = findPreference("bt_address");
        if (bluetooth == null) {
            getPreferenceScreen().removePreference(btAddressPref);
            return;
        }
        CharSequence string;
        if (bluetooth.isEnabled()) {
            address = bluetooth.getAddress();
        }
        if (TextUtils.isEmpty(address)) {
            string = getResources().getString(2131625250);
        } else {
            string = address.toUpperCase(Locale.US);
        }
        btAddressPref.setSummary(string);
    }

    void updateTimes() {
        long ut = SystemClock.elapsedRealtime() / 1000;
        if (ut == 0) {
            ut = 1;
        }
        if (this.mUptime != null) {
            this.mUptime.setSummary(DateUtils.formatElapsedTime(ut));
        }
    }
}
