package com.huawei.systemmanager.applock.fingerprint;

public interface IFingerprintAuth {
    boolean cancelAuthenticate();

    int getRemainedNum();

    long getRemainedTime();

    boolean isFingerprintReady();

    void release();

    boolean startAuthenticate(IFingerprintAuthCallback iFingerprintAuthCallback);
}
