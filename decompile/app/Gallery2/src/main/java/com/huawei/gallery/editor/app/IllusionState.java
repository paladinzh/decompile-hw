package com.huawei.gallery.editor.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;
import com.huawei.gallery.editor.filters.FilterIllusionRepresentation;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender;
import com.huawei.gallery.editor.glrender.MenuRender;
import com.huawei.gallery.editor.step.IllusionEditorStep;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.EditorUIController;
import com.huawei.gallery.editor.ui.IllusionUIController;
import com.huawei.gallery.editor.ui.IllusionUIController.IllusionListener;

public class IllusionState extends EditorState implements IllusionListener {
    private Bitmap mApplyBitmap;
    private int mCurrentValue = -1;
    private FilterIllusionRepresentation mFilterIllusionRepresentation;
    private IllusionEditorStep mIllusionEditorStep;
    private IllusionUIController mIllusionUIController;
    private MenuRender mMenuRender;
    private float mScale;
    private Rect mViewBounds = new Rect();

    public IllusionState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
        this.mActionInfo = new ActionInfo(context.getResources().getString(R.string.simple_editor_illusion), Action.BACK, null, false);
        for (FilterRepresentation rep : this.mEditorView.getEditorManager().getIllusion()) {
            if (rep instanceof FilterIllusionRepresentation) {
                this.mFilterIllusionRepresentation = (FilterIllusionRepresentation) rep;
            }
        }
    }

    protected BaseRender createRender() {
        this.mMenuRender = new MenuRender(this.mEditorView, this);
        return this.mMenuRender;
    }

    protected EditorUIController createUIController() {
        this.mIllusionUIController = new IllusionUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
        return this.mIllusionUIController;
    }

    public void show() {
        this.mIllusionEditorStep = new IllusionEditorStep();
        this.mIllusionEditorStep.setEditorState(this);
        super.show();
        if (this.mIllusionUIController != null) {
            this.mIllusionUIController.setFilterIllusionRepresentation(this.mFilterIllusionRepresentation);
            Bitmap bmp = computeRenderTexture();
            if (bmp != null) {
                this.mFilterIllusionRepresentation.setBound(new Rect(0, 0, bmp.getWidth(), bmp.getHeight()));
            }
        }
    }

    public void hide() {
        super.hide();
        this.mViewBounds.setEmpty();
        getImage().cache(this.mApplyBitmap);
        this.mApplyBitmap = null;
        this.mFilterIllusionRepresentation.reset();
        this.mCurrentValue = -1;
    }

    public void onRenderFinished(Rect drawRect, boolean isAnimationFinished) {
        final boolean isValid = isAnimationFinished && !this.mEditorView.isNavigationBarAnimationRunning();
        if (drawRect != null && !this.mViewBounds.equals(drawRect)) {
            this.mViewBounds.set(drawRect);
            Activity activity = this.mEditorView.getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (IllusionState.this.isActive()) {
                            IllusionState.this.layoutIllusionView(isValid);
                        }
                    }
                });
            }
        }
    }

    protected void layoutIllusionView(boolean isVaild) {
        if (this.mIllusionUIController != null) {
            this.mIllusionUIController.layoutIllusionView(this.mViewBounds);
            if (isVaild) {
                onStrokeDataChange(this.mFilterIllusionRepresentation.copy());
            }
        }
    }

    public void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        if (changeSize) {
            this.mEditorView.invalidate();
        }
    }

    public void onStrokeDataChange(FilterIllusionRepresentation rep) {
        getSimpleEditorManager().showRepresentation(rep, this.mIllusionEditorStep);
    }

    public float getScaleScreenToImage(boolean force) {
        if (force || this.mScale == 0.0f) {
            this.mScale = ((float) getImage().getPreviewTexture().getBitmap().getWidth()) / ((float) this.mViewBounds.width());
        }
        return this.mScale;
    }

    public Bitmap getApplyBitmap() {
        return this.mApplyBitmap;
    }

    public void setApplyBitmap(final Bitmap bitmap) {
        Activity activity = this.mEditorView.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (IllusionState.this.mActivited) {
                        IllusionState.this.getImage().getBitmapCache().cache(IllusionState.this.mApplyBitmap);
                        IllusionState.this.mApplyBitmap = bitmap;
                        return;
                    }
                    IllusionState.this.getImage().getBitmapCache().cache(IllusionState.this.mApplyBitmap);
                    IllusionState.this.getImage().getBitmapCache().cache(bitmap);
                    IllusionState.this.mApplyBitmap = null;
                }
            });
        }
    }

    public void executeAction(Action action) {
        getSimpleEditorManager().pushEditorStep(this.mIllusionEditorStep);
    }

    public void onProgressChanged(float progress) {
        if (this.mFilterIllusionRepresentation != null) {
            this.mFilterIllusionRepresentation.setValue((int) progress);
            getSimpleEditorManager().showRepresentation(this.mFilterIllusionRepresentation.copy(), this.mIllusionEditorStep);
        }
    }

    public int getCurrentSeekbarValue() {
        if (this.mFilterIllusionRepresentation == null) {
            return 50;
        }
        return this.mFilterIllusionRepresentation.getValue();
    }

    protected boolean enableComparison() {
        return false;
    }

    public void setCurrentValue(int value) {
        this.mCurrentValue = value;
    }

    public int getCurrentValue() {
        return this.mCurrentValue;
    }
}
