package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.FloatAnimation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.ui.stackblur.StackBlurUtils;
import com.huawei.watermark.manager.parse.WMElement;
import javax.microedition.khronos.opengles.GL11;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoMagnifierView extends GLView {
    public static final int MAGNIFIER_CENTER_TO_TOUCH_POINT = GalleryUtils.dpToPixel(127);
    private static final int MAGNIFIER_FRAME_PADDING = GalleryUtils.dpToPixel(11);
    public static final int MAGNIFIER_HEIGHT = GalleryUtils.dpToPixel((int) SmsCheckResult.ESCT_192);
    public static final int MAGNIFIER_SQUARE_WIDTH = GalleryUtils.dpToPixel(102);
    public static final int MAGNIFIER_WIDTH = GalleryUtils.dpToPixel((int) SmsCheckResult.ESCT_192);
    private Rect[] mActiveRange = new Rect[]{new Rect(), new Rect()};
    private float mAnimationProgress;
    private BitmapTexture mBitmapTexture;
    private GalleryContext mContext;
    private Rect mFramePadding;
    private NinePatchTexture mFramePhoto;
    private PhotoMagnifierListener mListener;
    private MagnifierStyle mMagnifierStyle;
    private NinePatchTexture mMask;
    private Point mOffset;
    private RectF mPhotoRange;
    private boolean mRequestRender;
    private float mScale;
    private Rect mScaledPadding;
    private Rect mTileRange;
    private PointF mTouchPoint;
    private FloatAnimation mTransitionAnimation;

    public interface PhotoMagnifierListener {
        void drawPhotoMagnifier(float f, float f2);

        float getScaleForAnimation(float f);

        TileImageView getTileImageView();

        void onMagnifierAnimationEnd();
    }

    public enum MagnifierStyle {
        SQUARE(0),
        CIRCLE(1);
        
        private int mStyle;

        private MagnifierStyle(int style) {
            this.mStyle = style;
        }

        public int getStyle() {
            return this.mStyle;
        }

        public boolean equal(MagnifierStyle other) {
            return this.mStyle == other.getStyle();
        }
    }

    public PhotoMagnifierView(GalleryContext activity) {
        this.mContext = activity;
        setVisibility(1);
        initData();
        initRes();
    }

    public void setAnimation(FloatAnimation anim) {
        this.mTransitionAnimation = anim;
    }

    public void setPhotoRange(RectF range) {
        this.mPhotoRange.set(range);
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public float getScale() {
        return this.mScale;
    }

    public void draw(float x, float y) {
        FloatAnimation animation = this.mTransitionAnimation;
        if (this.mListener != null) {
            this.mRequestRender = false;
            if (animation != null && animation.isActive()) {
                AnimationTime.update();
                this.mRequestRender = animation.calculate(AnimationTime.get());
                if (this.mRequestRender) {
                    this.mAnimationProgress = animation.get();
                    float scale = this.mListener.getScaleForAnimation(this.mAnimationProgress);
                    if (Float.compare(scale, 0.0f) < 0) {
                        scale = this.mScale;
                    }
                    setScale(scale);
                } else {
                    this.mAnimationProgress = animation.get();
                    this.mListener.onMagnifierAnimationEnd();
                    return;
                }
            }
            this.mListener.drawPhotoMagnifier(x, y);
            this.mTouchPoint.set(x, y);
        }
    }

    public void setPhotoPosition() {
        TileImageView tileImageView = null;
        if (this.mListener != null) {
            tileImageView = this.mListener.getTileImageView();
        }
        if (tileImageView != null) {
            tileImageView.layoutMagnifierTiles(this.mPhotoRange, this.mScale, this.mTileRange, this.mActiveRange, this.mOffset);
        }
    }

    public void setPhotoMagnifierListener(PhotoMagnifierListener listener) {
        this.mListener = listener;
    }

    public int getPadding() {
        if (this.mMagnifierStyle.equal(MagnifierStyle.CIRCLE)) {
            return GalleryUtils.dpToPixel(1);
        }
        return 0;
    }

    public int getGapBetweenMagnifierAndTouchPoint() {
        return (int) (((float) MAGNIFIER_CENTER_TO_TOUCH_POINT) * this.mAnimationProgress);
    }

    public void setBitmap(Bitmap bitmap) {
        setBitmap(bitmap, 80);
    }

    public void setBitmap(Bitmap bitmap, int radius) {
        if (bitmap != null && !bitmap.isRecycled()) {
            if (this.mBitmapTexture != null) {
                this.mBitmapTexture.getBitmap().recycle();
                this.mBitmapTexture = null;
            }
            Bitmap blurredBitmap = StackBlurUtils.getBlurBitmap(this.mContext.getAndroidContext(), bitmap, radius);
            if (blurredBitmap != null) {
                this.mBitmapTexture = new BitmapTexture(blurredBitmap);
            } else {
                GalleryLog.w("PhotoMagnifierView", "blurredBitmap is null");
            }
        }
    }

    protected void render(GLCanvas canvas) {
        super.render(canvas);
        TileImageView tileImageView = null;
        if (this.mListener != null) {
            tileImageView = this.mListener.getTileImageView();
        }
        if (tileImageView != null) {
            float progress = this.mAnimationProgress;
            canvas.save();
            GL11 gl = canvas.getGLInstance();
            gl.glEnable(2960);
            gl.glClear(1024);
            gl.glStencilFunc(512, 1, 1);
            gl.glStencilOp(7681, 7680, 7680);
            gl.glEnable(3008);
            gl.glAlphaFunc(516, 0.0f);
            if (!this.mMagnifierStyle.equal(MagnifierStyle.CIRCLE) || this.mMask == null) {
                canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), -16777216);
            } else {
                this.mScaledPadding.set(this.mMask.getPaddings());
                this.mScaledPadding.scale(progress);
                drawNinePatchTexture(canvas, this.mScaledPadding, this.mMask, (int) ((((float) getWidth()) / 2.0f) * (WMElement.CAMERASIZEVALUE1B1 - progress)), (int) ((((float) getHeight()) / 2.0f) * (WMElement.CAMERASIZEVALUE1B1 - progress)), (int) (((float) getWidth()) * progress), (int) (((float) getHeight()) * progress));
            }
            gl.glDisable(3008);
            gl.glStencilFunc(514, 1, 1);
            gl.glStencilOp(7680, 7680, 7680);
            if (this.mBitmapTexture != null) {
                this.mBitmapTexture.draw(canvas, 0, 0, getWidth(), getHeight());
            }
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), Color.argb(179, 0, 0, 0));
            tileImageView.drawPhotoInMagnifier(canvas, this.mTileRange, this.mOffset, this.mScale);
            gl.glDisable(2960);
            this.mScaledPadding.set(this.mFramePadding);
            this.mScaledPadding.scale(progress);
            drawNinePatchTexture(canvas, this.mScaledPadding, this.mFramePhoto, (int) ((((float) getWidth()) / 2.0f) * (WMElement.CAMERASIZEVALUE1B1 - progress)), (int) ((((float) getHeight()) / 2.0f) * (WMElement.CAMERASIZEVALUE1B1 - progress)), (int) (((float) getWidth()) * progress), (int) (((float) getHeight()) * progress));
            canvas.restore();
            if (this.mRequestRender) {
                draw(this.mTouchPoint.x, this.mTouchPoint.y);
            }
        }
    }

    private void drawNinePatchTexture(GLCanvas canvas, Rect padding, Texture texture, int x, int y, int width, int height) {
        if (texture != null) {
            texture.draw(canvas, x - padding.left, y - padding.top, (padding.left + width) + padding.right, (padding.top + height) + padding.bottom);
        }
    }

    private void initData() {
        this.mScale = WMElement.CAMERASIZEVALUE1B1;
        this.mAnimationProgress = WMElement.CAMERASIZEVALUE1B1;
        this.mTransitionAnimation = null;
        this.mMagnifierStyle = MagnifierStyle.CIRCLE;
        this.mPhotoRange = new RectF();
        this.mFramePadding = new Rect();
        this.mTileRange = new Rect();
        this.mOffset = new Point();
        this.mTouchPoint = new PointF();
        this.mScaledPadding = new Rect();
    }

    private void initRes() {
        int frameResId;
        int maskResId;
        if (this.mMagnifierStyle.equal(MagnifierStyle.CIRCLE)) {
            frameResId = R.drawable.mask_shadow;
            maskResId = R.drawable.mask_circle;
            this.mFramePadding.set(MAGNIFIER_FRAME_PADDING, MAGNIFIER_FRAME_PADDING, MAGNIFIER_FRAME_PADDING, MAGNIFIER_FRAME_PADDING);
        } else {
            frameResId = R.drawable.picture_frame;
            maskResId = 0;
        }
        this.mFramePhoto = new NinePatchTexture(this.mContext.getActivityContext(), frameResId);
        if (maskResId != 0) {
            this.mMask = new NinePatchTexture(this.mContext.getActivityContext(), maskResId);
        } else {
            this.mMask = null;
        }
    }
}
