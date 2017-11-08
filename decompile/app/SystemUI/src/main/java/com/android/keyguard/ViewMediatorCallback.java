package com.android.keyguard;

public interface ViewMediatorCallback {
    int getBouncerPromptReason();

    boolean isInputRestricted();

    boolean isScreenOn();

    void keyguardDone(boolean z);

    void keyguardDoneDrawing();

    void keyguardDonePending(boolean z);

    void keyguardGone();

    void playTrustedSound();

    void readyForKeyguardDone();

    void resetKeyguard();

    void setNeedsInput(boolean z);

    void userActivity();
}
