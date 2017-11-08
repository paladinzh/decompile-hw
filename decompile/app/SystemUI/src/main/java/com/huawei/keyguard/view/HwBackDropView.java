package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.android.keyguard.hwlockscreen.HwLockScreenPanel;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.charge.ChargingAnimController;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;
import com.huawei.keyguard.view.widget.MagazineSwitchViewPager;
import com.huawei.openalliance.ad.inter.constant.EventType;

public class HwBackDropView extends FrameLayout implements Callback {
    private static boolean mIsScreenOn = true;
    private WallpaperPagerAdapter mAdapter;
    private HwMagazineImageView mBackdropBack;
    private boolean mBlockAnimWhenInCover = false;
    private View mBtmMaskView = null;
    private HwLockScreenPanel mLockScreenPanel;
    private Runnable mMagazineImgGC = new Runnable() {
        public void run() {
            if (HwBackDropView.this.mBackdropBack != null) {
                HwBackDropView.this.mBackdropBack.resetCurrentWallPaper();
                HwBackDropView.this.mAdapter.clearViewData(null);
            }
        }
    };
    private View mMaskView = null;
    private Runnable mOnVisibilityChangedRunnable;
    private MagazineSwitchViewPager mPager;
    private KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        public void onStartedWakingUp() {
            if (HwBackDropView.this.mLockScreenPanel != null) {
                HwBackDropView.this.mLockScreenPanel.reset();
            }
        }

        public void onUserSwitching(int userId) {
            if (HwBackDropView.this.mBackdropBack != null) {
                HwBackDropView.this.mBackdropBack.setImageViewVisible();
                HwBackDropView.this.removeFyuseCallback();
                HwFyuseUtils.recordMagazineEnableStatus(HwFyuseUtils.isMagazineSwitchEnale(HwBackDropView.this.getContext()));
                HwBackDropView.this.mBackdropBack.forceLoadFyuseFile();
            }
        }
    };
    private HwKeyguardUpdateMonitor mUpdateMonitor;

    final /* synthetic */ class -void_reportAdEvent_boolean_isExit_LambdaImpl0 implements Runnable {
        private /* synthetic */ HwBackDropView val$this;

        public /* synthetic */ -void_reportAdEvent_boolean_isExit_LambdaImpl0(HwBackDropView hwBackDropView) {
            this.val$this = hwBackDropView;
        }

        public void run() {
            this.val$this.-com_huawei_keyguard_view_HwBackDropView_lambda$1();
        }
    }

    public HwBackDropView(Context context) {
        super(context);
    }

    public HwBackDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwBackDropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwBackDropView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mUpdateMonitor = HwKeyguardUpdateMonitor.getInstance(this.mContext);
        this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
        AppHandler.addListener(this);
        updateWallpaper();
        if (this.mUpdateMonitor.isShowing()) {
            setVisibility(0);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppHandler.removeListener(this);
        if (this.mUpdateMonitor != null) {
            this.mUpdateMonitor.removeCallback(this.mUpdateCallback);
        }
        HwFyuseUtils.clearFyuseViewMemory(getContext());
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        View v = getRootView().findViewWithTag("backdrop_pager");
        if (v instanceof MagazineSwitchViewPager) {
            this.mPager = (MagazineSwitchViewPager) v;
        }
        this.mMaskView = findViewById(R$id.wallpaper_mask);
        checkBottomMask();
        this.mAdapter = WallpaperPagerAdapter.getInst(this.mContext, this);
        if (this.mPager != null) {
            this.mPager.setAdapter(this.mAdapter);
            this.mPager.setCurrentItem(1073741823);
            this.mPager.setPageMargin((int) getContext().getResources().getDimension(R$dimen.magazine_pic_page_margin));
        }
        this.mAdapter.setBackdropView(1073741823);
        this.mLockScreenPanel = (HwLockScreenPanel) findViewWithTag("hw_lock_screen");
    }

    public ImageView getBackdropBack() {
        if (this.mBackdropBack == null) {
            return null;
        }
        return this.mBackdropBack.getImageView();
    }

    public void setBackdropBackImage(HwMagazineImageView backdropBack) {
        this.mBackdropBack = backdropBack;
        checkToEnableMotion();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView != this || this.mOnVisibilityChangedRunnable == null) {
            HwLog.w("HwBackDropView", "setDrawBehindAsSrc skipped. " + this.mOnVisibilityChangedRunnable);
            return;
        }
        HwLog.w("HwBackDropView", "setDrawBehindAsSrc " + visibility + ";" + this.mOnVisibilityChangedRunnable);
        this.mOnVisibilityChangedRunnable.run();
    }

    public void setOnVisibilityChangedRunnable(Runnable runnable) {
        this.mOnVisibilityChangedRunnable = runnable;
    }

    private void checkLockScreenVisibility() {
        int i = 0;
        if (this.mUpdateMonitor == null || HwKeyguardPolicy.getInst().isQsExpanded()) {
            HwLog.i("HwBackDropView", "skip restore animation param");
            return;
        }
        int i2;
        boolean showBackground = this.mUpdateMonitor.shouldShowing();
        boolean bouncer = this.mUpdateMonitor.isInBouncer();
        boolean isDoubleLockOn = (!showBackground || bouncer) ? false : KeyguardCfg.isDoubleLockOn(this.mContext);
        View view = this.mLockScreenPanel;
        if (isDoubleLockOn) {
            i2 = 0;
        } else {
            i2 = 8;
        }
        KgViewUtils.setViewVisibility(view, i2);
        KgViewUtils.restoreViewState(this.mLockScreenPanel);
        if (bouncer) {
            HwLog.i("HwBackDropView", "Not need restore state");
            if (this.mBackdropBack != null) {
                this.mBackdropBack.setFyuseAnimationParamInner();
            }
            setAnimationParamInner(1.0f, 0.0f, 0, 0);
        } else {
            HwLog.i("HwBackDropView", "restore param to 0");
            setAnimationParamInner(0.0f, 1.0f, 0, 0);
        }
        if (showBackground && this.mBackdropBack != null) {
            ImageView imageView = this.mBackdropBack.getImageView();
            if (imageView.getAlpha() < 0.999f) {
                imageView.setAlpha(1.0f);
            }
        }
        if (!showBackground) {
            i = 8;
        }
        setVisibility(i);
        HwLog.i("HwBackDropView", "checkVisibility : " + isDoubleLockOn + "; " + showBackground);
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                checkBottomMask();
                if (this.mBackdropBack != null) {
                    this.mBackdropBack.setImageViewVisible();
                    break;
                }
                break;
            case 2:
                if (this.mUpdateMonitor.shouldShowing() && !this.mUpdateMonitor.isInBouncer()) {
                    HwLog.d("HwBackDropView", "user changed");
                    checkLockScreenVisibility();
                    break;
                }
            case 10:
                if (!this.mUpdateMonitor.shouldShowing() || this.mUpdateMonitor.isInBouncer() || 0.001f >= getAnimationParam() || getAnimationParam() >= 0.999f) {
                    checkLockScreenVisibility();
                } else {
                    HwLog.w("HwBackDropView", "MSG_KEYGUARD_STATE_CHANGED as in animation" + getAnimationParam());
                }
                reportAdEvent(false);
                break;
            case 11:
                setVisibility(8);
                resetFyuseView();
                reportAdEvent(true);
                break;
            case 13:
                resetFyuseView();
                setIsScreenOn(false);
                break;
            case 14:
                removeFyuseCallback();
                if (this.mBackdropBack != null) {
                    this.mBackdropBack.forceToUpdateFyusePic();
                }
                setIsScreenOn(true);
                break;
            case 15:
                HwLog.w("HwBackDropView", "MSG_FINISHED_GOING_SLEEP isQsExpanded " + HwKeyguardPolicy.getInst().isQsExpanded());
                onScreenTurnedoff();
                break;
            case 16:
                resetFyuseView();
                break;
            case 21:
                updateWallpaper();
                break;
            case 30:
                this.mBlockAnimWhenInCover = true;
                break;
            case 31:
                this.mBlockAnimWhenInCover = false;
                checkLockScreenVisibility();
                if (this.mBackdropBack != null) {
                    this.mBackdropBack.forceToUpdateFyusePic();
                }
                PowerManager mPM = (PowerManager) this.mContext.getSystemService("power");
                if (!(mPM == null || mPM.isScreenOn())) {
                    mPM.wakeUp(SystemClock.uptimeMillis());
                    break;
                }
            case 100:
                handleBatteryChanged(Boolean.TRUE.equals(msg.obj));
                break;
        }
        return false;
    }

    private void resetFyuseView() {
        if (HwFyuseUtils.isSupport3DFyuse()) {
            removeFyuseCallback();
            this.mAdapter.disableAllFyuseMotion();
            GlobalContext.getUIHandler().postDelayed(this.mMagazineImgGC, 5000);
            HwFyuseUtils.clearFyuseViewMemory(getContext());
            if (this.mBackdropBack != null) {
                this.mBackdropBack.resetStatus();
            }
        }
    }

    private void removeFyuseCallback() {
        if (HwFyuseUtils.isSupport3DFyuse() && this.mBackdropBack != null) {
            this.mBackdropBack.resetStatus();
            HwFyuseUtils.removeCallbacks();
            GlobalContext.getUIHandler().removeCallbacks(this.mMagazineImgGC);
        }
    }

    private void reportAdEvent(boolean isExit) {
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
        boolean bouncer = monitor.isInBouncer();
        boolean showBackground = monitor.shouldShowing();
        if (isExit || (!isExit && bouncer && showBackground)) {
            GlobalContext.getBackgroundHandler().post(new -void_reportAdEvent_boolean_isExit_LambdaImpl0());
        }
    }

    /* synthetic */ void -com_huawei_keyguard_view_HwBackDropView_lambda$1() {
        HwLockScreenReporter.reportAdEvent(this.mContext, MagazineWallpaper.getInst(this.mContext).getPictureInfo(0), EventType.SHOWEND);
    }

    private void onScreenTurnedoff() {
        if (this.mUpdateMonitor.shouldShowing() && !this.mUpdateMonitor.isInBouncer()) {
            checkLockScreenVisibility();
        }
    }

    private void checkBottomMask() {
        if (this.mBtmMaskView == null) {
            this.mBtmMaskView = findViewById(R$id.mask_bottom);
        }
        if (this.mBtmMaskView != null) {
            int i;
            boolean showBTMMask = KeyguardTheme.getInst().getLockStyle() == 2;
            HwLog.w("HwBackDropView", "onThemeChanged. showBTMMask: " + showBTMMask);
            View view = this.mBtmMaskView;
            if (showBTMMask) {
                i = 0;
            } else {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    private void handleBatteryChanged(boolean pluginStateChanged) {
        if (this.mLockScreenPanel != null) {
            this.mLockScreenPanel.refreshBatteryInfo(null);
        }
        if (pluginStateChanged) {
            ChargingAnimController.getInst(this.mContext).onPluginStateChanged(getRootView());
        }
    }

    private BokehDrawable getBackgroudDrawable() {
        if (this.mBackdropBack == null) {
            return null;
        }
        Drawable groundPic = this.mBackdropBack.getImageView().getDrawable();
        return groundPic instanceof BokehDrawable ? (BokehDrawable) groundPic : null;
    }

    public float getAnimationParam() {
        BokehDrawable groundPic = getBackgroudDrawable();
        if (groundPic != null) {
            return groundPic.getBokehValue();
        }
        return 0.0f;
    }

    public void setAnimationParam(float param, float scale, int iPara100, int iPara255) {
        if (this.mUpdateMonitor == null || this.mUpdateMonitor.isInBouncer()) {
            HwLog.w("HwBackDropView", "BackdropView setAnimationParam Skip as in bouncer");
        } else {
            setAnimationParamInner(param, scale, iPara100, iPara255);
        }
    }

    public void setAnimationParamInner(float param, float scale, int iPara100, int iPara255) {
        if (this.mBackdropBack == null || this.mBlockAnimWhenInCover) {
            HwLog.v("HwBackDropView", "BackdropView Block or Skip Anim");
            return;
        }
        BokehDrawable groundPic = getBackgroudDrawable();
        if (!(this.mBackdropBack.setFyuseBokehValue(param, mIsScreenOn) || groundPic == null)) {
            groundPic.setBokehValue(param);
            groundPic.invalidateSelf();
        }
        setMaskViewAlpha(1.0f - param);
    }

    private void setMaskViewAlpha(float alpha) {
        boolean hasBmpMask = true;
        if (this.mMaskView != null) {
            int style = KeyguardTheme.getInst().getLockStyle();
            if (!(style == 7 || style == 8)) {
                hasBmpMask = false;
            }
            float maxAlpha = hasBmpMask ? 0.5f : 1.0f;
            if (alpha > maxAlpha) {
                alpha = maxAlpha;
            }
            this.mMaskView.setAlpha(alpha);
        }
    }

    private void updateWallpaper() {
        Drawable wallpaper = KeyguardWallpaper.getInst(this.mContext).getCurrentWallPaper();
        if (this.mBackdropBack == null || wallpaper == null) {
            HwLog.e("HwBackDropView", "update Wallpaper skiped as null pointer. " + wallpaper);
            return;
        }
        ImageView imageView = this.mBackdropBack.getImageView();
        if (wallpaper instanceof BokehDrawable) {
            this.mBackdropBack.handleUpdateWallpaper(mIsScreenOn);
            BokehDrawable oldPic = getBackgroudDrawable();
            if (oldPic != null) {
                float oldBokehValue = oldPic.getBokehValue();
                ((BokehDrawable) wallpaper).setBokehValue(oldBokehValue);
                if (oldBokehValue > 0.001f) {
                    this.mBackdropBack.setImageViewVisible();
                }
            }
            HwLog.i("HwBackDropView", "update Wallpaper with Bokeh.");
            imageView.setImageDrawable(wallpaper);
            return;
        }
        this.mBackdropBack.releaseFyuseData();
        imageView.setImageDrawable(wallpaper);
        HwLog.i("HwBackDropView", "update Wallpaper without bokeh");
    }

    public void proFyuseMotionEvent(MotionEvent event) {
        if (this.mBackdropBack == null) {
            HwLog.i("HwBackDropView", "procUnlockMotionEvent mBackdropBack is null");
        } else {
            this.mBackdropBack.proFyuseMotionEvent(event, mIsScreenOn);
        }
    }

    private void setIsScreenOn(boolean isScreenOn) {
        mIsScreenOn = isScreenOn;
        checkToEnableMotion();
    }

    private void checkToEnableMotion() {
        if (HwFyuseUtils.isSupport3DFyuse()) {
            if (mIsScreenOn && this.mBackdropBack != null && HwFyuseUtils.isCurrent3DWallpaper(this.mContext)) {
                HwFyuseView fyuseView = this.mBackdropBack.getFyuseView();
                if (fyuseView != null) {
                    fyuseView.enableMotion(mIsScreenOn);
                }
                this.mAdapter.disableFyuseMotion(this.mBackdropBack);
            } else {
                HwLog.w("HwBackDropView", "checkToEnableMotion disableAllFyuseMotion : ");
                this.mAdapter.disableAllFyuseMotion();
            }
        }
    }
}
