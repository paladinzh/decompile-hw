package com.android.server.rms;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.rms.utils.Interrupt;
import com.android.server.rms.utils.Utils;
import com.android.server.wifipro.WifiProCommonDefs;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class CompactJobService extends JobService {
    private static final int COMPACT_PERIOD_INTERVAL = (Utils.DEBUG ? WifiProCommonDefs.QUERY_TIMEOUT_MS : 28800000);
    private static final int FAILED_PERIOD_INTERVAL;
    private static final int JOB_ID = 1684366962;
    static final String TAG = "CompactJobService";
    private static ComponentName sCompactServiceName = new ComponentName("android", CompactJobService.class.getName());
    private static final ArrayList<IDefraggler> sDefragglers = new ArrayList();
    private static LocalLog sHistory = new LocalLog(20);
    private static AtomicInteger sTimes = new AtomicInteger(0);
    private final Interrupt mInterrupt = new Interrupt();

    static {
        int i = WifiProCommonDefs.QUERY_TIMEOUT_MS;
        if (!Utils.DEBUG) {
            i = 3600000;
        }
        FAILED_PERIOD_INTERVAL = i;
    }

    public static void schedule(Context context) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).build());
    }

    private static void delay_schedule(Context context, long delay) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setMinimumLatency(delay).build());
    }

    public boolean onStartJob(final JobParameters params) {
        Log.w(TAG, "onIdleStart");
        if (sDefragglers.size() <= 0) {
            return false;
        }
        JobParameters jobParams = params;
        new Thread("CompactJobService_Handler") {
            public void run() {
                int -get0;
                boolean bFinished = true;
                for (IDefraggler defraggler : CompactJobService.sDefragglers) {
                    if (CompactJobService.this.mInterrupt.checkInterruptAndReset()) {
                        bFinished = false;
                        break;
                    }
                    defraggler.compact("background compact", null);
                }
                CompactJobService.this.mInterrupt.reset();
                CompactJobService.sTimes.getAndIncrement();
                CompactJobService.sHistory.log("do compact " + bFinished + " times = " + CompactJobService.sTimes.get());
                CompactJobService.this.jobFinished(params, false);
                Context context = CompactJobService.this;
                if (bFinished) {
                    -get0 = CompactJobService.COMPACT_PERIOD_INTERVAL;
                } else {
                    -get0 = CompactJobService.FAILED_PERIOD_INTERVAL;
                }
                CompactJobService.delay_schedule(context, (long) -get0);
            }
        }.start();
        return true;
    }

    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "onIdleStop");
        this.mInterrupt.trigger();
        for (IDefraggler defraggler : sDefragglers) {
            defraggler.interrupt();
        }
        return false;
    }

    protected static void dumpLog(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CompactJobService dump");
        sHistory.dump(fd, pw, args);
    }

    public static void addDefragglers(IDefraggler defraggler) {
        sDefragglers.add(defraggler);
    }
}
