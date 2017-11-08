package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.BaseViewAdapter.OnSelectedChangedListener;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.category.SeekBarManager;
import com.huawei.gallery.editor.category.SeekBarManager.OnSeekBarChangeListener;
import com.huawei.gallery.editor.category.VolatileViewTrack;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.filters.fx.FilterChangableFxRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.tools.EditorConstant;
import com.huawei.gallery.editor.ui.BasePaintBar.UIListener;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;

public class FilterUIController extends EditorUIController implements UIListener {
    private CategoryAdapter mCurrentAdapter;
    private FilterBar mFilterBar;
    private View mFilterSeekBarBtn;
    private SparseArray<CategoryAdapter> mFiltersAdpater;
    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(int key, int progress) {
            if (FilterUIController.this.mCurrentAdapter != null) {
                Action action = FilterUIController.this.mCurrentAdapter.getSelectedAction();
                if (action != null) {
                    FilterRepresentation rep = action.getRepresentation();
                    if (rep != null && (rep instanceof FilterChangableFxRepresentation)) {
                        ((FilterChangableFxRepresentation) rep).getParameter().setOneParameter(key, progress);
                        action.showRepresentation(FilterUIController.this.mCurrentAdapter.getEditorStep());
                    }
                }
            }
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (FilterUIController.this.mCurrentAdapter != null && FilterUIController.this.mCurrentAdapter.getSelectedAction() != null) {
                if (FilterUIController.this.mCurrentAdapter.getSelectedAction().getRepresentation() instanceof FilterChangableFxRepresentation) {
                    int progress = (int) ((((float) seekBar.getProgress()) / ((float) seekBar.getMax())) * 100.0f);
                    ReportToBigData.report(116, String.format("{FilterType:%s,AdjustKey:%s,Progress:%s}", new Object[]{FilterUIController.this.mCurrentAdapter.getSelectedAction().getRepresentation().getSerializationName(), seekBar.getTag(), Integer.valueOf(progress)}));
                }
            }
        }
    };
    private OnSelectedChangedListener mOnSelectedChangedListener = new OnSelectedChangedListener() {
        public void onSelectedChanged(int selected, BaseViewAdapter adapter) {
            if (FilterUIController.this.mSeekBarManager != null) {
                FilterUIController.this.mSeekBarManager.hide();
            }
            if (ImageFilterFx.getFilterChangeable() && adapter.getSelectedAction() != null) {
                FilterUIController.this.mFilterSeekBarBtn.setVisibility(8);
                FilterRepresentation rep = adapter.getSelectedAction().getRepresentation();
                if ((rep instanceof FilterChangableFxRepresentation) && FilterUIController.this.mSeekBarManager != null) {
                    FilterUIController.this.mSeekBarManager.reloadedListView(((FilterChangableFxRepresentation) rep).getSeekBarItems(), ((FilterChangableFxRepresentation) rep).getParameter());
                    FilterUIController.this.mFilterSeekBarBtn.setVisibility(0);
                    if (FilterUIController.this.mFilterSeekBarBtn.isSelected()) {
                        FilterUIController.this.mSeekBarManager.show();
                    } else {
                        FilterUIController.this.mSeekBarManager.hide();
                    }
                }
                if (!(rep == null || rep.getSerializationName() == null)) {
                    ReportToBigData.report(115, String.format("{FilterType:%s}", new Object[]{rep.getSerializationName()}));
                }
            }
        }

        public void onRepeatOnClick(int selected, BaseViewAdapter adapter) {
            onSelectedChanged(selected, adapter);
        }
    };
    private SeekBarManager mSeekBarManager;

    public FilterUIController(Context context, ViewGroup parentLayout, Listener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
    }

    public void setAdapter(SparseArray<CategoryAdapter> filtersAdpater) {
        this.mFiltersAdpater = filtersAdpater;
        for (int i = 0; i < filtersAdpater.size(); i++) {
            ((CategoryAdapter) filtersAdpater.get(filtersAdpater.keyAt(i))).setSelectedChangedListener(this.mOnSelectedChangedListener);
        }
    }

    public void show() {
        super.show();
        if (this.mFiltersAdpater != null) {
            for (int i = 0; i < this.mFiltersAdpater.size(); i++) {
                ((CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(i))).initializeSelection();
            }
            this.mCurrentAdapter = (CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(0));
        }
        PaintFilterMenu filterMenu = (PaintFilterMenu) this.mContainer.findViewById(R.id.filter_menu);
        filterMenu.onClick(filterMenu.getChildAt(0));
        if (this.mFilterSeekBarBtn != null) {
            this.mFilterSeekBarBtn.setVisibility(8);
            this.mFilterSeekBarBtn.setSelected(true);
        }
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_filter_foot_bar : R.layout.editor_filter_foot_bar_land;
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        this.mFilterBar = (FilterBar) this.mContainer.findViewById(R.id.filter_bar);
        this.mFilterBar.initialize(this, this.mFiltersAdpater);
        this.mFilterSeekBarBtn = this.mContainer.findViewById(R.id.filter_seekbar_button);
        this.mFilterSeekBarBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (v.isSelected()) {
                    v.setSelected(false);
                    if (FilterUIController.this.mSeekBarManager != null) {
                        FilterUIController.this.mSeekBarManager.hide();
                        return;
                    }
                    return;
                }
                v.setSelected(true);
                if (FilterUIController.this.mSeekBarManager != null) {
                    FilterUIController.this.mSeekBarManager.show();
                }
            }
        });
        this.mSeekBarManager = new SeekBarManager((VolatileViewTrack) this.mContainer.findViewById(R.id.filter_seekbarlist));
        this.mSeekBarManager.setListener(this.mOnSeekBarChangeListener);
        this.mSeekBarManager.hide();
    }

    public void setEditorStep(EditorStep step) {
        if (this.mFiltersAdpater != null) {
            for (int i = 0; i < this.mFiltersAdpater.size(); i++) {
                ((CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(i))).setEditorStep(step);
            }
        }
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        if (this.mFiltersAdpater != null) {
            for (int i = 0; i < this.mFiltersAdpater.size(); i++) {
                ((CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(i))).resetActionImage();
            }
        }
    }

    public void hide() {
        super.hide();
        if (this.mSeekBarManager != null) {
            this.mSeekBarManager.hide();
        }
        if (this.mFilterBar != null) {
            this.mFilterBar.hide();
        }
        if (this.mFilterSeekBarBtn != null) {
            this.mFilterSeekBarBtn.setVisibility(8);
        }
        resetFilterRepresentation();
    }

    private void resetFilterRepresentation() {
        if (this.mFiltersAdpater != null) {
            for (int i = 0; i < this.mFiltersAdpater.size(); i++) {
                CategoryAdapter adapter = (CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(i));
                for (int position = 0; position < adapter.getCount(); position++) {
                    FilterRepresentation rep = ((Action) adapter.getItem(position)).getRepresentation();
                    if (rep instanceof FilterChangableFxRepresentation) {
                        ((FilterChangableFxRepresentation) rep).reset();
                    }
                }
            }
        }
    }

    public void onSubMenuShow() {
    }

    public void onSubMenuHide() {
    }

    public void onEraseSelectedChanged(boolean selected) {
    }

    public void onClickBar(View view) {
        if (this.mSeekBarManager != null) {
            this.mSeekBarManager.hide();
        }
        if (this.mFiltersAdpater != null) {
            this.mCurrentAdapter = (CategoryAdapter) this.mFiltersAdpater.get(view.getId());
            this.mCurrentAdapter.onClickSelectView();
        }
    }

    protected View getFootAnimationTargetView() {
        return this.mFilterBar;
    }

    public int getSubMenuHeight() {
        return EditorConstant.SUB_MENU_HEIGHT_BIG;
    }

    protected void saveUIController() {
        if (this.mFilterBar != null && this.mCurrentAdapter != null && this.mSeekBarManager != null) {
            this.mFilterBar.saveUIController(this.mTransitionStore);
            this.mCurrentAdapter.saveUIController(this.mTransitionStore);
            this.mSeekBarManager.saveUIController(this.mTransitionStore);
        }
    }

    protected void restoreUIController() {
        if (this.mFilterBar != null && this.mCurrentAdapter != null && this.mSeekBarManager != null) {
            this.mFilterBar.restoreUIController(this.mTransitionStore);
            this.mCurrentAdapter.restoreUIController(this.mTransitionStore);
            this.mSeekBarManager.restoreUIController(this.mTransitionStore);
            if (this.mFilterSeekBarBtn != null) {
                int i;
                this.mFilterSeekBarBtn.setSelected(this.mSeekBarManager.isVisible());
                FilterRepresentation rep = this.mCurrentAdapter.getSelectedAction().getRepresentation();
                View view = this.mFilterSeekBarBtn;
                if (rep instanceof FilterChangableFxRepresentation) {
                    i = 0;
                } else {
                    i = 8;
                }
                view.setVisibility(i);
            }
        }
    }
}
