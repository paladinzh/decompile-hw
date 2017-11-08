package com.android.systemui.statusbar.phone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.hwlockscreen.HwKeyguardBottomArea;
import com.android.systemui.R;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import com.huawei.keyguard.inf.HwKeyguardPolicy;

@SuppressLint({"NewApi"})
public abstract class HwPanelView extends PanelView {
    private boolean mChangeStatus = true;
    protected HwKeyguardBottomArea mHwKeyguardBottom = null;
    protected boolean mInAnimation = false;
    protected boolean mKeyguardShowing = false;
    protected KeyguardStatusBarView mKeyguardStatusBar;
    protected KeyguardStatusView mKeyguardStatusView;
    protected NotificationsQuickSettingsContainer mNotificationContainerParent;
    private IPanelDragListener mPanelDragListener = null;

    public interface IPanelDragListener {
        void blockAnimation(boolean z);

        void onDrag(float f);

        void onProcFyuseMotionEvent(MotionEvent motionEvent);
    }

    public HwPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setKeyguardStatusBar(KeyguardStatusBarView keyguardBar, IPanelDragListener dragListener) {
        HwLog.v(TAG, "setKeyguardStatusBar " + keyguardBar + "  " + this.mNotificationContainerParent);
        this.mPanelDragListener = dragListener;
        if (keyguardBar != null) {
            this.mKeyguardStatusBar = keyguardBar;
            if (this.mNotificationContainerParent != null) {
                this.mNotificationContainerParent.mKeyguardStatusBar = keyguardBar;
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mParent != null) {
            this.mKeyguardStatusBar = (KeyguardStatusBarView) ((View) this.mParent).findViewById(R.id.keyguard_header);
            HwLog.w(TAG, "onAttachedToWindow " + this.mKeyguardStatusBar + "  " + this.mNotificationContainerParent);
            if (!(this.mKeyguardStatusBar == null || this.mNotificationContainerParent == null)) {
                this.mNotificationContainerParent.mKeyguardStatusBar = this.mKeyguardStatusBar;
            }
        }
        Log.w(TAG, "onAttachedToWindow " + this.mParent + ";    " + this.mKeyguardStatusBar);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHwKeyguardBottom = (HwKeyguardBottomArea) findViewById(R.id.hw_keyguard_bottom_area);
    }

    protected boolean isKeyguardStatusViewVisiable() {
        return this.mKeyguardStatusView != null && this.mKeyguardStatusView.getVisibility() == 0;
    }

    public boolean isUseGgBottomView() {
        return this.mKeyguardStatusView != null ? HwKeyguardPolicy.isUseGgBottomView() : false;
    }

    protected boolean isUseGgStatusView() {
        return HwKeyguardPolicy.isUseGgStatusView();
    }

    protected void setViewVisibility(View v, int visibility) {
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    protected KeyguardAffordanceView getLockIcon() {
        return this.mKeyguardBottomArea == null ? null : this.mKeyguardBottomArea.getLockIcon();
    }

    protected View getBottomView() {
        return this.mHwKeyguardBottom != null ? this.mHwKeyguardBottom : this.mKeyguardBottomArea;
    }

    protected void setLaunchingAffordance(KeyguardAffordanceView view, boolean launchingAffordance) {
        if (view != null) {
            view.setLaunchingAffordance(launchingAffordance);
        }
    }

    public void setVisibility(int visibility) {
        PerfDebugUtils.beginSystraceSection("HwPanelView_setVisibility_super");
        if (visibility != getVisibility()) {
            super.setVisibility(visibility);
        }
        if (visibility == 0 && !this.mKeyguardShowing && isKeyguardStatusViewVisiable()) {
            this.mKeyguardStatusView.setVisibility(8);
        }
        PerfDebugUtils.endSystraceSection();
    }

    public void updateKeyguardState(boolean showAndNotOccluded, boolean bouncerShowing) {
        if (showAndNotOccluded && !bouncerShowing) {
            setVisibility(0);
        }
    }

    public void expand(boolean animate) {
        super.expand(animate);
        setVisibility(0);
    }

    public void afterKeyguardExit() {
        restoreDrawState();
        this.mStatusBar.makeExpandedInvisible();
        onTrackingStopped(false);
    }

    public void restoreDrawState() {
        HwLog.w(TAG, "HwPanelView restoreDrawState");
        this.mInAnimation = false;
        setAnimationParam(0.0f, 1.0f, 0, 0);
    }

    public void setAnimationParam(float param, float scale, int iPara100, int iPara255) {
        HwLog.w(TAG, "setAnimationParam " + param + " " + getVisibility() + "  " + getAlpha());
        float alpha = 1.0f - param;
        int iSize = getChildCount();
        for (int idx = 0; idx < iSize; idx++) {
            View child = getChildAt(idx);
            if (child.getVisibility() == 0 && isAnimationChild(child.getId())) {
                updateAlaphaAndScaleForChild(child, scale, alpha);
            }
        }
    }

    private boolean isAnimationChild(int id) {
        if (id == R.id.notification_container_parent) {
            return true;
        }
        if (id == R.id.keyguard_status_view) {
            return HwKeyguardPolicy.isUseGgStatusView();
        }
        return false;
    }

    public void setAnimStartState(int visibiliy, float scale, float alpha) {
        setVisibility(visibiliy);
        int iSize = getChildCount();
        for (int idx = 0; idx < iSize; idx++) {
            View child = getChildAt(idx);
            if (isAnimationChild(child.getId())) {
                updateAlaphaAndScaleForChild(child, scale, alpha);
                child.setVisibility(visibiliy);
            }
        }
        clearAnimation();
        postInvalidateOnAnimation();
        this.mInAnimation = true;
    }

    private void updateAlaphaAndScaleForChild(View child, float scale, float alpha) {
        HwLog.w(TAG, "updateAlaphaAndScaleForChild " + child.getVisibility() + "  " + child);
        child.setScaleX(scale);
        child.setScaleY(scale);
        child.setAlpha(alpha);
    }

    protected void updateBackdropView(float alpha) {
        if (this.mPanelDragListener == null || this.mInAnimation) {
            HwLog.i(TAG, "updateBackdropView Skiped " + this.mInAnimation);
        } else {
            this.mPanelDragListener.onDrag(alpha);
        }
    }

    protected void blockBackdropAnimation(boolean block) {
        if (this.mPanelDragListener != null) {
            this.mPanelDragListener.blockAnimation(block);
        }
    }

    public void setBokehChangeStatus(boolean change) {
        this.mChangeStatus = change;
    }

    public boolean getBokehChangeStatus() {
        return this.mChangeStatus;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean ret = super.onInterceptTouchEvent(event);
        if (!(ret || this.mPanelDragListener == null)) {
            this.mPanelDragListener.onProcFyuseMotionEvent(event);
        }
        return ret;
    }
}
