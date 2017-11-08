package com.android.settings.dashboard;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class BaseSwitchEnabler implements OnCheckedChangeListener {
    protected Switch mSwitch;

    public BaseSwitchEnabler(Context context, Switch switch_) {
        this.mSwitch = switch_;
    }

    public void resume() {
        if (this.mSwitch != null) {
            updateSwitchStatus();
            this.mSwitch.setOnCheckedChangeListener(this);
        }
    }

    public void pause() {
        if (this.mSwitch != null) {
            this.mSwitch.setOnCheckedChangeListener(null);
        }
    }

    public void setSwitch(Switch switch_) {
        if (this.mSwitch != null) {
            this.mSwitch.setOnCheckedChangeListener(null);
        }
        this.mSwitch = switch_;
        updateSwitchStatus();
        if (switch_ != null) {
            this.mSwitch.setOnCheckedChangeListener(this);
        }
    }

    protected void updateSwitchStatus() {
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        performCheck(isChecked);
        notifyOthers(isChecked);
    }

    protected void performCheck(boolean isChecked) {
    }

    protected void notifyOthers(boolean isChecked) {
    }
}
