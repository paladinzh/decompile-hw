package com.android.deskclock.provider;

import android.content.Context;
import android.os.SystemClock;
import com.android.deskclock.alarmclock.Alarms;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.Log;
import com.android.util.Utils;

public class DataLoadHelper {
    public static void loadCalendarData(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();
                Utils.setsIsZhArea(context);
                if (Utils.isChinaRegionalVersion()) {
                    DayOfWeekRepeatUtil.initGetRestWork(context);
                    if (!DayOfWeekRepeatUtil.isHasWorkDayfn()) {
                        DataLoadHelper.downloadData(context);
                    }
                    if (DayOfWeekRepeatUtil.isHasWorkDayfn()) {
                        DayOfWeekRepeatUtil.isAcrossYearsNow(context);
                    }
                }
                SystemClock.sleep(50);
                Utils.initTypeface(context);
                Log.dRelease("DataLoadHelper", Thread.currentThread().getId() + " complete-time enough = " + (System.currentTimeMillis() - startTime));
            }
        }).start();
    }

    private static void downloadData(Context context) {
        if (Alarms.isContainWorkDayAlarm(context.getContentResolver())) {
            DayOfWeekRepeatUtil.getCalendarWorldData(context);
            Log.dRelease("DataLoadHelper", "downloadData : will download.");
            return;
        }
        Log.iRelease("DataLoadHelper", "no work day alarm.");
    }
}
