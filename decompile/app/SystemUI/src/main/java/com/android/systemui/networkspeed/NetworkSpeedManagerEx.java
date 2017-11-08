package com.android.systemui.networkspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.INetworkManagementService.Stub;
import android.os.Parcelable;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.NumberLocationPercent;
import com.android.systemui.utils.SimCardMethod;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import fyusion.vislib.BuildConfig;
import java.math.BigDecimal;
import java.util.Locale;

public class NetworkSpeedManagerEx {
    private static final String TAG = NetworkSpeedManagerEx.class.getSimpleName();
    ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i(NetworkSpeedManagerEx.TAG, "onChange: airplane_mode_on, oldState=" + NetworkSpeedManagerEx.this.mIsAirplaneMode);
            NetworkSpeedManagerEx.this.initStateValue_CheckShowAndUpdate();
        }
    };
    private Callback mCallback;
    private Context mContext;
    private long mCurrentValue = 0;
    ContentObserver mDataSwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            NetworkSpeedManagerEx.this.mIsDataSwitchEnabled = NetworkSpeedManagerEx.this.isDataSwitchEnabled(NetworkSpeedManagerEx.this.mContext);
            Log.i(NetworkSpeedManagerEx.TAG, "MOBILE_DATA is changed, mIsDataSwitchEnabled:" + NetworkSpeedManagerEx.this.mIsDataSwitchEnabled);
            NetworkSpeedManagerEx.this.initStateValue_CheckShowAndUpdate();
        }
    };
    private Handler mHandler;
    private boolean mIsAirplaneMode;
    private boolean mIsDataSwitchEnabled;
    private boolean mIsFirst = true;
    private boolean mIsHasIccCard;
    private boolean mIsNetworkSpeedEnabled;
    private boolean mIsNetworkValid;
    private boolean mIsRegister = false;
    private boolean mIsStop = true;
    private boolean mIsVsimEnable;
    private boolean mIsWifiConnected;
    private long mLastValue = 0;
    private INetworkManagementService mNetworkManager = null;
    private BroadcastReceiver mReceiver;
    private Runnable mRunnable = new Runnable() {
        public void run() {
            Log.i(NetworkSpeedManagerEx.TAG, "mIsStop = " + NetworkSpeedManagerEx.this.mIsStop);
            if (!NetworkSpeedManagerEx.this.mIsStop) {
                new AsyncTask<Void, Void, Long>() {
                    protected Long doInBackground(Void... params) {
                        return Long.valueOf(NetworkSpeedManagerEx.this.getTetherStats());
                    }

                    protected void onPostExecute(Long result) {
                        NetworkSpeedManagerEx.this.refreshSpeed(result.longValue());
                        NetworkSpeedManagerEx.this.mHandler.postDelayed(NetworkSpeedManagerEx.this.mRunnable, 3000);
                    }
                }.execute(new Void[0]);
            }
        }
    };
    OnChangeListener mStateChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            NetworkSpeedManagerEx.this.mIsVsimEnable = NetworkSpeedManagerEx.this.isVSimEnable();
            NetworkSpeedManagerEx.this.checkShowAndUpdate();
        }
    };
    ContentObserver mSwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            NetworkSpeedManagerEx.this.mIsNetworkSpeedEnabled = NetworkSpeedManagerEx.this.isShowNetworkSpeedEnabled(NetworkSpeedManagerEx.this.mContext);
            Log.i(NetworkSpeedManagerEx.TAG, " KEY_SHOW_NETWORK_SPEED_ENABLED is changed, mIsNetworkSpeedEnabled:" + NetworkSpeedManagerEx.this.mIsNetworkSpeedEnabled);
            NetworkSpeedManagerEx.this.initStateValue_CheckShowAndUpdate();
        }
    };
    TelephonyManager mTelephonyManager;

    public interface Callback {
        void updateSpeed(String str);

        void updateVisibility(boolean z);
    }

    public void init(Context context, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        if (this.mContext != null && callback != null) {
            this.mTelephonyManager = TelephonyManager.from(context);
            this.mNetworkManager = Stub.asInterface(ServiceManager.getService("network_management"));
            if (this.mNetworkManager == null) {
                Log.e(TAG, "mNetworkManager = null");
                return;
            }
            this.mHandler = new Handler();
            createBroadcastReceiver();
            registerBroadcast();
            initStateValue_CheckShowAndUpdate();
        }
    }

    private void initStateValue() {
        this.mIsNetworkSpeedEnabled = isShowNetworkSpeedEnabled(this.mContext);
        this.mIsHasIccCard = SimCardMethod.hasIccCard(this.mTelephonyManager, this.mContext);
        this.mIsNetworkValid = isNetworkAvailable(this.mContext);
        this.mIsAirplaneMode = isAirplaneModeOn(this.mContext);
        this.mIsDataSwitchEnabled = isDataSwitchEnabled(this.mContext);
        this.mIsWifiConnected = isWifiConnected();
        this.mIsVsimEnable = isVSimEnable();
        HwLog.i(TAG, "mIsNetworkSpeedEnabled = " + this.mIsNetworkSpeedEnabled + ", mIsAirplaneMode = " + this.mIsAirplaneMode + ", mIsWifiConnected = " + this.mIsWifiConnected + ", mIsHasIccCard = " + this.mIsHasIccCard + ", mIsNetworkValid = " + this.mIsNetworkValid + ", mIsDataSwitchEnabled = " + this.mIsDataSwitchEnabled + ", mIsVsimEnable = " + this.mIsVsimEnable);
    }

    private void initStateValue_CheckShowAndUpdate() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                NetworkSpeedManagerEx.this.initStateValue();
                return true;
            }

            public void runInUI() {
                NetworkSpeedManagerEx.this.checkShowAndUpdate();
            }
        });
    }

    public boolean isShowNetworkSpeedEnabled(Context context) {
        try {
            if (SystemProperties.getBoolean("ro.config.hw_hideNetworkSpeed", false)) {
                return false;
            }
            return 1 == System.getIntForUser(context.getContentResolver(), "show_network_speed_enabled", UserSwitchUtils.getCurrentUser());
        } catch (SettingNotFoundException e) {
            setShowNetworkSpeedDisabled(context);
            return false;
        }
    }

    private static void setShowNetworkSpeedDisabled(Context context) {
        System.putIntForUser(context.getContentResolver(), "show_network_speed_enabled", 0, UserSwitchUtils.getCurrentUser());
    }

    private void registerBroadcast() {
        if (!this.mIsRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SIM_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mReceiver, filter);
            filter = new IntentFilter();
            filter.addAction("com.android.huawei.DATASERVICE_SETTING_CHANGED");
            filter.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiver(this.mReceiver, filter, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM", null);
            SystemUIObserver.getObserver(19).addOnChangeListener(this.mStateChangeListener);
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor("show_network_speed_enabled"), true, this.mSwitchObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, this.mDataSwitchObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
            this.mIsRegister = true;
        }
    }

    public void unRegister() {
        if (this.mIsRegister) {
            this.mIsRegister = false;
            if (this.mReceiver != null) {
                this.mContext.unregisterReceiver(this.mReceiver);
            }
            SystemUIObserver.getObserver(19).removeOnChangeListener(this.mStateChangeListener);
            this.mContext.getContentResolver().unregisterContentObserver(this.mSwitchObserver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mDataSwitchObserver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
        }
    }

    private void createBroadcastReceiver() {
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    SystemUIThread.runAsync(new SimpleAsyncTask() {
                        public boolean runInThread() {
                            String action = intent.getAction();
                            HwLog.i(NetworkSpeedManagerEx.TAG, "receive:" + action);
                            if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                                NetworkSpeedManagerEx.this.mIsDataSwitchEnabled = NetworkSpeedManagerEx.this.isDataSwitchEnabled(NetworkSpeedManagerEx.this.mContext);
                                NetworkSpeedManagerEx.this.mIsHasIccCard = SimCardMethod.hasIccCard(NetworkSpeedManagerEx.this.mTelephonyManager, NetworkSpeedManagerEx.this.mContext);
                            } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                                boolean z;
                                NetworkSpeedManagerEx networkSpeedManagerEx = NetworkSpeedManagerEx.this;
                                if (NetworkSpeedManagerEx.isNetworkAvailable(intent)) {
                                    z = true;
                                } else {
                                    z = NetworkSpeedManagerEx.isNetworkAvailable(NetworkSpeedManagerEx.this.mContext);
                                }
                                networkSpeedManagerEx.mIsNetworkValid = z;
                                NetworkSpeedManagerEx.this.mIsDataSwitchEnabled = NetworkSpeedManagerEx.this.isDataSwitchEnabled(context);
                            } else if ("com.android.huawei.DATASERVICE_SETTING_CHANGED".equals(action)) {
                                NetworkSpeedManagerEx.this.mIsDataSwitchEnabled = NetworkSpeedManagerEx.this.isDataSwitchEnabled(context);
                            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                                NetworkSpeedManagerEx.this.mIsWifiConnected = NetworkSpeedManagerEx.this.isWifiConnected();
                            } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
                                NetworkSpeedManagerEx.this.mIsNetworkSpeedEnabled = NetworkSpeedManagerEx.this.isShowNetworkSpeedEnabled(NetworkSpeedManagerEx.this.mContext);
                            }
                            return true;
                        }

                        public void runInUI() {
                            NetworkSpeedManagerEx.this.checkShowAndUpdate();
                        }
                    });
                }
            }
        };
    }

    private void start() {
        if (this.mCallback != null) {
            this.mCallback.updateVisibility(true);
        }
        if (this.mIsStop) {
            this.mIsStop = false;
            if (this.mHandler != null) {
                this.mHandler.removeCallbacks(this.mRunnable);
                this.mHandler.post(this.mRunnable);
            }
        }
    }

    private void stop() {
        this.mIsStop = true;
        this.mIsFirst = true;
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mRunnable);
        }
        if (this.mCallback != null) {
            this.mCallback.updateVisibility(false);
        }
        this.mLastValue = 0;
    }

    private void refreshSpeed(long currentValue) {
        this.mCurrentValue = currentValue;
        float value = Float.parseFloat(String.valueOf(Math.abs(this.mCurrentValue - this.mLastValue))) / 3.0f;
        if (value < 0.0f) {
            Log.w(TAG, " net speed invalid: value = " + value);
            return;
        }
        this.mLastValue = this.mCurrentValue;
        if (this.mIsFirst) {
            this.mIsFirst = false;
            value = 0.0f;
        }
        String textValue = this.mContext.getResources().getString(R.string.speed, new Object[]{formatFileSize(this.mContext, value)});
        Log.i(TAG, "value=" + value + "speed = " + textValue);
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive()) {
            if (this.mCallback != null) {
                this.mCallback.updateSpeed(textValue);
            } else {
                HwLog.i(TAG, "null == mCallback , error !!!");
            }
            return;
        }
        Log.i(TAG, "refreshSpeed: sreen off, no need to refresh net speed!");
    }

    private long getTetherStats() {
        try {
            long loTotalSize = TrafficStats.getRxBytes("lo") + TrafficStats.getTxBytes("lo");
            long statsSize = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) - loTotalSize;
            HwLog.i(TAG, "getTetherStats statsSize:" + statsSize + ", LOSize=" + loTotalSize);
            return statsSize;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return 0;
        } catch (Exception e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    private boolean isDataSwitchEnabled(Context context) {
        try {
            return TelephonyManager.from(context).getDataEnabled();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isAirplaneModeOn(Context context) {
        int airplaneMode = 0;
        try {
            airplaneMode = Global.getInt(context.getContentResolver(), "airplane_mode_on");
        } catch (SettingNotFoundException e) {
            Log.e(TAG, "isAirplaneModeOn throw AIRPLANE_MODE_ON SettingNotFoundException");
        }
        if (airplaneMode == 1) {
            return true;
        }
        return false;
    }

    private boolean isWifiConnected() {
        boolean z = false;
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            return false;
        }
        boolean enabled = wifiManager.isWifiEnabled();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (enabled && ipAddress != 0) {
            z = true;
        }
        return z;
    }

    private void checkShowAndUpdate() {
        Log.i(TAG, "mIsNetworkSpeedEnabled = " + this.mIsNetworkSpeedEnabled + ", mIsAirplaneMode = " + this.mIsAirplaneMode + ", mIsWifiConnected = " + this.mIsWifiConnected + ", mIsHasIccCard = " + this.mIsHasIccCard + ", mIsNetworkValid = " + this.mIsNetworkValid + ", mIsDataSwitchEnabled = " + this.mIsDataSwitchEnabled + ", mIsVsimEnable = " + this.mIsVsimEnable);
        if (!this.mIsNetworkSpeedEnabled) {
            stop();
        } else if (!this.mIsNetworkValid) {
            stop();
        } else if (!this.mIsWifiConnected && this.mIsAirplaneMode) {
            stop();
        } else if (!this.mIsWifiConnected && !this.mIsHasIccCard && !this.mIsVsimEnable) {
            stop();
        } else if (this.mIsWifiConnected || this.mIsDataSwitchEnabled || this.mIsVsimEnable) {
            start();
        } else {
            stop();
        }
    }

    private String formatFileSize(Context context, float number) {
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        String value;
        int newScale = 0;
        float result = number;
        int suffix = R.string.kilobyteShort;
        if (number <= 900.0f && number > 0.0f) {
            suffix = R.string.byteShort;
        }
        if (number > 900.0f) {
            suffix = R.string.kilobyteShort;
            result = number / 1024.0f;
            newScale = 1;
        }
        if (result > 900.0f) {
            suffix = R.string.megabyteShort;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.gigabyteShort;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.terabyteShort;
            result /= 1024.0f;
        }
        if (result > 900.0f) {
            suffix = R.string.petabyteShort;
            result /= 1024.0f;
        }
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (result == 0.0f) {
            value = getLocaleFormatString(0, locale);
        } else {
            try {
                float temp = new BigDecimal((double) result).setScale(newScale, 4).floatValue();
                if (temp % 1.0f == 0.0f) {
                    value = getLocaleFormatString((int) temp, locale);
                } else {
                    value = getLocaleFormatString(temp, locale);
                }
            } catch (Exception e) {
                e.printStackTrace();
                value = getLocaleFormatString(0, locale);
            }
        }
        return context.getResources().getString(R.string.fileSizeSuffix, new Object[]{value, context.getString(suffix)});
    }

    private String getLocaleFormatString(int value, Locale locale) {
        if (locale != null) {
            return NumberLocationPercent.getFormatnumberString(value, locale);
        }
        HwLog.w(TAG, "int::getLocaleFormatString::locale is null!");
        return NumberLocationPercent.getFormatnumberString(value);
    }

    private String getLocaleFormatString(float value, Locale locale) {
        if (locale != null) {
            return NumberLocationPercent.getFormatnumberString(value, locale);
        }
        HwLog.w(TAG, "float::getLocaleFormatString::locale is null!");
        return NumberLocationPercent.getFormatnumberString(value);
    }

    private static boolean isNetworkAvailable(Intent intent) {
        Parcelable netInfo = intent.getParcelableExtra("networkInfo");
        if (netInfo == null || !(netInfo instanceof NetworkInfo)) {
            HwLog.w(TAG, "isNetworkAvailable::netInfo or object type is not correct!");
            return false;
        }
        boolean isConnected = ((NetworkInfo) netInfo).isConnected();
        if (isConnected) {
            HwLog.i(TAG, "isNetworkAvailable::netInfo isConnected!");
        }
        return isConnected;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivity == null) {
            HwLog.w(TAG, "isNetworkAvailable::connectivity is null!");
            return false;
        }
        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info == null) {
            HwLog.w(TAG, "isNetworkAvailable::netInfo list is null!");
            return false;
        }
        int i = 0;
        while (i < info.length) {
            if (info[i] == null || !info[i].isConnected()) {
                i++;
            } else {
                HwLog.i(TAG, "isNetworkAvailable::info[i] isConnected!");
                return true;
            }
        }
        return false;
    }

    private boolean isVSimEnable() {
        if (!SystemUiUtil.isSupportVSim()) {
            return false;
        }
        if (((Integer) SystemUIObserver.get(19, Integer.valueOf(-1))).intValue() != -1) {
            return true;
        }
        return false;
    }
}
