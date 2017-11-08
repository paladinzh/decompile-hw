package com.android.settings.dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.Utils;

public class DataEnabler extends BaseSwitchEnabler {
    private static boolean mUserToolbox = SystemProperties.getBoolean("ro.config.hw_toolbox", true);
    private boolean hasObserverChange = false;
    private Context mContext;
    private ContentObserver mDataSwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            DataEnabler.this.updateStatus();
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1011:
                    Log.d("DataEnabler", "received CARD_STATE_CHANGED call updateStatus");
                    DataEnabler.this.updateStatus();
                    return;
                case 1012:
                    DataEnabler.this.updateStatusAfterAirplaneModeChanged();
                    return;
                default:
                    Log.d("DataEnabler", "received unknow event, just return");
                    return;
            }
        }
    };
    private IntentFilter mIntentFilter = null;
    private boolean mIsActive;
    private boolean mIsChecked;
    private boolean mIsEnable;
    private boolean mIsSimCardAvailable;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("DataEnabler", "BroadcastReceiver  action=" + action);
            if ("android.intent.action.ANY_DATA_STATE".equals(action)) {
                String dataState = intent.getStringExtra("state");
                Log.d("DataEnabler", "BroadcastReceiver  dataState=" + dataState);
                if ("DISCONNECTED".equalsIgnoreCase(dataState) || "CONNECTED".equalsIgnoreCase(dataState)) {
                    DataEnabler.this.sendMessage(1011, 1000);
                }
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                DataEnabler.this.sendMessage(1011, 1000);
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                DataEnabler.this.mHandler.sendEmptyMessage(1012);
            }
        }
    };
    private TelephonyManager tm;

    public DataEnabler(Context context, Switch switch_) {
        super(context, switch_);
        this.mSwitch = switch_;
        this.mContext = context;
        this.tm = (TelephonyManager) context.getSystemService("phone");
        updateStatus();
    }

    private void registerObserver() {
        if (!this.hasObserverChange) {
            if (this.mIntentFilter == null) {
                this.mIntentFilter = new IntentFilter("android.intent.action.ANY_DATA_STATE");
                this.mIntentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
                this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
            }
            this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
            this.hasObserverChange = true;
        }
        if (!mUserToolbox) {
            return;
        }
        if (Utils.isMultiSimEnabled()) {
            if (this.mContext != null) {
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data0"), true, this.mDataSwitchObserver);
                this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data1"), true, this.mDataSwitchObserver);
            }
        } else if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, this.mDataSwitchObserver);
        }
    }

    private void unregisterObserver() {
        if (this.hasObserverChange) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.hasObserverChange = false;
        }
        if (mUserToolbox && this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDataSwitchObserver);
        }
    }

    protected void updateSwitchStatus() {
        if (this.mSwitch != null) {
            this.mSwitch.setChecked(this.mIsChecked);
            this.mSwitch.setEnabled(this.mIsEnable);
        }
    }

    protected void performCheck(boolean isChecked) {
        if (getDataEnabled() == isChecked) {
            Log.d("DataEnabler", "performCheck is the same");
            return;
        }
        if (isChecked) {
            setDataEnabled(true);
        } else {
            setDataEnabled(false);
        }
    }

    private void updateStatus() {
        getSwitchStatus();
        updateSwitchStatus();
    }

    private void updateStatusAfterAirplaneModeChanged() {
        if (AirplaneModeEnablerHwBase.isAirplaneModeOn(this.mContext)) {
            this.mIsEnable = false;
        } else {
            this.mIsEnable = this.mIsSimCardAvailable;
        }
        if (this.mSwitch != null) {
            this.mSwitch.setEnabled(this.mIsEnable);
        }
    }

    private void getSwitchStatus() {
        boolean z;
        this.mIsChecked = getDataEnabled();
        if (isCardDataUnable()) {
            z = false;
        } else {
            z = true;
        }
        this.mIsSimCardAvailable = z;
        if (this.mIsSimCardAvailable) {
            this.mIsEnable = true;
        } else {
            this.mIsEnable = false;
        }
        if (AirplaneModeEnablerHwBase.isAirplaneModeOn(this.mContext)) {
            this.mIsEnable = false;
        }
    }

    public void resume() {
        if (this.mSwitch != null) {
            super.resume();
            registerObserver();
        }
        this.mIsActive = true;
    }

    public void pause() {
        if (this.mSwitch != null) {
            super.pause();
            unregisterObserver();
        }
        this.mIsActive = false;
    }

    private void sendMessage(int what, int delay) {
        this.mHandler.removeMessages(what);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what), (long) delay);
    }

    private boolean getDataEnabled() {
        try {
            return this.tm.getDataEnabled();
        } catch (NullPointerException e) {
            Log.w("DataEnabler", "Phone did not start!");
            return false;
        }
    }

    private void setDataEnabled(boolean dataState) {
        try {
            this.tm.setDataEnabled(dataState);
        } catch (NullPointerException e) {
            Log.w("DataEnabler", "Phone did not start!");
        }
    }

    private boolean isCardDataUnable() {
        boolean z = false;
        if (!Utils.isMultiSimEnabled()) {
            return isDataUnableFromSim();
        }
        if (isDataUnableFromSim(0)) {
            z = isDataUnableFromSim(1);
        }
        return z;
    }

    private boolean isDataUnableFromSim(int subId) {
        return TelephonyManager.getDefault().getSimState(subId) == 1 || HwTelephonyManager.getDefault().getSubState((long) subId) == 0;
    }

    private boolean isDataUnableFromSim() {
        return TelephonyManager.getDefault().getSimState() == 1;
    }

    public boolean isActive() {
        return this.mIsActive;
    }
}
