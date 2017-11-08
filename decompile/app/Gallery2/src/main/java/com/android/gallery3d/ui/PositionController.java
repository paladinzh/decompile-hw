package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.RangeArray;
import com.android.gallery3d.util.RangeIntArray;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.watermark.manager.parse.WMElement;

public class PositionController {
    private static final int[] ANIM_TIME = new int[]{0, 0, 600, AbsAlbumPage.LAUNCH_QUIK_ACTIVITY, 300, 300, 0, 0, 0, 700, 700};
    private static final int[] CENTER_OUT_INDEX = new int[7];
    private static final int FILM_IMAGE_GAP = GalleryUtils.dpToPixel(16);
    private static final int HORIZONTAL_SLACK = GalleryUtils.dpToPixel(0);
    private static final int IMAGE_GAP = GalleryUtils.dpToPixel(6);
    public static final int RUBBER_BAND_RANGE = (GalleryUtils.getWidthPixels() / 4);
    private static final Interpolator sSnapBackInterpolator = new CubicBezierInterpolator(0.2f, 0.65f, 0.28f, 0.97f);
    private int mBoundBottom;
    private int mBoundLeft;
    private int mBoundRight;
    private int mBoundTop;
    private RangeArray<Box> mBoxes = new RangeArray(-3, 3);
    private boolean mConstrained = true;
    private Rect mConstrainedFrame = new Rect();
    private boolean mExtraScalingRange = false;
    private boolean mFilmMode = false;
    private FilmRatio mFilmRatio = new FilmRatio();
    private Scroller mFilmScroller;
    private float mFocusX;
    private float mFocusY;
    private RangeArray<Gap> mGaps = new RangeArray(-3, 2);
    private boolean mHasNext;
    private boolean mHasPrev;
    private boolean mInScale;
    private Listener mListener;
    private FlingScroller mPageScroller;
    private Platform mPlatform = new Platform();
    boolean mPopFromTop;
    private RangeArray<Rect> mRects = new RangeArray(-3, 3);
    private RangeArray<Rect> mTargetRects = new RangeArray(-3, 3);
    private RangeArray<Box> mTempBoxes = new RangeArray(-3, 3);
    private RangeArray<Gap> mTempGaps = new RangeArray(-3, 2);
    private int mViewH = 1200;
    private int mViewW = 1200;

    public interface Listener {
        void invalidate();

        boolean isHoldingDelete();

        boolean isHoldingDown();

        int needRubberBandEffectEdge();

        void onEdgeRubberBand(int i);
    }

    private static abstract class Animatable {
        public int mAnimationDuration;
        public int mAnimationKind;
        public long mAnimationStartTime;

        protected abstract boolean interpolate(float f);

        public abstract boolean startSnapback();

        private Animatable() {
        }

        public boolean advanceAnimation() {
            if (this.mAnimationStartTime == -1) {
                return false;
            }
            if (this.mAnimationStartTime == -2) {
                this.mAnimationStartTime = -1;
                return startSnapback();
            }
            float progress;
            if (this.mAnimationDuration == 0) {
                progress = WMElement.CAMERASIZEVALUE1B1;
            } else {
                long now = AnimationTime.get();
                if (this.mAnimationKind != 10) {
                    progress = ((float) (now - this.mAnimationStartTime)) / ((float) this.mAnimationDuration);
                } else if (now - this.mAnimationStartTime <= 400) {
                    progress = 0.0f;
                } else {
                    progress = ((float) ((now - this.mAnimationStartTime) - 400)) / ((float) (this.mAnimationDuration - 400));
                }
            }
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                progress = WMElement.CAMERASIZEVALUE1B1;
            } else {
                progress = applyInterpolationCurve(this.mAnimationKind, progress);
            }
            if (interpolate(progress)) {
                this.mAnimationStartTime = -2;
            }
            return true;
        }

        private static float applyInterpolationCurve(int kind, float progress) {
            float f = WMElement.CAMERASIZEVALUE1B1 - progress;
            switch (kind) {
                case 0:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    return WMElement.CAMERASIZEVALUE1B1 - f;
                case 1:
                case 5:
                    return WMElement.CAMERASIZEVALUE1B1 - (f * f);
                case 2:
                    return PositionController.sSnapBackInterpolator.getInterpolation(progress);
                case 3:
                    return WMElement.CAMERASIZEVALUE1B1 - ((f * f) * f);
                case 4:
                    return WMElement.CAMERASIZEVALUE1B1 - ((((f * f) * f) * f) * f);
                default:
                    return progress;
            }
        }
    }

    private class Box extends Animatable {
        public int mAbsoluteX;
        public float mCurrentScale;
        public int mCurrentY;
        public float mFromScale;
        public int mFromY;
        public int mImageH;
        public int mImageW;
        public float mScaleCustom;
        public float mScaleFillLong;
        public float mScaleFillShort;
        public float mScaleMax;
        public float mScaleMin;
        public float mToScale;
        public int mToY;
        public boolean mUseViewSize;

        private Box() {
            super();
        }

        public boolean startSnapback() {
            if (this.mAnimationStartTime != -1) {
                return false;
            }
            if (this.mAnimationKind == 0 && PositionController.this.mListener.isHoldingDown()) {
                return false;
            }
            if (this.mAnimationKind == 8 && PositionController.this.mListener.isHoldingDelete()) {
                return false;
            }
            if (PositionController.this.mInScale && this == PositionController.this.mBoxes.get(0)) {
                return false;
            }
            float scale;
            int y = this.mCurrentY;
            if (this == PositionController.this.mBoxes.get(0)) {
                scale = Utils.clamp(this.mCurrentScale, PositionController.this.mExtraScalingRange ? this.mScaleMin * 0.7f : this.mScaleMin, PositionController.this.mExtraScalingRange ? this.mScaleMax * 2.8f : this.mScaleMax);
                if (PositionController.this.mFilmMode) {
                    y = 0;
                } else {
                    PositionController.this.calculateStableBound(scale, PositionController.HORIZONTAL_SLACK);
                    if (!PositionController.this.viewTallerThanScaledImage(scale)) {
                        y += (int) ((PositionController.this.mFocusY * (this.mCurrentScale - scale)) + 0.5f);
                    }
                    y = Utils.clamp(y, PositionController.this.mBoundTop, PositionController.this.mBoundBottom);
                }
            } else {
                y = 0;
                scale = this.mScaleFillShort;
            }
            if (this.mCurrentY == y && this.mCurrentScale == scale) {
                return false;
            }
            PositionController.ANIM_TIME[2] = this == PositionController.this.mBoxes.get(0) ? PositionController.this.getSnapBackDuration(this.mCurrentY, y) : 600;
            return doAnimation(y, scale, 2);
        }

        private boolean doAnimation(int targetY, float targetScale, int kind) {
            targetScale = clampScale(targetScale);
            if (this.mCurrentY == targetY && this.mCurrentScale == targetScale && kind != 9) {
                return false;
            }
            this.mAnimationKind = kind;
            this.mFromY = this.mCurrentY;
            this.mFromScale = this.mCurrentScale;
            this.mToY = targetY;
            this.mToScale = targetScale;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = PositionController.ANIM_TIME[kind];
            advanceAnimation();
            return true;
        }

        public float clampScale(float s) {
            return Utils.clamp(s, this.mScaleMin * 0.7f, this.mScaleMax * 2.8f);
        }

        protected boolean interpolate(float progress) {
            if (this.mAnimationKind == 6) {
                return interpolateFlingPage(progress);
            }
            return interpolateLinear(progress);
        }

        private boolean interpolateFlingPage(float progress) {
            PositionController.this.mPageScroller.computeScrollOffset(progress);
            PositionController.this.calculateStableBound(this.mCurrentScale);
            this.mCurrentY = PositionController.this.mPageScroller.getCurrY();
            if (this.mCurrentY - this.mToY == 0 || progress >= WMElement.CAMERASIZEVALUE1B1) {
                return true;
            }
            return false;
        }

        private boolean interpolateLinear(float progress) {
            boolean z = true;
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                this.mCurrentY = this.mToY;
                this.mCurrentScale = this.mToScale;
                return true;
            }
            this.mCurrentY = (int) (((float) this.mFromY) + (((float) (this.mToY - this.mFromY)) * progress));
            this.mCurrentScale = this.mFromScale + ((this.mToScale - this.mFromScale) * progress);
            if (this.mAnimationKind == 9) {
                this.mCurrentScale *= CaptureAnimation.calculateScale(progress);
                return false;
            }
            if (!(this.mCurrentY == this.mToY && this.mCurrentScale == this.mToScale)) {
                z = false;
            }
            return z;
        }
    }

    private class FilmRatio extends Animatable {
        public float mCurrentRatio;
        public float mFromRatio;
        public float mToRatio;

        private FilmRatio() {
            super();
        }

        public boolean startSnapback() {
            float target = PositionController.this.mFilmMode ? WMElement.CAMERASIZEVALUE1B1 : 0.0f;
            if (target == this.mToRatio) {
                return false;
            }
            PositionController.ANIM_TIME[2] = 600;
            return doAnimation(target, 2);
        }

        protected boolean interpolate(float progress) {
            boolean z = true;
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                this.mCurrentRatio = this.mToRatio;
                return true;
            }
            this.mCurrentRatio = this.mFromRatio + ((this.mToRatio - this.mFromRatio) * progress);
            if (this.mCurrentRatio != this.mToRatio) {
                z = false;
            }
            return z;
        }

        private boolean doAnimation(float targetRatio, int kind) {
            this.mAnimationKind = kind;
            this.mFromRatio = this.mCurrentRatio;
            this.mToRatio = targetRatio;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = PositionController.ANIM_TIME[this.mAnimationKind];
            advanceAnimation();
            return true;
        }
    }

    private class Gap extends Animatable {
        public int mCurrentGap;
        public int mDefaultSize;
        public int mFromGap;
        public int mToGap;

        private Gap() {
            super();
        }

        public boolean startSnapback() {
            if (this.mAnimationStartTime != -1) {
                return false;
            }
            int targetGap = this.mDefaultSize;
            if (PositionController.this.mGaps.get(-1) == this) {
                targetGap = PositionController.this.getTargetGap(0, PositionController.this.getTargetScale((Box) PositionController.this.mBoxes.get(0)), -1);
            } else if (PositionController.this.mGaps.get(0) == this) {
                targetGap = PositionController.this.getTargetGap(0, PositionController.this.getTargetScale((Box) PositionController.this.mBoxes.get(0)), 1);
            }
            PositionController.ANIM_TIME[2] = PositionController.this.getSnapBackDuration(this.mCurrentGap, targetGap);
            return doAnimation(targetGap, 2);
        }

        public boolean doAnimation(int targetSize, int kind) {
            if (this.mCurrentGap == targetSize && kind != 9) {
                return false;
            }
            this.mAnimationKind = kind;
            this.mFromGap = this.mCurrentGap;
            this.mToGap = targetSize;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = PositionController.ANIM_TIME[this.mAnimationKind];
            advanceAnimation();
            return true;
        }

        protected boolean interpolate(float progress) {
            boolean z = true;
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                this.mCurrentGap = this.mToGap;
                return true;
            }
            this.mCurrentGap = (int) (((float) this.mFromGap) + (((float) (this.mToGap - this.mFromGap)) * progress));
            if (this.mAnimationKind == 9) {
                this.mCurrentGap = (int) (((float) this.mCurrentGap) * CaptureAnimation.calculateScale(progress));
                return false;
            }
            if (this.mCurrentGap != this.mToGap) {
                z = false;
            }
            return z;
        }
    }

    private class Platform extends Animatable {
        public int mCurrentX;
        public int mCurrentY;
        public int mDefaultX;
        public int mDefaultY;
        public int mFlingOffset;
        public int mFromX;
        public int mFromY;
        public int mToX;
        public int mToY;

        private Platform() {
            super();
        }

        public boolean startSnapback() {
            if (this.mAnimationStartTime != -1) {
                return false;
            }
            if ((this.mAnimationKind == 0 && PositionController.this.mListener.isHoldingDown()) || PositionController.this.mInScale) {
                return false;
            }
            Box b = (Box) PositionController.this.mBoxes.get(0);
            float scale = Utils.clamp(b.mCurrentScale, PositionController.this.mExtraScalingRange ? b.mScaleMin * 0.7f : b.mScaleMin, PositionController.this.mExtraScalingRange ? b.mScaleMax * 2.8f : b.mScaleMax);
            int x = this.mCurrentX;
            int y = this.mDefaultY;
            if (PositionController.this.mFilmMode) {
                x = this.mDefaultX;
            } else {
                PositionController.this.calculateStableBound(scale, PositionController.HORIZONTAL_SLACK);
                if (!PositionController.this.viewWiderThanScaledImage(scale)) {
                    x += (int) ((PositionController.this.mFocusX * (b.mCurrentScale - scale)) + 0.5f);
                }
                x = Utils.clamp(x, PositionController.this.mBoundLeft, PositionController.this.mBoundRight);
            }
            if (this.mCurrentX == x && this.mCurrentY == y) {
                return false;
            }
            PositionController.ANIM_TIME[2] = Math.max(PositionController.this.getSnapBackDuration(this.mCurrentX, x), PositionController.this.getSnapBackDuration(this.mCurrentY, y));
            return doAnimation(x, y, 2);
        }

        public void updateDefaultXY() {
            int i = 0;
            if (!PositionController.this.mConstrained || PositionController.this.mConstrainedFrame.isEmpty()) {
                this.mDefaultX = 0;
                this.mDefaultY = 0;
                return;
            }
            this.mDefaultX = PositionController.this.mConstrainedFrame.centerX() - (PositionController.this.mViewW / 2);
            if (!PositionController.this.mFilmMode) {
                i = PositionController.this.mConstrainedFrame.centerY() - (PositionController.this.mViewH / 2);
            }
            this.mDefaultY = i;
        }

        private boolean doAnimation(int targetX, int targetY, int kind) {
            if (this.mCurrentX == targetX && this.mCurrentY == targetY) {
                return false;
            }
            this.mAnimationKind = kind;
            this.mFromX = this.mCurrentX;
            this.mFromY = this.mCurrentY;
            this.mToX = targetX;
            this.mToY = targetY;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = PositionController.ANIM_TIME[kind];
            this.mFlingOffset = 0;
            advanceAnimation();
            return true;
        }

        protected boolean interpolate(float progress) {
            if (this.mAnimationKind == 6) {
                return interpolateFlingPage(progress);
            }
            if (this.mAnimationKind == 7) {
                return interpolateFlingFilm(progress);
            }
            return interpolateLinear(progress);
        }

        private boolean interpolateFlingFilm(float progress) {
            PositionController.this.mFilmScroller.computeScrollOffset();
            this.mCurrentX = PositionController.this.mFilmScroller.getCurrX() + this.mFlingOffset;
            int dir = 0;
            if (this.mCurrentX < this.mDefaultX - PositionController.RUBBER_BAND_RANGE) {
                if (!PositionController.this.mHasNext) {
                    dir = 8;
                }
            } else if (this.mCurrentX > this.mDefaultX + PositionController.RUBBER_BAND_RANGE && !PositionController.this.mHasPrev) {
                dir = 2;
            }
            if (dir != 0) {
                PositionController.this.mFilmScroller.forceFinished(true);
            }
            return PositionController.this.mFilmScroller.isFinished();
        }

        private boolean interpolateFlingPage(float progress) {
            PositionController.this.mPageScroller.computeScrollOffset(progress);
            PositionController.this.calculateStableBound(((Box) PositionController.this.mBoxes.get(0)).mCurrentScale);
            int oldX = this.mCurrentX;
            this.mCurrentX = PositionController.this.mPageScroller.getCurrX();
            if (oldX > PositionController.this.mBoundLeft && this.mCurrentX <= PositionController.this.mBoundLeft) {
                PositionController.this.mListener.onEdgeRubberBand(8);
            } else if (oldX < PositionController.this.mBoundRight && this.mCurrentX >= PositionController.this.mBoundRight) {
                PositionController.this.mListener.onEdgeRubberBand(2);
            }
            if (this.mCurrentX - this.mToX == 0 || progress >= WMElement.CAMERASIZEVALUE1B1) {
                return true;
            }
            return false;
        }

        private boolean interpolateLinear(float progress) {
            boolean z = true;
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                this.mCurrentX = this.mToX;
                this.mCurrentY = this.mToY;
                return true;
            }
            if (this.mAnimationKind == 9) {
                progress = CaptureAnimation.calculateSlide(progress);
            }
            this.mCurrentX = (int) (((float) this.mFromX) + (((float) (this.mToX - this.mFromX)) * progress));
            this.mCurrentY = (int) (((float) this.mFromY) + (((float) (this.mToY - this.mFromY)) * progress));
            if (this.mAnimationKind == 9) {
                return false;
            }
            if (!(this.mCurrentX == this.mToX && this.mCurrentY == this.mToY)) {
                z = false;
            }
            return z;
        }
    }

    static {
        for (int i = 0; i < CENTER_OUT_INDEX.length; i++) {
            int j = (i + 1) / 2;
            if ((i & 1) == 0) {
                j = -j;
            }
            CENTER_OUT_INDEX[i] = j;
        }
    }

    public PositionController(Context context, Listener listener) {
        int i;
        this.mListener = listener;
        this.mPageScroller = new FlingScroller();
        this.mFilmScroller = new Scroller(context, null, false);
        initPlatform();
        for (i = -3; i <= 3; i++) {
            this.mBoxes.put(i, new Box());
            initBox(i);
            this.mRects.put(i, new Rect());
            this.mTargetRects.put(i, new Rect());
        }
        for (i = -3; i < 3; i++) {
            this.mGaps.put(i, new Gap());
            initGap(i);
        }
    }

    public void setViewSize(int viewW, int viewH) {
        if (viewW != this.mViewW || viewH != this.mViewH) {
            boolean wasFillShort = isFillShortScale();
            boolean wasFillLong = isFillLongScale();
            this.mViewW = viewW;
            this.mViewH = viewH;
            initPlatform();
            for (int i = -3; i <= 3; i++) {
                setBoxSize(i, viewW, viewH, true);
            }
            updateScaleAndGapLimit();
            Box b = (Box) this.mBoxes.get(0);
            if (wasFillShort) {
                b.mCurrentScale = b.mScaleFillShort;
            } else if (wasFillLong) {
                b.mCurrentScale = b.mScaleFillLong;
            }
            skipToFinalPosition();
        }
    }

    public void setConstrainedFrame(Rect cFrame) {
        if (!this.mConstrainedFrame.equals(cFrame)) {
            this.mConstrainedFrame.set(cFrame);
            this.mPlatform.updateDefaultXY();
            updateScaleAndGapLimit();
            snapAndRedraw();
        }
    }

    public void forceImageSize(int index, Size s) {
        if (s.width != 0 && s.height != 0) {
            Box b = (Box) this.mBoxes.get(index);
            b.mImageW = s.width;
            b.mImageH = s.height;
        }
    }

    public void setImageSize(int index, Size s, Rect cFrame) {
        if (s.width != 0 && s.height != 0) {
            boolean needUpdate = index == 0;
            if (!(cFrame == null || this.mConstrainedFrame.equals(cFrame))) {
                this.mConstrainedFrame.set(cFrame);
                this.mPlatform.updateDefaultXY();
                needUpdate = true;
            }
            if (needUpdate | setBoxSize(index, s.width, s.height, false)) {
                updateScaleAndGapLimit();
                snapAndRedraw();
            }
        }
    }

    private boolean setBoxSize(int i, int width, int height, boolean isViewSize) {
        Box b = (Box) this.mBoxes.get(i);
        boolean wasViewSize = b.mUseViewSize;
        if (!wasViewSize && isViewSize) {
            return false;
        }
        b.mUseViewSize = isViewSize;
        if (width == b.mImageW && height == b.mImageH) {
            return false;
        }
        float ratio;
        boolean isFillShortScale = i == 0 ? isFillShortScale() : false;
        boolean isFillLongScale = i == 0 ? isFillLongScale() : false;
        if (width > height) {
            ratio = ((float) b.mImageW) / ((float) width);
        } else {
            ratio = ((float) b.mImageH) / ((float) height);
        }
        b.mImageW = width;
        b.mImageH = height;
        if ((!wasViewSize || isViewSize) && this.mFilmMode) {
            b.mCurrentScale *= ratio;
            b.mFromScale *= ratio;
            b.mToScale *= ratio;
        } else {
            updateScaleFillShortAndLong(b);
            float f = (isFillShortScale || !isFillLongScale) ? b.mScaleFillShort : b.mScaleFillLong;
            b.mCurrentScale = f;
            b.mAnimationStartTime = -1;
        }
        if (i == 0) {
            this.mFocusX /= ratio;
            this.mFocusY /= ratio;
        }
        return true;
    }

    public void setFilmMode(boolean enabled) {
        if (enabled != this.mFilmMode) {
            this.mFilmMode = enabled;
            this.mPlatform.updateDefaultXY();
            updateScaleAndGapLimit();
            stopAnimation();
            if (!enabled) {
                startAnimation(this.mPlatform.mCurrentX, ((Box) this.mBoxes.get(0)).mCurrentY, ((Box) this.mBoxes.get(0)).mScaleFillShort, 4);
            }
            snapAndRedraw();
        }
    }

    public void setExtraScalingRange(boolean enabled) {
        if (this.mExtraScalingRange != enabled) {
            this.mExtraScalingRange = enabled;
            if (!enabled) {
                snapAndRedraw();
            }
        }
    }

    private void updateScaleAndGapLimit() {
        int i;
        for (i = -3; i <= 3; i++) {
            Box b = (Box) this.mBoxes.get(i);
            b.mScaleMin = getMinimalScale(b);
            updateScaleFillShortAndLong(b);
            b.mScaleMax = getMaximalScale(b);
        }
        for (i = -3; i < 3; i++) {
            ((Gap) this.mGaps.get(i)).mDefaultSize = getDefaultGapSize(i);
        }
    }

    private int getDefaultGapSize(int i) {
        if (this.mFilmMode) {
            return FILM_IMAGE_GAP;
        }
        return IMAGE_GAP + Math.max(gapToSide((Box) this.mBoxes.get(i)), gapToSide((Box) this.mBoxes.get(i + 1)));
    }

    private int gapToSide(Box b) {
        return (int) (((((float) this.mViewW) - (b.mScaleFillShort * ((float) b.mImageW))) / 2.0f) + 0.5f);
    }

    private int gapToSide(Box b, float scale) {
        return Math.max(0, (int) (((((float) this.mViewW) - (((float) b.mImageW) * scale)) / 2.0f) + 0.5f));
    }

    public void stopAnimation() {
        int i;
        this.mPlatform.mAnimationStartTime = -1;
        for (i = -3; i <= 3; i++) {
            ((Box) this.mBoxes.get(i)).mAnimationStartTime = -1;
        }
        for (i = -3; i < 3; i++) {
            ((Gap) this.mGaps.get(i)).mAnimationStartTime = -1;
        }
    }

    public void skipAnimation() {
        int i;
        if (this.mPlatform.mAnimationStartTime != -1) {
            this.mPlatform.mCurrentX = this.mPlatform.mToX;
            this.mPlatform.mCurrentY = this.mPlatform.mToY;
            this.mPlatform.mAnimationStartTime = -1;
        }
        for (i = -3; i <= 3; i++) {
            Box b = (Box) this.mBoxes.get(i);
            if (b.mAnimationStartTime != -1) {
                b.mCurrentY = b.mToY;
                b.mCurrentScale = b.mToScale;
                b.mAnimationStartTime = -1;
            }
        }
        for (i = -3; i < 3; i++) {
            Gap g = (Gap) this.mGaps.get(i);
            if (g.mAnimationStartTime != -1) {
                g.mCurrentGap = g.mToGap;
                g.mAnimationStartTime = -1;
            }
        }
        redraw();
    }

    public void snapback() {
        snapAndRedraw();
    }

    public void skipToFinalPosition() {
        stopAnimation();
        snapAndRedraw();
        skipAnimation();
    }

    public void zoomIn(float tapX, float tapY, float targetScale) {
        Box b = (Box) this.mBoxes.get(0);
        int x = (int) (((-(((tapX - ((float) (this.mViewW / 2))) - ((float) this.mPlatform.mCurrentX)) / b.mCurrentScale)) * targetScale) + 0.5f);
        int y = (int) (((-(((tapY - ((float) (this.mViewH / 2))) - ((float) b.mCurrentY)) / b.mCurrentScale)) * targetScale) + 0.5f);
        calculateStableBound(targetScale);
        startAnimation(Utils.clamp(x, this.mBoundLeft, this.mBoundRight), Utils.clamp(y, this.mBoundTop, this.mBoundBottom), Utils.clamp(targetScale, b.mScaleMin, b.mScaleMax), 4);
    }

    public void resetToFullView() {
        Box b = (Box) this.mBoxes.get(0);
        float targetScale = b.mScaleFillShort;
        calculateStableBound(targetScale);
        startAnimation(this.mPlatform.mDefaultX, 0, Utils.clamp(targetScale, b.mScaleMin, b.mScaleMax), 4);
    }

    public void beginScale(float focusX, float focusY) {
        focusX -= (float) (this.mViewW / 2);
        focusY -= (float) (this.mViewH / 2);
        Box b = (Box) this.mBoxes.get(0);
        Platform p = this.mPlatform;
        this.mInScale = true;
        this.mFocusX = (float) ((int) (((focusX - ((float) p.mCurrentX)) / b.mCurrentScale) + 0.5f));
        this.mFocusY = (float) ((int) (((focusY - ((float) b.mCurrentY)) / b.mCurrentScale) + 0.5f));
    }

    public int scaleBy(float s, float focusX, float focusY) {
        focusX -= (float) (this.mViewW / 2);
        focusY -= (float) (this.mViewH / 2);
        Box b = (Box) this.mBoxes.get(0);
        Platform p = this.mPlatform;
        s = b.clampScale(getTargetScale(b) * s);
        startAnimation(this.mFilmMode ? p.mCurrentX : (int) ((focusX - (this.mFocusX * s)) + 0.5f), this.mFilmMode ? b.mCurrentY : (int) ((focusY - (this.mFocusY * s)) + 0.5f), s, 1);
        if (s < b.mScaleMin) {
            return -1;
        }
        return s > b.mScaleMax ? 1 : 0;
    }

    public void endScale() {
        this.mInScale = false;
    }

    public void startHorizontalSlide(boolean autoSlide) {
        startAnimation(this.mPlatform.mDefaultX, 0, ((Box) this.mBoxes.get(0)).mScaleFillShort, autoSlide ? 10 : 3);
    }

    private boolean canScroll() {
        Box b = (Box) this.mBoxes.get(0);
        if (b.mAnimationStartTime == -1) {
            return true;
        }
        switch (b.mAnimationKind) {
            case 0:
            case 6:
            case 7:
                return true;
            default:
                return false;
        }
    }

    public boolean isNeedRubberBandEffect(int edge) {
        boolean z = false;
        if (this.mListener == null) {
            return false;
        }
        if ((this.mListener.needRubberBandEffectEdge() & edge) != 0) {
            z = true;
        }
        return z;
    }

    public void scrollPage(int dx, int dy) {
        if (canScroll()) {
            Box b = (Box) this.mBoxes.get(0);
            Platform p = this.mPlatform;
            calculateStableBound(b.mCurrentScale);
            int x = p.mCurrentX + dx;
            int y = b.mCurrentY + dy;
            if (!isZoomIn() || (this.mBoundTop == this.mBoundBottom && !scaledImageTallerThanView(b.mCurrentScale))) {
                y = Utils.clamp(y, this.mBoundTop, this.mBoundBottom);
            } else {
                if (y < this.mBoundTop) {
                    y = b.mCurrentY + Utils.getElasticInterpolation(dy, b.mCurrentY - this.mBoundTop, 100);
                } else if (y > this.mBoundBottom) {
                    y = b.mCurrentY + Utils.getElasticInterpolation(dy, b.mCurrentY - this.mBoundBottom, 100);
                }
                y = Utils.clamp(y, this.mBoundTop - RUBBER_BAND_RANGE, this.mBoundBottom + RUBBER_BAND_RANGE);
            }
            if ((!this.mHasPrev || isNeedRubberBandEffect(2)) && x > this.mBoundRight) {
                x = Utils.clamp(p.mCurrentX + Utils.getElasticInterpolation(dx, p.mCurrentX - this.mBoundRight, 100), this.mBoundRight, this.mBoundRight + RUBBER_BAND_RANGE);
                if (isNeedRubberBandEffect(2)) {
                    this.mListener.onEdgeRubberBand(2);
                }
            } else if ((!this.mHasNext || isNeedRubberBandEffect(8)) && x < this.mBoundLeft) {
                x = Utils.clamp(p.mCurrentX + Utils.getElasticInterpolation(dx, p.mCurrentX - this.mBoundLeft, 100), this.mBoundLeft - RUBBER_BAND_RANGE, this.mBoundLeft);
                if (isNeedRubberBandEffect(8)) {
                    this.mListener.onEdgeRubberBand(8);
                }
            }
            startAnimation(x, y, b.mCurrentScale, 0);
        }
    }

    public void scrollFilmX(int dx) {
        if (canScroll()) {
            Box b = (Box) this.mBoxes.get(0);
            Platform p = this.mPlatform;
            if (b.mAnimationStartTime != -1) {
                switch (b.mAnimationKind) {
                    case 0:
                    case 6:
                    case 7:
                        break;
                    default:
                        return;
                }
            }
            int x = (p.mCurrentX + dx) - this.mPlatform.mDefaultX;
            if (!this.mHasPrev && x > 0) {
                x = Math.min((p.mCurrentX - this.mPlatform.mDefaultX) + Utils.getElasticInterpolation(dx, p.mCurrentX - this.mPlatform.mDefaultX, 100), RUBBER_BAND_RANGE);
            } else if (!this.mHasNext && x < 0) {
                x = Math.max((p.mCurrentX - this.mPlatform.mDefaultX) + Utils.getElasticInterpolation(dx, p.mCurrentX - this.mPlatform.mDefaultX, 100), -RUBBER_BAND_RANGE);
            }
            startAnimation(x + this.mPlatform.mDefaultX, b.mCurrentY, b.mCurrentScale, 0);
        }
    }

    public boolean flingPage(int velocityX, int velocityY) {
        Box b = (Box) this.mBoxes.get(0);
        Platform p = this.mPlatform;
        if (viewWiderThanScaledImage(b.mCurrentScale) && viewTallerThanScaledImage(b.mCurrentScale)) {
            return false;
        }
        int targetX;
        int targetY;
        int edges = getImageAtEdges();
        if (velocityX <= 0 || (edges & 1) == 0) {
            if (velocityX < 0 && (edges & 2) != 0) {
            }
            if (velocityY <= 0 || (edges & 4) == 0) {
                if (velocityY < 0 && (edges & 8) != 0) {
                }
                if (velocityX != 0 && velocityY == 0) {
                    return false;
                }
                this.mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, this.mBoundLeft - RUBBER_BAND_RANGE, this.mBoundRight + RUBBER_BAND_RANGE, this.mBoundTop - RUBBER_BAND_RANGE, this.mBoundBottom + RUBBER_BAND_RANGE);
                targetX = this.mPageScroller.getFinalX();
                targetY = this.mPageScroller.getFinalY();
                ANIM_TIME[6] = this.mPageScroller.getDuration();
                return startAnimation(targetX, targetY, b.mCurrentScale, 6);
            }
            velocityY = 0;
            if (velocityX != 0) {
            }
            this.mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, this.mBoundLeft - RUBBER_BAND_RANGE, this.mBoundRight + RUBBER_BAND_RANGE, this.mBoundTop - RUBBER_BAND_RANGE, this.mBoundBottom + RUBBER_BAND_RANGE);
            targetX = this.mPageScroller.getFinalX();
            targetY = this.mPageScroller.getFinalY();
            ANIM_TIME[6] = this.mPageScroller.getDuration();
            return startAnimation(targetX, targetY, b.mCurrentScale, 6);
        }
        velocityX = 0;
        velocityY = 0;
        if (velocityX != 0) {
        }
        this.mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, this.mBoundLeft - RUBBER_BAND_RANGE, this.mBoundRight + RUBBER_BAND_RANGE, this.mBoundTop - RUBBER_BAND_RANGE, this.mBoundBottom + RUBBER_BAND_RANGE);
        targetX = this.mPageScroller.getFinalX();
        targetY = this.mPageScroller.getFinalY();
        ANIM_TIME[6] = this.mPageScroller.getDuration();
        return startAnimation(targetX, targetY, b.mCurrentScale, 6);
    }

    public boolean flingFilmX(int velocityX) {
        if (velocityX == 0) {
            return false;
        }
        Box b = (Box) this.mBoxes.get(0);
        this.mFilmScroller.fling(this.mPlatform.mCurrentX, 0, velocityX, 0, Target.SIZE_ORIGINAL, Integer.MAX_VALUE, 0, 0);
        return startAnimation(this.mFilmScroller.getFinalX(), b.mCurrentY, b.mCurrentScale, 7);
    }

    public int hitTest(int x, int y) {
        for (int i = 0; i < 7; i++) {
            int j = CENTER_OUT_INDEX[i];
            if (((Rect) this.mRects.get(j)).contains(x, y)) {
                return j;
            }
        }
        return Integer.MAX_VALUE;
    }

    private void redraw() {
        layoutAndSetPosition();
        this.mListener.invalidate();
    }

    private void snapAndRedraw() {
        int i;
        this.mPlatform.startSnapback();
        for (i = -3; i <= 3; i++) {
            ((Box) this.mBoxes.get(i)).startSnapback();
        }
        for (i = -3; i < 3; i++) {
            ((Gap) this.mGaps.get(i)).startSnapback();
        }
        this.mFilmRatio.startSnapback();
        redraw();
    }

    private boolean startAnimation(int targetX, int targetY, float targetScale, int kind) {
        boolean changed = ((this.mPlatform.doAnimation(targetX, this.mPlatform.mDefaultY, kind) | ((Box) this.mBoxes.get(0)).doAnimation(targetY, targetScale, kind)) | ((Gap) this.mGaps.get(-1)).doAnimation(getTargetGap(0, targetScale, -1), kind)) | ((Gap) this.mGaps.get(0)).doAnimation(getTargetGap(0, targetScale, 1), kind);
        if (changed) {
            redraw();
        }
        return changed;
    }

    private int getTargetGap(int i, float scale, int neiborIndex) {
        if (this.mFilmMode) {
            return FILM_IMAGE_GAP;
        }
        return IMAGE_GAP + Math.max(gapToSide((Box) this.mBoxes.get(i), scale), gapToSide((Box) this.mBoxes.get(neiborIndex)));
    }

    public void advanceAnimation() {
        int i;
        boolean hasChanged = this.mPlatform.advanceAnimation();
        for (i = -3; i <= 3; i++) {
            hasChanged |= ((Box) this.mBoxes.get(i)).advanceAnimation();
        }
        for (i = -3; i < 3; i++) {
            hasChanged |= ((Gap) this.mGaps.get(i)).advanceAnimation();
        }
        if (hasChanged | this.mFilmRatio.advanceAnimation()) {
            redraw();
        }
    }

    public boolean inOpeningAnimation() {
        if (this.mPlatform.mAnimationKind != 5 || this.mPlatform.mAnimationStartTime == -1) {
            return ((Box) this.mBoxes.get(0)).mAnimationKind == 5 && ((Box) this.mBoxes.get(0)).mAnimationStartTime != -1;
        } else {
            return true;
        }
    }

    private int widthOf(Box b, float scale) {
        return (int) ((((float) b.mImageW) * scale) + 0.5f);
    }

    private int heightOf(Box b, float scale) {
        return (int) ((((float) b.mImageH) * scale) + 0.5f);
    }

    private int widthOf(Box b) {
        return (int) ((((float) b.mImageW) * b.mCurrentScale) + 0.5f);
    }

    private int heightOf(Box b) {
        return (int) ((((float) b.mImageH) * b.mCurrentScale) + 0.5f);
    }

    private void layoutAndSetPosition() {
        int i;
        for (i = 0; i < 7; i++) {
            convertBoxToRect(CENTER_OUT_INDEX[i]);
        }
        for (i = 0; i < 7; i++) {
            convertBoxToTargetRect(CENTER_OUT_INDEX[i]);
        }
    }

    private void convertBoxToRect(int i) {
        Box b = (Box) this.mBoxes.get(i);
        Rect r = (Rect) this.mRects.get(i);
        int y = (b.mCurrentY + this.mPlatform.mCurrentY) + (this.mViewH / 2);
        int w = widthOf(b);
        int h = heightOf(b);
        if (i == 0) {
            r.left = (this.mPlatform.mCurrentX + (this.mViewW / 2)) - (w / 2);
            r.right = r.left + w;
        } else if (i > 0) {
            r.left = ((Rect) this.mRects.get(i - 1)).right + ((Gap) this.mGaps.get(i - 1)).mCurrentGap;
            r.right = r.left + w;
        } else {
            r.right = ((Rect) this.mRects.get(i + 1)).left - ((Gap) this.mGaps.get(i)).mCurrentGap;
            r.left = r.right - w;
        }
        r.top = y - (h / 2);
        r.bottom = r.top + h;
    }

    private void convertBoxToTargetRect(int i) {
        Box b = (Box) this.mBoxes.get(i);
        Rect r = (Rect) this.mTargetRects.get(i);
        int y = this.mViewH / 2;
        int w = (int) ((((float) b.mImageW) * b.mScaleFillShort) + 0.5f);
        int h = (int) ((((float) b.mImageH) * b.mScaleFillShort) + 0.5f);
        if (i == 0) {
            r.left = (this.mViewW / 2) - (w / 2);
            r.right = r.left + w;
        } else if (i > 0) {
            r.left = ((Rect) this.mTargetRects.get(i - 1)).right + ((Gap) this.mGaps.get(i - 1)).mCurrentGap;
            r.right = r.left + w;
        } else {
            r.right = ((Rect) this.mTargetRects.get(i + 1)).left - ((Gap) this.mGaps.get(i)).mCurrentGap;
            r.left = r.right - w;
        }
        r.top = y - (h / 2);
        r.bottom = r.top + h;
    }

    public Rect getPosition(int index) {
        return (Rect) this.mRects.get(index);
    }

    public Rect getTargetPosition(int index) {
        return (Rect) this.mTargetRects.get(index);
    }

    private void initPlatform() {
        this.mPlatform.updateDefaultXY();
        this.mPlatform.mCurrentX = this.mPlatform.mDefaultX;
        this.mPlatform.mCurrentY = this.mPlatform.mDefaultY;
        this.mPlatform.mAnimationStartTime = -1;
    }

    private void initBox(int index) {
        Box b = (Box) this.mBoxes.get(index);
        b.mImageW = this.mViewW;
        b.mImageH = this.mViewH;
        b.mUseViewSize = true;
        b.mScaleMin = getMinimalScale(b);
        updateScaleFillShortAndLong(b);
        b.mScaleMax = getMaximalScale(b);
        b.mCurrentY = 0;
        b.mCurrentScale = b.mScaleFillShort;
        b.mAnimationStartTime = -1;
        b.mAnimationKind = -1;
    }

    private void initBox(int index, Size size) {
        if (size.width == 0 || size.height == 0) {
            initBox(index);
            return;
        }
        Box b = (Box) this.mBoxes.get(index);
        b.mImageW = size.width;
        b.mImageH = size.height;
        b.mUseViewSize = false;
        b.mScaleMin = getMinimalScale(b);
        updateScaleFillShortAndLong(b);
        b.mScaleMax = getMaximalScale(b);
        b.mCurrentY = 0;
        b.mCurrentScale = b.mScaleFillShort;
        b.mAnimationStartTime = -1;
        b.mAnimationKind = -1;
    }

    private void initGap(int index) {
        Gap g = (Gap) this.mGaps.get(index);
        g.mDefaultSize = getDefaultGapSize(index);
        g.mCurrentGap = g.mDefaultSize;
        g.mAnimationStartTime = -1;
    }

    private void initGap(int index, int size) {
        Gap g = (Gap) this.mGaps.get(index);
        g.mDefaultSize = getDefaultGapSize(index);
        g.mCurrentGap = size;
        g.mAnimationStartTime = -1;
    }

    public void moveBox(int[] fromIndex, boolean hasPrev, boolean hasNext, boolean constrained, Size[] sizes, int maskOffset) {
        int i;
        int k;
        Box b;
        this.mHasPrev = hasPrev;
        this.mHasNext = hasNext;
        RangeIntArray from = new RangeIntArray(fromIndex, -3, 3);
        layoutAndSetPosition();
        for (i = -3; i <= 3; i++) {
            ((Box) this.mBoxes.get(i)).mAbsoluteX = (((Rect) this.mRects.get(i)).centerX() - (this.mViewW / 2)) + maskOffset;
        }
        for (i = -3; i <= 3; i++) {
            this.mTempBoxes.put(i, (Box) this.mBoxes.get(i));
            this.mBoxes.put(i, null);
        }
        for (i = -3; i < 3; i++) {
            this.mTempGaps.put(i, (Gap) this.mGaps.get(i));
            this.mGaps.put(i, null);
        }
        for (i = -3; i <= 3; i++) {
            int j = from.get(i);
            if (j != Integer.MAX_VALUE) {
                this.mBoxes.put(i, (Box) this.mTempBoxes.get(j));
                this.mTempBoxes.put(j, null);
            }
        }
        for (i = -3; i < 3; i++) {
            j = from.get(i);
            if (j != Integer.MAX_VALUE) {
                k = from.get(i + 1);
                if (k != Integer.MAX_VALUE && j + 1 == k) {
                    this.mGaps.put(i, (Gap) this.mTempGaps.get(j));
                    this.mTempGaps.put(j, null);
                }
            }
        }
        k = -3;
        for (i = -3; i <= 3; i++) {
            if (this.mBoxes.get(i) == null) {
                while (this.mTempBoxes.get(k) == null) {
                    k++;
                }
                int k2 = k + 1;
                this.mBoxes.put(i, (Box) this.mTempBoxes.get(k));
                initBox(i, sizes[i + 3]);
                k = k2;
            }
        }
        int first = -3;
        while (first <= 3 && from.get(first) == Integer.MAX_VALUE) {
            first++;
        }
        int last = 3;
        while (last >= -3 && from.get(last) == Integer.MAX_VALUE) {
            last--;
        }
        if (first > 3) {
            ((Box) this.mBoxes.get(0)).mAbsoluteX = this.mPlatform.mCurrentX;
            last = 0;
            first = 0;
        }
        for (i = Math.max(0, first + 1); i < last; i++) {
            if (from.get(i) == Integer.MAX_VALUE) {
                Box a = (Box) this.mBoxes.get(i - 1);
                b = (Box) this.mBoxes.get(i);
                int wa = widthOf(a);
                b.mAbsoluteX = ((a.mAbsoluteX + (wa - (wa / 2))) + (widthOf(b) / 2)) + getDefaultGapSize(i);
                if (this.mPopFromTop) {
                    b.mCurrentY = -((this.mViewH / 2) + (heightOf(b) / 2));
                } else {
                    b.mCurrentY = (this.mViewH / 2) + (heightOf(b) / 2);
                }
            }
        }
        for (i = Math.min(-1, last - 1); i > first; i--) {
            if (from.get(i) == Integer.MAX_VALUE) {
                a = (Box) this.mBoxes.get(i + 1);
                b = (Box) this.mBoxes.get(i);
                wa = widthOf(a);
                int wb = widthOf(b);
                b.mAbsoluteX = ((a.mAbsoluteX - (wa / 2)) - (wb - (wb / 2))) - getDefaultGapSize(i);
                if (this.mPopFromTop) {
                    b.mCurrentY = -((this.mViewH / 2) + (heightOf(b) / 2));
                } else {
                    b.mCurrentY = (this.mViewH / 2) + (heightOf(b) / 2);
                }
            }
        }
        k = -3;
        i = -3;
        while (i < 3) {
            if (this.mGaps.get(i) == null) {
                while (this.mTempGaps.get(k) == null) {
                    k++;
                }
                k2 = k + 1;
                this.mGaps.put(i, (Gap) this.mTempGaps.get(k));
                a = (Box) this.mBoxes.get(i);
                b = (Box) this.mBoxes.get(i + 1);
                wa = widthOf(a);
                wb = widthOf(b);
                if (i < first || i >= last) {
                    initGap(i);
                    k = k2;
                } else {
                    initGap(i, ((b.mAbsoluteX - a.mAbsoluteX) - (wb / 2)) - (wa - (wa / 2)));
                    k = k2;
                }
            }
            i++;
        }
        for (i = first - 1; i >= -3; i--) {
            a = (Box) this.mBoxes.get(i + 1);
            b = (Box) this.mBoxes.get(i);
            wa = widthOf(a);
            wb = widthOf(b);
            b.mAbsoluteX = ((a.mAbsoluteX - (wa / 2)) - (wb - (wb / 2))) - ((Gap) this.mGaps.get(i)).mCurrentGap;
        }
        for (i = last + 1; i <= 3; i++) {
            a = (Box) this.mBoxes.get(i - 1);
            b = (Box) this.mBoxes.get(i);
            wa = widthOf(a);
            b.mAbsoluteX = ((a.mAbsoluteX + (wa - (wa / 2))) + (widthOf(b) / 2)) + ((Gap) this.mGaps.get(i - 1)).mCurrentGap;
        }
        int dx = ((Box) this.mBoxes.get(0)).mAbsoluteX - this.mPlatform.mCurrentX;
        Platform platform = this.mPlatform;
        platform.mCurrentX += dx;
        platform = this.mPlatform;
        platform.mFromX += dx;
        platform = this.mPlatform;
        platform.mToX += dx;
        platform = this.mPlatform;
        platform.mFlingOffset += dx;
        if (this.mConstrained != constrained) {
            this.mConstrained = constrained;
            this.mPlatform.updateDefaultXY();
            updateScaleAndGapLimit();
        }
        snapAndRedraw();
    }

    public boolean isFillShortScale() {
        Box b = (Box) this.mBoxes.get(0);
        return isAlmostEqual(b.mCurrentScale, b.mScaleFillShort);
    }

    public boolean isFillLongScale() {
        Box b = (Box) this.mBoxes.get(0);
        return isAlmostEqual(b.mCurrentScale, b.mScaleFillLong);
    }

    public boolean isZoomIn() {
        Box b = (Box) this.mBoxes.get(0);
        if (isFillShortScale() || b.mCurrentScale <= b.mScaleFillShort) {
            return false;
        }
        return true;
    }

    public boolean isCenter() {
        Box b = (Box) this.mBoxes.get(0);
        if (this.mPlatform.mCurrentX == this.mPlatform.mDefaultX && b.mCurrentY == 0) {
            return true;
        }
        return false;
    }

    public int getImageWidth() {
        return ((Box) this.mBoxes.get(0)).mImageW;
    }

    public int getImageHeight() {
        return ((Box) this.mBoxes.get(0)).mImageH;
    }

    public float getImageScale() {
        return ((Box) this.mBoxes.get(0)).mCurrentScale;
    }

    public float getScaleShort() {
        return ((Box) this.mBoxes.get(0)).mScaleFillShort;
    }

    public float getScaleMax() {
        return ((Box) this.mBoxes.get(0)).mScaleMax;
    }

    public float getNextDoubleTapScale() {
        Box b = (Box) this.mBoxes.get(0);
        if (isAlmostEqual(b.mCurrentScale, b.mScaleFillShort)) {
            float scale;
            if (!isAlmostEqual(b.mScaleFillShort, b.mScaleFillLong)) {
                scale = b.mScaleFillLong;
            } else if (isAlmostEqual(b.mScaleFillLong, b.mScaleCustom)) {
                scale = WMElement.CAMERASIZEVALUE4B3;
            } else {
                scale = b.mScaleCustom;
            }
            return scale;
        } else if (!isAlmostEqual(b.mCurrentScale, b.mScaleFillLong)) {
            return b.mScaleFillShort;
        } else {
            return isAlmostEqual(b.mScaleFillLong, b.mScaleCustom) ? b.mScaleFillShort : b.mScaleCustom;
        }
    }

    public int getImageAtEdges() {
        Box b = (Box) this.mBoxes.get(0);
        Platform p = this.mPlatform;
        calculateStableBound(b.mCurrentScale);
        int edges = 0;
        if (p.mCurrentX <= this.mBoundLeft) {
            edges = 2;
        }
        if (p.mCurrentX >= this.mBoundRight) {
            edges |= 1;
        }
        if (b.mCurrentY <= this.mBoundTop) {
            edges |= 8;
        }
        if (b.mCurrentY >= this.mBoundBottom) {
            return edges | 4;
        }
        return edges;
    }

    public boolean isScrolling() {
        if (this.mPlatform.mAnimationStartTime == -1 || this.mPlatform.mCurrentX == this.mPlatform.mToX) {
            return false;
        }
        return true;
    }

    public void stopScrolling() {
        if (this.mPlatform.mAnimationStartTime != -1) {
            if (this.mFilmMode) {
                this.mFilmScroller.forceFinished(true);
            }
            Platform platform = this.mPlatform;
            int i = this.mPlatform.mCurrentX;
            this.mPlatform.mToX = i;
            platform.mFromX = i;
        }
    }

    public float getFilmRatio() {
        return this.mFilmRatio.mCurrentRatio;
    }

    public boolean hasDeletingBox() {
        for (int i = -3; i <= 3; i++) {
            if (((Box) this.mBoxes.get(i)).mAnimationKind == 8) {
                return true;
            }
        }
        return false;
    }

    public static float getMinimalScale(int imageW, int imageH, int viewW, int viewH) {
        return Math.min(((float) viewW) / ((float) imageW), ((float) viewH) / ((float) imageH));
    }

    private float getMinimalScale(Box b) {
        int viewW;
        int viewH;
        float wFactor = WMElement.CAMERASIZEVALUE1B1;
        float hFactor = WMElement.CAMERASIZEVALUE1B1;
        if (this.mFilmMode || !this.mConstrained || this.mConstrainedFrame.isEmpty() || b != this.mBoxes.get(0)) {
            viewW = this.mViewW;
            viewH = this.mViewH;
        } else {
            viewW = this.mConstrainedFrame.width();
            viewH = this.mConstrainedFrame.height();
        }
        if (this.mFilmMode) {
            if (this.mViewH > this.mViewW) {
                wFactor = 0.7f;
                hFactor = 0.48f;
            } else {
                wFactor = 0.7f;
                if (b.mImageH > b.mImageW) {
                    hFactor = 0.7f;
                } else {
                    hFactor = 0.48f;
                }
            }
        }
        return Math.min(WMElement.CAMERASIZEVALUE1B1, Math.min((((float) viewW) * wFactor) / ((float) b.mImageW), (((float) viewH) * hFactor) / ((float) b.mImageH)));
    }

    private float getMaximalScale(Box b) {
        if (this.mFilmMode) {
            return getMinimalScale(b);
        }
        if (!this.mConstrained || this.mConstrainedFrame.isEmpty()) {
            return Math.max(2.0f, b.mScaleFillLong * 1.5f);
        }
        return getMinimalScale(b);
    }

    private void updateScaleFillShortAndLong(Box b) {
        if (this.mFilmMode) {
            b.mScaleFillShort = getMinimalScale(b);
            b.mScaleFillLong = getMinimalScale(b);
            b.mScaleCustom = getMinimalScale(b);
            return;
        }
        float widthRatio = ((float) this.mViewW) / ((float) b.mImageW);
        float heightRatio = ((float) this.mViewH) / ((float) b.mImageH);
        b.mScaleFillShort = Math.min(widthRatio, heightRatio);
        b.mScaleFillLong = Math.max(widthRatio, heightRatio);
        if (b.mScaleFillLong >= WMElement.CAMERASIZEVALUE1B1) {
            b.mScaleCustom = WMElement.CAMERASIZEVALUE1B1;
        } else {
            b.mScaleCustom = Utils.clamp(calculateScale(b.mImageW, b.mImageH), b.mScaleFillLong, (float) WMElement.CAMERASIZEVALUE1B1);
        }
    }

    private static boolean isAlmostEqual(float a, float b) {
        float diff = a - b;
        if (diff < 0.0f) {
            diff = -diff;
        }
        return diff < 0.02f;
    }

    private void calculateStableBound(float scale, int horizontalSlack) {
        Box b = (Box) this.mBoxes.get(0);
        int w = widthOf(b, scale);
        int h = heightOf(b, scale);
        this.mBoundLeft = (((this.mViewW + 1) / 2) - ((w + 1) / 2)) - horizontalSlack;
        this.mBoundRight = ((w / 2) - (this.mViewW / 2)) + horizontalSlack;
        this.mBoundTop = ((this.mViewH + 1) / 2) - ((h + 1) / 2);
        this.mBoundBottom = (h / 2) - (this.mViewH / 2);
        if (viewTallerThanScaledImage(scale)) {
            this.mBoundBottom = 0;
            this.mBoundTop = 0;
        }
        if (viewWiderThanScaledImage(scale)) {
            int i = this.mPlatform.mDefaultX;
            this.mBoundRight = i;
            this.mBoundLeft = i;
        }
    }

    private void calculateStableBound(float scale) {
        calculateStableBound(scale, 0);
    }

    private boolean viewTallerThanScaledImage(float scale) {
        return this.mViewH >= heightOf((Box) this.mBoxes.get(0), scale);
    }

    private boolean scaledImageTallerThanView(float scale) {
        return heightOf((Box) this.mBoxes.get(0), scale) >= this.mViewH;
    }

    private boolean viewWiderThanScaledImage(float scale) {
        return this.mViewW >= widthOf((Box) this.mBoxes.get(0), scale);
    }

    private float getTargetScale(Box b) {
        return b.mAnimationStartTime == -1 ? b.mCurrentScale : b.mToScale;
    }

    private static float getDeceleration(int velocity) {
        return velocity > 0 ? -2000.0f : 2000.0f;
    }

    private int getSnapBackDuration(int start, int end) {
        int delta = start - end;
        return Math.min(600, (int) (Math.sqrt((((double) delta) * -2.0d) / ((double) getDeceleration(delta))) * 1000.0d));
    }

    private float calculateScale(int width, int height) {
        float minScreenLength = (float) Math.min(GalleryUtils.getWidthPixels(), GalleryUtils.getHeightPixels());
        float x = (float) Math.min(width, height);
        if (x <= minScreenLength / 2.0f) {
            return 2.0f;
        }
        if (x < minScreenLength * 2.0f) {
            return 0.33333334f + ((5.0f * minScreenLength) / (6.0f * x));
        }
        return 0.5f + ((0.5f * minScreenLength) / x);
    }
}
