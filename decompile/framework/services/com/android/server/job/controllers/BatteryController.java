package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManagerInternal;
import android.os.UserHandle;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.StateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BatteryController extends StateController {
    private static final String TAG = "JobScheduler.Batt";
    private static volatile BatteryController sController;
    private static final Object sCreationLock = new Object();
    private ChargingTracker mChargeTracker = new ChargingTracker();
    private List<JobStatus> mTrackedTasks = new ArrayList();

    public class ChargingTracker extends BroadcastReceiver {
        private boolean mBatteryHealthy;
        private boolean mCharging;

        public void startTracking() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_LOW");
            filter.addAction("android.intent.action.BATTERY_OKAY");
            filter.addAction("android.os.action.CHARGING");
            filter.addAction("android.os.action.DISCHARGING");
            BatteryController.this.mContext.registerReceiver(this, filter);
            BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            this.mBatteryHealthy = !batteryManagerInternal.getBatteryLevelLow();
            this.mCharging = batteryManagerInternal.isPowered(7);
        }

        boolean isOnStablePower() {
            return this.mCharging ? this.mBatteryHealthy : false;
        }

        public void onReceive(Context context, Intent intent) {
            onReceiveInternal(intent);
        }

        public void onReceiveInternal(Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_LOW".equals(action)) {
                this.mBatteryHealthy = false;
            } else if ("android.intent.action.BATTERY_OKAY".equals(action)) {
                this.mBatteryHealthy = true;
                BatteryController.this.maybeReportNewChargingState();
            } else if ("android.os.action.CHARGING".equals(action)) {
                this.mCharging = true;
                BatteryController.this.maybeReportNewChargingState();
            } else if ("android.os.action.DISCHARGING".equals(action)) {
                this.mCharging = false;
                BatteryController.this.maybeReportNewChargingState();
            }
        }
    }

    public static BatteryController get(JobSchedulerService taskManagerService) {
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new BatteryController(taskManagerService, taskManagerService.getContext(), taskManagerService.getLock());
            }
        }
        return sController;
    }

    public ChargingTracker getTracker() {
        return this.mChargeTracker;
    }

    public static BatteryController getForTesting(StateChangedListener stateChangedListener, Context context) {
        return new BatteryController(stateChangedListener, context, new Object());
    }

    private BatteryController(StateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        this.mChargeTracker.startTracking();
    }

    public void maybeStartTrackingJobLocked(JobStatus taskStatus, JobStatus lastJob) {
        boolean isOnStablePower = this.mChargeTracker.isOnStablePower();
        if (taskStatus.hasChargingConstraint()) {
            this.mTrackedTasks.add(taskStatus);
            taskStatus.setChargingConstraintSatisfied(isOnStablePower);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus taskStatus, JobStatus incomingJob, boolean forUpdate) {
        if (taskStatus.hasChargingConstraint()) {
            this.mTrackedTasks.remove(taskStatus);
        }
    }

    private void maybeReportNewChargingState() {
        boolean stablePower = this.mChargeTracker.isOnStablePower();
        boolean reportChange = false;
        synchronized (this.mLock) {
            for (JobStatus ts : this.mTrackedTasks) {
                if (ts.setChargingConstraintSatisfied(stablePower) != stablePower) {
                    reportChange = true;
                }
            }
        }
        if (reportChange) {
            this.mStateChangedListener.onControllerStateChanged();
        }
        if (stablePower) {
            this.mStateChangedListener.onRunJobNow(null);
        }
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("Battery: stable power = ");
        pw.println(this.mChargeTracker.isOnStablePower());
        pw.print("Tracking ");
        pw.print(this.mTrackedTasks.size());
        pw.println(":");
        for (int i = 0; i < this.mTrackedTasks.size(); i++) {
            JobStatus js = (JobStatus) this.mTrackedTasks.get(i);
            if (js.shouldDump(filterUid)) {
                pw.print("  #");
                js.printUniqueId(pw);
                pw.print(" from ");
                UserHandle.formatUid(pw, js.getSourceUid());
                pw.println();
            }
        }
    }
}
