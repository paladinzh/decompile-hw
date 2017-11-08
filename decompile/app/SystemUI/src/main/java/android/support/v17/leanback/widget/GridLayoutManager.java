package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v17.leanback.widget.Grid.Location;
import android.support.v17.leanback.widget.Grid.Provider;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.support.v17.leanback.widget.WindowAlignment.Axis;
import android.support.v4.util.CircularIntArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import java.util.ArrayList;

final class GridLayoutManager extends LayoutManager {
    private static final Rect sTempRect = new Rect();
    private static int[] sTwoInts = new int[2];
    private final Runnable mAskFocusRunnable = new Runnable() {
        public void run() {
            if (!GridLayoutManager.this.hasFocus()) {
                View view = GridLayoutManager.this.findViewByPosition(GridLayoutManager.this.mFocusPosition);
                if (view == null || !view.hasFocusable()) {
                    int count = GridLayoutManager.this.getChildCount();
                    for (int i = 0; i < count; i++) {
                        view = GridLayoutManager.this.getChildAt(i);
                        if (view != null && view.hasFocusable()) {
                            GridLayoutManager.this.mBaseGridView.focusableViewAvailable(view);
                            break;
                        }
                    }
                    return;
                }
                GridLayoutManager.this.mBaseGridView.focusableViewAvailable(view);
            }
        }
    };
    private final BaseGridView mBaseGridView;
    private OnChildLaidOutListener mChildLaidOutListener = null;
    private OnChildSelectedListener mChildSelectedListener = null;
    private ArrayList<OnChildViewHolderSelectedListener> mChildViewHolderSelectedListeners = null;
    private int mChildVisibility = -1;
    final ViewsStateBundle mChildrenStates = new ViewsStateBundle();
    private int mExtraLayoutSpace;
    private FacetProviderAdapter mFacetProviderAdapter;
    private int mFixedRowSizeSecondary;
    private boolean mFocusOutEnd;
    private boolean mFocusOutFront;
    private boolean mFocusOutSideEnd = true;
    private boolean mFocusOutSideStart = true;
    private int mFocusPosition = -1;
    private int mFocusPositionOffset = 0;
    private int mFocusScrollStrategy = 0;
    private boolean mFocusSearchDisabled;
    private boolean mForceFullLayout;
    private int mGravity = 8388659;
    Grid mGrid;
    private Provider mGridProvider = new Provider() {
        public int getCount() {
            return GridLayoutManager.this.mState.getItemCount();
        }

        public int createItem(int index, boolean append, Object[] item) {
            View v = GridLayoutManager.this.getViewForPosition(index);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            lp.setItemAlignmentFacet((ItemAlignmentFacet) GridLayoutManager.this.getFacet(GridLayoutManager.this.mBaseGridView.getChildViewHolder(v), ItemAlignmentFacet.class));
            if (!lp.isItemRemoved()) {
                if (append) {
                    GridLayoutManager.this.addView(v);
                } else {
                    GridLayoutManager.this.addView(v, 0);
                }
                if (GridLayoutManager.this.mChildVisibility != -1) {
                    v.setVisibility(GridLayoutManager.this.mChildVisibility);
                }
                if (GridLayoutManager.this.mPendingMoveSmoothScroller != null) {
                    GridLayoutManager.this.mPendingMoveSmoothScroller.consumePendingMovesBeforeLayout();
                }
                int subindex = GridLayoutManager.this.getSubPositionByView(v, v.findFocus());
                if (GridLayoutManager.this.mInLayout) {
                    if (!GridLayoutManager.this.mInFastRelayout) {
                        if (!GridLayoutManager.this.mInLayoutSearchFocus && index == GridLayoutManager.this.mFocusPosition && subindex == GridLayoutManager.this.mSubFocusPosition) {
                            GridLayoutManager.this.dispatchChildSelected();
                        } else if (GridLayoutManager.this.mInLayoutSearchFocus && index >= GridLayoutManager.this.mFocusPosition && v.hasFocusable()) {
                            GridLayoutManager.this.mFocusPosition = index;
                            GridLayoutManager.this.mSubFocusPosition = subindex;
                            GridLayoutManager.this.mInLayoutSearchFocus = false;
                            GridLayoutManager.this.dispatchChildSelected();
                        }
                    }
                } else if (index == GridLayoutManager.this.mFocusPosition && subindex == GridLayoutManager.this.mSubFocusPosition && GridLayoutManager.this.mPendingMoveSmoothScroller == null) {
                    GridLayoutManager.this.dispatchChildSelected();
                }
                GridLayoutManager.this.measureChild(v);
            }
            item[0] = v;
            if (GridLayoutManager.this.mOrientation == 0) {
                return GridLayoutManager.this.getDecoratedMeasuredWidthWithMargin(v);
            }
            return GridLayoutManager.this.getDecoratedMeasuredHeightWithMargin(v);
        }

        public void addItem(Object item, int index, int length, int rowIndex, int edge) {
            int start;
            int end;
            View v = (View) item;
            if (edge == Integer.MIN_VALUE || edge == Integer.MAX_VALUE) {
                if (GridLayoutManager.this.mGrid.isReversedFlow()) {
                    edge = GridLayoutManager.this.mWindowAlignment.mainAxis().getSize() - GridLayoutManager.this.mWindowAlignment.mainAxis().getPaddingHigh();
                } else {
                    edge = GridLayoutManager.this.mWindowAlignment.mainAxis().getPaddingLow();
                }
            }
            if (!GridLayoutManager.this.mGrid.isReversedFlow()) {
                start = edge;
                end = edge + length;
            } else {
                start = edge - length;
                end = edge;
            }
            int startSecondary = GridLayoutManager.this.getRowStartSecondary(rowIndex) - GridLayoutManager.this.mScrollOffsetSecondary;
            GridLayoutManager.this.mChildrenStates.loadView(v, index);
            GridLayoutManager.this.layoutChild(rowIndex, v, start, end, startSecondary);
            if (index == GridLayoutManager.this.mGrid.getFirstVisibleIndex()) {
                if (GridLayoutManager.this.mGrid.isReversedFlow()) {
                    GridLayoutManager.this.updateScrollMax();
                } else {
                    GridLayoutManager.this.updateScrollMin();
                }
            }
            if (index == GridLayoutManager.this.mGrid.getLastVisibleIndex()) {
                if (GridLayoutManager.this.mGrid.isReversedFlow()) {
                    GridLayoutManager.this.updateScrollMin();
                } else {
                    GridLayoutManager.this.updateScrollMax();
                }
            }
            if (!(GridLayoutManager.this.mInLayout || GridLayoutManager.this.mPendingMoveSmoothScroller == null)) {
                GridLayoutManager.this.mPendingMoveSmoothScroller.consumePendingMovesAfterLayout();
            }
            if (GridLayoutManager.this.mChildLaidOutListener != null) {
                ViewHolder vh = GridLayoutManager.this.mBaseGridView.getChildViewHolder(v);
                GridLayoutManager.this.mChildLaidOutListener.onChildLaidOut(GridLayoutManager.this.mBaseGridView, v, index, vh == null ? -1 : vh.getItemId());
            }
        }

        public void removeItem(int index) {
            View v = GridLayoutManager.this.findViewByPosition(index);
            if (GridLayoutManager.this.mInLayout) {
                GridLayoutManager.this.detachAndScrapView(v, GridLayoutManager.this.mRecycler);
            } else {
                GridLayoutManager.this.removeAndRecycleView(v, GridLayoutManager.this.mRecycler);
            }
        }

        public int getEdge(int index) {
            if (GridLayoutManager.this.mReverseFlowPrimary) {
                return GridLayoutManager.this.getViewMax(GridLayoutManager.this.findViewByPosition(index));
            }
            return GridLayoutManager.this.getViewMin(GridLayoutManager.this.findViewByPosition(index));
        }

        public int getSize(int index) {
            return GridLayoutManager.this.getViewPrimarySize(GridLayoutManager.this.findViewByPosition(index));
        }
    };
    private int mHorizontalMargin;
    private boolean mInFastRelayout;
    private boolean mInLayout;
    private boolean mInLayoutSearchFocus;
    private boolean mInScroll;
    private boolean mInSelection = false;
    private final ItemAlignment mItemAlignment = new ItemAlignment();
    private boolean mLayoutEnabled = true;
    private int mMarginPrimary;
    private int mMarginSecondary;
    private int mMaxSizeSecondary;
    private int[] mMeasuredDimension = new int[2];
    private int mNumRows;
    private int mNumRowsRequested = 1;
    private int mOrientation = 0;
    private OrientationHelper mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
    private PendingMoveSmoothScroller mPendingMoveSmoothScroller;
    private int mPrimaryScrollExtra;
    private boolean mPruneChild = true;
    private Recycler mRecycler;
    private final Runnable mRequestLayoutRunnable = new Runnable() {
        public void run() {
            GridLayoutManager.this.requestLayout();
        }
    };
    private boolean mReverseFlowPrimary = false;
    private boolean mReverseFlowSecondary = false;
    private boolean mRowSecondarySizeRefresh;
    private int[] mRowSizeSecondary;
    private int mRowSizeSecondaryRequested;
    private boolean mScrollEnabled = true;
    private int mScrollOffsetPrimary;
    private int mScrollOffsetSecondary;
    private int mSizePrimary;
    private State mState;
    private int mSubFocusPosition = 0;
    private int mVerticalMargin;
    private final WindowAlignment mWindowAlignment = new WindowAlignment();

    abstract class GridLinearSmoothScroller extends LinearSmoothScroller {
        GridLinearSmoothScroller() {
            super(GridLayoutManager.this.mBaseGridView.getContext());
        }

        protected void onStop() {
            View targetView = findViewByPosition(getTargetPosition());
            if (targetView == null) {
                if (getTargetPosition() >= 0) {
                    GridLayoutManager.this.scrollToSelection(getTargetPosition(), 0, false, 0);
                }
                super.onStop();
                return;
            }
            if (GridLayoutManager.this.hasFocus()) {
                GridLayoutManager.this.mInSelection = true;
                targetView.requestFocus();
                GridLayoutManager.this.mInSelection = false;
            }
            GridLayoutManager.this.dispatchChildSelected();
            super.onStop();
        }

        protected int calculateTimeForScrolling(int dx) {
            int ms = super.calculateTimeForScrolling(dx);
            if (GridLayoutManager.this.mWindowAlignment.mainAxis().getSize() <= 0) {
                return ms;
            }
            float minMs = (30.0f / ((float) GridLayoutManager.this.mWindowAlignment.mainAxis().getSize())) * ((float) dx);
            if (((float) ms) < minMs) {
                return (int) minMs;
            }
            return ms;
        }

        protected void onTargetFound(View targetView, State state, Action action) {
            if (GridLayoutManager.this.getScrollPosition(targetView, null, GridLayoutManager.sTwoInts)) {
                int dx;
                int dy;
                if (GridLayoutManager.this.mOrientation == 0) {
                    dx = GridLayoutManager.sTwoInts[0];
                    dy = GridLayoutManager.sTwoInts[1];
                } else {
                    dx = GridLayoutManager.sTwoInts[1];
                    dy = GridLayoutManager.sTwoInts[0];
                }
                action.update(dx, dy, calculateTimeForDeceleration((int) Math.sqrt((double) ((dx * dx) + (dy * dy)))), this.mDecelerateInterpolator);
            }
        }
    }

    static final class LayoutParams extends android.support.v7.widget.RecyclerView.LayoutParams {
        private int[] mAlignMultiple;
        private int mAlignX;
        private int mAlignY;
        private ItemAlignmentFacet mAlignmentFacet;
        private int mBottomInset;
        private int mLeftInset;
        private int mRightInset;
        private int mTopInset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(android.support.v7.widget.RecyclerView.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((android.support.v7.widget.RecyclerView.LayoutParams) source);
        }

        int getAlignX() {
            return this.mAlignX;
        }

        int getAlignY() {
            return this.mAlignY;
        }

        int getOpticalLeft(View view) {
            return view.getLeft() + this.mLeftInset;
        }

        int getOpticalTop(View view) {
            return view.getTop() + this.mTopInset;
        }

        int getOpticalRight(View view) {
            return view.getRight() - this.mRightInset;
        }

        int getOpticalWidth(View view) {
            return (view.getWidth() - this.mLeftInset) - this.mRightInset;
        }

        int getOpticalHeight(View view) {
            return (view.getHeight() - this.mTopInset) - this.mBottomInset;
        }

        int getOpticalLeftInset() {
            return this.mLeftInset;
        }

        int getOpticalTopInset() {
            return this.mTopInset;
        }

        void setAlignX(int alignX) {
            this.mAlignX = alignX;
        }

        void setAlignY(int alignY) {
            this.mAlignY = alignY;
        }

        void setItemAlignmentFacet(ItemAlignmentFacet facet) {
            this.mAlignmentFacet = facet;
        }

        ItemAlignmentFacet getItemAlignmentFacet() {
            return this.mAlignmentFacet;
        }

        void calculateItemAlignments(int orientation, View view) {
            ItemAlignmentDef[] defs = this.mAlignmentFacet.getAlignmentDefs();
            if (this.mAlignMultiple == null || this.mAlignMultiple.length != defs.length) {
                this.mAlignMultiple = new int[defs.length];
            }
            for (int i = 0; i < defs.length; i++) {
                this.mAlignMultiple[i] = ItemAlignmentFacetHelper.getAlignmentPosition(view, defs[i], orientation);
            }
            if (orientation == 0) {
                this.mAlignX = this.mAlignMultiple[0];
            } else {
                this.mAlignY = this.mAlignMultiple[0];
            }
        }

        int[] getAlignMultiple() {
            return this.mAlignMultiple;
        }

        void setOpticalInsets(int leftInset, int topInset, int rightInset, int bottomInset) {
            this.mLeftInset = leftInset;
            this.mTopInset = topInset;
            this.mRightInset = rightInset;
            this.mBottomInset = bottomInset;
        }
    }

    final class PendingMoveSmoothScroller extends GridLinearSmoothScroller {
        private int mPendingMoves;
        private final boolean mStaggeredGrid;

        PendingMoveSmoothScroller(int initialPendingMoves, boolean staggeredGrid) {
            super();
            this.mPendingMoves = initialPendingMoves;
            this.mStaggeredGrid = staggeredGrid;
            setTargetPosition(-2);
        }

        void increasePendingMoves() {
            if (this.mPendingMoves < 10) {
                this.mPendingMoves++;
            }
        }

        void decreasePendingMoves() {
            if (this.mPendingMoves > -10) {
                this.mPendingMoves--;
            }
        }

        void consumePendingMovesBeforeLayout() {
            if (!this.mStaggeredGrid && this.mPendingMoves != 0) {
                int startPos;
                View newSelected = null;
                if (this.mPendingMoves > 0) {
                    startPos = GridLayoutManager.this.mFocusPosition + GridLayoutManager.this.mNumRows;
                } else {
                    startPos = GridLayoutManager.this.mFocusPosition - GridLayoutManager.this.mNumRows;
                }
                int pos = startPos;
                while (this.mPendingMoves != 0) {
                    View v = findViewByPosition(pos);
                    if (v == null) {
                        break;
                    }
                    if (GridLayoutManager.this.canScrollTo(v)) {
                        newSelected = v;
                        GridLayoutManager.this.mFocusPosition = pos;
                        GridLayoutManager.this.mSubFocusPosition = 0;
                        if (this.mPendingMoves > 0) {
                            this.mPendingMoves--;
                        } else {
                            this.mPendingMoves++;
                        }
                    }
                    pos = this.mPendingMoves > 0 ? pos + GridLayoutManager.this.mNumRows : pos - GridLayoutManager.this.mNumRows;
                }
                if (newSelected != null && GridLayoutManager.this.hasFocus()) {
                    GridLayoutManager.this.mInSelection = true;
                    newSelected.requestFocus();
                    GridLayoutManager.this.mInSelection = false;
                }
            }
        }

        void consumePendingMovesAfterLayout() {
            if (this.mStaggeredGrid && this.mPendingMoves != 0) {
                this.mPendingMoves = GridLayoutManager.this.processSelectionMoves(true, this.mPendingMoves);
            }
            if (this.mPendingMoves == 0 || ((this.mPendingMoves > 0 && GridLayoutManager.this.hasCreatedLastItem()) || (this.mPendingMoves < 0 && GridLayoutManager.this.hasCreatedFirstItem()))) {
                setTargetPosition(GridLayoutManager.this.mFocusPosition);
                stop();
            }
        }

        protected void updateActionForInterimTarget(Action action) {
            if (this.mPendingMoves != 0) {
                super.updateActionForInterimTarget(action);
            }
        }

        public PointF computeScrollVectorForPosition(int targetPosition) {
            if (this.mPendingMoves == 0) {
                return null;
            }
            int direction = (GridLayoutManager.this.mReverseFlowPrimary ? this.mPendingMoves > 0 : this.mPendingMoves < 0) ? -1 : 1;
            if (GridLayoutManager.this.mOrientation == 0) {
                return new PointF((float) direction, 0.0f);
            }
            return new PointF(0.0f, (float) direction);
        }

        protected void onStop() {
            super.onStop();
            this.mPendingMoves = 0;
            GridLayoutManager.this.mPendingMoveSmoothScroller = null;
            View v = findViewByPosition(getTargetPosition());
            if (v != null) {
                GridLayoutManager.this.scrollToView(v, true);
            }
        }
    }

    static final class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle childStates = Bundle.EMPTY;
        int index;

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.index);
            out.writeBundle(this.childStates);
        }

        public int describeContents() {
            return 0;
        }

        SavedState(Parcel in) {
            this.index = in.readInt();
            this.childStates = in.readBundle(GridLayoutManager.class.getClassLoader());
        }

        SavedState() {
        }
    }

    private String getTag() {
        return "GridLayoutManager:" + this.mBaseGridView.getId();
    }

    public GridLayoutManager(BaseGridView baseGridView) {
        this.mBaseGridView = baseGridView;
    }

    public void setOrientation(int orientation) {
        if (orientation == 0 || orientation == 1) {
            this.mOrientation = orientation;
            this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
            this.mWindowAlignment.setOrientation(orientation);
            this.mItemAlignment.setOrientation(orientation);
            this.mForceFullLayout = true;
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean z = true;
        boolean z2;
        if (this.mOrientation == 0) {
            if (layoutDirection == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mReverseFlowPrimary = z2;
            this.mReverseFlowSecondary = false;
        } else {
            if (layoutDirection == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mReverseFlowSecondary = z2;
            this.mReverseFlowPrimary = false;
        }
        Axis axis = this.mWindowAlignment.horizontal;
        if (layoutDirection != 1) {
            z = false;
        }
        axis.setReversedFlow(z);
    }

    public void setWindowAlignment(int windowAlignment) {
        this.mWindowAlignment.mainAxis().setWindowAlignment(windowAlignment);
    }

    public void setFocusOutAllowed(boolean throughFront, boolean throughEnd) {
        this.mFocusOutFront = throughFront;
        this.mFocusOutEnd = throughEnd;
    }

    public void setFocusOutSideAllowed(boolean throughStart, boolean throughEnd) {
        this.mFocusOutSideStart = throughStart;
        this.mFocusOutSideEnd = throughEnd;
    }

    public void setNumRows(int numRows) {
        if (numRows < 0) {
            throw new IllegalArgumentException();
        }
        this.mNumRowsRequested = numRows;
    }

    public void setRowHeight(int height) {
        if (height >= 0 || height == -2) {
            this.mRowSizeSecondaryRequested = height;
            return;
        }
        throw new IllegalArgumentException("Invalid row height: " + height);
    }

    public void setVerticalMargin(int margin) {
        if (this.mOrientation == 0) {
            this.mVerticalMargin = margin;
            this.mMarginSecondary = margin;
            return;
        }
        this.mVerticalMargin = margin;
        this.mMarginPrimary = margin;
    }

    public void setHorizontalMargin(int margin) {
        if (this.mOrientation == 0) {
            this.mHorizontalMargin = margin;
            this.mMarginPrimary = margin;
            return;
        }
        this.mHorizontalMargin = margin;
        this.mMarginSecondary = margin;
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    protected boolean hasDoneFirstLayout() {
        return this.mGrid != null;
    }

    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        if (listener == null) {
            this.mChildViewHolderSelectedListeners = null;
            return;
        }
        if (this.mChildViewHolderSelectedListeners == null) {
            this.mChildViewHolderSelectedListeners = new ArrayList();
        } else {
            this.mChildViewHolderSelectedListeners.clear();
        }
        this.mChildViewHolderSelectedListeners.add(listener);
    }

    boolean hasOnChildViewHolderSelectedListener() {
        if (this.mChildViewHolderSelectedListeners == null || this.mChildViewHolderSelectedListeners.size() <= 0) {
            return false;
        }
        return true;
    }

    void fireOnChildViewHolderSelected(RecyclerView parent, ViewHolder child, int position, int subposition) {
        if (this.mChildViewHolderSelectedListeners != null) {
            for (int i = this.mChildViewHolderSelectedListeners.size() - 1; i >= 0; i--) {
                ((OnChildViewHolderSelectedListener) this.mChildViewHolderSelectedListeners.get(i)).onChildViewHolderSelected(parent, child, position, subposition);
            }
        }
    }

    private int getPositionByView(View view) {
        if (view == null) {
            return -1;
        }
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null || params.isItemRemoved()) {
            return -1;
        }
        return params.getViewPosition();
    }

    private int getSubPositionByView(View view, View childView) {
        if (view == null || childView == null) {
            return 0;
        }
        ItemAlignmentFacet facet = ((LayoutParams) view.getLayoutParams()).getItemAlignmentFacet();
        if (facet != null) {
            ItemAlignmentDef[] defs = facet.getAlignmentDefs();
            if (defs.length > 1) {
                while (childView != view) {
                    int id = childView.getId();
                    if (id != -1) {
                        for (int i = 1; i < defs.length; i++) {
                            if (defs[i].getItemAlignmentFocusViewId() == id) {
                                return i;
                            }
                        }
                        continue;
                    }
                    childView = (View) childView.getParent();
                }
            }
        }
        return 0;
    }

    private int getPositionByIndex(int index) {
        return getPositionByView(getChildAt(index));
    }

    private void dispatchChildSelected() {
        if (this.mChildSelectedListener != null || hasOnChildViewHolderSelectedListener()) {
            View view = this.mFocusPosition == -1 ? null : findViewByPosition(this.mFocusPosition);
            if (view != null) {
                ViewHolder vh = this.mBaseGridView.getChildViewHolder(view);
                if (this.mChildSelectedListener != null) {
                    this.mChildSelectedListener.onChildSelected(this.mBaseGridView, view, this.mFocusPosition, vh == null ? -1 : vh.getItemId());
                }
                fireOnChildViewHolderSelected(this.mBaseGridView, vh, this.mFocusPosition, this.mSubFocusPosition);
            } else {
                if (this.mChildSelectedListener != null) {
                    this.mChildSelectedListener.onChildSelected(this.mBaseGridView, null, -1, -1);
                }
                fireOnChildViewHolderSelected(this.mBaseGridView, null, -1, 0);
            }
            if (!this.mInLayout && !this.mBaseGridView.isLayoutRequested()) {
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    if (getChildAt(i).isLayoutRequested()) {
                        forceRequestLayout();
                        break;
                    }
                }
            }
        }
    }

    public boolean canScrollHorizontally() {
        return this.mOrientation == 0 || this.mNumRows > 1;
    }

    public boolean canScrollVertically() {
        return this.mOrientation == 1 || this.mNumRows > 1;
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attrs) {
        return new LayoutParams(context, attrs);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        if (lp instanceof android.support.v7.widget.RecyclerView.LayoutParams) {
            return new LayoutParams((android.support.v7.widget.RecyclerView.LayoutParams) lp);
        }
        if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    protected View getViewForPosition(int position) {
        return this.mRecycler.getViewForPosition(position);
    }

    final int getOpticalLeft(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalLeft(v);
    }

    final int getOpticalRight(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalRight(v);
    }

    public int getDecoratedLeft(View child) {
        return ((LayoutParams) child.getLayoutParams()).mLeftInset + super.getDecoratedLeft(child);
    }

    public int getDecoratedTop(View child) {
        return ((LayoutParams) child.getLayoutParams()).mTopInset + super.getDecoratedTop(child);
    }

    public int getDecoratedRight(View child) {
        return super.getDecoratedRight(child) - ((LayoutParams) child.getLayoutParams()).mRightInset;
    }

    public int getDecoratedBottom(View child) {
        return super.getDecoratedBottom(child) - ((LayoutParams) child.getLayoutParams()).mBottomInset;
    }

    public void getDecoratedBoundsWithMargins(View view, Rect outBounds) {
        super.getDecoratedBoundsWithMargins(view, outBounds);
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        outBounds.left += params.mLeftInset;
        outBounds.top += params.mTopInset;
        outBounds.right -= params.mRightInset;
        outBounds.bottom -= params.mBottomInset;
    }

    private int getViewMin(View v) {
        return this.mOrientationHelper.getDecoratedStart(v);
    }

    private int getViewMax(View v) {
        return this.mOrientationHelper.getDecoratedEnd(v);
    }

    private int getViewPrimarySize(View view) {
        getDecoratedBoundsWithMargins(view, sTempRect);
        return this.mOrientation == 0 ? sTempRect.width() : sTempRect.height();
    }

    private int getViewCenter(View view) {
        return this.mOrientation == 0 ? getViewCenterX(view) : getViewCenterY(view);
    }

    private int getViewCenterSecondary(View view) {
        return this.mOrientation == 0 ? getViewCenterY(view) : getViewCenterX(view);
    }

    private int getViewCenterX(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        return p.getOpticalLeft(v) + p.getAlignX();
    }

    private int getViewCenterY(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        return p.getOpticalTop(v) + p.getAlignY();
    }

    private void saveContext(Recycler recycler, State state) {
        if (!(this.mRecycler == null && this.mState == null)) {
            Log.e("GridLayoutManager", "Recycler information was not released, bug!");
        }
        this.mRecycler = recycler;
        this.mState = state;
    }

    private void leaveContext() {
        this.mRecycler = null;
        this.mState = null;
    }

    private boolean layoutInit() {
        boolean focusViewWasInTree = (this.mGrid == null || this.mFocusPosition < 0 || this.mFocusPosition < this.mGrid.getFirstVisibleIndex()) ? false : this.mFocusPosition <= this.mGrid.getLastVisibleIndex();
        int newItemCount = this.mState.getItemCount();
        if (newItemCount == 0) {
            this.mFocusPosition = -1;
            this.mSubFocusPosition = 0;
        } else if (this.mFocusPosition >= newItemCount) {
            this.mFocusPosition = newItemCount - 1;
            this.mSubFocusPosition = 0;
        } else if (this.mFocusPosition == -1 && newItemCount > 0) {
            this.mFocusPosition = 0;
            this.mSubFocusPosition = 0;
        }
        if (this.mState.didStructureChange() || this.mGrid.getFirstVisibleIndex() < 0 || this.mForceFullLayout || this.mGrid == null || this.mGrid.getNumRows() != this.mNumRows) {
            this.mForceFullLayout = false;
            int firstVisibleIndex = focusViewWasInTree ? this.mGrid.getFirstVisibleIndex() : 0;
            if (this.mGrid != null && this.mNumRows == this.mGrid.getNumRows()) {
                if (this.mReverseFlowPrimary != this.mGrid.isReversedFlow()) {
                }
                initScrollController();
                updateScrollSecondAxis();
                this.mGrid.setMargin(this.mMarginPrimary);
                detachAndScrapAttachedViews(this.mRecycler);
                this.mGrid.resetVisibleIndex();
                if (this.mFocusPosition == -1) {
                    this.mBaseGridView.clearFocus();
                }
                this.mWindowAlignment.mainAxis().invalidateScrollMin();
                this.mWindowAlignment.mainAxis().invalidateScrollMax();
                if (focusViewWasInTree || firstVisibleIndex > this.mFocusPosition) {
                    this.mGrid.setStart(this.mFocusPosition);
                } else {
                    this.mGrid.setStart(firstVisibleIndex);
                }
                return false;
            }
            this.mGrid = Grid.createGrid(this.mNumRows);
            this.mGrid.setProvider(this.mGridProvider);
            this.mGrid.setReversedFlow(this.mReverseFlowPrimary);
            initScrollController();
            updateScrollSecondAxis();
            this.mGrid.setMargin(this.mMarginPrimary);
            detachAndScrapAttachedViews(this.mRecycler);
            this.mGrid.resetVisibleIndex();
            if (this.mFocusPosition == -1) {
                this.mBaseGridView.clearFocus();
            }
            this.mWindowAlignment.mainAxis().invalidateScrollMin();
            this.mWindowAlignment.mainAxis().invalidateScrollMax();
            if (focusViewWasInTree) {
            }
            this.mGrid.setStart(this.mFocusPosition);
            return false;
        }
        updateScrollController();
        updateScrollSecondAxis();
        this.mGrid.setMargin(this.mMarginPrimary);
        if (!(focusViewWasInTree || this.mFocusPosition == -1)) {
            this.mGrid.setStart(this.mFocusPosition);
        }
        return true;
    }

    private int getRowSizeSecondary(int rowIndex) {
        if (this.mFixedRowSizeSecondary != 0) {
            return this.mFixedRowSizeSecondary;
        }
        if (this.mRowSizeSecondary == null) {
            return 0;
        }
        return this.mRowSizeSecondary[rowIndex];
    }

    private int getRowStartSecondary(int rowIndex) {
        int start = 0;
        int i;
        if (this.mReverseFlowSecondary) {
            for (i = this.mNumRows - 1; i > rowIndex; i--) {
                start += getRowSizeSecondary(i) + this.mMarginSecondary;
            }
        } else {
            for (i = 0; i < rowIndex; i++) {
                start += getRowSizeSecondary(i) + this.mMarginSecondary;
            }
        }
        return start;
    }

    private int getSizeSecondary() {
        int rightmostIndex = this.mReverseFlowSecondary ? 0 : this.mNumRows - 1;
        return getRowStartSecondary(rightmostIndex) + getRowSizeSecondary(rightmostIndex);
    }

    int getDecoratedMeasuredWidthWithMargin(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        return (getDecoratedMeasuredWidth(v) + lp.leftMargin) + lp.rightMargin;
    }

    int getDecoratedMeasuredHeightWithMargin(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        return (getDecoratedMeasuredHeight(v) + lp.topMargin) + lp.bottomMargin;
    }

    private void measureScrapChild(int position, int widthSpec, int heightSpec, int[] measuredDimension) {
        View view = this.mRecycler.getViewForPosition(position);
        if (view != null) {
            LayoutParams p = (LayoutParams) view.getLayoutParams();
            calculateItemDecorationsForChild(view, sTempRect);
            view.measure(ViewGroup.getChildMeasureSpec(widthSpec, (getPaddingLeft() + getPaddingRight()) + (((p.leftMargin + p.rightMargin) + sTempRect.left) + sTempRect.right), p.width), ViewGroup.getChildMeasureSpec(heightSpec, (getPaddingTop() + getPaddingBottom()) + (((p.topMargin + p.bottomMargin) + sTempRect.top) + sTempRect.bottom), p.height));
            measuredDimension[0] = getDecoratedMeasuredWidthWithMargin(view);
            measuredDimension[1] = getDecoratedMeasuredHeightWithMargin(view);
            this.mRecycler.recycleView(view);
        }
    }

    private boolean processRowSizeSecondary(boolean measure) {
        if (this.mFixedRowSizeSecondary != 0 || this.mRowSizeSecondary == null) {
            return false;
        }
        CircularIntArray[] rows = this.mGrid == null ? null : this.mGrid.getItemPositionsInRows();
        boolean changed = false;
        int scrapChildWidth = -1;
        int scrapChildHeight = -1;
        int rowIndex = 0;
        while (rowIndex < this.mNumRows) {
            CircularIntArray row = rows == null ? null : rows[rowIndex];
            int rowItemsPairCount = row == null ? 0 : row.size();
            int rowSize = -1;
            for (int rowItemPairIndex = 0; rowItemPairIndex < rowItemsPairCount; rowItemPairIndex += 2) {
                int rowIndexStart = row.get(rowItemPairIndex);
                int rowIndexEnd = row.get(rowItemPairIndex + 1);
                for (int i = rowIndexStart; i <= rowIndexEnd; i++) {
                    View view = findViewByPosition(i);
                    if (view != null) {
                        int secondarySize;
                        if (measure) {
                            measureChild(view);
                        }
                        if (this.mOrientation == 0) {
                            secondarySize = getDecoratedMeasuredHeightWithMargin(view);
                        } else {
                            secondarySize = getDecoratedMeasuredWidthWithMargin(view);
                        }
                        if (secondarySize > rowSize) {
                            rowSize = secondarySize;
                        }
                    }
                }
            }
            int itemCount = this.mState.getItemCount();
            if (!this.mBaseGridView.hasFixedSize() && measure && rowSize < 0 && itemCount > 0) {
                if (scrapChildWidth < 0 && scrapChildHeight < 0) {
                    int position;
                    if (this.mFocusPosition == -1) {
                        position = 0;
                    } else if (this.mFocusPosition >= itemCount) {
                        position = itemCount - 1;
                    } else {
                        position = this.mFocusPosition;
                    }
                    measureScrapChild(position, MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0), this.mMeasuredDimension);
                    scrapChildWidth = this.mMeasuredDimension[0];
                    scrapChildHeight = this.mMeasuredDimension[1];
                }
                rowSize = this.mOrientation == 0 ? scrapChildHeight : scrapChildWidth;
            }
            if (rowSize < 0) {
                rowSize = 0;
            }
            if (this.mRowSizeSecondary[rowIndex] != rowSize) {
                this.mRowSizeSecondary[rowIndex] = rowSize;
                changed = true;
            }
            rowIndex++;
        }
        return changed;
    }

    private void updateRowSecondarySizeRefresh() {
        this.mRowSecondarySizeRefresh = processRowSizeSecondary(false);
        if (this.mRowSecondarySizeRefresh) {
            forceRequestLayout();
        }
    }

    private void forceRequestLayout() {
        ViewCompat.postOnAnimation(this.mBaseGridView, this.mRequestLayoutRunnable);
    }

    public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
        int sizePrimary;
        int sizeSecondary;
        int modeSecondary;
        int paddingSecondary;
        int i = 1;
        saveContext(recycler, state);
        if (this.mOrientation == 0) {
            sizePrimary = MeasureSpec.getSize(widthSpec);
            sizeSecondary = MeasureSpec.getSize(heightSpec);
            modeSecondary = MeasureSpec.getMode(heightSpec);
            paddingSecondary = getPaddingTop() + getPaddingBottom();
        } else {
            sizeSecondary = MeasureSpec.getSize(widthSpec);
            sizePrimary = MeasureSpec.getSize(heightSpec);
            modeSecondary = MeasureSpec.getMode(widthSpec);
            paddingSecondary = getPaddingLeft() + getPaddingRight();
        }
        this.mMaxSizeSecondary = sizeSecondary;
        int measuredSizeSecondary;
        if (this.mRowSizeSecondaryRequested == -2) {
            this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
            this.mFixedRowSizeSecondary = 0;
            if (this.mRowSizeSecondary == null || this.mRowSizeSecondary.length != this.mNumRows) {
                this.mRowSizeSecondary = new int[this.mNumRows];
            }
            processRowSizeSecondary(true);
            switch (modeSecondary) {
                case Integer.MIN_VALUE:
                    measuredSizeSecondary = Math.min(getSizeSecondary() + paddingSecondary, this.mMaxSizeSecondary);
                    break;
                case 0:
                    measuredSizeSecondary = getSizeSecondary() + paddingSecondary;
                    break;
                case 1073741824:
                    measuredSizeSecondary = this.mMaxSizeSecondary;
                    break;
                default:
                    throw new IllegalStateException("wrong spec");
            }
        }
        switch (modeSecondary) {
            case Integer.MIN_VALUE:
            case 1073741824:
                if (this.mNumRowsRequested == 0 && this.mRowSizeSecondaryRequested == 0) {
                    this.mNumRows = 1;
                    this.mFixedRowSizeSecondary = sizeSecondary - paddingSecondary;
                } else if (this.mNumRowsRequested == 0) {
                    this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
                    this.mNumRows = (this.mMarginSecondary + sizeSecondary) / (this.mRowSizeSecondaryRequested + this.mMarginSecondary);
                } else if (this.mRowSizeSecondaryRequested == 0) {
                    this.mNumRows = this.mNumRowsRequested;
                    this.mFixedRowSizeSecondary = ((sizeSecondary - paddingSecondary) - (this.mMarginSecondary * (this.mNumRows - 1))) / this.mNumRows;
                } else {
                    this.mNumRows = this.mNumRowsRequested;
                    this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
                }
                measuredSizeSecondary = sizeSecondary;
                if (modeSecondary == Integer.MIN_VALUE) {
                    int childrenSize = ((this.mFixedRowSizeSecondary * this.mNumRows) + (this.mMarginSecondary * (this.mNumRows - 1))) + paddingSecondary;
                    if (childrenSize < measuredSizeSecondary) {
                        measuredSizeSecondary = childrenSize;
                        break;
                    }
                }
                break;
            case 0:
                this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested == 0 ? sizeSecondary - paddingSecondary : this.mRowSizeSecondaryRequested;
                if (this.mNumRowsRequested != 0) {
                    i = this.mNumRowsRequested;
                }
                this.mNumRows = i;
                measuredSizeSecondary = ((this.mFixedRowSizeSecondary * this.mNumRows) + (this.mMarginSecondary * (this.mNumRows - 1))) + paddingSecondary;
                break;
            default:
                throw new IllegalStateException("wrong spec");
        }
        if (this.mOrientation == 0) {
            setMeasuredDimension(sizePrimary, measuredSizeSecondary);
        } else {
            setMeasuredDimension(measuredSizeSecondary, sizePrimary);
        }
        leaveContext();
    }

    private void measureChild(View child) {
        int secondarySpec;
        int widthSpec;
        int heightSpec;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        calculateItemDecorationsForChild(child, sTempRect);
        int widthUsed = ((lp.leftMargin + lp.rightMargin) + sTempRect.left) + sTempRect.right;
        int heightUsed = ((lp.topMargin + lp.bottomMargin) + sTempRect.top) + sTempRect.bottom;
        if (this.mRowSizeSecondaryRequested == -2) {
            secondarySpec = MeasureSpec.makeMeasureSpec(0, 0);
        } else {
            secondarySpec = MeasureSpec.makeMeasureSpec(this.mFixedRowSizeSecondary, 1073741824);
        }
        if (this.mOrientation == 0) {
            widthSpec = ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), widthUsed, lp.width);
            heightSpec = ViewGroup.getChildMeasureSpec(secondarySpec, heightUsed, lp.height);
        } else {
            heightSpec = ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), heightUsed, lp.height);
            widthSpec = ViewGroup.getChildMeasureSpec(secondarySpec, widthUsed, lp.width);
        }
        child.measure(widthSpec, heightSpec);
    }

    private <E> E getFacet(ViewHolder vh, Class<? extends E> facetClass) {
        E facet = null;
        if (vh instanceof FacetProvider) {
            facet = ((FacetProvider) vh).getFacet(facetClass);
        }
        if (facet != null || this.mFacetProviderAdapter == null) {
            return facet;
        }
        FacetProvider p = this.mFacetProviderAdapter.getFacetProvider(vh.getItemViewType());
        if (p != null) {
            return p.getFacet(facetClass);
        }
        return facet;
    }

    private void layoutChild(int rowIndex, View v, int start, int end, int startSecondary) {
        int sizeSecondary;
        int left;
        int top;
        int right;
        int bottom;
        if (this.mOrientation == 0) {
            sizeSecondary = getDecoratedMeasuredHeightWithMargin(v);
        } else {
            sizeSecondary = getDecoratedMeasuredWidthWithMargin(v);
        }
        if (this.mFixedRowSizeSecondary > 0) {
            sizeSecondary = Math.min(sizeSecondary, this.mFixedRowSizeSecondary);
        }
        int verticalGravity = this.mGravity & 112;
        int horizontalGravity;
        if (this.mReverseFlowPrimary || this.mReverseFlowSecondary) {
            horizontalGravity = Gravity.getAbsoluteGravity(this.mGravity & 8388615, 1);
        } else {
            horizontalGravity = this.mGravity & 7;
        }
        if (!((this.mOrientation == 0 && verticalGravity == 48) || (this.mOrientation == 1 && horizontalGravity == 3))) {
            if ((this.mOrientation == 0 && verticalGravity == 80) || (this.mOrientation == 1 && horizontalGravity == 5)) {
                startSecondary += getRowSizeSecondary(rowIndex) - sizeSecondary;
            } else {
                if (!(this.mOrientation == 0 && verticalGravity == 16)) {
                    if (this.mOrientation == 1 && horizontalGravity == 1) {
                    }
                }
                startSecondary += (getRowSizeSecondary(rowIndex) - sizeSecondary) / 2;
            }
        }
        if (this.mOrientation == 0) {
            left = start;
            top = startSecondary;
            right = end;
            bottom = startSecondary + sizeSecondary;
        } else {
            top = start;
            left = startSecondary;
            bottom = end;
            right = startSecondary + sizeSecondary;
        }
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        layoutDecoratedWithMargins(v, left, top, right, bottom);
        super.getDecoratedBoundsWithMargins(v, sTempRect);
        params.setOpticalInsets(left - sTempRect.left, top - sTempRect.top, sTempRect.right - right, sTempRect.bottom - bottom);
        updateChildAlignments(v);
    }

    private void updateChildAlignments(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        if (p.getItemAlignmentFacet() == null) {
            p.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(v));
            p.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(v));
            return;
        }
        p.calculateItemAlignments(this.mOrientation, v);
        if (this.mOrientation == 0) {
            p.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(v));
        } else {
            p.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(v));
        }
    }

    private void removeInvisibleViewsAtEnd() {
        if (this.mPruneChild) {
            this.mGrid.removeInvisibleItemsAtEnd(this.mFocusPosition, this.mReverseFlowPrimary ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace);
        }
    }

    private void removeInvisibleViewsAtFront() {
        if (this.mPruneChild) {
            this.mGrid.removeInvisibleItemsAtFront(this.mFocusPosition, this.mReverseFlowPrimary ? this.mSizePrimary + this.mExtraLayoutSpace : -this.mExtraLayoutSpace);
        }
    }

    private boolean appendOneColumnVisibleItems() {
        return this.mGrid.appendOneColumnVisibleItems();
    }

    private boolean prependOneColumnVisibleItems() {
        return this.mGrid.prependOneColumnVisibleItems();
    }

    private void appendVisibleItems() {
        int i;
        Grid grid = this.mGrid;
        if (this.mReverseFlowPrimary) {
            i = -this.mExtraLayoutSpace;
        } else {
            i = this.mSizePrimary + this.mExtraLayoutSpace;
        }
        grid.appendVisibleItems(i);
    }

    private void prependVisibleItems() {
        int i;
        Grid grid = this.mGrid;
        if (this.mReverseFlowPrimary) {
            i = this.mSizePrimary + this.mExtraLayoutSpace;
        } else {
            i = -this.mExtraLayoutSpace;
        }
        grid.prependVisibleItems(i);
    }

    private void fastRelayout() {
        boolean invalidateAfter = false;
        int childCount = getChildCount();
        int position = -1;
        for (int index = 0; index < childCount; index++) {
            View view = getChildAt(index);
            position = getPositionByIndex(index);
            Location location = this.mGrid.getLocation(position);
            if (location == null) {
                invalidateAfter = true;
                break;
            }
            int primarySize;
            int end;
            int startSecondary = getRowStartSecondary(location.row) - this.mScrollOffsetSecondary;
            int start = getViewMin(view);
            int oldPrimarySize = getViewPrimarySize(view);
            if (((LayoutParams) view.getLayoutParams()).viewNeedsUpdate()) {
                int viewIndex = this.mBaseGridView.indexOfChild(view);
                detachAndScrapView(view, this.mRecycler);
                view = getViewForPosition(position);
                addView(view, viewIndex);
            }
            measureChild(view);
            if (this.mOrientation == 0) {
                primarySize = getDecoratedMeasuredWidthWithMargin(view);
                end = start + primarySize;
            } else {
                primarySize = getDecoratedMeasuredHeightWithMargin(view);
                end = start + primarySize;
            }
            layoutChild(location.row, view, start, end, startSecondary);
            if (oldPrimarySize != primarySize) {
                invalidateAfter = true;
                break;
            }
        }
        if (invalidateAfter) {
            int savedLastPos = this.mGrid.getLastVisibleIndex();
            this.mGrid.invalidateItemsAfter(position);
            if (!this.mPruneChild) {
                while (this.mGrid.appendOneColumnVisibleItems()) {
                    if (this.mGrid.getLastVisibleIndex() >= savedLastPos) {
                        break;
                    }
                }
            }
            appendVisibleItems();
            if (this.mFocusPosition >= 0 && this.mFocusPosition <= savedLastPos) {
                while (this.mGrid.getLastVisibleIndex() < this.mFocusPosition) {
                    this.mGrid.appendOneColumnVisibleItems();
                }
            }
        }
        updateScrollMin();
        updateScrollMax();
        updateScrollSecondAxis();
    }

    public void removeAndRecycleAllViews(Recycler recycler) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
    }

    public void onLayoutChildren(Recycler recycler, State state) {
        if (this.mNumRows == 0 || state.getItemCount() < 0) {
            return;
        }
        if (this.mLayoutEnabled) {
            this.mInLayout = true;
            if (state.didStructureChange()) {
                this.mBaseGridView.stopScroll();
            }
            boolean scrollToFocus = !isSmoothScrolling() ? this.mFocusScrollStrategy == 0 : false;
            if (!(this.mFocusPosition == -1 || this.mFocusPositionOffset == Integer.MIN_VALUE)) {
                this.mFocusPosition += this.mFocusPositionOffset;
                this.mSubFocusPosition = 0;
            }
            this.mFocusPositionOffset = 0;
            saveContext(recycler, state);
            View savedFocusView = findViewByPosition(this.mFocusPosition);
            int savedFocusPos = this.mFocusPosition;
            int savedSubFocusPos = this.mSubFocusPosition;
            boolean hadFocus = this.mBaseGridView.hasFocus();
            int delta = 0;
            int deltaSecondary = 0;
            if (!(this.mFocusPosition == -1 || !scrollToFocus || this.mBaseGridView.getScrollState() == 0 || savedFocusView == null || !getScrollPosition(savedFocusView, savedFocusView.findFocus(), sTwoInts))) {
                delta = sTwoInts[0];
                deltaSecondary = sTwoInts[1];
            }
            boolean layoutInit = layoutInit();
            this.mInFastRelayout = layoutInit;
            View focusView;
            if (!layoutInit) {
                this.mInLayoutSearchFocus = hadFocus;
                if (this.mFocusPosition != -1) {
                    while (appendOneColumnVisibleItems()) {
                        if (findViewByPosition(this.mFocusPosition) != null) {
                            break;
                        }
                    }
                }
                while (true) {
                    updateScrollMin();
                    updateScrollMax();
                    int oldFirstVisible = this.mGrid.getFirstVisibleIndex();
                    int oldLastVisible = this.mGrid.getLastVisibleIndex();
                    focusView = findViewByPosition(this.mFocusPosition);
                    scrollToView(focusView, false);
                    if (!(focusView == null || !hadFocus || focusView.hasFocus())) {
                        focusView.requestFocus();
                    }
                    appendVisibleItems();
                    prependVisibleItems();
                    removeInvisibleViewsAtFront();
                    removeInvisibleViewsAtEnd();
                    if (this.mGrid.getFirstVisibleIndex() == oldFirstVisible) {
                        if (this.mGrid.getLastVisibleIndex() == oldLastVisible) {
                            break;
                        }
                    }
                }
            } else {
                fastRelayout();
                if (this.mFocusPosition != -1) {
                    focusView = findViewByPosition(this.mFocusPosition);
                    if (focusView != null) {
                        if (scrollToFocus) {
                            scrollToView(focusView, false);
                        }
                        if (hadFocus && !focusView.hasFocus()) {
                            focusView.requestFocus();
                        }
                    }
                }
            }
            if (scrollToFocus) {
                scrollDirectionPrimary(-delta);
                scrollDirectionSecondary(-deltaSecondary);
            }
            appendVisibleItems();
            prependVisibleItems();
            removeInvisibleViewsAtFront();
            removeInvisibleViewsAtEnd();
            if (this.mRowSecondarySizeRefresh) {
                this.mRowSecondarySizeRefresh = false;
            } else {
                updateRowSecondarySizeRefresh();
            }
            if (this.mInFastRelayout && (this.mFocusPosition != savedFocusPos || this.mSubFocusPosition != savedSubFocusPos || findViewByPosition(this.mFocusPosition) != savedFocusView)) {
                dispatchChildSelected();
            } else if (!this.mInFastRelayout && this.mInLayoutSearchFocus) {
                dispatchChildSelected();
            }
            this.mInLayout = false;
            leaveContext();
            if (!(hadFocus || this.mInFastRelayout || !this.mBaseGridView.hasFocusable())) {
                ViewCompat.postOnAnimation(this.mBaseGridView, this.mAskFocusRunnable);
            }
            return;
        }
        discardLayoutInfo();
        removeAndRecycleAllViews(recycler);
    }

    private void offsetChildrenSecondary(int increment) {
        int childCount = getChildCount();
        int i;
        if (this.mOrientation == 0) {
            for (i = 0; i < childCount; i++) {
                getChildAt(i).offsetTopAndBottom(increment);
            }
            return;
        }
        for (i = 0; i < childCount; i++) {
            getChildAt(i).offsetLeftAndRight(increment);
        }
    }

    private void offsetChildrenPrimary(int increment) {
        int childCount = getChildCount();
        int i;
        if (this.mOrientation == 1) {
            for (i = 0; i < childCount; i++) {
                getChildAt(i).offsetTopAndBottom(increment);
            }
            return;
        }
        for (i = 0; i < childCount; i++) {
            getChildAt(i).offsetLeftAndRight(increment);
        }
    }

    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        if (!this.mLayoutEnabled || !hasDoneFirstLayout()) {
            return 0;
        }
        int result;
        saveContext(recycler, state);
        this.mInScroll = true;
        if (this.mOrientation == 0) {
            result = scrollDirectionPrimary(dx);
        } else {
            result = scrollDirectionSecondary(dx);
        }
        leaveContext();
        this.mInScroll = false;
        return result;
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        if (!this.mLayoutEnabled || !hasDoneFirstLayout()) {
            return 0;
        }
        int result;
        this.mInScroll = true;
        saveContext(recycler, state);
        if (this.mOrientation == 1) {
            result = scrollDirectionPrimary(dy);
        } else {
            result = scrollDirectionSecondary(dy);
        }
        leaveContext();
        this.mInScroll = false;
        return result;
    }

    private int scrollDirectionPrimary(int da) {
        int i = 0;
        if (da > 0) {
            if (!this.mWindowAlignment.mainAxis().isMaxUnknown()) {
                int maxScroll = this.mWindowAlignment.mainAxis().getMaxScroll();
                if (this.mScrollOffsetPrimary + da > maxScroll) {
                    da = maxScroll - this.mScrollOffsetPrimary;
                }
            }
        } else if (da < 0 && !this.mWindowAlignment.mainAxis().isMinUnknown()) {
            int minScroll = this.mWindowAlignment.mainAxis().getMinScroll();
            if (this.mScrollOffsetPrimary + da < minScroll) {
                da = minScroll - this.mScrollOffsetPrimary;
            }
        }
        if (da == 0) {
            return 0;
        }
        offsetChildrenPrimary(-da);
        this.mScrollOffsetPrimary += da;
        if (this.mInLayout) {
            return da;
        }
        int childCount = getChildCount();
        if (this.mReverseFlowPrimary ? da > 0 : da < 0) {
            prependVisibleItems();
        } else {
            appendVisibleItems();
        }
        boolean updated = getChildCount() > childCount;
        childCount = getChildCount();
        if (this.mReverseFlowPrimary ? da > 0 : da < 0) {
            removeInvisibleViewsAtEnd();
        } else {
            removeInvisibleViewsAtFront();
        }
        if (getChildCount() < childCount) {
            i = 1;
        }
        if (updated | i) {
            updateRowSecondarySizeRefresh();
        }
        this.mBaseGridView.invalidate();
        return da;
    }

    private int scrollDirectionSecondary(int dy) {
        if (dy == 0) {
            return 0;
        }
        offsetChildrenSecondary(-dy);
        this.mScrollOffsetSecondary += dy;
        this.mBaseGridView.invalidate();
        return dy;
    }

    private void updateScrollMax() {
        int highVisiblePos;
        if (this.mReverseFlowPrimary) {
            highVisiblePos = this.mGrid.getFirstVisibleIndex();
        } else {
            highVisiblePos = this.mGrid.getLastVisibleIndex();
        }
        int highMaxPos = !this.mReverseFlowPrimary ? this.mState.getItemCount() - 1 : 0;
        if (highVisiblePos >= 0) {
            boolean highAvailable = highVisiblePos == highMaxPos;
            boolean maxUnknown = this.mWindowAlignment.mainAxis().isMaxUnknown();
            if (highAvailable || !maxUnknown) {
                int maxEdge = this.mGrid.findRowMax(true, sTwoInts) + this.mScrollOffsetPrimary;
                int rowIndex = sTwoInts[0];
                int pos = sTwoInts[1];
                int savedMaxEdge = this.mWindowAlignment.mainAxis().getMaxEdge();
                this.mWindowAlignment.mainAxis().setMaxEdge(maxEdge);
                int maxScroll = getPrimarySystemScrollPositionOfChildMax(findViewByPosition(pos));
                this.mWindowAlignment.mainAxis().setMaxEdge(savedMaxEdge);
                if (highAvailable) {
                    this.mWindowAlignment.mainAxis().setMaxEdge(maxEdge);
                    this.mWindowAlignment.mainAxis().setMaxScroll(maxScroll);
                } else {
                    this.mWindowAlignment.mainAxis().invalidateScrollMax();
                }
            }
        }
    }

    private void updateScrollMin() {
        int lowVisiblePos;
        if (this.mReverseFlowPrimary) {
            lowVisiblePos = this.mGrid.getLastVisibleIndex();
        } else {
            lowVisiblePos = this.mGrid.getFirstVisibleIndex();
        }
        int lowMinPos = !this.mReverseFlowPrimary ? 0 : this.mState.getItemCount() - 1;
        if (lowVisiblePos >= 0) {
            boolean lowAvailable = lowVisiblePos == lowMinPos;
            boolean minUnknown = this.mWindowAlignment.mainAxis().isMinUnknown();
            if (lowAvailable || !minUnknown) {
                int minEdge = this.mGrid.findRowMin(false, sTwoInts) + this.mScrollOffsetPrimary;
                int rowIndex = sTwoInts[0];
                int pos = sTwoInts[1];
                int savedMinEdge = this.mWindowAlignment.mainAxis().getMinEdge();
                this.mWindowAlignment.mainAxis().setMinEdge(minEdge);
                int minScroll = getPrimarySystemScrollPosition(findViewByPosition(pos));
                this.mWindowAlignment.mainAxis().setMinEdge(savedMinEdge);
                if (lowAvailable) {
                    this.mWindowAlignment.mainAxis().setMinEdge(minEdge);
                    this.mWindowAlignment.mainAxis().setMinScroll(minScroll);
                } else {
                    this.mWindowAlignment.mainAxis().invalidateScrollMin();
                }
            }
        }
    }

    private void updateScrollSecondAxis() {
        this.mWindowAlignment.secondAxis().setMinEdge(0);
        this.mWindowAlignment.secondAxis().setMaxEdge(getSizeSecondary());
    }

    private void initScrollController() {
        this.mWindowAlignment.reset();
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
        this.mScrollOffsetPrimary = -this.mWindowAlignment.mainAxis().getPaddingLow();
        this.mScrollOffsetSecondary = -this.mWindowAlignment.secondAxis().getPaddingLow();
    }

    private void updateScrollController() {
        int paddingPrimaryDiff;
        int paddingSecondaryDiff;
        if (this.mOrientation == 0) {
            paddingPrimaryDiff = getPaddingLeft() - this.mWindowAlignment.horizontal.getPaddingLow();
            paddingSecondaryDiff = getPaddingTop() - this.mWindowAlignment.vertical.getPaddingLow();
        } else {
            paddingPrimaryDiff = getPaddingTop() - this.mWindowAlignment.vertical.getPaddingLow();
            paddingSecondaryDiff = getPaddingLeft() - this.mWindowAlignment.horizontal.getPaddingLow();
        }
        this.mScrollOffsetPrimary -= paddingPrimaryDiff;
        this.mScrollOffsetSecondary -= paddingSecondaryDiff;
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
    }

    public void scrollToPosition(int position) {
        setSelection(position, 0, false, 0);
    }

    public void setSelection(int position, int primaryScrollExtra) {
        setSelection(position, 0, false, primaryScrollExtra);
    }

    public void setSelectionSmooth(int position) {
        setSelection(position, 0, true, 0);
    }

    public int getSelection() {
        return this.mFocusPosition;
    }

    public void setSelection(int position, int subposition, boolean smooth, int primaryScrollExtra) {
        if ((this.mFocusPosition == position || position == -1) && subposition == this.mSubFocusPosition) {
            if (primaryScrollExtra == this.mPrimaryScrollExtra) {
                return;
            }
        }
        scrollToSelection(position, subposition, smooth, primaryScrollExtra);
    }

    private void scrollToSelection(int position, int subposition, boolean smooth, int primaryScrollExtra) {
        this.mPrimaryScrollExtra = primaryScrollExtra;
        View view = findViewByPosition(position);
        if (view != null) {
            this.mInSelection = true;
            scrollToView(view, smooth);
            this.mInSelection = false;
        } else {
            this.mFocusPosition = position;
            this.mSubFocusPosition = subposition;
            this.mFocusPositionOffset = Integer.MIN_VALUE;
            if (!this.mLayoutEnabled) {
                return;
            }
            if (!smooth) {
                this.mForceFullLayout = true;
                requestLayout();
            } else if (hasDoneFirstLayout()) {
                startPositionSmoothScroller(position);
            } else {
                Log.w(getTag(), "setSelectionSmooth should not be called before first layout pass");
            }
        }
    }

    void startPositionSmoothScroller(int position) {
        LinearSmoothScroller linearSmoothScroller = new GridLinearSmoothScroller(this) {
            public PointF computeScrollVectorForPosition(int targetPosition) {
                boolean isStart = true;
                if (getChildCount() == 0) {
                    return null;
                }
                int firstChildPos = this.getPosition(this.getChildAt(0));
                if (this.mReverseFlowPrimary) {
                    if (targetPosition <= firstChildPos) {
                        isStart = false;
                    }
                } else if (targetPosition >= firstChildPos) {
                    isStart = false;
                }
                int direction = isStart ? -1 : 1;
                if (this.mOrientation == 0) {
                    return new PointF((float) direction, 0.0f);
                }
                return new PointF(0.0f, (float) direction);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    private void processPendingMovement(boolean forward) {
        boolean z = true;
        if (!(forward ? hasCreatedLastItem() : hasCreatedFirstItem())) {
            if (this.mPendingMoveSmoothScroller == null) {
                this.mBaseGridView.stopScroll();
                int i = forward ? 1 : -1;
                if (this.mNumRows <= 1) {
                    z = false;
                }
                PendingMoveSmoothScroller linearSmoothScroller = new PendingMoveSmoothScroller(i, z);
                this.mFocusPositionOffset = 0;
                startSmoothScroll(linearSmoothScroller);
                if (linearSmoothScroller.isRunning()) {
                    this.mPendingMoveSmoothScroller = linearSmoothScroller;
                }
            } else if (forward) {
                this.mPendingMoveSmoothScroller.increasePendingMoves();
            } else {
                this.mPendingMoveSmoothScroller.decreasePendingMoves();
            }
        }
    }

    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        if (!(this.mFocusPosition == -1 || this.mGrid == null || this.mGrid.getFirstVisibleIndex() < 0 || this.mFocusPositionOffset == Integer.MIN_VALUE || positionStart > this.mFocusPosition + this.mFocusPositionOffset)) {
            this.mFocusPositionOffset += itemCount;
        }
        this.mChildrenStates.clear();
    }

    public void onItemsChanged(RecyclerView recyclerView) {
        this.mFocusPositionOffset = 0;
        this.mChildrenStates.clear();
    }

    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        if (!(this.mFocusPosition == -1 || this.mGrid == null || this.mGrid.getFirstVisibleIndex() < 0 || this.mFocusPositionOffset == Integer.MIN_VALUE)) {
            int pos = this.mFocusPosition + this.mFocusPositionOffset;
            if (positionStart <= pos) {
                if (positionStart + itemCount > pos) {
                    this.mFocusPositionOffset = Integer.MIN_VALUE;
                } else {
                    this.mFocusPositionOffset -= itemCount;
                }
            }
        }
        this.mChildrenStates.clear();
    }

    public void onItemsMoved(RecyclerView recyclerView, int fromPosition, int toPosition, int itemCount) {
        if (!(this.mFocusPosition == -1 || this.mFocusPositionOffset == Integer.MIN_VALUE)) {
            int pos = this.mFocusPosition + this.mFocusPositionOffset;
            if (fromPosition <= pos && pos < fromPosition + itemCount) {
                this.mFocusPositionOffset += toPosition - fromPosition;
            } else if (fromPosition < pos && toPosition > pos - itemCount) {
                this.mFocusPositionOffset -= itemCount;
            } else if (fromPosition > pos && toPosition < pos) {
                this.mFocusPositionOffset += itemCount;
            }
        }
        this.mChildrenStates.clear();
    }

    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        int end = positionStart + itemCount;
        for (int i = positionStart; i < end; i++) {
            this.mChildrenStates.remove(i);
        }
    }

    public boolean onRequestChildFocus(RecyclerView parent, View child, View focused) {
        if (!(this.mFocusSearchDisabled || getPositionByView(child) == -1 || this.mInLayout || this.mInSelection || this.mInScroll)) {
            scrollToView(child, focused, true);
        }
        return true;
    }

    public boolean requestChildRectangleOnScreen(RecyclerView parent, View view, Rect rect, boolean immediate) {
        return false;
    }

    private int getPrimarySystemScrollPosition(View view) {
        boolean isMax;
        boolean isMin;
        int viewCenterPrimary = this.mScrollOffsetPrimary + getViewCenter(view);
        int viewMin = getViewMin(view);
        int viewMax = getViewMax(view);
        if (this.mReverseFlowPrimary) {
            isMax = this.mGrid.getFirstVisibleIndex() == 0;
            isMin = this.mGrid.getLastVisibleIndex() == (this.mState == null ? getItemCount() : this.mState.getItemCount()) + -1;
        } else {
            isMin = this.mGrid.getFirstVisibleIndex() == 0;
            isMax = this.mGrid.getLastVisibleIndex() == (this.mState == null ? getItemCount() : this.mState.getItemCount()) + -1;
        }
        int i = getChildCount() - 1;
        while (true) {
            if ((isMin || isMax) && i >= 0) {
                View v = getChildAt(i);
                if (!(v == view || v == null)) {
                    if (isMin && getViewMin(v) < viewMin) {
                        isMin = false;
                    }
                    if (isMax && getViewMax(v) > viewMax) {
                        isMax = false;
                    }
                }
                i--;
            }
        }
        return this.mWindowAlignment.mainAxis().getSystemScrollPos(viewCenterPrimary, isMin, isMax);
    }

    private int getPrimarySystemScrollPositionOfChildMax(View view) {
        int scrollPosition = getPrimarySystemScrollPosition(view);
        int[] multipleAligns = ((LayoutParams) view.getLayoutParams()).getAlignMultiple();
        if (multipleAligns == null || multipleAligns.length <= 0) {
            return scrollPosition;
        }
        return scrollPosition + (multipleAligns[multipleAligns.length - 1] - multipleAligns[0]);
    }

    private int getAdjustedPrimaryScrollPosition(int scrollPrimary, View view, View childView) {
        int subindex = getSubPositionByView(view, childView);
        if (subindex == 0) {
            return scrollPrimary;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        return scrollPrimary + (lp.getAlignMultiple()[subindex] - lp.getAlignMultiple()[0]);
    }

    private int getSecondarySystemScrollPosition(View view) {
        boolean isMax;
        boolean isMin;
        int viewCenterSecondary = this.mScrollOffsetSecondary + getViewCenterSecondary(view);
        int row = this.mGrid.getLocation(getPositionByView(view)).row;
        if (this.mReverseFlowSecondary) {
            isMax = row == 0;
            isMin = row == this.mGrid.getNumRows() + -1;
        } else {
            isMin = row == 0;
            isMax = row == this.mGrid.getNumRows() + -1;
        }
        return this.mWindowAlignment.secondAxis().getSystemScrollPos(viewCenterSecondary, isMin, isMax);
    }

    private void scrollToView(View view, boolean smooth) {
        View view2 = null;
        if (view != null) {
            view2 = view.findFocus();
        }
        scrollToView(view, view2, smooth);
    }

    private void scrollToView(View view, View childView, boolean smooth) {
        int newFocusPosition = getPositionByView(view);
        int newSubFocusPosition = getSubPositionByView(view, childView);
        if (!(newFocusPosition == this.mFocusPosition && newSubFocusPosition == this.mSubFocusPosition)) {
            this.mFocusPosition = newFocusPosition;
            this.mSubFocusPosition = newSubFocusPosition;
            this.mFocusPositionOffset = 0;
            if (!this.mInLayout) {
                dispatchChildSelected();
            }
            if (this.mBaseGridView.isChildrenDrawingOrderEnabledInternal()) {
                this.mBaseGridView.invalidate();
            }
        }
        if (view != null) {
            if (!view.hasFocus() && this.mBaseGridView.hasFocus()) {
                view.requestFocus();
            }
            if ((this.mScrollEnabled || !smooth) && getScrollPosition(view, childView, sTwoInts)) {
                scrollGrid(sTwoInts[0], sTwoInts[1], smooth);
            }
        }
    }

    private boolean getScrollPosition(View view, View childView, int[] deltas) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return getNoneAlignedPosition(view, deltas);
            default:
                return getAlignedPosition(view, childView, deltas);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean getNoneAlignedPosition(View view, int[] deltas) {
        View secondaryAlignedView;
        int pos = getPositionByView(view);
        int viewMin = getViewMin(view);
        int viewMax = getViewMax(view);
        View firstView = null;
        View lastView = null;
        int paddingLow = this.mWindowAlignment.mainAxis().getPaddingLow();
        int clientSize = this.mWindowAlignment.mainAxis().getClientSize();
        int row = this.mGrid.getRowIndex(pos);
        CircularIntArray positions;
        if (viewMin < paddingLow) {
            firstView = view;
            if (this.mFocusScrollStrategy == 2) {
                while (prependOneColumnVisibleItems()) {
                    positions = this.mGrid.getItemPositionsInRows(this.mGrid.getFirstVisibleIndex(), pos)[row];
                    firstView = findViewByPosition(positions.get(0));
                    if (viewMax - getViewMin(firstView) > clientSize) {
                        if (positions.size() > 2) {
                            firstView = findViewByPosition(positions.get(2));
                        }
                    }
                }
            }
        } else if (viewMax > clientSize + paddingLow) {
            if (this.mFocusScrollStrategy == 2) {
                firstView = view;
                while (true) {
                    positions = this.mGrid.getItemPositionsInRows(pos, this.mGrid.getLastVisibleIndex())[row];
                    lastView = findViewByPosition(positions.get(positions.size() - 1));
                    if (getViewMax(lastView) - viewMin <= clientSize) {
                        if (!appendOneColumnVisibleItems()) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (lastView != null) {
                    firstView = null;
                }
            } else {
                lastView = view;
            }
        }
        int scrollPrimary = 0;
        if (firstView != null) {
            scrollPrimary = getViewMin(firstView) - paddingLow;
        } else if (lastView != null) {
            scrollPrimary = getViewMax(lastView) - (paddingLow + clientSize);
        }
        if (firstView != null) {
            secondaryAlignedView = firstView;
        } else if (lastView != null) {
            secondaryAlignedView = lastView;
        } else {
            secondaryAlignedView = view;
        }
        int scrollSecondary = getSecondarySystemScrollPosition(secondaryAlignedView) - this.mScrollOffsetSecondary;
        if (scrollPrimary == 0 && scrollSecondary == 0) {
            return false;
        }
        deltas[0] = scrollPrimary;
        deltas[1] = scrollSecondary;
        return true;
    }

    private boolean getAlignedPosition(View view, View childView, int[] deltas) {
        int scrollPrimary = getPrimarySystemScrollPosition(view);
        if (childView != null) {
            scrollPrimary = getAdjustedPrimaryScrollPosition(scrollPrimary, view, childView);
        }
        int scrollSecondary = getSecondarySystemScrollPosition(view) - this.mScrollOffsetSecondary;
        scrollPrimary = (scrollPrimary - this.mScrollOffsetPrimary) + this.mPrimaryScrollExtra;
        if (scrollPrimary == 0 && scrollSecondary == 0) {
            return false;
        }
        deltas[0] = scrollPrimary;
        deltas[1] = scrollSecondary;
        return true;
    }

    private void scrollGrid(int scrollPrimary, int scrollSecondary, boolean smooth) {
        if (this.mInLayout) {
            scrollDirectionPrimary(scrollPrimary);
            scrollDirectionSecondary(scrollSecondary);
            return;
        }
        int scrollX;
        int scrollY;
        if (this.mOrientation == 0) {
            scrollX = scrollPrimary;
            scrollY = scrollSecondary;
        } else {
            scrollX = scrollSecondary;
            scrollY = scrollPrimary;
        }
        if (smooth) {
            this.mBaseGridView.smoothScrollBy(scrollX, scrollY);
        } else {
            this.mBaseGridView.scrollBy(scrollX, scrollY);
        }
    }

    private int findImmediateChildIndex(View view) {
        if (!(this.mBaseGridView == null || view == this.mBaseGridView)) {
            view = findContainingItemView(view);
            if (view != null) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    if (getChildAt(i) == view) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            int i = this.mFocusPosition;
            while (true) {
                View view = findViewByPosition(i);
                if (view != null) {
                    if (view.getVisibility() == 0 && view.hasFocusable()) {
                        view.requestFocus();
                        return;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    public View onInterceptFocusSearch(View focused, int direction) {
        if (this.mFocusSearchDisabled) {
            return focused;
        }
        FocusFinder ff = FocusFinder.getInstance();
        View result = null;
        if (direction == 2 || direction == 1) {
            if (canScrollVertically()) {
                result = ff.findNextFocus(this.mBaseGridView, focused, direction == 2 ? 130 : 33);
            }
            if (canScrollHorizontally()) {
                int i;
                boolean rtl = getLayoutDirection() == 1;
                if (direction == 2) {
                    i = 1;
                } else {
                    i = 0;
                }
                result = ff.findNextFocus(this.mBaseGridView, focused, (i ^ rtl) != 0 ? 66 : 17);
            }
        } else {
            result = ff.findNextFocus(this.mBaseGridView, focused, direction);
        }
        if (result != null) {
            return result;
        }
        int movement = getMovement(direction);
        boolean isScroll = this.mBaseGridView.getScrollState() != 0;
        if (movement == 1) {
            if (isScroll || !this.mFocusOutEnd) {
                result = focused;
            }
            if (this.mScrollEnabled && !hasCreatedLastItem()) {
                processPendingMovement(true);
                result = focused;
            }
        } else if (movement == 0) {
            if (isScroll || !this.mFocusOutFront) {
                result = focused;
            }
            if (this.mScrollEnabled && !hasCreatedFirstItem()) {
                processPendingMovement(false);
                result = focused;
            }
        } else if (movement == 3) {
            if (isScroll || !this.mFocusOutSideEnd) {
                result = focused;
            }
        } else if (movement == 2 && (isScroll || !this.mFocusOutSideStart)) {
            result = focused;
        }
        if (result != null) {
            return result;
        }
        result = this.mBaseGridView.getParent().focusSearch(focused, direction);
        if (result != null) {
            return result;
        }
        if (focused == null) {
            focused = this.mBaseGridView;
        }
        return focused;
    }

    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        if (this.mFocusSearchDisabled) {
            return true;
        }
        int focusableCount;
        int i;
        View child;
        if (!recyclerView.hasFocus()) {
            focusableCount = views.size();
            if (this.mFocusScrollStrategy != 0) {
                int left = this.mWindowAlignment.mainAxis().getPaddingLow();
                int right = this.mWindowAlignment.mainAxis().getClientSize() + left;
                int count = getChildCount();
                for (i = 0; i < count; i++) {
                    child = getChildAt(i);
                    if (child.getVisibility() == 0 && getViewMin(child) >= left && getViewMax(child) <= right) {
                        child.addFocusables(views, direction, focusableMode);
                    }
                }
                if (views.size() == focusableCount) {
                    count = getChildCount();
                    for (i = 0; i < count; i++) {
                        child = getChildAt(i);
                        if (child.getVisibility() == 0) {
                            child.addFocusables(views, direction, focusableMode);
                        }
                    }
                }
            } else {
                View view = findViewByPosition(this.mFocusPosition);
                if (view != null) {
                    view.addFocusables(views, direction, focusableMode);
                }
            }
            if (views.size() != focusableCount) {
                return true;
            }
            if (recyclerView.isFocusable()) {
                views.add(recyclerView);
            }
        } else if (this.mPendingMoveSmoothScroller != null) {
            return true;
        } else {
            int movement = getMovement(direction);
            int focusedIndex = findImmediateChildIndex(recyclerView.findFocus());
            int focusedPos = getPositionByIndex(focusedIndex);
            if (focusedPos != -1) {
                findViewByPosition(focusedPos).addFocusables(views, direction, focusableMode);
            }
            if (this.mGrid == null || getChildCount() == 0) {
                return true;
            }
            if ((movement == 3 || movement == 2) && this.mGrid.getNumRows() <= 1) {
                return true;
            }
            int focusedRow = (this.mGrid == null || focusedPos == -1) ? -1 : this.mGrid.getLocation(focusedPos).row;
            focusableCount = views.size();
            int inc = (movement == 1 || movement == 3) ? 1 : -1;
            int loop_end = inc > 0 ? getChildCount() - 1 : 0;
            int loop_start = focusedIndex == -1 ? inc > 0 ? 0 : getChildCount() - 1 : focusedIndex + inc;
            i = loop_start;
            while (true) {
                if (inc <= 0) {
                    if (i < loop_end) {
                        break;
                    }
                } else if (i > loop_end) {
                    break;
                }
                child = getChildAt(i);
                if (child.getVisibility() == 0 && child.hasFocusable()) {
                    if (focusedPos == -1) {
                        child.addFocusables(views, direction, focusableMode);
                        if (views.size() > focusableCount) {
                            break;
                        }
                    } else {
                        int position = getPositionByIndex(i);
                        Location loc = this.mGrid.getLocation(position);
                        if (loc != null) {
                            if (movement != 1) {
                                if (movement != 0) {
                                    if (movement != 3) {
                                        if (movement == 2 && loc.row != focusedRow) {
                                            if (loc.row > focusedRow) {
                                                break;
                                            }
                                            child.addFocusables(views, direction, focusableMode);
                                        }
                                    } else if (loc.row != focusedRow) {
                                        if (loc.row < focusedRow) {
                                            break;
                                        }
                                        child.addFocusables(views, direction, focusableMode);
                                    } else {
                                        continue;
                                    }
                                } else if (loc.row == focusedRow && position < focusedPos) {
                                    child.addFocusables(views, direction, focusableMode);
                                    if (views.size() > focusableCount) {
                                        break;
                                    }
                                }
                            } else if (loc.row == focusedRow && position > focusedPos) {
                                child.addFocusables(views, direction, focusableMode);
                                if (views.size() > focusableCount) {
                                    break;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                }
                i += inc;
            }
        }
        return true;
    }

    private boolean hasCreatedLastItem() {
        int count = getItemCount();
        if (count == 0 || this.mBaseGridView.findViewHolderForAdapterPosition(count - 1) != null) {
            return true;
        }
        return false;
    }

    private boolean hasCreatedFirstItem() {
        if (getItemCount() == 0 || this.mBaseGridView.findViewHolderForAdapterPosition(0) != null) {
            return true;
        }
        return false;
    }

    boolean canScrollTo(View view) {
        if (view.getVisibility() == 0) {
            return hasFocus() ? view.hasFocusable() : true;
        } else {
            return false;
        }
    }

    boolean gridOnRequestFocusInDescendants(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return gridOnRequestFocusInDescendantsUnaligned(recyclerView, direction, previouslyFocusedRect);
            default:
                return gridOnRequestFocusInDescendantsAligned(recyclerView, direction, previouslyFocusedRect);
        }
    }

    private boolean gridOnRequestFocusInDescendantsAligned(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        View view = findViewByPosition(this.mFocusPosition);
        if (view == null) {
            return false;
        }
        boolean result = view.requestFocus(direction, previouslyFocusedRect);
        return !result ? result : result;
    }

    private boolean gridOnRequestFocusInDescendantsUnaligned(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = getChildCount();
        if ((direction & 2) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
        int left = this.mWindowAlignment.mainAxis().getPaddingLow();
        int right = this.mWindowAlignment.mainAxis().getClientSize() + left;
        for (int i = index; i != end; i += increment) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0 && getViewMin(child) >= left && getViewMax(child) <= right && child.requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return false;
    }

    private int getMovement(int direction) {
        if (this.mOrientation == 0) {
            switch (direction) {
                case 17:
                    return !this.mReverseFlowPrimary ? 0 : 1;
                case 33:
                    return 2;
                case 66:
                    return !this.mReverseFlowPrimary ? 1 : 0;
                case 130:
                    return 3;
                default:
                    return 17;
            }
        } else if (this.mOrientation != 1) {
            return 17;
        } else {
            switch (direction) {
                case 17:
                    return !this.mReverseFlowSecondary ? 2 : 3;
                case 33:
                    return 0;
                case 66:
                    return !this.mReverseFlowSecondary ? 3 : 2;
                case 130:
                    return 1;
                default:
                    return 17;
            }
        }
    }

    int getChildDrawingOrder(RecyclerView recyclerView, int childCount, int i) {
        View view = findViewByPosition(this.mFocusPosition);
        if (view == null) {
            return i;
        }
        int focusIndex = recyclerView.indexOfChild(view);
        if (i < focusIndex) {
            return i;
        }
        if (i < childCount - 1) {
            return ((focusIndex + childCount) - 1) - i;
        }
        return focusIndex;
    }

    public void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter) {
        if (oldAdapter != null) {
            discardLayoutInfo();
            this.mFocusPosition = -1;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.clear();
        }
        if (newAdapter instanceof FacetProviderAdapter) {
            this.mFacetProviderAdapter = (FacetProviderAdapter) newAdapter;
        } else {
            this.mFacetProviderAdapter = null;
        }
        super.onAdapterChanged(oldAdapter, newAdapter);
    }

    private void discardLayoutInfo() {
        this.mGrid = null;
        this.mRowSizeSecondary = null;
        this.mRowSecondarySizeRefresh = false;
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState();
        ss.index = getSelection();
        Bundle bundle = this.mChildrenStates.saveAsBundle();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            int position = getPositionByView(view);
            if (position != -1) {
                bundle = this.mChildrenStates.saveOnScreenView(bundle, view, position);
            }
        }
        ss.childStates = bundle;
        return ss;
    }

    void onChildRecycled(ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (position != -1) {
            this.mChildrenStates.saveOffscreenView(holder.itemView, position);
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState loadingState = (SavedState) state;
            this.mFocusPosition = loadingState.index;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.loadFromBundle(loadingState.childStates);
            this.mForceFullLayout = true;
            requestLayout();
        }
    }

    public int getRowCountForAccessibility(Recycler recycler, State state) {
        if (this.mOrientation != 0 || this.mGrid == null) {
            return super.getRowCountForAccessibility(recycler, state);
        }
        return this.mGrid.getNumRows();
    }

    public int getColumnCountForAccessibility(Recycler recycler, State state) {
        if (this.mOrientation != 1 || this.mGrid == null) {
            return super.getColumnCountForAccessibility(recycler, state);
        }
        return this.mGrid.getNumRows();
    }

    public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
        android.view.ViewGroup.LayoutParams lp = host.getLayoutParams();
        if (this.mGrid == null || !(lp instanceof LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(recycler, state, host, info);
            return;
        }
        int position = ((LayoutParams) lp).getViewLayoutPosition();
        int rowIndex = this.mGrid.getRowIndex(position);
        int guessSpanIndex = position / this.mGrid.getNumRows();
        if (this.mOrientation == 0) {
            info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(rowIndex, 1, guessSpanIndex, 1, false, false));
        } else {
            info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(guessSpanIndex, 1, rowIndex, 1, false, false));
        }
    }

    public boolean performAccessibilityAction(Recycler recycler, State state, int action, Bundle args) {
        saveContext(recycler, state);
        switch (action) {
            case 4096:
                processSelectionMoves(false, this.mState.getItemCount());
                break;
            case 8192:
                processSelectionMoves(false, -this.mState.getItemCount());
                break;
        }
        leaveContext();
        return true;
    }

    private int processSelectionMoves(boolean preventScroll, int moves) {
        if (this.mGrid == null) {
            return moves;
        }
        int focusPosition = this.mFocusPosition;
        int focusedRow = focusPosition != -1 ? this.mGrid.getRowIndex(focusPosition) : -1;
        View newSelected = null;
        int i = 0;
        int count = getChildCount();
        while (i < count && moves != 0) {
            int index = moves > 0 ? i : (count - 1) - i;
            View child = getChildAt(index);
            if (canScrollTo(child)) {
                int position = getPositionByIndex(index);
                int rowIndex = this.mGrid.getRowIndex(position);
                if (focusedRow == -1) {
                    focusPosition = position;
                    newSelected = child;
                    focusedRow = rowIndex;
                } else if (rowIndex == focusedRow) {
                    if (moves <= 0 || position <= focusPosition) {
                        if (moves < 0 && position < focusPosition) {
                        }
                    }
                    focusPosition = position;
                    newSelected = child;
                    if (moves > 0) {
                        moves--;
                    } else {
                        moves++;
                    }
                }
            }
            i++;
        }
        if (newSelected != null) {
            if (preventScroll) {
                if (hasFocus()) {
                    this.mInSelection = true;
                    newSelected.requestFocus();
                    this.mInSelection = false;
                }
                this.mFocusPosition = focusPosition;
                this.mSubFocusPosition = 0;
            } else {
                scrollToView(newSelected, true);
            }
        }
        return moves;
    }

    public void onInitializeAccessibilityNodeInfo(Recycler recycler, State state, AccessibilityNodeInfoCompat info) {
        saveContext(recycler, state);
        if (this.mScrollEnabled && !hasCreatedFirstItem()) {
            info.addAction(8192);
            info.setScrollable(true);
        }
        if (this.mScrollEnabled && !hasCreatedLastItem()) {
            info.addAction(4096);
            info.setScrollable(true);
        }
        info.setCollectionInfo(CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        leaveContext();
    }
}
