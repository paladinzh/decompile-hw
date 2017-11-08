package com.android.contacts.vcard;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.vcard.VCardComposer;
import com.android.vcard.VCardConfig;
import com.google.android.gms.R;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class ExportProcessor extends ProcessorBase {
    private volatile boolean mCanceled;
    private volatile boolean mDone;
    private final ExportRequest mExportRequest;
    private final int mJobId;
    private final NotificationManager mNotificationManager = ((NotificationManager) this.mService.getSystemService("notification"));
    private volatile boolean mNotified;
    private final ContentResolver mResolver;
    private final VCardService mService;

    public ExportProcessor(VCardService service, ExportRequest exportRequest, int jobId, String callingActivity) {
        this.mService = service;
        this.mResolver = service.getContentResolver();
        this.mExportRequest = exportRequest;
        this.mJobId = jobId;
    }

    public final int getType() {
        return 2;
    }

    public void run() {
        try {
            runInternal();
            if (isCancelled() && !this.mNotified) {
                doCancelNotification();
            }
            synchronized (this) {
                this.mDone = true;
            }
        } catch (OutOfMemoryError e) {
            HwLog.e("VCardExport", "OutOfMemoryError thrown during import", e);
            ExceptionCapture.captureExportVcardException("OutOfMemoryError thrown during export");
            throw e;
        } catch (RuntimeException e2) {
            HwLog.e("VCardExport", "RuntimeException thrown during export", e2);
            ExceptionCapture.captureExportVcardException("RuntimeException thrown during export");
            throw e2;
        } catch (Throwable th) {
            synchronized (this) {
                this.mDone = true;
            }
        }
    }

    private void runInternal() {
        Throwable th;
        this.mService.setCurrentWorkingJobId(this.mJobId);
        ExportRequest request = this.mExportRequest;
        VCardComposer vCardComposer = null;
        Writer writer = null;
        boolean successful = false;
        Uri uri;
        try {
            if (isCancelled()) {
                HwLog.i("VCardExport", "Export request is cancelled before handling the request");
                this.mService.handleFinishExportNotification(this.mJobId, false);
                this.mService.showExportProcess(false, 0, 0);
                return;
            }
            int vcardType;
            uri = request.destUri;
            OutputStream outputStream = this.mResolver.openOutputStream(uri);
            String exportType = request.exportType;
            if (TextUtils.isEmpty(exportType)) {
                vcardType = VCardConfig.getVCardTypeFromString(this.mService.getString(R.string.config_export_vcard_type));
            } else {
                vcardType = VCardConfig.getVCardTypeFromString(exportType);
            }
            VCardComposer composer = new VCardComposer(this.mService, vcardType, true);
            try {
                Writer writer2 = new BufferedWriter(new OutputStreamWriter(outputStream, CommonConstants.DEFAULT_CHARSET));
                try {
                    String title;
                    if (initComposer(composer, RawContactsEntity.CONTENT_URI.buildUpon().appendQueryParameter("for_export_only", CallInterceptDetails.BRANDED_STATE).build())) {
                        int total = composer.getCount();
                        if (total == 0) {
                            title = this.mService.getString(R.string.fail_reason_no_exportable_contact);
                            if (!new File(uri.getPath()).delete()) {
                                HwLog.w("VCardExport", "Failed to delete file: " + uri.getPath());
                            }
                            doFinishNotification(title, null);
                            if (composer != null) {
                                composer.terminate();
                            }
                            if (writer2 != null) {
                                try {
                                    writer2.close();
                                } catch (IOException e) {
                                    HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e);
                                }
                            }
                            this.mService.handleFinishExportNotification(this.mJobId, false);
                            this.mService.showExportProcess(false, 0, 0);
                            return;
                        }
                        int current = 1;
                        while (!composer.isAfterLast()) {
                            if (isCancelled()) {
                                HwLog.i("VCardExport", "Export request is cancelled during composing vCard");
                                if (composer != null) {
                                    composer.terminate();
                                }
                                if (writer2 != null) {
                                    try {
                                        writer2.close();
                                    } catch (IOException e2) {
                                        HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e2);
                                    }
                                }
                                this.mService.handleFinishExportNotification(this.mJobId, false);
                                this.mService.showExportProcess(false, 0, 0);
                                return;
                            }
                            try {
                                writer2.write(composer.createOneEntry());
                                if (current % 100 == 1) {
                                    doProgressNotification(uri, total, current);
                                }
                                if (total >= 100 && current % 100 == 1) {
                                    this.mService.showExportProcess(true, current, total);
                                }
                                current++;
                            } catch (IOException e3) {
                                HwLog.e("VCardExport", "Failed to read a contact: " + composer.getErrorReason());
                                if (!new File(uri.getPath()).delete()) {
                                    HwLog.w("VCardExport", "Failed to delete file: " + uri.getPath());
                                }
                                doFinishNotification(this.mService.getString(R.string.exporting_contact_failed_title), this.mService.getString(R.string.fail_reason_error_occurred_during_export, new Object[]{this.mService.getString(R.string.io_error_message)}));
                                if (composer != null) {
                                    composer.terminate();
                                }
                                if (writer2 != null) {
                                    try {
                                        writer2.close();
                                    } catch (IOException e22) {
                                        HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e22);
                                    }
                                }
                                this.mService.handleFinishExportNotification(this.mJobId, false);
                                this.mService.showExportProcess(false, 0, 0);
                                return;
                            }
                        }
                        try {
                            writer2.flush();
                            HwLog.i("VCardExport", "Successfully finished exporting vCard " + request.destUri);
                            this.mService.updateMediaScanner(request.destUri.getPath());
                            successful = true;
                            String filename = uri.getLastPathSegment();
                            doFinishNotification(this.mService.getString(R.string.exporting_vcard_finished_title, new Object[]{filename}), null);
                            if (composer != null) {
                                composer.terminate();
                            }
                            if (writer2 != null) {
                                try {
                                    writer2.close();
                                } catch (IOException e222) {
                                    HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e222);
                                }
                            }
                            this.mService.handleFinishExportNotification(this.mJobId, true);
                            this.mService.showExportProcess(false, 0, 0);
                            return;
                        } catch (IOException e4) {
                            HwLog.e("VCardExport", "Failed to flush stream to vcard file error.");
                            if (!new File(uri.getPath()).delete()) {
                                HwLog.w("VCardExport", "Failed to delete file: " + uri.getPath());
                            }
                            doFinishNotification(this.mService.getString(R.string.exporting_contact_failed_title), this.mService.getString(R.string.fail_reason_error_occurred_during_export, new Object[]{this.mService.getString(R.string.io_error_message)}));
                            if (composer != null) {
                                composer.terminate();
                            }
                            if (writer2 != null) {
                                try {
                                    writer2.close();
                                } catch (IOException e2222) {
                                    HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e2222);
                                }
                            }
                            this.mService.handleFinishExportNotification(this.mJobId, false);
                            this.mService.showExportProcess(false, 0, 0);
                            return;
                        }
                    }
                    String errorReason = composer.getErrorReason();
                    HwLog.e("VCardExport", "initialization of vCard composer failed: " + errorReason);
                    ExceptionCapture.captureExportVcardException("initialization of vCard composer failed: " + errorReason);
                    String translatedErrorReason = translateComposerError(errorReason);
                    if (!new File(uri.getPath()).delete()) {
                        HwLog.w("VCardExport", "Failed to delete file: " + uri.getPath());
                    }
                    Thread.sleep(20);
                    if ("No error".equalsIgnoreCase(translatedErrorReason)) {
                        String desc = null;
                        if (composer.getCount() == 0) {
                            title = this.mService.getString(R.string.exporting_contact_failed_title);
                            desc = this.mService.getString(R.string.fail_reason_no_exportable_contact);
                        } else {
                            title = this.mService.getString(R.string.fail_reason_could_not_initialize_exporter, new Object[]{translatedErrorReason});
                        }
                        doFinishNotification(title, desc);
                    } else {
                        doFinishNotification(this.mService.getString(R.string.fail_reason_could_not_initialize_exporter), translatedErrorReason);
                    }
                    if (composer != null) {
                        composer.terminate();
                    }
                    if (writer2 != null) {
                        try {
                            writer2.close();
                        } catch (IOException e22222) {
                            HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e22222);
                        }
                    }
                    this.mService.handleFinishExportNotification(this.mJobId, false);
                    this.mService.showExportProcess(false, 0, 0);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (Throwable th2) {
                    th = th2;
                    writer = writer2;
                    vCardComposer = composer;
                }
            } catch (Throwable th3) {
                th = th3;
                vCardComposer = composer;
                if (vCardComposer != null) {
                    vCardComposer.terminate();
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e222222) {
                        HwLog.w("VCardExport", "IOException is thrown during close(). Ignored. " + e222222);
                    }
                }
                this.mService.handleFinishExportNotification(this.mJobId, successful);
                this.mService.showExportProcess(false, 0, 0);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            HwLog.w("VCardExport", "FileNotFoundException thrown", e5);
            doFinishNotification(this.mService.getString(R.string.fail_reason_could_not_open_file, new Object[]{uri, e5.getMessage()}), null);
            this.mService.handleFinishExportNotification(this.mJobId, false);
            this.mService.showExportProcess(false, 0, 0);
        } catch (Throwable th4) {
            th = th4;
            if (vCardComposer != null) {
                vCardComposer.terminate();
            }
            if (writer != null) {
                writer.close();
            }
            this.mService.handleFinishExportNotification(this.mJobId, successful);
            this.mService.showExportProcess(false, 0, 0);
            throw th;
        }
    }

    private String translateComposerError(String errorMessage) {
        Resources resources = this.mService.getResources();
        if ("Failed to get database information".equals(errorMessage)) {
            return resources.getString(R.string.composer_failed_to_get_database_infomation);
        }
        if ("There's no exportable in the database".equals(errorMessage)) {
            return resources.getString(R.string.composer_has_no_exportable_contact);
        }
        if ("The vCard composer object is not correctly initialized".equals(errorMessage)) {
            return resources.getString(R.string.composer_not_initialized);
        }
        return errorMessage;
    }

    private void doProgressNotification(Uri uri, int totalCount, int currentCount) {
        String displayName = uri.getLastPathSegment();
        this.mNotificationManager.notify("VCardServiceProgress", this.mJobId, NotificationImportExportListener.constructProgressNotification(this.mService, 2, this.mService.getString(R.string.exporting_contact_list_message, new Object[]{displayName}), this.mService.getString(R.string.exporting_contact_list_title), this.mJobId, displayName, totalCount, currentCount));
    }

    public void doCancelNotification() {
        this.mNotificationManager.cancelAll();
    }

    private void doFinishNotification(String title, String description) {
        this.mNotificationManager.cancelAll();
    }

    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (this.mDone || this.mCanceled) {
            return false;
        }
        this.mCanceled = true;
        return true;
    }

    public synchronized boolean isCancelled() {
        return this.mCanceled;
    }

    public synchronized boolean isDone() {
        return this.mDone;
    }

    public ExportRequest getRequest() {
        return this.mExportRequest;
    }

    public synchronized void cancelAndNotified(boolean aNotified) {
        this.mNotified = true;
    }

    private boolean initComposer(VCardComposer aComposer, Uri aContentUri) {
        String selection = null;
        if (this.mExportRequest.selectedContactIds == null) {
            return false;
        }
        if (this.mExportRequest.mIsSelectedContacts && this.mExportRequest.selectedContactIds.length > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("_id IN (");
            for (long valueOf : this.mExportRequest.selectedContactIds) {
                builder.append(Long.valueOf(valueOf));
                builder.append(",");
            }
            builder.setLength(builder.length() - 1);
            builder.append(")");
            selection = builder.toString();
        }
        return aComposer.init(Contacts.CONTENT_URI, new String[]{"_id"}, selection, null, null, aContentUri);
    }
}
