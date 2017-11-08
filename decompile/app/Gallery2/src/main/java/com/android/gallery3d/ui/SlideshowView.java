package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.PointF;
import com.android.gallery3d.anim.CanvasAnimation;
import com.android.gallery3d.anim.FloatAnimation;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.Random;

public class SlideshowView extends GLView {
    protected SlideshowAnimation mCurrentAnimation;
    protected int mCurrentRotation;
    protected BitmapTexture mCurrentTexture;
    private HwCustSlideshowView mCust = ((HwCustSlideshowView) HwCustUtilsWrapper.createObj(HwCustSlideshowView.class, new Object[0]));
    protected SlideshowAnimation mPrevAnimation;
    protected int mPrevRotation;
    protected BitmapTexture mPrevTexture;
    private Random mRandom = new Random();
    protected final FloatAnimation mTransitionAnimation = new FloatAnimation(0.0f, WMElement.CAMERASIZEVALUE1B1, 1000);

    protected class SlideshowAnimation extends CanvasAnimation {
        private final int mHeight;
        private final PointF mMovingVector;
        private float mProgress;
        private final int mWidth;

        public SlideshowAnimation(int width, int height, Random random) {
            this.mWidth = width;
            this.mHeight = height;
            this.mMovingVector = new PointF((((float) this.mWidth) * 0.2f) * (random.nextFloat() - 0.5f), (((float) this.mHeight) * 0.2f) * (random.nextFloat() - 0.5f));
            if (SlideshowView.this.mCust == null || !SlideshowView.this.mCust.allowCustSlideShow()) {
                setDuration(3500);
            } else {
                setDuration(SlideshowView.this.mCust.getCurrentDuration(3500));
            }
        }

        public float getProgress() {
            return this.mProgress;
        }

        public void apply(GLCanvas canvas) {
            int viewWidth = SlideshowView.this.getWidth();
            int viewHeight = SlideshowView.this.getHeight();
            float scale = Math.min(((float) viewWidth) / ((float) this.mWidth), ((float) viewHeight) / ((float) this.mHeight)) * ((this.mProgress * 0.2f) + WMElement.CAMERASIZEVALUE1B1);
            canvas.translate(((float) (viewWidth / 2)) + (this.mMovingVector.x * this.mProgress), ((float) (viewHeight / 2)) + (this.mMovingVector.y * this.mProgress));
            canvas.scale(scale, scale, 0.0f);
        }

        protected void onCalculate(float progress) {
            this.mProgress = progress;
        }
    }

    protected Bitmap cropBitmap(Bitmap bitmap, int rotation) {
        return bitmap;
    }

    public void next(Bitmap bitmap, int rotation) {
        this.mTransitionAnimation.start();
        if (this.mPrevTexture != null) {
            this.mPrevTexture.getBitmap().recycle();
            this.mPrevTexture.recycle();
        }
        this.mPrevTexture = this.mCurrentTexture;
        this.mPrevAnimation = this.mCurrentAnimation;
        this.mPrevRotation = this.mCurrentRotation;
        this.mCurrentRotation = rotation;
        this.mCurrentTexture = new BitmapTexture(cropBitmap(bitmap, rotation));
        if (((rotation / 90) & 1) == 0) {
            this.mCurrentAnimation = new SlideshowAnimation(this.mCurrentTexture.getWidth(), this.mCurrentTexture.getHeight(), this.mRandom);
        } else {
            this.mCurrentAnimation = new SlideshowAnimation(this.mCurrentTexture.getHeight(), this.mCurrentTexture.getWidth(), this.mRandom);
        }
        this.mCurrentAnimation.start();
        invalidate();
    }

    public void release() {
        if (this.mPrevTexture != null) {
            this.mPrevTexture.recycle();
            this.mPrevTexture = null;
        }
        if (this.mCurrentTexture != null) {
            this.mCurrentTexture.recycle();
            this.mCurrentTexture = null;
        }
    }

    protected void render(GLCanvas canvas) {
        long animTime = AnimationTime.get();
        boolean requestRender = this.mTransitionAnimation.calculate(animTime);
        float alpha = this.mPrevTexture == null ? WMElement.CAMERASIZEVALUE1B1 : this.mTransitionAnimation.get();
        if (!(this.mPrevTexture == null || alpha == WMElement.CAMERASIZEVALUE1B1 || this.mPrevAnimation == null)) {
            requestRender |= this.mPrevAnimation.calculate(animTime);
            canvas.save(3);
            canvas.setAlpha(WMElement.CAMERASIZEVALUE1B1 - alpha);
            this.mPrevAnimation.apply(canvas);
            canvas.rotate((float) this.mPrevRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            this.mPrevTexture.draw(canvas, (-this.mPrevTexture.getWidth()) / 2, (-this.mPrevTexture.getHeight()) / 2);
            canvas.restore();
        }
        if (!(this.mCurrentTexture == null || this.mCurrentAnimation == null)) {
            requestRender |= this.mCurrentAnimation.calculate(animTime);
            canvas.save(3);
            canvas.setAlpha(alpha);
            this.mCurrentAnimation.apply(canvas);
            canvas.rotate((float) this.mCurrentRotation, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
            this.mCurrentTexture.draw(canvas, (-this.mCurrentTexture.getWidth()) / 2, (-this.mCurrentTexture.getHeight()) / 2);
            canvas.restore();
        }
        if (requestRender) {
            invalidate();
        }
    }
}
