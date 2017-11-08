package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

public class SuspendButtonEnabler {
    protected Context mContext;
    private IntentFilter mIntentFilter;
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(SuspendButtonEnabler.this.mContext, preference, newValue);
            SuspendButtonEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            return true;
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.huawei.android.FloatTasks.state_change".equals(intent.getAction())) {
                SuspendButtonEnabler.this.handleStateChanged(intent.getIntExtra("float_task_state", 0));
            }
        }
    };
    private boolean mStateMachineEvent;
    protected Preference mStatusPreference;
    protected SwitchPreference mSwitchPreference;

    public SuspendButtonEnabler(Context context, SwitchPreference switchPreference) {
        this.mContext = context;
        this.mSwitchPreference = switchPreference;
        initIntentFilter();
    }

    public SuspendButtonEnabler(Context context, Preference preference) {
        this.mContext = context;
        this.mStatusPreference = preference;
        initIntentFilter();
    }

    private void initIntentFilter() {
        this.mIntentFilter = new IntentFilter("com.huawei.android.FloatTasks.state_change");
    }

    public void resume() {
        this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter, "com.huawei.android.FloatTasks.readPermission", null);
        boolean setChecked = isFloatTaskRunning();
        updateStatusText(setChecked);
        updateSwitchStatus(setChecked);
    }

    public void pause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    void handleStateChanged(int state) {
        MLog.d("SuspenButtonEnabler", "The received state of suspend button is: " + state);
        switch (state) {
            case 0:
                setSwitchChecked(false);
                updateStatusText(false);
                return;
            case 1:
                setSwitchChecked(true);
                updateStatusText(true);
                return;
            default:
                return;
        }
    }

    protected void updateStatusText(boolean setChecked) {
        if (this.mStatusPreference != null) {
            int i;
            Preference preference = this.mStatusPreference;
            if (setChecked) {
                i = 2131627698;
            } else {
                i = 2131627699;
            }
            preference.setSummary(i);
        }
    }

    protected void updateSwitchStatus(boolean setChecked) {
        if (this.mSwitchPreference != null) {
            this.mSwitchPreference.setEnabled(true);
            this.mSwitchPreference.setChecked(setChecked);
            this.mSwitchPreference.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
    }

    protected void performCheck(boolean isChecked) {
        if (!(this.mStateMachineEvent || this.mSwitchPreference == null)) {
            this.mSwitchPreference.setEnabled(false);
            setFloatTaskEnabled(isChecked);
            this.mSwitchPreference.setEnabled(true);
        }
    }

    private boolean isFloatTaskRunning() {
        try {
            Bundle bundle = this.mContext.getContentResolver().call(Uri.parse("content://com.huawei.android.FloatTasksContentProvider"), "get", null, null);
            if (bundle == null) {
                return false;
            }
            int state = bundle.getInt("float_task_state", 0);
            if (state != 0) {
                return state == 1;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void setFloatTaskEnabled(boolean enable) {
        try {
            this.mContext.getContentResolver().call(Uri.parse("content://com.huawei.android.FloatTasksContentProvider"), "set", enable ? "1" : "0", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSwitchChecked(boolean checked) {
        if (!(this.mSwitchPreference == null || checked == this.mSwitchPreference.isChecked())) {
            this.mStateMachineEvent = true;
            this.mSwitchPreference.setChecked(checked);
            this.mStateMachineEvent = false;
        }
    }
}
