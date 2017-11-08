package com.android.keyguard.hwlockscreen;

import com.android.internal.widget.LockPatternUtils;

public interface HwUnlockInterface$HwLockScreenReal {
    boolean needsInput();

    void onBatteryInfoChanged();

    void onPhoneStateChanged();

    void onResume();

    void onTimeChanged();

    void setLockPatternUtils(LockPatternUtils lockPatternUtils);

    void setLockScreenCallback(HwUnlockInterface$LockScreenCallback hwUnlockInterface$LockScreenCallback);
}
