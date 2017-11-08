package com.huawei.systemmanager.spacecleanner.setting;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.util.HwLog;
import java.util.Random;

public class UpdateSetting extends SpaceSwitchSetting {
    private static final int DEFAULT_AUTO_UPDATE_PERIOD_DAY = 3;
    private static final String KEY_UPDATE_LIB_TIME = "space_time_update_lib";
    private static final String TAG = UpdateSetting.class.getSimpleName();

    public UpdateSetting(String key) {
        super(key);
    }

    public void doSwitchOn() {
        schduleAutoUpdate();
    }

    public void doSwitchOff() {
        cancelAutoUpdate();
    }

    public void doAction(IUpdateListener listener) {
        Context context = GlobalContext.getContext();
        boolean flag = false;
        NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (info != null) {
            flag = info.isConnected();
        }
        if (flag) {
            ScanManager.update(context, listener);
            return;
        }
        HwLog.d(TAG, "space clean update cancel because of no network");
        if (listener != null) {
            listener.onError(IUpdateListener.ERROR_CODE_NO_NETWORK);
        }
    }

    public void doAutoUpdate() {
        doAction(new IUpdateListener() {
            public void onUpdateStarted() {
            }

            public void onUpdateFinished() {
                UpdateSetting.this.setUpdateTimeStamp();
            }

            public void onError(int errorCode) {
            }
        });
    }

    public void setUpdateTimeStamp() {
        synchronized (SpaceSwitchSetting.class) {
            mPreferences.edit().putLong(KEY_UPDATE_LIB_TIME, System.currentTimeMillis()).commit();
        }
    }

    public long getUpdateTimeStamp() {
        long j;
        synchronized (SpaceSwitchSetting.class) {
            j = mPreferences.getLong(KEY_UPDATE_LIB_TIME, 0);
        }
        return j;
    }

    private void schduleAutoUpdate() {
        Context context = GlobalContext.getContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        long lastTime = getUpdateTimeStamp();
        long currentTime = System.currentTimeMillis();
        if (lastTime <= 0 || currentTime < lastTime) {
            setUpdateTimeStamp();
            lastTime = getUpdateTimeStamp();
        }
        if (currentTime - lastTime > 157680000000L) {
            HwLog.i(TAG, "the time not update have more than five year ,do not update");
            setUpdateTimeStamp();
            return;
        }
        long firstAlramTime = (259200000 + lastTime) + getDeltaTime();
        HwLog.d(TAG, "first time to alram is " + firstAlramTime);
        alarmManager.setRepeating(1, firstAlramTime, 259200000, createAotuUpdateIntent(context));
        HwLog.d(TAG, "auto update started");
    }

    private void cancelAutoUpdate() {
        Context context = GlobalContext.getContext();
        ((AlarmManager) context.getSystemService("alarm")).cancel(createAotuUpdateIntent(context));
        HwLog.d(TAG, "auto update cancelled");
    }

    private PendingIntent createAotuUpdateIntent(Context context) {
        Intent intent = new Intent(SpaceScheduleService.ACTION_AUTO_UPDATE_SPACE_LIB);
        intent.setPackage("com.huawei.systemmanager");
        return PendingIntent.getService(context, 0, intent, 134217728);
    }

    public boolean isSwitchOn() {
        return super.isSwitchOn();
    }

    public void doAction() {
        doAction(null);
    }

    private long getDeltaTime() {
        return (long) new Random().nextInt(21600000);
    }

    public boolean isSeccussUpdateInTime() {
        long lastTime = getUpdateTimeStamp();
        long currTime = System.currentTimeMillis();
        if (lastTime <= 0) {
            setUpdateTimeStamp();
            HwLog.i(TAG, "Init update time.");
            return true;
        } else if (currTime < lastTime) {
            HwLog.i(TAG, "Time is changed to past, so update. lastTime = " + lastTime);
            return false;
        } else {
            long nextAlarm = (259200000 + lastTime) + 21600000;
            boolean isInTime = currTime < nextAlarm;
            HwLog.i(TAG, "Is auto update in time : " + isInTime + " nextAlarm = " + nextAlarm);
            return isInTime;
        }
    }
}
