package com.android.mms.transaction;

public abstract class AbstractRetryScheme {
    protected int mRetriedTimes;

    public AbstractRetryScheme(int retriedTimes) {
        this.mRetriedTimes = retriedTimes;
    }
}
