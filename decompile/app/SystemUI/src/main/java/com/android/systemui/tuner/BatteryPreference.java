package com.android.systemui.tuner;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.DropDownPreference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService.Tunable;

public class BatteryPreference extends DropDownPreference implements Tunable {
    private final String mBattery;
    private boolean mBatteryEnabled;
    private ArraySet<String> mBlacklist;
    private boolean mHasPercentage;
    private boolean mHasSetValue;

    public BatteryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBattery = context.getString(17039407);
        setEntryValues(new CharSequence[]{"percent", "default", "disabled"});
    }

    public void onAttached() {
        boolean z = true;
        super.onAttached();
        TunerService.get(getContext()).addTunable((Tunable) this, "icon_blacklist");
        if (System.getInt(getContext().getContentResolver(), "status_bar_show_battery_percent", 0) == 0) {
            z = false;
        }
        this.mHasPercentage = z;
    }

    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    public void onTuningChanged(String key, String newValue) {
        if ("icon_blacklist".equals(key)) {
            boolean z;
            this.mBlacklist = StatusBarIconController.getIconBlacklist(newValue);
            if (this.mBlacklist.contains(this.mBattery)) {
                z = false;
            } else {
                z = true;
            }
            this.mBatteryEnabled = z;
        }
        if (!this.mHasSetValue) {
            this.mHasSetValue = true;
            if (this.mBatteryEnabled && this.mHasPercentage) {
                setValue("percent");
            } else if (this.mBatteryEnabled) {
                setValue("default");
            } else {
                setValue("disabled");
            }
        }
    }

    protected boolean persistString(String value) {
        boolean v = "percent".equals(value);
        MetricsLogger.action(getContext(), 237, v);
        System.putInt(getContext().getContentResolver(), "status_bar_show_battery_percent", v ? 1 : 0);
        if ("disabled".equals(value)) {
            this.mBlacklist.add(this.mBattery);
        } else {
            this.mBlacklist.remove(this.mBattery);
        }
        TunerService.get(getContext()).setValue("icon_blacklist", TextUtils.join(",", this.mBlacklist));
        return true;
    }
}
