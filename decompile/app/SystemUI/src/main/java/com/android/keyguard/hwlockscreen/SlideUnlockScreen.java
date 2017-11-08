package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.MusicUtils;

public class SlideUnlockScreen extends RelativeLayout implements HwUnlockInterface$HwLockScreenReal {
    private TextView mChargeStatus;
    private HwUnlockInterface$LockScreenCallback mLockScreenCallback;
    private View mTimeStatusView;
    protected TextView mUnlockTip;

    public SlideUnlockScreen(Context context) {
        this(context, null);
    }

    public SlideUnlockScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initLockViews();
    }

    private void initLockViews() {
        HwLog.i("SlideUnlockScreen", "SlideUnlockScreen initLockViews");
        this.mUnlockTip = (TextView) findViewById(R$id.locktip);
        this.mUnlockTip.setVisibility(0);
        this.mChargeStatus = (TextView) findViewById(R$id.chargingstatus);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        this.mUnlockTip.setText(R$string.slide_to_unlock);
        updateBatteryAndOwnerInfo();
        super.onConfigurationChanged(newConfig);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View root = getRootView();
        if (root instanceof ViewGroup) {
            View statusView = ((ViewGroup) root).findViewById(R$id.keyguard_status_view_face_palm);
            if (statusView != null) {
                this.mTimeStatusView = statusView;
            }
        }
        updateBatteryAndOwnerInfo();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        View cameracontainer = ((ViewGroup) getRootView()).findViewById(R$id.camera_container);
        if (cameracontainer != null) {
            LayoutParams pa = (LayoutParams) cameracontainer.getLayoutParams();
            pa.height = getResources().getDimensionPixelSize(R$dimen.camera_container_height);
            pa.width = getResources().getDimensionPixelSize(R$dimen.camera_container_width);
            cameracontainer.setLayoutParams(pa);
        }
    }

    public void setLockScreenCallback(HwUnlockInterface$LockScreenCallback callback) {
        this.mLockScreenCallback = callback;
    }

    public void onTimeChanged() {
    }

    public void onPhoneStateChanged() {
    }

    private void updateBatteryAndOwnerInfo() {
        if (this.mLockScreenCallback == null || this.mChargeStatus == null) {
            HwLog.w("SlideUnlockScreen", "updateBatteryAndOwnerInfo fail: " + this.mLockScreenCallback + "  " + this.mChargeStatus);
            return;
        }
        if (BatteryStateInfo.getInst().showBatteryInfo()) {
            HwLog.w("SlideUnlockScreen", "updateBatteryAndOwnerInfo showBatteryInfo: ");
            this.mChargeStatus.setText(BatteryStateInfo.getInst().getBatteryInfo(this.mContext));
            this.mChargeStatus.setVisibility(0);
        } else {
            HwLog.w("SlideUnlockScreen", "updateBatteryAndOwnerInfo hideBatteryInfo: ");
            this.mChargeStatus.setVisibility(4);
        }
    }

    public void onBatteryInfoChanged() {
        updateBatteryAndOwnerInfo();
    }

    public boolean needsInput() {
        return false;
    }

    public void onResume() {
        if (3 != MusicUtils.getMusicState() || KeyguardCfg.isExtremePowerSavingMode()) {
            if (this.mTimeStatusView != null) {
                this.mTimeStatusView.setVisibility(0);
            }
            if (this.mUnlockTip != null) {
                this.mUnlockTip.setVisibility(0);
            }
            HwKeyguardUpdateMonitor.getInstance().dispatchSetBackground(null);
        }
        updateBatteryAndOwnerInfo();
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
    }
}
