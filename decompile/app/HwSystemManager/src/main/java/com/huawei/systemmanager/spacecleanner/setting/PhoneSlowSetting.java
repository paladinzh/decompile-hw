package com.huawei.systemmanager.spacecleanner.setting;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.optimize.monitor.MemCPUMonitor;
import com.huawei.systemmanager.optimize.monitor.MemCPUMonitorSwitchManager;
import com.huawei.systemmanager.util.HwLog;

public class PhoneSlowSetting extends SpaceSwitchSetting {
    public static final String KEY = "processmanagersetting";
    private static final int PHONE_SLOW_CHECK_TIME = 20;
    public static final String TAG = "PhoneSlowSetting";

    public PhoneSlowSetting() {
        super("processmanagersetting");
    }

    public void doSwitchOn() {
        updateNotifyChoice(true);
        schdulePhoneSlowNotify();
    }

    public void doSwitchOff() {
        ((NotificationManager) GlobalContext.getContext().getSystemService("notification")).cancel(R.string.optimize_app_protected);
        updateNotifyChoice(false);
        cancelPhoneSlowNotify();
    }

    public boolean isSwitchOn() {
        return MemCPUMonitorSwitchManager.isMemCpuSwitchOn(GlobalContext.getContext());
    }

    public void setValue(Boolean value) {
        if (value.booleanValue()) {
            doSwitchOn();
        } else {
            doSwitchOff();
        }
    }

    public void doSettingChanged(Boolean change, boolean manually) {
        super.doSettingChanged(change, manually);
        if (manually) {
            String param = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(change));
            HsmStat.statE(15, param);
        }
    }

    public void doAction() {
        HwLog.d(TAG, "start to check phone slow");
        MemCPUMonitor monitor = MemCPUMonitor.getInstance(GlobalContext.getContext());
        monitor.stopTimerMonitor();
        monitor.startTimerMonitor();
    }

    public void onBackup(String value) {
        int beforeIntValue = -1;
        try {
            beforeIntValue = Integer.parseInt(value);
        } catch (Exception e) {
            HwLog.i("processmanagersetting", "onBacup, value is not integer");
        }
        boolean backupValue = Boolean.valueOf(value).booleanValue();
        if (beforeIntValue >= 0) {
            backupValue = beforeIntValue == 1;
        }
        setValue(Boolean.valueOf(backupValue));
    }

    private void updateNotifyChoice(boolean stat) {
        int checked;
        if (stat) {
            checked = 1;
        } else {
            checked = 0;
        }
        MemCPUMonitorSwitchManager.setMemCPUSwitchState(GlobalContext.getContext(), checked);
    }

    private void schdulePhoneSlowNotify() {
        HwLog.d(TAG, "Start to schdulePhoneSlowNotify");
        Context context = GlobalContext.getContext();
        ((AlarmManager) context.getSystemService("alarm")).setRepeating(1, getTriggerTime(20, 0), 3600000, createPhoneSlowNotifyIntent(context));
    }

    private void cancelPhoneSlowNotify() {
        Context context = GlobalContext.getContext();
        ((AlarmManager) context.getSystemService("alarm")).cancel(createPhoneSlowNotifyIntent(context));
        HwLog.d(TAG, "cancelLowerSpaceNotify");
    }

    private PendingIntent createPhoneSlowNotifyIntent(Context context) {
        Intent intent = new Intent(SpaceScheduleService.ACTION_PHONE_SLOW_NOTIFY);
        intent.setPackage("com.huawei.systemmanager");
        return PendingIntent.getService(context, 0, intent, 134217728);
    }

    private long getTriggerTime(int hour, int minute) {
        Time time = new Time();
        long nowTime = System.currentTimeMillis();
        time.set(nowTime);
        time.hour = hour;
        time.minute = minute;
        time.second = 0;
        return nowTime + 3600000;
    }
}
