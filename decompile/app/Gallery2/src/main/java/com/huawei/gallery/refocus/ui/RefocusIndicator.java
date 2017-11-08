package com.huawei.gallery.refocus.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.refocus.wideaperture.app.ApertureParameter;
import com.huawei.gallery.refocus.wideaperture.ui.ApertureLeafDrawer;
import com.huawei.watermark.manager.parse.WMElement;

public class RefocusIndicator extends RelativeLayout {
    private int BALL_LINE_PRESS_AREA = 0;
    private int BALL_PRESS_AREA_HORIZONTAL = 0;
    private int BALL_PRESS_AREA_VERTICAL = 0;
    private int DISTANCE = 0;
    private final Drawable mApertureBallDrawable;
    private Drawable mApertureBallOperationDrawable;
    private ApertureLeafDrawer mApertureLeafDrawer;
    private float mApertureRaito = 0.0f;
    private AnimatorSet mApertureReshowAnimation;
    private boolean mBallHasBeenOperated = false;
    private boolean mBallHasBeenTouched = false;
    private boolean mBallLineHasBeenTouched;
    private float mBallMovingDistance;
    private AnimatorSet mBallSmoothAnimation;
    private Runnable mDisappear = new Disappear();
    private float mFocusAlpha = WMElement.CAMERASIZEVALUE1B1;
    private AnimatorSet mFocusAnimation;
    private final int mFocusAreaWidth;
    private Drawable mFocusDrawble;
    private Rect mFocusEdge;
    private int mFocusIndicatorHeight;
    private int mFocusIndicatorWidth;
    private float mFocusScale = WMElement.CAMERASIZEVALUE1B1;
    private final AnimatorSet mFocusedAnimation;
    private final AnimatorSet mFocusedAnimationForAllFocus;
    private final Drawable mFocusedDrawable;
    private float mFocusingAlpha = 0.0f;
    private final AnimatorSet mFocusingAnimation;
    private final AnimatorSet mFocusingAnimationForAllFocus;
    private final Drawable mFocusingDrawable;
    private boolean mHasMoved = false;
    private boolean mIsBallOperatingOutOfHotArea = false;
    private boolean mIsFocusResultShow = false;
    private boolean mIsFocusing;
    private onWideApertureListener mListener;
    private float mPositionX = 10000.0f;
    private float mPositionY = 10000.0f;
    private LastPoint mReferenceZeroPoint;
    private int mSideDrawableHeight;
    private int mSideDrawableWidth;
    private int mState = 0;
    private int mTotalLength = ((int) (((float) GalleryUtils.getHeightPixels()) / MapConfig.MIN_ZOOM));
    AnimatorUpdateListener mUpdateListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            RefocusIndicator.this.invalidate();
        }
    };
    private ApertureParameter mWideAperturePara;

    private class Disappear implements Runnable {
        private Disappear() {
        }

        public void run() {
            RefocusIndicator.this.mState = 0;
            RefocusIndicator.this.mIsFocusResultShow = false;
            RefocusIndicator.this.mFocusDrawble = null;
            RefocusIndicator.this.mHasMoved = false;
            RefocusIndicator.this.mBallLineHasBeenTouched = false;
            RefocusIndicator.this.mBallHasBeenOperated = false;
            RefocusIndicator.this.invalidate();
        }
    }

    private static class LastPoint {
        private float pointX;
        private float pointY;

        private LastPoint(float x, float y) {
            this.pointX = x;
            this.pointY = y;
        }
    }

    public interface onWideApertureListener {
        boolean needSupportWideAperture();

        void onWideApertureValueChanged(int i);
    }

    public void setOnWideApertureListener(onWideApertureListener listener) {
        this.mListener = listener;
    }

    public void setFocusScale(float focusScale) {
        this.mFocusScale = focusScale;
    }

    public void setFocusAlpha(float focusAlpha) {
        this.mFocusAlpha = focusAlpha;
    }

    public void setFocusingAlpha(float focusingAlpha) {
        this.mFocusingAlpha = focusingAlpha;
    }

    private void cancelFocusAnimation() {
        if (this.mFocusAnimation != null && this.mFocusAnimation.isRunning()) {
            this.mFocusAnimation.cancel();
        }
    }

    private void cancelReshowFocusAnimation() {
        if (this.mApertureReshowAnimation != null && this.mApertureReshowAnimation.isRunning()) {
            this.mApertureReshowAnimation.cancel();
        }
    }

    private void cancelApertureBallClickAnimation() {
        if (this.mBallSmoothAnimation != null && this.mBallSmoothAnimation.isRunning()) {
            this.mBallSmoothAnimation.cancel();
        }
    }

    private void cancelAnimations() {
        cancelFocusAnimation();
        cancelReshowFocusAnimation();
        cancelApertureBallClickAnimation();
    }

    private void onMoveDetectWhenFocusShow() {
        cancelAnimations();
        this.mIsFocusResultShow = false;
        setFocusAlpha(WMElement.CAMERASIZEVALUE1B1);
        setFocusingAlpha(0.0f);
        this.mFocusDrawble = this.mFocusedDrawable;
        invalidate();
    }

    private float getLeafRatio() {
        return WMElement.CAMERASIZEVALUE1B1 - (((float) this.mWideAperturePara.getValue()) / ((float) (this.mWideAperturePara.getLevelCount() - 1)));
    }

    public void reshowRefocusIndicator(float adjustProgress) {
        this.mApertureRaito = WMElement.CAMERASIZEVALUE1B1 - Math.abs(adjustProgress);
        onMoveDetectWhenFocusShow();
        this.mApertureReshowAnimation.start();
    }

    private void setAperturePara() {
        int index = Utils.clamp((this.mWideAperturePara.getLevelCount() - 1) - ((int) (((double) ((this.mBallMovingDistance / ((float) this.mTotalLength)) * ((float) (this.mWideAperturePara.getLevelCount() - 1)))) + 0.5d)), 0, this.mWideAperturePara.getLevelCount() - 1);
        if (index >= 0 && index < this.mWideAperturePara.getLevelCount()) {
            this.mWideAperturePara.setValue(index);
        }
    }

    public void setWideApertureParameter(ApertureParameter para) {
        this.mWideAperturePara = para;
    }

    private CubicBezierInterpolator getCubicBezierInterpolatorA() {
        return new CubicBezierInterpolator(0.51f, 0.35f, 0.15f, WMElement.CAMERASIZEVALUE1B1);
    }

    public RefocusIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSideDrawableWidth = context.getResources().getDrawable(R.drawable.ic_allfocus_focus_focusing).getIntrinsicWidth();
        this.mSideDrawableHeight = context.getResources().getDrawable(R.drawable.ic_allfocus_focus_focusing).getIntrinsicHeight();
        this.mApertureBallDrawable = getResources().getDrawable(R.drawable.ic_focus_wide_aperture_normal);
        this.mFocusedDrawable = getResources().getDrawable(R.drawable.ic_allfocus_focus_focused);
        this.mFocusingDrawable = getResources().getDrawable(R.drawable.ic_allfocus_focus_focusing);
        this.mApertureLeafDrawer = new ApertureLeafDrawer(context);
        this.mFocusAreaWidth = this.mSideDrawableWidth;
        this.DISTANCE = context.getResources().getDimensionPixelSize(R.dimen.light_ball_distance);
        this.BALL_PRESS_AREA_HORIZONTAL = context.getResources().getDimensionPixelSize(R.dimen.aperture_ball_press_area_horizontal);
        this.BALL_PRESS_AREA_VERTICAL = context.getResources().getDimensionPixelSize(R.dimen.aperture_ball_press_area_vertical);
        this.BALL_LINE_PRESS_AREA = context.getResources().getDimensionPixelSize(R.dimen.aperture_ball_line_press_area);
        this.mTotalLength = context.getResources().getDimensionPixelSize(R.dimen.aperture_line_length);
        this.mFocusingAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.focus_indicator_focusing_animator);
        ((AnimatorSet) this.mFocusingAnimation.getChildAnimations().get(0)).setInterpolator(getCubicBezierInterpolatorA());
        this.mFocusingAnimationForAllFocus = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.focus_indicator_focusing_animator_for_allfocus);
        this.mFocusedAnimationForAllFocus = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.focus_indicator_focused_animator_for_allfocus);
        this.mFocusedAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.focus_indicator_focused_animator);
        this.mApertureReshowAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.focus_reshow_animator);
        this.mBallSmoothAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.aperture_ball_moving_for_click_animation);
        this.mBallSmoothAnimation.setInterpolator(getCubicBezierInterpolatorA());
        if (this.mReferenceZeroPoint == null) {
            this.mReferenceZeroPoint = new LastPoint(0.0f, 0.0f);
        }
        initFocusingAnimation();
        initFocusedAnimation();
        initFocusedAnimationForAllFocus();
        initReshowFoucsAnimation();
        initFocusingAnimationForAllFocus();
        initApertureBallMovingForClickAnimation();
    }

    private void resetFocusAnimation() {
        cancelAnimations();
        this.mDisappear.run();
    }

    private boolean needHideAutoFocusMoveIndicator() {
        if (getX() == GroundOverlayOptions.NO_DIMENSION && getY() == GroundOverlayOptions.NO_DIMENSION) {
            return true;
        }
        return false;
    }

    private void showStart() {
        post(new Runnable() {
            public void run() {
                RefocusIndicator.this.resetFocusAnimation();
                if (!RefocusIndicator.this.needHideAutoFocusMoveIndicator() && RefocusIndicator.this.mState == 0) {
                    RefocusIndicator.this.mState = 1;
                    RefocusIndicator.this.mFocusDrawble = RefocusIndicator.this.mFocusingDrawable;
                    RefocusIndicator.this.cancelFocusAnimation();
                    if (RefocusIndicator.this.needShowWideApertureBall()) {
                        RefocusIndicator.this.mFocusAnimation = RefocusIndicator.this.mFocusingAnimation;
                    } else {
                        RefocusIndicator.this.mFocusAnimation = RefocusIndicator.this.mFocusingAnimationForAllFocus;
                    }
                    RefocusIndicator.this.mIsFocusing = true;
                    RefocusIndicator.this.mFocusAnimation.start();
                }
            }
        });
    }

    public void showFocuing() {
        showStart();
    }

    public void clear() {
        resetFocusAnimation();
    }

    public void setScale(Rect focusEdge) {
        this.mFocusEdge = new Rect(focusEdge);
    }

    public int getFocusIndicatorViewWidth() {
        this.mFocusIndicatorWidth = (int) (((float) this.mSideDrawableWidth) * WMElement.CAMERASIZEVALUE1B1);
        return this.mFocusIndicatorWidth;
    }

    public int getFocusIndicatorViewHeight() {
        this.mFocusIndicatorHeight = (int) (((float) this.mSideDrawableHeight) * WMElement.CAMERASIZEVALUE1B1);
        return this.mFocusIndicatorHeight;
    }

    private Rect getBounds(int x, int y, float scale) {
        if (x < 0) {
            x = getWidth() / 2;
        }
        if (y < 0) {
            y = getHeight() / 2;
        }
        int left = Utils.clamp(x - (this.mFocusAreaWidth / 2), 0, getWidth() - this.mFocusAreaWidth);
        int top = Utils.clamp(y - (this.mFocusAreaWidth / 2), 0, getHeight() - this.mFocusAreaWidth);
        Rect rect = new Rect(left, top, this.mFocusAreaWidth + left, this.mFocusAreaWidth + top);
        int delta = (int) (((scale - WMElement.CAMERASIZEVALUE1B1) * ((float) this.mFocusAreaWidth)) / 2.0f);
        if (delta != 0) {
            return new Rect(rect.left - delta, rect.top - delta, rect.right + delta, rect.bottom + delta);
        }
        return rect;
    }

    public void setLocation(int x, int y) {
        resetFocusAnimation();
        setLocationCoordinate(x, y);
    }

    public void setLocationCoordinate(int x, int y) {
        if (x - (getFocusIndicatorViewWidth() / 2) < this.mFocusEdge.left) {
            x = this.mFocusEdge.left + (getFocusIndicatorViewWidth() / 2);
        } else if ((getFocusIndicatorViewWidth() / 2) + x > this.mFocusEdge.right) {
            x = this.mFocusEdge.right - (getFocusIndicatorViewWidth() / 2);
        }
        if (y - (getFocusIndicatorViewHeight() / 2) < this.mFocusEdge.top) {
            y = this.mFocusEdge.top + (getFocusIndicatorViewHeight() / 2);
        } else if ((getFocusIndicatorViewHeight() / 2) + y > this.mFocusEdge.bottom) {
            y = this.mFocusEdge.bottom - (getFocusIndicatorViewHeight() / 2);
        }
        setX((float) x);
        setY((float) y);
    }

    public void setX(float x) {
        this.mPositionX = x;
    }

    public void setY(float y) {
        this.mPositionY = y;
    }

    public float getX() {
        return this.mPositionX;
    }

    public float getY() {
        return this.mPositionY;
    }

    public void setScaleX(float scaleX) {
        setX(getX());
    }

    public void setScaleY(float scaleY) {
        setY(getY());
    }

    private void drawFocusIndicator(Canvas canvas) {
        this.mFocusDrawble.setBounds(getBounds((int) getX(), (int) getY(), this.mFocusScale));
        this.mFocusDrawble.setAlpha((int) (this.mFocusAlpha * 255.0f));
        this.mFocusDrawble.draw(canvas);
        if (this.mIsFocusResultShow) {
            this.mFocusingDrawable.setAlpha((int) (this.mFocusingAlpha * 255.0f));
            this.mFocusingDrawable.draw(canvas);
        }
    }

    private void drawApertureLeaf(Canvas canvas) {
        int alpha = (int) (this.mFocusAlpha * 255.0f);
        this.mApertureLeafDrawer.drawApertureLeaf(canvas, this.mApertureRaito, (int) getX(), (int) getY(), alpha);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mFocusDrawble != null) {
            drawFocusIndicator(canvas);
            if (!this.mIsFocusing) {
                drawApertureLeaf(canvas);
            }
        }
    }

    private Rect getApertureBallRect() {
        int left;
        int x = (int) getX();
        int y = (int) getY();
        if (x < 0) {
            x = getWidth() / 2;
        }
        if (y < 0) {
            y = getHeight() / 2;
        }
        int focusCircleLeft = Utils.clamp(x - (this.mFocusAreaWidth / 2), 0, getWidth() - this.mFocusAreaWidth);
        int focusCircleTop = Utils.clamp(y - (this.mFocusAreaWidth / 2), 0, getHeight() - this.mFocusAreaWidth);
        if (((this.mFocusAreaWidth + focusCircleLeft) + this.DISTANCE) + this.mApertureBallDrawable.getIntrinsicWidth() <= getWidth()) {
            left = ((this.mFocusAreaWidth + focusCircleLeft) + this.DISTANCE) - (this.mApertureBallDrawable.getIntrinsicWidth() / 2);
        } else {
            left = (focusCircleLeft - this.DISTANCE) - (this.mApertureBallDrawable.getIntrinsicWidth() / 2);
        }
        int top = ((((this.mFocusAreaWidth / 2) + focusCircleTop) - (this.mApertureBallDrawable.getIntrinsicHeight() / 2)) + (this.mTotalLength / 2)) - ((int) this.mBallMovingDistance);
        return new Rect(left, top, this.mApertureBallDrawable.getIntrinsicWidth() + left, this.mApertureBallDrawable.getIntrinsicHeight() + top);
    }

    private boolean needShowWideApertureBall() {
        if (this.mListener != null) {
            return this.mListener.needSupportWideAperture();
        }
        return false;
    }

    private void showApertureBall() {
        if (needShowWideApertureBall()) {
            this.mApertureBallOperationDrawable = this.mApertureBallDrawable;
        } else {
            this.mApertureBallOperationDrawable = null;
        }
    }

    private void doAfterFocusSuccess() {
        this.mFocusDrawble = this.mFocusedDrawable;
        cancelAnimations();
        invalidate();
        this.mIsFocusResultShow = true;
        if (needShowWideApertureBall()) {
            this.mFocusAnimation = this.mFocusedAnimation;
        } else {
            this.mFocusAnimation = this.mFocusedAnimationForAllFocus;
        }
        this.mFocusAnimation.start();
    }

    private void showAutoFocusSuccess() {
        if (!needHideAutoFocusMoveIndicator()) {
            post(new Runnable() {
                public void run() {
                    if (RefocusIndicator.this.mState == 1) {
                        RefocusIndicator.this.mState = 4;
                        RefocusIndicator.this.doAfterFocusSuccess();
                        if (RefocusIndicator.this.needShowWideApertureBall()) {
                            RefocusIndicator.this.updateBallMovingDistance();
                            RefocusIndicator.this.showApertureBall();
                            RefocusIndicator.this.updateReferenceZeroPoint();
                        }
                    }
                }
            });
        }
    }

    private void updateBallMovingDistance() {
        this.mApertureRaito = getLeafRatio();
        this.mBallMovingDistance = ((float) this.mTotalLength) * (WMElement.CAMERASIZEVALUE1B1 - this.mApertureRaito);
    }

    private void updateReferenceZeroPoint() {
        if (this.mApertureBallOperationDrawable != null) {
            this.mReferenceZeroPoint.pointX = (float) getApertureBallRect().centerX();
            this.mReferenceZeroPoint.pointY = (float) (getBounds((int) getX(), (int) getY(), this.mFocusScale).centerY() + (this.mTotalLength / 2));
        }
    }

    private void initFocusedAnimation() {
        registerAnimatorListenr(this.mFocusedAnimation);
        this.mFocusedAnimation.setTarget(this);
        this.mFocusedAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!RefocusIndicator.this.mBallHasBeenTouched && !RefocusIndicator.this.mIsBallOperatingOutOfHotArea) {
                    RefocusIndicator.this.mDisappear.run();
                }
            }
        });
    }

    private void initFocusedAnimationForAllFocus() {
        registerAnimatorListenr(this.mFocusedAnimationForAllFocus);
        this.mFocusedAnimationForAllFocus.setTarget(this);
        this.mFocusedAnimationForAllFocus.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!RefocusIndicator.this.mBallHasBeenTouched && !RefocusIndicator.this.mIsBallOperatingOutOfHotArea) {
                    RefocusIndicator.this.mDisappear.run();
                }
            }
        });
    }

    private void initFocusingAnimation() {
        registerAnimatorListenr(this.mFocusingAnimation);
        this.mFocusingAnimation.setTarget(this);
        this.mFocusingAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animation) {
                RefocusIndicator.this.mIsFocusing = false;
                RefocusIndicator.this.setFocusAlpha(WMElement.CAMERASIZEVALUE1B1);
                RefocusIndicator.this.setFocusScale(WMElement.CAMERASIZEVALUE1B1);
                RefocusIndicator.this.mDisappear.run();
            }

            public void onAnimationEnd(Animator animation) {
                RefocusIndicator.this.mIsFocusing = false;
                RefocusIndicator.this.showAutoFocusSuccess();
            }
        });
    }

    private void initFocusingAnimationForAllFocus() {
        registerAnimatorListenr(this.mFocusingAnimationForAllFocus);
        this.mFocusingAnimationForAllFocus.setTarget(this);
        this.mFocusingAnimationForAllFocus.addListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animation) {
                RefocusIndicator.this.setFocusAlpha(WMElement.CAMERASIZEVALUE1B1);
                RefocusIndicator.this.setFocusScale(WMElement.CAMERASIZEVALUE1B1);
                RefocusIndicator.this.mDisappear.run();
            }

            public void onAnimationEnd(Animator animation) {
                RefocusIndicator.this.showAutoFocusSuccess();
            }
        });
    }

    private void initApertureBallMovingForClickAnimation() {
        registerAnimatorListenr(this.mBallSmoothAnimation);
        this.mBallSmoothAnimation.setTarget(this);
        this.mBallSmoothAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                RefocusIndicator.this.setAperturePara();
                if (RefocusIndicator.this.mListener != null) {
                    RefocusIndicator.this.mListener.onWideApertureValueChanged(RefocusIndicator.this.mWideAperturePara.getValue());
                }
                RefocusIndicator.this.mApertureReshowAnimation.start();
            }
        });
    }

    private void initReshowFoucsAnimation() {
        registerAnimatorListenr(this.mApertureReshowAnimation);
        this.mApertureReshowAnimation.setTarget(this);
        this.mApertureReshowAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                RefocusIndicator.this.setFocusAlpha(WMElement.CAMERASIZEVALUE1B1);
                if (!RefocusIndicator.this.mBallHasBeenTouched && !RefocusIndicator.this.mIsBallOperatingOutOfHotArea) {
                    RefocusIndicator.this.mDisappear.run();
                }
            }
        });
    }

    private void registerAnimatorListenr(AnimatorSet animatorSet) {
        for (Animator animator : animatorSet.getChildAnimations()) {
            if (animator instanceof AnimatorSet) {
                registerAnimatorListenr((AnimatorSet) animator);
            }
            if (animator instanceof ValueAnimator) {
                ((ValueAnimator) animator).addUpdateListener(this.mUpdateListener);
            }
        }
    }
}
