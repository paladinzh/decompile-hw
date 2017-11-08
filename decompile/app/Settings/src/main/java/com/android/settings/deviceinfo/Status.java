package com.android.settings.deviceinfo;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CellBroadcastMessage;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.internal.util.ArrayUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.huawei.cust.HwCustUtils;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class Status extends SettingsPreferenceFragment {
    private static final String[] CONNECTIVITY_INTENTS = new String[]{"android.bluetooth.adapter.action.STATE_CHANGED", "android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.LINK_CONFIGURATION_CHANGED", "android.net.wifi.STATE_CHANGE"};
    private static final String[] PHONE_RELATED_ENTRIES = new String[]{"data_state", "service_state", "operator_name", "roaming_state", "network_type", "latest_area_info", "number", "imei_sv", "prl_version", "min_number", "signal_strength", "icc_id"};
    private BroadcastReceiver mAreaInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED".equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    CellBroadcastMessage cbMessage = (CellBroadcastMessage) extras.get("message");
                    if (cbMessage != null && cbMessage.getServiceCategory() == 50) {
                        Status.this.updateAreaInfo(cbMessage.getMessageBody());
                    }
                }
            }
        }
    };
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                Status.this.mBatteryLevel.setSummary(Utils.getBatteryPercentage(intent));
                Status.this.mBatteryStatus.setSummary(com.android.settingslib.Utils.getBatteryStatus(Status.this.getResources(), intent));
            }
        }
    };
    private Preference mBatteryLevel;
    private Preference mBatteryStatus;
    private Preference mBtAddress;
    private ConnectivityManager mCM;
    private IntentFilter mConnectivityIntentFilter;
    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ArrayUtils.contains(Status.CONNECTIVITY_INTENTS, intent.getAction())) {
                Status.this.mHandler.sendEmptyMessage(600);
            }
        }
    };
    private BroadcastReceiver mGetCAstate = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.LTE_CA_STATE".equals(intent.getAction())) {
                Status.this.mIsCAstate = intent.getBooleanExtra("LteCAstate", false);
                Status.this.mHandler.sendEmptyMessage(700);
            }
        }
    };
    private Handler mHandler;
    private HwCustStatus mHwCustStatus;
    private Preference mIpAddress;
    private boolean mIsCAstate = false;
    private Phone mPhone = null;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onDataConnectionStateChanged(int state) {
            Status.this.updateDataState();
            Status.this.updateNetworkType();
        }
    };
    private PhoneStateIntentReceiver mPhoneStateReceiver;
    private Resources mRes;
    private boolean mShowLatestAreaInfo;
    private Preference mSignalStrength;
    private TelephonyManager mTelephonyManager;
    private String mUnavailable;
    private String mUnknown;
    private UpdateNumerReceiver mUpdateNumerReceiver = new UpdateNumerReceiver();
    private Preference mUptime;
    private Preference mWifiMacAddress;
    private WifiManager mWifiManager;
    private Preference mWimaxMacAddress;

    private static class MyHandler extends Handler {
        private WeakReference<Status> mStatus;

        public MyHandler(Status activity) {
            this.mStatus = new WeakReference(activity);
        }

        public void handleMessage(Message msg) {
            Status status = (Status) this.mStatus.get();
            if (status != null) {
                switch (msg.what) {
                    case 200:
                        Log.i("SimStatus", "updateSignalStrength:");
                        status.updateSignalStrength();
                        break;
                    case 300:
                        status.updateServiceState(status.mPhoneStateReceiver.getServiceState());
                        Log.i("SimStatus", "updateServiceState");
                        break;
                    case 500:
                        status.updateTimes();
                        sendEmptyMessageDelayed(500, 1000);
                        Log.i("SimStatus", "updatestatus");
                        break;
                    case 600:
                        status.updateConnectivity();
                        Log.i("SimStatus", "updateConnectivity");
                        break;
                    case 700:
                        status.updateNetworkType();
                        Log.i("SimStatus", "updateNetworkType");
                        break;
                }
            }
        }
    }

    private class UpdateNumerReceiver extends BroadcastReceiver {
        private UpdateNumerReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    String rawNumber = Status.this.mTelephonyManager.getLine1Number();
                    String formattedNumber = null;
                    if (!TextUtils.isEmpty(rawNumber)) {
                        formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
                    }
                    Status.this.setSummaryText("number", formattedNumber);
                }
            }
        }
    }

    private boolean hasBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    private boolean hasWimax() {
        return this.mCM.getNetworkInfo(6) != null;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mHwCustStatus = (HwCustStatus) HwCustUtils.createObj(HwCustStatus.class, new Object[]{this});
        this.mHandler = new MyHandler(this);
        this.mCM = (ConnectivityManager) getSystemService("connectivity");
        this.mTelephonyManager = (TelephonyManager) getContext().getSystemService("phone");
        this.mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService("wifi");
        addPreferencesFromResource(2131230774);
        this.mBatteryLevel = findPreference("battery_level");
        this.mBatteryStatus = findPreference("battery_status");
        this.mBtAddress = findPreference("bt_address");
        this.mWifiMacAddress = findPreference("wifi_mac_address");
        this.mWimaxMacAddress = findPreference("wimax_mac_address");
        this.mIpAddress = findPreference("wifi_ip_address");
        disPlayIms();
        this.mRes = getResources();
        this.mUnknown = this.mRes.getString(2131624355);
        this.mUnavailable = this.mRes.getString(2131625250);
        if (UserManager.get(getContext()).isAdminUser()) {
            try {
                PhoneFactory.makeDefaultPhones(getContext());
                this.mPhone = PhoneFactory.getDefaultPhone();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mSignalStrength = findPreference("signal_strength");
        this.mUptime = findPreference("up_time");
        if (this.mPhone == null || Utils.isWifiOnly(getContext().getApplicationContext())) {
            for (String key : PHONE_RELATED_ENTRIES) {
                removePreferenceFromScreen(key);
            }
        } else {
            boolean iccidDisabled = System.getInt(getContext().getContentResolver(), "hw_iccid_enabled", 0) != 1;
            if (this.mPhone.getPhoneName().equals("CDMA")) {
                setSummaryText("min_number", this.mPhone.getCdmaMin());
                if (getResources().getBoolean(2131492917)) {
                    findPreference("min_number").setTitle(2131625236);
                }
                setSummaryText("prl_version", this.mPhone.getCdmaPrlVersion());
                removePreferenceFromScreen("imei_sv");
                if (this.mPhone.getLteOnCdmaMode() == 1) {
                    setSummaryText("icc_id", this.mPhone.getIccSerialNumber());
                } else if (iccidDisabled) {
                    removePreferenceFromScreen("icc_id");
                } else {
                    setSummaryText("icc_id", this.mPhone.getIccSerialNumber());
                }
            } else {
                if (this.mHwCustStatus == null || !this.mHwCustStatus.isIMEISVShowTwo(this.mTelephonyManager)) {
                    setSummaryText("imei_sv", ((TelephonyManager) getSystemService("phone")).getDeviceSoftwareVersion());
                } else {
                    setSummaryText("imei_sv", this.mHwCustStatus.getIMEISVSummaryText());
                }
                removePreferenceFromScreen("prl_version");
                removePreferenceFromScreen("min_number");
                if (iccidDisabled) {
                    removePreferenceFromScreen("icc_id");
                } else {
                    setSummaryText("icc_id", this.mPhone.getIccSerialNumber());
                }
                if ("br".equals(this.mTelephonyManager.getSimCountryIso())) {
                    this.mShowLatestAreaInfo = true;
                }
            }
            String rawNumber = this.mTelephonyManager.getLine1Number();
            String formattedNumber = null;
            if (!TextUtils.isEmpty(rawNumber)) {
                formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
            }
            setSummaryText("number", formattedNumber);
            this.mPhoneStateReceiver = new PhoneStateIntentReceiver(getContext(), this.mHandler);
            this.mPhoneStateReceiver.notifySignalStrength(200);
            this.mPhoneStateReceiver.notifyServiceState(300);
            if (!this.mShowLatestAreaInfo) {
                removePreferenceFromScreen("latest_area_info");
            }
        }
        if (this.mPhone == null) {
            setSummaryText("number", null);
            this.mPhoneStateReceiver = new PhoneStateIntentReceiver(getContext(), this.mHandler);
            this.mPhoneStateReceiver.notifySignalStrength(200);
            this.mPhoneStateReceiver.notifyServiceState(300);
        }
        if (!hasBluetooth()) {
            getPreferenceScreen().removePreference(this.mBtAddress);
            this.mBtAddress = null;
        }
        if (!hasWimax()) {
            getPreferenceScreen().removePreference(this.mWimaxMacAddress);
            this.mWimaxMacAddress = null;
        }
        this.mConnectivityIntentFilter = new IntentFilter();
        for (String intent : CONNECTIVITY_INTENTS) {
            this.mConnectivityIntentFilter.addAction(intent);
        }
        updateConnectivity();
        String serial = Build.SERIAL;
        if (serial == null || serial.equals("")) {
            removePreferenceFromScreen("serial_number");
        } else {
            setSummaryText("serial_number", serial.toUpperCase(Locale.US));
        }
        if (!UserManager.get(getContext()).isAdminUser() || Utils.isWifiOnly(getContext())) {
            removePreferenceFromScreen("sim_status");
            removePreferenceFromScreen("imei_info");
        }
        StatusHwBase.initExtralPreferences(this);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void disPlayIms() {
        if (this.mHwCustStatus != null && this.mHwCustStatus.isDisplayIms()) {
            Preference parentPreference = findPreference("device_info_status");
            if (parentPreference instanceof PreferenceScreen) {
                this.mHwCustStatus.addImsPreference(getContext().getApplicationContext(), (PreferenceScreen) parentPreference);
            }
        }
    }

    protected int getMetricsCategory() {
        return 44;
    }

    public void onResume() {
        super.onResume();
        if (!(this.mPhone == null || Utils.isWifiOnly(getContext().getApplicationContext()))) {
            this.mIsCAstate = getContext().getApplicationContext().getSharedPreferences("caStatePreferences", 0).getBoolean("isCAstate", false);
            this.mPhoneStateReceiver.registerIntent();
            updateSignalStrength();
            updateServiceState(this.mPhone.getServiceState());
            updateDataState();
            this.mTelephonyManager.listen(this.mPhoneStateListener, 64);
            if (this.mShowLatestAreaInfo) {
                getContext().registerReceiver(this.mAreaInfoReceiver, new IntentFilter("android.cellbroadcastreceiver.CB_AREA_INFO_RECEIVED"), "android.permission.RECEIVE_EMERGENCY_BROADCAST", null);
                getContext().sendBroadcastAsUser(new Intent("android.cellbroadcastreceiver.GET_LATEST_CB_AREA_INFO"), UserHandle.ALL, "android.permission.RECEIVE_EMERGENCY_BROADCAST");
            }
        }
        getContext().registerReceiver(this.mConnectivityReceiver, this.mConnectivityIntentFilter, "android.permission.CHANGE_NETWORK_STATE", null);
        getContext().registerReceiver(this.mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        getContext().registerReceiver(this.mGetCAstate, new IntentFilter("android.intent.action.LTE_CA_STATE"));
        if (!(!Utils.isTablet() || Utils.isCDMAPhone() || Utils.isWifiOnly(getContext()))) {
            getContext().registerReceiver(this.mUpdateNumerReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        }
        this.mHandler.sendEmptyMessage(500);
    }

    public void onPause() {
        super.onPause();
        if (!(this.mPhone == null || Utils.isWifiOnly(getContext().getApplicationContext()))) {
            this.mPhoneStateReceiver.unregisterIntent();
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        }
        if (this.mShowLatestAreaInfo) {
            getContext().unregisterReceiver(this.mAreaInfoReceiver);
        }
        getContext().unregisterReceiver(this.mBatteryInfoReceiver);
        getContext().unregisterReceiver(this.mConnectivityReceiver);
        getContext().unregisterReceiver(this.mGetCAstate);
        if (!(!Utils.isTablet() || Utils.isCDMAPhone() || Utils.isWifiOnly(getContext()))) {
            getContext().unregisterReceiver(this.mUpdateNumerReceiver);
        }
        this.mHandler.removeMessages(500);
    }

    private void removePreferenceFromScreen(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            getPreferenceScreen().removePreference(pref);
        }
    }

    private void setSummaryText(String preference, String text) {
        if (TextUtils.isEmpty(text)) {
            CharSequence text2 = this.mUnknown;
            if ("operator_name".equals(preference)) {
                text2 = getString(2131628161);
            }
        }
        if (findPreference(preference) != null) {
            findPreference(preference).setSummary(text2);
        }
    }

    private void updateNetworkType() {
        String str = null;
        if (this.mTelephonyManager.getNetworkType() != 0) {
            str = this.mTelephonyManager.getNetworkTypeName();
            if ("UNKNOWN".equals(str)) {
                str = null;
            }
        }
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (!TelephonyManager.getDefault().hasIccCard()) {
            str = null;
        }
        if (this.mHwCustStatus != null) {
            str = this.mHwCustStatus.getCustomNetworkType(str, this.mTelephonyManager, this.mIsCAstate);
        }
        setSummaryText("network_type", str);
    }

    private void updateDataState() {
        int state = this.mTelephonyManager.getDataState();
        String display = this.mRes.getString(2131624395);
        switch (state) {
            case 0:
                display = this.mRes.getString(2131624391);
                break;
            case 1:
                display = this.mRes.getString(2131624392);
                break;
            case 2:
                display = this.mRes.getString(2131624393);
                break;
            case 3:
                display = this.mRes.getString(2131624394);
                break;
        }
        setSummaryText("data_state", display);
    }

    private void updateServiceState(ServiceState serviceState) {
        int state = serviceState.getState();
        String display = this.mRes.getString(2131624395);
        switch (state) {
            case 0:
                display = this.mRes.getString(2131624382);
                break;
            case 1:
            case 2:
                display = this.mRes.getString(2131624383);
                break;
            case 3:
                display = this.mRes.getString(2131624385);
                break;
        }
        setSummaryText("service_state", display);
        if (!serviceState.getRoaming() || this.mHwCustStatus == null || this.mHwCustStatus.isHideRoaming()) {
            setSummaryText("roaming_state", this.mRes.getString(2131624387));
        } else {
            setSummaryText("roaming_state", this.mRes.getString(2131624386));
        }
        String operatorName = "";
        if (this.mPhone != null) {
            operatorName = TelephonyManager.getDefault().getNetworkOperatorName(this.mPhone.getPhoneId());
        }
        if (this.mHwCustStatus != null) {
            operatorName = this.mHwCustStatus.getCustOperatorName(operatorName, serviceState);
        }
        setSummaryText("operator_name", operatorName);
    }

    private void updateAreaInfo(String areaInfo) {
        if (areaInfo != null) {
            setSummaryText("latest_area_info", areaInfo);
        }
    }

    void updateSignalStrength() {
        if (this.mSignalStrength != null) {
            if (isAdded()) {
                Resources r = getResources();
                ServiceState serviceState = this.mPhoneStateReceiver.getServiceState();
                int voiceState = serviceState.getState();
                boolean isOutOfVoiceService = false;
                if (1 == voiceState || 3 == voiceState) {
                    isOutOfVoiceService = true;
                    Log.i("SimStatus", "VoiceServiceState is out of service:" + voiceState);
                }
                int dataState = serviceState.getDataRegState();
                boolean isOutOfDataService = false;
                if (1 == dataState || 3 == dataState) {
                    isOutOfDataService = true;
                    Log.i("SimStatus", "DataService is out of service:" + dataState);
                }
                if (isOutOfDataService && isOutOfVoiceService) {
                    Log.i("SimStatus", "ServiceState is out of service:");
                    this.mSignalStrength.setSummary((CharSequence) "0");
                    return;
                }
                int signalDbm = this.mPhoneStateReceiver.getSignalStrengthDbm();
                if (-1 == signalDbm) {
                    signalDbm = 0;
                }
                int signalAsu = this.mPhoneStateReceiver.getSignalStrengthLevelAsu();
                if (-1 == signalAsu) {
                    signalAsu = 0;
                }
                CharSequence signal = String.valueOf(signalDbm) + " " + r.getString(2131624398) + "   " + String.valueOf(signalAsu) + " " + r.getString(2131624399);
                Log.i("SimStatus", "signal is:" + signal);
                this.mSignalStrength.setSummary(signal);
            } else {
                Log.w("SimStatus", " updateSignalStrength() Status fragment not attached to Activity.");
            }
        }
    }

    private void setWimaxStatus() {
        if (this.mWimaxMacAddress != null) {
            this.mWimaxMacAddress.setSummary(SystemProperties.get("net.wimax.mac.address", this.mUnavailable));
        }
    }

    private void setWifiStatus() {
        CharSequence charSequence;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        Object macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        Preference preference = this.mWifiMacAddress;
        if (TextUtils.isEmpty(macAddress)) {
            charSequence = this.mUnavailable;
        } else {
            charSequence = macAddress.toUpperCase(Locale.US);
        }
        preference.setSummary(charSequence);
    }

    private void setIpAddressStatus() {
        CharSequence ipAddress = Utils.getDefaultIpAddresses(this.mCM);
        if (ipAddress != null) {
            this.mIpAddress.setSummary(ipAddress);
        } else {
            this.mIpAddress.setSummary(this.mUnavailable);
        }
    }

    private void setBtStatus() {
        String address = null;
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth != null && this.mBtAddress != null) {
            if (bluetooth.isEnabled()) {
                address = bluetooth.getAddress();
            }
            if (TextUtils.isEmpty(address)) {
                this.mBtAddress.setSummary(this.mUnavailable);
            } else {
                this.mBtAddress.setSummary(address.toUpperCase());
            }
        }
    }

    void updateConnectivity() {
        setWimaxStatus();
        setWifiStatus();
        setBtStatus();
        setIpAddressStatus();
    }

    void updateTimes() {
        long ut = SystemClock.elapsedRealtime() / 1000;
        if (ut == 0) {
            ut = 1;
        }
        this.mUptime.setSummary(convertToLocale(ut));
    }

    private String convertToLocale(long t) {
        return DateUtils.formatElapsedTime(t);
    }
}
