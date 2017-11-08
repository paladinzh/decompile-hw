package com.android.settings.dashboard.conditional;

import android.graphics.drawable.Icon;
import android.os.PowerManager;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.BatterySaverSettings;

public class BatterySaverCondition extends Condition {
    public BatterySaverCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        setActive(((PowerManager) this.mManager.getContext().getSystemService(PowerManager.class)).isPowerSaveMode());
    }

    public Icon getIcon() {
        return Icon.createWithResource(this.mManager.getContext(), 2130838421);
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(2131627121);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(2131627122);
    }

    public CharSequence[] getActions() {
        return new CharSequence[]{this.mManager.getContext().getString(2131627112)};
    }

    public void onPrimaryClick() {
        Utils.startWithFragment(this.mManager.getContext(), BatterySaverSettings.class.getName(), null, null, 0, 2131626041, null);
    }

    public void onActionClick(int index) {
        if (index == 0) {
            ((PowerManager) this.mManager.getContext().getSystemService(PowerManager.class)).setPowerSaveMode(false);
            refreshState();
            return;
        }
        throw new IllegalArgumentException("Unexpected index " + index);
    }

    public int getMetricsConstant() {
        return 379;
    }
}
