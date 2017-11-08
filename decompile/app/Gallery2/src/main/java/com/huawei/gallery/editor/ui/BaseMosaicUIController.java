package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.ViewGroup;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation;
import com.huawei.gallery.editor.filters.FilterMosaicRepresentation.StrokeData;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.ui.MosaicView.Delegate;
import com.huawei.gallery.editor.ui.MosaicView.Listener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public abstract class BaseMosaicUIController extends EditorUIController implements Listener, Delegate {
    protected BaseMosaicListener mBaseMosaicListener;
    protected MosaicView mMosaicView;
    protected Rect mSourceRectVisible = new Rect();
    protected float[] mTmpPoint = new float[2];
    protected Matrix mTransCanvasToBitmapMatrix = new Matrix();
    protected Rect mViewBounds = new Rect();

    public interface BaseMosaicListener extends EditorUIController.Listener {
        boolean forceAddData();

        int getColor(int i, int i2);

        Bitmap getCurrentDrawBitmap(StrokeData strokeData);

        Matrix getDoubleFingerControlMatrix();

        Bitmap getExpandBitmap();

        float getExpandBitmapInitScale();

        boolean isEditorMatrixAnimeActive();

        void onDoubleFingerControlChange(Matrix matrix, boolean z);

        void onStrokeDataCommit(FilterMosaicRepresentation filterMosaicRepresentation);

        void reset();
    }

    public BaseMosaicUIController(Context context, ViewGroup parentLayout, BaseMosaicListener baseMosaicListener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, baseMosaicListener, EditorViewDelegate);
        this.mBaseMosaicListener = baseMosaicListener;
    }

    public void show() {
        super.show();
        onTouchChanged(true);
    }

    public void hide() {
        this.mViewBounds.setEmpty();
        this.mSourceRectVisible.setEmpty();
        super.hide();
    }

    public void setFilterMosaicRepresentation(FilterMosaicRepresentation representation) {
        if (this.mMosaicView != null) {
            this.mMosaicView.setFilterMosaicRepresentation(representation);
        }
    }

    public void onAnimationRenderFinished(Rect source, final Rect target) {
        updateScreenToImage(source, target);
        this.mSourceRectVisible.set(source);
        if (this.mContainer != null) {
            this.mContainer.post(new Runnable() {
                public void run() {
                    BaseMosaicUIController.this.layoutMosaicView(target);
                }
            });
        }
    }

    public void layoutMosaicView(Rect viewBounds) {
        if (this.mMosaicView != null) {
            int right;
            int bottom;
            this.mViewBounds.set(viewBounds);
            if (this.mEditorViewDelegate.getWidth() > this.mEditorViewDelegate.getHeight()) {
                right = (this.mEditorViewDelegate.getWidth() - viewBounds.right) - this.mEditorViewDelegate.getNavigationBarHeight();
                bottom = this.mEditorViewDelegate.getHeight() - viewBounds.bottom;
            } else {
                right = this.mEditorViewDelegate.getWidth() - viewBounds.right;
                bottom = (this.mEditorViewDelegate.getHeight() - viewBounds.bottom) - this.mEditorViewDelegate.getNavigationBarHeight();
            }
            this.mMosaicView.setPadding(viewBounds.left, viewBounds.top, right, bottom);
            this.mMosaicView.setVisibility(0);
            this.mMosaicView.setBounds(viewBounds, this.mSourceRectVisible);
            this.mMosaicView.invalidate();
        }
    }

    public int getColor(int x, int y) {
        return this.mBaseMosaicListener.getColor(x, y);
    }

    public int getPaintType() {
        return 1;
    }

    public Bitmap getExpandBitmap() {
        return this.mBaseMosaicListener.getExpandBitmap();
    }

    public float getExpandBitmapInitScale() {
        return this.mBaseMosaicListener.getExpandBitmapInitScale();
    }

    public Matrix getDoubleFingerControlMatrix() {
        return this.mBaseMosaicListener.getDoubleFingerControlMatrix();
    }

    public boolean isEditorMatrixAnimeActive() {
        return this.mBaseMosaicListener.isEditorMatrixAnimeActive();
    }

    public Matrix getBitmapToCanvasMatrix() {
        Matrix matrix = new Matrix();
        this.mTransCanvasToBitmapMatrix.invert(matrix);
        return matrix;
    }

    @SuppressWarnings({"EI_EXPOSE_REP"})
    public float[] getAbsolutePreviewPoint(float x, float y) {
        this.mTmpPoint[0] = x;
        this.mTmpPoint[1] = y;
        this.mTransCanvasToBitmapMatrix.mapPoints(this.mTmpPoint);
        return this.mTmpPoint;
    }

    public float[] getAbsolutePreviewPointWithOutCrop(float x, float y) {
        return getAbsolutePreviewPoint(x, y);
    }

    public Bitmap getCurrentDrawBitmap(StrokeData sd) {
        return this.mBaseMosaicListener.getCurrentDrawBitmap(sd);
    }

    public void onDoubleFingerControlChange(Matrix matrix, boolean needAnime) {
        this.mBaseMosaicListener.onDoubleFingerControlChange(matrix, needAnime);
    }

    public void onStrokeDataCommit(FilterMosaicRepresentation representation) {
        this.mBaseMosaicListener.onStrokeDataCommit(representation);
    }

    public boolean forceAddData() {
        return this.mBaseMosaicListener.forceAddData();
    }

    public void onTouchChanged(boolean clickable) {
    }

    public void reset() {
        this.mBaseMosaicListener.reset();
    }

    protected void updateScreenToImage(Rect source, Rect target) {
        if (this.mViewBounds.equals(target) && this.mSourceRectVisible.equals(source)) {
            int width = source.width();
            float scaleX = ((float) width) / ((float) target.width());
            float scaleY = ((float) source.height()) / ((float) target.height());
            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleY);
            matrix.postTranslate((float) source.left, (float) source.top);
            this.mTransCanvasToBitmapMatrix.set(matrix);
        }
    }
}
