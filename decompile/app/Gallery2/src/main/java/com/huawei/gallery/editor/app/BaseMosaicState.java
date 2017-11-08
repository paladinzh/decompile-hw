package com.huawei.gallery.editor.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Handler;
import android.view.ViewGroup;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.editor.cache.CustDrawBrushCache;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.draw.DrawStyle;
import com.huawei.gallery.editor.glrender.BaseRender;
import com.huawei.gallery.editor.glrender.SupportExpandMenuRender;
import com.huawei.gallery.editor.step.MosaicEditorStep;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseMosaicUIController;
import com.huawei.gallery.editor.ui.BaseMosaicUIController.BaseMosaicListener;
import com.huawei.gallery.editor.ui.EditorUIController;

public abstract class BaseMosaicState extends BaseActionState implements BaseMosaicListener {
    private BaseMosaicUIController mBaseMosaicUIController;
    private Bitmap mCurrentDataBmp;
    private DrawStyle[] mDrawingsTypes;
    protected FilterMosaicRepresentation mFilterMosaicRepresentation;
    private Handler mHandler = new Handler();
    protected MosaicEditorStep mMosaicEditorStep;
    protected SupportExpandMenuRender mSupportExpandMenuRender;

    protected abstract BaseMosaicUIController createBaseMosaicUIController();

    public BaseMosaicState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
        for (FilterRepresentation rep : this.mEditorView.getEditorManager().getMosaics()) {
            if (rep instanceof FilterMosaicRepresentation) {
                this.mFilterMosaicRepresentation = (FilterMosaicRepresentation) rep;
            }
        }
        this.mDrawingsTypes = this.mEditorView.getEditorManager().getDrawingsTypes();
    }

    protected BaseRender createRender() {
        this.mSupportExpandMenuRender = new SupportExpandMenuRender(this.mEditorView, this);
        setMenuRenderPadding();
        return this.mSupportExpandMenuRender;
    }

    protected void setMenuRenderPadding() {
    }

    protected EditorUIController createUIController() {
        this.mBaseMosaicUIController = createBaseMosaicUIController();
        return this.mBaseMosaicUIController;
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        setMenuRenderPadding();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                BaseMosaicState.this.onDoubleFingerControlChange(BaseMosaicState.this.mSupportExpandMenuRender.getDoubleFingerControlMatrix(), true);
            }
        }, 200);
    }

    public void show() {
        super.show();
        this.mFilterMosaicRepresentation.reset();
        this.mBaseMosaicUIController.setFilterMosaicRepresentation(this.mFilterMosaicRepresentation);
        this.mCurrentDataBmp = getImage().getBitmapCacheCopy(getCurrentDataBmp());
        reset();
    }

    protected Bitmap getCurrentDataBmp() {
        return getImage().getFilteredImage();
    }

    public Matrix getDoubleFingerControlMatrix() {
        return this.mSupportExpandMenuRender.getDoubleFingerControlMatrix();
    }

    public boolean isEditorMatrixAnimeActive() {
        return this.mSupportExpandMenuRender.isMatrixAnimationActived();
    }

    public void onDoubleFingerControlChange(Matrix matrix, boolean needAnime) {
        this.mSupportExpandMenuRender.prepareMatrixAnimation(matrix, needAnime);
    }

    public void hide() {
        super.hide();
        getImage().cache(this.mCurrentDataBmp);
        this.mCurrentDataBmp = null;
    }

    public void onLeaveEditor() {
        getImage().resetDrawCache();
    }

    public void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        if (changeSize) {
            this.mEditorView.invalidate();
        }
    }

    public void onNavigationBarChanged() {
        super.onNavigationBarChanged();
        this.mEditorView.invalidate();
    }

    public void onStrokeDataChange(FilterMosaicRepresentation representation) {
        getSimpleEditorManager().showRepresentation(representation, this.mMosaicEditorStep);
    }

    public void onStrokeDataCommit(FilterMosaicRepresentation representation) {
    }

    public boolean forceAddData() {
        return false;
    }

    public int getColor(int x, int y) {
        return 0;
    }

    public Bitmap getExpandBitmap() {
        return getImage().getFilteredImage();
    }

    public float getExpandBitmapInitScale() {
        Bitmap bmp = getImage().getFilteredImage();
        Rect rect = this.mSupportExpandMenuRender.getViewMargins();
        return Math.min(((float) ((this.mEditorView.getWidth() - rect.left) - rect.right)) / ((float) bmp.getWidth()), ((float) ((this.mEditorView.getHeight() - rect.top) - rect.bottom)) / ((float) bmp.getHeight()));
    }

    public void reset() {
        if (this.mCurrentDataBmp != null) {
            new Canvas(this.mCurrentDataBmp).drawColor(0, Mode.CLEAR);
        }
    }

    public Bitmap getCurrentDrawBitmap(StrokeData sd) {
        if (this.mCurrentDataBmp == null || sd == null) {
            return this.mCurrentDataBmp;
        }
        GLRoot root = this.mEditorView.getGLRoot();
        if (root == null) {
            return this.mCurrentDataBmp;
        }
        root.lockRenderThread();
        try {
            Canvas canvas = new Canvas(this.mCurrentDataBmp);
            CustDrawBrushCache brushCache = getImage().getCustDrawBrushCache();
            if (sd.type != 5) {
                if (sd.type < 5 && sd.type >= 1) {
                    canvas.drawColor(0, Mode.CLEAR);
                }
                if (this.mDrawingsTypes != null && this.mDrawingsTypes.length > sd.type) {
                    this.mDrawingsTypes[sd.type].paint(getImage().getDrawCacheApplyBitmap(), canvas, new Matrix(), sd, brushCache);
                }
            } else {
                Bitmap bitmap = getBitmap();
                if (this.mDrawingsTypes != null && this.mDrawingsTypes.length > sd.type) {
                    this.mDrawingsTypes[6].paint(bitmap, canvas, new Matrix(), sd, brushCache);
                }
            }
            Bitmap bitmap2 = this.mCurrentDataBmp;
            return bitmap2;
        } finally {
            root.unlockRenderThread();
        }
    }

    private Bitmap getBitmap() {
        switch (this.mBaseMosaicUIController.getPaintType()) {
            case 0:
            case 1:
                Bitmap bitmap = getImage().getCacheLatestBitmap();
                if (bitmap == null) {
                    return getImage().getOriginalBitmapLarge();
                }
                return bitmap;
            case 2:
                return getImage().getDrawCacheApplyBitmap();
            default:
                return null;
        }
    }

    public void onAnimationRenderFinished(Rect source, Rect target) {
        this.mBaseMosaicUIController.onAnimationRenderFinished(source, target);
        Activity activity = this.mEditorView.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    BaseMosaicState.this.layoutMosaicView(!BaseMosaicState.this.mEditorView.isNavigationBarAnimationRunning());
                }
            });
        }
    }

    private void layoutMosaicView(boolean isLayoutFinished) {
        if (isLayoutFinished && getImage().getDrawCacheApplyBitmap() == null) {
            FilterRepresentation filterMosaicRepresentation = this.mFilterMosaicRepresentation.copy();
            if (filterMosaicRepresentation instanceof FilterMosaicRepresentation) {
                ((FilterMosaicRepresentation) filterMosaicRepresentation).disableNil();
                onStrokeDataChange((FilterMosaicRepresentation) filterMosaicRepresentation);
            }
        }
    }
}
