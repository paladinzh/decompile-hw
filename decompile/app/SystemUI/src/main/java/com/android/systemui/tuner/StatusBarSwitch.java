package com.android.systemui.tuner;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.Set;

public class StatusBarSwitch extends SwitchPreference implements Tunable {
    private Set<String> mBlacklist;

    public StatusBarSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onAttached() {
        super.onAttached();
        TunerService.get(getContext()).addTunable((Tunable) this, "icon_blacklist");
    }

    public void onDetached() {
        TunerService.get(getContext()).removeTunable(this);
        super.onDetached();
    }

    public void onTuningChanged(String key, String newValue) {
        if ("icon_blacklist".equals(key)) {
            boolean z;
            this.mBlacklist = StatusBarIconController.getIconBlacklist(newValue);
            if (this.mBlacklist.contains(getKey())) {
                z = false;
            } else {
                z = true;
            }
            setChecked(z);
        }
    }

    protected boolean persistBoolean(boolean value) {
        if (value) {
            if (this.mBlacklist.remove(getKey())) {
                MetricsLogger.action(getContext(), 233, getKey());
                setList(this.mBlacklist);
            }
        } else if (!this.mBlacklist.contains(getKey())) {
            MetricsLogger.action(getContext(), 234, getKey());
            this.mBlacklist.add(getKey());
            setList(this.mBlacklist);
        }
        return true;
    }

    private void setList(Set<String> blacklist) {
        Secure.putStringForUser(getContext().getContentResolver(), "icon_blacklist", TextUtils.join(",", blacklist), UserSwitchUtils.getCurrentUser());
    }
}
