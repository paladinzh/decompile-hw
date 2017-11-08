package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.RecyclerListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

abstract class BaseGridView extends RecyclerView {
    private boolean mAnimateChildLayout = true;
    private RecyclerListener mChainedRecyclerListener;
    private boolean mHasOverlappingRendering = true;
    final GridLayoutManager mLayoutManager = new GridLayoutManager(this);
    private OnKeyInterceptListener mOnKeyInterceptListener;
    private OnMotionInterceptListener mOnMotionInterceptListener;
    private OnTouchInterceptListener mOnTouchInterceptListener;
    private OnUnhandledKeyListener mOnUnhandledKeyListener;

    public interface OnKeyInterceptListener {
        boolean onInterceptKeyEvent(KeyEvent keyEvent);
    }

    public interface OnMotionInterceptListener {
        boolean onInterceptMotionEvent(MotionEvent motionEvent);
    }

    public interface OnTouchInterceptListener {
        boolean onInterceptTouchEvent(MotionEvent motionEvent);
    }

    public interface OnUnhandledKeyListener {
        boolean onUnhandledKey(KeyEvent keyEvent);
    }

    public BaseGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(this.mLayoutManager);
        setDescendantFocusability(262144);
        setHasFixedSize(true);
        setChildrenDrawingOrderEnabled(true);
        setWillNotDraw(true);
        setOverScrollMode(2);
        ((SimpleItemAnimator) getItemAnimator()).setSupportsChangeAnimations(false);
        super.setRecyclerListener(new RecyclerListener() {
            public void onViewRecycled(ViewHolder holder) {
                BaseGridView.this.mLayoutManager.onChildRecycled(holder);
                if (BaseGridView.this.mChainedRecyclerListener != null) {
                    BaseGridView.this.mChainedRecyclerListener.onViewRecycled(holder);
                }
            }
        });
    }

    protected void initBaseGridViewAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.lbBaseGridView);
        this.mLayoutManager.setFocusOutAllowed(a.getBoolean(R$styleable.lbBaseGridView_focusOutFront, false), a.getBoolean(R$styleable.lbBaseGridView_focusOutEnd, false));
        this.mLayoutManager.setFocusOutSideAllowed(a.getBoolean(R$styleable.lbBaseGridView_focusOutSideStart, true), a.getBoolean(R$styleable.lbBaseGridView_focusOutSideEnd, true));
        this.mLayoutManager.setVerticalMargin(a.getDimensionPixelSize(R$styleable.lbBaseGridView_verticalMargin, 0));
        this.mLayoutManager.setHorizontalMargin(a.getDimensionPixelSize(R$styleable.lbBaseGridView_horizontalMargin, 0));
        if (a.hasValue(R$styleable.lbBaseGridView_android_gravity)) {
            setGravity(a.getInt(R$styleable.lbBaseGridView_android_gravity, 0));
        }
        a.recycle();
    }

    public void setWindowAlignment(int windowAlignment) {
        this.mLayoutManager.setWindowAlignment(windowAlignment);
        requestLayout();
    }

    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        this.mLayoutManager.setOnChildViewHolderSelectedListener(listener);
    }

    public void setSelectedPosition(int position) {
        this.mLayoutManager.setSelection(position, 0);
    }

    public void setSelectedPositionSmooth(int position) {
        this.mLayoutManager.setSelectionSmooth(position);
    }

    public int getSelectedPosition() {
        return this.mLayoutManager.getSelection();
    }

    public void setGravity(int gravity) {
        this.mLayoutManager.setGravity(gravity);
        requestLayout();
    }

    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return this.mLayoutManager.gridOnRequestFocusInDescendants(this, direction, previouslyFocusedRect);
    }

    public int getChildDrawingOrder(int childCount, int i) {
        return this.mLayoutManager.getChildDrawingOrder(this, childCount, i);
    }

    final boolean isChildrenDrawingOrderEnabledInternal() {
        return isChildrenDrawingOrderEnabled();
    }

    public View focusSearch(int direction) {
        if (isFocused()) {
            View view = this.mLayoutManager.findViewByPosition(this.mLayoutManager.getSelection());
            if (view != null) {
                return focusSearch(view, direction);
            }
        }
        return super.focusSearch(direction);
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        this.mLayoutManager.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((this.mOnKeyInterceptListener != null && this.mOnKeyInterceptListener.onInterceptKeyEvent(event)) || super.dispatchKeyEvent(event)) {
            return true;
        }
        if (this.mOnUnhandledKeyListener == null || !this.mOnUnhandledKeyListener.onUnhandledKey(event)) {
            return false;
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.mOnTouchInterceptListener == null || !this.mOnTouchInterceptListener.onInterceptTouchEvent(event)) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    public boolean dispatchGenericFocusedEvent(MotionEvent event) {
        if (this.mOnMotionInterceptListener == null || !this.mOnMotionInterceptListener.onInterceptMotionEvent(event)) {
            return super.dispatchGenericFocusedEvent(event);
        }
        return true;
    }

    public boolean hasOverlappingRendering() {
        return this.mHasOverlappingRendering;
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        this.mLayoutManager.onRtlPropertiesChanged(layoutDirection);
    }

    public void setRecyclerListener(RecyclerListener listener) {
        this.mChainedRecyclerListener = listener;
    }
}
