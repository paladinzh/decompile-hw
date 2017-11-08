package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.BatteryStats;
import android.os.SystemClock;
import android.support.v7.preference.Preference;
import com.android.settings.Utils;

public class BatteryStatusPreference extends Preference {
    public BatteryStatusPreference(Context context, BatteryStats status) {
        super(context);
        setLayoutResource(2130968977);
        String durationString = Utils.formatElapsedTime(context, ((double) status.computeBatteryRealtime(SystemClock.elapsedRealtime() * 1000, 0)) / 1000.0d, true);
        setTitle((CharSequence) context.getString(2131625958, new Object[]{durationString}));
        setWidgetLayoutResource(2130968998);
    }
}
