package com.android.systemui.stackdivider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;

public class DividerMenusView extends RelativeLayout {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    private boolean mAdded;
    private DividerView mDividerView;
    private final Handler mHandler = new Handler();
    private LayoutParams mParams;
    private ImageView mReversedView;
    private WindowManager mWindowMgr;
    Point screenDims = new Point();

    public DividerMenusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mReversedView = (ImageView) findViewById(R.id.docked_menu_change);
        this.mReversedView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BDReporter.c(DividerMenusView.this.getContext(), 333);
                DividerMenusView.this.setVisibility(8);
                int position = DividerMenusView.this.mDividerView.getCurrentPosition();
                DividerMenusView.this.mDividerView.startDragging(true, true);
                DividerMenusView.this.mDividerView.setStartPosition(position);
                DividerMenusView.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        DividerMenusView.this.mDividerView.updateDockSide();
                        DividerMenusView.this.mDividerView.getWindowManagerProxy().swapTasks();
                    }
                }, 100);
                DividerMenusView.this.mDividerView.stopDragging(position, DividerMenusView.this.mDividerView.getSnapAlgorithm().calculateSnapTarget(position, 0.0f, false), 336, 100, 0, Interpolators.FAST_OUT_SLOW_IN);
            }
        });
        ((ImageView) findViewById(R.id.docked_menu_close)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BDReporter.c(DividerMenusView.this.getContext(), 334);
                if (Recents.getSystemServices().isCurrentHomeActivity()) {
                    DividerMenusView.this.mDividerView.getWindowManagerProxy().dismissDockedStack();
                } else {
                    HwPhoneStatusBar.getInstance().toggleSplitScreenMode(271, 286);
                }
                DividerMenusView.this.setVisibility(8);
            }
        });
        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                DividerMenusView.this.setVisibility(8);
                return false;
            }
        });
        this.mWindowMgr = (WindowManager) this.mContext.getSystemService("window");
        Point screenDims = new Point();
        this.mWindowMgr.getDefaultDisplay().getRealSize(screenDims);
        this.mParams = new LayoutParams(screenDims.x, screenDims.y, 2034, 262656, -3);
        adjustParam();
    }

    public void adjustParam() {
        Resources res = getResources();
        boolean isScreenLarge = isScreenLarge();
        if (res.getConfiguration().orientation == 2) {
            if (isScreenLarge) {
                this.mParams.y = res.getInteger(R.integer.divider_menus_view_offset_y);
            } else {
                this.mParams.y = (-res.getDimensionPixelSize(R.dimen.status_bar_height)) / 2;
            }
        }
        this.mParams.setTitle("DividerMenusView-" + isScreenLarge);
    }

    public void setDividerView(DividerView dividerView) {
        this.mDividerView = dividerView;
    }

    public void showMenusView() {
        if (this.mReversedView != null) {
            if (canShowReversedView()) {
                this.mReversedView.setVisibility(0);
            } else {
                this.mReversedView.setVisibility(8);
            }
        }
        setVisibility(0);
    }

    private boolean canShowReversedView() {
        SystemServicesProxy ssp = Recents.getSystemServices();
        if (!(this.mDividerView == null || this.mDividerView.getDockSide() == -1 || ssp.isRecentsActivityVisible())) {
            if (!ssp.isCurrentHomeActivity()) {
                return true;
            }
        }
        return false;
    }

    public void addViewToWindow() {
        try {
            if (!this.mAdded && this.mWindowMgr != null) {
                this.mWindowMgr.addView(this, this.mParams);
                this.mAdded = true;
            }
        } catch (RuntimeException e) {
            HwLog.e("DividerMenusView", " from addViewToWindow() " + e.getMessage());
        }
    }

    public void removeViewToWindow() {
        try {
            if (this.mAdded && this.mWindowMgr != null) {
                this.mWindowMgr.removeView(this);
                this.mAdded = false;
            }
        } catch (RuntimeException e) {
            HwLog.e("DividerMenusView", " from removeViewToWindow() " + e.getMessage());
        }
    }

    public boolean isScreenLarge() {
        int shortSize;
        Display display = this.mWindowMgr.getDefaultDisplay();
        display.getMetrics(this.displayMetrics);
        display.getRealSize(this.screenDims);
        if (this.screenDims.x < this.screenDims.y) {
            shortSize = this.screenDims.x;
        } else {
            shortSize = this.screenDims.y;
        }
        if (((float) shortSize) / this.displayMetrics.density >= 600.0f) {
            return true;
        }
        return false;
    }
}
