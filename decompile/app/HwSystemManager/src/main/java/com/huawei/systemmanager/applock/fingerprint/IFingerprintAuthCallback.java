package com.huawei.systemmanager.applock.fingerprint;

public interface IFingerprintAuthCallback {
    void onFingerprintAuthFailed();

    void onFingerprintAuthLockout();

    void onFingerprintAuthSuccess();
}
