package com.huawei.systemmanager.applock.utils.compatibility;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.timekeeper.TimeKeeper;
import com.huawei.timekeeper.TimeTickInfo;

public class AuthRetryUtil {

    public interface TimeKeeperSuffix {
        public static final String SUFFIX_APPLOCK_ANSWER = "_APPLOCK_ANSWER";
        public static final String SUFFIX_APPLOCK_PASSWORD = "_APPLOCK_PWD";
    }

    public static TimeKeeper getTimeKeeper(Context ctx, String suffix) {
        return TimeKeeper.getInstance(ctx, ctx.getPackageName() + suffix, 0, 1);
    }

    public static void resetTimeKeeper(Context ctx, String suffix) {
        getTimeKeeper(ctx, suffix).resetErrorCount(ctx);
    }

    public static boolean isTimeKeeperLocking(Context ctx, String suffix) {
        return getTimeKeeper(ctx, suffix).getRemainingChance() == 0;
    }

    public static String getRemainingLockoutTime(Context ctx, TimeTickInfo timeTickInfo) {
        HwLog.i("AuthRetryUtil", "getRemainingLockoutTime: " + timeTickInfo.toString());
        if (timeTickInfo.getHour() > 0) {
            return ctx.getResources().getQuantityString(R.plurals.applock_lockout_remaining_hour, timeTickInfo.getHour(), new Object[]{Integer.valueOf(timeTickInfo.getHour())});
        } else if (timeTickInfo.getMinute() > 0) {
            return ctx.getResources().getQuantityString(R.plurals.applock_lockout_remaining_minute, timeTickInfo.getMinute(), new Object[]{Integer.valueOf(timeTickInfo.getMinute())});
        } else {
            return ctx.getResources().getQuantityString(R.plurals.applock_lockout_remaining_second, timeTickInfo.getSecond(), new Object[]{Integer.valueOf(timeTickInfo.getSecond())});
        }
    }
}
