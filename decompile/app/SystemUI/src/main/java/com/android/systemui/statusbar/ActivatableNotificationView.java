package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.notification.FakeShadowView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.utils.HwLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class ActivatableNotificationView extends ExpandableOutlineView {
    private static final Interpolator ACTIVATE_INVERSE_ALPHA_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.5f, 1.0f);
    private static final Interpolator ACTIVATE_INVERSE_INTERPOLATOR = new PathInterpolator(0.6f, 0.0f, 0.5f, 1.0f);
    private boolean mActivated;
    protected float mActiveX;
    protected float mActiveY;
    private float mAnimationTranslationY;
    private float mAppearAnimationFraction = -1.0f;
    private RectF mAppearAnimationRect = new RectF();
    private float mAppearAnimationTranslation;
    private ValueAnimator mAppearAnimator;
    private ObjectAnimator mBackgroundAnimator;
    private ValueAnimator mBackgroundColorAnimator;
    protected NotificationBackgroundView mBackgroundDimmed;
    protected NotificationBackgroundView mBackgroundNormal;
    private AnimatorUpdateListener mBackgroundVisibilityUpdater = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            ActivatableNotificationView.this.setNormalBackgroundVisibilityAmount(ActivatableNotificationView.this.mBackgroundNormal.getAlpha());
        }
    };
    private float mBgAlpha = 1.0f;
    private int mBgTint = 0;
    private boolean mChild;
    private Interpolator mCurrentAlphaInterpolator;
    private Interpolator mCurrentAppearInterpolator;
    private int mCurrentBackgroundTint;
    private boolean mDark;
    private boolean mDimmed;
    private float mDownX;
    private float mDownY;
    private boolean mDrawingAppearAnimation;
    private AnimatorListenerAdapter mFadeInEndListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            ActivatableNotificationView.this.mFadeInFromDarkAnimator = null;
            ActivatableNotificationView.this.updateBackground();
        }
    };
    private ValueAnimator mFadeInFromDarkAnimator;
    private FakeShadowView mFakeShadow;
    private FalsingManager mFalsingManager;
    private boolean mIsBelowSpeedBump;
    private boolean mIsLastChild;
    private final int mLegacyColor;
    private final int mLowPriorityColor;
    private final int mLowPriorityRippleColor;
    private float mNormalBackgroundVisibilityAmount;
    private final int mNormalColor;
    protected final int mNormalRippleColor;
    private OnActivatedListener mOnActivatedListener;
    private float mShadowAlpha = 1.0f;
    private boolean mShowingLegacyBackground;
    private final Interpolator mSlowOutFastInInterpolator;
    private final Interpolator mSlowOutLinearInInterpolator;
    private int mStartTint;
    private final Runnable mTapTimeoutRunnable = new Runnable() {
        public void run() {
            ActivatableNotificationView.this.makeInactive(true);
        }
    };
    private int mTargetTint;
    private final int mTintedRippleColor;
    private final float mTouchSlop;
    private boolean mTrackTouch;
    private AnimatorUpdateListener mUpdateOutlineListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            ActivatableNotificationView.this.updateOutlineAlpha();
        }
    };

    public interface OnActivatedListener {
        void onActivated(ActivatableNotificationView activatableNotificationView);

        void onActivationReset(ActivatableNotificationView activatableNotificationView);
    }

    protected abstract View getContentView();

    public ActivatableNotificationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTouchSlop = (float) ViewConfiguration.get(context).getScaledTouchSlop();
        this.mSlowOutFastInInterpolator = new PathInterpolator(0.8f, 0.0f, 0.6f, 1.0f);
        this.mSlowOutLinearInInterpolator = new PathInterpolator(0.8f, 0.0f, 1.0f, 1.0f);
        setClipChildren(false);
        setClipToPadding(false);
        this.mLegacyColor = context.getColor(R.color.notification_legacy_background_color);
        this.mNormalColor = context.getColor(R.color.notification_material_background_color);
        this.mLowPriorityColor = context.getColor(R.color.notification_material_background_low_priority_color);
        this.mTintedRippleColor = context.getColor(R.color.notification_ripple_tinted_color);
        this.mLowPriorityRippleColor = context.getColor(R.color.notification_ripple_color_low_priority);
        this.mNormalRippleColor = context.getColor(R.color.notification_ripple_untinted_color);
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(R.id.backgroundNormal);
        this.mFakeShadow = (FakeShadowView) findViewById(R.id.fake_shadow);
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(R.id.backgroundDimmed);
        this.mBackgroundNormal.setCustomBackground((int) R.drawable.notification_material_bg);
        this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_material_bg_dim);
        updateBackground();
        updateBackgroundTint();
        updateOutlineAlpha();
    }

    public void setChild(boolean child) {
        this.mChild = child;
    }

    public boolean isChild() {
        return this.mChild;
    }

    public void setIsLastChild(boolean isLast) {
        this.mIsLastChild = isLast;
    }

    public boolean isLastChild() {
        return this.mIsLastChild;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mDimmed && !this.mActivated && ev.getActionMasked() == 0 && disallowSingleClick(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    protected boolean disallowSingleClick(MotionEvent ev) {
        return false;
    }

    protected boolean handleSlideBack() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mDimmed) {
            return super.onTouchEvent(event);
        }
        boolean wasActivated = this.mActivated;
        boolean result = handleTouchEventDimmed(event);
        if (!wasActivated || !result || event.getAction() != 1) {
            return result;
        }
        this.mFalsingManager.onNotificationDoubleTap();
        removeCallbacks(this.mTapTimeoutRunnable);
        return result;
    }

    public void drawableHotspotChanged(float x, float y) {
        if (!this.mDimmed) {
            this.mBackgroundNormal.drawableHotspotChanged(x, y);
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mDimmed) {
            this.mBackgroundDimmed.setState(getDrawableState());
        } else {
            this.mBackgroundNormal.setState(getDrawableState());
        }
    }

    private boolean handleTouchEventDimmed(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                this.mDownX = event.getX();
                this.mDownY = event.getY();
                this.mTrackTouch = true;
                if (this.mDownY > ((float) getActualHeight())) {
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 1:
                if (!isWithinTouchSlop(event)) {
                    makeInactive(true);
                    this.mTrackTouch = false;
                    break;
                } else if (handleSlideBack()) {
                    return true;
                } else {
                    if (!this.mActivated) {
                        this.mActiveX = event.getX();
                        this.mActiveY = event.getY();
                        makeActive();
                        postDelayed(this.mTapTimeoutRunnable, 1200);
                        break;
                    } else if (!performClick()) {
                        return false;
                    }
                }
                break;
            case 2:
                if (!isWithinTouchSlop(event)) {
                    makeInactive(true);
                    this.mTrackTouch = false;
                    break;
                }
                break;
            case 3:
                makeInactive(true);
                this.mTrackTouch = false;
                break;
        }
        return this.mTrackTouch;
    }

    private void makeActive() {
        HwLog.i("ActivatableNotificationView", "makeActive: " + this);
        this.mFalsingManager.onNotificationActive();
        startActivateAnimation(false);
        this.mActivated = true;
        if (this.mOnActivatedListener != null) {
            this.mOnActivatedListener.onActivated(this);
        }
    }

    private void startActivateAnimation(final boolean reverse) {
        float f = 0.0f;
        if (isAttachedToWindow()) {
            Animator animator;
            Interpolator interpolator;
            Interpolator alphaInterpolator;
            int widthHalf = this.mBackgroundNormal.getWidth() / 2;
            int heightHalf = this.mBackgroundNormal.getActualHeight() / 2;
            float radius = (float) Math.sqrt((double) ((widthHalf * widthHalf) + (heightHalf * heightHalf)));
            if (reverse) {
                animator = ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, widthHalf, heightHalf, radius, 0.0f);
            } else {
                animator = ViewAnimationUtils.createCircularReveal(this.mBackgroundNormal, widthHalf, heightHalf, 0.0f, radius);
            }
            this.mBackgroundNormal.setVisibility(0);
            if (reverse) {
                interpolator = ACTIVATE_INVERSE_INTERPOLATOR;
                alphaInterpolator = ACTIVATE_INVERSE_ALPHA_INTERPOLATOR;
            } else {
                interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
                alphaInterpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            }
            animator.setInterpolator(interpolator);
            animator.setDuration(220);
            if (reverse) {
                this.mBackgroundNormal.setAlpha(1.0f);
                animator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ActivatableNotificationView.this.updateBackground();
                    }
                });
                animator.start();
            } else {
                this.mBackgroundNormal.setAlpha(0.4f);
                animator.start();
            }
            ViewPropertyAnimator animate = this.mBackgroundNormal.animate();
            if (!reverse) {
                f = 1.0f;
            }
            animate.alpha(f).setInterpolator(alphaInterpolator).setUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedFraction = animation.getAnimatedFraction();
                    if (reverse) {
                        animatedFraction = 1.0f - animatedFraction;
                    }
                    ActivatableNotificationView.this.setNormalBackgroundVisibilityAmount(animatedFraction);
                }
            }).setDuration(220);
        }
    }

    public void makeInactive(boolean animate) {
        HwLog.i("ActivatableNotificationView", "makeInactive: animate=" + animate + ", " + this);
        if (this.mActivated) {
            this.mActivated = false;
            if (this.mDimmed) {
                if (animate) {
                    startActivateAnimation(true);
                } else {
                    updateBackground();
                }
            }
        }
        if (this.mOnActivatedListener != null) {
            this.mOnActivatedListener.onActivationReset(this);
        }
        removeCallbacks(this.mTapTimeoutRunnable);
    }

    private boolean isWithinTouchSlop(MotionEvent event) {
        if (Math.abs(event.getX() - this.mDownX) >= this.mTouchSlop || Math.abs(event.getY() - this.mDownY) >= this.mTouchSlop) {
            return false;
        }
        return true;
    }

    public void setDimmed(boolean dimmed, boolean fade) {
        if (this.mDimmed != dimmed) {
            this.mDimmed = dimmed;
            resetBackgroundAlpha();
            if (fade) {
                fadeDimmedBackground();
            } else {
                updateBackground();
            }
        }
    }

    public void setDark(boolean dark, boolean fade, long delay) {
        super.setDark(dark, fade, delay);
        if (this.mDark != dark) {
            this.mDark = dark;
            updateBackground();
            if (!(dark || !fade || shouldHideBackground())) {
                fadeInFromDark(delay);
            }
            updateOutlineAlpha();
        }
    }

    public void setOverlap(boolean overlap) {
        if (this.mOverlap != overlap) {
            this.mOverlap = overlap;
            HwLog.i("ActivatableNotificationView", "setOverlap mOverlap:" + this.mOverlap + ", mDimmed:" + this.mDimmed);
            if (this.mDimmed && this.mOverlap) {
                this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_material_bg);
            } else {
                this.mBackgroundDimmed.setCustomBackground((int) R.drawable.notification_material_bg_dim);
            }
        }
    }

    private void updateOutlineAlpha() {
        if (this.mDark) {
            setOutlineAlpha(0.0f);
            return;
        }
        float alpha = (0.7f + (0.3f * this.mNormalBackgroundVisibilityAmount)) * this.mShadowAlpha;
        if (this.mFadeInFromDarkAnimator != null) {
            alpha *= this.mFadeInFromDarkAnimator.getAnimatedFraction();
        }
        setOutlineAlpha(alpha);
    }

    public void setNormalBackgroundVisibilityAmount(float normalBackgroundVisibilityAmount) {
        this.mNormalBackgroundVisibilityAmount = normalBackgroundVisibilityAmount;
        updateOutlineAlpha();
    }

    public void setShowingLegacyBackground(boolean showing) {
        this.mShowingLegacyBackground = showing;
        updateBackgroundTint();
    }

    public void setBelowSpeedBump(boolean below) {
        super.setBelowSpeedBump(below);
        if (below != this.mIsBelowSpeedBump) {
            this.mIsBelowSpeedBump = below;
            updateBackgroundTint();
        }
    }

    public void setTintColor(int color) {
        setTintColor(color, false);
    }

    public void setTintColor(int color, boolean animated) {
        this.mBgTint = color;
        updateBackgroundTint(animated);
    }

    protected void updateBackgroundTint() {
        updateBackgroundTint(false);
    }

    private void updateBackgroundTint(boolean animated) {
        if (this.mBackgroundColorAnimator != null) {
            this.mBackgroundColorAnimator.cancel();
        }
        int rippleColor = getRippleColor();
        this.mBackgroundDimmed.setRippleColor(rippleColor);
        this.mBackgroundNormal.setRippleColor(rippleColor);
        int color = calculateBgColor();
        if (!animated) {
            setBackgroundTintColor(color);
        } else if (color != this.mCurrentBackgroundTint) {
            this.mStartTint = this.mCurrentBackgroundTint;
            this.mTargetTint = color;
            this.mBackgroundColorAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mBackgroundColorAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    ActivatableNotificationView.this.setBackgroundTintColor(NotificationUtils.interpolateColors(ActivatableNotificationView.this.mStartTint, ActivatableNotificationView.this.mTargetTint, animation.getAnimatedFraction()));
                }
            });
            this.mBackgroundColorAnimator.setDuration(360);
            this.mBackgroundColorAnimator.setInterpolator(Interpolators.LINEAR);
            this.mBackgroundColorAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ActivatableNotificationView.this.mBackgroundColorAnimator = null;
                }
            });
            this.mBackgroundColorAnimator.start();
        }
    }

    private void setBackgroundTintColor(int color) {
        this.mCurrentBackgroundTint = color;
        if (color == this.mNormalColor) {
            color = 0;
        }
        this.mBackgroundDimmed.setTint(color);
        this.mBackgroundNormal.setTint(color);
    }

    private void fadeInFromDark(long delay) {
        final View background = this.mDimmed ? this.mBackgroundDimmed : this.mBackgroundNormal;
        background.setAlpha(0.0f);
        this.mBackgroundVisibilityUpdater.onAnimationUpdate(null);
        background.setPivotX(((float) this.mBackgroundDimmed.getWidth()) / 2.0f);
        background.setPivotY(((float) getActualHeight()) / 2.0f);
        background.setScaleX(0.93f);
        background.setScaleY(0.93f);
        background.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(170).setStartDelay(delay).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animation) {
                background.setScaleX(1.0f);
                background.setScaleY(1.0f);
                background.setAlpha(1.0f);
            }
        }).setUpdateListener(this.mBackgroundVisibilityUpdater).start();
        this.mFadeInFromDarkAnimator = TimeAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mFadeInFromDarkAnimator.setDuration(170);
        this.mFadeInFromDarkAnimator.setStartDelay(delay);
        this.mFadeInFromDarkAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        this.mFadeInFromDarkAnimator.addListener(this.mFadeInEndListener);
        this.mFadeInFromDarkAnimator.addUpdateListener(this.mUpdateOutlineListener);
        this.mFadeInFromDarkAnimator.start();
    }

    private void fadeDimmedBackground() {
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
        if (this.mActivated) {
            updateBackground();
            return;
        }
        if (!shouldHideBackground()) {
            if (this.mDimmed) {
                this.mBackgroundDimmed.setVisibility(0);
            } else {
                this.mBackgroundNormal.setVisibility(0);
            }
        }
        float startAlpha = this.mDimmed ? 1.0f : 0.0f;
        float endAlpha = this.mDimmed ? 0.0f : 1.0f;
        int duration = 220;
        if (this.mBackgroundAnimator != null) {
            startAlpha = ((Float) this.mBackgroundAnimator.getAnimatedValue()).floatValue();
            duration = (int) this.mBackgroundAnimator.getCurrentPlayTime();
            this.mBackgroundAnimator.removeAllListeners();
            this.mBackgroundAnimator.cancel();
            if (duration <= 0) {
                updateBackground();
                return;
            }
        }
        this.mBackgroundNormal.setAlpha(startAlpha);
        this.mBackgroundAnimator = ObjectAnimator.ofFloat(this.mBackgroundNormal, View.ALPHA, new float[]{startAlpha, endAlpha});
        this.mBackgroundAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mBackgroundAnimator.setDuration((long) duration);
        this.mBackgroundAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ActivatableNotificationView.this.updateBackground();
                ActivatableNotificationView.this.mBackgroundAnimator = null;
            }
        });
        this.mBackgroundAnimator.addUpdateListener(this.mBackgroundVisibilityUpdater);
        this.mBackgroundAnimator.start();
    }

    protected void updateBackgroundAlpha(float transformationAmount) {
        if (!(isChildInGroup() && this.mDimmed)) {
            transformationAmount = 1.0f;
        }
        this.mBgAlpha = transformationAmount;
        this.mBackgroundDimmed.setAlpha(this.mBgAlpha);
    }

    protected void resetBackgroundAlpha() {
        updateBackgroundAlpha(0.0f);
    }

    protected void updateBackground() {
        int i = 4;
        cancelFadeAnimations();
        if (shouldHideBackground()) {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(4);
        } else if (this.mDimmed) {
            int i2;
            boolean isChildInGroup = isGroupExpansionChanging() ? isChildInGroup() : false;
            NotificationBackgroundView notificationBackgroundView = this.mBackgroundDimmed;
            if (isChildInGroup) {
                i2 = 4;
            } else {
                i2 = 0;
            }
            notificationBackgroundView.setVisibility(i2);
            NotificationBackgroundView notificationBackgroundView2 = this.mBackgroundNormal;
            if (this.mActivated || isChildInGroup) {
                i = 0;
            }
            notificationBackgroundView2.setVisibility(i);
        } else {
            this.mBackgroundDimmed.setVisibility(4);
            this.mBackgroundNormal.setVisibility(0);
            this.mBackgroundNormal.setAlpha(1.0f);
            removeCallbacks(this.mTapTimeoutRunnable);
            makeInactive(false);
        }
        setNormalBackgroundVisibilityAmount(this.mBackgroundNormal.getVisibility() == 0 ? 1.0f : 0.0f);
    }

    protected boolean shouldHideBackground() {
        return this.mDark;
    }

    private void cancelFadeAnimations() {
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
        }
        this.mBackgroundDimmed.animate().cancel();
        this.mBackgroundNormal.animate().cancel();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setPivotX((float) (getWidth() / 2));
    }

    public void setActualHeight(int actualHeight, boolean notifyListeners) {
        super.setActualHeight(actualHeight, notifyListeners);
        setPivotY((float) (actualHeight / 2));
        this.mBackgroundNormal.setActualHeight(actualHeight);
        this.mBackgroundDimmed.setActualHeight(actualHeight);
    }

    public void setClipTopAmount(int clipTopAmount) {
        super.setClipTopAmount(clipTopAmount);
        this.mBackgroundNormal.setClipTopAmount(clipTopAmount);
        this.mBackgroundDimmed.setClipTopAmount(clipTopAmount);
    }

    public void performRemoveAnimation(long duration, float translationDirection, Runnable onFinishedRunnable) {
        enableAppearDrawing(true);
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(false, translationDirection, 0, duration, onFinishedRunnable);
        } else if (onFinishedRunnable != null) {
            onFinishedRunnable.run();
        }
    }

    public void performAddAnimation(long delay, long duration) {
        enableAppearDrawing(true);
        if (this.mDrawingAppearAnimation) {
            startAppearAnimation(true, -1.0f, delay, duration, null);
        }
    }

    private void startAppearAnimation(boolean isAppearing, float translationDirection, long delay, long duration, final Runnable onFinishedRunnable) {
        float targetValue;
        cancelAppearAnimation();
        this.mAnimationTranslationY = ((float) getActualHeight()) * translationDirection;
        if (this.mAppearAnimationFraction == -1.0f) {
            if (isAppearing) {
                this.mAppearAnimationFraction = 0.0f;
                this.mAppearAnimationTranslation = this.mAnimationTranslationY;
            } else {
                this.mAppearAnimationFraction = 1.0f;
                this.mAppearAnimationTranslation = 0.0f;
            }
        }
        if (isAppearing) {
            this.mCurrentAppearInterpolator = this.mSlowOutFastInInterpolator;
            this.mCurrentAlphaInterpolator = Interpolators.LINEAR_OUT_SLOW_IN;
            targetValue = 1.0f;
        } else {
            this.mCurrentAppearInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            this.mCurrentAlphaInterpolator = this.mSlowOutLinearInInterpolator;
            targetValue = 0.0f;
        }
        this.mAppearAnimator = ValueAnimator.ofFloat(new float[]{this.mAppearAnimationFraction, targetValue});
        this.mAppearAnimator.setInterpolator(Interpolators.LINEAR);
        this.mAppearAnimator.setDuration((long) (((float) duration) * Math.abs(this.mAppearAnimationFraction - targetValue)));
        this.mAppearAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ActivatableNotificationView.this.mAppearAnimationFraction = ((Float) animation.getAnimatedValue()).floatValue();
                ActivatableNotificationView.this.updateAppearAnimationAlpha();
                ActivatableNotificationView.this.updateAppearRect();
                ActivatableNotificationView.this.invalidate();
            }
        });
        if (delay > 0) {
            updateAppearAnimationAlpha();
            updateAppearRect();
            this.mAppearAnimator.setStartDelay(delay);
        }
        this.mAppearAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mWasCancelled;

            public void onAnimationEnd(Animator animation) {
                if (onFinishedRunnable != null) {
                    onFinishedRunnable.run();
                }
                if (!this.mWasCancelled) {
                    ActivatableNotificationView.this.enableAppearDrawing(false);
                }
            }

            public void onAnimationStart(Animator animation) {
                this.mWasCancelled = false;
            }

            public void onAnimationCancel(Animator animation) {
                this.mWasCancelled = true;
            }
        });
        this.mAppearAnimator.start();
    }

    private void cancelAppearAnimation() {
        if (this.mAppearAnimator != null) {
            this.mAppearAnimator.cancel();
            this.mAppearAnimator = null;
        }
    }

    public void cancelAppearDrawing() {
        cancelAppearAnimation();
        enableAppearDrawing(false);
    }

    private void updateAppearRect() {
        float bottom;
        float top;
        float inverseFraction = 1.0f - this.mAppearAnimationFraction;
        float translateYTotalAmount = this.mCurrentAppearInterpolator.getInterpolation(inverseFraction) * this.mAnimationTranslationY;
        this.mAppearAnimationTranslation = translateYTotalAmount;
        float left = (((float) getWidth()) * 0.475f) * this.mCurrentAppearInterpolator.getInterpolation(Math.min(1.0f, Math.max(0.0f, (inverseFraction - 0.0f) / 0.8f)));
        float right = ((float) getWidth()) - left;
        float heightFraction = this.mCurrentAppearInterpolator.getInterpolation(Math.max(0.0f, (inverseFraction - 0.0f) / 1.0f));
        int actualHeight = getActualHeight();
        if (this.mAnimationTranslationY > 0.0f) {
            bottom = (((float) actualHeight) - ((this.mAnimationTranslationY * heightFraction) * 0.1f)) - translateYTotalAmount;
            top = bottom * heightFraction;
        } else {
            top = (((((float) actualHeight) + this.mAnimationTranslationY) * heightFraction) * 0.1f) - translateYTotalAmount;
            bottom = (((float) actualHeight) * (1.0f - heightFraction)) + (top * heightFraction);
        }
        this.mAppearAnimationRect.set(left, top, right, bottom);
        setOutlineRect(left, this.mAppearAnimationTranslation + top, right, this.mAppearAnimationTranslation + bottom);
    }

    private void updateAppearAnimationAlpha() {
        setContentAlpha(this.mCurrentAlphaInterpolator.getInterpolation(Math.min(1.0f, this.mAppearAnimationFraction / 1.0f)));
    }

    private void setContentAlpha(float contentAlpha) {
        View contentView = getContentView();
        if (contentView.hasOverlappingRendering()) {
            int layerType;
            if (contentAlpha == 0.0f || contentAlpha == 1.0f) {
                layerType = 0;
            } else {
                layerType = 2;
            }
            if (contentView.getLayerType() != layerType) {
                contentView.setLayerType(layerType, null);
            }
        }
        contentView.setAlpha(contentAlpha);
    }

    public int calculateBgColor() {
        return calculateBgColor(true);
    }

    private int calculateBgColor(boolean withTint) {
        if (withTint && this.mBgTint != 0) {
            return this.mBgTint;
        }
        if (this.mShowingLegacyBackground && !isSingleView()) {
            return this.mLegacyColor;
        }
        if (this.mIsBelowSpeedBump) {
            return this.mLowPriorityColor;
        }
        return this.mNormalColor;
    }

    protected int getRippleColor() {
        if (this.mBgTint != 0) {
            return this.mTintedRippleColor;
        }
        if (this.mShowingLegacyBackground && !isSingleView()) {
            return this.mTintedRippleColor;
        }
        if (this.mIsBelowSpeedBump) {
            return this.mLowPriorityRippleColor;
        }
        return this.mNormalRippleColor;
    }

    public boolean shouldShowNodetails() {
        return false;
    }

    private void enableAppearDrawing(boolean enable) {
        if (enable != this.mDrawingAppearAnimation) {
            this.mDrawingAppearAnimation = enable;
            if (!enable) {
                setContentAlpha(1.0f);
                this.mAppearAnimationFraction = -1.0f;
                setOutlineRect(null);
            }
            invalidate();
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mDrawingAppearAnimation) {
            canvas.save();
            canvas.translate(0.0f, this.mAppearAnimationTranslation);
        }
        super.dispatchDraw(canvas);
        if (this.mDrawingAppearAnimation) {
            canvas.restore();
        }
    }

    public void setOnActivatedListener(OnActivatedListener onActivatedListener) {
        this.mOnActivatedListener = onActivatedListener;
    }

    public void reset() {
        setTintColor(0);
        resetBackgroundAlpha();
        setShowingLegacyBackground(false);
        setBelowSpeedBump(false);
    }

    public float getShadowAlpha() {
        return this.mShadowAlpha;
    }

    public void setShadowAlpha(float shadowAlpha) {
        if (shadowAlpha != this.mShadowAlpha) {
            this.mShadowAlpha = shadowAlpha;
            updateOutlineAlpha();
        }
    }

    public void setFakeShadowIntensity(float shadowIntensity, float outlineAlpha, int shadowYEnd, int outlineTranslation) {
        this.mFakeShadow.setFakeShadowTranslationZ((getTranslationZ() + 0.1f) * shadowIntensity, outlineAlpha, shadowYEnd, outlineTranslation);
    }

    public int getBackgroundColorWithoutTint() {
        return calculateBgColor(false);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        this.mBackgroundDimmed.dump(fd, pw, args);
        this.mBackgroundNormal.dump(fd, pw, args);
    }

    private boolean isSingleView() {
        View view = getContentView();
        if (view == null || !(view instanceof NotificationContentView)) {
            return false;
        }
        return ((NotificationContentView) view).isSingleView();
    }
}
