package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.BaseViewAdapter.OnSelectedChangedListener;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorConstant;
import com.huawei.gallery.editor.ui.SkinUIController.SkinUIListener;

public class SkinOmronUIController extends SkinUIController {
    private OnSelectedChangedListener mOnSelectedChangedListener = new OnSelectedChangedListener() {
        public void onSelectedChanged(int selected, BaseViewAdapter adapter) {
            if (selected >= 0 && SkinOmronUIController.this.mSkinSeekBar != null) {
                FilterRepresentation rep = ((Action) SkinOmronUIController.this.mAdapter.getItem(selected)).getRepresentation();
                if (rep instanceof FilterFaceRepresentation) {
                    SkinOmronUIController.this.onSeekBarShow();
                    FilterFaceRepresentation faceRep = (FilterFaceRepresentation) rep;
                    SkinOmronUIController.this.mSkinSeekBar.setMax(faceRep.getMaximum() - faceRep.getMinimum());
                    SkinOmronUIController.this.mSkinSeekBar.setProgress(faceRep.getValue() - faceRep.getMinimum());
                }
            }
        }

        public void onRepeatOnClick(int selected, BaseViewAdapter adapter) {
            SkinOmronUIController.this.onRepeatClick();
        }
    };

    public SkinOmronUIController(Context context, ViewGroup parentLayout, SkinUIListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
    }

    public void setAdpater(BaseViewAdapter adpater) {
        super.setAdpater(adpater);
        if (this.mAdapter instanceof CategoryAdapter) {
            ((CategoryAdapter) this.mAdapter).setOrientation(1);
            this.mAdapter.setSelectedChangedListener(this.mOnSelectedChangedListener);
        }
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_category_track : R.layout.editor_category_track_land;
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        EditorAnimation.startFadeAnimationForViewGroup(this.mSkinSeekBar, 1, 0, null);
    }

    protected void onSeekBarShowAnime() {
        this.mSkinSeekBar.setVisibility(0);
    }

    protected void onSeekBarHideAnime() {
        this.mSkinSeekBar.setVisibility(8);
    }

    public int getMenuHeight() {
        return EditorConstant.SUB_MENU_HEIGHT_OMRON;
    }
}
