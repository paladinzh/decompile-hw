package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v17.leanback.R$styleable;
import android.support.v7.widget.RecyclerView.RecyclerListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class VerticalGridView extends BaseGridView {
    public /* bridge */ /* synthetic */ boolean dispatchGenericFocusedEvent(MotionEvent event) {
        return super.dispatchGenericFocusedEvent(event);
    }

    public /* bridge */ /* synthetic */ boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    public /* bridge */ /* synthetic */ boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public /* bridge */ /* synthetic */ View focusSearch(int direction) {
        return super.focusSearch(direction);
    }

    public /* bridge */ /* synthetic */ int getChildDrawingOrder(int childCount, int i) {
        return super.getChildDrawingOrder(childCount, i);
    }

    public /* bridge */ /* synthetic */ int getSelectedPosition() {
        return super.getSelectedPosition();
    }

    public /* bridge */ /* synthetic */ boolean hasOverlappingRendering() {
        return super.hasOverlappingRendering();
    }

    public /* bridge */ /* synthetic */ boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    public /* bridge */ /* synthetic */ void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
    }

    public /* bridge */ /* synthetic */ void setGravity(int gravity) {
        super.setGravity(gravity);
    }

    public /* bridge */ /* synthetic */ void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        super.setOnChildViewHolderSelectedListener(listener);
    }

    public /* bridge */ /* synthetic */ void setRecyclerListener(RecyclerListener listener) {
        super.setRecyclerListener(listener);
    }

    public /* bridge */ /* synthetic */ void setSelectedPosition(int position) {
        super.setSelectedPosition(position);
    }

    public /* bridge */ /* synthetic */ void setSelectedPositionSmooth(int position) {
        super.setSelectedPositionSmooth(position);
    }

    public /* bridge */ /* synthetic */ void setWindowAlignment(int windowAlignment) {
        super.setWindowAlignment(windowAlignment);
    }

    public VerticalGridView(Context context) {
        this(context, null);
    }

    public VerticalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLayoutManager.setOrientation(1);
        initAttributes(context, attrs);
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        initBaseGridViewAttributes(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.lbVerticalGridView);
        setColumnWidth(a);
        setNumColumns(a.getInt(R$styleable.lbVerticalGridView_numberOfColumns, 1));
        a.recycle();
    }

    void setColumnWidth(TypedArray array) {
        if (array.peekValue(R$styleable.lbVerticalGridView_columnWidth) != null) {
            setColumnWidth(array.getLayoutDimension(R$styleable.lbVerticalGridView_columnWidth, 0));
        }
    }

    public void setNumColumns(int numColumns) {
        this.mLayoutManager.setNumRows(numColumns);
        requestLayout();
    }

    public void setColumnWidth(int width) {
        this.mLayoutManager.setRowHeight(width);
        requestLayout();
    }
}
