package com.huawei.gallery.editor.animation;

import android.graphics.Rect;
import android.graphics.RectF;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLPaint;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation.Mirror;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.watermark.manager.parse.WMElement;
import javax.microedition.khronos.opengles.GL11;

public class EditorRotationEffect extends Animation {
    private RectF mCrop;
    private Rect mHighlightSourceRect;
    private float mHighlightSourceRotation;
    private float mHighlightTargetRotation;
    private float mProgress;
    private Rect mSourceRect;
    private float mSourceRotation;
    private float mStraightenAngle;
    private Rect mTargetRect;
    private float mTargetRotation;

    public void init(Rect sourceRect, Rect targetRect, Rotation sourceRotation, Rotation targetRotation, float straightenAngle, Rect highlightSourceRect, RectF crop) {
        this.mSourceRect = sourceRect;
        this.mTargetRect = targetRect;
        this.mStraightenAngle = straightenAngle;
        this.mHighlightSourceRect = highlightSourceRect;
        this.mHighlightSourceRotation = (float) sourceRotation.value();
        this.mSourceRotation = ((float) sourceRotation.value()) + straightenAngle;
        if (sourceRotation == Rotation.TWO_SEVENTY && targetRotation == Rotation.ZERO) {
            this.mHighlightTargetRotation = 360.0f;
            this.mTargetRotation = 360.0f + straightenAngle;
        } else {
            this.mHighlightTargetRotation = (float) targetRotation.value();
            this.mTargetRotation = ((float) targetRotation.value()) + straightenAngle;
        }
        this.mCrop = crop;
    }

    protected void onCalculate(float progress) {
        this.mProgress = progress;
    }

    public int getCurrentWidth() {
        return (int) (((float) this.mSourceRect.width()) + (((float) (this.mTargetRect.width() - this.mSourceRect.width())) * this.mProgress));
    }

    public int getCurrentHeight() {
        return (int) (((float) this.mSourceRect.height()) + (((float) (this.mTargetRect.height() - this.mSourceRect.height())) * this.mProgress));
    }

    public float getCurrentRotation() {
        return this.mSourceRotation + ((this.mTargetRotation - this.mSourceRotation) * this.mProgress);
    }

    public Rect getHighlightSourceRect() {
        return this.mHighlightSourceRect;
    }

    public int getHighlightCurrentWidth() {
        return (int) (((float) getCurrentWidth()) * this.mCrop.width());
    }

    public int getHighlightCurrentHeight() {
        return (int) (((float) getCurrentHeight()) * this.mCrop.height());
    }

    public float getHighlightCurrentRotation() {
        return this.mHighlightSourceRotation + ((this.mHighlightTargetRotation - this.mHighlightSourceRotation) * this.mProgress);
    }

    public float getStraightenAngle() {
        return this.mStraightenAngle;
    }

    public boolean render(GLCanvas canvas, GLPaint paint, BitmapTexture texture, Mirror mirror, EditorViewDelegate EditorViewDelegate) {
        canvas.save();
        canvas.translate((float) getHighlightSourceRect().centerX(), (float) getHighlightSourceRect().centerY());
        canvas.rotate(getCurrentRotation(), 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        if (mirror == Mirror.HORIZONTAL) {
            canvas.scale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
        } else if (mirror == Mirror.VERTICAL) {
            canvas.scale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
        } else if (mirror == Mirror.BOTH) {
            canvas.scale(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
        }
        int renderWidth = getCurrentWidth();
        int renderHeight = getCurrentHeight();
        texture.draw(canvas, -Math.round(((float) renderWidth) / 2.0f), -Math.round(((float) renderHeight) / 2.0f), renderWidth, renderHeight);
        canvas.restore();
        renderHighlight(canvas, paint, EditorViewDelegate);
        return isActive();
    }

    private void renderHighlight(GLCanvas canvas, GLPaint paint, EditorViewDelegate EditorViewDelegate) {
        GL11 gl = canvas.getGLInstance();
        gl.glLineWidth(MapConfig.MIN_ZOOM);
        gl.glEnable(2848);
        gl.glEnable(2960);
        gl.glClear(1024);
        gl.glStencilOp(7680, 7680, 7681);
        gl.glStencilFunc(519, 1, 1);
        canvas.save();
        canvas.translate((float) getHighlightSourceRect().centerX(), (float) getHighlightSourceRect().centerY());
        canvas.rotate(getHighlightCurrentRotation(), 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        int highlightWidth = getHighlightCurrentWidth();
        int highlightHeight = getHighlightCurrentHeight();
        canvas.fillRect((float) (-Math.round(((float) highlightWidth) / 2.0f)), (float) (-Math.round(((float) highlightHeight) / 2.0f)), (float) highlightWidth, (float) highlightHeight, 0);
        if (getStraightenAngle() != 0.0f) {
            canvas.drawRect((float) (-Math.round(((float) highlightWidth) / 2.0f)), (float) (-Math.round(((float) highlightHeight) / 2.0f)), (float) highlightWidth, (float) highlightHeight, paint);
        }
        canvas.restore();
        gl.glStencilFunc(517, 1, 1);
        gl.glStencilOp(7680, 7680, 7681);
        canvas.fillRect(0.0f, 0.0f, (float) EditorViewDelegate.getWidth(), (float) EditorViewDelegate.getHeight(), -16777216);
        gl.glDisable(2960);
    }
}
