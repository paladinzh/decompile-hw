package com.android.server.job;

import android.app.ActivityManager;
import android.app.job.IJobCallback.Stub;
import android.app.job.IJobService;
import android.app.job.JobParameters;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.server.job.controllers.JobStatus;
import java.util.concurrent.atomic.AtomicBoolean;

public class JobServiceContext extends Stub implements ServiceConnection {
    private static final boolean DEBUG = false;
    private static final long EXECUTING_TIMESLICE_MILLIS = 600000;
    private static final int MSG_CALLBACK = 1;
    private static final int MSG_CANCEL = 3;
    private static final int MSG_SERVICE_BOUND = 2;
    private static final int MSG_SHUTDOWN_EXECUTION = 4;
    private static final int MSG_TIMEOUT = 0;
    public static final int NO_PREFERRED_UID = -1;
    private static final long OP_TIMEOUT_MILLIS = 8000;
    private static final String TAG = "JobServiceContext";
    static final int VERB_BINDING = 0;
    static final int VERB_EXECUTING = 2;
    static final int VERB_FINISHED = 4;
    static final int VERB_STARTING = 1;
    static final int VERB_STOPPING = 3;
    private static final String[] VERB_STRINGS = new String[]{"VERB_BINDING", "VERB_STARTING", "VERB_EXECUTING", "VERB_STOPPING", "VERB_FINISHED"};
    private static final int defaultMaxActiveJobsPerService;
    @GuardedBy("mLock")
    private boolean mAvailable;
    private final IBatteryStats mBatteryStats;
    private final Handler mCallbackHandler;
    private AtomicBoolean mCancelled;
    private final JobCompletedListener mCompletedListener;
    private final Context mContext;
    private long mExecutionStartTimeElapsed;
    private final JobPackageTracker mJobPackageTracker;
    private final Object mLock;
    private JobParameters mParams;
    private int mPreferredUid;
    private JobStatus mRunningJob;
    private long mTimeoutElapsed;
    int mVerb;
    private WakeLock mWakeLock;
    IJobService service;

    private class JobServiceHandler extends Handler {
        JobServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            boolean workOngoing = true;
            switch (message.what) {
                case 0:
                    handleOpTimeoutH();
                    break;
                case 1:
                    JobServiceContext.this.removeOpTimeOut();
                    if (JobServiceContext.this.mVerb != 1) {
                        if (JobServiceContext.this.mVerb == 2 || JobServiceContext.this.mVerb == 3) {
                            boolean reschedule;
                            if (message.arg2 == 1) {
                                reschedule = true;
                            } else {
                                reschedule = false;
                            }
                            handleFinishedH(reschedule);
                            break;
                        }
                    }
                    if (message.arg2 != 1) {
                        workOngoing = false;
                    }
                    handleStartedH(workOngoing);
                    break;
                case 2:
                    JobServiceContext.this.removeOpTimeOut();
                    handleServiceBoundH();
                    break;
                case 3:
                    if (JobServiceContext.this.mVerb != 4) {
                        JobServiceContext.this.mParams.setStopReason(message.arg1);
                        if (message.arg1 == 2) {
                            int uid;
                            JobServiceContext jobServiceContext = JobServiceContext.this;
                            if (JobServiceContext.this.mRunningJob != null) {
                                uid = JobServiceContext.this.mRunningJob.getUid();
                            } else {
                                uid = -1;
                            }
                            jobServiceContext.mPreferredUid = uid;
                        }
                        handleCancelH();
                        break;
                    }
                    return;
                case 4:
                    closeAndCleanupJobH(true);
                    break;
                default:
                    Slog.e(JobServiceContext.TAG, "Unrecognised message: " + message);
                    break;
            }
        }

        private void handleServiceBoundH() {
            if (JobServiceContext.this.mVerb != 0) {
                Slog.e(JobServiceContext.TAG, "Sending onStartJob for a job that isn't pending. " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb]);
                closeAndCleanupJobH(false);
            } else if (JobServiceContext.this.mCancelled.get()) {
                closeAndCleanupJobH(true);
            } else {
                try {
                    JobServiceContext.this.mVerb = 1;
                    JobServiceContext.this.scheduleOpTimeOut();
                    JobServiceContext.this.service.startJob(JobServiceContext.this.mParams);
                } catch (RemoteException e) {
                    Slog.e(JobServiceContext.TAG, "Error sending onStart message to '" + JobServiceContext.this.mRunningJob.getServiceComponent().getShortClassName() + "' ", e);
                }
            }
        }

        private void handleStartedH(boolean workOngoing) {
            switch (JobServiceContext.this.mVerb) {
                case 1:
                    JobServiceContext.this.mVerb = 2;
                    if (!workOngoing) {
                        handleFinishedH(false);
                        return;
                    } else if (JobServiceContext.this.mCancelled.get()) {
                        handleCancelH();
                        return;
                    } else {
                        JobServiceContext.this.scheduleOpTimeOut();
                        return;
                    }
                default:
                    Slog.e(JobServiceContext.TAG, "Handling started job but job wasn't starting! Was " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb] + ".");
                    return;
            }
        }

        private void handleFinishedH(boolean reschedule) {
            switch (JobServiceContext.this.mVerb) {
                case 2:
                case 3:
                    closeAndCleanupJobH(reschedule);
                    return;
                default:
                    Slog.e(JobServiceContext.TAG, "Got an execution complete message for a job that wasn't beingexecuted. Was " + JobServiceContext.VERB_STRINGS[JobServiceContext.this.mVerb] + ".");
                    return;
            }
        }

        private void handleCancelH() {
            switch (JobServiceContext.this.mVerb) {
                case 0:
                case 1:
                    JobServiceContext.this.mCancelled.set(true);
                    break;
                case 2:
                    if (!hasMessages(1)) {
                        sendStopMessageH();
                        break;
                    }
                    return;
                case 3:
                    break;
                default:
                    Slog.e(JobServiceContext.TAG, "Cancelling a job without a valid verb: " + JobServiceContext.this.mVerb);
                    break;
            }
        }

        private void handleOpTimeoutH() {
            switch (JobServiceContext.this.mVerb) {
                case 0:
                    Slog.e(JobServiceContext.TAG, "Time-out while trying to bind " + JobServiceContext.this.mRunningJob.toShortString() + ", dropping.");
                    closeAndCleanupJobH(false);
                    return;
                case 1:
                    Slog.e(JobServiceContext.TAG, "No response from client for onStartJob '" + JobServiceContext.this.mRunningJob.toShortString());
                    closeAndCleanupJobH(false);
                    return;
                case 2:
                    Slog.i(JobServiceContext.TAG, "Client timed out while executing (no jobFinished received). sending onStop. " + JobServiceContext.this.mRunningJob.toShortString());
                    JobServiceContext.this.mParams.setStopReason(3);
                    sendStopMessageH();
                    return;
                case 3:
                    Slog.e(JobServiceContext.TAG, "No response from client for onStopJob, '" + JobServiceContext.this.mRunningJob.toShortString());
                    closeAndCleanupJobH(true);
                    return;
                default:
                    Slog.e(JobServiceContext.TAG, "Handling timeout for an invalid job state: " + JobServiceContext.this.mRunningJob.toShortString() + ", dropping.");
                    closeAndCleanupJobH(false);
                    return;
            }
        }

        private void sendStopMessageH() {
            JobServiceContext.this.removeOpTimeOut();
            if (JobServiceContext.this.mVerb != 2) {
                Slog.e(JobServiceContext.TAG, "Sending onStopJob for a job that isn't started. " + JobServiceContext.this.mRunningJob);
                closeAndCleanupJobH(false);
                return;
            }
            try {
                JobServiceContext.this.mVerb = 3;
                JobServiceContext.this.scheduleOpTimeOut();
                JobServiceContext.this.service.stopJob(JobServiceContext.this.mParams);
            } catch (RemoteException e) {
                Slog.e(JobServiceContext.TAG, "Error sending onStopJob to client.", e);
                closeAndCleanupJobH(false);
            }
        }

        private void closeAndCleanupJobH(boolean reschedule) {
            synchronized (JobServiceContext.this.mLock) {
                if (JobServiceContext.this.mVerb == 4) {
                    return;
                }
                JobStatus completedJob = JobServiceContext.this.mRunningJob;
                JobServiceContext.this.mJobPackageTracker.noteInactive(completedJob);
                try {
                    if (JobServiceContext.this.mRunningJob != null) {
                        JobServiceContext.this.mBatteryStats.noteJobFinish(JobServiceContext.this.mRunningJob.getBatteryName(), JobServiceContext.this.mRunningJob.getSourceUid());
                    }
                } catch (RemoteException e) {
                }
                if (JobServiceContext.this.mWakeLock != null) {
                    JobServiceContext.this.mWakeLock.release();
                }
                try {
                    JobServiceContext.this.mContext.unbindService(JobServiceContext.this);
                } catch (Exception e2) {
                    Slog.e(JobServiceContext.TAG, "Service not bind: JobServiceContext");
                }
                JobServiceContext.this.mWakeLock = null;
                JobServiceContext.this.mRunningJob = null;
                JobServiceContext.this.mParams = null;
                JobServiceContext.this.mVerb = 4;
                JobServiceContext.this.mCancelled.set(false);
                JobServiceContext.this.service = null;
                JobServiceContext.this.mAvailable = true;
                JobServiceContext.this.removeOpTimeOut();
                removeMessages(1);
                removeMessages(2);
                removeMessages(3);
                removeMessages(4);
                JobServiceContext.this.mCompletedListener.onJobCompleted(completedJob, reschedule);
            }
        }
    }

    static {
        int i;
        if (ActivityManager.isLowRamDeviceStatic()) {
            i = 1;
        } else {
            i = 3;
        }
        defaultMaxActiveJobsPerService = i;
    }

    JobServiceContext(JobSchedulerService service, IBatteryStats batteryStats, JobPackageTracker tracker, Looper looper) {
        this(service.getContext(), service.getLock(), batteryStats, tracker, service, looper);
    }

    JobServiceContext(Context context, Object lock, IBatteryStats batteryStats, JobPackageTracker tracker, JobCompletedListener completedListener, Looper looper) {
        this.mCancelled = new AtomicBoolean();
        this.mContext = context;
        this.mLock = lock;
        this.mBatteryStats = batteryStats;
        this.mJobPackageTracker = tracker;
        this.mCallbackHandler = new JobServiceHandler(looper);
        this.mCompletedListener = completedListener;
        this.mAvailable = true;
        this.mVerb = 4;
        this.mPreferredUid = -1;
    }

    boolean executeRunnableJob(JobStatus job) {
        synchronized (this.mLock) {
            if (this.mAvailable) {
                this.mPreferredUid = -1;
                this.mRunningJob = job;
                boolean isDeadlineExpired = job.hasDeadlineConstraint() ? job.getLatestRunTimeElapsed() < SystemClock.elapsedRealtime() : false;
                Uri[] uriArr = null;
                if (job.changedUris != null) {
                    uriArr = new Uri[job.changedUris.size()];
                    job.changedUris.toArray(uriArr);
                }
                String[] strArr = null;
                if (job.changedAuthorities != null) {
                    strArr = new String[job.changedAuthorities.size()];
                    job.changedAuthorities.toArray(strArr);
                }
                this.mParams = new JobParameters(this, job.getJobId(), job.getExtras(), isDeadlineExpired, uriArr, strArr);
                this.mExecutionStartTimeElapsed = SystemClock.elapsedRealtime();
                this.mVerb = 0;
                scheduleOpTimeOut();
                Intent intent = new Intent().setComponent(job.getServiceComponent());
                if ((this.mCompletedListener instanceof JobSchedulerService) && ((JobSchedulerService) this.mCompletedListener).checkShouldFilterIntent(intent, job.getUserId())) {
                    Slog.i(TAG, job.getServiceComponent().getShortClassName() + " binding failed");
                    return false;
                } else if (this.mContext.bindServiceAsUser(intent, this, 5, new UserHandle(job.getUserId()))) {
                    try {
                        this.mBatteryStats.noteJobStart(job.getBatteryName(), job.getSourceUid());
                    } catch (RemoteException e) {
                    }
                    this.mJobPackageTracker.noteActive(job);
                    this.mAvailable = false;
                    return true;
                } else {
                    this.mRunningJob = null;
                    this.mParams = null;
                    this.mExecutionStartTimeElapsed = 0;
                    this.mVerb = 4;
                    removeOpTimeOut();
                    return false;
                }
            }
            Slog.e(TAG, "Starting new runnable but context is unavailable > Error.");
            return false;
        }
    }

    JobStatus getRunningJob() {
        synchronized (this.mLock) {
            JobStatus job = this.mRunningJob;
        }
        if (job == null) {
            return null;
        }
        return new JobStatus(job);
    }

    void cancelExecutingJob(int reason) {
        this.mCallbackHandler.obtainMessage(3, reason, 0).sendToTarget();
    }

    void preemptExecutingJob() {
        Message m = this.mCallbackHandler.obtainMessage(3);
        m.arg1 = 2;
        m.sendToTarget();
    }

    int getPreferredUid() {
        return this.mPreferredUid;
    }

    void clearPreferredUid() {
        this.mPreferredUid = -1;
    }

    long getExecutionStartTimeElapsed() {
        return this.mExecutionStartTimeElapsed;
    }

    long getTimeoutElapsed() {
        return this.mTimeoutElapsed;
    }

    public void jobFinished(int jobId, boolean reschedule) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(1, jobId, reschedule ? 1 : 0).sendToTarget();
        }
    }

    public void acknowledgeStopMessage(int jobId, boolean reschedule) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(1, jobId, reschedule ? 1 : 0).sendToTarget();
        }
    }

    public void acknowledgeStartMessage(int jobId, boolean ongoing) {
        if (verifyCallingUid()) {
            this.mCallbackHandler.obtainMessage(1, jobId, ongoing ? 1 : 0).sendToTarget();
        }
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this.mLock) {
            JobStatus runningJob = this.mRunningJob;
        }
        if (runningJob == null || !name.equals(runningJob.getServiceComponent())) {
            this.mCallbackHandler.obtainMessage(4).sendToTarget();
            return;
        }
        this.service = IJobService.Stub.asInterface(service);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, runningJob.getTag());
        this.mWakeLock.setWorkSource(new WorkSource(runningJob.getSourceUid()));
        this.mWakeLock.setReferenceCounted(false);
        this.mWakeLock.acquire();
        this.mCallbackHandler.obtainMessage(2).sendToTarget();
    }

    public void onServiceDisconnected(ComponentName name) {
        this.mCallbackHandler.obtainMessage(4).sendToTarget();
    }

    private boolean verifyCallingUid() {
        synchronized (this.mLock) {
            if (this.mRunningJob == null || Binder.getCallingUid() != this.mRunningJob.getUid()) {
                return false;
            }
            return true;
        }
    }

    private void scheduleOpTimeOut() {
        removeOpTimeOut();
        long timeoutMillis = this.mVerb == 2 ? 600000 : OP_TIMEOUT_MILLIS;
        this.mCallbackHandler.sendMessageDelayed(this.mCallbackHandler.obtainMessage(0), timeoutMillis);
        this.mTimeoutElapsed = SystemClock.elapsedRealtime() + timeoutMillis;
    }

    private void removeOpTimeOut() {
        this.mCallbackHandler.removeMessages(0);
    }
}
