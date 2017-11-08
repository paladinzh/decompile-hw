package com.huawei.gallery.refocus.wideaperture.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.tools.EditorUtils;

public class ApertureMenu extends LinearLayout implements OnClickListener {
    private static final SparseArray<IconData> mIconDataContainer = new SparseArray();
    private int mCurrentSelectedViewId = -1;
    public boolean mIsShow = false;
    protected MenuClickListener mMenuClickListener;
    private boolean mMenuItemClickable = true;

    public enum MENU {
        APERTURE,
        FILTER,
        UNKONW
    }

    public interface MenuClickListener {
        boolean isMenuChangeAnimationEnd();

        void onChangeSelect(int i, int i2);

        void onSelect(int i, boolean z);

        void restoreOperationViewSelectionSate(int i);
    }

    static {
        mIconDataContainer.put(MENU.APERTURE.ordinal(), new IconData(MENU.APERTURE.ordinal(), (int) R.drawable.ic_gallery_info_aperture, (int) R.string.aperture));
        mIconDataContainer.put(MENU.FILTER.ordinal(), new IconData(MENU.FILTER.ordinal(), (int) R.drawable.btn_editor_filter, (int) R.string.simple_editor_filter));
    }

    public ApertureMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < mIconDataContainer.size(); i++) {
            IconData iconData = (IconData) mIconDataContainer.get(mIconDataContainer.keyAt(i));
            EditorTextView editorTextView = new EditorTextView(getContext());
            editorTextView.setId(EditorUtils.getViewId(i));
            editorTextView.setAttributes(iconData, i, mIconDataContainer.size(), true, needScroll());
            editorTextView.setOnClickListener(this);
            if (editorTextView.getId() == MENU.APERTURE.ordinal()) {
                editorTextView.setTag(MENU.APERTURE);
            } else if (editorTextView.getId() == MENU.FILTER.ordinal()) {
                editorTextView.setTag(MENU.FILTER);
                setSelected(true);
            }
            addView(editorTextView);
        }
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            firstView.setSelected(true);
            this.mCurrentSelectedViewId = firstView.getId();
        }
    }

    public void restoreSelectionSate(TransitionStore transitionStore) {
        int currentViewId = ((Integer) transitionStore.get("current_menu_id")).intValue();
        View currentView = findViewById(currentViewId);
        if (currentView != null) {
            currentView.setSelected(true);
            this.mCurrentSelectedViewId = currentViewId;
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i).getId() != currentViewId) {
                    getChildAt(i).setSelected(false);
                }
            }
            if (this.mMenuClickListener != null) {
                this.mMenuClickListener.restoreOperationViewSelectionSate(currentViewId);
            }
        }
    }

    public void saveSelectionState(TransitionStore transitionStore) {
        transitionStore.put("current_menu_id", Integer.valueOf(this.mCurrentSelectedViewId));
    }

    private boolean needScroll() {
        return mIconDataContainer.size() > 3;
    }

    public void setMenuItemClickable(boolean clickable) {
        this.mMenuItemClickable = clickable;
    }

    public void onClick(View v) {
        if ((this.mMenuClickListener == null || this.mMenuClickListener.isMenuChangeAnimationEnd()) && this.mMenuItemClickable) {
            int viewId = v.getId();
            if (this.mCurrentSelectedViewId == -1) {
                v.setSelected(true);
                this.mCurrentSelectedViewId = viewId;
                if (this.mMenuClickListener != null) {
                    this.mIsShow = true;
                    this.mMenuClickListener.onSelect(viewId, this.mIsShow);
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
        }
    }

    public void setMenuClickListener(MenuClickListener listener) {
        this.mMenuClickListener = listener;
    }
}
