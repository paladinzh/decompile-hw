package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.PerfDebugUtils;

public abstract class PanelBar extends FrameLayout {
    public static final String TAG = PanelBar.class.getSimpleName();
    private Runnable mDalayedTintRunnable = new Runnable() {
        public void run() {
            TintManager.getInstance().setFullExpanded(PanelBar.this.mFullyOpened);
        }
    };
    private boolean mFullyOpened = false;
    private boolean mHasOpenSend = false;
    private boolean mIsExpandLast = false;
    PanelView mPanel;
    private int mState = 0;
    private boolean mTracking;

    public abstract void panelScrimMinFractionChanged(float f);

    public void go(int state) {
        this.mState = state;
    }

    public PanelBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setPanel(PanelView pv) {
        this.mPanel = pv;
        pv.setBar(this);
    }

    public void setBouncerShowing(boolean showing) {
        int important;
        if (showing) {
            important = 4;
        } else {
            important = 0;
        }
        setImportantForAccessibility(important);
        if (this.mPanel != null) {
            this.mPanel.setImportantForAccessibility(important);
        }
    }

    public boolean panelEnabled() {
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean z = true;
        if (panelEnabled()) {
            if (event.getAction() == 0) {
                PanelView panel = this.mPanel;
                if (panel == null) {
                    Log.v(TAG, String.format("onTouch: no panel for touch at (%d,%d)", new Object[]{Integer.valueOf((int) event.getX()), Integer.valueOf((int) event.getY())}));
                    return true;
                } else if (!panel.isEnabled()) {
                    Log.v(TAG, String.format("onTouch: panel (%s) is disabled, ignoring touch at (%d,%d)", new Object[]{panel, Integer.valueOf((int) event.getX()), Integer.valueOf((int) event.getY())}));
                    return true;
                }
            }
            if (this.mPanel != null) {
                z = this.mPanel.onTouchEvent(event);
            }
            return z;
        }
        if (event.getAction() == 0) {
            Log.v(TAG, String.format("onTouch: all panels disabled, ignoring touch at (%d,%d)", new Object[]{Integer.valueOf((int) event.getX()), Integer.valueOf((int) event.getY())}));
        }
        if (event.getAction() == 1 && this.mPanel != null) {
            HwLog.i(TAG, "process up after disabled");
            this.mPanel.onTouchEvent(event);
        }
        return false;
    }

    public void panelExpansionChanged(float frac, boolean expanded) {
        if (this.mIsExpandLast != expanded) {
            Log.i(TAG, "panelExpansionChanged " + frac + ", " + expanded);
            this.mIsExpandLast = expanded;
        }
        boolean fullyClosed = true;
        boolean z = false;
        PerfDebugUtils.beginSystraceSection("PanelBar_panelExpansionChanged_setVisibility");
        if (this.mPanel == null) {
            HwLog.e(TAG, "panelExpansionChanged mPanel == null");
            return;
        }
        PanelView pv = this.mPanel;
        SystemUiUtil.setViewVisibility(pv, expanded ? 0 : 4);
        PerfDebugUtils.endSystraceSection();
        if (expanded) {
            if (this.mState == 0) {
                go(1);
                onPanelPeeked();
            }
            fullyClosed = false;
            if (pv.getExpandedFraction() >= 1.0f) {
                z = true;
            } else {
                z = false;
            }
            if (!z) {
                TintManager.getInstance().updateBarAlpha(0.0f);
            }
        } else {
            TintManager.getInstance().updateBarAlpha(1.0f);
        }
        if (z && !this.mTracking) {
            go(2);
            onPanelFullyOpened();
            if (!this.mHasOpenSend && frac > 0.0f) {
                sendStatusBarVisibleBroadcast(true);
                this.mHasOpenSend = true;
            }
        } else if (!(!fullyClosed || this.mTracking || this.mState == 0)) {
            go(0);
            onPanelCollapsed();
            if (this.mHasOpenSend) {
                sendStatusBarVisibleBroadcast(false);
                this.mHasOpenSend = false;
            }
        }
        if (this.mPanel == null || !this.mPanel.getFastUnlockMode()) {
            TintManager.getInstance().setFullExpanded(z);
        } else {
            this.mFullyOpened = z;
            postDelayed(this.mDalayedTintRunnable, 100);
        }
    }

    public void collapsePanel(boolean animate, boolean delayed, float speedUpFactor) {
        HwLog.i(TAG, "collapsePanel:" + animate);
        boolean waiting = false;
        PanelView pv = this.mPanel;
        if (!animate || pv.isFullyCollapsed()) {
            pv.resetViews();
            pv.setExpandedFraction(0.0f);
            pv.cancelPeek();
            pv.instantCollapse();
            HwLog.i(TAG, "collapsePanel2");
        } else {
            pv.collapse(delayed, speedUpFactor);
            waiting = true;
            HwLog.i(TAG, "collapsePanel1");
        }
        if (!waiting && this.mState != 0) {
            go(0);
            onPanelCollapsed();
            HwLog.i(TAG, "collapsePane3");
            if (this.mHasOpenSend) {
                sendStatusBarVisibleBroadcast(false);
                this.mHasOpenSend = false;
            }
        }
    }

    public void onPanelPeeked() {
    }

    public void onPanelCollapsed() {
    }

    public void onPanelFullyOpened() {
    }

    public void onTrackingStarted() {
        this.mTracking = true;
    }

    public void onTrackingStopped(boolean expand) {
        this.mTracking = false;
    }

    public void onExpandingFinished() {
    }

    public void onClosingFinished() {
    }

    public int getState() {
        return this.mState;
    }

    private void sendStatusBarVisibleBroadcast(final boolean isVisible) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                Intent statusBarVisibleIntent = new Intent("com.android.systemui.statusbar.visible.change");
                statusBarVisibleIntent.putExtra("visible", isVisible ? "true" : "false");
                PanelBar.this.getContext().sendBroadcastAsUser(statusBarVisibleIntent, UserHandle.ALL, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
                HwLog.i(PanelBar.TAG, "INTENT_STATUSBAR_VISIBLE_CHANGE sended, EXTRA_VISIBLE = " + isVisible);
                return false;
            }
        });
    }
}
