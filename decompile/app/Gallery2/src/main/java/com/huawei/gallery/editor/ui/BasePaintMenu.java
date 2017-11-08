package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.android.gallery3d.app.TransitionStore;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.tools.EditorUtils;

public abstract class BasePaintMenu extends LinearLayout implements OnClickListener {
    private int mCurrentSelectedViewId = -1;
    private Delegate mDelegate;
    protected final SparseArray<IconData> mIconDataContainer = new SparseArray();
    private boolean mLock = false;
    private MenuClickListener mMenuClickListener;

    public interface MenuClickListener {
        void onChangeSelect(int i, int i2);

        void onSelect(int i, boolean z);

        void onUnSelect(int i);
    }

    public interface Delegate {
        boolean isAnimationRunning();
    }

    protected abstract void updateIconDataContainer();

    public BasePaintMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void lock() {
        this.mLock = true;
    }

    public void unLock() {
        this.mLock = false;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        attachView();
    }

    protected void attachView() {
        updateIconDataContainer();
        for (int i = 0; i < this.mIconDataContainer.size(); i++) {
            IconData iconData = (IconData) this.mIconDataContainer.get(this.mIconDataContainer.keyAt(i));
            EditorTextView editorTextView = new EditorTextView(getContext());
            editorTextView.setId(EditorUtils.getViewId(i));
            editorTextView.setAttributes(iconData, i, this.mIconDataContainer.size(), true, needScroll());
            editorTextView.setOnClickListener(this);
            addView(editorTextView);
        }
    }

    private boolean needScroll() {
        boolean z = true;
        if (getContext().getResources().getConfiguration().orientation == 1) {
            if (this.mIconDataContainer.size() <= 5) {
                z = false;
            }
            return z;
        }
        if (this.mIconDataContainer.size() <= 3) {
            z = false;
        }
        return z;
    }

    public void onClick(View v) {
        if ((this.mDelegate == null || !this.mDelegate.isAnimationRunning()) && !this.mLock) {
            int viewId = v.getId();
            if (this.mCurrentSelectedViewId == -1) {
                v.setSelected(true);
                this.mCurrentSelectedViewId = viewId;
                if (this.mMenuClickListener != null) {
                    this.mMenuClickListener.onSelect(viewId, false);
                }
            } else if (viewId == this.mCurrentSelectedViewId) {
                v.setSelected(false);
                this.mCurrentSelectedViewId = -1;
                if (this.mMenuClickListener != null) {
                    this.mMenuClickListener.onUnSelect(viewId);
                }
            } else {
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

    public void setDelegate(Delegate delegate) {
        this.mDelegate = delegate;
    }

    protected void saveUIController(TransitionStore transitionStore) {
        transitionStore.put("select_index", Integer.valueOf(this.mCurrentSelectedViewId));
    }

    protected void restoreUIController(TransitionStore transitionStore) {
        int id = ((Integer) transitionStore.get("select_index")).intValue();
        if (findViewById(id) != null) {
            onClick(findViewById(id));
        }
    }
}
