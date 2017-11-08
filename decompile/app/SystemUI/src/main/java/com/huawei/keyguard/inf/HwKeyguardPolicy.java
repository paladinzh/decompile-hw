package com.huawei.keyguard.inf;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.SparseIntArray;
import android.view.View;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.systemui.IPhoneStatusBar;

public class HwKeyguardPolicy {
    private static HwKeyguardPolicy inst = new HwKeyguardPolicy();
    private IPhoneStatusBar mPhoneStatusBar = null;

    public static HwKeyguardPolicy getInst() {
        return inst;
    }

    public static boolean isUseGgStatusView() {
        return KeyguardTheme.getInst().showGgStatusView();
    }

    public static boolean isSupportAnyDirectionUnlock() {
        return true;
    }

    public static boolean isUseGgBottomView() {
        return false;
    }

    public void setPhoneStatusBar(IPhoneStatusBar manager) {
        this.mPhoneStatusBar = manager;
    }

    public int getMaxKeyguardSportMusicNotifications() {
        if (KeyguardTheme.getInst().getLockStyle() == 8) {
            return 2;
        }
        return -1;
    }

    public void dismiss() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.dismissKeyguard();
        } else {
            HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for dismiss");
        }
    }

    public void userActivity() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.userActivity();
        } else {
            HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for userActivity");
        }
    }

    public void startActivity(Intent intent, boolean dismissShade) {
        if (CoverViewManager.getInstance(GlobalContext.getContext()).isCoverAdded()) {
            HwLog.w("HwKeyguardPolicy", "startActivity skiped as cover closed");
            return;
        }
        if (this.mPhoneStatusBar != null) {
            try {
                this.mPhoneStatusBar.startActivity(intent, dismissShade);
            } catch (ActivityNotFoundException ex) {
                HwLog.w("HwKeyguardPolicy", "startActivity fail!" + ex);
            }
        } else {
            HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for startActivity");
        }
    }

    public void preventNextAnimation() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.preventNextAnimation();
        } else {
            HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for preventNextAnimation");
        }
    }

    public IFlashlightController getFlashlightController() {
        if (this.mPhoneStatusBar != null) {
            return this.mPhoneStatusBar.getFlashlightController();
        }
        HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for getFlashlightController");
        return null;
    }

    public View getNotificationStackScrollerView() {
        if (this.mPhoneStatusBar != null) {
            return this.mPhoneStatusBar.getNotificationStackScrollerView();
        }
        HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for getNotificationStackScrollerView");
        return null;
    }

    public boolean updateMediaMetaData(boolean metaDataChanged, boolean allowEnterAnimation) {
        return true;
    }

    public boolean showNavigationBarInbouncer() {
        return false;
    }

    public boolean showKeyguardStatusBarInbouncer() {
        return true;
    }

    public boolean blockNotificationInKeyguard() {
        int style = KeyguardTheme.getInst().getLockStyle();
        if (style == 3 || style == 4) {
            return true;
        }
        return false;
    }

    public boolean isSkipKeyguardView(Context context) {
        return false;
    }

    public void updateKeyguardState(int oldState, int newState, boolean goingToFullShade, boolean fromShadeLocked) {
        HwLog.w("HwKeyguardPolicy", "KGSvcCall updateKeyguardState " + oldState + "->" + newState + "; " + goingToFullShade + " " + fromShadeLocked);
    }

    public boolean isSupportCameraGesture() {
        return false;
    }

    public void onLockscreenWallpaperChanged(Context context) {
        AppHandler.sendMessage(22);
    }

    public Bitmap getLockScreenWallpaper() {
        if (this.mPhoneStatusBar != null) {
            Bitmap retBmp = this.mPhoneStatusBar.getLockScreenWallpaper();
            HwLog.v("HwKeyguardPolicy", "getLockScreenWallpaper: " + retBmp);
            return retBmp;
        }
        HwLog.e("HwKeyguardPolicy", "No PhoneStatusBar for getLockScreenWallpaper");
        return null;
    }

    public boolean onBackPressed() {
        return this.mPhoneStatusBar.onBackPressed();
    }

    public boolean updateKeyguardStatusbarColor(SparseIntArray resultMap) {
        if (this.mPhoneStatusBar == null || resultMap == null) {
            return false;
        }
        return this.mPhoneStatusBar.updateKeyguardStatusbarColor(resultMap);
    }

    public View getCoverStatusBarView() {
        return this.mPhoneStatusBar != null ? this.mPhoneStatusBar.getCoverStatusBarView() : null;
    }

    public void removeFingerprintMsg() {
        if (this.mPhoneStatusBar != null) {
            this.mPhoneStatusBar.removeFingerprintMsg();
        }
    }

    public boolean isQsExpanded() {
        if (this.mPhoneStatusBar != null) {
            return this.mPhoneStatusBar.isQsExpanded();
        }
        return false;
    }
}
