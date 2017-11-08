package com.android.settings.wifi.cmcc;

import android.app.AlarmManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import com.android.settings.MLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SwitchToWifiUtils {
    private static volatile SwitchToWifiUtils mInstance;
    private Context mApplicationContext;
    private DisconnectedSSID mDisconnectedSSID;
    private int mHighestPriorityNetworkId = -1;
    private long mIgnoreTime;
    private MobileNework mMobileNework;
    private ScanAlarm mScanAlarm;
    private ScanEvent mScanEvent;
    private StateSaver mStateSaver;
    private ArrayList<ISwitchToWifi> mSwitchList;
    private UserDisconnectEvent mUserDisconnectEvent;
    private WifiConnection mWifiConnection;
    private WifiToWifi mWifiToWifi;
    private WifiWrapper mWifiWrapper;

    private interface ISwitchToWifi {
        boolean switchToWifi();
    }

    private class AutoSwitch implements ISwitchToWifi {
        private AutoSwitch() {
        }

        public boolean switchToWifi() {
            return SwitchToWifiUtils.this.mWifiWrapper.canSwitchToWifi(true);
        }
    }

    public static class Config {
        public static boolean isCMCC() {
            return "CMCC".equalsIgnoreCase(SystemProperties.get("ro.config.operators", ""));
        }

        public static int getSwitchToWifiType(Context context) {
            return System.getInt(context.getContentResolver(), "wifi_connect_type", 0);
        }

        public static boolean shouldNotShowDialog(Context context) {
            if (1 != System.getInt(context.getContentResolver(), "not_show_wifi_to_wifi_pop", 0)) {
                return false;
            }
            Log.d("WifiUtils", "do not show wifi to wifi pop");
            return true;
        }
    }

    private class DisconnectedSSID {
        private DisconnectedSSID() {
        }

        public void onWifiDisconnected() {
            SwitchToWifiUtils.this.mStateSaver.saveLong("ssid_disconnect_time", System.currentTimeMillis());
        }

        public void onWifiConnected(String ssid) {
            SwitchToWifiUtils.this.mStateSaver.saveString("last_ssid", ssid);
            SwitchToWifiUtils.this.mStateSaver.saveLong("ssid_disconnect_time", 0);
        }

        public String getRecentDisconnectedSSID() {
            if (isRecentDisconnected()) {
                return SwitchToWifiUtils.this.mStateSaver.queryString("last_ssid", null);
            }
            return null;
        }

        public void setRecentDisconnectedSSID(String ssid) {
            SwitchToWifiUtils.this.mStateSaver.saveString("last_ssid", ssid);
        }

        private boolean isRecentDisconnected() {
            if (System.currentTimeMillis() - SwitchToWifiUtils.this.mStateSaver.queryLong("ssid_disconnect_time", 0) < 10000) {
                return true;
            }
            return false;
        }
    }

    private interface OnPromptResultListener {
        void onPromptResult(boolean z, boolean z2);
    }

    private class ManualSwitch implements ISwitchToWifi, OnPromptResultListener {
        private ManualSwitch() {
        }

        public boolean switchToWifi() {
            if (!isPromptAllowed()) {
                MLog.v("WifiUtils", "Manual prompt is not allowed!");
                return false;
            } else if (Config.shouldNotShowDialog(SwitchToWifiUtils.this.mApplicationContext) || !SwitchToWifiUtils.this.mWifiWrapper.canSwitchToWifi(false)) {
                return false;
            } else {
                PromptDialog.show(SwitchToWifiUtils.this.mApplicationContext, SwitchToWifiUtils.this.mApplicationContext.getString(2131627527), this);
                return true;
            }
        }

        public void onPromptResult(boolean positive, boolean checked) {
            System.putInt(SwitchToWifiUtils.this.mApplicationContext.getContentResolver(), "not_show_wifi_to_wifi_pop", checked ? 1 : 0);
            if (positive) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                intent.setFlags(268435456);
                SwitchToWifiUtils.this.mApplicationContext.startActivity(intent);
            } else {
                SwitchToWifiUtils.this.mMobileNework.onCancelSwitchToWifi();
            }
            updatePromptTime(3600000);
        }

        private boolean isPromptAllowed() {
            long now = System.currentTimeMillis();
            long nextPromptTime = SwitchToWifiUtils.this.mStateSaver.queryLong("next_manual_prompt_time", 0);
            MLog.v("WifiUtils", "now = " + now + ", nextPromptTime = " + nextPromptTime);
            return now >= nextPromptTime;
        }

        private void updatePromptTime(long nextPromptInterval) {
            SwitchToWifiUtils.this.mStateSaver.saveLong("next_manual_prompt_time", System.currentTimeMillis() + nextPromptInterval);
        }
    }

    private class MobileNework {
        private AlarmManager mAlarmManager;
        private PendingIntent mPendingIntent;

        public MobileNework() {
            this.mAlarmManager = (AlarmManager) SwitchToWifiUtils.this.mApplicationContext.getSystemService("alarm");
            Intent intent = new Intent("com.android.settings.action.ALARM_SWITCH_TO_MOBILE_NETWORK");
            intent.setPackage("com.android.settings");
            this.mPendingIntent = PendingIntent.getBroadcast(SwitchToWifiUtils.this.mApplicationContext, 0, intent, 0);
        }

        public void setAlarm(long delayMillis) {
            this.mAlarmManager.setExact(0, System.currentTimeMillis() + delayMillis, this.mPendingIntent);
        }

        public void cancelAlarm() {
            this.mAlarmManager.cancel(this.mPendingIntent);
        }

        private void setSwitchFlag(boolean flag) {
            SwitchToWifiUtils.this.mStateSaver.saveBoolean("needSwitchToMoblie", flag);
        }

        private boolean getSwitchFlag() {
            return SwitchToWifiUtils.this.mStateSaver.queryBoolean("needSwitchToMoblie", false).booleanValue();
        }

        public void onWifiConnectionChanged(boolean isWifiConnectingOrConnected) {
            if (isWifiConnectingOrConnected) {
                setSwitchFlag(false);
                cancelAlarm();
                switchToMobileNetwork(false);
            } else if (SwitchToWifiUtils.this.mWifiWrapper.isWifiEnabled()) {
                SwitchToWifiUtils.this.mWifiWrapper.startScan();
                setSwitchFlag(true);
                if (Config.getSwitchToWifiType(SwitchToWifiUtils.this.mApplicationContext) == 0) {
                    onWifiDisconnected();
                }
            } else {
                cancelAlarm();
                switchToMobileNetwork(true);
            }
        }

        public void onChooseWifiFirst() {
            MLog.v("WifiUtils", "onChooseWifiFirst()");
            checkSwitchToMobileNetworkDelayed(20000);
        }

        public void onWifiConnecting() {
            MLog.v("WifiUtils", "onWifiConnecting()");
            checkSwitchToMobileNetworkDelayed(10000);
        }

        public void onWifiConnected() {
            MLog.v("WifiUtils", "onWifiConnected()");
            checkSwitchToMobileNetworkDelayed(0);
        }

        public void onWifiDisconnected() {
            MLog.v("WifiUtils", "onWifiDisconnected()");
            checkSwitchToMobileNetworkDelayed(5000);
        }

        private void onCancelSwitchToWifi() {
            MLog.v("WifiUtils", "onCancelSwitchToWifi()");
            checkSwitchToMobileNetworkDelayed(0);
        }

        private void checkSwitchToMobileNetworkDelayed(long delayMillis) {
            cancelAlarm();
            if (getSwitchFlag()) {
                setAlarm(delayMillis);
            }
        }

        private void checkSwitchToMobileNetwork() {
            if (getSwitchFlag()) {
                switchToMobileNetwork(true);
                setSwitchFlag(false);
            }
        }

        private void switchToMobileNetwork(boolean switchToNetwork) {
            MLog.i("WifiUtils", "switch to mobile network: " + switchToNetwork);
            Intent intent = new Intent("android.intent.action.SWITCH_TO_MOBILE_NETWORK");
            intent.putExtra("switch_state", switchToNetwork);
            SwitchToWifiUtils.this.mApplicationContext.sendBroadcast(intent, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
        }
    }

    private class NotificationSwitch implements ISwitchToWifi, OnPromptResultListener {
        private NotificationSwitch() {
        }

        public boolean switchToWifi() {
            if (!isPromptAllowed()) {
                MLog.v("WifiUtils", "Query prompt is not allowed!");
                return false;
            } else if (Config.shouldNotShowDialog(SwitchToWifiUtils.this.mApplicationContext) || !SwitchToWifiUtils.this.mWifiWrapper.canSwitchToWifi(false)) {
                return false;
            } else {
                String msg = createDialogMessage();
                if (msg == null || msg.length() == 0) {
                    return false;
                }
                PromptDialog.show(SwitchToWifiUtils.this.mApplicationContext, msg, this);
                return true;
            }
        }

        private String createDialogMessage() {
            String ssid = SwitchToWifiUtils.this.mWifiWrapper.getHighestPriorityApSsid();
            MLog.i("WifiUtils", "highest priority ap ssid: " + ssid);
            if (ssid == null) {
                return null;
            }
            String message;
            if (((ConnectivityManager) SwitchToWifiUtils.this.mApplicationContext.getSystemService("connectivity")).getMobileDataEnabled()) {
                message = SwitchToWifiUtils.this.mApplicationContext.getString(2131627526, new Object[]{ssid});
            } else {
                message = SwitchToWifiUtils.this.mApplicationContext.getString(2131627528, new Object[]{ssid});
            }
            return message;
        }

        public void onPromptResult(boolean positive, boolean checked) {
            System.putInt(SwitchToWifiUtils.this.mApplicationContext.getContentResolver(), "not_show_wifi_to_wifi_pop", checked ? 1 : 0);
            if (positive) {
                trySwitchToWifi();
                return;
            }
            updatePromptTime(3600000);
            SwitchToWifiUtils.this.mMobileNework.onCancelSwitchToWifi();
        }

        private boolean isPromptAllowed() {
            long now = System.currentTimeMillis();
            long nextPromptTime = SwitchToWifiUtils.this.mStateSaver.queryLong("next_query_prompt_time", 0);
            MLog.v("WifiUtils", "now = " + now + ", nextPromptTime = " + nextPromptTime);
            return now >= nextPromptTime;
        }

        private void trySwitchToWifi() {
            if (SwitchToWifiUtils.this.mWifiWrapper.canSwitchToWifi(false)) {
                SwitchToWifiUtils.this.onUserConnectEvent();
                SwitchToWifiUtils.this.mWifiWrapper.switchToWifi();
            }
        }

        private void updatePromptTime(long nextPromptInterval) {
            SwitchToWifiUtils.this.mStateSaver.saveLong("next_query_prompt_time", System.currentTimeMillis() + nextPromptInterval);
        }
    }

    private static class PromptDialog {
        private static boolean mIsShowing = false;
        private static boolean mPromptResult = false;

        private PromptDialog() {
        }

        public static void show(Context context, String message, final OnPromptResultListener listener) {
            if (!mIsShowing) {
                OnPromptResultListener promptResultListener = listener;
                mPromptResult = false;
                Builder builder = new Builder(context, 33947691);
                View view = View.inflate(builder.getContext(), 2130969286, null);
                builder.setView(view);
                final CheckBox cb = (CheckBox) view.findViewById(2131887597);
                builder.setMessage(message).setTitle(2131627350).setCancelable(false).setPositiveButton(2131625656, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PromptDialog.mPromptResult = true;
                    }
                }).setNegativeButton(2131625657, null).setOnDismissListener(new OnDismissListener() {
                    public void onDismiss(DialogInterface arg0) {
                        listener.onPromptResult(PromptDialog.mPromptResult, cb.isChecked());
                        PromptDialog.mIsShowing = false;
                    }
                });
                Dialog dialog = builder.create();
                dialog.getWindow().setType(2003);
                dialog.show();
                mIsShowing = true;
            }
        }
    }

    private static class ScanAlarm {
        private AlarmManager mAlarmManager;
        private PendingIntent mPendingIntent;
        private PowerManager mPowerManager;
        private long mSupplicantScanIntervalMs;

        public ScanAlarm(Context context) {
            this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
            this.mPowerManager = (PowerManager) context.getSystemService("power");
            Intent intent = new Intent("com.android.settings.action.START_SCAN_WIFI");
            intent.setPackage("com.android.settings");
            this.mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 268435456);
            int defaultScanInterval = context.getResources().getInteger(17694766);
            this.mSupplicantScanIntervalMs = Global.getLong(context.getContentResolver(), "wifi_supplicant_scan_interval_ms", (long) defaultScanInterval);
            MLog.i("WifiUtils", "Supplicant scan interval: " + this.mSupplicantScanIntervalMs + ", default scan interval: " + defaultScanInterval);
        }

        public void setAlarm() {
            MLog.v("WifiUtils", "set wifi scan alarm!");
            this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + (this.mPowerManager.isScreenOn() ? this.mSupplicantScanIntervalMs : 300000), this.mPendingIntent);
        }
    }

    private class ScanEvent {
        public ScanEvent() {
            SwitchToWifiUtils.this.mIgnoreTime = SwitchToWifiUtils.this.mStateSaver.queryLong("ignore_scan_event_time", 0);
        }

        public void onWifiConnecting() {
            setIgnoreScanEventTime(10000);
        }

        public void onWifiConnected() {
            setIgnoreScanEventTime(0);
        }

        public void onWifiDisconnected() {
            setIgnoreScanEventTime(10000);
        }

        public void onChooseWifiFirst() {
            setIgnoreScanEventTime(30000);
        }

        private void setIgnoreScanEventTime(long interval) {
            SwitchToWifiUtils.this.mIgnoreTime = System.currentTimeMillis() + interval;
            SwitchToWifiUtils.this.mStateSaver.saveLong("ignore_scan_event_time", SwitchToWifiUtils.this.mIgnoreTime);
        }

        private boolean shouldIgnoreScanEvent() {
            return System.currentTimeMillis() < SwitchToWifiUtils.this.mIgnoreTime;
        }
    }

    private static class Security {
        private Security() {
        }

        public static boolean isSameSecurity(WifiConfiguration config, ScanResult result) {
            boolean z = false;
            if (config == null || result == null) {
                return false;
            }
            if (getSecurity(config) == getSecurity(result)) {
                z = true;
            }
            return z;
        }

        private static int getSecurity(WifiConfiguration config) {
            if (config.allowedKeyManagement.get(1)) {
                return 2;
            }
            if (config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(3)) {
                return 3;
            }
            return config.wepKeys[0] != null ? 1 : 0;
        }

        private static int getSecurity(ScanResult result) {
            if (result.capabilities.contains("WEP")) {
                return 1;
            }
            if (result.capabilities.contains("PSK")) {
                return 2;
            }
            if (result.capabilities.contains("EAP")) {
                return 3;
            }
            return 0;
        }
    }

    private class StateSaver {
        private Editor mEditor = this.mSharedPreferences.edit();
        private SharedPreferences mSharedPreferences;

        public StateSaver(Context context) {
            this.mSharedPreferences = context.getSharedPreferences("wifi_switch_pref", 0);
        }

        public void saveLong(String key, long value) {
            MLog.d("WifiUtils", "save state: key = " + key + ", value = " + value);
            this.mEditor.putLong(key, value);
            this.mEditor.apply();
        }

        public long queryLong(String key, long defaultValue) {
            return this.mSharedPreferences.getLong(key, defaultValue);
        }

        public void saveString(String key, String value) {
            MLog.d("WifiUtils", "save state: key = " + key + ", value = " + value);
            this.mEditor.putString(key, value);
            this.mEditor.apply();
        }

        public String queryString(String key, String defaultValue) {
            return this.mSharedPreferences.getString(key, defaultValue);
        }

        public void saveBoolean(String key, boolean value) {
            this.mEditor.putBoolean(key, value);
            this.mEditor.apply();
        }

        public Boolean queryBoolean(String key, boolean defaultValue) {
            return Boolean.valueOf(this.mSharedPreferences.getBoolean(key, defaultValue));
        }
    }

    private class UserDisconnectEvent {
        private long mIgnoreDisconnectTime;

        public UserDisconnectEvent() {
            this.mIgnoreDisconnectTime = SwitchToWifiUtils.this.mStateSaver.queryLong("ignore_user_disconnect_time", 0);
        }

        public void onUserDisconnect() {
            setIgnoreTime(5000);
        }

        public void onWifiConnected() {
            setIgnoreTime(0);
        }

        private void setIgnoreTime(long interval) {
            this.mIgnoreDisconnectTime = System.currentTimeMillis() + interval;
            SwitchToWifiUtils.this.mStateSaver.saveLong("ignore_user_disconnect_time", this.mIgnoreDisconnectTime);
        }

        private boolean isUserDisconnectWifi() {
            return System.currentTimeMillis() < this.mIgnoreDisconnectTime;
        }
    }

    private final class WifiConnection {
        private AtomicBoolean mConnected = new AtomicBoolean(false);

        public WifiConnection(boolean isConnected) {
            update(isConnected);
        }

        public boolean update(boolean isConnected) {
            if (isConnected() == isConnected) {
                return false;
            }
            this.mConnected.set(isConnected);
            return true;
        }

        private boolean isConnected() {
            return this.mConnected.get();
        }
    }

    private class WifiToWifi implements ISwitchToWifi, OnPromptResultListener {
        private WifiToWifi() {
        }

        public boolean switchToWifi() {
            if (Config.shouldNotShowDialog(SwitchToWifiUtils.this.mApplicationContext) || Config.getSwitchToWifiType(SwitchToWifiUtils.this.mApplicationContext) == 0 || SwitchToWifiUtils.this.mUserDisconnectEvent.isUserDisconnectWifi() || !SwitchToWifiUtils.this.mWifiWrapper.canSwitchToWifi(false)) {
                return false;
            }
            PromptDialog.show(SwitchToWifiUtils.this.mApplicationContext, SwitchToWifiUtils.this.mApplicationContext.getString(2131627529), this);
            SwitchToWifiUtils.this.mScanEvent.onChooseWifiFirst();
            SwitchToWifiUtils.this.mMobileNework.onChooseWifiFirst();
            return true;
        }

        public void onPromptResult(boolean positive, boolean checked) {
            System.putInt(SwitchToWifiUtils.this.mApplicationContext.getContentResolver(), "not_show_wifi_to_wifi_pop", checked ? 1 : 0);
            if (positive) {
                Intent intent = new Intent("android.settings.WIFI_SETTINGS");
                intent.setFlags(268435456);
                SwitchToWifiUtils.this.mApplicationContext.startActivity(intent);
                SwitchToWifiUtils.this.mScanEvent.onChooseWifiFirst();
                SwitchToWifiUtils.this.mMobileNework.onChooseWifiFirst();
                return;
            }
            SwitchToWifiUtils.this.mMobileNework.onCancelSwitchToWifi();
        }
    }

    private class WifiWrapper {
        private ActionListener mConnectListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
            }
        };
        private WifiManager mWifiManager;

        public WifiWrapper() {
            this.mWifiManager = (WifiManager) SwitchToWifiUtils.this.mApplicationContext.getSystemService("wifi");
        }

        public boolean isWifiEnabled() {
            return this.mWifiManager.isWifiEnabled();
        }

        public boolean startScan() {
            return this.mWifiManager.startScan();
        }

        public boolean canSwitchToWifi(boolean includeRecentDisconnectedSSID) {
            boolean z;
            if (!this.mWifiManager.isWifiEnabled() || isWiFiConnected()) {
                z = false;
            } else {
                z = isWifiApAvailable(includeRecentDisconnectedSSID);
            }
            MLog.v("WifiUtils", "Can switch to wifi:" + z);
            return z;
        }

        public void switchToWifi() {
            if (SwitchToWifiUtils.this.mHighestPriorityNetworkId != -1) {
                this.mWifiManager.connect(SwitchToWifiUtils.this.mHighestPriorityNetworkId, this.mConnectListener);
                SwitchToWifiUtils.this.mHighestPriorityNetworkId = -1;
            }
        }

        public String getConnectedSSID() {
            WifiInfo info = this.mWifiManager.getConnectionInfo();
            if (info != null) {
                return info.getSSID();
            }
            return null;
        }

        public String getHighestPriorityApSsid() {
            String recentDisconnectSSID = WifiInfo.removeDoubleQuotes(SwitchToWifiUtils.this.mDisconnectedSSID.getRecentDisconnectedSSID());
            List<WifiConfiguration> networks = this.mWifiManager.getConfiguredNetworks();
            List<ScanResult> scanResults = this.mWifiManager.getScanResults();
            String highestPriorityNetworkSSID = null;
            int highestPriority = -1;
            if (!(networks == null || scanResults == null)) {
                for (WifiConfiguration network : networks) {
                    for (ScanResult scanresult : scanResults) {
                        if (network.SSID != null && scanresult.SSID != null && network.SSID.equals("\"" + scanresult.SSID + "\"") && Security.isSameSecurity(network, scanresult) && !scanresult.SSID.equals(recentDisconnectSSID) && network.priority > highestPriority) {
                            highestPriority = network.priority;
                            highestPriorityNetworkSSID = network.SSID;
                            SwitchToWifiUtils.this.mHighestPriorityNetworkId = network.networkId;
                        }
                    }
                }
            }
            return highestPriorityNetworkSSID;
        }

        public boolean isWifiApAvailable(boolean includeRecentDisconnectedSSID) {
            String recentDisconnectSSID = WifiInfo.removeDoubleQuotes(SwitchToWifiUtils.this.mDisconnectedSSID.getRecentDisconnectedSSID());
            List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
            List<ScanResult> results = this.mWifiManager.getScanResults();
            if (!(results == null || configs == null)) {
                for (ScanResult result : results) {
                    if (!(result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]"))) {
                        for (WifiConfiguration conf : configs) {
                            if (conf.SSID != null && result.SSID.equals(WifiInfo.removeDoubleQuotes(conf.SSID)) && Security.isSameSecurity(conf, result)) {
                                if (includeRecentDisconnectedSSID || !result.SSID.equals(recentDisconnectSSID)) {
                                    MLog.v("WifiUtils", "isWifiAvailable() return true :" + result.SSID);
                                    return true;
                                }
                                MLog.v("WifiUtils", "Recent disconnected ssid is ignored: " + recentDisconnectSSID);
                            }
                        }
                        continue;
                    }
                }
            }
            MLog.v("WifiUtils", "isWifiAvailable() return false");
            return false;
        }

        private boolean isWiFiConnected() {
            NetworkInfo info = ((ConnectivityManager) SwitchToWifiUtils.this.mApplicationContext.getSystemService("connectivity")).getNetworkInfo(1);
            if (info != null) {
                MLog.v("WifiUtils", "wifi NetworkInfo: " + info.toString());
                return info.isConnectedOrConnecting();
            }
            MLog.v("WifiUtils", "No wifi connection info");
            return false;
        }
    }

    private SwitchToWifiUtils(Context context) {
        this.mApplicationContext = context.getApplicationContext();
        this.mWifiWrapper = new WifiWrapper();
        this.mStateSaver = new StateSaver(this.mApplicationContext);
        this.mScanAlarm = new ScanAlarm(this.mApplicationContext);
        this.mMobileNework = new MobileNework();
        this.mUserDisconnectEvent = new UserDisconnectEvent();
        this.mScanEvent = new ScanEvent();
        this.mDisconnectedSSID = new DisconnectedSSID();
        if (this.mWifiWrapper.isWiFiConnected()) {
            this.mWifiConnection = new WifiConnection(true);
            this.mDisconnectedSSID.setRecentDisconnectedSSID(this.mWifiWrapper.getConnectedSSID());
        } else {
            this.mWifiConnection = new WifiConnection(false);
        }
        this.mSwitchList = new ArrayList();
        this.mSwitchList.add(0, new AutoSwitch());
        this.mSwitchList.add(1, new ManualSwitch());
        this.mSwitchList.add(2, new NotificationSwitch());
        this.mWifiToWifi = new WifiToWifi();
    }

    public static SwitchToWifiUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SwitchToWifiUtils(context);
        }
        return mInstance;
    }

    public void onUserConnectEvent() {
        this.mScanEvent.onWifiConnecting();
        this.mMobileNework.onWifiConnecting();
        this.mUserDisconnectEvent.onUserDisconnect();
        System.putInt(this.mApplicationContext.getContentResolver(), "wifi_ap_manual_connect", 1);
        Log.d("WifiUtils", "onUserConnectEvent manual connect");
    }

    public void onUserDisconnectEvent() {
        this.mUserDisconnectEvent.onUserDisconnect();
    }

    public void onUserForgetEvent() {
        this.mUserDisconnectEvent.onUserDisconnect();
    }

    public void onWifiStateChanged(Intent intent) {
        switch (intent.getIntExtra("wifi_state", 0)) {
            case 0:
            case 1:
                this.mDisconnectedSSID.setRecentDisconnectedSSID(null);
                return;
            default:
                return;
        }
    }

    public void onWifiNetworkStateChanged(NetworkInfo info) {
        if (info == null) {
            MLog.e("WifiUtils", "NetworkInfo is null!");
            return;
        }
        if (this.mWifiConnection.update(info.isConnected())) {
            State state = info.getState();
            if (state == State.DISCONNECTED) {
                MLog.i("WifiUtils", "Wifi is disconnected!");
                this.mDisconnectedSSID.onWifiDisconnected();
                this.mWifiToWifi.switchToWifi();
            } else if (state == State.CONNECTED) {
                String ssid = this.mWifiWrapper.getConnectedSSID();
                MLog.i("WifiUtils", ssid + " is connected!");
                this.mDisconnectedSSID.onWifiConnected(ssid);
                this.mUserDisconnectEvent.onWifiConnected();
                this.mScanEvent.onWifiConnected();
                this.mMobileNework.onWifiConnected();
            }
        }
    }

    public void onScanFinished() {
        if (Config.getSwitchToWifiType(this.mApplicationContext) != 0) {
            this.mScanAlarm.setAlarm();
        }
        if (!this.mScanEvent.shouldIgnoreScanEvent()) {
            if (!this.mWifiConnection.isConnected() || this.mWifiConnection.isConnected() == this.mWifiWrapper.isWiFiConnected()) {
                boolean result = ((ISwitchToWifi) this.mSwitchList.get(Config.getSwitchToWifiType(this.mApplicationContext))).switchToWifi();
                MLog.i("WifiUtils", "switch type:" + Config.getSwitchToWifiType(this.mApplicationContext) + " result:" + result);
                if (!(result || this.mWifiWrapper.isWiFiConnected())) {
                    this.mMobileNework.onCancelSwitchToWifi();
                }
                return;
            }
            MLog.i("WifiUtils", "Wifi state is changing, ignore scan result!");
        }
    }

    public void startScan() {
        this.mWifiWrapper.startScan();
    }

    public void handleSwitchToMoblie() {
        this.mMobileNework.checkSwitchToMobileNetwork();
    }

    public void onWifiConnectionChanged(boolean isConnectingOrConnected) {
        this.mMobileNework.onWifiConnectionChanged(isConnectingOrConnected);
    }

    public void updateSupplicantState(SupplicantState state) {
        if (!this.mWifiConnection.isConnected()) {
            if ((state != null && SupplicantState.isHandshakeState(state)) || SupplicantState.DISCONNECTED.equals(state)) {
                if (SupplicantState.DISCONNECTED.equals(state)) {
                    this.mScanEvent.onWifiDisconnected();
                    this.mMobileNework.onWifiDisconnected();
                    return;
                }
                this.mScanEvent.onWifiConnecting();
                this.mMobileNework.onWifiConnecting();
            }
        }
    }
}
