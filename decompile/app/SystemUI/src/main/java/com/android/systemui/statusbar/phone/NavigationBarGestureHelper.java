package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.internal.policy.DividerSnapAlgorithm.SnapTarget;
import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerView;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

public class NavigationBarGestureHelper extends SimpleOnGestureListener implements Tunable {
    private Context mContext;
    private Divider mDivider;
    private boolean mDockWindowEnabled;
    private boolean mDockWindowTouchSlopExceeded;
    private boolean mDownOnRecents;
    private int mDragMode;
    private boolean mIsRTL;
    private boolean mIsVertical;
    private final int mMinFlingVelocity;
    private NavigationBarView mNavigationBarView;
    private RecentsComponent mRecentsComponent;
    private final int mScrollTouchSlop;
    private final GestureDetector mTaskSwitcherDetector;
    private int mTouchDownX;
    private int mTouchDownY;
    private VelocityTracker mVelocityTracker;

    public NavigationBarGestureHelper(Context context) {
        this.mContext = context;
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.mScrollTouchSlop = context.getResources().getDimensionPixelSize(R.dimen.navigation_bar_min_swipe_distance);
        this.mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mTaskSwitcherDetector = new GestureDetector(context, this);
        TunerService.get(context).addTunable((Tunable) this, "overview_nav_bar_gesture");
    }

    public void setComponents(RecentsComponent recentsComponent, Divider divider, NavigationBarView navigationBarView) {
        this.mRecentsComponent = recentsComponent;
        this.mDivider = divider;
        this.mNavigationBarView = navigationBarView;
    }

    public void setBarState(boolean isVertical, boolean isRTL) {
        this.mIsVertical = isVertical;
        this.mIsRTL = isRTL;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean z = false;
        this.mTaskSwitcherDetector.onTouchEvent(event);
        switch (event.getAction() & 255) {
            case 0:
                this.mTouchDownX = (int) event.getX();
                this.mTouchDownY = (int) event.getY();
                break;
            case 2:
                int y = (int) event.getY();
                int xDiff = Math.abs(((int) event.getX()) - this.mTouchDownX);
                int yDiff = Math.abs(y - this.mTouchDownY);
                boolean exceededTouchSlop = !this.mIsVertical ? xDiff > this.mScrollTouchSlop && xDiff > yDiff : yDiff > this.mScrollTouchSlop && yDiff > xDiff;
                if (exceededTouchSlop) {
                    return true;
                }
                break;
        }
        if (this.mDockWindowEnabled) {
            z = interceptDockWindowEvent(event);
        }
        return z;
    }

    private boolean interceptDockWindowEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                handleDragActionDownEvent(event);
                break;
            case 1:
            case 3:
                handleDragActionUpEvent(event);
                break;
            case 2:
                return handleDragActionMoveEvent(event);
        }
        return false;
    }

    private boolean handleDockWindowEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                handleDragActionDownEvent(event);
                break;
            case 1:
            case 3:
                handleDragActionUpEvent(event);
                break;
            case 2:
                handleDragActionMoveEvent(event);
                break;
        }
        return true;
    }

    private void handleDragActionDownEvent(MotionEvent event) {
        boolean z = false;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mVelocityTracker.addMovement(event);
        this.mDockWindowTouchSlopExceeded = false;
        this.mTouchDownX = (int) event.getX();
        this.mTouchDownY = (int) event.getY();
        if (this.mNavigationBarView != null) {
            View recentsButton = this.mNavigationBarView.getRecentsButton().getCurrentView();
            if (recentsButton != null) {
                if (this.mTouchDownX >= recentsButton.getLeft() && this.mTouchDownX <= recentsButton.getRight() && this.mTouchDownY >= recentsButton.getTop() && this.mTouchDownY <= recentsButton.getBottom()) {
                    z = true;
                }
                this.mDownOnRecents = z;
                return;
            }
            this.mDownOnRecents = false;
        }
    }

    private boolean handleDragActionMoveEvent(MotionEvent event) {
        this.mVelocityTracker.addMovement(event);
        int y = (int) event.getY();
        int xDiff = Math.abs(((int) event.getX()) - this.mTouchDownX);
        int yDiff = Math.abs(y - this.mTouchDownY);
        if (this.mDivider == null || this.mRecentsComponent == null) {
            return false;
        }
        if (!this.mDockWindowTouchSlopExceeded) {
            boolean touchSlopExceeded = !this.mIsVertical ? yDiff > this.mScrollTouchSlop && yDiff > xDiff : xDiff > this.mScrollTouchSlop && xDiff > yDiff;
            if (this.mDownOnRecents && touchSlopExceeded && this.mDivider.getView().getWindowManagerProxy().getDockSide() == -1) {
                Rect initialBounds = null;
                int dragMode = calculateDragMode();
                int createMode = 0;
                if (dragMode == 1) {
                    int rawX;
                    int i;
                    initialBounds = new Rect();
                    DividerView view = this.mDivider.getView();
                    if (this.mIsVertical) {
                        rawX = (int) event.getRawX();
                    } else {
                        rawX = (int) event.getRawY();
                    }
                    if (this.mDivider.getView().isHorizontalDivision()) {
                        i = 2;
                    } else {
                        i = 1;
                    }
                    view.calculateBoundsForPosition(rawX, i, initialBounds);
                } else if (dragMode == 0 && this.mTouchDownX < this.mContext.getResources().getDisplayMetrics().widthPixels / 2) {
                    createMode = 1;
                }
                if (this.mRecentsComponent.dockTopTask(dragMode, createMode, initialBounds, 272)) {
                    this.mDragMode = dragMode;
                    if (this.mDragMode == 1) {
                        this.mDivider.getView().startDragging(false, true);
                    }
                    this.mDockWindowTouchSlopExceeded = true;
                    return true;
                }
            }
        } else if (this.mDragMode == 1) {
            int position = (int) (!this.mIsVertical ? event.getRawY() : event.getRawX());
            SnapTarget snapTarget = this.mDivider.getView().getSnapAlgorithm().calculateSnapTarget(position, 0.0f, false);
            this.mDivider.getView().resizeStack(position, snapTarget.position, snapTarget);
        } else if (this.mDragMode == 0) {
            this.mRecentsComponent.onDraggingInRecents(event.getRawY());
        }
        return false;
    }

    private void handleDragActionUpEvent(MotionEvent event) {
        this.mVelocityTracker.addMovement(event);
        this.mVelocityTracker.computeCurrentVelocity(1000);
        if (!(!this.mDockWindowTouchSlopExceeded || this.mDivider == null || this.mRecentsComponent == null)) {
            if (this.mDragMode == 1) {
                int rawX;
                float xVelocity;
                DividerView view = this.mDivider.getView();
                if (this.mIsVertical) {
                    rawX = (int) event.getRawX();
                } else {
                    rawX = (int) event.getRawY();
                }
                if (this.mIsVertical) {
                    xVelocity = this.mVelocityTracker.getXVelocity();
                } else {
                    xVelocity = this.mVelocityTracker.getYVelocity();
                }
                view.stopDragging(rawX, xVelocity, true, false);
            } else if (this.mDragMode == 0) {
                this.mRecentsComponent.onDraggingInRecentsEnded(this.mVelocityTracker.getYVelocity());
            }
        }
        this.mVelocityTracker.recycle();
        this.mVelocityTracker = null;
    }

    private int calculateDragMode() {
        if (this.mIsVertical && !this.mDivider.getView().isHorizontalDivision()) {
            return 1;
        }
        if (this.mIsVertical || !this.mDivider.getView().isHorizontalDivision()) {
            return 0;
        }
        return 1;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = this.mTaskSwitcherDetector.onTouchEvent(event);
        if (this.mDockWindowEnabled) {
            return result | handleDockWindowEvent(event);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean showNext;
        boolean isValidFling = false;
        float absVelX = Math.abs(velocityX);
        float absVelY = Math.abs(velocityY);
        if (absVelX <= ((float) this.mMinFlingVelocity) || !this.mIsVertical) {
            if (absVelX > absVelY) {
            }
            if (isValidFling && this.mRecentsComponent != null) {
                if (this.mIsRTL) {
                    if (this.mIsVertical) {
                        if (velocityX < 0.0f) {
                        }
                        showNext = false;
                    }
                    showNext = true;
                } else {
                    if (this.mIsVertical) {
                        if (velocityX > 0.0f) {
                        }
                        showNext = false;
                    }
                    showNext = true;
                }
                if (showNext) {
                    this.mRecentsComponent.showPrevAffiliatedTask();
                } else {
                    this.mRecentsComponent.showNextAffiliatedTask();
                }
            }
            return true;
        }
        isValidFling = true;
        if (this.mIsRTL) {
            if (this.mIsVertical) {
                if (velocityX > 0.0f) {
                }
                showNext = false;
            }
            showNext = true;
        } else {
            if (this.mIsVertical) {
                if (velocityX < 0.0f) {
                }
                showNext = false;
            }
            showNext = true;
        }
        if (showNext) {
            this.mRecentsComponent.showPrevAffiliatedTask();
        } else {
            this.mRecentsComponent.showNextAffiliatedTask();
        }
        return true;
    }

    public void onTuningChanged(String key, String newValue) {
        boolean z = false;
        if (key.equals("overview_nav_bar_gesture")) {
            if (!(newValue == null || Integer.parseInt(newValue) == 0)) {
                z = true;
            }
            this.mDockWindowEnabled = z;
        }
    }
}
