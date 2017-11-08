package com.android.keyguard.hwlockscreen;

import android.content.Intent;
import android.view.animation.Animation;

public interface HwUnlockInterface$LockScreenCallback {
    String getOwnerInfo();

    boolean isScreenOn();

    boolean isShowOwnerInfo();

    void onTrigger(Intent intent, Animation animation);

    void setClickKey(int i);
}
