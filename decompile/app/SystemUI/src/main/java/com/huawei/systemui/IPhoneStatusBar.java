package com.huawei.systemui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.SparseIntArray;
import android.view.View;
import com.huawei.keyguard.inf.IFlashlightController;

public interface IPhoneStatusBar {
    void dismissKeyguard();

    View getCoverStatusBarView();

    IFlashlightController getFlashlightController();

    Bitmap getLockScreenWallpaper();

    View getNotificationStackScrollerView();

    boolean isQsExpanded();

    boolean onBackPressed();

    void preventNextAnimation();

    void removeFingerprintMsg();

    void startActivity(Intent intent, boolean z);

    boolean updateKeyguardStatusbarColor(SparseIntArray sparseIntArray);

    void userActivity();
}
