package com.huawei.gallery.editor.glrender;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.huawei.gallery.editor.animation.EditorOpenOrQuitEffect;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation.Mirror;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorStateDelegate;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;

public class BasePreviewRender extends BaseRender {
    public BasePreviewRender(EditorViewDelegate render, EditorStateDelegate delegate) {
        super(render, delegate);
    }

    public boolean isEditorOpenOrQuitEffectActive() {
        return this.mEditorOpenOrQuitEffect != null ? this.mEditorOpenOrQuitEffect.isActive() : false;
    }

    public void prepareGLOpenOrQuitAnimation() {
        TransitionStore transitionStore = this.mEditorViewDelegate.getTransitionStore();
        if (transitionStore != null) {
            Rect sourceRect = (Rect) transitionStore.get("key-quit-rect-for-editor");
            Rect bitmapBounds = (Rect) transitionStore.get("key-quit-bitmap-rect-for-editor");
            RectF cropRect = (RectF) transitionStore.get("key-quit-crop-rectf--for-editor");
            Rotation rotation = (Rotation) transitionStore.get("key-quit-rotate-for-editor");
            Mirror mirror = (Mirror) transitionStore.get("key-quit-mirror-for-editor");
            Float angle = (Float) transitionStore.get("key-quit-angle-for-editor");
            transitionStore.clear();
            Bitmap bitmap = this.mDelegate.computeRenderTexture();
            if (bitmap != null) {
                Rect targetRect;
                int bmpWidth = bitmap.getWidth();
                int bmpHeight = bitmap.getHeight();
                if (sourceRect == null) {
                    sourceRect = computeDisplayRect(bmpWidth, bmpHeight);
                }
                if (cropRect == null) {
                    targetRect = computeDisplayRectByRotate(bmpWidth, bmpHeight, rotation);
                } else {
                    targetRect = computeDisplayRectByRotate(Math.max(1, (int) (((float) bitmapBounds.width()) * cropRect.width())), Math.max(1, (int) (((float) bitmapBounds.height()) * cropRect.height())), rotation);
                }
                this.mEditorOpenOrQuitEffect = new EditorOpenOrQuitEffect();
                this.mEditorOpenOrQuitEffect.setSourceBounds(bitmapBounds);
                this.mEditorOpenOrQuitEffect.setCropRect(cropRect);
                this.mEditorOpenOrQuitEffect.setRotateArgs(rotation, mirror, angle);
                this.mEditorOpenOrQuitEffect.init(sourceRect, targetRect);
                this.mEditorOpenOrQuitEffect.setComputerRender(this.mEditorViewDelegate);
                this.mEditorOpenOrQuitEffect.start();
            }
        }
    }

    public void render(GLCanvas canvas) {
        long animTime = AnimationTime.get();
        if (this.mEditorOpenOrQuitEffect != null) {
            this.mEditorOpenOrQuitEffect.calculate(animTime);
            if (!this.mEditorOpenOrQuitEffect.isActive()) {
                this.mEditorOpenOrQuitEffect = null;
                this.mDelegate.onAnimationRenderFinished(null, null);
            }
            this.mEditorViewDelegate.invalidate();
        }
        if (this.mEditorOpenOrQuitEffect != null) {
            this.mEditorOpenOrQuitEffect.render(canvas, this.mDelegate.getPreviewTexture());
        } else {
            super.render(canvas);
        }
    }

    private Rect computeDisplayRectByRotate(int imageWidth, int imageHeight, Rotation rotation) {
        if (rotation != null && (rotation == Rotation.NINETY || rotation == Rotation.TWO_SEVENTY)) {
            int temp = imageWidth;
            imageWidth = imageHeight;
            imageHeight = temp;
        }
        Rect r = new Rect();
        RectComputer.computerRect(this, imageWidth, imageHeight, null, r);
        return r;
    }

    public void hide() {
        if (this.mEditorViewDelegate.getTransitionStore() != null) {
            prepareGLQuitAnimationArgs();
            this.mEditorOpenOrQuitEffect = null;
        }
    }
}
