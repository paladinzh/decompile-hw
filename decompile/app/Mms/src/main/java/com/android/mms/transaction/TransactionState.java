package com.android.mms.transaction;

import android.net.Uri;

public class TransactionState {
    private Uri mContentUri = null;
    private int mState = 0;

    public synchronized int getState() {
        return this.mState;
    }

    synchronized void setState(int state) {
        if (state < 0 || state > 2) {
            throw new IllegalArgumentException("Bad state: " + state);
        }
        this.mState = state;
    }

    public synchronized Uri getContentUri() {
        return this.mContentUri;
    }

    synchronized void setContentUri(Uri uri) {
        this.mContentUri = uri;
    }

    public String translateState(int state) {
        switch (state) {
            case 0:
                return "INITIALIZED";
            case 1:
                return "SUCCESS";
            default:
                return "FAILED";
        }
    }

    public String toString() {
        return translateState(this.mState) + ", ContentUri:" + this.mContentUri;
    }
}
