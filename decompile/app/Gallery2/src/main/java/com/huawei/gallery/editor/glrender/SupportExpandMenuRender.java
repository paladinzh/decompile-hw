package com.huawei.gallery.editor.glrender;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.editor.animation.EditorMatrixAnimation;
import com.huawei.gallery.editor.animation.EditorOpenOrQuitEffect;
import com.huawei.gallery.editor.glrender.BaseRender.EditorStateDelegate;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer.RenderDelegate;
import com.huawei.watermark.manager.parse.WMElement;

public class SupportExpandMenuRender extends MenuRender {
    private EditorMatrixAnimation mMatrixAnimation;
    private Matrix mScaleMatrix = new Matrix();

    public SupportExpandMenuRender(EditorViewDelegate render, EditorStateDelegate delegate) {
        super(render, delegate);
    }

    public Matrix getScaleMatrix() {
        return this.mScaleMatrix;
    }

    public void prepareGLOpenOrQuitAnimationForScreenShot() {
        TransitionStore transitionStore = this.mEditorViewDelegate.getTransitionStore();
        if (transitionStore != null) {
            Rect viewRect = (Rect) transitionStore.get("key-quit-rect-for-editor");
            Rect bitmapBounds = (Rect) transitionStore.get("key-quit-bitmap-rect-for-editor");
            RectF cropRect = (RectF) transitionStore.get("key-quit-crop-rectf--for-editor");
            transitionStore.clear();
            Bitmap bitmap = this.mDelegate.computeRenderTexture();
            if (bitmap != null) {
                Rect sourceRect = viewRect;
                Rect targetRect = new Rect();
                int bmpWidth = bitmap.getWidth();
                int bmpHeight = bitmap.getHeight();
                if (viewRect == null) {
                    sourceRect = computeDisplayRect(bmpWidth, bmpHeight);
                }
                if (cropRect != null) {
                    RectComputer.computerRect(this, (int) (((float) bmpWidth) * cropRect.width()), (int) (((float) bmpHeight) * cropRect.height()), null, targetRect);
                } else {
                    RectComputer.computerRect((RenderDelegate) this, null, targetRect);
                }
                this.mEditorOpenOrQuitEffect = new EditorOpenOrQuitEffect();
                this.mEditorOpenOrQuitEffect.init(sourceRect, targetRect);
                this.mEditorOpenOrQuitEffect.setSourceBounds(bitmapBounds);
                this.mEditorOpenOrQuitEffect.setCropRect(cropRect);
                this.mEditorOpenOrQuitEffect.start();
            }
        }
    }

    public void prepareMatrixAnimation(Matrix matrix, boolean needAnime) {
        GLRoot root = this.mEditorViewDelegate.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mScaleMatrix.set(matrix);
                if (needAnime) {
                    Matrix sourceMatrix = new Matrix(this.mScaleMatrix);
                    if (!EditorUtils.isAlmostEquals(sourceMatrix, getTargetMatrix(sourceMatrix))) {
                        if (this.mMatrixAnimation == null) {
                            this.mMatrixAnimation = new EditorMatrixAnimation();
                        }
                        this.mMatrixAnimation.init(sourceMatrix, getTargetMatrix(sourceMatrix));
                        this.mMatrixAnimation.start();
                    }
                } else if (this.mMatrixAnimation != null) {
                    this.mMatrixAnimation.forceStop();
                }
                this.mEditorViewDelegate.invalidate();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public void render(GLCanvas canvas) {
        boolean more = false;
        long animTime = AnimationTime.get();
        if (this.mEditorOpenOrQuitEffect != null) {
            this.mEditorOpenOrQuitEffect.calculate(animTime);
            more = true;
            if (!this.mEditorOpenOrQuitEffect.isActive()) {
                this.mEditorOpenOrQuitEffect = null;
            }
        }
        if (this.mMatrixAnimation != null) {
            this.mMatrixAnimation.calculate(animTime);
            this.mScaleMatrix.set(this.mMatrixAnimation.getMatrix());
            more = true;
            if (!this.mMatrixAnimation.isActive()) {
                this.mMatrixAnimation = null;
            }
        }
        if (this.mEditorOpenOrQuitEffect != null) {
            renderByOpenAnimation(canvas, this.mEditorOpenOrQuitEffect);
        } else if (this.mDelegate.computeRenderTexture() != null) {
            RectComputer.computerRect((RenderDelegate) this, this.mSourceRect, this.mTargetRect);
            this.mSourceRectF.set(this.mSourceRect);
            this.mTargetRectF.set(this.mTargetRect);
            canvas.drawTexture(this.mDelegate.getPreviewTexture(), this.mSourceRectF, this.mTargetRectF);
            if (this.mMatrixAnimation == null) {
                this.mDelegate.onAnimationRenderFinished(this.mSourceRect, this.mTargetRect);
            }
            if (this.mDelegate.getGeometryOnlyImage() != null) {
                drawCompareImage(canvas, this.mSourceRectF, this.mTargetRectF);
            }
        }
        if (more) {
            this.mEditorViewDelegate.invalidate();
        }
    }

    public void hide() {
        this.mScaleMatrix.reset();
        this.mMatrixAnimation = null;
    }

    public void prepareGLQuitAnimationArgs() {
        this.mScaleMatrix.reset();
        super.prepareGLQuitAnimationArgs();
    }

    public boolean isMatrixAnimationActived() {
        return this.mMatrixAnimation != null ? this.mMatrixAnimation.isActive() : false;
    }

    public boolean isEditorOpenOrQuitEffectActived() {
        return this.mEditorOpenOrQuitEffect != null ? this.mEditorOpenOrQuitEffect.isActive() : false;
    }

    public Matrix getDoubleFingerControlMatrix() {
        return this.mScaleMatrix;
    }

    private int getDelta(int x, int y) {
        if (x + y >= 0) {
            return ((x + y) / 2) - x;
        }
        if (x < 0 && y < 0) {
            return 0;
        }
        if (x < 0) {
            return y;
        }
        return -x;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Matrix getTargetMatrix(Matrix source) {
        Matrix matrix = new Matrix();
        float[] values = new float[9];
        source.getValues(values);
        if (values[0] >= WMElement.CAMERASIZEVALUE1B1 && values[4] >= WMElement.CAMERASIZEVALUE1B1 && this.mDelegate.computeRenderTexture() != null) {
            Rect rect = new Rect();
            RectComputer.computerRect((RenderDelegate) this, null, rect, false);
            Rect margin = getViewMargins();
            int top = rect.top - margin.top;
            int bottom = (this.mEditorViewDelegate.getHeight() - rect.bottom) - margin.bottom;
            int dx = getDelta(rect.left - margin.left, (this.mEditorViewDelegate.getWidth() - rect.right) - margin.right);
            int dy = getDelta(top, bottom);
            matrix.set(source);
            matrix.postTranslate((float) dx, (float) dy);
        }
        return matrix;
    }
}
