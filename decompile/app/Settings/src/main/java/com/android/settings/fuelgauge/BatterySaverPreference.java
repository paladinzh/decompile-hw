package com.android.settings.fuelgauge;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import com.android.settings.Utils;

public class BatterySaverPreference extends Preference {
    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            BatterySaverPreference.this.updateSwitch();
        }
    };
    private PowerManager mPowerManager;

    public BatterySaverPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void performClick(View view) {
        Utils.startWithFragment(getContext(), getFragment(), null, null, 0, 0, getTitle());
    }

    public void onAttached() {
        super.onAttached();
        this.mPowerManager = (PowerManager) getContext().getSystemService("power");
        this.mObserver.onChange(true);
        getContext().getContentResolver().registerContentObserver(Global.getUriFor("low_power_trigger_level"), true, this.mObserver);
        getContext().getContentResolver().registerContentObserver(Global.getUriFor("low_power"), true, this.mObserver);
    }

    public void onDetached() {
        super.onDetached();
        getContext().getContentResolver().unregisterContentObserver(this.mObserver);
    }

    private void updateSwitch() {
        int format;
        int percentFormat;
        Context context = getContext();
        if (this.mPowerManager.isPowerSaveMode()) {
            format = 2131627173;
        } else {
            format = 2131627174;
        }
        if (Global.getInt(context.getContentResolver(), "low_power_trigger_level", 0) > 0) {
            percentFormat = 2131627176;
        } else {
            percentFormat = 2131627175;
        }
        Object[] objArr = new Object[1];
        objArr[0] = context.getString(percentFormat, new Object[]{com.android.settingslib.Utils.formatPercentage(Global.getInt(context.getContentResolver(), "low_power_trigger_level", 0))});
        setSummary(context.getString(format, objArr));
    }
}
