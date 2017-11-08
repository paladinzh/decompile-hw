package com.huawei.gallery.editor.category;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.gallery3d.app.TransitionStore;
import com.huawei.gallery.editor.step.EditorStep;

public class BaseViewAdapter extends ArrayAdapter<Action> {
    protected View mContainer;
    protected EditorStep mEditorStep;
    protected OnSelectedChangedListener mOnSelectedChangedListener;
    protected int mSelectedPosition;

    public interface OnSelectedChangedListener {
        void onRepeatOnClick(int i, BaseViewAdapter baseViewAdapter);

        void onSelectedChanged(int i, BaseViewAdapter baseViewAdapter);
    }

    public BaseViewAdapter(Context context) {
        super(context, 0);
        this.mSelectedPosition = -1;
    }

    public void saveUIController(TransitionStore transitionStore) {
        transitionStore.put("menu_select_index", Integer.valueOf(this.mSelectedPosition));
    }

    public void restoreUIController(TransitionStore transitionStore) {
        invalidateView(((Integer) transitionStore.get("menu_select_index")).intValue());
    }

    public BaseViewAdapter(Context context, int resource) {
        super(context, resource);
        this.mSelectedPosition = -1;
    }

    public BaseViewAdapter(Context context, BaseViewAdapter baseViewAdapter) {
        this(context);
        for (int i = 0; i < baseViewAdapter.getCount(); i++) {
            add((Action) baseViewAdapter.getItem(i));
        }
    }

    public void setEditorStep(EditorStep editorStep) {
        this.mEditorStep = editorStep;
    }

    public EditorStep getEditorStep() {
        return this.mEditorStep;
    }

    public void setSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void add(Action action) {
        super.add(action);
        action.setAdapter(this);
    }

    public void remove(Action action) {
        super.remove(action);
    }

    public void insert(Action action, int index) {
        super.insert(action, index);
        action.setAdapter(this);
    }

    public void initializeSelection() {
    }

    public void setSelected(View v) {
        int old = this.mSelectedPosition;
        setSelectedPosition(((Integer) v.getTag()).intValue());
        if (old != -1) {
            invalidateView(old);
        }
        invalidateView(this.mSelectedPosition);
    }

    protected void setSelectedPosition(int position) {
        if (this.mSelectedPosition != position) {
            this.mSelectedPosition = position;
            if (this.mOnSelectedChangedListener != null) {
                this.mOnSelectedChangedListener.onSelectedChanged(this.mSelectedPosition, this);
            }
        } else if (this.mOnSelectedChangedListener != null) {
            this.mOnSelectedChangedListener.onRepeatOnClick(this.mSelectedPosition, this);
        }
    }

    protected void invalidateView(int position) {
        View child = null;
        if (this.mContainer instanceof ListView) {
            ListView lv = this.mContainer;
            child = lv.getChildAt(position - lv.getFirstVisiblePosition());
        } else if (this.mContainer instanceof ViewGroup) {
            child = this.mContainer.getChildAt(position);
        }
        if (child != null) {
            invalidate(child);
        }
    }

    protected void invalidate(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                invalidate(((ViewGroup) view).getChildAt(i));
            }
            return;
        }
        view.invalidate();
    }

    public boolean isSelected(View v) {
        return ((Integer) v.getTag()).intValue() == this.mSelectedPosition;
    }

    public void setContainer(View container) {
        this.mContainer = container;
    }

    public void imageLoaded() {
        notifyDataSetChanged();
    }

    public void resetAllRepresentation() {
        for (int i = 0; i < getCount(); i++) {
            ((Action) getItem(i)).getRepresentation().reset();
        }
    }

    public void resetUnSelectedRepresentation() {
        for (int i = 0; i < getCount(); i++) {
            ((Action) getItem(i)).getRepresentation().reset();
        }
    }

    public void resetActionImage() {
        for (int i = 0; i < getCount(); i++) {
            ((Action) getItem(i)).resetBitmap();
        }
    }

    public Action getSelectedAction() {
        if (this.mSelectedPosition < 0 || this.mSelectedPosition >= getCount()) {
            return null;
        }
        return (Action) getItem(this.mSelectedPosition);
    }

    public void clearSelection() {
        int old = this.mSelectedPosition;
        this.mSelectedPosition = -1;
        invalidateView(old);
    }

    public void clear() {
        resetActionImage();
        super.clear();
    }
}
