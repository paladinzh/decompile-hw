package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.BaseViewTrack;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceColorRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.sfb.FaceEdit;
import com.huawei.gallery.editor.step.FaceEditorStep;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;
import com.huawei.gallery.util.ColorfulUtils;
import java.util.Locale;

public abstract class SkinUIController extends EditorUIController {
    protected BaseViewAdapter mAdapter;
    protected FaceEditorStep mFaceEditorStep;
    protected OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (SkinUIController.this.mAdapter != null) {
                Action action = SkinUIController.this.mAdapter.getSelectedAction();
                if (action != null && SkinUIController.this.mSkinSeekBar != null) {
                    FilterRepresentation rep = action.getRepresentation();
                    if (rep instanceof FilterFaceRepresentation) {
                        FilterFaceRepresentation faceRep = (FilterFaceRepresentation) rep;
                        if (faceRep instanceof FilterFaceColorRepresentation) {
                            ((FilterFaceColorRepresentation) faceRep).setValue(Integer.parseInt(seekBar.getTag().toString()), faceRep.getMinimum() + seekBar.getProgress());
                        } else {
                            faceRep.setValue(faceRep.getMinimum() + seekBar.getProgress());
                        }
                        action.showRepresentation(SkinUIController.this.mFaceEditorStep);
                    }
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            SkinUIController.this.reportDataForSeekBarProgress();
        }
    };
    protected SeekBar mSkinSeekBar;
    protected boolean mSubMenuShowing;

    public interface SkinUIListener extends Listener {
        void onSubMenuHide();

        void onSubMenuShow();
    }

    public static class NullTouchListener implements OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    protected abstract void onSeekBarHideAnime();

    protected abstract void onSeekBarShowAnime();

    public SkinUIController(Context context, ViewGroup parentLayout, SkinUIListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
    }

    public void setAdpater(BaseViewAdapter adpater) {
        this.mAdapter = adpater;
    }

    public void setEditorStep(FaceEditorStep faceEditorStep) {
        if (this.mAdapter != null) {
            this.mAdapter.setEditorStep(faceEditorStep);
            this.mFaceEditorStep = faceEditorStep;
        }
    }

    public void show() {
        super.show();
        this.mAdapter.initializeSelection();
    }

    protected void onSeekBarShow() {
        if (!this.mSubMenuShowing) {
            onSeekBarShowAnime();
        }
        this.mSubMenuShowing = true;
    }

    protected void onSeekBarHide() {
        if (this.mSubMenuShowing) {
            onSeekBarHideAnime();
        }
        this.mSubMenuShowing = false;
    }

    public void hide() {
        super.hide();
        this.mAdapter.clearSelection();
    }

    protected void saveUIController() {
        this.mAdapter.saveUIController(this.mTransitionStore);
        this.mTransitionStore.put("seekbar_progress_index", Integer.valueOf(this.mSkinSeekBar.getProgress()));
    }

    protected void restoreUIController() {
        int i;
        this.mAdapter.restoreUIController(this.mTransitionStore);
        Object obj = this.mTransitionStore.get("seekbar_progress_index");
        if (obj instanceof Integer) {
            this.mSkinSeekBar.setProgress(((Integer) obj).intValue());
        }
        SeekBar seekBar = this.mSkinSeekBar;
        if (this.mSubMenuShowing) {
            i = 0;
        } else {
            i = 8;
        }
        seekBar.setVisibility(i);
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_faceedit_track : R.layout.editor_faceedit_track_land;
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        if ("pk".equals(this.mContext.getResources().getConfiguration().locale.getCountry().toLowerCase(Locale.US))) {
            this.mContainer.findViewById(R.id.seekbar_controls).setLayoutDirection(0);
        }
        this.mSkinSeekBar = (SeekBar) ((ViewGroup) this.mContainer.findViewById(R.id.skin_seekbar_item)).findViewById(R.id.filter_seekbar);
        this.mSkinSeekBar.setOnSeekBarChangeListener(this.mOnSeekBarChangeListener);
        if (FaceEdit.getSupportVersion()) {
            this.mSkinSeekBar.setTag(Integer.valueOf(1));
        } else {
            this.mSkinSeekBar.setTag(Integer.valueOf(0));
        }
        this.mSkinSeekBar.setVisibility(8);
        this.mSubMenuShowing = false;
        ColorfulUtils.decorateColorfulForSeekbar(this.mContext, this.mSkinSeekBar);
        ((LinearLayout) this.mContainer.findViewById(R.id.base_root)).setOnTouchListener(new NullTouchListener());
        BaseViewTrack panel = (BaseViewTrack) this.mContainer.findViewById(R.id.listItems);
        panel.setVisibility(0);
        panel.setAdapter(this.mAdapter);
        this.mAdapter.setContainer(panel);
    }

    private void reportDataForSeekBarProgress() {
        if (this.mAdapter != null) {
            Action action = this.mAdapter.getSelectedAction();
            if (action != null && this.mSkinSeekBar != null) {
                if (action.getRepresentation() instanceof FilterFaceRepresentation) {
                    int progress = (int) ((((float) this.mSkinSeekBar.getProgress()) / ((float) this.mSkinSeekBar.getMax())) * 100.0f);
                    ReportToBigData.report(123, String.format("{BeautyType:%s,Progress:%s}", new Object[]{action.getRepresentation().getSerializationName(), GalleryUtils.getPercentString((float) progress, 0)}));
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
}
