package com.huawei.gallery.editor.category;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import com.huawei.gallery.editor.ui.AspectInfo;
import com.huawei.gallery.util.ColorfulUtils;

public class EditorTextViewAdapter extends BaseViewAdapter {
    private boolean mFreezeSelection = false;

    public EditorTextViewAdapter(Context context, BaseViewAdapter baseViewAdapter) {
        super(context, baseViewAdapter);
    }

    public void setFreezeSelection(boolean freeze) {
        this.mFreezeSelection = freeze;
    }

    public void initializeSelection() {
        setSelectedPosition(0);
        invalidateView(0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new EditorTextView(getContext());
        }
        Action action = (Action) getItem(position);
        EditorTextView view = (EditorTextView) convertView;
        view.setAttributes(new IconData(view.getId(), action.getRepresentation().getOverlayId(), action.getRepresentation().getTextId()), position, getCount());
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!EditorTextViewAdapter.this.mFreezeSelection) {
                    EditorTextViewAdapter.this.setSelected(view);
                }
            }
        });
        view.setTag(Integer.valueOf(position));
        view.invalidate();
        return view;
    }

    protected void invalidateView(int position) {
        View child;
        if (this.mContainer instanceof ListView) {
            ListView lv = this.mContainer;
            child = lv.getChildAt(position - lv.getFirstVisiblePosition());
        } else {
            child = this.mContainer.getChildAt(position);
        }
        if (child != null) {
            if (child instanceof EditorTextView) {
                if (((Integer) child.getTag()).intValue() == this.mSelectedPosition) {
                    ((EditorTextView) child).setCompoundDrawablesWithIntrinsicBounds(null, ColorfulUtils.mappingColorfulDrawableForce(getContext(), ((Action) getItem(position)).getRepresentation().getOverlayPressedId()), null, null);
                    child.setSelected(true);
                } else {
                    ((EditorTextView) child).setTextColor(AspectInfo.getAlphaColor());
                    ((EditorTextView) child).setCompoundDrawablesWithIntrinsicBounds(0, ((Action) getItem(position)).getRepresentation().getOverlayId(), 0, 0);
                    child.setSelected(false);
                }
            }
            child.invalidate();
        }
    }
}
