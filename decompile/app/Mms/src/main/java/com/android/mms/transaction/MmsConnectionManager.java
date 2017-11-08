package com.android.mms.transaction;

import android.net.http.AndroidHttpClient;

public class MmsConnectionManager {
    private boolean mDownloadingStatus = false;
    private boolean mManualDownloadMode = false;
    private AndroidHttpClient mMmsTransactionCleint = null;
    private boolean mUserStopTransaction = false;

    public boolean getUserStopTransaction() {
        return this.mUserStopTransaction;
    }

    public void setUserStopTransaction(boolean isUserStop) {
        this.mUserStopTransaction = isUserStop;
    }

    public boolean getDownloadingStatus() {
        return this.mDownloadingStatus;
    }

    public void setDownloadingStatus(boolean status) {
        this.mDownloadingStatus = status;
    }

    public boolean getManualDownloadMode() {
        return this.mManualDownloadMode;
    }

    public void setManualDownloadMode(boolean ManualDownload) {
        this.mManualDownloadMode = ManualDownload;
    }

    public AndroidHttpClient getMmsTransactionCleint() {
        return this.mMmsTransactionCleint;
    }

    public void setMmsTransactionCleint(AndroidHttpClient client) {
        this.mMmsTransactionCleint = client;
    }
}
