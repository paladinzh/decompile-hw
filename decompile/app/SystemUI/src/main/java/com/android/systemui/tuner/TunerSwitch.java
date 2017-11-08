package com.android.systemui.tuner;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.util.AttributeSet;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R$styleable;
import com.android.systemui.tuner.TunerService.Tunable;

public class TunerSwitch extends SwitchPreference implements Tunable {
    private final int mAction;
    private final boolean mDefault;

    public TunerSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.TunerSwitch);
        this.mDefault = a.getBoolean(0, false);
        this.mAction = a.getInt(1, -1);
    }

    public void onAttached() {
        super.onAttached();
        TunerService.get(getContext()).addTunable((Tunable) this, getKey().split(","));
    }

    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    public void onTuningChanged(String key, String newValue) {
        boolean z = false;
        if (newValue == null) {
            z = this.mDefault;
        } else if (Integer.parseInt(newValue) != 0) {
            z = true;
        }
        setChecked(z);
    }

    protected void onClick() {
        super.onClick();
        if (this.mAction != -1) {
            MetricsLogger.action(getContext(), this.mAction, isChecked());
        }
    }

    protected boolean persistBoolean(boolean value) {
        for (String key : getKey().split(",")) {
            Secure.putString(getContext().getContentResolver(), key, value ? "1" : "0");
        }
        return true;
    }
}
