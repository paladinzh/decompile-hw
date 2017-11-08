package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.IllusionBar.STYLE;

public class PaintIllusionMenu extends LinearLayout implements OnClickListener {
    private static final SparseArray<IconData> mIconDataContainer = new SparseArray();
    private int mCurrentSelectedViewId = -1;
    public boolean mIsShow = false;
    protected MenuClickListener mMenuClickListener;

    public interface MenuClickListener {
        void onChangeSelect(int i, int i2);

        void onSelect(int i, boolean z);
    }

    static {
        mIconDataContainer.put(STYLE.CIRCLE.ordinal(), new IconData(STYLE.CIRCLE.ordinal(), (int) R.drawable.ic_gallery_blur_circle, (int) R.string.illusion_icon_circular));
        mIconDataContainer.put(STYLE.BAND.ordinal(), new IconData(STYLE.BAND.ordinal(), (int) R.drawable.ic_gallery_blur_linear, (int) R.string.illusion_icon_linear));
        mIconDataContainer.put(STYLE.WHOLE.ordinal(), new IconData(STYLE.WHOLE.ordinal(), (int) R.drawable.ic_gallery_blur, (int) R.string.illusion_icon_blur));
    }

    public PaintIllusionMenu(Context context, AttributeSet attrs) {
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
            if (editorTextView.getId() == STYLE.WHOLE.ordinal()) {
                editorTextView.setTag(STYLE.WHOLE);
            } else if (editorTextView.getId() == STYLE.CIRCLE.ordinal()) {
                editorTextView.setTag(STYLE.CIRCLE);
                setSelected(true);
            } else if (editorTextView.getId() == STYLE.BAND.ordinal()) {
                editorTextView.setTag(STYLE.BAND);
            }
            addView(editorTextView);
        }
    }

    private boolean needScroll() {
        boolean z = true;
        if (getContext().getResources().getConfiguration().orientation == 1) {
            if (mIconDataContainer.size() <= 5) {
                z = false;
            }
            return z;
        }
        if (mIconDataContainer.size() <= 3) {
            z = false;
        }
        return z;
    }

    public void hide() {
        this.mCurrentSelectedViewId = -1;
        this.mIsShow = true;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setSelected(false);
        }
    }

    public void onClick(View v) {
        boolean z = false;
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
                ReportToBigData.report(119, String.format("{IllusionStyle:%s}", new Object[]{findViewById(viewId).getTag().toString()}));
            }
        } else if (this.mMenuClickListener != null) {
            if (!this.mIsShow) {
                z = true;
            }
            this.mIsShow = z;
            this.mMenuClickListener.onSelect(viewId, this.mIsShow);
        }
    }

    public void saveUIController(TransitionStore transitionStore) {
        transitionStore.put("menu_select_index", Integer.valueOf(this.mCurrentSelectedViewId));
        transitionStore.put("seekbar_show", Boolean.valueOf(this.mIsShow));
    }

    public void restoreUIController(TransitionStore transitionStore) {
        this.mCurrentSelectedViewId = ((Integer) transitionStore.get("menu_select_index")).intValue();
        findViewById(this.mCurrentSelectedViewId).setSelected(true);
        this.mIsShow = ((Boolean) transitionStore.get("seekbar_show", Boolean.valueOf(false))).booleanValue();
    }

    public void setMenuClickListener(MenuClickListener listener) {
        this.mMenuClickListener = listener;
    }
}
