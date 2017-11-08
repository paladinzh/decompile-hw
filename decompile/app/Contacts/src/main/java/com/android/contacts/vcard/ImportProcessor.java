package com.android.contacts.vcard;

import android.accounts.Account;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.util.AutoMergeContacts;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.vcard.VCardEntry;
import com.android.vcard.VCardEntryConstructor;
import com.android.vcard.VCardEntryHandler;
import com.android.vcard.VCardInterpreter;
import com.android.vcard.VCardParser;
import com.android.vcard.VCardParser_V21;
import com.android.vcard.VCardParser_V30;
import com.android.vcard.exception.VCardException;
import com.android.vcard.exception.VCardNestedException;
import com.android.vcard.exception.VCardNotSupportedException;
import com.android.vcard.exception.VCardVersionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImportProcessor extends ProcessorBase implements VCardEntryHandler {
    private static final boolean DEBUG = HwLog.HWDBG;
    VCardEntryCommitterCustom committer;
    private Application mApp;
    private volatile boolean mCanceled;
    private Context mContext = null;
    private int mCurrentCount = 0;
    private volatile boolean mDone;
    private final List<Uri> mFailedUris = new ArrayList();
    private final ImportRequest mImportRequest;
    private final int mJobId;
    private VCardImportExportListener mListener;
    private volatile boolean mNotified;
    private final ContentResolver mResolver;
    private final VCardService mService;
    private int mTotalCount = 0;
    boolean mVCardEntryCommitterImportNeedSleep = false;
    private VCardParser mVCardParser;

    public ImportProcessor(VCardService service, VCardImportExportListener listener, ImportRequest request, int jobId, Context context, Application app) {
        this.mService = service;
        this.mResolver = this.mService.getContentResolver();
        this.mListener = listener;
        this.mImportRequest = request;
        this.mJobId = jobId;
        this.mContext = context;
        this.mApp = app;
    }

    public void onStart() {
    }

    public void onEnd() {
    }

    public void onEntryCreated(VCardEntry entry) {
        this.mCurrentCount++;
        if (this.mListener == null) {
            return;
        }
        if (this.mCurrentCount % 25 == 1 || this.mCurrentCount >= this.mTotalCount) {
            this.mListener.onImportParsed(this.mImportRequest, this.mJobId, entry, this.mCurrentCount, this.mTotalCount, this.mService);
            this.mService.sendMessage(1, this.mCurrentCount, this.mImportRequest.displayName);
        }
    }

    public final int getType() {
        return 1;
    }

    public VCardImportExportListener getListener() {
        return this.mListener;
    }

    public void run() {
        try {
            runInternal();
            if (this.mCanceled) {
                this.mService.sendMessage(1, -1, null);
            }
            if (!(!this.mCanceled || this.mListener == null || this.mNotified)) {
                this.mListener.onImportCanceled(this.mImportRequest, this.mJobId);
            }
            synchronized (this) {
                this.mDone = true;
            }
        } catch (SQLiteFullException e) {
            ExceptionCapture.captureImportVcardException("SQLiteFullException thrown during import");
            this.mListener.onMemoryFull(this.mImportRequest, this.mJobId);
            this.mService.sendMessage(1, -1, null);
            synchronized (this) {
                this.mDone = true;
            }
        } catch (OutOfMemoryError e2) {
            HwLog.e("VCardImport", "OutOfMemoryError thrown during import", e2);
            ExceptionCapture.captureImportVcardException("OutOfMemoryError thrown during import");
            throw e2;
        } catch (RuntimeException e3) {
            HwLog.e("VCardImport", "RuntimeException thrown during import", e3);
            ExceptionCapture.captureImportVcardException("RuntimeException thrown during import");
            throw e3;
        } catch (Throwable th) {
            synchronized (this) {
                this.mDone = true;
            }
        }
    }

    private void runInternal() {
        HwLog.i("VCardImport", String.format("vCard import (id: %d) has started.", new Object[]{Integer.valueOf(this.mJobId)}));
        this.mService.setCurrentWorkingJobId(this.mJobId);
        ImportRequest request = this.mImportRequest;
        if (this.mCanceled) {
            HwLog.i("VCardImport", "Canceled before actually handling parameter (" + request.uri + ")");
            return;
        }
        int[] possibleVCardVersions = request.vcardVersion == 0 ? new int[]{1, 2} : new int[]{request.vcardVersion};
        Uri uri = request.uri;
        Account account = request.account;
        int estimatedVCardType = request.estimatedVCardType;
        String estimatedCharset = request.estimatedCharset;
        this.mTotalCount += request.entryCount;
        this.mService.sendMessage(2, this.mTotalCount, null);
        VCardEntryConstructor constructor = new VCardEntryConstructor(estimatedVCardType, account, estimatedCharset);
        this.committer = new VCardEntryCommitterCustom(this.mResolver, this.mApp);
        this.committer.setImportVcardNeedSleep(this.mVCardEntryCommitterImportNeedSleep);
        constructor.addEntryHandler(this.committer);
        constructor.addEntryHandler(this);
        InputStream inputStream = null;
        boolean successful = false;
        if (uri != null) {
            try {
                HwLog.i("VCardImport", "start importing one vCard (Uri: " + uri + ")");
                inputStream = this.mResolver.openInputStream(uri);
            } catch (IOException e) {
                successful = false;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e2) {
                    }
                }
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e3) {
                    }
                }
            }
        } else if (request.data != null) {
            HwLog.i("VCardImport", "start importing one vCard (byte[])");
            inputStream = new ByteArrayInputStream(request.data);
        }
        if (inputStream != null) {
            successful = readOneVCard(inputStream, estimatedVCardType, estimatedCharset, constructor, possibleVCardVersions);
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e4) {
            }
        }
        this.mService.handleFinishImportNotification(this.mJobId, successful);
        if (successful) {
            if (this.mCanceled) {
                HwLog.i("VCardImport", "vCard import has been canceled (uri: " + uri + ")");
            } else {
                HwLog.i("VCardImport", "Successfully finished importing one vCard file: " + uri);
                List<Uri> uris = this.committer.getCreatedUris();
                if (this.mListener != null) {
                    if (uris == null || uris.size() <= 0) {
                        HwLog.w("VCardImport", "Created Uris is null or 0 length though the creation itself is successful.");
                        this.mListener.onImportFinished(this.mImportRequest, this.mJobId, null);
                    } else {
                        this.mListener.onImportFinished(this.mImportRequest, this.mJobId, (Uri) uris.get(0));
                    }
                }
            }
            if (!(account == null || !"com.android.huawei.phone".equals(account.type) || CommonUtilMethods.isMergeFeatureEnabled())) {
                AutoMergeContacts.autoMergeRawContacts(this.mContext);
            }
        } else {
            HwLog.w("VCardImport", "Failed to read one vCard file: " + uri);
            this.mFailedUris.add(uri);
        }
    }

    private boolean readOneVCard(InputStream is, int vcardType, String charset, VCardInterpreter interpreter, int[] possibleVCardVersions) {
        boolean successful = false;
        int length = possibleVCardVersions.length;
        int i = 0;
        while (i < length) {
            int vcardVersion = possibleVCardVersions[i];
            if (i > 0) {
                try {
                    if (interpreter instanceof VCardEntryConstructor) {
                        ((VCardEntryConstructor) interpreter).clear();
                    }
                } catch (IOException e) {
                    HwLog.e("VCardImport", "IOException was emitted: " + e.getMessage());
                    ExceptionCapture.captureImportVcardException("ImportProcessor->readOneVCard IOException was emitted");
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e2) {
                        }
                    }
                    i++;
                } catch (VCardNestedException e3) {
                    HwLog.e("VCardImport", "Nested Exception is found.");
                    ExceptionCapture.captureImportVcardException("ImportProcessor->readOneVCard Nested Exception is found.");
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e4) {
                        }
                    }
                    i++;
                } catch (VCardNotSupportedException e5) {
                    try {
                        HwLog.e("VCardImport", e5.toString());
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e6) {
                            }
                        }
                        i++;
                    } catch (Throwable th) {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e7) {
                            }
                        }
                    }
                } catch (VCardVersionException e8) {
                    if (i == length - 1) {
                        HwLog.e("VCardImport", "Appropriate version for this vCard is not found.");
                        ExceptionCapture.captureImportVcardException("ImportProcessor->readOneVCard Appropriate version for this vCard is not found.");
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e9) {
                        }
                    }
                    i++;
                } catch (VCardException e10) {
                    HwLog.e("VCardImport", e10.toString());
                    ExceptionCapture.captureImportVcardException("ImportProcessor->readOneVCard VCardException is found.");
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e11) {
                        }
                    }
                    i++;
                }
            }
            synchronized (this) {
                VCardParser vCardParser_V30;
                if (vcardVersion == 2) {
                    vCardParser_V30 = new VCardParser_V30(vcardType);
                } else {
                    vCardParser_V30 = new VCardParser_V21(vcardType);
                }
                this.mVCardParser = vCardParser_V30;
                if (this.mCanceled) {
                    HwLog.i("VCardImport", "ImportProcessor already recieves cancel request, so send cancel request to vCard parser too.");
                    this.mVCardParser.cancel();
                }
            }
            this.mVCardParser.parse(is, interpreter);
            successful = true;
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e12) {
                }
            }
            return successful;
        }
        return successful;
    }

    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (DEBUG) {
            HwLog.d("VCardImport", "ImportProcessor received cancel request");
        }
        if (this.mDone || this.mCanceled) {
            return false;
        }
        this.mCanceled = true;
        synchronized (this) {
            if (this.mVCardParser != null) {
                this.mVCardParser.cancel();
            }
        }
        return true;
    }

    public synchronized boolean isCancelled() {
        return this.mCanceled;
    }

    public synchronized boolean isDone() {
        return this.mDone;
    }

    public ImportRequest getRequest() {
        return this.mImportRequest;
    }

    public synchronized void cancelAndNotified(boolean aNotified) {
        if (DEBUG) {
            HwLog.d("VCardImport", "received cancel request and notified");
        }
        this.mListener = null;
        this.mNotified = true;
    }

    public VCardEntryCommitterCustom getVCardEntryCommitter() {
        return this.committer;
    }

    public void setVCardEntryCommitterImportNeedSleep(boolean mVCardEntryCommitterImportNeedSleep) {
        this.mVCardEntryCommitterImportNeedSleep = mVCardEntryCommitterImportNeedSleep;
    }
}
