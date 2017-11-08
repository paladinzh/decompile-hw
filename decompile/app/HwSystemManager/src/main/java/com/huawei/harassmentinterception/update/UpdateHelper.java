package com.huawei.harassmentinterception.update;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.receiver.InterceptionUIReceiver;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.valueprefer.ValuePrefer;
import com.huawei.systemmanager.util.HwLog;
import java.util.Random;

public class UpdateHelper {
    public static final int AUTO_UPDATE_ALL_NETWORK = 3;
    public static final int AUTO_UPDATE_CLOSE = 1;
    public static final int AUTO_UPDATE_ONLY_WIFI = 2;
    public static final String KEY_AUTO_UPDATE = "harassment_auto_update_state";
    private static final String TAG = "HarassmentInterceptionUpdateHelper";
    public static final int UPDATE_CODE_BACKGROUND = 5;
    public static final int UPDATE_CODE_FAIL = 0;
    public static final int UPDATE_CODE_INNERERROR = 4;
    public static final int UPDATE_CODE_NOT_NEEDED = 3;
    public static final int UPDATE_CODE_NOT_SUPPORT = -1;
    public static final int UPDATE_CODE_STARTED = 2;
    public static final int UPDATE_CODE_SUCCEED = 1;

    public static void scheduleAutoUpdate(Context context) {
        int rate = PreferenceHelper.getUpdateRate(context);
        long interval = ((long) rate) * 259200000;
        HwLog.i(TAG, "scheduleAutoUpdate: Auto update rate = " + rate);
        addTask(context, interval);
    }

    public static void cancelAutoUpdateSchedule(Context context) {
        Intent newIntent = new Intent(ConstValues.ACTION_ALARM_AUTO_UPDATE);
        newIntent.setClass(context, InterceptionUIReceiver.class);
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, newIntent, ShareCfg.PERMISSION_MODIFY_CALENDAR));
        context.stopServiceAsUser(new Intent(context, UpdateService.class), UserHandle.OWNER);
        HwLog.i(TAG, "cancelAutoUpdateSchedule");
    }

    private static void addTask(Context context, long interval) {
        Intent newIntent = new Intent(ConstValues.ACTION_ALARM_AUTO_UPDATE);
        newIntent.setClass(context, InterceptionUIReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, newIntent, 134217728);
        Random random = new Random();
        AlarmManager am = (AlarmManager) context.getSystemService("alarm");
        long currentTime = System.currentTimeMillis();
        long lastTime = PreferenceHelper.getLastAlarmTime(context);
        if (currentTime - lastTime > 157680000000L) {
            HwLog.i(TAG, "the time not update have more than five year ,do not update");
            PreferenceHelper.setLastAlarmTime(context, currentTime);
            return;
        }
        long firstAlarm = (interval + lastTime) + ((long) random.nextInt(21600000));
        am.setRepeating(0, firstAlarm, interval, pi);
        HwLog.i(TAG, "Auto update will be triggered at " + CommonHelper.getSystemDateStyle(context, firstAlarm));
    }

    public static int getAutoUpdateStrategy(Context ctx) {
        int state = ValuePrefer.getValueInt(ctx, "harassment_auto_update_state", -1);
        if (state != -1) {
            return state;
        }
        HwLog.i(TAG, "getAutoUpdateStrategy state is -1, use default only Wifi");
        return 2;
    }

    public static boolean setAutoUpdateStrategy(Context ctx, int strategy) {
        if (strategy == 3 || strategy == 1 || strategy == 2) {
            HwLog.i(TAG, "setAutoUpdateStrategy called, curStrategy:" + strategy + ", preStrategy:" + getAutoUpdateStrategy(ctx));
            boolean res = ValuePrefer.putValueInt(ctx, "harassment_auto_update_state", strategy);
            if (strategy != 3 && strategy != 2) {
                cancelAutoUpdateSchedule(ctx);
            } else if (PreferenceHelper.isAccessNetworkAuthorized(ctx)) {
                scheduleAutoUpdate(ctx);
            } else {
                HwLog.i(TAG, "setAutoUpdateStrategy called, but isAccessNetworkAuthorized failed!");
                return false;
            }
            return res;
        }
        HwLog.e(TAG, "setAutoUpdateStrategy illegal stratey param:" + strategy);
        return false;
    }

    public static boolean isUpdaterOpened(int strategy) {
        return strategy == 3 || strategy == 2;
    }
}
