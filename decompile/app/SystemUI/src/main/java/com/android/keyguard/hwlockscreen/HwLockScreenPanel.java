package com.android.keyguard.hwlockscreen;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor.BatteryStatus;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.dynamiclockscreen.DynamicUnlockScreen;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.events.MusicMonitor;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.RadarUtil;
import com.huawei.keyguard.theme.HwThemeParser;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.DisabledFeatureUtils;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.util.XmlUtils;
import com.huawei.keyguard.view.effect.LensFlareRenderer;
import com.huawei.keyguard.view.widget.LensFlareView;
import java.io.File;
import org.w3c.dom.Document;

public class HwLockScreenPanel extends FrameLayout implements KeyguardSecurityView, Callback {
    public static final Object sLock = new Object();
    private int RESUME_REASON_LATE_RESUME;
    private BroadcastReceiver mGLSurfaceViewReceiver;
    private Handler mHandler;
    boolean mIsIntersetEvent;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardSecurityCallback mKeyguardsecuritycallback;
    private LensFlareView mLensFlareView;
    private LockPatternUtils mLockPatternUtils;
    private HwUnlockInterface$HwLockScreenReal mLockScreen;
    private LockScreenCallbackImpl mLockScreenCallbackImpl;
    private boolean mLockScreenResumeSuspended;
    private View mLockScreenView;
    private int mLockType;
    private int mSecurityHeight;
    private boolean mStyleLensFlareEnable;
    private IUnlocMotionDetector mUnlocMotionDetector;
    private boolean mUnlockBlock;
    private int orientation;

    public interface IUnlocMotionDetector {
        boolean procUnlockMotionEvent(MotionEvent motionEvent);
    }

    public void setUnlocMontionDetector(IUnlocMotionDetector unlocMotionDetector) {
        this.mUnlocMotionDetector = unlocMotionDetector;
    }

    public HwLockScreenPanel(Context context) throws Exception {
        this(context, null);
    }

    public HwLockScreenPanel(Context context, AttributeSet attrs) throws Exception {
        this(context, attrs, 0);
    }

    @SuppressLint({"NewApi"})
    public HwLockScreenPanel(Context context, AttributeSet attrs, int defStyle) throws Exception {
        super(context, attrs, 0);
        this.mLockScreen = null;
        this.mUnlockBlock = false;
        this.mLockType = 0;
        this.RESUME_REASON_LATE_RESUME = -1;
        this.mLockScreenResumeSuspended = false;
        this.mStyleLensFlareEnable = true;
        this.mKeyguardUpdateMonitor = null;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 102:
                        HwLog.i("HwLockScreenPanel", "MSG_REMOVE_VIEW removeLensFlareView");
                        HwLockScreenPanel.this.removeLensFlareView();
                        return;
                    case 103:
                        HwLockScreenPanel.this.makeAmazingView(msg.obj);
                        if (HwLockScreenPanel.this.mLockScreenResumeSuspended && HwLockScreenPanel.this.mLockScreen != null) {
                            HwLockScreenPanel.this.onResume(HwLockScreenPanel.this.RESUME_REASON_LATE_RESUME);
                            return;
                        }
                        return;
                    case 104:
                        DisabledFeatureUtils.refreshCameraDisabled(HwLockScreenPanel.this.getContext());
                        return;
                    default:
                        return;
                }
            }
        };
        this.mGLSurfaceViewReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    HwLog.w("HwLockScreenPanel", "onReceive, the intent is null!");
                } else {
                    HwLockScreenPanel.this.mHandler.obtainMessage(102).sendToTarget();
                }
            }
        };
        this.mIsIntersetEvent = false;
        HwLog.i("HwLockScreenPanel", "HwLockScreenImpl construct");
        try {
            this.mSecurityHeight = (int) getResources().getDimension(R$dimen.security_height);
        } catch (NotFoundException e) {
            Configuration config = getResources().getConfiguration();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            HwLog.i("HwLockScreenPanel", "config: densityDpi=" + config.densityDpi + " screenHeightDp=" + config.screenHeightDp + " screenWidthDp=" + config.screenWidthDp);
            HwLog.i("HwLockScreenPanel", "metrics: density/densityDpi/scaledDensity=" + metrics.density + "/" + metrics.densityDpi + "/" + metrics.scaledDensity + " screenHeightDp=" + metrics.heightPixels + " screenWidthDp=" + metrics.widthPixels);
        }
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(getContext());
        this.mLockPatternUtils = new LockPatternUtils(getContext());
        this.mLockScreenCallbackImpl = new LockScreenCallbackImpl(getContext(), this);
        this.mLockScreenCallbackImpl.setLockPatternUtils(this.mLockPatternUtils);
    }

    private void dispatchSetTransparent(boolean transparent) {
        HwKeyguardUpdateMonitor base = HwKeyguardUpdateMonitor.getInstance();
        if (base != null) {
            base.dispatchSetTransparent(transparent);
        }
    }

    private void createLockScreenView(Context context) {
        if (this.mLockScreen != null) {
            removeAllViews();
        }
        boolean ret = true;
        removeAllViews();
        switch (this.mLockType) {
            case 1:
            case 5:
                this.mStyleLensFlareEnable = false;
                dispatchSetTransparent(true);
                loadSlide();
                break;
            case 2:
                loadMagazine();
                break;
            case 3:
                loadDynamic();
                break;
            case 4:
                loadAmazing();
                break;
            case 6:
                loadSimple();
                break;
            case 7:
                loadMusic();
                break;
            case 8:
                loadSport();
                break;
            default:
                ret = false;
                break;
        }
        this.mHandler.obtainMessage(104).sendToTarget();
        String style = HwThemeParser.getInstance().getStyle();
        if (ret) {
            HwLockScreenReporter.report(this.mContext, 1, this.mLockPatternUtils.isSecure(OsUtils.getCurrentUser()) ? "{lock_type: password}" : "{lock_type: " + style + "}");
        } else {
            RadarUtil.uploadUploadLockscreenFail(context, "LockScreen style: " + style);
        }
    }

    private void loadAmazing() {
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                String layout = HwThemeParser.getInstance().getLayout(HwLockScreenPanel.this.getContext());
                String layoutPath = "/data/skin/unlock/layout-hdpi/" + layout + ".xml";
                if (HwUnlockUtils.isLandscape(HwLockScreenPanel.this.mContext)) {
                    layoutPath = "/data/skin/unlock/layout-hdpi/" + layout + "_land.xml";
                    if (!new File(layoutPath).exists()) {
                        layoutPath = "/data/skin/unlock/layout-hdpi/" + layout + ".xml";
                    }
                }
                HwLockScreenPanel.this.mHandler.obtainMessage(103, XmlUtils.getXMLDocument(layoutPath)).sendToTarget();
            }
        });
    }

    private void makeAmazingView(Object obj) {
        if (obj instanceof Document) {
            WakeLock wakelock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "View Lock");
            wakelock.acquire();
            try {
                loadAmazing((Document) obj);
                return;
            } catch (Exception ex) {
                HwLog.w("HwLockScreenPanel", "Load Amazing failed and got err:", ex);
            } finally {
                wakelock.release();
            }
        }
        this.mLockType = 1;
        loadSlide();
    }

    private void loadAmazing(Document document) {
        this.mLockScreenView = HwViewLoader.createView(this.mContext, document);
        this.mLockScreen = (HwUnlockInterface$HwLockScreenReal) this.mLockScreenView;
        if (this.mLockScreenCallbackImpl != null) {
            this.mLockScreenCallbackImpl.setLockScreen(this.mLockScreen);
        }
        this.mLockScreen.setLockScreenCallback(this.mLockScreenCallbackImpl);
        addView(this.mLockScreenView, -1, -1);
        this.mLockScreen.setLockPatternUtils(this.mLockPatternUtils);
    }

    public void onResume(int reason) {
        HwLog.i("HwLockScreenPanel", "onResume: " + reason);
        if (reason == 1) {
        }
        if (this.mLockScreen == null) {
            HwLog.i("HwLockScreenPanel", "mLockScreen is null, resume suspended!");
            this.mLockScreenResumeSuspended = true;
        } else if (FpUtils.isInFastScreenOn()) {
            HwLog.i("HwLockScreenPanel", "LockScreen isInFastScreenOn, so return!");
        } else {
            refreshLockScreenInfo();
            this.mLockScreen.onResume();
        }
    }

    public void onPause() {
        HwLog.i("HwLockScreenPanel", "onpause");
    }

    public boolean isInterestMotionEvent(MotionEvent event) {
        boolean z = false;
        if (getVisibility() == 0 && this.mLockScreenView != null && this.mLockScreenView.getVisibility() == 0) {
            z = KeyguardTheme.isFullTouchTheme(this.mLockType);
        }
        this.mIsIntersetEvent = z;
        HwLog.w("HwLockScreenPanel", "isInterestMotionEvent " + this.mIsIntersetEvent + "  " + this.mLockType);
        return this.mIsIntersetEvent;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return super.onInterceptTouchEvent(event);
    }

    protected void onFinishInflate() {
        this.orientation = this.mContext.getResources().getConfiguration().orientation;
        super.onFinishInflate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mKeyguardUpdateMonitor.registerCallback(this.mLockScreenCallbackImpl);
        getContext().registerReceiver(this.mGLSurfaceViewReceiver, new IntentFilter("Remove_GLSurfaceview"));
        HwLog.i("HwLockScreenPanel", "HwLockScreenPanel mUpdateCallback");
        AppHandler.addListener(this);
        reset();
        setVisibility(8);
    }

    protected void onDetachedFromWindow() {
        HwLog.d("HwLockScreenPanel", "onDetachedFromWindow");
        removeLensFlareView();
        LayoutParams lp = getLayoutParams();
        if (lp != null) {
            lp.height = this.mSecurityHeight;
            requestLayout();
        }
        this.mKeyguardUpdateMonitor.removeCallback(this.mLockScreenCallbackImpl);
        if (this.mGLSurfaceViewReceiver != null) {
            getContext().unregisterReceiver(this.mGLSurfaceViewReceiver);
            this.mGLSurfaceViewReceiver = null;
        }
        this.mHandler.removeMessages(103);
        this.mLockScreen = null;
        this.mLensFlareView = null;
        this.mLockScreenView = null;
        super.onDetachedFromWindow();
        AppHandler.removeListener(this);
    }

    public boolean needsInput() {
        if (this.mLockScreen != null) {
            return this.mLockScreen.needsInput();
        }
        return false;
    }

    public void onTrigger(Intent intent, Animation anim) {
        LensFlareRenderer.setbFirstBoot(false);
        if (this.mKeyguardsecuritycallback != null) {
            HwUnlockUtils.vibrate(this.mContext);
            HwLog.i("HwLockScreenPanel", "onTrigger startActivity intent=" + intent);
            if (!(intent == null || HwUnlockUtils.isUnlockIntent(intent))) {
                try {
                    this.mContext.startActivityAsUser(intent, new UserHandle(-2));
                } catch (ActivityNotFoundException ex) {
                    HwLog.w("HwLockScreenPanel", "start activity fail, just dismiss keyguard " + ex);
                }
            }
            this.mKeyguardsecuritycallback.dismiss(false);
        }
    }

    public void setClickKey(int value) {
    }

    private void refreshLockScreenInfo() {
        this.mLockScreenCallbackImpl.refreshLockScreenInfo();
    }

    private void removeLensFlareView() {
        ViewGroup vg = (ViewGroup) getRootView();
        if (vg != null) {
            if (this.mLensFlareView != null) {
                HwLog.i("HwLockScreenPanel", "remove lensFlareView  = " + this.mLensFlareView);
                vg.removeView(this.mLensFlareView);
            } else {
                for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                    if (vg.getChildAt(i) instanceof LensFlareView) {
                        HwLog.i("HwLockScreenPanel", "remove lensFlareView idx = " + i);
                        vg.removeViewAt(i);
                    }
                }
            }
            this.mLensFlareView = null;
        }
    }

    private void loadSlide() {
        HwLog.i("HwLockScreenPanel", "loadSlide");
        this.mLockScreenView = View.inflate(getContext(), R$layout.slide_unlock, null);
        this.mLockScreen = (HwUnlockInterface$HwLockScreenReal) this.mLockScreenView;
        if (this.mLockScreenCallbackImpl != null) {
            this.mLockScreenCallbackImpl.setLockScreen(this.mLockScreen);
        }
        this.mLockScreen.setLockScreenCallback(this.mLockScreenCallbackImpl);
        this.mLockScreen.setLockPatternUtils(this.mLockPatternUtils);
        addView(this.mLockScreenView, -1, -1);
    }

    public void loadMagazine() {
        HwLog.i("HwLockScreenPanel", "loadMagazine");
        this.mStyleLensFlareEnable = false;
    }

    private void loadMusic() {
        HwLog.i("HwLockScreenPanel", "loadMusic");
        removeAllViews();
        this.mLockScreenView = View.inflate(getContext(), R$layout.lockscreen_music_view, null);
        addView(this.mLockScreenView, -1, -1);
    }

    private void loadSport() {
        HwLog.i("HwLockScreenPanel", "loadSport");
        removeAllViews();
        this.mLockScreenView = View.inflate(getContext(), R$layout.lockscreen_sport_view, null);
        addView(this.mLockScreenView, -1, -1);
    }

    private void loadSimple() {
        HwLog.i("HwLockScreenPanel", "loadSimple");
        this.mStyleLensFlareEnable = false;
        this.mLockScreenView = View.inflate(getContext(), R$layout.slide_unlock, null);
        this.mLockScreen = (HwUnlockInterface$HwLockScreenReal) this.mLockScreenView;
        if (this.mLockScreenCallbackImpl != null) {
            this.mLockScreenCallbackImpl.setLockScreen(this.mLockScreen);
        }
        this.mLockScreen.setLockScreenCallback(this.mLockScreenCallbackImpl);
        this.mLockScreen.setLockPatternUtils(this.mLockPatternUtils);
        addView(this.mLockScreenView, -1, -1);
    }

    private void loadDynamic() {
        HwThemeParser themeParser = HwThemeParser.getInstance();
        String packageName = themeParser.getPackageName();
        String subPathName = themeParser.getDynamicPath();
        if (!TextUtils.isEmpty(packageName)) {
            HwLog.i("HwLockScreenPanel", "loadDynamic");
            this.mLockScreenView = View.inflate(getContext(), R$layout.dynamic_unlock_screen, null);
            this.mLockScreen = (HwUnlockInterface$HwLockScreenReal) this.mLockScreenView;
            if (this.mLockScreenCallbackImpl != null) {
                this.mLockScreenCallbackImpl.setLockScreen(this.mLockScreen);
            }
            this.mLockScreen.setLockScreenCallback(this.mLockScreenCallbackImpl);
            this.mLockScreen.setLockPatternUtils(this.mLockPatternUtils);
            if (((DynamicUnlockScreen) this.mLockScreenView).init(packageName, subPathName)) {
                addView(this.mLockScreenView, -1, -1);
                hiddenHuaweiKeyguardView();
                return;
            }
        }
        this.mLockType = 1;
        loadSlide();
    }

    private void hiddenHuaweiKeyguardView() {
        View keyguardStatusView = getRootView().findViewById(R$id.keyguard_clock_container);
        if (keyguardStatusView != null) {
            keyguardStatusView.setVisibility(8);
        }
        View cameraDraglayerView = getRootView().findViewById(R$id.camera_container);
        if (cameraDraglayerView != null) {
            cameraDraglayerView.setVisibility(8);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!this.mIsIntersetEvent) {
            HwLog.w("HwLockScreenPanel", "unspect event dispatched to me");
        }
        boolean ret = super.dispatchTouchEvent(ev);
        if (ret || this.mUnlocMotionDetector == null) {
            return ret;
        }
        if (this.mLockType != 7 && this.mLockType != 8) {
            return ret;
        }
        int action = ev.getAction();
        if (action == 2) {
            this.mUnlockBlock = true;
        }
        if (action != 1 && action != 3) {
            return this.mUnlocMotionDetector.procUnlockMotionEvent(ev);
        }
        if (this.mUnlockBlock) {
            ret = this.mUnlocMotionDetector.procUnlockMotionEvent(ev);
        }
        this.mUnlockBlock = false;
        return ret;
    }

    public void reset() {
        int lockType = KeyguardTheme.getInst().getLockStyle();
        if (lockType != this.mLockType || lockType == 4 || lockType == 3) {
            HwLog.w("HwLockScreenPanel", "Style changed reset to " + lockType);
            this.mLockType = lockType;
            createLockScreenView(this.mContext);
        } else {
            this.mHandler.obtainMessage(104).sendToTarget();
        }
        boolean showKeyguard = KeyguardCfg.isDoubleLockOn(this.mContext);
        if (this.mLockScreenView != null) {
            int i;
            View view = this.mLockScreenView;
            if (showKeyguard) {
                i = 0;
            } else {
                i = 8;
            }
            view.setVisibility(i);
        } else if (showKeyguard) {
            HwLog.w("HwLockScreenPanel", "createLockScreenView when reset");
            createLockScreenView(this.mContext);
        }
    }

    public void setKeyguardCallback(KeyguardSecurityCallback keyguardsecuritycallback) {
        this.mKeyguardsecuritycallback = keyguardsecuritycallback;
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
    }

    public void showPromptReason(int reason) {
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return false;
    }

    public boolean startRevertAnimation(Runnable finishRunnable) {
        return false;
    }

    public void startAppearAnimation() {
    }

    public void showMessage(String message, int color) {
    }

    public void refreshBatteryInfo(BatteryStatus status) {
        HwLog.w("HwLockScreenPanel", "refreshBatteryInfo: " + this.mLockScreen);
        if (this.mLockScreen != null) {
            this.mLockScreen.onBatteryInfoChanged();
        }
    }

    public boolean handleMessage(Message msg) {
        HwLog.w("HwLockScreenPanel", "Handle message: " + msg.what);
        switch (msg.what) {
            case 1:
                this.mLockType = 0;
                HwLog.w("HwLockScreenPanel", "MSG_THEME_CHANGED");
                break;
            case 2:
                HwLog.w("HwLockScreenPanel", "MSG_USER_CHANGED");
                MusicMonitor.getInst(getContext()).freshState();
                reset();
                break;
            case 11:
            case 16:
                if (this.mLockType == 4) {
                    removeAllViews();
                    break;
                }
                break;
            case 30:
                HwLog.w("HwLockScreenPanel", "MSG_KEYGUARD_ADD_COVER");
                reset();
                break;
        }
        return false;
    }

    private boolean isSkipUnlockAnimation() {
        return this.mLockScreenView == null;
    }

    public void setAnimationParam(float param, float scale, int iPara100, int iPara255) {
        if (!isSkipUnlockAnimation()) {
            setScaleX(scale);
            setScaleY(scale);
            float oAlpha = getAlpha();
            float nAlpha = 1.0f - param;
            setAlpha(nAlpha);
            checkLyricShowState(oAlpha, nAlpha);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void checkLyricShowState(float oAlpha, float nAlpha) {
        if (this.mLockType == 7 && ((oAlpha >= 0.5f || nAlpha >= 0.5f) && ((oAlpha <= 0.5f || nAlpha <= 0.5f) && (this.mLockScreenView instanceof HwMusicView)))) {
            ((HwMusicView) this.mLockScreenView).updateLyricShowingState();
        }
    }

    public void setAnimStartState(int visible, float scale, float alpha) {
        if (!isSkipUnlockAnimation()) {
            setVisibility(visible);
            if (visible == 0) {
                if (this.mLockType == 3 && (this.mLockScreenView instanceof DynamicUnlockScreen)) {
                    reset();
                    ((DynamicUnlockScreen) this.mLockScreenView).playDynamic();
                } else if (this.mLockType == 4) {
                    reset();
                }
            }
            setScaleX(scale);
            setScaleY(scale);
            setAlpha(alpha);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (this.mLockType == 4 && KeyguardUpdateMonitor.getInstance(getContext()).isShowing()) {
            reset();
        }
        if (HwUnlockUtils.isTablet() && this.orientation != this.mContext.getResources().getConfiguration().orientation) {
            switch (this.mLockType) {
                case 7:
                case 8:
                    createLockScreenView(this.mContext);
                    break;
            }
        }
        this.orientation = this.mContext.getResources().getConfiguration().orientation;
        super.onConfigurationChanged(newConfig);
    }
}
