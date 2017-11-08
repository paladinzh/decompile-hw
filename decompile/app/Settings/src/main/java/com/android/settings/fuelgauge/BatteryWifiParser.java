package com.android.settings.fuelgauge;

import android.os.BatteryStats.HistoryItem;

public class BatteryWifiParser extends BatteryFlagParser {
    public BatteryWifiParser(int accentColor) {
        super(accentColor, false, 0);
    }

    protected boolean isSet(HistoryItem record) {
        switch ((record.states2 & 15) >> 0) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 11:
            case 12:
                return false;
            default:
                return true;
        }
    }
}
