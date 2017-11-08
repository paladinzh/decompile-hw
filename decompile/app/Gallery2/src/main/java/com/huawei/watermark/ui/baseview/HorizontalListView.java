package com.huawei.watermark.ui.baseview;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.Scroller;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import com.huawei.watermark.ui.element.WMRotateLinearLayout2;
import com.huawei.watermark.wmutil.WMBaseUtil;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.LinkedList;
import java.util.Queue;

public class HorizontalListView extends AdapterView<ListAdapter> {
    protected ListAdapter mAdapter;
    protected int mCurrentX;
    private boolean mDataChanged = false;
    private DataSetObserver mDataObserver = new DataSetObserver() {
        public void onChanged() {
            synchronized (HorizontalListView.this) {
                HorizontalListView.this.mDataChanged = true;
            }
            HorizontalListView.this.invalidate();
            HorizontalListView.this.requestLayout();
        }

        public void onInvalidated() {
            HorizontalListView.this.reset();
            HorizontalListView.this.invalidate();
            HorizontalListView.this.requestLayout();
        }
    };
    private int mDisplayOffset = 0;
    private GestureDetector mGesture;
    private int mLeftViewIndex = -1;
    private int mMaxX = Integer.MAX_VALUE;
    protected int mNextX;
    private OnGestureListener mOnGesture = new SimpleOnGestureListener() {
        public boolean onDown(MotionEvent e) {
            return HorizontalListView.this.onDown(e);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            synchronized (HorizontalListView.this) {
                HorizontalListView horizontalListView = HorizontalListView.this;
                horizontalListView.mNextX += (int) distanceX;
            }
            HorizontalListView.this.requestLayout();
            return true;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            for (int i = 0; i < HorizontalListView.this.getChildCount(); i++) {
                View child = HorizontalListView.this.getChildAt(i);
                if (isEventWithinView(e, child)) {
                    if (HorizontalListView.this.mOnItemClicked != null) {
                        HorizontalListView.this.mOnItemClicked.onItemClick(HorizontalListView.this, child, (HorizontalListView.this.mLeftViewIndex + 1) + i, HorizontalListView.this.mAdapter.getItemId((HorizontalListView.this.mLeftViewIndex + 1) + i));
                    }
                    if (HorizontalListView.this.mOnItemSelected != null) {
                        HorizontalListView.this.mOnItemSelected.onItemSelected(HorizontalListView.this, child, (HorizontalListView.this.mLeftViewIndex + 1) + i, HorizontalListView.this.mAdapter.getItemId((HorizontalListView.this.mLeftViewIndex + 1) + i));
                    }
                    return true;
                }
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
            int childCount = HorizontalListView.this.getChildCount();
            int i = 0;
            while (i < childCount) {
                View child = HorizontalListView.this.getChildAt(i);
                if (!isEventWithinView(e, child)) {
                    i++;
                } else if (HorizontalListView.this.mOnItemLongClicked != null) {
                    HorizontalListView.this.mOnItemLongClicked.onItemLongClick(HorizontalListView.this, child, (HorizontalListView.this.mLeftViewIndex + 1) + i, HorizontalListView.this.mAdapter.getItemId((HorizontalListView.this.mLeftViewIndex + 1) + i));
                    return;
                } else {
                    return;
                }
            }
        }

        private boolean isEventWithinView(MotionEvent e, View child) {
            int i = 0;
            Rect viewRect = new Rect();
            int[] childPosition = new int[2];
            child.getLocationOnScreen(childPosition);
            int left = childPosition[0] + (HorizontalListView.this.orientation != 270 ? 0 : -child.getHeight());
            int width = (HorizontalListView.this.orientation == 0 || HorizontalListView.this.orientation == 180) ? child.getWidth() : child.getHeight();
            int right = left + width;
            width = childPosition[1];
            if (HorizontalListView.this.orientation == 90) {
                i = -child.getWidth();
            }
            int top = width + i;
            width = (HorizontalListView.this.orientation == 0 || HorizontalListView.this.orientation == 180) ? child.getHeight() : child.getWidth();
            viewRect.set(left, top, right, top + width);
            return viewRect.contains((int) e.getRawX(), (int) e.getRawY());
        }
    };
    private OnItemClickListener mOnItemClicked;
    private OnItemLongClickListener mOnItemLongClicked;
    private OnItemSelectedListener mOnItemSelected;
    protected int mOrientationType = 1;
    private Queue<View> mRemovedViewQueue = new LinkedList();
    private int mRightViewIndex = 0;
    protected Scroller mScroller;
    private int oldOrientation = 0;
    private int orientation = 0;

    public HorizontalListView(Context context) {
        super(context);
        initView();
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private synchronized void initView() {
        this.mLeftViewIndex = -1;
        this.mRightViewIndex = 0;
        this.mDisplayOffset = 0;
        this.mCurrentX = 0;
        this.mNextX = 0;
        this.mMaxX = Integer.MAX_VALUE;
        this.mScroller = new Scroller(getContext());
        this.mGesture = new GestureDetector(getContext(), this.mOnGesture);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mOnItemSelected = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClicked = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClicked = listener;
    }

    public ListAdapter getAdapter() {
        if (this.mAdapter == null) {
            return null;
        }
        HorizontalListViewAdapter res = new HorizontalListViewAdapter(((HorizontalListViewAdapter) this.mAdapter).mContext);
        res.setSelectIndex(((HorizontalListViewAdapter) this.mAdapter).selectIndex);
        return res;
    }

    public View getSelectedView() {
        return null;
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter == null) {
            this.mAdapter = null;
            return;
        }
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(this.mDataObserver);
        reset();
    }

    private synchronized void reset() {
        initView();
        removeAllViewsInLayout();
        requestLayout();
    }

    public void setSelection(int position) {
    }

    private void addAndMeasureChild(View child, int viewPos) {
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(-1, -1);
        }
        addViewInLayout(child, viewPos, params, true);
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), Target.SIZE_ORIGINAL), MeasureSpec.makeMeasureSpec(getHeight(), Target.SIZE_ORIGINAL));
    }

    public synchronized int getScrollToLeft() {
        return this.mNextX;
    }

    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mAdapter != null) {
            boolean needKeepCurrent = false;
            if (this.mDataChanged) {
                needKeepCurrent = true;
            }
            if (this.mDataChanged) {
                int oldCurrentX = this.mCurrentX;
                initView();
                removeAllViewsInLayout();
                this.mNextX = oldCurrentX;
                this.mDataChanged = false;
            }
            if (this.mScroller.computeScrollOffset()) {
                this.mNextX = this.mScroller.getCurrX();
            }
            if (this.mNextX <= 0) {
                this.mNextX = 0;
                this.mScroller.forceFinished(true);
            }
            if (this.mNextX >= this.mMaxX) {
                this.mNextX = this.mMaxX;
                this.mScroller.forceFinished(true);
            }
            int dx = this.mCurrentX - this.mNextX;
            removeNonVisibleItems(dx);
            fillList(dx);
            positionItems(dx);
            this.mCurrentX = this.mNextX;
            if (!this.mScroller.isFinished()) {
                post(new Runnable() {
                    public void run() {
                        HorizontalListView.this.requestLayout();
                    }
                });
            }
            if (WMBaseUtil.containType(this.mOrientationType, 1)) {
                int i;
                View child;
                if (this.orientation != this.oldOrientation) {
                    for (i = 0; i < getChildCount(); i++) {
                        child = getChildAt(i);
                        child.setRotation((float) ((360 - this.oldOrientation) % 360));
                        child.setVisibility(0);
                        child.requestLayout();
                        ((WMRotateLinearLayout2) child).setCurrentDegree(this.oldOrientation);
                        ((WMRotateLinearLayout2) child).setTargetDegree(this.oldOrientation);
                        if (child instanceof WMRotateLinearLayout2) {
                            ((WMRotateLinearLayout2) child).setOrientation(this.orientation, true);
                        }
                    }
                } else if (needKeepCurrent) {
                    for (i = 0; i < getChildCount(); i++) {
                        child = getChildAt(i);
                        if (child instanceof WMRotateLinearLayout2) {
                            child.setRotation((float) ((360 - this.orientation) % 360));
                        }
                        child.setVisibility(0);
                    }
                }
            }
            this.oldOrientation = this.orientation;
        }
    }

    private void fillList(int dx) {
        int edge = 0;
        View child = getChildAt(getChildCount() - 1);
        if (child != null) {
            edge = child.getRight();
        }
        fillListRight(edge, dx);
        edge = 0;
        child = getChildAt(0);
        if (child != null) {
            edge = child.getLeft();
        }
        fillListLeft(edge, dx);
    }

    private void fillListRight(int rightEdge, int dx) {
        while (rightEdge + dx < getWidth() && this.mRightViewIndex < this.mAdapter.getCount()) {
            View child = this.mAdapter.getView(this.mRightViewIndex, (View) this.mRemovedViewQueue.poll(), this);
            if (child != null) {
                if (WMBaseUtil.containType(this.mOrientationType, 1)) {
                    child.setVisibility(4);
                }
                addAndMeasureChild(child, -1);
                rightEdge += child.getMeasuredWidth();
                if (this.mRightViewIndex == this.mAdapter.getCount() - 1) {
                    this.mMaxX = (this.mCurrentX + rightEdge) - getWidth();
                }
                if (this.mMaxX < 0) {
                    this.mMaxX = 0;
                }
                this.mRightViewIndex++;
            }
        }
    }

    private void fillListLeft(int leftEdge, int dx) {
        while (leftEdge + dx > 0 && this.mLeftViewIndex >= 0) {
            View child = this.mAdapter.getView(this.mLeftViewIndex, (View) this.mRemovedViewQueue.poll(), this);
            if (child != null) {
                addAndMeasureChild(child, 0);
                leftEdge -= child.getMeasuredWidth();
                this.mLeftViewIndex--;
                this.mDisplayOffset -= child.getMeasuredWidth();
            }
        }
    }

    private void removeNonVisibleItems(int dx) {
        View child = getChildAt(0);
        while (child != null && child.getRight() + dx <= 0) {
            this.mDisplayOffset += child.getMeasuredWidth();
            if (!this.mRemovedViewQueue.offer(child)) {
                break;
            }
            removeViewInLayout(child);
            this.mLeftViewIndex++;
            child = getChildAt(0);
        }
        child = getChildAt(getChildCount() - 1);
        while (child != null && child.getLeft() + dx >= getWidth() && this.mRemovedViewQueue.offer(child)) {
            removeViewInLayout(child);
            this.mRightViewIndex--;
            child = getChildAt(getChildCount() - 1);
        }
    }

    private void positionItems(int dx) {
        if (getChildCount() > 0) {
            this.mDisplayOffset += dx;
            int left = this.mDisplayOffset;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(left, 0, left + childWidth, child.getMeasuredHeight());
                left += childWidth;
            }
        }
    }

    public synchronized void scrollTo(int x) {
        this.mScroller.startScroll(this.mNextX, 0, x - this.mNextX, 0);
        requestLayout();
    }

    @SuppressWarnings({"IS2_INCONSISTENT_SYNC"})
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev) | this.mGesture.onTouchEvent(ev);
    }

    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        synchronized (this) {
            this.mScroller.fling(this.mNextX, 0, (int) (-velocityX), 0, 0, this.mMaxX, 0, 0);
        }
        requestLayout();
        return true;
    }

    protected boolean onDown(MotionEvent e) {
        synchronized (this) {
            this.mScroller.forceFinished(true);
        }
        return true;
    }

    public void onOrientationChanged(int ori) {
        if (16 == this.mOrientationType) {
            Log.d("HorizontalListView", "gallery should not change orientation = " + this.orientation);
            return;
        }
        if (WMBaseUtil.containType(this.mOrientationType, 1)) {
            this.orientation = ori;
        }
    }

    public void setOrientationType(int type) {
        this.mOrientationType = type;
    }
}
