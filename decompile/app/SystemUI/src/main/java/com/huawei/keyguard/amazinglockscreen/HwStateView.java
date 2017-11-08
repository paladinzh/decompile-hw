package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwOnTriggerCallback;
import com.huawei.keyguard.HwUnlockConstants$LockScreenSubType;
import com.huawei.keyguard.HwUnlockConstants$StateViewSubType;
import com.huawei.keyguard.theme.HwThemeParser;
import org.w3c.dom.NamedNodeMap;

public class HwStateView extends HwDynamicView {
    private boolean mIsClickedKey;
    HwUnlockInterface$HwOnTriggerCallback mOnTriggerCallback;
    private boolean mPressAreaFlag;
    private Point mTouchPoint;

    public HwStateView(Context context, NamedNodeMap attrs) {
        super(context, attrs);
        this.mIsClickedKey = false;
        this.mOnTriggerCallback = null;
        this.mPressAreaFlag = false;
        this.mType = HwUnlockConstants$LockScreenSubType.STATE;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            int i;
            int x = (int) event.getX();
            int y = (int) event.getY();
            this.mTouchPoint = new Point(x, y);
            int visibleIndex = -1;
            int childSize = getChildCount();
            for (i = 0; i < childSize; i++) {
                HwStateViewSub view = (HwStateViewSub) getChildAt(i);
                if (HwUnlockConstants$StateViewSubType.PRESS == view.getStateType() && view.isInPressArea(x, y)) {
                    this.mPressAreaFlag = true;
                    visibleIndex = i;
                    HwPropertyManager manager = HwPropertyManager.getInstance();
                    manager.updatePosition(x, y);
                    manager.updatePressState(true);
                    view.setVisibility(0);
                    view.setUnlocker(true);
                    break;
                }
            }
            if (-1 != visibleIndex) {
                for (i = 0; i < childSize; i++) {
                    if (i != visibleIndex) {
                        getChildAt(i).setVisibility(4);
                    }
                }
                this.mIsClickedKey = true;
                if (this.mOnTriggerCallback != null) {
                    this.mOnTriggerCallback.setClickKey(this.mIsClickedKey);
                }
                return true;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & event.getActionMasked()) {
            case 1:
            case 6:
                dealActionUpEvent(event);
                break;
            case 2:
                if (this.mIsClickedKey) {
                    HwViewProperty.setUP(false);
                    int x = (int) event.getX(0);
                    int y = (int) event.getY(0);
                    HwPropertyManager manager = HwPropertyManager.getInstance();
                    manager.updatePosition(x, y);
                    manager.updateMove(x - this.mTouchPoint.x, y - this.mTouchPoint.y);
                    break;
                }
                break;
            case 3:
                removeToOrigin(event);
                break;
        }
        if (!this.mPressAreaFlag && HwThemeParser.getInstance().getSlideInAmazeFlag()) {
            return false;
        }
        this.mPressAreaFlag = false;
        return true;
    }

    private void dealActionUpEvent(MotionEvent event) {
        if (event.getActionIndex() == 0) {
            HwViewProperty.setUP(true);
            HwPropertyManager.getInstance().updatePosition((int) event.getX(0), (int) event.getY(0));
            HwViewProperty.setUP(false);
            removeToOrigin(event);
        }
    }

    private void removeToOrigin(MotionEvent event) {
        HwPropertyManager.getInstance().updatePressState(false);
        if (HwPropertyManager.getInstance().getTriggerFlag()) {
            HwPropertyManager.getInstance().updateMove((int) event.getX(), (int) event.getY());
        } else {
            HwPropertyManager.getInstance().updateMove(0, 0);
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                HwStateViewSub view = (HwStateViewSub) getChildAt(i);
                if (HwUnlockConstants$StateViewSubType.NORMAL == view.getStateType()) {
                    view.setVisibility(0);
                } else if (HwUnlockConstants$StateViewSubType.PRESS == view.getStateType()) {
                    view.setVisibility(4);
                    view.setUnlocker(false);
                }
            }
        }
        this.mIsClickedKey = false;
        this.mOnTriggerCallback.setClickKey(this.mIsClickedKey);
    }

    protected void parserSpecialAttributes(NamedNodeMap attrs) {
    }

    public void registerOnTriggerCallback(HwUnlockInterface$HwOnTriggerCallback onTriggerCallback) {
        this.mOnTriggerCallback = onTriggerCallback;
    }
}
