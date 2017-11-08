package com.huawei.gallery.refocus.app;

import android.content.Context;
import android.graphics.Rect;
import android.widget.Scroller;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.AbsPhotoView.Size;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.CaptureAnimation;
import com.android.gallery3d.ui.FlingScroller;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.RangeArray;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.app.AbsAlbumPage;
import com.huawei.watermark.manager.parse.WMElement;

public class RefocusPositionController {
    private static final int[] ANIM_TIME = new int[]{0, 0, 600, AbsAlbumPage.LAUNCH_QUIK_ACTIVITY, 300, 300, 0, 0, 0, 700, 600};
    private static final int[] CENTER_OUT_INDEX = new int[7];
    private static final int HORIZONTAL_SLACK = GalleryUtils.dpToPixel(12);
    private static final int IMAGE_GAP = GalleryUtils.dpToPixel(16);
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
    private boolean mIsDeleteSnapBack;
    private Listener mListener;
    private volatile Rect mOpenAnimationRect;
    private FlingScroller mPageScroller;
    private Platform mPlatform = new Platform();
    private RangeArray<Rect> mRects = new RangeArray(-3, 3);
    private RangeArray<Box> mTempBoxes = new RangeArray(-3, 3);
    private RangeArray<Gap> mTempGaps = new RangeArray(-3, 2);
    private int mViewH = 1200;
    private int mViewW = 1200;

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
                progress = ((float) (AnimationTime.get() - this.mAnimationStartTime)) / ((float) this.mAnimationDuration);
            }
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                progress = WMElement.CAMERASIZEVALUE1B1;
            } else {
                GalleryLog.printDFXLog("RefocusPositionController.Animatable advanceAnimation applyInterpolationCurve");
                progress = applyInterpolationCurve(this.mAnimationKind, progress);
            }
            if (interpolate(progress)) {
                this.mAnimationStartTime = -2;
            }
            return true;
        }

        private static float applyInterpolationCurve(int kind, float progress) {
            float f = WMElement.CAMERASIZEVALUE1B1 - progress;
            GalleryLog.printDFXLog("RefocusPositionController.Animatable.applyInterpolationCurve f=" + f);
            switch (kind) {
                case 0:
                case 6:
                case 7:
                case 8:
                case 9:
                    return WMElement.CAMERASIZEVALUE1B1 - f;
                case 1:
                case 5:
                    return WMElement.CAMERASIZEVALUE1B1 - (f * f);
                case 2:
                case 3:
                case 4:
                case 10:
                    return WMElement.CAMERASIZEVALUE1B1 - ((((f * f) * f) * f) * f);
                default:
                    return progress;
            }
        }
    }

    private class Box extends Animatable {
        public float mCurrentScale;
        public int mCurrentY;
        public float mFromScale;
        public int mFromY;
        public int mImageH;
        public int mImageW;
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
            if (this.mAnimationKind == 0 && RefocusPositionController.this.mListener.isHoldingDown()) {
                return false;
            }
            if (this.mAnimationKind == 8 && RefocusPositionController.this.mListener.isHoldingDelete()) {
                return false;
            }
            if (RefocusPositionController.this.mInScale && this == RefocusPositionController.this.mBoxes.get(0)) {
                return false;
            }
            float scale;
            int y = this.mCurrentY;
            if (this == RefocusPositionController.this.mBoxes.get(0)) {
                scale = Utils.clamp(this.mCurrentScale, RefocusPositionController.this.mExtraScalingRange ? this.mScaleMin * 0.7f : this.mScaleMin, RefocusPositionController.this.mExtraScalingRange ? this.mScaleMax * 2.8f : this.mScaleMax);
                if (RefocusPositionController.this.mFilmMode) {
                    y = 0;
                } else {
                    RefocusPositionController.this.calculateStableBound(scale, RefocusPositionController.HORIZONTAL_SLACK);
                    if (!RefocusPositionController.this.viewTallerThanScaledImage(scale)) {
                        y += (int) ((RefocusPositionController.this.mFocusY * (this.mCurrentScale - scale)) + 0.5f);
                    }
                    y = Utils.clamp(y, RefocusPositionController.this.mBoundTop, RefocusPositionController.this.mBoundBottom);
                }
            } else {
                y = 0;
                scale = this.mScaleMin;
            }
            if (this.mCurrentY == y && Float.compare(this.mCurrentScale, scale) == 0) {
                return false;
            }
            int i;
            if (RefocusPositionController.this.mIsDeleteSnapBack) {
                i = 10;
            } else {
                i = 2;
            }
            return doAnimation(y, scale, i);
        }

        private boolean doAnimation(int targetY, float targetScale, int kind) {
            targetScale = clampScale(targetScale);
            if (this.mCurrentY == targetY && Float.compare(this.mCurrentScale, targetScale) == 0 && kind != 9) {
                return false;
            }
            this.mAnimationKind = kind;
            this.mFromY = this.mCurrentY;
            this.mFromScale = this.mCurrentScale;
            this.mToY = targetY;
            this.mToScale = targetScale;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = RefocusPositionController.ANIM_TIME[kind];
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
            RefocusPositionController.this.mPageScroller.computeScrollOffset(progress);
            RefocusPositionController.this.calculateStableBound(this.mCurrentScale);
            this.mCurrentY = RefocusPositionController.this.mPageScroller.getCurrY();
            return progress >= WMElement.CAMERASIZEVALUE1B1;
        }

        private boolean interpolateLinear(float progress) {
            boolean z = true;
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                GalleryLog.printDFXLog("RefocusPositionController");
                this.mCurrentY = this.mToY;
                this.mCurrentScale = this.mToScale;
                return true;
            }
            GalleryLog.printDFXLog("RefocusPositionController");
            this.mCurrentY = (int) (((float) this.mFromY) + (((float) (this.mToY - this.mFromY)) * progress));
            this.mCurrentScale = this.mFromScale + ((this.mToScale - this.mFromScale) * progress);
            if (this.mAnimationKind == 9) {
                this.mCurrentScale *= CaptureAnimation.calculateScale(progress);
                return false;
            }
            if (!(this.mCurrentY == this.mToY && Float.compare(this.mCurrentScale, this.mToScale) == 0)) {
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
            float target = RefocusPositionController.this.mFilmMode ? WMElement.CAMERASIZEVALUE1B1 : 0.0f;
            if (Float.compare(target, this.mToRatio) == 0) {
                return false;
            }
            return doAnimation(target, RefocusPositionController.this.mIsDeleteSnapBack ? 10 : 2);
        }

        private boolean doAnimation(float targetRatio, int kind) {
            this.mAnimationKind = kind;
            this.mFromRatio = this.mCurrentRatio;
            this.mToRatio = targetRatio;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = RefocusPositionController.ANIM_TIME[this.mAnimationKind];
            advanceAnimation();
            return true;
        }

        protected boolean interpolate(float progress) {
            boolean z = true;
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
                this.mCurrentRatio = this.mToRatio;
                return true;
            }
            this.mCurrentRatio = this.mFromRatio + ((this.mToRatio - this.mFromRatio) * progress);
            if (Float.compare(this.mCurrentRatio, this.mToRatio) != 0) {
                z = false;
            }
            return z;
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
            return doAnimation(this.mDefaultSize, RefocusPositionController.this.mIsDeleteSnapBack ? 10 : 2);
        }

        public boolean doAnimation(int targetSize, int kind) {
            if (this.mCurrentGap == targetSize && kind != 9) {
                return false;
            }
            this.mAnimationKind = kind;
            this.mFromGap = this.mCurrentGap;
            this.mToGap = targetSize;
            this.mAnimationStartTime = AnimationTime.startTime();
            this.mAnimationDuration = RefocusPositionController.ANIM_TIME[this.mAnimationKind];
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

    public interface Listener {
        void invalidate();

        boolean isHoldingDelete();

        boolean isHoldingDown();
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
            if ((this.mAnimationKind == 0 && RefocusPositionController.this.mListener.isHoldingDown()) || RefocusPositionController.this.mInScale) {
                return false;
            }
            Box b = (Box) RefocusPositionController.this.mBoxes.get(0);
            float scale = Utils.clamp(b.mCurrentScale, RefocusPositionController.this.mExtraScalingRange ? b.mScaleMin * 0.7f : b.mScaleMin, RefocusPositionController.this.mExtraScalingRange ? b.mScaleMax * 2.8f : b.mScaleMax);
            int x = this.mCurrentX;
            int y = this.mDefaultY;
            if (RefocusPositionController.this.mFilmMode) {
                x = this.mDefaultX;
            } else {
                RefocusPositionController.this.calculateStableBound(scale, RefocusPositionController.HORIZONTAL_SLACK);
                if (!RefocusPositionController.this.viewWiderThanScaledImage(scale)) {
                    x += (int) ((RefocusPositionController.this.mFocusX * (b.mCurrentScale - scale)) + 0.5f);
                }
                x = Utils.clamp(x, RefocusPositionController.this.mBoundLeft, RefocusPositionController.this.mBoundRight);
            }
            if (this.mCurrentX == x && this.mCurrentY == y) {
                return false;
            }
            int i;
            if (RefocusPositionController.this.mIsDeleteSnapBack) {
                i = 10;
            } else {
                i = 2;
            }
            return doAnimation(x, y, i);
        }

        public void updateDefaultXY() {
            int i = 0;
            if (!RefocusPositionController.this.mConstrained || RefocusPositionController.this.mConstrainedFrame.isEmpty()) {
                this.mDefaultX = 0;
                this.mDefaultY = 0;
                return;
            }
            this.mDefaultX = RefocusPositionController.this.mConstrainedFrame.centerX() - (RefocusPositionController.this.mViewW / 2);
            if (!RefocusPositionController.this.mFilmMode) {
                i = RefocusPositionController.this.mConstrainedFrame.centerY() - (RefocusPositionController.this.mViewH / 2);
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
            this.mAnimationDuration = RefocusPositionController.ANIM_TIME[kind];
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
            RefocusPositionController.this.mFilmScroller.computeScrollOffset();
            this.mCurrentX = RefocusPositionController.this.mFilmScroller.getCurrX() + this.mFlingOffset;
            int dir = 0;
            if (this.mCurrentX < this.mDefaultX) {
                if (!RefocusPositionController.this.mHasNext) {
                    dir = 8;
                }
            } else if (this.mCurrentX > this.mDefaultX && !RefocusPositionController.this.mHasPrev) {
                dir = 2;
            }
            if (dir != 0) {
                RefocusPositionController.this.mFilmScroller.forceFinished(true);
                this.mCurrentX = this.mDefaultX;
            }
            return RefocusPositionController.this.mFilmScroller.isFinished();
        }

        private boolean interpolateFlingPage(float progress) {
            RefocusPositionController.this.mPageScroller.computeScrollOffset(progress);
            RefocusPositionController.this.calculateStableBound(((Box) RefocusPositionController.this.mBoxes.get(0)).mCurrentScale);
            this.mCurrentX = RefocusPositionController.this.mPageScroller.getCurrX();
            if (progress >= WMElement.CAMERASIZEVALUE1B1) {
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
                GalleryLog.printDFXLog("RefocusPositionController.Platform.interpolateLinear calculateSlide progress");
                progress = CaptureAnimation.calculateSlide(progress);
            }
            this.mCurrentX = (int) (((float) this.mFromX) + (((float) (this.mToX - this.mFromX)) * progress));
            this.mCurrentY = (int) (((float) this.mFromY) + (((float) (this.mToY - this.mFromY)) * progress));
            if (this.mAnimationKind == 9) {
                return false;
            }
            GalleryLog.printDFXLog("RefocusPositionController.Platform.interpolateLinear mCurrentX == mToX && mCurrentY == mToY");
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

    public RefocusPositionController(Context context, Listener listener) {
        int i;
        this.mListener = listener;
        this.mPageScroller = new FlingScroller();
        this.mFilmScroller = new Scroller(context, null, false);
        initPlatform();
        for (i = -3; i <= 3; i++) {
            this.mBoxes.put(i, new Box());
            initBox(i);
            this.mRects.put(i, new Rect());
        }
        for (i = -3; i < 3; i++) {
            this.mGaps.put(i, new Gap());
            initGap(i);
        }
    }

    public void setViewSize(int viewW, int viewH) {
        if (viewW != this.mViewW || viewH != this.mViewH) {
            boolean wasMinimal = isAtMinimalScale();
            this.mViewW = viewW;
            this.mViewH = viewH;
            initPlatform();
            for (int i = -3; i <= 3; i++) {
                setBoxSize(i, viewW, viewH, true);
            }
            updateScaleAndGapLimit();
            if (wasMinimal) {
                Box b = (Box) this.mBoxes.get(0);
                b.mCurrentScale = b.mScaleMin;
            }
            if (!startOpeningAnimationIfNeeded()) {
                skipToFinalPosition();
            }
        }
    }

    public void setImageSize(int index, Size s, Rect cFrame) {
        if (s.width != 0 && s.height != 0) {
            boolean needUpdate = false;
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
            b.mCurrentScale = getMinimalScale(b);
            b.mAnimationStartTime = -1;
        }
        if (i == 0) {
            this.mFocusX /= ratio;
            this.mFocusY /= ratio;
        }
        return true;
    }

    private boolean startOpeningAnimationIfNeeded() {
        if (this.mOpenAnimationRect == null) {
            return false;
        }
        Box b = (Box) this.mBoxes.get(0);
        if (b.mUseViewSize) {
            return false;
        }
        Rect r = this.mOpenAnimationRect;
        this.mOpenAnimationRect = null;
        this.mPlatform.mCurrentX = r.centerX() - (this.mViewW / 2);
        b.mCurrentY = r.centerY() - (this.mViewH / 2);
        b.mCurrentScale = Math.max(((float) r.width()) / ((float) b.mImageW), ((float) r.height()) / ((float) b.mImageH));
        startAnimation(this.mPlatform.mDefaultX, 0, b.mScaleMin, 5);
        for (int i = -1; i < 1; i++) {
            Gap g = (Gap) this.mGaps.get(i);
            g.mCurrentGap = this.mViewW;
            g.doAnimation(g.mDefaultSize, 5);
        }
        return true;
    }

    public void setExtraScalingRange(boolean enabled) {
        if (this.mExtraScalingRange != enabled) {
            this.mExtraScalingRange = enabled;
            GalleryLog.printDFXLog("setExtraScalingRange enable changed");
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
            b.mScaleMax = getMaximalScale(b);
        }
        GalleryLog.printDFXLog("updateScaleAndGapLimit");
        for (i = -3; i < 3; i++) {
            ((Gap) this.mGaps.get(i)).mDefaultSize = getDefaultGapSize(i);
        }
    }

    private int getDefaultGapSize(int i) {
        if (this.mFilmMode) {
            return IMAGE_GAP;
        }
        return IMAGE_GAP + Math.max(gapToSide((Box) this.mBoxes.get(i)), gapToSide((Box) this.mBoxes.get(i + 1)));
    }

    private int gapToSide(Box b) {
        return (int) (((((float) this.mViewW) - (getMinimalScale(b) * ((float) b.mImageW))) / 2.0f) + 0.5f);
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

    public void skipToFinalPosition() {
        stopAnimation();
        snapAndRedraw();
        skipAnimation();
    }

    public void zoomIn(float tapX, float tapY, float targetScale) {
        tapX -= ((float) this.mViewW) / 2.0f;
        tapY -= ((float) this.mViewH) / 2.0f;
        Box b = (Box) this.mBoxes.get(0);
        GalleryLog.printDFXLog("RefocusPositionController");
        int x = (int) (((-((tapX - ((float) this.mPlatform.mCurrentX)) / b.mCurrentScale)) * targetScale) + 0.5f);
        int y = (int) (((-((tapY - ((float) b.mCurrentY)) / b.mCurrentScale)) * targetScale) + 0.5f);
        GalleryLog.printDFXLog("RefocusPositionController");
        calculateStableBound(targetScale);
        startAnimation(Utils.clamp(x, this.mBoundLeft, this.mBoundRight), Utils.clamp(y, this.mBoundTop, this.mBoundBottom), Utils.clamp(targetScale, b.mScaleMin, b.mScaleMax), 4);
    }

    public void resetToFullView() {
        startAnimation(this.mPlatform.mDefaultX, 0, ((Box) this.mBoxes.get(0)).mScaleMin, 4);
    }

    public void beginScale(float focusX, float focusY) {
        focusX -= ((float) this.mViewW) / 2.0f;
        focusY -= ((float) this.mViewH) / 2.0f;
        Box b = (Box) this.mBoxes.get(0);
        Platform p = this.mPlatform;
        this.mInScale = true;
        this.mFocusX = (float) ((int) (((focusX - ((float) p.mCurrentX)) / b.mCurrentScale) + 0.5f));
        this.mFocusY = (float) ((int) (((focusY - ((float) b.mCurrentY)) / b.mCurrentScale) + 0.5f));
    }

    public int scaleBy(float s, float focusX, float focusY) {
        focusX -= ((float) this.mViewW) / 2.0f;
        focusY -= ((float) this.mViewH) / 2.0f;
        Box b = (Box) this.mBoxes.get(0);
        Platform p = this.mPlatform;
        s = b.clampScale(getTargetScale(b) * s);
        startAnimation(this.mFilmMode ? p.mCurrentX : (int) ((focusX - (this.mFocusX * s)) + 0.5f), this.mFilmMode ? b.mCurrentY : (int) ((focusY - (this.mFocusY * s)) + 0.5f), s, 1);
        if (s < b.mScaleMin) {
            return -1;
        }
        return s > b.mScaleMax ? 1 : 0;
    }

    public float getScaleLimit(int imageW, int imageH) {
        return getScaleLimit(imageW, imageH, this.mViewW, this.mViewH);
    }

    private static float getScaleLimit(int imageW, int imageH, int viewW, int viewH) {
        float minViewLength = (float) Math.min(viewW, viewH);
        float x = (float) Math.min(imageW, imageH);
        if (x <= minViewLength / 2.0f) {
            return 2.0f;
        }
        if (x <= minViewLength / 2.0f || x >= minViewLength * 2.0f) {
            return WMElement.CAMERASIZEVALUE1B1;
        }
        return 0.6666667f + ((2.0f * minViewLength) / (MapConfig.MIN_ZOOM * x));
    }

    public void endScale() {
        this.mInScale = false;
    }

    private boolean canScroll() {
        Box b = (Box) this.mBoxes.get(0);
        if (b.mAnimationStartTime == -1) {
            GalleryLog.v("RefocusPositionController", "no animation, return true");
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

    public void scrollPage(int dx, int dy) {
        if (canScroll()) {
            Box b = (Box) this.mBoxes.get(0);
            Platform p = this.mPlatform;
            calculateStableBound(b.mCurrentScale);
            int x = p.mCurrentX + dx;
            int y = Utils.clamp(b.mCurrentY + dy, this.mBoundTop, this.mBoundBottom);
            if (!this.mHasPrev && x > this.mBoundRight) {
                x = this.mBoundRight;
            } else if (!this.mHasNext && x < this.mBoundLeft) {
                x = this.mBoundLeft;
            }
            startAnimation(x, y, b.mCurrentScale, 0);
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
                this.mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, this.mBoundLeft, this.mBoundRight, this.mBoundTop, this.mBoundBottom);
                targetX = this.mPageScroller.getFinalX();
                targetY = this.mPageScroller.getFinalY();
                ANIM_TIME[6] = this.mPageScroller.getDuration();
                return startAnimation(targetX, targetY, b.mCurrentScale, 6);
            }
            velocityY = 0;
            if (velocityX != 0) {
            }
            this.mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, this.mBoundLeft, this.mBoundRight, this.mBoundTop, this.mBoundBottom);
            targetX = this.mPageScroller.getFinalX();
            targetY = this.mPageScroller.getFinalY();
            ANIM_TIME[6] = this.mPageScroller.getDuration();
            return startAnimation(targetX, targetY, b.mCurrentScale, 6);
        }
        velocityX = 0;
        velocityY = 0;
        if (velocityX != 0) {
        }
        this.mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, this.mBoundLeft, this.mBoundRight, this.mBoundTop, this.mBoundBottom);
        targetX = this.mPageScroller.getFinalX();
        targetY = this.mPageScroller.getFinalY();
        ANIM_TIME[6] = this.mPageScroller.getDuration();
        return startAnimation(targetX, targetY, b.mCurrentScale, 6);
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
        boolean changed = this.mPlatform.doAnimation(targetX, this.mPlatform.mDefaultY, kind) | ((Box) this.mBoxes.get(0)).doAnimation(targetY, targetScale, kind);
        if (changed) {
            redraw();
        }
        return changed;
    }

    public boolean inDeleteSnapBackAnimation() {
        if (this.mPlatform.mAnimationKind == 10 && this.mPlatform.mAnimationStartTime != -1) {
            return true;
        }
        if (((Box) this.mBoxes.get(0)).mAnimationKind == 10) {
            return ((Box) this.mBoxes.get(0)).mAnimationStartTime != -1;
        } else {
            return false;
        }
    }

    public void advanceAnimation() {
        int i;
        boolean changed = this.mPlatform.advanceAnimation();
        for (i = -3; i <= 3; i++) {
            changed |= ((Box) this.mBoxes.get(i)).advanceAnimation();
        }
        for (i = -3; i < 3; i++) {
            changed |= ((Gap) this.mGaps.get(i)).advanceAnimation();
        }
        if (changed | this.mFilmRatio.advanceAnimation()) {
            redraw();
        }
    }

    public boolean inOpeningAnimation() {
        if (this.mPlatform.mAnimationKind == 5 && this.mPlatform.mAnimationStartTime != -1) {
            return true;
        }
        if (((Box) this.mBoxes.get(0)).mAnimationKind == 5) {
            return ((Box) this.mBoxes.get(0)).mAnimationStartTime != -1;
        } else {
            return false;
        }
    }

    private int widthOf(Box b) {
        return (int) ((((float) b.mImageW) * b.mCurrentScale) + 0.5f);
    }

    private int heightOf(Box b) {
        return (int) ((((float) b.mImageH) * b.mCurrentScale) + 0.5f);
    }

    private void layoutAndSetPosition() {
        for (int i = 0; i < 7; i++) {
            convertBoxToRect(CENTER_OUT_INDEX[i]);
        }
    }

    private int widthOf(Box b, float scale) {
        return (int) ((((float) b.mImageW) * scale) + 0.5f);
    }

    private int heightOf(Box b, float scale) {
        return (int) ((((float) b.mImageH) * scale) + 0.5f);
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

    public Rect getPosition(int index) {
        return (Rect) this.mRects.get(index);
    }

    private void initBox(int index) {
        Box b = (Box) this.mBoxes.get(index);
        b.mImageW = this.mViewW;
        b.mImageH = this.mViewH;
        b.mUseViewSize = true;
        b.mScaleMin = getMinimalScale(b);
        b.mScaleMax = getMaximalScale(b);
        b.mCurrentY = 0;
        b.mCurrentScale = b.mScaleMin;
        b.mAnimationStartTime = -1;
        b.mAnimationKind = -1;
    }

    private void initPlatform() {
        this.mPlatform.updateDefaultXY();
        this.mPlatform.mCurrentX = this.mPlatform.mDefaultX;
        this.mPlatform.mCurrentY = this.mPlatform.mDefaultY;
        this.mPlatform.mAnimationStartTime = -1;
    }

    private void initGap(int index) {
        Gap g = (Gap) this.mGaps.get(index);
        g.mDefaultSize = getDefaultGapSize(index);
        g.mCurrentGap = g.mDefaultSize;
        g.mAnimationStartTime = -1;
    }

    public boolean isAtMinimalScale() {
        Box b = (Box) this.mBoxes.get(0);
        return isAlmostEqual(b.mCurrentScale, b.mScaleMin);
    }

    public int getImageHeight() {
        return ((Box) this.mBoxes.get(0)).mImageH;
    }

    public int getImageWidth() {
        return ((Box) this.mBoxes.get(0)).mImageW;
    }

    public float getImageScale() {
        return ((Box) this.mBoxes.get(0)).mCurrentScale;
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

    public float getFilmRatio() {
        return this.mFilmRatio.mCurrentRatio;
    }

    public static float getMinimalScale(int imageW, int imageH, int viewW, int viewH) {
        return Math.min(getScaleLimit(imageW, imageH, viewW, viewH), Math.min((((float) viewW) * WMElement.CAMERASIZEVALUE1B1) / ((float) imageW), (((float) viewH) * WMElement.CAMERASIZEVALUE1B1) / ((float) imageH)));
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
            GalleryLog.printDFXLog("RefocusPositionController getMinimalScale viewW=" + viewW + ", viewH=" + viewH);
        }
        if (this.mFilmMode) {
            if (this.mViewH > this.mViewW) {
                wFactor = 0.7f;
                hFactor = 0.48f;
            } else {
                wFactor = 0.7f;
                hFactor = 0.7f;
            }
        }
        return Math.min(getScaleLimit(b.mImageW, b.mImageH), Math.min((((float) viewW) * wFactor) / ((float) b.mImageW), (((float) viewH) * hFactor) / ((float) b.mImageH)));
    }

    private float getMaximalScale(Box b) {
        if (this.mFilmMode) {
            return getMinimalScale(b);
        }
        if (!this.mConstrained || this.mConstrainedFrame.isEmpty()) {
            return getScaleLimit(b.mImageW, b.mImageH);
        }
        return getMinimalScale(b);
    }

    private static boolean isAlmostEqual(float a, float b) {
        float diff = a - b;
        if (diff < 0.0f) {
            diff = -diff;
        }
        return diff < 0.02f;
    }

    private void calculateStableBound(float scale, int horizontalSlack) {
        GalleryLog.printDFXLog("RefocusPositionController.calculateStableBound");
        Box b = (Box) this.mBoxes.get(0);
        int w = widthOf(b, scale);
        int h = heightOf(b, scale);
        this.mBoundLeft = (((this.mViewW + 1) / 2) - ((w + 1) / 2)) - horizontalSlack;
        this.mBoundRight = ((w / 2) - (this.mViewW / 2)) + horizontalSlack;
        this.mBoundTop = ((this.mViewH + 1) / 2) - ((h + 1) / 2);
        this.mBoundBottom = (h / 2) - (this.mViewH / 2);
        if (viewTallerThanScaledImage(scale)) {
            GalleryLog.printDFXLog("RefocusPositionController.calculateStableBound viewTallerThanScaledImage");
            this.mBoundBottom = 0;
            this.mBoundTop = 0;
        }
        if (viewWiderThanScaledImage(scale)) {
            GalleryLog.printDFXLog("RefocusPositionController.calculateStableBound viewWiderThanScaledImage");
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

    private boolean viewWiderThanScaledImage(float scale) {
        return this.mViewW >= widthOf((Box) this.mBoxes.get(0), scale);
    }

    private float getTargetScale(Box b) {
        return b.mAnimationStartTime == -1 ? b.mCurrentScale : b.mToScale;
    }
}
