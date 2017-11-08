package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.animation.EditorAnimation;
import com.huawei.gallery.editor.animation.EditorAnimation.EditorAnimationListener;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.BaseViewAdapter.OnSelectedChangedListener;
import com.huawei.gallery.editor.category.EditorTextViewAdapter;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceColorRepresentation;
import com.huawei.gallery.editor.filters.beauty.FilterFaceRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.sfb.FaceBeautifierParameter;
import com.huawei.gallery.editor.sfb.FaceEdit;
import com.huawei.gallery.editor.ui.SkinUIController.SkinUIListener;
import com.huawei.gallery.util.ColorfulUtils;
import java.util.ArrayList;

public class SkinSfbUIController extends SkinUIController {
    private TextView mFaceBronzerText;
    private SeekBar mFaceWhiteSeekbar;
    private TextView mFaceWhiteText;
    private boolean mIsFaceColorAdjust;
    private OnSelectedChangedListener mOnSelectedChangedListener = new OnSelectedChangedListener() {
        public void onSelectedChanged(int selected, BaseViewAdapter adapter) {
            if (selected >= 0 && SkinSfbUIController.this.mSkinSeekBar != null && SkinSfbUIController.this.mFaceEditorStep != null) {
                FilterRepresentation rep = ((Action) SkinSfbUIController.this.mAdapter.getItem(selected)).getRepresentation();
                if (rep instanceof FilterFaceRepresentation) {
                    ReportToBigData.report(122, String.format("{BeautyAction:%s}", new Object[]{rep.getSerializationName()}));
                    SkinSfbUIController.this.setType(rep);
                    if (SkinSfbUIController.this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType == 0) {
                        FilterFaceRepresentation faceRep = (FilterFaceRepresentation) rep;
                        if (FaceEdit.getSupportVersion() && (faceRep instanceof FilterFaceColorRepresentation)) {
                            SkinSfbUIController.this.mIsFaceColorAdjust = true;
                        } else {
                            SkinSfbUIController.this.mIsFaceColorAdjust = false;
                        }
                        SkinSfbUIController.this.onSeekBarShow();
                        SkinSfbUIController.this.mSkinSeekBar.setMax(faceRep.getMaximum() - faceRep.getMinimum());
                        SkinSfbUIController.this.mSkinSeekBar.setProgress(faceRep.getValue() - faceRep.getMinimum());
                    } else {
                        SkinSfbUIController.this.mIsFaceColorAdjust = false;
                        SkinSfbUIController.this.onSeekBarHide();
                        ArrayList<FilterRepresentation> filter = new ArrayList();
                        for (int j = 0; j < SkinSfbUIController.this.mAdapter.getCount(); j++) {
                            FilterRepresentation repTemp = ((Action) SkinSfbUIController.this.mAdapter.getItem(j)).getRepresentation();
                            if (repTemp instanceof FilterFaceRepresentation) {
                                if (SkinSfbUIController.this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType == 1) {
                                    ((FilterFaceRepresentation) repTemp).setValue((int) (((float) SkinSfbUIController.this.mSkinSfbListener.getQuickBeautyParameter().getSfbPara(((FilterFaceRepresentation) repTemp).getFaceType())) / FaceBeautifierParameter.UIPARA_TO_SFBPARA));
                                } else if (SkinSfbUIController.this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType == 2) {
                                    ((FilterFaceRepresentation) repTemp).setValue(0);
                                }
                                filter.add(repTemp);
                            }
                        }
                        SkinSfbUIController.this.mSkinSfbListener.commitLocalRepresentations(filter);
                    }
                }
            }
        }

        public void onRepeatOnClick(int selected, BaseViewAdapter adapter) {
            if (selected >= 0 && SkinSfbUIController.this.mSkinSeekBar != null && SkinSfbUIController.this.mFaceEditorStep != null) {
                FilterRepresentation rep = ((Action) SkinSfbUIController.this.mAdapter.getItem(selected)).getRepresentation();
                if (rep instanceof FilterFaceRepresentation) {
                    SkinSfbUIController.this.setType(rep);
                    if (SkinSfbUIController.this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType == 0) {
                        SkinSfbUIController.this.onRepeatClick();
                    }
                }
            }
        }
    };
    private Listener mSkinSfbListener;
    private ViewGroup mViewGroupWhite;

    public interface Listener extends SkinUIListener {
        void commitLocalRepresentations(ArrayList<FilterRepresentation> arrayList);

        FaceBeautifierParameter getFaceBeautifierParameter();

        FaceBeautifierParameter getQuickBeautyParameter();
    }

    public SkinSfbUIController(Context context, ViewGroup parentLayout, Listener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mSkinSfbListener = listener;
    }

    public void show() {
        super.show();
        setFreezeSelection(true);
    }

    protected void onSeekBarShowAnime() {
        this.mSkinSeekBar.setVisibility(0);
        EditorAnimation.startFadeAnimationForViewGroup(this.mSkinSeekBar, 1, 0, null);
        this.mSkinSfbListener.onSubMenuShow();
    }

    public void setAdpater(BaseViewAdapter adpater) {
        super.setAdpater(adpater);
        this.mAdapter.setSelectedChangedListener(this.mOnSelectedChangedListener);
    }

    public void hide() {
        super.hide();
        setFreezeSelection(true);
    }

    public void setFreezeSelection(boolean freeze) {
        if (this.mAdapter == null || !(this.mAdapter instanceof EditorTextViewAdapter)) {
            throw new IllegalStateException("mAdpater Exception");
        }
        ((EditorTextViewAdapter) this.mAdapter).setFreezeSelection(freeze);
    }

    private void setType(FilterRepresentation rep) {
        if ("ORIGIN".equalsIgnoreCase(rep.getSerializationName())) {
            this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType = 2;
        } else if ("BEAUTY".equalsIgnoreCase(rep.getSerializationName())) {
            this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType = 1;
        } else {
            this.mSkinSfbListener.getFaceBeautifierParameter().mSfbType = 0;
        }
    }

    protected void onSeekBarShow() {
        if (!this.mSubMenuShowing) {
            this.mSkinSfbListener.onSubMenuShow();
        }
        super.onSeekBarShow();
        if (this.mIsFaceColorAdjust) {
            this.mViewGroupWhite.setVisibility(0);
            this.mFaceBronzerText.setVisibility(0);
            EditorAnimation.startFadeAnimationForViewGroup(this.mFaceWhiteSeekbar, 1, 0, null);
            return;
        }
        this.mViewGroupWhite.setVisibility(8);
        this.mFaceBronzerText.setVisibility(4);
    }

    protected void onSeekBarHideAnime() {
        EditorAnimation.startFadeAnimationForViewGroup(this.mSkinSeekBar, 2, 0, new EditorAnimationListener() {
            public void onAnimationEnd() {
                if (!SkinSfbUIController.this.mSubMenuShowing) {
                    SkinSfbUIController.this.mSkinSeekBar.setVisibility(8);
                }
            }
        });
        this.mSkinSfbListener.onSubMenuHide();
    }

    protected void onSeekBarHide() {
        if (this.mSubMenuShowing) {
            this.mSkinSfbListener.onSubMenuHide();
        }
        super.onSeekBarHide();
        this.mViewGroupWhite.setVisibility(8);
        this.mFaceBronzerText.setVisibility(4);
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        this.mViewGroupWhite = (ViewGroup) this.mContainer.findViewById(R.id.skin_seekbar_item_1);
        this.mFaceWhiteSeekbar = (SeekBar) this.mViewGroupWhite.findViewById(R.id.filter_seekbar);
        this.mFaceWhiteSeekbar.setOnSeekBarChangeListener(this.mOnSeekBarChangeListener);
        this.mFaceWhiteSeekbar.setTag(Integer.valueOf(0));
        this.mFaceWhiteText = (TextView) this.mViewGroupWhite.findViewById(R.id.filter_seekbar_text);
        this.mFaceWhiteText.setText(R.string.face_edit_color);
        this.mViewGroupWhite.setVisibility(8);
        ColorfulUtils.decorateColorfulForSeekbar(this.mContext, this.mFaceWhiteSeekbar);
        this.mFaceBronzerText = (TextView) ((ViewGroup) this.mContainer.findViewById(R.id.skin_seekbar_item)).findViewById(R.id.filter_seekbar_text);
        this.mFaceBronzerText.setText(R.string.bronze);
        this.mFaceBronzerText.setVisibility(4);
    }
}
