package com.android.keyguard.hwlockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityCallback;
import com.android.keyguard.KeyguardSecurityView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.huawei.hwtransition.control.LiftTransition.LiftListener;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.DisabledFeatureUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.view.KgViewUtils;
import java.util.HashSet;
import java.util.Set;

public class HwKeyguardBottomArea extends RelativeLayout implements KeyguardSecurityView, Callback {
    private IHwkeyguardBottomView mBottomArea = null;
    private ViewGroup mBottomContainer = null;
    private int mBottomType = 0;
    private Runnable mCameraFresher = new Runnable() {
        public void run() {
            if (DisabledFeatureUtils.getCameraDisabled() != DisabledFeatureUtils.refreshCameraDisabled(HwKeyguardBottomArea.this.getContext())) {
                AppHandler.sendMessage(120);
            }
        }
    };
    private View mContentsView;
    private final BroadcastReceiver mDevicePolicyReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HwLog.i("HwKeyguardBottomArea", "mDevicePolicyReceiver.onReceive()");
            if (intent != null && "android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction())) {
                GlobalContext.getBackgroundHandler().post(HwKeyguardBottomArea.this.mCameraFresher);
            }
        }
    };
    private boolean mInteresetEvent = false;
    private IOnProgressChangeListener mLiftChangeListener = new IOnProgressChangeListener() {
        public void onProgressChangeListener(float progress) {
            synchronized (HwKeyguardBottomArea.this.mSensorViews) {
                if (HwKeyguardBottomArea.this.mSensorViews.size() == 0) {
                    HwKeyguardBottomArea.this.updateLiftSensorViews();
                }
                if (progress < 0.01f) {
                    HwKeyguardBottomArea.this.updateNotificationScrollView();
                    HwKeyguardBottomArea.this.updateLiftSensorViews();
                }
                for (View view : HwKeyguardBottomArea.this.mSensorViews) {
                    if (view != null) {
                        HwKeyguardBottomArea.this.onProgressChangeView(view, progress, true);
                    }
                }
            }
        }
    };
    private Set<View> mSensorViews = new HashSet();
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitchComplete(int userId) {
            HwLog.i("HwKeyguardBottomArea", "onUserSwitchComplete");
            GlobalContext.getBackgroundHandler().post(HwKeyguardBottomArea.this.mCameraFresher);
        }
    };
    private int orientation;

    public interface IOnProgressChangeListener {
        void onProgressChangeListener(float f);
    }

    public interface IHwkeyguardBottomView extends LiftListener {
        void addProgressChangeListener(IOnProgressChangeListener iOnProgressChangeListener);

        boolean dispatchTouchEvent(MotionEvent motionEvent);

        int getVisibility();

        boolean isInterestedEvent(MotionEvent motionEvent);

        void onThemeStlyeChange();

        void setLiftReset();
    }

    public HwKeyguardBottomArea(Context context) {
        super(context);
    }

    public HwKeyguardBottomArea(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwKeyguardBottomArea(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.orientation = this.mContext.getResources().getConfiguration().orientation;
        this.mContentsView = findViewById(R$id.front_content);
        this.mBottomContainer = (ViewGroup) findViewById(R$id.bottom_container);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwLog.w("HwKeyguardBottomArea", "HwKeyguardBottomArea onAttachedToWindow");
        AppHandler.addListener(this);
        loadBottomArea();
        checkBottomVisibility();
        watchForCameraPolicyChanges();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AppHandler.removeListener(this);
        getContext().unregisterReceiver(this.mDevicePolicyReceiver);
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateMonitorCallback);
    }

    private static int getBottomType() {
        int type = KeyguardTheme.getInst().getLockStyle();
        if (type == 5) {
            return 3;
        }
        if (type == 6 || type == 7 || type == 8) {
            return 4;
        }
        if (type == 2) {
            return 2;
        }
        return 1;
    }

    private void loadBottomArea() {
        int bottomType = getBottomType();
        if (this.mBottomArea == null || bottomType != this.mBottomType || MusicInfo.getInst().isPlaying()) {
            loadCommonBottomArea(bottomType);
        } else {
            this.mBottomArea.onThemeStlyeChange();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (HwUnlockUtils.isTablet() && this.orientation != this.mContext.getResources().getConfiguration().orientation) {
            loadBottomAreaForTable();
        }
        this.orientation = this.mContext.getResources().getConfiguration().orientation;
    }

    private void loadBottomAreaForTable() {
        loadCommonBottomArea(getBottomType());
    }

    private void loadCommonBottomArea(int bottomType) {
        this.mBottomType = bottomType;
        this.mBottomContainer.removeAllViews();
        this.mBottomArea = null;
        View newView = null;
        if (this.mBottomType == 2) {
            newView = View.inflate(this.mContext, R$layout.bottom_magazine, null);
        } else if (this.mBottomType == 1 || this.mBottomType == 4) {
            newView = View.inflate(this.mContext, R$layout.bottom_panel_slide, null);
        } else {
            HwLog.w("HwKeyguardBottomArea", "LoadBottomArea fail as invalide type");
        }
        if (newView != null) {
            this.mBottomContainer.addView(newView);
            HwLog.d("HwKeyguardBottomArea", "loadBottomArea with " + bottomType + "; newView");
        }
        if (newView instanceof IHwkeyguardBottomView) {
            this.mBottomArea = (IHwkeyguardBottomView) newView;
            this.mBottomArea.addProgressChangeListener(this.mLiftChangeListener);
            this.mBottomType = bottomType;
        }
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
    }

    public void onPause() {
    }

    public void onResume(int reason) {
    }

    public boolean needsInput() {
        return false;
    }

    public void showPromptReason(int reason) {
    }

    public void showMessage(String message, int color) {
    }

    public void startAppearAnimation() {
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        return false;
    }

    public boolean startRevertAnimation(Runnable finishRunnable) {
        return false;
    }

    public boolean isInterestedEvent(MotionEvent event) {
        boolean z = false;
        if (this.mBottomArea != null && this.mBottomArea.getVisibility() == 0) {
            z = true;
        }
        this.mInteresetEvent = z;
        if (this.mInteresetEvent) {
            this.mInteresetEvent = this.mBottomArea.isInterestedEvent(event);
        } else {
            HwLog.w("HwKeyguardBottomArea", "BottomArea not interested as null view.");
        }
        return this.mInteresetEvent;
    }

    private void checkBottomVisibility() {
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
        int i = (!monitor.isShowing() || monitor.isInBouncer()) ? 8 : 0;
        super.setVisibility(i);
        KgViewUtils.restoreViewState(this.mContentsView);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!KeyguardUpdateMonitor.getInstance(getContext()).isDeviceProvisioned()) {
            return true;
        }
        int action = event.getActionMasked();
        boolean ret = false;
        if (this.mInteresetEvent && this.mBottomArea != null && getVisibility() == 0) {
            ret = this.mBottomArea.dispatchTouchEvent(event);
        }
        if (action == 3 || action == 1) {
            this.mInteresetEvent = false;
        }
        return ret;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.mInteresetEvent;
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 1 || msg.what == 2) {
            loadBottomArea();
            updateLiftSensorViews();
        } else if (msg.what == 10 || msg.what == 120) {
            checkBottomVisibility();
            if (this.mBottomArea != null) {
                this.mBottomArea.setLiftReset();
            }
        } else if (msg.what == 11) {
            setVisibility(8);
        } else if (msg.what == 15) {
            if (!HwKeyguardUpdateMonitor.getInstance().isOccluded()) {
                checkBottomVisibility();
            }
            if (this.mBottomArea != null) {
                this.mBottomArea.setLiftReset();
            }
        }
        return false;
    }

    public void addLiftSensorView(View v) {
        if (v != null) {
            synchronized (this.mSensorViews) {
                this.mSensorViews.add(v);
            }
        }
    }

    public void removeLiftSensorView(View v) {
        synchronized (this.mSensorViews) {
            this.mSensorViews.remove(v);
        }
    }

    private void updateLiftSensorViews() {
        addLiftSensorView(findViewById(R$id.camera_container));
        View root = getRootView();
        if (root != null) {
            addLiftSensorView(root.findViewById(R$id.slide_locktip));
        }
    }

    private void updateNotificationScrollView() {
        View notificationScrollView = HwKeyguardPolicy.getInst().getNotificationStackScrollerView();
        if (notificationScrollView != null) {
            if (!(HwKeyguardPolicy.getInst().blockNotificationInKeyguard() || this.mSensorViews.contains(notificationScrollView))) {
                addLiftSensorView(notificationScrollView);
            }
            if (HwKeyguardPolicy.getInst().blockNotificationInKeyguard() && this.mSensorViews.contains(notificationScrollView)) {
                removeLiftSensorView(notificationScrollView);
            }
        }
    }

    private void onProgressChangeView(View targetView, float progress, boolean goneStatus) {
        if (progress < 0.01f) {
            targetView.setAlpha(1.0f);
            targetView.setVisibility(0);
        } else if (progress > 0.99f) {
            targetView.setAlpha(1.0f);
            targetView.setVisibility(goneStatus ? 8 : 4);
        } else {
            targetView.setAlpha(1.0f - progress);
        }
    }

    public void setAnimationParam(float param, float scale, int iPara100, int iPara255) {
        if (this.mContentsView != null) {
            this.mContentsView.setScaleX(scale);
            this.mContentsView.setScaleY(scale);
            this.mContentsView.setAlpha(1.0f - param);
        }
    }

    public void setAnimStartState(int visibility, float scale, float alpha) {
        if (this.mContentsView != null) {
            this.mContentsView.setVisibility(visibility);
            this.mContentsView.setScaleX(scale);
            this.mContentsView.setScaleY(scale);
            this.mContentsView.setAlpha(alpha);
        }
    }

    public void setVisibility(int visibility) {
        if (visibility == 0) {
            HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
            if (monitor.isInBouncer() || !monitor.isShowing()) {
                HwLog.i("HwKeyguardBottomArea", "HwKeyguardBottom setVisibility with error state");
            }
        }
        super.setVisibility(visibility);
    }

    private void watchForCameraPolicyChanges() {
        HwLog.i("HwKeyguardBottomArea", "watchForCameraPolicyChanges()");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, filter, null, null);
    }
}
