package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.WirelessUtils;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.cust.HwCustUtils;

public class Enable4GEnabler {
    private String[] INTENT_ACTIONS = new String[]{"com.huawei.telephony.PREF_4G_SWITCH_DONE", "android.intent.action.SIM_STATE_CHANGED", "android.provider.Telephony.SPN_STRINGS_UPDATED", "android.intent.action.AIRPLANE_MODE", "android.intent.action.PHONE_STATE"};
    private Context mContext;
    private HwCustEnable4GEnabler mCustEnable4GEnabler;
    private ContentObserver mLTESwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.d("Enable4GEnabler", "Receive PREFERRED_NETWORK_MODE changed. ");
            Enable4GEnabler.this.updateSwitchStatus();
        }
    };
    private OnPreferenceChangeListener mPreferenceChangedListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Enable4GEnabler.this.performCheck(((Boolean) newValue).booleanValue());
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(Enable4GEnabler.this.mContext, preference, newValue);
            return true;
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && ArrayUtils.contains(Enable4GEnabler.this.INTENT_ACTIONS, intent.getAction())) {
                Log.d("Enable4GEnabler", "Receive phone state changed. Action is: " + intent.getAction());
                Enable4GEnabler.this.updateSwitchStatus();
            }
        }
    };
    private SwitchPreference mSwitch;

    public Enable4GEnabler(Context context, SwitchPreference switch_) {
        this.mContext = context;
        this.mSwitch = switch_;
        this.mCustEnable4GEnabler = (HwCustEnable4GEnabler) HwCustUtils.createObj(HwCustEnable4GEnabler.class, new Object[]{context});
    }

    private boolean isVSimEnabled() {
        if (this.mCustEnable4GEnabler != null) {
            return this.mCustEnable4GEnabler.isVSimEnabled();
        }
        return false;
    }

    public boolean isSimCardReady() {
        if (this.mContext == null) {
            return false;
        }
        boolean isReady = false;
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        if (tm.isMultiSimEnabled()) {
            if (tm.getSimState(0) == 5 || tm.getSimState(1) == 5) {
                isReady = true;
            }
        } else if (tm.getSimState() == 5) {
            isReady = true;
        }
        Log.d("Enable4GEnabler", "isSimCardReady: " + isReady);
        return isReady;
    }

    private boolean isCalling() {
        if (this.mContext == null) {
            return false;
        }
        boolean isCalling = TelephonyManager.from(this.mContext).getCallState() != 0;
        Log.d("Enable4GEnabler", "isCalling: " + isCalling);
        return isCalling;
    }

    private void setChecked(boolean checked) {
        if (this.mSwitch == null || checked == this.mSwitch.isChecked()) {
            Log.d("Enable4GEnabler", "Switch checked state is not changed.");
            return;
        }
        this.mSwitch.setChecked(checked);
        Log.d("Enable4GEnabler", "Set switch checked in setChecked: " + checked);
    }

    private void setEnabled(boolean enabled) {
        if (this.mSwitch == null || enabled == this.mSwitch.isEnabled()) {
            Log.d("Enable4GEnabler", "Switch enabled state is not changed.");
            return;
        }
        this.mSwitch.setEnabled(enabled);
        Log.d("Enable4GEnabler", "Set switch enabled in setEnabled: " + enabled);
    }

    private int getLTEMode() {
        int lteMode = 1;
        if (WirelessUtils.isAirplaneModeOn(this.mContext) || !isSimCardReady() || isVSimEnabled() || isCalling()) {
            lteMode = 3;
        } else {
            try {
                lteMode = TelephonyManagerEx.getLteServiceAbility();
            } catch (NoSuchMethodError e) {
                Log.e("Enable4GEnabler", "TelephonyManagerEx.getLteServiceAbility NoSuchMethodError");
            } catch (Exception e2) {
                Log.e("Enable4GEnabler", "TelephonyManagerEx getLteServiceAbility error");
            }
        }
        Log.d("Enable4GEnabler", "current LTE mode is: " + lteMode);
        return lteMode;
    }

    protected void updateSwitchStatus() {
        if (this.mSwitch != null) {
            int lteMode = getLTEMode();
            this.mSwitch.setOnPreferenceChangeListener(null);
            switch (lteMode) {
                case 0:
                    setEnabled(true);
                    setChecked(false);
                    break;
                case 1:
                    setEnabled(true);
                    setChecked(true);
                    break;
                case 3:
                    setEnabled(false);
                    break;
                default:
                    setChecked(false);
                    break;
            }
            this.mSwitch.setOnPreferenceChangeListener(this.mPreferenceChangedListener);
        }
    }

    protected void performCheck(boolean isChecked) {
        int mode = isChecked ? 1 : 0;
        Log.d("Enable4GEnabler", "Set 4g mode enabled: " + isChecked);
        try {
            TelephonyManagerEx.setLteServiceAbility(mode);
        } catch (NoSuchMethodError e) {
            Log.e("Enable4GEnabler", "TelephonyManagerEx.setLteServiceAbility NoSuchMethodError");
        } catch (Exception e2) {
            Log.e("Enable4GEnabler", "TelephonyManagerEx.getDefault().setLteServiceAbility error, lteMode = " + mode);
        }
    }

    public void resume() {
        registerObserver();
        updateSwitchStatus();
    }

    public void pause() {
        unregisterObserver();
    }

    private void registerObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), true, this.mLTESwitchObserver);
            for (String action : this.INTENT_ACTIONS) {
                this.mContext.registerReceiver(this.mReceiver, new IntentFilter(action));
            }
        }
    }

    private void unregisterObserver() {
        if (this.mContext != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mLTESwitchObserver);
            this.mContext.unregisterReceiver(this.mReceiver);
        }
    }
}
