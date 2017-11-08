package com.android.deskclock.alarmclock;

import android.content.Context;
import android.util.SparseBooleanArray;

public class HwCustWeekStartDayController {
    public boolean handleCustomWeekStartDay(SparseBooleanArray isSelected, boolean[] array) {
        return false;
    }

    public int getFirstDayOfWeekFromDB(Context context, int firstdayofweek) {
        return firstdayofweek;
    }
}
