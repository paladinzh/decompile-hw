package com.huawei.gallery.editor.imageshow;

import android.graphics.Rect;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.TileImageView;
import com.android.gallery3d.ui.TileImageView.Model;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.watermark.ui.WMComponent;

public class SinglePhoto {
    private int mContainerHeight;
    private int mImageHeight = -1;
    private int mImageRotation;
    private TileImageView mImageView;
    private int mImageWidth = -1;
    private Listener mListener;
    private OpenAnimationProxyView mOpenAnimationProxyView;
    private BaseEditorView mRoot;
    private ScaleInfo mScaleInfo = new ScaleInfo();

    public interface Listener {
        void onPhotoChange(int i, int i2, int i3, int i4, int i5, int i6);

        boolean renderOpenAnimation(GLCanvas gLCanvas);
    }

    private class OpenAnimationProxyView extends GLView {
        private OpenAnimationProxyView() {
        }

        protected void render(GLCanvas canvas) {
            if (SinglePhoto.this.mListener == null || !SinglePhoto.this.mListener.renderOpenAnimation(canvas)) {
                setVisibility(1);
                SinglePhoto.this.mImageView.setVisibility(0);
                return;
            }
            SinglePhoto.this.mImageView.setVisibility(1);
        }
    }

    private class ScaleInfo {
        private float mCurrentScale;
        private float mCurrentX;
        private float mCurrentY;

        private ScaleInfo() {
        }

        public void initialize() {
            this.mCurrentX = ((float) SinglePhoto.this.mImageWidth) / 2.0f;
            this.mCurrentY = ((float) SinglePhoto.this.mImageHeight) / 2.0f;
            this.mCurrentScale = Math.min(((float) SinglePhoto.this.getWidth()) / ((float) SinglePhoto.this.mImageWidth), ((float) SinglePhoto.this.getHeight()) / ((float) SinglePhoto.this.mImageHeight));
        }

        public float getCenterX() {
            return this.mCurrentX;
        }

        public float getCenterY() {
            return this.mCurrentY;
        }

        public float getScale() {
            return this.mCurrentScale;
        }
    }

    public SinglePhoto(BaseEditorView root, Listener listener) {
        GalleryContext context = root.getGalleryContext();
        this.mRoot = root;
        this.mListener = listener;
        this.mImageView = new TileImageView(context);
        this.mContainerHeight = context.getResources().getDimensionPixelSize(R.dimen.crop_state_layout_bottom_margin);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        Rect rect = computeDisplayRect(l, t, r, b);
        this.mImageView.layout(rect.left, rect.top, rect.right, rect.bottom);
        if (this.mImageHeight != -1) {
            this.mScaleInfo.initialize();
        }
    }

    private Rect computeDisplayRect(int l, int t, int r, int b) {
        int left;
        int right;
        int bottom;
        int width = r - l;
        int height = b - t;
        if (width < height) {
            left = 0;
            right = width;
            bottom = (height - this.mRoot.getNavigationBarHeight()) - this.mContainerHeight;
        } else {
            left = 0;
            right = width - this.mRoot.getNavigationBarHeight();
            bottom = height - this.mContainerHeight;
            if (LayoutHelper.isDefaultLandOrientationProduct()) {
                bottom -= LayoutHelper.getNavigationBarHeightForDefaultLand();
            }
        }
        return new Rect(left, this.mRoot.getActionBarHeight(), right, bottom);
    }

    private boolean setImageViewPosition(float centerX, float centerY, float scale) {
        float inverseX = ((float) this.mImageWidth) - centerX;
        float inverseY = ((float) this.mImageHeight) - centerY;
        TileImageView t = this.mImageView;
        int rotation = this.mImageRotation;
        switch (rotation) {
            case 0:
                if (this.mListener != null) {
                    Rect bounds = t.bounds();
                    int offsetX = Math.round((((float) t.getWidth()) / 2.0f) - (centerX * scale));
                    int offsetY = Math.round((((float) t.getHeight()) / 2.0f) - (centerY * scale));
                    int rightPadding = offsetX;
                    int bottomPadding = offsetY;
                    this.mListener.onPhotoChange(bounds.left + offsetX, bounds.top + offsetY, offsetX, offsetY, Math.round(((float) this.mImageWidth) * scale), Math.round(((float) this.mImageHeight) * scale));
                }
                return t.setPosition(centerX, centerY, scale, 0);
            case WMComponent.ORI_90 /*90*/:
                return t.setPosition(centerY, inverseX, scale, 90);
            case 180:
                return t.setPosition(inverseX, inverseY, scale, 180);
            case 270:
                return t.setPosition(inverseY, centerX, scale, 270);
            default:
                throw new IllegalArgumentException(String.valueOf(rotation));
        }
    }

    public void render(GLCanvas canvas) {
        ScaleInfo a = this.mScaleInfo;
        setImageViewPosition(a.getCenterX(), a.getCenterY(), a.getScale());
    }

    public int getWidth() {
        return this.mImageView.getWidth();
    }

    public int getHeight() {
        return this.mImageView.getHeight();
    }

    public void setDataModel(Model dataModel, int rotation) {
        if (((rotation / 90) & 1) != 0) {
            this.mImageWidth = dataModel.getImageHeight();
            this.mImageHeight = dataModel.getImageWidth();
        } else {
            this.mImageWidth = dataModel.getImageWidth();
            this.mImageHeight = dataModel.getImageHeight();
        }
        this.mImageRotation = rotation;
        this.mImageView.setModel(dataModel);
        this.mScaleInfo.initialize();
    }

    public void resume() {
        this.mOpenAnimationProxyView = new OpenAnimationProxyView();
        this.mRoot.addComponent(this.mOpenAnimationProxyView);
        this.mRoot.addComponent(this.mImageView);
        this.mImageView.prepareTextures();
    }

    public void pause() {
        this.mRoot.removeComponent(this.mOpenAnimationProxyView);
        this.mRoot.removeComponent(this.mImageView);
        this.mImageView.freeTextures();
    }

    public Rect getOpenAnimationRect(BaseEditorView editorView) {
        Rect viewBounds = editorView.bounds();
        onLayout(true, viewBounds.left, viewBounds.top, viewBounds.right, viewBounds.bottom);
        Rect rect = computeDisplayRect(viewBounds.left, viewBounds.top, viewBounds.right, viewBounds.bottom);
        int offsetX = Math.round((((float) rect.width()) / 2.0f) - (this.mScaleInfo.getCenterX() * this.mScaleInfo.getScale())) + rect.left;
        int offsetY = Math.round((((float) rect.height()) / 2.0f) - (this.mScaleInfo.getCenterY() * this.mScaleInfo.getScale())) + rect.top;
        rect.set(offsetX, offsetY, Math.round(((float) this.mImageWidth) * this.mScaleInfo.getScale()) + offsetX, Math.round(((float) this.mImageHeight) * this.mScaleInfo.getScale()) + offsetY);
        return rect;
    }
}
