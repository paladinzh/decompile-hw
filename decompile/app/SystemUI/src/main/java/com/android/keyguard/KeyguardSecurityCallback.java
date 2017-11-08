package com.android.keyguard;

public interface KeyguardSecurityCallback {
    void dismiss(boolean z);

    boolean isVerifyUnlockOnly();

    void reportUnlockAttempt(int i, boolean z, int i2);

    void reset();

    void showBackupSecurity();

    void userActivity();
}
