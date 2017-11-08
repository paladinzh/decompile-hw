package huawei.com.android.internal.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Scroller;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback;
import huawei.com.android.internal.app.HwActionBarImpl;
import huawei.com.android.internal.app.HwActionBarImpl.InnerOnStageChangedListener;
import java.util.ArrayList;

public class HwActionBarOverlayLayout extends ActionBarOverlayLayout {
    private static final int ALPHA_ANIMATION_DELAY = 133;
    private static final int ALPHA_ANIMATION_DURATION = 167;
    private static final float ALPHA_ANIMATION_START = 0.3325f;
    private static final int FLING_THRESHOULD = 2200;
    private static final int INVALID_POINTER = -1;
    private static final int SCROLL_ANIMATION_DURATION = 400;
    private static final int STAGE_ONE = 1;
    private static final int STAGE_RESET = 0;
    private static final int STAGE_TWO = 2;
    private static final String TAG = "HwActionBarOverlayLayout";
    private int mActivePointerId = -1;
    private InnerOnStageChangedListener mCallback;
    private boolean mCanDragFromContent = false;
    private int mCustViewHeight;
    private HwCustomPanel mCustomPanel;
    private int mDragStage = 0;
    private boolean mDrawerOpend;
    private int mFirstCustViewHeight = 0;
    private GestureDetector mGestureDetector;
    private boolean mHwDrawerFeature;
    private int mHwOverlayActionBar;
    private boolean mIsAnimationEnable = true;
    private boolean mIsBeingDragged;
    private boolean mIsDraggable = false;
    private boolean mIsStage2Changed = false;
    private boolean mIsStageChanged = true;
    private boolean mIsStartStageChanged = false;
    private int mLastMotionY;
    private boolean mLazyMode = false;
    private Scroller mScroller;
    private int mSecondCustViewHeight = 0;
    private InnerOnStageChangedListener mStartCallback;
    private final ArrayList<View> mStillViews = new ArrayList();
    private int mTouchSlop = 0;
    private int mUiOptions;

    private class SimpleGestureListener extends SimpleOnGestureListener {
        private SimpleGestureListener() {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean z = false;
            float velocity = velocityY;
            if (Math.abs(velocityY) <= 2200.0f) {
                return false;
            }
            HwActionBarOverlayLayout hwActionBarOverlayLayout = HwActionBarOverlayLayout.this;
            int -get0 = HwActionBarOverlayLayout.this.mScrollY;
            if (Math.signum(velocityY) != -1.0f) {
                z = true;
            }
            hwActionBarOverlayLayout.startFlingAnimation(-get0, z);
            return true;
        }
    }

    public HwActionBarOverlayLayout(Context context) {
        super(context);
    }

    public HwActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z;
        initCustomPanel();
        if (this.mIsDraggable) {
            z = this.mIsAnimationEnable;
        } else {
            z = false;
        }
        if (!z) {
            return false;
        }
        int action = ev.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        switch (action & 255) {
            case 0:
                this.mLastMotionY = (int) ev.getY();
                this.mActivePointerId = ev.getPointerId(0);
                this.mIsBeingDragged = false;
                break;
            case 1:
            case 3:
                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                break;
            case 2:
                int activePointerId = this.mActivePointerId;
                if (activePointerId != -1) {
                    int pointerIndex = ev.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        int y = (int) ev.getY(pointerIndex);
                        if (inActionBarTop(y)) {
                            if (Math.abs(y - this.mLastMotionY) > this.mTouchSlop) {
                                this.mIsBeingDragged = true;
                                this.mLastMotionY = y;
                                break;
                            }
                        }
                        this.mIsBeingDragged = false;
                        break;
                    }
                    Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                    break;
                }
                break;
            default:
                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                break;
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z;
        initCustomPanel();
        if (this.mScroller == null) {
            initScroller();
        }
        if (this.mIsDraggable) {
            z = this.mIsAnimationEnable;
        } else {
            z = false;
        }
        if (!z) {
            return false;
        }
        if (!this.mScroller.isFinished()) {
            return true;
        }
        if (inActionBarTop((int) ev.getY()) || inCustPanel((int) ev.getY())) {
            if (this.mGestureDetector == null) {
                this.mGestureDetector = new GestureDetector(this.mContext, new SimpleGestureListener());
            }
            if (this.mGestureDetector.onTouchEvent(ev)) {
                return true;
            }
        }
        switch (ev.getAction() & 255) {
            case 0:
                this.mLastMotionY = (int) ev.getY();
                this.mActivePointerId = ev.getPointerId(0);
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    break;
                }
                break;
            case 1:
            case 3:
                if (this.mIsBeingDragged) {
                    this.mActivePointerId = -1;
                    startAnimation(this.mScrollY);
                    this.mIsBeingDragged = false;
                    break;
                }
                break;
            case 2:
                int activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex != -1) {
                    int y = (int) ev.getY(activePointerIndex);
                    int deltaY = this.mLastMotionY - y;
                    if (!this.mIsBeingDragged && Math.abs(deltaY) > this.mTouchSlop) {
                        this.mIsBeingDragged = true;
                        deltaY = deltaY > 0 ? deltaY - this.mTouchSlop : deltaY + this.mTouchSlop;
                    }
                    if (this.mIsBeingDragged) {
                        this.mLastMotionY = y;
                        int clampY = this.mScrollY + deltaY;
                        if (clampY >= 0) {
                            deltaY -= clampY + 0;
                        } else if (clampY <= (-this.mCustViewHeight)) {
                            deltaY -= clampY - (-this.mCustViewHeight);
                        }
                        moveBy(deltaY);
                        break;
                    }
                }
                Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                break;
                break;
            case 5:
                int index = ev.getActionIndex();
                this.mLastMotionY = (int) ev.getY(index);
                this.mActivePointerId = ev.getPointerId(index);
                break;
            case 6:
                onSecondaryPointerUp(ev);
                int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (pointerIndex >= 0 && pointerIndex < ev.getPointerCount()) {
                    this.mLastMotionY = (int) ev.getY(pointerIndex);
                    break;
                }
            default:
                this.mIsBeingDragged = false;
                break;
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int newPointerIndex = 0;
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            if (pointerIndex == 0) {
                newPointerIndex = 1;
            }
            this.mLastMotionY = (int) ev.getY(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        MotionEvent ev = MotionEvent.obtain(event);
        initCustomPanel();
        if (inCustPanel((int) event.getY()) && this.mCustomPanel != null && this.mCustomPanel.dispatchTouchEvent(ev)) {
            ev.recycle();
            return true;
        }
        ev.recycle();
        return super.dispatchTouchEvent(event);
    }

    private void startFlingAnimation(int startY, boolean isDown) {
        int finalPos = 0;
        if (isCustomPanelEmpty()) {
            if (isDown) {
                finalPos = -this.mCustViewHeight;
            } else {
                finalPos = 0;
            }
        } else if (Math.abs(startY) <= this.mFirstCustViewHeight) {
            if (isDown) {
                finalPos = -this.mFirstCustViewHeight;
            } else {
                finalPos = 0;
            }
        } else if (Math.abs(startY) <= this.mCustViewHeight) {
            if (isDown) {
                finalPos = -this.mCustViewHeight;
            } else {
                finalPos = -this.mFirstCustViewHeight;
            }
        }
        smoothScrollTo(startY, finalPos, SCROLL_ANIMATION_DURATION);
    }

    private boolean inCustPanel(int y) {
        boolean rtn = false;
        View actionBarTop = getActionBarContainer();
        if (actionBarTop == null) {
            return false;
        }
        if (y <= actionBarTop.getTop() - this.mScrollY && y >= actionBarTop.getTop()) {
            rtn = true;
        }
        return rtn;
    }

    public void setActionBarDraggable(boolean isDraggable) {
        this.mIsDraggable = isDraggable;
    }

    public void setLazyMode(boolean isLazyMode) {
        this.mLazyMode = isLazyMode;
    }

    private boolean isCustomPanelEmpty() {
        initCustomPanel();
        if (this.mFirstCustViewHeight == 0) {
            return true;
        }
        return false;
    }

    private void initScroller() {
        if (this.mScroller == null) {
            this.mScroller = new Scroller(this.mContext, AnimationUtils.loadInterpolator(this.mContext, 34209806));
        }
    }

    private void initCustomPanel() {
        if (this.mCustomPanel == null) {
            ActionBarVisibilityCallback abvc = getActionBar();
            HwActionBarImpl actionBar = null;
            if (abvc instanceof HwActionBarImpl) {
                actionBar = (HwActionBarImpl) abvc;
            }
            if (actionBar != null) {
                this.mCustomPanel = actionBar.getCustomPanel();
            } else {
                Log.w(TAG, "CustomPanel is not initialized correctly, CustViewHeight = " + this.mCustViewHeight);
                return;
            }
        }
        this.mFirstCustViewHeight = 0;
        this.mSecondCustViewHeight = 0;
        this.mCustViewHeight = 0;
        if (this.mCustomPanel != null) {
            View firstChild = this.mCustomPanel.getChildAt(0);
            if (firstChild != null) {
                this.mFirstCustViewHeight = firstChild.getHeight();
                View secondChild = this.mCustomPanel.getChildAt(1);
                if (secondChild != null) {
                    this.mSecondCustViewHeight = secondChild.getHeight();
                }
                this.mCustViewHeight = this.mFirstCustViewHeight + this.mSecondCustViewHeight;
            } else {
                this.mCustViewHeight = this.mCustomPanel.getHeight() - getActionBarHeight();
            }
            if (HwWidgetUtils.isActionbarBackgroundThemed(getContext())) {
                int[] cpLoc = new int[2];
                int[] ctLoc = new int[2];
                this.mCustomPanel.getLocationOnScreen(cpLoc);
                View actionBarTop = getActionBarContainer();
                if (actionBarTop != null) {
                    actionBarTop.getLocationOnScreen(ctLoc);
                }
                if (cpLoc[1] < ctLoc[1]) {
                    this.mCustomPanel.setBackgroundDrawable(getContext().getResources().getDrawable(33751069));
                } else {
                    this.mCustomPanel.setBackgroundDrawable(null);
                }
            }
        }
    }

    private boolean inActionBarTop(int y) {
        boolean rtn = false;
        View actionBarTop = getActionBarContainer();
        if (actionBarTop == null) {
            return false;
        }
        int scrollY = this.mScrollY;
        if (y >= actionBarTop.getTop() - scrollY && y <= actionBarTop.getBottom() - scrollY) {
            rtn = true;
        }
        return rtn;
    }

    public void setCanDragFromContent(boolean canDragFromContent) {
        this.mCanDragFromContent = canDragFromContent;
    }

    private void smoothScrollTo(int startY, int y, int duration) {
        this.mScroller.startScroll(0, startY, 0, y - startY, duration);
        postInvalidateOnAnimation();
    }

    public void computeScroll() {
        if (this.mScroller == null) {
            initScroller();
        }
        if (this.mScroller.computeScrollOffset()) {
            custScrollTo(this.mScroller.getCurrY());
            postInvalidateOnAnimation();
        }
    }

    private void moveBy(int deltaY) {
        custScrollTo(this.mScrollY + deltaY);
        invalidate();
    }

    private void custScrollTo(int y) {
        View actionBarBottom = getActionBarBottom();
        if (actionBarBottom != null) {
            actionBarBottom.setTranslationY((float) y);
        }
        int size = this.mStillViews.size();
        for (int i = 0; i < size; i++) {
            View v = (View) this.mStillViews.get(i);
            if (v != null) {
                v.setTranslationY((float) y);
            }
        }
        scrollTo(0, y);
        if (this.mCustomPanel != null) {
            this.mCustomPanel.setClipY(y - getActionBarHeight());
        }
        View childAt = this.mCustomPanel != null ? this.mCustomPanel.getChildAt(0) : null;
        View childAt2 = this.mCustomPanel != null ? this.mCustomPanel.getChildAt(1) : null;
        if (Math.abs(y) == 0) {
            this.mDragStage = 0;
            if (childAt != null) {
                childAt.setAlpha(0.0f);
            }
            if (childAt2 != null) {
                childAt2.setAlpha(0.0f);
            }
            if (this.mStartCallback != null && this.mIsStartStageChanged) {
                this.mIsStartStageChanged = false;
                this.mStartCallback.onExitNextStage();
            }
        } else if (Math.abs(y) <= this.mFirstCustViewHeight) {
            this.mDragStage = 1;
            if (childAt2 != null) {
                childAt2.setAlpha(0.0f);
            }
            if (this.mCallback != null && this.mIsStageChanged) {
                this.mCallback.onExitNextStage();
                this.mIsStageChanged = false;
                this.mIsStage2Changed = true;
            }
            if (!(this.mStartCallback == null || Math.abs(y) < 10 || this.mIsStartStageChanged)) {
                this.mIsStartStageChanged = true;
                this.mStartCallback.onEnterNextStage();
            }
            float firstStageProgress = (((float) (-y)) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) this.mFirstCustViewHeight);
            if (childAt != null) {
                childAt.setAlpha(getAlphaInterpolation(firstStageProgress));
            }
        } else if (Math.abs(y) <= this.mCustViewHeight) {
            this.mDragStage = 2;
            if (childAt != null) {
                childAt.setAlpha(HwFragmentMenuItemView.ALPHA_NORMAL);
            }
            if (this.mCallback != null && this.mIsStage2Changed) {
                this.mCallback.onEnterNextStage();
                this.mIsStageChanged = true;
                this.mIsStage2Changed = false;
            }
            float secondStageProgress = (((float) ((-y) - this.mFirstCustViewHeight)) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) this.mSecondCustViewHeight);
            if (childAt2 != null) {
                childAt2.setAlpha(getAlphaInterpolation(secondStageProgress));
            }
        }
    }

    private float getAlphaInterpolation(float input) {
        float progress = input;
        if (input < ALPHA_ANIMATION_START) {
            progress = 0.0f;
        } else {
            progress = ((input - ALPHA_ANIMATION_START) * 400.0f) / 167.0f;
        }
        if (progress > HwFragmentMenuItemView.ALPHA_NORMAL) {
            return HwFragmentMenuItemView.ALPHA_NORMAL;
        }
        return progress;
    }

    private void startAnimation(int startY) {
        int finalPos = 0;
        if (isCustomPanelEmpty()) {
            if (this.mLazyMode) {
                finalPos = -this.mCustViewHeight;
                this.mLazyMode = false;
            } else if (this.mCanDragFromContent) {
                finalPos = 0;
                this.mCanDragFromContent = false;
            } else if (this.mCustViewHeight != 0) {
                finalPos = (((float) Math.abs(this.mScrollY)) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) this.mCustViewHeight) > 0.5f ? -this.mCustViewHeight : 0;
            }
        } else if (this.mLazyMode) {
            finalPos = -this.mFirstCustViewHeight;
            this.mLazyMode = false;
        } else if (this.mCanDragFromContent) {
            finalPos = 0;
            this.mCanDragFromContent = false;
        } else if (Math.abs(startY) <= this.mFirstCustViewHeight) {
            finalPos = (((float) Math.abs(this.mScrollY)) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) this.mFirstCustViewHeight) > 0.5f ? -this.mFirstCustViewHeight : 0;
        } else if (Math.abs(startY) <= this.mCustViewHeight) {
            finalPos = (((float) (Math.abs(this.mScrollY) - this.mFirstCustViewHeight)) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) this.mSecondCustViewHeight) > 0.5f ? -this.mCustViewHeight : -this.mFirstCustViewHeight;
        }
        smoothScrollTo(startY, finalPos, SCROLL_ANIMATION_DURATION);
    }

    public void pullBackAnimation() {
        if (this.mScrollY == (-this.mCustViewHeight)) {
            smoothScrollTo(-this.mCustViewHeight, 0, SCROLL_ANIMATION_DURATION);
        }
    }

    public void resetDragAnimation() {
        if (this.mIsDraggable) {
            initCustomPanel();
            if (this.mScroller == null) {
                initScroller();
            }
            if (!this.mScroller.isFinished()) {
                this.mScroller.forceFinished(true);
            }
            custScrollTo(0);
            invalidate();
        }
    }

    public void startStageAnimation(int stage, boolean isScrollDown) {
        if (!isCustomPanelEmpty()) {
            switch (stage) {
                case 1:
                    if (!isScrollDown) {
                        if (this.mScrollY == (-this.mFirstCustViewHeight)) {
                            smoothScrollTo(-this.mFirstCustViewHeight, 0, SCROLL_ANIMATION_DURATION);
                            break;
                        }
                    } else if (this.mScrollY == 0) {
                        smoothScrollTo(0, -this.mFirstCustViewHeight, SCROLL_ANIMATION_DURATION);
                        break;
                    }
                    break;
                case 2:
                    if (!isScrollDown) {
                        if (this.mScrollY == (-this.mCustViewHeight)) {
                            smoothScrollTo(-this.mCustViewHeight, -this.mFirstCustViewHeight, SCROLL_ANIMATION_DURATION);
                            break;
                        }
                    } else if (this.mScrollY == (-this.mFirstCustViewHeight)) {
                        smoothScrollTo(-this.mFirstCustViewHeight, -this.mCustViewHeight, SCROLL_ANIMATION_DURATION);
                        break;
                    }
                    break;
            }
        }
    }

    public int getDragAnimationStage() {
        return this.mDragStage;
    }

    public void setStillView(View view, boolean isStill) {
        if (isStill) {
            this.mStillViews.add(view);
        } else {
            this.mStillViews.remove(view);
        }
    }

    public void setAnimationEnable(boolean isEnable) {
        this.mIsAnimationEnable = isEnable;
    }

    public void setCallback(InnerOnStageChangedListener callback) {
        this.mCallback = callback;
    }

    public void setStartStageCallback(InnerOnStageChangedListener callback) {
        this.mStartCallback = callback;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        boolean isLand;
        boolean z = false;
        super.onConfigurationChanged(newConfig);
        if (getContext().getResources().getConfiguration().orientation == 2) {
            isLand = true;
        } else {
            isLand = false;
        }
        if (!isLand) {
            z = true;
        }
        setAnimationEnable(z);
        setUiOptions(this.mUiOptions);
    }

    public void setUiOptions(int uiOptions) {
        this.mUiOptions = uiOptions;
        super.setUiOptions(uiOptions);
    }

    protected int getBottomInset(int bottomInset) {
        return 0;
    }

    public void setHwDrawerFeature(boolean open, int overlayActionBar) {
        this.mHwDrawerFeature = open;
        if (open) {
            this.mHwOverlayActionBar = overlayActionBar;
        } else {
            this.mHwOverlayActionBar = 0;
        }
        initHwDrawerFeature();
    }

    protected void initHwDrawerFeature() {
        float z = 0.0f;
        View v = null;
        if (this.mHwOverlayActionBar == 1) {
            v = getContent();
            z = HwFragmentMenuItemView.ALPHA_NORMAL;
        } else if (this.mHwOverlayActionBar == -1) {
            v = getActionBarContainer();
            z = -1.0f;
        }
        if (!this.mHwDrawerFeature) {
            v = getContent();
            if (!(v == null || v.getTranslationZ() == 0.0f)) {
                v.setTranslationZ(0.0f);
            }
            v = getActionBarContainer();
            if (v != null && v.getTranslationZ() != 0.0f) {
                v.setTranslationZ(0.0f);
            }
        } else if (v != null) {
            v.setTranslationZ(z);
        }
    }

    public void setDrawerOpend(boolean open) {
        this.mDrawerOpend = open;
    }

    public ArrayList<View> buildTouchDispatchChildList() {
        ArrayList<View> list = super.buildTouchDispatchChildList();
        if (this.mHwDrawerFeature && list != null) {
            if (!this.mDrawerOpend) {
                swapTouchDispatchTarget(list, 16908290, 16909290);
            } else if (this.mHwOverlayActionBar == -1) {
                swapTouchDispatchTarget(list, 16909293, 16908290);
            }
        }
        return list;
    }

    private void swapTouchDispatchTarget(ArrayList<View> list, int id1, int id2) {
        if (list != null) {
            Object view1 = null;
            int index1 = -1;
            Object view2 = null;
            int index2 = -1;
            for (int i = 0; i < list.size(); i++) {
                View child = (View) list.get(i);
                if (child.getId() == id1) {
                    view1 = child;
                    index1 = i;
                } else if (child.getId() == id2) {
                    View view22 = child;
                    index2 = i;
                }
            }
            if (index1 > index2 && index2 > -1) {
                list.set(index1, view2);
                list.set(index2, view1);
            }
        }
    }
}
