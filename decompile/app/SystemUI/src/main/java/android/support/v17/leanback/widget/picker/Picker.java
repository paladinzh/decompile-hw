package android.support.v17.leanback.widget.picker;

import android.content.Context;
import android.graphics.Rect;
import android.support.v17.leanback.R$dimen;
import android.support.v17.leanback.R$id;
import android.support.v17.leanback.R$layout;
import android.support.v17.leanback.widget.OnChildViewHolderSelectedListener;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class Picker extends FrameLayout {
    private Interpolator mAccelerateInterpolator;
    private int mAlphaAnimDuration;
    private final OnChildViewHolderSelectedListener mColumnChangeListener = new OnChildViewHolderSelectedListener() {
        public void onChildViewHolderSelected(RecyclerView parent, android.support.v7.widget.RecyclerView.ViewHolder child, int position, int subposition) {
            PickerScrollArrayAdapter pickerScrollArrayAdapter = (PickerScrollArrayAdapter) parent.getAdapter();
            int colIndex = Picker.this.mColumnViews.indexOf(parent);
            Picker.this.updateColumnAlpha(colIndex, true);
            if (child != null) {
                Picker.this.onColumnValueChanged(colIndex, ((PickerColumn) Picker.this.mColumns.get(colIndex)).getMinValue() + position);
            }
        }
    };
    private final List<VerticalGridView> mColumnViews = new ArrayList();
    private ArrayList<PickerColumn> mColumns;
    private Interpolator mDecelerateInterpolator;
    private float mFocusedAlpha;
    private float mInvisibleColumnAlpha;
    private ArrayList<PickerValueListener> mListeners;
    private int mPickerItemLayoutId = R$layout.lb_picker_item;
    private int mPickerItemTextViewId = 0;
    private ViewGroup mPickerView;
    private ViewGroup mRootView;
    private int mSelectedColumn = 0;
    private CharSequence mSeparator;
    private float mUnfocusedAlpha;
    private float mVisibleColumnAlpha;
    private float mVisibleItems = 1.0f;
    private float mVisibleItemsActivated = 3.0f;

    class PickerScrollArrayAdapter extends Adapter<ViewHolder> {
        private final int mColIndex;
        private PickerColumn mData;
        private final int mResource;
        private final int mTextViewResourceId;

        PickerScrollArrayAdapter(Context context, int resource, int textViewResourceId, int colIndex) {
            this.mResource = resource;
            this.mColIndex = colIndex;
            this.mTextViewResourceId = textViewResourceId;
            this.mData = (PickerColumn) Picker.this.mColumns.get(this.mColIndex);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView;
            View v = LayoutInflater.from(parent.getContext()).inflate(this.mResource, parent, false);
            if (this.mTextViewResourceId != 0) {
                textView = (TextView) v.findViewById(this.mTextViewResourceId);
            } else {
                textView = (TextView) v;
            }
            return new ViewHolder(v, textView);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            boolean z;
            if (!(holder.textView == null || this.mData == null)) {
                holder.textView.setText(this.mData.getLabelFor(this.mData.getMinValue() + position));
            }
            Picker picker = Picker.this;
            View view = holder.itemView;
            if (((VerticalGridView) Picker.this.mColumnViews.get(this.mColIndex)).getSelectedPosition() == position) {
                z = true;
            } else {
                z = false;
            }
            picker.setOrAnimateAlpha(view, z, this.mColIndex, false);
        }

        public void onViewAttachedToWindow(ViewHolder holder) {
            holder.itemView.setFocusable(Picker.this.isActivated());
        }

        public int getItemCount() {
            return this.mData == null ? 0 : this.mData.getCount();
        }
    }

    public interface PickerValueListener {
        void onValueChanged(Picker picker, int i);
    }

    static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(View v, TextView textView) {
            super(v);
            this.textView = textView;
        }
    }

    public final CharSequence getSeparator() {
        return this.mSeparator;
    }

    public final void setSeparator(CharSequence seperator) {
        this.mSeparator = seperator;
    }

    public final int getPickerItemLayoutId() {
        return this.mPickerItemLayoutId;
    }

    public final int getPickerItemTextViewId() {
        return this.mPickerItemTextViewId;
    }

    public Picker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setEnabled(true);
        this.mFocusedAlpha = 1.0f;
        this.mUnfocusedAlpha = 1.0f;
        this.mVisibleColumnAlpha = 0.5f;
        this.mInvisibleColumnAlpha = 0.0f;
        this.mAlphaAnimDuration = 200;
        this.mDecelerateInterpolator = new DecelerateInterpolator(2.5f);
        this.mAccelerateInterpolator = new AccelerateInterpolator(2.5f);
        this.mRootView = (ViewGroup) LayoutInflater.from(getContext()).inflate(R$layout.lb_picker, this, true);
        this.mPickerView = (ViewGroup) this.mRootView.findViewById(R$id.picker);
    }

    public PickerColumn getColumnAt(int colIndex) {
        if (this.mColumns == null) {
            return null;
        }
        return (PickerColumn) this.mColumns.get(colIndex);
    }

    public int getColumnsCount() {
        if (this.mColumns == null) {
            return 0;
        }
        return this.mColumns.size();
    }

    public void setColumns(List<PickerColumn> columns) {
        this.mColumnViews.clear();
        this.mPickerView.removeAllViews();
        this.mColumns = new ArrayList(columns);
        if (this.mSelectedColumn > this.mColumns.size() - 1) {
            this.mSelectedColumn = this.mColumns.size() - 1;
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int totalCol = getColumnsCount();
        for (int i = 0; i < totalCol; i++) {
            int colIndex = i;
            VerticalGridView columnView = (VerticalGridView) inflater.inflate(R$layout.lb_picker_column, this.mPickerView, false);
            updateColumnSize(columnView);
            columnView.setWindowAlignment(0);
            columnView.setHasFixedSize(false);
            this.mColumnViews.add(columnView);
            this.mPickerView.addView(columnView);
            if (!(i == totalCol - 1 || getSeparator() == null)) {
                TextView separator = (TextView) inflater.inflate(R$layout.lb_picker_separator, this.mPickerView, false);
                separator.setText(getSeparator());
                this.mPickerView.addView(separator);
            }
            columnView.setAdapter(new PickerScrollArrayAdapter(getContext(), getPickerItemLayoutId(), getPickerItemTextViewId(), colIndex));
            columnView.setOnChildViewHolderSelectedListener(this.mColumnChangeListener);
        }
    }

    public void setColumnAt(int columnIndex, PickerColumn column) {
        this.mColumns.set(columnIndex, column);
        VerticalGridView columnView = (VerticalGridView) this.mColumnViews.get(columnIndex);
        PickerScrollArrayAdapter adapter = (PickerScrollArrayAdapter) columnView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        columnView.setSelectedPosition(column.getCurrentValue() - column.getMinValue());
    }

    public void setColumnValue(int columnIndex, int value, boolean runAnimation) {
        PickerColumn column = (PickerColumn) this.mColumns.get(columnIndex);
        if (column.getCurrentValue() != value) {
            column.setCurrentValue(value);
            notifyValueChanged(columnIndex);
            VerticalGridView columnView = (VerticalGridView) this.mColumnViews.get(columnIndex);
            if (columnView != null) {
                int position = value - ((PickerColumn) this.mColumns.get(columnIndex)).getMinValue();
                if (runAnimation) {
                    columnView.setSelectedPositionSmooth(position);
                } else {
                    columnView.setSelectedPosition(position);
                }
            }
        }
    }

    private void notifyValueChanged(int columnIndex) {
        if (this.mListeners != null) {
            for (int i = this.mListeners.size() - 1; i >= 0; i--) {
                ((PickerValueListener) this.mListeners.get(i)).onValueChanged(this, columnIndex);
            }
        }
    }

    private void updateColumnAlpha(int colIndex, boolean animate) {
        VerticalGridView column = (VerticalGridView) this.mColumnViews.get(colIndex);
        int selected = column.getSelectedPosition();
        int i = 0;
        while (i < column.getAdapter().getItemCount()) {
            View item = column.getLayoutManager().findViewByPosition(i);
            if (item != null) {
                setOrAnimateAlpha(item, selected == i, colIndex, animate);
            }
            i++;
        }
    }

    private void setOrAnimateAlpha(View view, boolean selected, int colIndex, boolean animate) {
        boolean columnShownAsActivated = colIndex == this.mSelectedColumn || !hasFocus();
        if (selected) {
            if (columnShownAsActivated) {
                setOrAnimateAlpha(view, animate, this.mFocusedAlpha, -1.0f, this.mDecelerateInterpolator);
            } else {
                setOrAnimateAlpha(view, animate, this.mUnfocusedAlpha, -1.0f, this.mDecelerateInterpolator);
            }
        } else if (columnShownAsActivated) {
            setOrAnimateAlpha(view, animate, this.mVisibleColumnAlpha, -1.0f, this.mDecelerateInterpolator);
        } else {
            setOrAnimateAlpha(view, animate, this.mInvisibleColumnAlpha, -1.0f, this.mDecelerateInterpolator);
        }
    }

    private void setOrAnimateAlpha(View view, boolean animate, float destAlpha, float startAlpha, Interpolator interpolator) {
        view.animate().cancel();
        if (animate) {
            if (startAlpha >= 0.0f) {
                view.setAlpha(startAlpha);
            }
            view.animate().alpha(destAlpha).setDuration((long) this.mAlphaAnimDuration).setInterpolator(interpolator).start();
            return;
        }
        view.setAlpha(destAlpha);
    }

    public void onColumnValueChanged(int columnIndex, int newValue) {
        PickerColumn column = (PickerColumn) this.mColumns.get(columnIndex);
        if (column.getCurrentValue() != newValue) {
            column.setCurrentValue(newValue);
            notifyValueChanged(columnIndex);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!isActivated()) {
            return super.dispatchKeyEvent(event);
        }
        switch (event.getKeyCode()) {
            case 23:
            case 66:
                if (event.getAction() == 1) {
                    performClick();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int column = getSelectedColumn();
        if (column < this.mColumnViews.size()) {
            return ((VerticalGridView) this.mColumnViews.get(column)).requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    protected int getPickerItemHeightPixels() {
        return getContext().getResources().getDimensionPixelSize(R$dimen.picker_item_height);
    }

    private void updateColumnSize() {
        for (int i = 0; i < getColumnsCount(); i++) {
            updateColumnSize((VerticalGridView) this.mColumnViews.get(i));
        }
    }

    private void updateColumnSize(VerticalGridView columnView) {
        LayoutParams lp = columnView.getLayoutParams();
        lp.height = (int) ((isActivated() ? getActivatedVisibleItemCount() : getVisibleItemCount()) * ((float) getPickerItemHeightPixels()));
        columnView.setLayoutParams(lp);
    }

    private void updateItemFocusable() {
        boolean activated = isActivated();
        for (int i = 0; i < getColumnsCount(); i++) {
            VerticalGridView grid = (VerticalGridView) this.mColumnViews.get(i);
            for (int j = 0; j < grid.getChildCount(); j++) {
                grid.getChildAt(j).setFocusable(activated);
            }
        }
    }

    public float getActivatedVisibleItemCount() {
        return this.mVisibleItemsActivated;
    }

    public float getVisibleItemCount() {
        return 1.0f;
    }

    public void setActivated(boolean activated) {
        if (activated != isActivated()) {
            super.setActivated(activated);
            updateColumnSize();
            updateItemFocusable();
            return;
        }
        super.setActivated(activated);
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        for (int i = 0; i < this.mColumnViews.size(); i++) {
            if (((VerticalGridView) this.mColumnViews.get(i)).hasFocus()) {
                setSelectedColumn(i);
            }
        }
    }

    public void setSelectedColumn(int columnIndex) {
        if (this.mSelectedColumn != columnIndex) {
            this.mSelectedColumn = columnIndex;
            for (int i = 0; i < this.mColumnViews.size(); i++) {
                updateColumnAlpha(i, true);
            }
        }
    }

    public int getSelectedColumn() {
        return this.mSelectedColumn;
    }
}
