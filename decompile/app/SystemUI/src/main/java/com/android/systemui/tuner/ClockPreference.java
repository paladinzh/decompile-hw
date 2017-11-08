package com.android.systemui.tuner;

import android.content.Context;
import android.support.v7.preference.DropDownPreference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService.Tunable;

public class ClockPreference extends DropDownPreference implements Tunable {
    private ArraySet<String> mBlacklist;
    private final String mClock;
    private boolean mClockEnabled;
    private boolean mHasSeconds;
    private boolean mHasSetValue;

    public ClockPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mClock = context.getString(17039410);
        setEntryValues(new CharSequence[]{"seconds", "default", "disabled"});
    }

    public void onAttached() {
        super.onAttached();
        TunerService.get(getContext()).addTunable((Tunable) this, "icon_blacklist", "clock_seconds");
    }

    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    public void onTuningChanged(String key, String newValue) {
        boolean z = false;
        if ("icon_blacklist".equals(key)) {
            this.mBlacklist = StatusBarIconController.getIconBlacklist(newValue);
            if (!this.mBlacklist.contains(this.mClock)) {
                z = true;
            }
            this.mClockEnabled = z;
        } else if ("clock_seconds".equals(key)) {
            if (!(newValue == null || Integer.parseInt(newValue) == 0)) {
                z = true;
            }
            this.mHasSeconds = z;
        }
        if (!this.mHasSetValue) {
            this.mHasSetValue = true;
            if (this.mClockEnabled && this.mHasSeconds) {
                setValue("seconds");
            } else if (this.mClockEnabled) {
                setValue("default");
            } else {
                setValue("disabled");
            }
        }
    }

    protected boolean persistString(String value) {
        TunerService.get(getContext()).setValue("clock_seconds", "seconds".equals(value) ? 1 : 0);
        if ("disabled".equals(value)) {
            this.mBlacklist.add(this.mClock);
        } else {
            this.mBlacklist.remove(this.mClock);
        }
        TunerService.get(getContext()).setValue("icon_blacklist", TextUtils.join(",", this.mBlacklist));
        return true;
    }
}
