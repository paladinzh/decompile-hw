package com.android.huawei.coverscreen;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Scroller;
import com.android.huawei.coverscreen.VerticalGalleryAdapterView.AdapterContextMenuInfo;

public class VerticalGallery extends VerticalGalleryAbsSpinner implements OnGestureListener {
    private int mAnimationDuration;
    private int mBottomMost;
    private AdapterContextMenuInfo mContextMenuInfo;
    private Runnable mDisableSuppressSelectionChangedRunnable;
    private int mDownTouchPosition;
    private View mDownTouchView;
    private FlingRunnable mFlingRunnable;
    private GestureDetector mGestureDetector;
    private int mGravity;
    private boolean mIsFirstScroll;
    private boolean mReceivedInvokeKeyDown;
    private View mSelectedChild;
    private boolean mShouldCallbackDuringFling;
    private boolean mShouldCallbackOnUnselectedItemClick;
    private boolean mShouldStopFling;
    private int mSpacing;
    private boolean mSuppressSelectionChanged;
    private int mTopMost;
    private float mUnselectedAlpha;

    private class FlingRunnable implements Runnable {
        private int mLastFlingY;
        private Scroller mScroller;

        public FlingRunnable() {
            this.mScroller = new Scroller(VerticalGallery.this.getContext());
        }

        private void startCommon() {
            VerticalGallery.this.removeCallbacks(this);
        }

        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity != 0) {
                int initialY;
                startCommon();
                if (initialVelocity < 0) {
                    initialY = Integer.MAX_VALUE;
                } else {
                    initialY = 0;
                }
                this.mLastFlingY = initialY;
                this.mScroller.fling(0, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
                VerticalGallery.this.post(this);
            }
        }

        public void startUsingDistance(int distance) {
            if (distance != 0) {
                startCommon();
                this.mLastFlingY = 0;
                this.mScroller.startScroll(0, 0, 0, -distance, VerticalGallery.this.mAnimationDuration);
                VerticalGallery.this.post(this);
            }
        }

        public void stop(boolean scrollIntoSlots) {
            VerticalGallery.this.removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        private void endFling(boolean scrollIntoSlots) {
            this.mScroller.forceFinished(true);
            if (scrollIntoSlots) {
                VerticalGallery.this.scrollIntoSlots();
            }
        }

        public void run() {
            if (VerticalGallery.this.mItemCount == 0) {
                endFling(true);
                return;
            }
            VerticalGallery.this.mShouldStopFling = false;
            Scroller scroller = this.mScroller;
            boolean more = scroller.computeScrollOffset();
            int y = scroller.getCurrY();
            int delta = this.mLastFlingY - y;
            if (delta > 0) {
                VerticalGallery.this.mDownTouchPosition = VerticalGallery.this.mFirstPosition;
                delta = Math.min(((VerticalGallery.this.getHeight() - VerticalGallery.this.getPaddingTop()) - VerticalGallery.this.getPaddingBottom()) - 1, delta);
            } else {
                VerticalGallery.this.mDownTouchPosition = VerticalGallery.this.mFirstPosition + (VerticalGallery.this.getChildCount() - 1);
                delta = Math.max(-(((VerticalGallery.this.getHeight() - VerticalGallery.this.getPaddingBottom()) - VerticalGallery.this.getPaddingTop()) - 1), delta);
            }
            VerticalGallery.this.trackMotionScroll(delta);
            if (!more || VerticalGallery.this.mShouldStopFling) {
                endFling(true);
            } else {
                this.mLastFlingY = y;
                VerticalGallery.this.post(this);
            }
        }
    }

    public static class LayoutParams extends android.widget.RelativeLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public VerticalGallery(Context context) {
        this(context, null);
    }

    public VerticalGallery(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSpacing = 0;
        this.mAnimationDuration = 400;
        this.mFlingRunnable = new FlingRunnable();
        this.mDisableSuppressSelectionChangedRunnable = new Runnable() {
            public void run() {
                VerticalGallery.this.mSuppressSelectionChanged = false;
                VerticalGallery.this.selectionChanged();
            }
        };
        this.mShouldCallbackDuringFling = true;
        this.mShouldCallbackOnUnselectedItemClick = true;
        init(context);
    }

    private void init(Context context) {
        this.mGestureDetector = new GestureDetector(context, this);
        this.mGestureDetector.setIsLongpressEnabled(true);
        setUnselectedAlpha(0.5f);
    }

    public void setCallbackDuringFling(boolean shouldCallback) {
        this.mShouldCallbackDuringFling = shouldCallback;
    }

    public void setSpacing(int spacing) {
        this.mSpacing = spacing;
    }

    public void setUnselectedAlpha(float unselectedAlpha) {
        this.mUnselectedAlpha = unselectedAlpha;
    }

    protected boolean getChildStaticTransformation(View child, Transformation t) {
        t.clear();
        t.setAlpha(child == this.mSelectedChild ? 1.0f : this.mUnselectedAlpha);
        return true;
    }

    protected int computeHorizontalScrollExtent() {
        return 1;
    }

    protected int computeHorizontalScrollOffset() {
        return this.mSelectedPosition;
    }

    protected int computeHorizontalScrollRange() {
        return this.mItemCount;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        layout(0, false);
        this.mInLayout = false;
    }

    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    void trackMotionScroll(int deltaY) {
        if (getChildCount() != 0) {
            boolean toTop;
            if (deltaY < 0) {
                toTop = true;
            } else {
                toTop = false;
            }
            int limitedDeltaY = getLimitedMotionScrollAmount(toTop, deltaY);
            if (limitedDeltaY != deltaY) {
                this.mFlingRunnable.endFling(false);
                onFinishedMovement();
            }
            offsetChildrenTopAndBottom(limitedDeltaY);
            detachOffScreenChildren(toTop);
            if (toTop) {
                fillToGalleryBottom();
            } else {
                fillToGalleryTop();
            }
            this.mRecycler.clear();
            setSelectionToCenterChild();
            invalidate();
        }
    }

    int getLimitedMotionScrollAmount(boolean motionToTop, int deltaY) {
        View extremeChild = getChildAt((motionToTop ? this.mItemCount - 1 : 0) - this.mFirstPosition);
        if (extremeChild == null) {
            return deltaY;
        }
        int extremeChildCenter = getCenterOfView(extremeChild);
        int galleryCenter = getCenterOfGallery();
        if (motionToTop) {
            if (extremeChildCenter <= galleryCenter) {
                return 0;
            }
        } else if (extremeChildCenter >= galleryCenter) {
            return 0;
        }
        int centerDifference = galleryCenter - extremeChildCenter;
        return motionToTop ? Math.max(centerDifference, deltaY) : Math.min(centerDifference, deltaY);
    }

    public void offsetChildrenTopAndBottom(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetTopAndBottom(offset);
        }
    }

    private int getCenterOfGallery() {
        return (((getHeight() - getPaddingTop()) - getPaddingBottom()) / 2) + getPaddingTop();
    }

    private static int getCenterOfView(View view) {
        return view.getTop() + (view.getHeight() / 2);
    }

    private void detachOffScreenChildren(boolean toTop) {
        int numChildren = getChildCount();
        int firstPosition = this.mFirstPosition;
        int start = 0;
        int count = 0;
        int i;
        View child;
        if (!toTop) {
            int galleryBottom = getHeight() - getPaddingBottom();
            for (i = numChildren - 1; i >= 0; i--) {
                child = getChildAt(i);
                if (child.getTop() <= galleryBottom) {
                    break;
                }
                start = i;
                count++;
                this.mRecycler.put(firstPosition + i, child);
            }
        } else {
            int galleryTop = getPaddingTop();
            for (i = 0; i < numChildren; i++) {
                child = getChildAt(i);
                if (child.getBottom() >= galleryTop) {
                    break;
                }
                count++;
                this.mRecycler.put(firstPosition + i, child);
            }
        }
        detachViewsFromParent(start, count);
        if (toTop) {
            this.mFirstPosition += count;
        }
    }

    private void scrollIntoSlots() {
        if (getChildCount() != 0 && this.mSelectedChild != null) {
            int scrollAmount = getCenterOfGallery() - getCenterOfView(this.mSelectedChild);
            if (scrollAmount != 0) {
                this.mFlingRunnable.startUsingDistance(scrollAmount);
            } else {
                onFinishedMovement();
            }
        }
    }

    private void onFinishedMovement() {
        if (this.mSuppressSelectionChanged) {
            this.mSuppressSelectionChanged = false;
            super.selectionChanged();
        }
        invalidate();
    }

    void selectionChanged() {
        if (!this.mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }

    private void setSelectionToCenterChild() {
        View selView = this.mSelectedChild;
        if (this.mSelectedChild != null) {
            int galleryCenter = getCenterOfGallery();
            if (selView.getTop() > galleryCenter || selView.getBottom() < galleryCenter) {
                int closestEdgeDistance = Integer.MAX_VALUE;
                int newSelectedChildIndex = 0;
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (child.getTop() <= galleryCenter && child.getBottom() >= galleryCenter) {
                        newSelectedChildIndex = i;
                        break;
                    }
                    int childClosestEdgeDistance = Math.min(Math.abs(child.getTop() - galleryCenter), Math.abs(child.getBottom() - galleryCenter));
                    if (childClosestEdgeDistance < closestEdgeDistance) {
                        closestEdgeDistance = childClosestEdgeDistance;
                        newSelectedChildIndex = i;
                    }
                }
                int newPos = this.mFirstPosition + newSelectedChildIndex;
                if (newPos != this.mSelectedPosition) {
                    setSelectedPositionInt(newPos);
                    setNextSelectedPositionInt(newPos);
                    checkSelectionChanged();
                }
            }
        }
    }

    void layout(int delta, boolean animate) {
        int childrenTop = this.mSpinnerPadding.top;
        int childrenHeight = ((getBottom() - getTop()) - this.mSpinnerPadding.top) - this.mSpinnerPadding.bottom;
        if (this.mDataChanged) {
            handleDataChanged();
        }
        if (this.mItemCount == 0) {
            resetList();
            return;
        }
        if (this.mNextSelectedPosition >= 0) {
            setSelectedPositionInt(this.mNextSelectedPosition);
        }
        recycleAllViews();
        detachAllViewsFromParent();
        this.mBottomMost = 0;
        this.mTopMost = 0;
        this.mFirstPosition = this.mSelectedPosition;
        View sel = makeAndAddView(this.mSelectedPosition, 0, 0, true);
        sel.offsetTopAndBottom(((childrenHeight / 2) + childrenTop) - (sel.getHeight() / 2));
        fillToGalleryBottom();
        fillToGalleryTop();
        this.mRecycler.clear();
        invalidate();
        checkSelectionChanged();
        this.mDataChanged = false;
        this.mNeedSync = false;
        setNextSelectedPositionInt(this.mSelectedPosition);
        updateSelectedItemMetadata();
    }

    private void fillToGalleryTop() {
        int curPosition;
        int curBottomEdge;
        int itemSpacing = this.mSpacing;
        int galleryTop = getPaddingTop();
        View prevIterationView = getChildAt(0);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition - 1;
            curBottomEdge = prevIterationView.getTop() - itemSpacing;
        } else {
            curPosition = 0;
            curBottomEdge = (getBottom() - getTop()) - getPaddingBottom();
            this.mShouldStopFling = true;
        }
        while (curBottomEdge > galleryTop && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curBottomEdge, false);
            this.mFirstPosition = curPosition;
            curBottomEdge = prevIterationView.getTop() - itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryBottom() {
        int curPosition;
        int curTopEdge;
        int itemSpacing = this.mSpacing;
        int galleryBottom = (getBottom() - getTop()) - getPaddingBottom();
        int numChildren = getChildCount();
        int numItems = this.mItemCount;
        View prevIterationView = getChildAt(numChildren - 1);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition + numChildren;
            curTopEdge = prevIterationView.getBottom() + itemSpacing;
        } else {
            curPosition = this.mItemCount - 1;
            this.mFirstPosition = curPosition;
            curTopEdge = getPaddingTop();
            this.mShouldStopFling = true;
        }
        while (curTopEdge < galleryBottom && curPosition < numItems) {
            curTopEdge = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curTopEdge, true).getBottom() + itemSpacing;
            curPosition++;
        }
    }

    private View makeAndAddView(int position, int offset, int y, boolean fromTop) {
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.get(position);
            if (child != null) {
                int childTop = child.getTop();
                this.mBottomMost = Math.max(this.mBottomMost, child.getMeasuredHeight() + childTop);
                this.mTopMost = Math.min(this.mTopMost, childTop);
                setUpChild(child, offset, y, fromTop);
                return child;
            }
        }
        child = this.mAdapter.getView(position, null, this);
        setUpChild(child, offset, y, fromTop);
        return child;
    }

    private void setUpChild(View child, int offset, int y, boolean fromTop) {
        int childTop;
        int childBottom;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        }
        addViewInLayout(child, fromTop ? -1 : 0, lp);
        child.setSelected(offset == 0);
        child.measure(ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, lp.width), ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, lp.height));
        int childLeft = calculateLeft(child, true);
        int childRight = childLeft + child.getMeasuredWidth();
        int height = child.getMeasuredHeight();
        if (fromTop) {
            childTop = y;
            childBottom = y + height;
        } else {
            childTop = y - height;
            childBottom = y;
        }
        child.layout(childLeft, childTop, childRight, childBottom);
    }

    private int calculateLeft(View child, boolean duringLayout) {
        int myWidth = duringLayout ? getMeasuredWidth() : getWidth();
        int childWidth = duringLayout ? child.getMeasuredWidth() : child.getWidth();
        switch (this.mGravity) {
            case 1:
                return this.mSpinnerPadding.left + ((((myWidth - this.mSpinnerPadding.right) - this.mSpinnerPadding.left) - childWidth) / 2);
            case 3:
                return this.mSpinnerPadding.left;
            case 5:
                return (myWidth - this.mSpinnerPadding.right) - childWidth;
            default:
                return 0;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == 1) {
            onUp();
        } else if (action == 3) {
            onCancel();
        }
        return retValue;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        if (this.mDownTouchPosition < 0) {
            return false;
        }
        scrollToChild(this.mDownTouchPosition - this.mFirstPosition);
        if (this.mShouldCallbackOnUnselectedItemClick || this.mDownTouchPosition == this.mSelectedPosition) {
            performItemClick(this.mDownTouchView, this.mDownTouchPosition, this.mAdapter.getItemId(this.mDownTouchPosition));
        }
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!this.mShouldCallbackDuringFling) {
            removeCallbacks(this.mDisableSuppressSelectionChangedRunnable);
            if (!this.mSuppressSelectionChanged) {
                this.mSuppressSelectionChanged = true;
            }
        }
        this.mFlingRunnable.startUsingVelocity((int) (-velocityY));
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        getParent().requestDisallowInterceptTouchEvent(true);
        if (this.mShouldCallbackDuringFling) {
            if (this.mSuppressSelectionChanged) {
                this.mSuppressSelectionChanged = false;
            }
        } else if (this.mIsFirstScroll) {
            if (!this.mSuppressSelectionChanged) {
                this.mSuppressSelectionChanged = true;
            }
            postDelayed(this.mDisableSuppressSelectionChangedRunnable, 250);
        }
        trackMotionScroll(((int) distanceY) * -1);
        this.mIsFirstScroll = false;
        return true;
    }

    public boolean onDown(MotionEvent e) {
        this.mFlingRunnable.stop(false);
        this.mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());
        if (this.mDownTouchPosition >= 0) {
            this.mDownTouchView = getChildAt(this.mDownTouchPosition - this.mFirstPosition);
            this.mDownTouchView.setPressed(true);
        }
        this.mIsFirstScroll = true;
        return true;
    }

    void onUp() {
        if (this.mFlingRunnable.mScroller.isFinished()) {
            scrollIntoSlots();
        }
        dispatchUnpress();
    }

    void onCancel() {
        onUp();
    }

    public void onLongPress(MotionEvent e) {
        if (this.mDownTouchPosition >= 0) {
            performHapticFeedback(0);
            dispatchLongPress(this.mDownTouchView, this.mDownTouchPosition, getItemIdAtPosition(this.mDownTouchPosition));
        }
    }

    public void onShowPress(MotionEvent e) {
    }

    private void dispatchPress(View child) {
        if (child != null) {
            child.setPressed(true);
        }
        setPressed(true);
    }

    private void dispatchUnpress() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }
        setPressed(false);
    }

    public void dispatchSetSelected(boolean selected) {
    }

    protected void dispatchSetPressed(boolean pressed) {
        if (this.mSelectedChild != null) {
            this.mSelectedChild.setPressed(pressed);
        }
    }

    protected ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public boolean showContextMenuForChild(View originalView) {
        int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }
        return dispatchLongPress(originalView, longPressPosition, this.mAdapter.getItemId(longPressPosition));
    }

    public boolean showContextMenu() {
        if (!isPressed() || this.mSelectedPosition < 0) {
            return false;
        }
        return dispatchLongPress(getChildAt(this.mSelectedPosition - this.mFirstPosition), this.mSelectedPosition, this.mSelectedRowId);
    }

    private boolean dispatchLongPress(View view, int position, long id) {
        boolean handled = false;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, this.mDownTouchView, this.mDownTouchPosition, id);
        }
        if (!handled) {
            this.mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
            handled = super.showContextMenuForChild(this);
        }
        if (handled) {
            performHapticFeedback(0);
        }
        return handled;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        super.dispatchKeyEvent(event);
        return event.dispatch(this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 19:
                if (movePrevious()) {
                    playSoundEffect(2);
                }
                return true;
            case 20:
                if (moveNext()) {
                    playSoundEffect(4);
                }
                return true;
            case 23:
            case 66:
                this.mReceivedInvokeKeyDown = true;
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 23:
            case 66:
                if (this.mReceivedInvokeKeyDown && this.mItemCount > 0) {
                    dispatchPress(this.mSelectedChild);
                    postDelayed(new Runnable() {
                        public void run() {
                            VerticalGallery.this.dispatchUnpress();
                        }
                    }, (long) ViewConfiguration.getPressedStateDuration());
                    performItemClick(getChildAt(this.mSelectedPosition - this.mFirstPosition), this.mSelectedPosition, this.mAdapter.getItemId(this.mSelectedPosition));
                }
                this.mReceivedInvokeKeyDown = false;
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    boolean movePrevious() {
        if (this.mItemCount <= 0 || this.mSelectedPosition <= 0) {
            return false;
        }
        scrollToChild((this.mSelectedPosition - this.mFirstPosition) - 1);
        return true;
    }

    boolean moveNext() {
        if (this.mItemCount <= 0 || this.mSelectedPosition >= this.mItemCount - 1) {
            return false;
        }
        scrollToChild((this.mSelectedPosition - this.mFirstPosition) + 1);
        return true;
    }

    private boolean scrollToChild(int childPosition) {
        View child = getChildAt(childPosition);
        if (child == null) {
            return false;
        }
        this.mFlingRunnable.startUsingDistance(getCenterOfGallery() - getCenterOfView(child));
        return true;
    }

    void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);
        updateSelectedItemMetadata();
    }

    private void updateSelectedItemMetadata() {
        View oldSelectedChild = this.mSelectedChild;
        View child = getChildAt(this.mSelectedPosition - this.mFirstPosition);
        this.mSelectedChild = child;
        if (child != null) {
            child.setSelected(true);
            child.setFocusable(true);
            if (hasFocus()) {
                child.requestFocus();
            }
            if (oldSelectedChild != null) {
                oldSelectedChild.setSelected(false);
                oldSelectedChild.setFocusable(false);
            }
        }
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        int selectedIndex = this.mSelectedPosition - this.mFirstPosition;
        if (selectedIndex < 0) {
            return i;
        }
        if (i == childCount - 1) {
            return selectedIndex;
        }
        if (i >= selectedIndex) {
            return i + 1;
        }
        return i;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && this.mSelectedChild != null) {
            this.mSelectedChild.requestFocus(direction);
        }
    }
}
