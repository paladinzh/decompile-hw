package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.android.internal.util.AsyncChannel;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkController.IconState;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.systemui.utils.SystemUIIdleHandler;
import java.util.Objects;

public abstract class WifiSignalController extends SignalController<WifiState, IconGroup> {
    protected Context mContext;
    private final boolean mHasMobileData;
    private final AsyncChannel mWifiChannel;
    private final WifiManager mWifiManager;
    private final WifiStatusTracker mWifiTracker = new WifiStatusTracker(this.mWifiManager);

    static class HwWifiStateBase extends State {
        boolean isCharged;
        boolean wifiNoInternet;

        HwWifiStateBase() {
        }

        public void copyFrom(State s) {
            super.copyFrom(s);
            HwWifiStateBase state = (HwWifiStateBase) s;
            this.isCharged = state.isCharged;
            this.wifiNoInternet = state.wifiNoInternet;
        }

        protected void toString(StringBuilder builder) {
            super.toString(builder);
            builder.append(",isChaged:").append(this.isCharged).append(",wifiNoInternet=").append(this.wifiNoInternet);
        }

        public boolean equals(Object o) {
            if (super.equals(o) && Objects.equals(Boolean.valueOf(((WifiState) o).isCharged), Boolean.valueOf(this.isCharged))) {
                return Objects.equals(Boolean.valueOf(((WifiState) o).wifiNoInternet), Boolean.valueOf(this.wifiNoInternet));
            }
            return false;
        }
    }

    private class WifiHandler extends Handler {
        private WifiHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    WifiSignalController.this.setActivity(msg.arg1);
                    return;
                case 69632:
                    if (msg.arg1 == 0) {
                        WifiSignalController.this.mWifiChannel.sendMessage(Message.obtain(this, 69633));
                        return;
                    } else {
                        Log.e(WifiSignalController.this.mTag, "Failed to connect to wifi");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    static class WifiState extends HwWifiStateBase {
        String ssid;

        WifiState() {
        }

        public void copyFrom(State s) {
            super.copyFrom(s);
            this.ssid = ((WifiState) s).ssid;
        }

        protected void toString(StringBuilder builder) {
            super.toString(builder);
            builder.append(',').append("ssid=").append(this.ssid);
        }

        public boolean equals(Object o) {
            if (super.equals(o)) {
                return Objects.equals(((WifiState) o).ssid, this.ssid);
            }
            return false;
        }
    }

    public WifiSignalController(Context context, boolean hasMobileData, CallbackHandler callbackHandler, NetworkControllerImpl networkController) {
        super("WifiSignalController", context, 1, callbackHandler, networkController);
        this.mContext = context;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mHasMobileData = hasMobileData;
        Handler handler = new WifiHandler();
        this.mWifiChannel = new AsyncChannel();
        Messenger wifiMessenger = this.mWifiManager.getWifiServiceMessenger();
        if (wifiMessenger != null) {
            this.mWifiChannel.connect(context, handler, wifiMessenger);
        }
        WifiState wifiState = (WifiState) this.mCurrentState;
        IconGroup iconGroup = new IconGroup("Wi-Fi Icons", WifiIcons.WIFI_SIGNAL_STRENGTH, WifiIcons.QS_WIFI_SIGNAL_STRENGTH, AccessibilityContentDescriptions.WIFI_CONNECTION_STRENGTH, R.drawable.stat_sys_wifi_signal_null, R.drawable.ic_qs_wifi_no_network, R.drawable.stat_sys_wifi_signal_null, R.drawable.ic_qs_wifi_no_network, R.string.accessibility_no_wifi);
        ((WifiState) this.mLastState).iconGroup = iconGroup;
        wifiState.iconGroup = iconGroup;
    }

    protected WifiState cleanState() {
        return new WifiState();
    }

    public void notifyListeners(SignalCallback callback) {
        boolean wifiVisible;
        boolean z;
        boolean z2 = false;
        if (((WifiState) this.mCurrentState).enabled) {
            wifiVisible = ((WifiState) this.mCurrentState).connected;
        } else {
            wifiVisible = false;
        }
        String str = wifiVisible ? ((WifiState) this.mCurrentState).ssid : null;
        boolean ssidPresent = wifiVisible && ((WifiState) this.mCurrentState).ssid != null;
        String contentDescription = getStringIfExists(getContentDescription());
        if (((WifiState) this.mCurrentState).inetCondition == 0) {
            contentDescription = contentDescription + "," + this.mContext.getString(R.string.accessibility_quick_settings_no_internet);
        }
        IconState statusIcon = new IconState(wifiVisible, getCurrentIconId(), contentDescription);
        IconState qsIcon = new IconState(((WifiState) this.mCurrentState).connected, getQsCurrentIconId(), contentDescription);
        boolean z3 = ((WifiState) this.mCurrentState).enabled;
        if (ssidPresent) {
            z = ((WifiState) this.mCurrentState).activityIn;
        } else {
            z = false;
        }
        if (ssidPresent) {
            z2 = ((WifiState) this.mCurrentState).activityOut;
        }
        callback.setWifiIndicators(z3, statusIcon, qsIcon, z, z2, str);
    }

    public void handleBroadcast(Intent intent) {
        this.mWifiTracker.handleBroadcast(intent);
        ((WifiState) this.mCurrentState).enabled = this.mWifiTracker.enabled;
        ((WifiState) this.mCurrentState).connected = this.mWifiTracker.connected;
        ((WifiState) this.mCurrentState).ssid = this.mWifiTracker.ssid;
        ((WifiState) this.mCurrentState).rssi = this.mWifiTracker.rssi;
        ((WifiState) this.mCurrentState).level = this.mWifiTracker.level;
        handleBroadcastHuawei(intent);
        notifyListenersIfNecessary();
    }

    void setActivity(final int wifiActivity) {
        SystemUIIdleHandler.addToIdleMessage(new Runnable() {
            public void run() {
                boolean z;
                boolean z2 = true;
                WifiState wifiState = (WifiState) WifiSignalController.this.mCurrentState;
                if (wifiActivity == 3) {
                    z = true;
                } else if (wifiActivity == 1) {
                    z = true;
                } else {
                    z = false;
                }
                wifiState.activityIn = z;
                wifiState = (WifiState) WifiSignalController.this.mCurrentState;
                if (!(wifiActivity == 3 || wifiActivity == 2)) {
                    z2 = false;
                }
                wifiState.activityOut = z2;
                WifiSignalController.this.notifyListenersIfNecessary();
            }
        }, 1002);
    }
}
