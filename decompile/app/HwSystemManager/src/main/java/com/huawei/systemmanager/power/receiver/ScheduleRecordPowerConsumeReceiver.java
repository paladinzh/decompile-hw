package com.huawei.systemmanager.power.receiver;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.data.stats.PowerStatsException;
import com.huawei.systemmanager.power.data.stats.PowerStatsHelper;
import com.huawei.systemmanager.power.data.stats.UidAndPower;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;
import java.util.List;

public class ScheduleRecordPowerConsumeReceiver extends HsmBroadcastReceiver {
    private static final String TAG = "ScheduleRecordPowerConsumeReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                HwLog.w(TAG, "onReceive : Invalid  action");
                return;
            }
            HwLog.i(TAG, "onReceive: Action = " + action);
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        if (ApplicationConstant.ACTION_ALARM_SCHEDULE_RECORD_POWER_CONSUME.equals(intent.getAction())) {
            List<UidAndPower> list = Lists.newArrayList();
            try {
                list = PowerStatsHelper.newInstance(context, true).getTopHighPowerApps(context, true);
            } catch (PowerStatsException e) {
                e.printStackTrace();
            }
            BatteryStatisticsHelper.insertBatteryStatistics(list);
            BatteryStatisticsHelper.refreshBatteryCache(list);
            BatteryStatisticsHelper.deleteBatteryInfo2DaysAgo();
        }
    }
}
