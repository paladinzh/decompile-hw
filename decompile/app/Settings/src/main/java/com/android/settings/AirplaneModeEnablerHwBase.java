package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.android.settings.bluetooth.Utils;
import com.android.settings.dashboard.BaseSwitchEnabler;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

class AirplaneModeEnablerHwBase extends BaseSwitchEnabler {
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AirplaneModeEnablerHwBase.this.enableSwitchPreference(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE));
        }
    };
    protected Context mContext;
    private int mCurrentBluetoothState = 10;
    private Handler mHwExtHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                    if (AirplaneModeEnablerHwBase.this.mShouldEnableSwitch) {
                        AirplaneModeEnablerHwBase.this.mSwitch.setEnabled(true);
                        AirplaneModeEnablerHwBase.this.stopTimers();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    protected final IntentFilter mIntentFilter;
    private LocalBluetoothAdapter mLocalAdapter;
    protected boolean mShouldEnableSwitch = false;

    private int getBluetoothState() {
        if (this.mLocalAdapter != null) {
            return this.mLocalAdapter.getBluetoothState();
        }
        return 10;
    }

    private final boolean isBluetoothPersistedStateOn() {
        return Global.getInt(this.mContext.getContentResolver(), "bluetooth_on", 0) != 0;
    }

    public void enableSwitchPreference(int blutoothState) {
        if (this.mSwitch != null && !this.mSwitch.isEnabled()) {
            if (12 == blutoothState) {
                if (!this.mSwitch.isChecked()) {
                    this.mSwitch.setEnabled(true);
                    stopTimers();
                }
            } else if (10 == blutoothState && this.mSwitch.isChecked()) {
                this.mSwitch.setEnabled(true);
                stopTimers();
            }
        }
    }

    private void startTimers() {
        this.mHwExtHandler.sendMessageDelayed(this.mHwExtHandler.obtainMessage(4), 5000);
        this.mShouldEnableSwitch = true;
    }

    private void stopTimers() {
        this.mShouldEnableSwitch = false;
        if (this.mHwExtHandler.hasMessages(4)) {
            this.mHwExtHandler.removeMessages(4);
        }
    }

    public AirplaneModeEnablerHwBase(Context context, Switch switch_) {
        super(context, switch_);
        this.mContext = context;
        this.mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            this.mLocalAdapter = null;
        } else {
            this.mLocalAdapter = manager.getBluetoothAdapter();
        }
    }

    public void resume() {
        super.resume();
        this.mContext.registerReceiver(this.mBluetoothStateReceiver, this.mIntentFilter);
    }

    public void pause() {
        super.pause();
        this.mContext.unregisterReceiver(this.mBluetoothStateReceiver);
    }

    protected void updateSwitchStatus() {
        if (this.mSwitch != null) {
            setSwitchChecked(WirelessUtils.isAirplaneModeOn(this.mContext));
        }
    }

    public void onCheckedChanged(CompoundButton mSwitchPref, boolean isChecked) {
        if (mSwitchPref == this.mSwitch && isChecked != isAirplaneModeOn(this.mContext)) {
            int i;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "airplane_mode_on";
            if (isChecked) {
                i = 1;
            } else {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
            if (isChecked) {
                this.mCurrentBluetoothState = getBluetoothState();
                if (!(10 == this.mCurrentBluetoothState || 13 == this.mCurrentBluetoothState)) {
                    mSwitchPref.setEnabled(false);
                    startTimers();
                }
            } else {
                this.mCurrentBluetoothState = getBluetoothState();
                if (!(12 == this.mCurrentBluetoothState || 11 == this.mCurrentBluetoothState || !isBluetoothPersistedStateOn())) {
                    mSwitchPref.setEnabled(false);
                    startTimers();
                }
            }
            Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
            intent.putExtra("state", isChecked);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void setSwitchChecked(boolean checked) {
        if (this.mSwitch != null && checked != this.mSwitch.isChecked()) {
            this.mSwitch.setOnCheckedChangeListener(null);
            this.mSwitch.setChecked(checked);
            this.mSwitch.setOnCheckedChangeListener(this);
        }
    }

    public static boolean isAirplaneModeOn(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        return z;
    }
}
