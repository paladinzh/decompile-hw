package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import com.android.systemui.DejankUtils;
import com.android.systemui.R;
import com.android.systemui.utils.analyze.PerfDebugUtils;

public class PhoneStatusBarView extends PanelBar {
    PhoneStatusBar mBar;
    private final PhoneStatusBarTransitions mBarTransitions = new HwPhoneStatusBarTransitions(this);
    private Runnable mDelayedHideExpandedRunnable = new Runnable() {
        public void run() {
            if (PhoneStatusBarView.this.mBar != null) {
                PhoneStatusBarView.this.mBar.makeExpandedInvisible();
            }
        }
    };
    private Runnable mHideExpandedRunnable = new Runnable() {
        public void run() {
            if (PhoneStatusBarView.this.mPanelFraction != 0.0f) {
                return;
            }
            if (PhoneStatusBarView.this.mBar.getFastUnlockMode()) {
                PhoneStatusBarView.this.postDelayed(PhoneStatusBarView.this.mDelayedHideExpandedRunnable, 50);
            } else {
                PhoneStatusBarView.this.mBar.makeExpandedInvisible();
            }
        }
    };
    boolean mIsFullyOpenedPanel = false;
    private float mMinFraction;
    private float mPanelFraction;
    private ScrimController mScrimController;

    public PhoneStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public void setBar(PhoneStatusBar bar) {
        this.mBar = bar;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
    }

    public void onFinishInflate() {
        this.mBarTransitions.init();
    }

    public boolean panelEnabled() {
        return this.mBar.panelsEnabled();
    }

    public boolean onRequestSendAccessibilityEventInternal(View child, AccessibilityEvent event) {
        if (!super.onRequestSendAccessibilityEventInternal(child, event)) {
            return false;
        }
        AccessibilityEvent record = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(record);
        dispatchPopulateAccessibilityEvent(record);
        event.appendRecord(record);
        return true;
    }

    public void onPanelPeeked() {
        PerfDebugUtils.beginSystraceSection("PhoneStatusBarView_onPanelPeeked");
        super.onPanelPeeked();
        this.mBar.makeExpandedVisible(false);
        PerfDebugUtils.endSystraceSection();
    }

    public void onPanelCollapsed() {
        super.onPanelCollapsed();
        DejankUtils.postAfterTraversal(this.mHideExpandedRunnable);
        this.mIsFullyOpenedPanel = false;
    }

    public void removePendingHideExpandedRunnables() {
        DejankUtils.removeCallbacks(this.mHideExpandedRunnable);
    }

    public void onPanelFullyOpened() {
        super.onPanelFullyOpened();
        if (!this.mIsFullyOpenedPanel) {
            this.mPanel.sendAccessibilityEvent(32);
        }
        this.mIsFullyOpenedPanel = true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return !this.mBar.interceptTouchEvent(event) ? super.onTouchEvent(event) : true;
    }

    public void onTrackingStarted() {
        super.onTrackingStarted();
        this.mBar.onTrackingStarted();
        this.mScrimController.onTrackingStarted();
        removePendingHideExpandedRunnables();
    }

    public void onClosingFinished() {
        super.onClosingFinished();
        this.mBar.onClosingFinished();
    }

    public void onTrackingStopped(boolean expand) {
        super.onTrackingStopped(expand);
        this.mBar.onTrackingStopped(expand);
    }

    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mScrimController.onExpandingFinished();
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !this.mBar.interceptTouchEvent(event) ? super.onInterceptTouchEvent(event) : true;
    }

    public void panelScrimMinFractionChanged(float minFraction) {
        if (this.mMinFraction != minFraction) {
            this.mMinFraction = minFraction;
            if (minFraction != 0.0f) {
                this.mScrimController.animateNextChange();
            }
            updateScrimFraction();
        }
    }

    public void panelExpansionChanged(float frac, boolean expanded) {
        super.panelExpansionChanged(frac, expanded);
        this.mPanelFraction = frac;
        updateScrimFraction();
    }

    private void updateScrimFraction() {
        this.mScrimController.setPanelExpansion(Math.max(this.mPanelFraction, this.mMinFraction));
    }

    public void onDensityOrFontScaleChanged() {
        LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        setLayoutParams(layoutParams);
    }
}
