package com.huawei.systemmanager.optimize.monitor;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.PhoneAccelerate;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.spacecleanner.SpaceCleanActivity;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public final class MemCPUMonitor {
    private static final int CAL_MEM_CPU_MESSAGE = 1;
    private static final int CYCLE_TIME = 6;
    private static final long DELAY_TIME = 5000;
    public static final int NOTIFY_DEVICE_ACCELERATE_ID = 2131231257;
    private static final long SLEEP_TIME = 200;
    private static final int START_MONITOR_MESSAGE = 0;
    private static final int STOP_MONITOR_MESSAGE = 2;
    private static final String TAG = "MemCPUMonitor";
    private static MemCPUMonitor mMonitorInstance = null;
    private boolean isCompleted = true;
    private Context mContext = null;
    private CPUInfoManager mCpuInfomanager = null;
    private MemoryInfoManager mMemoryInfoManager = null;
    private NotificationManager mNotificationManager = null;
    private int mSampleCount = 0;
    private Handler mScreenOnHandler = null;
    private ScreenSwitchReceiver mScreenSwitchReceiver = null;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private long mTotalSampeMemory = 0;
    private long mTotalSampleCPU = 0;

    private class ScreenSwitchReceiver extends BroadcastReceiver {
        private ScreenSwitchReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                    MemCPUMonitor.this.mScreenOnHandler.sendEmptyMessage(0);
                } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                    MemCPUMonitor.this.mScreenOnHandler.sendEmptyMessage(2);
                }
            }
        }
    }

    public static synchronized MemCPUMonitor getInstance(Context context) {
        MemCPUMonitor memCPUMonitor;
        synchronized (MemCPUMonitor.class) {
            if (mMonitorInstance == null) {
                mMonitorInstance = new MemCPUMonitor(context);
            }
            memCPUMonitor = mMonitorInstance;
        }
        return memCPUMonitor;
    }

    private MemCPUMonitor(Context context) {
        this.mContext = context;
        this.mScreenSwitchReceiver = new ScreenSwitchReceiver();
        this.mCpuInfomanager = new CPUInfoManager();
        this.mMemoryInfoManager = new MemoryInfoManager(context);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        initScreenOnHandler();
        registerScreenActionReceiver();
    }

    public void setMemCPUMonitorComplete(boolean isCompleteFlag) {
        this.isCompleted = isCompleteFlag;
    }

    public boolean isMemCPUMonitorCompleted() {
        return this.isCompleted;
    }

    public void startTimerMonitor() {
        if (this.isCompleted || isMonitorNotificationSwithOff()) {
            HwLog.e(TAG, "startTimerMonitor isCompleted = " + this.isCompleted);
            return;
        }
        stopTimerMonitor();
        this.mNotificationManager.cancel(R.string.optimize_app_protected);
        if (isScreenOn()) {
            startTimer();
        }
    }

    private boolean isMonitorNotificationSwithOff() {
        return MemCPUMonitorSwitchManager.getMemCPUSwitchState(this.mContext) == 0;
    }

    public void stopTimerMonitor() {
        stopTimer();
        cleanSampleData();
        setMemCPUMonitorComplete(false);
    }

    private void initScreenOnHandler() {
        this.mScreenOnHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        HwLog.v(MemCPUMonitor.TAG, "MemCPUMonitor : Send START_MONITOR_MESSAGE message.");
                        MemCPUMonitor.this.startTimerMonitor();
                        break;
                    case 1:
                        HwLog.v(MemCPUMonitor.TAG, "MemCPUMonitor : Send CAL_MEM_CPU_MESSAGE.");
                        MemCPUMonitor.this.calCPUMemValue(msg);
                        break;
                    case 2:
                        HwLog.v(MemCPUMonitor.TAG, "MemCPUMonitor : STOP_MONITOR_MESSAGE.");
                        if (!MemCPUMonitor.this.isCompleted) {
                            MemCPUMonitor.this.stopTimerMonitor();
                            break;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    private void calCPUMemValue(Message msg) {
        try {
            Bundle bundle = msg.getData();
            int lastTotalCPUTime = bundle.getInt(CPUInfoManager.TOTAL_CPU_TIME);
            int lastIdleCPUTime = bundle.getInt(CPUInfoManager.IDLE_CPU_TIME);
            Map<String, Integer> cpuinfo = this.mCpuInfomanager.getCpuInfo();
            int totalCpuTimeDiff = ((Integer) cpuinfo.get(CPUInfoManager.TOTAL_CPU_TIME)).intValue() - lastTotalCPUTime;
            this.mTotalSampleCPU += (long) (((totalCpuTimeDiff - (((Integer) cpuinfo.get(CPUInfoManager.IDLE_CPU_TIME)).intValue() - lastIdleCPUTime)) * 100) / totalCpuTimeDiff);
            this.mTotalSampeMemory += this.mMemoryInfoManager.getAvailMemory();
        } catch (Exception e) {
            HwLog.e(TAG, "MemCPUMonitor.calCPUMemValue(): Exception" + e.toString());
        }
    }

    private void registerScreenActionReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mScreenSwitchReceiver, filter);
    }

    private boolean isScreenOn() {
        return ((PowerManager) this.mContext.getSystemService(BatteryStatisticsHelper.DB_POWER)).isScreenOn();
    }

    private boolean isMemoryBelowLimit(long curMemory) {
        return curMemory <= this.mMemoryInfoManager.getMemoryLimit(this.mContext);
    }

    private void cleanSampleData() {
        this.mTotalSampeMemory = 0;
        this.mTotalSampleCPU = 0;
        this.mSampleCount = 0;
    }

    private void startTimer() {
        if (this.mTimer == null) {
            this.mTimer = new Timer();
        }
        if (this.mTimerTask == null) {
            this.mTimerTask = new TimerTask() {
                public void run() {
                    if (MemCPUMonitor.this.isMonitorNotificationSwithOff()) {
                        MemCPUMonitor.this.stopTimer();
                        MemCPUMonitor.this.cleanSampleData();
                        return;
                    }
                    if (MemCPUMonitor.this.mSampleCount >= 6) {
                        MemCPUMonitor.this.stopTimer();
                        long averageSampleCPU = MemCPUMonitor.this.mTotalSampleCPU / 6;
                        long averagemSampeMemory = (MemCPUMonitor.this.mTotalSampeMemory / 1048576) / 6;
                        HwLog.v(MemCPUMonitor.TAG, "MemCPUMonitor.startTimer complete: averageSampleCPU." + averageSampleCPU);
                        HwLog.v(MemCPUMonitor.TAG, "MemCPUMonitor.startTimer complete: averagemSampeMemory." + averagemSampeMemory);
                        MemCPUMonitor.this.setMemCPUMonitorComplete(true);
                        if (averageSampleCPU >= 70 || MemCPUMonitor.this.isMemoryBelowLimit(averagemSampeMemory)) {
                            MemCPUMonitor.this.showNotification();
                        }
                    } else if (MemCPUMonitor.this.isScreenOn()) {
                        Message msg = new Message();
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        Map<String, Integer> cpuinfo = MemCPUMonitor.this.mCpuInfomanager.getCpuInfo();
                        bundle.putInt(CPUInfoManager.TOTAL_CPU_TIME, ((Integer) cpuinfo.get(CPUInfoManager.TOTAL_CPU_TIME)).intValue());
                        bundle.putInt(CPUInfoManager.IDLE_CPU_TIME, ((Integer) cpuinfo.get(CPUInfoManager.IDLE_CPU_TIME)).intValue());
                        msg.setData(bundle);
                        MemCPUMonitor.this.mScreenOnHandler.sendMessageDelayed(msg, 200);
                        MemCPUMonitor memCPUMonitor = MemCPUMonitor.this;
                        memCPUMonitor.mSampleCount = memCPUMonitor.mSampleCount + 1;
                    } else {
                        HwLog.v(MemCPUMonitor.TAG, "MemCPUMonitor.startTimer(): sample task teminete due to screen off");
                        MemCPUMonitor.this.stopTimerMonitor();
                    }
                }
            };
        }
        try {
            HwLog.d(TAG, "start schedule task");
            this.mTimer.schedule(this.mTimerTask, 0, DELAY_TIME);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void stopTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
        if (this.mTimerTask != null) {
            this.mTimerTask.cancel();
            this.mTimerTask = null;
        }
    }

    public void showNotification() {
        Intent inetent = new Intent(this.mContext, SpaceCleanActivity.class);
        inetent.setFlags(603979776);
        inetent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, PhoneAccelerate.ACTION_CLICK_RUN_SLOW_NOTIFICATION);
        this.mNotificationManager.notify(R.string.optimize_app_protected, new Builder(this.mContext).setAutoCancel(true).setDefaults(0).setTicker(this.mContext.getString(R.string.memcpu_notification_Ticker)).setSmallIcon(R.drawable.ic_phone_slow_notification).setContentTitle(this.mContext.getString(R.string.memcpu_notification_Title)).setContentText(this.mContext.getString(R.string.memcpu_notification_Content)).setWhen(System.currentTimeMillis()).setContentIntent(PendingIntent.getActivity(this.mContext, 0, inetent, 0)).build());
    }
}
