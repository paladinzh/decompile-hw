package com.android.contacts.hap.copy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.CancelRequest;
import com.android.contacts.vcard.ProcessorBase;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class CopyContactService extends Service {
    private int mCurrentJobId = Long.valueOf(System.currentTimeMillis()).intValue();
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    public boolean mImportToSimFlag = false;
    private final Map<Integer, ProcessorBase> mRunningJobMap = new HashMap();

    public void onDestroy() {
        if (HwLog.HWDBG) {
            HwLog.d("CopyContactService", "CopyContactService is being destroyed.");
        }
        cancelAllRequestsAndShutdown();
        super.onDestroy();
    }

    public static Intent createCopyContactsIntent(Context context, long[] aRawContactIds, String aAccountName, String aAccountType, String aDataSet) {
        return setServiceIntent(context, aRawContactIds, aAccountName, aAccountType, aDataSet);
    }

    public static Intent createCopyContactsIntent(Context context, long[] aRawContactIds, String aAccountName, String aAccountType, String aDataSet, String aCurrentAccountType) {
        Intent serviceIntent = setServiceIntent(context, aRawContactIds, aAccountName, aAccountType, aDataSet);
        serviceIntent.putExtra("CurrentAccountType", aCurrentAccountType);
        return serviceIntent;
    }

    public static Intent setServiceIntent(Context context, long[] aRawContactIds, String aAccountName, String aAccountType, String aDataSet) {
        if (HwLog.HWDBG) {
            HwLog.v("CopyContactService", "Create Copy Contacts Intent");
        }
        Intent serviceIntent = new Intent(context, CopyContactService.class);
        serviceIntent.setAction("CopyContactsAction");
        serviceIntent.putExtra("ContactIds", aRawContactIds);
        serviceIntent.putExtra("AccountName", aAccountName);
        serviceIntent.putExtra("AccountType", aAccountType);
        serviceIntent.putExtra("DataSet", aDataSet);
        return serviceIntent;
    }

    public static Intent createCancelRequestIntent(Context context, int lJobId, String lDisplayName) {
        if (HwLog.HWDBG) {
            HwLog.v("CopyContactService", "Create Cancel Request Intent");
        }
        Intent serviceIntent = new Intent(context, CopyContactService.class);
        serviceIntent.setAction("CancelRequest");
        serviceIntent.putExtra("JobId", lJobId);
        serviceIntent.putExtra("DisplayName", lDisplayName);
        return serviceIntent;
    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        if (intent == null) {
            return 1;
        }
        String action = intent.getAction();
        if (HwLog.HWDBG) {
            HwLog.v("CopyContactService", "Requested action : " + action);
        }
        String lAccountName;
        String lAccountType;
        String lDataSet;
        String lCurrentAccountType;
        Messenger msger;
        if ("CopyContactsAction".equals(action)) {
            ContactsCopyListener lListener;
            int category;
            long[] lRawContactIds = intent.getLongArrayExtra("ContactIds");
            lAccountName = intent.getStringExtra("AccountName");
            lAccountType = intent.getStringExtra("AccountType");
            lDataSet = intent.getStringExtra("DataSet");
            lCurrentAccountType = intent.getStringExtra("CurrentAccountType");
            msger = (Messenger) intent.getParcelableExtra("messenger");
            if (msger == null) {
                HwLog.i("CopyContactService", "COPY_CONTACTS_ACTION : msger == null");
            }
            boolean mExportToSimFlag = intent.getBooleanExtra("export_to_sim", false);
            this.mImportToSimFlag = intent.getBooleanExtra("import_to_sim", false);
            if (mExportToSimFlag) {
                lListener = new NotificationCopyContactsListener(this, mExportToSimFlag);
                category = 1;
            } else if (this.mImportToSimFlag) {
                lListener = new NotificationCopyContactsListener(this, false, this.mImportToSimFlag);
                category = 2;
            } else {
                lListener = new NotificationCopyContactsListener(this);
                category = 3;
            }
            Message msg = Message.obtain();
            msg.what = 4;
            msg.arg1 = category;
            sendMessgeToMessenger(msger, msg);
            if (!this.mRunningJobMap.isEmpty()) {
                sendMessgeToMessenger(msger, Message.obtain(null, 7));
            }
            handleCopyContactRequest(lRawContactIds, lAccountName, lAccountType, lDataSet, lListener, mExportToSimFlag, msger, lCurrentAccountType);
        } else if ("CopyContactsActionFromSIM".equals(action)) {
            Cursor lRawContactIdCursor = getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"}, "account_type=?", new String[]{"com.android.huawei.sim"}, null);
            if (lRawContactIdCursor != null && lRawContactIdCursor.moveToFirst()) {
                long[] lRawContactsIds = new long[lRawContactIdCursor.getCount()];
                int i = 0;
                while (true) {
                    int i2 = i + 1;
                    lRawContactsIds[i] = lRawContactIdCursor.getLong(lRawContactIdCursor.getColumnIndex("_id"));
                    if (!lRawContactIdCursor.moveToNext()) {
                        break;
                    }
                    i = i2;
                }
                lAccountName = intent.getStringExtra("AccountName");
                lAccountType = intent.getStringExtra("AccountType");
                lDataSet = intent.getStringExtra("DataSet");
                lCurrentAccountType = intent.getStringExtra("CurrentAccountType");
                msger = (Messenger) intent.getParcelableExtra("messenger");
                if (msger == null) {
                    HwLog.i("LOG_TAG", "COPY_CONTACTS_FROM_SIM_ACTION : msger == null");
                }
                handleCopyContactRequest(lRawContactsIds, lAccountName, lAccountType, lDataSet, new NotificationCopyContactsListener(this), false, msger, lCurrentAccountType);
            }
            if (lRawContactIdCursor != null) {
                lRawContactIdCursor.close();
            }
        } else {
            handleCancelRequest(new CancelRequest(intent.getIntExtra("JobId", 0), intent.getStringExtra("DisplayName")), new NotificationCopyContactsListener(this));
        }
        return 1;
    }

    public void startForegroundCopyNotification(Notification notification) {
        startForeground(2, notification);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void handleCopyContactRequest(long[] aRawContactIds, String aAccountName, String aAccountType, String aDataSet, ContactsCopyListener listener, boolean aExportToSimFlag, Messenger msger, String aCurrentAccountType) {
        if (HwLog.HWDBG) {
            HwLog.d("CopyContactService", "In handleCopyContactRequest for account Type : " + aAccountType);
        }
        if (aRawContactIds == null) {
            HwLog.e("CopyContactService", "aRawContactIds == null");
            return;
        }
        ProcessorBase lImportProcessor;
        if (CommonUtilMethods.isSimAccount(aAccountType)) {
            ContactsCopyListener contactsCopyListener = listener;
            long[] jArr = aRawContactIds;
            lImportProcessor = new CopyContactsProcessor(getApplicationContext(), this, contactsCopyListener, jArr, this.mCurrentJobId, aAccountName, aAccountType, aDataSet, SimFactoryManager.getSimCardDisplayLabel(aAccountType), msger, aCurrentAccountType);
            if (aExportToSimFlag) {
                ((CopyContactsProcessor) lImportProcessor).setExportToSimFlag(aExportToSimFlag);
            }
            if (this.mImportToSimFlag) {
                ((CopyContactsProcessor) lImportProcessor).setImportToSimFlag(this.mImportToSimFlag);
            }
        } else {
            ProcessorBase copyContactsProcessor = new CopyContactsProcessor(getApplicationContext(), this, listener, aRawContactIds, this.mCurrentJobId, aAccountName, aAccountType, aDataSet, aAccountName, msger, aCurrentAccountType);
            if (aExportToSimFlag) {
                ((CopyContactsProcessor) copyContactsProcessor).setExportToSimFlag(aExportToSimFlag);
            }
            if (this.mImportToSimFlag) {
                ((CopyContactsProcessor) copyContactsProcessor).setImportToSimFlag(this.mImportToSimFlag);
            }
        }
        if (tryExecute(lImportProcessor)) {
            if (listener != null) {
                if (CommonUtilMethods.isSimAccount(aAccountType)) {
                    listener.onCopyContactsQueued(SimFactoryManager.getSimCardDisplayLabel(aAccountType), this.mCurrentJobId, aRawContactIds.length);
                } else {
                    listener.onCopyContactsQueued(aAccountName, this.mCurrentJobId, aRawContactIds.length);
                }
            }
            this.mCurrentJobId = Long.valueOf(System.currentTimeMillis()).intValue();
        } else if (listener != null) {
            listener.onCopyContactsFailed(aAccountName);
        }
    }

    private synchronized boolean tryExecute(ProcessorBase processor) {
        try {
            if (HwLog.HWDBG) {
                HwLog.d("CopyContactService", "Executor service status: shutdown: " + this.mExecutorService.isShutdown() + ", terminated: " + this.mExecutorService.isTerminated());
            }
            this.mExecutorService.execute(processor);
            this.mRunningJobMap.put(Integer.valueOf(this.mCurrentJobId), processor);
        } catch (RejectedExecutionException e) {
            HwLog.w("CopyContactService", "Failed to excetute a job.", e);
            return false;
        }
        return true;
    }

    private synchronized void handleCancelRequest(CancelRequest request, ContactsCopyListener listener) {
        int jobId = request.jobId;
        if (HwLog.HWDBG) {
            HwLog.d("CopyContactService", String.format("Received cancel request. (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        ProcessorBase processor = (ProcessorBase) this.mRunningJobMap.remove(Integer.valueOf(jobId));
        if (processor != null) {
            processor.cancel(true);
            if (listener != null) {
                listener.onCancelRequest(request);
            }
        } else {
            cancelNotification(request);
            HwLog.w("CopyContactService", String.format("Tried to remove unknown job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        if (SimFactoryManager.isDualSim()) {
            SimFactoryManager.getSharedPreferences("SimInfoFile", 0).edit().putBoolean("sim_copy_contacts_progress", false).apply();
            SimFactoryManager.getSharedPreferences("SimInfoFile", 1).edit().putBoolean("sim_copy_contacts_progress", false).apply();
        } else {
            SimFactoryManager.getSharedPreferences("SimInfoFile", -1).edit().putBoolean("sim_copy_contacts_progress", false).apply();
        }
        stopServiceIfAppropriate();
    }

    private void cancelNotification(CancelRequest request) {
        ((NotificationManager) getApplication().getSystemService("notification")).cancelAll();
    }

    private synchronized void stopServiceIfAppropriate() {
        if (this.mRunningJobMap.size() > 0) {
            for (Entry<Integer, ProcessorBase> entry : this.mRunningJobMap.entrySet()) {
                try {
                    int jobId = ((Integer) entry.getKey()).intValue();
                    if (((ProcessorBase) entry.getValue()).isDone()) {
                        this.mRunningJobMap.remove(Integer.valueOf(jobId));
                    } else {
                        HwLog.i("CopyContactService", String.format("Found unfinished job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
                        return;
                    }
                } catch (ConcurrentModificationException e) {
                    HwLog.e("CopyContactService", e.getMessage(), e);
                    return;
                }
            }
        }
        HwLog.i("CopyContactService", "No unfinished job. Stop this service.");
        this.mExecutorService.shutdown();
        stopForeground(true);
        stopSelf();
    }

    synchronized void handleFinishCopyContactsNotification(int jobId, boolean successful) {
        if (HwLog.HWDBG) {
            String str = "CopyContactService";
            String str2 = "Received CopyContact finish notification id: " + jobId + " Result: ";
            Object[] objArr = new Object[1];
            objArr[0] = successful ? "success" : "failure";
            HwLog.d(str, String.format(str2, objArr));
        }
        if (this.mRunningJobMap.remove(Integer.valueOf(jobId)) == null) {
            HwLog.w("CopyContactService", String.format("Tried to remove unknown job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        stopServiceIfAppropriate();
    }

    private synchronized void cancelAllRequestsAndShutdown() {
        for (Entry<Integer, ProcessorBase> entry : this.mRunningJobMap.entrySet()) {
            ((ProcessorBase) entry.getValue()).cancel(true);
        }
        this.mRunningJobMap.clear();
        this.mExecutorService.shutdown();
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void sendMessgeToMessenger(Messenger messenger, Message msg) {
        if (messenger != null) {
            try {
                messenger.send(msg);
            } catch (Exception e) {
                HwLog.w("LOG_TAG", "COPY_CONTACTS_ACTION : send message fail !");
                e.printStackTrace();
            }
        }
    }
}
