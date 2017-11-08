package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.android.keyguard.R$string;
import com.android.keyguard.hwlockscreen.HwKeyguardBottomArea.IHwkeyguardBottomView;
import com.android.keyguard.hwlockscreen.HwKeyguardBottomArea.IOnProgressChangeListener;
import com.huawei.hwtransition.control.LiftTransition;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.inf.ILiftAbleView;
import com.huawei.keyguard.inf.ILiftAbleView.ILiftStateListener;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.KgViewUtils;
import fyusion.vislib.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class InfoCenterView extends RelativeLayout implements IHwkeyguardBottomView, ILiftAbleView {
    private ArrayList<WeakReference<ILiftStateListener>> mCallbacks;
    private float mContentHeight;
    private int mHintHeight;
    private View mHintViewDown;
    private View mHintViewUp;
    private float mLastLiftY;
    private long mLastTimePokeWakeCalled;
    private float mLiftRange;
    private LiftTransition mLiftTransition;
    private int mMode;
    private IOnProgressChangeListener mOnProgressChangeListener;
    private boolean mSizeChange;
    private TextView mTVChangeStyle;
    private ToolBoxView mToolBoxView;
    private int mTouchMode;

    public InfoCenterView(Context context) {
        this(context, null);
    }

    public InfoCenterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMode = 0;
        this.mLastTimePokeWakeCalled = 0;
        this.mSizeChange = false;
        this.mTouchMode = 0;
        this.mToolBoxView = null;
        this.mCallbacks = new ArrayList();
        this.mOnProgressChangeListener = null;
        this.mTVChangeStyle = null;
        this.mLiftTransition = new LiftTransition(context, this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHintViewUp = findViewById(R$id.info_center_hint_up);
        this.mHintViewDown = findViewById(R$id.info_center_hint_down);
        this.mToolBoxView = (ToolBoxView) findViewById(R$id.bottom_tool_box);
        this.mLiftTransition.setMultiDirectional(false);
        initChangeThemeStyle();
    }

    public void onThemeStlyeChange() {
        boolean isMagazineLockStyle;
        int i = 8;
        boolean showMask = true;
        int i2 = 0;
        int mStyle = KeyguardTheme.getInst().getLockStyle();
        if (mStyle == 7 || mStyle == 6 || mStyle == 8) {
            isMagazineLockStyle = MagazineUtils.isMagazineLockStyle();
        } else {
            isMagazineLockStyle = false;
        }
        TextView textView = this.mTVChangeStyle;
        if (!isMagazineLockStyle) {
            i = 0;
        }
        textView.setVisibility(i);
        View container = findViewById(R$id.info_center);
        if (container != null) {
            if (!(mStyle == 3 || mStyle == 4)) {
                showMask = false;
            }
            if (showMask) {
                i2 = R$drawable.slide_toolbox_mask;
            }
            container.setBackgroundResource(i2);
        }
    }

    private void initChangeThemeStyle() {
        LinearLayout extraView = (LinearLayout) findViewById(R$id.extra_layout);
        this.mTVChangeStyle = (TextView) findViewById(R$id.change_lockscreen_style);
        if (this.mTVChangeStyle != null) {
            onThemeStlyeChange();
            this.mTVChangeStyle.setOnClickListener(this.mToolBoxView);
            this.mTVChangeStyle.setFocusable(true);
            if (extraView != null && this.mTVChangeStyle.getVisibility() == 0) {
                extraView.setShowDividers(2);
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (KeyguardCfg.isExtremePowerSavingMode()) {
            setVisibility(8);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        if (checkHeight()) {
            this.mLiftTransition.setLiftRange(-this.mContentHeight, this.mContentHeight / 2.0f);
            if (this.mMode == 0) {
                setLift(0.0f);
            }
        }
        super.onConfigurationChanged(newConfig);
        this.mTVChangeStyle.setText(R$string.emui30_keyguard_change_style);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        HwLog.w("InfoCenterView", "InfoCenterView dispatchDraw " + this.mContentHeight + "  " + this.mSizeChange);
        if (checkHeight() || this.mSizeChange) {
            this.mLiftTransition.setLiftRange(-this.mContentHeight, this.mContentHeight / 2.0f);
            this.mLiftTransition.setHeight(this.mContentHeight, 0);
            if (this.mMode == 0) {
                setLift(0.0f);
            }
            this.mSizeChange = false;
        }
    }

    private boolean checkHeight() {
        if (this.mContentHeight != 0.0f && this.mHintHeight != 0) {
            return false;
        }
        View content = findViewById(R$id.extra_layout);
        if (content != null) {
            this.mContentHeight = (float) content.getHeight();
        }
        View hint = findViewById(R$id.info_center_hint);
        if (hint != null) {
            this.mHintHeight = hint.getHeight();
        }
        return true;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        HwLog.w("InfoCenterView", "InfoCenterView dispatchDraw " + h + "  " + oldh);
        if (h != oldh) {
            this.mSizeChange = true;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        HwLog.w("InfoCenterView", "InfoCenterView onInterceptTouchEvent " + event);
        onGrabbedStateChange();
        return false;
    }

    private boolean isInLiftView(MotionEvent event) {
        if (event.getRawY() > ((float) ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getHeight()) + this.mLiftRange) {
            return true;
        }
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        int touchMode = this.mTouchMode;
        if (KgViewUtils.isCancelAction(event)) {
            this.mTouchMode = 0;
        }
        switch (touchMode) {
            case 1:
                return super.dispatchTouchEvent(event);
            case 2:
                return this.mLiftTransition.onTouchEvent(event, isInLiftView(event));
            default:
                return false;
        }
    }

    public void setLift(float deltaX, float deltaY) {
    }

    public void setLift(float delta) {
        this.mLiftRange = delta;
        setTranslationY(this.mContentHeight + delta);
        float percent = 0.0f;
        if (this.mContentHeight > 0.01f) {
            percent = Math.abs(delta) / this.mContentHeight;
        }
        if (percent > 1.0f) {
            percent = 1.0f;
        }
        if (percent < 0.5f) {
            this.mHintViewUp.setAlpha(1.0f - (percent * 2.0f));
            this.mHintViewDown.setAlpha(0.0f);
        } else {
            this.mHintViewUp.setAlpha(0.0f);
            this.mHintViewDown.setAlpha((percent * 2.0f) - 1.0f);
        }
        if (this.mOnProgressChangeListener != null) {
            this.mOnProgressChangeListener.onProgressChangeListener(percent);
        }
        if (percent < 0.1f) {
            this.mMode = 0;
        } else {
            this.mMode = 1;
        }
        for (WeakReference<ILiftStateListener> item : this.mCallbacks) {
            ILiftStateListener callback = (ILiftStateListener) item.get();
            if (callback != null) {
                callback.onLiftModeStateChange(delta);
            }
        }
        this.mLastLiftY = delta;
    }

    public void setLiftMode(int liftMode) {
    }

    public void onLiftAnimationEnd() {
        if (!(this.mLiftTransition == null || this.mLiftTransition.isLifted())) {
            setLift(0.0f);
        }
        doReporterSlideLift();
    }

    private void doReporterSlideLift() {
        if (this.mLiftTransition != null && this.mLiftTransition.isLifted()) {
            HwLockScreenReporter.report(this.mContext, 147, BuildConfig.FLAVOR);
        }
    }

    private void onGrabbedStateChange() {
        if (System.currentTimeMillis() - this.mLastTimePokeWakeCalled > 2000) {
            HwKeyguardPolicy.getInst().userActivity();
            this.mLastTimePokeWakeCalled = System.currentTimeMillis();
        }
    }

    public void reset() {
        setLift(0.0f);
        if (this.mLiftTransition != null) {
            this.mLiftTransition.reset();
        }
    }

    public void registerLiftStateListener(ILiftStateListener callback) {
        int sz = this.mCallbacks.size();
        HwLog.d("InfoCenterView", "registerInfoModeCallback " + callback + ",size=" + sz);
        for (int i = 0; i < sz; i++) {
            if (((WeakReference) this.mCallbacks.get(i)).get() == callback) {
                HwLog.w("InfoCenterView", "tried to add another callback " + callback);
                return;
            }
        }
        this.mCallbacks.add(new WeakReference(callback));
        clearCallback(null);
    }

    public void unregisterLiftStateListener(ILiftStateListener callback) {
        int sz = this.mCallbacks.size();
        HwLog.i("InfoCenterView", "unRegisterCallback " + callback + ", callback size = " + sz);
        int i = sz - 1;
        while (i >= 0 && ((WeakReference) this.mCallbacks.get(i)).get() != callback) {
            i--;
        }
        if (i < 0) {
            HwLog.w("InfoCenterView", "unRegisterCallback not found " + callback);
        } else {
            this.mCallbacks.remove(i);
        }
    }

    private void clearCallback(ILiftStateListener callback) {
        for (int i = this.mCallbacks.size() - 1; i >= 0; i--) {
            if (((WeakReference) this.mCallbacks.get(i)).get() == callback) {
                this.mCallbacks.remove(i);
            }
        }
    }

    public void dismiss() {
        if (this.mLiftTransition != null) {
            this.mLiftTransition.startAnimation(2);
        }
    }

    public boolean isInterestedEvent(MotionEvent event) {
        boolean z = true;
        HwLog.w("InfoCenterView", "Bottom InfoCenter liftY " + this.mLastLiftY + " event-Y" + event.getY() + " View-Y:" + getY() + " " + getHeight() + " ; HintHeight   " + this.mHintHeight);
        if (event.getAction() != 0) {
            return false;
        }
        this.mTouchMode = 0;
        if (KgViewUtils.isTouchInEffectView(event, this.mToolBoxView) || KgViewUtils.isTouchInEffectView(event, this.mTVChangeStyle)) {
            this.mTouchMode = 1;
        } else if (KgViewUtils.isTouchBelowView(event, this.mHintViewUp, false) || KgViewUtils.isViewCanbeTouched(this.mToolBoxView)) {
            this.mTouchMode = 2;
        }
        HwLog.v("InfoCenterView", "InfoCenterView interest " + this.mTouchMode);
        if (this.mTouchMode <= 0) {
            z = false;
        }
        return z;
    }

    public void setLiftReset() {
        if (this.mLiftTransition != null) {
            this.mLiftTransition.reset();
        }
        setLift(0.0f);
        this.mToolBoxView.updateContentDescription();
    }

    public void addProgressChangeListener(IOnProgressChangeListener iListener) {
        this.mOnProgressChangeListener = iListener;
    }
}
