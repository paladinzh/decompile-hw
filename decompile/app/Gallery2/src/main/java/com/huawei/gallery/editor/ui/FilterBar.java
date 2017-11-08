package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.animation.EditorAnimation.EditorAnimationListener;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.category.CategoryTrack;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin.FILTER_STYLE;
import com.huawei.gallery.editor.ui.BasePaintBar.UIListener;
import com.huawei.gallery.editor.ui.BasePaintMenu.MenuClickListener;

public class FilterBar extends LinearLayout implements MenuClickListener {
    private static final int SUB_MENU_HEIGHT = GalleryUtils.dpToPixel(67);
    private SparseArray<CategoryAdapter> mAdapterSparseArray;
    private final SparseArray<ViewGroup> mCategoryScollViews = new SparseArray();
    private PaintFilterMenu mSubMenuContainer;
    private FrameLayout mSubMenuRoot;
    private UIListener mUIListener;

    private static class FilterScrollView extends ElasticBothHorizontalScrollView {
        public FilterScrollView(Context context, CategoryTrack track) {
            super(context);
            setLayoutParams(new LayoutParams(-2, -2, 1));
            setHorizontalScrollBarEnabled(false);
            addView(track);
        }
    }

    public FilterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize(UIListener uiListener, SparseArray<CategoryAdapter> adapterSparseArray) {
        if (adapterSparseArray != null) {
            this.mCategoryScollViews.clear();
            this.mUIListener = uiListener;
            this.mSubMenuRoot = (FrameLayout) findViewById(R.id.filter_sub_menu_root);
            this.mSubMenuContainer = (PaintFilterMenu) findViewById(R.id.filter_menu);
            this.mSubMenuContainer.setMenuClickListener(this);
            for (int i = 0; i < adapterSparseArray.size(); i++) {
                int key = adapterSparseArray.keyAt(i);
                this.mSubMenuContainer.addChildTextView(key, FILTER_STYLE.values()[key].filterClassNameId, i, adapterSparseArray.size());
            }
            this.mAdapterSparseArray = adapterSparseArray;
            this.mSubMenuContainer.invalidate();
        }
    }

    public void hide() {
        this.mSubMenuRoot.clearAnimation();
        this.mSubMenuRoot.removeAllViews();
        this.mSubMenuContainer.hide();
    }

    private boolean isPort() {
        return getResources().getConfiguration().orientation == 1;
    }

    private ViewGroup getFilterContainerByViewId(int viewId) {
        if (this.mCategoryScollViews.get(viewId) != null) {
            return (ViewGroup) this.mCategoryScollViews.get(viewId);
        }
        ViewGroup scrollView;
        CategoryAdapter adapter = (CategoryAdapter) this.mAdapterSparseArray.get(viewId);
        CategoryTrack track = new CategoryTrack(getContext());
        if (isPort()) {
            track.setOrientation(0);
            scrollView = new FilterScrollView(getContext(), track);
        } else {
            track.setOrientation(1);
            scrollView = new ScrollView(getContext());
            scrollView.setVerticalScrollBarEnabled(false);
            scrollView.setLayoutParams(new LayoutParams(-2, -2, 16));
            scrollView.addView(track);
        }
        adapter.setContainer(track);
        track.setAdapter(adapter);
        this.mCategoryScollViews.put(viewId, scrollView);
        return scrollView;
    }

    public void onSelect(int viewId, boolean isChang) {
        this.mSubMenuRoot.removeAllViews();
        ViewGroup viewGroup = getFilterContainerByViewId(viewId);
        if (viewGroup != null) {
            this.mSubMenuRoot.addView(viewGroup);
        }
    }

    public void onUnSelect(int viewId) {
    }

    public void onChangeSelect(final int oldViewId, final int newViewId) {
        EditorAnimation.startAnimationForAllChildView(getFilterContainerByViewId(oldViewId), 250, SUB_MENU_HEIGHT, 2, 0, 30, new EditorAnimationListener() {
            public void onAnimationEnd() {
                if (FilterBar.this.mSubMenuContainer.isCurrentSelect(oldViewId)) {
                    FilterBar.this.getFilterContainerByViewId(oldViewId).setVisibility(0);
                } else {
                    FilterBar.this.getFilterContainerByViewId(oldViewId).setVisibility(4);
                }
            }
        }, isPort());
        if (getFilterContainerByViewId(newViewId).getParent() == null) {
            this.mSubMenuRoot.addView(getFilterContainerByViewId(newViewId));
        }
        getFilterContainerByViewId(newViewId).setVisibility(0);
        EditorAnimation.startAnimationForAllChildView(getFilterContainerByViewId(newViewId), 250, SUB_MENU_HEIGHT, 1, 100, 30, new EditorAnimationListener() {
            public void onAnimationEnd() {
                if (FilterBar.this.mSubMenuContainer.isCurrentSelect(newViewId)) {
                    FilterBar.this.getFilterContainerByViewId(newViewId).setVisibility(0);
                } else {
                    FilterBar.this.getFilterContainerByViewId(newViewId).setVisibility(4);
                }
            }
        }, isPort());
        this.mUIListener.onClickBar(findViewById(newViewId));
    }

    public void saveUIController(TransitionStore store) {
        this.mSubMenuContainer.saveUIController(store);
    }

    public void restoreUIController(TransitionStore store) {
        this.mSubMenuContainer.restoreUIController(store);
    }
}
