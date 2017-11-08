package com.android.settings.dashboard;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Switch;
import com.android.settings.Utils;

public class LteEnabler extends BaseSwitchEnabler {
    private static boolean mOkClicked = false;
    private boolean hasObserverChange = false;
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1011:
                    LteEnabler.this.updateStatus();
                    return;
                default:
                    Log.d("LteEnabler", "received unknow event, just return");
                    return;
            }
        }
    };
    private IntentFilter mIntentFilter = null;
    private boolean mIsActive;
    private boolean mIsChecked;
    private boolean mIsEnable;
    private ContentObserver mLTESwitchObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            LteEnabler.this.updateStatus();
        }
    };
    private int mMainCard;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.ANY_DATA_STATE".equals(action)) {
                String dataState = intent.getStringExtra("state");
                if ("DISCONNECTED".equalsIgnoreCase(dataState) || "CONNECTED".equalsIgnoreCase(dataState)) {
                    LteEnabler.this.sendMessage(1011, 1000);
                }
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                LteEnabler.this.updateStatus();
            } else if ("android.net.wifi.LTEDATA_COMPLETED_ACTION".equals(action)) {
                int status = intent.getIntExtra("lte_mobile_data_status", -1);
                if (status == 1 || status == 3) {
                    LteEnabler.this.updateStatus();
                }
            } else if ("com.huawei.telephony.PREF_4G_SWITCH_DONE".equals(action)) {
                if (!intent.getBooleanExtra("setting_result", true)) {
                    LteEnabler.this.updateStatus();
                }
            } else if ("com.android.huawei.PREFERRED_NETWORK_MODE_DATABASE_CHANGED".equals(action)) {
                LteEnabler.this.updateStatus();
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                LteEnabler.this.updateStatus();
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                LteEnabler.this.updateStatus();
            }
        }
    };

    private static class OnDialogClick implements OnClickListener {
        LteEnabler lteContextVar;
        Switch mSwitchVar;

        private OnDialogClick(Switch mSwitch, LteEnabler lteContext) {
            this.mSwitchVar = mSwitch;
            this.lteContextVar = lteContext;
        }

        public void onClick(DialogInterface dialog, int arg1) {
            switch (arg1) {
                case -2:
                    this.mSwitchVar.setOnCheckedChangeListener(null);
                    this.mSwitchVar.setChecked(false);
                    this.mSwitchVar.setOnCheckedChangeListener(this.lteContextVar);
                    dialog.dismiss();
                    return;
                case -1:
                    HwTelephonyManager.getDefault().setLteServiceAbility(1);
                    LteEnabler.mOkClicked = true;
                    dialog.dismiss();
                    return;
                default:
                    Log.d("LteEnabler", "received unknow event, just return");
                    return;
            }
        }
    }

    public static class PromptDialogFragment extends DialogFragment {
        static LteEnabler lteContext;
        static Context mContext;
        static Switch mSwitch;

        public static void show(Context activityContext, Switch mSwitchs, LteEnabler ctrolContext) {
            Activity parent = (Activity) activityContext;
            mContext = activityContext;
            mSwitch = mSwitchs;
            lteContext = ctrolContext;
            LteEnabler.mOkClicked = false;
            new PromptDialogFragment().show(parent.getFragmentManager(), null);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(mContext).setMessage(mContext.getResources().getString(2131628025)).setTitle(17039380).setIconAttribute(16843605).setPositiveButton(17039379, new OnDialogClick(mSwitch, lteContext)).setNegativeButton(17039369, new OnDialogClick(mSwitch, lteContext)).create();
        }

        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            mSwitch.setOnCheckedChangeListener(null);
            mSwitch.setChecked(LteEnabler.mOkClicked);
            mSwitch.setOnCheckedChangeListener(lteContext);
        }
    }

    public LteEnabler(Context context, Switch switch_) {
        super(context, switch_);
        this.mSwitch = switch_;
        this.mContext = context;
        updateStatus();
    }

    private void registerObserver() {
        if (!this.hasObserverChange) {
            if (this.mIntentFilter == null) {
                this.mIntentFilter = new IntentFilter("android.intent.action.ANY_DATA_STATE");
            }
            if (!Utils.isWifiOnly(this.mContext)) {
                this.mIntentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
                this.mIntentFilter.addAction("android.net.wifi.LTEDATA_COMPLETED_ACTION");
                this.mIntentFilter.addAction("com.android.huawei.PREFERED_NETWORK_CHANGED_ToolBox");
                this.mIntentFilter.addAction("com.huawei.telephony.PREF_4G_SWITCH_DONE");
                this.mIntentFilter.addAction("com.android.huawei.PREFERRED_NETWORK_MODE_DATABASE_CHANGED");
                this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
                this.mIntentFilter.addAction("android.intent.action.PHONE_STATE");
            }
            this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
            this.hasObserverChange = true;
            this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("preferred_network_mode"), true, this.mLTESwitchObserver);
        }
    }

    private void unregisterObserver() {
        if (this.hasObserverChange) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.hasObserverChange = false;
            if (this.mLTESwitchObserver != null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mLTESwitchObserver);
            }
        }
    }

    protected void updateSwitchStatus() {
        if (this.mSwitch != null) {
            if (this.mIsChecked != this.mSwitch.isChecked()) {
                this.mSwitch.setChecked(this.mIsChecked);
            }
            this.mSwitch.setEnabled(this.mIsEnable);
        }
    }

    protected void performCheck(boolean isChecked) {
        Log.d("LteEnabler", "performCheck isChecked = " + isChecked);
        if (getLteConnectStatus(this.mContext) == isChecked) {
            Log.d("LteEnabler", "performCheck is the same");
            return;
        }
        if (Utils.isChinaTelecomArea() && isChecked && isDataConnected()) {
            PromptDialogFragment.show(this.mContext, this.mSwitch, this);
        } else {
            int i;
            HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
            if (isChecked) {
                i = 1;
            } else {
                i = 0;
            }
            hwTelephonyManager.setLteServiceAbility(i);
            this.mSwitch.setEnabled(false);
        }
    }

    private boolean isDataConnected() {
        boolean z = false;
        if (!isCurrentSlotSupportLTE(this.mContext, HwTelephonyManager.getDefault().getPreferredDataSubscription())) {
            return false;
        }
        if (2 == TelephonyManager.getDefault().getDataState()) {
            z = true;
        }
        return z;
    }

    private static boolean isCurrentSlotSupportLTE(Context context, int subscription) {
        return subscription == Utils.getUserDefaultSubscription(context);
    }

    private void updateStatus() {
        getSwitchStatus();
        updateSwitchStatus();
    }

    private void getSwitchStatus() {
        boolean z = false;
        this.mIsChecked = getLteConnectStatus(this.mContext);
        boolean air_mode = AirplaneModeEnablerHwBase.isAirplaneModeOn(this.mContext);
        if (!(!getSimState() || air_mode || isInCall())) {
            z = true;
        }
        this.mIsEnable = z;
    }

    private boolean getLteConnectStatus(Context mContext) {
        int lteMode = 1;
        if (HwTelephonyManager.getDefault().getLteServiceAbility() == 0) {
            lteMode = 0;
        }
        if (lteMode == 1) {
            return true;
        }
        return false;
    }

    public void resume() {
        this.mMainCard = HwTelephonyManager.getDefault().getDefault4GSlotId();
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

    private boolean getSimState() {
        boolean z = true;
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            if (TelephonyManager.getDefault().getSimState(this.mMainCard) != 5) {
                z = false;
            }
            return z;
        }
        if (TelephonyManager.getDefault().getSimState() != 5) {
            z = false;
        }
        return z;
    }

    private boolean isInCall() {
        return TelephonyManager.getDefault().getCallState() == 2;
    }

    public boolean isActive() {
        return this.mIsActive;
    }
}
