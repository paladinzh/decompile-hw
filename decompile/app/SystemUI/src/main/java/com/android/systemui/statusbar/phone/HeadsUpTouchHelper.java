package com.android.systemui.statusbar.phone;

import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.utils.analyze.BDReporter;

public class HeadsUpTouchHelper {
    private boolean mCollapseSnoozes;
    private HeadsUpManager mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private NotificationPanelView mPanel;
    private ExpandableNotificationRow mPickedChild;
    private NotificationStackScrollLayout mStackScroller;
    private float mTouchSlop;
    private boolean mTouchingHeadsUpView;
    private boolean mTrackingHeadsUp;
    private int mTrackingPointer;

    public HeadsUpTouchHelper(HeadsUpManager headsUpManager, NotificationStackScrollLayout stackScroller, NotificationPanelView notificationPanelView) {
        this.mHeadsUpManager = headsUpManager;
        this.mStackScroller = stackScroller;
        this.mPanel = notificationPanelView;
        this.mTouchSlop = (float) ViewConfiguration.get(stackScroller.getContext()).getScaledTouchSlop();
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        int newIndex = 1;
        boolean z = false;
        if (!this.mTouchingHeadsUpView && event.getActionMasked() != 0) {
            return false;
        }
        int pointerIndex = event.findPointerIndex(this.mTrackingPointer);
        if (pointerIndex < 0) {
            pointerIndex = 0;
            this.mTrackingPointer = event.getPointerId(0);
        }
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        switch (event.getActionMasked()) {
            case 0:
                this.mInitialTouchY = y;
                this.mInitialTouchX = x;
                setTrackingHeadsUp(false);
                ExpandableView child = this.mStackScroller.getChildAtRawPosition(x, y);
                this.mTouchingHeadsUpView = false;
                if (child instanceof ExpandableNotificationRow) {
                    boolean z2;
                    this.mPickedChild = (ExpandableNotificationRow) child;
                    if (this.mStackScroller.isExpanded() || !this.mPickedChild.isHeadsUp()) {
                        z2 = false;
                    } else {
                        z2 = this.mPickedChild.isPinned();
                    }
                    this.mTouchingHeadsUpView = z2;
                    break;
                }
                break;
            case 1:
            case 3:
                if (this.mPickedChild == null || !this.mTouchingHeadsUpView || !this.mHeadsUpManager.shouldSwallowClick(this.mPickedChild.getStatusBarNotification().getKey())) {
                    endMotion();
                    break;
                }
                endMotion();
                return true;
            case 2:
                float h = y - this.mInitialTouchY;
                if (this.mTouchingHeadsUpView && Math.abs(h) > this.mTouchSlop && Math.abs(h) > Math.abs(x - this.mInitialTouchX)) {
                    setTrackingHeadsUp(true);
                    if (h < 0.0f) {
                        z = true;
                    }
                    this.mCollapseSnoozes = z;
                    if (this.mCollapseSnoozes) {
                        BDReporter.e(this.mStackScroller.getContext(), 350, "closed:true, pkg :" + (this.mPickedChild.getStatusBarNotification() != null ? this.mPickedChild.getStatusBarNotification().getPackageName() : "null"));
                    } else {
                        BDReporter.e(this.mStackScroller.getContext(), 351, "closed:false, pkg :" + (this.mPickedChild.getStatusBarNotification() != null ? this.mPickedChild.getStatusBarNotification().getPackageName() : "null"));
                    }
                    this.mInitialTouchX = x;
                    this.mInitialTouchY = y;
                    int expandedHeight = this.mPickedChild.getActualHeight();
                    this.mPanel.setPanelScrimMinFraction(((float) expandedHeight) / ((float) this.mPanel.getMaxPanelHeight()));
                    this.mPanel.startExpandMotion(x, y, true, (float) expandedHeight);
                    this.mHeadsUpManager.unpinAll();
                    this.mPanel.clearNotificationEffects();
                    return true;
                }
            case 6:
                int upPointer = event.getPointerId(event.getActionIndex());
                if (this.mTrackingPointer == upPointer) {
                    if (event.getPointerId(0) != upPointer) {
                        newIndex = 0;
                    }
                    this.mTrackingPointer = event.getPointerId(newIndex);
                    this.mInitialTouchX = event.getX(newIndex);
                    this.mInitialTouchY = event.getY(newIndex);
                    break;
                }
                break;
        }
        return false;
    }

    private void setTrackingHeadsUp(boolean tracking) {
        this.mTrackingHeadsUp = tracking;
        this.mHeadsUpManager.setTrackingHeadsUp(tracking);
        this.mPanel.setTrackingHeadsUp(tracking);
    }

    public void notifyFling(boolean collapse) {
        if (collapse && this.mCollapseSnoozes) {
            this.mHeadsUpManager.snooze();
        }
        this.mCollapseSnoozes = false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mTrackingHeadsUp) {
            return false;
        }
        switch (event.getActionMasked()) {
            case 1:
            case 3:
                endMotion();
                setTrackingHeadsUp(false);
                break;
        }
        return true;
    }

    private void endMotion() {
        this.mTrackingPointer = -1;
        this.mPickedChild = null;
        this.mTouchingHeadsUpView = false;
    }
}
