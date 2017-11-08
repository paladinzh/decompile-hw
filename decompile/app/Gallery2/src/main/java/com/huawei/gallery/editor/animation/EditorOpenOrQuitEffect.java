package com.huawei.gallery.editor.animation;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation.Mirror;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.watermark.manager.parse.WMElement;
import javax.microedition.khronos.opengles.GL11;

public class EditorOpenOrQuitEffect extends Animation {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues = null;
    protected RectF mCropRect;
    private EditorViewDelegate mEditorViewDelegate;
    private Mirror mMirror = Mirror.NONE;
    protected float mProgress;
    private RectF mRenderSource = new RectF();
    private RectF mRenderTarget = new RectF();
    private Rotation mRotation = Rotation.ZERO;
    protected Rect mSourceBounds;
    protected Rect mSourceRect;
    private float mStraighten = 0.0f;
    protected Rect mTargetRect;

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues() {
        if (-com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues != null) {
            return -com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues;
        }
        int[] iArr = new int[Mirror.values().length];
        try {
            iArr[Mirror.BOTH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mirror.HORIZONTAL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mirror.NONE.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mirror.VERTICAL.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues = iArr;
        return iArr;
    }

    public EditorOpenOrQuitEffect() {
        setInterpolator(new AccelerateInterpolator(2.0f));
        setDuration(300);
    }

    public EditorOpenOrQuitEffect(Interpolator interpolator) {
        setInterpolator(interpolator);
        setDuration(300);
    }

    public void init(Rect sourceRect, Rect targetRect) {
        this.mSourceRect = sourceRect;
        this.mTargetRect = targetRect;
    }

    protected void onCalculate(float progress) {
        this.mProgress = progress;
    }

    public PointF getCenterPoint() {
        return new PointF(((float) this.mSourceRect.centerX()) + (((float) (this.mTargetRect.centerX() - this.mSourceRect.centerX())) * this.mProgress), ((float) this.mSourceRect.centerY()) + (((float) (this.mTargetRect.centerY() - this.mSourceRect.centerY())) * this.mProgress));
    }

    public int getWidth() {
        return (int) (((float) this.mSourceRect.width()) + (((float) (this.mTargetRect.width() - this.mSourceRect.width())) * this.mProgress));
    }

    public int getHeight() {
        return (int) (((float) this.mSourceRect.height()) + (((float) (this.mTargetRect.height() - this.mSourceRect.height())) * this.mProgress));
    }

    public void setSourceBounds(Rect bounds) {
        this.mSourceBounds = bounds;
    }

    public void setCropRect(RectF cropRect) {
        this.mCropRect = cropRect;
    }

    public Rotation getRotation() {
        return this.mRotation;
    }

    public void setRotateArgs(Rotation rotation, Mirror mirror, Float straighten) {
        if (rotation != null) {
            this.mRotation = rotation;
        }
        if (mirror != null) {
            this.mMirror = mirror;
        }
        if (straighten != null) {
            this.mStraighten = straighten.floatValue();
        }
    }

    public boolean isRotate() {
        if (this.mRotation == Rotation.ZERO && this.mMirror == Mirror.NONE && this.mStraighten == 0.0f) {
            return false;
        }
        return true;
    }

    public void render(GLCanvas canvas, BitmapTexture previewTexture) {
        if (this.mSourceBounds == null || this.mSourceBounds.width() != previewTexture.getWidth() || this.mSourceBounds.height() != previewTexture.getHeight()) {
            renderByOpenAnimation(canvas, previewTexture);
        } else if (isRotate()) {
            renderCropByRotate(canvas, previewTexture, this.mCropRect);
        } else {
            if (this.mCropRect != null) {
                renderByOpenAnimation(canvas, previewTexture, this.mCropRect);
            }
        }
    }

    public void setComputerRender(EditorViewDelegate EditorViewDelegate) {
        this.mEditorViewDelegate = EditorViewDelegate;
    }

    private void renderCropByRotate(GLCanvas canvas, BitmapTexture previewTexture, RectF cropRect) {
        canvas.save();
        PointF centerPoint = getCenterPoint();
        Rotation rotation = getRotation();
        rotateCanvas(canvas, centerPoint);
        int width = getWidth();
        int height = getHeight();
        if (cropRect != null) {
            if (rotation == Rotation.NINETY || rotation == Rotation.TWO_SEVENTY) {
                this.mRenderTarget.set(centerPoint.x - (((float) height) / 2.0f), centerPoint.y - (((float) width) / 2.0f), centerPoint.x + (((float) height) / 2.0f), centerPoint.y + (((float) width) / 2.0f));
            } else {
                this.mRenderTarget.set(centerPoint.x - (((float) width) / 2.0f), centerPoint.y - (((float) height) / 2.0f), centerPoint.x + (((float) width) / 2.0f), centerPoint.y + (((float) height) / 2.0f));
            }
            canvas.drawTexture(previewTexture, new RectF(0.0f, 0.0f, (float) previewTexture.getWidth(), (float) previewTexture.getHeight()), getRenderRect(cropRect, this.mRenderTarget));
            canvas.restore();
            canvas.save();
            renderCover(canvas);
        } else {
            renderByOpenAnimation(canvas, previewTexture);
        }
        canvas.restore();
    }

    private void rotateCanvas(GLCanvas canvas, PointF centerPoint) {
        canvas.translate(centerPoint.x, centerPoint.y);
        canvas.rotate(((float) this.mRotation.value()) + this.mStraighten, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        switch (-getcom-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues()[this.mMirror.ordinal()]) {
            case 1:
                canvas.scale(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
                break;
            case 2:
                canvas.scale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
                break;
            case 3:
                canvas.scale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
                break;
        }
        canvas.translate(-centerPoint.x, -centerPoint.y);
    }

    private RectF getRenderRect(RectF crop, RectF renderCrop) {
        if (EditorUtils.isAlmostEquals(crop.left, crop.right) || EditorUtils.isAlmostEquals(crop.top, crop.bottom)) {
            return new RectF();
        }
        float left;
        float right;
        float top;
        float bottom;
        if (EditorUtils.isAlmostEquals(crop.left, 0.0f)) {
            left = renderCrop.left;
            right = (renderCrop.right - ((WMElement.CAMERASIZEVALUE1B1 - crop.right) * left)) / crop.right;
        } else if (EditorUtils.isAlmostEquals(crop.right, (float) WMElement.CAMERASIZEVALUE1B1)) {
            right = renderCrop.right;
            left = (renderCrop.left - (crop.left * right)) / (WMElement.CAMERASIZEVALUE1B1 - crop.left);
        } else {
            left = ((renderCrop.left * crop.right) - (renderCrop.right * crop.left)) / (crop.right - crop.left);
            right = (renderCrop.left - ((WMElement.CAMERASIZEVALUE1B1 - crop.left) * left)) / crop.left;
        }
        if (EditorUtils.isAlmostEquals(crop.top, 0.0f)) {
            top = renderCrop.top;
            bottom = (renderCrop.bottom - ((WMElement.CAMERASIZEVALUE1B1 - crop.bottom) * top)) / crop.bottom;
        } else if (EditorUtils.isAlmostEquals(crop.bottom, (float) WMElement.CAMERASIZEVALUE1B1)) {
            bottom = renderCrop.bottom;
            top = (renderCrop.top - (crop.top * bottom)) / (WMElement.CAMERASIZEVALUE1B1 - crop.top);
        } else {
            top = ((renderCrop.top * crop.bottom) - (renderCrop.bottom * crop.top)) / (crop.bottom - crop.top);
            bottom = (renderCrop.top - ((WMElement.CAMERASIZEVALUE1B1 - crop.top) * top)) / crop.top;
        }
        return new RectF(left, top, right, bottom);
    }

    private void renderCover(GLCanvas canvas) {
        PointF centerPoint = getCenterPoint();
        int width = getWidth();
        int height = getHeight();
        this.mRenderTarget.set(centerPoint.x - (((float) width) / 2.0f), centerPoint.y - (((float) height) / 2.0f), centerPoint.x + (((float) width) / 2.0f), centerPoint.y + (((float) height) / 2.0f));
        GL11 gl = canvas.getGLInstance();
        gl.glLineWidth(MapConfig.MIN_ZOOM);
        gl.glEnable(2848);
        gl.glEnable(2960);
        gl.glClear(1024);
        gl.glStencilOp(7680, 7680, 7681);
        gl.glStencilFunc(519, 1, 1);
        canvas.fillRect(this.mRenderTarget.left, this.mRenderTarget.top, this.mRenderTarget.width(), this.mRenderTarget.height(), 0);
        gl.glStencilFunc(517, 1, 1);
        gl.glStencilOp(7680, 7680, 7681);
        if (this.mEditorViewDelegate != null) {
            canvas.fillRect(0.0f, 0.0f, (float) this.mEditorViewDelegate.getWidth(), (float) this.mEditorViewDelegate.getHeight(), Color.argb(255, 0, 0, 0));
        }
        gl.glDisable(2960);
    }

    private void renderByOpenAnimation(GLCanvas canvas, BitmapTexture previewTexture) {
        PointF centerPoint = getCenterPoint();
        canvas.translate(centerPoint.x, centerPoint.y);
        int width = getWidth();
        int height = getHeight();
        previewTexture.draw(canvas, (-width) / 2, (-height) / 2, width, height);
    }

    protected void renderByOpenAnimation(GLCanvas canvas, BitmapTexture previewTexture, RectF cropRect) {
        PointF centerPoint = getCenterPoint();
        int width = getWidth();
        int height = getHeight();
        this.mRenderSource.set(((float) previewTexture.getWidth()) * cropRect.left, ((float) previewTexture.getHeight()) * cropRect.top, ((float) previewTexture.getWidth()) * cropRect.right, ((float) previewTexture.getHeight()) * cropRect.bottom);
        this.mRenderTarget.set(centerPoint.x - (((float) width) / 2.0f), centerPoint.y - (((float) height) / 2.0f), centerPoint.x + (((float) width) / 2.0f), centerPoint.y + (((float) height) / 2.0f));
        canvas.drawTexture(previewTexture, this.mRenderSource, this.mRenderTarget);
    }
}
