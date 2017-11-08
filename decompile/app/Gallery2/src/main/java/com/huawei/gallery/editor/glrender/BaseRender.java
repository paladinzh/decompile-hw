package com.huawei.gallery.editor.glrender;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLPaint;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.PositionController;
import com.huawei.gallery.editor.animation.EditorOpenOrQuitEffect;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer.RenderDelegate;
import com.huawei.gallery.util.LayoutHelper;

public class BaseRender implements RenderDelegate {
    protected EditorStateDelegate mDelegate;
    protected Rect mDisplayPaddingRect = new Rect(0, 0, 0, 0);
    protected EditorOpenOrQuitEffect mEditorOpenOrQuitEffect;
    protected EditorViewDelegate mEditorViewDelegate;
    protected GLPaint mPaint = new GLPaint();
    protected Rect mSourceRect = new Rect();
    protected RectF mSourceRectF = new RectF();
    protected Rect mTargetRect = new Rect();
    protected RectF mTargetRectF = new RectF();
    protected Rect mViewMarginRect = new Rect();

    public interface EditorStateDelegate {
        Bitmap computeRenderTexture();

        Bitmap getGeometryOnlyImage();

        BitmapTexture getGeometryTexture();

        int getMenuHeight();

        BitmapTexture getPreviewTexture();

        int getSubMenuHeight();

        boolean needDrawOriginBitmap();

        void onAnimationRenderFinished(Rect rect, Rect rect2);

        void onRenderFinished(Rect rect, boolean z);

        void setGeometryTexture(BitmapTexture bitmapTexture);
    }

    public interface EditorViewDelegate {
        int getActionBarHeight();

        GLRoot getGLRoot();

        int getHeight();

        int getNavigationBarHeight();

        TransitionStore getTransitionStore();

        int getWidth();

        void invalidate();

        boolean isPort();

        void updateBackground();

        void updateOriginalCompareButton();
    }

    public void setDisplayPaddingRect(int left, int top, int right, int bottom) {
        this.mDisplayPaddingRect.set(left, top, right, bottom);
    }

    public int getViewWidth() {
        return this.mEditorViewDelegate.getWidth();
    }

    public int getViewHeight() {
        return this.mEditorViewDelegate.getHeight();
    }

    public int getBitmapWidth() {
        if (this.mDelegate.computeRenderTexture() == null) {
            return 0;
        }
        return this.mDelegate.computeRenderTexture().getWidth();
    }

    public int getBitmapHeight() {
        if (this.mDelegate.computeRenderTexture() == null) {
            return 0;
        }
        return this.mDelegate.computeRenderTexture().getHeight();
    }

    public boolean isLongEdgeFull() {
        return true;
    }

    public Rect getViewMargins() {
        int bottom;
        int top = this.mEditorViewDelegate.getActionBarHeight();
        int right = this.mEditorViewDelegate.isPort() ? 0 : (this.mEditorViewDelegate.getNavigationBarHeight() + this.mDelegate.getMenuHeight()) + this.mDelegate.getSubMenuHeight();
        if (this.mEditorViewDelegate.isPort()) {
            bottom = (this.mEditorViewDelegate.getNavigationBarHeight() + this.mDelegate.getMenuHeight()) + this.mDelegate.getSubMenuHeight();
        } else if (LayoutHelper.isDefaultLandOrientationProduct()) {
            bottom = LayoutHelper.getNavigationBarHeightForDefaultLand();
        } else {
            bottom = 0;
        }
        this.mViewMarginRect.set(0, top, right, bottom);
        return this.mViewMarginRect;
    }

    public Rect getDisplayPaddings() {
        return this.mDisplayPaddingRect;
    }

    public Matrix getScaleMatrix() {
        return null;
    }

    public BaseRender(EditorViewDelegate render, EditorStateDelegate delegate) {
        this.mEditorViewDelegate = render;
        this.mDelegate = delegate;
    }

    public void updateGLPaint(int color, float lineWidth) {
        this.mPaint.setColor(color);
        this.mPaint.setLineWidth(lineWidth);
    }

    public void show() {
    }

    public void prepareGLOpenOrQuitAnimation() {
        TransitionStore transitionStore = this.mEditorViewDelegate.getTransitionStore();
        if (transitionStore != null) {
            Rect sourceRect = (Rect) transitionStore.get("key-quit-rect-for-editor");
            Bitmap bitmap = this.mDelegate.computeRenderTexture();
            if (bitmap != null) {
                int bmpWidth = bitmap.getWidth();
                int bmpHeight = bitmap.getHeight();
                if (sourceRect == null) {
                    sourceRect = computeDisplayRect(bmpWidth, bmpHeight);
                }
                Rect targetRect = new Rect();
                RectComputer.computerRect((RenderDelegate) this, null, targetRect);
                this.mEditorOpenOrQuitEffect = new EditorOpenOrQuitEffect();
                this.mEditorOpenOrQuitEffect.init(sourceRect, targetRect);
                this.mEditorOpenOrQuitEffect.setDuration(300);
                this.mEditorOpenOrQuitEffect.start();
            }
        }
    }

    public void prepareGLOpenOrQuitAnimation(Rect targetRect) {
        TransitionStore transitionStore = this.mEditorViewDelegate.getTransitionStore();
        if (transitionStore != null) {
            Rect sourceRect = (Rect) transitionStore.get("key-quit-rect-for-editor");
            Bitmap bitmap = this.mDelegate.computeRenderTexture();
            if (bitmap != null) {
                int bmpWidth = bitmap.getWidth();
                int bmpHeight = bitmap.getHeight();
                if (sourceRect == null) {
                    sourceRect = computeDisplayRect(bmpWidth, bmpHeight);
                }
                this.mEditorOpenOrQuitEffect = new EditorOpenOrQuitEffect();
                this.mEditorOpenOrQuitEffect.init(sourceRect, targetRect);
                this.mEditorOpenOrQuitEffect.setDuration(300);
                this.mEditorOpenOrQuitEffect.start();
            }
        }
    }

    public void prepareGLQuitAnimationArgs() {
        TransitionStore transitionStore = this.mEditorViewDelegate.getTransitionStore();
        if (transitionStore != null) {
            Rect target = new Rect();
            RectComputer.computerRect((RenderDelegate) this, this.mSourceRect, target);
            transitionStore.put("key-quit-rect-for-editor", target);
        }
    }

    public void render(GLCanvas canvas) {
        if (this.mDelegate.computeRenderTexture() != null) {
            RectComputer.computerRect((RenderDelegate) this, this.mSourceRect, this.mTargetRect);
            this.mDelegate.getPreviewTexture().draw(canvas, this.mTargetRect.left, this.mTargetRect.top, this.mTargetRect.width(), this.mTargetRect.height());
            this.mTargetRectF.set(this.mTargetRect);
            drawCompareImage(canvas, this.mTargetRect);
        }
    }

    protected void drawCompareImage(GLCanvas canvas, Rect imageBounds) {
        Bitmap image = this.mDelegate.getGeometryOnlyImage();
        if (this.mDelegate.needDrawOriginBitmap() && image != null) {
            this.mTargetRectF.set(imageBounds);
            canvas.save();
            float scale = ((float) image.getWidth()) / ((float) imageBounds.width());
            float sourceLeft = (this.mTargetRectF.left - ((float) imageBounds.left)) * scale;
            float sourceRight = (this.mTargetRectF.top - ((float) imageBounds.top)) * scale;
            this.mSourceRectF.set(sourceLeft, sourceRight, (this.mTargetRectF.width() * scale) + sourceLeft, (this.mTargetRectF.height() * scale) + sourceRight);
            BitmapTexture texture = this.mDelegate.getGeometryTexture();
            if (texture == null || texture.getBitmap() != image) {
                if (texture != null) {
                    texture.recycle();
                }
                this.mDelegate.setGeometryTexture(new BitmapTexture(image));
            }
            canvas.drawTexture(this.mDelegate.getGeometryTexture(), this.mSourceRectF, this.mTargetRectF);
            canvas.restore();
        }
    }

    protected void drawCompareImage(GLCanvas canvas, RectF sourceRectF, RectF drawRectF) {
        Bitmap image = this.mDelegate.getGeometryOnlyImage();
        if (this.mDelegate.needDrawOriginBitmap() && image != null) {
            canvas.save();
            BitmapTexture texture = this.mDelegate.getGeometryTexture();
            if (texture == null || texture.getBitmap() != image) {
                if (texture != null) {
                    texture.recycle();
                }
                texture = new BitmapTexture(image);
                this.mDelegate.setGeometryTexture(texture);
            }
            canvas.drawTexture(texture, sourceRectF, drawRectF);
            canvas.restore();
        }
    }

    public void hide() {
    }

    protected Rect computeDisplayRect(int imageWidth, int imageHeight) {
        float scale = PositionController.getMinimalScale(imageWidth, imageHeight, this.mEditorViewDelegate.getWidth(), this.mEditorViewDelegate.getHeight());
        float renderWidth = ((float) imageWidth) * scale;
        float renderHeight = ((float) imageHeight) * scale;
        int offsetX = Math.round((((float) this.mEditorViewDelegate.getWidth()) - renderWidth) / 2.0f);
        int offsetY = Math.round((((float) this.mEditorViewDelegate.getHeight()) - renderHeight) / 2.0f);
        return new Rect(offsetX, offsetY, Math.round(renderWidth) + offsetX, Math.round(renderHeight) + offsetY);
    }

    protected boolean renderByOpenAnimation(GLCanvas canvas, EditorOpenOrQuitEffect openAnimation) {
        if (openAnimation == null) {
            return false;
        }
        openAnimation.render(canvas, this.mDelegate.getPreviewTexture());
        return true;
    }
}
