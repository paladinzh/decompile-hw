package com.huawei.gallery.story.ui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.SlideshowView;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer.RenderDelegate;
import com.huawei.gallery.editor.tools.EditorUtils.RenderDelegateWithTexture;
import com.huawei.watermark.manager.parse.WMElement;

public class StorySlideshowView extends SlideshowView {
    private int mHeight;
    Matrix mMatrix = new Matrix();
    private final GLView mRoot;
    RectF mSourceRectF = new RectF();
    RectF mTargetRectF = new RectF();
    private Rect mTempRect = new Rect();
    private int mWidth;

    StorySlideshowView(GLView root) {
        this.mRoot = root;
    }

    protected Bitmap cropBitmap(Bitmap bitmap, int rotation) {
        if (this.mWidth == 0 || this.mHeight == 0) {
            return bitmap;
        }
        int w = this.mWidth;
        int h = this.mHeight;
        if (rotation % 90 != 0 || rotation % 180 == 0) {
            this.mTempRect.set(0, 0, w, h);
        } else {
            this.mTempRect.set(0, 0, h, w);
        }
        float scale = Math.min(WMElement.CAMERASIZEVALUE1B1, Math.min(((float) bitmap.getWidth()) / ((float) this.mTempRect.width()), ((float) bitmap.getHeight()) / ((float) this.mTempRect.height())));
        w = (int) (((float) this.mTempRect.width()) * scale);
        h = (int) (((float) this.mTempRect.height()) * scale);
        scale = Math.max(WMElement.CAMERASIZEVALUE1B1, Math.min(((float) w) / 510.0f, ((float) h) / 510.0f));
        Matrix matrix = new Matrix();
        matrix.setScale(WMElement.CAMERASIZEVALUE1B1 / scale, WMElement.CAMERASIZEVALUE1B1 / scale);
        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() / 2) - (w / 2), (bitmap.getHeight() / 2) - (h / 2), w, h, matrix, false);
    }

    public void render(GLCanvas canvas, int left, int top, int right, int bottom) {
        this.mWidth = right - left;
        this.mHeight = bottom - top;
        long animTime = AnimationTime.get();
        boolean requestRender = this.mTransitionAnimation.calculate(animTime);
        float alpha = this.mPrevTexture == null ? WMElement.CAMERASIZEVALUE1B1 : this.mTransitionAnimation.get();
        if (!(this.mPrevTexture == null || alpha == WMElement.CAMERASIZEVALUE1B1 || this.mPrevAnimation == null)) {
            requestRender |= this.mPrevAnimation.calculate(animTime);
            canvas.save(3);
            canvas.setAlpha(WMElement.CAMERASIZEVALUE1B1 - alpha);
            drawTexture(canvas, this.mPrevTexture, this.mPrevRotation, this.mPrevAnimation, left, top);
            canvas.restore();
        }
        if (!(this.mCurrentTexture == null || alpha == 0.0f || this.mCurrentAnimation == null)) {
            requestRender |= this.mCurrentAnimation.calculate(animTime);
            canvas.save(3);
            canvas.setAlpha(alpha);
            drawTexture(canvas, this.mCurrentTexture, this.mCurrentRotation, this.mCurrentAnimation, left, top);
            canvas.restore();
        }
        if (requestRender) {
            invalidate();
        }
    }

    private void drawTexture(GLCanvas canvas, BitmapTexture texture, int rotate, SlideshowAnimation animation, int offsetX, int offsetY) {
        float scale = Utils.clamp(animation == null ? WMElement.CAMERASIZEVALUE1B1 : (animation.getProgress() * 0.1f) + WMElement.CAMERASIZEVALUE1B1, (float) WMElement.CAMERASIZEVALUE1B1, 1.1f);
        this.mMatrix.reset();
        this.mMatrix.postScale(scale, scale);
        RenderDelegate renderDelegate = new RenderDelegateWithTexture(texture, this.mMatrix, this.mWidth, this.mHeight, rotate);
        RectComputer.computerRect(renderDelegate, this.mSourceRectF, this.mTargetRectF);
        if (rotate == 0) {
            this.mTargetRectF.offset((float) offsetX, (float) offsetY);
        } else if (rotate == 90) {
            this.mTargetRectF.offset((float) offsetY, (float) (-offsetX));
        } else if (rotate == 180) {
            this.mTargetRectF.offset((float) (-offsetX), (float) (-offsetY));
        } else if (rotate == 270) {
            this.mTargetRectF.offset((float) (-offsetY), (float) offsetX);
        }
        if (rotate != 0) {
            canvas.translate(((float) this.mWidth) / 2.0f, ((float) this.mHeight) / 2.0f);
            canvas.rotate((float) rotate, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            if (renderDelegate.isOverTurn()) {
                canvas.translate(((float) (-this.mHeight)) / 2.0f, ((float) (-this.mWidth)) / 2.0f);
            } else {
                canvas.translate(((float) (-this.mWidth)) / 2.0f, ((float) (-this.mHeight)) / 2.0f);
            }
        }
        canvas.drawTexture(texture, this.mSourceRectF, this.mTargetRectF);
    }

    public void invalidate() {
        this.mRoot.invalidate();
    }
}
