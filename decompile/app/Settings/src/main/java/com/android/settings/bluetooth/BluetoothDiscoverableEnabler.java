package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.text.format.DateUtils;
import android.util.Log;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.Utils;
import com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;

final class BluetoothDiscoverableEnabler implements OnPreferenceChangeListener {
    private Context mContext;
    private boolean mDiscoverable;
    private final TwoStatePreference mDiscoveryPreference;
    private final LocalBluetoothAdapter mLocalAdapter;
    private int mNumberOfPairedDevices;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.SCAN_MODE_CHANGED".equals(intent.getAction())) {
                int mode = intent.getIntExtra("android.bluetooth.adapter.extra.SCAN_MODE", Integer.MIN_VALUE);
                if (mode != Integer.MIN_VALUE) {
                    BluetoothDiscoverableEnabler.this.handleModeChanged(mode);
                }
            }
        }
    };
    private final SharedPreferences mSharedPreferences;
    private int mTimeoutSecs = -1;
    private final Handler mUiHandler = new Handler();
    private final Runnable mUpdateCountdownSummaryRunnable = new Runnable() {
        public void run() {
            BluetoothDiscoverableEnabler.this.updateCountdownSummary();
        }
    };

    BluetoothDiscoverableEnabler(LocalBluetoothAdapter adapter, TwoStatePreference discoveryPreference) {
        this.mLocalAdapter = adapter;
        this.mDiscoveryPreference = discoveryPreference;
        this.mSharedPreferences = discoveryPreference.getSharedPreferences();
        discoveryPreference.setPersistent(false);
    }

    public void resume(Context context) {
        if (this.mLocalAdapter == null) {
            MLog.e("BluetoothDiscoverableEnabler", "error! in resume BluetoothAdapter is null!");
            return;
        }
        if (this.mContext != context) {
            this.mContext = context;
        }
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.SCAN_MODE_CHANGED"));
        this.mDiscoveryPreference.setOnPreferenceChangeListener(this);
        handleModeChanged(this.mLocalAdapter.getScanMode());
    }

    public void pause() {
        if (this.mLocalAdapter == null) {
            MLog.e("BluetoothDiscoverableEnabler", "error! in pause BluetoothAdapter is null!");
            return;
        }
        this.mUiHandler.removeCallbacks(this.mUpdateCountdownSummaryRunnable);
        try {
            this.mContext.unregisterReceiver(this.mReceiver);
        } catch (Throwable e) {
            MLog.e("BluetoothDiscoverableEnabler", "exception of unregister receiver ", e);
        }
        this.mDiscoveryPreference.setOnPreferenceChangeListener(null);
    }

    public boolean onPreferenceClick(Preference preference) {
        boolean z;
        if (this.mDiscoverable) {
            z = false;
        } else {
            z = true;
        }
        this.mDiscoverable = z;
        setEnabled(this.mDiscoverable);
        return true;
    }

    private void setEnabled(boolean enable) {
        if (this.mLocalAdapter == null) {
            MLog.e("BluetoothDiscoverableEnabler", "error! in setEnabled BluetoothAdapter is null!");
            return;
        }
        if (enable) {
            int timeout = getDiscoverableTimeout();
            long endTimestamp = System.currentTimeMillis() + (((long) timeout) * 1000);
            this.mDiscoveryPreference.setChecked(enable);
            LocalBluetoothPreferences.persistDiscoverableEndTimestamp(this.mContext, endTimestamp);
            this.mLocalAdapter.setScanMode(23, timeout);
            updateCountdownSummary();
            Log.d("BluetoothDiscoverableEnabler", "setEnabled(): enabled = " + enable + "timeout = " + timeout);
            if (timeout > 0) {
                BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(this.mContext, endTimestamp);
            } else {
                BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(this.mContext);
            }
        } else {
            this.mLocalAdapter.setScanMode(21);
            BluetoothDiscoverableTimeoutReceiver.cancelDiscoverableAlarm(this.mContext);
        }
    }

    private void updateTimerDisplay(int timeout) {
        if (getDiscoverableTimeout() == 0) {
            this.mDiscoveryPreference.setSummary(2131627470);
            return;
        }
        String textTimeout = DateUtils.formatElapsedTime((long) timeout);
        this.mDiscoveryPreference.setSummary(this.mContext.getString(2131627469, new Object[]{textTimeout}));
        if (timeout == 0) {
            Intent intent = new Intent("android.bluetooth.intent.DISCOVERABLE_TIMEOUT");
            intent.setClass(this.mContext, BluetoothDiscoverableTimeoutReceiver.class);
            this.mContext.sendBroadcast(intent);
        }
    }

    void setDiscoverableTimeout(int index) {
        String timeoutValue;
        switch (index) {
            case 1:
                this.mTimeoutSecs = 300;
                timeoutValue = "fivemin";
                break;
            case 2:
                this.mTimeoutSecs = 3600;
                timeoutValue = "onehour";
                break;
            case 3:
                this.mTimeoutSecs = 0;
                timeoutValue = "never";
                break;
            default:
                this.mTimeoutSecs = 120;
                timeoutValue = "twomin";
                break;
        }
        this.mSharedPreferences.edit().putString("bt_discoverable_timeout", timeoutValue).apply();
        this.mSharedPreferences.edit().putInt("bt_discoverable_timeout_number", this.mTimeoutSecs).apply();
        setEnabled(true);
    }

    private int getDiscoverableTimeout() {
        if (this.mTimeoutSecs != -1) {
            return this.mTimeoutSecs;
        }
        int timeout = SystemProperties.getInt("debug.bt.discoverable_time", -1);
        if (timeout < 0) {
            String timeoutValue = this.mSharedPreferences.getString("bt_discoverable_timeout", "twomin");
            if (timeoutValue.equals("never")) {
                timeout = 0;
            } else if (timeoutValue.equals("onehour")) {
                timeout = 3600;
            } else if (timeoutValue.equals("fivemin")) {
                timeout = 300;
            } else {
                timeout = 120;
            }
        }
        this.mTimeoutSecs = timeout;
        return timeout;
    }

    int getDiscoverableTimeoutIndex() {
        switch (getDiscoverableTimeout()) {
            case 0:
                return 3;
            case 300:
                return 1;
            case 3600:
                return 2;
            default:
                return 0;
        }
    }

    void setNumberOfPairedDevices(int pairedDevices) {
        if (this.mLocalAdapter == null) {
            MLog.e("BluetoothDiscoverableEnabler", "error! in setNumberOfPairedDevices BluetoothAdapter is null!");
            return;
        }
        this.mNumberOfPairedDevices = pairedDevices;
        handleModeChanged(this.mLocalAdapter.getScanMode());
    }

    void handleModeChanged(int mode) {
        Log.d("BluetoothDiscoverableEnabler", "handleModeChanged(): mode = " + mode);
        if (mode == 23) {
            this.mDiscoverable = true;
            updateCountdownSummary();
            this.mDiscoveryPreference.setChecked(true);
            return;
        }
        this.mDiscoverable = false;
        setSummaryNotDiscoverable();
        this.mDiscoveryPreference.setChecked(false);
    }

    private void setSummaryNotDiscoverable() {
        if (this.mNumberOfPairedDevices != 0) {
            this.mDiscoveryPreference.setSummary(2131624423);
        } else {
            this.mDiscoveryPreference.setSummary(2131624422);
        }
    }

    private void updateCountdownSummary() {
        if (this.mLocalAdapter == null) {
            MLog.e("BluetoothDiscoverableEnabler", "error! in updateCountdownSummary BluetoothAdapter is null!");
        } else if (this.mLocalAdapter.getScanMode() == 23) {
            long currentTimestamp = System.currentTimeMillis();
            long endTimestamp = LocalBluetoothPreferences.getDiscoverableEndTimestamp(this.mContext);
            if (Utils.isTablet()) {
                int sharedPreferencesTimeout = this.mSharedPreferences.getInt("bt_discoverable_timeout_number", -1);
                if (!(sharedPreferencesTimeout == this.mTimeoutSecs || sharedPreferencesTimeout == -1)) {
                    this.mTimeoutSecs = sharedPreferencesTimeout;
                }
            }
            if (currentTimestamp > endTimestamp) {
                updateTimerDisplay(0);
                return;
            }
            updateTime((int) ((endTimestamp - currentTimestamp) / 1000));
            synchronized (this) {
                this.mUiHandler.removeCallbacks(this.mUpdateCountdownSummaryRunnable);
                this.mUiHandler.postDelayed(this.mUpdateCountdownSummaryRunnable, 1000);
            }
        }
    }

    private void updateTime(int timeLeft) {
        if (timeLeft <= 120 && getDiscoverableTimeout() == 120) {
            updateTimerDisplay(timeLeft);
        } else if (timeLeft <= 300 && getDiscoverableTimeout() == 300) {
            updateTimerDisplay(timeLeft);
        } else if (timeLeft > 3600 || getDiscoverableTimeout() != 3600) {
            updateTimerDisplay(0);
        } else {
            updateTimerDisplay(timeLeft);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ItemUseStat.getInstance().handleTwoStatePreferenceClick(this.mContext, preference, newValue);
        if ((preference instanceof TwoStatePreference) && ((Boolean) newValue).booleanValue() != ((TwoStatePreference) preference).isChecked()) {
            onPreferenceClick(preference);
        }
        return true;
    }
}
