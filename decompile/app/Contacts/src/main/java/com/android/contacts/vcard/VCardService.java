package com.android.contacts.vcard;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class VCardService extends Service {
    static final boolean DEBUG = HwLog.HWDBG;
    private MyBinder mBinder;
    private String mCallingActivity;
    private int mCurrentJobId;
    private int mCurrentWorkingJobId = -1;
    private String mErrorReason;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Set<String> mExtensionsToConsider;
    BroadcastReceiver mExternalStorageReceiver;
    private int mFileIndexMaximum;
    private int mFileIndexMinimum;
    private String mFileNameExtension;
    private String mFileNamePrefix;
    private String mFileNameSuffix;
    private boolean mHideImportDialog = true;
    private Messenger mIncomingExportMessenger;
    private Messenger mIncomingImportMessenger;
    private final List<CustomMediaScannerConnectionClient> mRemainingScannerConnections = new ArrayList();
    private final Set<String> mReservedDestination = new HashSet();
    private final SparseArray<ProcessorBase> mRunningJobMap = new SparseArray();
    private boolean mShouldUpdateProcessDialog;
    private File mTargetDirectory;
    WorkerHandler mWorkerHandler = new WorkerHandler();

    private class CustomMediaScannerConnectionClient implements MediaScannerConnectionClient {
        final MediaScannerConnection mConnection;
        final String mPath;

        public CustomMediaScannerConnectionClient(String path) {
            this.mConnection = new MediaScannerConnection(VCardService.this, this);
            this.mPath = path;
        }

        public void start() {
            this.mConnection.connect();
        }

        public void onMediaScannerConnected() {
            if (VCardService.DEBUG) {
                HwLog.d("VCardService", "Connected to MediaScanner. Start scanning.");
            }
            this.mConnection.scanFile(this.mPath, null);
        }

        public void onScanCompleted(String path, Uri uri) {
            if (VCardService.DEBUG) {
                HwLog.d("VCardService", "scan completed: " + path);
            }
            this.mConnection.disconnect();
            VCardService.this.removeConnectionClient(this);
        }
    }

    private class MediaStateChangeReceiver extends BroadcastReceiver {
        private MediaStateChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            VCardService.this.mWorkerHandler.sendEmptyMessage(0);
        }
    }

    public class MyBinder extends Binder {
        public VCardService getService() {
            return VCardService.this;
        }
    }

    private class WorkerHandler extends Handler {
        private WorkerHandler() {
        }

        public void handleMessage(Message msg) {
            VCardService.this.initExporterParams();
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mBinder = new MyBinder();
        if (DEBUG) {
            HwLog.d("VCardService", "vCard Service is being created.");
        }
        registerForMediaStateChange();
        initExporterParams();
    }

    private void registerForMediaStateChange() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_REMOVED");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        filter.addDataScheme("file");
        this.mExternalStorageReceiver = new MediaStateChangeReceiver();
        registerReceiver(this.mExternalStorageReceiver, filter);
    }

    private void initExporterParams() {
        File file = CommonUtilMethods.getUseStroagePathsPriorSDCardDefault(getApplication());
        if (file.exists() && file.canWrite() && "mounted".equals(Environment.getExternalStorageState()) && file.getFreeSpace() >= 10485760) {
            this.mTargetDirectory = file;
        } else {
            this.mTargetDirectory = Environment.getExternalStorageDirectory();
        }
        this.mFileNamePrefix = getString(R.string.config_export_file_prefix);
        this.mFileNameSuffix = getString(R.string.config_export_file_suffix);
        this.mFileNameExtension = getString(R.string.config_export_file_extension);
        this.mExtensionsToConsider = new HashSet();
        this.mExtensionsToConsider.add(this.mFileNameExtension);
        String additionalExtensions = getString(R.string.config_export_extensions_to_consider);
        if (!TextUtils.isEmpty(additionalExtensions)) {
            for (String extension : additionalExtensions.split(",")) {
                String trimed = extension.trim();
                if (trimed.length() > 0) {
                    this.mExtensionsToConsider.add(trimed);
                }
            }
        }
        Resources resources = getResources();
        this.mFileIndexMinimum = resources.getInteger(R.integer.config_export_file_min_index);
        this.mFileIndexMaximum = resources.getInteger(R.integer.config_export_file_max_index);
    }

    public int onStartCommand(Intent intent, int flags, int id) {
        if (intent == null || intent.getExtras() == null) {
            this.mCallingActivity = null;
        } else {
            this.mCallingActivity = intent.getExtras().getString("CALLING_ACTIVITY");
        }
        return 1;
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onDestroy() {
        if (DEBUG) {
            HwLog.d("VCardService", "VCardService is being destroyed.");
        }
        unregisterReceiver(this.mExternalStorageReceiver);
        cancelAllRequestsAndShutdown();
        clearCache();
        super.onDestroy();
    }

    public synchronized void handleImportRequest(List<ImportRequest> requests, VCardImportExportListener listener) {
        ImportRequest request;
        if (HwLog.HWDBG) {
            ArrayList<String> uris = new ArrayList();
            ArrayList<String> displayNames = new ArrayList();
            for (ImportRequest request2 : requests) {
                if (!(request2 == null || request2.uri == null)) {
                    uris.add(request2.uri.toString());
                    displayNames.add(request2.displayName);
                }
            }
            HwLog.d("VCardService", String.format("received multiple import request (uri: %s, displayName: %s)", new Object[]{uris.toString(), displayNames.toString()}));
        }
        int size = requests.size();
        if (this.mExecutorService.isTerminated()) {
            if (DEBUG) {
                HwLog.d("VCardService", "mExecutorService is terminated");
            }
            this.mExecutorService = Executors.newSingleThreadExecutor();
        }
        int i = 0;
        while (i < size) {
            request2 = (ImportRequest) requests.get(i);
            ImportProcessor importProcessor = new ImportProcessor(this, listener, request2, this.mCurrentJobId, getApplicationContext(), getApplication());
            importProcessor.setVCardEntryCommitterImportNeedSleep(this.mHideImportDialog);
            if (tryExecute(importProcessor)) {
                if (listener != null) {
                    listener.onImportProcessed(request2, this.mCurrentJobId, i);
                }
                this.mCurrentJobId++;
                i++;
            } else if (listener != null) {
                listener.onImportFailed(request2);
            }
        }
    }

    public synchronized void setHideImportDialog(boolean isHideImportDialog) {
        this.mHideImportDialog = isHideImportDialog;
        setVCardEntryCommitterNeedSleep(isHideImportDialog);
    }

    private void setVCardEntryCommitterNeedSleep(boolean isHideImportDialog) {
        if (this.mRunningJobMap != null) {
            int size = this.mRunningJobMap.size();
            for (int i = 0; i < size; i++) {
                if (this.mRunningJobMap.get(i) instanceof ImportProcessor) {
                    VCardEntryCommitterCustom vCardCommitter = ((ImportProcessor) this.mRunningJobMap.get(i)).getVCardEntryCommitter();
                    if (vCardCommitter != null) {
                        vCardCommitter.setImportVcardNeedSleep(isHideImportDialog);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void handleExportRequest(ExportRequest request, VCardImportExportListener listener) {
        if (this.mExecutorService.isTerminated()) {
            if (DEBUG) {
                HwLog.d("VCardService", "mExecutorService is terminated");
            }
            this.mExecutorService = Executors.newSingleThreadExecutor();
        }
        if (tryExecute(new ExportProcessor(this, request, this.mCurrentJobId, this.mCallingActivity))) {
            String path = request.destUri.getEncodedPath();
            if (DEBUG) {
                HwLog.d("VCardService", "Reserve the path " + path);
            }
            if (this.mReservedDestination.add(path)) {
                if (listener != null) {
                    listener.onExportProcessed(request, this.mCurrentJobId);
                }
                this.mCurrentJobId++;
            } else {
                HwLog.w("VCardService", String.format("The path %s is already reserved. Reject export request", new Object[]{path}));
                if (listener != null) {
                    listener.onExportFailed(request);
                }
            }
        } else if (listener != null) {
            listener.onExportFailed(request);
        }
    }

    private synchronized boolean tryExecute(ProcessorBase processor) {
        try {
            if (DEBUG) {
                HwLog.d("VCardService", "Executor service status: shutdown: " + this.mExecutorService.isShutdown() + ", terminated: " + this.mExecutorService.isTerminated());
            }
            this.mExecutorService.execute(processor);
            this.mRunningJobMap.put(this.mCurrentJobId, processor);
        } catch (RejectedExecutionException e) {
            HwLog.w("VCardService", "Failed to excetute a job.", e);
            return false;
        }
        return true;
    }

    public synchronized void handleCancelRequest(CancelRequest request) {
        int jobId = request.jobId;
        if (DEBUG) {
            HwLog.d("VCardService", String.format("Received cancel request. (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        ProcessorBase processor = (ProcessorBase) this.mRunningJobMap.get(jobId);
        this.mRunningJobMap.remove(jobId);
        if (processor != null) {
            processor.cancel(true);
            VCardImportExportListener listener = processor.getListener();
            processor.cancelAndNotified(true);
            int type = processor.getType();
            if (2 == type) {
                ((ExportProcessor) processor).doCancelNotification();
                HwLog.i("VCardService", String.format("Cancel reservation for the path %s if appropriate", new Object[]{((ExportProcessor) processor).getRequest().destUri.getEncodedPath()}));
                if (!this.mReservedDestination.remove(((ExportProcessor) processor).getRequest().destUri.getEncodedPath())) {
                    HwLog.w("VCardService", "Not reserved.");
                }
            } else if (listener != null) {
                listener.onCancelRequest(request, type);
            }
        } else {
            HwLog.w("VCardService", String.format("Tried to remove unknown job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        }
        stopServiceIfAppropriate();
    }

    public synchronized void handleRequestAvailableExportDestination() {
        Message message;
        if (DEBUG) {
            HwLog.d("VCardService", "Received available export destination request.");
        }
        String path = getAppropriateDestination(this.mTargetDirectory);
        if (path != null) {
            message = Message.obtain(null, 5, 0, 0, path);
        } else {
            message = Message.obtain(null, 5, R.id.dialog_fail_to_export_with_reason, 0, this.mErrorReason);
        }
        try {
            if (this.mIncomingExportMessenger != null) {
                this.mIncomingExportMessenger.send(message);
            }
        } catch (RemoteException e) {
            HwLog.w("VCardService", "Failed to send reply for available export destination request.", e);
        }
    }

    private synchronized void stopServiceIfAppropriate() {
        if (this.mRunningJobMap.size() > 0) {
            int size = this.mRunningJobMap.size();
            int[] toBeRemoved = new int[size];
            int i = 0;
            while (i < size) {
                int jobId = this.mRunningJobMap.keyAt(i);
                if (((ProcessorBase) this.mRunningJobMap.valueAt(i)).isDone()) {
                    toBeRemoved[i] = jobId;
                    i++;
                } else {
                    if (DEBUG) {
                        HwLog.d("VCardService", String.format("Found unfinished job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
                    }
                    for (int j = 0; j < i; j++) {
                        this.mRunningJobMap.remove(toBeRemoved[j]);
                    }
                    return;
                }
            }
            this.mRunningJobMap.clear();
        }
        if (this.mRemainingScannerConnections.isEmpty()) {
            HwLog.i("VCardService", "No unfinished job. Stop this service.");
            this.mExecutorService.shutdown();
            stopForeground(true);
            stopSelf();
            return;
        }
        HwLog.i("VCardService", "MediaScanner update is in progress.");
    }

    synchronized void updateMediaScanner(String path) {
        if (DEBUG) {
            HwLog.d("VCardService", "MediaScanner is being updated: " + path);
        }
        if (this.mExecutorService.isShutdown()) {
            HwLog.w("VCardService", "MediaScanner update is requested after executor's being shut down. Ignoring the update request");
            return;
        }
        CustomMediaScannerConnectionClient client = new CustomMediaScannerConnectionClient(path);
        this.mRemainingScannerConnections.add(client);
        client.start();
    }

    private synchronized void removeConnectionClient(CustomMediaScannerConnectionClient client) {
        if (DEBUG) {
            HwLog.d("VCardService", "Removing custom MediaScannerConnectionClient.");
        }
        this.mRemainingScannerConnections.remove(client);
        stopServiceIfAppropriate();
    }

    synchronized void handleFinishImportNotification(int jobId, boolean successful) {
        this.mRunningJobMap.remove(jobId);
        stopServiceIfAppropriate();
    }

    synchronized void handleFinishExportNotification(int jobId, boolean successful) {
        ProcessorBase job = (ProcessorBase) this.mRunningJobMap.get(jobId);
        this.mRunningJobMap.remove(jobId);
        if (job == null) {
            HwLog.w("VCardService", String.format("Tried to remove unknown job (id: %d)", new Object[]{Integer.valueOf(jobId)}));
        } else if (job instanceof ExportProcessor) {
            String path = ((ExportProcessor) job).getRequest().destUri.getEncodedPath();
            if (DEBUG) {
                HwLog.d("VCardService", "Remove reserved path " + path);
            }
            this.mReservedDestination.remove(path);
        } else {
            HwLog.w("VCardService", String.format("Removed job (id: %s) isn't ExportProcessor", new Object[]{Integer.valueOf(jobId)}));
        }
        stopServiceIfAppropriate();
    }

    private synchronized void cancelAllRequestsAndShutdown() {
        for (int i = 0; i < this.mRunningJobMap.size(); i++) {
            ((ProcessorBase) this.mRunningJobMap.valueAt(i)).cancel(true);
        }
        this.mRunningJobMap.clear();
        this.mExecutorService.shutdown();
    }

    private void clearCache() {
        for (String fileName : fileList()) {
            if (fileName.startsWith("import_tmp_")) {
                if (DEBUG) {
                    HwLog.d("VCardService", "Remove a temporary file: " + fileName);
                }
                deleteFile(fileName);
            }
        }
    }

    private String getAppropriateDestination(File destDirectory) {
        int i;
        int fileIndexDigit = 0;
        for (int tmp = this.mFileIndexMaximum; tmp > 0; tmp /= 10) {
            fileIndexDigit++;
        }
        StringBuilder lZero = new StringBuilder();
        for (i = 1; i < fileIndexDigit; i++) {
            lZero.append("0");
        }
        String bodyFormat = "%s" + lZero + "%s%s";
        if (String.format(bodyFormat, new Object[]{this.mFileNamePrefix, Integer.valueOf(1), this.mFileNameSuffix}).length() > 8 || this.mFileNameExtension.length() > 3) {
            HwLog.e("VCardService", "This code does not allow any long file name.");
            Object[] objArr = new Object[1];
            objArr[0] = String.format("%s.%s", new Object[]{possibleBody, this.mFileNameExtension});
            this.mErrorReason = getString(R.string.fail_reason_too_long_filename, objArr);
            HwLog.w("VCardService", "File name becomes too long.");
            return null;
        }
        for (i = this.mFileIndexMinimum; i <= this.mFileIndexMaximum; i++) {
            boolean numberIsAvailable = true;
            String body = String.format(bodyFormat, new Object[]{this.mFileNamePrefix, Integer.valueOf(i), this.mFileNameSuffix});
            for (String possibleExtension : this.mExtensionsToConsider) {
                File file = destDirectory;
                File file2 = new File(file, body + "." + possibleExtension);
                String path = file2.getAbsolutePath();
                synchronized (this) {
                    if (!this.mReservedDestination.contains(path)) {
                        if (file2.exists()) {
                            numberIsAvailable = false;
                            break;
                        }
                    }
                    if (DEBUG) {
                        HwLog.d("VCardService", String.format("%s is already being exported.", new Object[]{path}));
                    }
                    numberIsAvailable = false;
                }
            }
            if (numberIsAvailable) {
                return new File(destDirectory, body + "." + this.mFileNameExtension).getAbsolutePath();
            }
        }
        HwLog.w("VCardService", "Reached vCard number limit. Maybe there are too many vCard in the storage");
        this.mErrorReason = getString(R.string.fail_reason_too_many_vcard);
        return null;
    }

    public void setCurrentWorkingJobId(int jobId) {
        if (DEBUG) {
            HwLog.d("VCardService", "setCurrentWorkingJobId,mCurrentWorkingJobId=" + jobId);
        }
        this.mCurrentWorkingJobId = jobId;
    }

    public synchronized void setIncomingExportMessenger(Messenger messenger) {
        this.mIncomingExportMessenger = messenger;
    }

    public synchronized void setIncomingImportMessenger(Messenger messenger) {
        this.mIncomingImportMessenger = messenger;
    }

    public synchronized void setUpdateExportProgressDialog(boolean shouldUpdate) {
        this.mShouldUpdateProcessDialog = shouldUpdate;
    }

    public synchronized void showExportProcess(boolean show, int current, int total) {
        if (this.mShouldUpdateProcessDialog) {
            Message message = Message.obtain(null, 6, current, total, Boolean.valueOf(show));
            try {
                if (this.mIncomingExportMessenger != null) {
                    this.mIncomingExportMessenger.send(message);
                }
            } catch (RemoteException e) {
                HwLog.w("VCardService", "Failed to send export process message", e);
            }
        }
    }

    public synchronized void sendMessage(int what, int count, Object obj) {
        if (this.mShouldUpdateProcessDialog) {
            Message msg = Message.obtain();
            msg.what = what;
            if (count != 0) {
                msg.arg1 = count;
            }
            if (obj != null) {
                msg.obj = obj;
            }
            try {
                if (this.mIncomingImportMessenger != null) {
                    this.mIncomingImportMessenger.send(msg);
                }
            } catch (Exception e) {
                HwLog.i("VCardService", "send message fail.");
                e.printStackTrace();
            }
        }
    }

    public void cancelCurrentWorking(int type) {
        ProcessorBase processor = (ProcessorBase) this.mRunningJobMap.get(this.mCurrentWorkingJobId);
        String displayName = null;
        if (processor == null) {
            HwLog.w("VCardService", String.format("Tried to cancel unknown job (id: %d)", new Object[]{Integer.valueOf(this.mCurrentWorkingJobId)}));
        } else if (type == 1) {
            if (processor.getType() != 1) {
                HwLog.w("VCardService", String.format("Tried to cancel export job (id: %d) as import request.", new Object[]{Integer.valueOf(this.mCurrentWorkingJobId)}));
                return;
            }
            displayName = ((ImportProcessor) processor).getRequest().displayName;
        } else if (type == 2) {
            if (processor.getType() != 2) {
                HwLog.w("VCardService", String.format("Tried to cancel import job (id: %d) as import request.", new Object[]{Integer.valueOf(this.mCurrentWorkingJobId)}));
                return;
            }
            displayName = ((ExportProcessor) processor).getRequest().destUri.getLastPathSegment();
        }
        handleCancelRequest(new CancelRequest(this.mCurrentWorkingJobId, displayName));
    }

    public boolean isServiceRunning() {
        if (this.mRunningJobMap.size() > 0) {
            return true;
        }
        return false;
    }
}
