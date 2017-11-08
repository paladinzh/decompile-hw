package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.BasePaintMenu.MenuClickListener;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.LayoutHelper;

public class PaintFilterMenu extends LinearLayout implements OnClickListener {
    private int mCurrentSelectedViewId = -1;
    protected MenuClickListener mMenuClickListener;

    public PaintFilterMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isCurrentSelect(int id) {
        return id == this.mCurrentSelectedViewId;
    }

    public void addChildTextView(int id, int nameId, int i, int size) {
        IconData iconData = new IconData(id, 0, nameId);
        EditorTextView editorTextView = new EditorTextView(getContext());
        editorTextView.setId(EditorUtils.getViewId(i));
        editorTextView.setAttributes(iconData, i, size, true, true);
        editorTextView.setTextSize(12.0f);
        editorTextView.getLayoutParams().height = GalleryUtils.dpToPixel(32);
        if (LayoutHelper.isPort()) {
            editorTextView.setMaxWidth(Integer.MAX_VALUE);
        }
        editorTextView.setOnClickListener(this);
        addView(editorTextView);
    }

    public void hide() {
        this.mCurrentSelectedViewId = -1;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setSelected(false);
        }
        invalidate();
    }

    public void invalidate() {
        for (int i = 0; i < getChildCount(); i++) {
            View container = getChildAt(i);
            if (container instanceof TextView) {
                if (container.isSelected()) {
                    ((TextView) container).setTextColor(ColorfulUtils.mappingColorfulColor(getContext(), getResources().getColor(R.color.editor_reset_color)));
                } else {
                    ((TextView) container).setTextColor(getResources().getColor(R.color.editor_label_color));
                }
            }
        }
        super.invalidate();
    }

    public void onClick(View v) {
        int viewId = v.getId();
        if (this.mCurrentSelectedViewId == -1) {
            v.setSelected(true);
            this.mCurrentSelectedViewId = viewId;
            if (this.mMenuClickListener != null) {
                this.mMenuClickListener.onSelect(viewId, false);
            }
        } else if (viewId != this.mCurrentSelectedViewId) {
            int oldViewId = this.mCurrentSelectedViewId;
            findViewById(oldViewId).setSelected(false);
            v.setSelected(true);
            this.mCurrentSelectedViewId = viewId;
            if (this.mMenuClickListener != null) {
                this.mMenuClickListener.onChangeSelect(oldViewId, viewId);
            }
        }
        invalidate();
    }

    public void setMenuClickListener(MenuClickListener listener) {
        this.mMenuClickListener = listener;
    }

    public void saveUIController(TransitionStore transitionStore) {
        transitionStore.put("select_index", Integer.valueOf(this.mCurrentSelectedViewId));
    }

    public void restoreUIController(TransitionStore transitionStore) {
        onClick(findViewById(((Integer) transitionStore.get("select_index")).intValue()));
        invalidate();
    }
}
