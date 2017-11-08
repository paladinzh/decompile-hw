package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.systemui.AutoReinflateContainer;
import com.android.systemui.AutoReinflateContainer.InflateListener;
import com.android.systemui.R;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.utils.HwLog;

public class NotificationsQuickSettingsContainer extends FrameLayout implements OnInflateListener, InflateListener {
    private boolean mCustomizerAnimating;
    private boolean mInflated;
    View mKeyguardStatusBar;
    private int mLastOrientation = -1;
    private AutoReinflateContainer mQsContainer;
    private boolean mQsExpanded;
    private View mStackScroller;
    private int mStackScrollerMargin;
    private View mUserSwitcher;

    public NotificationsQuickSettingsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mQsContainer = (AutoReinflateContainer) findViewById(R.id.qs_auto_reinflate_container);
        this.mQsContainer.addInflateListener(this);
        this.mStackScroller = findViewById(R.id.notification_stack_scroller);
        this.mStackScrollerMargin = ((LayoutParams) this.mStackScroller.getLayoutParams()).bottomMargin;
        ViewStub userSwitcher = (ViewStub) findViewById(R.id.keyguard_user_switcher);
        userSwitcher.setOnInflateListener(this);
        this.mUserSwitcher = userSwitcher;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadWidth(this.mQsContainer);
        reloadWidth(this.mStackScroller);
        updateStackScrollerBottomMargin(newConfig);
    }

    private void updateStackScrollerBottomMargin(Configuration newConfig) {
        if (this.mLastOrientation != newConfig.orientation) {
            if (2 == newConfig.orientation) {
                this.mStackScrollerMargin = getContext().getResources().getDimensionPixelSize(R.dimen.close_handle_underlap_land);
            } else {
                this.mStackScrollerMargin = getContext().getResources().getDimensionPixelSize(R.dimen.close_handle_underlap);
            }
        }
        this.mLastOrientation = newConfig.orientation;
    }

    private void reloadWidth(View view) {
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.width = getContext().getResources().getDimensionPixelSize(R.dimen.notification_panel_width);
        view.setLayoutParams(params);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(0, 0, 0, getContext().getResources().getDimensionPixelSize(R.dimen.hw_notification_margin_bottom));
        return insets;
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean statusBarVisible;
        boolean qsBottom = false;
        boolean userSwitcherVisible = this.mInflated && this.mUserSwitcher.getVisibility() == 0;
        if (this.mKeyguardStatusBar == null || this.mKeyguardStatusBar.getVisibility() != 0) {
            statusBarVisible = false;
        } else {
            statusBarVisible = true;
        }
        if (statusBarVisible) {
            return super.drawChild(canvas, child, drawingTime);
        }
        if (this.mQsExpanded && !this.mCustomizerAnimating) {
            qsBottom = true;
        }
        View stackQsTop = qsBottom ? this.mStackScroller : this.mQsContainer;
        View stackQsBottom = !qsBottom ? this.mStackScroller : this.mQsContainer;
        if (child == this.mQsContainer) {
            if (userSwitcherVisible && statusBarVisible) {
                stackQsBottom = this.mUserSwitcher;
            } else if (statusBarVisible) {
                stackQsBottom = this.mKeyguardStatusBar;
            } else if (userSwitcherVisible) {
                stackQsBottom = this.mUserSwitcher;
            }
            return super.drawChild(canvas, stackQsBottom, drawingTime);
        } else if (child == this.mStackScroller) {
            if (userSwitcherVisible && statusBarVisible) {
                stackQsTop = this.mKeyguardStatusBar;
            } else if (statusBarVisible || userSwitcherVisible) {
                stackQsTop = stackQsBottom;
            }
            return super.drawChild(canvas, stackQsTop, drawingTime);
        } else if (child == this.mUserSwitcher) {
            if (!(userSwitcherVisible && statusBarVisible)) {
                stackQsBottom = stackQsTop;
            }
            return super.drawChild(canvas, stackQsBottom, drawingTime);
        } else if (child == this.mKeyguardStatusBar) {
            return super.drawChild(canvas, stackQsTop, drawingTime);
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    public void onInflate(ViewStub stub, View inflated) {
        if (stub == this.mUserSwitcher) {
            this.mUserSwitcher = inflated;
            this.mInflated = true;
        }
    }

    public void onInflated(View v) {
        ((QSContainer) v).getCustomizer().setContainer(this);
    }

    public void onAllViewsRemoved() {
    }

    public void setQsExpanded(boolean expanded) {
        if (this.mQsExpanded != expanded) {
            HwLog.i("NotificationsQuickSettingsContainer", "setQsExpanded: " + expanded);
            this.mQsExpanded = expanded;
            invalidate();
        }
    }

    public void setCustomizerAnimating(boolean isAnimating) {
        if (this.mCustomizerAnimating != isAnimating) {
            this.mCustomizerAnimating = isAnimating;
            invalidate();
        }
    }

    public void setCustomizerShowing(boolean isShowing) {
        if (isShowing) {
            setPadding(0, 0, 0, 0);
            setBottomMargin(this.mStackScroller, 0);
            return;
        }
        setPadding(0, 0, 0, 0);
        setBottomMargin(this.mStackScroller, this.mStackScrollerMargin);
    }

    private void setBottomMargin(View v, int bottomMargin) {
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        params.bottomMargin = bottomMargin;
        v.setLayoutParams(params);
    }
}
