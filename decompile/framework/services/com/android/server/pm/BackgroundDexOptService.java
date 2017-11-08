package com.android.server.pm;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ServiceManager;
import android.util.ArraySet;
import com.android.server.am.HwBroadcastRadarUtil;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundDexOptService extends JobService {
    static final int JOB_IDLE_OPTIMIZE = 800;
    static final int JOB_POST_BOOT_UPDATE = 801;
    static final long RETRY_LATENCY = 14400000;
    static final String TAG = "BackgroundDexOptService";
    private static ComponentName sDexoptServiceName = new ComponentName("android", BackgroundDexOptService.class.getName());
    static final ArraySet<String> sFailedPackageNames = new ArraySet();
    final AtomicBoolean mAbortIdleOptimization = new AtomicBoolean(false);
    final AtomicBoolean mAbortPostBootUpdate = new AtomicBoolean(false);
    final AtomicBoolean mExitPostBootUpdate = new AtomicBoolean(false);

    public static void schedule(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService("jobscheduler");
        js.schedule(new Builder(JOB_POST_BOOT_UPDATE, sDexoptServiceName).setMinimumLatency(TimeUnit.MINUTES.toMillis(1)).setOverrideDeadline(TimeUnit.MINUTES.toMillis(1)).build());
        js.schedule(new Builder(JOB_IDLE_OPTIMIZE, sDexoptServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setPeriodic(TimeUnit.DAYS.toMillis(1)).build());
    }

    public static void notifyPackageChanged(String packageName) {
        synchronized (sFailedPackageNames) {
            sFailedPackageNames.remove(packageName);
        }
    }

    private int getBatteryLevel() {
        Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (level < 0 || scale <= 0) {
            return 0;
        }
        return (level * 100) / scale;
    }

    private boolean runPostBootUpdate(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        if (this.mExitPostBootUpdate.get()) {
            return false;
        }
        final int lowBatteryThreshold = getResources().getInteger(17694807);
        this.mAbortPostBootUpdate.set(false);
        final ArraySet<String> arraySet = pkgs;
        final PackageManagerService packageManagerService = pm;
        final JobParameters jobParameters = jobParams;
        new Thread("BackgroundDexOptService_PostBootUpdate") {
            public void run() {
                for (String pkg : arraySet) {
                    if (!BackgroundDexOptService.this.mAbortPostBootUpdate.get()) {
                        if (BackgroundDexOptService.this.mExitPostBootUpdate.get() || BackgroundDexOptService.this.getBatteryLevel() < lowBatteryThreshold) {
                            break;
                        }
                        packageManagerService.performDexOpt(pkg, false, 1, false);
                    } else {
                        return;
                    }
                }
                BackgroundDexOptService.this.jobFinished(jobParameters, false);
            }
        }.start();
        return true;
    }

    private boolean runIdleOptimization(JobParameters jobParams, PackageManagerService pm, ArraySet<String> pkgs) {
        this.mExitPostBootUpdate.set(true);
        this.mAbortIdleOptimization.set(false);
        final ArraySet<String> arraySet = pkgs;
        final PackageManagerService packageManagerService = pm;
        final JobParameters jobParameters = jobParams;
        new Thread("BackgroundDexOptService_IdleOptimization") {
            public void run() {
                ArraySet<String> SPEED_MODE_SET = new ArraySet();
                SPEED_MODE_SET.add("com.google.android.gms");
                SPEED_MODE_SET.add("com.tencent.mm");
                for (String pkg : arraySet) {
                    if (!BackgroundDexOptService.this.mAbortIdleOptimization.get()) {
                        if (!BackgroundDexOptService.sFailedPackageNames.contains(pkg)) {
                            synchronized (BackgroundDexOptService.sFailedPackageNames) {
                                BackgroundDexOptService.sFailedPackageNames.add(pkg);
                            }
                            int compileReason = 3;
                            if (SPEED_MODE_SET.contains(pkg)) {
                                compileReason = 6;
                            }
                            if (packageManagerService.performDexOpt(pkg, true, compileReason, false)) {
                                synchronized (BackgroundDexOptService.sFailedPackageNames) {
                                    BackgroundDexOptService.sFailedPackageNames.remove(pkg);
                                }
                            } else {
                                continue;
                            }
                        }
                    } else {
                        return;
                    }
                }
                BackgroundDexOptService.this.jobFinished(jobParameters, false);
            }
        }.start();
        return true;
    }

    public boolean onStartJob(JobParameters params) {
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(HwBroadcastRadarUtil.KEY_PACKAGE);
        if (pm.isStorageLow()) {
            return false;
        }
        ArraySet<String> pkgs = pm.getOptimizablePackages();
        if (pkgs == null || pkgs.isEmpty()) {
            return false;
        }
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            return runPostBootUpdate(params, pm, pkgs);
        }
        return runIdleOptimization(params, pm, pkgs);
    }

    public boolean onStopJob(JobParameters params) {
        if (params.getJobId() == JOB_POST_BOOT_UPDATE) {
            this.mAbortPostBootUpdate.set(true);
        } else {
            this.mAbortIdleOptimization.set(true);
        }
        return false;
    }
}
