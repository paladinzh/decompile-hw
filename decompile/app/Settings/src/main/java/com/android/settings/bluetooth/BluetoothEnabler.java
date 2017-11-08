package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;
import com.android.settings.ItemUseStat;
import com.android.settings.RadarReporter;
import com.android.settings.dashboard.DashBoardTileEnabler;
import com.android.settings.search.Index;
import com.android.settingslib.WirelessUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.huawei.cust.HwCustUtils;
import java.util.HashMap;

public final class BluetoothEnabler extends DashBoardTileEnabler implements OnPreferenceChangeListener {
    HwCustBluetoothEnabler mCustBluetoothEnabler;
    private boolean mEnabling = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Index.getInstance(BluetoothEnabler.this.mContext).updateFromClassNameResource(BluetoothSettings.class.getName(), true, msg.getData().getBoolean("is_bluetooth_on"));
                    return;
                case 1:
                    if (BluetoothEnabler.this.mEnabling && BluetoothEnabler.this.mLocalAdapter != null) {
                        HashMap<Short, Object> map = new HashMap();
                        map.put(Short.valueOf((short) 0), Integer.valueOf(BluetoothEnabler.this.mLocalAdapter.getBluetoothState()));
                        RadarReporter.reportRadar(907018003, map);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private final IntentFilter mIntentFilter;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE);
            HwLog.i("BluetoothEnabler", "onReceive Action = " + intent.getAction() + ", bluetooth adapter state = " + state + ", getBluetoothState = " + BluetoothEnabler.this.mLocalAdapter.getBluetoothState());
            BluetoothEnabler.this.handleStateChanged(state);
            if (state == 12) {
                HwLog.i("BluetoothEnabler", "BT-Enable-ST  end enable bt");
            } else if (state == 10) {
                HwLog.i("BluetoothEnabler", "BT-Disable-ST  end disable bt");
            }
        }
    };
    private SwitchPreference mSwitch;
    private boolean mValidListener;

    public BluetoothEnabler(Context context, SwitchPreference switch_) {
        this.mContext = context;
        this.mSwitch = switch_;
        this.mValidListener = false;
        this.mCustBluetoothEnabler = (HwCustBluetoothEnabler) HwCustUtils.createObj(HwCustBluetoothEnabler.class, new Object[0]);
        if (this.mCustBluetoothEnabler != null) {
            this.mCustBluetoothEnabler.custBluetoothDisable(this.mSwitch);
        }
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            this.mLocalAdapter = null;
            if (this.mSwitch != null) {
                this.mSwitch.setEnabled(false);
            }
        } else {
            this.mLocalAdapter = manager.getBluetoothAdapter();
        }
        this.mIntentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
    }

    public void updateStatusText(boolean isEnabled) {
        if (this.mContext != null) {
            int i;
            Context context = this.mContext;
            if (isEnabled) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            updateStatusText(context.getString(i));
        }
    }

    public void resume() {
        if (this.mLocalAdapter == null) {
            setSwitchEnabled(false);
        } else if (this.mCustBluetoothEnabler == null || !this.mCustBluetoothEnabler.custBluetoothDisable(this.mSwitch)) {
            this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
            if (this.mSwitch != null) {
                this.mSwitch.setOnPreferenceChangeListener(this);
            }
            handleStateChanged(this.mLocalAdapter.getBluetoothState());
            this.mValidListener = true;
        }
    }

    public void pause() {
        if (this.mLocalAdapter != null) {
            if (this.mCustBluetoothEnabler == null || !this.mCustBluetoothEnabler.custBluetoothDisable(this.mSwitch)) {
                this.mContext.unregisterReceiver(this.mReceiver);
                if (this.mSwitch != null) {
                    this.mSwitch.setOnPreferenceChangeListener(null);
                }
                this.mValidListener = false;
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(this.mContext, preference, newValue);
        if (preference == this.mSwitch) {
            boolean isChecked = ((Boolean) newValue).booleanValue();
            if (this.mLocalAdapter == null || isChecked == this.mLocalAdapter.isEnabled()) {
                return true;
            }
            if (isChecked && !WirelessUtils.isRadioAllowed(this.mContext, "bluetooth")) {
                Toast.makeText(this.mContext, 2131627278, 0).show();
                setChecked(false);
            }
            HwLog.d("BluetoothEnabler", "onPreferenceChange  before set bt checked = " + isChecked + ", btEnable = " + this.mLocalAdapter.isEnabled() + ", pref enabled = " + this.mSwitch.isEnabled());
            this.mLocalAdapter.setBluetoothEnabled(isChecked);
            this.mSwitch.setEnabled(false);
            if (isChecked) {
                HwLog.i("BluetoothEnabler", "BT-Enable-ST  begin enable bt");
                startCount();
            } else {
                HwLog.i("BluetoothEnabler", "BT-Disable-ST  begin disable bt");
                stopCount();
            }
        }
        return true;
    }

    void handleStateChanged(int state) {
        switch (state) {
            case 10:
                HwLog.i("BluetoothEnabler", "handleStateChanged STATE_OFF: check false, enable true");
                setChecked(false);
                setSwitchEnabled(true);
                updateStatusText(false);
                updateSearchIndex(false);
                stopCount();
                return;
            case 11:
                HwLog.i("BluetoothEnabler", "handleStateChanged STATE_TURNING_ON: enable false");
                setSwitchEnabled(false);
                updateStatusText(false);
                return;
            case 12:
                HwLog.i("BluetoothEnabler", "handleStateChanged STATE_ON: check true, enable true");
                setChecked(true);
                setSwitchEnabled(true);
                updateStatusText(true);
                updateSearchIndex(true);
                stopCount();
                return;
            case 13:
                HwLog.i("BluetoothEnabler", "handleStateChanged STATE_TURNING_OFF: enable false");
                setSwitchEnabled(false);
                updateStatusText(false);
                stopCount();
                return;
            case 15:
                HwLog.i("BluetoothEnabler", "handleStateChanged STATE_BLE_ON: enable false");
                setSwitchEnabled(false);
                updateStatusText(false);
                return;
            default:
                HwLog.w("BluetoothEnabler", "handleStateChanged default: check false, enable true");
                setChecked(false);
                setSwitchEnabled(true);
                updateStatusText(false);
                updateSearchIndex(false);
                return;
        }
    }

    private void setSwitchEnabled(boolean enabled) {
        if (this.mSwitch != null) {
            this.mSwitch.setEnabled(enabled);
        }
    }

    private void setChecked(boolean isChecked) {
        if (this.mSwitch != null && isChecked != this.mSwitch.isChecked()) {
            if (this.mValidListener) {
                this.mSwitch.setOnPreferenceChangeListener(null);
            }
            this.mSwitch.setChecked(isChecked);
            if (this.mValidListener) {
                this.mSwitch.setOnPreferenceChangeListener(this);
            }
        }
    }

    private void updateSearchIndex(boolean isBluetoothOn) {
        this.mHandler.removeMessages(0);
        Message msg = new Message();
        msg.what = 0;
        msg.getData().putBoolean("is_bluetooth_on", isBluetoothOn);
        this.mHandler.sendMessage(msg);
    }

    private void startCount() {
        this.mEnabling = true;
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), 10000);
    }

    private void stopCount() {
        this.mEnabling = false;
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
    }
}
