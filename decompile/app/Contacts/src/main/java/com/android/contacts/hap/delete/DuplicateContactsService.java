package com.android.contacts.hap.delete;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.CancelRequest;
import com.android.contacts.vcard.ProcessorBase;
import com.google.android.gms.R;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class DuplicateContactsService extends Service {
    private int mCurrentJobId;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Map<Integer, ProcessorBase> mRunningJobMap = new HashMap();

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return 1;
        }
        String action = intent.getAction();
        if (HwLog.HWDBG) {
            HwLog.v("DuplicateContactsService", "Requested action : " + action);
        }
        if ("DeleteDuplicateAction".equals(action)) {
            handleDeleteDuplicateContactsRequest(new AccountWithDataSet(intent.getStringExtra("AccountName"), intent.getStringExtra("AccountType"), intent.getStringExtra("DataSet")));
        } else {
            handleCancelRequest(new CancelRequest(intent.getIntExtra("JobId", 0), intent.getStringExtra("DisplayName")));
        }
        return 1;
    }

    private void handleDeleteDuplicateContactsRequest(AccountWithDataSet aAccount) {
        String lAccountName;
        if (HwLog.HWDBG) {
            HwLog.d("DuplicateContactsService", "In Delete Duplicate contacts for account Type : " + aAccount.type);
        }
        DuplicateContactsNotificationListener listener = new DuplicateContactsNotificationListener(this);
        ProcessorBase lDuplicateContactsProcessor = new DuplicateContactsProcessor(this, listener, this.mCurrentJobId, aAccount);
        if (!"com.android.huawei.phone".equals(aAccount.type)) {
            lAccountName = aAccount.name;
        } else if (getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
            lAccountName = getString(R.string.phoneLabelsGroup_in);
        } else {
            lAccountName = getString(R.string.phoneLabelsGroup);
        }
        if (tryExecute(lDuplicateContactsProcessor)) {
            listener.onDeleteDuplicateContactsQueued(lAccountName, this.mCurrentJobId);
            this.mCurrentJobId++;
            return;
        }
        listener.onDeleteDuplicateContactsFailed(lAccountName, this.mCurrentJobId);
    }

    private void handleCancelRequest(CancelRequest aRequest) {
        int jobId = aRequest.jobId;
        if (HwLog.HWDBG) {
            HwLog.d("DuplicateContactsService", "Received cancel request for JOBID = " + jobId);
        }
        synchronized (this) {
            ProcessorBase processor = (ProcessorBase) this.mRunningJobMap.remove(Integer.valueOf(jobId));
        }
        DuplicateContactsNotificationListener listener = new DuplicateContactsNotificationListener(this);
        if (processor != null) {
            processor.cancel(true);
            if (listener != null) {
                listener.onCancelRequest(aRequest);
            }
        } else {
            HwLog.w("DuplicateContactsService", String.format("Tried to remove unknown job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        stopServiceIfAppropriate();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void stopServiceIfAppropriate() {
        if (this.mRunningJobMap.size() > 0) {
            for (Entry<Integer, ProcessorBase> entry : this.mRunningJobMap.entrySet()) {
                try {
                    int jobId = ((Integer) entry.getKey()).intValue();
                    if (((ProcessorBase) entry.getValue()).isDone()) {
                        this.mRunningJobMap.remove(Integer.valueOf(jobId));
                    } else if (HwLog.HWFLOW) {
                        HwLog.i("DuplicateContactsService", String.format("Found unfinished job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
                    }
                } catch (ConcurrentModificationException e) {
                    HwLog.e("DuplicateContactsService", e.getMessage(), e);
                    return;
                }
            }
        }
        if (HwLog.HWFLOW) {
            HwLog.i("DuplicateContactsService", "No unfinished job. Stop this service.");
        }
        this.mExecutorService.shutdown();
        stopSelf();
    }

    public static Intent createDeleteDuplicateContactsIntent(Context context, AccountWithDataSet aAccount) {
        if (HwLog.HWDBG) {
            HwLog.v("DuplicateContactsService", "Create Delete duplicate Contacts Intent");
        }
        Intent serviceIntent = new Intent(context, DuplicateContactsService.class);
        serviceIntent.setAction("DeleteDuplicateAction");
        if (CommonUtilMethods.isSimAccount(aAccount.type)) {
            serviceIntent.putExtra("AccountName", SimFactoryManager.getSimCardDisplayLabel(aAccount.type));
        } else {
            serviceIntent.putExtra("AccountName", aAccount.name);
        }
        serviceIntent.putExtra("AccountType", aAccount.type);
        serviceIntent.putExtra("DataSet", aAccount.dataSet);
        return serviceIntent;
    }

    private synchronized boolean tryExecute(ProcessorBase processor) {
        try {
            if (HwLog.HWDBG) {
                HwLog.d("DuplicateContactsService", "Executor service status: shutdown: " + this.mExecutorService.isShutdown() + ", terminated: " + this.mExecutorService.isTerminated());
            }
            this.mExecutorService.execute(processor);
            this.mRunningJobMap.put(Integer.valueOf(this.mCurrentJobId), processor);
        } catch (RejectedExecutionException e) {
            HwLog.w("DuplicateContactsService", "Failed to excetute a job.", e);
            return false;
        }
        return true;
    }
}
