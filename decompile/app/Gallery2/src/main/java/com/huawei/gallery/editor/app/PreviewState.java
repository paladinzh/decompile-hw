package com.huawei.gallery.editor.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.tools.MakeupUtils;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.EditorUIController;
import com.huawei.gallery.editor.ui.EditorView;
import com.huawei.gallery.editor.ui.PreviewUIController;
import com.huawei.gallery.editor.ui.PreviewUIController.PreviewListener;
import com.huawei.gallery.editor.watermark.GalleryWMUtils;

public class PreviewState extends BasePreviewState implements PreviewListener {
    private ActionInfo mDefaultActionInfo;
    private boolean mIsPituSupport;
    private PreviewUIController mPreviewUIController;
    private ActionInfo mRedoUndoActionInfo;

    public PreviewState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
        this.mDefaultActionInfo = new ActionInfo(context.getString(R.string.edit), Action.BACK, Action.SAVE, true);
        this.mRedoUndoActionInfo = new ActionInfo(context.getString(R.string.edit), Action.BACK, Action.SAVE, Action.REDO, Action.UNDO, true);
        this.mActionInfo = this.mDefaultActionInfo;
        this.mIsPituSupport = MakeupUtils.isPituSupport(this.mContext);
        this.mIconDataContainer.put(1, new IconData(1, (int) R.drawable.ic_gallery_edit_rotate, (int) R.string.rotate));
        this.mIconDataContainer.put(2, new IconData(2, (int) R.drawable.ic_gallery_edit_crop, (int) R.string.crop));
        this.mIconDataContainer.put(3, new IconData(3, (int) R.drawable.ic_gallery_edit_filter, (int) R.string.simple_editor_filter));
        if (ImageFilterFx.supportSplashFilter()) {
            this.mIconDataContainer.put(4, new IconData(4, (int) R.drawable.ic_gallery_edit_splash, (int) R.string.simple_editor_splash));
        }
        if (ImageFilterFx.supportIllusionFilter()) {
            this.mIconDataContainer.put(5, new IconData(5, (int) R.drawable.ic_gallery_blur, (int) R.string.simple_editor_illusion));
        }
        this.mIconDataContainer.put(6, new IconData(6, (int) R.drawable.ic_gallery_edit_adjust, (int) R.string.simple_editor_adjust));
        if (EditorLoadLib.isSupportSkin()) {
            this.mIconDataContainer.put(7, new IconData(7, (int) R.drawable.ic_gallery_edit_beauty, (int) R.string.filtershow_title_beauty));
        }
        this.mIconDataContainer.put(8, new IconData(8, (int) R.drawable.ic_gallery_edit_mosaic, (int) R.string.simple_editor_mosaic));
        if (GalleryWMUtils.checkWMZipExist()) {
            this.mIconDataContainer.put(10, new IconData(10, (int) R.drawable.ic_gallery_edit_watermark, (int) R.string.water_mark_attention_dialog_title_511));
        }
        this.mIconDataContainer.put(11, new IconData(11, (int) R.drawable.ic_gallery_edit_label, (int) R.string.editor_label));
        this.mIconDataContainer.put(9, new IconData(9, (int) R.drawable.ic_gallery_edit_pen, (int) R.string.editor_brush));
        if (this.mIsPituSupport) {
            this.mIconDataContainer.put(12, new IconData(12, (int) R.drawable.ic_menu_makeup_white, (int) R.string.makeup));
        }
    }

    protected EditorUIController createUIController() {
        this.mPreviewUIController = new PreviewUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
        return this.mPreviewUIController;
    }

    public void onAnimationRenderFinished(Rect source, Rect target) {
        this.mEditorView.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (PreviewState.this.mEditorView instanceof EditorView) {
                    ((EditorView) PreviewState.this.mEditorView).setBackgroundVisible(true);
                }
            }
        });
    }

    public void show() {
        super.show();
        getImage().requestStackChangeCall();
        getImage().requestStackChangeCall();
        Activity activity = this.mEditorView.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Bitmap bitmap = null;
                    GLRoot root = PreviewState.this.mEditorView.getGLRoot();
                    if (root != null) {
                        root.lockRenderThread();
                        try {
                            bitmap = PreviewState.this.getImage().getBitmapCacheCopy(PreviewState.this.getImage().getFilteredImage());
                            PreviewState.this.mEditorView.getEditorManager().invalidate(1, bitmap);
                        } finally {
                            root.unlockRenderThread();
                        }
                    }
                }
            });
        }
    }

    public boolean onBackPressed() {
        prepareGLQuitAnimationArgs();
        this.mPreviewUIController.reset();
        return super.onBackPressed();
    }

    public void onActionItemClick(Action action) {
        prepareGLQuitAnimationArgs();
        if (action != Action.SAVE || this.mBasePreviewRender == null || !this.mBasePreviewRender.isEditorOpenOrQuitEffectActive()) {
            if (action == Action.NO) {
                onBackPressed();
            } else {
                super.onActionItemClick(action);
            }
        }
    }

    public void hide() {
        super.hide();
        this.mActionInfo = this.mDefaultActionInfo;
        if (this.mBasePreviewRender != null) {
            this.mBasePreviewRender.hide();
        }
    }

    public void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        if (changeSize) {
            this.mEditorView.invalidate();
        }
    }

    public void onLeaveEditor() {
        super.onLeaveEditor();
        this.mPreviewUIController.reset();
        onDetectedFace(false);
    }

    public void onDetectedFace(boolean enable) {
        this.mPreviewUIController.onDetectedFace(enable, this.mIsPituSupport, this.mEditorView.isFromLocalImage());
    }

    protected EditorState selectEditorState(int key) {
        switch (key) {
            case 1:
                return new RotateState(this.mContext, this.mParentLayout, this.mEditorView);
            case 2:
                return new CropState(this.mContext, this.mParentLayout, this.mEditorView);
            case 3:
                return new FilterState(this.mContext, this.mParentLayout, this.mEditorView);
            case 4:
                return new SplashState(this.mContext, this.mParentLayout, this.mEditorView);
            case 5:
                return new IllusionState(this.mContext, this.mParentLayout, this.mEditorView);
            case 6:
                return new AdjustState(this.mContext, this.mParentLayout, this.mEditorView);
            case 7:
                if (EditorLoadLib.isArcSoftLoaded() || EditorLoadLib.SFBJNI_LOADED) {
                    return new SkinSfbState(this.mContext, this.mParentLayout, this.mEditorView);
                }
                return new SkinOmronState(this.mContext, this.mParentLayout, this.mEditorView);
            case 8:
                return new MosaicState(this.mContext, this.mParentLayout, this.mEditorView);
            case 9:
                return new BrushState(this.mContext, this.mParentLayout, this.mEditorView);
            case 10:
                return new WaterMarkState(this.mContext, this.mParentLayout, this.mEditorView);
            case 11:
                return new LabelState(this.mContext, this.mParentLayout, this.mEditorView);
            default:
                return null;
        }
    }

    public void onStackChanged(boolean canUndo, boolean canRedo, boolean canCompare) {
        if (canUndo || canRedo) {
            if (this.mActionInfo == this.mDefaultActionInfo) {
                this.mActionInfo = this.mRedoUndoActionInfo;
            }
        } else if (this.mActionInfo == this.mRedoUndoActionInfo) {
            this.mActionInfo = this.mDefaultActionInfo;
        }
        super.onStackChanged(canUndo, canRedo, canCompare);
    }

    protected boolean needShowSavingProgress() {
        return true;
    }

    public SparseArray<IconData> getIconDataContainer() {
        return this.mIconDataContainer;
    }

    public OnClickListener getClickListener() {
        return this.mClickListener;
    }
}
