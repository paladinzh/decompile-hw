package com.huawei.gallery.editor.glrender;

import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.anim.Animation.AnimationListener;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.BitmapTexture;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.autonavi.amap.mapcore.VirtualEarthProjection;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.gallery.editor.animation.EditorOpenOrQuitEffect;
import com.huawei.gallery.editor.animation.EditorRotationEffect;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation.Mirror;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;
import com.huawei.gallery.editor.glrender.BaseRender.EditorStateDelegate;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils.RectComputer;
import com.huawei.watermark.manager.parse.WMElement;
import javax.microedition.khronos.opengles.GL11;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class RotateRender extends MenuRender {
    private static final int LINE_STEP = GalleryUtils.dpToPixel(36);
    private ValueAnimator mAutoRotateAnimation;
    private FloatAnimation mColorShadowFadeOutAnimation = new FloatAnimation(WMElement.CAMERASIZEVALUE1B1, 0.0f, 1000);
    private FloatAnimation mCurrentLineAnimation;
    private RenderInfo mCurrentRenderInfo;
    private EditorRotationEffect mEditorRotationEffect = new EditorRotationEffect();
    private FloatAnimation mLineFadeInAnimation = new FloatAnimation(0.0f, WMElement.CAMERASIZEVALUE1B1, 500);
    private FloatAnimation mLineFadeOutAnimation = new FloatAnimation(WMElement.CAMERASIZEVALUE1B1, 0.0f, 500);
    private FloatAnimation mResetAnimation;
    private RotateDelegate mRotateDelegate;
    private Rect mTextureRect = new Rect();
    private volatile boolean mTracking;

    public interface RotateDelegate extends EditorStateDelegate {
        float getCurrentRotatedAngle();

        boolean getForceUpdateView();

        Mirror getMirror();

        int getPaintColor();

        Rotation getRotation();

        void rotateCW();
    }

    private static class RenderInfo {
        public float straightenMaxRadius;
        public Rect textureRect;
        public float textureRectAngle;

        public RenderInfo(Rect renderRect) {
            this.straightenMaxRadius = (float) Math.sqrt(Math.pow(((double) renderRect.width()) / 2.0d, 2.0d) + Math.pow(((double) renderRect.height()) / 2.0d, 2.0d));
            this.textureRectAngle = (float) ((Math.asin(((double) renderRect.width()) / (((double) this.straightenMaxRadius) * 2.0d)) * VirtualEarthProjection.MaxLongitude) / 3.141592653589793d);
            this.textureRect = new Rect(renderRect);
        }

        public Rect getRenderRect(Rotation rotation, float straightenAngle) {
            int renderWidth;
            int renderHeight;
            float positiveStraightenAngle = Math.abs(straightenAngle);
            float scale = (float) Math.max(((Math.cos((((double) Math.abs(positiveStraightenAngle - this.textureRectAngle)) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) * ((double) this.straightenMaxRadius)) * 2.0d) / ((double) this.textureRect.height()), ((Math.cos((((double) Math.abs((90.0f - positiveStraightenAngle) - this.textureRectAngle)) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) * ((double) this.straightenMaxRadius)) * 2.0d) / ((double) this.textureRect.width()));
            if (rotation == Rotation.NINETY || rotation == Rotation.TWO_SEVENTY) {
                renderWidth = Math.round(((float) this.textureRect.height()) * scale);
                renderHeight = Math.round(((float) this.textureRect.width()) * scale);
            } else {
                renderWidth = Math.round(((float) this.textureRect.width()) * scale);
                renderHeight = Math.round(((float) this.textureRect.height()) * scale);
            }
            return new Rect(0, 0, renderWidth, renderHeight);
        }
    }

    public RotateRender(EditorViewDelegate render, RotateDelegate delegate, Context context) {
        super(render, delegate);
        this.mRotateDelegate = delegate;
        this.mLineFadeOutAnimation.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd() {
                RotateRender.this.mTracking = false;
                RotateRender.this.mEditorViewDelegate.invalidate();
            }
        });
        this.mEditorRotationEffect.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd() {
                RotateRender.this.mRotateDelegate.rotateCW();
                RotateRender.this.mColorShadowFadeOutAnimation.start();
                RotateRender.this.mEditorViewDelegate.invalidate();
            }
        });
    }

    public void prepareLineFadeInAnimation() {
        GLRoot root = this.mEditorViewDelegate.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mLineFadeOutAnimation.forceStop();
                this.mTracking = true;
                this.mCurrentLineAnimation = this.mLineFadeInAnimation;
                this.mCurrentLineAnimation.start();
                this.mEditorViewDelegate.invalidate();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public void prepareLineFadeOutAnimation() {
        GLRoot root = this.mEditorViewDelegate.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mLineFadeInAnimation.forceStop();
                this.mCurrentLineAnimation = this.mLineFadeOutAnimation;
                this.mCurrentLineAnimation.start();
                this.mEditorViewDelegate.invalidate();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public void prepareResetAnimation() {
        GLRoot root = this.mEditorViewDelegate.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mResetAnimation = new FloatAnimation(this.mRotateDelegate.getCurrentRotatedAngle(), 0.0f, (int) ((Math.abs(this.mRotateDelegate.getCurrentRotatedAngle()) * 500.0f) / 45.0f));
                this.mResetAnimation.start();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public void prepareEditorRotationAnimation(Rotation sourceRotation, Rotation targetRotation, RectF crop) {
        this.mEditorRotationEffect.forceStop();
        Bitmap bmp = this.mRotateDelegate.computeRenderTexture();
        if (bmp != null) {
            Rect highlightSourceRect = computeRect(bmp.getWidth(), bmp.getHeight(), sourceRotation);
            RenderInfo sourceRenderInfo = new RenderInfo(highlightSourceRect);
            RenderInfo targetRenderInfo = new RenderInfo(computeRect(bmp.getWidth(), bmp.getHeight(), targetRotation));
            this.mEditorRotationEffect.init(sourceRenderInfo.getRenderRect(sourceRotation, this.mRotateDelegate.getCurrentRotatedAngle()), targetRenderInfo.getRenderRect(targetRotation, this.mRotateDelegate.getCurrentRotatedAngle()), sourceRotation, targetRotation, this.mRotateDelegate.getCurrentRotatedAngle(), highlightSourceRect, crop);
            this.mEditorRotationEffect.setDuration(300);
            this.mEditorRotationEffect.start();
            this.mEditorViewDelegate.invalidate();
        }
    }

    public void prepareGLOpenOrQuitAnimation() {
        TransitionStore transitionStore = this.mEditorViewDelegate.getTransitionStore();
        if (transitionStore != null) {
            GalleryLog.v("RotateRender", "RotateRender Quit Animation");
            Rect sourceRect = (Rect) transitionStore.get("key-quit-rect-for-editor");
            Bitmap bitmap = this.mDelegate.computeRenderTexture();
            if (bitmap != null) {
                int bw = bitmap.getWidth();
                int bh = bitmap.getHeight();
                if (sourceRect == null) {
                    sourceRect = computeDisplayRect(bw, bh);
                }
                Rect targetRect = computeRect(bw, bh, Rotation.ZERO);
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
            Bitmap bitmap = this.mDelegate.computeRenderTexture();
            if (bitmap != null) {
                transitionStore.put("key-quit-rect-for-editor", computeRect(bitmap.getWidth(), bitmap.getHeight(), Rotation.ZERO));
            }
        }
    }

    public void prepareAutoRotateAnimation(float targetAngle, AnimatorUpdateListener updateListener, AnimatorListener animatorListener) {
        this.mAutoRotateAnimation = ValueAnimator.ofFloat(new float[]{0.0f, targetAngle});
        this.mAutoRotateAnimation.addUpdateListener(updateListener);
        this.mAutoRotateAnimation.addListener(animatorListener);
        this.mAutoRotateAnimation.setDuration((long) (((int) ((Math.abs(targetAngle) * 500.0f) / 45.0f)) + AbsAlbumPage.LAUNCH_QUIK_ACTIVITY));
        this.mAutoRotateAnimation.setStartDelay(400);
        this.mAutoRotateAnimation.start();
    }

    public void show() {
        this.mTextureRect.setEmpty();
    }

    public Rect getRenderRect() {
        return new RenderInfo(this.mTextureRect).getRenderRect(this.mRotateDelegate.getRotation(), this.mRotateDelegate.getCurrentRotatedAngle());
    }

    public void render(GLCanvas canvas) {
        long animTime = AnimationTime.get();
        int more = this.mEditorRotationEffect.calculate(animTime) | this.mColorShadowFadeOutAnimation.calculate(animTime);
        if (this.mResetAnimation != null) {
            more |= this.mResetAnimation.calculate(animTime);
            if (!this.mResetAnimation.isActive()) {
                this.mResetAnimation = null;
            }
        }
        if (this.mEditorOpenOrQuitEffect != null) {
            this.mEditorOpenOrQuitEffect.calculate(animTime);
            more = 1;
            if (!this.mEditorOpenOrQuitEffect.isActive()) {
                this.mEditorOpenOrQuitEffect = null;
                this.mColorShadowFadeOutAnimation.start();
                this.mColorShadowFadeOutAnimation.calculate(animTime);
            }
        }
        if (this.mEditorOpenOrQuitEffect != null) {
            canvas.fillRect(0.0f, 0.0f, (float) this.mEditorViewDelegate.getWidth(), (float) this.mEditorViewDelegate.getHeight(), Color.argb(SmsCheckResult.ESCT_208, 0, 0, 0));
            renderByOpenAnimation(canvas, this.mEditorOpenOrQuitEffect);
        } else if (this.mEditorRotationEffect.isActive()) {
            more |= renderByRotationAnimation(canvas);
        } else {
            more |= renderByTouchDial(canvas, animTime);
        }
        if (more != 0) {
            this.mEditorViewDelegate.invalidate();
        }
    }

    private boolean renderByTouchDial(GLCanvas canvas, long animTime) {
        FloatAnimation animation = this.mCurrentLineAnimation;
        if (animation != null) {
            animation.calculate(animTime);
        }
        Bitmap bitmap = this.mDelegate.computeRenderTexture();
        if (bitmap == null) {
            return false;
        }
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        Rotation rotation = this.mRotateDelegate.getRotation();
        Mirror mirror = this.mRotateDelegate.getMirror();
        computeDisplayRect(imageWidth, imageHeight, rotation);
        drawPhoto(canvas, rotation, mirror, this.mResetAnimation == null ? this.mRotateDelegate.getCurrentRotatedAngle() : this.mResetAnimation.get(), this.mCurrentRenderInfo, this.mDelegate.getPreviewTexture());
        drawReferenceLine(canvas, animation);
        return animation != null ? animation.isActive() : false;
    }

    private void computeDisplayRect(int bw, int bh, Rotation rotation) {
        Rect currentRect = computeRect(bw, bh, rotation);
        if (!this.mTextureRect.equals(currentRect) || this.mRotateDelegate.getForceUpdateView()) {
            this.mCurrentRenderInfo = new RenderInfo(currentRect);
            this.mDelegate.onAnimationRenderFinished(null, currentRect);
        }
        this.mTextureRect.set(currentRect);
    }

    private boolean renderByRotationAnimation(GLCanvas canvas) {
        Bitmap bitmap = this.mDelegate.computeRenderTexture();
        BitmapTexture texture = this.mDelegate.getPreviewTexture();
        if (bitmap == null || texture == null) {
            return false;
        }
        return this.mEditorRotationEffect.render(canvas, this.mPaint, texture, this.mRotateDelegate.getMirror(), this.mEditorViewDelegate);
    }

    private void drawPhoto(GLCanvas canvas, Rotation rotation, Mirror mirror, float straightenAngle, RenderInfo renderInfo, BitmapTexture texture) {
        if (renderInfo != null) {
            canvas.save();
            canvas.translate((float) renderInfo.textureRect.centerX(), (float) renderInfo.textureRect.centerY());
            canvas.rotate(((float) rotation.value()) + straightenAngle, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            if (mirror == Mirror.HORIZONTAL) {
                canvas.scale(GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
            } else if (mirror == Mirror.VERTICAL) {
                canvas.scale(WMElement.CAMERASIZEVALUE1B1, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
            } else if (mirror == Mirror.BOTH) {
                canvas.scale(GroundOverlayOptions.NO_DIMENSION, GroundOverlayOptions.NO_DIMENSION, WMElement.CAMERASIZEVALUE1B1);
            }
            Rect renderRect = renderInfo.getRenderRect(rotation, straightenAngle);
            texture.draw(canvas, -Math.round(((float) renderRect.width()) / 2.0f), -Math.round(((float) renderRect.height()) / 2.0f), renderRect.width(), renderRect.height());
            canvas.restore();
        }
    }

    private void drawReferenceLine(GLCanvas canvas, FloatAnimation animation) {
        boolean needDrawHighlightBorder = Math.abs(this.mRotateDelegate.getCurrentRotatedAngle()) > 0.0f;
        drawHighlightRectangle(canvas, this.mRotateDelegate.getPaintColor(), this.mTextureRect, needDrawHighlightBorder, animation, this.mTracking, this.mColorShadowFadeOutAnimation);
        if (needDrawHighlightBorder && this.mTracking) {
            canvas.save();
            if (animation != null) {
                canvas.setAlpha(animation.get());
            }
            drawRelativeLine(canvas, this.mTextureRect, this.mRotateDelegate.getPaintColor(), LINE_STEP);
            canvas.restore();
        }
    }

    public Rect computeRect(int bw, int bh, Rotation rotation) {
        if (rotation == Rotation.NINETY || rotation == Rotation.TWO_SEVENTY) {
            int temp = bw;
            bw = bh;
            bh = temp;
        }
        Rect rect = new Rect();
        RectComputer.computerRect(this, bw, bh, null, rect);
        return rect;
    }

    public void cancelAutoRotateAnimation() {
        if (this.mAutoRotateAnimation != null) {
            this.mAutoRotateAnimation.cancel();
        }
    }

    public void hide() {
        clearAnimation();
    }

    private void clearAnimation() {
        if (this.mAutoRotateAnimation != null) {
            this.mAutoRotateAnimation.removeAllListeners();
            this.mAutoRotateAnimation.cancel();
            this.mAutoRotateAnimation = null;
        }
        this.mLineFadeInAnimation.forceStop();
        this.mLineFadeOutAnimation.forceStop();
        this.mCurrentLineAnimation = null;
        this.mEditorRotationEffect.forceStop();
        this.mColorShadowFadeOutAnimation.forceStop();
        if (this.mResetAnimation != null) {
            this.mResetAnimation.forceStop();
            this.mResetAnimation = null;
        }
    }

    private void drawHighlightRectangle(GLCanvas canvas, int paintColor, Rect r, boolean needDrawHighlightBorder, FloatAnimation animation, boolean tracking, FloatAnimation colorShadowFadeOutAnimation) {
        int alpha;
        GL11 gl = canvas.getGLInstance();
        gl.glLineWidth(MapConfig.MIN_ZOOM);
        gl.glEnable(2848);
        gl.glEnable(2960);
        gl.glClear(1024);
        gl.glStencilOp(7680, 7680, 7681);
        gl.glStencilFunc(519, 1, 1);
        canvas.fillRect((float) r.left, (float) r.top, (float) r.width(), (float) r.height(), 0);
        if (needDrawHighlightBorder) {
            updateGLPaint(paintColor, MapConfig.MIN_ZOOM);
            canvas.drawRect((float) r.left, (float) r.top, (float) r.width(), (float) r.height(), this.mPaint);
        }
        gl.glStencilFunc(517, 1, 1);
        gl.glStencilOp(7680, 7680, 7681);
        if (tracking) {
            alpha = (int) ((-106.0f * (animation != null ? animation.get() : WMElement.CAMERASIZEVALUE1B1)) + 208.0f);
        } else if (animation == null || !animation.isActive()) {
            alpha = (int) (((WMElement.CAMERASIZEVALUE1B1 - colorShadowFadeOutAnimation.get()) * -47.0f) + 255.0f);
        } else {
            alpha = (int) ((106.0f * animation.get()) + 102.0f);
        }
        canvas.fillRect(0.0f, 0.0f, (float) this.mEditorViewDelegate.getWidth(), (float) this.mEditorViewDelegate.getHeight(), Color.argb(alpha, 0, 0, 0));
        gl.glDisable(2960);
    }

    private void drawRelativeLine(GLCanvas canvas, Rect bounds, int paintColor, int LINE_STEP) {
        int widthStepNum = computeStepNum(bounds.width(), LINE_STEP);
        float stepX = (((float) bounds.width()) * WMElement.CAMERASIZEVALUE1B1) / ((float) widthStepNum);
        int heightStepNum = computeStepNum(bounds.height(), LINE_STEP);
        float stepY = (((float) bounds.height()) * WMElement.CAMERASIZEVALUE1B1) / ((float) heightStepNum);
        float x = ((float) bounds.left) + stepX;
        float y = ((float) bounds.top) + stepY;
        updateGLPaint(paintColor, WMElement.CAMERASIZEVALUE1B1);
        for (int i = 0; i < widthStepNum - 1; i++) {
            canvas.drawLine(x, (float) bounds.top, x, (float) bounds.bottom, this.mPaint);
            x += stepX;
        }
        float y2 = y;
        for (int j = 0; j < heightStepNum - 1; j++) {
            canvas.drawLine((float) bounds.left, y2, (float) bounds.right, y2, this.mPaint);
            y2 += stepY;
        }
    }

    private int computeStepNum(int length, int LINE_STEP) {
        int stepNum = length / LINE_STEP;
        if (length % LINE_STEP > 0) {
            return stepNum + 1;
        }
        return stepNum;
    }
}
