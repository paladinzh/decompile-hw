package com.android.deskclock.alarmclock;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import java.util.HashMap;

public class HwCustWeekStartDayControllerImpl extends HwCustWeekStartDayController {
    private static final String WEEK_START_DEFAULT = "-1";
    String custContry = SystemProperties.get("ro.config.hw_opta", "");
    String custOperator = SystemProperties.get("ro.config.hw_optb", "");
    private boolean isSupportReadFirstDayOfWeekFromXML = "true".equals(SystemProperties.get("ro.config.firstWeekdayFromXML", "false"));

    public boolean handleCustomWeekStartDay(HashMap<Integer, Boolean> isSelected, boolean[] mDayOfWeeks) {
        boolean isOrange;
        if (this.custContry.equals("109")) {
            isOrange = this.custOperator.equals("724");
        } else {
            isOrange = false;
        }
        if (!isOrange) {
            return false;
        }
        int count = mDayOfWeeks.length;
        for (int i = 0; i < count; i++) {
            isSelected.put(Integer.valueOf(i), Boolean.valueOf(mDayOfWeeks[i]));
        }
        return true;
    }

    public int getFirstDayOfWeekFromDB(Context context, int firstdayofweek) {
        if (!this.isSupportReadFirstDayOfWeekFromXML) {
            return firstdayofweek;
        }
        String value = System.getString(context.getContentResolver(), "first_day_of_week");
        if (value == null || WEEK_START_DEFAULT.equals(value)) {
            return firstdayofweek;
        }
        return (Integer.parseInt(value) + 5) % 7;
    }
}
