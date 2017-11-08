package com.android.settings;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.android.settingslib.WirelessUtils;

public class AirplaneModeEnabler extends AirplaneModeEnablerHwBase {
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            AirplaneModeEnabler.this.onAirplaneModeChanged();
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    AirplaneModeEnabler.this.onAirplaneModeChanged();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsActive;
    private PhoneStateIntentReceiver mPhoneStateReceiver = new PhoneStateIntentReceiver(this.mContext, this.mHandler);

    public /* bridge */ /* synthetic */ void enableSwitchPreference(int blutoothState) {
        super.enableSwitchPreference(blutoothState);
    }

    public /* bridge */ /* synthetic */ void onCheckedChanged(CompoundButton mSwitchPref, boolean isChecked) {
        super.onCheckedChanged(mSwitchPref, isChecked);
    }

    public AirplaneModeEnabler(Context context, Switch switch_) {
        super(context, switch_);
        this.mPhoneStateReceiver.notifyServiceState(3);
    }

    public void resume() {
        super.resume();
        this.mPhoneStateReceiver.registerIntent();
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        this.mIsActive = true;
    }

    public void pause() {
        super.pause();
        this.mPhoneStateReceiver.unregisterIntent();
        this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
        this.mIsActive = false;
    }

    private void onAirplaneModeChanged() {
        boolean isChecked = WirelessUtils.isAirplaneModeOn(this.mContext);
        if (this.mSwitch != null && isChecked != this.mSwitch.isChecked()) {
            this.mSwitch.setOnCheckedChangeListener(null);
            this.mSwitch.setChecked(isChecked);
            this.mSwitch.setOnCheckedChangeListener(this);
        }
    }

    public boolean isActive() {
        return this.mIsActive;
    }
}
