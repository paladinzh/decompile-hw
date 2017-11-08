package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings.Global;
import android.support.v7.preference.TwoStatePreference;
import com.android.settings.MLog;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settingslib.TetherUtil;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class WifiApEnabler extends WifiApEnablerHwBase {
    ConnectivityManager mCm;
    private final Context mContext;
    private HwCustHotspotAuthentication mCust;
    private HwCustWifiApEnabler mCustWifiApEnabler;
    private final DataSaverBackend mDataSaverBackend;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiApEnabler.this.mCust != null) {
                WifiApEnabler.this.mCust.custReceiveBroadcast(intent);
            }
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                MLog.d("WifiApEnabler", "onReceive WifiManager.WIFI_AP_STATE_CHANGED_ACTION.");
                WifiApEnabler.this.handleWifiApStateChanged(intent);
            } else if ("android.net.conn.TETHER_STATE_CHANGED".equals(action)) {
                ArrayList<String> active = intent.getStringArrayListExtra("activeArray");
                ArrayList<String> errored = intent.getStringArrayListExtra("erroredArray");
                if (active != null && errored != null) {
                    WifiApEnabler.this.updateTetherState(active.toArray(), errored.toArray());
                }
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                WifiApEnabler.this.enableWifiSwitch();
            }
        }
    };
    private final TwoStatePreference mSwitch;
    private String[] mWifiRegexs;

    public WifiApEnabler(Context context, DataSaverBackend dataSaverBackend, TwoStatePreference checkBox) {
        super(context, checkBox);
        this.mContext = context;
        this.mDataSaverBackend = dataSaverBackend;
        this.mSwitch = checkBox;
        checkBox.setPersistent(false);
        this.mCustWifiApEnabler = (HwCustWifiApEnabler) HwCustUtils.createObj(HwCustWifiApEnabler.class, new Object[0]);
        this.mCm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mCust = (HwCustHotspotAuthentication) HwCustUtils.createObj(HwCustHotspotAuthentication.class, new Object[0]);
        if (this.mCust != null) {
            this.mCust.initHwCustHotspotAuthenticationImpl(this.mContext);
        }
        this.mWifiRegexs = this.mCm.getTetherableWifiRegexs();
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.conn.TETHER_STATE_CHANGED");
        this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
    }

    public void resume() {
        super.resume();
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
        enableWifiSwitch();
    }

    public void pause() {
        super.pause();
        this.mContext.unregisterReceiver(this.mReceiver);
        if (this.mCust != null) {
            this.mCust.custStop();
        }
    }

    protected void enableWifiSwitch() {
        boolean isAirplaneMode;
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            isAirplaneMode = true;
        } else {
            isAirplaneMode = false;
        }
        if (isAirplaneMode) {
            this.mSwitch.setEnabled(false);
        } else {
            this.mSwitch.setEnabled(true);
        }
        if (this.mCustWifiApEnabler != null) {
            this.mCustWifiApEnabler.custHotSpotDisable(this.mSwitch);
        }
    }

    public void setSoftapEnabled(boolean enable) {
        if (isSameState(enable)) {
            MLog.w("WifiApEnabler", "alread the state.when try to change softap enable state " + enable);
        } else if (this.mCust != null && this.mCust.isHotspotAuthorization(enable, this.mSwitch, 0)) {
        } else {
            if (this.mCust == null || this.mCust.isTetheringAllowed(this.mSwitch)) {
                checkWifiState(enable);
                if (TetherUtil.setWifiTethering(enable, this.mContext)) {
                    this.mSwitch.setEnabled(false);
                } else {
                    MLog.w("WifiApEnabler", "faild setWifiApEnabled:" + enable);
                }
                return;
            }
            MLog.d("WifiApEnabler", "APN_TYPE_NOT_AVAILABLE");
        }
    }

    public void updateConfigSummary(WifiConfiguration wifiConfig) {
    }

    private void updateTetherState(Object[] tethered, Object[] errored) {
        boolean wifiTethered = false;
        boolean wifiErrored = false;
        for (String s : tethered) {
            for (String regex : this.mWifiRegexs) {
                String s2;
                if (s2.matches(regex)) {
                    wifiTethered = true;
                }
            }
        }
        for (Object o : errored) {
            s2 = (String) o;
            for (String regex2 : this.mWifiRegexs) {
                if (s2.matches(regex2)) {
                    wifiErrored = true;
                }
            }
        }
        if (wifiTethered) {
            updateConfigSummary(this.mWifiManager.getWifiApConfiguration());
        } else if (wifiErrored && this.mCust != null) {
            this.mCust.handleCustErrorView(this.mSwitch);
        }
    }

    protected void handleWifiApStateChanged(int state, int reason) {
        boolean z = false;
        super.handleWifiApStateChanged(state, reason);
        switch (state) {
            case 10:
                MLog.d("WifiApEnabler", "handleWifiApStateChanged WIFI_AP_STATE_DISABLING, state = " + state);
                this.mSwitch.setChecked(false);
                this.mSwitch.setEnabled(false);
                break;
            case 11:
                MLog.d("WifiApEnabler", "handleWifiApStateChanged WIFI_AP_STATE_DISABLED, state = " + state);
                this.mSwitch.setChecked(false);
                if (!this.mWaitForWifiStateChange) {
                    enableWifiSwitch();
                    break;
                }
                break;
            case 12:
                MLog.d("WifiApEnabler", "handleWifiApStateChanged WIFI_AP_STATE_ENABLING, state = " + state);
                this.mSwitch.setEnabled(false);
                break;
            case 13:
                MLog.d("WifiApEnabler", "handleWifiApStateChanged WIFI_AP_STATE_ENABLED, state = " + state);
                this.mSwitch.setChecked(true);
                TwoStatePreference twoStatePreference = this.mSwitch;
                if (!this.mDataSaverBackend.isDataSaverEnabled()) {
                    z = true;
                }
                twoStatePreference.setEnabled(z);
                break;
            default:
                MLog.d("WifiApEnabler", "handleWifiApStateChanged default, state = " + state);
                this.mSwitch.setChecked(false);
                enableWifiSwitch();
                break;
        }
        MLog.d("WifiApEnabler", "handleWifiApStateChanged EXIT, state = " + state);
    }
}
