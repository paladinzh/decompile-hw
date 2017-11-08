package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.HwInnerWifiManagerImpl;
import android.net.wifi.WifiManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.android.net.wifi.WifiManagerCommonEx;
import com.huawei.android.util.NoExtAPIException;

public class HwWifiSignalController extends WifiSignalController {
    private static final String TAG = HwWifiSignalController.class.getSimpleName();
    boolean mIsWifiCharged;
    boolean mIsWifiChargedLast;
    boolean mIsWifiNoInternet;
    boolean mIsWifiNoInternetLast;
    private boolean mWifiCloudEnable;
    private boolean mWifiCloudEnableLast;

    public HwWifiSignalController(Context context, boolean hasMobileData, CallbackHandler callbackHandler, NetworkControllerImpl networkController) {
        super(context, hasMobileData, callbackHandler, networkController);
    }

    public void handleBroadcastHuawei(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                updateWifiChargedState(this.mContext);
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                updateWifiChargedState(this.mContext);
                updateWifiNoInternetState((NetworkInfo) intent.getParcelableExtra("networkInfo"));
            } else if ("huawei.wifi.pro.INTERNET_ACCESS_CHANGE".equals(action)) {
                updateWifiNoInternetState(intent);
            }
            ((WifiState) this.mCurrentState).level = getHwWifiSignalStregth(((WifiState) this.mCurrentState).rssi);
            HwLog.i(TAG, "wifi handleBroadcast action:" + action + "wifi state:" + " mCurrentState.inetCondition:" + ((WifiState) this.mCurrentState).inetCondition + " mCurrentState.level:" + ((WifiState) this.mCurrentState).level + " mCurrentState.rssi:" + ((WifiState) this.mCurrentState).rssi + " mCurrentState.ssid:" + ((WifiState) this.mCurrentState).ssid + " mCurrentState.time:" + ((WifiState) this.mCurrentState).time + " mCurrentState.activityIn:" + ((WifiState) this.mCurrentState).activityIn + " mCurrentState.activityOut:" + ((WifiState) this.mCurrentState).activityOut + " mCurrentState.connected:" + ((WifiState) this.mCurrentState).connected + " mCurrentState.enabled:" + ((WifiState) this.mCurrentState).enabled + " mIsWifiCharged:" + this.mIsWifiCharged + " mIsWifiNoInternet:" + this.mIsWifiNoInternet);
        }
    }

    public void updateWifiChargedState(Context context) {
        this.mIsWifiCharged = HwInnerWifiManagerImpl.getDefault().getHwMeteredHint(this.mContext);
    }

    public void updateWifiNoInternetState(Intent intent) {
        if (intent == null) {
            HwLog.e(TAG, "updateWifiNoInternetState::intent is null, return!");
            return;
        }
        this.mIsWifiNoInternet = intent.getBooleanExtra("extra_wifipro_no_Internet", true);
        HwLog.i(TAG, "updateWifiNoInternetState::canAcessInterent:" + this.mIsWifiNoInternet);
    }

    public void updateWifiNoInternetState(NetworkInfo networkInfo) {
        if (networkInfo != null && DetailedState.CONNECTING == networkInfo.getDetailedState()) {
            this.mIsWifiNoInternet = false;
        }
    }

    public int getHwWifiSignalStregth(int wifiRssi) {
        try {
            return WifiManagerCommonEx.calculateSignalLevelHW(wifiRssi);
        } catch (NoExtAPIException e) {
            e.printStackTrace();
            return WifiManager.calculateSignalLevel(wifiRssi, WifiIcons.WIFI_LEVEL_COUNT);
        }
    }

    public IconGroup getIcons(IconGroup icons) {
        if (SystemUiUtil.isSupportVSim() && this.mWifiCloudEnable) {
            return HwTelephonyIcons.TIANJITONG_WIFI;
        }
        if (isWifiCharged()) {
            return HwTelephonyIcons.CHARGED_WIFI;
        }
        return icons;
    }

    protected IconGroup getIcons() {
        return getIcons(super.getIcons());
    }

    public boolean isWifiNoInternet() {
        return this.mIsWifiNoInternet;
    }

    public boolean isWifiCharged() {
        return this.mIsWifiCharged;
    }

    public boolean isDirty() {
        if (this.mIsWifiCharged == this.mIsWifiChargedLast && this.mIsWifiNoInternet == this.mIsWifiNoInternetLast && this.mWifiCloudEnable == this.mWifiCloudEnableLast) {
            return super.isDirty();
        }
        HwLog.i(TAG, "isDirty::mIsWifiChargedLast=" + this.mIsWifiChargedLast + ", mIsWifiNoInternetLast=" + this.mIsWifiNoInternetLast + ", mWifiCloudEnableLast=" + this.mWifiCloudEnableLast + ", mIsWifiCharged=" + this.mIsWifiCharged + ", mIsWifiNoInternet=" + this.mIsWifiNoInternet + ", mWifiCloudEnable=" + this.mWifiCloudEnable);
        this.mIsWifiChargedLast = this.mIsWifiCharged;
        this.mIsWifiNoInternetLast = this.mIsWifiNoInternet;
        this.mWifiCloudEnableLast = this.mWifiCloudEnable;
        return true;
    }

    public void updateWifiCloudState(Intent intent) {
        if (!SecurityCodeCheck.isValidIntentAndAction(intent)) {
            HwLog.e(TAG, "updateWifiState: null intent or null action");
        } else if (SystemUiUtil.isSupportVSim()) {
            HwLog.i(TAG, "updateWifiState: intent=" + Proguard.get(intent));
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                boolean wifiEnable = 3 == intent.getIntExtra("wifi_state", 4);
                HwLog.i(TAG, "updateWifiState::wifiEnable=" + wifiEnable);
                if (!wifiEnable) {
                    this.mWifiCloudEnable = false;
                }
            } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                boolean isConnected = networkInfo != null ? networkInfo.isConnected() : false;
                HwLog.i(TAG, "updateWifiState::wifiConnected=" + isConnected);
                if (!isConnected) {
                    this.mWifiCloudEnable = false;
                }
            } else if ("com.huawei.vsim.action.HW_WIFI_ICON_ENTER_ACTION".equals(action)) {
                this.mWifiCloudEnable = true;
            } else if ("com.huawei.vsim.action.HW_WIFI_ICON_EXIT_ACTION".equals(action)) {
                this.mWifiCloudEnable = false;
            }
        } else {
            HwLog.i(TAG, "updateWifiState: do not support VSim");
        }
    }
}
