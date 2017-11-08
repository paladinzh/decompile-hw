package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.BaseViewAdapter.OnSelectedChangedListener;
import com.huawei.gallery.editor.category.BaseViewTrack;
import com.huawei.gallery.editor.category.EditorTextViewAdapter;
import com.huawei.gallery.editor.filters.FilterBasicRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.step.AdjustEditorStep;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;
import com.huawei.gallery.editor.ui.SkinUIController.NullTouchListener;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.watermark.manager.parse.WMElement;

public class AdjustUIController extends EditorUIController {
    private EditorTextViewAdapter mAdapter;
    private AdjustEditorStep mAdjustEditorStep;
    private SeekBar mAdjustSeekbar;
    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (AdjustUIController.this.mAdapter != null && AdjustUIController.this.mAdjustEditorStep != null) {
                Action action = AdjustUIController.this.mAdapter.getSelectedAction();
                if (action != null && AdjustUIController.this.mAdjustSeekbar != null) {
                    FilterRepresentation rep = action.getRepresentation();
                    if (rep instanceof FilterBasicRepresentation) {
                        FilterBasicRepresentation basic = (FilterBasicRepresentation) rep;
                        basic.setValue((int) (((float) basic.getMinimum()) + (((float) (basic.getMaximum() - basic.getMinimum())) * (((float) AdjustUIController.this.mAdjustSeekbar.getProgress()) / ((float) AdjustUIController.this.mAdjustSeekbar.getMax())))));
                        action.showRepresentation(AdjustUIController.this.mAdjustEditorStep);
                    }
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            AdjustUIController.this.reportDataForSeekBarProgress();
        }
    };
    private OnSelectedChangedListener mOnSelectedChangedListener = new OnSelectedChangedListener() {
        public void onSelectedChanged(int selected, BaseViewAdapter adapter) {
            if (selected >= 0 && AdjustUIController.this.mAdjustSeekbar != null) {
                FilterRepresentation rep = ((Action) AdjustUIController.this.mAdapter.getItem(selected)).getRepresentation();
                if (rep instanceof FilterBasicRepresentation) {
                    FilterBasicRepresentation basic = (FilterBasicRepresentation) rep;
                    AdjustUIController.this.onSeekBarShow();
                    AdjustUIController.this.mAdjustSeekbar.setProgress((int) (((float) (AdjustUIController.this.mAdjustSeekbar.getMax() * (basic.getValue() - basic.getMinimum()))) / ((float) (basic.getMaximum() - basic.getMinimum()))));
                }
            }
        }

        public void onRepeatOnClick(int selected, BaseViewAdapter adapter) {
            AdjustUIController.this.onRepeatClick();
        }
    };
    private boolean mSubMenuShowing = false;

    public AdjustUIController(Context context, ViewGroup parentLayout, Listener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
    }

    public void setAdapter(EditorTextViewAdapter adapter) {
        this.mAdapter = adapter;
        this.mAdapter.setSelectedChangedListener(this.mOnSelectedChangedListener);
    }

    public void setEditorStep(AdjustEditorStep adjustEditorStep) {
        if (this.mAdapter != null) {
            this.mAdapter.setEditorStep(adjustEditorStep);
            this.mAdjustEditorStep = adjustEditorStep;
        }
    }

    public void show() {
        super.show();
        this.mAdapter.initializeSelection();
    }

    public void hide() {
        super.hide();
        this.mAdapter.clearSelection();
    }

    protected void onShowFootAnimeEnd() {
        onSeekBarShow();
    }

    protected void onHideFootAnimeEnd() {
        resetFilterRepresentation();
        super.onHideFootAnimeEnd();
    }

    protected void saveUIController() {
        this.mAdapter.saveUIController(this.mTransitionStore);
        this.mTransitionStore.put("seekbar_progress_index", Integer.valueOf(this.mAdjustSeekbar.getProgress()));
    }

    protected void restoreUIController() {
        float f;
        this.mAdapter.restoreUIController(this.mTransitionStore);
        Object obj = this.mTransitionStore.get("seekbar_progress_index");
        if (obj instanceof Integer) {
            this.mAdjustSeekbar.setProgress(((Integer) obj).intValue());
        }
        SeekBar seekBar = this.mAdjustSeekbar;
        if (this.mSubMenuShowing) {
            f = WMElement.CAMERASIZEVALUE1B1;
        } else {
            f = 0.0f;
        }
        seekBar.setAlpha(f);
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_base_track : R.layout.editor_base_track_land;
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        this.mAdjustSeekbar = (SeekBar) this.mContainer.findViewById(R.id.progress_seekbar);
        this.mAdjustSeekbar.setOnSeekBarChangeListener(this.mOnSeekBarChangeListener);
        ColorfulUtils.decorateColorfulForSeekbar(this.mContext, this.mAdjustSeekbar);
        ((LinearLayout) this.mContainer.findViewById(R.id.base_root)).setOnTouchListener(new NullTouchListener());
        BaseViewTrack panel = (BaseViewTrack) this.mContainer.findViewById(R.id.listItems);
        panel.setVisibility(0);
        panel.setAdapter(this.mAdapter);
        this.mAdapter.setContainer(panel);
    }

    private void resetFilterRepresentation() {
        for (int position = 0; position < this.mAdapter.getCount(); position++) {
            FilterRepresentation rep = ((Action) this.mAdapter.getItem(position)).getRepresentation();
            if (rep instanceof FilterBasicRepresentation) {
                FilterBasicRepresentation basic = (FilterBasicRepresentation) rep;
                basic.setValue(basic.getDefaultValue());
            }
        }
    }

    public void resetUnSelectedRepresentation() {
        if (this.mAdapter != null) {
            this.mAdapter.resetUnSelectedRepresentation();
        }
    }

    private void reportDataForSeekBarProgress() {
        if (this.mAdapter != null) {
            Action action = this.mAdapter.getSelectedAction();
            if (action != null && this.mAdjustSeekbar != null) {
                if (action.getRepresentation() instanceof FilterBasicRepresentation) {
                    int progress = (int) ((((float) this.mAdjustSeekbar.getProgress()) / ((float) this.mAdjustSeekbar.getMax())) * 100.0f);
                    ReportToBigData.report(121, String.format("{AdjustType:%s,Progress:%s}", new Object[]{action.getRepresentation().getSerializationName(), GalleryUtils.getPercentString((float) progress, 0)}));
                }
            }
        }
    }

    protected void onRepeatClick() {
        if (this.mSubMenuShowing) {
            onSeekBarHide();
        } else {
            onSeekBarShow();
        }
    }

    private void onSeekBarShow() {
        if (!this.mSubMenuShowing) {
            this.mSubMenuShowing = true;
            EditorAnimation.startFadeAnimationForViewGroup(this.mAdjustSeekbar, 1, 0, null);
        }
    }

    private void onSeekBarHide() {
        if (this.mSubMenuShowing) {
            this.mSubMenuShowing = false;
            EditorAnimation.startFadeAnimationForViewGroup(this.mAdjustSeekbar, 2, 0, null);
        }
    }
}
