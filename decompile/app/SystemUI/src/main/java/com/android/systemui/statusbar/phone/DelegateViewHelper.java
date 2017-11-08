package com.android.systemui.statusbar.phone;

import android.os.SystemProperties;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import com.android.systemui.lazymode.SlideTouchEvent;
import com.android.systemui.statusbar.policy.HwSplitScreenArrowView;

public class DelegateViewHelper {
    private View mDelegateView;
    private float[] mDownPoint = new float[2];
    private boolean mPanelShowing;
    private SlideTouchEvent mSlideTouchEvent;
    private View mSourceView;
    private HwSplitScreenArrowView mSplitScreenArrowView;
    private int[] mTempPoint = new int[2];

    public DelegateViewHelper(View sourceView) {
        setSourceView(sourceView);
        this.mSlideTouchEvent = new SlideTouchEvent(sourceView.getContext());
        sourceView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            public void onViewDetachedFromWindow(View v) {
                DelegateViewHelper.this.mSlideTouchEvent.unRegister();
            }

            public void onViewAttachedToWindow(View v) {
                DelegateViewHelper.this.mSlideTouchEvent.register(v.getContext());
            }
        });
    }

    public void setDelegateView(View view) {
        this.mDelegateView = view;
    }

    public void setSourceView(View view) {
        this.mSourceView = view;
    }

    public void setSplitScreenArrowView(HwSplitScreenArrowView splitScreenArrowView) {
        this.mSplitScreenArrowView = splitScreenArrowView;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mSplitScreenArrowView != null) {
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                return false;
            }
            if (!"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                this.mSplitScreenArrowView.handleSplitScreenGesture(event);
            }
            if (8 != this.mSplitScreenArrowView.getVisibility()) {
                return false;
            }
        }
        this.mSlideTouchEvent.handleTouchEvent(event);
        if (this.mSourceView == null || this.mDelegateView == null) {
            return false;
        }
        int action = event.getAction();
        switch (action) {
            case 0:
                boolean z;
                if (this.mDelegateView.getVisibility() == 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.mPanelShowing = z;
                this.mDownPoint[0] = event.getX();
                this.mDownPoint[1] = event.getY();
                break;
        }
        if (HwPhoneStatusBar.getInstance().hasSearchPanel()) {
            return false;
        }
        if (action == 0) {
            HwPhoneStatusBar.getInstance().setInteracting(2, true);
        } else if (action == 1 || action == 3) {
            HwPhoneStatusBar.getInstance().setInteracting(2, false);
        }
        this.mSourceView.getLocationOnScreen(this.mTempPoint);
        float sourceX = (float) this.mTempPoint[0];
        float sourceY = (float) this.mTempPoint[1];
        this.mDelegateView.getLocationOnScreen(this.mTempPoint);
        float deltaX = sourceX - ((float) this.mTempPoint[0]);
        float deltaY = sourceY - ((float) this.mTempPoint[1]);
        event.offsetLocation(deltaX, deltaY);
        this.mDelegateView.dispatchTouchEvent(event);
        event.offsetLocation(-deltaX, -deltaY);
        return this.mPanelShowing;
    }
}
