package com.android.contacts.activities;

import android.app.Activity;
import android.os.Bundle;

public abstract class TransactionSafeActivity extends Activity {
    private boolean mIsSafeToCommitTransactions;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mIsSafeToCommitTransactions = true;
    }

    protected void onStart() {
        super.onStart();
        this.mIsSafeToCommitTransactions = true;
    }

    protected void onResume() {
        super.onResume();
        this.mIsSafeToCommitTransactions = true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mIsSafeToCommitTransactions = false;
    }

    public boolean isSafeToCommitTransactions() {
        return this.mIsSafeToCommitTransactions;
    }
}
