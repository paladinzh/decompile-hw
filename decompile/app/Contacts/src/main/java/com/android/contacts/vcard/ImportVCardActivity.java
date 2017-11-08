package com.android.contacts.vcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.activities.RequestImportVCardPermissionsActivity;
import com.android.contacts.common.vcard.ImportVCardDialogFragment;
import com.android.contacts.common.vcard.ImportVCardDialogFragment.Listener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.AccountSelectionUtil.AccountSelectedListener;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.VCardService.MyBinder;
import com.android.vcard.VCardParser;
import com.android.vcard.exception.VCardException;
import com.google.android.gms.R;
import huawei.android.app.HwProgressDialog;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

public class ImportVCardActivity extends Activity implements Listener {
    private static final Uri BTIMPORT_URI = Uri.parse("content://com.huawei.bluetooth.BluetoothImportProvider/files/temp.vcf");
    private static final Uri WFDFTIMPORT_URI = Uri.parse("content://com.huawei.android.wfdft/files/android.vcf");
    private int copyCount;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int value;
            if (msg.what == 2 && !ImportVCardActivity.this.isFinishing()) {
                value = msg.arg1;
                if (value >= 50) {
                    ImportVCardActivity.this.showProgressDialogWithButton(value);
                    ImportVCardActivity.this.isImporting = true;
                } else {
                    ImportVCardActivity.this.isImporting = false;
                    if (ImportVCardActivity.this.mProgressDialog != null) {
                        ImportVCardActivity.this.mProgressDialog.dismiss();
                    }
                    ImportVCardActivity importVCardActivity = ImportVCardActivity.this;
                    importVCardActivity.mImportFinishCount = importVCardActivity.mImportFinishCount + 1;
                    if (ImportVCardActivity.this.mImportFinishCount >= ImportVCardActivity.this.mSelectedVcardCount) {
                        ImportVCardActivity.this.mImportFinishCount = 0;
                        ImportVCardActivity.this.finish();
                    }
                }
            }
            if (msg.what == 1 && ImportVCardActivity.this.copyCount >= 50 && !ImportVCardActivity.this.isFinishing()) {
                value = msg.arg1;
                if (value < 0) {
                    ImportVCardActivity.this.isImporting = false;
                    ImportVCardActivity.this.mProgressDialog.dismiss();
                    ImportVCardActivity.this.finish();
                }
                ImportVCardActivity.this.mProgressDialog.setProgress(value);
                ImportVCardActivity.this.mCurrentCount = value;
                if (value >= ImportVCardActivity.this.copyCount) {
                    importVCardActivity = ImportVCardActivity.this;
                    importVCardActivity.mImportFinishCount = importVCardActivity.mImportFinishCount + 1;
                    ImportVCardActivity.this.mProgressDialog.dismiss();
                    if (ImportVCardActivity.this.mImportFinishCount >= ImportVCardActivity.this.mSelectedVcardCount) {
                        ImportVCardActivity.this.mImportFinishCount = 0;
                        ImportVCardActivity.this.isImporting = false;
                        ImportVCardActivity.this.finish();
                    }
                }
            }
        }
    };
    private final IntentFilter homeFilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
    private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("homekey")) {
                    ImportVCardActivity.this.finish();
                }
            }
        }
    };
    private boolean isImporting = false;
    private AccountWithDataSet mAccount;
    private AccountSelectedListener mAccountSelectionListener;
    private List<VCardFile> mAllVCardFileList;
    private CancelListener mCancelListener = new CancelListener();
    private ImportRequestConnection mConnection;
    private int mCurrentCount;
    private int mCurrentDialogId;
    private boolean mDeleteFileAfterImport = false;
    private String mErrorMessage;
    private Handler mHandler = new Handler();
    private int mImportFinishCount;
    private boolean mImportFromDirectoryViaExternalIntent = false;
    VCardImportExportListener mListener;
    private HwProgressDialog mProgressDialog;
    private ProgressDialog mProgressDialogForCachingVCard;
    private ProgressDialog mProgressDialogForScanVCard;
    private int mSelectedVcardCount;
    private VCardService mService;
    private VCardCacheThread mVCardCacheThread;
    private VCardScanThread mVCardScanThread;
    private boolean mVcardIllegalFormat = false;

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            ImportVCardActivity.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            ImportVCardActivity.this.finish();
        }
    }

    private class DialogDisplayer implements Runnable {
        private final int mResId;

        public DialogDisplayer(int resId) {
            this.mResId = resId;
        }

        public DialogDisplayer(String errorMessage) {
            this.mResId = R.id.dialog_error_with_message;
            ImportVCardActivity.this.mErrorMessage = errorMessage;
        }

        public void run() {
            if (!ImportVCardActivity.this.isFinishing()) {
                ImportVCardActivity.this.showDialogById(this.mResId);
            }
        }
    }

    private class ImportRequestConnection implements ServiceConnection {
        private ImportRequestConnection() {
        }

        public void sendImportRequest(List<ImportRequest> requests) {
            HwLog.i("VCardImport", "Send an import request");
            Messenger msger = new Messenger(ImportVCardActivity.this.handler);
            if (ImportVCardActivity.this.mService.isServiceRunning()) {
                ImportVCardActivity.this.mService.setIncomingImportMessenger(null);
                ImportVCardActivity.this.mService.setUpdateExportProgressDialog(false);
                ImportVCardActivity.this.finish();
            } else {
                ImportVCardActivity.this.mService.setIncomingImportMessenger(msger);
                ImportVCardActivity.this.mService.setUpdateExportProgressDialog(true);
            }
            ImportVCardActivity.this.mService.handleImportRequest(requests, ImportVCardActivity.this.mListener);
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            ImportVCardActivity.this.mService = ((MyBinder) binder).getService();
            if (ImportVCardActivity.this.mVCardCacheThread != null) {
                ImportVCardActivity.this.mVCardCacheThread.start();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            HwLog.i("VCardImport", "Disconnected from VCardService");
            ImportVCardActivity.this.mService.setIncomingImportMessenger(null);
        }
    }

    private class VCardCacheThread extends Thread implements OnCancelListener {
        private boolean mCanceled;
        private final String mDisplayName;
        private final byte[] mSource = null;
        private final Uri[] mSourceUris;
        private VCardParser mVCardParser;
        private WakeLock mWakeLock;

        public VCardCacheThread(Uri[] sourceUris) {
            this.mSourceUris = sourceUris;
            Context context = ImportVCardActivity.this;
            this.mWakeLock = ((PowerManager) ImportVCardActivity.this.getSystemService("power")).newWakeLock(536870918, "VCardImport");
            this.mDisplayName = null;
        }

        protected void finalize() {
            if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
                HwLog.w("VCardImport", "WakeLock is being held.");
                this.mWakeLock.release();
            }
        }

        public void run() {
            Cursor cursor;
            HwLog.i("VCardImport", "vCard cache thread starts running.");
            if (ImportVCardActivity.this.mConnection == null) {
                throw new NullPointerException("vCard cache thread must be launched after a service connection is established");
            }
            ImportVCardActivity.this.setVcardCacheIllegal(false);
            this.mWakeLock.acquire();
            try {
                if (this.mCanceled) {
                    ImportVCardActivity.this.setVcardCacheIllegal(true);
                    HwLog.i("VCardImport", "vCard cache operation is canceled.");
                    HwLog.i("VCardImport", "Finished caching vCard.");
                    this.mWakeLock.release();
                    ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                    ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                    if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                        if (ImportVCardActivity.this.mService != null) {
                            ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                        }
                        ImportVCardActivity.this.finish();
                    }
                    return;
                }
                Context context = ImportVCardActivity.this;
                int cache_index = 0;
                ArrayList<ImportRequest> requests = new ArrayList();
                if (this.mSource != null) {
                    try {
                        requests.add(constructImportRequest(this.mSource, null, this.mDisplayName));
                    } catch (VCardException e) {
                        HwLog.e("VCardImport", "Maybe the file is in wrong format", e);
                        ImportVCardActivity.this.setVcardCacheIllegal(true);
                        ImportVCardActivity.this.showFailureNotification(R.string.fail_reason_not_supported);
                        HwLog.i("VCardImport", "Finished caching vCard.");
                        this.mWakeLock.release();
                        ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                        ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                        if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                            if (ImportVCardActivity.this.mService != null) {
                                ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                            }
                            ImportVCardActivity.this.finish();
                        }
                        return;
                    }
                }
                ContentResolver resolver = ImportVCardActivity.this.getContentResolver();
                Uri[] uriArr = this.mSourceUris;
                int length = uriArr.length;
                int i = 0;
                while (i < length) {
                    String filename;
                    Uri sourceUri = uriArr[i];
                    while (true) {
                        filename = "import_tmp_" + cache_index + ".vcf";
                        if (!context.getFileStreamPath(filename).exists()) {
                            break;
                        } else if (cache_index == Integer.MAX_VALUE) {
                            throw new RuntimeException("Exceeded cache limit");
                        } else {
                            cache_index++;
                        }
                    }
                    Uri localDataUri = copyTo(sourceUri, filename);
                    if (ImportVCardActivity.BTIMPORT_URI.equals(sourceUri)) {
                        context.sendBroadcast(new Intent("android.intent.action.NOTIFICATION_BTIMPORT_DONE"), "com.huawei.bluetooth.permission.ACCESS_BLUETOOTHIMPORT_SHARE");
                        HwLog.i("VCardImport", "notify that btimport's data has been featched.");
                    }
                    if (ImportVCardActivity.WFDFTIMPORT_URI.equals(sourceUri)) {
                        Intent intent = new Intent("android.intent.action.NOTIFICATION_WFDFTIMPORT_DONE");
                        intent.setPackage("com.huawei.android.wfdft");
                        context.sendBroadcast(intent, "com.huawei.android.wfdft.permission.ACCESS_INTERFACE");
                        HwLog.i("VCardImport", "notify that wfdftimport's data has been featched.");
                    }
                    if (this.mCanceled) {
                        ImportVCardActivity.this.setVcardCacheIllegal(true);
                        HwLog.i("VCardImport", "vCard cache operation is canceled.");
                        break;
                    } else if (localDataUri == null) {
                        HwLog.w("VCardImport", "destUri is null");
                        ImportVCardActivity.this.setVcardCacheIllegal(true);
                        break;
                    } else {
                        int index;
                        String displayName = null;
                        cursor = null;
                        cursor = resolver.query(sourceUri, new String[]{"_display_name"}, null, null, null);
                        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                            if (cursor.getCount() > 1) {
                                HwLog.w("VCardImport", "Unexpected multiple rows: " + cursor.getCount());
                            }
                            index = cursor.getColumnIndex("_display_name");
                            if (index >= 0) {
                                displayName = cursor.getString(index);
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (TextUtils.isEmpty(displayName)) {
                            try {
                                cursor = resolver.query(sourceUri, new String[]{"_data"}, null, null, null);
                                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                                    if (cursor.getCount() > 1) {
                                        HwLog.w("VCardImport", "Unexpected multiple rows: " + cursor.getCount());
                                    }
                                    index = cursor.getColumnIndexOrThrow("_data");
                                    if (index >= 0) {
                                        displayName = cursor.getString(index);
                                        if (displayName != null) {
                                            displayName = Uri.parse(displayName).getLastPathSegment();
                                        }
                                    }
                                }
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } catch (VCardException e2) {
                                HwLog.e("VCardImport", "Maybe the file is in wrong format", e2);
                                ImportVCardActivity.this.setVcardCacheIllegal(true);
                                ImportVCardActivity.this.showFailureNotification(R.string.fail_reason_not_supported, displayName);
                                HwLog.i("VCardImport", "Finished caching vCard.");
                                this.mWakeLock.release();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                                    if (ImportVCardActivity.this.mService != null) {
                                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                                    }
                                    ImportVCardActivity.this.finish();
                                }
                                return;
                            } catch (IllegalArgumentException e3) {
                                HwLog.e("VCardImport", "Maybe the file is in wrong format", e3);
                                ImportVCardActivity.this.setVcardCacheIllegal(true);
                                ImportVCardActivity.this.showFailureNotification(R.string.fail_reason_not_supported, displayName);
                                HwLog.i("VCardImport", "Finished caching vCard.");
                                this.mWakeLock.release();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                                    if (ImportVCardActivity.this.mService != null) {
                                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                                    }
                                    ImportVCardActivity.this.finish();
                                }
                                return;
                            } catch (IOException e4) {
                                HwLog.e("VCardImport", "Unexpected IOException", e4);
                                ImportVCardActivity.this.setVcardCacheIllegal(true);
                                ImportVCardActivity.this.showFailureNotification(R.string.contacts_notification_input_or_out_error);
                                HwLog.i("VCardImport", "Finished caching vCard.");
                                this.mWakeLock.release();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                                    if (ImportVCardActivity.this.mService != null) {
                                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                                    }
                                    ImportVCardActivity.this.finish();
                                }
                                return;
                            } catch (OutOfMemoryError e5) {
                                HwLog.e("VCardImport", "OutOfMemoryError occured during caching vCard");
                                ImportVCardActivity.this.setVcardCacheIllegal(true);
                                ImportVCardActivity.this.runOnUiThread(new DialogDisplayer(ImportVCardActivity.this.getString(R.string.fail_reason_low_memory_during_import)));
                                HwLog.i("VCardImport", "Finished caching vCard.");
                                this.mWakeLock.release();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                                    if (ImportVCardActivity.this.mService != null) {
                                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                                    }
                                    ImportVCardActivity.this.finish();
                                }
                            } catch (Throwable th) {
                                HwLog.i("VCardImport", "Finished caching vCard.");
                                this.mWakeLock.release();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                                    if (ImportVCardActivity.this.mService != null) {
                                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                                    }
                                    ImportVCardActivity.this.finish();
                                }
                            }
                        }
                        if (TextUtils.isEmpty(displayName)) {
                            displayName = sourceUri.getLastPathSegment();
                        }
                        ImportRequest request = constructImportRequest(null, localDataUri, displayName);
                        if (request != null && this.mSourceUris.length > 1) {
                            request.isMultipleRequest = true;
                        }
                        if (this.mCanceled) {
                            ImportVCardActivity.this.setVcardCacheIllegal(true);
                            HwLog.i("VCardImport", "vCard cache operation is canceled.");
                            HwLog.i("VCardImport", "Finished caching vCard.");
                            this.mWakeLock.release();
                            ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                            ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                            if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                                if (ImportVCardActivity.this.mService != null) {
                                    ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                                }
                                ImportVCardActivity.this.finish();
                            }
                            return;
                        }
                        if (request != null) {
                            requests.add(request);
                        }
                        i++;
                    }
                }
                if (requests.isEmpty()) {
                    ImportVCardActivity.this.setVcardCacheIllegal(true);
                    HwLog.w("VCardImport", "Empty import requests. Ignore it.");
                } else {
                    ImportVCardActivity.this.mConnection.sendImportRequest(requests);
                }
                if (ImportVCardActivity.this.mAllVCardFileList != null) {
                    clearCacheFile();
                }
                HwLog.i("VCardImport", "Finished caching vCard.");
                this.mWakeLock.release();
                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                    if (ImportVCardActivity.this.mService != null) {
                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                    }
                    ImportVCardActivity.this.finish();
                }
            } catch (OutOfMemoryError e52) {
                HwLog.e("VCardImport", "OutOfMemoryError occured during caching vCard");
                ImportVCardActivity.this.setVcardCacheIllegal(true);
                ImportVCardActivity.this.runOnUiThread(new DialogDisplayer(ImportVCardActivity.this.getString(R.string.fail_reason_low_memory_during_import)));
                HwLog.i("VCardImport", "Finished caching vCard.");
                this.mWakeLock.release();
                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                    if (ImportVCardActivity.this.mService != null) {
                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                    }
                    ImportVCardActivity.this.finish();
                }
            } catch (IOException e42) {
                HwLog.e("VCardImport", "IOException during caching vCard", e42);
                ImportVCardActivity.this.setVcardCacheIllegal(true);
                ImportVCardActivity.this.runOnUiThread(new DialogDisplayer(ImportVCardActivity.this.getString(R.string.io_error_message_text)));
                HwLog.i("VCardImport", "Finished caching vCard.");
                this.mWakeLock.release();
                ImportVCardActivity.this.mProgressDialogForCachingVCard.dismiss();
                ImportVCardActivity.this.mProgressDialogForCachingVCard = null;
                if (ImportVCardActivity.this.getVcardCacheIllegal()) {
                    if (ImportVCardActivity.this.mService != null) {
                        ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                    }
                    ImportVCardActivity.this.finish();
                }
            } catch (Throwable th2) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private void clearCacheFile() {
            for (final VCardFile vcardFile : ImportVCardActivity.this.mAllVCardFileList) {
                new Thread() {
                    public void run() {
                        if (ImportVCardActivity.this.mDeleteFileAfterImport && !new File(vcardFile.getCanonicalPath()).delete()) {
                            HwLog.w("VCardImport", "Failed to delete vacard file.");
                        }
                    }
                }.start();
            }
        }

        private Uri copyTo(Uri sourceUri, String filename) throws IOException {
            HwLog.i("VCardImport", String.format("Copy a Uri to app local storage (%s -> %s)", new Object[]{sourceUri, filename}));
            Context context = ImportVCardActivity.this;
            ReadableByteChannel readableByteChannel = null;
            WritableByteChannel writableByteChannel = null;
            Uri uri = null;
            try {
                readableByteChannel = Channels.newChannel(context.getContentResolver().openInputStream(sourceUri));
                uri = Uri.parse(context.getFileStreamPath(filename).toURI().toString());
                writableByteChannel = context.openFileOutput(filename, 0).getChannel();
                ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
                while (readableByteChannel.read(buffer) != -1) {
                    if (this.mCanceled) {
                        if (HwLog.HWDBG) {
                            HwLog.d("VCardImport", "Canceled during caching " + sourceUri);
                        }
                        if (readableByteChannel != null) {
                            try {
                                readableByteChannel.close();
                            } catch (IOException e) {
                                HwLog.w("VCardImport", "Failed to close inputChannel.");
                            }
                        }
                        if (writableByteChannel != null) {
                            try {
                                writableByteChannel.close();
                            } catch (IOException e2) {
                                HwLog.w("VCardImport", "Failed to close outputChannel");
                            }
                        }
                        return null;
                    }
                    buffer.flip();
                    writableByteChannel.write(buffer);
                    buffer.compact();
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    writableByteChannel.write(buffer);
                }
                if (readableByteChannel != null) {
                    try {
                        readableByteChannel.close();
                    } catch (IOException e3) {
                        HwLog.w("VCardImport", "Failed to close inputChannel.");
                    }
                }
                if (writableByteChannel != null) {
                    try {
                        writableByteChannel.close();
                    } catch (IOException e4) {
                        HwLog.w("VCardImport", "Failed to close outputChannel");
                    }
                }
            } catch (Exception e5) {
                HwLog.e("VCardImport", "Exception while importing = " + e5.getMessage());
                if (readableByteChannel != null) {
                    try {
                        readableByteChannel.close();
                    } catch (IOException e6) {
                        HwLog.w("VCardImport", "Failed to close inputChannel.");
                    }
                }
                if (writableByteChannel != null) {
                    try {
                        writableByteChannel.close();
                    } catch (IOException e7) {
                        HwLog.w("VCardImport", "Failed to close outputChannel");
                    }
                }
            } catch (Throwable th) {
                if (readableByteChannel != null) {
                    try {
                        readableByteChannel.close();
                    } catch (IOException e8) {
                        HwLog.w("VCardImport", "Failed to close inputChannel.");
                    }
                }
                if (writableByteChannel != null) {
                    try {
                        writableByteChannel.close();
                    } catch (IOException e9) {
                        HwLog.w("VCardImport", "Failed to close outputChannel");
                    }
                }
            }
            return uri;
        }

        private com.android.contacts.vcard.ImportRequest constructImportRequest(byte[] r23, android.net.Uri r24, java.lang.String r25) throws java.io.IOException, com.android.vcard.exception.VCardException {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.contacts.vcard.ImportVCardActivity.VCardCacheThread.constructImportRequest(byte[], android.net.Uri, java.lang.String):com.android.contacts.vcard.ImportRequest. bs: [B:12:0x0046, B:42:0x00a9]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r22 = this;
            r0 = r22;
            r2 = com.android.contacts.vcard.ImportVCardActivity.this;
            r20 = r2.getContentResolver();
            r10 = 0;
            r12 = 0;
            r8 = 1;
            r21 = 0;
            if (r23 == 0) goto L_0x005f;
        L_0x000f:
            r18 = new java.io.ByteArrayInputStream;	 Catch:{ VCardNestedException -> 0x00cb }
            r0 = r18;	 Catch:{ VCardNestedException -> 0x00cb }
            r1 = r23;	 Catch:{ VCardNestedException -> 0x00cb }
            r0.<init>(r1);	 Catch:{ VCardNestedException -> 0x00cb }
            r19 = r18;	 Catch:{ VCardNestedException -> 0x00cb }
        L_0x001a:
            r2 = new com.android.vcard.VCardParser_V21;	 Catch:{ VCardNestedException -> 0x00cb }
            r2.<init>();	 Catch:{ VCardNestedException -> 0x00cb }
            r0 = r22;	 Catch:{ VCardNestedException -> 0x00cb }
            r0.mVCardParser = r2;	 Catch:{ VCardNestedException -> 0x00cb }
            r11 = new com.android.vcard.VCardEntryCounter;	 Catch:{ VCardVersionException -> 0x006c }
            r11.<init>();	 Catch:{ VCardVersionException -> 0x006c }
            r13 = new com.android.vcard.VCardSourceDetector;	 Catch:{ VCardVersionException -> 0x011e, all -> 0x0105 }
            r13.<init>();	 Catch:{ VCardVersionException -> 0x011e, all -> 0x0105 }
            r0 = r22;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r2 = r0.mVCardParser;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r2.addInterpreter(r11);	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r0 = r22;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r2 = r0.mVCardParser;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r2.addInterpreter(r13);	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r0 = r22;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r2 = r0.mVCardParser;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r0 = r19;	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            r2.parse(r0);	 Catch:{ VCardVersionException -> 0x0122, all -> 0x010a }
            if (r19 == 0) goto L_0x0049;
        L_0x0046:
            r19.close();	 Catch:{ IOException -> 0x006a, VCardNestedException -> 0x00fd }
        L_0x0049:
            r18 = r19;
            r12 = r13;
            r10 = r11;
        L_0x004d:
            if (r21 == 0) goto L_0x00db;
        L_0x004f:
            r8 = 2;
        L_0x0050:
            if (r12 == 0) goto L_0x0054;
        L_0x0052:
            if (r10 != 0) goto L_0x00de;
        L_0x0054:
            r2 = "VCardImport";
            r3 = "VCard import Exception.";
            com.android.contacts.util.HwLog.w(r2, r3);
            r2 = 0;
            return r2;
        L_0x005f:
            r0 = r20;	 Catch:{ VCardNestedException -> 0x00cb }
            r1 = r24;	 Catch:{ VCardNestedException -> 0x00cb }
            r18 = r0.openInputStream(r1);	 Catch:{ VCardNestedException -> 0x00cb }
            r19 = r18;
            goto L_0x001a;
        L_0x006a:
            r15 = move-exception;
            goto L_0x0049;
        L_0x006c:
            r16 = move-exception;
        L_0x006d:
            r19.close();	 Catch:{ IOException -> 0x00af }
        L_0x0070:
            r21 = 1;
            if (r23 == 0) goto L_0x00b1;
        L_0x0074:
            r18 = new java.io.ByteArrayInputStream;	 Catch:{ all -> 0x0101 }
            r0 = r18;	 Catch:{ all -> 0x0101 }
            r1 = r23;	 Catch:{ all -> 0x0101 }
            r0.<init>(r1);	 Catch:{ all -> 0x0101 }
        L_0x007d:
            r2 = new com.android.vcard.VCardParser_V30;	 Catch:{ all -> 0x00c4 }
            r2.<init>();	 Catch:{ all -> 0x00c4 }
            r0 = r22;	 Catch:{ all -> 0x00c4 }
            r0.mVCardParser = r2;	 Catch:{ all -> 0x00c4 }
            r11 = new com.android.vcard.VCardEntryCounter;	 Catch:{ VCardVersionException -> 0x00ba }
            r11.<init>();	 Catch:{ VCardVersionException -> 0x00ba }
            r13 = new com.android.vcard.VCardSourceDetector;	 Catch:{ VCardVersionException -> 0x0117, all -> 0x0110 }
            r13.<init>();	 Catch:{ VCardVersionException -> 0x0117, all -> 0x0110 }
            r0 = r22;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r2 = r0.mVCardParser;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r2.addInterpreter(r11);	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r0 = r22;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r2 = r0.mVCardParser;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r2.addInterpreter(r13);	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r0 = r22;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r2 = r0.mVCardParser;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r0 = r18;	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            r2.parse(r0);	 Catch:{ VCardVersionException -> 0x011a, all -> 0x0113 }
            if (r18 == 0) goto L_0x00ac;
        L_0x00a9:
            r18.close();	 Catch:{ IOException -> 0x00d7, VCardNestedException -> 0x00fd }
        L_0x00ac:
            r12 = r13;
            r10 = r11;
            goto L_0x004d;
        L_0x00af:
            r15 = move-exception;
            goto L_0x0070;
        L_0x00b1:
            r0 = r20;	 Catch:{ all -> 0x0101 }
            r1 = r24;	 Catch:{ all -> 0x0101 }
            r18 = r0.openInputStream(r1);	 Catch:{ all -> 0x0101 }
            goto L_0x007d;
        L_0x00ba:
            r17 = move-exception;
        L_0x00bb:
            r2 = new com.android.vcard.exception.VCardException;	 Catch:{ all -> 0x00c4 }
            r3 = "vCard with unspported version.";	 Catch:{ all -> 0x00c4 }
            r2.<init>(r3);	 Catch:{ all -> 0x00c4 }
            throw r2;	 Catch:{ all -> 0x00c4 }
        L_0x00c4:
            r2 = move-exception;
        L_0x00c5:
            if (r18 == 0) goto L_0x00ca;
        L_0x00c7:
            r18.close();	 Catch:{ IOException -> 0x00d9 }
        L_0x00ca:
            throw r2;	 Catch:{ VCardNestedException -> 0x00cb }
        L_0x00cb:
            r14 = move-exception;
        L_0x00cc:
            r2 = "VCardImport";
            r3 = "Nested Exception is found (it may be false-positive).";
            com.android.contacts.util.HwLog.w(r2, r3);
            goto L_0x0050;
        L_0x00d7:
            r15 = move-exception;
            goto L_0x00ac;
        L_0x00d9:
            r15 = move-exception;
            goto L_0x00ca;
        L_0x00db:
            r8 = 1;
            goto L_0x0050;
        L_0x00de:
            r0 = r22;
            r2 = com.android.contacts.vcard.ImportVCardActivity.this;
            r2 = r2.mAccount;
            r6 = r12.getEstimatedType();
            r7 = r12.getEstimatedCharset();
            r9 = r10.getCount();
            r3 = r23;
            r4 = r24;
            r5 = r25;
            r2 = com.android.contacts.vcard.ImportRequest.newInstance(r2, r3, r4, r5, r6, r7, r8, r9);
            return r2;
        L_0x00fd:
            r14 = move-exception;
            r12 = r13;
            r10 = r11;
            goto L_0x00cc;
        L_0x0101:
            r2 = move-exception;
            r18 = r19;
            goto L_0x00c5;
        L_0x0105:
            r2 = move-exception;
            r18 = r19;
            r10 = r11;
            goto L_0x00c5;
        L_0x010a:
            r2 = move-exception;
            r18 = r19;
            r12 = r13;
            r10 = r11;
            goto L_0x00c5;
        L_0x0110:
            r2 = move-exception;
            r10 = r11;
            goto L_0x00c5;
        L_0x0113:
            r2 = move-exception;
            r12 = r13;
            r10 = r11;
            goto L_0x00c5;
        L_0x0117:
            r17 = move-exception;
            r10 = r11;
            goto L_0x00bb;
        L_0x011a:
            r17 = move-exception;
            r12 = r13;
            r10 = r11;
            goto L_0x00bb;
        L_0x011e:
            r16 = move-exception;
            r10 = r11;
            goto L_0x006d;
        L_0x0122:
            r16 = move-exception;
            r12 = r13;
            r10 = r11;
            goto L_0x006d;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.vcard.ImportVCardActivity.VCardCacheThread.constructImportRequest(byte[], android.net.Uri, java.lang.String):com.android.contacts.vcard.ImportRequest");
        }

        public void cancel() {
            this.mCanceled = true;
            if (this.mVCardParser != null) {
                this.mVCardParser.cancel();
            }
            ImportVCardActivity.this.finish();
        }

        public void onCancel(DialogInterface dialog) {
            HwLog.i("VCardImport", "Cancel request has come. Abort caching vCard.");
            cancel();
        }
    }

    private static class VCardFile implements Serializable {
        private static final long serialVersionUID = 1;
        private final String mCanonicalPath;
        private final long mLastModified;
        private final String mName;

        public VCardFile(String name, String canonicalPath, long lastModified) {
            this.mName = name;
            this.mCanonicalPath = canonicalPath;
            this.mLastModified = lastModified;
        }

        public String getName() {
            return this.mName;
        }

        public String getCanonicalPath() {
            return this.mCanonicalPath;
        }

        public long getLastModified() {
            return this.mLastModified;
        }
    }

    private class VCardScanThread extends Thread implements OnCancelListener, OnClickListener {
        private boolean mCanceled = false;
        private Set<String> mCheckedPaths;
        private boolean mGotIOException = false;
        private File[] mRootDirectory;
        private WakeLock mWakeLock;

        private class CanceledException extends Exception {
            private CanceledException() {
            }
        }

        public VCardScanThread(File[] sdcardDirectory) {
            this.mRootDirectory = sdcardDirectory;
            this.mCheckedPaths = new HashSet();
            this.mWakeLock = ((PowerManager) ImportVCardActivity.this.getSystemService("power")).newWakeLock(536870913, "VCardImport");
        }

        public void run() {
            ImportVCardActivity.this.mAllVCardFileList = new Vector();
            for (int i = 0; i < this.mRootDirectory.length; i++) {
                if (this.mRootDirectory[i] != null) {
                    try {
                        this.mWakeLock.acquire();
                        getVCardFileRecursively(this.mRootDirectory[i]);
                    } catch (CanceledException e) {
                        this.mCanceled = true;
                    } catch (IOException e2) {
                        this.mGotIOException = true;
                    } finally {
                        this.mWakeLock.release();
                    }
                }
            }
            if (this.mCanceled) {
                ImportVCardActivity.this.mAllVCardFileList = null;
            }
            ImportVCardActivity.this.mProgressDialogForScanVCard.dismiss();
            ImportVCardActivity.this.mProgressDialogForScanVCard = null;
            ImportVCardActivity.this.removeDialog(R.id.dialog_searching_vcard);
            if (this.mGotIOException) {
                ImportVCardActivity.this.runOnUiThread(new DialogDisplayer((int) R.id.dialog_io_exception));
            } else if (this.mCanceled) {
                ImportVCardActivity.this.finish();
            } else {
                int size = ImportVCardActivity.this.mAllVCardFileList.size();
                Context context = ImportVCardActivity.this;
                if (size == 0) {
                    ImportVCardActivity.this.runOnUiThread(new DialogDisplayer((int) R.id.dialog_vcard_not_found));
                } else {
                    ImportVCardActivity.this.startVCardSelectAndImport();
                }
            }
        }

        private void getVCardFileRecursively(File directory) throws CanceledException, IOException {
            if (this.mCanceled) {
                throw new CanceledException();
            }
            File[] files = directory.listFiles();
            if (files == null) {
                if (!TextUtils.equals(directory.getCanonicalPath(), directory.getCanonicalPath().concat(".android_secure"))) {
                    HwLog.w("VCardImport", "listFiles() returned null (directory: " + directory + ")");
                }
                return;
            }
            for (File file : files) {
                if (this.mCanceled) {
                    throw new CanceledException();
                }
                String canonicalPath = file.getCanonicalPath();
                if (!this.mCheckedPaths.contains(canonicalPath)) {
                    this.mCheckedPaths.add(canonicalPath);
                    if (file.isDirectory()) {
                        getVCardFileRecursively(file);
                    } else if (canonicalPath.toLowerCase().endsWith(".vcf") && file.canRead()) {
                        ImportVCardActivity.this.mAllVCardFileList.add(new VCardFile(file.getName(), canonicalPath, file.lastModified()));
                    }
                }
            }
        }

        public void onCancel(DialogInterface dialog) {
            this.mCanceled = true;
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -2) {
                this.mCanceled = true;
            }
        }
    }

    private class VCardSelectedListener implements OnClickListener, OnMultiChoiceClickListener {
        private int mCurrentIndex = 0;
        private boolean mSelectcheck;
        private Set<Integer> mSelectedIndexSet;

        public VCardSelectedListener(boolean multipleSelect) {
            this.mSelectcheck = multipleSelect;
            if (multipleSelect) {
                this.mSelectedIndexSet = new HashSet();
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            ListView alerdialogListView = ((AlertDialog) dialog).getListView();
            int i;
            if (which == -1) {
                if (this.mSelectedIndexSet != null) {
                    List<VCardFile> selectedVCardFileList = new ArrayList();
                    int size = ImportVCardActivity.this.mAllVCardFileList.size();
                    for (i = 0; i < size; i++) {
                        if (this.mSelectedIndexSet.contains(Integer.valueOf(i + 1))) {
                            selectedVCardFileList.add((VCardFile) ImportVCardActivity.this.mAllVCardFileList.get(i));
                        }
                    }
                    if (selectedVCardFileList.size() > 0) {
                        StatisticalHelper.report(4011);
                        ImportVCardActivity.this.importVCardFromSDCard((List) selectedVCardFileList);
                        return;
                    }
                    ImportVCardActivity.this.finish();
                } else if (this.mCurrentIndex != -1) {
                    StatisticalHelper.report(4011);
                    ImportVCardActivity.this.importVCardFromSDCard((VCardFile) ImportVCardActivity.this.mAllVCardFileList.get(this.mCurrentIndex));
                } else {
                    ImportVCardActivity.this.finish();
                }
            } else if (which == -2) {
                ImportVCardActivity.this.finish();
            } else if (this.mSelectcheck && which == 0) {
                int listViewsize = alerdialogListView.getCount();
                boolean checked = alerdialogListView.isItemChecked(which);
                for (i = 0; i < listViewsize; i++) {
                    alerdialogListView.setItemChecked(i, checked);
                    if (checked) {
                        this.mSelectedIndexSet.add(Integer.valueOf(i + 1));
                    } else {
                        this.mSelectedIndexSet.remove(Integer.valueOf(i + 1));
                    }
                }
            } else if (this.mSelectedIndexSet == null) {
                this.mCurrentIndex = which;
                if (!alerdialogListView.isItemChecked(which)) {
                    this.mCurrentIndex = -1;
                }
            } else if (this.mSelectedIndexSet.contains(Integer.valueOf(which))) {
                if (this.mSelectcheck) {
                    alerdialogListView.setItemChecked(0, false);
                }
                if (this.mSelectedIndexSet.size() - 1 == ImportVCardActivity.this.mAllVCardFileList.size()) {
                    this.mSelectedIndexSet.remove(Integer.valueOf(this.mSelectedIndexSet.size()));
                }
                this.mSelectedIndexSet.remove(Integer.valueOf(which));
            } else {
                this.mSelectedIndexSet.add(Integer.valueOf(which));
                if (this.mSelectedIndexSet.size() == ImportVCardActivity.this.mAllVCardFileList.size()) {
                    alerdialogListView.setItemChecked(0, true);
                } else {
                    alerdialogListView.setItemChecked(0, false);
                }
            }
        }

        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            if (which == 0 || this.mSelectedIndexSet.contains(Integer.valueOf(which)) != isChecked) {
                onClick(dialog, which);
                return;
            }
            if (this.mSelectcheck) {
                which--;
            }
            HwLog.e("VCardImport", String.format("Inconsist state in index %d (%s)", new Object[]{Integer.valueOf(which), ((VCardFile) ImportVCardActivity.this.mAllVCardFileList.get(which)).getCanonicalPath()}));
        }
    }

    private void startVCardSelectAndImport() {
        if (getResources().getBoolean(R.bool.config_import_all_vcard_from_sdcard_automatically) || this.mDeleteFileAfterImport) {
            importVCardFromSDCard(this.mAllVCardFileList);
        } else if (this.mProgressDialogForCachingVCard != null) {
        } else {
            if (getResources().getBoolean(R.bool.config_allow_users_select_all_vcard_import)) {
                runOnUiThread(new DialogDisplayer((int) R.id.dialog_select_import_type));
            } else {
                runOnUiThread(new DialogDisplayer((int) R.id.dialog_select_one_vcard));
            }
        }
    }

    private void importVCardFromSDCard(List<VCardFile> selectedVCardFileList) {
        String[] uriStrings = new String[selectedVCardFileList.size()];
        int i = 0;
        for (VCardFile vcardFile : selectedVCardFileList) {
            uriStrings[i] = "file://" + Uri.encode(vcardFile.getCanonicalPath(), "/");
            i++;
        }
        importVCard(uriStrings);
    }

    private void importVCardFromSDCard(VCardFile vcardFile) {
        importVCard(new Uri[]{Uri.parse("file://" + Uri.encode(vcardFile.getCanonicalPath(), "/"))});
    }

    private void importVCard(Uri uri) {
        importVCard(new Uri[]{uri});
    }

    private void importVCard(String[] uriStrings) {
        int length = uriStrings.length;
        Uri[] uris = new Uri[length];
        for (int i = 0; i < length; i++) {
            uris[i] = Uri.parse(uriStrings[i]);
        }
        importVCard(uris);
    }

    private void importVCard(final Uri[] uris) {
        this.mSelectedVcardCount = uris.length;
        runOnUiThread(new Runnable() {
            public void run() {
                ImportVCardActivity.this.mVCardCacheThread = new VCardCacheThread(uris);
                ImportVCardActivity.this.mListener = new NotificationImportExportListener(ImportVCardActivity.this);
                ImportVCardActivity.this.showDialogById(R.id.dialog_cache_vcard);
            }
        });
    }

    private Dialog getVCardFileSelectDialog(boolean multipleSelect) {
        int size;
        if (this.mAllVCardFileList != null) {
            size = this.mAllVCardFileList.size();
        } else {
            size = 0;
        }
        if (size == 0) {
            HwLog.e("VCardImport", "getVCardFileSelectDialog called where 0 vard files are found or vcard list is null:" + this.mAllVCardFileList);
            return null;
        }
        int lSelectAll = size <= 1 ? 0 : 1;
        VCardSelectedListener listener = new VCardSelectedListener(lSelectAll == 1);
        Builder builder = new Builder(this).setTitle(R.string.select_vcard_title).setPositiveButton(17039370, listener).setOnCancelListener(this.mCancelListener).setNegativeButton(17039360, this.mCancelListener);
        CharSequence[] items = new CharSequence[(size + lSelectAll)];
        if (lSelectAll == 1) {
            items[0] = getResources().getString(R.string.contact_menu_select_all);
        }
        for (int i = 0; i < size; i++) {
            VCardFile vcardFile = (VCardFile) this.mAllVCardFileList.get(i);
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            if (Locale.getDefault().getLanguage() == null || !CommonUtilMethods.isLayoutRTL()) {
                stringBuilder.append(vcardFile.getName());
            } else {
                stringBuilder.append("‎" + vcardFile.getName());
            }
            stringBuilder.append('\n');
            int indexToBeSpanned = stringBuilder.length();
            stringBuilder.append(DateUtils.formatDateTime(this, vcardFile.getLastModified(), 68117));
            stringBuilder.setSpan(new RelativeSizeSpan(0.7f), indexToBeSpanned, stringBuilder.length(), 33);
            stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.shortcut_item_data_textcolor)), indexToBeSpanned, stringBuilder.length(), 33);
            items[i + lSelectAll] = stringBuilder;
        }
        if (!multipleSelect) {
            builder.setSingleChoiceItems(items, 0, listener);
        } else if (size == 1) {
            builder.setMultiChoiceItems(items, new boolean[]{true}, listener);
        } else {
            builder.setMultiChoiceItems(items, (boolean[]) null, listener);
        }
        return builder.create();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        registerReceiver(this.homePressReceiver, this.homeFilter, "android.permission.INJECT_EVENTS", null);
        if (RequestImportVCardPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        Window win = getWindow();
        LayoutParams winParams = win.getAttributes();
        winParams.flags |= 67108864;
        win.setAttributes(winParams);
        setContentView(R.layout.vcard);
        Object obj = null;
        Object obj2 = null;
        String dataSet = null;
        Intent intent = getIntent();
        if (intent != null) {
            obj = intent.getStringExtra("account_name");
            obj2 = intent.getStringExtra("account_type");
            dataSet = intent.getStringExtra("data_set");
        } else {
            HwLog.e("VCardImport", "intent does not exist");
        }
        if (TextUtils.isEmpty(obj) || TextUtils.isEmpty(obj2)) {
            List<AccountWithDataSet> accountList = AccountTypeManager.getInstance(this).getAccountsExcludeSim(true);
            if (accountList.size() == 0) {
                this.mAccount = null;
            } else if (accountList.size() == 1) {
                this.mAccount = (AccountWithDataSet) accountList.get(0);
            } else {
                this.mAccountSelectionListener = new AccountSelectedListener(this, accountList, R.string.import_from_sdcard) {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ImportVCardActivity.this.mAccount = (AccountWithDataSet) this.mAccountList.get(which);
                        ImportVCardActivity.this.startImport();
                    }
                };
                if (isCallerSelf(this)) {
                    showDialogById(R.string.import_from_sdcard);
                } else {
                    ImportVCardDialogFragment.show(this, R.string.import_from_sdcard);
                }
                return;
            }
        }
        this.mAccount = new AccountWithDataSet(obj, obj2, dataSet);
        if (bundle != null) {
            if (bundle.getBoolean("recreate")) {
                finish();
            }
            this.mAllVCardFileList = (List) bundle.getSerializable("key_vcard_list");
        } else if (isCallerSelf(this)) {
            startImport();
        } else {
            ImportVCardDialogFragment.show(this, 0);
        }
    }

    private static boolean isCallerSelf(Activity activity) {
        ComponentName callingActivity = activity.getCallingActivity();
        if (callingActivity == null) {
            return false;
        }
        String packageName = callingActivity.getPackageName();
        if (packageName == null) {
            return false;
        }
        return packageName.equals(activity.getApplicationContext().getPackageName());
    }

    public void onImportVCardConfirmed(int resId) {
        if (resId == 0) {
            startImport();
        } else {
            showDialogById(resId);
        }
    }

    public void onImportVCardDenied() {
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode != 0) {
            return;
        }
        if (resultCode == -1) {
            this.mAccount = new AccountWithDataSet(intent.getStringExtra("account_name"), intent.getStringExtra("account_type"), intent.getStringExtra("data_set"));
            startImport();
            return;
        }
        if (resultCode != 0) {
            HwLog.w("VCardImport", "Result code was not OK nor CANCELED: " + resultCode);
        }
        finish();
    }

    private void startImport() {
        String directoryPath = null;
        Intent intent = getIntent();
        this.mImportFromDirectoryViaExternalIntent = TextUtils.equals("text/directory", intent.getType());
        this.mDeleteFileAfterImport = intent.getBooleanExtra("deleteonimport", false);
        String uristring = intent.getStringExtra("BluetoothImportContentUri");
        String uristringForWlan = intent.getStringExtra("WiFiDirectImportContentUri");
        Uri uri;
        if (uristring != null) {
            uri = Uri.parse(uristring);
            HwLog.i("VCardImport", "Starting vCard import for BT using Uri " + uri);
            importVCard(uri);
        } else if (uristringForWlan != null) {
            uri = Uri.parse(uristringForWlan);
            HwLog.i("VCardImport", "Starting vCard import for wlan using Uri " + uri);
            importVCard(uri);
        } else {
            uri = intent.getData();
            if (uri != null) {
                HwLog.i("VCardImport", "Starting vCard import using Uri " + uri);
                importVCard(uri);
                return;
            }
            HwLog.i("VCardImport", "Start vCard without Uri. The user will select vCard manually.");
            if (this.mImportFromDirectoryViaExternalIntent) {
                directoryPath = intent.getStringExtra("android.intent.extra.TEXT");
            }
            doScanExternalStorageAndImportVCard(directoryPath);
        }
    }

    protected Dialog onCreateDialog(int resId, Bundle bundle) {
        Builder builder;
        View view;
        String message;
        switch (resId) {
            case R.string.import_from_sdcard:
                if (this.mAccountSelectionListener != null) {
                    return AccountSelectionUtil.getSelectAccountDialog(this, resId, this.mAccountSelectionListener, this.mCancelListener, true);
                }
                throw new NullPointerException("mAccountSelectionListener must not be null.");
            case R.id.dialog_searching_vcard:
                if (this.mProgressDialogForScanVCard == null) {
                    this.mProgressDialogForScanVCard = ProgressDialog.show(this, "", getString(R.string.searching_vcard_message), true, false);
                    if (this.mVCardScanThread != null) {
                        this.mProgressDialogForScanVCard.setOnCancelListener(this.mVCardScanThread);
                        this.mVCardScanThread.start();
                    } else {
                        Toast.makeText(this, getString(R.string.vcard_import_failed_Toast), 1).show();
                        finish();
                    }
                }
                return this.mProgressDialogForScanVCard;
            case R.id.dialog_sdcard_not_found:
                builder = new Builder(this).setTitle(R.string.no_sdcard_title).setIconAttribute(16843605).setOnCancelListener(this.mCancelListener).setPositiveButton(R.string.contact_known_button_text, this.mCancelListener);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getString(R.string.failed_reason_index, new Object[]{getString(R.string.no_sdcard_message)}));
                builder.setView(view);
                return builder.create();
            case R.id.dialog_vcard_not_found:
                break;
            case R.id.dialog_select_import_type:
                Dialog dlg = getVCardFileSelectDialog(true);
                if (dlg != null) {
                    return dlg;
                }
                break;
            case R.id.dialog_select_one_vcard:
                return getVCardFileSelectDialog(false);
            case R.id.dialog_select_multiple_vcard:
                return getVCardFileSelectDialog(true);
            case R.id.dialog_cache_vcard:
                if (this.mProgressDialogForCachingVCard == null) {
                    message = getString(R.string.caching_vcard);
                    this.mProgressDialogForCachingVCard = new ProgressDialog(this);
                    this.mProgressDialogForCachingVCard.setMessage(message);
                    this.mProgressDialogForCachingVCard.setProgressStyle(0);
                    this.mProgressDialogForCachingVCard.setOnCancelListener(this.mVCardCacheThread);
                    startVCardService();
                }
                return this.mProgressDialogForCachingVCard;
            case R.id.dialog_io_exception:
                message = getString(R.string.failed_reason_index, new Object[]{getString(R.string.io_error_message_text)});
                builder = new Builder(this).setTitle(R.string.scanning_sdcard_failure_title).setIconAttribute(16843605).setOnCancelListener(this.mCancelListener).setPositiveButton(R.string.contact_known_button_text, this.mCancelListener);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                builder.setView(view);
                return builder.create();
            case R.id.dialog_error_with_message:
                if (TextUtils.isEmpty(this.mErrorMessage)) {
                    HwLog.e("VCardImport", "Error message is null while it must not.");
                    message = getString(R.string.failed_reason_index, new Object[]{getString(R.string.fail_reason_unknown)});
                } else {
                    message = getString(R.string.failed_reason_index, new Object[]{this.mErrorMessage});
                }
                builder = new Builder(this).setTitle(getString(R.string.reading_vcard_failed_title)).setIconAttribute(16843605).setOnCancelListener(this.mCancelListener).setPositiveButton(R.string.contact_known_button_text, this.mCancelListener);
                view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                builder.setView(view);
                return builder.create();
            default:
                return super.onCreateDialog(resId, bundle);
        }
        message = getString(R.string.failed_reason_index, new Object[]{getString(R.string.import_failure_no_vcard_file)});
        builder = new Builder(this).setTitle(R.string.import_from_sdcard_failed_title).setOnCancelListener(this.mCancelListener).setPositiveButton(R.string.contact_known_button_text, this.mCancelListener);
        view = getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
        ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
        builder.setView(view);
        return builder.create();
    }

    void startVCardService() {
        this.mConnection = new ImportRequestConnection();
        HwLog.i("VCardImport", "Bind to VCardService.");
        startService(new Intent(this, VCardService.class));
        getApplicationContext().bindService(new Intent(getApplicationContext(), VCardService.class), this.mConnection, 1);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (this.mProgressDialogForCachingVCard != null) {
            HwLog.i("VCardImport", "Cache thread is still running. Show progress dialog again.");
            showDialogById(R.id.dialog_cache_vcard);
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        if (this.mAllVCardFileList instanceof Serializable) {
            outState.putSerializable("key_vcard_list", (Serializable) this.mAllVCardFileList);
        }
        if (this.isImporting) {
            outState.putBoolean("recreate", true);
        }
        super.onSaveInstanceState(outState);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
        if (this.mService != null) {
            this.mService.setUpdateExportProgressDialog(false);
            this.mService.setIncomingImportMessenger(null);
            this.mService.setHideImportDialog(true);
            getApplicationContext().unbindService(this.mConnection);
        }
        removeDialog(R.id.dialog_select_import_type);
        if (this.homePressReceiver != null) {
            unregisterReceiver(this.homePressReceiver);
        }
    }

    private void doScanExternalStorageAndImportVCard(String filePath) {
        boolean flagNoAvailablePath = false;
        File[] fileArr = null;
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                fileArr = new File[]{file};
            } else {
                flagNoAvailablePath = true;
            }
        } else {
            ArrayList<String> filePaths = getAllExternalStoragePaths();
            ArrayList<File> files = new ArrayList();
            for (String path : filePaths) {
                File externalSDcardFile = new File(path);
                if (externalSDcardFile.exists() && externalSDcardFile.canRead() && externalSDcardFile.isDirectory()) {
                    files.add(externalSDcardFile);
                }
            }
            if (files.isEmpty()) {
                flagNoAvailablePath = true;
            } else {
                fileArr = new File[files.size()];
                files.toArray(fileArr);
            }
        }
        if (flagNoAvailablePath) {
            showDialogById(R.id.dialog_sdcard_not_found);
            return;
        }
        this.mVCardScanThread = new VCardScanThread(fileArr);
        showDialogById(R.id.dialog_searching_vcard);
    }

    void showFailureNotification(int reasonId) {
        ((NotificationManager) getSystemService("notification")).notify("VCardServiceFailure", 1, NotificationImportExportListener.constructImportFailureNotification(this, getString(reasonId)));
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(ImportVCardActivity.this, ImportVCardActivity.this.getString(R.string.vcard_import_failed_Toast), 1).show();
            }
        });
    }

    void showFailureNotification(int reasonId, final String aSourceFile) {
        String lSourceFile = aSourceFile;
        ((NotificationManager) getSystemService("notification")).notify("VCardServiceFailure", 1, NotificationImportExportListener.constructImportFailureNotification(this, getString(reasonId)));
        this.mHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(ImportVCardActivity.this, ImportVCardActivity.this.getString(R.string.vcard_file_import_failed_Toast, new Object[]{aSourceFile}), 1).show();
            }
        });
    }

    private ArrayList<String> getAllExternalStoragePaths() {
        StorageManager storageManager = (StorageManager) getSystemService("storage");
        StorageVolume[] storageVolumes = storageManager.getVolumeList();
        ArrayList<String> result = new ArrayList();
        for (StorageVolume volume : storageVolumes) {
            if (!(CommonUtilMethods.isUsbStorage(storageManager, volume) || "unmounted".equals(volume.getState()))) {
                result.add(volume.getPath());
            }
        }
        return result;
    }

    private void showProgressDialogWithButton(int count) {
        if (!isFinishing()) {
            this.copyCount = count;
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
                this.mProgressDialog = null;
            }
            this.mProgressDialog = new HwProgressDialog(this);
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setMessage(getString(R.string.import_contacts_message));
            this.mProgressDialog.setProgressStyle(1);
            this.mProgressDialog.setMax(count);
            this.mProgressDialog.setButton(-1, getString(R.string.hide), new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ImportVCardActivity.this.mService.setUpdateExportProgressDialog(false);
                    dialog.dismiss();
                    ImportVCardActivity.this.mService.setHideImportDialog(true);
                    ImportVCardActivity.this.finish();
                }
            });
            this.mProgressDialog.show();
            View cancelView = this.mProgressDialog.getCancelButton();
            if (cancelView != null) {
                cancelView.setContentDescription(getString(R.string.stop));
                cancelView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ImportVCardActivity.this.showAlertDialog();
                    }
                });
            }
            if (this.mService != null) {
                this.mService.setHideImportDialog(false);
            }
        }
    }

    private void showAlertDialog() {
        AlertDialog mAlertDialog = new Builder(this).create();
        mAlertDialog.setMessage(getString(R.string.stop_import_message));
        mAlertDialog.setMessageNotScrolling();
        OnClickListener aListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    ImportVCardActivity.this.mService.cancelCurrentWorking(1);
                    if (ImportVCardActivity.this.mProgressDialog != null) {
                        ImportVCardActivity.this.mProgressDialog.dismiss();
                    }
                    ImportVCardActivity.this.finish();
                    return;
                }
                ImportVCardActivity.this.mProgressDialog.show();
                ImportVCardActivity.this.mProgressDialog.setProgress(ImportVCardActivity.this.mCurrentCount);
            }
        };
        mAlertDialog.setButton(-1, getString(R.string.stop), aListener);
        mAlertDialog.setButton(-2, getString(R.string.cancel), aListener);
        mAlertDialog.show();
    }

    protected void onPause() {
        super.onPause();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.show();
        }
    }

    protected void onRestart() {
        super.onRestart();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.show();
        }
    }

    private void showDialogById(int resId) {
        this.mCurrentDialogId = resId;
        showDialog(this.mCurrentDialogId);
    }

    public void finish() {
        removeDialog(this.mCurrentDialogId);
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void setVcardCacheIllegal(boolean flag) {
        this.mVcardIllegalFormat = flag;
    }

    private boolean getVcardCacheIllegal() {
        return this.mVcardIllegalFormat;
    }
}
