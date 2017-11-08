package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import com.android.keyguard.hwlockscreen.HwKeyguardBottomArea;
import com.android.keyguard.hwlockscreen.HwLockScreenPanel;
import com.android.keyguard.hwlockscreen.HwLockScreenPanel.IUnlocMotionDetector;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.HwPanelView.IPanelDragListener;
import com.android.systemui.statusbar.phone.HwStatusBarKeyguardViewManager.IAnimationListener;
import com.android.systemui.utils.HwLog;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.DoubleTapUtils;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.view.CameraLayout;
import com.huawei.keyguard.view.CurveScreenAnimView;
import com.huawei.keyguard.view.HwBackDropView;
import com.huawei.keyguard.view.KgViewUtils;
import com.huawei.keyguard.view.charge.ChargingAnimController;
import com.huawei.keyguard.view.effect.AnimUtils.SimpleAnimListener;
import fyusion.vislib.BuildConfig;

public class HwKeyguardDragHelper implements IUnlocMotionDetector, IPanelDragListener {
    public static final boolean sSupportAnyDirectionUnlock = HwKeyguardPolicy.isSupportAnyDirectionUnlock();
    private KeyguardAnimationControl mAnimationControl = new KeyguardAnimationControl();
    private HwBackDropView mBackDropView = null;
    private StatusBarWindowView mBarView;
    private boolean mBlockAnimation = false;
    private int mBlockType = 0;
    private CameraLayout mCameraView;
    private Context mContext;
    private CurveScreenAnimView mCureView = null;
    private boolean mEnableCurveAnimation;
    private GestureDetector mGestureDetector = null;
    protected HwKeyguardBottomArea mHwKeyguardBottom = null;
    private HwLockScreenPanel mHwLockscreenPanel;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private IAnimationListener mKeyguardAnimCallack = new IAnimationListener() {
        public void onPreHideAnimation() {
            HwKeyguardDragHelper.this.mAnimationControl.animateToExitBouncer();
            if (HwKeyguardDragHelper.this.mBlockType != 0) {
                HwLog.w("HwKeyguardDragHelper", "KeyguardAnimCallack onPreHideAnimation and motion unfinished: " + HwKeyguardDragHelper.this.mBlockType);
                HwKeyguardDragHelper.this.mBlockType = 0;
                return;
            }
            HwLog.w("HwKeyguardDragHelper", "KeyguardAnimCallack onPreHideAnimation");
        }

        public void onRevertToKeyguard(Runnable finishRunner) {
            HwLog.w("HwKeyguardDragHelper", "KeyguardAnimCallack onRevertToKeyguard.");
            HwKeyguardDragHelper.this.mBarView.mNotificationPanel.setAnimStartState(0, 0.8f, 0.0f);
            HwKeyguardDragHelper.this.mHwKeyguardBottom.setAnimStartState(0, 0.8f, 0.0f);
            HwKeyguardDragHelper.this.mHwLockscreenPanel.setAnimStartState(0, 0.8f, 0.0f);
            HwKeyguardDragHelper.this.mAnimationControl.setAnimationParam(1.0f);
            if (finishRunner != null) {
                finishRunner.run();
            }
            HwKeyguardDragHelper.this.mAnimationControl.animateToRestoreKeyguardFromBouncer(null);
        }
    };
    private KeyguardStatusBarView mKeyguardStatusBar;
    private HwStatusBarKeyguardViewManager mKeyguardViewManaer;
    private long mLastActiveTime = 0;
    private boolean mMotionAborted = false;
    private HwPhoneStatusBar mService;
    private long mStartUnlockInputTime = 0;
    private float mUnlockSlideDistance;

    private class KeyguardAnimationControl {
        private Animator mCurrentAnimator;
        private float mLastAnimParam;
        private int mLastPara255;

        private KeyguardAnimationControl() {
            this.mCurrentAnimator = null;
        }

        public boolean isAnimationRunning() {
            return this.mCurrentAnimator != null ? this.mCurrentAnimator.isStarted() : false;
        }

        public void setAnimationParam(float param) {
            if (param < 0.001f) {
                param = 0.0f;
            }
            int iPara100 = (int) (100.0f * param);
            int iPara255 = (int) (255.0f * param);
            if (this.mLastPara255 != iPara255) {
                this.mLastAnimParam = param;
                this.mLastPara255 = iPara255;
                updateAnimationViews(param, 1.0f - (0.15f * param), iPara100, iPara255);
            }
        }

        private void updateAnimationViews(float param, float scale, int iPara100, int iPara255) {
            HwLog.v("HwKeyguardDragHelper", "updateAnimationViews with param: " + param);
            HwKeyguardDragHelper.this.mBarView.mNotificationPanel.setAnimationParam(param, scale, iPara100, iPara255);
            if (HwKeyguardDragHelper.this.mBackDropView != null) {
                HwKeyguardDragHelper.this.mBackDropView.setAnimationParam(param, scale, iPara100, iPara255);
            }
            if (HwKeyguardDragHelper.this.mHwKeyguardBottom != null) {
                HwKeyguardDragHelper.this.mHwKeyguardBottom.setAnimationParam(param, scale, iPara100, iPara255);
            }
            if (HwKeyguardDragHelper.this.mHwLockscreenPanel != null) {
                HwKeyguardDragHelper.this.mHwLockscreenPanel.setAnimationParam(param, scale, iPara100, iPara255);
            }
        }

        private void animateToBouncer() {
            HwLog.w("HwKeyguardDragHelper", "KeyguardDrag animatToExitKeyguard");
            if (this.mLastAnimParam < 0.9f) {
                ObjectAnimator objAnimator = ObjectAnimator.ofFloat(this, "AnimationParam", new float[]{this.mLastAnimParam, 1.0f});
                objAnimator.setDuration(HwKeyguardDragHelper.this.mEnableCurveAnimation ? 250 : 200);
                objAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        HwLog.w("HwKeyguardDragHelper", "HwPanelView onAnimationEnd animateToExitKeyguard");
                        KeyguardAnimationControl.this.mCurrentAnimator = null;
                        KeyguardAnimationControl.this.afterKeyguardExit();
                    }
                });
                objAnimator.start();
                this.mCurrentAnimator = objAnimator;
            } else if (this.mLastAnimParam <= 1.0f) {
                afterKeyguardExit();
            }
        }

        private void afterKeyguardExit() {
            updateAnimationViews(1.0f, 0.85f, 100, 255);
            HwKeyguardDragHelper.this.mBarView.mNotificationPanel.afterKeyguardExit();
            HwKeyguardDragHelper.this.mHwLockscreenPanel.setVisibility(8);
            if (HwKeyguardDragHelper.this.mCureView != null) {
                HwKeyguardDragHelper.this.mCureView.setVisibility(8);
            }
            KgViewUtils.restoreViewState(HwKeyguardDragHelper.this.mHwLockscreenPanel);
        }

        private void animateToRestoreKeyguardFromBouncer(final Runnable finishRunner) {
            HwLog.w("HwKeyguardDragHelper", "KeyguardDrag animateToRestoreKeyguard");
            ObjectAnimator objAnimator = ObjectAnimator.ofFloat(this, "AnimationParam", new float[]{this.mLastAnimParam, 0.0f});
            objAnimator.setDuration(300);
            objAnimator.addListener(new SimpleAnimListener() {
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    boolean isExit = (HwKeyguardDragHelper.this.mService.getFpUnlockingStatus() || !HwKeyguardDragHelper.this.mKeyguardViewManaer.isShowing()) ? true : HwKeyguardDragHelper.this.mKeyguardViewManaer.isOccluded();
                    if (isExit) {
                        HwLog.w("HwKeyguardDragHelper", "HwPanelView animateToRestoreKeyguard interupted.");
                        KgViewUtils.restoreViewState(HwKeyguardDragHelper.this.mHwLockscreenPanel);
                    } else {
                        HwKeyguardDragHelper.this.mBarView.mNotificationPanel.setVisibility(0);
                    }
                    KeyguardAnimationControl.this.updateAnimationViews(0.0f, 1.0f, 0, 0);
                    HwKeyguardDragHelper.this.mBarView.mNotificationPanel.restoreDrawState();
                    if (finishRunner != null) {
                        finishRunner.run();
                    }
                    KeyguardAnimationControl.this.mCurrentAnimator = null;
                }
            });
            objAnimator.start();
            this.mCurrentAnimator = objAnimator;
        }

        private void animateToExitBouncer() {
            HwLog.w("HwKeyguardDragHelper", "KeyguardDrag animateToExitBouncer");
            HwKeyguardDragHelper.this.mBackDropView.setAlpha(1.0f);
            HwKeyguardDragHelper.this.mBackDropView.clearAnimation();
        }
    }

    private class SimpleGestureListener extends SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onDoubleTap(MotionEvent event) {
            if (!DoubleTapUtils.readWakeupCheckValue(HwKeyguardDragHelper.this.mContext)) {
                return super.onDoubleTap(event);
            }
            DoubleTapUtils.offScreen(HwKeyguardDragHelper.this.mContext);
            HwLockScreenReporter.report(HwKeyguardDragHelper.this.mContext, 155, BuildConfig.FLAVOR);
            return super.onDoubleTap(event);
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (HwKeyguardDragHelper.this.mMotionAborted || e1 == null || e2 == null) {
                Log.d("HwKeyguardDragHelper", "onFling skipped " + HwKeyguardDragHelper.this.mMotionAborted);
                return false;
            }
            int xMv = Math.abs((int) (e2.getX() - e1.getX()));
            int yMv = Math.abs((int) (e2.getY() - e1.getY()));
            float v = (float) Math.sqrt((double) ((velocityX * velocityX) + (velocityY * velocityY)));
            int distance = (int) Math.sqrt((double) ((xMv * xMv) + (yMv * yMv)));
            if (v < 2500.0f || distance < 300) {
                return false;
            }
            Log.d("HwKeyguardDragHelper", "onFling distance=" + distance + ", velocity=" + v);
            HwKeyguardDragHelper.this.mAnimationControl.animateToBouncer();
            HwKeyguardDragHelper.this.reportUnlockInfo(true);
            return true;
        }
    }

    public void init(StatusBarWindowView barView, PhoneStatusBar service) {
        this.mBarView = barView;
        if (service instanceof HwPhoneStatusBar) {
            this.mService = (HwPhoneStatusBar) service;
        } else {
            HwLog.w("HwKeyguardDragHelper", "init. HwPhoneStatusBar error");
        }
        this.mContext = this.mBarView.getContext().getApplicationContext();
        this.mEnableCurveAnimation = KeyguardCfg.isCurveScreen(this.mContext);
        this.mBackDropView = (HwBackDropView) barView.findViewById(R.id.backdrop);
        this.mHwLockscreenPanel = (HwLockScreenPanel) barView.findViewById(R.id.keyguard_lock_screen_panel);
        this.mHwLockscreenPanel.setUnlocMontionDetector(this);
        this.mCameraView = (CameraLayout) barView.findViewById(R.id.camera_container);
        this.mCameraView.addTransalteView(this.mBarView.mNotificationPanel);
        this.mCameraView.addTransalteView(this.mHwLockscreenPanel);
        this.mHwKeyguardBottom = (HwKeyguardBottomArea) barView.findViewById(R.id.hw_keyguard_bottom_area);
        this.mCureView = (CurveScreenAnimView) barView.findViewById(R.id.curve_touch_effect_view);
        this.mKeyguardStatusBar = (KeyguardStatusBarView) barView.findViewById(R.id.keyguard_header);
        barView.mNotificationPanel.setKeyguardStatusBar(this.mKeyguardStatusBar, this);
        this.mGestureDetector = new GestureDetector(this.mBarView.getContext(), new SimpleGestureListener());
        if (!(this.mCureView == null || this.mKeyguardStatusBar == null)) {
            boolean show = this.mEnableCurveAnimation && this.mKeyguardStatusBar.getVisibility() == 0;
            this.mCureView.setVisibility(show ? 0 : 8);
        }
        this.mKeyguardViewManaer = this.mService.getStatusBarKeyguardViewManager();
        this.mKeyguardViewManaer.setAnimationListener(this.mKeyguardAnimCallack);
        initUnlockSlideDistance();
    }

    private void updateUserActivity(MotionEvent ev) {
        long curretTime = SystemClock.uptimeMillis();
        if (this.mLastActiveTime + 3000 < curretTime) {
            this.mService.userActivity();
            this.mLastActiveTime = curretTime;
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        Log.w("HwKeyguardDragHelper", "  onTouchEvent " + ev.getAction() + "   " + this.mBlockType);
        updateUserActivity(ev);
        switch (this.mBlockType) {
            case 1:
                return this.mCameraView.onDragEvent(ev);
            case 2:
                if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                    return false;
                }
                return this.mHwKeyguardBottom.dispatchTouchEvent(ev);
            case 3:
                return this.mHwLockscreenPanel.dispatchTouchEvent(ev);
            case 4:
                return procUnlockMotionEvent(ev);
            case 5:
                this.mService.getCallingLayout().onTouchEvent(ev);
                break;
        }
        return false;
    }

    public boolean isTouchBlocked() {
        return this.mBlockType != 0;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = true;
        if (this.mKeyguardStatusBar == null) {
            return false;
        }
        if (ev.getActionMasked() != 0) {
            if (this.mBlockType == 0) {
                z = false;
            }
            return z;
        } else if (ChargingAnimController.getInst(this.mContext).isChargingViewVisible() && BatteryStateInfo.getInst().isCharge()) {
            this.mBlockType = 6;
            return true;
        } else {
            boolean isCallBarShowing;
            this.mBlockType = 0;
            BackgrounCallingLayout callLayout = this.mService.getCallingLayout();
            if (callLayout != null) {
                isCallBarShowing = callLayout.isIsCalllinearlayouShowing();
            } else {
                isCallBarShowing = false;
            }
            if (isCallBarShowing && callLayout.isInterestEvent(ev) && (this.mService.isBouncerShowing() || this.mService.isFullscreenBouncer())) {
                this.mBlockType = 5;
                Log.w("HwKeyguardDragHelper", "onInterceptTouchEvent: " + this.mBlockType);
                return true;
            }
            boolean canBlocked = (this.mService.getBarState() == 1 && this.mBarView.mNotificationPanel.isUnlockAvaile()) ? !this.mService.isBouncerShowing() : false;
            if (((float) (this.mKeyguardStatusBar.getHeight() + 10)) > ev.getY() || this.mKeyguardViewManaer.isOccluded() || !(isCallBarShowing || this.mKeyguardStatusBar.getVisibility() == 0)) {
                canBlocked = false;
            }
            if (canBlocked && this.mBarView.mDragDownHelper.captureStartingChildChecked(ev.getX(), ev.getY()) != null) {
                Log.w("HwKeyguardDragHelper", "onInterceptTouchEvent skip as touch in View ");
                canBlocked = false;
            }
            while (canBlocked) {
                if (!this.mAnimationControl.isAnimationRunning()) {
                    if (!this.mCameraView.isInterestEvent(ev)) {
                        if (!this.mHwKeyguardBottom.isInterestedEvent(ev)) {
                            if (!this.mHwLockscreenPanel.isInterestMotionEvent(ev)) {
                                if (sSupportAnyDirectionUnlock) {
                                    this.mBlockType = 4;
                                    this.mMotionAborted = false;
                                    break;
                                }
                            }
                            this.mBlockType = 3;
                            break;
                        }
                        this.mBlockType = 2;
                        break;
                    }
                    this.mBlockType = 1;
                    break;
                }
                this.mBlockType = 7;
                break;
            }
            this.mBarView.mNotificationPanel.initDownStates(ev);
            Log.w("HwKeyguardDragHelper", "HwKyguardDragHelper onInterceptTouchEvent: " + this.mBlockType);
            if (this.mBlockType == 0) {
                z = false;
            }
            return z;
        }
    }

    public boolean procUnlockMotionEvent(MotionEvent event) {
        boolean z = false;
        int action = event.getAction();
        int fpIdx = (65280 & action) >> 8;
        action &= 255;
        if (fpIdx > 0) {
            if (!this.mMotionAborted) {
                z = true;
            }
            return z;
        }
        int currentX = (int) event.getX();
        int currentY = (int) event.getY();
        if (this.mEnableCurveAnimation && this.mCureView != null) {
            this.mCureView.procTouchEvent(event);
        }
        if (this.mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        if (!this.mEnableCurveAnimation) {
            this.mBackDropView.proFyuseMotionEvent(event);
        }
        switch (action) {
            case 0:
                this.mMotionAborted = false;
                this.mInitialTouchX = (float) currentX;
                this.mInitialTouchY = (float) currentY;
                this.mStartUnlockInputTime = SystemClock.uptimeMillis();
                break;
            case 1:
            case 3:
            case 6:
                HwLog.w("HwKeyguardDragHelper", "ACTION_UP." + this.mKeyguardViewManaer.isOccluded());
                this.mMotionAborted = action == 3;
                if (this.mKeyguardViewManaer.isOccluded() || this.mKeyguardViewManaer.isBouncerShowing() || !this.mKeyguardViewManaer.isShowing()) {
                    HwLog.w("HwKeyguardDragHelper", "Disturbed by finger or Occluded.");
                    if (!this.mEnableCurveAnimation) {
                        KgViewUtils.restoreViewState(this.mHwLockscreenPanel);
                    }
                } else if (!this.mMotionAborted && ((float) getMotionDistance(currentX, currentY)) > this.mUnlockSlideDistance) {
                    this.mAnimationControl.animateToBouncer();
                    reportUnlockInfo(true);
                } else if (!this.mEnableCurveAnimation) {
                    this.mAnimationControl.animateToRestoreKeyguardFromBouncer(null);
                    reportUnlockInfo(false);
                }
                this.mBlockType = 0;
                break;
            case 2:
                if (!this.mEnableCurveAnimation) {
                    this.mAnimationControl.setAnimationParam(getMotionParam(currentX, currentY));
                    break;
                }
                break;
            case 5:
                this.mMotionAborted = true;
                break;
        }
        if (!this.mMotionAborted) {
            z = true;
        }
        return z;
    }

    private void initUnlockSlideDistance() {
        if (HwUnlockUtils.isTablet()) {
            float widthPx;
            int widthPixels = this.mContext.getResources().getDisplayMetrics().widthPixels;
            float xdpi = HwUnlockUtils.getRealXdpi(this.mContext);
            if (2.3f * xdpi > ((float) widthPixels) || xdpi == -1.0f) {
                widthPx = (float) widthPixels;
            } else {
                widthPx = 2.3f * xdpi;
            }
            this.mUnlockSlideDistance = Math.abs(widthPx / 2.0f);
            if (this.mUnlockSlideDistance > 500.0f) {
                this.mUnlockSlideDistance = 500.0f;
            }
            return;
        }
        this.mUnlockSlideDistance = 500.0f;
    }

    private float getMotionParam(int curX, int curY) {
        float animParam = ((float) getMotionDistance(curX, curY)) / 800.0f;
        if (animParam > 1.0f) {
            return 1.0f;
        }
        return animParam < 0.0f ? 0.0f : animParam;
    }

    private int getMotionDistance(int curX, int curY) {
        int deltaX = (int) (((float) curX) - this.mInitialTouchX);
        int deltaY = (int) (((float) curY) - this.mInitialTouchY);
        return (int) Math.sqrt((double) ((deltaX * deltaX) + (deltaY * deltaY)));
    }

    private void reportUnlockInfo(boolean verifyResult) {
        if (verifyResult) {
            HwLog.d("HwKeyguardDragHelper", "reportUnlockInfo verifyResult is true");
            HwUnlockUtils.vibrate(this.mContext);
        }
        if (this.mStartUnlockInputTime > 0) {
            long unlockUsedTime = SystemClock.uptimeMillis() - this.mStartUnlockInputTime;
            this.mStartUnlockInputTime = 0;
            int type = KeyguardTheme.getInst().getLockStyle();
            String lock_type = type == 7 ? "MusicUnlock" : type == 1 ? "SlideUnlock" : type == 2 ? "MagazineUnlock" : "Unlock";
            HwLockScreenReporter.report(this.mContext, 153, "{" + lock_type + ":" + verifyResult + ",CostTime:" + unlockUsedTime + "}");
        }
    }

    public void onDrag(float fraction) {
        if (!this.mBlockAnimation) {
            if (((this.mBackDropView != null && !this.mKeyguardViewManaer.isBouncerShowing() && !this.mKeyguardViewManaer.willShowBouncer()) || this.mHwKeyguardBottom.getVisibility() != 0) && this.mBlockType != 4) {
                float scale = 1.0f - (0.15f * fraction);
                if (this.mBackDropView != null) {
                    this.mBackDropView.setAnimationParam(fraction, scale, 0, 0);
                }
                if (this.mHwLockscreenPanel != null) {
                    this.mHwLockscreenPanel.setAnimationParam(fraction, scale, 0, 0);
                }
            }
        }
    }

    public void blockAnimation(boolean block) {
        if (block) {
            HwLog.w("HwKeyguardDragHelper", "AnimationBlocked");
        }
        this.mBlockAnimation = block;
    }

    public void onProcFyuseMotionEvent(MotionEvent event) {
        if (this.mBackDropView != null) {
            this.mBackDropView.proFyuseMotionEvent(event);
        }
    }
}
