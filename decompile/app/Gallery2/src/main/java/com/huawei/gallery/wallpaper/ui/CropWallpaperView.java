package com.huawei.gallery.wallpaper.ui;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import com.android.gallery3d.R;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLPaint;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.GestureRecognizer;
import com.android.gallery3d.ui.GestureRecognizer.Listener;
import com.android.gallery3d.ui.NinePatchTexture;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.watermark.manager.parse.WMElement;
import javax.microedition.khronos.opengles.GL11;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class CropWallpaperView extends GLView {
    private static final int BOTTOM_MASK_HEIGHT = GalleryUtils.dpToPixel((int) SmsCheckResult.ESCT_175);
    private static final MyPrinter PRT = new MyPrinter("CropWallpaperView");
    private static final int SCROLL_BOTTOM_MARGIN = GalleryUtils.dpToPixel(25);
    private static final int SCROLL_TOP_MARGIN = GalleryUtils.dpToPixel(54);
    private GalleryContext mActivity;
    private AnimationController mAnimation = new AnimationController();
    private NinePatchTexture mBottomMask;
    private Point mFixedWallpaperSize;
    private final MyGestureListener mGestureListener;
    private final GestureRecognizer mGestureRecognizer;
    private boolean mHasItems;
    private HighlightRectangle mHighlightRectangle;
    private int mImageHeight = -1;
    private float mImageScale = WMElement.CAMERASIZEVALUE1B1;
    private GLImageView mImageView;
    private int mImageWidth = -1;
    private GLPaint mPaint = new GLPaint();
    private Point mScrollWallpaperSize;
    private boolean mScrollable;
    private String mState;

    private class AnimationController extends Animation {
        private RectF mBound = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        private float mCurrentScale;
        private float mCurrentX;
        private float mCurrentY;
        private float mFocusX;
        private float mFocusY;
        private float mMinScale;
        private float mStartScale;
        private float mStartX;
        private float mStartY;
        private float mTargetScale;
        private float mTargetX;
        private float mTargetY;

        public AnimationController() {
            setInterpolator(new DecelerateInterpolator(4.0f));
            setDuration(300);
        }

        public void initialize() {
            float -get5 = ((float) CropWallpaperView.this.mImageWidth) / 2.0f;
            this.mCurrentX = -get5;
            this.mTargetX = -get5;
            -get5 = ((float) CropWallpaperView.this.mImageHeight) / 2.0f;
            this.mCurrentY = -get5;
            this.mTargetY = -get5;
            this.mMinScale = Math.max(((float) CropWallpaperView.this.mHighlightRectangle.getRectWidth()) / ((float) CropWallpaperView.this.mImageWidth), ((float) CropWallpaperView.this.mHighlightRectangle.getRectHeight()) / ((float) CropWallpaperView.this.mImageHeight));
            this.mCurrentScale = this.mMinScale;
            restoreState();
        }

        private void restoreState() {
            String state = CropWallpaperView.this.mState;
            if (state != null) {
                CropWallpaperView.PRT.d("restoreState");
                CropWallpaperView.this.mState = null;
                String[] param = state.split("x");
                if (param.length > 2) {
                    try {
                        float scale = Float.valueOf(param[0]).floatValue();
                        float currentX = Float.valueOf(param[1]).floatValue();
                        float currentY = Float.valueOf(param[2]).floatValue();
                        this.mCurrentX = currentX;
                        this.mStartX = currentX;
                        this.mCurrentY = currentY;
                        this.mStartY = currentY;
                        this.mCurrentScale = scale;
                        this.mStartScale = scale;
                        this.mTargetScale = Math.max(scale, this.mMinScale);
                        calculateStableBound(this.mTargetScale);
                        this.mTargetX = Utils.clamp(this.mCurrentX, this.mBound.left, this.mBound.right);
                        this.mTargetY = Utils.clamp(this.mCurrentY, this.mBound.top, this.mBound.bottom);
                        start();
                    } catch (NumberFormatException e) {
                        CropWallpaperView.PRT.w("restore state error.", e);
                    }
                    CropWallpaperView.this.invalidate();
                }
            }
        }

        protected void onCalculate(float progress) {
            this.mCurrentX = this.mStartX + ((this.mTargetX - this.mStartX) * progress);
            this.mCurrentY = this.mStartY + ((this.mTargetY - this.mStartY) * progress);
            this.mCurrentScale = this.mStartScale + ((this.mTargetScale - this.mStartScale) * progress);
            if (Utils.equal(this.mCurrentX, this.mTargetX) && Utils.equal(this.mCurrentY, this.mTargetY) && Utils.equal(this.mCurrentScale, this.mTargetScale)) {
                forceStop();
            }
            dumpAnimation("onCalculate");
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

        public void beginScale(float focusX, float focusY) {
            if (!isAnimating()) {
                this.mFocusX = this.mCurrentX + ((focusX - (((float) CropWallpaperView.this.getWidth()) / 2.0f)) / this.mCurrentScale);
                this.mFocusY = this.mCurrentY + ((focusY - (((float) CropWallpaperView.this.getHeight()) / 2.0f)) / this.mCurrentScale);
            }
        }

        public int scaleBy(float focusX, float focusY, float scale) {
            scale *= this.mCurrentScale;
            float currentX = this.mFocusX - ((focusX - (((float) CropWallpaperView.this.getWidth()) / 2.0f)) / scale);
            float currentY = this.mFocusY - ((focusY - (((float) CropWallpaperView.this.getHeight()) / 2.0f)) / scale);
            calculateStableBound(scale);
            currentX = Utils.clamp(currentX, this.mBound.left, this.mBound.right);
            currentY = Utils.clamp(currentY, this.mBound.top, this.mBound.bottom);
            this.mTargetX = currentX;
            this.mCurrentX = currentX;
            this.mStartX = currentX;
            this.mTargetY = currentY;
            this.mCurrentY = currentY;
            this.mStartY = currentY;
            this.mCurrentScale = scale;
            this.mTargetScale = scale;
            this.mStartScale = scale;
            CropWallpaperView.this.invalidate();
            return 0;
        }

        public void endScale() {
            if (!isAnimating() && this.mTargetScale < this.mMinScale) {
                this.mTargetScale = this.mMinScale;
                calculateStableBound(this.mTargetScale);
                this.mTargetX = Utils.clamp(this.mCurrentX, this.mBound.left, this.mBound.right);
                this.mTargetY = Utils.clamp(this.mCurrentY, this.mBound.top, this.mBound.bottom);
                start();
                CropWallpaperView.this.invalidate();
            }
        }

        public void scrollBy(int dxi, int dyi) {
            if (!isAnimating()) {
                float currentX = this.mCurrentX + (((float) dxi) / this.mCurrentScale);
                float currentY = this.mCurrentY + (((float) dyi) / this.mCurrentScale);
                calculateStableBound(this.mCurrentScale);
                currentX = Utils.clamp(currentX, this.mBound.left, this.mBound.right);
                currentY = Utils.clamp(currentY, this.mBound.top, this.mBound.bottom);
                this.mTargetX = currentX;
                this.mCurrentX = currentX;
                this.mStartX = currentX;
                this.mTargetY = currentY;
                this.mCurrentY = currentY;
                this.mStartY = currentY;
                float f = this.mCurrentScale;
                this.mTargetScale = f;
                this.mStartScale = f;
                CropWallpaperView.this.invalidate();
            }
        }

        private void calculateStableBound(float scale) {
            int w = CropWallpaperView.this.mHighlightRectangle.getRectWidth();
            float viewWidth = ((float) w) / scale;
            float viewHeight = ((float) CropWallpaperView.this.mHighlightRectangle.getRectHeight()) / scale;
            this.mBound.left = viewWidth / 2.0f;
            this.mBound.right = ((float) CropWallpaperView.this.mImageWidth) - (viewWidth / 2.0f);
            this.mBound.top = viewHeight / 2.0f;
            this.mBound.bottom = ((float) CropWallpaperView.this.mImageHeight) - (viewHeight / 2.0f);
            if (((float) CropWallpaperView.this.mImageWidth) <= viewWidth) {
                RectF rectF = this.mBound;
                float -get5 = ((float) CropWallpaperView.this.mImageWidth) / 2.0f;
                this.mBound.right = -get5;
                rectF.left = -get5;
            }
            if (((float) CropWallpaperView.this.mImageHeight) <= viewHeight) {
                rectF = this.mBound;
                -get5 = ((float) CropWallpaperView.this.mImageHeight) / 2.0f;
                this.mBound.bottom = -get5;
                rectF.top = -get5;
            }
        }

        public Rect mapRect() {
            int halfW = scaleToOrigin(CropWallpaperView.this.mHighlightRectangle.getRectWidth()) / 2;
            int halfH = scaleToOrigin(CropWallpaperView.this.mHighlightRectangle.getRectHeight()) / 2;
            CropWallpaperView.PRT.d("[mapRect] halfW->" + halfW + ", halfH->" + halfH);
            CropWallpaperView.PRT.d("[mapRect] mImageWidth->" + CropWallpaperView.this.mImageWidth + ", mImageHeight->" + CropWallpaperView.this.mImageHeight);
            int centerX = (int) this.mCurrentX;
            int centerY = (int) this.mCurrentY;
            return new Rect(centerX - halfW, centerY - halfH, centerX + halfW, centerY + halfH);
        }

        private int scaleToOrigin(int side) {
            return (int) (((double) (((float) side) / this.mCurrentScale)) + 0.5d);
        }

        private void dumpAnimation(String method) {
            CropWallpaperView.PRT.d(String.format("[%s] mStartX = %s , mStartY = %s, mCurrentX = %s, mCurrentY = %s, mTargetX = %s, mTargetY = %s", new Object[]{method, Float.valueOf(this.mStartX), Float.valueOf(this.mStartY), Float.valueOf(this.mCurrentX), Float.valueOf(this.mCurrentY), Float.valueOf(this.mTargetX), Float.valueOf(this.mTargetY)}));
        }
    }

    private class HighlightRectangle extends GLView {
        private int mRectH;
        private int mRectW;

        private HighlightRectangle() {
        }

        public int getRectWidth() {
            return this.mRectW;
        }

        public int getRectHeight() {
            return this.mRectH;
        }

        protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
            super.onLayout(changeSize, left, top, right, bottom);
            int width = getWidth();
            int height = getHeight();
            Point size = CropWallpaperView.this.mScrollable ? CropWallpaperView.this.mScrollWallpaperSize : CropWallpaperView.this.mFixedWallpaperSize;
            float scaleX = ((float) width) / ((float) size.x);
            float scaleY = ((float) height) / ((float) size.y);
            if (scaleY > scaleX) {
                this.mRectW = width;
                this.mRectH = (int) (((double) (((float) size.y) * scaleX)) + 0.5d);
                return;
            }
            this.mRectW = (int) (((double) (((float) size.x) * scaleY)) + 0.5d);
            this.mRectH = height;
        }

        protected void renderBackground(GLCanvas canvas) {
            GL11 gl = canvas.getGLInstance();
            gl.glLineWidth(MapConfig.MIN_ZOOM);
            gl.glEnable(2848);
            gl.glEnable(2960);
            gl.glClear(1024);
            gl.glStencilOp(7680, 7680, 7681);
            gl.glStencilFunc(519, 1, 1);
            canvas.fillRect((float) ((getWidth() - this.mRectW) / 2), (float) ((getHeight() - this.mRectH) / 2), (float) this.mRectW, (float) this.mRectH, 0);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7681);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), -1275068416);
            gl.glStencilFunc(517, 1, 1);
            gl.glStencilOp(7680, 7680, 7680);
            canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), 0);
            gl.glDisable(2960);
        }
    }

    private class MyGestureListener implements Listener {
        private MyGestureListener() {
        }

        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        public boolean onFling(float velocityX, float velocityY) {
            return true;
        }

        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            CropWallpaperView.this.mAnimation.scrollBy((int) (dx + 0.5f), (int) (dy + 0.5f));
            return true;
        }

        public boolean onScaleBegin(float focusX, float focusY) {
            CropWallpaperView.this.mAnimation.beginScale(focusX, focusY);
            return true;
        }

        public void onScaleEnd() {
            CropWallpaperView.this.mAnimation.endScale();
        }

        public boolean onScale(float focusX, float focusY, float scale) {
            CropWallpaperView.this.mAnimation.scaleBy(focusX, focusY, scale);
            return true;
        }

        public void onLongPress(MotionEvent e) {
        }

        public void onDown(float x, float y) {
        }

        public void onUp() {
        }
    }

    public CropWallpaperView(GalleryContext activity) {
        this.mActivity = activity;
        this.mImageView = new GLImageView(activity);
        this.mHighlightRectangle = new HighlightRectangle();
        this.mHighlightRectangle.setVisibility(0);
        addComponent(this.mImageView);
        addComponent(this.mHighlightRectangle);
        this.mPaint.setColor(this.mActivity.getAndroidContext().getResources().getColor(R.color.crop_bolder_selected));
        this.mPaint.setLineWidth(MapConfig.MIN_ZOOM);
        this.mBottomMask = new NinePatchTexture(activity.getActivityContext(), R.drawable.shadow_wallpaper_bottom);
        this.mGestureListener = new MyGestureListener();
        this.mGestureRecognizer = new GestureRecognizer(activity.getActivityContext(), this.mGestureListener);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;
        int top = 0;
        int naviH = LayoutHelper.getNavigationBarHeight();
        if (this.mScrollable) {
            top = SCROLL_TOP_MARGIN;
            height = (height - naviH) - SCROLL_TOP_MARGIN;
            if (this.mHasItems) {
                height -= SCROLL_BOTTOM_MARGIN;
            }
        }
        this.mImageView.layout(0, top, width, height);
        this.mHighlightRectangle.layout(0, top, width, height);
        if (this.mImageHeight != -1) {
            this.mAnimation.initialize();
        }
    }

    public void render(GLCanvas canvas) {
        AnimationController a = this.mAnimation;
        if (a.calculate(AnimationTime.get())) {
            PRT.d("invalidate with animation");
            invalidate();
        }
        this.mImageView.setPosition(a.getCenterX(), a.getCenterY(), a.getScale());
        super.render(canvas);
        int w = getWidth();
        int h = getHeight();
        int height = LayoutHelper.getNavigationBarHeight() + BOTTOM_MASK_HEIGHT;
        if (!this.mHasItems) {
            height -= SCROLL_BOTTOM_MARGIN;
        }
        canvas.drawTexture(this.mBottomMask, 0, h - height, w, height);
    }

    public void renderBackground(GLCanvas canvas) {
        super.renderBackground(canvas);
    }

    public Rect getCropRectangle() {
        return this.mAnimation.mapRect();
    }

    public int getImageWidth() {
        return this.mImageWidth;
    }

    public int getImageHeight() {
        return this.mImageHeight;
    }

    protected boolean onTouch(MotionEvent event) {
        this.mGestureRecognizer.onTouchEvent(event);
        return true;
    }

    public void setBlurFactor(int blurFactor) {
        this.mImageView.setBlurFactor(blurFactor);
    }

    public int getBlurRadius(int factor) {
        return this.mImageView.getBlurRadius(factor);
    }

    public void setDataModel(Bitmap data, int rotation) {
        if (data != null) {
            data = BitmapUtils.rotateBitmap(data, rotation, false);
            this.mImageWidth = data.getWidth();
            this.mImageHeight = data.getHeight();
            this.mImageView.setData(data);
        } else {
            this.mImageWidth = 0;
            this.mImageHeight = 0;
            this.mImageView.setData(null);
        }
        invalidate();
        this.mAnimation.initialize();
    }

    public void setFixedWallpaperSize(int w, int h) {
        this.mFixedWallpaperSize = new Point(w, h);
    }

    public void setScrollWallpaperSize(int w, int h) {
        this.mScrollWallpaperSize = new Point(w, h);
    }

    public void setState(String state) {
        this.mState = state;
        requestLayout();
        PRT.d("setState: " + state);
    }

    public String getState() {
        return String.format("%sx%sx%s", new Object[]{Float.valueOf(this.mAnimation.mCurrentScale), Float.valueOf(this.mAnimation.mCurrentX), Float.valueOf(this.mAnimation.mCurrentY)});
    }

    public void setScrollableWallper(boolean scrollable) {
        this.mScrollable = scrollable;
        this.mAnimation.initialize();
        requestLayout();
    }

    public void setHasItems(boolean hasItems) {
        this.mHasItems = hasItems;
        requestLayout();
    }
}
